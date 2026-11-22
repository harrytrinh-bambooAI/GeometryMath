package vn.geo.server.iam.internal.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.ClientIp;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.iam.internal.service.AuthService;

/** Vào lớp (học sinh), vào bằng mã Giáo viên mẫu, lớp của giáo viên. */
@RestController
public class AuthController {

  /** Màn "Vào lớp" chỉ thu hai ô (BR-GEO-100); trường khác trong thân bị bỏ qua. */
  public record StudentJoin(String displayName, String joinCode) {}

  public record TeacherCode(String code) {}

  private final AuthService auth;
  private final ClientIp clientIp;

  public AuthController(AuthService auth, ClientIp clientIp) {
    this.auth = auth;
    this.clientIp = clientIp;
  }

  @NoStore
  @PostMapping("/api/v1/auth/student")
  public AuthService.Session student(@RequestBody StudentJoin body) {
    return auth.studentJoin(body.displayName(), body.joinCode());
  }

  @NoStore
  @PostMapping("/api/v1/auth/teacher")
  public AuthService.Session teacher(@RequestBody TeacherCode body, HttpServletRequest request) {
    return auth.teacherLogin(body.code(), clientIp.of(request));
  }

  @GetMapping("/api/v1/teacher/classes")
  public Map<String, List<AuthService.ClassItem>> classes() {
    return Map.of("items", auth.myClasses());
  }

  /** MÓC KIỂM THỬ PO-51 (chỉ dev/test, không có ở prod): đặt lại bộ đếm thử sai mã giáo viên. */
  @RestController
  @Profile({"dev", "test"})
  public static class AuthTestHooks {
    private final AuthService auth;

    public AuthTestHooks(AuthService auth) {
      this.auth = auth;
    }

    /** Dọn học sinh do ca kiểm tạo (QF-E1-02: khôi phục số học sinh của lớp): đặt left_at. */
    public record Leave(String joinCode, String displayName) {}

    @PostMapping("/test-hooks/students/leave")
    public Map<String, Integer> leave(@RequestBody Leave b) {
      return Map.of("left", auth.leaveClass(b.joinCode(), b.displayName()));
    }

    /** Phạm vi đặt lại: địa chỉ IP (như X-Forwarded-For ca kiểm gửi) và/hoặc tài khoản. */
    public record ResetScope(List<String> ips, List<java.util.UUID> userIds) {}

    @PostMapping("/test-hooks/auth-failures/reset")
    public org.springframework.http.ResponseEntity<Map<String, Object>> reset(
        @RequestBody ResetScope b) {
      if ((b.ips() == null || b.ips().isEmpty())
          && (b.userIds() == null || b.userIds().isEmpty())) {
        return org.springframework.http.ResponseEntity.badRequest()
            .body(Map.of("error", "cần ips hoặc userIds (không xóa toàn bảng)"));
      }
      return org.springframework.http.ResponseEntity.ok(
          Map.of("deleted", auth.resetAuthFailures(b.ips(), b.userIds())));
    }
  }
}
