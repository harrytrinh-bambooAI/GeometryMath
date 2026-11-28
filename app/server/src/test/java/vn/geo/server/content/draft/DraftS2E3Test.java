package vn.geo.server.content.draft;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.content.internal.draft.service.DraftViews;
import vn.geo.server.content.internal.draft.service.ImageSanitizer;
import vn.geo.server.content.internal.solution.service.AnswerCompare;

/** S2-E3 — đơn vị: ngưỡng ô vàng, đối chiếu ba đáp số, nhận diện và xóa siêu dữ liệu ảnh. */
class DraftS2E3Test {

  @Test
  @DisplayName("TC-S2E3-012 Ô tin cậy 0,80 không tô, 0,79 tô")
  void tc012_threshold() {
    assertThat(DraftViews.cellState(0.80)).isEqualTo("ok");
    assertThat(DraftViews.cellState(0.79)).isEqualTo("unsure");
    assertThat(DraftViews.cellState(0.95)).isEqualTo("ok");
    assertThat(DraftViews.cellState(0.60)).isEqualTo("unsure");
  }

  @Test
  @DisplayName("TC-S2E3-037 Lệch 0,01 coi là khớp, lệch 0,02 là lệch")
  void tc037_tolerance() {
    AnswerCompare.Result one = AnswerCompare.compare("6,93", null, 6.94);
    assertThat(one.state()).isEqualTo(AnswerCompare.MATCH);
    AnswerCompare.Result two = AnswerCompare.compare("6,93", null, 6.95);
    assertThat(two.state()).isEqualTo(AnswerCompare.MISMATCH);
    // đối xứng: AI giải lệch đúng 0,01 vẫn khớp
    assertThat(AnswerCompare.compare(null, "6,95", 6.94).state()).isEqualTo(AnswerCompare.MATCH);
  }

  @Test
  @DisplayName("TC-S2E3-038 Dạng viết tương đương tính là khớp")
  void tc038_equivalentForms() {
    AnswerCompare.Result r = AnswerCompare.compare("4√3", null, 6.9282);
    assertThat(r.state()).isEqualTo(AnswerCompare.MATCH);
    assertThat(r.stated()).isCloseTo(6.9282, org.assertj.core.data.Offset.offset(0.001));
    assertThat(AnswerCompare.compare("90°", "90 độ", 90.0).state()).isEqualTo(AnswerCompare.MATCH);
    assertThat(AnswerCompare.compare("90°", "80°", 90.0).state()).isEqualTo(AnswerCompare.MISMATCH);
  }

  @Test
  @DisplayName("TC-S2E3-039 Đề không có đáp số ghi sẵn thì không tính là lệch")
  void tc039_noStatedAnswer() {
    AnswerCompare.Result r = AnswerCompare.compare(null, "90°", 90.0);
    assertThat(r.state()).isEqualTo(AnswerCompare.MATCH);
    assertThat(r.stated()).isNull();
    assertThat(AnswerCompare.statedIn("Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A."))
        .isNull();
    assertThat(
            AnswerCompare.statedIn(
                "Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A. Đáp số: 90°"))
        .isEqualTo("90°");
  }

  @Test
  @DisplayName("Đáp số của lời giải và thay bằng đáp số máy (US-S2E3-04-SC5)")
  void lastValueAndReplace() {
    List<String> steps = List.of("BC^2 = 13^2 = 169.", "Vậy góc A = 80°.");
    assertThat(AnswerCompare.lastValue(steps)).isEqualTo("80°");
    assertThat(AnswerCompare.replaceLastValue("Vậy góc A = 80°.", "90°"))
        .isEqualTo("Vậy góc A = 90°.");
    assertThat(AnswerCompare.lastValue(List.of("Vậy góc giữa SC và mặt phẳng đáy bằng 45°.")))
        .isEqualTo("45°");
    assertThat(AnswerCompare.format(90.0, true)).isEqualTo("90°");
    assertThat(AnswerCompare.format(6.92820323, false)).isEqualTo("6,9282");
  }

  @Test
  @DisplayName(
      "Ảnh nhận theo nội dung; xóa EXIF/GPS, giữ chú thích và dữ liệu ảnh (BR-S2E3-06, 09)")
  void sanitizer() {
    byte[] img = DraftS2E3TestImages.jpegWithGps("S2E3-CLEAR");
    assertThat(ImageSanitizer.detect(img)).isEqualTo("jpeg");
    assertThat(ImageSanitizer.hasMetadata(img)).isTrue();
    byte[] clean = ImageSanitizer.strip(img);
    assertThat(ImageSanitizer.hasMetadata(clean)).isFalse();
    assertThat(new String(clean, StandardCharsets.ISO_8859_1)).contains("GEO-S2E3:S2E3-CLEAR");
    assertThat(ImageSanitizer.detect("GIF89a....".getBytes(StandardCharsets.ISO_8859_1))).isNull();
    assertThat(ImageSanitizer.detect("%PDF-1.7 ..".getBytes(StandardCharsets.ISO_8859_1))).isNull();
    byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    assertThat(ImageSanitizer.detect(png)).isEqualTo("png");
  }
}
