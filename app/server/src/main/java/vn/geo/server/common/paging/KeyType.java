package vn.geo.server.common.paging;

/** Kiểu của một thành phần khóa sắp xếp đứng trước {@code id} trong con trỏ. */
public enum KeyType {
  /** {@link java.time.Instant}, cắt về micro giây như {@code timestamptz} của PostgreSQL. */
  TIMESTAMP(1),
  /** {@link Long} (số nguyên, kể cả int/smallint của cột). */
  LONG(2),
  /** {@link String}, tối đa {@link KeysetCursor#MAX_TEXT_BYTES} byte UTF-8. */
  TEXT(3),
  /** {@link java.util.UUID}. */
  UUID(4);

  final byte tag;

  KeyType(int tag) {
    this.tag = (byte) tag;
  }

  static KeyType ofTag(byte tag) {
    for (KeyType t : values()) {
      if (t.tag == tag) {
        return t;
      }
    }
    return null;
  }

  static KeyType of(Object value) {
    return switch (value) {
      case java.time.Instant i -> TIMESTAMP;
      case Long l -> LONG;
      case String s -> TEXT;
      case java.util.UUID u -> UUID;
      default -> null;
    };
  }
}
