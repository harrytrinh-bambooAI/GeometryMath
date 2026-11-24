package vn.geo.server.tracking.internal.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.paging.KeyType;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.PageRequest;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.tracking.internal.domain.MonitorView;
import vn.geo.server.tracking.internal.repo.MonitorRepository;

/**
 * E7: danh sách bài đã giao của giáo viên (mục "Theo dõi", US-GEO-701-SC6, D-GEO-129), đọc sự kiện
 * theo con trỏ có cận thời gian (US-GEO-704-SC3, FC3), báo cáo tỉ lệ phiên có thao tác trên hình
 * (M-GEO-08, US-GEO-703-SC4).
 */
@Service
public class ClassMonitorService {

  static final ListKind ASSIGNED = ListKind.byId("tracking.assigned");
  static final ListKind EVENTS = ListKind.of("tracking.events", KeyType.TIMESTAMP, KeyType.LONG);
  static final int EVENTS_PAGE = 50;

  /** Cận thời gian mặc định khi không truyền from: 31 ngày trước to. */
  static final Duration DEFAULT_SPAN = Duration.ofDays(31);

  private static final UUID NIL = new UUID(0, 0);

  public record Assigned(
      UUID id,
      String problemCode,
      String problemTitle,
      UUID classId,
      String className,
      Instant assignedAt,
      Instant releaseAt,
      String releaseState) {}

  public record Event(long id, Instant occurredAt, String kind, Integer arg) {}

  /** Báo cáo M-GEO-08: số phiên, số phiên có thao tác, tỉ lệ "{x,y}%" hoặc "—", ngưỡng đề xuất. */
  public record SessionReport(int sessions, int toolSessions, String rate, String threshold) {}

  private final MonitorRepository repo;
  private final Paging paging;

  public ClassMonitorService(MonitorRepository repo, Paging paging) {
    this.repo = repo;
    this.paging = paging;
  }

  /** Bài đã giao của các lớp mình dạy, mới giao ở trên, theo con trỏ (không tổng số). */
  public Page<Assigned> assigned(Integer limit, String cursor) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    PageRequest req = paging.request(ASSIGNED, limit, cursor);
    UUID before = req.after() == null ? null : req.after().id();
    return paging
        .page(
            repo.assigned(p.userId(), before, req.fetchSize()), req, r -> KeysetCursor.ofId(r.id()))
        .map(r ->
            new Assigned(
                r.id(),
                r.problemCode(),
                r.problemTitle(),
                r.classId(),
                r.className(),
                r.assignedAt(),
                r.releaseAt(),
                r.releaseState()));
  }

  /**
   * Sự kiện của một học sinh trên một bài (giáo viên của lớp), mới nhất trước, trang 50, con trỏ
   * (thời điểm, mã sự kiện), cận thời gian [from, to).
   */
  public Page<Event> events(
      UUID assignmentId, UUID userId, Instant from, Instant to, Integer limit, String cursor) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    PageRequest req = paging.request(EVENTS, limit == null ? EVENTS_PAGE : limit, cursor);
    repo.header(p.userId(), assignmentId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Instant hi = to == null ? Instant.now().plusSeconds(1) : to;
    Instant lo = from == null ? hi.minus(DEFAULT_SPAN) : from;
    if (!lo.isBefore(hi)) {
      throw AppException.invalidField("from", "Cận thời gian không hợp lệ.");
    }
    Instant beforeAt = req.after() == null ? null : req.after().instant(0);
    Long beforeId = req.after() == null ? null : req.after().longKey(1);
    return paging
        .page(
            repo.events(assignmentId, userId, lo, hi, beforeAt, beforeId, req.fetchSize()),
            req,
            r -> KeysetCursor.of(NIL, r.occurredAt(), r.id()))
        .map(r -> new Event(r.id(), r.occurredAt(), r.kind(), r.arg()));
  }

  /** M-GEO-08 trên các sự kiện của trường hiện tại trong [from, to). */
  public SessionReport sessionReport(Instant from, Instant to) {
    List<MonitorView.Session> s = MonitorView.sessions(repo.eventsForSessions(from, to));
    int tool = (int) s.stream().filter(MonitorView.Session::tool).count();
    return new SessionReport(
        s.size(), tool, MonitorView.toolSessionRate(s), MonitorView.TOOL_SESSION_THRESHOLD);
  }
}
