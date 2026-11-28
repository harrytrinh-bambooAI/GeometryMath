package vn.geo.server.it.e5;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * FIX3 — quyền bộ đề (bank) trên mọi đường đọc/giao đề kho theo id, không chỉ danh sách/đếm (phát
 * hiện soát E5). Giáo viên A (THPT Mẫu A) không được cấp bộ 'kho_thu'; Giáo viên mẫu e50003 được
 * cấp. Ngoài bộ = 404, không lộ là đề có tồn tại (BR-GEO-112). Dev-E5-01…05 (qc_findings E5 mục
 * Dev).
 */
class BankAccessE5IT extends AbstractE1IT {

  /** HKG-07 của kho thử (bank 'kho_thu', seed 500_e5_catalog.sql). */
  static final UUID KHO_THU_V = UUID.fromString("00000000-0000-7000-8000-0000e5200007");

  static final UUID E5_ORG = UUID.fromString("00000000-0000-7000-8000-000000e50001");
  static final UUID E5_TEACHER = UUID.fromString("00000000-0000-7000-8000-000000e50003");
  static final UUID CLASS_9B = id(11);

  private String laneTeacher() {
    return token(E5_ORG, E5_TEACHER, Role.TEACHER);
  }

  private String assignBody(UUID assignmentId, UUID versionId) {
    return "{\"assignmentId\":\""
        + assignmentId
        + "\",\"classId\":\""
        + CLASS_9B
        + "\",\"problemVersionId\":\""
        + versionId
        + "\"}";
  }

  @Test
  @DisplayName("Dev-E5-01 chi tiết đề kho ngoài bộ → 404; trong bộ → 200")
  void detailOutsideBank() throws Exception {
    String path = "/api/v1/teacher/catalog/problems/" + KHO_THU_V;
    var out = get(path, teacherA());
    assertThat(out.statusCode()).isEqualTo(404);
    assertThat(out.body()).doesNotContain("HKG-07").doesNotContain("Đề thử");
    assertThat(get(path, laneTeacher()).statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("Dev-E5-02 lời giải đề kho ngoài bộ → 404 (API và hàm cổng SQL); trong bộ → 200")
  void solutionOutsideBank() throws Exception {
    String path = "/api/v1/teacher/problems/" + KHO_THU_V + "/solution";
    var out = get(path, teacherA());
    assertThat(out.statusCode()).isEqualTo(404);
    assertThat(out.body()).doesNotContain("steps");
    assertThat(get(path, laneTeacher()).statusCode()).isEqualTo(200);
    assertThat(
            count(
                "SELECT count(*) FROM fn_get_solution_teacher('"
                    + ORG_A
                    + "', '"
                    + TEACHER_A
                    + "', '"
                    + KHO_THU_V
                    + "')"))
        .isZero();
  }

  @Test
  @DisplayName("Dev-E5-03 trang chi tiết đề có cảnh, gợi ý, đề kho ngoài bộ → 404; trong bộ → 200")
  void sceneOutsideBank() throws Exception {
    String path = "/api/v1/teacher/problems/" + KHO_THU_V;
    var out = get(path, teacherA());
    assertThat(out.statusCode()).isEqualTo(404);
    assertThat(out.body()).doesNotContain("scene").doesNotContain("Đề thử");
    assertThat(get(path, laneTeacher()).statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName(
      "Dev-E5-04 giao đề kho ngoài bộ → 404, không tạo bài giao (API và fn_create_assignment)")
  void assignOutsideBank() throws Exception {
    UUID id = Uuid7.next();
    var r = postJson("/api/v1/teacher/assignments", teacherA(), assignBody(id, KHO_THU_V));
    assertThat(r.statusCode()).as(r.body()).isEqualTo(404);
    assertThat(count("SELECT count(*) FROM assignment WHERE id = '" + id + "'")).isZero();
    String status =
        jdbc.sql(
                "SELECT status FROM fn_create_assignment(:org, :actor, :id, :cls, :pv, NULL, true, true, NULL)")
            .param("org", ORG_A)
            .param("actor", TEACHER_A)
            .param("id", id)
            .param("cls", CLASS_9B)
            .param("pv", KHO_THU_V)
            .query(String.class)
            .single();
    assertThat(status).isEqualTo("NOT_FOUND");
    assertThat(count("SELECT count(*) FROM assignment WHERE id = '" + id + "'")).isZero();
  }

  /**
   * Guard Java độc lập với lớp SQL: giả lập một migration sau định nghĩa lại hàm mà quên điều kiện
   * bộ (fn_catalog_bank_visible luôn đúng) → API vẫn 404 nhờ {@code content.api.BankAccess}.
   */
  @Test
  @DisplayName(
      "Dev-E5-05 hàm SQL mất điều kiện bộ → guard Java vẫn trả 404 cho lời giải và giao bài")
  void javaGuardIndependentOfSql() throws Exception {
    String original =
        jdbc.sql("SELECT pg_get_functiondef('fn_catalog_bank_visible(uuid,text)'::regprocedure)")
            .query(String.class)
            .single();
    jdbc.sql(
            "CREATE OR REPLACE FUNCTION fn_catalog_bank_visible(p_org uuid, p_bank text) RETURNS boolean"
                + " LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp AS $$ SELECT true $$")
        .update();
    try {
      assertThat(
              count(
                  "SELECT count(*) FROM fn_get_solution_teacher('"
                      + ORG_A
                      + "', '"
                      + TEACHER_A
                      + "', '"
                      + KHO_THU_V
                      + "')"))
          .as("lớp SQL đã mất điều kiện bộ (giả lập)")
          .isEqualTo(1);
      var sol = get("/api/v1/teacher/problems/" + KHO_THU_V + "/solution", teacherA());
      assertThat(sol.statusCode()).isEqualTo(404);
      UUID id = Uuid7.next();
      var r = postJson("/api/v1/teacher/assignments", teacherA(), assignBody(id, KHO_THU_V));
      assertThat(r.statusCode()).as(r.body()).isEqualTo(404);
      assertThat(count("SELECT count(*) FROM assignment WHERE id = '" + id + "'")).isZero();
    } finally {
      jdbc.sql(original).update();
    }
  }
}
