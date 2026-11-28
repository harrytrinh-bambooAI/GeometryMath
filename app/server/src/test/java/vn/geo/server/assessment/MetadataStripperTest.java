package vn.geo.server.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.assessment.internal.storage.service.ImageStoreService;
import vn.geo.server.assessment.internal.storage.service.MetadataStripper;
import vn.geo.server.common.error.AppException;

/** US-S5E4-08 (BR-S5E4-27): gỡ EXIF/XMP khỏi JPEG/PNG; năm học theo giờ Việt Nam (BR-S5E4-28). */
class MetadataStripperTest {

  private static byte[] seg(int marker, byte[] payload) {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.write(0xFF);
    o.write(marker);
    int len = payload.length + 2;
    o.write(len >> 8);
    o.write(len & 0xFF);
    o.writeBytes(payload);
    return o.toByteArray();
  }

  private static byte[] jpeg() {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xD8});
    o.writeBytes(seg(0xE0, "JFIF\0\1\1".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xE1, "Exif\0\0GPS 21.0285 iPhone".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xE1, "http://ns.adobe.com/xap/1.0/".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xFE, "comment".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xDB, new byte[] {0, 1, 2}));
    o.writeBytes(seg(0xDA, new byte[] {0, 1}));
    o.writeBytes(new byte[] {0x11, 0x22, (byte) 0xFF, 0x00, 0x33, (byte) 0xFF, (byte) 0xD9});
    return o.toByteArray();
  }

  @Test
  @DisplayName(
      "US-S5E4-08 JPEG: bỏ APP1 (EXIF, XMP) và COM, giữ JFIF, bảng lượng tử và dữ liệu ảnh")
  void jpegLosesExif() {
    MetadataStripper.Clean c = MetadataStripper.strip(jpeg());
    String s = new String(c.bytes(), StandardCharsets.ISO_8859_1);
    assertThat(c.contentType()).isEqualTo("image/jpeg");
    assertThat(s).doesNotContain("Exif").doesNotContain("iPhone").doesNotContain("adobe");
    assertThat(s).doesNotContain("comment").contains("JFIF");
    assertThat(c.bytes()[c.bytes().length - 1]).isEqualTo((byte) 0xD9);
    // gỡ lần hai không đổi
    assertThat(MetadataStripper.strip(c.bytes()).bytes()).isEqualTo(c.bytes());
  }

  @Test
  @DisplayName("TC-S5E4-40 (REVIEW-S5 #3) JPEG: bỏ mọi byte sau EOI (MPF/ảnh thu nhỏ kèm EXIF/GPS)")
  void jpegDropsTrailerAfterEoi() {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.writeBytes(jpeg());
    // ảnh thứ hai (MPF) của điện thoại sau EOI, mang EXIF/GPS riêng
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xD8});
    o.writeBytes(seg(0xE1, "Exif\0\0GPS 21.0285,105.8542".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xDA, new byte[] {0, 1}));
    o.writeBytes(new byte[] {0x44, (byte) 0xFF, (byte) 0xD9});
    MetadataStripper.Clean c = MetadataStripper.strip(o.toByteArray());
    String s = new String(c.bytes(), StandardCharsets.ISO_8859_1);
    assertThat(s).doesNotContain("Exif").doesNotContain("21.0285");
    assertThat(c.bytes()).isEqualTo(MetadataStripper.strip(jpeg()).bytes());
  }

  @Test
  @DisplayName("TC-S5E4-40 JPEG lũy tiến: nhiều SOS, RSTn, DHT và APPn giữa các lượt quét")
  void jpegProgressiveScans() {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.writeBytes(new byte[] {(byte) 0xFF, (byte) 0xD8});
    o.writeBytes(seg(0xE0, "JFIF\0\1\1".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xDA, new byte[] {0, 1}));
    o.writeBytes(new byte[] {0x11, (byte) 0xFF, (byte) 0xD0, 0x12, (byte) 0xFF, 0x00});
    o.writeBytes(seg(0xC4, new byte[] {7, 7}));
    o.writeBytes(seg(0xE1, "Exif\0\0GPS".getBytes(StandardCharsets.ISO_8859_1)));
    o.writeBytes(seg(0xDA, new byte[] {0, 2}));
    o.writeBytes(new byte[] {0x21, (byte) 0xFF, (byte) 0xFF, (byte) 0xD9});
    o.writeBytes("trailer GPS".getBytes(StandardCharsets.ISO_8859_1));
    byte[] out = MetadataStripper.strip(o.toByteArray()).bytes();
    String s = new String(out, StandardCharsets.ISO_8859_1);
    assertThat(s).doesNotContain("Exif").doesNotContain("trailer");
    assertThat(out[out.length - 1]).isEqualTo((byte) 0xD9);
    assertThat(out[out.length - 2]).isEqualTo((byte) 0xFF);
    // giữ cả hai lượt quét và bảng Huffman giữa chúng
    assertThat(out.length).isEqualTo(o.size() - 13 - 11);
  }

  @Test
  @DisplayName("US-S5E4-08 PNG: chỉ giữ khúc hiển thị, bỏ eXIf/tEXt")
  void pngLosesText() {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    o.writeBytes(new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
    for (String[] ch :
        new String[][] {
          {"IHDR", "0123456789abc"},
          {"tEXt", "Author Binh An"},
          {"eXIf", "GPS"},
          {"IDAT", "xx"},
          {"IEND", ""}
        }) {
      byte[] d = ch[1].getBytes(StandardCharsets.ISO_8859_1);
      o.writeBytes(new byte[] {0, 0, 0, (byte) d.length});
      o.writeBytes(ch[0].getBytes(StandardCharsets.ISO_8859_1));
      o.writeBytes(d);
      o.writeBytes(new byte[] {0, 0, 0, 0});
    }
    String s =
        new String(MetadataStripper.strip(o.toByteArray()).bytes(), StandardCharsets.ISO_8859_1);
    assertThat(s).contains("IHDR").contains("IDAT").contains("IEND");
    assertThat(s).doesNotContain("Binh An").doesNotContain("eXIf");
  }

  @Test
  @DisplayName("US-S5E4-08 định dạng lạ bị từ chối")
  void rejectsOther() {
    assertThatThrownBy(
            () -> MetadataStripper.strip("GIF89a-----".getBytes(StandardCharsets.US_ASCII)))
        .isInstanceOf(AppException.class);
  }

  @Test
  @DisplayName("BR-S5E4-28 năm học: từ 01/08 giờ Việt Nam sang năm học mới")
  void schoolYear() {
    assertThat(ImageStoreService.schoolYear(Instant.parse("2026-12-11T13:00:00Z")))
        .isEqualTo("2026-2027");
    assertThat(ImageStoreService.schoolYear(Instant.parse("2027-07-31T16:59:00Z")))
        .isEqualTo("2026-2027");
    assertThat(ImageStoreService.schoolYear(Instant.parse("2027-07-31T17:00:00Z")))
        .isEqualTo("2027-2028");
  }
}
