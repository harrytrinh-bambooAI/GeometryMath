package vn.geo.server.geometry.api;

/**
 * Engine không dựng được hình: ràng buộc mâu thuẫn (F-GEO-074), hình suy biến (tham số 0), tọa độ
 * AI hỏng (F-GEO-111) hoặc bộ dựng không biết. Người dùng thấy MSG-GEO-18.
 */
public class SceneBuildException extends RuntimeException {

  public enum Reason {
    CONTRADICTORY,
    DEGENERATE,
    UNKNOWN_TEMPLATE,
    BAD_COORDINATES
  }

  private final Reason reason;

  public SceneBuildException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  public Reason reason() {
    return reason;
  }
}
