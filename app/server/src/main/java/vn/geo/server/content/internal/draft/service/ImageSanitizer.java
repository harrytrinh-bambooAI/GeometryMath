package vn.geo.server.content.internal.draft.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * S2-E3 (BR-S2E3-06, 09): nhận diện ảnh theo NỘI DUNG (không theo đuôi tệp) và xóa siêu dữ liệu (vị
 * trí, thiết bị) trước khi lưu tạm. Hàm thuần, không giải mã điểm ảnh.
 *
 * <ul>
 *   <li>JPEG: bỏ mọi đoạn APP1…APP15 (EXIF có GPS, XMP, ICC, IPTC…), giữ APP0 (JFIF), chú thích và
 *       dữ liệu ảnh từ SOS trở đi.
 *   <li>PNG: bỏ chunk eXIf, tEXt, zTXt, iTXt, tIME.
 * </ul>
 */
public final class ImageSanitizer {

  public static final int MAX_BYTES = 5_242_880;

  private static final Set<String> PNG_DROP = Set.of("eXIf", "tEXt", "zTXt", "iTXt", "tIME");

  private ImageSanitizer() {}

  /** "jpeg" | "png" theo byte đầu; null khi không phải hai định dạng được nhận. */
  public static String detect(byte[] head) {
    if (head == null || head.length < 8) {
      return head != null && head.length >= 3 && jpeg(head) ? "jpeg" : null;
    }
    if (jpeg(head)) {
      return "jpeg";
    }
    if ((head[0] & 0xFF) == 0x89
        && head[1] == 'P'
        && head[2] == 'N'
        && head[3] == 'G'
        && head[4] == 0x0D
        && head[5] == 0x0A
        && head[6] == 0x1A
        && head[7] == 0x0A) {
      return "png";
    }
    return null;
  }

  private static boolean jpeg(byte[] b) {
    return (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
  }

  /** Bản sao đã xóa siêu dữ liệu; ảnh không đọc được cấu trúc thì trả nguyên phần đọc được. */
  public static byte[] strip(byte[] img) {
    String type = detect(img);
    if ("jpeg".equals(type)) {
      return stripJpeg(img);
    }
    if ("png".equals(type)) {
      return stripPng(img);
    }
    return img;
  }

  private static byte[] stripJpeg(byte[] b) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(b.length);
    out.write(0xFF);
    out.write(0xD8);
    int i = 2;
    while (i + 4 <= b.length && (b[i] & 0xFF) == 0xFF) {
      int marker = b[i + 1] & 0xFF;
      if (marker == 0xDA || marker == 0xD9) { // SOS / EOI: phần còn lại là dữ liệu ảnh
        break;
      }
      int len = ((b[i + 2] & 0xFF) << 8) | (b[i + 3] & 0xFF);
      int end = Math.min(b.length, i + 2 + len);
      boolean meta = marker >= 0xE1 && marker <= 0xEF;
      if (!meta) {
        out.write(b, i, end - i);
      }
      i = end;
    }
    if (i < b.length) {
      out.write(b, i, b.length - i);
    }
    return out.toByteArray();
  }

  private static byte[] stripPng(byte[] b) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(b.length);
    out.write(b, 0, 8);
    int i = 8;
    while (i + 12 <= b.length) {
      long len =
          ((long) (b[i] & 0xFF) << 24)
              | ((b[i + 1] & 0xFF) << 16)
              | ((b[i + 2] & 0xFF) << 8)
              | (b[i + 3] & 0xFF);
      long end = i + 12 + len;
      if (end > b.length) {
        break;
      }
      String name = new String(b, i + 4, 4, StandardCharsets.ISO_8859_1);
      if (!PNG_DROP.contains(name)) {
        out.write(b, i, (int) (end - i));
      }
      i = (int) end;
    }
    if (i < b.length) {
      out.write(b, i, b.length - i);
    }
    return out.toByteArray();
  }

  /** Ảnh còn siêu dữ liệu vị trí / EXIF không (dùng cho kiểm thử và tự kiểm sau khi xóa). */
  public static boolean hasMetadata(byte[] img) {
    String s = new String(img, StandardCharsets.ISO_8859_1);
    return s.contains("Exif\0") || s.contains("GPS") || s.contains("eXIf");
  }
}
