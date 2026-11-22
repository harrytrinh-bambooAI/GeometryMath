package vn.geo.server.common.paging;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.TenantContext;

/**
 * Cửa duy nhất để phân trang theo khóa. Quy ước truy vấn: {@code ORDER BY <khóa...> DESC, id DESC};
 * lấy {@code fetchSize()} dòng; trang sau dùng điều kiện so sánh bộ {@code (<khóa...>, id) <
 * (:k..., :afterId)} để đi thẳng theo chỉ mục {@code (org_id, <khóa...> DESC, id DESC)}. Không dùng
 * OFFSET, không COUNT toàn bảng.
 *
 * <p>Con trỏ gắn với trường + loại danh sách + phiên bản (xem {@link CursorCodec}).
 */
@Component
public class Paging {

  private final PagingProperties props;
  private final CursorCodec codec;

  public Paging(PagingProperties props) {
    this.props = props;
    this.codec = new CursorCodec(Base64.getDecoder().decode(props.cursorKey()));
  }

  /** Dùng mã trường của yêu cầu hiện tại. */
  public PageRequest request(ListKind kind, Integer limit, String cursor) {
    return request(kind, limit, cursor, TenantContext.orgId());
  }

  public PageRequest request(ListKind kind, Integer limit, String cursor, UUID orgId) {
    int effective = props.defaultLimitFor(kind.code());
    if (limit != null) {
      if (limit < 1) {
        throw new AppException(ErrorCode.VALIDATION_FAILED, "Kích thước trang phải từ 1 trở lên.");
      }
      effective = Math.min(limit, props.maxLimitFor(kind.code()));
    }
    KeysetCursor after =
        cursor == null || cursor.isEmpty() ? null : codec.decode(kind, cursor, orgId);
    return new PageRequest(kind, effective, after);
  }

  public <T> Page<T> page(List<T> fetched, PageRequest request, Function<T, KeysetCursor> keyOf) {
    return page(fetched, request, keyOf, TenantContext.orgId());
  }

  public <T> Page<T> page(
      List<T> fetched, PageRequest request, Function<T, KeysetCursor> keyOf, UUID orgId) {
    if (fetched.size() <= request.limit()) {
      return new Page<>(List.copyOf(fetched), null);
    }
    List<T> items = List.copyOf(fetched.subList(0, request.limit()));
    return new Page<>(
        items, codec.encode(request.kind(), keyOf.apply(items.get(items.size() - 1)), orgId));
  }
}
