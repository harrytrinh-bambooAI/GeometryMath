package vn.geo.server.assessment.internal.storage.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import vn.geo.server.common.error.AppException;

/**
 * Gỡ EXIF/XMP khỏi ảnh gốc HEIC (BR-S5E4-27; T-261128-60 nối Dev-S5E4-07): trong cấu trúc ISOBMFF,
 * EXIF là mục {@code Exif} và XMP là mục {@code mime} (application/rdf+xml) của hộp {@code meta};
 * vùng dữ liệu của các mục đó (theo {@code iloc}, trong tệp hoặc trong {@code idat}) được ghi đè
 * bằng 0, giữ nguyên độ dài và mọi hộp khác nên tệp vẫn giải mã được. Không tìm thấy mục siêu dữ
 * liệu → trả bản sao nguyên vẹn. Cấu trúc lạ không đọc được → từ chối lưu (fail closed, REVIEW-S5
 * #4): VALIDATION_FAILED {@link #MSG_RETAKE}, không lưu bản gốc chưa gỡ.
 */
public final class HeicStripper {

  private HeicStripper() {}

  /** Thông báo cho học sinh khi không gỡ được EXIF/GPS của ảnh HEIC. */
  public static final String MSG_RETAKE =
      "Không đọc được ảnh HEIC để gỡ thông tin vị trí. Em chụp lại hoặc chọn ảnh JPEG nhé.";

  private record Box(String type, int start, int header, long size) {
    int body() {
      return start + header;
    }

    long end() {
      return start + size;
    }
  }

  public static byte[] strip(byte[] in) {
    byte[] out = in.clone();
    try {
      for (Box top : boxes(out, 0, out.length)) {
        if ("meta".equals(top.type())) {
          blankMeta(out, top);
        }
      }
    } catch (RuntimeException e) {
      // cấu trúc lạ: không chắc đã gỡ hết EXIF/GPS → không lưu bản gốc (fail closed)
      throw AppException.invalidField("pages", MSG_RETAKE);
    }
    return out;
  }

  private static List<Box> boxes(byte[] b, int from, long to) {
    List<Box> out = new ArrayList<>();
    int p = from;
    while (p + 8 <= to) {
      long size = u32(b, p);
      String type = new String(b, p + 4, 4, StandardCharsets.ISO_8859_1);
      int header = 8;
      if (size == 1) {
        size = (u32(b, p + 8) << 32) | u32(b, p + 12);
        header = 16;
      } else if (size == 0) {
        size = to - p;
      }
      if (size < header || p + size > to) {
        break;
      }
      out.add(new Box(type, p, header, size));
      p += (int) size;
    }
    return out;
  }

  private static void blankMeta(byte[] b, Box meta) {
    List<Box> kids = boxes(b, meta.body() + 4, meta.end()); // FullBox: version + flags
    Set<Long> targets = new HashSet<>();
    Box iloc = null;
    Box idat = null;
    for (Box k : kids) {
      switch (k.type()) {
        case "iinf" -> targets.addAll(metadataItems(b, k));
        case "iloc" -> iloc = k;
        case "idat" -> idat = k;
        default -> {}
      }
    }
    if (iloc == null || targets.isEmpty()) {
      return;
    }
    int p = iloc.body();
    int version = b[p] & 0xFF;
    p += 4;
    int sizes = b[p] & 0xFF;
    int offSize = sizes >> 4;
    int lenSize = sizes & 0x0F;
    int sizes2 = b[p + 1] & 0xFF;
    int baseSize = sizes2 >> 4;
    int indexSize = (version == 1 || version == 2) ? sizes2 & 0x0F : 0;
    p += 2;
    long count;
    if (version < 2) {
      count = u16(b, p);
      p += 2;
    } else {
      count = u32(b, p);
      p += 4;
    }
    for (long i = 0; i < count; i++) {
      long id;
      if (version < 2) {
        id = u16(b, p);
        p += 2;
      } else {
        id = u32(b, p);
        p += 4;
      }
      int method = 0;
      if (version == 1 || version == 2) {
        method = (int) (u16(b, p) & 0x0F);
        p += 2;
      }
      p += 2; // data_reference_index
      long base = un(b, p, baseSize);
      p += baseSize;
      int extents = (int) u16(b, p);
      p += 2;
      for (int e = 0; e < extents; e++) {
        p += indexSize;
        long off = un(b, p, offSize);
        p += offSize;
        long len = un(b, p, lenSize);
        p += lenSize;
        if (!targets.contains(id)) {
          continue;
        }
        long abs;
        if (method == 0) {
          abs = base + off;
        } else if (method == 1 && idat != null) {
          abs = idat.body() + base + off;
        } else {
          continue;
        }
        long stop = len == 0 ? b.length : Math.min(b.length, abs + len);
        for (long x = abs; x >= 0 && x < stop; x++) {
          b[(int) x] = 0;
        }
      }
    }
  }

  /** Mã các mục Exif và XMP trong {@code iinf}. */
  private static Set<Long> metadataItems(byte[] b, Box iinf) {
    Set<Long> out = new HashSet<>();
    int p = iinf.body();
    int version = b[p] & 0xFF;
    p += 4 + (version == 0 ? 2 : 4);
    for (Box infe : boxes(b, p, iinf.end())) {
      if (!"infe".equals(infe.type())) {
        continue;
      }
      int q = infe.body();
      int v = b[q] & 0xFF;
      q += 4;
      if (v < 2) {
        continue;
      }
      long id;
      if (v == 2) {
        id = u16(b, q);
        q += 2;
      } else {
        id = u32(b, q);
        q += 4;
      }
      q += 2; // item_protection_index
      String type = new String(b, q, 4, StandardCharsets.ISO_8859_1);
      q += 4;
      if ("Exif".equals(type)) {
        out.add(id);
      } else if ("mime".equals(type)) {
        int end = (int) infe.end();
        while (q < end && b[q] != 0) {
          q++; // item_name
        }
        q++;
        int s = q;
        while (q < end && b[q] != 0) {
          q++;
        }
        String ct = new String(b, s, Math.max(0, q - s), StandardCharsets.ISO_8859_1);
        if (ct.contains("rdf+xml") || ct.contains("xmp")) {
          out.add(id);
        }
      }
    }
    return out;
  }

  private static long u16(byte[] b, int p) {
    return ((b[p] & 0xFFL) << 8) | (b[p + 1] & 0xFFL);
  }

  private static long u32(byte[] b, int p) {
    return ((b[p] & 0xFFL) << 24)
        | ((b[p + 1] & 0xFFL) << 16)
        | ((b[p + 2] & 0xFFL) << 8)
        | (b[p + 3] & 0xFFL);
  }

  private static long un(byte[] b, int p, int n) {
    long v = 0;
    for (int i = 0; i < n; i++) {
      v = (v << 8) | (b[p + i] & 0xFFL);
    }
    return v;
  }
}
