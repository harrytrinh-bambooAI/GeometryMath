package vn.geo.server.iam.internal.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Con trỏ trang danh sách học sinh, ký HMAC và gắn với MỘT lớp (US-S3E1-05-FC5): con trỏ của lớp
 * khác, bị sửa hoặc sai dạng đều là INVALID_CURSOR.
 */
@Component
public class ClassCursor {

  public record Key(String nameKey, UUID userId) {}

  private final byte[] key;

  public ClassCursor(@Value("${geo.auth.code-pepper}") String pepper) {
    this.key = ("class-cursor|" + pepper).getBytes(StandardCharsets.UTF_8);
  }

  public String encode(UUID classId, String nameKey, UUID userId) {
    String body = classId + "|" + userId + "|" + nameKey;
    Base64.Encoder enc = Base64.getUrlEncoder().withoutPadding();
    return enc.encodeToString(body.getBytes(StandardCharsets.UTF_8))
        + "."
        + enc.encodeToString(mac(body));
  }

  public Optional<Key> decode(String cursor, UUID classId) {
    try {
      int dot = cursor.indexOf('.');
      if (dot <= 0) {
        return Optional.empty();
      }
      Base64.Decoder dec = Base64.getUrlDecoder();
      String body = new String(dec.decode(cursor.substring(0, dot)), StandardCharsets.UTF_8);
      byte[] sig = dec.decode(cursor.substring(dot + 1));
      if (!MessageDigest.isEqual(sig, mac(body))) {
        return Optional.empty();
      }
      String[] parts = body.split("\\|", 3);
      if (parts.length != 3 || !UUID.fromString(parts[0]).equals(classId)) {
        return Optional.empty();
      }
      return Optional.of(new Key(parts[2], UUID.fromString(parts[1])));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private byte[] mac(String body) {
    try {
      Mac m = Mac.getInstance("HmacSHA256");
      m.init(new SecretKeySpec(key, "HmacSHA256"));
      return Arrays.copyOf(m.doFinal(body.getBytes(StandardCharsets.UTF_8)), 16);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
