package vn.geo.server.lessonplan.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Knowledge;
import vn.geo.server.lessonplan.internal.repo.LessonPlanRepository.Row;

/**
 * Phản hồi API của giáo án (US-S5E1-03, giao-dien.md §2): dựng theo {@code kind}; nhãn người "AI
 * soạn"; {@code unsure} gắn vào hoạt động được nhắc (nhãn "AI chưa chắc"); {@code de_xuat} gom
 * riêng (khung ⚠, chưa áp); mã kiến thức {@code nhap-tam} mang nhãn "Kiến thức chưa soát".
 */
final class LessonPlanViews {

  static final String MSG_FAILED = "Chưa soạn xong, thầy cô bấm Thử lại."; // MSG-S5-03
  static final String MSG_REFUSED = "AI không xử lý được yêu cầu này."; // MSG-S5-04

  private static final Map<String, String> STATUS =
      Map.of(
          "drafting", "Đang soạn",
          "ready", "Sẵn sàng",
          "error", "Lỗi",
          "question", "Cần trả lời",
          "reply", "AI trả lời",
          "refused", "Từ chối");
  private static final Map<String, String> KIND_LABEL =
      Map.of(
          "mo-dau", "Mở đầu",
          "hinh-thanh", "Hình thành kiến thức",
          "luyen-tap", "Luyện tập",
          "van-dung", "Vận dụng");
  private static final Map<String, String> STEP_KEY =
      Map.of(
          "chuyen_giao", "chuyenGiao",
          "thuc_hien", "thucHien",
          "bao_cao", "baoCao",
          "ket_luan", "ketLuan");

  private final JsonMapper mapper;
  private final LessonCatalogRepository catalog;

  LessonPlanViews(JsonMapper mapper, LessonCatalogRepository catalog) {
    this.mapper = mapper;
    this.catalog = catalog;
  }

  /** Phản hồi 202 của yêu cầu soạn: chỉ mã và trạng thái "Đang soạn". */
  Map<String, Object> created(Row row) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("draftId", row.id().toString());
    m.put("code", row.code());
    m.put("status", "drafting");
    m.put("statusLabel", STATUS.get("drafting"));
    m.put("humanLabel", "AI soạn");
    return m;
  }

  Map<String, Object> full(Row row) {
    JsonNode req = mapper.readTree(row.requestJson());
    var period = catalog.period(row.periodId()).orElseThrow();
    var lesson = catalog.lesson(period.lessonNo()).orElseThrow();
    int soTiet = req.path("soTiet").asInt(period.periods());
    String style = req.path("phongCach").asString("truyen-thong");
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("draftId", row.id().toString());
    m.put("code", row.code());
    m.put("status", row.status());
    m.put("statusLabel", STATUS.get(row.status()));
    m.put("humanLabel", "approved".equals(row.humanLabel()) ? "Giáo viên đã duyệt" : "AI soạn");
    m.put("version", row.version());
    m.put(
        "title",
        "Bài "
            + lesson.no()
            + ". "
            + lesson.title()
            + " · "
            + period.label()
            + " · "
            + LessonForm.AUDIENCE_LABEL.getOrDefault(req.path("doiTuong").asString(""), "")
            + " · "
            + capital(LessonForm.STYLES.getOrDefault(style, style))
            + " · "
            + soTiet * 45
            + "'");
    m.put("periodId", period.id());
    m.put("request", req);
    m.put("lockedSections", row.lockedSections());
    m.put("createdAt", row.createdAt().toString());
    m.put("updatedAt", row.updatedAt().toString());
    switch (row.status()) {
      case "ready" -> {
        m.put("kind", "lesson");
        m.put("lesson", lesson(mapper.readTree(row.contentJson())));
      }
      case "question", "reply" -> {
        JsonNode r = mapper.readTree(row.replyJson());
        m.put("kind", r.path("kind").asString(row.status()));
        Map<String, Object> rv = new LinkedHashMap<>();
        rv.put("text", r.path("text").asString(""));
        rv.put("field", r.hasNonNull("field") ? r.get("field").asString() : null);
        m.put("reply", rv);
      }
      case "error" -> m.put("message", MSG_FAILED);
      case "refused" -> m.put("message", MSG_REFUSED);
      default -> {
        // Đang soạn: chỉ trạng thái
      }
    }
    return m;
  }

  private ObjectNode lesson(JsonNode c) {
    Map<String, String> status =
        catalog.knowledge().stream()
            .collect(Collectors.toMap(Knowledge::code, Knowledge::status, (a, b) -> a));
    ObjectNode out = mapper.createObjectNode();
    out.set("meta", c.path("meta"));
    out.putArray("sections").add("Mục tiêu").add("Thiết bị").add("Tiến trình").add("Phiếu học tập");
    ObjectNode mt = out.putObject("mucTieu");
    mt.set("kienThuc", c.path("muc_tieu").path("kien_thuc"));
    mt.set("nangLuc", c.path("muc_tieu").path("nang_luc"));
    mt.set("phamChat", c.path("muc_tieu").path("pham_chat"));
    ObjectNode tb = out.putObject("thietBi");
    tb.set("giaoVien", c.path("thiet_bi").path("giao_vien"));
    tb.set("hocSinh", c.path("thiet_bi").path("hoc_sinh"));
    List<String> unsureLeft = new ArrayList<>();
    c.path("unsure").forEach(u -> unsureLeft.add(u.asString("")));
    ArrayNode acts = out.putArray("hoatDong");
    int total = 0;
    for (JsonNode a : c.path("hoat_dong")) {
      ObjectNode v = acts.addObject();
      String ma = a.path("ma").asString("");
      v.put("ma", ma);
      v.put("nhan", ma.replace("HD", "HĐ"));
      v.put("loai", a.path("loai").asString(""));
      v.put("loaiLabel", KIND_LABEL.getOrDefault(a.path("loai").asString(""), ""));
      v.put("ten", a.path("ten").asString(""));
      int min = a.path("thoi_luong_phut").asInt(0);
      boolean home = a.path("o_nha").asBoolean(false);
      v.put("thoiLuongPhut", min).put("oNha", home);
      if (!home) {
        total += min;
      }
      v.put("mucTieu", a.path("muc_tieu").asString(""));
      v.put("noiDung", a.path("noi_dung").asString(""));
      v.put("sanPham", a.path("san_pham").asString(""));
      ObjectNode tc = v.putObject("toChuc");
      STEP_KEY.forEach((k, k2) -> tc.put(k2, a.path("to_chuc").path(k).asString("")));
      ArrayNode kts = v.putArray("kienThuc");
      ArrayNode unrev = v.putArray("chuaSoat");
      for (JsonNode k : a.path("kien_thuc")) {
        String code = k.path("kt").asString("");
        kts.addObject().put("kt", code).put("phatBieu", k.path("phat_bieu").asString(""));
        if ("nhap-tam".equals(status.get(code))) {
          unrev.add("Kiến thức chưa soát: " + code);
        }
      }
      ArrayNode uns = v.putArray("unsure");
      Pattern mention =
          Pattern.compile("(?<![0-9])(HĐ|HD)\\s?" + ma.replace("HD", "") + "(?![0-9])");
      for (var it = unsureLeft.iterator(); it.hasNext(); ) {
        String u = it.next();
        if (!ma.isEmpty() && mention.matcher(u).find()) {
          uns.add(u);
          it.remove();
        }
      }
    }
    out.put("tongPhut", total);
    ArrayNode phts = out.putArray("phieuHocTap");
    for (JsonNode ph : c.path("phieu_hoc_tap")) {
      ObjectNode p = phts.addObject().put("ma", ph.path("ma").asString(""));
      ArrayNode cau = p.putArray("cau");
      for (JsonNode q : ph.path("cau")) {
        ObjectNode qv = cau.addObject();
        qv.put("so", q.path("so").asInt());
        qv.put("nguon", q.path("nguon").asString(""));
        qv.put("mucDo", q.path("muc_do").asString(""));
        qv.put("de", q.path("de").asString(""));
        qv.set("kt", q.path("kt"));
        if (q.hasNonNull("dap_so")) {
          qv.put("dapSo", q.get("dap_so").asString());
        } else {
          qv.putNull("dapSo");
        }
        String src = q.path("dap_so_nguon").asString("can-xem-lai");
        qv.put("dapSoNguon", src);
        qv.put("nhanMay", "kho".equals(src) ? "Đã kiểm chứng" : "Cần xem lại");
        qv.set("buocGoiY", q.path("buoc_goi_y"));
      }
    }
    ArrayNode rest = out.putArray("unsure");
    unsureLeft.forEach(rest::add);
    ArrayNode dx = out.putArray("deXuat");
    for (JsonNode d : c.path("de_xuat")) {
      dx.addObject()
          .put("viTri", d.path("vi_tri").asString(""))
          .put("vanDe", d.path("van_de").asString(""))
          .put("goiY", d.path("goi_y").asString(""))
          .put("lyDo", d.path("ly_do").asString(""));
    }
    return out;
  }

  private static String capital(String s) {
    return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
