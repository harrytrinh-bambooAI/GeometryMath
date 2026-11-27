package vn.geo.server.exam.internal.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Kiểm dữ liệu đề trước khi nạp (US-S5E2-01): schema (BR-S5E2-01), đáp số câu từ mẫu MVP khớp
 * engine trong 1e-6 (BR-S5E2-02, Kiểm 3), ma trận tổng điểm = 10, số câu từng phần, trọng số nhóm ô
 * xoay vòng cộng = 1 (BR-S5E2-03, Kiểm 5). Bản ghi lỗi bị loại, phần còn lại vẫn được nạp.
 */
public final class DatasetValidator {

  public static final double TOTAL_POINTS = 10.0;
  public static final double ENGINE_TOL = 1e-6;

  public record Issue(String kind, String id, String message) {}

  public record Result(
      List<ObjectNode> matrices,
      List<ObjectNode> templates,
      List<ObjectNode> items,
      List<Issue> issues) {}

  private DatasetValidator() {}

  public static Result validate(ExamDataset d) {
    List<Issue> issues = new ArrayList<>();
    List<ObjectNode> templates = new ArrayList<>();
    for (ObjectNode t : d.templates) {
      List<String> e = MiniSchema.validate(t, d.schemas.get("mau_cau"));
      if (e.isEmpty()) {
        templates.add(t);
      } else {
        issues.add(new Issue("mau_cau", t.path("id").asString(), "schema: " + first(e)));
      }
    }
    List<ObjectNode> items = new ArrayList<>();
    for (ObjectNode it : d.items) {
      String id = it.path("id").asString();
      List<String> e = MiniSchema.validate(it, d.schemas.get("cau"));
      if (!e.isEmpty()) {
        issues.add(new Issue("cau", id, "schema: " + first(e)));
        continue;
      }
      String bad = engineMismatch(it, d);
      if (bad != null) {
        issues.add(new Issue("cau", id, bad));
        continue;
      }
      items.add(it);
    }
    List<ObjectNode> matrices = new ArrayList<>();
    for (ObjectNode m : d.matrices) {
      String id = m.path("id").asString();
      List<String> e = MiniSchema.validate(m, d.schemas.get("ma_tran"));
      String bad = e.isEmpty() ? matrixProblem(m) : "schema: " + first(e);
      if (bad == null) {
        matrices.add(m);
      } else {
        issues.add(new Issue("ma_tran", id, bad));
      }
    }
    return new Result(matrices, templates, items, issues);
  }

  /** Lỗi đáp số so với engine (câu nguồn MVP), hoặc null khi khớp / không thuộc diện kiểm. */
  static String engineMismatch(JsonNode it, ExamDataset d) {
    if (!"mvp".equals(it.path("nguon").path("loai").asString())) {
      return null;
    }
    String id = it.path("id").asString();
    if ("dung-sai".equals(it.path("loai_cau").asString())) {
      for (JsonNode y : it.path("y")) {
        String key = id + "#" + y.path("nhan").asString();
        Double eng = d.engineValues.get(key);
        if (eng == null && y.path("so_engine").isNumber()) {
          eng = y.path("so_engine").doubleValue();
        }
        if (eng == null || !y.path("so").isNumber()) {
          continue;
        }
        double v = y.path("so").doubleValue();
        if (differs(eng, v)) {
          return "ý " + y.path("nhan").asString() + ": đáp số " + fmt(v) + " ≠ engine " + fmt(eng);
        }
      }
      return null;
    }
    Double eng = d.engineValues.get(id);
    if (eng == null && it.path("dap_so_engine").isNumber()) {
      eng = it.path("dap_so_engine").doubleValue();
    }
    JsonNode so = it.path("dap_so").path("so");
    if (eng == null) {
      return "thiếu giá trị engine";
    }
    if (!so.isNumber()) {
      return "thiếu đáp số số để so với engine " + fmt(eng);
    }
    double v = so.doubleValue();
    return differs(eng, v) ? "đáp số " + fmt(v) + " ≠ engine " + fmt(eng) : null;
  }

  private static boolean differs(double eng, double v) {
    return Math.abs(eng - v) > ENGINE_TOL * Math.max(1.0, Math.abs(v));
  }

  /** Lỗi ma trận (tổng điểm, phần, trọng số), hoặc null khi đạt. */
  static String matrixProblem(JsonNode m) {
    double td = 0;
    for (JsonNode c : m.path("cells")) {
      td += c.path("diem").doubleValue() * c.path("so_cau").asInt();
    }
    td = Math.round(td * 1e6) / 1e6;
    if (Math.abs(td - TOTAL_POINTS) > 1e-9) {
      return "tổng điểm " + fmt(td) + " ≠ " + fmt(TOTAL_POINTS);
    }
    if (Math.abs(m.path("tong_diem").doubleValue() - TOTAL_POINTS) > 1e-9) {
      return "tổng điểm ma trận " + fmt(m.path("tong_diem").doubleValue()) + " ≠ 10";
    }
    for (JsonNode p : m.path("phan")) {
      int n = 0;
      boolean ok = true;
      for (JsonNode c : m.path("cells")) {
        if (c.path("phan").asString().equals(p.path("id").asString())) {
          n += c.path("so_cau").asInt();
          ok &=
              c.path("loai_cau").asString().equals(p.path("loai_cau").asString())
                  && Math.abs(c.path("diem").doubleValue() - p.path("diem_moi_cau").doubleValue())
                      <= 1e-9;
        }
      }
      if (n != p.path("so_cau").asInt() || !ok) {
        return "phần " + p.path("id").asString() + " lệch số câu/loại/điểm";
      }
    }
    for (JsonNode c : m.path("cells")) {
      if ("xoay-vong".equals(c.path("loai_o").asString())) {
        double s = 0;
        for (JsonNode g : c.path("nhom")) {
          s += g.path("trong_so").doubleValue();
        }
        if (Math.abs(s - 1) > 2e-3) {
          return "ô " + c.path("id").asString() + ": tổng trọng số " + fmt(s) + " ≠ 1";
        }
      }
    }
    return null;
  }

  public static String fmt(double v) {
    return BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
  }

  private static String first(List<String> e) {
    return String.join("; ", e.subList(0, Math.min(3, e.size())));
  }
}
