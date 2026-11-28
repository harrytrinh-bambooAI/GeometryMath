package vn.geo.server.geometry.internal.plane;

/**
 * Không dựng được hình phẳng; {@code getMessage()} là lý do hiện cho người dùng (vd "ba cạnh không
 * lập thành tam giác", "góc không hợp lệ", "hình chưa xác định, thiếu dữ kiện", "dạng chưa hỗ
 * trợ").
 */
public class PlaneBuildException extends RuntimeException {

  public static final String NOT_TRIANGLE = "ba cạnh không lập thành tam giác";
  public static final String BAD_ANGLE = "góc không hợp lệ";
  public static final String UNDETERMINED = "hình chưa xác định, thiếu dữ kiện";
  public static final String UNSUPPORTED = "dạng chưa hỗ trợ";
  public static final String CONTRADICTION = "số liệu mâu thuẫn";
  public static final String BAD_LENGTH = "độ dài không hợp lệ";

  public PlaneBuildException(String reason) {
    super(reason);
  }
}
