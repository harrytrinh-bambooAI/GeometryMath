package vn.geo.server.aigateway.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** S2-E4 — đơn vị: đáp mẫu giả lập của task authoring_chat (chat chỉnh nháp đề, D-GEO-047). */
class MockAuthoringRepliesTest {

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final MockAuthoringReplies mock = new MockAuthoringReplies(mapper);

  private JsonNode ask(String request) {
    String prompt =
        "Đề bài: Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.\nĐáp số đề ghi: (chưa có)"
            + "\nYêu cầu của giáo viên: "
            + request;
    return mapper.readTree(mock.reply(prompt));
  }

  @Test
  @DisplayName("S2-E4 mock: yêu cầu rõ trả đề xuất có trước / sau / nguồn; mơ hồ thì hỏi lại")
  void repliesByRequest() {
    JsonNode p = ask("đổi cạnh BC thành 14");
    assertThat(p.path("kind").asString()).isEqualTo("proposal");
    assertThat(p.path("target").asString()).isEqualTo("statement");
    assertThat(p.path("before").asString()).isEqualTo("BC = 13");
    assertThat(p.path("after").asString()).isEqualTo("BC = 14");
    assertThat(p.path("source").asString()).isEqualTo("nháp hiện tại");
    assertThat(p.path("unsure").asString()).isNotBlank();

    JsonNode q = ask("làm đề khó hơn");
    assertThat(q.path("kind").asString()).isEqualTo("question");
    assertThat(q.path("text").asString())
        .isEqualTo("Khó hơn theo hướng nào: thêm ý hỏi, đổi số, hay thêm bước?");
    assertThat(q.has("before")).isFalse();

    JsonNode a = ask("sửa đáp số thành 80°.");
    assertThat(a.path("target").asString()).isEqualTo("answer");
    assertThat(a.path("after").asString()).isEqualTo("80°");

    assertThat(ask("đổi cạnh XY thành 3").path("kind").asString()).isEqualTo("question");
    assertThat(ask("chào").path("kind").asString()).isEqualTo("question");
    assertThat(MockAuthoringReplies.handles("authoring_chat")).isTrue();
    assertThat(MockAuthoringReplies.handles("draft_read")).isFalse();
  }
}
