package vn.geo.server.common.paging;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Loại danh sách được phân trang. Con trỏ phát cho loại này chỉ dùng được với chính loại này (và
 * chính trường đó): dùng sang danh sách khác bị {@code INVALID_CURSOR} (US-GEO-104-FC8, 606-FC4).
 *
 * <p>Khóa sắp xếp của danh sách là {@code (keyTypes..., id)} theo đúng thứ tự {@code ORDER BY};
 * {@code id} UUIDv7 luôn là thành phần cuối, phá hòa (D-GEO-124, D-GEO-168). Danh sách chỉ sắp theo
 * {@code id} dùng {@link #byId(String)}.
 *
 * <p>Trần kích thước trang theo loại đặt ở {@code geo.paging.max-limit-by-list} (CONFIG.md).
 *
 * @param code mã ổn định, chữ thường, dạng {@code <module>.<danh-sách>}; đổi mã làm mọi con trỏ cũ
 *     hết hiệu lực
 * @param keyTypes kiểu các thành phần khóa đứng trước {@code id} (có thể rỗng)
 */
public record ListKind(String code, List<KeyType> keyTypes) {

  private static final Pattern CODE = Pattern.compile("[a-z][a-z0-9_-]*(\\.[a-z0-9_-]+)*");
  static final int MAX_KEYS = 8;

  public ListKind {
    if (code == null || code.length() > 64 || !CODE.matcher(code).matches()) {
      throw new IllegalArgumentException("Mã loại danh sách không hợp lệ: " + code);
    }
    keyTypes = List.copyOf(keyTypes);
    if (keyTypes.size() > MAX_KEYS) {
      throw new IllegalArgumentException("Tối đa " + MAX_KEYS + " thành phần khóa trước id");
    }
  }

  /** Danh sách sắp theo {@code id} UUIDv7 giảm dần. */
  public static ListKind byId(String code) {
    return new ListKind(code, List.of());
  }

  /** Danh sách sắp theo {@code (leading..., id)}. */
  public static ListKind of(String code, KeyType... leading) {
    return new ListKind(code, List.of(leading));
  }
}
