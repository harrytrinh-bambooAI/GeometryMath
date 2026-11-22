package vn.geo.server.common.id;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Sinh UUIDv7 (RFC 9562) phía ứng dụng: 48 bit mili giây, 12 bit bộ đếm đơn điệu, 62 bit ngẫu
 * nhiên. Cùng một tiến trình, giá trị sinh ra luôn tăng dần nên khóa chính theo thời gian, chèn
 * cuối chỉ mục.
 */
public final class Uuid7 {

  private static final Generator DEFAULT = new Generator(System::currentTimeMillis);

  private Uuid7() {}

  public static UUID next() {
    return DEFAULT.next();
  }

  /** Thời điểm (mili giây) nhúng trong UUIDv7. */
  public static Instant timestamp(UUID id) {
    if (id.version() != 7) {
      throw new IllegalArgumentException("Không phải UUIDv7: " + id);
    }
    return Instant.ofEpochMilli(id.getMostSignificantBits() >>> 16);
  }

  /** Bộ sinh có đồng hồ tiêm vào được, để kiểm thử. */
  public static final class Generator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final LongSupplier clockMillis;
    private long lastMillis = Long.MIN_VALUE;
    private int counter;

    public Generator(LongSupplier clockMillis) {
      this.clockMillis = clockMillis;
    }

    public synchronized UUID next() {
      long now = clockMillis.getAsLong();
      if (now > lastMillis) {
        lastMillis = now;
        counter = RANDOM.nextInt(0x800); // chừa chỗ cho 2048 giá trị trong cùng mili giây
      } else {
        counter++;
        if (counter > 0xFFF) { // tràn bộ đếm: đẩy sang mili giây kế tiếp để giữ đơn điệu
          lastMillis++;
          counter = RANDOM.nextInt(0x800);
        }
      }
      long msb = (lastMillis << 16) | (0x7L << 12) | counter;
      long lsb = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
      return new UUID(msb, lsb);
    }
  }
}
