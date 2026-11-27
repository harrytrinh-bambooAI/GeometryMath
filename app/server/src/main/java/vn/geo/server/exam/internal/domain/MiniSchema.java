package vn.geo.server.exam.internal.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Bộ kiểm JSON Schema tập con 2020-12, đúng tập con của {@code materials/de-thi/check.py}
 * (validate): type, enum, const, minLength/maxLength, pattern, minimum/maximum/exclusiveMinimum,
 * minItems/maxItems, items, required, properties, allOf (kể cả if/then). US-S5E2-01 BR-S5E2-01.
 */
public final class MiniSchema {

  private MiniSchema() {}

  public static List<String> validate(JsonNode v, JsonNode s) {
    List<String> errs = new ArrayList<>();
    validate(v, s, "$", errs);
    return errs;
  }

  private static boolean typeOk(JsonNode v, String t) {
    return switch (t) {
      case "object" -> v.isObject();
      case "array" -> v.isArray();
      case "string" -> v.isString();
      case "boolean" -> v.isBoolean();
      case "null" -> v.isNull();
      case "number" -> v.isNumber();
      case "integer" -> v.isIntegralNumber();
      default -> false;
    };
  }

  private static boolean sameValue(JsonNode a, JsonNode b) {
    if (a.isNumber() && b.isNumber()) {
      return a.doubleValue() == b.doubleValue();
    }
    return a.equals(b);
  }

  private static void validate(JsonNode v, JsonNode s, String path, List<String> errs) {
    if (s == null || !s.isObject()) {
      return;
    }
    if (s.has("type")) {
      JsonNode t = s.get("type");
      boolean ok = false;
      if (t.isArray()) {
        for (JsonNode x : t) {
          ok |= typeOk(v, x.asString());
        }
      } else {
        ok = typeOk(v, t.asString());
      }
      if (!ok) {
        errs.add(path + ": kiểu không thuộc " + t);
        return;
      }
    }
    if (s.has("enum")) {
      boolean in = false;
      for (JsonNode e : s.get("enum")) {
        in |= sameValue(v, e);
      }
      if (!in) {
        errs.add(path + ": " + v + " ∉ enum");
      }
    }
    if (s.has("const") && !sameValue(v, s.get("const"))) {
      errs.add(path + ": ≠ const");
    }
    if (v.isString()) {
      int len = v.asString().length();
      if (len < s.path("minLength").asInt(0)
          || len > s.path("maxLength").asInt(Integer.MAX_VALUE)) {
        errs.add(path + ": độ dài chuỗi");
      }
      if (s.has("pattern")
          && !Pattern.compile(s.get("pattern").asString()).matcher(v.asString()).find()) {
        errs.add(path + ": không khớp pattern " + s.get("pattern").asString());
      }
    }
    if (v.isNumber()) {
      double d = v.doubleValue();
      if (s.has("minimum") && d < s.get("minimum").doubleValue()) {
        errs.add(path + ": < minimum");
      }
      if (s.has("maximum") && d > s.get("maximum").doubleValue()) {
        errs.add(path + ": > maximum");
      }
      if (s.has("exclusiveMinimum") && d <= s.get("exclusiveMinimum").doubleValue()) {
        errs.add(path + ": ≤ exclusiveMinimum");
      }
    }
    if (v.isArray()) {
      if (v.size() < s.path("minItems").asInt(0)
          || v.size() > s.path("maxItems").asInt(Integer.MAX_VALUE)) {
        errs.add(path + ": số phần tử " + v.size());
      }
      if (s.has("items")) {
        for (int i = 0; i < v.size(); i++) {
          validate(v.get(i), s.get("items"), path + "[" + i + "]", errs);
        }
      }
    }
    if (v.isObject()) {
      for (JsonNode r : s.path("required")) {
        if (!v.has(r.asString())) {
          errs.add(path + ": thiếu " + r.asString());
        }
      }
      if (s.has("properties")) {
        for (Map.Entry<String, JsonNode> e : s.get("properties").properties()) {
          if (v.has(e.getKey())) {
            validate(v.get(e.getKey()), e.getValue(), path + "." + e.getKey(), errs);
          }
        }
      }
    }
    for (JsonNode sub : s.path("allOf")) {
      if (sub.has("if")) {
        if (validate(v, sub.get("if")).isEmpty()) {
          validate(v, sub.path("then"), path, errs);
        }
      } else {
        validate(v, sub, path, errs);
      }
    }
  }
}
