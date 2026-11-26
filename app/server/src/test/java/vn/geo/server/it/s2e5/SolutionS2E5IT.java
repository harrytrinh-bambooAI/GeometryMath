package vn.geo.server.it.s2e5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.solution.service.SolutionReviewService;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E5 — tích hợp (máy chủ + CSDL): StepCheck gắn với phiên bản (US-S2E5-02), nhãn máy chỉ do
 * engine gắn (US-S2E5-03), bản duyệt trước thành "Bị thay thế" (US-S2E5-04-SC7). Dữ liệu:
 * db/seed/e2e/820_s2e5_core.sql, mỗi ca một sổ lời giải riêng (làn 0).
 */
class SolutionS2E5IT extends AbstractE1IT {

  static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000000100");
  static final UUID TRANG = UUID.fromString("00000000-0000-7000-8002-000000000001");

  static final String S1 = "SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.";
  static final String S2 = "ABCD vuông cạnh a nên AC = a√2.";
  static final String S3_WRONG = "tan SCA = SA / AC = √2.";
  static final String S4_WRONG = "Vậy góc giữa SC và mặt phẳng đáy bằng 54,7°.";
  static final String S3_RIGHT = "tan SCA = a√2 / a√2 = 1.";
  static final String S4_RIGHT = "Vậy góc giữa SC và mặt phẳng đáy bằng 45°.";
  static final String VERIFIED = "Đã kiểm chứng";
  static final String NEEDS_REVIEW = "Cần xem lại";

  @Autowired SolutionReviewService solutions;

  @AfterEach
  void restoreEngine() {
    solutions.overrideForTest(null, null);
  }

  static UUID doc(int tc) {
    return UUID.fromString(String.format("00000000-0000-7000-8002-%012d", 820000000L + tc * 100L));
  }

  String trang() {
    return token(ORG, TRANG, Role.TEACHER);
  }

  JsonNode view(UUID doc, int version) throws Exception {
    HttpResponse<String> r =
        get("/api/v1/teacher/solutions/" + doc + "?version=" + version, trang());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  JsonNode save(UUID doc, int base, String... steps) throws Exception {
    ObjectNode body = mapper.createObjectNode().put("baseVersion", base);
    ArrayNode arr = body.putArray("steps");
    for (String s : steps) {
      arr.add(s);
    }
    HttpResponse<String> r =
        postJson("/api/v1/teacher/solutions/" + doc + "/versions", trang(), body.toString());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    JsonNode v = json(r);
    assertThat(v.path("version").path("version").asInt()).isEqualTo(base + 1);
    return v;
  }

  HttpResponse<String> post(UUID doc, String action, int version, boolean confirm)
      throws Exception {
    ObjectNode body =
        mapper.createObjectNode().put("version", version).put("confirmNeedsReview", confirm);
    return postJson("/api/v1/teacher/solutions/" + doc + "/" + action, trang(), body.toString());
  }

  JsonNode check(UUID doc, int version) throws Exception {
    HttpResponse<String> r = post(doc, "check", version, false);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  static List<String> checks(JsonNode view) {
    List<String> out = new ArrayList<>();
    view.path("version").path("steps").forEach(s -> out.add(s.path("check").asString()));
    return out;
  }

  static String machine(JsonNode view) {
    return view.path("version").path("machineLabel").asString();
  }

  @Test
  @DisplayName("TC-S2E5-021 Biểu thức không đọc được là Không kiểm được, không lỗi hệ thống")
  void tc021_unreadable() throws Exception {
    UUID d = doc(21);
    save(d, 1, S1, S2, "tan SCA = abc", S4_RIGHT);
    JsonNode v = check(d, 2);
    JsonNode step3 = v.path("version").path("steps").get(2);
    assertThat(step3.path("check").asString()).isEqualTo("unverifiable");
    assertThat(step3.path("checkLabel").asString()).isEqualTo("Không kiểm được");
    assertThat(step3.path("reason").asString()).isEqualTo("không đọc được biểu thức");
    assertThat(checks(v).get(1)).isEqualTo("pass"); // các bước khác vẫn kiểm
    assertThat(v.path("version").path("failCount").asInt()).isZero();
  }

  @Test
  @DisplayName("TC-S2E5-022 Sửa bước 3 thì bước 3 và sau về Chưa kiểm, bước trước giữ Đạt")
  void tc022_editResetsFromChangedStep() throws Exception {
    UUID d = doc(22);
    JsonNode v1 = check(d, 1);
    assertThat(checks(v1)).containsExactly("unverifiable", "pass", "fail", "pass");
    JsonNode v2 = save(d, 1, S1, S2, S3_RIGHT, S4_WRONG);
    assertThat(checks(v2)).containsExactly("unverifiable", "pass", "unchecked", "unchecked");
    assertThat(v2.path("version").path("steps").get(2).path("checkLabel").asString())
        .isEqualTo("Chưa kiểm");
    assertThat(machine(v2)).isEqualTo(NEEDS_REVIEW);
    // phiên bản cũ giữ kết quả của nó
    assertThat(checks(view(d, 1))).containsExactly("unverifiable", "pass", "fail", "pass");
  }

  @Test
  @DisplayName("TC-S2E5-024 Kiểm 10 giây xong, 11 giây quá hạn")
  void tc024_timeout() throws Exception {
    UUID d = doc(24);
    save(d, 1, S1, S2, S3_RIGHT, S4_RIGHT);
    long sec = 1_000_000_000L;
    long[] clock = {0, 10 * sec, 100 * sec, 111 * sec};
    AtomicInteger tick = new AtomicInteger();
    solutions.overrideForTest(
        (steps, ctx) -> {
          List<Map<String, Object>> rows = new ArrayList<>();
          for (int i = 1; i <= steps.size(); i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("stepNo", i);
            m.put("status", "pass");
            m.put("reason", "máy tính lại khớp");
            rows.add(m);
          }
          return Map.of("steps", rows, "finalState", "match");
        },
        () -> clock[Math.min(tick.getAndIncrement(), clock.length - 1)]);
    JsonNode first = check(d, 2); // đúng 10 giây: có kết quả
    assertThat(checks(first)).containsExactly("pass", "pass", "pass", "pass");
    HttpResponse<String> second = post(d, "check", 2, false); // 11 giây: quá hạn
    assertThat(second.statusCode()).isEqualTo(504);
    assertThat(json(second).path("code").asString()).isEqualTo("TIMEOUT");
    JsonNode after = view(d, 2);
    assertThat(checks(after)).containsOnly("unchecked");
    assertThat(machine(after)).isEqualTo(NEEDS_REVIEW);
  }

  @Test
  @DisplayName("TC-S2E5-025 Lời giải 30 bước có đủ 30 kết quả")
  void tc025_thirtyResults() throws Exception {
    UUID d = doc(25);
    String[] steps = new String[30];
    for (int i = 0; i < 30; i++) {
      steps[i] = i % 4 == 0 ? "Bước lập luận thứ " + (i + 1) + "." : (i + 1) + " + 1 = " + (i + 2);
    }
    save(d, 1, steps);
    JsonNode v = check(d, 2);
    JsonNode out = v.path("version").path("steps");
    assertThat(out.size()).isEqualTo(30);
    for (int i = 0; i < 30; i++) {
      assertThat(out.get(i).path("no").asInt()).isEqualTo(i + 1);
      assertThat(out.get(i).path("check").asString())
          .isEqualTo(i % 4 == 0 ? "unverifiable" : "pass");
    }
    assertThat(
            count(
                "SELECT count(*) FROM step_check_result r JOIN solution_version v ON v.id = r.version_id"
                    + " WHERE v.doc_id = '"
                    + d
                    + "' AND v.version = 2"))
        .isEqualTo(30);
  }

  @Test
  @DisplayName("TC-S2E5-027 Bước đầu vào thiếu giá trị bước trước thì không kiểm được")
  void tc027_missingInput() throws Exception {
    UUID d = doc(27);
    save(d, 1, S1, "tan SCA = SA / AC = 1.", S4_RIGHT); // đã xóa bước 2 (AC = a√2)
    JsonNode v = check(d, 2);
    JsonNode step = v.path("version").path("steps").get(1);
    assertThat(step.path("check").asString()).isEqualTo("unverifiable");
    assertThat(step.path("reason").asString()).isEqualTo("thiếu giá trị đầu vào từ bước trước");
  }

  @Test
  @DisplayName("TC-S2E5-032 Còn bước Không đạt thì không thể Đã kiểm chứng dù đã duyệt")
  void tc032_failKeepsNeedsReviewAfterApproval() throws Exception {
    UUID d = doc(32);
    HttpResponse<String> a = post(d, "approve", 1, true); // duyệt khi chưa kiểm (có xác nhận)
    assertThat(a.statusCode()).as(a.body()).isEqualTo(200);
    assertThat(json(a).path("version").path("humanLabel").asString())
        .isEqualTo("Giáo viên đã duyệt");
    JsonNode v = check(d, 1);
    assertThat(v.path("version").path("failCount").asInt()).isEqualTo(1);
    assertThat(machine(v)).isEqualTo(NEEDS_REVIEW);
    assertThat(v.path("version").path("humanLabel").asString()).isEqualTo("Giáo viên đã duyệt");
  }

  @Test
  @DisplayName("TC-S2E5-033 Mọi bước Không kiểm được thì Cần xem lại")
  void tc033_allUnverifiable() throws Exception {
    UUID d = doc(33);
    save(d, 1, S1, "Tam giác SAC vuông cân tại A.", "Vậy góc cần tìm là góc nhọn của tam giác đó.");
    JsonNode v = check(d, 2);
    assertThat(checks(v)).containsOnly("unverifiable");
    assertThat(machine(v)).isEqualTo(NEEDS_REVIEW);
    assertThat(v.path("version").hasNonNull("machineNote")).isFalse();
  }

  @Test
  @DisplayName("TC-S2E5-034 Mọi bước Đạt nhưng đáp số cuối lệch engine thì Cần xem lại")
  void tc034_finalMismatch() throws Exception {
    UUID d = doc(34);
    save(d, 1, S1, "tan SCA = SA / AB = a√2 / a = √2.", S4_WRONG);
    JsonNode v = check(d, 2);
    assertThat(checks(v)).containsExactly("unverifiable", "pass", "pass");
    assertThat(machine(v)).isEqualTo(NEEDS_REVIEW);
    assertThat(v.path("version").path("finalState").asString()).isEqualTo("mismatch");
    assertThat(v.path("version").path("finalNote").asString()).contains("54,7°").contains("45°");
    assertThat(v.path("approve").path("allowed").asBoolean()).isFalse();
    assertThat(v.path("approve").path("blockedReason").asString()).contains("đáp số lệch");
    HttpResponse<String> a = post(d, "approve", 2, true);
    assertThat(a.statusCode()).isEqualTo(409);
  }

  @Test
  @DisplayName("TC-S2E5-037 Bộ câu thử bài sai không có mẫu nào mang Đã kiểm chứng")
  void tc037_wrongSolutionsNeverVerified() throws Exception {
    UUID d = doc(37);
    String goc = "Vậy góc giữa SC và mặt phẳng đáy bằng ";
    List<String[]> wrong =
        List.of(
            new String[] {S1, S2, "tan SCA = SA / AC = 2.", goc + "63,4°."}, // sai bước tính
            new String[] {S1, S2, S3_RIGHT, S4_WRONG}, // sai đáp số
            new String[] {S1, S2, S3_RIGHT, goc + "45 cm."}, // sai đơn vị
            new String[] {S1, S2, S3_RIGHT, goc + "0,785 rad."}, // sai đơn vị
            new String[] {S1, S2, S3_RIGHT}, // thiếu bước kết luận
            new String[] {"Góc cần tìm bằng 45°."}, // thiếu mọi bước tính
            new String[] {
              S1, "ABCD vuông cạnh a nên AC = a√3.", "tan SCA = SA / AC = 0,82.", goc + "39,2°."
            },
            new String[] {S1, S2, "tan SCA = AC / SA = 1.", goc + "60°."}, // sai đáp số
            new String[] {
              S1, "tan SCA = SA / AB = a√2 / a = √2.", S4_WRONG
            }, // sai cạnh, tính nhất quán
            new String[] {S1, S2, "sin SCA = SA / AC = 1.", goc + "90°."}); // sai tỉ số
    assertThat(wrong).hasSize(10);
    int version = 1;
    int verified = 0;
    for (String[] steps : wrong) {
      save(d, version, steps);
      version++;
      JsonNode v = check(d, version);
      verified += VERIFIED.equals(machine(v)) ? 1 : 0;
      assertThat(machine(v)).as("lời giải sai thứ " + (version - 1)).isEqualTo(NEEDS_REVIEW);
    }
    assertThat(verified).isZero(); // M-S2-03 = 0
    // Kể cả ghi thẳng CSDL cũng không đặt được nhãn (trigger BR-S2E5-12).
    assertThatThrownBy(() ->
            jdbc.sql(
                    "UPDATE solution_version SET machine_status = 'verified' WHERE doc_id = :d AND version = 2")
                .param("d", d)
                .update())
        .hasMessageContaining("BR-S2E5-12");
    // Đối chứng: lời giải đúng nhận nhãn, bộ thử không "đạt" vì máy không bao giờ gắn nhãn.
    save(d, version, S1, S2, S3_RIGHT, S4_RIGHT);
    JsonNode ok = check(d, version + 1);
    assertThat(machine(ok)).isEqualTo(VERIFIED);
    assertThat(ok.path("version").path("machineNote").asString())
        .isEqualTo("Máy đã kiểm tra phần tính toán");
  }

  @Test
  @DisplayName("TC-S2E5-050 Duyệt phiên bản mới thì bản duyệt trước thành Bị thay thế")
  void tc050_supersede() throws Exception {
    UUID d = doc(50);
    save(d, 1, S1, S2, S3_RIGHT, S4_RIGHT);
    assertThat(post(d, "approve", 2, true).statusCode()).isEqualTo(200);
    save(d, 2, S1, S2, S3_RIGHT, "Vậy góc giữa SC và mặt đáy bằng 45°.");
    check(d, 3);
    HttpResponse<String> a = post(d, "approve", 3, false);
    assertThat(a.statusCode()).as(a.body()).isEqualTo(200);
    assertThat(json(a).path("version").path("humanLabel").asString())
        .isEqualTo("Giáo viên đã duyệt");
    assertThat(view(d, 2).path("version").path("humanLabel").asString()).isEqualTo("Bị thay thế");
    assertThat(
            count(
                "SELECT count(*) FROM solution_version WHERE doc_id = '"
                    + d
                    + "' AND human_status = 'teacher_approved'"))
        .isEqualTo(1);
    assertThat(
            count(
                "SELECT count(*) FROM solution_version WHERE doc_id = '"
                    + d
                    + "' AND approved_at IS NOT NULL"))
        .isEqualTo(2);
  }

  /**
   * Chạy từng câu dưới vai geo_app, mỗi câu trong một savepoint; trả SQLSTATE kèm câu lệnh (null =
   * thành công). Cuối cùng ROLLBACK.
   */
  private List<String> asApp(String... sqls) throws SQLException {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(false);
      List<String> out = new ArrayList<>();
      try (Statement st = c.createStatement()) {
        st.execute("SET LOCAL ROLE geo_app");
        for (String sql : sqls) {
          Savepoint sp = c.setSavepoint();
          try {
            st.execute(sql);
            out.add(null);
          } catch (SQLException e) {
            out.add(e.getSQLState() + " " + sql);
            c.rollback(sp);
          }
        }
      } finally {
        c.rollback();
      }
      return out;
    }
  }

  @Test
  @DisplayName(
      "US-S2E5-02-FC2 vai ứng dụng không đọc thẳng được bước giải, StepCheck, đề xuất; chỉ chủ sổ đọc qua hàm cổng")
  void sensitiveColumnsOnlyThroughOwnerGate() throws Exception {
    UUID d = doc(60); // sổ không ca nào khác dùng
    check(d, 1);
    String org = "'" + ORG + "'";
    String doc = "'" + d + "'";
    String v1 =
        "(SELECT id FROM solution_version WHERE org_id = "
            + org
            + " AND doc_id = "
            + doc
            + " AND version = 1)";
    String v2 = "'00000000-0000-7000-8002-000829000002'";
    // 1) đọc thẳng cột nhạy cảm: bị từ chối (42501)
    List<String> denied =
        asApp(
            "SELECT steps FROM solution_version LIMIT 1",
            "SELECT final_note FROM solution_version LIMIT 1",
            "SELECT reason FROM step_check_result LIMIT 1",
            "SELECT computed FROM step_check_result LIMIT 1",
            "SELECT content FROM review_inbox_item LIMIT 1");
    assertThat(denied).allMatch(x -> x != null && x.startsWith("42501"));
    // 2) các câu lệnh của ứng dụng (siêu dữ liệu, hàm cổng, ghi phiên bản / kết quả kiểm / duyệt)
    // chạy được
    List<String> allowed =
        asApp(
            "SELECT id, owner_user_id, code, title, statement, figure::text, latest_version, in_my_shelf"
                + " FROM solution_doc WHERE org_id = "
                + org
                + " AND id = "
                + doc
                + " FOR UPDATE",
            "SELECT v.id, v.version, v.ai_authored, v.author_user_id, u.display_name, u.user_code, v.note,"
                + " v.human_status::text, v.machine_status::text, v.final_state, v.checked_at, v.approved_code,"
                + " v.approved_at, v.created_at FROM solution_version v LEFT JOIN app_user u"
                + " ON u.org_id = v.org_id AND u.id = v.author_user_id WHERE v.org_id = "
                + org
                + " AND v.doc_id = "
                + doc
                + " LIMIT 200",
            "SELECT steps::text, checks::text, proposals::text, final_note FROM fn_solution_content_teacher("
                + org
                + ", '"
                + TRANG
                + "', "
                + doc
                + ", 1)",
            "UPDATE solution_doc SET latest_version = latest_version + 1 WHERE org_id = "
                + org
                + " AND id = "
                + doc
                + " AND latest_version = 1",
            "INSERT INTO solution_version (id, org_id, doc_id, version, steps, ai_authored, author_user_id,"
                + " note, human_status) VALUES ("
                + v2
                + ", "
                + org
                + ", "
                + doc
                + ", 2, '[\"1 + 1 = 2\"]'::jsonb, false, '"
                + TRANG
                + "', 'sửa bước 1', 'editing')",
            "SELECT fn_solution_copy_checks(" + org + ", " + v1 + ", " + v2 + ", 1)",
            "UPDATE solution_version SET machine_status = 'needs_review', final_state = NULL,"
                + " final_note = NULL, checked_at = NULL WHERE org_id = "
                + org
                + " AND id = "
                + v2,
            "DELETE FROM step_check_result WHERE org_id = " + org + " AND version_id = " + v2,
            "INSERT INTO step_check_result (org_id, version_id, step_no, status, reason, written, computed)"
                + " SELECT "
                + org
                + ", "
                + v2
                + ", s.no, CAST(s.status AS step_check_status), s.reason,"
                + " s.written, s.computed FROM jsonb_to_recordset("
                + "'[{\"no\":1,\"status\":\"pass\",\"reason\":\"khớp\"}]'::jsonb)"
                + " AS s(no smallint, status text, reason text, written double precision,"
                + " computed double precision)",
            "UPDATE solution_version SET final_state = 'unknown', final_note = 'ghi chú', checked_at = now(),"
                + " machine_status = 'needs_review' WHERE org_id = "
                + org
                + " AND id = "
                + v2,
            "SELECT count(*) FROM step_check_result WHERE org_id = "
                + org
                + " AND version_id = "
                + v2
                + " AND status = 'fail'",
            "UPDATE solution_version SET human_status = 'teacher_approved', approved_by = '"
                + TRANG
                + "', approved_code = 'GV-7K2M-9Q4D', approved_at = now() WHERE org_id = "
                + org
                + " AND id = "
                + v2
                + " AND approved_at IS NULL",
            "UPDATE solution_version SET human_status = 'superseded' WHERE org_id = "
                + org
                + " AND doc_id = "
                + doc
                + " AND id <> "
                + v2
                + " AND human_status = 'teacher_approved'",
            "UPDATE solution_doc SET in_my_shelf = true WHERE org_id = " + org + " AND id = " + doc,
            "SELECT id, after_step, status::text, decided_at, result_version, created_at"
                + " FROM review_inbox_item WHERE org_id = "
                + org
                + " AND doc_id = "
                + doc
                + " LIMIT 100",
            "UPDATE review_inbox_item SET status = 'dismissed', decided_by = '"
                + TRANG
                + "', decided_at = now() WHERE org_id = "
                + org
                + " AND doc_id = "
                + doc
                + " AND status = 'pending'",
            "SELECT c.name, v.version FROM solution_assignment_pin sp JOIN solution_version v"
                + " ON v.org_id = sp.org_id AND v.id = sp.version_id JOIN assignment a"
                + " ON a.org_id = sp.org_id AND a.id = sp.assignment_id JOIN class c"
                + " ON c.org_id = a.org_id AND c.id = a.class_id WHERE sp.org_id = "
                + org
                + " LIMIT 100");
    assertThat(allowed).containsOnlyNulls();
    // 3) hàm cổng chỉ trả hàng cho giáo viên chủ sổ: Cô Lan (cùng tổ chức) và học sinh An không
    // nhận gì
    String gate =
        "SELECT count(*) FROM fn_solution_content_teacher(" + org + ", '%s', " + doc + ", 1)";
    assertThat(count(String.format(gate, TRANG))).isEqualTo(1);
    assertThat(count(String.format(gate, "00000000-0000-7000-8002-000000820002"))).isZero();
    assertThat(count(String.format(gate, "00000000-0000-7000-8002-000000000002"))).isZero();
  }
}
