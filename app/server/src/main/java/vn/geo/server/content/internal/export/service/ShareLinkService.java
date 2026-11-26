package vn.geo.server.content.internal.export.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
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
import vn.geo.server.content.internal.export.repo.ExportRepository;
import vn.geo.server.content.internal.export.repo.ExportRepository.LinkRow;

/**
 * Liên kết chỉ xem của một đề (US-S2E9-02, BR-S2E9-09, 10). Mã ngẫu nhiên 256 bit (base64url 43 ký
 * tự), máy chủ chỉ lưu SHA-256 của mã nên địa chỉ đầy đủ chỉ hiện MỘT lần lúc tạo. Hạn 1, 7 hoặc 30
 * ngày (mặc định 7); tối đa 5 liên kết đang dùng mỗi đề; thu hồi được. Người mở liên kết chỉ nhận
 * chữ đề và hình.
 */
@Service
public class ShareLinkService {

  /** Liên kết đang dùng; {@code token} chỉ có ở phản hồi tạo mới. */
  public record Link(
      UUID id,
      UUID docId,
      String token,
      String hint,
      int days,
      String daysLabel,
      Instant createdAt,
      Instant expiresAt) {}

  /** Nội dung công khai của một liên kết còn dùng được. */
  public record SharedView(
      String title, String statement, String kind, String sceneKind, Object scene) {}

  public static final String MSG_GONE = "Liên kết không còn dùng được";
  public static final String MSG_MAX = "Tối đa 5 liên kết đang dùng";
  public static final String MSG_DAYS = "Hạn liên kết là 1, 7 hoặc 30 ngày";
  static final int MAX_ACTIVE = 5;
  static final int DEFAULT_DAYS = 7;

  private final ExportRepository repo;
  private final ExportService export;
  private final JsonMapper mapper;
  private final SecureRandom random = new SecureRandom();
  private volatile Clock clock;

  public ShareLinkService(
      ExportRepository repo, ExportService export, JsonMapper mapper, Clock clock) {
    this.repo = repo;
    this.export = export;
    this.mapper = mapper;
    this.clock = clock;
  }

  /** Chỉ dùng trong kiểm thử: đồng hồ giả (US-S2E9-02-BV2). null = đồng hồ hệ thống. */
  public void overrideClockForTest(Clock testClock) {
    this.clock = testClock == null ? Clock.systemUTC() : testClock;
  }

  @Transactional
  public Link create(UUID docId, Integer days) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (docId == null) {
      throw AppException.invalidField("docId", "Hãy chọn đề");
    }
    int d = days == null ? DEFAULT_DAYS : days;
    if (d != 1 && d != 7 && d != 30) {
      throw AppException.invalidField("days", MSG_DAYS);
    }
    if (!repo.lockOwnDoc(me.userId(), docId)) {
      throw new AppException(ErrorCode.NOT_FOUND); // đề của người khác (BR-S2-06)
    }
    Instant now = clock.instant();
    if (repo.activeLinks(me.userId(), docId, now).size() >= MAX_ACTIVE) {
      throw new AppException(ErrorCode.CONFLICT, MSG_MAX);
    }
    byte[] raw = new byte[32];
    random.nextBytes(raw);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    UUID id = Uuid7.next();
    Instant until = now.plus(Duration.ofDays(d));
    String hint = token.substring(token.length() - 4);
    repo.insertLink(id, docId, me.userId(), hash(token), hint, d, now, until);
    return new Link(id, docId, token, hint, d, d + " ngày", now, until);
  }

  public List<Link> list(UUID docId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return repo.activeLinks(me.userId(), docId, clock.instant()).stream().map(this::link).toList();
  }

  /** Thu hồi: liên kết của người khác hoặc đã thu hồi là NOT_FOUND. */
  public void revoke(UUID linkId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (repo.revoke(me.userId(), linkId, clock.instant()) == 0) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
  }

  /** Mở liên kết (không đăng nhập). Rỗng = mã sai, hết hạn hoặc đã thu hồi (không phân biệt). */
  public Optional<SharedView> view(String token) {
    if (token == null || token.length() < 22 || token.length() > 64) {
      return Optional.empty();
    }
    return repo.shared(hash(token), clock.instant())
        .map(d -> {
          ExportService.Scene scene = export.scene(d.statement(), d.figure(), d.machineStatus());
          return new SharedView(
              d.title(), d.statement(), kind(d.figure()), scene.kind(), scene.scene());
        });
  }

  private String kind(String figureJson) {
    if (figureJson == null) {
      return "space";
    }
    JsonNode fig = mapper.readTree(figureJson);
    return fig.path("plane").isObject() ? "plane" : "space";
  }

  private Link link(LinkRow r) {
    return new Link(
        r.id(),
        r.docId(),
        null,
        r.hint(),
        r.days(),
        r.days() + " ngày",
        r.createdAt(),
        r.expiresAt());
  }

  static byte[] hash(String token) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
