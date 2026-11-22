package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.repo.ContentRepository;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;

/**
 * Engine tính lại nhãn máy của cả kho đề (BR-GEO-213; D-GEO-004, 105): với mỗi version hiện hành,
 * dựng hình theo mô tả dựng của đề và so đáp số của lời giải kho với giá trị tính. Mỗi lần chạy ghi
 * một báo cáo kiểm chứng mới mang phiên bản engine; nhãn hiển thị là nhãn của báo cáo mới nhất
 * (US-GEO-504-SC6). Cột nhãn của tệp nguồn không bao giờ là đầu vào.
 */
@Service
public class CatalogVerifier {

  /** Kết quả một đề. */
  public record Item(
      String code,
      UUID versionId,
      boolean sceneOk,
      boolean answerOk,
      String status,
      String label) {}

  private final ContentRepository repo;
  private final GeometryEngine engine;
  private final JsonMapper mapper;
  private final String engineVersion;

  public CatalogVerifier(
      ContentRepository repo,
      GeometryEngine engine,
      JsonMapper mapper,
      @Value("${geo.engine.version:engine-1}") String engineVersion) {
    this.repo = repo;
    this.engine = engine;
    this.mapper = mapper;
    this.engineVersion = engineVersion;
  }

  public String engineVersion() {
    return engineVersion;
  }

  /** Chạy trên cả kho với phiên bản engine đang cấu hình. */
  public List<Item> verifyAll() {
    return verify(engineVersion, c -> true);
  }

  /** Chạy trên các đề có mã thỏa {@code codes}, ghi {@code version} vào báo cáo. */
  public List<Item> verify(String version, Predicate<String> codes) {
    List<Item> out = new ArrayList<>();
    for (ContentRepository.CatalogRef r : repo.catalogReference()) {
      if (!codes.test(r.code())) {
        continue;
      }
      String specCode = repo.specCode(r.versionId()).orElse(r.code());
      Optional<ProblemSpec> spec = engine.catalogSpec(specCode);
      boolean sceneOk;
      boolean answerOk;
      Map<String, Object> checks = new LinkedHashMap<>();
      checks.put("engine_version", version);
      checks.put("spec_code", specCode);
      if (spec.isEmpty() || r.finalAnswer() == null || r.finalAnswer().isBlank()) {
        sceneOk = spec.isPresent();
        answerOk = false;
        checks.put("reason", spec.isEmpty() ? "không có mô tả dựng" : "không có đáp số");
      } else {
        GeometryEngine.Verification v = engine.verify(spec.get(), r.finalAnswer());
        sceneOk = v.sceneOk();
        answerOk = v.answerOk();
        checks.put("checks", v.report());
      }
      checks.put("scene_ok", sceneOk);
      checks.put("answer_ok", answerOk);
      String status =
          repo.recordVerification(
              r.versionId(), Uuid7.next(), sceneOk, answerOk, mapper.writeValueAsString(checks));
      out.add(
          new Item(
              r.code(), r.versionId(), sceneOk, answerOk, status, CatalogService.label(status)));
    }
    return out;
  }
}
