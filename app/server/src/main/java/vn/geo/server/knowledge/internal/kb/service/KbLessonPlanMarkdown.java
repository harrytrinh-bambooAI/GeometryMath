package vn.geo.server.knowledge.internal.kb.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Giáo án đã duyệt (US-S5E1-05) → Markdown của kho (US-S5E3-01-SC3): 4 mục theo khung 5512 — Hoạt
 * động 1 Mở đầu, 2 Hình thành kiến thức, 3 Luyện tập, 4 Vận dụng. Mục tiêu và thiết bị của bài đi
 * vào mục Hoạt động 1 (TẠM, ghi báo cáo T-261128-56); phiếu học tập (có đáp số) KHÔNG đưa vào kho
 * để hỏi đáp của học sinh không có nguồn chứa đáp số. Hàm thuần.
 */
public final class KbLessonPlanMarkdown {

  private KbLessonPlanMarkdown() {}

  static final Map<String, String> LOAI =
      Map.of(
          "mo-dau", "Mở đầu",
          "hinh-thanh", "Hình thành kiến thức",
          "luyen-tap", "Luyện tập",
          "van-dung", "Vận dụng");

  static final List<String> ORDER = List.of("mo-dau", "hinh-thanh", "luyen-tap", "van-dung");

  /** Tiêu đề tài liệu: "Giáo án GA-0001 · Bài 26. Khoảng cách · Tiết 1/2". */
  public static String title(String code, JsonNode content) {
    JsonNode meta = content.path("meta");
    StringBuilder t = new StringBuilder("Giáo án ").append(code);
    if (!meta.path("bai").asString("").isBlank()) {
      t.append(" · ").append(meta.path("bai").asString());
    }
    if (!meta.path("tiet").asString("").isBlank()) {
      t.append(" · ").append(meta.path("tiet").asString());
    }
    return t.length() > 200 ? t.substring(0, 200) : t.toString();
  }

  public static String markdown(String title, JsonNode content) {
    StringBuilder md = new StringBuilder("# ").append(title).append("\n\n");
    List<JsonNode> acts = new ArrayList<>();
    content.path("hoat_dong").forEach(acts::add);
    int n = 0;
    for (String loai : ORDER) {
      n++;
      List<JsonNode> group =
          acts.stream().filter(a -> loai.equals(a.path("loai").asString(""))).toList();
      md.append("## Hoạt động ").append(n).append(": ").append(LOAI.get(loai));
      if (group.size() == 1 && !group.get(0).path("ten").asString("").isBlank()) {
        md.append(" — ").append(group.get(0).path("ten").asString());
      }
      md.append("\n\n");
      if (n == 1) {
        objectives(md, content);
      }
      if (group.isEmpty()) {
        md.append("(Giáo án không có hoạt động này.)\n\n");
      }
      for (JsonNode a : group) {
        activity(md, a, group.size() > 1);
      }
    }
    return md.toString().strip() + "\n";
  }

  private static void objectives(StringBuilder md, JsonNode c) {
    JsonNode mt = c.path("muc_tieu");
    List<String> lines = new ArrayList<>();
    mt.path("kien_thuc")
        .forEach(k -> {
          List<String> kt = new ArrayList<>();
          k.path("kt").forEach(x -> kt.add(x.asString()));
          lines.add(
              k.path("noi_dung").asString("")
                  + (kt.isEmpty() ? "" : " (" + String.join(", ", kt) + ")"));
        });
    mt.path("nang_luc").forEach(x -> lines.add("Năng lực: " + x.asString()));
    mt.path("pham_chat").forEach(x -> lines.add("Phẩm chất: " + x.asString()));
    if (!lines.isEmpty()) {
      md.append("**Mục tiêu bài:**\n");
      lines.forEach(l -> md.append("- ").append(l).append('\n'));
      md.append('\n');
    }
    List<String> tb = new ArrayList<>();
    c.path("thiet_bi").path("giao_vien").forEach(x -> tb.add(x.asString()));
    c.path("thiet_bi").path("hoc_sinh").forEach(x -> tb.add(x.asString()));
    if (!tb.isEmpty()) {
      md.append("**Thiết bị:** ").append(String.join("; ", tb)).append("\n\n");
    }
  }

  private static void activity(StringBuilder md, JsonNode a, boolean named) {
    if (named) {
      md.append("**").append(a.path("ten").asString("")).append("**\n\n");
    }
    line(md, "Thời lượng", a.path("thoi_luong_phut").asInt(0) + " phút");
    line(md, "Mục tiêu", a.path("muc_tieu").asString(""));
    line(md, "Nội dung", a.path("noi_dung").asString(""));
    line(md, "Sản phẩm", a.path("san_pham").asString(""));
    JsonNode tc = a.path("to_chuc");
    line(md, "Chuyển giao nhiệm vụ", tc.path("chuyen_giao").asString(""));
    line(md, "Thực hiện nhiệm vụ", tc.path("thuc_hien").asString(""));
    line(md, "Báo cáo, thảo luận", tc.path("bao_cao").asString(""));
    line(md, "Kết luận, nhận định", tc.path("ket_luan").asString(""));
    a.path("kien_thuc")
        .forEach(k ->
            md.append("- ")
                .append(k.path("kt").asString(""))
                .append(": ")
                .append(k.path("phat_bieu").asString(""))
                .append('\n'));
    md.append('\n');
  }

  private static void line(StringBuilder md, String label, String value) {
    if (value != null && !value.isBlank()) {
      md.append("- ").append(label).append(": ").append(value).append('\n');
    }
  }
}
