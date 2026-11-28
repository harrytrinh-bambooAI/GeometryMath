package vn.geo.server.content.internal.authoring.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.authoring.repo.AuthoringRepository;
import vn.geo.server.content.internal.authoring.repo.AuthoringRepository.ChatRow;
import vn.geo.server.content.internal.authoring.repo.AuthoringRepository.EditRow;
import vn.geo.server.content.internal.authoring.service.AuthoringViews.ChatView;
import vn.geo.server.content.internal.authoring.service.AuthoringViews.MessageView;
import vn.geo.server.content.internal.authoring.service.AuthoringViews.ProposalView;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.draft.service.DraftService;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftView;
import vn.geo.server.content.internal.solution.repo.SolutionRepository;
import vn.geo.server.content.internal.solution.service.SolutionReviewService;
import vn.geo.server.content.internal.solution.service.SolutionViews;

/**
 * S2-E4 (US-S2E4-01): chat chỉnh nháp đề. Giáo viên nêu định hướng bằng chữ; AI (qua {@link
 * AiGateway}, giả lập trong kiểm thử) hoặc hỏi lại MỘT câu khi định hướng chưa rõ (BR-S2E4-01),
 * hoặc nêu MỘT đề xuất có trước / sau / nguồn / chỗ chưa chắc (BR-S2E4-02). AI không bao giờ tự đổi
 * nháp: chỉ khi giáo viên bấm Nhận (có thể sửa tay trước) thì nháp đổi, nhãn máy về "Cần xem lại"
 * (BR-S2E4-05); Bác thì nháp giữ nguyên. Hoàn tác tối đa 10 bước gần nhất (BR-S2E4-03). Mỗi tin 1…
 * 1000 ký tự, mỗi nháp tối đa 30 lượt (BR-S2E4-06) — lượt 31 bị chặn TRƯỚC khi gọi AI. Nháp không
 * vào giáo trình ở đây (BR-S2E4-04): duyệt vẫn là màn soát lời giải của S2-E5.
 */
@Service
public class DraftChatService {

  private static final Logger log = LoggerFactory.getLogger(DraftChatService.class);

  static final String TASK = "authoring_chat";
  static final String FALLBACK_QUESTION =
      "Tôi chưa rõ ý. Cô muốn chỉnh phần nào của đề: số liệu, điều kiện hay câu hỏi?";
  static final String AI_DOWN = "AI chưa trả lời được, hãy gửi lại sau";

  private final DraftRepository drafts;
  private final DraftService draftService;
  private final AuthoringRepository repo;
  private final DraftEditor editor;
  private final SolutionRepository solutionRepo;
  private final SolutionReviewService solutions;
  private final AiGateway ai;
  private final JsonMapper mapper;
  private final TransactionTemplate tx;

  public DraftChatService(
      DraftRepository drafts,
      DraftService draftService,
      AuthoringRepository repo,
      DraftEditor editor,
      SolutionRepository solutionRepo,
      SolutionReviewService solutions,
      AiGateway ai,
      JsonMapper mapper,
      PlatformTransactionManager tm) {
    this.drafts = drafts;
    this.draftService = draftService;
    this.repo = repo;
    this.editor = editor;
    this.solutionRepo = solutionRepo;
    this.solutions = solutions;
    this.ai = ai;
    this.mapper = mapper;
    this.tx = new TransactionTemplate(tm);
  }

  private DraftRow owned(UUID id, AuthPrincipal me) {
    return drafts
        .draft(id)
        .filter(d -> d.ownerId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Khóa nháp của người gọi trong giao dịch; nháp đang dựng không chỉnh được. */
  private DraftRow lockOwned(UUID id, AuthPrincipal me) {
    owned(id, me);
    repo.lockDraft(id);
    DraftRow d = owned(id, me);
    if ("building".equals(d.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Đề đang dựng, chờ xong rồi chỉnh");
    }
    return d;
  }

  private static int chars(String s) {
    return s.codePointCount(0, s.length());
  }

  // ---------------------------------------------------------------- đọc

  public ChatView view(UUID draftId) {
    Roles.require(Role.TEACHER);
    DraftView dv = draftService.view(draftId);
    List<ChatRow> rows = repo.chat(draftId);
    List<MessageView> messages = new ArrayList<>();
    int turns = 0;
    boolean anyAccepted = false;
    for (ChatRow r : rows) {
      if ("teacher".equals(r.role())) {
        turns++;
      }
      ProposalView p = null;
      if (r.proposalJson() != null) {
        JsonNode j = mapper.readTree(r.proposalJson());
        anyAccepted |= "accepted".equals(r.proposalState());
        p =
            new ProposalView(
                j.path("target").asString("statement"),
                j.path("before").asString(""),
                j.path("after").asString(""),
                j.path("source").asString(AuthoringViews.SOURCE_DRAFT),
                j.hasNonNull("unsure") ? j.get("unsure").asString() : null,
                r.proposalState(),
                r.decidedNote());
      }
      messages.add(
          new MessageView(r.id(), r.seq(), r.role(), r.kind(), r.content(), p, r.createdAt()));
    }
    int undo = repo.editCount(draftId);
    boolean full = turns >= AuthoringViews.MAX_TURNS;
    boolean building = "building".equals(dv.status());
    return new ChatView(
        dv,
        dv.machineLabel(),
        dv.result() != null ? dv.result().humanLabel() : SolutionViews.HUMAN_AI,
        dv.inMyShelf(),
        messages,
        turns,
        AuthoringViews.MAX_TURNS,
        !full && !building,
        full ? AuthoringViews.TURNS_FULL : null,
        undo,
        undo > 0,
        undo == 0 && anyAccepted ? AuthoringViews.NO_UNDO : null);
  }

  // ---------------------------------------------------------------- gửi tin

  /** Gửi một tin; AI trả một câu hỏi lại hoặc một đề xuất. Nháp KHÔNG đổi ở bước này. */
  public ChatView send(UUID draftId, String text) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String t = text == null ? "" : text.strip();
    if (t.isEmpty()) {
      throw AppException.invalidField("text", AuthoringViews.EMPTY);
    }
    if (chars(t) > AuthoringViews.MAX_CHARS) {
      throw AppException.invalidField("text", AuthoringViews.TOO_LONG);
    }
    DraftRow d =
        tx.execute(s -> {
          DraftRow row = lockOwned(draftId, me);
          editor.editableDoc(row);
          if (repo.teacherTurns(draftId) >= AuthoringViews.MAX_TURNS) {
            throw new AppException(ErrorCode.CONFLICT, AuthoringViews.TURNS_FULL);
          }
          repo.insertChat(Uuid7.next(), draftId, me.userId(), "teacher", "message", t, null);
          return row;
        });
    String stated =
        d.docId() == null
            ? null
            : solutionRepo.doc(d.docId()).map(SolutionRepository.DocRow::statedAnswer).orElse(null);
    String prompt =
        "Đề bài: "
            + d.statement().strip()
            + "\nĐáp số đề ghi: "
            + (stated == null ? "(chưa có)" : stated)
            + "\nYêu cầu của giáo viên: "
            + t;
    JsonNode reply;
    try {
      reply = mapper.readTree(ai.complete(new AiRequest(TASK, prompt)).content());
    } catch (RuntimeException e) {
      log.info("chat chỉnh nháp {}: AI lỗi {}", draftId, e.toString());
      repo.insertChat(Uuid7.next(), draftId, me.userId(), "system", "note", AI_DOWN, null);
      return view(draftId);
    }
    ObjectNode proposal = proposal(reply, d.statement(), stated);
    if (proposal != null) {
      String content =
          "Đề xuất: đổi \""
              + proposal.path("before").asString()
              + "\" thành \""
              + proposal.path("after").asString()
              + "\". Chưa có gì được lưu cho tới khi cô bấm Nhận.";
      repo.insertChat(
          Uuid7.next(),
          draftId,
          me.userId(),
          "ai",
          "proposal",
          content,
          mapper.writeValueAsString(proposal));
    } else {
      String q = reply.path("text").asString("").strip();
      if (!"question".equals(reply.path("kind").asString()) || q.isEmpty()) {
        q = FALLBACK_QUESTION;
      }
      repo.insertChat(
          Uuid7.next(),
          draftId,
          me.userId(),
          "ai",
          "question",
          q.length() > 2000 ? q.substring(0, 2000) : q,
          null);
    }
    return view(draftId);
  }

  /**
   * Đề xuất hợp lệ từ câu trả lời của AI, null khi AI hỏi lại hoặc trả đề xuất không dùng được
   * (thiếu trước / sau, "trước" không có trong nháp) — khi đó coi như AI hỏi lại, không tự đoán.
   */
  private ObjectNode proposal(JsonNode reply, String statement, String stated) {
    if (!"proposal".equals(reply.path("kind").asString())) {
      return null;
    }
    boolean answer = "answer".equals(reply.path("target").asString());
    String before = reply.path("before").asString("").strip();
    String after = reply.path("after").asString("").strip();
    if (answer) {
      before = stated == null ? "(chưa ghi)" : stated;
    }
    if (before.isEmpty() || after.isEmpty() || before.equals(after)) {
      return null;
    }
    if (before.length() > 500 || after.length() > (answer ? 100 : 500)) {
      return null;
    }
    if (!answer && !statement.contains(before)) {
      return null;
    }
    ObjectNode p = mapper.createObjectNode();
    p.put("target", answer ? "answer" : "statement");
    p.put("before", before);
    p.put("after", after);
    String source = reply.path("source").asString("").strip();
    p.put("source", source.isEmpty() ? AuthoringViews.SOURCE_DRAFT : source);
    String unsure = reply.path("unsure").asString("").strip();
    if (!unsure.isEmpty()) {
      p.put("unsure", unsure.length() > 300 ? unsure.substring(0, 300) : unsure);
    }
    return p;
  }

  // ---------------------------------------------------------------- nhận / bác / hoàn tác

  private ChatRow pending(UUID draftId, UUID messageId) {
    ChatRow m =
        repo.chatRow(draftId, messageId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!"proposal".equals(m.kind()) || !"pending".equals(m.proposalState())) {
      throw new AppException(ErrorCode.CONFLICT, AuthoringViews.DECIDED);
    }
    return m;
  }

  /**
   * "Nhận" (kèm "Sửa tay": {@code editedAfter} khác đề xuất): nháp đổi, nhãn máy "Cần xem lại", ghi
   * một bước hoàn tác.
   */
  public ChatView accept(UUID draftId, UUID messageId, String editedAfter) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    tx.executeWithoutResult(s -> {
      DraftRow d = lockOwned(draftId, me);
      ChatRow m = pending(draftId, messageId);
      JsonNode p = mapper.readTree(m.proposalJson());
      boolean answer = "answer".equals(p.path("target").asString());
      String before = p.path("before").asString();
      String proposed = p.path("after").asString();
      String after = editedAfter == null || editedAfter.isBlank() ? proposed : editedAfter.strip();
      if (after.length() > (answer ? 100 : 500)) {
        throw AppException.invalidField("after", "Nội dung sửa tay quá dài");
      }
      String snapshot = editor.snapshot(d);
      if (answer) {
        editor.applyStatedAnswer(d, after);
      } else {
        int at = d.statement().indexOf(before);
        if (at < 0) {
          throw new AppException(ErrorCode.CONFLICT, AuthoringViews.STALE);
        }
        String next =
            d.statement().substring(0, at) + after + d.statement().substring(at + before.length());
        if (chars(next.strip()) > DraftService.MAX_CHARS) {
          throw AppException.invalidField("after", "Đề tối đa 2000 ký tự");
        }
        editor.applyStatement(d, next, before, after);
      }
      repo.pushEdit(Uuid7.next(), draftId, m.id(), snapshot, AuthoringViews.MAX_UNDO);
      String note = AuthoringViews.ACCEPTED;
      if (!after.equals(proposed)) {
        String[] diff = diff(proposed, after);
        note = "đã sửa tay từ " + diff[0] + " thành " + diff[1];
      }
      if (repo.decide(m.id(), "accepted", note) != 1) {
        throw new AppException(ErrorCode.CONFLICT, AuthoringViews.DECIDED);
      }
    });
    return view(draftId);
  }

  /** "Bác": nháp không đổi, đề xuất ghi "đã bác". */
  public ChatView reject(UUID draftId, UUID messageId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    tx.executeWithoutResult(s -> {
      lockOwned(draftId, me);
      ChatRow m = pending(draftId, messageId);
      if (repo.decide(m.id(), "rejected", AuthoringViews.REJECTED) != 1) {
        throw new AppException(ErrorCode.CONFLICT, AuthoringViews.DECIDED);
      }
    });
    return view(draftId);
  }

  /** "Hoàn tác": trả nháp về trạng thái trước lần nhận gần nhất; hết bước thì CONFLICT. */
  public ChatView undo(UUID draftId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    tx.executeWithoutResult(s -> {
      DraftRow d = lockOwned(draftId, me);
      EditRow top =
          repo.topEdit(draftId)
              .orElseThrow(() -> new AppException(ErrorCode.CONFLICT, AuthoringViews.NO_UNDO));
      editor.editableDoc(d);
      editor.restore(d, top.snapshotJson());
      repo.deleteEdit(top.id());
      repo.insertChat(
          Uuid7.next(), draftId, me.userId(), "system", "note", AuthoringViews.UNDONE, null);
    });
    return view(draftId);
  }

  /** "Kiểm tra lại": máy tính lại đáp số và kiểm các bước; nhãn máy chỉ do engine gắn (PO-48). */
  public ChatView recheck(UUID draftId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DraftRow d = owned(draftId, me);
    if (d.docId() == null) {
      throw new AppException(ErrorCode.CONFLICT, "Nháp chưa dựng, hãy bấm Dựng hình ở màn Thêm đề");
    }
    solutions.check(d.docId(), null);
    return view(draftId);
  }

  /** Phần khác nhau của hai chuỗi theo từ: "BC = 14" và "BC = 15" → {"14", "15"}. */
  static String[] diff(String a, String b) {
    List<String> x = new ArrayList<>(Arrays.asList(a.strip().split("\\s+")));
    List<String> y = new ArrayList<>(Arrays.asList(b.strip().split("\\s+")));
    while (!x.isEmpty() && !y.isEmpty() && x.get(0).equals(y.get(0))) {
      x.remove(0);
      y.remove(0);
    }
    while (!x.isEmpty() && !y.isEmpty() && x.get(x.size() - 1).equals(y.get(y.size() - 1))) {
      x.remove(x.size() - 1);
      y.remove(y.size() - 1);
    }
    if (x.isEmpty() || y.isEmpty()) {
      return new String[] {a.strip(), b.strip()};
    }
    return new String[] {String.join(" ", x), String.join(" ", y)};
  }
}
