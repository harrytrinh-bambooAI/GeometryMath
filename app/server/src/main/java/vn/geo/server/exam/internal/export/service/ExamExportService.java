package vn.geo.server.exam.internal.export.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.export.ExportDoc;
import vn.geo.server.common.export.ExportFile;
import vn.geo.server.common.export.ExportFile.Format;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.exam.internal.service.ExamSetService;

/**
 * Xuất mã đề (US-S5E2-05, dùng chung bộ dựng DOCX/PDF của US-S5E1-05): mỗi mã 4 phần — Đề (không
 * đáp án), Đáp án, Hướng dẫn chấm (cộng đủ điểm), Bảng đặc tả (ô, YCCĐ, mức độ, điểm) theo đầu ra
 * của {@code lap_de.py} (BR-S5E2-12). Một phần = một tệp; nhiều phần = tệp nén. Chỉ giáo viên chủ
 * bộ đề (học sinh FORBIDDEN kể cả sau giờ mở lời giải, BR-S5E2-13; giáo viên khác NOT_FOUND). Bộ đề
 * chưa duyệt có chân trang "Bản nháp – chưa duyệt" ở mọi trang.
 */
@Service
public class ExamExportService {

  public static final String DRAFT_FOOTER = "Bản nháp – chưa duyệt";
  static final List<String> PARTS = List.of("de", "dap-an", "huong-dan-cham", "dac-ta");
  private static final Map<String, String> PART_TITLE =
      Map.of(
          "de", "ĐỀ KIỂM TRA",
          "dap-an", "ĐÁP ÁN",
          "huong-dan-cham", "HƯỚNG DẪN CHẤM",
          "dac-ta", "BẢNG ĐẶC TẢ");

  private final ExamSetService sets;

  public ExamExportService(ExamSetService sets) {
    this.sets = sets;
  }

  public ExportFile export(UUID setId, String code, String format, String parts) {
    Roles.require(Role.TEACHER);
    Format f = Format.parse(format);
    if (f == null) {
      throw AppException.invalidField("format", "Định dạng xuất là docx hoặc pdf.");
    }
    List<String> wanted = parts(parts);
    ObjectNode set = sets.get(setId); // chỉ bộ đề của chính giáo viên, khác: NOT_FOUND
    JsonNode variant = null;
    for (JsonNode v : set.path("variants")) {
      if (v.path("code").asString("").equals(code)) {
        variant = v;
      }
    }
    if (variant == null) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    boolean approved = "approved".equals(set.path("status").asString(""));
    String base = safeName(set.path("matrixCode").asString("de")) + "_" + code;
    List<ExportFile> files = new ArrayList<>();
    for (String p : wanted) {
      files.add(ExportFile.render(document(set, variant, p, approved), f, base + "_" + p));
    }
    return files.size() == 1 ? files.get(0) : ExportFile.zip(base + ".zip", files);
  }

  static List<String> parts(String parts) {
    if (parts == null || parts.isBlank() || "all".equals(parts)) {
      return PARTS;
    }
    Set<String> out = new LinkedHashSet<>();
    for (String p : parts.split(",")) {
      String t = p.trim();
      if (!PARTS.contains(t)) {
        throw AppException.invalidField(
            "parts", "Phần xuất là de, dap-an, huong-dan-cham, dac-ta hoặc all.");
      }
      out.add(t);
    }
    return List.copyOf(out);
  }

  static ExportDoc document(JsonNode set, JsonNode v, String part, boolean approved) {
    String code = v.path("code").asString("");
    ExportDoc.Builder d = ExportDoc.builder(PART_TITLE.get(part) + " · MÃ ĐỀ " + code);
    d.header("Mã đề " + code);
    d.para(set.path("name").asString("") + " (" + set.path("matrixCode").asString("") + ")");
    d.para(
        "Thời gian làm bài: "
            + num(v.path("thoi_gian_phut").doubleValue())
            + " phút · Tổng điểm: "
            + num(v.path("tong_diem").doubleValue()));
    JsonNode qs = v.path("cau");
    switch (part) {
      case "de" -> {
        String phan = null;
        for (JsonNode q : qs) {
          if (!q.path("phan").asString("").equals(phan)) {
            phan = q.path("phan").asString("");
            d.heading("Phần " + phan);
          }
          d.bold(
              "Câu " + q.path("so").asInt() + " (" + num(q.path("diem").doubleValue()) + " điểm).");
          d.para(q.path("de_bai").asString(""));
          if (q.path("phuong_an").isObject()) {
            for (String x : List.of("A", "B", "C", "D")) {
              if (q.path("phuong_an").has(x)) {
                d.para(x + ". " + q.path("phuong_an").path(x).asString(""));
              }
            }
          }
          for (JsonNode y : q.path("y")) {
            d.para(y.path("nhan").asString("") + ") " + y.path("menh_de").asString(""));
          }
        }
      }
      case "dap-an" -> {
        List<List<String>> rows = new ArrayList<>();
        for (JsonNode q : qs) {
          rows.add(
              List.of(
                  String.valueOf(q.path("so").asInt()), q.path("phan").asString(""), answer(q)));
        }
        d.table(List.of("Câu", "Phần", "Đáp án"), rows);
      }
      case "huong-dan-cham" -> {
        List<List<String>> rows = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (JsonNode q : qs) {
          String so = String.valueOf(q.path("so").asInt());
          if (q.path("huong_dan_cham").isArray() && !q.path("huong_dan_cham").isEmpty()) {
            for (JsonNode st : q.path("huong_dan_cham")) {
              BigDecimal p = BigDecimal.valueOf(st.path("diem").doubleValue());
              total = total.add(p);
              rows.add(List.of(so, stepText(st), num(p.doubleValue())));
            }
          } else {
            BigDecimal p = BigDecimal.valueOf(q.path("diem").doubleValue());
            total = total.add(p);
            String how =
                "dung-sai".equals(q.path("loai_cau").asString(""))
                    ? "Đúng/sai từng ý: " + answer(q) + " (chấm theo số ý đúng)"
                    : "Đáp án: " + answer(q);
            rows.add(List.of(so, how, num(p.doubleValue())));
          }
        }
        rows.add(List.of("Tổng", "", num(total.doubleValue())));
        d.table(List.of("Câu", "Nội dung chấm", "Điểm"), rows);
      }
      default -> {
        List<List<String>> rows = new ArrayList<>();
        for (JsonNode q : qs) {
          List<String> yccd = new ArrayList<>();
          q.path("yccd").forEach(x -> yccd.add(x.asString("")));
          rows.add(
              List.of(
                  String.valueOf(q.path("so").asInt()),
                  q.path("o").asString(""),
                  q.path("chu_de").asString(""),
                  q.path("dang_tx").asString(""),
                  String.join(", ", yccd),
                  q.path("muc_do").asString(""),
                  q.path("loai_cau").asString(""),
                  num(q.path("diem").doubleValue())));
        }
        d.table(
            List.of("Câu", "Ô ma trận", "Chủ đề", "Dạng", "YCCĐ", "Mức độ", "Loại câu", "Điểm"),
            rows);
      }
    }
    if (!approved) {
      d.footer(DRAFT_FOOTER);
    }
    return d.build();
  }

  /**
   * Đáp án dạng chữ của một câu (tn4: chữ cái; đúng/sai: từng ý; trả lời ngắn / tự luận: giá trị).
   */
  static String answer(JsonNode q) {
    JsonNode a = q.path("dap_an");
    if ("dung-sai".equals(q.path("loai_cau").asString(""))
        && q.path("y").isArray()
        && !q.path("y").isEmpty()) {
      List<String> out = new ArrayList<>();
      for (JsonNode y : q.path("y")) {
        if (y.has("dung")) {
          out.add(y.path("nhan").asString("") + ") " + (y.path("dung").asBoolean() ? "Đ" : "S"));
        }
      }
      if (!out.isEmpty()) {
        return String.join(", ", out);
      }
    }
    return text(a);
  }

  private static String text(JsonNode a) {
    if (a == null || a.isMissingNode() || a.isNull()) {
      return "";
    }
    if (a.isNumber()) {
      return num(a.doubleValue());
    }
    if (a.isValueNode()) {
      return a.asString("");
    }
    if (a.isArray()) {
      List<String> out = new ArrayList<>();
      a.forEach(x -> out.add(text(x)));
      return String.join("; ", out);
    }
    for (String k : List.of("gia_tri", "dap_so", "noi_dung", "ket_qua")) {
      if (a.has(k)) {
        return text(a.get(k));
      }
    }
    List<String> out = new ArrayList<>();
    a.properties().forEach(e -> out.add(e.getKey() + ": " + text(e.getValue())));
    return String.join(", ", out);
  }

  private static String stepText(JsonNode st) {
    for (String k : List.of("noi_dung", "buoc", "mo_ta", "y")) {
      if (st.hasNonNull(k) && st.get(k).isValueNode()) {
        return st.get(k).asString("");
      }
    }
    return st.toString();
  }

  /** Số gọn: 1 → "1", 0.25 → "0.25". */
  static String num(double x) {
    return BigDecimal.valueOf(Math.round(x * 1e4) / 1e4).stripTrailingZeros().toPlainString();
  }

  private static String safeName(String s) {
    return s.replaceAll("[^A-Za-z0-9._-]", "-");
  }
}
