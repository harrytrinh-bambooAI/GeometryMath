package vn.geo.server.content.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vn.geo.server.content.internal.importer.service.ExerciseSplitter;
import vn.geo.server.content.internal.importer.service.ExerciseSplitter.Split;
import vn.geo.server.content.internal.importer.service.FileText;
import vn.geo.server.content.internal.importer.service.FileText.Extracted;

/**
 * S2-E4 — đơn vị (hàm thuần): đọc chữ docx / pdf và tách bài tập (US-S2E4-02). Ca gắn mã TC nằm ở
 * IT / E2E; ở đây kiểm từng quy tắc của bộ đọc và bộ tách.
 */
class ImportFilesTest {

  static final List<String> PDF_TWO =
      List.of(
          "Bài 1. Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.",
          "Bài 2. Hình vuông ABCD có AB = 6; tính AC.");

  @Test
  @DisplayName("S2-E4 docx: 3 bài tập + 1 phần lý thuyết → 3 mục, lý thuyết bị bỏ và được đếm")
  void docxThreeExercises() {
    Extracted ex =
        FileText.extract("giao-an.docx", ImportTestFiles.docx(ImportTestFiles.THREE, false));
    assertThat(ex.type()).isEqualTo("docx");
    assertThat(ex.macro()).isFalse();
    Split s = ExerciseSplitter.split(ex.paragraphs());
    assertThat(s.found()).isEqualTo(3);
    assertThat(s.theoryBlocks()).isEqualTo(1);
    assertThat(s.items())
        .extracting(ExerciseSplitter.Exercise::title)
        .containsExactly("Bài 1", "Bài 2", "Bài 3");
    assertThat(s.items().get(0).text()).isEqualTo(ImportTestFiles.FX10);
    assertThat(s.items().get(2).text()).isEqualTo(ImportTestFiles.FX12);
  }

  @Test
  @DisplayName(
      "S2-E4 tách bài: đoạn nối tiếp thuộc bài trước; 51 bài chỉ lấy 50; không nhãn bài = 0 mục")
  void splitterRules() {
    Split s =
        ExerciseSplitter.split(
            List.of(
                "ĐỀ ÔN TẬP",
                "Câu 1: Cho tam giác ABC.",
                "Biết AB = 3.",
                "câu 2) Tính AC.",
                "Ghi nhớ: công thức",
                "x = 1"));
    assertThat(s.items()).hasSize(2);
    assertThat(s.items().get(0).text()).isEqualTo("Cho tam giác ABC. Biết AB = 3.");
    assertThat(s.items().get(1).title()).isEqualTo("Câu 2");
    assertThat(s.theoryBlocks()).isEqualTo(1);

    List<String> many = new ArrayList<>();
    for (int i = 1; i <= 51; i++) {
      many.add("Bài " + i + ". Tính cạnh thứ " + i + " của hình số " + i + ".");
    }
    Split big = ExerciseSplitter.split(many);
    assertThat(big.found()).isEqualTo(51);
    assertThat(big.items()).hasSize(50);
    assertThat(big.items().get(49).title()).isEqualTo("Bài 50");
    assertThat(ExerciseSplitter.split(many.subList(0, 50)).items()).hasSize(50);

    assertThat(ExerciseSplitter.split(List.of("Chỉ có chữ thường", "không có nhãn bài")).items())
        .isEmpty();
  }

  @Test
  @DisplayName("S2-E4 docx có macro: đọc như văn bản, không nạp vbaProject.bin, có cờ macro")
  void docxMacro() {
    Extracted ex =
        FileText.extract("macro.docx", ImportTestFiles.docx(ImportTestFiles.THREE, true));
    assertThat(ex.macro()).isTrue();
    assertThat(String.join("\n", ex.paragraphs()))
        .doesNotContain("AutoOpen")
        .doesNotContain("Shell");
    assertThat(ExerciseSplitter.split(ex.paragraphs()).found()).isEqualTo(3);
  }

  @Test
  @DisplayName(
      "S2-E4 pdf: có lớp chữ thì tách như docx; ảnh quét, mật khẩu, tệp hỏng, xlsx bị từ chối đúng lý do")
  void pdfAndRejections() {
    Extracted ex = FileText.extract("de.pdf", ImportTestFiles.pdf(PDF_TWO, null));
    assertThat(ex.type()).isEqualTo("pdf");
    Split s = ExerciseSplitter.split(ex.paragraphs());
    assertThat(s.found()).isEqualTo(2);
    assertThat(s.items().get(0).text()).isEqualTo(ImportTestFiles.FX10);

    assertThatThrownBy(() -> FileText.extract("quet.pdf", ImportTestFiles.pdf(List.of(), null)))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Tệp không có lớp chữ. Hãy dùng Tải ảnh cho từng trang");
    assertThatThrownBy(() -> FileText.extract("khoa.pdf", ImportTestFiles.pdf(PDF_TWO, "bimat")))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Tệp đặt mật khẩu, hãy bỏ mật khẩu rồi tải lại");
    assertThatThrownBy(() ->
            FileText.extract(
                "hong.docx", "PK\u0003\u0004 không phải zip".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Không mở được tệp");
    assertThatThrownBy(
            () -> FileText.extract("hong.docx", "chữ thường".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Không mở được tệp");
    assertThatThrownBy(
            () -> FileText.extract("hong.pdf", "%PDF-1.7 rác".getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Không mở được tệp");
    assertThatThrownBy(() -> FileText.extract("bang.xlsx", ImportTestFiles.xlsx()))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Chỉ nhận docx và pdf");
    // xlsx đổi đuôi thành docx vẫn bị nhận ra theo nội dung
    assertThatThrownBy(() -> FileText.extract("bang.docx", ImportTestFiles.xlsx()))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Chỉ nhận docx và pdf");
    // docx đặt mật khẩu là tệp OLE
    byte[] ole = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0, 0, 0, 0};
    assertThatThrownBy(() -> FileText.extract("khoa.docx", ole))
        .isInstanceOf(FileText.Rejected.class)
        .hasMessage("Tệp đặt mật khẩu, hãy bỏ mật khẩu rồi tải lại");
  }

  /**
   * Ghi tệp pdf mẫu cho E2E (chạy tay: ./mvnw test -Dtest=ImportFilesTest
   * -Dgeo.s2e4.fixtures=write).
   */
  @Test
  @EnabledIfSystemProperty(named = "geo.s2e4.fixtures", matches = "write")
  void writeE2eFixtures() throws Exception {
    Path dir = Path.of("../e2e/tests/s2e4/fixtures");
    Files.createDirectories(dir);
    Files.write(dir.resolve("hai-bai.pdf"), ImportTestFiles.pdf(PDF_TWO, null));
    Files.write(dir.resolve("anh-quet.pdf"), ImportTestFiles.pdf(List.of(), null));
    Files.write(dir.resolve("mat-khau.pdf"), ImportTestFiles.pdf(PDF_TWO, "bimat"));
  }
}
