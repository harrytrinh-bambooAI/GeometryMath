package vn.geo.server.it.mvp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.content.internal.service.ContentPackLoader;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Kho đề MVP ≥ 40 đề đã kiểm chứng (T-261126-02; PO-60, MVP.md §5 P0, tiêu chí US-GEO-504-SC4): nạp
 * gói nội dung vào CSDL, nhãn do engine tính (bảng verification_report), Kho đề của giáo viên đạt
 * tiêu chí MVP, đề mới có hình dựng bằng engine, nạp lại không tạo bản sao.
 */
class ContentPackMvpIT extends AbstractE1IT {

  @Autowired ContentPackLoader loader;

  @AfterEach
  void restoreGolden() throws IOException {
    reseed(); // lớp IT khác đọc dữ liệu vàng (Kho đề sáu đề)
  }

  private long verifiedInDb() {
    return count(
        """
        SELECT count(*) FROM problem p
        JOIN problem_version pv ON pv.org_id = p.org_id AND pv.id = p.current_version_id
        JOIN verification_report r ON r.org_id = pv.org_id AND r.id = pv.verification_report_id
        WHERE p.org_id = platform_org_id() AND p.code LIKE 'HKN-%' AND p.bank = 'main'
          AND p.archived_at IS NULL AND pv.verify_status = 'verified' AND r.result = 'verified'
          AND r.scene_ok AND r.answer_ok
        """);
  }

  private UUID currentVersion(String code) {
    return jdbc.sql(
            "SELECT current_version_id FROM problem WHERE org_id = platform_org_id() AND code = :code")
        .param("code", code)
        .query(UUID.class)
        .single();
  }

  @Test
  @DisplayName("Gói nội dung: ≥ 40 đề máy kiểm chứng trong CSDL, Kho đề giáo viên đạt tiêu chí MVP")
  void packLoadsFortyVerified() throws Exception {
    reseed();
    ContentPackLoader.Result r = loader.load();
    assertThat(r.codes()).hasSizeGreaterThanOrEqualTo(40);
    assertThat(r.verified()).isGreaterThanOrEqualTo(40);
    assertThat(r.statuses().values()).allMatch("verified"::equals);
    assertThat(verifiedInDb()).isGreaterThanOrEqualTo(40).isEqualTo(r.codes().size());

    JsonNode sum = json(get("/api/v1/catalog/summary", teacherA()));
    assertThat(sum.get("total").asLong()).isEqualTo(6 + r.codes().size());
    assertThat(sum.get("verified").asLong()).isEqualTo(6 + r.codes().size());
    assertThat(sum.get("mvpMet").asBoolean()).isTrue();
    assertThat(sum.get("mvp").asString()).isEqualTo("đạt");

    List<String> codes = new ArrayList<>();
    JsonNode page = json(get("/api/v1/catalog/problems?limit=50", teacherA()));
    page.get("items").forEach(p -> codes.add(p.get("code").asString()));
    assertThat(codes).contains("HKG-01", "HKN-01", "HKN-20");

    // đề mới: chi tiết có hình engine dựng, nhãn máy "Đã kiểm chứng", lời giải AI soạn
    JsonNode d =
        json(get("/api/v1/teacher/catalog/problems/" + currentVersion("HKN-01"), teacherA()));
    assertThat(d.get("label").asString()).isEqualTo("Đã kiểm chứng");
    assertThat(d.get("scene").get("points").size()).isEqualTo(5);
    assertThat(
            count(
                "SELECT count(*) FROM solution s JOIN problem p ON p.current_version_id = s.problem_version_id"
                    + " WHERE p.org_id = platform_org_id() AND p.code LIKE 'HKN-%' AND s.ai_authored"))
        .isEqualTo(r.codes().size());
    assertThat(
            count(
                "SELECT count(*) FROM hint h JOIN problem p ON p.current_version_id = h.problem_version_id"
                    + " WHERE p.org_id = platform_org_id() AND p.code LIKE 'HKN-%'"))
        .isEqualTo(2L * r.codes().size());
  }

  @Test
  @DisplayName("Gói nội dung: nạp lại cùng nội dung không tạo đề hay version mới")
  void reloadIsIdempotent() throws Exception {
    reseed();
    ContentPackLoader.Result first = loader.load();
    UUID v = currentVersion("HKN-05");
    long problems = count("SELECT count(*) FROM problem WHERE org_id = platform_org_id()");
    ContentPackLoader.Result again = loader.load();
    assertThat(first.written()).isEqualTo(first.codes().size());
    assertThat(again.written()).isZero();
    assertThat(currentVersion("HKN-05")).isEqualTo(v);
    assertThat(count("SELECT count(*) FROM problem WHERE org_id = platform_org_id()"))
        .isEqualTo(problems);
    assertThat(again.verified()).isGreaterThanOrEqualTo(40);
  }
}
