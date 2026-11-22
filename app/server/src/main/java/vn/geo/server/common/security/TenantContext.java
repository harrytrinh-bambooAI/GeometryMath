package vn.geo.server.common.security;

import java.util.UUID;

/**
 * Điểm DUY NHẤT cung cấp mã trường cho truy vấn. Mã trường đến từ thẻ đã xác thực; mọi mã trường do
 * client gửi (tham số, thân, header) bị bỏ qua.
 */
public final class TenantContext {

  private TenantContext() {}

  public static UUID orgId() {
    return RequestContext.principal().orgId();
  }
}
