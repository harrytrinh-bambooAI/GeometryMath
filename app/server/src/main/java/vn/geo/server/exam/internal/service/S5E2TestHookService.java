package vn.geo.server.exam.internal.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.exam.internal.domain.ExamDataset;

/**
 * Móc kiểm thử S5-E2 (dev/test): chạy lệnh nạp trên dữ liệu thật hoặc bản sao cài lỗi, trong giao
 * dịch hoàn tác (không đổi ngân hàng dùng chung của các ca khác).
 */
@Service
@Profile({"dev", "test"})
public class S5E2TestHookService {

  /** Đáp số engine của câu mẫu F-S5-050 (a√6/3 tại a = 1). */
  static final double ENGINE_A6_3 = Math.sqrt(6) / 3;

  private final ExamImportService importer;

  public S5E2TestHookService(ExamImportService importer) {
    this.importer = importer;
  }

  public Map<String, Object> importData(String mode) {
    ExamDataset d = importer.loadDataset();
    String v = importer.defaultVersion();
    Map<String, Object> out = new LinkedHashMap<>();
    switch (mode) {
      case "commit" -> out.put("report", importer.run(d, v));
      case "fresh" -> out.put("report", importer.dryRun(d, v, true, out::putAll));
      case "bad-answer" -> {
        ObjectNode it = corruptAnswer(d, 0.82);
        out.put("badItem", it.path("id").asString());
        out.put("report", importer.dryRun(d, v, true, out::putAll));
      }
      case "bad-matrix" -> {
        corruptMatrixTotal(d, "KT15-HHKG11", 1.5);
        out.put("report", importer.dryRun(d, v, true, out::putAll));
      }
      default -> throw new IllegalArgumentException("mode");
    }
    return out;
  }

  /**
   * Sửa đáp số một câu HHKG nguồn MVP (ưu tiên câu có engine = a√6/3 ≈ 0,8165) thành {@code value}.
   */
  public static ObjectNode corruptAnswer(ExamDataset d, double value) {
    ObjectNode pick = null;
    for (ObjectNode it : d.items) {
      if (!"mvp".equals(it.path("nguon").path("loai").asString())
          || "dung-sai".equals(it.path("loai_cau").asString())
          || !it.path("chu_de").asString().startsWith("hinh-khong-gian")) {
        continue;
      }
      Double e = d.engineValues.get(it.path("id").asString());
      if (pick == null) {
        pick = it;
      }
      if (e != null && Math.abs(e - ENGINE_A6_3) < 1e-6) {
        pick = it;
        break;
      }
    }
    if (pick == null) {
      throw new IllegalStateException("không có câu HHKG nguồn MVP");
    }
    ((ObjectNode) pick.path("dap_so")).put("so", value);
    return pick;
  }

  /** Đổi điểm mỗi câu của phần trả lời ngắn để tổng điểm ma trận thành 9 (6 × 1 + 2 × 1,5). */
  public static void corruptMatrixTotal(ExamDataset d, String code, double shortAnswerPoints) {
    for (ObjectNode m : d.matrices) {
      if (!m.path("id").asString().equals(code)) {
        continue;
      }
      for (JsonNode c : m.path("cells")) {
        if ("tra-loi-ngan".equals(c.path("loai_cau").asString())) {
          ((ObjectNode) c).put("diem", shortAnswerPoints);
        }
      }
    }
  }
}
