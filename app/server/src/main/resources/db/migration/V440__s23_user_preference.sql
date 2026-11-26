-- T-261126-50 (S23, hệ theme): tùy chọn giao diện theo tài khoản. Mỗi người một hàng, khóa (org_id, user_id).
-- theme: 'a' Lớp học sáng · 'b' Bảng đen tối · 'c' Giấy kẻ ô · 'd' Bảng điều khiển gọn (mặc định, DECISIONS 26/11).
-- Không có hàng = mặc định 'd'. Module sở hữu: settings (không sửa iam/app_user).
CREATE TABLE user_preference (
  org_id     uuid NOT NULL,
  user_id    uuid NOT NULL,
  theme      text NOT NULL DEFAULT 'd' CHECK (theme IN ('a', 'b', 'c', 'd')),
  updated_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, user_id),
  FOREIGN KEY (org_id, user_id) REFERENCES app_user (org_id, id) ON DELETE CASCADE
);

GRANT SELECT, INSERT, UPDATE ON user_preference TO geo_app;
