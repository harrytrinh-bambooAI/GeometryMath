package vn.geo.server.lessonplan.internal.patch.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Đường dẫn bản vá (US-S5E1-04, BR-S5E1-11): đọc, mục, áp replace / add / remove, so nguyên văn.
 */
class PatchPathsTest {

  private final JsonMapper mapper = JsonMapper.builder().build();

  private ObjectNode lesson() {
    return (ObjectNode)
        mapper.readTree(
            """
            {"muc_tieu":{"nang_luc":["Tư duy"]},"thiet_bi":{"giao_vien":["Máy chiếu"]},
             "hoat_dong":[{"ma":"HD1","ten":"Khởi động: Khoảng cách từ điểm","thoi_luong_phut":5,
                           "to_chuc":{"chuyen_giao":"GV giao"}}],
             "phieu_hoc_tap":[{"ma":"PHT1","cau":[{"so":1,"nguon":"BM-007"}]}],"meta":{"bai":"26"}}
            """);
  }

  @Test
  @DisplayName("Mục của đường dẫn: muc_tieu, thiet_bi, HĐ, phiếu; ngoài phạm vi = null")
  void sections() {
    ObjectNode c = lesson();
    assertThat(PatchPaths.section(c, PatchPaths.parse("muc_tieu.nang_luc[0]")))
        .isEqualTo("muc_tieu");
    assertThat(PatchPaths.section(c, PatchPaths.parse("hoat_dong[0].to_chuc.chuyen_giao")))
        .isEqualTo("HD1");
    assertThat(PatchPaths.section(c, PatchPaths.parse("phieu_hoc_tap[0].cau"))).isEqualTo("PHT1");
    assertThat(PatchPaths.section(c, PatchPaths.parse("meta.bai"))).isNull();
    assertThat(PatchPaths.section(c, PatchPaths.parse("hoat_dong[0]")))
        .isNull(); // không thay cả HĐ (A6)
    assertThat(PatchPaths.section(c, PatchPaths.parse("hoat_dong"))).isNull();
    assertThat(PatchPaths.parse("hoat_dong[x].ten")).isNull();
  }

  @Test
  @DisplayName("before phải trùng nguyên văn; số so theo giá trị")
  void verbatim() {
    ObjectNode c = lesson();
    var ten = PatchPaths.get(c, PatchPaths.parse("hoat_dong[0].ten"));
    assertThat(
            LessonPatchService.same(
                ten, mapper.getNodeFactory().stringNode("Khởi động: Khoảng cách từ điểm")))
        .isTrue();
    assertThat(
            LessonPatchService.same(
                ten, mapper.getNodeFactory().stringNode("Khởi động: Khoảng cách")))
        .isFalse();
    var min = PatchPaths.get(c, PatchPaths.parse("hoat_dong[0].thoi_luong_phut"));
    assertThat(LessonPatchService.same(min, mapper.getNodeFactory().numberNode(5.0))).isTrue();
  }

  @Test
  @DisplayName("Áp replace, add (cuối danh sách), remove")
  void apply() {
    ObjectNode c = lesson();
    PatchPaths.apply(
        c,
        PatchPaths.parse("hoat_dong[0].ten"),
        "replace",
        mapper.getNodeFactory().stringNode("Trò chơi"));
    PatchPaths.apply(
        c,
        PatchPaths.parse("muc_tieu.nang_luc[0]"),
        "replace",
        mapper.getNodeFactory().stringNode("Mô hình hoá"));
    PatchPaths.apply(
        c,
        PatchPaths.parse("phieu_hoc_tap[0].cau"),
        "add",
        mapper.readTree("{\"so\":2,\"nguon\":\"moi\"}"));
    PatchPaths.apply(c, PatchPaths.parse("thiet_bi.giao_vien[0]"), "remove", null);
    assertThat(c.path("hoat_dong").path(0).path("ten").asString()).isEqualTo("Trò chơi");
    assertThat(c.path("muc_tieu").path("nang_luc").path(0).asString()).isEqualTo("Mô hình hoá");
    assertThat(c.path("phieu_hoc_tap").path(0).path("cau").size()).isEqualTo(2);
    assertThat(c.path("thiet_bi").path("giao_vien").size()).isZero();
  }

  @Test
  @DisplayName("Nhãn dòng bản vá")
  void labels() {
    assertThat(PatchPaths.label("HD1", PatchPaths.parse("hoat_dong[0].to_chuc.chuyen_giao")))
        .isEqualTo("HĐ1 · Chuyển giao");
    assertThat(PatchPaths.label("muc_tieu", PatchPaths.parse("muc_tieu.nang_luc[0]")))
        .isEqualTo("Mục tiêu · Năng lực 1");
    assertThat(LessonPatchService.sections(lesson()))
        .isEqualTo(java.util.Set.copyOf(List.of("muc_tieu", "thiet_bi", "HD1", "PHT1")));
  }
}
