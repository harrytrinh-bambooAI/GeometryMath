package vn.geo.server.lessonplan.internal.patch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Đường dẫn bản vá kiểu {@code hoat_dong[0].to_chuc.chuyen_giao} (prompt-final.md §2 mục 2): đọc
 * giá trị, áp replace / add / remove, xác định mục (muc_tieu, thiet_bi, HDn, PHTn) mà đường dẫn
 * chạm tới.
 */
public final class PatchPaths {

  /** Một đoạn: tên khoá, chỉ số mảng (-1 = không có). */
  record Seg(String key, int index) {}

  private static final Pattern SEG = Pattern.compile("([a-z_]+)(?:\\[(\\d+)])?");

  /** Nhãn người đọc của trường lá (dòng bản vá trên màn hình). */
  static final Map<String, String> LABEL =
      Map.ofEntries(
          Map.entry("ten", "Tên"),
          Map.entry("muc_tieu", "Mục tiêu"),
          Map.entry("noi_dung", "Nội dung"),
          Map.entry("san_pham", "Sản phẩm"),
          Map.entry("thoi_luong_phut", "Thời lượng (phút)"),
          Map.entry("o_nha", "Ở nhà"),
          Map.entry("chuyen_giao", "Chuyển giao"),
          Map.entry("thuc_hien", "Thực hiện"),
          Map.entry("bao_cao", "Báo cáo"),
          Map.entry("ket_luan", "Kết luận"),
          Map.entry("kien_thuc", "Kiến thức"),
          Map.entry("nang_luc", "Năng lực"),
          Map.entry("pham_chat", "Phẩm chất"),
          Map.entry("giao_vien", "Thiết bị giáo viên"),
          Map.entry("hoc_sinh", "Thiết bị học sinh"),
          Map.entry("cau", "Câu"),
          Map.entry("de", "Đề"));

  private PatchPaths() {}

  /** Tách đường dẫn; null nếu sai cú pháp. */
  static List<Seg> parse(String path) {
    if (path == null || path.isBlank()) {
      return null;
    }
    List<Seg> out = new ArrayList<>();
    for (String part : path.split("\\.", -1)) {
      Matcher m = SEG.matcher(part);
      if (!m.matches()) {
        return null;
      }
      out.add(new Seg(m.group(1), m.group(2) == null ? -1 : Integer.parseInt(m.group(2))));
    }
    return out;
  }

  /** Giá trị tại đường dẫn; null nếu không tồn tại. */
  static JsonNode get(JsonNode root, List<Seg> segs) {
    JsonNode n = root;
    for (Seg s : segs) {
      n = n == null ? null : n.get(s.key());
      if (n != null && s.index() >= 0) {
        n = n.isArray() && s.index() < n.size() ? n.get(s.index()) : null;
      }
      if (n == null) {
        return null;
      }
    }
    return n;
  }

  /**
   * Mục mà đường dẫn chạm tới: {@code muc_tieu}, {@code thiet_bi}, mã HĐ ({@code HD1}…) hoặc mã
   * phiếu ({@code PHT1}…); null = ngoài phạm vi được sửa (meta, unsure, de_xuat…).
   */
  static String section(JsonNode content, List<Seg> segs) {
    Seg first = segs.get(0);
    return switch (first.key()) {
      case "muc_tieu", "thiet_bi" -> first.index() < 0 ? first.key() : null;
      case "hoat_dong", "phieu_hoc_tap" -> {
        if (first.index() < 0 || segs.size() < 2) {
          yield null; // không thay cả danh sách / cả hoạt động (A6)
        }
        JsonNode item = content.path(first.key()).path(first.index());
        String ma = item.path("ma").asString("");
        yield ma.isEmpty() ? null : ma;
      }
      default -> null;
    };
  }

  /** Áp một dòng bản vá lên {@code root} (đã kiểm hợp lệ). */
  static void apply(ObjectNode root, List<Seg> segs, String op, JsonNode after) {
    JsonNode parent = segs.size() == 1 ? root : get(root, segs.subList(0, segs.size() - 1));
    Seg last = segs.get(segs.size() - 1);
    if (!(parent instanceof ObjectNode obj)) {
      throw new IllegalStateException("đường dẫn không tồn tại");
    }
    switch (op) {
      case "add" -> {
        JsonNode list = obj.get(last.key());
        ArrayNode arr = list instanceof ArrayNode a ? a : obj.putArray(last.key());
        arr.add(after == null ? null : after.deepCopy());
      }
      case "remove" -> {
        if (last.index() >= 0 && obj.get(last.key()) instanceof ArrayNode a) {
          a.remove(last.index());
        } else {
          obj.remove(last.key());
        }
      }
      default -> {
        if (last.index() >= 0 && obj.get(last.key()) instanceof ArrayNode a) {
          a.set(last.index(), after == null ? null : after.deepCopy());
        } else {
          obj.set(last.key(), after == null ? null : after.deepCopy());
        }
      }
    }
  }

  /** Nhãn dòng: "HĐ1 · Tổ chức · Chuyển giao". */
  static String label(String section, List<Seg> segs) {
    String head =
        section == null
            ? ""
            : switch (section) {
              case "muc_tieu" -> "Mục tiêu";
              case "thiet_bi" -> "Thiết bị";
              default -> section.replace("HD", "HĐ");
            };
    StringBuilder b = new StringBuilder(head);
    for (int i = 1; i < segs.size(); i++) {
      Seg s = segs.get(i);
      String l = LABEL.get(s.key());
      if (l == null) {
        continue;
      }
      b.append(" · ").append(l);
      if (s.index() >= 0) {
        b.append(' ').append(s.index() + 1);
      }
    }
    return b.toString();
  }
}
