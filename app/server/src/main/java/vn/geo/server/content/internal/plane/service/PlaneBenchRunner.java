package vn.geo.server.content.internal.plane.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.content.internal.plane.repo.PlaneRepository;
import vn.geo.server.geometry.api.GeometryEngine;

/**
 * Chạy {@code make bench-s2} trên bộ đề chuẩn hình phẳng ĐÃ NẠP (US-S2E2-05): mỗi đề đi qua khung
 * môn học (dựng hình, giải, nhãn máy do engine gắn), so đáp số với tham chiếu rồi tính ba chỉ số
 * ({@link PlaneBenchCalc}). Chỉ chạy với AI giả lập: cấu hình có nhà cung cấp AI thật hoặc khóa AI
 * thì từ chối trước khi làm gì (BR-S2E2-17, không gọi mạng). Tất định: hai lần chạy cùng bộ cho
 * cùng chỉ số và danh sách đề rớt.
 */
@Service
public class PlaneBenchRunner {

  public static final String MOCK_ONLY = "chỉ chạy với AI giả lập";

  /** Khóa giả của profile dev/test (application-test.yml), không gọi được AI thật. */
  static final String PLACEHOLDER_KEY = "KHOA-THU-KHONG-LO";

  private static final DateTimeFormatter VN = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");

  private final PlaneRepository repo;
  private final PlaneBenchmarkLoader loader;
  private final JsonMapper mapper;
  private final String provider;
  private final String apiKey;

  public PlaneBenchRunner(
      PlaneRepository repo,
      PlaneBenchmarkLoader loader,
      JsonMapper mapper,
      @Value("${geo.ai.provider:mock}") String provider,
      @Value("${geo.ai.api-key:}") String apiKey) {
    this.repo = repo;
    this.loader = loader;
    this.mapper = mapper;
    this.provider = provider;
    this.apiKey = apiKey;
  }

  /** Từ chối chạy khi có AI thật (BR-S2E2-17). */
  public static class RealAiRefused extends RuntimeException {
    public RealAiRefused() {
      super(MOCK_ONLY);
    }
  }

  /** Chạy theo cấu hình hiện tại và biến môi trường GEO_AI_API_KEY. */
  public PlaneBenchCalc.Report run() {
    // Khóa thật chỉ đến từ biến môi trường GEO_AI_API_KEY; profile dev/test đặt sẵn khóa giả
    // "KHOA-THU-KHONG-LO" (kiểm che khóa trong nhật ký) — không phải AI thật.
    String envKey = System.getenv("GEO_AI_API_KEY");
    boolean placeholder = apiKey == null || apiKey.isBlank() || PLACEHOLDER_KEY.equals(apiKey);
    return run(provider, envKey != null && !envKey.isBlank() ? envKey : placeholder ? "" : apiKey);
  }

  /** Chạy với nhà cung cấp và khóa cho trước (kiểm thử FC2). */
  public PlaneBenchCalc.Report run(String aiProvider, String aiKey) {
    if (!"mock".equals(aiProvider) || (aiKey != null && !aiKey.isBlank())) {
      throw new RealAiRefused();
    }
    List<PlaneBenchCalc.Item> items = new ArrayList<>();
    for (PlaneRepository.BenchRow r : repo.benchmark()) {
      PlaneBenchmarkLoader.Evaluated e =
          loader.evaluate(r.statement(), mapper.readTree(r.spec()), r.answer());
      boolean sceneOk =
          e.rendering().built() && GeometryEngine.LABEL_VERIFIED.equals(e.rendering().label());
      boolean labeled = GeometryEngine.LABEL_VERIFIED.equals(e.solution().label());
      items.add(new PlaneBenchCalc.Item(r.code(), sceneOk, e.answerOk(), labeled));
    }
    return PlaneBenchCalc.evaluate(
        items, OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(VN));
  }
}
