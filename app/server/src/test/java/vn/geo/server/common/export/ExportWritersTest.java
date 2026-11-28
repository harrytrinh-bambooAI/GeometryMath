package vn.geo.server.common.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.common.export.ExportFile.Format;

/**
 * Bộ dựng xuất dùng chung (T-261128-54, PLAN_code-s5 P2): DOCX (POI), PDF (pdfbox + DejaVu), ZIP.
 */
class ExportWritersTest {

  private static ExportDoc sample(String footer, int rows) {
    List<List<String>> r = new ArrayList<>();
    for (int i = 1; i <= rows; i++) {
      r.add(
          List.of(
              "HĐ" + i, i * 5 + "'", "Khoảng cách từ điểm đến mặt phẳng (SBC): √6/3, SA ⊥ (ABCD)"));
    }
    return ExportDoc.builder("KẾ HOẠCH BÀI DẠY")
        .header("Mã đề 101")
        .heading("I. Mục tiêu")
        .para("Học sinh tính được khoảng cách.\nDòng thứ hai.")
        .bold("Câu 1 (0.25 điểm).")
        .table(List.of("Hoạt động", "Thời lượng", "Nội dung"), r)
        .footer(footer)
        .build();
  }

  @Test
  @DisplayName("DOCX mở lại được: chữ tiếng Việt, bảng, đầu trang và chân trang")
  @SuppressWarnings("unchecked")
  void docxRoundTrip() {
    ExportFile f =
        ExportFile.render(
            sample("Bản nháp AI soạn – chưa duyệt", 4), Format.DOCX, "GA_26_T1_20261128");
    assertThat(f.filename()).isEqualTo("GA_26_T1_20261128.docx");
    Map<String, Object> m = ExportInspector.inspect(f.filename(), f.bytes());
    assertThat(m.get("kind")).isEqualTo("docx");
    assertThat((String) m.get("text"))
        .contains("KẾ HOẠCH BÀI DẠY", "I. Mục tiêu", "Dòng thứ hai.", "SA ⊥ (ABCD)");
    List<List<List<String>>> tables = (List<List<List<String>>>) m.get("tables");
    assertThat(tables).hasSize(1);
    assertThat(tables.get(0)).hasSize(5);
    assertThat(tables.get(0).get(2))
        .containsExactly(
            "HĐ2", "10'", "Khoảng cách từ điểm đến mặt phẳng (SBC): √6/3, SA ⊥ (ABCD)");
    assertThat(String.join(" ", (List<String>) m.get("footers")))
        .contains("Bản nháp AI soạn – chưa duyệt");
    assertThat(String.join(" ", (List<String>) m.get("headers"))).contains("Mã đề 101");
  }

  @Test
  @DisplayName("PDF nhiều trang: đầu trang và chân trang ở MỌI trang, chữ tiếng Việt giữ nguyên")
  @SuppressWarnings("unchecked")
  void pdfEveryPage() {
    ExportFile f = ExportFile.render(sample("Bản nháp – chưa duyệt", 80), Format.PDF, "x");
    Map<String, Object> m = ExportInspector.inspect(f.filename(), f.bytes());
    assertThat(m.get("kind")).isEqualTo("pdf");
    List<String> pages = (List<String>) m.get("pages");
    assertThat(pages.size()).isGreaterThan(1);
    for (String p : pages) {
      assertThat(p).contains("Bản nháp – chưa duyệt").contains("Mã đề 101");
    }
    assertThat(pages.get(0)).contains("KẾ HOẠCH BÀI DẠY", "Khoảng cách");
  }

  @Test
  @DisplayName("PDF không chân trang khi footer null; ký tự phông không có thay bằng ?")
  @SuppressWarnings("unchecked")
  void pdfNoFooter() {
    ExportDoc d = ExportDoc.builder("Đề").para("Emoji 🙂 bị thay").build();
    Map<String, Object> m =
        ExportInspector.inspect("a.pdf", ExportFile.render(d, Format.PDF, "a").bytes());
    String text = String.join("", (List<String>) m.get("pages"));
    assertThat(text).doesNotContain("Bản nháp").contains("Emoji");
  }

  @Test
  @DisplayName("Tệp nén giữ tên và nội dung từng tệp")
  @SuppressWarnings("unchecked")
  void zip() {
    ExportFile a = ExportFile.render(sample(null, 1), Format.DOCX, "a");
    ExportFile b = ExportFile.render(sample(null, 1), Format.PDF, "b");
    ExportFile z = ExportFile.zip("ma-101.zip", List.of(a, b));
    Map<String, Object> m = ExportInspector.inspect(z.filename(), z.bytes());
    assertThat(m.get("kind")).isEqualTo("zip");
    List<Map<String, Object>> entries = (List<Map<String, Object>>) m.get("entries");
    assertThat(entries).extracting(e -> e.get("name")).containsExactly("a.docx", "b.pdf");
    assertThat(entries).extracting(e -> e.get("kind")).containsExactly("docx", "pdf");
  }

  @Test
  @DisplayName("Định dạng: rỗng = docx, pdf, giá trị lạ = null")
  void format() {
    assertThat(Format.parse(null)).isEqualTo(Format.DOCX);
    assertThat(Format.parse("PDF")).isEqualTo(Format.PDF);
    assertThat(Format.parse("xlsx")).isNull();
  }
}
