package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.repo.ContentRepository;

/**
 * Nạp gói nội dung Kho đề MVP ({@link ContentPack}) vào kho nền tảng (T-261126-02): đề chưa có thì
 * tạo; đề đã có theo mã thì GIỮ NGUYÊN (không đổi trạng thái lưu trữ, không ghi version mới, kể cả
 * khi quản trị đã sửa tay hoặc nội dung gói khác; FIX5). Sau đó engine tính lại nhãn máy của các đề
 * trong gói ({@link CatalogVerifier}, BR-GEO-213): nhãn "Đã kiểm chứng" chỉ có khi hình dựng thỏa
 * ràng buộc và đáp số khớp giá trị tính.
 */
@Service
public class ContentPackLoader {

  /** Mô hình/phiên bản ghi kèm lời giải soạn sẵn của gói (cột solution.model, prompt_version). */
  static final String MODEL = "soan-san-mvp";

  /** Kết quả nạp: mã đề của gói, trạng thái máy theo mã, số đề tạo mới, số đề đã có nên bỏ qua. */
  public record Result(List<String> codes, Map<String, String> statuses, int written, int skipped) {

    public long verified() {
      return statuses.values().stream().filter("verified"::equals).count();
    }
  }

  private final ContentRepository repo;
  private final CatalogVerifier verifier;
  private final JsonMapper mapper;

  public ContentPackLoader(ContentRepository repo, CatalogVerifier verifier, JsonMapper mapper) {
    this.repo = repo;
    this.verifier = verifier;
    this.mapper = mapper;
  }

  @Transactional
  public Result load() {
    ContentPack pack = ContentPack.read(mapper);
    // một lần nạp tại một thời điểm (nhiều máy chủ khởi động cùng lúc)
    repo.lockBank(pack.bank());
    UUID author = repo.platformAuthor();
    List<String> codes = new ArrayList<>();
    int written = 0;
    int skipped = 0;
    for (ContentPack.Problem p : pack.problems()) {
      if (insertIfAbsent(pack, p, author)) {
        written++;
      } else {
        skipped++;
      }
      codes.add(p.code());
    }
    Set<String> mine = Set.copyOf(codes);
    Map<String, String> statuses = new LinkedHashMap<>();
    for (CatalogVerifier.Item it : verifier.verify(verifier.engineVersion(), mine::contains)) {
      statuses.put(it.code(), it.status());
    }
    return new Result(List.copyOf(codes), Map.copyOf(statuses), written, skipped);
  }

  /** Chỉ THÊM đề chưa có (theo mã); true khi tạo đề mới, false khi đề đã có (bỏ qua hoàn toàn). */
  private boolean insertIfAbsent(ContentPack pack, ContentPack.Problem p, UUID author) {
    if (repo.problemByCode(p.code()).isPresent()) {
      return false;
    }
    String stepsJson = p.stepsJson(mapper);
    UUID problemId = Uuid7.next();
    int version = 1;
    Short topic =
        repo.topicId(p.topic())
            .orElseThrow(() -> new IllegalStateException("dạng bài không có: " + p.topic()));
    if ("main".equals(pack.bank())) {
      repo.insertProblem(problemId, p.code(), p.level(), p.solid(), topic, author);
    } else {
      repo.insertProblem(problemId, p.code(), p.level(), p.solid(), topic, author, pack.bank());
    }
    UUID versionId = Uuid7.next();
    repo.insertVersion(
        versionId,
        problemId,
        version,
        p.statement(),
        CatalogImporter.statementHash(p.statement()),
        author);
    for (int i = 0; i < p.hints().size(); i++) {
      repo.insertHint(versionId, i + 1, p.hints().get(i));
    }
    repo.putSolution(
        versionId,
        p.answer(),
        stepsJson,
        mapper.writeValueAsString(AnswerExpr.forms(AnswerExpr.parse(p.answer()))),
        MODEL,
        "pack-v" + pack.version());
    return true;
  }
}
