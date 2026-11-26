package vn.geo.server.content.internal.authoring.service;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.content.internal.authoring.repo.AuthoringRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.draft.repo.DraftRepository.JobRow;
import vn.geo.server.content.internal.solution.repo.SolutionRepository;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.DocRow;
import vn.geo.server.content.internal.solution.repo.SolutionRepository.VersionRow;

/**
 * S2-E4: đổi nội dung một NHÁP đề (chat chỉnh nháp, import "cập nhật khi lưu") và giữ sổ lời giải
 * chưa duyệt của nháp đồng bộ. Gọi trong giao dịch của người gọi, sau khi đã khóa hàng nháp.
 *
 * <ul>
 *   <li>Đổi dữ kiện đề: chữ đề của nháp và của sổ đổi theo; mô tả hình được vá khi dữ kiện là độ
 *       dài một cạnh ("BC = 13" → "BC = 14"); dữ kiện không ánh xạ được vào mô tả hình thì bỏ mô tả
 *       hình của sổ (máy không còn đáp số đối chiếu, nhãn máy không thể tự về "Đã kiểm chứng").
 *   <li>Mọi thay đổi đưa nhãn máy của phiên bản mới nhất về "Cần xem lại" (xóa kết quả kiểm) cho
 *       tới khi máy kiểm lại đạt (BR-S2E4-05, PO-48). Trục người không đổi.
 *   <li>Đề đã duyệt (phiên bản "Giáo viên đã duyệt" hoặc đã vào ngăn Của tôi) không chỉnh qua đường
 *       này: CONFLICT.
 * </ul>
 */
@Component
public class DraftEditor {

  public static final String APPROVED = "Đề đã duyệt, hãy tạo nháp mới để chỉnh";

  private static final Pattern LENGTH =
      Pattern.compile("^\\s*([A-Z])([A-Z])\\s*=\\s*(\\d+(?:[.,]\\d+)?)\\s*$");

  private final DraftRepository drafts;
  private final SolutionRepository solutions;
  private final AuthoringRepository repo;
  private final JsonMapper mapper;

  public DraftEditor(
      DraftRepository drafts,
      SolutionRepository solutions,
      AuthoringRepository repo,
      JsonMapper mapper) {
    this.drafts = drafts;
    this.solutions = solutions;
    this.repo = repo;
    this.mapper = mapper;
  }

  static String title(String statement) {
    String s = statement.strip();
    return s.length() > 80 ? s.substring(0, 80) + "…" : s;
  }

  /** Sổ lời giải của nháp nếu còn chỉnh được; null khi nháp chưa có sổ; CONFLICT khi đã duyệt. */
  public DocRow editableDoc(DraftRow d) {
    if (d.docId() == null) {
      return null;
    }
    DocRow doc = solutions.doc(d.docId()).orElse(null);
    if (doc == null) {
      return null;
    }
    VersionRow v = solutions.version(doc.id(), doc.latestVersion()).orElse(null);
    boolean approved =
        v != null
            && ("teacher_approved".equals(v.humanStatus()) || "superseded".equals(v.humanStatus()));
    if (doc.inMyShelf() || approved) {
      throw new AppException(ErrorCode.CONFLICT, APPROVED);
    }
    return doc;
  }

  /** Đã duyệt chưa (không ném lỗi) — import dùng để quyết định có "cập nhật khi lưu" được không. */
  public boolean editable(DraftRow d) {
    if ("building".equals(d.status())) {
      return false;
    }
    try {
      editableDoc(d);
      return true;
    } catch (AppException e) {
      return false;
    }
  }

  /** Ảnh chụp trạng thái hiện tại của nháp + sổ + mô tả hình của tác vụ (để hoàn tác). */
  public String snapshot(DraftRow d) {
    ObjectNode s = mapper.createObjectNode();
    s.put("statement", d.statement());
    DocRow doc = d.docId() == null ? null : solutions.doc(d.docId()).orElse(null);
    if (doc != null) {
      ObjectNode o = s.putObject("doc");
      o.put("id", doc.id().toString());
      o.put("title", doc.title());
      o.put("statement", doc.statement());
      o.put("figure", doc.figure());
      o.put("statedAnswer", doc.statedAnswer());
    }
    JobRow job = drafts.lastJob(d.id()).orElse(null);
    if (job != null) {
      ObjectNode j = s.putObject("job");
      j.put("id", job.id().toString());
      j.put("outcome", job.outcomeJson());
    }
    return mapper.writeValueAsString(s);
  }

  /** Trả nháp về đúng ảnh chụp; nhãn máy về "Cần xem lại" tới khi kiểm lại. */
  public void restore(DraftRow d, String snapshotJson) {
    JsonNode s = mapper.readTree(snapshotJson);
    drafts.updateText(d.id(), s.path("statement").asString(""), d.cellsJson(), d.textConfirmed());
    JsonNode doc = s.path("doc");
    if (doc.isObject()) {
      UUID docId = UUID.fromString(doc.path("id").asString());
      repo.updateDoc(
          docId,
          doc.path("title").asString(),
          doc.path("statement").asString(),
          doc.hasNonNull("figure") ? doc.get("figure").asString() : null);
      repo.setStatedAnswer(
          docId, doc.hasNonNull("statedAnswer") ? doc.get("statedAnswer").asString() : null);
      clearChecks(docId);
    }
    JsonNode job = s.path("job");
    if (job.isObject()) {
      UUID jobId = UUID.fromString(job.path("id").asString());
      drafts
          .job(jobId)
          .ifPresent(j ->
              drafts.updateJob(
                  j.id(),
                  j.state(),
                  j.state(),
                  j.stagesJson(),
                  job.path("outcome").asString("{}")));
    }
  }

  private void clearChecks(UUID docId) {
    solutions
        .doc(docId)
        .flatMap(doc -> solutions.version(doc.id(), doc.latestVersion()))
        .ifPresent(v -> solutions.clearChecks(v.id()));
  }

  /**
   * Đổi chữ đề của nháp. {@code before}/{@code after} (có thể null) là đoạn dữ kiện vừa đổi, dùng
   * để vá mô tả hình.
   */
  public void applyStatement(DraftRow d, String statement, String before, String after) {
    DocRow doc = editableDoc(d);
    drafts.updateText(d.id(), statement, d.cellsJson(), d.textConfirmed());
    if (doc == null) {
      return;
    }
    String figure = doc.figure() == null ? null : patchFigure(doc.figure(), before, after);
    repo.updateDoc(doc.id(), title(statement), statement.strip(), figure);
    clearChecks(doc.id());
    if (figure != null) {
      JobRow job = drafts.lastJob(d.id()).orElse(null);
      if (job != null) {
        ObjectNode outcome = (ObjectNode) mapper.readTree(job.outcomeJson());
        if (outcome.path("figure").isObject()) {
          outcome.set("figure", mapper.readTree(figure));
          drafts.updateJob(
              job.id(),
              job.state(),
              job.state(),
              job.stagesJson(),
              mapper.writeValueAsString(outcome));
        }
      }
    }
  }

  /** Đổi đáp số đề ghi của sổ (đề xuất sửa đáp số, US-S2E4-01-FC2); cần nháp đã dựng. */
  public void applyStatedAnswer(DraftRow d, String answer) {
    DocRow doc = editableDoc(d);
    if (doc == null) {
      throw new AppException(
          ErrorCode.CONFLICT, "Nháp chưa có lời giải, hãy Dựng hình trước khi đổi đáp số");
    }
    repo.setStatedAnswer(doc.id(), answer);
    clearChecks(doc.id());
  }

  /**
   * Vá mô tả hình phẳng khi dữ kiện đổi là độ dài một cạnh: "BC = 13" → "BC = 14" đổi {@code
   * given[type=length, of=[B,C]].value}. Trả null khi không vá được (mô tả hình không còn tin
   * được).
   */
  String patchFigure(String figureJson, String before, String after) {
    if (before == null || after == null) {
      return null;
    }
    Matcher b = LENGTH.matcher(before);
    Matcher a = LENGTH.matcher(after);
    if (!b.matches() || !a.matches()) {
      return null;
    }
    if (!b.group(1).equals(a.group(1)) || !b.group(2).equals(a.group(2))) {
      return null;
    }
    JsonNode root = mapper.readTree(figureJson);
    JsonNode given = root.path("plane").path("given");
    if (!given.isArray()) {
      return null;
    }
    double value = Double.parseDouble(a.group(3).replace(',', '.'));
    boolean patched = false;
    for (JsonNode g : given) {
      JsonNode of = g.path("of");
      if (!"length".equals(g.path("type").asString()) || of.size() != 2) {
        continue;
      }
      String p = of.get(0).asString();
      String q = of.get(1).asString();
      boolean same =
          p.equals(a.group(1)) && q.equals(a.group(2))
              || p.equals(a.group(2)) && q.equals(a.group(1));
      if (same) {
        if (value == Math.rint(value)) {
          ((ObjectNode) g).put("value", (long) value);
        } else {
          ((ObjectNode) g).put("value", value);
        }
        patched = true;
      }
    }
    return patched ? mapper.writeValueAsString(root) : null;
  }
}
