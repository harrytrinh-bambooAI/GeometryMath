package vn.geo.server.content.api;

import java.util.UUID;

/**
 * Guard DÙNG CHUNG cho quyền bộ đề (bank) của Kho đề (E5, FIX3). Bộ 'main' mọi trường thấy; bộ khác
 * chỉ tổ chức có hàng {@code catalog_bank_access} thấy. MỌI đường đọc hoặc giao một version đề kho
 * theo id (chi tiết đề, lời giải giáo viên, cảnh/hình của trang chi tiết, tạo bài giao, và mọi
 * đường thêm sau này) PHẢI gọi {@link #requireVisible(UUID)} ở tầng dịch vụ TRƯỚC khi đọc/ghi, kể
 * cả khi hàm SQL bên dưới đã kiểm (phòng một migration sau định nghĩa lại hàm mà quên điều kiện).
 * Xem {@code app/docs/CONFIG.md} mục "Quyền bộ đề".
 */
public interface BankAccess {

  /** Tổ chức của người gọi (TenantContext) thấy version đề kho này không. */
  boolean visible(UUID problemVersionId);

  /**
   * Ném {@code AppException(NOT_FOUND)} khi version không phải đề kho hoặc thuộc bộ mà tổ chức
   * người gọi không thấy (không lộ là có tồn tại: BR-GEO-112).
   */
  void requireVisible(UUID problemVersionId);
}
