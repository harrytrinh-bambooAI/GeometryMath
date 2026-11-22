package vn.geo.server.common.id;

import java.util.UUID;

/**
 * Tổ chức nền tảng: sở hữu nội dung dùng chung (kho đề, lời giải kho). Khớp hàm {@code
 * platform_org_id()} của migration V10.
 */
public final class PlatformOrg {

  public static final UUID ID = UUID.fromString("00000000-0000-7000-8000-00000000f001");

  private PlatformOrg() {}
}
