package vn.geo.server.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import vn.geo.server.assessment.internal.review.service.ReviewRules;

/** Luật chấm tay US-S5E4-06 (BR-S5E4-17) và câu chữ MSG-S5-37/38/39 (T-261128-60). */
class ReviewRulesTest {

  private static BigDecimal d(String s) {
    return new BigDecimal(s);
  }

  @Test
  void diemTieuChiTu0DenToiDaBuoc025() {
    assertThat(ReviewRules.valid(d("0"), d("4"))).isTrue();
    assertThat(ReviewRules.valid(d("3.5"), d("4"))).isTrue();
    assertThat(ReviewRules.valid(d("3.75"), d("4"))).isTrue();
    assertThat(ReviewRules.valid(d("4"), d("4"))).isTrue();
    assertThat(ReviewRules.valid(d("4.5"), d("4"))).isFalse();
    assertThat(ReviewRules.valid(d("0.6"), d("1"))).isFalse();
    assertThat(ReviewRules.valid(d("-0.25"), d("1"))).isFalse();
    assertThat(ReviewRules.valid(null, d("1"))).isFalse();
  }

  @Test
  void cauChu() {
    assertThat(ReviewRules.msg37("Các bước", d("4")))
        .isEqualTo("Điểm tiêu chí Các bước phải từ 0 đến 4, bước 0,25.");
    assertThat(ReviewRules.msg37("Hình", d("1.50"))).contains("từ 0 đến 1,5,");
    assertThat(ReviewRules.msg39("Kiểm tra 15 phút – Khoảng cách"))
        .isEqualTo("Em có bài mới được trả: Kiểm tra 15 phút – Khoảng cách.");
    assertThat(ReviewRules.updated("Kiểm tra 15 phút – Khoảng cách"))
        .isEqualTo("Điểm bài Kiểm tra 15 phút – Khoảng cách đã được thầy cô cập nhật");
    assertThat(ReviewRules.summary(4, 1, 0)).isEqualTo("4 chờ chấm · 1 chưa nộp · 0 đã duyệt");
  }
}
