package vn.geo.server.common.security;

/**
 * Ngữ cảnh yêu cầu hiện tại, ràng buộc bằng ScopedValue (an toàn với luồng ảo, tự hết hiệu lực khi
 * rời phạm vi). Chỉ {@link AuthFilter} được đặt giá trị.
 */
public final class RequestContext {

  static final ScopedValue<AuthPrincipal> PRINCIPAL = ScopedValue.newInstance();

  private RequestContext() {}

  /** Danh tính của yêu cầu đang xử lý; ném lỗi lập trình nếu gọi ngoài phạm vi xác thực. */
  public static AuthPrincipal principal() {
    if (!PRINCIPAL.isBound()) {
      throw new IllegalStateException("Không có ngữ cảnh xác thực cho yêu cầu này");
    }
    return PRINCIPAL.get();
  }

  public static boolean isAuthenticated() {
    return PRINCIPAL.isBound();
  }

  /** Dùng cho tác vụ nền/kiểm thử cần chạy dưới một danh tính. */
  public static <R, X extends Throwable> R callAs(
      AuthPrincipal principal, ScopedValue.CallableOp<R, X> op) throws X {
    return ScopedValue.where(PRINCIPAL, principal).call(op);
  }
}
