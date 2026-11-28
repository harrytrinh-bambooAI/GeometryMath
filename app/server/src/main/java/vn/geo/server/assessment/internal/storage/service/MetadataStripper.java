package vn.geo.server.assessment.internal.storage.service;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import vn.geo.server.common.error.AppException;

/**
 * Gỡ toàn bộ metadata ảnh (BR-S5E4-27): JPEG bỏ mọi đoạn APP1…APP15 (EXIF, XMP, GPS, IPTC, ICC) và
 * COM, giữ APP0 (JFIF) và dữ liệu ảnh; PNG chỉ giữ các khúc hiển thị (bỏ eXIf, tEXt, zTXt, iTXt,
 * tIME…). Không giải mã điểm ảnh nên không đổi chất lượng. Định dạng khác: VALIDATION_FAILED.
 */
public final class MetadataStripper {

  private MetadataStripper() {}

  /** Ảnh đã gỡ và kiểu của nó. */
  public record Clean(byte[] bytes, String contentType) {}

  private static final Set<String> PNG_KEEP =
      Set.of(
          "IHDR", "PLTE", "IDAT", "IEND", "tRNS", "gAMA", "cHRM", "sRGB", "sBIT", "pHYs", "bKGD");

  public static Clean strip(byte[] in) {
    if (in == null || in.length < 8) {
      throw AppException.invalidField("image", "Ảnh không hợp lệ (chỉ nhận JPEG hoặc PNG).");
    }
    if ((in[0] & 0xFF) == 0xFF && (in[1] & 0xFF) == 0xD8) {
      return new Clean(jpeg(in), "image/jpeg");
    }
    if ((in[0] & 0xFF) == 0x89 && in[1] == 'P' && in[2] == 'N' && in[3] == 'G') {
      return new Clean(png(in), "image/png");
    }
    throw AppException.invalidField("image", "Ảnh không hợp lệ (chỉ nhận JPEG hoặc PNG).");
  }

  static byte[] jpeg(byte[] in) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(in.length);
    out.write(0xFF);
    out.write(0xD8);
    int i = 2;
    while (i + 2 <= in.length) {
      if ((in[i] & 0xFF) != 0xFF) {
        throw bad();
      }
      int marker = in[i + 1] & 0xFF;
      if (marker == 0xFF) { // đệm
        i++;
        continue;
      }
      if (marker == 0xD9) { // EOI: mọi byte sau EOI (MPF, ảnh thu nhỏ kèm EXIF/GPS…) bị bỏ
        out.write(in, i, 2);
        return out.toByteArray();
      }
      if (marker >= 0xD0 && marker <= 0xD7 || marker == 0x01) {
        out.write(in, i, 2);
        i += 2;
        continue;
      }
      if (i + 4 > in.length) {
        throw bad();
      }
      int len = ((in[i + 2] & 0xFF) << 8) | (in[i + 3] & 0xFF);
      if (len < 2 || i + 2 + len > in.length) {
        throw bad();
      }
      boolean drop = (marker >= 0xE1 && marker <= 0xEF) || marker == 0xFE;
      if (!drop) {
        out.write(in, i, 2 + len);
      }
      i += 2 + len;
      if (marker == 0xDA) { // SOS: chép dữ liệu nén tới marker kế (bỏ qua 0xFF00 và RSTn)
        int end = scanEnd(in, i);
        out.write(in, i, end - i);
        i = end;
        if (i >= in.length) { // thiếu EOI: đóng ảnh
          out.write(0xFF);
          out.write(0xD9);
          return out.toByteArray();
        }
      }
    }
    throw bad();
  }

  /** Vị trí marker đầu tiên sau dữ liệu nén bắt đầu ở {@code from} (hoặc cuối tệp). */
  private static int scanEnd(byte[] in, int from) {
    int j = from;
    while (j + 1 < in.length) {
      if ((in[j] & 0xFF) == 0xFF) {
        int next = in[j + 1] & 0xFF;
        if (next == 0x00 || (next >= 0xD0 && next <= 0xD7)) {
          j += 2;
          continue;
        }
        if (next != 0xFF) {
          return j;
        }
        // 0xFF 0xFF: byte đệm trước marker — marker bắt đầu ở byte 0xFF cuối
        int k = j;
        while (k + 1 < in.length && (in[k + 1] & 0xFF) == 0xFF) {
          k++;
        }
        return k + 1 < in.length ? k : in.length;
      }
      j++;
    }
    return in.length;
  }

  static byte[] png(byte[] in) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(in.length);
    out.write(in, 0, 8);
    int i = 8;
    while (i + 12 <= in.length) {
      long len =
          ((long) (in[i] & 0xFF) << 24)
              | ((in[i + 1] & 0xFF) << 16)
              | ((in[i + 2] & 0xFF) << 8)
              | (in[i + 3] & 0xFF);
      String type = new String(in, i + 4, 4, java.nio.charset.StandardCharsets.US_ASCII);
      long total = 12 + len;
      if (len < 0 || i + total > in.length) {
        throw bad();
      }
      if (PNG_KEEP.contains(type)) {
        out.write(in, i, (int) total);
      }
      i += (int) total;
      if ("IEND".equals(type)) {
        return out.toByteArray();
      }
    }
    throw bad();
  }

  private static AppException bad() {
    return AppException.invalidField("image", "Ảnh hỏng hoặc không đọc được.");
  }
}
