package vn.geo.server.knowledge.internal.kb.service;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.knowledge.internal.kb.repo.KbGrantRepository;
import vn.geo.server.knowledge.internal.kb.repo.KbGrantRepository.Plan;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Doc;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Duplicate;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Section;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Upload;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Version;

/**
 * Kho tri thức của giáo viên (US-S5E3-01): thêm tài liệu (soạn tay Markdown, tệp docx/pdf/md ≤ 10
 * MB tải theo đoạn, giáo án đã duyệt), phiên bản bất biến, máy trạng thái Nháp → Chờ duyệt → Đang
 * dùng → Lưu trữ, đối chiếu trùng theo băm. Chỉ giáo viên chủ sở hữu thấy và tác động (người khác
 * NOT_FOUND, học sinh FORBIDDEN).
 */
@Service
public class KbDocumentService {

  /** Thân tạo tài liệu: đúng một nguồn. */
  public record CreateRequest(
      String uploadId, String title, String content, String fromLessonPlan, Boolean aiAuthored) {}

  public record EditRequest(String content) {}

  public record UploadRequest(String fileName, Long sizeBytes) {}

  /** Tệp gốc của một phiên bản. */
  public record FileOut(String fileName, String fileMime, byte[] bytes) {}

  /** Kết quả kèm mã HTTP (201 tạo mới / 200 trùng hoặc sửa tại chỗ). */
  public record Result(int status, Map<String, Object> body) {}

  private final KbRepository repo;
  private final KbGrantRepository grants;
  private final JsonMapper mapper;
  private final Clock clock;

  public KbDocumentService(
      KbRepository repo, KbGrantRepository grants, JsonMapper mapper, Clock clock) {
    this.repo = repo;
    this.grants = grants;
    this.mapper = mapper;
    this.clock = clock;
  }

  // ---------------------------------------------------------------- tải tệp theo đoạn

  public Map<String, Object> startUpload(UploadRequest req) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String name = req == null ? null : req.fileName();
    long size = req == null || req.sizeBytes() == null ? 0 : req.sizeBytes();
    if (KbText.extension(name) == null || size < 1 || size > KbMessages.MAX_BYTES) {
      throw AppException.invalidField("file", KbMessages.MSG_22);
    }
    String clean = name.strip();
    if (clean.length() > 200) {
      clean = clean.substring(clean.length() - 200);
    }
    UUID id = Uuid7.next();
    repo.insertUpload(me.orgId(), id, me.userId(), clean, (int) size);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("uploadId", id.toString());
    m.put("fileName", clean);
    m.put("sizeBytes", size);
    m.put("received", 0);
    m.put("partBytes", KbMessages.PART_BYTES);
    return m;
  }

  public Map<String, Object> uploadPart(UUID uploadId, int offset, byte[] chunk) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Upload u =
        repo.upload(me.orgId(), me.userId(), uploadId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (chunk == null || chunk.length == 0 || offset < 0) {
      throw AppException.invalidField("file", "Đoạn tệp trống.");
    }
    if ((long) offset + chunk.length > u.sizeBytes()) {
      throw AppException.invalidField("file", KbMessages.MSG_22);
    }
    if (repo.appendUpload(me.orgId(), uploadId, offset, chunk) != 1) {
      throw new AppException(ErrorCode.CONFLICT, "Đoạn tệp lệch vị trí. Thầy cô tải lại tệp.");
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("uploadId", uploadId.toString());
    m.put("sizeBytes", u.sizeBytes());
    m.put("received", offset + chunk.length);
    return m;
  }

  // ---------------------------------------------------------------- tạo tài liệu

  @Transactional
  public Result create(CreateRequest req) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (req == null) {
      throw AppException.invalidField("content", KbMessages.EMPTY_CONTENT);
    }
    int sources =
        (blank(req.uploadId()) ? 0 : 1)
            + (blank(req.content()) ? 0 : 1)
            + (blank(req.fromLessonPlan()) ? 0 : 1);
    if (sources != 1) {
      throw AppException.invalidField(
          "content", "Chọn đúng một nguồn: tệp, soạn tay hoặc giáo án đã duyệt.");
    }
    if (!blank(req.uploadId())) {
      return fromUpload(me, req);
    }
    if (!blank(req.fromLessonPlan())) {
      return fromLessonPlan(me, req.fromLessonPlan().strip());
    }
    String title = title(req.title());
    String md = req.content().strip();
    boolean ai = Boolean.TRUE.equals(req.aiAuthored());
    return insertNew(me, title, ai, ai ? "ai" : "manual", null, null, null, null, md);
  }

  private Result fromUpload(AuthPrincipal me, CreateRequest req) {
    UUID uploadId = uuid(req.uploadId(), "uploadId");
    Upload u =
        repo.upload(me.orgId(), me.userId(), uploadId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (u.received() != u.sizeBytes()) {
      throw AppException.invalidField("file", "Tệp chưa tải xong.");
    }
    byte[] bytes = repo.uploadBytes(me.orgId(), uploadId);
    String ext = KbText.extension(u.fileName());
    String md;
    try {
      md = KbText.toMarkdown(ext, bytes, KbMessages.MSG_22);
    } catch (KbText.Rejected e) {
      repo.deleteUpload(me.orgId(), uploadId);
      throw AppException.invalidField("file", e.getMessage());
    }
    String hash = KbSplitter.hash(md);
    var dup = repo.duplicate(me.orgId(), me.userId(), hash);
    repo.deleteUpload(me.orgId(), uploadId);
    if (dup.isPresent()) {
      return duplicate(dup.get());
    }
    String title = blank(req.title()) ? stem(u.fileName()) : title(req.title());
    return insertNew(
        me, title, false, "file", u.fileName(), u.fileName(), KbText.mime(ext), bytes, md);
  }

  private Result fromLessonPlan(AuthPrincipal me, String ref) {
    Plan p =
        grants
            .plan(me.orgId(), me.userId(), ref)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (p.approvedVersion() == null) {
      throw new AppException(ErrorCode.CONFLICT, KbMessages.NOT_APPROVED_PLAN);
    }
    String json =
        grants
            .planContent(me.orgId(), p.id(), p.approvedVersion())
            .orElseThrow(() -> new AppException(ErrorCode.CONFLICT, KbMessages.NOT_APPROVED_PLAN));
    JsonNode content = mapper.readTree(json);
    String title = KbLessonPlanMarkdown.title(p.code(), content);
    String md = KbLessonPlanMarkdown.markdown(title, content);
    String label = "Giáo án " + p.code() + " v" + p.approvedVersion();
    var dup = repo.duplicate(me.orgId(), me.userId(), KbSplitter.hash(md));
    if (dup.isPresent()) {
      return duplicate(dup.get());
    }
    return insertNew(me, title, false, "lesson_plan", label, null, null, null, md);
  }

  private Result insertNew(
      AuthPrincipal me,
      String title,
      boolean ai,
      String kind,
      String label,
      String fileName,
      String mime,
      byte[] bytes,
      String md) {
    List<Section> sections = KbSplitter.split(md, title);
    if (sections.isEmpty()) {
      throw AppException.invalidField("content", KbMessages.EMPTY_CONTENT);
    }
    UUID id = Uuid7.next();
    String code = repo.nextCode(me.orgId(), me.userId());
    repo.insertDoc(me.orgId(), id, me.userId(), code, title, ai);
    repo.insertVersion(
        me.orgId(),
        id,
        1,
        kind,
        label,
        fileName,
        mime,
        bytes,
        md,
        KbSplitter.hash(md),
        me.userId());
    repo.insertSections(me.orgId(), id, 1, sections);
    return new Result(201, view(me, id.toString(), 1));
  }

  private Result duplicate(Duplicate d) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.docId().toString());
    m.put("code", d.code());
    m.put("version", d.version());
    m.put("duplicate", true);
    m.put("message", KbMessages.DUPLICATE);
    return new Result(200, m);
  }

  // ---------------------------------------------------------------- sửa, chuyển trạng thái

  /**
   * Sửa phiên bản: bản Nháp chưa từng dùng sửa tại chỗ (200); bản khác tạo phiên bản mới Nháp (201,
   * MSG-S5-23 nếu bản đã từng Đang dùng). Nội dung bản cũ không đổi (BR-S5E3-01).
   */
  @Transactional
  public Result edit(String ref, int version, EditRequest req) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    Version v = version(me, d, version);
    if (req == null || blank(req.content())) {
      throw AppException.invalidField("content", KbMessages.EMPTY_CONTENT);
    }
    String md = req.content().strip();
    List<Section> sections = KbSplitter.split(md, d.title());
    String hash = KbSplitter.hash(md);
    Instant now = clock.instant();
    if ("draft".equals(v.status()) && !v.wasActive()) {
      if (repo.updateDraft(me.orgId(), d.id(), version, md, hash) == 1) {
        repo.deleteSections(me.orgId(), d.id(), version);
        repo.insertSections(me.orgId(), d.id(), version, sections);
        repo.touchDoc(me.orgId(), d.id(), now, false);
        return new Result(200, view(me, d.id().toString(), version));
      }
    }
    int next = repo.maxVersion(me.orgId(), d.id()) + 1;
    repo.insertVersion(
        me.orgId(),
        d.id(),
        next,
        "manual",
        "Sửa từ phiên bản " + version,
        null,
        null,
        null,
        md,
        hash,
        me.userId());
    repo.insertSections(me.orgId(), d.id(), next, sections);
    repo.touchDoc(me.orgId(), d.id(), now, false);
    Map<String, Object> body = view(me, d.id().toString(), next);
    if (v.wasActive()) {
      body.put("message", KbMessages.MSG_23);
    }
    return new Result(201, body);
  }

  @Transactional
  public Map<String, Object> submit(String ref, int version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    version(me, d, version);
    if (repo.setStatus(me.orgId(), d.id(), version, "draft", "pending", clock.instant()) != 1) {
      throw new AppException(ErrorCode.CONFLICT, KbMessages.SUBMIT_ONLY_DRAFT);
    }
    return view(me, d.id().toString(), version);
  }

  /** Chờ duyệt → Đang dùng; bản Đang dùng cũ sang Lưu trữ (BR-S5E3-02). Chỉ giáo viên duyệt. */
  @Transactional
  public Map<String, Object> activate(String ref, int version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    Version v = version(me, d, version);
    if (!"pending".equals(v.status())) {
      throw new AppException(ErrorCode.CONFLICT, KbMessages.ACTIVATE_ONLY_PENDING);
    }
    Instant now = clock.instant();
    repo.archiveActive(me.orgId(), d.id(), now);
    if (repo.setStatus(me.orgId(), d.id(), version, "pending", "active", now) != 1) {
      throw new AppException(ErrorCode.CONFLICT, KbMessages.ACTIVATE_ONLY_PENDING);
    }
    repo.touchDoc(me.orgId(), d.id(), now, true);
    return view(me, d.id().toString(), version);
  }

  /**
   * Lưu trữ toàn bộ tài liệu: mọi phiên bản Lưu trữ, mọi gắn kết hết hiệu lực ngay (BR-S5E3-07).
   */
  @Transactional
  public Map<String, Object> archive(String ref) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    repo.archiveDoc(me.orgId(), d.id(), clock.instant());
    return view(me, d.id().toString(), null);
  }

  // ---------------------------------------------------------------- đọc

  public Map<String, Object> list() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<Map<String, Object>> items =
        repo.list(me.orgId(), me.userId(), 500).stream()
            .map(r -> {
              Map<String, Object> m = new LinkedHashMap<>();
              m.put("id", r.id().toString());
              m.put("code", r.code());
              m.put("title", r.title());
              m.put("humanLabel", humanLabel(r.aiAuthored(), r.everActive()));
              m.put("activeVersion", r.activeVersion());
              m.put("latestVersion", r.latestVersion());
              m.put("status", r.latestStatus());
              m.put("statusLabel", KbMessages.STATUS.get(r.latestStatus()));
              m.put("archived", r.archivedAt() != null);
              m.put("updatedAt", r.updatedAt().toString());
              return m;
            })
            .toList();
    return Map.of("items", items);
  }

  public Map<String, Object> get(String ref, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return view(me, ref, version);
  }

  public FileOut file(String ref, int version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    return repo.file(me.orgId(), d.id(), version)
        .map(f -> new FileOut(f.fileName(), f.fileMime(), f.bytes()))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Chi tiết tài liệu + phiên bản chọn (mặc định: bản mới nhất) kèm danh sách mục. */
  Map<String, Object> view(AuthPrincipal me, String ref, Integer version) {
    Doc d = doc(me, ref);
    List<Version> versions = repo.versions(me.orgId(), d.id());
    Version sel =
        version == null
            ? versions.get(versions.size() - 1)
            : versions.stream()
                .filter(v -> v.version() == version)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    boolean everActive = versions.stream().anyMatch(Version::wasActive);
    Integer active =
        versions.stream()
            .filter(v -> "active".equals(v.status()))
            .map(Version::version)
            .findFirst()
            .orElse(null);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.id().toString());
    m.put("code", d.code());
    m.put("title", d.title());
    m.put("ownerId", d.ownerId().toString());
    m.put("aiAuthored", d.aiAuthored());
    m.put("humanLabel", humanLabel(d.aiAuthored(), everActive));
    m.put("archived", d.archivedAt() != null);
    m.put("activeVersion", active);
    m.put("version", sel.version());
    m.put("status", sel.status());
    m.put("statusLabel", KbMessages.STATUS.get(sel.status()));
    m.put("wasActive", sel.wasActive());
    m.put("sourceKind", sel.sourceKind());
    m.put("sourceLabel", sel.sourceLabel());
    m.put("fileName", sel.fileName());
    m.put("fileSize", sel.fileSize());
    m.put("contentHash", sel.contentHash());
    m.put("content", sel.contentMd());
    m.put("createdAt", sel.createdAt().toString());
    m.put(
        "sections",
        repo.sections(me.orgId(), d.id(), sel.version()).stream()
            .map(s ->
                Map.<String, Object>of(
                    "ord", s.ord(), "title", s.title(), "body", s.body(), "codes", s.codes()))
            .toList());
    m.put(
        "versions",
        versions.stream()
            .map(v -> {
              Map<String, Object> x = new LinkedHashMap<>();
              x.put("version", v.version());
              x.put("status", v.status());
              x.put("statusLabel", KbMessages.STATUS.get(v.status()));
              x.put("sourceLabel", v.sourceLabel());
              x.put("createdAt", v.createdAt().toString());
              return x;
            })
            .toList());
    return m;
  }

  // ---------------------------------------------------------------- tiện ích

  Doc doc(AuthPrincipal me, String ref) {
    return repo.doc(me.orgId(), me.userId(), ref)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private Version version(AuthPrincipal me, Doc d, int version) {
    return repo.versions(me.orgId(), d.id()).stream()
        .filter(v -> v.version() == version)
        .findFirst()
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  static String humanLabel(boolean aiAuthored, boolean everActive) {
    return aiAuthored && !everActive ? KbMessages.AI_LABEL : KbMessages.TEACHER_LABEL;
  }

  private static String title(String t) {
    if (t == null || t.isBlank() || t.strip().length() > 200) {
      throw AppException.invalidField("title", KbMessages.TITLE);
    }
    return t.strip();
  }

  private static String stem(String fileName) {
    int dot = fileName.lastIndexOf('.');
    String s = (dot > 0 ? fileName.substring(0, dot) : fileName).strip();
    return s.isEmpty() ? fileName : s;
  }

  static UUID uuid(String s, String field) {
    try {
      return UUID.fromString(s.strip());
    } catch (IllegalArgumentException e) {
      throw AppException.invalidField(field, "Mã không hợp lệ.");
    }
  }

  static boolean blank(String s) {
    return s == null || s.isBlank();
  }
}
