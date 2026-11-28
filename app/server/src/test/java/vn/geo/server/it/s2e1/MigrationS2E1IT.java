package vn.geo.server.it.s2e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.iam.internal.code.UserCodes;
import vn.geo.server.iam.internal.service.MvpMigration;
import vn.geo.server.iam.internal.service.MvpMigration.Report;
import vn.geo.server.iam.internal.service.MvpMigration.Row;
import vn.geo.server.iam.internal.service.MvpMigration.Status;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * US-S2E1-04: lệnh chuyển tài khoản mẫu MVP (trường F-GEO-001: Giáo viên mẫu, lớp 11A1 có 5 học
 * sinh, An tiến độ trên BG-1) sang mã người dùng. Bộ thử thêm một học sinh tên rỗng (dòng lỗi). Mỗi
 * ca tự dọn: bỏ mã đã gắn, xóa học sinh lỗi, để các IT khác thấy dữ liệu vàng nguyên vẹn.
 */
class MigrationS2E1IT extends AbstractE1IT {

  static final UUID BLANK = UUID.fromString("00000000-0000-7000-8002-000000000999");
  static final List<String> PROFILES = List.of("test");

  /** Dạng đầy đủ của mã khôi phục; mã người dùng (hai nhóm) không bao giờ khớp. */
  static final String RECOVERY_CODE =
      "RC-["
          + UserCodes.ALPHABET
          + "]{4}-["
          + UserCodes.ALPHABET
          + "]{4}-["
          + UserCodes.ALPHABET
          + "]{4}";

  @Autowired MvpMigration migration;

  @BeforeEach
  void addBlankRow() {
    cleanup();
    jdbc.sql(
            "INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (:id, :org, 'student', 'light', ' ')")
        .param("id", BLANK)
        .param("org", ORG_A)
        .update();
    jdbc.sql(
            "INSERT INTO class_member (org_id, class_id, user_id, name_key) VALUES (:org, :cls, :id, ' ')")
        .param("org", ORG_A)
        .param("cls", CLASS_11A1)
        .param("id", BLANK)
        .update();
  }

  @AfterEach
  void cleanup() {
    jdbc.sql("DELETE FROM class_member WHERE user_id = :id").param("id", BLANK).update();
    jdbc.sql("DELETE FROM app_user WHERE id = :id").param("id", BLANK).update();
    jdbc.sql(
            """
            UPDATE app_user SET user_code = NULL, password_hash = NULL, recovery_hmac = NULL,
                   must_change_password = false WHERE org_id = :org
            """)
        .param("org", ORG_A)
        .update();
  }

  private List<Row> classRows(Report r, String className, String role) {
    return r.rows().stream()
        .filter(x -> role.equals(x.role()) && className.equals(x.className()))
        .toList();
  }

  private long progressRows() {
    return count("SELECT count(*) FROM assignment_progress WHERE org_id = '" + ORG_A + "'");
  }

  private String anProgress() {
    return jdbc.sql(
            "SELECT string_agg(assignment_id || ':' || coalesce(row_to_json(p)::text, ''), '|' ORDER BY assignment_id)"
                + " FROM assignment_progress p WHERE org_id = :org AND user_id = :an")
        .param("org", ORG_A)
        .param("an", AN)
        .query(String.class)
        .optional()
        .orElse("");
  }

  @Test
  @DisplayName(
      "TC-S2E1-039 Mỗi học sinh 11A1 thành một tài khoản HS- riêng; tiến độ An, số bài làm giữ nguyên")
  void tc_S2E1_039_studentsGetCodes() {
    long before = progressRows();
    String anBefore = anProgress();
    Report r = migration.run(PROFILES, ORG_A);
    List<Row> students =
        classRows(r, "11A1", "student").stream()
            .filter(x -> x.status() == Status.CONVERTED)
            .toList();
    assertThat(students).hasSize(5);
    Set<String> codes = students.stream().map(Row::userCode).collect(Collectors.toSet());
    assertThat(codes).hasSize(5).allMatch(c -> c.startsWith("HS-") && UserCodes.isValidUserCode(c));
    assertThat(students.stream().map(Row::userId)).contains(AN);
    assertThat(
            count(
                "SELECT count(*) FROM app_user WHERE org_id = '"
                    + ORG_A
                    + "' AND user_code LIKE 'HS-%' AND must_change_password"))
        .isGreaterThanOrEqualTo(5);
    assertThat(progressRows()).isEqualTo(before);
    assertThat(anProgress()).isEqualTo(anBefore).isNotEmpty();
  }

  @Test
  @DisplayName(
      "TC-S2E1-040 Giáo viên mẫu thành đúng một tài khoản GV-, vẫn là chủ 11A1, có mã khôi phục trong báo cáo")
  void tc_S2E1_040_teacherGetsCode() {
    Report r = migration.run(PROFILES, ORG_A);
    List<Row> teachers = r.rows().stream().filter(x -> TEACHER_A.equals(x.userId())).toList();
    assertThat(teachers).hasSize(1);
    Row t = teachers.get(0);
    assertThat(t.status()).isEqualTo(Status.CONVERTED);
    assertThat(t.userCode()).startsWith("GV-");
    assertThat(UserCodes.normalizeRecoveryCode(t.recoveryCode())).contains(t.recoveryCode());
    assertThat(t.tempPassword()).isNotBlank();
    assertThat(r.toCsv()).contains(t.userCode()).contains(t.recoveryCode());
    assertThat(
            count(
                "SELECT count(*) FROM class WHERE id = '"
                    + CLASS_11A1
                    + "' AND teacher_id = '"
                    + TEACHER_A
                    + "'"))
        .isEqualTo(1);
    assertThat(count("SELECT count(*) FROM app_user WHERE user_code = '" + t.userCode() + "'"))
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "TC-S2E1-041 Chạy lại lệnh chuyển: không tạo trùng, báo cáo ghi 'đã chuyển trước đó'")
  void tc_S2E1_041_rerunIsIdempotent() {
    migration.run(PROFILES, ORG_A);
    long users = count("SELECT count(*) FROM app_user");
    long classes = count("SELECT count(*) FROM class");
    long progress = progressRows();
    String codes = codesOfOrg();
    Report second = migration.run(PROFILES, ORG_A);
    assertThat(count("SELECT count(*) FROM app_user")).isEqualTo(users);
    assertThat(count("SELECT count(*) FROM class")).isEqualTo(classes);
    assertThat(progressRows()).isEqualTo(progress);
    assertThat(codesOfOrg()).isEqualTo(codes);
    List<Row> ok = second.rows().stream().filter(x -> x.status() != Status.ERROR).toList();
    assertThat(ok).isNotEmpty().allMatch(x -> x.status() == Status.ALREADY);
    assertThat(ok).allMatch(x -> MvpMigration.ALREADY.equals(x.reason()));
    // Mã khôi phục chỉ in một lần. Không dò chuỗi "RC-" trơn: mã người dùng sinh ngẫu nhiên có thể
    // chứa "RC-" (ví dụ HS-5NRC-7RPZ), nên kiểm đúng cột và đúng dạng RC-xxxx-xxxx-xxxx.
    assertThat(second.rows()).allMatch(x -> x.recoveryCode() == null || x.recoveryCode().isBlank());
    assertThat(second.rows()).allMatch(x -> x.tempPassword() == null || x.tempPassword().isBlank());
    assertThat(second.toCsv()).doesNotContainPattern(RECOVERY_CODE);
  }

  @Test
  @DisplayName(
      "TC-S2E1-042 Dòng tên rỗng vào báo cáo lỗi; 4 học sinh còn lại vẫn chuyển; mã thoát khác 0")
  void tc_S2E1_042_badRowDoesNotStopBatch() {
    Report r = migration.run(PROFILES, ORG_A);
    List<Row> errors = r.rows().stream().filter(x -> x.status() == Status.ERROR).toList();
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0).userId()).isEqualTo(BLANK);
    assertThat(errors.get(0).reason()).isEqualTo("tên hiển thị rỗng");
    List<Row> others =
        classRows(r, "11A1", "student").stream()
            .filter(x -> !BLANK.equals(x.userId()) && x.status() == Status.CONVERTED)
            .toList();
    assertThat(others).hasSizeGreaterThanOrEqualTo(4);
    assertThat(r.exitCode()).isNotZero();
    assertThat(r.toCsv()).contains("ERROR").contains("tên hiển thị rỗng");
  }

  @Test
  @DisplayName("TC-S2E1-044 Môi trường prod: lệnh từ chối ngay, không đọc hay ghi dữ liệu")
  void tc_S2E1_044_refusesProd() {
    String before = codesOfOrg();
    Report r = migration.run(List.of("prod"), null);
    assertThat(r.refused()).isTrue();
    assertThat(r.message()).contains("không chạy trên prod");
    assertThat(r.rows()).isEmpty();
    assertThat(r.exitCode()).isEqualTo(3);
    assertThat(codesOfOrg()).isEqualTo(before);
  }

  private String codesOfOrg() {
    return jdbc.sql(
            "SELECT coalesce(string_agg(id || '=' || coalesce(user_code, '-'), ',' ORDER BY id), '') FROM app_user WHERE org_id = :org")
        .param("org", ORG_A)
        .query(String.class)
        .single();
  }
}
