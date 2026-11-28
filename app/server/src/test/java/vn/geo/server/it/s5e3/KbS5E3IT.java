package vn.geo.server.it.s5e3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.knowledge.api.KbRetriever;
import vn.geo.server.knowledge.api.KbRetriever.Hit;

/**
 * S5-E3 kho tri thức (T-261128-56) trên dữ liệu mẫu F-S5-040 / F-S5-041 (seed s5_300_s5e3_kb.sql):
 * trạng thái phiên bản, quyền theo quan hệ, phiên bản bất biến ở CSDL, bộ truy xuất bỏ dấu. Chỉ đọc
 * dữ liệu mẫu (không đổi seed dùng chung). Ca E2E đầy đủ: e2e/tests/s5e3/kb.api.spec.ts.
 */
class KbS5E3IT extends AbstractE1IT {

  static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000000100");
  static final UUID LAN = UUID.fromString("00000000-0000-7000-8003-000000000001");
  static final UUID KHOI = UUID.fromString("00000000-0000-7000-8003-000000000002");
  static final UUID BINH_AN = UUID.fromString("00000000-0000-7000-8003-000000000020");
  static final UUID C11A1 = UUID.fromString("00000000-0000-7000-8003-000000000010");
  static final UUID C10B = UUID.fromString("00000000-0000-7000-8003-000000000012");
  static final UUID TL01 = UUID.fromString("00000000-0000-7000-8005-000000000040");
  static final String KB = "/api/v1/teacher/knowledge";

  @Autowired KbRetriever retriever;

  String lan() {
    return token(ORG, LAN, Role.TEACHER);
  }

  @Test
  @DisplayName(
      "US-S5E3-01 Kho F-S5-040: TL-01 v2 Đang dùng, v1 Lưu trữ; TL-02 Chờ duyệt; TL-03 AI soạn")
  void seededStates() throws Exception {
    HttpResponse<String> r = get(KB + "/TL-01", lan());
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    JsonNode d = json(r);
    assertThat(d.get("activeVersion").asInt()).isEqualTo(2);
    assertThat(d.get("versions").get(0).get("status").asString()).isEqualTo("archived");
    assertThat(d.get("versions").get(1).get("status").asString()).isEqualTo("active");
    assertThat(d.get("sections").size()).isEqualTo(3);
    assertThat(d.get("sections").get(1).get("title").asString()).isEqualTo("Hệ thức 1/AH²");
    assertThat(d.get("sections").get(1).get("codes").get(0).asString())
        .isEqualTo("KT11-C07-CT-003");
    assertThat(r.headers().firstValue("Cache-Control").orElse("")).contains("no-store");
    assertThat(json(get(KB + "/TL-02", lan())).get("status").asString()).isEqualTo("pending");
    JsonNode tl03 = json(get(KB + "/tl-03", lan()));
    assertThat(tl03.get("humanLabel").asString()).isEqualTo("AI soạn");
    assertThat(tl03.get("statusLabel").asString()).isEqualTo("Nháp");
    JsonNode list = json(get(KB, lan()));
    assertThat(list.get("items").size()).isEqualTo(3);
  }

  @Test
  @DisplayName(
      "US-S5E3-01-FC4 Thầy Khôi NOT_FOUND, Bình An FORBIDDEN; TL-09 của thầy Khôi không lộ")
  void relationScoped() throws Exception {
    String khoi = token(ORG, KHOI, Role.TEACHER);
    assertThat(get(KB + "/TL-01", khoi).statusCode()).isEqualTo(404);
    assertThat(get(KB + "/" + TL01, khoi).statusCode()).isEqualTo(404);
    assertThat(get(KB + "/TL-01", token(ORG, BINH_AN, Role.STUDENT)).statusCode()).isEqualTo(403);
    assertThat(get(KB + "/TL-09", lan()).statusCode()).isEqualTo(404);
    assertThat(json(get(KB + "/TL-09", khoi)).get("activeVersion").asInt()).isEqualTo(1);
    // lớp của người khác: NOT_FOUND
    assertThat(get("/api/v1/teacher/classes/" + C10B + "/knowledge", lan()).statusCode())
        .isEqualTo(404);
    JsonNode mine = json(get("/api/v1/teacher/classes/" + C11A1 + "/knowledge", lan()));
    assertThat(mine.get("items").get(0).get("code").asString()).isEqualTo("TL-01");
    assertThat(mine.get("items").get(0).get("activeVersion").asInt()).isEqualTo(2);
  }

  @Test
  @DisplayName("BR-S5E3-01 Phiên bản đã từng Đang dùng bất biến ở CSDL (trigger)")
  void usedVersionImmutableInDb() {
    assertThatThrownBy(() ->
            jdbc.sql(
                    "UPDATE kb_document_version SET content_md = 'x' WHERE org_id = :o AND"
                        + " doc_id = :d AND version = 2")
                .param("o", ORG)
                .param("d", TL01)
                .update())
        .hasMessageContaining("bất biến");
    assertThatThrownBy(() ->
            jdbc.sql(
                    "UPDATE kb_document_version SET status = 'active' WHERE org_id = :o AND"
                        + " doc_id = :d AND version = 1")
                .param("o", ORG)
                .param("d", TL01)
                .update())
        .hasMessageContaining("kb_version_one_active_uq");
  }

  @Test
  @DisplayName("US-S5E3-07 Truy xuất bỏ dấu, ưu tiên mã kiến thức, chỉ tài liệu gắn lớp")
  void retrieval() {
    Instant now = Instant.now();
    List<Hit> h = retriever.retrieve(ORG, C11A1, null, "he thuc 1/AH2 khoang cach", List.of(), now);
    assertThat(h).isNotEmpty().hasSizeLessThanOrEqualTo(5);
    assertThat(h.get(0).title()).isEqualTo("Hệ thức 1/AH²");
    assertThat(h).allSatisfy(x -> assertThat(x.docCode()).isEqualTo("TL-01"));
    assertThat(h).allSatisfy(x -> assertThat(x.version()).isEqualTo(2));
    List<Hit> coded =
        retriever.retrieve(
            ORG, C11A1, null, "Cách tính KHOẢNG CÁCH", List.of("KT11-C07-CT-003"), now);
    assertThat(coded.get(0).boosted()).isTrue();
    assertThat(coded.get(0).title()).isEqualTo("Hệ thức 1/AH²");
    assertThat(retriever.retrieve(ORG, C11A1, null, "đạo hàm của hàm hợp", List.of(), now))
        .isEmpty();
    // TL-09 (gắn 10B) không bao giờ ra ở 11A1; chỉ từ dừng thì rỗng
    assertThat(retriever.retrieve(ORG, C11A1, null, "định lí côsin", List.of(), now)).isEmpty();
    assertThat(retriever.retrieve(ORG, C10B, null, "dinh li cosin", List.of(), now))
        .extracting(Hit::docCode)
        .containsExactly("TL-09");
    assertThat(retriever.retrieve(ORG, C11A1, null, "của là và", List.of(), now)).isEmpty();
  }
}
