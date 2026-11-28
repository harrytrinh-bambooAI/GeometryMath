package vn.geo.server.content.api;

import java.util.UUID;

/**
 * S2-E6 (BR-S2E6-13, PO-63): giới hạn thêm đề theo TÀI KHOẢN — tối đa 20 đề trong 60 phút trượt,
 * tính CHUNG mọi đường tạo đề. Mọi đường tạo đề vào "Của tôi" (dán chữ, ảnh, import của
 * S2-E3/S2-E4, sao chép từ Kho chung) PHẢI gọi {@link #consume} trong CÙNG giao dịch với lệnh ghi
 * đề, TRƯỚC khi ghi. Vượt giới hạn: ném {@code AppException} RATE_LIMITED kèm {@code Retry-After}.
 */
public interface LibraryQuota {

  /** Kênh tạo đề. */
  enum Channel {
    TEXT,
    IMAGE,
    IMPORT,
    COPY
  }

  /** Ghi một lần thêm đề của người gọi; ném RATE_LIMITED nếu đã đủ giới hạn trong cửa sổ. */
  void consume(Channel channel, UUID refId);
}
