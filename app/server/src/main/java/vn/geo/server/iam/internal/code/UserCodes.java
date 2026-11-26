package vn.geo.server.iam.internal.code;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

/**
 * Mã người dùng {@code GV-xxxx-xxxx} / {@code HS-xxxx-xxxx} và mã khôi phục {@code
 * RC-xxxx-xxxx-xxxx} (PO-61, DQ-16, BR-S2E1-04/05). Bảng 32 ký tự chữ hoa và số, bỏ ký tự dễ nhầm
 * O, 0, I, 1: mỗi ký tự 5 bit (mã người dùng 40 bit, mã khôi phục 60 bit).
 */
public final class UserCodes {

  /** 24 chữ cái (bỏ I, O) + 8 chữ số (bỏ 0, 1) = 32 ký tự. */
  public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

  private static final SecureRandom RANDOM = new SecureRandom();

  private UserCodes() {}

  /** Sinh mã người dùng mới theo vai ({@code teacher} → GV-, {@code student} → HS-). */
  public static String newUserCode(boolean teacher) {
    return (teacher ? "GV-" : "HS-") + group(4) + "-" + group(4);
  }

  public static String newRecoveryCode() {
    return "RC-" + group(4) + "-" + group(4) + "-" + group(4);
  }

  /** Mật khẩu tạm cho tài khoản chuyển từ MVP (BR-S2E1-15): 12 ký tự trong bảng 32 ký tự. */
  public static String newTempPassword() {
    return group(4) + "-" + group(4) + "-" + group(2);
  }

  private static String group(int n) {
    StringBuilder b = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      b.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return b.toString();
  }

  /**
   * Chuẩn hóa mã người dùng gõ tay: cắt khoảng trắng, không phân biệt hoa thường, nhận cả dạng
   * thiếu gạch nối (BR-S2E1-07, QA-S2E1-08). Rỗng nếu không đúng dạng — khi đó không cần tra CSDL.
   */
  public static Optional<String> normalizeUserCode(String raw) {
    if (raw == null) {
      return Optional.empty();
    }
    String s = raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    if (!s.matches("(GV|HS)[A-Z0-9]{8}")) {
      return Optional.empty();
    }
    return Optional.of(s.substring(0, 2) + "-" + s.substring(2, 6) + "-" + s.substring(6, 10));
  }

  /** Chuẩn hóa mã khôi phục gõ tay về dạng {@code RC-XXXX-XXXX-XXXX}; rỗng nếu sai dạng. */
  public static Optional<String> normalizeRecoveryCode(String raw) {
    if (raw == null) {
      return Optional.empty();
    }
    String s = raw.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    if (s.startsWith("RC") && s.length() == 14) {
      s = s.substring(2);
    }
    if (!s.matches("[A-Z0-9]{12}")) {
      return Optional.empty();
    }
    return Optional.of("RC-" + s.substring(0, 4) + "-" + s.substring(4, 8) + "-" + s.substring(8));
  }

  /** Mã hợp lệ theo BR-S2E1-04 (dùng trong kiểm thử và khi nhận mã từ dữ liệu cũ). */
  public static boolean isValidUserCode(String code) {
    return code != null && code.matches("(GV|HS)-[" + ALPHABET + "]{4}-[" + ALPHABET + "]{4}");
  }
}
