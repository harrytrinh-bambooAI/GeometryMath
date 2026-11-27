package vn.geo.server.lessonplan.internal.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Xử lý tất định phía máy chủ đi kèm prompt V8 (prompt-final.md v1.1 §4; chuyển từ {@code
 * score.py:fix_keys/auto/server_check}): parse JSON nghiêm ngặt, chuẩn hoá tên khoá (đếm {@code
 * fix_keys}), kiểm luật A (BR-S5E1-06).
 */
public final class LessonCheck {

  /** Bí danh tiếng Anh của bốn bước tổ chức (V8) → khoá lưu. */
  static final Map<String, String> ALIAS =
      Map.of(
          "assign", "chuyen_giao",
          "perform", "thuc_hien",
          "report", "bao_cao",
          "conclude", "ket_luan");

  static final List<String> STEPS = List.of("chuyen_giao", "thuc_hien", "bao_cao", "ket_luan");
  static final List<String> KINDS = List.of("mo-dau", "hinh-thanh", "luyen-tap", "van-dung");

  /**
   * Kết quả kiểm một đầu ra.
   *
   * @param node đầu ra đã chuẩn hoá khoá (null = không parse được)
   * @param fixKeys số khoá phải sửa (có dấu / khoảng trắng; đổi bí danh không tính)
   * @param errors lỗi kiểm máy chủ (rỗng = đạt)
   * @param parseError true = JSON hỏng (gọi lại với cùng đầu vào, §4.1)
   */
  public record Result(ObjectNode node, int fixKeys, List<String> errors, boolean parseError) {
    public boolean ok() {
      return errors.isEmpty();
    }

    public String kind() {
      return node == null ? null : node.path("kind").asString("");
    }
  }

  private final JsonMapper mapper;

  public LessonCheck(JsonMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Kiểm đầu ra của task {@code lesson_plan}.
   *
   * @param soTiet số tiết của yêu cầu (Σ phút trên lớp = soTiet × 45)
   * @param allowedKt mã kiến thức đã cấp
   * @param bank mã bài tập trong kho đã cấp
   */
  public Result check(String text, int soTiet, Set<String> allowedKt, Set<String> bank) {
    JsonNode raw;
    try {
      raw = mapper.readTree(strip(text));
    } catch (RuntimeException e) {
      return new Result(null, 0, List.of("JSON sai định dạng: không parse được"), true);
    }
    if (!(raw instanceof ObjectNode obj)) {
      return new Result(null, 0, List.of("JSON sai định dạng: không phải một đối tượng"), true);
    }
    int[] fixed = {0};
    ObjectNode node = (ObjectNode) normalize(obj, fixed);
    List<String> errs = new ArrayList<>();
    String kind = node.path("kind").asString("");
    switch (kind) {
      case "lesson" -> checkLesson(node, soTiet, allowedKt, bank, errs);
      case "question", "reply" -> {
        if (node.path("text").asString("").isBlank()) {
          errs.add("JSON sai định dạng: thiếu \"text\"");
        }
      }
      default -> errs.add("JSON sai định dạng: kind \"" + kind + "\" không hợp với soạn mới");
    }
    return new Result(node, fixed[0], errs, false);
  }

  private static void checkLesson(
      ObjectNode o, int soTiet, Set<String> allowedKt, Set<String> bank, List<String> errs) {
    JsonNode acts = o.path("hoat_dong");
    if (!acts.isArray() || acts.isEmpty()) {
      errs.add("A4: thiếu \"hoat_dong\"");
      return;
    }
    int rank = -1;
    boolean order = true;
    Set<String> seen = new java.util.HashSet<>();
    int minutes = 0;
    for (JsonNode a : acts) {
      String ma = a.path("ma").asString("?");
      int r = KINDS.indexOf(a.path("loai").asString(""));
      if (r < 0 || r < rank) {
        order = false;
      }
      rank = Math.max(rank, r);
      seen.add(a.path("loai").asString(""));
      for (String s : STEPS) {
        if (a.path("to_chuc").path(s).asString("").isBlank()) {
          errs.add("A4: " + ma + " thiếu bước tổ chức \"" + s + "\"");
        }
      }
      int m = a.path("thoi_luong_phut").asInt(0);
      if (a.path("o_nha").asBoolean(false)) {
        if (m != 0) {
          errs.add("A5: " + ma + " làm ở nhà phải 0 phút");
        }
      } else {
        minutes += m;
      }
      for (JsonNode k : a.path("kien_thuc")) {
        kt(k.path("kt").asString(""), allowedKt, errs);
      }
    }
    if (!order || !seen.containsAll(KINDS)) {
      errs.add("A4: hoạt động phải đủ và đúng thứ tự Mở đầu → Hình thành → Luyện tập → Vận dụng");
    }
    if (minutes != soTiet * 45) {
      errs.add("A5: tổng phút " + minutes + " ≠ " + soTiet * 45);
    }
    for (JsonNode k : o.path("muc_tieu").path("kien_thuc")) {
      for (JsonNode c : k.path("kt")) {
        kt(c.asString(""), allowedKt, errs);
      }
    }
    for (JsonNode ph : o.path("phieu_hoc_tap")) {
      for (JsonNode c : ph.path("cau")) {
        String where = ph.path("ma").asString("PHT") + " câu " + c.path("so").asString("?");
        String src = c.path("nguon").asString("");
        if (!"moi".equals(src) && !bank.contains(src)) {
          errs.add("A9: " + where + " có nguồn \"" + src + "\" không có trong kho bài tập");
        }
        if (c.has("dap_so") && !c.get("dap_so").isNull()) {
          errs.add("A3: " + where + " có \"dap_so\" do AI điền (phải null, máy chủ điền)");
        }
        for (JsonNode k : c.path("kt")) {
          kt(k.asString(""), allowedKt, errs);
        }
      }
    }
  }

  private static void kt(String code, Set<String> allowed, List<String> errs) {
    if (!code.isBlank() && !allowed.contains(code)) {
      String e = "A1: mã kiến thức " + code + " ngoài danh sách được cấp";
      if (!errs.contains(e)) {
        errs.add(e);
      }
    }
  }

  /** Chuẩn hoá khoá đệ quy như score.py fix_keys: bỏ dấu + khoảng trắng; đổi bí danh to_chuc. */
  private JsonNode normalize(JsonNode n, int[] fixed) {
    if (n instanceof ObjectNode o) {
      ObjectNode out = mapper.createObjectNode();
      for (Map.Entry<String, JsonNode> e : o.properties()) {
        String k = e.getKey();
        String plain = ascii(k).strip();
        String k2 = ALIAS.getOrDefault(plain, plain);
        if (!plain.equals(k)) {
          fixed[0]++;
          if (o.has(k2)) {
            continue; // đã có khoá đúng: bỏ khoá thừa có dấu
          }
        }
        out.set(k2, normalize(e.getValue(), fixed));
      }
      return out;
    }
    if (n instanceof ArrayNode a) {
      ArrayNode out = mapper.createArrayNode();
      a.forEach(x -> out.add(normalize(x, fixed)));
      return out;
    }
    return n;
  }

  static String ascii(String k) {
    String d = Normalizer.normalize(k.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD);
    return d.replaceAll("\\p{Mn}+", "");
  }

  /** Bỏ một lớp rào ``` nếu có (nội dung bên trong vẫn phải là đúng MỘT đối tượng JSON). */
  private static String strip(String text) {
    String t = text == null ? "" : text.strip();
    if (t.startsWith("```")) {
      int nl = t.indexOf('\n');
      int end = t.lastIndexOf("```");
      if (nl > 0 && end > nl) {
        t = t.substring(nl + 1, end).strip();
      }
    }
    return t;
  }
}
