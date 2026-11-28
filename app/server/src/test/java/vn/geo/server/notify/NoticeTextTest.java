package vn.geo.server.notify;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.notify.internal.service.draft.NoticeText;
import vn.geo.server.notify.internal.service.draft.NoticeText.Sentence;

/** Xử lý văn bản nháp thông báo (US-S5E5-01; BR-S5E5-03, 14) — thuần logic. */
class NoticeTextTest {

  private static final Instant DUE = Instant.parse("2026-12-11T13:00:00Z"); // 20:00 giờ VN
  private static final List<String> NAMES = List.of("Chi Mai", "Minh Quân", "HS-8L2K-77AB");

  @Test
  @DisplayName("Gỡ tên học sinh (có dấu, bỏ dấu, mã) trước khi gửi AI")
  void scrubBeforeAi() {
    assertThat(NoticeText.scrub("Chi Mai nhầm SB; minh quan thieu buoc; HS-8L2K-77AB", NAMES))
        .isEqualTo("[HS] nhầm SB; [HS] thieu buoc; [HS]");
    assertThat(NoticeText.scrub("Chi Mai Anh", List.of())).isEqualTo("Chi Mai Anh");
  }

  @Test
  @DisplayName("Câu AI có tên học sinh: tên bị loại, câu gắn AI chưa chắc")
  void nameRemovedFromDraft() {
    List<Sentence> out =
        NoticeText.check(List.of("Nhắc Chi Mai và cả lớp nộp bài trước 20:00."), NAMES, DUE);
    assertThat(out.get(0).text()).isEqualTo("Nhắc cả lớp nộp bài trước 20:00.");
    assertThat(out.get(0).unsure()).isTrue();
  }

  @Test
  @DisplayName("Giờ / ngày hạn sai được thay bằng dữ kiện máy chủ")
  void wrongTimeReplaced() {
    List<Sentence> out =
        NoticeText.check(
            List.of("Hạn nộp 21:00 thứ Sáu.", "Nộp trước 20:00, 12/12/2026.", "Có 1 học sinh"),
            List.of(),
            DUE);
    assertThat(out.get(0).text()).isEqualTo("Hạn nộp 20:00 thứ Sáu.");
    assertThat(out.get(0).why()).isEqualTo("giờ hạn");
    assertThat(out.get(1).text()).isEqualTo("Nộp trước 20:00, 11/12/2026.");
    assertThat(out.get(2).unsure()).isFalse();
    assertThat(NoticeText.dueLabel(DUE)).isEqualTo("20:00, 11/12/2026");
    assertThat(NoticeText.weekday(DUE)).isEqualTo("thứ Sáu");
  }
}
