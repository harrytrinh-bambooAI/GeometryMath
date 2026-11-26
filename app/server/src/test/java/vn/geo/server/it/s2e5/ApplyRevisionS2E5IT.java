package vn.geo.server.it.s2e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * US-S2E5-05 (T-261126-22) — tích hợp: "Áp dụng cho lớp" chỉ đổi đúng bài giao được chọn, học sinh
 * lớp kia vẫn nhận phiên bản cũ (BR-S2E5-23); đáp số đổi thì cảnh báo, kết quả đã chấm giữ nguyên
 * (BR-S2E5-26). Dữ liệu: db/seed/e2e/840_s2e3_core.sql (làn 0): sổ v2 "Giáo viên đã duyệt" ghim vào
 * BG-1 (11A1, khóa) và BG-2 (11A2, đã mở).
 */
class ApplyRevisionS2E5IT extends AbstractE1IT {

  static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000840001");
  static final UUID TRANG = UUID.fromString("00000000-0000-7000-8002-000000840003");
  static final UUID AN = UUID.fromString("00000000-0000-7000-8002-000000840021");
  static final UUID BINH = UUID.fromString("00000000-0000-7000-8002-000000840023");
  static final String S1 = "SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.";
  static final String S2 = "ABCD vuông cạnh a nên AC = a√2.";
  static final String V2_S3 = "tan SCA = a√2 / a√2 = 1.";
  static final String V2_S4 = "Vậy góc giữa SC và mặt phẳng đáy bằng 45°.";

  static UUID f(long n) {
    return UUID.fromString(String.format("00000000-0000-7000-8002-%012d", n));
  }

  static UUID doc(int tc) {
    return f(843000000L + tc * 10L);
  }

  static UUID bg(int tc, int k) {
    return f(845000000L + (tc * 10L) * 10 + k);
  }

  String trang() {
    return token(ORG, TRANG, Role.TEACHER);
  }

  JsonNode ok(HttpResponse<String> r) {
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  /** Lưu v3 từ v2 rồi duyệt (nhãn máy vẫn Cần xem lại: gửi xác nhận). */
  void approvedV3(int tc, String... steps) throws Exception {
    ObjectNode body = mapper.createObjectNode().put("baseVersion", 2);
    ArrayNode arr = body.putArray("steps");
    for (String s : steps) {
      arr.add(s);
    }
    ok(postJson("/api/v1/teacher/solutions/" + doc(tc) + "/versions", trang(), body.toString()));
    ok(
        postJson(
            "/api/v1/teacher/solutions/" + doc(tc) + "/approve",
            trang(),
            "{\"version\":3,\"confirmNeedsReview\":true}"));
  }

  static int pinned(JsonNode view, String className) {
    for (JsonNode a : view.path("assignments")) {
      if (className.equals(a.path("className").asString())) {
        return a.path("version").asInt();
      }
    }
    return -1;
  }

  @Test
  @DisplayName("TC-S2E5-058 Áp cho BG-1 không đổi BG-2 cùng đề")
  void tc058_applyOnlyOne() throws Exception {
    String v3s3 = "Tam giác SAC vuông cân tại A nên tan SCA = 1.";
    approvedV3(58, S1, S2, v3s3, V2_S4);
    JsonNode after =
        ok(
            postJson(
                "/api/v1/teacher/solutions/" + doc(58) + "/assignments/" + bg(58, 1) + "/apply",
                trang(),
                "{\"version\":3}"));
    assertThat(pinned(after, "11A1")).isEqualTo(3);
    assertThat(pinned(after, "11A2")).isEqualTo(2);
    // học sinh 11A2 (Bình) vẫn nhận v2: nhãn và nội dung lời giải (BG-2 đã tới giờ mở)
    String binh = token(ORG, BINH, Role.STUDENT);
    JsonNode labels =
        ok(get("/api/v1/student/assignments/" + bg(58, 2) + "/solution-labels", binh));
    assertThat(labels.path("version").asInt()).isEqualTo(2);
    HttpResponse<String> sol = get("/api/v1/student/assignments/" + bg(58, 2) + "/solution", binh);
    assertThat(sol.statusCode()).as(sol.body()).isEqualTo(200);
    assertThat(sol.body()).contains(V2_S3).doesNotContain(v3s3);
    // học sinh 11A1 (An) nhận nhãn v3; lời giải BG-1 vẫn khóa tới giờ mở
    String an = token(ORG, AN, Role.STUDENT);
    assertThat(
            ok(get("/api/v1/student/assignments/" + bg(58, 1) + "/solution-labels", an))
                .path("version")
                .asInt())
        .isEqualTo(3);
    HttpResponse<String> locked = get("/api/v1/student/assignments/" + bg(58, 1) + "/solution", an);
    assertThat(locked.body()).doesNotContain(v3s3).doesNotContain("45°");
  }

  @Test
  @DisplayName("TC-S2E5-061 Đổi đáp số thì cảnh báo kết quả không tính lại")
  void tc061_answerChanged() throws Exception {
    UUID attempt = UUID.randomUUID();
    jdbc.sql(
            """
            INSERT INTO practice_attempt (id, org_id, assignment_id, user_id, request_key, submitted, outcome, wrong_after)
            VALUES (:id, :org, :a, :u, 'it-061', '45°', 'correct', 0)
            """)
        .param("id", attempt)
        .param("org", ORG)
        .param("a", bg(61, 1))
        .param("u", AN)
        .update();
    approvedV3(61, S1, S2, "tan SCA = √3.", "Vậy góc giữa SC và mặt phẳng đáy bằng 60°.");
    String path = "/api/v1/teacher/solutions/" + doc(61) + "/assignments/" + bg(61, 1) + "/apply";
    JsonNode preview = ok(get(path + "?version=3", trang()));
    assertThat(preview.path("warnings").toString())
        .contains("Đáp số đổi từ 45° thành 60°; kết quả đã chấm không tính lại");
    // chưa xác nhận: CONFLICT, bài giao giữ v2
    HttpResponse<String> noConfirm = postJson(path, trang(), "{\"version\":3}");
    assertThat(noConfirm.statusCode()).isEqualTo(409);
    assertThat(noConfirm.body())
        .contains("Đáp số đổi từ 45° thành 60°; kết quả đã chấm không tính lại");
    assertThat(
            count(
                "SELECT count(*) FROM solution_assignment_pin sp JOIN solution_version v ON v.id = sp.version_id"
                    + " WHERE sp.assignment_id = '"
                    + bg(61, 1)
                    + "' AND v.version = 2"))
        .isEqualTo(1);
    JsonNode done = ok(postJson(path, trang(), "{\"version\":3,\"confirm\":true}"));
    assertThat(pinned(done, "11A1")).isEqualTo(3);
    // kết quả đã chấm giữ nguyên
    var row =
        jdbc.sql("SELECT submitted, outcome FROM practice_attempt WHERE id = :id")
            .param("id", attempt)
            .query((rs, i) -> rs.getString(1) + "|" + rs.getString(2))
            .single();
    assertThat(row).isEqualTo("45°|correct");
  }
}
