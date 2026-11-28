package vn.geo.server.content.internal.export.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Trang HTML tự chứa của liên kết chỉ xem (US-S2E9-02-SC5): chữ đề và hình vẽ SVG dựng từ cảnh
 * (phẳng: scene2d-v1; không gian: chiếu xiên các đỉnh và cạnh). Không kịch bản, không tài nguyên
 * ngoài, không nút thao tác, không lời giải, không danh tính giáo viên. Màu theo chế độ sáng/tối
 * của thiết bị ({@code currentColor}), không mã màu cứng.
 */
public final class SharePageExport {

  private static final double W = 360;
  private static final double H = 260;
  private static final double PAD = 28;

  private SharePageExport() {}

  public static String page(String title, String statement, String sceneKind, JsonNode scene) {
    StringBuilder sb = new StringBuilder(2048);
    sb.append("<!doctype html><html lang=\"vi\"><head><meta charset=\"utf-8\">")
        .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
        .append("<meta name=\"robots\" content=\"noindex, nofollow\">")
        .append("<title>")
        .append(esc(title))
        .append("</title><style>")
        .append(":root{color-scheme:light dark}")
        .append("body{font-family:system-ui,sans-serif;margin:0;padding:24px;line-height:1.6}")
        .append("main{max-width:720px;margin:0 auto}h1{font-size:20px;margin:0 0 12px}")
        .append("p{font-size:17px;white-space:pre-wrap}small{font-size:14px;opacity:.75}")
        .append("svg{max-width:100%;height:auto;display:block;margin:16px 0}")
        .append("svg text{font-size:14px;fill:currentColor;stroke:none}")
        .append("</style></head><body><main data-testid=\"s2e9-share-page\">")
        .append("<h1 data-testid=\"s2e9-share-title\">")
        .append(esc(title))
        .append("</h1><p data-testid=\"s2e9-share-statement\">")
        .append(esc(statement))
        .append("</p>");
    String svg = svg(sceneKind, scene);
    if (svg != null) {
      sb.append(svg);
    }
    sb.append("<small>Liên kết chỉ xem · Hình Học Không Gian Cùng AI</small>")
        .append("</main></body></html>");
    return sb.toString();
  }

  /** Hình SVG của cảnh; null khi đề không có hình dựng được. */
  static String svg(String sceneKind, JsonNode scene) {
    if (scene == null || scene.isNull()) {
      return null;
    }
    Map<String, double[]> pts = new LinkedHashMap<>();
    JsonNode segments;
    if ("plane".equals(sceneKind)) {
      for (JsonNode p : scene.path("points")) {
        pts.put(
            p.path("id").asString(), new double[] {p.path("x").asDouble(), p.path("y").asDouble()});
      }
      segments = scene.path("segments");
    } else if ("space".equals(sceneKind)) {
      boolean upY = "y".equalsIgnoreCase(scene.path("axes").path("up").asString("z"));
      for (JsonNode p : scene.path("points")) {
        double x = p.path("x").asDouble();
        double y = p.path("y").asDouble();
        double z = p.path("z").asDouble();
        double depth = upY ? z : y;
        double up = upY ? y : z;
        pts.put(p.path("name").asString(), new double[] {x - 0.45 * depth, up - 0.3 * depth});
      }
      segments = scene.path("edges");
    } else {
      return null;
    }
    if (pts.isEmpty()) {
      return null;
    }
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for (double[] p : pts.values()) {
      minX = Math.min(minX, p[0]);
      maxX = Math.max(maxX, p[0]);
      minY = Math.min(minY, p[1]);
      maxY = Math.max(maxY, p[1]);
    }
    for (JsonNode c : scene.path("circles")) {
      double[] o = pts.get(c.path("center").asString());
      double r = c.path("r").asDouble();
      if (o != null) {
        minX = Math.min(minX, o[0] - r);
        maxX = Math.max(maxX, o[0] + r);
        minY = Math.min(minY, o[1] - r);
        maxY = Math.max(maxY, o[1] + r);
      }
    }
    double k =
        Math.min(
            (W - 2 * PAD) / Math.max(1e-9, maxX - minX),
            (H - 2 * PAD) / Math.max(1e-9, maxY - minY));
    StringBuilder sb = new StringBuilder();
    sb.append("<svg data-testid=\"s2e9-share-figure\" role=\"img\" aria-label=\"Hình vẽ của đề\" ")
        .append("viewBox=\"0 0 ")
        .append(n(W))
        .append(' ')
        .append(n(H))
        .append("\" width=\"")
        .append(n(W))
        .append("\" height=\"")
        .append(n(H))
        .append("\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"1.6\">");
    for (JsonNode s : segments) {
      double[] a = pts.get(s.path(0).asString());
      double[] b = pts.get(s.path(1).asString());
      if (a != null && b != null) {
        sb.append("<line x1=\"")
            .append(n(PAD + (a[0] - minX) * k))
            .append("\" y1=\"")
            .append(n(H - PAD - (a[1] - minY) * k))
            .append("\" x2=\"")
            .append(n(PAD + (b[0] - minX) * k))
            .append("\" y2=\"")
            .append(n(H - PAD - (b[1] - minY) * k))
            .append("\"/>");
      }
    }
    for (JsonNode c : scene.path("circles")) {
      double[] o = pts.get(c.path("center").asString());
      if (o != null) {
        sb.append("<circle cx=\"")
            .append(n(PAD + (o[0] - minX) * k))
            .append("\" cy=\"")
            .append(n(H - PAD - (o[1] - minY) * k))
            .append("\" r=\"")
            .append(n(c.path("r").asDouble() * k))
            .append("\"/>");
      }
    }
    for (Map.Entry<String, double[]> e : pts.entrySet()) {
      sb.append("<text x=\"")
          .append(n(PAD + (e.getValue()[0] - minX) * k + 6))
          .append("\" y=\"")
          .append(n(H - PAD - (e.getValue()[1] - minY) * k - 6))
          .append("\">")
          .append(esc(e.getKey()))
          .append("</text>");
    }
    return sb.append("</svg>").toString();
  }

  private static String n(double v) {
    return String.format(Locale.ROOT, "%.1f", v);
  }

  static String esc(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
