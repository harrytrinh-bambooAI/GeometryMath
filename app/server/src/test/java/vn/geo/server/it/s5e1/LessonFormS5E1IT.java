package vn.geo.server.it.s5e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** US-S5E1-01 — form "Soạn giáo án mới": máy chủ kiểm lại trường B/C, quyền theo quan hệ. */
class LessonFormS5E1IT extends AbstractS5E1IT {

  static final String MSG_01 = "Chọn Tiết PPCT, Đối tượng và ít nhất một kiến thức trọng tâm.";

  @Test
  @DisplayName("TC-S5E1-02 Gửi form hợp lệ tạo yêu cầu soạn")
  void validFormCreatesRequest() throws Exception {
    HttpResponse<String> r = post(BASE, lan(), F020);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(202);
    JsonNode b = json(r);
    assertThat(b.get("status").asString()).isEqualTo("drafting");
    assertThat(b.get("statusLabel").asString()).isEqualTo("Đang soạn");
    assertThat(b.get("humanLabel").asString()).isEqualTo("AI soạn");
    assertThat(b.get("code").asString()).matches("GA-\\d{4}");
    // thân phản hồi chỉ có mã và trạng thái: không trường nào của học sinh
    assertThat(new TreeSet<>(b.propertyNames()))
        .containsExactly("code", "draftId", "humanLabel", "status", "statusLabel");
    String row =
        jdbc.sql(
                "SELECT teacher_id || '|' || human_label || '|' || period_id FROM lesson_plan"
                    + " WHERE id = CAST(:id AS uuid)")
            .param("id", b.get("draftId").asString())
            .query(String.class)
            .single();
    assertThat(row).isEqualTo(LAN + "|ai|B26-T1");
    await(b.get("draftId").asString());
  }

  @Test
  @DisplayName("TC-S5E1-03 Thiếu Đối tượng hoặc bỏ hết kiến thức trọng tâm bị từ chối")
  void missingRequiredFields() throws Exception {
    long calls = aiCalls();
    HttpResponse<String> noAudience =
        post(
            BASE,
            lan(),
            "{\"tietPpct\":\"B26-T1\",\"kienThuc\":[\"KT11-C07-DN-010\",\"KT11-C07-CT-003\"]}");
    expectField(noAudience, "doiTuong", MSG_01);
    HttpResponse<String> noKnowledge =
        post(BASE, lan(), "{\"tietPpct\":\"B26-T1\",\"doiTuong\":\"kha\",\"kienThuc\":[]}");
    expectField(noKnowledge, "kienThuc", MSG_01);
    Thread.sleep(300);
    assertThat(aiCalls()).isEqualTo(calls);
  }

  @Test
  @DisplayName("TC-S5E1-04 Tổng tỉ lệ khác số bài tập bị từ chối")
  void ratioMustMatchCount() throws Exception {
    long calls = aiCalls();
    HttpResponse<String> r =
        post(
            BASE,
            lan(),
            withField("soBaiTap", "6")
                .replace("}", ",\"tiLe\":{\"NB\":2,\"TH\":2,\"VD\":1,\"VDC\":2}}"));
    expectField(r, "tiLe", "Tổng tỉ lệ NB/TH/VD/VDC phải bằng số bài tập.");
    Thread.sleep(300);
    assertThat(aiCalls()).isEqualTo(calls);
  }

  @Test
  @DisplayName("TC-S5E1-05 Ghi chú 501 ký tự bị từ chối, 500 ký tự được nhận")
  void noteLimit() throws Exception {
    HttpResponse<String> tooLong =
        post(BASE, lan(), withField("ghiChu", "\"" + "a".repeat(501) + "\""));
    expectField(tooLong, "ghiChu", "Ghi chú tối đa 500 ký tự.");
    HttpResponse<String> ok = post(BASE, lan(), withField("ghiChu", "\"" + "a".repeat(500) + "\""));
    assertThat(ok.statusCode()).as(ok.body()).isEqualTo(202);
    await(json(ok).get("draftId").asString());
  }

  @Test
  @DisplayName("TC-S5E1-06 Giáo viên khác hoặc học sinh không đọc được bản nháp của cô Lan")
  void othersCannotRead() throws Exception {
    HttpResponse<String> own = get(BASE + "/GA-0001", lan());
    assertThat(own.statusCode()).as(own.body()).isEqualTo(200);
    HttpResponse<String> khoi = get(BASE + "/GA-0001", khoi());
    assertThat(khoi.statusCode()).isEqualTo(404);
    assertThat(json(khoi).get("code").asString()).isEqualTo("NOT_FOUND");
    HttpResponse<String> an = get(BASE + "/GA-0001", binhAn());
    assertThat(an.statusCode()).isEqualTo(403);
    assertThat(json(an).get("code").asString()).isEqualTo("FORBIDDEN");
    for (HttpResponse<String> r : java.util.List.of(khoi, an)) {
      assertThat(r.body())
          .doesNotContain("Khởi động")
          .doesNotContain("PHT1")
          .doesNotContain("BM-007");
    }
    // cùng kết quả khi gọi theo id
    String id = json(own).get("draftId").asString();
    assertThat(get(BASE + "/" + id, khoi()).statusCode()).isEqualTo(404);
  }

  private void expectField(HttpResponse<String> r, String field, String message) {
    assertThat(r.statusCode()).as(r.body()).isEqualTo(400);
    JsonNode e = json(r);
    assertThat(e.get("code").asString()).isEqualTo("VALIDATION_FAILED");
    assertThat(e.get("message").asString()).isEqualTo(message);
    assertThat(e.get("details")).hasSize(1);
    assertThat(e.get("details").get(0).get("field").asString()).isEqualTo(field);
    Set<String> keys = new TreeSet<>(e.propertyNames());
    assertThat(keys).contains("requestId");
  }
}
