package vn.geo.server.exam.internal.domain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** Mô hình lắp đề (bản Java của cấu trúc {@code lap_de.py}): câu ngân hàng, ô, phần, ma trận. */
public final class ExamModel {

  private ExamModel() {}

  /** Một câu ngân hàng (body = JSON gốc đã qua kiểm). */
  public record Item(
      String id,
      String template,
      String topic,
      String dang,
      String dangTx,
      List<String> yccd,
      String type,
      String level,
      double time,
      double difficulty,
      String ctx,
      ObjectNode body) {

    public static Item of(ObjectNode n) {
      return new Item(
          n.path("id").asString(),
          n.path("mau_cau").asString(),
          n.path("chu_de").asString(),
          n.path("dang").asString(),
          n.path("dang_tx").asString(),
          strings(n.path("yccd")),
          n.path("loai_cau").asString(),
          n.path("muc_do").asString(),
          n.path("thoi_gian_phut").doubleValue(),
          n.path("do_kho").doubleValue(),
          n.path("boi_canh_key").asString(),
          n);
    }

    /** Họ bối cảnh (phần trước dấu |), dùng để ưu tiên đề đa dạng. */
    public String family() {
      int i = ctx.indexOf('|');
      return i < 0 ? ctx : ctx.substring(0, i);
    }
  }

  /** Một phương án điền ô: chủ đề, lọc dạng (null = mọi dạng), YCCĐ (rỗng = mọi YCCĐ), trọng số. */
  public record Opt(String topic, Set<String> dangTx, List<String> yccd, double weight) {}

  public record Cell(
      String id,
      String part,
      String type,
      String level,
      int count,
      double points,
      String kind,
      List<Opt> opts,
      String spec) {

    public Cell withPoints(double p) {
      return new Cell(id, part, type, level, count, p, kind, opts, spec);
    }

    public Cell withLevel(String l) {
      return new Cell(id, part, type, l, count, points, kind, opts, spec);
    }

    public Cell withOpts(List<Opt> o) {
      return new Cell(id, part, type, level, count, points, kind, o, spec);
    }

    public Cell withId(String i) {
      return new Cell(i, part, type, level, count, points, kind, opts, spec);
    }
  }

  public record Part(String id, String name, String type, int count, double points) {}

  public record Matrix(
      String code,
      String name,
      int grade,
      double minutes,
      double totalPoints,
      List<Part> parts,
      List<Cell> cells) {

    public static Matrix of(JsonNode m) {
      List<Part> parts = new ArrayList<>();
      for (JsonNode p : m.path("phan")) {
        parts.add(
            new Part(
                p.path("id").asString(),
                p.path("ten").asString(),
                p.path("loai_cau").asString(),
                p.path("so_cau").asInt(),
                p.path("diem_moi_cau").doubleValue()));
      }
      List<Cell> cells = new ArrayList<>();
      for (JsonNode c : m.path("cells")) {
        List<Opt> opts = new ArrayList<>();
        if ("co-dinh".equals(c.path("loai_o").asString())) {
          opts.add(new Opt(c.path("chu_de").asString(), dangs(c), strings(c.path("yccd")), 1.0));
        } else {
          for (JsonNode g : c.path("nhom")) {
            opts.add(
                new Opt(
                    g.path("chu_de").asString(),
                    dangs(g),
                    strings(g.path("yccd")),
                    g.path("trong_so").doubleValue()));
          }
        }
        cells.add(
            new Cell(
                c.path("id").asString(),
                c.path("phan").asString(),
                c.path("loai_cau").asString(),
                c.path("muc_do").asString(),
                c.path("so_cau").asInt(),
                c.path("diem").doubleValue(),
                c.path("loai_o").asString(),
                List.copyOf(opts),
                c.path("dac_ta").asString()));
      }
      return new Matrix(
          m.path("id").asString(),
          m.path("ten").asString(),
          m.path("lop").asInt(),
          m.path("thoi_gian_phut").doubleValue(),
          m.path("tong_diem").doubleValue(),
          List.copyOf(parts),
          List.copyOf(cells));
    }
  }

  /** Câu hợp ô theo một phương án (hop_le của lap_de.py; YCCĐ rỗng = không lọc YCCĐ). */
  public static boolean fits(Item it, Cell c, Opt o) {
    if (!it.topic().equals(o.topic())
        || !it.type().equals(c.type())
        || !it.level().equals(c.level())) {
      return false;
    }
    if (o.dangTx() != null && !o.dangTx().isEmpty() && !o.dangTx().contains(it.dangTx())) {
      return false;
    }
    if (o.yccd().isEmpty()) {
      return true;
    }
    for (String y : it.yccd()) {
      if (o.yccd().contains(y)) {
        return true;
      }
    }
    return false;
  }

  private static Set<String> dangs(JsonNode n) {
    JsonNode d = n.path("dang_tx");
    if (!d.isArray()) {
      return null;
    }
    Set<String> s = new LinkedHashSet<>();
    for (JsonNode x : d) {
      s.add(x.asString());
    }
    return s;
  }

  static List<String> strings(JsonNode a) {
    List<String> out = new ArrayList<>();
    for (JsonNode x : a) {
      out.add(x.asString());
    }
    return List.copyOf(out);
  }
}
