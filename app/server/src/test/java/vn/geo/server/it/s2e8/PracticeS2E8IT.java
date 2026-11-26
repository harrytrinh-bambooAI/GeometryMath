package vn.geo.server.it.s2e8;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.assignment.internal.repo.S2E8SandboxRepository;
import vn.geo.server.assignment.internal.service.S2E8SandboxService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E8 — tích hợp: giao bài luyện thành bộ bài chung (US-S2E8-05-SC5, SC10), ảnh chụp đáp số
 * (FC2), xem trước không ghi gì (SC4), xác nhận báo chấm sai (SC8), biên giờ mở lời giải
 * (US-S2E8-04-BV1).
 */
class PracticeS2E8IT extends AbstractE1IT {

  static final String ORG = S2E8SandboxRepository.ORG.toString();

  @Autowired S2E8SandboxService sandbox;

  record Sb(String teacher, String other, String an, UUID anId, String binh, UUID classId) {}

  Sb sb() {
    var out =
        sandbox.create(List.of(new S2E8SandboxService.ClassSpec("11A1", List.of("An", "Bình"))));
    var c = out.classes().getFirst();
    return new Sb(
        out.teacher().token(),
        out.otherTeacher().token(),
        c.students().get("An").token(),
        c.students().get("An").id(),
        c.students().get("Bình").token(),
        c.id());
  }

  Map<String, String> versions(Sb s) throws Exception {
    Map<String, String> out = new HashMap<>();
    json(get("/api/v1/teacher/practice/problems", s.teacher()))
        .get("items")
        .forEach(p -> out.put(p.get("code").asString(), p.get("problemVersionId").asString()));
    return out;
  }

  /** BL-1 (FX-20): 5 câu, câu 2 là FX-14 kiểu xấp xỉ ±0,5 ba lượt; giờ mở T0 + 2 giờ. */
  JsonNode draftBl1(Sb s) throws Exception {
    Map<String, String> v = versions(s);
    String body =
        """
        {"title":"Góc trong hình chóp","releaseAt":"%s","items":[
          {"problemVersionId":"%s"},
          {"problemVersionId":"%s","answerKind":"approx","tolerance":0.5,"maxAttempts":3},
          {"problemVersionId":"%s"},
          {"problemVersionId":"%s"},
          {"problemVersionId":"%s"}]}
        """
            .formatted(
                Instant.now().plusSeconds(7200),
                v.get("LC-01"),
                v.get("SP-01"),
                v.get("LA-01"),
                v.get("LB-01"),
                v.get("HCN-01"));
    var r = postJson("/api/v1/teacher/practice-sets", s.teacher(), body);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(201);
    return json(r);
  }

  JsonNode assign(Sb s, String setId) throws Exception {
    var r =
        postJson(
            "/api/v1/teacher/practice-sets/" + setId + "/assign",
            s.teacher(),
            "{\"classId\":\"" + s.classId() + "\"}");
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  JsonNode attempt(String token, String assignmentId, String answer) throws Exception {
    var r =
        postJson(
            "/api/v1/student/practice-items/" + assignmentId + "/attempts",
            token,
            mapper.writeValueAsString(
                Map.of("answer", answer, "requestKey", UUID.randomUUID().toString())));
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  static String item(JsonNode set, int position) {
    return set.get("items").get(position - 1).get("assignmentId").asString();
  }

  @Test
  @DisplayName("TC-S2E8-092 Giao bài chụp lại cấu hình từng câu")
  void tc092_assignSnapshots() throws Exception {
    Sb s = sb();
    String setId = draftBl1(s).get("id").asString();
    JsonNode set = assign(s, setId);
    assertThat(set.get("status").asString()).isEqualTo("assigned");
    var rows =
        jdbc.sql(
                "SELECT position, answer_snapshot, answer_kind::text, tolerance, max_attempts FROM assignment_item"
                    + " WHERE set_id = :id ORDER BY position")
            .param("id", UUID.fromString(setId))
            .query((rs, i) ->
                rs.getInt(1)
                    + "|"
                    + rs.getString(2)
                    + "|"
                    + rs.getString(3)
                    + "|"
                    + rs.getBigDecimal(4)
                    + "|"
                    + rs.getInt(5))
            .list();
    assertThat(rows)
        .containsExactly(
            "1|6|exact|null|3",
            "2|45°|approx|0.50|3",
            "3|4√3|exact|null|3",
            "4|1/2|exact|null|3",
            "5|10|exact|null|3");
    // học sinh 11A1 thấy bài; dữ liệu trả về không có đáp số
    var mine = get("/api/v1/student/practice-sets", s.an());
    assertThat(mine.body()).contains(setId).doesNotContain("answer");
    assertThat(json(get("/api/v1/student/practice-sets/" + setId, s.an())).get("items").size())
        .isEqualTo(5);
  }

  @Test
  @DisplayName("TC-S2E8-114 Bài luyện được lưu thành bộ bài dùng chung mô hình với S3-E2")
  void tc114_oneSetModel() throws Exception {
    Sb s = sb();
    String setId = draftBl1(s).get("id").asString();
    assign(s, setId);
    assertThat(
            count(
                "SELECT count(*) FROM assignment_set WHERE id = '"
                    + setId
                    + "' AND status = 'assigned' AND class_id = '"
                    + s.classId()
                    + "'"))
        .isEqualTo(1);
    // 5 AssignmentItem, mỗi item một hàng assignment MVP (kind = class) của đúng lớp 11A1
    assertThat(
            count(
                "SELECT count(*) FROM assignment_item i JOIN assignment a ON a.org_id = i.org_id AND a.id = i.assignment_id"
                    + " WHERE i.set_id = '"
                    + setId
                    + "' AND a.kind = 'class' AND a.class_id = '"
                    + s.classId()
                    + "' AND i.max_attempts BETWEEN 1 AND 5"))
        .isEqualTo(5);
    // không có bảng bài luyện riêng
    assertThat(
            count(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'"
                    + " AND table_name IN ('practice_set', 'practice_item')"))
        .isZero();
    // giao lại: bộ đã giao không giao lần hai, không sinh thêm hàng
    var again =
        postJson(
            "/api/v1/teacher/practice-sets/" + setId + "/assign",
            s.teacher(),
            "{\"classId\":\"" + s.classId() + "\"}");
    assertThat(again.statusCode()).isEqualTo(409);
    assertThat(count("SELECT count(*) FROM assignment WHERE class_id = '" + s.classId() + "'"))
        .isEqualTo(5);
  }

  @Test
  @DisplayName("TC-S2E8-094 Sửa đáp số của đề gốc không đổi snapshot bài đã giao")
  void tc094_snapshotSurvivesSourceEdit() throws Exception {
    Sb s = sb();
    JsonNode set = assign(s, draftBl1(s).get("id").asString());
    UUID v1 = UUID.fromString(versions(s).get("SP-01"));
    UUID v2 = UUID.randomUUID();
    try {
      // "sửa đề gốc thành đáp số 60": lời giải bất biến (BR-11) nên sửa = phiên bản mới thành phiên
      // bản hiện hành
      jdbc.sql(
              """
              INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, approval_status,
                                           ai_authored, created_by)
              SELECT :v2, pv.org_id, pv.problem_id, 2, pv.statement, sha256(convert_to('s2e8-v2', 'UTF8')),
                     pv.approval_status, pv.ai_authored, pv.created_by
              FROM problem_version pv WHERE pv.id = :v1
              """)
          .param("v1", v1)
          .param("v2", v2)
          .update();
      jdbc.sql(
              """
              INSERT INTO solution (org_id, problem_version_id, final_answer, steps, model, prompt_version, author_user_id,
                                    ai_authored)
              SELECT so.org_id, :v2, '60°', so.steps, so.model, so.prompt_version, so.author_user_id, so.ai_authored
              FROM solution so WHERE so.problem_version_id = :v1
              """)
          .param("v1", v1)
          .param("v2", v2)
          .update();
      jdbc.sql("UPDATE problem SET current_version_id = :v2 WHERE current_version_id = :v1")
          .param("v1", v1)
          .param("v2", v2)
          .update();
      assertThat(versions(s).get("SP-01")).isEqualTo(v2.toString());
      JsonNode r = attempt(s.an(), item(set, 2), "45");
      assertThat(r.get("outcome").asString()).isEqualTo("correct");
      assertThat(attempt(s.binh(), item(set, 2), "60").get("outcome").asString())
          .isEqualTo("wrong");
    } finally {
      jdbc.sql("UPDATE problem SET current_version_id = :v1 WHERE current_version_id = :v2")
          .param("v1", v1)
          .param("v2", v2)
          .update();
    }
  }

  @Test
  @DisplayName("TC-S2E8-091 Xem trước như học sinh không ghi gì")
  void tc091_previewWritesNothing() throws Exception {
    Sb s = sb();
    String setId = draftBl1(s).get("id").asString();
    long before = count("SELECT count(*) FROM practice_attempt");
    String path = "/api/v1/teacher/practice-sets/" + setId + "/preview/check";
    for (String wrong : List.of("60", "61")) {
      var r = postJson(path, s.teacher(), "{\"position\":2,\"answer\":\"" + wrong + "\"}");
      assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
      assertThat(json(r).get("outcome").asString()).isEqualTo("wrong");
      assertThat(json(r).get("hint").asString()).contains("hình chiếu");
      assertThat(r.body()).doesNotContain("45");
    }
    var ok = postJson(path, s.teacher(), "{\"position\":2,\"answer\":\"45\"}");
    assertThat(json(ok).get("outcome").asString()).isEqualTo("correct");
    assertThat(count("SELECT count(*) FROM practice_attempt")).isEqualTo(before);
    // bài còn nháp: học sinh chưa thấy; giáo viên khác không xem trước được
    assertThat(get("/api/v1/student/practice-sets/" + setId, s.an()).statusCode()).isEqualTo(404);
    assertThat(postJson(path, s.other(), "{\"position\":2,\"answer\":\"45\"}").statusCode())
        .isEqualTo(404);
    // giao rồi mở bài: lượt về đủ (xem trước không trừ lượt của ai)
    assign(s, setId);
    JsonNode play = json(get("/api/v1/student/practice-sets/" + setId, s.an()));
    assertThat(play.get("items").get(1).get("attemptsLeft").asInt()).isEqualTo(3);
  }

  @Test
  @DisplayName("TC-S2E8-106 Xác nhận đúng thì câu của em thành Đúng và đếm vào chỉ số")
  void tc106_confirmReport() throws Exception {
    Sb s = sb();
    String setId = draftBl1(s).get("id").asString();
    JsonNode set = assign(s, setId);
    String q2 = item(set, 2);
    assertThat(attempt(s.an(), q2, "44,4").get("outcome").asString()).isEqualTo("wrong");
    var sent =
        postJson(
            "/api/v1/student/practice-items/" + q2 + "/report",
            s.an(),
            "{\"note\":\"Em đo ra 44,4\"}");
    assertThat(sent.statusCode()).as(sent.body()).isEqualTo(201);
    long m0 =
        json(get("/api/v1/teacher/practice-reports/metrics", s.teacher()))
            .get("confirmed")
            .asLong();
    JsonNode reports = json(get("/api/v1/teacher/practice-reports", s.teacher())).get("items");
    assertThat(reports.size()).isEqualTo(1);
    assertThat(reports.get(0).get("submitted").asString()).isEqualTo("44,4");
    assertThat(reports.get(0).get("canonical").asString()).isEqualTo("45°");
    String reportId = reports.get(0).get("id").asString();
    var done =
        postJson(
            "/api/v1/teacher/practice-reports/" + reportId + "/decision",
            s.teacher(),
            "{\"decision\":\"confirm\"}");
    assertThat(done.statusCode()).as(done.body()).isEqualTo(200);
    assertThat(json(done).get("status").asString()).isEqualTo("confirmed");
    // câu 2 của An thành Đúng ở lần mở sau
    JsonNode play = json(get("/api/v1/student/practice-sets/" + setId, s.an()));
    assertThat(play.get("items").get(1).get("state").asString()).isEqualTo("correct");
    // dạng viết "44,4" thêm vào danh sách chấp nhận của câu: bạn cùng lớp nộp "44,4" được tính Đúng
    assertThat(
            jdbc.sql("SELECT accepted_forms::text FROM assignment_item WHERE assignment_id = :id")
                .param("id", UUID.fromString(q2))
                .query(String.class)
                .single())
        .contains("44,4");
    assertThat(attempt(s.binh(), q2, "44,4").get("outcome").asString()).isEqualTo("correct");
    // M-S2-07 tăng 1; xác nhận lần hai bị từ chối
    assertThat(
            json(get("/api/v1/teacher/practice-reports/metrics", s.teacher()))
                .get("confirmed")
                .asLong())
        .isEqualTo(m0 + 1);
    assertThat(
            postJson(
                    "/api/v1/teacher/practice-reports/" + reportId + "/decision",
                    s.teacher(),
                    "{\"decision\":\"keep\"}")
                .statusCode())
        .isEqualTo(409);
  }

  /** Trong MỘT giao dịch: giờ mở của câu = now() + giây rồi hỏi cổng lời giải; luôn ROLLBACK. */
  private String gateAt(UUID org, UUID user, String assignmentId, double seconds)
      throws SQLException {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      try (Statement st = c.createStatement()) {
        st.execute(
            "UPDATE assignment SET release_at = now() + make_interval(secs => "
                + seconds
                + ") WHERE id = '"
                + assignmentId
                + "'");
        var rs =
            st.executeQuery(
                "SELECT status, final_answer FROM fn_get_solution('"
                    + org
                    + "', '"
                    + user
                    + "', '"
                    + assignmentId
                    + "')");
        rs.next();
        String out = rs.getString(1) + "|" + rs.getString(2);
        c.rollback();
        return out;
      }
    }
  }

  @Test
  @DisplayName("TC-S2E8-080 Một giây trước giờ mở còn khóa, đúng giờ mở thì mở")
  void tc080_releaseBoundary() throws Exception {
    Sb s = sb();
    JsonNode set = assign(s, draftBl1(s).get("id").asString());
    String q2 = item(set, 2);
    UUID org = UUID.fromString(ORG);
    assertThat(gateAt(org, s.anId(), q2, 1)).isEqualTo("LOCKED|null"); // R − 1 giây
    assertThat(gateAt(org, s.anId(), q2, 0)).isEqualTo("OK|45°"); // đúng R
    // trước giờ mở, đường HTTP của học sinh không trả lời giải
    var locked = get("/api/v1/student/assignments/" + q2 + "/solution", s.an());
    assertThat(locked.statusCode()).isEqualTo(403);
    assertThat(locked.body()).doesNotContain("45°");
  }
}
