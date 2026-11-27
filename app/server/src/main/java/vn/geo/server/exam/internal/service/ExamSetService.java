package vn.geo.server.exam.internal.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.exam.api.ExamSetGuard;
import vn.geo.server.exam.internal.domain.Assembler;
import vn.geo.server.exam.internal.domain.Assembler.InsufficientBankException;
import vn.geo.server.exam.internal.domain.ExamModel.Item;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.Replacer;
import vn.geo.server.exam.internal.domain.VariantChecker;
import vn.geo.server.exam.internal.repo.ExamSetRepository;
import vn.geo.server.exam.internal.repo.ExamSetRepository.SetRow;
import vn.geo.server.exam.internal.repo.ExamSetRepository.VariantRow;
import vn.geo.server.exam.internal.service.ExamFormService.FormInput;

/**
 * Bộ đề nhiều mã: lắp (US-S5E2-03), xem, thay câu cùng ô, duyệt vào "Của tôi" (US-S5E2-04). Chỉ
 * giáo viên; bộ đề người khác hoặc lớp người khác là NOT_FOUND. Không gọi AI (BR-S5E2-08).
 */
@Service
public class ExamSetService implements ExamSetGuard {

  public static final String MSG_12 =
      "Ngân hàng chưa đủ câu cho ô %s. Thầy cô bỏ bớt ràng buộc hoặc giảm số mã đề.";
  public static final Map<String, String> HUMAN_LABEL =
      Map.of("may-lap", "Máy lắp", "giao-vien-da-duyet", "Giáo viên đã duyệt");

  private final ExamSetRepository repo;
  private final ExamCatalogService catalog;
  private final ExamFormService form;
  private final JsonMapper mapper;
  private final Clock clock;

  public ExamSetService(
      ExamSetRepository repo,
      ExamCatalogService catalog,
      ExamFormService form,
      JsonMapper mapper,
      Clock clock) {
    this.repo = repo;
    this.catalog = catalog;
    this.form = form;
    this.mapper = mapper;
    this.clock = clock;
  }

  @Transactional
  public ObjectNode create(FormInput in) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    if (in.classId() != null && !repo.ownsClass(p.userId(), in.classId())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    ExamFormService.Shaped shaped = form.shape(in);
    Matrix mt = shaped.matrix();
    long seed = in.seed() != null ? in.seed() : ThreadLocalRandom.current().nextLong(1, 1_000_000);
    Assembler.Result r;
    try {
      r = new Assembler(mt, catalog.snapshot().bank()).assemble(shaped.variantCount(), seed);
    } catch (InsufficientBankException e) {
      throw new AppException(ErrorCode.CONFLICT, MSG_12.formatted(e.cell()));
    }
    UUID id = Uuid7.next();
    String name = in.name() != null && !in.name().isBlank() ? in.name().strip() : mt.name();
    repo.insertSet(
        id,
        p.userId(),
        in.classId(),
        mt.code(),
        name,
        seed,
        mapper.writeValueAsString(in.withSeed(seed)),
        mapper.writeValueAsString(r.report()));
    int pos = 0;
    for (ObjectNode v : r.variants()) {
      repo.insertVariant(
          Uuid7.next(),
          id,
          v.path("ma_de").asString(),
          ++pos,
          v.path("do_kho_du_kien").doubleValue(),
          mapper.writeValueAsString(v));
    }
    return detail(owned(id));
  }

  public ObjectNode get(UUID id) {
    Roles.require(Role.TEACHER);
    return detail(owned(id));
  }

  public List<ObjectNode> list(String shelf) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    String status =
        shelf == null || shelf.isBlank()
            ? null
            : switch (shelf) {
              case "mine" -> "approved";
              case "draft" -> "draft";
              default -> throw AppException.invalidField("shelf", "Ngăn không hợp lệ.");
            };
    List<ObjectNode> out = new ArrayList<>();
    for (SetRow s : repo.listByTeacher(p.userId(), status, 100)) {
      out.add(header(s));
    }
    return out;
  }

  /** Thay câu {@code so} của mã {@code code} bằng câu khác cùng ô (BR-S5E2-09). */
  @Transactional
  public ObjectNode replace(UUID id, String code, int so) {
    return replace(id, code, so, it -> true);
  }

  /** Như trên, có lọc ngân hàng (móc kiểm thử TC-S5E2-18: giả lập ô chỉ còn 1 câu). */
  @Transactional
  public ObjectNode replace(UUID id, String code, int so, Predicate<Item> poolFilter) {
    Roles.require(Role.TEACHER);
    SetRow s = owned(id);
    if (!"draft".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Bộ đề đã duyệt, không thay câu được.");
    }
    List<VariantRow> vs = repo.variants(id);
    VariantRow target =
        vs.stream()
            .filter(v -> v.code().equals(code))
            .findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    ObjectNode body = (ObjectNode) mapper.readTree(target.body());
    if (so < 1 || so > body.path("cau").size()) {
      throw AppException.invalidField("so", "Câu không có trong mã đề.");
    }
    FormInput in = mapper.readValue(s.params(), FormInput.class);
    Matrix mt = form.shape(in).matrix();
    List<Double> others =
        vs.stream().filter(v -> !v.code().equals(code)).map(VariantRow::difficulty).toList();
    ObjectNode next;
    try {
      long seed = s.seed() * 31 + so + clock.millis();
      next = Replacer.replace(mt, catalog.snapshot().bank(), body, so, others, seed, poolFilter);
    } catch (InsufficientBankException e) {
      throw new AppException(ErrorCode.CONFLICT, MSG_12.formatted(e.cell()));
    }
    next.put("ma_de", code);
    repo.updateVariant(
        target.id(), next.path("do_kho_du_kien").doubleValue(), mapper.writeValueAsString(next));
    List<ObjectNode> all = new ArrayList<>();
    for (VariantRow v : repo.variants(id)) {
      all.add((ObjectNode) mapper.readTree(v.body()));
    }
    ObjectNode report = new Assembler(mt, List.of()).report(all, s.seed());
    repo.updateReport(id, mapper.writeValueAsString(report));
    return detail(owned(id));
  }

  @Transactional
  public ObjectNode approve(UUID id) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    SetRow s = owned(id);
    if ("approved".equals(s.status())) {
      return detail(s);
    }
    repo.approve(id, p.userId(), clock.instant());
    return detail(owned(id));
  }

  @Override
  public void requireAssignable(UUID examSetId) {
    SetRow s = owned(examSetId);
    if (!"approved".equals(s.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Bộ đề chưa duyệt, chưa giao được.");
    }
  }

  private SetRow owned(UUID id) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    return repo.findOwned(id, p.userId()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  public long countMine() {
    return repo.countByTeacher(Roles.require(Role.TEACHER).userId());
  }

  private ObjectNode header(SetRow s) {
    ObjectNode n = mapper.createObjectNode();
    n.put("id", s.id().toString());
    n.put("name", s.name());
    n.put("matrixCode", s.matrixCode());
    n.put("classId", s.classId() == null ? null : s.classId().toString());
    n.put("seed", s.seed());
    n.put("status", s.status());
    n.put("humanLabelCode", s.humanLabel());
    n.put("humanLabel", HUMAN_LABEL.get(s.humanLabel()));
    n.put("approvedBy", s.approvedBy() == null ? null : s.approvedBy().toString());
    n.put("approvedAt", s.approvedAt() == null ? null : s.approvedAt().toString());
    n.put("createdAt", s.createdAt().toString());
    return n;
  }

  private ObjectNode detail(SetRow s) {
    ObjectNode n = header(s);
    n.set("params", mapper.readTree(s.params()));
    n.set("report", mapper.readTree(s.report()));
    var arr = n.putArray("variants");
    for (VariantRow v : repo.variants(s.id())) {
      JsonNode body = mapper.readTree(v.body());
      ObjectNode o = arr.addObject();
      o.put("code", v.code());
      o.put("position", v.position());
      o.put("difficulty", v.difficulty());
      o.set("cau", body.path("cau"));
      o.set("rang_buoc", body.path("rang_buoc"));
      o.put("thoi_gian_phut", body.path("thoi_gian_phut").doubleValue());
      o.put("tong_diem", body.path("tong_diem").doubleValue());
    }
    List<Double> ds = repo.variants(s.id()).stream().map(VariantRow::difficulty).toList();
    n.put("difficultySpread", Math.round(VariantChecker.spread(ds) * 1e4) / 1e4);
    return n;
  }

  /** Nhãn máy của một câu (theo từng câu): câu ngân hàng đã qua kiểm đáp số → "Đã kiểm chứng". */
  public static Map<String, String> machineLabels() {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("da-kiem-chung", "Đã kiểm chứng");
    m.put("can-xem-lai", "Cần xem lại");
    return m;
  }
}
