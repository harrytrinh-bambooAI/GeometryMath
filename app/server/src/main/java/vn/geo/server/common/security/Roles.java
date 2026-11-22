package vn.geo.server.common.security;

import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/**
 * Kiểm vai ở máy chủ (BR-GEO-104, US-GEO-102-FC2): sai vai là {@code FORBIDDEN} (thấy được nhưng
 * không được làm); tài nguyên ngoài lớp/trường vẫn là {@code NOT_FOUND} (BR-GEO-112).
 */
public final class Roles {

  private Roles() {}

  /** Danh tính hiện tại nếu đúng vai, ngược lại ném FORBIDDEN (MSG-GEO-03). */
  public static AuthPrincipal require(Role role) {
    AuthPrincipal p = RequestContext.principal();
    if (p.role() != role) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    return p;
  }
}
