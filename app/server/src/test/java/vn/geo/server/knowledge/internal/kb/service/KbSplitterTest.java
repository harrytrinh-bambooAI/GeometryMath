package vn.geo.server.knowledge.internal.kb.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Section;

/** Tách mục, băm chuẩn hoá, đọc tệp, giáo án → Markdown của kho tri thức (T-261128-56). */
class KbSplitterTest {

  @Test
  @DisplayName("BR-S5E3-03 Tách mục theo tiêu đề; tiêu đề không thân bỏ qua; mã KT gắn vào mục")
  void splitsByHeadings() {
    List<Section> s =
        KbSplitter.split(
            "Lời dẫn\n# Tài liệu\n## A\nthân A KT11-C07-CT-003\n\n### B\nthân B\n## C\n", "Tên");
    assertThat(s).extracting(Section::title).containsExactly("Tên", "A", "B");
    assertThat(s.get(1).codes()).containsExactly("KT11-C07-CT-003");
    assertThat(s.get(2).body()).isEqualTo("thân B");
    assertThat(KbSplitter.split("chỉ có chữ", "Tên"))
        .extracting(Section::title)
        .containsExactly("Tên");
  }

  @Test
  @DisplayName("BR-S5E3-04 Băm sau chuẩn hoá: khác hoa thường, khoảng trắng vẫn trùng")
  void hashNormalizes() {
    assertThat(KbSplitter.hash("Hệ  thức\n1/AH²")).isEqualTo(KbSplitter.hash("hệ thức 1/ah² "));
    assertThat(KbSplitter.hash("a")).isNotEqualTo(KbSplitter.hash("b")).hasSize(64);
  }

  @Test
  @DisplayName("BR-S5E3-03 Đọc docx (Heading n → #), md UTF-8; sai nội dung = MSG-S5-22")
  void readsFiles() throws Exception {
    byte[] docx;
    try (XWPFDocument d = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      var h = d.createParagraph();
      h.setStyle("Heading1");
      h.createRun().setText("Hệ thức 1/AH²");
      d.createParagraph().createRun().setText("1/AH² = 1/SA² + 1/AK²");
      d.write(out);
      docx = out.toByteArray();
    }
    String md = KbText.toMarkdown("docx", docx, "x");
    assertThat(md).isEqualTo("# Hệ thức 1/AH²\n\n1/AH² = 1/SA² + 1/AK²");
    assertThat(KbText.toMarkdown("md", "## A\nb".getBytes(StandardCharsets.UTF_8), "x"))
        .isEqualTo("## A\nb");
    assertThatThrownBy(() -> KbText.toMarkdown("docx", "không phải zip".getBytes(), "MSG"))
        .hasMessage("MSG");
    assertThatThrownBy(() -> KbText.toMarkdown("pdf", "PK\3\4".getBytes(), "MSG"))
        .hasMessage("MSG");
    assertThatThrownBy(() -> KbText.toMarkdown("md", new byte[] {(byte) 0xC3, 0x28}, "MSG"))
        .hasMessage("MSG");
    assertThat(KbText.extension("a.DOCX")).isEqualTo("docx");
    assertThat(KbText.extension("b.xlsx")).isNull();
    assertThat(KbText.extension("md")).isNull();
  }

  @Test
  @DisplayName("US-S5E3-01-SC3 Giáo án → 4 mục khung 5512, không kèm phiếu học tập (đáp số)")
  void lessonPlanToFourSections() {
    var c =
        JsonMapper.builder()
            .build()
            .readTree(
                """
                {"meta":{"bai":"Bài 26. Khoảng cách","tiet":"Tiết 1/2"},
                 "muc_tieu":{"kien_thuc":[{"noi_dung":"Tính khoảng cách","kt":["KT11-C07-CT-003"]}]},
                 "hoat_dong":[{"loai":"mo-dau","ten":"Khởi động","thoi_luong_phut":5},
                              {"loai":"hinh-thanh","ten":"Hình thành","thoi_luong_phut":20},
                              {"loai":"luyen-tap","ten":"Luyện tập","thoi_luong_phut":15},
                              {"loai":"van-dung","ten":"Vận dụng","thoi_luong_phut":5}],
                 "phieu_hoc_tap":[{"cau":[{"dap_so":"a√6/3"}]}]}
                """);
    String title = KbLessonPlanMarkdown.title("GA-0001", c);
    assertThat(title).isEqualTo("Giáo án GA-0001 · Bài 26. Khoảng cách · Tiết 1/2");
    String md = KbLessonPlanMarkdown.markdown(title, c);
    List<Section> s = KbSplitter.split(md, title);
    assertThat(s)
        .extracting(x -> x.title().split(" — ")[0])
        .containsExactly(
            "Hoạt động 1: Mở đầu",
            "Hoạt động 2: Hình thành kiến thức",
            "Hoạt động 3: Luyện tập",
            "Hoạt động 4: Vận dụng");
    assertThat(md).doesNotContain("a√6/3");
    assertThat(s.get(0).codes()).containsExactly("KT11-C07-CT-003");
  }
}
