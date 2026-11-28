package vn.geo.server.exam.internal.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import vn.geo.server.exam.internal.domain.ExamModel.Cell;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.ExamModel.Opt;
import vn.geo.server.exam.internal.domain.ExamModel.Part;

/**
 * 7 ràng buộc mỗi mã đề (kiem_de của {@code lap_de.py}, BR-S5E2-06): phủ đặc tả 100 %, khớp ma
 * trận, không trùng dạng, tổng điểm, tổng thời gian, A/B/C/D lệch ≤ 1, không lộ đáp số. Ràng buộc
 * thứ 7 (độ khó các mã lệch ≤ 5 %) kiểm ở mức bộ đề ({@link #spread}).
 */
public final class VariantChecker {

  private static final String LETTERS = "ABCD";
  private static final Pattern NORM = Pattern.compile("\\s+|\\\\text\\{[^}]*\\}|\\\\,|[{}$]");
  private static final Pattern PLAIN_NUMBER = Pattern.compile("[-\\d,.]+");

  public record Check(boolean ok, String detail) {}

  private VariantChecker() {}

  public static String norm(String s) {
    return s == null ? "" : NORM.matcher(s).replaceAll("");
  }

  /** Độ khó dự kiến = Σ(điểm × do_kho) / Σ điểm (do_kho_de). */
  public static double difficulty(JsonNode questions) {
    double t = 0;
    double s = 0;
    for (JsonNode q : questions) {
      t += q.path("diem").doubleValue();
      s += q.path("diem").doubleValue() * q.path("do_kho").doubleValue();
    }
    return t == 0 ? 0 : s / t;
  }

  /** Độ lệch = (lớn nhất − nhỏ nhất) / trung bình (lech_do_kho). */
  public static double spread(List<Double> ds) {
    if (ds.isEmpty()) {
      return 0;
    }
    double max = ds.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    double min = ds.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    double avg = ds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    return avg == 0 ? 0 : (max - min) / avg;
  }

  public static Map<String, Check> check(JsonNode de, Matrix mt) {
    JsonNode qs = de.path("cau");
    Map<String, Cell> cells = new LinkedHashMap<>();
    mt.cells().forEach(c -> cells.put(c.id(), c));
    Map<String, Check> r = new LinkedHashMap<>();
    List<String> loi = new ArrayList<>();
    Map<String, Integer> dem = new HashMap<>();
    for (JsonNode q : qs) {
      dem.merge(q.path("o").asString(), 1, Integer::sum);
    }
    int full = 0;
    for (Cell c : cells.values()) {
      int n = dem.getOrDefault(c.id(), 0);
      if (n != c.count()) {
        loi.add("ô " + c.id() + ": " + n + "/" + c.count() + " câu");
      } else {
        full++;
      }
    }
    for (JsonNode q : qs) {
      Cell c = cells.get(q.path("o").asString());
      if (c == null) {
        loi.add("câu " + q.path("so").asInt() + ": ô lạ");
        continue;
      }
      boolean ok = false;
      for (Opt o : c.opts()) {
        ok |= fitsQuestion(q, c, o);
      }
      if (!ok
          || Math.abs(q.path("diem").doubleValue() - c.points()) > 1e-9
          || !q.path("phan").asString().equals(c.part())) {
        loi.add("câu " + q.path("so").asInt() + ": không khớp ô " + c.id());
      }
    }
    int phu = cells.isEmpty() ? 100 : Math.round(100f * full / cells.size());
    r.put(
        "phu_dac_ta_100",
        new Check(
            full == cells.size() && loi.isEmpty(),
            "phủ " + phu + "% ô; " + (loi.isEmpty() ? "khớp" : String.join("; ", loi))));
    List<String> lechPhan = new ArrayList<>();
    for (Part p : mt.parts()) {
      int n = 0;
      for (JsonNode q : qs) {
        n += q.path("phan").asString().equals(p.id()) ? 1 : 0;
      }
      if (n != p.count()) {
        lechPhan.add(p.id());
      }
    }
    r.put(
        "khop_ma_tran",
        new Check(
            loi.isEmpty() && lechPhan.isEmpty(),
            lechPhan.isEmpty() ? "khớp số câu từng phần" : "lệch phần " + lechPhan));
    Map<String, Integer> dang = new HashMap<>();
    Map<String, Integer> ctx = new HashMap<>();
    double td = 0;
    double tt = 0;
    int[] ans = new int[4];
    boolean hasTn4 = false;
    for (JsonNode q : qs) {
      dang.merge(q.path("dang").asString(), 1, Integer::sum);
      ctx.merge(q.path("boi_canh_key").asString(), 1, Integer::sum);
      td += q.path("diem").doubleValue();
      tt += q.path("thoi_gian_phut").doubleValue();
      if ("tn4".equals(q.path("loai_cau").asString())) {
        hasTn4 = true;
        int i = LETTERS.indexOf(q.path("dap_an").asString());
        if (i >= 0) {
          ans[i]++;
        }
      }
    }
    List<String> trung =
        dang.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList();
    r.put(
        "khong_trung_dang",
        new Check(trung.isEmpty(), trung.isEmpty() ? "không trùng" : String.join(",", trung)));
    td = Math.round(td * 1e6) / 1e6;
    r.put(
        "tong_diem",
        new Check(
            Math.abs(td - mt.totalPoints()) < 1e-9,
            DatasetValidator.fmt(td) + " / " + DatasetValidator.fmt(mt.totalPoints())));
    tt = Math.round(tt * 100) / 100.0;
    r.put(
        "tong_thoi_gian",
        new Check(
            tt <= mt.minutes(),
            DatasetValidator.fmt(tt)
                + " phút dự kiến / "
                + DatasetValidator.fmt(mt.minutes())
                + " phút"));
    if (hasTn4) {
      int max = Math.max(Math.max(ans[0], ans[1]), Math.max(ans[2], ans[3]));
      int min = Math.min(Math.min(ans[0], ans[1]), Math.min(ans[2], ans[3]));
      r.put(
          "can_bang_dap_an",
          new Check(
              max - min <= 1, "A=" + ans[0] + " B=" + ans[1] + " C=" + ans[2] + " D=" + ans[3]));
    } else {
      r.put("can_bang_dap_an", new Check(true, "không có câu TN4"));
    }
    List<String> lo = new ArrayList<>();
    ctx.forEach((k, v) -> {
      if (v > 1) {
        lo.add("chung bối cảnh " + k);
      }
    });
    for (JsonNode qi : qs) {
      String a = norm(qi.path("dap_so_hien").asString());
      if (a.length() < 5 || PLAIN_NUMBER.matcher(a).matches()) {
        continue;
      }
      for (JsonNode qj : qs) {
        if (qj != qi && norm(qj.path("de_bai").asString()).contains(a)) {
          lo.add(
              "đáp số câu "
                  + qi.path("so").asInt()
                  + " xuất hiện trong đề câu "
                  + qj.path("so").asInt());
        }
      }
    }
    r.put(
        "khong_lo_dap_so",
        new Check(lo.isEmpty(), lo.isEmpty() ? "không lộ" : String.join("; ", lo)));
    return r;
  }

  private static boolean fitsQuestion(JsonNode q, Cell c, Opt o) {
    if (!q.path("chu_de").asString().equals(o.topic())
        || !q.path("loai_cau").asString().equals(c.type())
        || !q.path("muc_do").asString().equals(c.level())) {
      return false;
    }
    if (o.dangTx() != null
        && !o.dangTx().isEmpty()
        && !o.dangTx().contains(q.path("dang_tx").asString())) {
      return false;
    }
    if (o.yccd().isEmpty()) {
      return true;
    }
    for (JsonNode y : q.path("yccd")) {
      if (o.yccd().contains(y.asString())) {
        return true;
      }
    }
    return false;
  }

  public static boolean allOk(Map<String, Check> r) {
    return r.values().stream().allMatch(Check::ok);
  }
}
