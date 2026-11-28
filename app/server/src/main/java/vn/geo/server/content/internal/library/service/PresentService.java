package vn.geo.server.content.internal.library.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.content.internal.library.repo.LibraryRepository;
import vn.geo.server.content.internal.library.repo.LibraryRepository.ClassRelease;
import vn.geo.server.content.internal.library.repo.LibraryRepository.DocCard;
import vn.geo.server.content.internal.library.service.LibraryViews.Preflight;
import vn.geo.server.content.internal.library.service.LibraryViews.Presentation;
import vn.geo.server.content.internal.solution.service.FigureFacts;

/**
 * S2-E7 — Trình chiếu trên lớp (khung C2-04). Chỉ giáo viên chủ đề (học sinh FORBIDDEN:
 * BR-S2E7-05). Một lần gọi trả TOÀN BỘ dữ liệu buổi chiếu (đề, hình, mọi bước) để màn chiếu chạy
 * được khi mất mạng (BR-S2E7-07); phản hồi no-store, trình duyệt chỉ giữ trong bộ nhớ trang
 * (BR-S2E7-10). Hỏi lại khi đề đang giao mà lời giải còn khóa, theo GIỜ MÁY CHỦ (BR-S2E7-11…14).
 * Trình chiếu không ghi gì: máy học sinh vẫn khóa, {@code release_at} không đổi (BR-S2E7-13).
 */
@Service
public class PresentService {

  private final LibraryRepository repo;
  private final LibraryService library;
  private final FigureFacts facts;
  private final int reconnectSeconds;
  private volatile Clock clock = Clock.systemUTC();

  public PresentService(
      LibraryRepository repo,
      LibraryService library,
      FigureFacts facts,
      @Value("${geo.s2e7.reconnect-seconds:5}") int reconnectSeconds) {
    this.repo = repo;
    this.library = library;
    this.facts = facts;
    this.reconnectSeconds = reconnectSeconds;
  }

  /** Chỉ dùng trong kiểm thử: đồng hồ giả của máy chủ (US-S2E7-03-BV1). null = đồng hồ hệ thống. */
  public void overrideClockForTest(Clock testClock) {
    this.clock = testClock == null ? Clock.systemUTC() : testClock;
  }

  private DocCard owned(UUID docId, AuthPrincipal me) {
    DocCard d = repo.doc(docId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!d.ownerId().equals(me.userId())) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return d;
  }

  /** Lớp còn khóa lời giải: chưa đặt giờ mở hoặc giờ máy chủ còn trước {@code release_at}. */
  List<String> lockedClasses(UUID teacher, UUID docId, Instant now) {
    return repo.classReleases(teacher, docId).stream()
        .filter(c -> c.releaseAt() == null || now.isBefore(c.releaseAt()))
        .map(ClassRelease::className)
        .distinct()
        .toList();
  }

  public Preflight preflight(UUID docId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    owned(docId, me);
    Instant now = clock.instant();
    List<String> locked = lockedClasses(me.userId(), docId, now);
    return new Preflight(!locked.isEmpty(), locked, now);
  }

  public Presentation load(UUID docId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    DocCard d = owned(docId, me);
    Integer version = repo.presentVersion(docId).orElse(null);
    List<String> steps = List.of();
    String machine = null;
    String human = null;
    if (version != null) {
      steps =
          repo.ownerSteps(me.userId(), docId, version).map(library::stepTexts).orElse(List.of());
      String[] labels = repo.versionLabels(docId, version).orElse(new String[] {null, null});
      machine = labels[0];
      human = labels[1];
    }
    Object scene = facts.scene(repo.figure(docId).orElse(null), machine);
    return new Presentation(
        d.id(),
        d.title(),
        d.statement(),
        d.kind(),
        scene,
        steps,
        !steps.isEmpty(),
        LibraryLabels.machine(machine),
        LibraryLabels.human(human),
        lockedClasses(me.userId(), docId, clock.instant()),
        reconnectSeconds);
  }
}
