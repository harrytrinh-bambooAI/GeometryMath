package vn.geo.server.it.e1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import vn.geo.server.aigateway.internal.AiSettings;
import vn.geo.server.aigateway.internal.MockAiControl;
import vn.geo.server.content.internal.service.BenchmarkReport;
import vn.geo.server.content.internal.service.BenchmarkRunner;
import vn.geo.server.content.internal.service.CatalogImporter;

/** US-GEO-105 (chạy bộ đề chuẩn, đo tỉ lệ) và US-GEO-106 (nạp đề chuẩn + đáp số tham chiếu). */
class BenchmarkE1IT extends AbstractE1IT {

  static final Path SOURCE = Path.of("../../materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx");

  @Autowired CatalogImporter importer;
  @Autowired BenchmarkRunner runner;
  @Autowired MockAiControl mock;
  @Autowired AiSettings settings;

  @AfterEach
  void restore() {
    mock.reset();
    settings.reset();
  }

  private CatalogImporter.Result freshImport(Path file) throws IOException {
    reseed();
    return importer.importXlsx(file);
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

  /** Chép tệp xlsx, sửa XML của trang "Bai tap" (sheet1). */
  static Path variant(UnaryOperator<String> sheet1) throws IOException {
    Path out = Files.createTempFile("bo-de-", ".xlsx");
    try (ZipFile zin = new ZipFile(SOURCE.toFile());
        OutputStream os = Files.newOutputStream(out);
        ZipOutputStream zout = new ZipOutputStream(os)) {
      for (ZipEntry e : java.util.Collections.list(zin.entries())) {
        zout.putNextEntry(new ZipEntry(e.getName()));
        try (InputStream in = zin.getInputStream(e)) {
          byte[] b = in.readAllBytes();
          if (e.getName().equals("xl/worksheets/sheet1.xml")) {
            b =
                sheet1
                    .apply(new String(b, StandardCharsets.UTF_8))
                    .getBytes(StandardCharsets.UTF_8);
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

  // ------------------------------------------------------------------ US-GEO-106

  @Test
  @DisplayName("TC-GEO-0143 Sáu đề có đáp số tham chiếu đúng nguồn (US-GEO-106-SC1)")
  void tc0143_sixProblemsFromSource() throws Exception {
    var r = freshImport(SOURCE);
    assertThat(r.codes())
        .containsExactly("HKG-01", "HKG-02", "HKG-03", "HKG-04", "HKG-05", "HKG-06");
    assertThat(r.problems()).isEmpty();
    Map<String, Object> hkg03 =
        jdbc.sql(
                """
                SELECT pv.statement, s.final_answer FROM benchmark_item bi
                JOIN problem_version pv ON pv.id = bi.problem_version_id
                JOIN solution s ON s.problem_version_id = pv.id
                WHERE bi.set_version = :set AND bi.code = 'HKG-03'
                """)
            .param("set", r.setVersion())
            .query()
            .singleRow();
    assertThat(hkg03.get("statement"))
        .isEqualTo(
            "Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 6 cm. Tính thể tích.");
    assertThat(hkg03.get("final_answer")).isEqualTo("24√3 cm³");
    assertThat(
            jdbc.sql(
                    """
                    SELECT s.final_answer FROM benchmark_item bi JOIN solution s ON s.problem_version_id = bi.problem_version_id
                    WHERE bi.set_version = :set ORDER BY bi.code
                    """)
                .param("set", r.setVersion())
                .query(String.class)
                .list())
        .containsExactly(
            "45°",
            "a",
            "24√3 cm³",
            "Sxq = 15π cm²; V = 12π cm³",
            "112π cm²",
            "R = a√3/2; V = (√3/2)·π·a³");
  }

  private JsonNode forms(int set, String code) {
    String json =
        jdbc.sql(
                """
                SELECT s.answer_forms::text FROM benchmark_item bi JOIN solution s ON s.problem_version_id = bi.problem_version_id
                WHERE bi.set_version = :set AND bi.code = :code
                """)
            .param("set", set)
            .param("code", code)
            .query(String.class)
            .single();
    return mapper.readTree(json);
  }

  @Test
  @DisplayName("TC-GEO-0144 Đáp số nhiều giá trị lưu thành từng đại lượng (US-GEO-106-SC2)")
  void tc0144_multiValueAnswer() throws Exception {
    var r = freshImport(SOURCE);
    JsonNode f = forms(r.setVersion(), "HKG-04");
    assertThat(f.size()).isEqualTo(2);
    assertThat(f.get(0).get("name").asString()).isEqualTo("Sxq");
    assertThat(f.get(0).get("expr").asString()).isEqualTo("15π");
    assertThat(f.get(0).get("values").get(0).get("value").asDouble()).isCloseTo(47.1239, within());
    assertThat(f.get(1).get("name").asString()).isEqualTo("V");
    assertThat(f.get(1).get("values").get(0).get("value").asDouble()).isCloseTo(37.6991, within());
  }

  private static org.assertj.core.data.Offset<Double> within() {
    return org.assertj.core.data.Offset.offset(1e-4);
  }

  @Test
  @DisplayName("TC-GEO-0145 Đáp số theo tham số có ba bộ giá trị kiểm (US-GEO-106-SC3)")
  void tc0145_parametricAnswer() throws Exception {
    var r = freshImport(SOURCE);
    JsonNode values = forms(r.setVersion(), "HKG-02").get(0).get("values");
    assertThat(values.size()).isEqualTo(3);
    for (int a = 1; a <= 3; a++) {
      assertThat(values.get(a - 1).get("a").asInt()).isEqualTo(a);
      assertThat(values.get(a - 1).get("value").asDouble()).isCloseTo(a, within());
    }
  }

  @Test
  @DisplayName("TC-GEO-0146 Một đề, một bản ghi cho cả kho đề và bộ đề chuẩn (US-GEO-106-SC4)")
  void tc0146_oneRecord() throws Exception {
    var r = freshImport(SOURCE);
    importer.importXlsx(SOURCE); // nạp lại cùng nội dung: không bản sao, không đổi phiên bản bộ đề
    assertThat(count("SELECT count(*) FROM problem WHERE code = 'HKG-01'")).isEqualTo(1);
    assertThat(count("SELECT count(*) FROM benchmark_set")).isEqualTo(1);
    Map<String, Object> item =
        jdbc.sql(
                "SELECT problem_id, problem_version_id FROM benchmark_item WHERE set_version = :s AND code = 'HKG-01'")
            .param("s", r.setVersion())
            .query()
            .singleRow();
    JsonNode bank = json(get("/api/v1/catalog/problems?limit=50", teacherA()));
    JsonNode hkg01 = null;
    for (JsonNode p : bank.get("items")) {
      if (p.get("code").asString().equals("HKG-01")) {
        hkg01 = p;
      }
    }
    assertThat(hkg01).isNotNull();
    assertThat(UUID.fromString(hkg01.get("id").asString())).isEqualTo(item.get("problem_id"));
    assertThat(UUID.fromString(hkg01.get("versionId").asString()))
        .isEqualTo(item.get("problem_version_id"));
  }

  @Test
  @DisplayName(
      "TC-GEO-0147 Đề thiếu đáp số không vào bộ đề chuẩn một cách im lặng (US-GEO-106-FC1)")
  void tc0147_missingAnswerListed() throws Exception {
    var r = freshImport(variant(x -> blankCell(x, "H6")));
    assertThat(r.problems()).containsExactly("HKG-05: thiếu đáp số tham chiếu");
    assertThat(r.codes()).hasSize(6);
    assertThat(
            count(
                "SELECT count(*) FROM benchmark_item WHERE set_version = "
                    + r.setVersion()
                    + " AND NOT missing_reference"))
        .isEqualTo(5);
  }

  @Test
  @DisplayName("TC-GEO-0148 Nhãn trong tệp nguồn không thành nhãn máy (US-GEO-106-FC2)")
  void tc0148_fileLabelsNotLoaded() throws Exception {
    reseed();
    // trạng thái "nạp xong, engine chưa chạy": bỏ nhãn máy của seed
    jdbc.sql(
            "UPDATE problem_version SET verify_status = 'needs_review', verification_report_id = NULL WHERE org_id = platform_org_id()")
        .update();
    // tệp ghi "Đã kiểm chứng" cho HKG-01 (đề sửa nhẹ để chắc chắn tạo version mới) và "Cần xem lại"
    // cho HKG-06
    Path file =
        variant(
            x -> x.replace("Tính góc giữa SD và mặt đáy.", "Tính góc giữa SD và mặt đáy (ABCD)."));
    var r = importer.importXlsx(file);
    assertThat(r.codes()).hasSize(6);
    assertThat(
            count(
                "SELECT count(*) FROM problem_version WHERE org_id = platform_org_id() AND verify_status = 'verified'"))
        .isZero();
    assertThat(labelOf("HKG-01")).isEqualTo("needs_review");
    assertThat(labelOf("HKG-06")).isEqualTo("needs_review");
  }

  // ------------------------------------------------------------------ US-GEO-105

  @Test
  @DisplayName("TC-GEO-0135 Báo cáo của một lần chạy trên sáu đề (US-GEO-105-SC1)")
  void tc0135_sixProblemReport() throws Exception {
    var imp = freshImport(SOURCE);
    BenchmarkReport rep = runner.run();
    assertThat(rep.n()).isEqualTo(6);
    assertThat(rep.sceneLine()).startsWith("Hình thỏa ràng buộc: 6/6 = 100,0%");
    assertThat(rep.answerLine()).startsWith("Đáp số đúng: 6/6 = 100,0%");
    assertThat(rep.wrongVerified()).isZero();
    assertThat(rep.verified()).isEqualTo(6);
    assertThat(rep.model()).isEqualTo("gia-lap-1");
    assertThat(rep.promptVersion()).isEqualTo("p1");
    assertThat(rep.setVersion()).isEqualTo(imp.setVersion());
    assertThat(rep.text())
        .contains(
            "N = 6",
            "Model: gia-lap-1",
            "Phiên bản prompt: p1",
            "Bộ đề chuẩn phiên bản " + imp.setVersion())
        .contains("Bài sai mang \"Đã kiểm chứng\": 0", "Số đề đã kiểm chứng: 6");
    assertThat(labelOf("HKG-06")).isEqualTo("verified"); // D-GEO-105: khác nhãn trong tệp là đúng ý
  }

  @Test
  @DisplayName("TC-GEO-0136 Một đề có đáp số sai làm tỉ lệ giảm đúng công thức (US-GEO-105-SC2)")
  void tc0136_wrongAnswerLowersRate() throws Exception {
    freshImport(SOURCE);
    mock.overrideAnswer("HKG-01", "60°"); // F-GEO-070
    BenchmarkReport rep = runner.run();
    assertThat(rep.answerLine()).startsWith("Đáp số đúng: 5/6 = 83,3%");
    assertThat(rep.sceneLine()).startsWith("Hình thỏa ràng buộc: 6/6 = 100,0%");
    assertThat(labelOf("HKG-01")).isEqualTo("needs_review");
    assertThat(rep.wrongVerified()).isZero();
    assertThat(rep.verified()).isEqualTo(5);
  }

  @Test
  @DisplayName("TC-GEO-0137 Chạy lại khi đổi model giữ cả hai báo cáo (US-GEO-105-SC3)")
  void tc0137_rerunKeepsReports() throws Exception {
    freshImport(SOURCE);
    runner.run();
    settings.model("gia-lap-2");
    BenchmarkReport second = runner.run();
    assertThat(second.model()).isEqualTo("gia-lap-2");
    List<String> models =
        jdbc.sql("SELECT model FROM benchmark_run ORDER BY created_at, id")
            .query(String.class)
            .list();
    assertThat(models).containsExactly("gia-lap-1", "gia-lap-2");
    assertThat(
            jdbc.sql("SELECT report FROM benchmark_run WHERE model = 'gia-lap-1'")
                .query(String.class)
                .single())
        .contains("Model: gia-lap-1");
  }

  @Test
  @DisplayName("TC-GEO-0138 So với ngưỡng đề xuất (US-GEO-105-SC4)")
  void tc0138_thresholds() {
    var pass = new BenchmarkReport(1, "gia-lap-1", "p1", true, 40, 38, 36, 0, 36, List.of());
    assertThat(pass.sceneLine()).endsWith("95,0% · đạt (ngưỡng đề xuất ≥ 95,0%)");
    assertThat(pass.answerLine()).endsWith("90,0% · đạt (ngưỡng đề xuất ≥ 90,0%)");
    var fail = new BenchmarkReport(1, "gia-lap-1", "p1", true, 40, 37, 35, 0, 35, List.of());
    assertThat(fail.sceneLine()).endsWith("92,5% · chưa đạt (ngưỡng đề xuất)");
    assertThat(fail.answerLine()).endsWith("87,5% · chưa đạt (ngưỡng đề xuất)");
  }

  @Test
  @DisplayName("TC-GEO-0139 Ghi chú AI giả lập (US-GEO-105-SC5)")
  void tc0139_simulatedNote() throws Exception {
    freshImport(SOURCE);
    BenchmarkReport rep = runner.run();
    assertThat(rep.text().lines().findFirst())
        .hasValue("Nhà cung cấp AI giả lập: số đo chưa phản ánh độ đúng của AI thật.");
  }

  @Test
  @DisplayName("TC-GEO-0140 Bộ đề trống (US-GEO-105-FC1)")
  void tc0140_emptySet() throws Exception {
    var imp = freshImport(variant(x -> x.replaceAll("(?s)<row r=\"[2-9]\".*?</row>", "")));
    assertThat(imp.codes()).isEmpty();
    BenchmarkReport rep = runner.run();
    assertThat(rep.n()).isZero();
    assertThat(rep.sceneLine()).isEqualTo("Hình thỏa ràng buộc: 0/0 = —");
    assertThat(rep.answerLine()).isEqualTo("Đáp số đúng: 0/0 = —");
    assertThat(rep.wrongVerified()).isZero();
  }

  @Test
  @DisplayName("TC-GEO-0141 Một đề thiếu đáp số tham chiếu (US-GEO-105-FC2)")
  void tc0141_missingReference() throws Exception {
    freshImport(variant(x -> blankCell(x, "H6")));
    BenchmarkReport rep = runner.run();
    assertThat(rep.n()).isEqualTo(6);
    assertThat(rep.answerLine()).startsWith("Đáp số đúng: 5/6 = 83,3%");
    assertThat(rep.text()).contains("Thiếu đáp số tham chiếu: HKG-05");
    assertThat(labelOf("HKG-05")).isEqualTo("needs_review");
  }

  @Test
  @DisplayName(
      "TC-GEO-0142 Không bản ghi nào mang \"Đã kiểm chứng\" khi hình hoặc đáp số chưa khớp (US-GEO-105-FC3)")
  void tc0142_noWrongVerified() throws Exception {
    freshImport(SOURCE);
    mock.overrideAnswer("HKG-01", "60°");
    runner.run();
    assertThat(
            count(
                """
                SELECT count(*) FROM problem_version pv JOIN verification_report r ON r.id = pv.verification_report_id
                WHERE pv.verify_status = 'verified' AND NOT (r.scene_ok AND r.answer_ok)
                """))
        .isZero();
    UUID hkg01 =
        jdbc.sql(
                "SELECT current_version_id FROM problem WHERE org_id = platform_org_id() AND code = 'HKG-01'")
            .query(UUID.class)
            .single();
    assertThatThrownBy(() ->
            jdbc.sql(
                    "UPDATE problem_version SET verify_status = 'verified' WHERE org_id = platform_org_id() AND id = :id")
                .param("id", hkg01)
                .update())
        .hasMessageContaining("Đã kiểm chứng");
    assertThat(labelOf("HKG-01")).isEqualTo("needs_review");
  }
}
