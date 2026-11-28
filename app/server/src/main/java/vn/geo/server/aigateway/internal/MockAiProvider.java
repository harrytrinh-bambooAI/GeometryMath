package vn.geo.server.aigateway.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Nhà cung cấp GIẢ LẬP (D-GEO-047): trả nội dung soạn sẵn, không gọi AI thật.
 *
 * <ul>
 *   <li>task "solution" (bộ đề chuẩn): đáp số + tọa độ soạn sẵn theo mã bài ở {@code
 *       mock-ai/benchmark.json}, có thể ghi đè từng đề (F-GEO-070) qua {@link MockAiControl}.
 *   <li>task "scene" (E2, tự học): mô tả dựng theo mẫu chữ của đề ở {@code mock-ai/scene.json}, ghi
 *       đè theo kịch bản của móc kiểm thử (F-GEO-111, F-GEO-112).
 *   <li>task khác (hỏi đáp…): câu trả lời chung, không chứa đáp số.
 * </ul>
 *
 * Chế độ lỗi/quá thời gian do móc kiểm thử đặt (US-GEO-103-FC1, FC2).
 */
@Component
@ConditionalOnProperty(name = "geo.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {

  static final String QA_REPLY =
      "[Trợ lý giả lập] Em hãy đọc lại dữ kiện của đề và thử tìm mối liên hệ giữa các yếu tố đã cho"
          + " nhé.";
  private static final Pattern CODE = Pattern.compile("Mã bài: ([A-Z]{2,5}-[0-9]{2,4})");

  private final AiProperties props;
  private final MockAiControl control;
  private final JsonMapper mapper = JsonMapper.builder().build();
  private final Map<String, JsonNode> canned;
  private final JsonNode sceneRules;
  private final JsonNode e4;
  private final MockDraftReplies drafts;
  private final MockAuthoringReplies authoring;

  public MockAiProvider(AiProperties props, MockAiControl control) {
    this.props = props;
    this.control = control;
    this.canned = loadCanned();
    this.sceneRules = loadScene();
    this.e4 = load("/mock-ai/e4.json");
    this.drafts = new MockDraftReplies(mapper);
    this.authoring = new MockAuthoringReplies(mapper);
  }

  @Override
  public String name() {
    return "mock";
  }

  @Override
  public String complete(ProviderCall call) throws Exception {
    // S2-E3: ảnh đề tới 5 MB (base64 ~7 MB) không giữ nguyên trong sổ prompt của móc kiểm thử
    control.received(
        MockDraftReplies.handles(call.task()) && call.prompt().length() > 4_000
            ? call.prompt().substring(0, 4_000)
            : call.prompt());
    if (!props.mockLatency().isZero()) {
      Thread.sleep(props.mockLatency());
    }
    if (call.delayMs() > 0) {
      Thread.sleep(call.delayMs());
    }
    switch (call.simulation()) {
      case ERROR -> throw new IllegalStateException("nhà cung cấp giả lập: chế độ lỗi");
      case TIMEOUT -> {
        Thread.sleep(600_000);
        return "";
      }
      default -> {
        // tiếp tục
      }
    }
    if (MockAuthoringReplies.handles(call.task())) {
      return authoring.reply(call.prompt()); // S2-E4: chat chỉnh nháp đề
    }
    if (MockDraftReplies.handles(call.task())) {
      return drafts.reply(call.task(), call.prompt(), call.scenario()); // S2-E3 (ai/s2e3-mock.json)
    }
    if ("scene".equals(call.task())) {
      return mapper.writeValueAsString(scene(call.prompt(), call.scenario()));
    }
    if ("solution".equals(call.task()) && !CODE.matcher(call.prompt()).find()) {
      return mapper.writeValueAsString(selfSolution(statementOf(call.prompt())));
    }
    if ("hint".equals(call.task()) && call.prompt().startsWith("Đề bài:")) {
      return mapper.writeValueAsString(selfHints(statementOf(call.prompt())));
    }
    if ("ocr".equals(call.task())) {
      return mapper.writeValueAsString(ocr(call.prompt()));
    }
    if ("qa".equals(call.task())) {
      String scripted = control.nextQa(call.scenario());
      if (scripted != null) {
        return scripted; // E6: câu soạn sẵn của ca kiểm (văn bản thuần hoặc JSON {kind, reply,
        // source})
      }
      return mapper.writeValueAsString(qa(call.prompt()));
    }
    if ("solution".equals(call.task())) {
      Matcher m = CODE.matcher(call.prompt());
      String code = m.find() ? m.group(1) : "";
      JsonNode base = canned.get(code);
      ObjectNode out = mapper.createObjectNode();
      String override = control.answerOverride(code);
      out.put(
          "answer",
          override != null ? override : base == null ? "" : base.get("answer").asString());
      if (base != null && base.has("points")) {
        out.set("points", base.get("points"));
      }
      return mapper.writeValueAsString(out);
    }
    // Chặng 5 (T-261128-51, PLAN_code-s5 P4): đáp mẫu task AI mới theo F-S5-090…095; mỗi task thêm
    // nhánh "if (\"<task>\".equals(call.task())) {…}" NGAY DƯỚI mốc của mình.
    // --- T-261128-51 (khung) ---
    // --- T-261128-52 (S5-E2) ---
    // --- T-261128-53 (S5-E1) ---
    if ("lesson_plan".equals(call.task())) {
      // F-S5-093 (giáo án hợp lệ F-S5-021 dựng từ chính tin nhắn: số tiết, kiến thức, kho) và biến
      // thể theo kịch bản của móc kiểm thử (scenario, hoặc qaReplies = danh sách theo lượt gọi):
      // F-S5-093b Σ 50' (phạm A5) · keys (khoá sai dấu) · badkt (mã ngoài danh sách + dap_so do AI)
      // · unsure (HĐ4 "unsure" + 1 de_xuat) · question · broken (JSON hỏng) · refusal · "{…}"
      // nguyên
      // văn.
      String scripted = control.nextQa(call.scenario());
      String variant =
          scripted != null ? scripted : call.scenario() == null ? "F-S5-093" : call.scenario();
      if (variant.startsWith("{")) {
        return variant;
      }
      switch (variant) {
        case "refusal" -> throw new IllegalStateException("AI từ chối yêu cầu (refusal)");
        case "broken" -> {
          return "{\"kind\":\"lesson\",\"meta\":{\"bai\":\"Bài 26";
        }
        case "question" -> {
          return "{\"kind\":\"question\",\"text\":\"Thầy cô muốn soạn 1 hay 2 tiết?\","
              + "\"field\":\"soTiet\"}";
        }
        default -> {
          // dựng tiếp bên dưới
        }
      }
      String p = call.prompt();
      Matcher mn = Pattern.compile("Số tiết: (\\d+)").matcher(p);
      int n = mn.find() ? Integer.parseInt(mn.group(1)) : 1;
      Matcher mb = Pattern.compile("Bài: (.+?) \\(Toán 11").matcher(p);
      Matcher mt = Pattern.compile("Tiết PPCT: (.+)").matcher(p);
      Matcher mf = Pattern.compile("Kiến thức trọng tâm: (.+)").matcher(p);
      java.util.List<String> focus =
          mf.find() ? java.util.List.of(mf.group(1).trim().split(",\\s*")) : java.util.List.of();
      Map<String, String> stmts = new java.util.LinkedHashMap<>();
      Matcher mk = Pattern.compile("(?m)^- (KT11-[A-Z0-9-]+): (.+)$").matcher(p);
      while (mk.find()) {
        stmts.put(mk.group(1), mk.group(2));
      }
      java.util.List<String[]> kho = new java.util.ArrayList<>();
      Matcher mx = Pattern.compile("(?m)^- ([A-Z0-9-]+) \\[(NB|TH|VD|VDC)\\] (.+)$").matcher(p);
      while (mx.find()) {
        kho.add(new String[] {mx.group(1), mx.group(2), mx.group(3)});
      }
      ObjectNode out = mapper.createObjectNode().put("kind", "lesson");
      ObjectNode meta = out.putObject("meta");
      meta.put("bai", mb.find() ? mb.group(1) : "");
      meta.put("tiet", mt.find() ? mt.group(1).trim() : "");
      meta.put("so_tiet", n).put("tong_phut", n * 45);
      ObjectNode mtieu = out.putObject("muc_tieu");
      ObjectNode kt0 = mtieu.putArray("kien_thuc").addObject();
      kt0.put("noi_dung", "Xác định được khoảng cách theo định nghĩa và tính được bằng hệ thức.");
      tools.jackson.databind.node.ArrayNode ktCodes = kt0.putArray("kt");
      focus.forEach(ktCodes::add);
      if ("badkt".equals(variant)) {
        ktCodes.add("KT11-C08-DN-099");
      }
      mtieu.putArray("keys".equals(variant) ? " nang_luc" : "nang_luc").add("Tư duy và lập luận.");
      mtieu.putArray("pham_chat").add("Chăm chỉ, trách nhiệm.");
      ObjectNode tb = out.putObject("thiet_bi");
      tb.putArray("giao_vien").add("Máy chiếu").add("Mô hình hình chóp");
      tb.putArray("hoc_sinh").add("Thước, bút chì");
      String[][] hd = {
        {"mo-dau", "Khởi động: Khoảng cách từ điểm"},
        {"hinh-thanh", "Hình thành: khoảng cách từ điểm đến mặt phẳng"},
        {"luyen-tap", "Luyện tập: PHT1"},
        {"van-dung", "Vận dụng: đo khoảng cách trong lớp học"}
      };
      int[] mins = {5 * n, 20 * n, 15 * n, ("F-S5-093b".equals(variant) ? 10 : 5) * n};
      tools.jackson.databind.node.ArrayNode acts = out.putArray("hoat_dong");
      for (int i = 0; i < 4; i++) {
        ObjectNode a = acts.addObject();
        a.put("ma", "HD" + (i + 1)).put("loai", hd[i][0]).put("ten", hd[i][1]);
        a.put("thoi_luong_phut", mins[i]).put("o_nha", false);
        a.put("muc_tieu", "HS đạt mục tiêu của " + hd[i][1] + ".");
        a.put("noi_dung", "Nội dung " + hd[i][1] + ".");
        a.put("san_pham", "Câu trả lời và lập luận của HS.");
        ObjectNode tc = a.putObject("to_chuc");
        tc.put("assign", "GV giao nhiệm vụ.").put("perform", "HS thực hiện cá nhân.");
        tc.put("report", "HS báo cáo.");
        tc.put(i == 0 && "keys".equals(variant) ? "ket_luận" : "conclude", "GV chốt.");
        tools.jackson.databind.node.ArrayNode akt = a.putArray("kien_thuc");
        if (i == 1) {
          for (String c : focus) {
            akt.addObject().put("kt", c).put("phat_bieu", stmts.getOrDefault(c, ""));
          }
        }
      }
      tools.jackson.databind.node.ArrayNode pht = out.putArray("phieu_hoc_tap");
      if (p.contains("Phiếu học tập: có")) {
        tools.jackson.databind.node.ArrayNode cau =
            pht.addObject().put("ma", "PHT1").putArray("cau");
        for (int i = 0; i < Math.min(3, kho.size()); i++) {
          ObjectNode c = cau.addObject().put("so", i + 1).put("nguon", kho.get(i)[0]);
          c.put("muc_do", kho.get(i)[1]).put("de", kho.get(i)[2]);
          c.putArray("kt").add(focus.isEmpty() ? "" : focus.get(0));
          if (i == 1 && "badkt".equals(variant)) {
            c.put("dap_so", "a√6/3");
          } else {
            c.putNull("dap_so");
          }
          c.putArray("buoc_goi_y").add("Dựng hình chiếu vuông góc rồi áp dụng hệ thức.");
        }
      }
      tools.jackson.databind.node.ArrayNode unsure = out.putArray("unsure");
      tools.jackson.databind.node.ArrayNode deXuat = out.putArray("de_xuat");
      if ("unsure".equals(variant)) {
        unsure.add("HĐ4: chưa chắc thời lượng vận dụng đủ cho cả lớp.");
        deXuat
            .addObject()
            .put("vi_tri", "HĐ4")
            .put("van_de", "Tổng 50' > 45'")
            .put("goi_y", "Giảm HĐ4 còn 5 phút.")
            .put("ly_do", "Tổng thời lượng trên lớp phải bằng số tiết × 45.");
      }
      return mapper.writeValueAsString(out);
    }
    // --- T-261128-54 (S5-E1, S5-E2 xuất) ---
    if ("lesson_patch".equals(call.task())) {
      // F-S5-096 (chat vá, US-S5E1-04): bản vá 7 trường của HĐ đang chọn, "before" lấy NGUYÊN VĂN
      // từ
      // <giao_an_hien_tai>. Biến thể theo kịch bản (scenario / qaReplies theo lượt): patch-2 (2
      // dòng)
      // · patch-locked (sửa muc_tieu) · patch-badbefore (before lệch) · patch-question ·
      // patch-reply
      // · "{…}" nguyên văn.
      String scripted = control.nextQa(call.scenario());
      String variant =
          scripted != null ? scripted : call.scenario() == null ? "F-S5-096" : call.scenario();
      if (variant.startsWith("{")) {
        return variant;
      }
      if ("patch-question".equals(variant)) {
        return "{\"kind\":\"question\",\"text\":\"Thầy cô muốn hoạt động này hay hơn theo hướng"
            + " nào: thêm trò chơi hay thêm ví dụ thực tế?\"}";
      }
      if ("patch-reply".equals(variant)) {
        return "{\"kind\":\"reply\",\"text\":\"Đáp số do kho và engine điền, AI không tự tính đáp"
            + " số (luật A3). Thầy cô có thể chọn câu khác từ kho bài tập.\",\"de_xuat\":[]}";
      }
      String p = call.prompt();
      int a = p.indexOf("<giao_an_hien_tai>\n");
      int b = p.indexOf("\n</giao_an_hien_tai>");
      JsonNode cur =
          a >= 0 && b > a
              ? mapper.readTree(p.substring(a + "<giao_an_hien_tai>\n".length(), b))
              : mapper.createObjectNode();
      Matcher ms = Pattern.compile("<muc_dang_chon>\n(\\S+)").matcher(p);
      String sel = ms.find() ? ms.group(1) : "HD1";
      int idx = 0;
      for (int i = 0; i < cur.path("hoat_dong").size(); i++) {
        if (sel.equals(cur.path("hoat_dong").get(i).path("ma").asString(""))) {
          idx = i;
        }
      }
      JsonNode hd = cur.path("hoat_dong").path(idx);
      String[][] fields = {
        {"ten", "Trò chơi: Ai nhanh hơn"},
        {"muc_tieu", "HS nhớ lại hình chiếu vuông góc qua trò chơi thi đua."},
        {"noi_dung", "Trò chơi Ai nhanh hơn: 4 câu hỏi nhanh về hình chiếu vuông góc."},
        {"san_pham", "Đáp án của các đội trên bảng con."},
        {"to_chuc.chuyen_giao", "GV chia lớp 4 đội, phổ biến luật chơi."},
        {"to_chuc.thuc_hien", "Các đội giơ bảng con trả lời trong 30 giây."},
        {"to_chuc.bao_cao", "Đội nhanh nhất trình bày cách làm."}
      };
      ObjectNode out = mapper.createObjectNode().put("kind", "patch");
      tools.jackson.databind.node.ArrayNode patches = out.putArray("patches");
      if ("patch-locked".equals(variant)) {
        patches
            .addObject()
            .put("op", "replace")
            .put("path", "muc_tieu.nang_luc[0]")
            .put("before", cur.path("muc_tieu").path("nang_luc").path(0).asString(""))
            .put("after", "Tư duy và lập luận; năng lực mô hình hoá toán học.")
            .put("ly_do", "Thêm năng lực mô hình hoá theo yêu cầu.");
      } else {
        int n = "patch-2".equals(variant) ? 2 : "patch-badbefore".equals(variant) ? 1 : 7;
        for (int i = 0; i < n; i++) {
          String path = fields[i][0];
          JsonNode before = hd;
          for (String k : path.split("\\.")) {
            before = before.path(k);
          }
          String bv = before.asString("");
          if ("patch-badbefore".equals(variant)) {
            bv = bv.replace(" từ điểm", "");
          }
          patches
              .addObject()
              .put("op", "replace")
              .put("path", "hoat_dong[" + idx + "]." + path)
              .put("before", bv)
              .put("after", fields[i][1])
              .put("ly_do", "Đổi hoạt động thành trò chơi theo yêu cầu, giữ nguyên thời lượng.");
        }
      }
      out.putArray("de_xuat");
      out.putArray("unsure");
      return mapper.writeValueAsString(out);
    }
    // --- T-261128-55 (S5-E5) ---
    if ("class_notice".equals(call.task())) {
      // Nháp thông báo 3 câu dựng từ dữ kiện trong tin nhắn (US-S5E5-01-SC1); biến thể theo kịch
      // bản
      // của móc kiểm thử: "s5e5-name" (AI chèn tên học sinh, FC4), "s5e5-wrong-time" (sai giờ hạn,
      // FC1), "{…}" nguyên văn; qaReplies = danh sách theo lượt gọi.
      String scripted = control.nextQa(call.scenario());
      String variant = scripted != null ? scripted : call.scenario() == null ? "" : call.scenario();
      if (variant.startsWith("{")) {
        return variant;
      }
      Matcher mt = Pattern.compile("Tên bài: (.+)").matcher(call.prompt());
      Matcher md =
          Pattern.compile("Hạn nộp: (\\d{2}:\\d{2}, \\d{2}/\\d{2}/\\d{4})").matcher(call.prompt());
      String title = mt.find() ? mt.group(1).trim() : "bài tập";
      String due = md.find() ? md.group(1) : "";
      ObjectNode out = mapper.createObjectNode();
      tools.jackson.databind.node.ArrayNode s = out.putArray("sentences");
      s.add("Chào các em, cô nhắc cả lớp bài \"" + title + "\".");
      switch (variant) {
        case "s5e5-name" -> s.add("Nhắc Chi Mai và cả lớp nộp bài trước 20:00.");
        case "s5e5-wrong-time" -> s.add("Hạn nộp 21:00 thứ Sáu, các em chú ý.");
        default -> s.add(due.isEmpty() ? "Các em chú ý thời gian nhé." : "Hạn nộp là " + due + ".");
      }
      s.add("Em nào cần hỏi thêm thì nhắn cô trong giờ học nhé.");
      return mapper.writeValueAsString(out);
    }
    if (("grade".equals(call.task()) || "grade_ocr".equals(call.task()))
        && call.prompt().startsWith("[s5e5-harness]")) {
      // Bộ chạy chấm nền giả của móc kiểm thử S5-E5 (hạn mức, gọi lại): qaReplies "error" = lỗi
      // lượt đó. Đáp F-S5-090 (BN-01: 10 điểm, tin cậy 0,86) / F-S5-095 (bản đọc chữ).
      String scripted = control.nextQa(call.scenario());
      if ("error".equals(scripted)) {
        throw new IllegalStateException("giả lập lỗi tạm thời");
      }
      return "grade".equals(call.task())
          ? "{\"total\":10,\"confidence\":0.86,\"criteria\":[{\"name\":\"Hướng\",\"score\":2},"
              + "{\"name\":\"Các bước\",\"score\":4},{\"name\":\"Kết quả\",\"score\":2},"
              + "{\"name\":\"Hình\",\"score\":1},{\"name\":\"Trình bày\",\"score\":1}]}"
          : "{\"text\":\"Kẻ AH ⊥ SB ... AH = a√6/3\",\"unsure\":[]}";
    }
    // --- T-261128-56 (S5-E3) ---
    // --- T-261128-57 (S5-E4) ---
    if ("grade_ocr".equals(call.task()) && call.prompt().startsWith("[s5e4-ocr]")) {
      // F-S5-095: bản đọc chữ BN-01 với 2 đoạn "chưa chắc" (tin cậy < 0,6). Giả lập GHI LẠI yêu cầu
      // đã nhận (khóa "request") để E2E soát ảnh gửi AI không còn EXIF/GPS và không có danh tính
      // (TC-S5E4-45, BR-S5E4-29).
      ObjectNode out = mapper.createObjectNode();
      out.put(
          "text",
          "Kẻ AH ⊥ SB tại H. Vì BC ⊥ (SAB) nên AH ⊥ (SBC). 1/AH² = 1/SA² + 1/AB². AH = a√6/3");
      out.putArray("unsure")
          .add(mapper.createObjectNode().put("text", "1/SA²").put("confidence", 0.52))
          .add(mapper.createObjectNode().put("text", "a√6/3").put("confidence", 0.48));
      out.put("request", call.prompt());
      return mapper.writeValueAsString(out);
    }
    // --- T-261128-58 (S5-E3) ---
    // --- T-261128-59 (S5-E4) ---
    if ("grade_ocr".equals(call.task()) && call.prompt().startsWith("[s5e4b-ocr]")) {
      // F-S5-095: bản đọc chữ BN-01 theo trang (luồng nộp thật US-S5E4-04): mỗi trang 1 đoạn "chưa
      // chắc" (tin cậy < 0,6) kèm vùng ảnh → bài 2 trang có 2 đoạn tô. Trang 2 đọc nhầm "a√6/2" để
      // giáo viên sửa thành "a√6/3" (US-S5E4-04-SC2).
      boolean p2 = call.prompt().startsWith("[s5e4b-ocr] trang 2.");
      ObjectNode out = mapper.createObjectNode();
      tools.jackson.databind.node.ArrayNode segs = out.putArray("segments");
      ObjectNode r1 =
          mapper.createObjectNode().put("x", 40).put("y", 60).put("w", 520).put("h", 48);
      ObjectNode r2 =
          mapper.createObjectNode().put("x", 40).put("y", 140).put("w", 300).put("h", 52);
      if (p2) {
        segs.addObject()
            .put("text", "1/AH² = 1/SA² + 1/AB²")
            .put("confidence", 0.91)
            .set("region", r1);
        segs.addObject().put("text", "AH = a√6/2").put("confidence", 0.48).set("region", r2);
      } else {
        segs.addObject()
            .put("text", "Kẻ AH ⊥ SB tại H. Vì BC ⊥ (SAB) nên AH ⊥ (SBC).")
            .put("confidence", 0.88)
            .set("region", r1);
        segs.addObject().put("text", "SB = a√3").put("confidence", 0.52).set("region", r2);
      }
      return mapper.writeValueAsString(out);
    }
    // --- T-261128-60 (S5-E4) ---
    // --- T-261128-61 (S5-E6) ---
    // --- T-261128-62 (S5-E6) ---
    // --- T-261128-63 (S5-E2, S5-E3) ---
    // --- T-261128-64 (S5-E5) ---
    // --- T-261128-65 (S5-E6) ---
    // --- FIX chặng 5 (task FIX T-261128-*) ---
    return QA_REPLY;
  }

  private static final Pattern STATEMENT = Pattern.compile("Đề bài: (.*)", Pattern.DOTALL);

  /** Kết quả "scene" giả lập: quy tắc đầu tiên khớp đề, thay $n, rồi ghi đè theo kịch bản. */
  ObjectNode scene(String prompt, String scenario) {
    Matcher sm = STATEMENT.matcher(prompt);
    String statement = sm.find() ? sm.group(1).strip() : prompt;
    ObjectNode out = null;
    for (JsonNode rule : sceneRules.path("rules")) {
      Matcher m = Pattern.compile(rule.path("pattern").asString()).matcher(statement);
      if (m.find()) {
        out = (ObjectNode) substitute(rule.get("response").deepCopy(), m);
        break;
      }
    }
    if (out == null) {
      out = (ObjectNode) sceneRules.path("fallback").deepCopy();
    }
    JsonNode over = scenario == null ? null : sceneRules.path("scenarios").get(scenario);
    if (over != null) {
      for (var e : over.properties()) {
        out.set(e.getKey(), e.getValue().deepCopy());
      }
    }
    return out;
  }

  private JsonNode substitute(JsonNode n, Matcher m) {
    if (n.isString()) {
      String s = n.asString();
      for (int g = m.groupCount(); g >= 1; g--) {
        String v = m.group(g) == null ? "" : m.group(g).replace(',', '.');
        s = s.replace("$" + g, v);
      }
      return mapper.getNodeFactory().stringNode(s);
    }
    if (n.isObject()) {
      ObjectNode o = (ObjectNode) n;
      for (var e : java.util.List.copyOf(o.properties())) {
        o.set(e.getKey(), substitute(e.getValue(), m));
      }
      return o;
    }
    if (n.isArray()) {
      tools.jackson.databind.node.ArrayNode a = (tools.jackson.databind.node.ArrayNode) n;
      for (int i = 0; i < a.size(); i++) {
        a.set(i, substitute(a.get(i), m));
      }
      return a;
    }
    return n;
  }

  // ---------------------------------------------------------------- E4 (mock-ai/e4.json)

  private static String statementOf(String prompt) {
    Matcher sm = STATEMENT.matcher(prompt);
    return sm.find() ? sm.group(1).strip() : prompt;
  }

  private JsonNode firstMatch(JsonNode rules, String text) {
    for (JsonNode r : rules) {
      if (Pattern.compile(r.path("pattern").asString()).matcher(text).find()) {
        return r;
      }
    }
    return null;
  }

  /** Lời giải của đề tự nhập: {answer, steps[]}. */
  ObjectNode selfSolution(String statement) {
    JsonNode r = firstMatch(e4.path("solution"), statement);
    JsonNode src = r != null ? r : e4.path("solutionFallback");
    ObjectNode out = mapper.createObjectNode();
    out.put("answer", src.path("answer").asString(""));
    out.set("steps", src.path("steps").deepCopy());
    return out;
  }

  /** Gợi ý 1, 2 của đề tự nhập: {hints: [h1, h2]}. */
  ObjectNode selfHints(String statement) {
    JsonNode r = firstMatch(e4.path("hint"), statement);
    ObjectNode out = mapper.createObjectNode();
    out.set("hints", (r != null ? r.path("hints") : e4.path("hintFallback")).deepCopy());
    return out;
  }

  /** Câu trả lời hỏi đáp: {kind, reply, source?}. Câu không khớp: lời gợi mở chung, không nguồn. */
  ObjectNode qa(String prompt) {
    JsonNode r = firstMatch(e4.path("qa"), prompt);
    ObjectNode out = mapper.createObjectNode();
    if (r == null) {
      out.put("kind", "hint");
      out.put("reply", QA_REPLY);
      return out;
    }
    out.put("kind", r.path("kind").asString("hint"));
    out.put("reply", r.path("reply").asString(""));
    if (r.hasNonNull("source")) {
      out.put("source", r.get("source").asString());
    }
    return out;
  }

  private static final Pattern IMAGE = Pattern.compile("Ảnh đề \\(base64\\): ([A-Za-z0-9+/=]+)");

  /**
   * Chữ đọc từ ảnh: ảnh dữ liệu vàng mang dấu "GEO-FIXTURE:F-GEO-nnn"; ảnh khác = chưa đọc được.
   */
  ObjectNode ocr(String prompt) {
    ObjectNode out = mapper.createObjectNode();
    Matcher m = IMAGE.matcher(prompt);
    String text = "";
    if (m.find()) {
      byte[] bytes;
      try {
        bytes = java.util.Base64.getDecoder().decode(m.group(1));
      } catch (IllegalArgumentException e) {
        bytes = new byte[0];
      }
      String raw = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
      Matcher f = Pattern.compile("GEO-FIXTURE:(F-GEO-[0-9]{3})").matcher(raw);
      if (f.find()) {
        text = e4.path("ocr").path(f.group(1)).asString("");
      }
    }
    out.put("text", text);
    return out;
  }

  private JsonNode load(String path) {
    try (InputStream in = MockAiProvider.class.getResourceAsStream(path)) {
      return in == null ? mapper.createObjectNode() : mapper.readTree(in);
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được " + path, e);
    }
  }

  private JsonNode loadScene() {
    try (InputStream in = MockAiProvider.class.getResourceAsStream("/mock-ai/scene.json")) {
      return in == null ? mapper.createObjectNode() : mapper.readTree(in);
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được mock-ai/scene.json", e);
    }
  }

  private Map<String, JsonNode> loadCanned() {
    try (InputStream in = MockAiProvider.class.getResourceAsStream("/mock-ai/benchmark.json")) {
      if (in == null) {
        return Map.of();
      }
      JsonNode root = mapper.readTree(in);
      java.util.HashMap<String, JsonNode> m = new java.util.HashMap<>();
      for (var e : root.properties()) {
        m.put(e.getKey(), e.getValue());
      }
      return Map.copyOf(m);
    } catch (IOException e) {
      throw new IllegalStateException("không đọc được mock-ai/benchmark.json", e);
    }
  }
}
