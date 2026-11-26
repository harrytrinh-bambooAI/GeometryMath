package vn.geo.server.iam.internal.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * US-S3E5-03 (BR-S3E5-33, 36): mẫu tên trường cấm, không phân biệt hoa thường và dấu gạch. Dùng
 * chung cho bộ lọc thân yêu cầu (máy chủ) và test quét danh sách trường được phép ({@code
 * allowed_fields}).
 *
 * <p>Mẫu dài (≥ 4 ký tự) so khớp như chuỗi con của tên đã chuẩn hóa (bỏ mọi ký tự không phải
 * chữ/số): {@code e-mail}, {@code phoneNumber}, {@code so_dien_thoai}, {@code birthday}, {@code
 * school_name}, {@code avatarUrl}, {@code deviceId}. Mẫu ngắn ({@code ip}, {@code dob}, {@code
 * sdt}, {@code anh}) chỉ khớp nguyên một từ của tên (tách theo camelCase, gạch dưới, gạch ngang) để
 * không bắt nhầm {@code tip}, {@code canh}.
 */
public final class FieldNames {

  /** Mẫu chuỗi con trên tên đã chuẩn hóa. */
  public static final List<String> SUBSTRINGS =
      List.of(
          "email",
          "mail",
          "phone",
          "mobile",
          "dienthoai",
          "birth",
          "ngaysinh",
          "address",
          "diachi",
          "school",
          "truong",
          "avatar",
          "photo",
          "image",
          "cmnd",
          "cccd",
          "device",
          "useragent",
          "browser");

  /** Mẫu nguyên từ. */
  public static final Set<String> TOKENS = Set.of("sdt", "dob", "ip", "anh");

  /** Mã trường kỹ thuật nội bộ (BR-S3E5-36): không nhận từ thân yêu cầu, không trả ra ngoài. */
  public static final Set<String> TECHNICAL = Set.of("orgid");

  private FieldNames() {}

  /** Chữ thường, bỏ mọi ký tự không phải chữ cái Latin cơ bản hay chữ số. */
  public static String normalize(String name) {
    return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
  }

  /** Tách tên thành từ: camelCase, gạch dưới, gạch ngang, khoảng trắng. */
  public static List<String> tokens(String name) {
    String spaced = name.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
    List<String> out = new ArrayList<>();
    for (String t : spaced.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
      if (!t.isEmpty()) {
        out.add(t);
      }
    }
    return out;
  }

  /** Mẫu cấm mà tên khớp (nếu có). */
  public static Optional<String> forbiddenPattern(String name) {
    String n = normalize(name);
    for (String s : SUBSTRINGS) {
      if (n.contains(s)) {
        return Optional.of(s);
      }
    }
    for (String t : tokens(name)) {
      if (TOKENS.contains(t)) {
        return Optional.of(t);
      }
    }
    return Optional.empty();
  }

  public static boolean isTechnical(String name) {
    return TECHNICAL.contains(normalize(name));
  }
}
