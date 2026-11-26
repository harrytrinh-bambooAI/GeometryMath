package vn.geo.server.content.internal.importer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.api.LibraryQuota;
import vn.geo.server.content.internal.authoring.repo.AuthoringRepository;
import vn.geo.server.content.internal.authoring.service.DraftEditor;
import vn.geo.server.content.internal.dedup.service.Dedup;
import vn.geo.server.content.internal.dedup.service.DedupMatcher;
import vn.geo.server.content.internal.dedup.service.DedupMatcher.Candidate;
import vn.geo.server.content.internal.dedup.service.DedupMatcher.Hit;
import vn.geo.server.content.internal.draft.repo.DraftRepository;
import vn.geo.server.content.internal.draft.repo.DraftRepository.DraftRow;
import vn.geo.server.content.internal.draft.service.DraftService;
import vn.geo.server.content.internal.importer.repo.ImportRepository;
import vn.geo.server.content.internal.importer.repo.ImportRepository.ItemRow;
import vn.geo.server.content.internal.importer.repo.ImportRepository.JobRow;
import vn.geo.server.content.internal.importer.service.ExerciseSplitter.Exercise;
import vn.geo.server.content.internal.importer.service.ExerciseSplitter.Split;
import vn.geo.server.content.internal.importer.service.ImportViews.ImportView;
import vn.geo.server.content.internal.importer.service.ImportViews.ItemView;

/**
 * S2-E4 (US-S2E4-02, 03): import tệp docx / pdf, tách từng bài tập, đối chiếu trùng với kho.
 *
 * <ol>
 *   <li>Tạo lượt import (tên + dung lượng): chỉ docx / pdf, tối đa 10 MB (BR-S2E4-07).
 *   <li>Gửi tệp theo đoạn (thân yêu cầu ≤ 1 MB). Đoạn cuối: đọc chữ ({@link FileText}), tách bài
 *       ({@link ExerciseSplitter}), XÓA tệp tạm. Tệp bị từ chối (ảnh quét, hỏng, mật khẩu, sai
 *       loại) = VALIDATION_FAILED kèm đúng lý do, không mục nào được tạo (BR-S2E4-10).
 *   <li>Xem các mục kèm kết quả đối chiếu (Kho chung + Của tôi của chính giáo viên, BR-S2E4-12).
 *   <li>"Tạo nháp": CHỈ mục được tick thành nháp đề của S2-E3 (nhãn "AI soạn", "Cần xem lại"); mục
 *       đã có trong kho không tạo bản trùng; mục đã lưu từ lần import trước nay đã chỉnh thì cập
 *       nhật đúng nháp đó (BR-S2E4-13). Mỗi nháp mới tính vào giới hạn thêm đề theo tài khoản
 *       ({@link LibraryQuota}, BR-S2E6-13) trong cùng giao dịch — vượt giới hạn thì cả lô không
 *       ghi.
 * </ol>
 *
 * Chưa có đề nào vào Kho ở đây: nháp chỉ vào giáo trình khi giáo viên duyệt (PO-30).
 */
@Service
public class ImportService {

  private final ImportRepository repo;
  private final DraftRepository drafts;
  private final AuthoringRepository authoring;
  private final DraftEditor editor;
  private final DedupMatcher matcher;
  private final LibraryQuota quota;
  private final JsonMapper mapper;
  private final TransactionTemplate tx;

  public ImportService(
      ImportRepository repo,
      DraftRepository drafts,
      AuthoringRepository authoring,
      DraftEditor editor,
      DedupMatcher matcher,
      LibraryQuota quota,
      JsonMapper mapper,
      PlatformTransactionManager tm) {
    this.repo = repo;
    this.drafts = drafts;
    this.authoring = authoring;
    this.editor = editor;
    this.matcher = matcher;
    this.quota = quota;
    this.mapper = mapper;
    this.tx = new TransactionTemplate(tm);
  }

  private JobRow owned(UUID id, AuthPrincipal me) {
    return repo.job(id)
        .filter(j -> j.ownerId().equals(me.userId()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private static String baseName(String fileName) {
    String n = fileName == null ? "" : fileName.strip();
    int cut = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
    n = cut >= 0 ? n.substring(cut + 1) : n;
    return n.length() > 200 ? n.substring(n.length() - 200) : n;
  }

  // ---------------------------------------------------------------- tạo lượt, nhận tệp

  /** Tạo lượt import: kiểm loại tệp theo đuôi và dung lượng trước khi nhận byte nào. */
  public ImportView create(String fileName, long sizeBytes) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String name = baseName(fileName);
    if (name.isEmpty() || !FileText.acceptedName(name)) {
      throw AppException.invalidField("file", FileText.ONLY_DOCX_PDF);
    }
    if (sizeBytes > ImportViews.MAX_BYTES) {
      throw new AppException(ErrorCode.PAYLOAD_TOO_LARGE, ImportViews.TOO_BIG);
    }
    if (sizeBytes < 1) {
      throw AppException.invalidField("file", FileText.BROKEN);
    }
    UUID id = Uuid7.next();
    repo.insertJob(id, me.userId(), name, (int) sizeBytes);
    return view(id);
  }

  /** Nhận một đoạn tệp; đoạn cuối thì đọc chữ, tách bài và trả các mục. */
  public ImportView upload(UUID id, int offset, int total, byte[] chunk) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    JobRow job = owned(id, me);
    if (total > ImportViews.MAX_BYTES) {
      throw new AppException(ErrorCode.PAYLOAD_TOO_LARGE, ImportViews.TOO_BIG);
    }
    if (chunk == null || chunk.length == 0 || offset < 0) {
      throw AppException.invalidField("file", "Thiếu dữ liệu tệp");
    }
    if (total != job.sizeBytes() || (long) offset + chunk.length > total) {
      throw new AppException(ErrorCode.CONFLICT, "Đoạn tệp không khớp dung lượng đã khai");
    }
    if (!"uploading".equals(job.status()) || repo.appendFile(id, offset, chunk) != 1) {
      throw new AppException(ErrorCode.CONFLICT, "Đoạn tệp không đúng thứ tự, hãy tải lại tệp");
    }
    if (offset + chunk.length < total) {
      return view(id);
    }
    parse(job, me);
    return view(id);
  }

  private void parse(JobRow job, AuthPrincipal me) {
    byte[] bytes = repo.file(job.id());
    FileText.Extracted ex;
    try {
      ex = FileText.extract(job.fileName(), bytes == null ? new byte[0] : bytes);
    } catch (FileText.Rejected r) {
      repo.close(job.id(), "rejected", null, r.getMessage(), "[]", 0);
      throw AppException.invalidField("file", r.getMessage());
    }
    Split split = ExerciseSplitter.split(ex.paragraphs());
    List<String> notes = new ArrayList<>();
    if (split.theoryBlocks() > 0) {
      notes.add(ImportViews.THEORY);
    }
    if (split.found() > ExerciseSplitter.MAX_ITEMS) {
      notes.add(ImportViews.overLimit(split.found()));
    }
    if (ex.macro()) {
      notes.add(FileText.MACRO);
    }
    if (split.items().isEmpty()) {
      notes.add(ImportViews.NO_EXERCISE);
    }
    String notesJson = mapper.writeValueAsString(notes);
    tx.executeWithoutResult(s -> {
      int idx = 1;
      for (Exercise e : split.items()) {
        String body = e.text().length() > 8000 ? e.text().substring(0, 8000) : e.text();
        String title = e.title().length() > 60 ? e.title().substring(0, 60) : e.title();
        repo.insertItem(job.id(), idx++, me.userId(), title, body, Dedup.key(body));
      }
      repo.close(job.id(), "parsed", ex.type(), null, notesJson, split.found());
    });
  }

  // ---------------------------------------------------------------- đối chiếu, hiển thị

  /** Kết quả đối chiếu một mục chưa thành nháp. */
  private record Eval(String state, String note, String refKind, UUID refId) {}

  private Eval eval(ItemRow it, List<Candidate> candidates, Map<String, UUID> origins, UUID me) {
    if (it.body().codePointCount(0, it.body().length()) > DraftService.MAX_CHARS) {
      return new Eval("too_long", ImportViews.TOO_LONG, null, null);
    }
    Hit hit = DedupMatcher.match(it.body(), candidates);
    if (hit.match() == Dedup.Match.EXISTS) {
      return new Eval("exists", hit.label(), hit.refKind(), hit.refId());
    }
    UUID origin = origins.get(it.normKey());
    if (origin != null) {
      DraftRow d = drafts.draft(origin).filter(x -> x.ownerId().equals(me)).orElse(null);
      if (d != null
          && !Dedup.normalize(d.statement()).equals(Dedup.normalize(it.body()))
          && editor.editable(d)) {
        return new Eval("update", Dedup.UPDATE_LABEL, "draft", d.id());
      }
    }
    if (hit.match() == Dedup.Match.SIMILAR) {
      return new Eval("similar", hit.label(), hit.refKind(), hit.refId());
    }
    return new Eval("new", null, null, null);
  }

  private Map<String, UUID> origins(UUID owner, UUID jobId, List<ItemRow> items) {
    Map<String, UUID> out = new HashMap<>();
    List<String> keys = items.stream().map(ItemRow::normKey).distinct().toList();
    for (ImportRepository.Origin o : repo.origins(owner, jobId, keys)) {
      out.putIfAbsent(o.normKey(), o.draftId());
    }
    return out;
  }

  public ImportView view(UUID id) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return render(owned(id, me), me, 0, 0, 0);
  }

  private ImportView render(JobRow job, AuthPrincipal me, int created, int updated, int skipped) {
    List<ItemView> items = new ArrayList<>();
    if ("parsed".equals(job.status())) {
      List<ItemRow> rows = repo.items(job.id());
      List<Candidate> candidates = matcher.candidates(me.userId());
      Map<String, UUID> origins = origins(me.userId(), job.id(), rows);
      for (ItemRow it : rows) {
        if (it.draftId() != null) {
          items.add(
              new ItemView(
                  it.idx(),
                  it.title(),
                  it.body(),
                  "created",
                  ImportViews.IN_DRAFT,
                  "draft",
                  it.draftId(),
                  false,
                  false,
                  it.draftId(),
                  ImportViews.HUMAN_AI,
                  ImportViews.MACHINE_NEEDS_REVIEW));
          continue;
        }
        Eval e = eval(it, candidates, origins, me.userId());
        boolean selectable = !"too_long".equals(e.state());
        boolean checked = selectable && !"exists".equals(e.state());
        items.add(
            new ItemView(
                it.idx(),
                it.title(),
                it.body(),
                e.state(),
                e.note(),
                e.refKind(),
                e.refId(),
                checked,
                selectable,
                null,
                null,
                null));
      }
    }
    List<String> notes = new ArrayList<>();
    for (JsonNode n : mapper.readTree(job.notesJson())) {
      notes.add(n.asString());
    }
    return new ImportView(
        job.id(),
        job.fileName(),
        job.fileType(),
        job.status(),
        job.sizeBytes(),
        job.received(),
        job.found(),
        notes,
        job.error(),
        items,
        created,
        updated,
        skipped);
  }

  // ---------------------------------------------------------------- tạo nháp

  /**
   * "Tạo nháp": chỉ các mục có số thứ tự trong {@code indexes}. Mục đã có trong kho bị bỏ qua
   * (không tạo bản trùng) kể cả khi được tick; mục "cập nhật khi lưu" cập nhật nháp cũ.
   */
  public ImportView createDrafts(UUID id, List<Integer> indexes) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    JobRow job = owned(id, me);
    if (!"parsed".equals(job.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Tệp chưa tách xong");
    }
    if (indexes == null || indexes.isEmpty()) {
      throw AppException.invalidField("indexes", "Hãy chọn ít nhất một bài");
    }
    int[] counts = new int[3];
    tx.executeWithoutResult(s -> {
      Map<Integer, ItemRow> rows = new HashMap<>();
      List<ItemRow> all = repo.items(job.id());
      all.forEach(r -> rows.put(r.idx(), r));
      List<Candidate> candidates = new ArrayList<>(matcher.candidates(me.userId()));
      Map<String, UUID> origins = origins(me.userId(), job.id(), all);
      for (int idx : new TreeSet<>(indexes)) {
        ItemRow it = rows.get(idx);
        if (it == null) {
          throw AppException.invalidField("indexes", "Không có bài số " + idx);
        }
        if (it.draftId() != null) {
          continue;
        }
        Eval e = eval(it, candidates, origins, me.userId());
        switch (e.state()) {
          case "exists", "too_long" -> counts[2]++;
          case "update" -> {
            authoring.lockDraft(e.refId());
            DraftRow d = drafts.draft(e.refId()).orElseThrow();
            editor.applyStatement(d, it.body(), null, null);
            repo.setItemDraft(job.id(), idx, d.id());
            counts[1]++;
          }
          default -> {
            UUID draftId = Uuid7.next();
            quota.consume(LibraryQuota.Channel.IMPORT, draftId);
            drafts.insert(draftId, me.userId(), "text", it.body());
            repo.setItemDraft(job.id(), idx, draftId);
            candidates.add(new Candidate("draft", draftId, it.title(), Dedup.normalize(it.body())));
            counts[0]++;
          }
        }
      }
    });
    return render(owned(id, me), me, counts[0], counts[1], counts[2]);
  }
}
