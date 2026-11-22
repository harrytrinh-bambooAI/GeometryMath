package vn.geo.server.common.paging;

import java.util.Base64;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình phân trang (bảng giá trị ở {@code app/docs/CONFIG.md}).
 *
 * @param defaultLimit kích thước trang mặc định (20)
 * @param maxLimit trần chung kích thước trang (100)
 * @param maxLimitByList trần riêng theo {@link ListKind#code()}, chỉ được thấp hơn trần chung (ví
 *     dụ Kho đề 50, D-GEO-142)
 * @param cursorKey khóa AES 32 byte mã hóa base64
 */
@ConfigurationProperties("geo.paging")
public record PagingProperties(
    int defaultLimit, int maxLimit, Map<String, Integer> maxLimitByList, String cursorKey) {
  public PagingProperties {
    if (defaultLimit <= 0) {
      defaultLimit = 20;
    }
    if (maxLimit < defaultLimit) {
      maxLimit = Math.max(100, defaultLimit);
    }
    maxLimitByList = maxLimitByList == null ? Map.of() : Map.copyOf(maxLimitByList);
    maxLimitByList.forEach((k, v) -> {
      if (v == null || v <= 0) {
        throw new IllegalArgumentException("geo.paging.max-limit-by-list." + k + " phải > 0");
      }
    });
    if (cursorKey == null || Base64.getDecoder().decode(cursorKey).length != 32) {
      throw new IllegalArgumentException("geo.paging.cursor-key phải là base64 của đúng 32 byte");
    }
  }

  /** Trần áp cho một loại danh sách: min(trần chung, trần riêng nếu có). */
  public int maxLimitFor(String listCode) {
    return Math.min(maxLimit, maxLimitByList.getOrDefault(listCode, maxLimit));
  }

  /** Kích thước mặc định cho một loại danh sách (không vượt trần của loại đó). */
  public int defaultLimitFor(String listCode) {
    return Math.min(defaultLimit, maxLimitFor(listCode));
  }
}
