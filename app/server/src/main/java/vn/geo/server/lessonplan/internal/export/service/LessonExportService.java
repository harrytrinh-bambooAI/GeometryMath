package vn.geo.server.lessonplan.internal.export.service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.export.ExportDoc;
import vn.geo.server.common.export.ExportFile;
import vn.geo.server.common.export.ExportFile.Format;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository;
import vn.geo.server.lessonplan.internal.patch.repo.LessonEditRepository.Plan;
import vn.geo.server.lessonplan.internal.patch.service.LessonPlanFinder;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Lesson;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Period;

/**
 * Xuất giáo án DOCX / PDF theo khung 5512 (US-S5E1-05): I. Mục tiêu · II. Thiết bị · III. Tiến
 * trình (bảng các HĐ) · IV. Phiếu học tập. Xuất được ở mọi trạng thái; phiên bản chưa duyệt có chân
 * trang "Bản nháp AI soạn – chưa duyệt"; câu "Cần xem lại" in "(đáp số chưa kiểm chứng)"
 * (BR-S5E1-14). Tên tệp {@code GA_<bai>_<tiet>_<yyyyMMdd>}; tệp chỉ chứa nội dung giáo án, không
 * tên, không mã người dùng (BR-S5E1-15).
 */
@Service
public class LessonExportService {

  public static final String DRAFT_FOOTER = "Bản nháp AI soạn – chưa duyệt";
  static final String UNVERIFIED = "(đáp số chưa kiểm chứng)";
  private static final Map<String, String> KIND =
      Map.of(
          "mo-dau", "Mở đầu",
          "hinh-thanh", "Hình thành kiến thức",
          "luyen-tap", "Luyện tập",
          "van-dung", "Vận dụng");
  private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final LessonPlanFinder finder;
  private final LessonEditRepository repo;
  private final LessonCatalogRepository catalog;
  private final JsonMapper mapper;
  private final Clock clock;

  public LessonExportService(
      LessonPlanFinder finder,
      LessonEditRepository repo,
      LessonCatalogRepository catalog,
      JsonMapper mapper,
      Clock clock) {
    this.finder = finder;
    this.repo = repo;
    this.catalog = catalog;
    this.mapper = mapper;
    this.clock = clock;
  }

  /** Xuất phiên bản {@code version} (null = phiên bản hiện tại). */
  public ExportFile export(String idOrCode, String format, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Format f = Format.parse(format);
    if (f == null) {
      throw AppException.invalidField("format", "Định dạng xuất là docx hoặc pdf.");
    }
    Plan plan = finder.find(me, idOrCode);
    if (plan.contentJson() == null) {
      throw new AppException(ErrorCode.CONFLICT, "Giáo án chưa có bản soạn để xuất.");
    }
    int v = version == null ? plan.version() : version;
    String content =
        v == plan.version()
            ? plan.contentJson()
            : v >= 1 && v < plan.version()
                ? repo.versionContent(plan.id(), v)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND))
                : null;
    if (content == null) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    boolean approved = plan.approvedVersion() != null && plan.approvedVersion() == v;
    Period period = catalog.period(plan.periodId()).orElseThrow();
    Lesson lesson = catalog.lesson(period.lessonNo()).orElseThrow();
    ExportDoc doc = document(mapper.readTree(content), lesson, period, approved);
    return ExportFile.render(doc, f, fileName(lesson, period));
  }

  /** {@code GA_26_T1_20261128}: số bài, tiết ("Tiết 1-2/3" → T1-2), ngày giờ Việt Nam. */
  String fileName(Lesson lesson, Period period) {
    String label = period.label().replace("Tiết", "").trim();
    String tiet =
        "T" + (label.contains("/") ? label.substring(0, label.indexOf('/')) : label).trim();
    String day = ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM).format(DAY);
    return "GA_" + lesson.no() + "_" + tiet + "_" + day;
  }

  static ExportDoc document(JsonNode c, Lesson lesson, Period period, boolean approved) {
    ExportDoc.Builder d = ExportDoc.builder("KẾ HOẠCH BÀI DẠY");
    int soTiet = c.path("meta").path("so_tiet").asInt(period.periods());
    d.para("Bài " + lesson.no() + ". " + lesson.title() + " (Toán 11, KNTT)");
    d.para(period.label() + " · Số tiết: " + soTiet + " · Thời lượng: " + soTiet * 45 + " phút");

    d.heading("I. Mục tiêu");
    d.subheading("1. Kiến thức");
    for (JsonNode k : c.path("muc_tieu").path("kien_thuc")) {
      List<String> codes = new ArrayList<>();
      k.path("kt").forEach(x -> codes.add(x.asString("")));
      d.para(
          "- "
              + k.path("noi_dung").asString("")
              + (codes.isEmpty() ? "" : " (" + String.join(", ", codes) + ")"));
    }
    d.subheading("2. Năng lực");
    c.path("muc_tieu").path("nang_luc").forEach(x -> d.para("- " + x.asString("")));
    d.subheading("3. Phẩm chất");
    c.path("muc_tieu").path("pham_chat").forEach(x -> d.para("- " + x.asString("")));

    d.heading("II. Thiết bị dạy học và học liệu");
    d.para("1. Giáo viên: " + join(c.path("thiet_bi").path("giao_vien")));
    d.para("2. Học sinh: " + join(c.path("thiet_bi").path("hoc_sinh")));

    d.heading("III. Tiến trình dạy học");
    List<List<String>> rows = new ArrayList<>();
    int total = 0;
    for (JsonNode a : c.path("hoat_dong")) {
      boolean home = a.path("o_nha").asBoolean(false);
      int min = a.path("thoi_luong_phut").asInt(0);
      if (!home) {
        total += min;
      }
      JsonNode tc = a.path("to_chuc");
      rows.add(
          List.of(
              a.path("ma").asString("").replace("HD", "HĐ")
                  + ". "
                  + KIND.getOrDefault(a.path("loai").asString(""), "")
                  + ": "
                  + a.path("ten").asString(""),
              home ? "ở nhà" : min + "'",
              a.path("muc_tieu").asString(""),
              a.path("noi_dung").asString(""),
              a.path("san_pham").asString(""),
              "Chuyển giao: "
                  + tc.path("chuyen_giao").asString("")
                  + "\nThực hiện: "
                  + tc.path("thuc_hien").asString("")
                  + "\nBáo cáo: "
                  + tc.path("bao_cao").asString("")
                  + "\nKết luận: "
                  + tc.path("ket_luan").asString("")));
    }
    d.table(
        List.of("Hoạt động", "Thời lượng", "Mục tiêu", "Nội dung", "Sản phẩm", "Tổ chức thực hiện"),
        rows);
    d.para("Tổng thời lượng trên lớp: " + total + "'");

    d.heading("IV. Phiếu học tập");
    if (c.path("phieu_hoc_tap").isEmpty()) {
      d.para("Không có phiếu học tập.");
    }
    for (JsonNode ph : c.path("phieu_hoc_tap")) {
      d.subheading("Phiếu học tập " + ph.path("ma").asString(""));
      List<List<String>> qs = new ArrayList<>();
      for (JsonNode q : ph.path("cau")) {
        boolean verified =
            "kho".equals(q.path("dap_so_nguon").asString("")) && q.hasNonNull("dap_so");
        qs.add(
            List.of(
                String.valueOf(q.path("so").asInt()),
                q.path("muc_do").asString(""),
                q.path("nguon").asString(""),
                q.path("de").asString(""),
                verified ? q.path("dap_so").asString("") : UNVERIFIED));
      }
      d.table(List.of("Câu", "Mức độ", "Nguồn", "Đề bài", "Đáp số"), qs);
    }
    if (!approved) {
      d.footer(DRAFT_FOOTER);
    }
    return d.build();
  }

  private static String join(JsonNode arr) {
    List<String> out = new ArrayList<>();
    arr.forEach(x -> out.add(x.asString("")));
    return String.join(", ", out);
  }
}
