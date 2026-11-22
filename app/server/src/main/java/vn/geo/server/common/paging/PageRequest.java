package vn.geo.server.common.paging;

/**
 * Yêu cầu trang đã kiểm tra. Truy vấn phải lấy {@link #fetchSize()} dòng (limit + 1) để biết còn
 * trang sau mà không cần COUNT.
 *
 * @param kind loại danh sách (con trỏ trang sau gắn với loại này)
 * @param limit số phần tử trả về, đã chặn trần của loại danh sách
 * @param after vị trí sau trang trước, hoặc null nếu là trang đầu
 */
public record PageRequest(ListKind kind, int limit, KeysetCursor after) {
  public int fetchSize() {
    return limit + 1;
  }

  public boolean isFirstPage() {
    return after == null;
  }
}
