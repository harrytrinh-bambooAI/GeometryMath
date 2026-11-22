package vn.geo.server.common.paging;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

/**
 * Con trỏ mờ AES-256-GCM (D-GEO-168): client không đọc được và không sửa được. Bên trong là khóa
 * sắp xếp của dòng cuối kết thúc bằng {@code id} UUIDv7 (D-GEO-124). Con trỏ gắn với
 *
 * <ul>
 *   <li><b>phiên bản</b> định dạng: byte đầu của token, đồng thời nằm trong dữ liệu xác thực;
 *   <li><b>trường</b> ({@code org_id}) và <b>loại danh sách</b> ({@link ListKind#code()}): nằm
 *       trong dữ liệu xác thực (AAD), nên dùng sang trường khác hay danh sách khác thì giải mã thất
 *       bại.
 * </ul>
 *
 * Mọi lỗi (sai định dạng, bị sửa, khác trường, khác danh sách, khác phiên bản, sai kiểu khóa) đều
 * thành {@code INVALID_CURSOR}, không phân biệt lý do để không lộ thông tin.
 *
 * <p>Token = base64url(phiên bản 1 byte ‖ IV 12 byte ‖ bản mã ‖ thẻ GCM 16 byte). Bản rõ = số khóa
 * ‖ (thẻ kiểu ‖ giá trị)… ‖ id 16 byte.
 */
public final class CursorCodec {

  /** Phiên bản định dạng hiện hành. Đổi định dạng thì tăng số; con trỏ cũ trả INVALID_CURSOR. */
  public static final byte VERSION = 2;

  private static final int IV_LEN = 12;
  private static final int TAG_BITS = 128;
  private static final int MAX_TOKEN_CHARS = 1024;
  private static final byte[] AAD_PREFIX = "geo-cursor".getBytes(StandardCharsets.US_ASCII);
  private static final SecureRandom RANDOM = new SecureRandom();

  private final SecretKeySpec key;

  /**
   * @param key32 đúng 32 byte
   */
  public CursorCodec(byte[] key32) {
    if (key32 == null || key32.length != 32) {
      throw new IllegalArgumentException("Khóa cursor phải đúng 32 byte");
    }
    this.key = new SecretKeySpec(key32, "AES");
  }

  public String encode(ListKind kind, KeysetCursor cursor, UUID orgId) {
    if (!cursor.matches(kind)) {
      throw new IllegalArgumentException(
          "Khóa con trỏ không khớp kiểu của danh sách " + kind.code());
    }
    ByteBuffer plain = ByteBuffer.allocate(plainSize(cursor));
    plain.put((byte) cursor.keys().size());
    for (Object k : cursor.keys()) {
      KeyType t = KeyType.of(k);
      plain.put(t.tag);
      switch (k) {
        case Instant i -> plain.putLong(toMicros(i));
        case Long l -> plain.putLong(l);
        case String s -> {
          byte[] b = s.getBytes(StandardCharsets.UTF_8);
          plain.putShort((short) b.length).put(b);
        }
        case UUID u -> putUuid(plain, u);
        default -> throw new IllegalStateException();
      }
    }
    putUuid(plain, cursor.id());
    byte[] iv = new byte[IV_LEN];
    RANDOM.nextBytes(iv);
    try {
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
      c.updateAAD(aad(VERSION, kind, orgId));
      byte[] ct = c.doFinal(plain.array());
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(
              ByteBuffer.allocate(1 + iv.length + ct.length).put(VERSION).put(iv).put(ct).array());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * @throws AppException INVALID_CURSOR nếu sai định dạng, bị sửa, khác phiên bản, thuộc trường
   *     khác hoặc danh sách khác
   */
  public KeysetCursor decode(ListKind kind, String token, UUID orgId) {
    try {
      if (token == null || token.isEmpty() || token.length() > MAX_TOKEN_CHARS) {
        throw invalid();
      }
      byte[] raw = Base64.getUrlDecoder().decode(token);
      if (raw.length < 1 + IV_LEN + TAG_BITS / 8 + 1 + 16 || raw[0] != VERSION) {
        throw invalid();
      }
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, raw, 1, IV_LEN));
      c.updateAAD(aad(raw[0], kind, orgId));
      ByteBuffer plain = ByteBuffer.wrap(c.doFinal(raw, 1 + IV_LEN, raw.length - 1 - IV_LEN));
      int n = plain.get();
      if (n != kind.keyTypes().size()) {
        throw invalid();
      }
      List<Object> keys = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
        KeyType t = KeyType.ofTag(plain.get());
        if (t != kind.keyTypes().get(i)) {
          throw invalid();
        }
        keys.add(
            switch (t) {
              case TIMESTAMP -> fromMicros(plain.getLong());
              case LONG -> plain.getLong();
              case TEXT -> {
                int len = plain.getShort();
                if (len < 0 || len > KeysetCursor.MAX_TEXT_BYTES) {
                  throw invalid();
                }
                byte[] b = new byte[len];
                plain.get(b);
                yield new String(b, StandardCharsets.UTF_8);
              }
              case UUID -> getUuid(plain);
            });
      }
      UUID id = getUuid(plain);
      if (plain.hasRemaining()) {
        throw invalid();
      }
      return new KeysetCursor(keys, id);
    } catch (IllegalArgumentException
        | BufferUnderflowException
        | GeneralSecurityException
        | java.time.DateTimeException e) {
      throw invalid();
    }
  }

  private static byte[] aad(byte version, ListKind kind, UUID orgId) {
    byte[] code = kind.code().getBytes(StandardCharsets.US_ASCII);
    ByteBuffer b = ByteBuffer.allocate(AAD_PREFIX.length + 1 + 16 + 1 + code.length);
    b.put(AAD_PREFIX).put(version);
    putUuid(b, orgId);
    b.put((byte) code.length).put(code);
    return b.array();
  }

  private static int plainSize(KeysetCursor cursor) {
    int size = 1 + 16;
    for (Object k : cursor.keys()) {
      size +=
          1
              + switch (k) {
                case String s -> 2 + s.getBytes(StandardCharsets.UTF_8).length;
                case UUID u -> 16;
                default -> 8;
              };
    }
    return size;
  }

  private static long toMicros(Instant t) {
    return Math.addExact(Math.multiplyExact(t.getEpochSecond(), 1_000_000L), t.getNano() / 1000);
  }

  private static Instant fromMicros(long micros) {
    return Instant.ofEpochSecond(
        Math.floorDiv(micros, 1_000_000L), Math.floorMod(micros, 1_000_000L) * 1000);
  }

  private static AppException invalid() {
    return new AppException(ErrorCode.INVALID_CURSOR);
  }

  private static void putUuid(ByteBuffer b, UUID u) {
    b.putLong(u.getMostSignificantBits()).putLong(u.getLeastSignificantBits());
  }

  private static UUID getUuid(ByteBuffer b) {
    return new UUID(b.getLong(), b.getLong());
  }
}
