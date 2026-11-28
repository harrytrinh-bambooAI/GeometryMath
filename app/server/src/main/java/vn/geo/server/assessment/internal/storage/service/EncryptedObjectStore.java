package vn.geo.server.assessment.internal.storage.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Kho đối tượng mã hoá (BR-S5E4-25, PLAN_code-s5 P6). Bản {@code local}: mỗi đối tượng một tệp
 * {@code <dir>/<orgId>/<imageId>.bin} dạng "GEO1" + IV 12 byte + bản mã AES-256-GCM (thẻ 128 bit);
 * không phục vụ tĩnh. Bản {@code s3} (kho tại Việt Nam, QS5-18) chưa cấu hình: dùng là lỗi rõ ràng,
 * không âm thầm lưu chỗ khác. Khoá do máy chủ quản lý, tách theo môi trường; trống chỉ được ở
 * dev/test (khoá suy từ tên profile).
 */
@Component
public class EncryptedObjectStore {

  private static final byte[] MAGIC = "GEO1".getBytes(StandardCharsets.US_ASCII);
  private static final SecureRandom RNG = new SecureRandom();

  private final ImageProperties props;
  private final Environment env;
  private volatile SecretKeySpec aes;
  private volatile SecretKeySpec hmac;

  public EncryptedObjectStore(ImageProperties props, Environment env) {
    this.props = props;
    this.env = env;
  }

  public void write(String objectKey, byte[] plain) {
    byte[] iv = new byte[12];
    RNG.nextBytes(iv);
    byte[] enc;
    try {
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
      c.updateAAD(objectKey.getBytes(StandardCharsets.UTF_8));
      enc = c.doFinal(plain);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("không mã hoá được ảnh", e);
    }
    byte[] out = new byte[MAGIC.length + iv.length + enc.length];
    System.arraycopy(MAGIC, 0, out, 0, MAGIC.length);
    System.arraycopy(iv, 0, out, MAGIC.length, iv.length);
    System.arraycopy(enc, 0, out, MAGIC.length + iv.length, enc.length);
    Path p = path(objectKey);
    try {
      Files.createDirectories(p.getParent());
      Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
      Files.write(tmp, out);
      Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public byte[] read(String objectKey) {
    byte[] raw = raw(objectKey);
    if (raw.length < MAGIC.length + 12 || !Arrays.equals(Arrays.copyOf(raw, 4), MAGIC)) {
      throw new IllegalStateException("đối tượng ảnh hỏng");
    }
    try {
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(
          Cipher.DECRYPT_MODE,
          aesKey(),
          new GCMParameterSpec(128, Arrays.copyOfRange(raw, MAGIC.length, MAGIC.length + 12)));
      c.updateAAD(objectKey.getBytes(StandardCharsets.UTF_8));
      return c.doFinal(raw, MAGIC.length + 12, raw.length - MAGIC.length - 12);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("không giải mã được ảnh", e);
    }
  }

  /** Bản thô trong kho (đã mã hoá) — chỉ cho móc kiểm thử TC-S5E4-40. */
  public byte[] raw(String objectKey) {
    try {
      return Files.readAllBytes(path(objectKey));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public boolean exists(String objectKey) {
    return Files.exists(path(objectKey));
  }

  public void delete(String objectKey) {
    try {
      Files.deleteIfExists(path(objectKey));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** HMAC-SHA256 base64url của chuỗi (ký URL ảnh). */
  public String sign(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(hmacKey());
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }

  private Path path(String objectKey) {
    if (!"local".equals(props.backend())) {
      throw new IllegalStateException(
          "kho ảnh '"
              + props.backend()
              + "' chưa cấu hình (QS5-18): đặt geo.s5.images.backend=local");
    }
    if (!objectKey.matches("[0-9a-f-]{36}/[0-9a-f-]{36}")) {
      throw new IllegalArgumentException("khoá đối tượng không hợp lệ");
    }
    return Path.of(props.dir()).toAbsolutePath().resolve(objectKey + ".bin");
  }

  private SecretKeySpec aesKey() {
    if (aes == null) {
      aes = new SecretKeySpec(material(props.key(), "enc"), "AES");
    }
    return aes;
  }

  private SecretKeySpec hmacKey() {
    if (hmac == null) {
      hmac = new SecretKeySpec(material(props.signKey(), "sign"), "HmacSHA256");
    }
    return hmac;
  }

  private byte[] material(String configured, String use) {
    if (!configured.isEmpty()) {
      byte[] k = Base64.getDecoder().decode(configured);
      if (k.length != 32) {
        throw new IllegalStateException("khoá kho ảnh phải 32 byte (base64)");
      }
      return k;
    }
    String[] profiles = env.getActiveProfiles();
    boolean devOrTest =
        Arrays.stream(profiles).anyMatch(p -> p.equals("dev") || p.equals("test"))
            && Arrays.stream(profiles).noneMatch(p -> p.equals("prod"));
    if (!devOrTest) {
      throw new IllegalStateException(
          "chưa đặt GEO_IMAGE_KEY / GEO_IMAGE_SIGN_KEY (khoá kho ảnh, BR-S5E4-25)");
    }
    try {
      return MessageDigest.getInstance("SHA-256")
          .digest(
              ("geo-s5-images|" + use + "|" + String.join(",", profiles))
                  .getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
