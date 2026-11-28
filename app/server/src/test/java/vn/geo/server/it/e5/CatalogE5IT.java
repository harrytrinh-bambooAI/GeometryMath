package vn.geo.server.it.e5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.internal.service.CatalogLoader;
import vn.geo.server.content.internal.service.CatalogVerifier;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.testing.SqlStatementCounter;

/** E5 — nạp Kho đề và tính lại nhãn (US-GEO-504), truy vấn Kho đề theo khóa (US-GEO-505-SC5). */
class CatalogE5IT extends AbstractE1IT {

  static final Path SOURCE = Path.of("../../materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx");
  static final UUID E5_ORG = UUID.fromString("00000000-0000-7000-8000-000000e50001");
  static final UUID E5_TEACHER = UUID.fromString("00000000-0000-7000-8000-000000e50003");

  @Autowired CatalogLoader loader;
  @Autowired CatalogVerifier verifier;
  @Autowired SqlStatementCounter counter;

  private boolean dirty;

  @AfterEach
  void restoreGolden() throws IOException {
    if (dirty) {
      reseed(); // lớp IT khác đọc dữ liệu vàng
      dirty = false;
    }
  }

  private CatalogLoader.Result freshLoad(Path file) throws IOException {
    reseed();
    dirty = true;
    return loader.load(file);
  }

  private String laneTeacher() {
    return token(E5_ORG, E5_TEACHER, Role.TEACHER);
  }

  private String labelOf(String code) {
    return jdbc.sql(
            """
            SELECT pv.verify_status::text FROM problem p JOIN problem_version pv ON pv.id = p.current_version_id
            WHERE p.org_id = platform_org_id() AND p.code = :code
            """)
        .param("code", code)
        .query(String.class)
        .single();
  }

  private UUID currentVersion(String code) {
    return jdbc.sql(
            "SELECT current_version_id FROM problem WHERE org_id = platform_org_id() AND code = :code")
        .param("code", code)
        .query(UUID.class)
        .single();
  }

  private List<String> codesInBank(String bearer) throws Exception {
    JsonNode page = json(get("/api/v1/catalog/problems?limit=50", bearer));
    List<String> out = new ArrayList<>();
    page.get("items").forEach(p -> out.add(p.get("code").asString()));
    return out;
  }

  /** Chép tệp xlsx, sửa XML của một trang (sheet1 "Bai tap", sheet2 "Loi giai"). */
  static Path variant(String sheet, UnaryOperator<String> edit) throws IOException {
    Path out = Files.createTempFile("kho-de-", ".xlsx");
    try (ZipFile zin = new ZipFile(SOURCE.toFile());
        OutputStream os = Files.newOutputStream(out);
        ZipOutputStream zout = new ZipOutputStream(os)) {
      for (ZipEntry e : java.util.Collections.list(zin.entries())) {
        zout.putNextEntry(new ZipEntry(e.getName()));
        try (InputStream in = zin.getInputStream(e)) {
          byte[] b = in.readAllBytes();
          if (e.getName().equals("xl/worksheets/" + sheet + ".xml")) {
            b = edit.apply(new String(b, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
          }
          zout.write(b);
        }
        zout.closeEntry();
      }
    }
    return out;
  }

  static String blankCell(String xml, String ref) {
    return xml.replaceAll("(<c r=\"" + ref + "\"[^>]*>(?:(?!</c>).)*?<t[^>]*>)[^<]*(</t>)", "$1$2");
  }

  @Test
  @DisplayName("TC-GEO-0524 Nạp sáu đề nguồn đủ trường (US-GEO-504-SC1)")
  void tc0524_sixProblemsAllFields() throws Exception {
    var r = freshLoad(SOURCE);
    assertThat(r.loaded())
        .containsExactly("HKG-01", "HKG-02", "HKG-03", "HKG-04", "HKG-05", "HKG-06");
    List<Map<String, Object>> rows =
        jdbc.sql(
                """
                SELECT p.code, pv.statement, s.final_answer, s.steps::text AS steps, s.model, s.prompt_version,
                       s.ai_authored, pv.ai_authored AS pv_ai, u.display_name AS author,
                       (SELECT count(*) FROM hint h WHERE h.problem_version_id = pv.id) AS hints
                FROM problem p JOIN problem_version pv ON pv.id = p.current_version_id
                JOIN solution s ON s.problem_version_id = pv.id
                JOIN app_user u ON u.id = s.author_user_id
                WHERE p.org_id = platform_org_id() AND p.bank = 'main' ORDER BY p.code
                """)
            .query()
            .listOfRows();
    assertThat(rows).hasSize(6);
    assertThat(rows.stream().map(x -> x.get("final_answer")))
        .containsExactly(
            "45°",
            "a",
            "24√3 cm³",
            "Sxq = 15π cm²; V = 12π cm³",
            "112π cm²",
            "R = a√3/2; V = (√3/2)·π·a³");
    assertThat(rows.getFirst().get("statement"))
        .isEqualTo(
            "Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính góc giữa SD và mặt đáy.");
    for (Map<String, Object> x : rows) {
      assertThat(x.get("hints")).as("hai gợi ý %s", x.get("code")).isEqualTo(2L);
      assertThat(x.get("author")).isEqualTo("Giáo viên mẫu");
      assertThat(x.get("ai_authored")).isEqualTo(true);
      assertThat(x.get("pv_ai")).isEqualTo(true);
      assertThat((String) x.get("model")).isNotBlank();
      assertThat((String) x.get("prompt_version")).isNotBlank();
      JsonNode steps = mapper.readTree((String) x.get("steps"));
      assertThat(steps.size()).isPositive();
      steps.forEach(st -> assertThat(st.get("why").asString()).as("vì sao").isNotBlank());
    }
    JsonNode hkg01 = mapper.readTree((String) rows.getFirst().get("steps"));
    assertThat(hkg01.size()).isEqualTo(3);
    assertThat(hkg01.get(2).get("body").asString())
        .isEqualTo("tan SDA = SA / AD = a / a = 1, suy ra góc SDA = 45°.");
  }

  @Test
  @DisplayName("TC-GEO-0525 Nhãn do engine tính lại (US-GEO-504-SC2)")
  void tc0525_engineRecomputesLabels() throws Exception {
    reseed();
    dirty = true;
    // trạng thái "vừa nạp, engine chưa chạy": bỏ nhãn của seed
    jdbc.sql(
            "UPDATE problem_version SET verify_status = 'needs_review', verification_report_id = NULL WHERE org_id = platform_org_id()")
        .update();
    var r = loader.load(SOURCE);
    for (String code : r.loaded()) {
      assertThat(labelOf(code)).as(code).isEqualTo("verified");
      assertThat(r.labels().get(code)).isEqualTo("Đã kiểm chứng");
    }
    assertThat(labelOf("HKG-06")).isEqualTo("verified");
  }

  @Test
  @DisplayName("TC-GEO-0526 Kết quả nạp nêu chỗ nhãn khác tệp (US-GEO-504-SC3)")
  void tc0526_reportListsLabelDiff() throws Exception {
    var r = freshLoad(SOURCE);
    assertThat(r.lines()).contains("HKG-06: tệp ghi Cần xem lại, engine tính Đã kiểm chứng");
    assertThat(r.lines()).hasSize(1);
    JsonNode page = json(get("/api/v1/catalog/problems?limit=50", teacherA()));
    for (JsonNode p : page.get("items")) {
      if (p.get("code").asString().equals("HKG-06")) {
        assertThat(p.get("label").asString()).isEqualTo("Đã kiểm chứng");
      }
    }
    JsonNode d =
        json(get("/api/v1/teacher/catalog/problems/" + currentVersion("HKG-06"), teacherA()));
    assertThat(d.get("label").asString()).isEqualTo("Đã kiểm chứng");
  }

  @Test
  @DisplayName("TC-GEO-0527 Đếm số đề đã kiểm chứng so với tiêu chí MVP (US-GEO-504-SC4)")
  void tc0527_mvpCriterion() throws Exception {
    JsonNode six = json(get("/api/v1/catalog/summary", teacherA()));
    assertThat(six.get("total").asLong()).isEqualTo(6);
    assertThat(six.get("verified").asLong()).isEqualTo(6);
    assertThat(six.get("mvpMet").asBoolean()).isFalse();
    assertThat(six.get("mvp").asString())
        .isEqualTo("chưa đạt (cần ≥ 40 đề đã kiểm chứng, kho 40 đến 100 đề)");
    JsonNode lane = json(get("/api/v1/catalog/summary", laneTeacher()));
    assertThat(lane.get("total").asLong()).isEqualTo(48);
    assertThat(lane.get("verified").asLong()).isEqualTo(44);
    assertThat(lane.get("mvpMet").asBoolean()).isTrue();
    assertThat(lane.get("mvp").asString()).isEqualTo("đạt");
  }

  @Test
  @DisplayName(
      "TC-GEO-0528 Sửa lời giải tạo phiên bản mới, bài đã giao giữ phiên bản cũ (US-GEO-504-SC5)")
  void tc0528_newSolutionVersion() throws Exception {
    reseed();
    dirty = true;
    loader.load(SOURCE); // nạp lại cùng nội dung: không tạo version mới
    assertThat(currentVersion("HKG-01")).isEqualTo(HKG01_V);
    var r =
        loader.load(
            variant("sheet2", x -> x.replace("suy ra g&#243;c SDA", "v&#7853;y g&#243;c SDA")));
    UUID v2 = currentVersion("HKG-01");
    assertThat(v2).isNotEqualTo(HKG01_V);
    assertThat(
            jdbc.sql("SELECT version FROM problem_version WHERE id = :v")
                .param("v", v2)
                .query(Integer.class)
                .single())
        .isEqualTo(2);
    assertThat(r.labels().get("HKG-01")).isEqualTo("Đã kiểm chứng");
    // BG-1 vẫn trỏ phiên bản 1 với lời giải cũ
    assertThat(
            jdbc.sql("SELECT problem_version_id FROM assignment WHERE id = :id")
                .param("id", BG1)
                .query(UUID.class)
                .single())
        .isEqualTo(HKG01_V);
    JsonNode s1 = json(get("/api/v1/teacher/problems/" + HKG01_V + "/solution", teacherA()));
    assertThat(s1.get("steps").get(2).get("body").asString()).contains("suy ra góc SDA");
    JsonNode s2 = json(get("/api/v1/teacher/problems/" + v2 + "/solution", teacherA()));
    assertThat(s2.get("steps").get(2).get("body").asString()).contains("vậy góc SDA");
    // bài giao tạo sau đó (lớp 9B, F-GEO-011; 11A1 đã có BG-1) dùng phiên bản 2
    String pv = null;
    for (JsonNode p : json(get("/api/v1/catalog/problems?limit=50", teacherA())).get("items")) {
      if (p.get("code").asString().equals("HKG-01")) {
        pv = p.get("versionId").asString();
      }
    }
    assertThat(pv).isEqualTo(v2.toString());
    UUID newId = Uuid7.next();
    var created =
        postJson(
            "/api/v1/teacher/assignments",
            teacherA(),
            "{\"assignmentId\":\""
                + newId
                + "\",\"classId\":\""
                + id(11)
                + "\",\"problemVersionId\":\""
                + pv
                + "\"}");
    assertThat(created.statusCode()).as(created.body()).isEqualTo(201);
    assertThat(
            jdbc.sql("SELECT problem_version_id FROM assignment WHERE id = :id")
                .param("id", UUID.fromString(json(created).get("id").asString()))
                .query(UUID.class)
                .single())
        .isEqualTo(v2);
  }

  @Test
  @DisplayName("TC-GEO-0529 Tính lại nhãn khi engine đổi (US-GEO-504-SC6)")
  void tc0529_rerunOnEngineChange() throws Exception {
    reseed();
    dirty = true;
    var items = verifier.verify("engine-2", c -> true);
    assertThat(items).hasSize(48);
    long withNew =
        count(
            """
            SELECT count(*) FROM problem p JOIN problem_version pv ON pv.id = p.current_version_id
            JOIN verification_report r ON r.id = pv.verification_report_id
            WHERE p.org_id = platform_org_id() AND p.source = 'catalog' AND r.checks->>'engine_version' = 'engine-2'
            """);
    assertThat(withNew).isEqualTo(48);
    for (CatalogVerifier.Item it : items) {
      assertThat(labelOf(it.code())).as(it.code()).isEqualTo(it.status());
    }
    // HKG-48 (không có lời giải) vẫn "Cần xem lại"; đề thử có lời giải đúng thì engine tính "Đã
    // kiểm chứng"
    assertThat(labelOf("HKG-48")).isEqualTo("needs_review");
    assertThat(labelOf("HKG-45")).isEqualTo("verified");
  }

  @Test
  @DisplayName("TC-GEO-0530 Đề thiếu đáp số (US-GEO-504-FC1)")
  void tc0530_missingAnswer() throws Exception {
    var r = freshLoad(variant("sheet1", x -> blankCell(x, "H6")));
    assertThat(r.lines()).contains("HKG-05: thiếu đáp số");
    assertThat(r.loaded()).containsExactly("HKG-01", "HKG-02", "HKG-03", "HKG-04", "HKG-06");
    assertThat(codesInBank(teacherA()))
        .containsExactly("HKG-01", "HKG-02", "HKG-03", "HKG-04", "HKG-06");
  }

  @Test
  @DisplayName("TC-GEO-0531 Sửa tại chỗ lời giải đã giao bị từ chối (US-GEO-504-FC2)")
  void tc0531_inPlaceEditRejected() {
    assertThatThrownBy(() ->
            jdbc.sql("UPDATE solution SET final_answer = '60°' WHERE problem_version_id = :v")
                .param("v", HKG01_V)
                .update())
        .hasMessageContaining("bất biến");
    assertThat(
            jdbc.sql("SELECT final_answer FROM solution WHERE problem_version_id = :v")
                .param("v", HKG01_V)
                .query(String.class)
                .single())
        .isEqualTo("45°");
  }

  @Test
  @DisplayName("TC-GEO-0536 Không OFFSET, không truy vấn lặp theo dòng (US-GEO-505-SC5)")
  void tc0536_noOffsetConstantStatements() throws Exception {
    String t = laneTeacher();
    String one = "e5-one-" + Uuid7.next();
    String twenty = "e5-twenty-" + Uuid7.next();
    var r1 = get("/api/v1/catalog/problems?limit=1", t, one);
    var r20 = get("/api/v1/catalog/problems?limit=20", t, twenty);
    assertThat(r1.statusCode()).isEqualTo(200);
    assertThat(json(r1).get("items").size()).isEqualTo(1);
    assertThat(json(r20).get("items").size()).isEqualTo(20);
    assertThat(counter.count(one)).as("%s", counter.statements(one)).isPositive();
    assertThat(counter.count(twenty))
        .as("trang 20 đề phải chạy cùng số câu với trang 1 đề: %s", counter.statements(twenty))
        .isEqualTo(counter.count(one));
    for (String sql : counter.statements(twenty)) {
      assertThat(sql.toUpperCase()).doesNotContain("OFFSET");
    }
  }
}
