package vn.geo.server.iam.internal.service;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.iam.internal.repo.PrivacyRepository;

/**
 * Thông báo riêng tư theo phiên bản (US-S3E5-02): cờ đã đọc lưu đúng mã người dùng + phiên bản máy
 * chủ đang áp dụng + giờ máy chủ (BR-S3E5-21, 22); tài khoản có mã chưa đọc bản hiện hành bị chặn
 * dùng phần còn lại của ứng dụng (BR-S3E5-24, {@code PrivacyGateFilter}).
 *
 * <p>Tập "phải đọc" giữ trong bộ nhớ của nút (như mốc hủy phiên, {@link SessionRevocations}): nạp
 * lúc khởi động (tài khoản có mã chưa đọc bản hiện hành — tài khoản MVP vừa chuyển, hoặc bản văn
 * tăng phiên bản bằng migration rồi khởi động lại), bớt khi bấm "Đã đọc". Yêu cầu thường KHÔNG tốn
 * thêm câu SQL. Đăng ký luôn kèm cờ bản hiện hành nên không cần thêm vào tập. TẠM (một nút): lệnh
 * chuyển MVP chạy ở tiến trình riêng có hiệu lực sau khi máy chủ khởi động lại.
 */
@Service
public class PrivacyService {

  public static final String MSG_84 = "Em cần đọc thông báo riêng tư để tiếp tục.";
  public static final String MSG_VERSION = "Phiên bản thông báo riêng tư không khớp bản hiện hành.";

  public record NoticeView(
      String version, String title, String body, String ackedVersion, boolean required) {}

  public record Acked(String version) {}

  private final PrivacyRepository repo;
  private final Clock clock;
  private final Set<UUID> mustRead = ConcurrentHashMap.newKeySet();

  /** Chỉ móc kiểm thử dev/test: phiên bản hiện hành riêng cho một người dùng (giả lập bản 1.1). */
  private final Map<UUID, String> overrides = new ConcurrentHashMap<>();

  private volatile String current = "1.0";

  public PrivacyService(PrivacyRepository repo, Clock clock) {
    this.repo = repo;
    this.clock = clock;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void load() {
    current = repo.currentVersion().orElse("1.0");
    mustRead.addAll(repo.codeUsersNotAcked(current));
  }

  public String currentFor(UUID userId) {
    String o = userId == null ? null : overrides.get(userId);
    return o != null ? o : current;
  }

  /** Người dùng này phải đọc thông báo trước khi dùng tiếp không (chỉ tài khoản có mã). */
  public boolean required(AuthPrincipal p) {
    return mustRead.contains(p.userId());
  }

  public NoticeView mine() {
    AuthPrincipal p = RequestContext.principal();
    String v = currentFor(p.userId());
    PrivacyRepository.Notice n =
        repo.notice(v)
            .orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND, "Chưa có thông báo riêng tư."));
    Optional<PrivacyRepository.AckState> s = repo.state(p.orgId(), p.userId());
    String acked = s.map(PrivacyRepository.AckState::ackVersion).orElse(null);
    boolean code = s.map(PrivacyRepository.AckState::codeAccount).orElse(false);
    return new NoticeView(n.version(), n.title(), n.body(), acked, code && !v.equals(acked));
  }

  /** "Đã đọc": phiên bản do máy chủ quyết; gửi kèm phiên bản khác bản hiện hành bị từ chối. */
  public Acked ack(String version) {
    AuthPrincipal p = RequestContext.principal();
    String v = currentFor(p.userId());
    if (version != null && !v.equals(version.strip())) {
      throw AppException.invalidField("version", MSG_VERSION);
    }
    repo.ack(p.orgId(), p.userId(), v, clock.instant());
    mustRead.remove(p.userId());
    return new Acked(v);
  }

  // ------------------------------------------------------------------ móc kiểm thử (dev/test)

  public void overrideForTest(UUID orgId, UUID userId, String version) {
    if (version == null || version.isBlank()) {
      overrides.remove(userId);
    } else {
      overrides.put(userId, version);
    }
    String acked =
        repo.state(orgId, userId).map(PrivacyRepository.AckState::ackVersion).orElse(null);
    if (currentFor(userId).equals(acked)) {
      mustRead.remove(userId);
    } else {
      mustRead.add(userId);
    }
  }

  public int clearAckForTest(UUID orgId, UUID userId) {
    mustRead.add(userId);
    return repo.ack(orgId, userId, null, null);
  }

  public Optional<String> ackFor(UUID orgId, UUID userId) {
    return repo.state(orgId, userId).map(PrivacyRepository.AckState::ackVersion);
  }
}
