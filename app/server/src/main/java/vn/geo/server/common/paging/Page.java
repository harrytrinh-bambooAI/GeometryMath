package vn.geo.server.common.paging;

import java.util.List;

/**
 * Một trang kết quả theo khóa. nextCursor rỗng (null) nghĩa là trang cuối.
 *
 * @param items các phần tử của trang
 * @param nextCursor cursor mờ để lấy trang kế tiếp, hoặc null
 */
public record Page<T>(List<T> items, String nextCursor) {
  public <R> Page<R> map(java.util.function.Function<T, R> f) {
    return new Page<>(items.stream().map(f).toList(), nextCursor);
  }
}
