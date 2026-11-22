package vn.geo.server.common.web;

import java.io.IOException;

/** Thân yêu cầu vượt giới hạn khi đọc dòng (trường hợp không có Content-Length). */
public class BodyTooLargeException extends IOException {
  public BodyTooLargeException(long limit) {
    super("Thân yêu cầu vượt quá " + limit + " byte");
  }
}
