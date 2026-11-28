package vn.geo.server.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.assessment.internal.ocr.service.OcrService;
import vn.geo.server.assessment.internal.submit.service.SubmitRules;

/** Luật thuần nộp bài và prompt đọc chữ (T-261128-59; US-S5E4-03, 04, 09). */
class SubmitRulesTest {

  private static final Instant DUE = Instant.parse("2026-12-11T13:00:00Z"); // 20:00 giờ VN

  @Test
  @DisplayName(
      "TC-S5E4-47 Biên hạn nộp: 20:00:00 đúng hạn, 20:00:01 muộn; 20:07 nhãn 'Nộp muộn 7 phút'")
  void lateBoundary() {
    assertThat(SubmitRules.lateSeconds(DUE, DUE)).isNull();
    assertThat(SubmitRules.lateSeconds(DUE, DUE.minusSeconds(1))).isNull();
    assertThat(SubmitRules.lateSeconds(DUE, DUE.plusSeconds(1))).isEqualTo(1);
    assertThat(SubmitRules.lateLabel(SubmitRules.lateSeconds(DUE, DUE.plusSeconds(1))))
        .isEqualTo("Nộp muộn 1 phút");
    assertThat(SubmitRules.lateLabel(SubmitRules.lateSeconds(DUE, DUE.plusSeconds(7 * 60))))
        .isEqualTo("Nộp muộn 7 phút");
    assertThat(SubmitRules.lateLabel(125 * 60)).isEqualTo("Nộp muộn 2 giờ 5 phút");
    assertThat(SubmitRules.lateLabel(null)).isNull();
  }

  @Test
  @DisplayName("TC-S5E4-15 Ngưỡng độ rõ: 0,50 đạt, 0,49 và 0,31 không đạt; MSG-S5-32 theo trang")
  void clarity() {
    assertThat(SubmitRules.clear(0.50)).isTrue();
    assertThat(SubmitRules.clear(0.49)).isFalse();
    assertThat(SubmitRules.clear(0.31)).isFalse();
    assertThat(SubmitRules.blurry(2)).isEqualTo("Ảnh trang 2 hơi mờ. Em chụp lại trang này nhé.");
  }

  @Test
  @DisplayName("TC-S5E4-16 Trần 8 MB/trang (8 388 608 byte) và 10 trang")
  void limits() {
    assertThat(SubmitRules.MAX_PAGE_BYTES).isEqualTo(8_388_608);
    assertThat(SubmitRules.MAX_PAGES).isEqualTo(10);
    assertThat(SubmitRules.PART_BYTES).isLessThanOrEqualTo(1_048_576);
  }

  @Test
  @DisplayName("TC-S5E4-19 Prompt grade_ocr chỉ có số trang và ảnh, không danh tính")
  void promptHasNoIdentity() {
    byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};
    String p = OcrService.prompt(2, jpeg);
    assertThat(p).startsWith(OcrService.MARK + " trang 2.");
    assertThat(p).contains("data:image/jpeg;base64,");
    assertThat(p).doesNotContain("HS-").doesNotContain("Bình An").doesNotContain("11A1");
    assertThat(p).contains("Không giải bài, không chấm");
  }

  @Test
  @DisplayName("TC-S5E4-14 Thông báo riêng tư S5 nói xử lý bằng AI và xoá cuối năm học")
  void privacyText() {
    assertThat(SubmitRules.MSG_36).contains("được xử lý bằng AI").contains("xoá cuối năm học");
    assertThat(SubmitRules.PRIVACY_VERSION).isEqualTo("privacy-s5-1.0");
  }
}
