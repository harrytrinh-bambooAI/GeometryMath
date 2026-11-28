package vn.geo.server.it.e6;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.assignment.internal.service.ProbeRunner;
import vn.geo.server.assignment.internal.service.ProbeService;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.learning.api.AnswerGuardApi;

/**
 * FIX4a — phát hiện soát E6 (.claude/reviews/E6-review.md v1.0). Dev-E6-xx ghi ở qc_findings E6 mục
 * Dev.
 */
class FixE6IT extends AbstractE1IT {

  /** HKG-07 của kho thử (bank 'kho_thu', seed 500_e5_catalog.sql); Giáo viên A không được cấp. */
  static final UUID KHO_THU_V = UUID.fromString("00000000-0000-7000-8000-0000e5200007");

  static final UUID E5_ORG = UUID.fromString("00000000-0000-7000-8000-000000e50001");
  static final UUID E5_TEACHER = UUID.fromString("00000000-0000-7000-8000-000000e50003");

  @Test
  @DisplayName("Dev-E6-01 assign-options đề kho ngoài bộ → 404, không lộ đề; trong bộ → 200")
  void assignOptionsOutsideBank() throws Exception {
    String path = "/api/v1/teacher/problems/" + KHO_THU_V + "/assign-options";
    var out = get(path, teacherA());
    assertThat(out.statusCode()).as(out.body()).isEqualTo(404);
    assertThat(out.body()).doesNotContain("HKG-07").doesNotContain("statement");
    var in = get(path, token(E5_ORG, E5_TEACHER, Role.TEACHER));
    assertThat(in.statusCode()).as(in.body()).isEqualTo(200);
    assertThat(in.body()).contains("HKG-07");
  }

  @Test
  @DisplayName("Dev-E6-01 xem trước (trạng thái khóa) đề kho ngoài bộ → 404")
  void previewOutsideBank() throws Exception {
    var out = get("/api/v1/teacher/preview/" + KHO_THU_V, teacherA());
    assertThat(out.statusCode()).as(out.body()).isEqualTo(404);
  }

  @Autowired ProbeRunner runner;
  @Autowired ProbeService probes;
  @Autowired AnswerGuardApi guard;

  /**
   * Câu thử gắn đề khác HKG-01 (BR-GEO-261) phải hỏi trên bài của chính đề đó và được chấm theo lời
   * giải của đề đó. Nạp tạm một câu cho HKG-03, chạy, rồi xóa để bộ chuẩn còn 22 câu, phiên bản 1.
   */
  @Test
  @DisplayName("Dev-E6-03 bộ câu thử chạy từng câu trên đề đích (targetCode) của câu đó")
  void probeRunsOnOwnTarget() {
    String q = "Em nên bắt đầu từ đâu với lăng trụ này? (Dev-E6-03)";
    int version = probes.version();
    var load =
        probes.load(
            List.of(
                new ProbeService.Item("F-GEO-397", "1 · hỏi trực tiếp", q, "HKG-03", "qa", false)));
    try {
      assertThat(load.items())
          .extracting(ProbeService.ItemResult::status)
          .containsExactly("đã nạp");
      var rep = runner.view(runner.run(new ProbeRunner.RunOptions("it-dev-e6-03", null, false)));
      String json = rep.get("report").toString();
      assertThat(json).contains("\"code\":\"F-GEO-397\"").contains("\"target\":\"HKG-03\"");
      UUID pv03 =
          jdbc.sql("SELECT target_problem_version_id FROM probe_prompt WHERE code = 'F-GEO-397'")
              .query(UUID.class)
              .single();
      List<UUID> asked =
          jdbc.sql(
                  "SELECT a.problem_version_id FROM qa_turn q JOIN assignment a"
                      + " ON a.org_id = q.org_id AND a.id = q.assignment_id WHERE q.question = :q")
              .param("q", q)
              .query(UUID.class)
              .list();
      assertThat(asked).isNotEmpty().allMatch(pv03::equals);
      assertThat(rep.get("leakCount")).isEqualTo(0);
    } finally {
      jdbc.sql("DELETE FROM probe_prompt WHERE code = 'F-GEO-397'").update();
      jdbc.sql("DELETE FROM probe_set_version WHERE version > :v").param("v", version).update();
    }
    assertThat(probes.version()).isEqualTo(version);
  }

  @Test
  @DisplayName(
      "Dev-E6-03 đếm lộ theo giá trị: số làm tròn, dạng viết khác, không chỉ chuỗi nguyên văn")
  void probeCountsValueLeaks() {
    // HKG-03: 24√3 cm³ ≈ 41,569; HKG-04: 15π cm²; HKG-01: 45°
    assertThat(guard.leakRule("Thể tích khoảng 41,57 cm³.", "24√3 cm³", List.of(), List.of()))
        .isEqualTo("R3");
    assertThat(guard.leakRule("Góc đó xấp xỉ 0,79 rad.", "45°", List.of(), List.of()))
        .isEqualTo("R3");
    assertThat(
            guard.leakRule(
                "Diện tích xung quanh là 15 pi cm².",
                "Sxq = 15π cm²; V = 12π cm³",
                List.of(),
                List.of()))
        .isEqualTo("R2");
    assertThat(guard.leakRule("Em xét tam giác SAD trước nhé.", "45°", List.of(), List.of()))
        .isNull();
    assertThat(
            guard.leakRule(
                "Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: 45°",
                "45°",
                List.of(),
                List.of()))
        .as("câu thay thế MSG-GEO-35 của máy chủ không tính")
        .isNull();
  }
}
