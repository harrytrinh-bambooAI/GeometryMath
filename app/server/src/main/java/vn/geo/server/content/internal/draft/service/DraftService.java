package vn.geo.server.content.internal.draft.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.draft.repo.DraftRepository.JobRow;
import vn.geo.server.content.internal.draft.service.DraftViews.CellView;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftSummary;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftView;

/**
 * S2-E3 (US-S2E3-01, 02, 04): nháp đề của giáo viên — dán chữ, tải ảnh (đọc chữ, ô chưa chắc tô
 * vàng), xác nhận chữ, chạy bốn bước, ba lối đi khi engine không dựng, chặn giao bản nháp chữ. Nháp
 * của người khác: NOT_FOUND (không lộ có tồn tại). Mỗi tài khoản tạo tối đa 20 nháp mỗi giờ
 * (BR-S2E3-05, đã chốt PO-66).
 */
@Service
public class DraftService {

  public static final int MIN_CHARS = 10;
  public static final int MAX_CHARS = 2000;
  public static final int PER_HOUR = 20;

  private final DraftRepository repo;
  private final DraftPipeline pipeline;
  private final DraftRender render;
  private final AiGateway ai;
  private final JsonMapper mapper;

  public DraftService(
      DraftRepository repo,
      DraftPipeline pipeline,
      DraftRender render,
      AiGateway ai,
      JsonMapper mapper) {
    this.repo = repo;
    this.pipeline = pipeline;
    this.render = render;
    this.ai = ai;
    this.mapper = mapper;
  }

  private DraftRow owned(UUID id, AuthPrincipal me) {
    return repo.draft(id)
        .filter(d -> d.ownerId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private JobRow ownedJob(UUID jobId, AuthPrincipal me) {
    return repo.job(jobId)
        .filter(j -> j.ownerId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private static int chars(String s) {
    return s.codePointCount(0, s.length());
  }

  /** Độ dài đề theo BR-S2E3-02 (sau khi cắt khoảng trắng đầu cuối). */
  static void checkLength(String statement, boolean forBuild) {
    String t = statement == null ? "" : statement.strip();
    if (chars(t) > MAX_CHARS) {
      throw AppException.invalidField("statement", "Đề tối đa 2000 ký tự");
    }
    if (forBuild && t.isEmpty()) {
      throw AppException.invalidField("statement", "Hãy dán hoặc gõ đề");
    }
    if (forBuild && chars(t) < MIN_CHARS) {
      throw AppException.invalidField("statement", "Đề quá ngắn (tối thiểu 10 ký tự)");
    }
  }

  // ---------------------------------------------------------------- đọc

  public DraftView view(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return render.view(owned(id, me), null);
  }

  /** Nháp đang soạn gần nhất; null khi không có (BR-S2E3-04). */
  public DraftView current() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.current(me.userId()).map(d -> render.view(d, null)).orElse(null);
  }

  public List<DraftSummary> mine() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.mine(me.userId()).stream().map(render::summary).toList();
  }

  public DraftViews.JobView job(UUID jobId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return render.job(ownedJob(jobId, me));
  }

  // ---------------------------------------------------------------- ghi

  /** Tạo nháp (BR-S2E3-05: quá 20 nháp trong giờ = RATE_LIMITED kèm Retry-After, không tạo). */
  public DraftView create(String source, String statement) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String src = "image".equals(source) ? "image" : "text";
    String text = statement == null ? "" : statement;
    checkLength(text, false);
    if (repo.createdLastHour(me.userId()) >= PER_HOUR) {
      throw AppException.rateLimited(
          Duration.ofSeconds(repo.retryAfterSeconds(me.userId())),
          "Bạn thêm đề hơi nhanh, thử lại sau");
    }
    UUID id = Uuid7.next();
    repo.insert(id, me.userId(), src, text);
    return render.view(repo.draft(id).orElseThrow(), null);
  }

  /**
   * Lưu chữ đang gõ (nháp chữ) hoặc trạng thái ô chữ đọc từ ảnh: {@code cells[i] = {text, state}},
   * state "confirmed" = bấm "đúng"; sửa chữ một ô chưa chắc thì ô thành "fixed".
   */
  public DraftView update(UUID id, String statement, List<Map<String, Object>> cells) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    if ("building".equals(d.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Đề đang dựng, chờ xong hoặc bấm Hủy");
    }
    String cellsJson = d.cellsJson();
    String text = statement == null ? d.statement() : statement;
    if (cells != null) {
      ArrayNode old = (ArrayNode) mapper.readTree(d.cellsJson());
      ArrayNode next = mapper.createArrayNode();
      List<String> parts = new ArrayList<>();
      for (int i = 0; i < old.size(); i++) {
        ObjectNode c = ((ObjectNode) old.get(i)).deepCopy();
        Map<String, Object> in = i < cells.size() ? cells.get(i) : null;
        if (in != null) {
          Object t = in.get("text");
          if (t != null && !t.toString().strip().equals(c.path("text").asString())) {
            c.put("text", t.toString().strip());
            if ("unsure".equals(c.path("state").asString())) {
              c.put("state", "fixed");
            }
          }
          if ("confirmed".equals(in.get("state")) && "unsure".equals(c.path("state").asString())) {
            c.put("state", "confirmed");
          }
        }
        parts.add(c.path("text").asString());
        next.add(c);
      }
      cellsJson = mapper.writeValueAsString(next);
      if (statement == null) {
        text = String.join(" ", parts).strip();
      }
    }
    checkLength(text, false);
    repo.updateText(d.id(), text, cellsJson, false);
    return render.view(repo.draft(id).orElseThrow(), null);
  }

  /**
   * Nhận một đoạn ảnh (thân yêu cầu ≤ 1 MB nên ảnh tới 5 MB gửi nhiều đoạn theo {@code offset}).
   * Đoạn cuối: xóa siêu dữ liệu, đọc chữ bằng AI (giả lập), lưu ô chữ kèm độ tin cậy.
   */
  public Object uploadImage(UUID id, int offset, int total, byte[] chunk) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    if (total > ImageSanitizer.MAX_BYTES) {
      throw new AppException(ErrorCode.PAYLOAD_TOO_LARGE, "Ảnh tối đa 5 MB");
    }
    if (total < 1 || chunk == null || chunk.length == 0 || offset < 0) {
      throw AppException.invalidField("image", "Thiếu dữ liệu ảnh");
    }
    if ((long) offset + chunk.length > total) {
      throw new AppException(ErrorCode.PAYLOAD_TOO_LARGE, "Ảnh tối đa 5 MB");
    }
    if ("building".equals(d.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Đề đang dựng, chờ xong hoặc bấm Hủy");
    }
    String type = offset == 0 ? ImageSanitizer.detect(chunk) : null;
    if (offset == 0 && type == null) {
      throw AppException.invalidField("image", "Chỉ nhận ảnh JPG hoặc PNG");
    }
    if (repo.appendImage(d.id(), offset, total, type, chunk) != 1) {
      throw new AppException(ErrorCode.CONFLICT, "Đoạn ảnh không đúng thứ tự, hãy tải lại ảnh");
    }
    int have = offset + chunk.length;
    if (have < total) {
      return Map.of("received", have, "total", total);
    }
    byte[] img = repo.image(d.id());
    if (img == null || ImageSanitizer.detect(img) == null) {
      repo.replaceImage(d.id(), null);
      throw AppException.invalidField("image", "Chỉ nhận ảnh JPG hoặc PNG");
    }
    byte[] clean = ImageSanitizer.strip(img);
    repo.replaceImage(d.id(), clean);
    String prompt = "Ảnh đề (base64): " + Base64.getEncoder().encodeToString(clean);
    JsonNode r = mapper.readTree(ai.complete(new AiRequest("draft_ocr", prompt)).content());
    ArrayNode cells = mapper.createArrayNode();
    List<String> parts = new ArrayList<>();
    for (JsonNode c : r.path("cells")) {
      String text = c.path("text").asString("").strip();
      if (text.isEmpty()) {
        continue;
      }
      double conf = c.path("confidence").asDouble(0);
      ObjectNode o = mapper.createObjectNode();
      o.put("text", text);
      o.put("confidence", conf);
      o.put("state", DraftViews.cellState(conf));
      cells.add(o);
      parts.add(text);
    }
    String statement = String.join(" ", parts).strip();
    if (chars(statement) > MAX_CHARS) {
      statement = statement.substring(0, MAX_CHARS);
    }
    repo.updateText(d.id(), statement, mapper.writeValueAsString(cells), false);
    return render.view(repo.draft(id).orElseThrow(), cells.isEmpty() ? DraftViews.NO_TEXT : null);
  }

  /**
   * "Xác nhận chữ và dựng tiếp": mọi ô vàng đã sửa hoặc bấm "đúng"; ảnh gốc bị xóa (BR-S2E3-08,
   * 09).
   */
  public DraftView confirmText(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    List<CellView> cells = render.cells(d);
    if (cells.stream().anyMatch(CellView::unsure)) {
      throw new AppException(ErrorCode.CONFLICT, "Còn ô tô vàng chưa sửa hoặc chưa bấm \"đúng\"");
    }
    checkLength(d.statement(), true);
    repo.updateText(d.id(), d.statement(), d.cellsJson(), true);
    repo.replaceImage(d.id(), null);
    return render.view(repo.draft(id).orElseThrow(), null);
  }

  /** "Dựng hình": chạy bốn bước; bấm hai lần chỉ một tác vụ (BR-S2E3-13). */
  public DraftView build(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    if ("building".equals(d.status())) {
      return render.view(d, null);
    }
    checkLength(d.statement(), true);
    if ("image".equals(d.source()) && !d.textConfirmed()) {
      throw new AppException(ErrorCode.CONFLICT, "Xác nhận chữ trước khi dựng hình");
    }
    pipeline.start(d, me);
    return render.view(repo.draft(id).orElseThrow(), null);
  }

  /** "Bổ sung điều kiện" (BR-S2E3-15): thêm điều kiện vào cuối đề rồi dựng lại. */
  public DraftView supplement(UUID id, String condition) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    String c = condition == null ? "" : condition.strip();
    if (c.isEmpty()) {
      throw AppException.invalidField("condition", "Hãy nhập điều kiện bổ sung");
    }
    String text = d.statement().strip() + " " + c;
    checkLength(text, true);
    repo.updateText(d.id(), text, d.cellsJson(), d.textConfirmed());
    repo.setStatus(d.id(), "editing");
    return build(id);
  }

  /** "Lưu bản nháp chữ": chưa có hình, chưa giao được (BR-S2E3-15). */
  public DraftView saveTextOnly(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    if ("building".equals(d.status()) || "ready".equals(d.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Đề không ở trạng thái lưu nháp chữ được");
    }
    repo.setStatus(d.id(), "text_only");
    return render.view(repo.draft(id).orElseThrow(), null);
  }

  public DraftViews.JobView retry(UUID jobId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return render.job(pipeline.retry(ownedJob(jobId, me), me));
  }

  public DraftViews.JobView cancel(UUID jobId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return render.job(pipeline.cancel(ownedJob(jobId, me)));
  }

  /**
   * Chọn đề để giao (US-S2E3-04-SC2, SC3): bản nháp chữ không giao được; đề "Cần xem lại · hình do
   * AI dựng" phải gửi {@code confirmAiFigure}; thiếu thì CONFLICT với câu hỏi xác nhận.
   */
  public DraftView assign(UUID id, boolean confirmAiFigure) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(id, me);
    switch (d.status()) {
      case "text_only" -> throw new AppException(ErrorCode.CONFLICT, "Bản nháp chữ chưa giao được");
      case "editing", "building" -> throw new AppException(ErrorCode.CONFLICT, "Đề chưa dựng xong");
      case "ai_figure" -> {
        if (!confirmAiFigure) {
          throw new AppException(ErrorCode.CONFLICT, DraftViews.ASK_AI_FIGURE);
        }
      }
      default -> {
        // ready: giao được
      }
    }
    repo.confirmAssign(d.id());
    return render.view(repo.draft(id).orElseThrow(), null);
  }
}
