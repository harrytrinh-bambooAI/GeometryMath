package vn.geo.server.iam.api;

import java.util.UUID;

/**
 * S2-E6 (BR-S2E6-07, QA-S2E6-01): vai "người soát nội dung" do chủ sản phẩm gán (bảng
 * user_role_grant, V230). Module khác chỉ HỎI vai qua giao diện này; không có đường gán vai trong
 * ứng dụng.
 */
public interface ContentReviewerRole {

  /** Tài khoản (thuộc tổ chức của người gọi) có vai người soát nội dung không. */
  boolean isContentReviewer(UUID userId);
}
