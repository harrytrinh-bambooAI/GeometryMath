package vn.geo.server.exam.internal.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.exam.internal.domain.DatasetValidator;
import vn.geo.server.exam.internal.domain.ExamDataset;
import vn.geo.server.exam.internal.repo.ExamBankRepository;

/**
 * Lệnh nạp dữ liệu đề có kiểm (US-S5E2-01): đọc {@code materials/de-thi} (chỉ đọc), kiểm schema,
 * đáp số engine (1e-6), ma trận; ghi phần đạt theo phiên bản dữ liệu, lũy đẳng theo băm nội dung
 * ("0 thêm, 0 đổi" khi chạy lại), báo cáo từng bản ghi bị loại.
 */
@Service
public class ExamImportService {

  /** Mã cố định của ma trận theo dữ liệu mẫu chung (fixture-ids.d/s5.json, F-S5-030, 031). */
  static final Map<String, UUID> FIXED_IDS =
      Map.of(
          "KT15-HHKG11", UUID.fromString("00000000-0000-7000-8005-000000000030"),
          "TN-THPT-764", UUID.fromString("00000000-0000-7000-8005-000000000031"));

  public record Issue(String kind, String id, String message) {}

  public record Report(
      String version,
      int matrices,
      int templates,
      int items,
      int errors,
      int added,
      int changed,
      int unchanged,
      int removed,
      List<Issue> issues,
      Map<String, Long> stored,
      String summary) {}

  private final ExamBankRepository repo;
  private final JsonMapper mapper;
  private final TransactionTemplate tx;
  private final ExamCatalogService catalog;
  private final String dataDir;
  private final String dataVersion;

  public ExamImportService(
      ExamBankRepository repo,
      JsonMapper mapper,
      TransactionTemplate tx,
      ExamCatalogService catalog,
      @Value("${geo.s5.exam.data-dir:artifacts/GEO/materials/de-thi}") String dataDir,
      @Value("${geo.s5.exam.data-version:de-thi v1.0}") String dataVersion) {
    this.repo = repo;
    this.mapper = mapper;
    this.tx = tx;
    this.catalog = catalog;
    this.dataDir = dataDir;
    this.dataVersion = dataVersion;
  }

  public String defaultVersion() {
    return dataVersion;
  }

  /**
   * Thư mục dữ liệu: đường dẫn tuyệt đối, hoặc tương đối tìm từ thư mục chạy lên các thư mục cha.
   */
  public Path dataPath() {
    Path p = Path.of(dataDir);
    if (p.isAbsolute() || Files.isDirectory(p)) {
      return p;
    }
    for (Path d = Path.of("").toAbsolutePath(); d != null; d = d.getParent()) {
      if (Files.isDirectory(d.resolve(dataDir))) {
        return d.resolve(dataDir);
      }
    }
    throw new IllegalStateException("không thấy thư mục dữ liệu đề " + dataDir);
  }

  public ExamDataset loadDataset() {
    return ExamDataset.load(dataPath(), mapper);
  }

  /** Nạp thật (commit). */
  public synchronized Report run(ExamDataset d, String version) {
    Report r = tx.execute(s -> persist(d, version));
    catalog.invalidate();
    return r;
  }

  /**
   * Nạp trong giao dịch rồi HOÀN TÁC (móc kiểm thử TC-S5E2-01, 03, 04: không đổi dữ liệu dùng
   * chung); {@code wipe} = xóa dữ liệu đề trước (giả lập CSDL chưa có dữ liệu đề); {@code probe}
   * đọc trạng thái sau nạp trong cùng giao dịch.
   */
  public synchronized Report dryRun(
      ExamDataset d, String version, boolean wipe, Consumer<Map<String, Object>> probe) {
    Report r =
        tx.execute(s -> {
          if (wipe) {
            repo.deleteAll();
          }
          Report out = persist(d, version);
          Map<String, Object> seen = new HashMap<>();
          for (Issue i : out.issues()) {
            if ("cau".equals(i.kind())) {
              seen.put("itemStored:" + i.id(), repo.itemExists(i.id()));
            }
          }
          probe.accept(seen);
          s.setRollbackOnly();
          return out;
        });
    catalog.invalidate();
    return r;
  }

  private Report persist(ExamDataset d, String version) {
    DatasetValidator.Result v = DatasetValidator.validate(d);
    List<Issue> issues =
        v.issues().stream().map(i -> new Issue(i.kind(), i.id(), i.message())).toList();
    int[] diff = new int[4]; // thêm, đổi, giữ, xóa
    List<String> hashes = new ArrayList<>();
    Map<String, String> old = repo.hashes("exam_matrix");
    List<Map<String, Object>> rows = new ArrayList<>();
    for (ObjectNode m : v.matrices()) {
      String code = m.path("id").asString();
      String h = hash(m);
      hashes.add(h);
      if (count(diff, old.get(code), h)) {
        Map<String, Object> row = new HashMap<>();
        row.put("code", code);
        row.put(
            "id",
            FIXED_IDS.getOrDefault(
                code,
                UUID.nameUUIDFromBytes(("exam_matrix:" + code).getBytes(StandardCharsets.UTF_8))));
        row.put("version", version);
        row.put("name", m.path("ten").asString());
        row.put("kind", m.path("loai_de").asString());
        row.put("grade", m.path("lop").asInt());
        row.put("minutes", m.path("thoi_gian_phut").doubleValue());
        row.put("total", m.path("tong_diem").doubleValue());
        row.put("body", mapper.writeValueAsString(m));
        row.put("hash", h);
        rows.add(row);
      }
    }
    repo.upsertVersion(version, "pending", meta(d), "{}");
    repo.upsertMatrices(rows);
    diff[3] += repo.deleteMissing("exam_matrix", codes(v.matrices()));
    old = repo.hashes("exam_item_template");
    rows = new ArrayList<>();
    for (ObjectNode t : v.templates()) {
      String h = hash(t);
      hashes.add(h);
      if (count(diff, old.get(t.path("id").asString()), h)) {
        Map<String, Object> row = new HashMap<>();
        row.put("code", t.path("id").asString());
        row.put("version", version);
        row.put("topic", t.path("chu_de").asString());
        row.put("body", mapper.writeValueAsString(t));
        row.put("hash", h);
        rows.add(row);
      }
    }
    repo.upsertTemplates(rows);
    diff[3] += repo.deleteMissing("exam_item_template", codes(v.templates()));
    old = repo.hashes("exam_bank_item");
    rows = new ArrayList<>();
    for (ObjectNode it : v.items()) {
      String h = hash(it);
      hashes.add(h);
      if (count(diff, old.get(it.path("id").asString()), h)) {
        Map<String, Object> row = new HashMap<>();
        row.put("code", it.path("id").asString());
        row.put("version", version);
        row.put("template", it.path("mau_cau").asString());
        row.put("topic", it.path("chu_de").asString());
        row.put("type", it.path("loai_cau").asString());
        row.put("level", it.path("muc_do").asString());
        row.put("body", mapper.writeValueAsString(it));
        row.put("hash", h);
        rows.add(row);
      }
    }
    repo.upsertItems(rows);
    diff[3] += repo.deleteMissing("exam_bank_item", codes(v.items()));
    String summary =
        v.matrices().size()
            + " ma trận, "
            + v.templates().size()
            + " mẫu, "
            + v.items().size()
            + " câu, "
            + issues.size()
            + " lỗi; "
            + diff[0]
            + " thêm, "
            + diff[1]
            + " đổi";
    Report r =
        new Report(
            version,
            v.matrices().size(),
            v.templates().size(),
            v.items().size(),
            issues.size(),
            diff[0],
            diff[1],
            diff[2],
            diff[3],
            issues,
            repo.counts(),
            summary);
    repo.upsertVersion(
        version, hash(String.join(",", hashes)), meta(d), mapper.writeValueAsString(r));
    return r;
  }

  /** Đếm thêm/đổi/giữ; true khi cần ghi. */
  private static boolean count(int[] diff, String oldHash, String h) {
    if (oldHash == null) {
      diff[0]++;
      return true;
    }
    if (!oldHash.equals(h)) {
      diff[1]++;
      return true;
    }
    diff[2]++;
    return false;
  }

  private String meta(ExamDataset d) {
    Map<String, Object> topics = new LinkedHashMap<>();
    d.topics
        .values()
        .forEach(t ->
            topics.put(t.id(), Map.of("ten", t.name(), "lop", t.grade() == null ? 0 : t.grade())));
    return mapper.writeValueAsString(Map.of("topics", topics));
  }

  private static List<String> codes(List<ObjectNode> ns) {
    return ns.stream().map(n -> n.path("id").asString()).toList();
  }

  private String hash(ObjectNode n) {
    return hash(mapper.writeValueAsString(n));
  }

  private static String hash(String s) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
