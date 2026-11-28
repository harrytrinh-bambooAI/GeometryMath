package vn.geo.server.iam.internal.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.iam.internal.service.AccountDeletionService;

/**
 * US-S3E5-05: "Xóa tài khoản" ở Cài đặt. Dưới {@code /api/v1/account/} nên không bị cổng thông báo
 * riêng tư chặn (người chưa đọc thông báo vẫn xóa được tài khoản của mình).
 */
@RestController
public class AccountController {

  /** Thân xác nhận: mật khẩu hiện tại và chữ "XÓA"; {@code requestId} là khóa thử lại. */
  public record DeleteBody(String password, String confirm, UUID requestId) {}

  private final AccountDeletionService deletion;

  public AccountController(AccountDeletionService deletion) {
    this.deletion = deletion;
  }

  @NoStore
  @PostMapping("/api/v1/account/users/{userId}/delete")
  public AccountDeletionService.Deleted delete(
      @PathVariable UUID userId, @RequestBody(required = false) DeleteBody b) {
    DeleteBody body = b == null ? new DeleteBody(null, null, null) : b;
    return deletion.delete(userId, body.password(), body.confirm());
  }
}
