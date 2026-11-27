package vn.geo.server.exam.api;

import java.util.UUID;

/**
 * Cổng cho module khác (US-S5E4-02 tạo bài có chấm): chỉ bộ đề đã duyệt mới giao hoặc gắn rubric
 * được (BR-S5E2-11). Gọi trong ngữ cảnh đã xác thực của giáo viên chủ bộ đề.
 */
public interface ExamSetGuard {

  /**
   * Ném {@code NOT_FOUND} khi bộ đề không thuộc giáo viên hiện tại, {@code CONFLICT} khi bộ đề chưa
   * duyệt (nhãn "Máy lắp").
   */
  void requireAssignable(UUID examSetId);
}
