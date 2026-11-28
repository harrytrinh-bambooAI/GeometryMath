package vn.geo.server.content.importer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** S2-E4: tệp docx / pdf mẫu dựng trong bộ nhớ cho ca kiểm import (không dùng tệp thật). */
public final class ImportTestFiles {

  private ImportTestFiles() {}

  public static final String FX10 = "Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.";
  public static final String FX11 = "Tam giác ABC cân tại A, góc A = 40°; tính góc B.";
  public static final String FX12 = "Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC.";

  /** Tệp T của các ca đối chiếu: 3 bài tập + 1 đoạn lý thuyết. */
  public static final List<String> THREE =
      List.of(
          "Lý thuyết: Định lý Pythagore đảo.",
          "Nếu bình phương một cạnh bằng tổng bình phương hai cạnh kia thì tam giác vuông.",
          "Bài tập",
          "Bài 1. " + FX10,
          "Bài 2. " + FX11,
          "Bài 3. " + FX12);

  private static String esc(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  public static String documentXml(List<String> paragraphs) {
    StringBuilder sb =
        new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
    for (String p : paragraphs) {
      sb.append("<w:p><w:r><w:t xml:space=\"preserve\">")
          .append(esc(p))
          .append("</w:t></w:r></w:p>");
    }
    return sb.append("</w:body></w:document>").toString();
  }

  /**
   * docx tối thiểu; {@code macro} thêm word/vbaProject.bin (macro tự chạy, không bao giờ được nạp).
   */
  public static byte[] docx(List<String> paragraphs, boolean macro) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (ZipOutputStream zip = new ZipOutputStream(out)) {
        put(zip, "[Content_Types].xml", "<Types/>".getBytes(StandardCharsets.UTF_8));
        put(zip, "word/document.xml", documentXml(paragraphs).getBytes(StandardCharsets.UTF_8));
        if (macro) {
          put(
              zip,
              "word/vbaProject.bin",
              "Sub AutoOpen()\nShell \"calc\"\nEnd Sub".getBytes(StandardCharsets.UTF_8));
        }
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /** Tệp xlsx tối thiểu (zip có xl/workbook.xml). */
  public static byte[] xlsx() {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (ZipOutputStream zip = new ZipOutputStream(out)) {
        put(zip, "[Content_Types].xml", "<Types/>".getBytes(StandardCharsets.UTF_8));
        put(zip, "xl/workbook.xml", "<workbook/>".getBytes(StandardCharsets.UTF_8));
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void put(ZipOutputStream zip, String name, byte[] data) throws IOException {
    ZipEntry e = new ZipEntry(name);
    e.setMethod(ZipEntry.STORED);
    e.setSize(data.length);
    e.setCompressedSize(data.length);
    CRC32 crc = new CRC32();
    crc.update(data);
    e.setCrc(crc.getValue());
    zip.putNextEntry(e);
    zip.write(data);
    zip.closeEntry();
  }

  /**
   * pdf có lớp chữ (mỗi phần tử một dòng; chỉ ký tự Latin-1); {@code password} khác null = đặt mật
   * khẩu mở.
   */
  public static byte[] pdf(List<String> lines, String password) {
    try (PDDocument doc = new PDDocument()) {
      PDPage page = new PDPage();
      doc.addPage(page);
      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        if (!lines.isEmpty()) {
          cs.beginText();
          cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
          cs.setLeading(16);
          cs.newLineAtOffset(50, 740);
          for (String l : lines) {
            cs.showText(l);
            cs.newLine();
          }
          cs.endText();
        } else {
          // "ảnh quét": trang chỉ có hình vẽ, không có lớp chữ
          cs.addRect(60, 500, 300, 200);
          cs.fill();
        }
      }
      if (password != null) {
        StandardProtectionPolicy policy =
            new StandardProtectionPolicy("chu-" + password, password, new AccessPermission());
        policy.setEncryptionKeyLength(128);
        doc.protect(policy);
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      doc.save(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
