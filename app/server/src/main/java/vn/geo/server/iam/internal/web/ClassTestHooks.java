package vn.geo.server.iam.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.iam.internal.service.ClassTestHookService;
import vn.geo.server.iam.internal.service.ClassTestWorld;

/** Móc kiểm thử S3-E1 / S3-E5 (chỉ dev/test, không có ở prod). */
@RestController
@Profile({"dev", "test"})
public class ClassTestHooks {

  private final ClassTestWorld world;
  private final ClassTestHookService hooks;

  public ClassTestHooks(ClassTestWorld world, ClassTestHookService hooks) {
    this.world = world;
    this.hooks = hooks;
  }

  /** Dựng thế giới riêng cho một ca (giáo viên, lớp, học sinh ở các trạng thái). */
  @PostMapping("/test-hooks/s3e1/world")
  public ClassTestWorld.World build(@RequestBody ClassTestWorld.Spec spec) {
    return world.build(spec);
  }

  public record Age(UUID userId, long seconds) {}

  /** Giả lập thời gian trôi cho bộ đếm nhập mã lớp sai của một tài khoản. */
  @PostMapping("/test-hooks/s3e1/join-attempts/age")
  public Map<String, Object> ageAttempts(@RequestBody Age b) {
    return Map.of("updated", hooks.ageJoinAttempts(b.userId(), b.seconds()));
  }

  public record Seed(UUID userId, List<Long> minutesAgo) {}

  /** Giả lập các lần nhập sai đã xảy ra (mỗi phần tử = số phút trước). */
  @PostMapping("/test-hooks/s3e1/join-attempts/seed")
  public Map<String, Object> seedAttempts(@RequestBody Seed b) {
    return Map.of("inserted", hooks.seedJoinAttempts(b.userId(), b.minutesAgo()));
  }

  /** Giả lập mật khẩu tạm của học sinh đã sinh sớm hơn {@code seconds} giây. */
  @PostMapping("/test-hooks/s3e1/temp-password/age")
  public Map<String, Object> ageTemp(@RequestBody Age b) {
    return Map.of("updated", hooks.ageTempPassword(b.userId(), b.seconds()));
  }

  /** Giả lập các lần đặt lại mật khẩu của giáo viên đã xảy ra sớm hơn. */
  @PostMapping("/test-hooks/s3e1/password-resets/age")
  public Map<String, Object> ageResets(@RequestBody Age b) {
    return Map.of("updated", hooks.ageResets(b.userId(), b.seconds()));
  }

  public record Who(UUID userId) {}

  /** Giả lập tài khoản đã xóa (US-S3E1-03-FC5; xóa thật ở US-S3E5-05, task 32). */
  @PostMapping("/test-hooks/s3e1/deactivate")
  public Map<String, Object> deactivate(@RequestBody Who b) {
    return Map.of("updated", hooks.deactivate(b.userId()));
  }

  /** Số sự kiện bảo mật (bị từ chối vì quan hệ) của một người gọi. */
  @GetMapping("/test-hooks/s3e1/denials")
  public Map<String, Object> denials(@RequestParam UUID actorId) {
    return Map.of("count", hooks.denials(actorId));
  }

  public record PrivacyOverride(UUID userId, String version) {}

  /** Phiên bản thông báo riêng tư hiện hành riêng cho một người dùng (giả lập văn bản tăng bản). */
  @PostMapping("/test-hooks/s3e5/privacy/current")
  public Map<String, Object> override(@RequestBody PrivacyOverride b) {
    hooks.overridePrivacy(b.userId(), b.version());
    return Map.of("ok", true);
  }

  /** Bỏ cờ đã đọc (giả lập tài khoản MVP chuyển sang chưa có cờ). */
  @PostMapping("/test-hooks/s3e5/privacy/clear")
  public Map<String, Object> clear(@RequestBody Who b) {
    return Map.of("updated", hooks.clearPrivacy(b.userId()));
  }

  /** Cờ đã đọc lưu trong CSDL (phiên bản) của một người dùng. */
  @GetMapping("/test-hooks/s3e5/privacy")
  public Map<String, Object> flag(@RequestParam UUID userId) {
    return Map.of("version", hooks.privacyAck(userId).orElse(""));
  }
}
