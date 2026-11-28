package vn.geo.server.lessonplan.internal.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import vn.geo.server.common.error.AppException;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Knowledge;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Period;

/**
 * Form "Soạn giáo án mới" (US-S5E1-01): trường B bắt buộc, trường C tuỳ chọn (truong.md v1.1). Máy
 * chủ kiểm lại mọi ràng buộc (BR-S5E1-03); form web chỉ là lớp tiện lợi.
 *
 * @param tietPpct mã Tiết PPCT (lp_ppct_period.id, ví dụ "B26-T1")
 * @param doiTuong co-ban | kha | chuyen
 * @param phongCach một trong 5 phong cách; null = truyen-thong (chọn sẵn)
 * @param kienThuc mã kiến thức trọng tâm (≥ 1, thuộc bài)
 * @param soTiet 1–5; null = suy từ Tiết PPCT
 * @param phieuHocTap null = Bật
 * @param soBaiTap 0–20; null = trợ lý tự chọn
 * @param tiLe NB/TH/VD/VDC; tổng phải bằng số bài tập
 * @param ghiChu ≤ 500 ký tự, gửi như DỮ LIỆU
 */
public record LessonForm(
    String tietPpct,
    String doiTuong,
    String phongCach,
    List<String> kienThuc,
    Integer soTiet,
    Boolean phieuHocTap,
    Integer soBaiTap,
    Map<String, Integer> tiLe,
    String phanHoa,
    String phanMem,
    List<String> thietBi,
    String ghiChu) {

  public static final String MSG_REQUIRED =
      "Chọn Tiết PPCT, Đối tượng và ít nhất một kiến thức trọng tâm."; // MSG-S5-01
  public static final String MSG_RATIO =
      "Tổng tỉ lệ NB/TH/VD/VDC phải bằng số bài tập."; // MSG-S5-02
  public static final String MSG_NOTE = "Ghi chú tối đa 500 ký tự."; // MSG-S5-07

  static final Set<String> AUDIENCES = Set.of("co-ban", "kha", "chuyen");
  static final Map<String, String> STYLES =
      Map.of(
          "truyen-thong", "truyền thống",
          "kham-pha", "khám phá",
          "lop-hoc-dao-nguoc", "lớp học đảo ngược",
          "du-an-stem", "dự án STEM",
          "tro-choi", "trò chơi");
  static final Map<String, String> AUDIENCE_LABEL =
      Map.of("co-ban", "Cơ bản", "kha", "Khá", "chuyen", "Chuyên");
  static final List<String> LEVELS = List.of("NB", "TH", "VD", "VDC");

  /**
   * Kiểm và chuẩn hoá (mặc định Truyền thống, Phiếu học tập Bật, Số tiết suy từ Tiết PPCT). Lỗi:
   * VALIDATION_FAILED với details[0].field = trường sai.
   *
   * @param period Tiết PPCT tìm theo {@link #tietPpct}; null = không có
   * @param lessonKnowledge đơn vị kiến thức của bài (để kiểm "thuộc bài")
   */
  public LessonForm validated(Period period, List<Knowledge> lessonKnowledge) {
    if (tietPpct == null || tietPpct.isBlank() || period == null) {
      throw AppException.invalidField("tietPpct", MSG_REQUIRED);
    }
    if (doiTuong == null || !AUDIENCES.contains(doiTuong)) {
      throw AppException.invalidField("doiTuong", MSG_REQUIRED);
    }
    if (kienThuc == null || kienThuc.stream().allMatch(k -> k == null || k.isBlank())) {
      throw AppException.invalidField("kienThuc", MSG_REQUIRED);
    }
    Set<String> ofLesson =
        lessonKnowledge.stream()
            .filter(k -> !"loai-bo".equals(k.status()))
            .map(Knowledge::code)
            .collect(java.util.stream.Collectors.toSet());
    List<String> focus =
        kienThuc.stream().filter(k -> k != null && !k.isBlank()).distinct().toList();
    if (!ofLesson.containsAll(focus)) {
      throw AppException.invalidField("kienThuc", "Kiến thức trọng tâm phải thuộc bài đã chọn.");
    }
    String style = phongCach == null || phongCach.isBlank() ? "truyen-thong" : phongCach;
    if (!STYLES.containsKey(style)) {
      throw AppException.invalidField("phongCach", "Phong cách không hợp lệ.");
    }
    int periods = soTiet == null ? period.periods() : soTiet;
    if (periods < 1 || periods > 5) {
      throw AppException.invalidField("soTiet", "Số tiết từ 1 đến 5.");
    }
    if (soBaiTap != null && (soBaiTap < 0 || soBaiTap > 20)) {
      throw AppException.invalidField("soBaiTap", "Số bài tập từ 0 đến 20.");
    }
    Map<String, Integer> ratio = null;
    if (tiLe != null && !tiLe.isEmpty()) {
      int sum = 0;
      for (Map.Entry<String, Integer> e : tiLe.entrySet()) {
        if (!LEVELS.contains(e.getKey()) || e.getValue() == null || e.getValue() < 0) {
          throw AppException.invalidField("tiLe", MSG_RATIO);
        }
        sum += e.getValue();
      }
      if (soBaiTap == null || sum != soBaiTap) {
        throw AppException.invalidField("tiLe", MSG_RATIO);
      }
      java.util.LinkedHashMap<String, Integer> ordered = new java.util.LinkedHashMap<>();
      for (String l : LEVELS) {
        ordered.put(l, tiLe.getOrDefault(l, 0));
      }
      ratio = ordered;
    }
    if (ghiChu != null && ghiChu.codePointCount(0, ghiChu.length()) > 500) {
      throw AppException.invalidField("ghiChu", MSG_NOTE);
    }
    return new LessonForm(
        tietPpct,
        doiTuong,
        style,
        focus,
        periods,
        phieuHocTap == null || phieuHocTap,
        soBaiTap,
        ratio,
        blankToNull(phanHoa),
        blankToNull(phanMem),
        thietBi == null
            ? List.of()
            : thietBi.stream().filter(s -> s != null && !s.isBlank()).toList(),
        blankToNull(ghiChu));
  }

  private static String blankToNull(String s) {
    return s == null || s.isBlank() ? null : s.strip();
  }
}
