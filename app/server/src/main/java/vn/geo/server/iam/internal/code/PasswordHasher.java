package vn.geo.server.iam.internal.code;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Băm mật khẩu PBKDF2-HMAC-SHA256 (JCE, không thêm thư viện) dạng {@code pbkdf2-sha256$<vòng>$<muối
 * b64>$<băm b64>}; số vòng nằm trong chuỗi băm nên đổi cấu hình không làm hỏng băm cũ. Mã khôi phục
 * (60 bit ngẫu nhiên) chỉ cần HMAC với pepper (như mã giáo viên D-GEO-152).
 */
public final class PasswordHasher {

  private static final String PREFIX = "pbkdf2-sha256";
  private static final SecureRandom RANDOM = new SecureRandom();

  private final int iterations;
  private final byte[] pepper;
  private final String dummyHash;

  public PasswordHasher(int iterations, byte[] pepper) {
    if (iterations < 1) {
      throw new IllegalArgumentException("số vòng PBKDF2 phải ≥ 1");
    }
    this.iterations = iterations;
    this.pepper = pepper.clone();
    this.dummyHash = hash("không-dùng-để-đăng-nhập");
  }

  public String hash(String password) {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);
    byte[] dk = pbkdf2(password, salt, iterations);
    Base64.Encoder b64 = Base64.getEncoder().withoutPadding();
    return PREFIX
        + "$"
        + iterations
        + "$"
        + b64.encodeToString(salt)
        + "$"
        + b64.encodeToString(dk);
  }

  /**
   * So mật khẩu với chuỗi băm; {@code stored == null} vẫn tốn cùng thời gian (không lộ mã có thật).
   */
  public boolean verify(String password, String stored) {
    String target = stored == null ? dummyHash : stored;
    String[] p = target.split("\\$");
    if (p.length != 4 || !PREFIX.equals(p[0])) {
      return false;
    }
    Base64.Decoder b64 = Base64.getDecoder();
    byte[] expect = b64.decode(p[3]);
    byte[] got = pbkdf2(password == null ? "" : password, b64.decode(p[2]), Integer.parseInt(p[1]));
    return MessageDigest.isEqual(expect, got) && stored != null;
  }

  /**
   * HMAC-SHA256(pepper, "rc:" + userId + ":" + mã khôi phục chuẩn hóa): gắn mã với đúng tài khoản.
   */
  public byte[] recoveryHmac(java.util.UUID userId, String recoveryCode) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
      return mac.doFinal(("rc:" + userId + ":" + recoveryCode).getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  /** HMAC của địa chỉ IP (bộ đếm đăng ký không giữ IP rõ). */
  public String ipKey(String ip) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(pepper, "HmacSHA256"));
      return Base64.getEncoder()
          .encodeToString(mac.doFinal(("reg-ip:" + ip).getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private static byte[] pbkdf2(String password, byte[] salt, int rounds) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, rounds, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    } finally {
      spec.clearPassword();
    }
  }
}
