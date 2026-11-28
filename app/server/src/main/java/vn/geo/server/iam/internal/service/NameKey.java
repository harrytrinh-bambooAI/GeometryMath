package vn.geo.server.iam.internal.service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Khóa tên trong lớp (class_member.name_key): bỏ dấu, chữ thường, gộp khoảng trắng. "An", "an", "
 * AN " là cùng một học sinh (D-GEO-106). Khớp {@code lower(f_unaccent(...))} của seed.
 */
public final class NameKey {

  private NameKey() {}

  /** Tên hiển thị sau khi bỏ khoảng trắng đầu/cuối và gộp khoảng trắng giữa (BR-GEO-102). */
  public static String clean(String displayName) {
    return displayName == null ? "" : displayName.strip().replaceAll("\\s+", " ");
  }

  public static String of(String displayName) {
    String s = Normalizer.normalize(clean(displayName), Normalizer.Form.NFD);
    s = s.replaceAll("\\p{M}+", "").replace('đ', 'd').replace('Đ', 'D');
    return s.toLowerCase(Locale.ROOT);
  }
}
