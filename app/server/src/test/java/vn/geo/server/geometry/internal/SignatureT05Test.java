package vn.geo.server.geometry.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.GeometryEngine;

/**
 * T-05 (đo AI thật 27/11/2026 08:16): F-GEO-226 (dán đề HKG-01 đổi tên S.MNPQ vào Tự học) lộ "45°"
 * vì chữ ký chuẩn bản 1 ghép nguyên danh sách điểm của đại lượng hỏi: đề kho ghi mặt đáy là
 * (A,B,D), AI thật trả (A,B,C) — cùng một mặt phẳng nhưng lệch chữ ký, bài tự học không kế thừa
 * khóa. Bản 2 dùng giá trị đo trên bản dựng (BR-GEO-183: không phụ thuộc tên điểm). Mô tả dựng dưới
 * đây là đầu ra THẬT của claude-sonnet-5-5 (prompt scene a1) cho đề F-GEO-226.
 */
class SignatureT05Test {

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final DefaultGeometryEngine engine = new DefaultGeometryEngine(mapper);

  static final String F226_REAL_AI =
      """
      {"kind":"space","solid":"pyramid","level":"thpt","template":"pyramid_square_sa",
       "params":{"a":"a","sa":"a"},"unit":"a",
       "chips":[{"text":"SA = a","points":["S","A"]},{"text":"AB = a","points":["A","B"]}],
       "asked":[{"name":"","kind":"angle_line_plane","points":["S","D","A","B","C"]}],
       "hidden":["ang:S-D-A"],"answer":"45°"}
      """;

  private GeometryEngine.Signature sig(String code) {
    return engine.signature(engine.catalogSpec(code).orElseThrow()).orElseThrow();
  }

  private GeometryEngine.Signature sigOf(String json) {
    JsonNode n = mapper.readTree(json);
    return engine.signature(engine.specFrom(n)).orElseThrow();
  }

  @Test
  @DisplayName("Chữ ký bản 2 của sáu đề kho (chuỗi chuẩn khớp seed 401_e4_catalog.sql)")
  void catalogCanonical() {
    assertThat(sig("HKG-01").canonical())
        .isEqualTo("2|pyramid_square_sa|a=1;sa=1|angle_line_plane=45");
    assertThat(sig("HKG-02").canonical()).isEqualTo("2|cube|a=1|dist_lines=1");
    assertThat(sig("HKG-03").canonical()).isEqualTo("2|prism|a=1;h=1.5|volume_prism=0.649519");
    assertThat(sig("HKG-04").canonical())
        .isEqualTo("2|cone_rl|r=1;l=1.666667|cone_lateral=5.235989+cone_volume=1.396264");
    assertThat(sig("HKG-05").canonical())
        .isEqualTo("2|cylinder|r=1;h=2.5|cyl_total_area=21.991149");
    assertThat(sig("HKG-06").canonical())
        .isEqualTo("2|sphere_circum_cube|a=1|sphere_radius=0.866025+sphere_volume=2.720699");
  }

  @Test
  @DisplayName("T-05 F-GEO-226: mô tả dựng THẬT của AI (mặt đáy A,B,C) cùng chữ ký HKG-01")
  void f226RealAiMatchesHkg01() {
    assertThat(sigOf(F226_REAL_AI).sha256()).isEqualTo(sig("HKG-01").sha256());
  }

  @Test
  @DisplayName("Không phụ thuộc cách chọn điểm: SB đối xứng SD, mặt (ABCD) bằng bốn điểm bất kỳ")
  void pointChoiceInvariant() {
    String sb =
        F226_REAL_AI.replace("[\"S\",\"D\",\"A\",\"B\",\"C\"]", "[\"B\",\"S\",\"C\",\"D\",\"A\"]");
    assertThat(sigOf(sb).sha256()).isEqualTo(sig("HKG-01").sha256());
  }

  @Test
  @DisplayName("Khác tham số (SA = a√3, F-GEO-078) hoặc khác đại lượng (góc SC) thì khác chữ ký")
  void differentProblemsDiffer() {
    String sa3 = F226_REAL_AI.replace("\"sa\":\"a\"", "\"sa\":\"a√3\"");
    assertThat(sigOf(sa3).sha256()).isNotEqualTo(sig("HKG-01").sha256());
    String sc =
        F226_REAL_AI.replace("[\"S\",\"D\",\"A\",\"B\",\"C\"]", "[\"S\",\"C\",\"A\",\"B\",\"C\"]");
    assertThat(sigOf(sc).sha256()).isNotEqualTo(sig("HKG-01").sha256());
  }

  @Test
  @DisplayName(
      "Đại lượng hỏi thiếu điểm hoặc tên điểm lạ: không có chữ ký (chỉ gợi ý, không lời giải)")
  void badAskedNoSignature() {
    String bad =
        F226_REAL_AI.replace("[\"S\",\"D\",\"A\",\"B\",\"C\"]", "[\"S\",\"Q\",\"M\",\"N\",\"P\"]");
    assertThat(engine.signature(engine.specFrom(mapper.readTree(bad)))).isEmpty();
  }
}
