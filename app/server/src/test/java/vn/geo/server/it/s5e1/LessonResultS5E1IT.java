package vn.geo.server.it.s5e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** US-S5E1-03 — màn kết quả: giáo án dựng theo kind (phần API). */
class LessonResultS5E1IT extends AbstractS5E1IT {

  @Test
  @DisplayName("TC-S5E1-16 Giáo án hợp lệ trả đủ khung và Σ 45'")
  void fullFrame() throws Exception {
    HttpResponse<String> r = get(BASE + "/GA-0001", lan());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    assertThat(r.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
    JsonNode v = json(r);
    assertThat(v.get("kind").asString()).isEqualTo("lesson");
    assertThat(v.get("status").asString()).isEqualTo("ready");
    assertThat(v.get("title").asString())
        .isEqualTo("Bài 26. Khoảng cách · Tiết 1/2 · Khá · Truyền thống · 45'");
    assertThat(v.get("humanLabel").asString()).isEqualTo("AI soạn");
    JsonNode l = v.get("lesson");
    assertThat(l.get("sections").valueStream().map(JsonNode::asString).toList())
        .containsExactly("Mục tiêu", "Thiết bị", "Tiến trình", "Phiếu học tập");
    assertThat(l.get("mucTieu").get("kienThuc")).isNotEmpty();
    assertThat(l.get("thietBi").get("giaoVien")).isNotEmpty();
    assertThat(l.get("hoatDong").valueStream().map(a -> a.get("thoiLuongPhut").asInt()).toList())
        .containsExactly(5, 20, 15, 5);
    assertThat(l.get("tongPhut").asInt()).isEqualTo(45);
    JsonNode pht = l.get("phieuHocTap").get(0);
    assertThat(pht.get("ma").asString()).isEqualTo("PHT1");
    assertThat(pht.get("cau")).hasSize(3);
    pht.get("cau").forEach(c -> assertThat(c.get("dapSoNguon").asString()).isEqualTo("kho"));
    assertThat(v.get("lockedSections").valueStream().map(JsonNode::asString).toList())
        .containsExactly("muc_tieu", "thiet_bi");
  }

  @Test
  @DisplayName("TC-S5E1-19 Bị từ chối an toàn trả thông điệp chung")
  void refusalShowsGenericMessage() throws Exception {
    scenario("refusal");
    JsonNode v = await(submit(F020));
    assertThat(v.get("status").asString()).isEqualTo("refused");
    assertThat(v.get("message").asString()).isEqualTo("AI không xử lý được yêu cầu này.");
    assertThat(v.has("lesson")).isFalse();
    assertThat(v.has("reply")).isFalse();
    assertThat(v.toString()).doesNotContain("refusal").doesNotContain("stop_reason");
  }

  @Test
  @DisplayName("TC-S5E1-18 AI hỏi lại đúng một câu (phần API: kind question, chưa có nội dung)")
  void questionKind() throws Exception {
    scenario("question");
    JsonNode v = await(submit(withField("ghiChu", "\"dạy gộp hai tiết\"")));
    assertThat(v.get("kind").asString()).isEqualTo("question");
    assertThat(v.get("reply").get("text").asString()).isEqualTo("Thầy cô muốn soạn 1 hay 2 tiết?");
    assertThat(v.get("reply").get("field").asString()).isEqualTo("soTiet");
    assertThat(v.has("lesson")).isFalse();
  }

  @Test
  @DisplayName("TC-S5E1-17 unsure HĐ4 và đề xuất gom riêng (phần API: chưa áp đề xuất)")
  void unsureAndProposals() throws Exception {
    scenario("unsure");
    JsonNode v = await(submit(F020));
    JsonNode l = v.get("lesson");
    assertThat(l.get("hoatDong").get(3).get("unsure")).hasSize(1);
    assertThat(l.get("hoatDong").get(0).get("unsure")).isEmpty();
    assertThat(l.get("deXuat")).hasSize(1);
    assertThat(l.get("deXuat").get(0).get("vanDe").asString()).isEqualTo("Tổng 50' > 45'");
    // đề xuất chưa được áp: thời lượng giữ nguyên
    assertThat(l.get("tongPhut").asInt()).isEqualTo(45);
  }
}
