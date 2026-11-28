package vn.geo.server.knowledge.internal.kb.service;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.knowledge.internal.kb.repo.KbGrantRepository;
import vn.geo.server.knowledge.internal.kb.repo.KbGrantRepository.Effective;
import vn.geo.server.knowledge.internal.kb.repo.KbGrantRepository.Grant;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository.Doc;

/**
 * Gắn tài liệu cho lớp hoặc một bài của lớp mình, thời hạn tuỳ chọn (US-S5E3-02, BR-S5E3-06, 07).
 * Gắn theo tài liệu (không theo phiên bản): AI dùng phiên bản Đang dùng tại thời điểm hỏi.
 */
@Service
public class KbGrantService {

  public record GrantRequest(
      String classId, String assignmentId, String validFrom, String validTo) {}

  private final KbRepository docs;
  private final KbGrantRepository repo;
  private final Clock clock;

  public KbGrantService(KbRepository docs, KbGrantRepository repo, Clock clock) {
    this.docs = docs;
    this.repo = repo;
    this.clock = clock;
  }

  @Transactional
  public Map<String, Object> grant(String ref, GrantRequest req) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    if (req == null || KbDocumentService.blank(req.classId())) {
      throw AppException.invalidField("classId", "Chọn lớp.");
    }
    UUID classId = id(req.classId());
    if (repo.ownedClass(me.orgId(), me.userId(), classId).isEmpty()) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    UUID assignmentId = null;
    if (!KbDocumentService.blank(req.assignmentId())) {
      assignmentId = id(req.assignmentId());
      if (!repo.assignmentInClass(me.orgId(), classId, assignmentId)) {
        throw new AppException(ErrorCode.NOT_FOUND);
      }
    }
    Instant from = instant(req.validFrom(), "validFrom");
    Instant to = instant(req.validTo(), "validTo");
    if (from != null && to != null && !to.isAfter(from)) {
      throw AppException.invalidField("validTo", KbMessages.VALID_RANGE);
    }
    boolean active =
        d.archivedAt() == null
            && docs.versions(me.orgId(), d.id()).stream()
                .anyMatch(v -> "active".equals(v.status()));
    if (!active) {
      throw new AppException(ErrorCode.CONFLICT, KbMessages.GRANT_NOT_ACTIVE);
    }
    UUID id = Uuid7.next();
    repo.insert(me.orgId(), id, d.id(), classId, assignmentId, from, to, me.userId());
    return repo.ofDoc(me.orgId(), d.id()).stream()
        .filter(g -> g.id().equals(id))
        .findFirst()
        .map(g -> view(g, clock.instant()))
        .orElseThrow();
  }

  public Map<String, Object> list(String ref) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    Instant now = clock.instant();
    return Map.of("items", repo.ofDoc(me.orgId(), d.id()).stream().map(g -> view(g, now)).toList());
  }

  @Transactional
  public Map<String, Object> revoke(String ref, UUID grantId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Doc d = doc(me, ref);
    if (repo.revoke(me.orgId(), d.id(), grantId, clock.instant()) != 1) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return Map.of("id", grantId.toString(), "revoked", true);
  }

  /**
   * Tài liệu hiệu lực của lớp tại {@code at} (mặc định: bây giờ): {@code assignmentId} có = tài
   * liệu dùng cho bài đó (gắn cả lớp + gắn riêng bài); không có = mọi gắn của lớp.
   */
  public Map<String, Object> ofClass(UUID classId, String assignmentId, String at) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    String className =
        repo.ownedClass(me.orgId(), me.userId(), classId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    UUID a = KbDocumentService.blank(assignmentId) ? null : id(assignmentId);
    if (a != null && !repo.assignmentInClass(me.orgId(), classId, a)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    Instant when = at == null || at.isBlank() ? clock.instant() : instant(at, "at");
    List<Effective> rows = repo.effective(me.orgId(), classId, a, a == null, when);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("classId", classId.toString());
    m.put("className", className);
    m.put("assignmentId", a == null ? null : a.toString());
    m.put("at", when.toString());
    m.put(
        "items",
        rows.stream()
            .map(e -> {
              Map<String, Object> x = new LinkedHashMap<>();
              x.put("grantId", e.grantId().toString());
              x.put("id", e.docId().toString());
              x.put("code", e.code());
              x.put("title", e.title());
              x.put("activeVersion", e.activeVersion());
              x.put("assignmentId", e.assignmentId() == null ? null : e.assignmentId().toString());
              x.put("validFrom", e.validFrom() == null ? null : e.validFrom().toString());
              x.put("validTo", e.validTo() == null ? null : e.validTo().toString());
              return x;
            })
            .toList());
    return m;
  }

  private Map<String, Object> view(Grant g, Instant now) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", g.id().toString());
    m.put("docId", g.docId().toString());
    m.put("classId", g.classId().toString());
    m.put("assignmentId", g.assignmentId() == null ? null : g.assignmentId().toString());
    m.put("validFrom", g.validFrom() == null ? null : g.validFrom().toString());
    m.put("validTo", g.validTo() == null ? null : g.validTo().toString());
    m.put(
        "effective",
        (g.validFrom() == null || !g.validFrom().isAfter(now))
            && (g.validTo() == null || g.validTo().isAfter(now)));
    m.put("createdAt", g.createdAt().toString());
    return m;
  }

  private Doc doc(AuthPrincipal me, String ref) {
    return docs.doc(me.orgId(), me.userId(), ref)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Mã lớp / bài sai dạng = đối tượng ngoài quan hệ (NOT_FOUND, không lộ tồn tại). */
  private static UUID id(String s) {
    try {
      return UUID.fromString(s.strip());
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
  }

  static Instant instant(String s, String field) {
    if (s == null || s.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(s.strip());
    } catch (DateTimeParseException e) {
      throw AppException.invalidField(field, "Thời điểm dạng ISO-8601 UTC.");
    }
  }
}
