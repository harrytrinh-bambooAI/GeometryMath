package vn.geo.server.learning.internal.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.learning.internal.repo.HelpRepository.GuardContext;

/**
 * FIX3 #10 — đáp số một ký tự ("a" của HKG-02) chỉ chặn khi đứng làm kết luận (D-GEO-156; F-GEO-238
 * chặn, F-GEO-116/239 không chặn). Dev-E4-07.
 */
class AnswerGuardShortAnswerTest {

  private final AnswerGuard guard = new AnswerGuard(null, JsonMapper.builder().build());

  private final GuardContext hkg02 = new GuardContext("OK", true, "a", "[]", null, null);

  @Test
  @DisplayName("Dev-E4-07 \"a\" làm kết luận bị chặn; \"cạnh a\" trong câu thường không chặn")
  void shortAnswerOnlyAsConclusion() {
    assertThat(guard.leaks(hkg02, "Vậy khoảng cách giữa AC và B′D′ bằng a.")).isTrue();
    assertThat(guard.leaks(hkg02, "d(AC, B′D′) = a")).isTrue();
    assertThat(guard.leaks(hkg02, "Đáp số là a, em nhé")).isTrue();
    assertThat(guard.leaks(hkg02, "Trong hình lập phương cạnh a, mọi cạnh đều bằng nhau."))
        .isFalse();
    assertThat(guard.leaks(hkg02, "Em xét mặt phẳng (ABCD) và (A′B′C′D′).")).isFalse();
    assertThat(guard.leaks(hkg02, "Khoảng cách bằng a√2 không đúng đâu")).isFalse();
  }

  @Test
  @DisplayName("Dev-E4-07 đáp số nhiều ký tự vẫn khớp như cũ")
  void longAnswersUnchanged() {
    GuardContext g = new GuardContext("OK", true, "45°", "[]", null, null);
    assertThat(guard.leaks(g, "Góc đó bằng 45 độ")).isTrue();
    assertThat(guard.leaks(g, "Góc đó là π/4")).isTrue();
    assertThat(guard.leaks(g, "Em tìm hình chiếu của SD trước")).isFalse();
  }
}
