package vn.geo.server.common.paging;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Vị trí cuối của trang trước: các giá trị khóa sắp xếp của dòng cuối theo đúng thứ tự {@code ORDER
 * BY}, kết thúc bằng {@code id} UUIDv7. Thời điểm cắt về micro giây như PostgreSQL.
 *
 * @param keys giá trị khóa đứng trước id ({@link Instant}, {@link Long}, {@link String}, {@link
 *     UUID}); Integer được đổi sang Long
 * @param id UUIDv7 của dòng cuối
 */
public record KeysetCursor(List<Object> keys, UUID id) {

  /** Trần độ dài khóa văn bản (byte UTF-8) để con trỏ luôn ngắn. */
  public static final int MAX_TEXT_BYTES = 256;

  public KeysetCursor {
    Objects.requireNonNull(id, "id");
    List<Object> norm = new ArrayList<>(keys.size());
    for (Object k : keys) {
      Object v =
          switch (k) {
            case null -> throw new IllegalArgumentException("Khóa con trỏ không được null");
            case Instant i -> i.truncatedTo(ChronoUnit.MICROS);
            case Integer n -> n.longValue();
            case String s -> {
              if (s.getBytes(StandardCharsets.UTF_8).length > MAX_TEXT_BYTES) {
                throw new IllegalArgumentException("Khóa văn bản quá dài cho con trỏ");
              }
              yield s;
            }
            case Long l -> l;
            case UUID u -> u;
            default ->
                throw new IllegalArgumentException("Kiểu khóa không hỗ trợ: " + k.getClass());
          };
      norm.add(v);
    }
    if (norm.size() > ListKind.MAX_KEYS) {
      throw new IllegalArgumentException("Quá nhiều thành phần khóa");
    }
    keys = List.copyOf(norm);
  }

  /** Con trỏ chỉ gồm id. */
  public static KeysetCursor ofId(UUID id) {
    return new KeysetCursor(List.of(), id);
  }

  /** Con trỏ {@code (leading..., id)}. */
  public static KeysetCursor of(UUID id, Object... leading) {
    return new KeysetCursor(List.of(leading), id);
  }

  public Instant instant(int i) {
    return (Instant) keys.get(i);
  }

  public long longKey(int i) {
    return (Long) keys.get(i);
  }

  public String text(int i) {
    return (String) keys.get(i);
  }

  public UUID uuid(int i) {
    return (UUID) keys.get(i);
  }

  boolean matches(ListKind kind) {
    if (keys.size() != kind.keyTypes().size()) {
      return false;
    }
    for (int i = 0; i < keys.size(); i++) {
      if (KeyType.of(keys.get(i)) != kind.keyTypes().get(i)) {
        return false;
      }
    }
    return true;
  }
}
