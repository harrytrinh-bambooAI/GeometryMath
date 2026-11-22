package vn.geo.server.common.security;

import java.util.UUID;

/**
 * Danh tính lấy từ thẻ đã kiểm chữ ký. orgId là mã trường; KHÔNG bao giờ lấy từ dữ liệu client gửi.
 */
public record AuthPrincipal(UUID orgId, UUID userId, Role role) {}
