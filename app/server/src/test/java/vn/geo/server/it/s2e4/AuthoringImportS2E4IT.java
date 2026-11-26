package vn.geo.server.it.s2e4;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.content.importer.ImportTestFiles;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * S2-E4 — tích hợp (máy chủ + CSDL + AI giả lập): đề xuất sửa đáp số không kiểm được giữ "Cần xem
 * lại" (US-S2E4-01-FC2), macro trong docx không chạy (US-S2E4-02-FC3), đối chiếu trùng khi import
 * lại (US-S2E4-03-SC3, FC2, FC3) và giới hạn thêm đề theo tài khoản áp cho đường import
 * (BR-S2E6-13). Dữ liệu: db/seed/e2e/860_s2e4_core.sql, mỗi ca một giáo viên riêng (làn 0).
 */
class AuthoringImportS2E4IT extends AbstractE1IT {

  static final UUID ORG = UUID.fromString("00000000-0000-7000-8002-000000860001");
  static final String DRAFTS = "/api/v1/teacher/problem-drafts";
  static final String JOBS = "/api/v1/teacher/problem-draft-jobs/";
  static final String IMPORTS = "/api/v1/teacher/problem-imports";

  private final HttpClient http = HttpClient.newHttpClient();

  @AfterEach
  void restore() throws Exception {
    post("/test-hooks/ai/reset", null, "{}");
  }

  static UUID teacher(int tc) {
    return UUID.fromString(String.format("00000000-0000-7000-8002-%012d", 861000000L + tc * 100L));
  }

  String tk(int tc) {
    return token(ORG, teacher(tc), Role.TEACHER);
  }

  JsonNode ok(HttpResponse<String> r) {
    assertThat(r.statusCode()).as(r.body()).isEqualTo(200);
    return json(r);
  }

  /** Tạo nháp chữ, dựng và chờ bốn bước xong. */
  JsonNode built(int tc, String statement) throws Exception {
    String body =
        mapper.createObjectNode().put("source", "text").put("statement", statement).toString();
    String id = ok(postJson(DRAFTS, tk(tc), body)).path("id").asString();
    String jobId =
        ok(postJson(DRAFTS + "/" + id + "/build", tk(tc), "{}")).path("job").path("id").asString();
    long end = System.currentTimeMillis() + 20_000;
    JsonNode j;
    do {
      Thread.sleep(100);
      j = ok(get(JOBS + jobId, tk(tc)));
    } while ("running".equals(j.path("state").asString()) && System.currentTimeMillis() < end);
    assertThat(j.path("state").asString()).isEqualTo("done");
    return ok(get(DRAFTS + "/" + id, tk(tc)));
  }

  JsonNode chat(int tc, String draftId, String path, String body) throws Exception {
    return ok(postJson(DRAFTS + "/" + draftId + "/chat" + path, tk(tc), body));
  }

  static JsonNode last(JsonNode chatView) {
    JsonNode m = chatView.path("messages");
    return m.get(m.size() - 1);
  }

  /** Tạo lượt import rồi gửi tệp theo đoạn 1 000 000 byte; trả phản hồi của đoạn cuối. */
  HttpResponse<String> upload(int tc, String name, byte[] bytes) throws Exception {
    String body =
        mapper.createObjectNode().put("fileName", name).put("sizeBytes", bytes.length).toString();
    String id = ok(postJson(IMPORTS, tk(tc), body)).path("id").asString();
    HttpResponse<String> last = null;
    for (int off = 0; off < bytes.length; off += 1_000_000) {
      byte[] chunk =
          java.util.Arrays.copyOfRange(bytes, off, Math.min(bytes.length, off + 1_000_000));
      last =
          http.send(
              HttpRequest.newBuilder(
                      URI.create(
                          "http://127.0.0.1:"
                              + port
                              + IMPORTS
                              + "/"
                              + id
                              + "/file?offset="
                              + off
                              + "&total="
                              + bytes.length))
                  .header("Authorization", "Bearer " + tk(tc))
                  .header("Content-Type", "application/octet-stream")
                  .PUT(HttpRequest.BodyPublishers.ofByteArray(chunk))
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      if (last.statusCode() != 200) {
        return last;
      }
    }
    return last;
  }

  JsonNode save(int tc, JsonNode view, int... indexes) throws Exception {
    ArrayNode arr = mapper.createArrayNode();
    for (int i : indexes) {
      arr.add(i);
    }
    String body = "{\"indexes\":" + arr + "}";
    return ok(postJson(IMPORTS + "/" + view.path("id").asString() + "/drafts", tk(tc), body));
  }

  static List<String> states(JsonNode view) {
    List<String> out = new ArrayList<>();
    view.path("items").forEach(i -> out.add(i.path("state").asString()));
    return out;
  }

  long drafts(int tc) {
    return count("SELECT count(*) FROM problem_draft WHERE owner_user_id = '" + teacher(tc) + "'");
  }

  @Test
  @DisplayName("TC-S2E4-011 Đề xuất đổi đáp số không kiểm được thì giữ nhãn Cần xem lại")
  void tc011_answerProposalStaysNeedsReview() throws Exception {
    JsonNode d = built(11, ImportTestFiles.FX10);
    String id = d.path("id").asString();
    assertThat(d.path("machineLabel").asString()).isEqualTo("Đã kiểm chứng");

    JsonNode v = chat(11, id, "/messages", "{\"text\":\"sửa đáp số thành 80°\"}");
    JsonNode p = last(v);
    assertThat(p.path("kind").asString()).isEqualTo("proposal");
    assertThat(p.path("proposal").path("target").asString()).isEqualTo("answer");
    assertThat(p.path("proposal").path("after").asString()).isEqualTo("80°");
    // chưa nhận: nháp chưa đổi
    assertThat(v.path("machineLabel").asString()).isEqualTo("Đã kiểm chứng");

    JsonNode acc = chat(11, id, "/proposals/" + p.path("id").asString() + "/accept", "{}");
    assertThat(acc.path("machineLabel").asString()).isEqualTo("Cần xem lại");
    assertThat(acc.path("humanLabel").asString()).isEqualTo("AI soạn");

    // chạy kiểm: máy tính lại 90°, đề ghi 80° → lệch, nhãn giữ "Cần xem lại", hộp duyệt báo lệch
    JsonNode chk = chat(11, id, "/recheck", "{}");
    JsonNode result = chk.path("draft").path("result");
    assertThat(result.path("answers").path("stated").asString()).isEqualTo("80°");
    assertThat(result.path("answers").path("computed").asString()).isEqualTo("90°");
    assertThat(result.path("answers").path("state").asString()).isEqualTo("mismatch");
    assertThat(chk.path("machineLabel").asString()).isEqualTo("Cần xem lại");
    assertThat(result.path("approveBlocked").asBoolean()).isTrue();
    assertThat(result.path("blockedReason").asString()).isEqualTo("Chặn duyệt: đáp số chưa khớp");
    assertThat(
            count(
                "SELECT count(*) FROM solution_version v JOIN solution_doc d ON d.id = v.doc_id"
                    + " WHERE d.draft_id = '"
                    + id
                    + "' AND v.machine_status = 'verified'"))
        .isZero();

    // hoàn tác trả đáp số đề ghi về như trước; kiểm lại thì đạt
    JsonNode undone = chat(11, id, "/undo", "{}");
    assertThat(undone.path("canUndo").asBoolean()).isFalse();
    assertThat(undone.path("undoNote").asString()).isEqualTo("Hết bước hoàn tác");
    assertThat(chat(11, id, "/recheck", "{}").path("machineLabel").asString())
        .isEqualTo("Đã kiểm chứng");
  }

  @Test
  @DisplayName("TC-S2E4-019 Lệnh macro trong docx không chạy")
  void tc019_macroIgnored() throws Exception {
    JsonNode v = ok(upload(19, "co-macro.docx", ImportTestFiles.docx(ImportTestFiles.THREE, true)));
    assertThat(v.path("status").asString()).isEqualTo("parsed");
    assertThat(v.path("fileType").asString()).isEqualTo("docx");
    assertThat(v.path("items")).hasSize(3);
    List<String> notes = new ArrayList<>();
    v.path("notes").forEach(n -> notes.add(n.asString()));
    assertThat(notes).contains("Tệp có macro, đã bỏ qua", "Phần lý thuyết chưa hỗ trợ ở chặng này");
    // tệp được đọc như văn bản: không có chữ nào của macro lọt vào mục, tệp gốc không còn giữ
    assertThat(v.toString()).doesNotContain("AutoOpen").doesNotContain("Shell");
    assertThat(
            count(
                "SELECT count(*) FROM import_job WHERE owner_user_id = '"
                    + teacher(19)
                    + "' AND file_tmp IS NOT NULL"))
        .isZero();
    assertThat(
            count(
                "SELECT count(*) FROM import_item WHERE owner_user_id = '"
                    + teacher(19)
                    + "' AND (body ILIKE '%AutoOpen%' OR body ILIKE '%Shell%')"))
        .isZero();
    assertThat(drafts(19)).isZero();
  }

  @Test
  @DisplayName("TC-S2E4-024 Mục đã sửa ghi cập nhật khi lưu")
  void tc024_updatedItem() throws Exception {
    byte[] file = ImportTestFiles.docx(ImportTestFiles.THREE, false);
    JsonNode first = ok(upload(24, "T.docx", file));
    JsonNode saved = save(24, first, 1, 2, 3);
    assertThat(saved.path("created").asInt()).isEqualTo(3);
    assertThat(drafts(24)).isEqualTo(3);
    String draft1 = saved.path("items").get(0).path("draftId").asString();

    // Cô Trang chỉnh số liệu của bài 1 trong "Của tôi" (chat: đổi BC 13 → 14, nhận)
    JsonNode v = chat(24, draft1, "/messages", "{\"text\":\"đổi cạnh BC thành 14\"}");
    JsonNode acc =
        chat(24, draft1, "/proposals/" + last(v).path("id").asString() + "/accept", "{}");
    assertThat(acc.path("draft").path("statement").asString()).contains("BC = 14");

    JsonNode again = ok(upload(24, "T.docx", file));
    assertThat(states(again)).containsExactly("update", "exists", "exists");
    JsonNode item1 = again.path("items").get(0);
    assertThat(item1.path("note").asString()).isEqualTo("cập nhật khi lưu");
    assertThat(item1.path("refId").asString()).isEqualTo(draft1);
    assertThat(item1.path("checked").asBoolean()).isTrue();

    JsonNode resaved = save(24, again, 1);
    assertThat(resaved.path("updated").asInt()).isEqualTo(1);
    assertThat(resaved.path("created").asInt()).isZero();
    // lưu xong chỉ có MỘT bản của đề: vẫn 3 nháp, nháp cũ mang chữ của tệp
    assertThat(drafts(24)).isEqualTo(3);
    assertThat(
            jdbc.sql("SELECT statement FROM problem_draft WHERE id = :id")
                .param("id", UUID.fromString(draft1))
                .query(String.class)
                .single())
        .isEqualTo(ImportTestFiles.FX10);
    assertThat(resaved.path("items").get(0).path("draftId").asString()).isEqualTo(draft1);
  }

  @Test
  @DisplayName("TC-S2E4-026 Import lần hai cùng tệp không tạo bản trùng")
  void tc026_secondImportNoDuplicates() throws Exception {
    byte[] file = ImportTestFiles.docx(ImportTestFiles.THREE, false);
    JsonNode first = ok(upload(26, "T.docx", file));
    assertThat(save(26, first, 1, 2, 3).path("created").asInt()).isEqualTo(3);
    assertThat(drafts(26)).isEqualTo(3);

    JsonNode again = ok(upload(26, "T.docx", file));
    assertThat(states(again)).containsExactly("exists", "exists", "exists");
    again
        .path("items")
        .forEach(i -> {
          assertThat(i.path("note").asString()).isEqualTo("đã có trong kho");
          assertThat(i.path("checked").asBoolean()).isFalse();
        });
    // "lưu tất cả" kể cả khi gửi đủ ba mục: không mục nào được tạo
    JsonNode saved = save(26, again, 1, 2, 3);
    assertThat(saved.path("created").asInt()).isZero();
    assertThat(saved.path("skipped").asInt()).isEqualTo(3);
    assertThat(drafts(26)).isEqualTo(3);
  }

  @Test
  @DisplayName("TC-S2E4-027 Kho rỗng thì không mục nào bị đánh trùng")
  void tc027_emptyStore() throws Exception {
    assertThat(count("SELECT count(*) FROM library_problem WHERE org_id = '" + ORG + "'")).isZero();
    assertThat(drafts(27)).isZero();
    JsonNode v = ok(upload(27, "T.docx", ImportTestFiles.docx(ImportTestFiles.THREE, false)));
    assertThat(states(v)).containsExactly("new", "new", "new");
    v.path("items")
        .forEach(i -> {
          assertThat(i.path("checked").asBoolean()).isTrue();
          assertThat(i.path("note").isNull() || i.path("note").isMissingNode()).isTrue();
        });
    assertThat(v.toString()).doesNotContain("đã có trong kho");
  }

  @Test
  @DisplayName(
      "S2-E4 import tính vào giới hạn thêm đề theo tài khoản: vượt hạn mức bị chặn, không ghi mục nào")
  void importQuota() throws Exception {
    List<String> paragraphs = new ArrayList<>();
    for (int i = 1; i <= 21; i++) {
      paragraphs.add(
          "Bài "
              + i
              + ". Hình vuông cạnh "
              + (i * 37)
              + " có chu vi bằng bao nhiêu khi nhân "
              + i
              + "?");
    }
    JsonNode v = ok(upload(28, "nhieu.docx", ImportTestFiles.docx(paragraphs, false)));
    assertThat(v.path("items")).hasSize(21);
    int[] all = java.util.stream.IntStream.rangeClosed(1, 21).toArray();
    ArrayNode arr = mapper.createArrayNode();
    for (int i : all) {
      arr.add(i);
    }
    String body = "{\"indexes\":" + arr + "}";
    HttpResponse<String> r =
        postJson(IMPORTS + "/" + v.path("id").asString() + "/drafts", tk(28), body);
    assertThat(r.statusCode()).as(r.body()).isEqualTo(429);
    assertThat(json(r).path("code").asString()).isEqualTo("RATE_LIMITED");
    assertThat(r.headers().firstValue("Retry-After")).isPresent();
    assertThat(drafts(28)).isZero();
    assertThat(
            count(
                "SELECT count(*) FROM problem_create_event WHERE user_id = '" + teacher(28) + "'"))
        .isZero();
    // trong hạn mức thì ghi được, mỗi nháp một lượt kênh import
    JsonNode okSave = save(28, v, java.util.stream.IntStream.rangeClosed(1, 20).toArray());
    assertThat(okSave.path("created").asInt()).isEqualTo(20);
    assertThat(
            count(
                "SELECT count(*) FROM problem_create_event WHERE channel = 'import' AND user_id = '"
                    + teacher(28)
                    + "'"))
        .isEqualTo(20);
    HttpResponse<String> over =
        postJson(IMPORTS + "/" + v.path("id").asString() + "/drafts", tk(28), "{\"indexes\":[21]}");
    assertThat(over.statusCode()).isEqualTo(429);
    assertThat(drafts(28)).isEqualTo(20);
  }
}
