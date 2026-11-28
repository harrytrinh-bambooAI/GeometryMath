package vn.geo.server.aigrade.internal.proposal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * Tin nhắn task {@code grade} (BR-S5E6-01): đề, đáp số engine, rubric phiên bản, bản đọc chữ phiên
 * bản mới nhất, ảnh trang. KHÔNG có tên, mã người dùng, mã lớp, mã bài nộp (NFR-GEO-071). Đầu ra
 * JSON: {@code {kind:"grade", tong, hinh, criteria[{ma, diem, ly_do, vung{trang,x,y,w,h}, tin_cay,
 * loi, kien_thuc[{ma, ten}]}], buoc[{ma, tieu_chi, diem, dung, phu_thuoc[], dua_tren_hinh}]}} hoặc
 * {@code {kind:"refuse", reason}}.
 */
public final class GradePrompt {

  /** Tiền tố để giả lập nhận biết task chấm của S5-E6 (mốc T-261128-61 của MockAiProvider). */
  public static final String MARK = "[s5e6-grade]";

  /** Phiên bản tin nhắn task grade (ghi vào đề xuất cùng phiên bản prompt của cổng AI). */
  public static final String VERSION = "grade-v1";

  private GradePrompt() {}

  /** Dòng chữ của bản đọc chữ (mỗi đoạn một dòng, theo trang). */
  public static List<String> lines(JsonNode pages) {
    List<String> out = new ArrayList<>();
    for (JsonNode p : pages) {
      for (JsonNode s : p.path("segments")) {
        out.add(s.path("text").asString(""));
      }
    }
    return out;
  }

  public static String build(
      String statement,
      String engineAnswer,
      List<GradeRules.Criterion> rubric,
      BigDecimal scale,
      int ocrVersion,
      JsonNode pages,
      List<byte[]> images) {
    StringBuilder b = new StringBuilder(MARK);
    b.append(" Chấm bài làm hình học theo rubric. Chỉ đề xuất điểm, giáo viên quyết định.\n");
    b.append("Đề: ").append(statement == null ? "(không có)" : statement).append('\n');
    b.append("Đáp số engine: ")
        .append(engineAnswer == null ? "(không có)" : engineAnswer)
        .append('\n');
    b.append("Rubric (thang ").append(scale.stripTrailingZeros().toPlainString()).append("):\n");
    for (GradeRules.Criterion c : rubric) {
      b.append("C")
          .append(c.ord())
          .append(' ')
          .append(c.name())
          .append(" (tối đa ")
          .append(c.max().stripTrailingZeros().toPlainString())
          .append(c.directionDependent() ? "; phụ thuộc hướng" : "")
          .append(")\n");
    }
    b.append("Bản đọc chữ (phiên bản ").append(ocrVersion).append("):\n");
    for (JsonNode p : pages) {
      b.append("Trang ").append(p.path("pageNo").asInt()).append(":\n");
      for (JsonNode s : p.path("segments")) {
        b.append("- ").append(s.path("text").asString("")).append('\n');
      }
    }
    b.append(
        "Trả JSON: criteria (ma C1…Cn, diem bội 0,25 trong [0, tối đa], ly_do ≤ 300 ký tự, vung"
            + " {trang,x,y,w,h}, tin_cay 0–1, loi thuộc sai-dinh-li | sai-tinh-toan | sai-hinh-dung |"
            + " nham-dieu-kien, kien_thuc [{ma KT…, ten}]), buoc [{ma, tieu_chi, diem, dung,"
            + " phu_thuoc[], dua_tren_hinh}], hinh (đúng | không có hình | hình sai), tong; hoặc"
            + " {kind: refuse, reason} khi không đọc được.\n");
    for (int i = 0; i < images.size(); i++) {
      byte[] img = images.get(i);
      if (img == null) {
        continue;
      }
      String type = img.length > 1 && (img[0] & 0xFF) == 0x89 ? "image/png" : "image/jpeg";
      b.append("Ảnh trang ")
          .append(i + 1)
          .append(": data:")
          .append(type)
          .append(";base64,")
          .append(Base64.getEncoder().encodeToString(img))
          .append('\n');
    }
    return b.toString();
  }
}
