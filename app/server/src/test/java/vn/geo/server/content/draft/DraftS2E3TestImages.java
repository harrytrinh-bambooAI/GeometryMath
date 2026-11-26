package vn.geo.server.content.draft;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Ảnh JPEG mẫu cho kiểm thử S2-E3: có EXIF chứa GPS và dấu đề của AI giả lập trong chú thích. */
public final class DraftS2E3TestImages {

  private DraftS2E3TestImages() {}

  public static byte[] jpegWithGps(String marker) {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xD8});
    byte[] jfif = "JFIF\0".getBytes(StandardCharsets.ISO_8859_1);
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xE0, 0, (byte) (jfif.length + 2)});
    o.writeBytes(jfif);
    byte[] exif = "Exif\0\0GPSLatitude=21.0285".getBytes(StandardCharsets.ISO_8859_1);
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xE1, 0, (byte) (exif.length + 2)});
    o.writeBytes(exif);
    byte[] com = ("GEO-S2E3:" + marker).getBytes(StandardCharsets.ISO_8859_1);
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xFE, 0, (byte) (com.length + 2)});
    o.writeBytes(com);
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xDA, 0, 2, 1, 2, 3, (byte) 0xFF, (byte) 0xD9});
    return o.toByteArray();
  }
}
