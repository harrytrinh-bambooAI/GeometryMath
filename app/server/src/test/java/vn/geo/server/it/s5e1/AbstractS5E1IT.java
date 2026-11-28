package vn.geo.server.it.s5e1;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import vn.geo.server.aigateway.internal.MockAiControl;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Nền IT của S5-E1 (T-261128-53): dữ liệu mẫu chung chặng 5 (F-S5-001 cô Lan = F-S3-001, F-S5-002
 * thầy Khôi, Bình An F-S3-020) và giáo án GA-0001 (F-S5-021, seed s5_150_s5e1_core.sql). Nhà cung
 * cấp AI giả lập: kịch bản lesson_plan theo người gọi (MockAiControl).
 */
abstract class AbstractS5E1IT extends AbstractE1IT {

  static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000000100");
  static final UUID LAN = UUID.fromString("00000000-0000-7000-8003-000000000001");
  static final UUID KHOI = UUID.fromString("00000000-0000-7000-8003-000000000002");
  static final UUID BINH_AN = UUID.fromString("00000000-0000-7000-8003-000000000020");
  static final String BASE = "/api/v1/teacher/lesson-plans";

  /** F-S5-020: Bài 26 · Tiết 1/2 · Khá · Truyền thống (chọn sẵn) · 2 kiến thức · Phiếu Bật. */
  static final String F020 =
      """
      {"tietPpct":"B26-T1","doiTuong":"kha","kienThuc":["KT11-C07-DN-010","KT11-C07-CT-003"],\
      "phieuHocTap":true}\
      """;

  @Autowired MockAiControl mock;

  @BeforeEach
  void cleanS5E1() {
    mock.reset();
    jdbc.sql("DELETE FROM lesson_plan_turn WHERE teacher_id = :t").param("t", LAN).update();
  }

  String lan() {
    return token(ORG, LAN, Role.TEACHER);
  }

  String khoi() {
    return token(ORG, KHOI, Role.TEACHER);
  }

  String binhAn() {
    return token(ORG, BINH_AN, Role.STUDENT);
  }

  long aiCalls() {
    return count("SELECT count(*) FROM ai_call_log WHERE user_id = '" + LAN + "'");
  }

  /** Gửi form, kỳ vọng 202; trả draftId. */
  String submit(String form) throws Exception {
    HttpResponse<String> r = post(BASE, lan(), form);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(202);
    return json(r).get("draftId").asString();
  }

  /** Chờ bản nháp rời trạng thái "Đang soạn" (AI chạy nền), trả phản hồi GET. */
  JsonNode await(String id) throws Exception {
    long end = System.currentTimeMillis() + 15_000;
    while (true) {
      HttpResponse<String> r = get(BASE + "/" + id, lan());
      assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
      JsonNode v = json(r);
      if (!"drafting".equals(v.get("status").asString()) || System.currentTimeMillis() > end) {
        return v;
      }
      Thread.sleep(100);
    }
  }

  /** Tin nhắn lesson_plan nhà cung cấp giả lập đã nhận (cũ trước). */
  List<String> lessonPrompts() {
    return mock.receivedPrompts().stream().filter(p -> p.contains("<phieu_yeu_cau>")).toList();
  }

  void scenario(String s) {
    mock.setScenario(LAN.toString(), null, s);
  }

  void script(String... replies) {
    mock.setQaScript(LAN.toString(), null, List.of(replies));
  }

  String withField(String field, String jsonValue) {
    return F020.substring(0, F020.length() - 1) + ",\"" + field + "\":" + jsonValue + "}";
  }
}
