package vn.geo.server.it.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.content.internal.service.SignatureUpgrade;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Câu mở sau FIX T-05: hàng problem_signature bản 1 (môi trường có dữ liệu thật trước T-05) không
 * còn khớp đề kho bản 2 nên bài tự học mất kế thừa khóa. Công việc {@link SignatureUpgrade} tính
 * lại chữ ký bản 2 từ mô tả dựng đã lưu; sau đó bài tự học khóa lại theo bài lớp (MSG-GEO-31). Lũy
 * đẳng.
 */
class SignatureUpgradeT05IT extends AbstractE1IT {

  @Autowired SignatureUpgrade upgrade;
  @Autowired GeometryEngine engine;

  private void scenario(String s) throws Exception {
    String body =
        mapper.createObjectNode().put("userId", AN.toString()).put("scenario", s).toString();
    assertThat(postJson("/test-hooks/ai", null, body).statusCode()).isEqualTo(200);
  }

  @BeforeEach
  void setUp() throws Exception {
    reseed();
    scenario(SelfStudyPasteT05IT.SCENARIO);
  }

  @AfterEach
  void tearDown() throws Exception {
    scenario("");
  }

  private UUID lockSource(UUID pv) {
    return jdbc.sql("SELECT fn_self_lock_source(:org, :user, :pv)")
        .param("org", ORG_A)
        .param("user", AN)
        .param("pv", pv)
        .query(UUID.class)
        .list()
        .getFirst();
  }

  @Test
  @DisplayName(
      "T-05 IT tính lại chữ ký v1 → v2: hàng bản 1 (cũ) được bổ sung bản 2 khớp HKG-01, bài tự học kế thừa khóa lại (fn_self_lock_source); chạy lần hai không làm gì")
  void upgradesLegacySignatures() throws Exception {
    var created =
        postJson(
            "/api/v1/student/self-study",
            an(),
            mapper.writeValueAsString(
                Map.of(
                    "statement",
                    "Cho hình chóp S.MNPQ, đáy là hình vuông cạnh a, SM ⊥ (MNPQ), SM = a. Tính góc"
                        + " giữa SQ và mặt đáy.")));
    assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
    String assignment = json(created).get("assignmentId").asString();
    UUID pv =
        jdbc.sql("SELECT problem_version_id FROM assignment WHERE id = :id")
            .param("id", UUID.fromString(assignment))
            .query(UUID.class)
            .single();
    // dữ liệu cũ: chỉ có chữ ký bản 1 (thuật toán ghép danh sách điểm, không khớp đề kho bản 2)
    jdbc.sql(
            "UPDATE problem_signature SET sig_version = 1, signature = decode(repeat('ab', 32), 'hex') WHERE problem_version_id = :pv")
        .param("pv", pv)
        .update();
    assertThat(lockSource(pv)).as("bản 1 không còn khớp đề kho bản 2").isNull();

    SignatureUpgrade.Report first = upgrade.run();
    assertThat(first.upgraded()).isGreaterThanOrEqualTo(1);
    byte[] v2 =
        jdbc.sql(
                "SELECT signature FROM problem_signature WHERE problem_version_id = :pv AND sig_version = :v")
            .param("pv", pv)
            .param("v", GeometryEngine.SIG_VERSION)
            .query(byte[].class)
            .single();
    byte[] hkg01 =
        engine.signature(engine.catalogSpec("HKG-01").orElseThrow()).orElseThrow().sha256();
    assertThat(v2).isEqualTo(hkg01);
    assertThat(lockSource(pv)).as("kế thừa khóa của bài lớp BG-1 trở lại").isNotNull();

    SignatureUpgrade.Report second = upgrade.run();
    assertThat(second.upgraded()).as("lũy đẳng").isZero();
    assertThat(
            jdbc.sql("SELECT count(*) FROM problem_signature WHERE problem_version_id = :pv")
                .param("pv", pv)
                .query(Long.class)
                .single())
        .isEqualTo(2);
  }
}
