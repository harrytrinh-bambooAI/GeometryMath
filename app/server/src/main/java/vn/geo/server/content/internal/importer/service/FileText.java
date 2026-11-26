package vn.geo.server.content.internal.importer.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * S2-E4 (US-S2E4-02, BR-S2E4-07, 10): đọc CHỮ của tệp docx / pdf thành danh sách đoạn văn. Hàm
 * thuần, không chạy bất kỳ mã nào trong tệp:
 *
 * <ul>
 *   <li>Loại tệp nhận theo NỘI DUNG (không tin đuôi tệp): {@code %PDF} = pdf; zip có {@code
 *       word/document.xml} = docx; zip có {@code xl/…} (xlsx) hoặc loại khác = "Chỉ nhận docx và
 *       pdf".
 *   <li>docx: chỉ giải nén và đọc XML của {@code word/document.xml} (StAX, tắt DTD và thực thể
 *       ngoài; trần 30 MB sau giải nén). Macro ({@code vbaProject.bin}) không bao giờ được nạp hay
 *       chạy — chỉ ghi nhận để báo "Tệp có macro, đã bỏ qua".
 *   <li>pdf: PDFBox lấy lớp chữ; không có chữ nào (ảnh quét) = từ chối kèm hướng dẫn "Tải ảnh"; đặt
 *       mật khẩu = từ chối.
 *   <li>Tệp hỏng (zip / pdf không mở được, docx thiếu document.xml) = "Không mở được tệp". Docx đặt
 *       mật khẩu là tệp OLE (không phải zip) = "Tệp đặt mật khẩu…".
 * </ul>
 */
public final class FileText {

  private FileText() {}

  public static final String ONLY_DOCX_PDF = "Chỉ nhận docx và pdf";
  public static final String BROKEN = "Không mở được tệp";
  public static final String PASSWORD = "Tệp đặt mật khẩu, hãy bỏ mật khẩu rồi tải lại";
  public static final String NO_TEXT_LAYER =
      "Tệp không có lớp chữ. Hãy dùng Tải ảnh cho từng trang";
  public static final String MACRO = "Tệp có macro, đã bỏ qua";

  /** Trần chữ đọc từ document.xml sau giải nén (chống bom nén). */
  static final int MAX_XML_BYTES = 30 * 1024 * 1024;

  /** Tệp bị từ chối kèm câu báo cho giáo viên. */
  public static final class Rejected extends RuntimeException {
    public Rejected(String message) {
      super(message);
    }
  }

  /** Chữ đọc được: type = docx | pdf; paragraphs = các đoạn theo thứ tự; macro = docx có macro. */
  public record Extracted(String type, List<String> paragraphs, boolean macro) {}

  /** Đuôi tệp có thuộc loại nhận không (kiểm sớm lúc tạo lượt import). */
  public static boolean acceptedName(String fileName) {
    String n = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
    return n.endsWith(".docx") || n.endsWith(".pdf");
  }

  private static boolean startsWith(byte[] b, int... magic) {
    if (b.length < magic.length) {
      return false;
    }
    for (int i = 0; i < magic.length; i++) {
      if ((b[i] & 0xff) != magic[i]) {
        return false;
      }
    }
    return true;
  }

  private static boolean isPdf(byte[] b) {
    String head = new String(b, 0, Math.min(b.length, 1024), StandardCharsets.ISO_8859_1);
    return head.contains("%PDF-");
  }

  public static Extracted extract(String fileName, byte[] bytes) {
    String name = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
    if (isPdf(bytes)) {
      return new Extracted("pdf", pdf(bytes), false);
    }
    if (startsWith(bytes, 'P', 'K', 3, 4)) {
      return docx(name, bytes);
    }
    if (startsWith(bytes, 0xD0, 0xCF, 0x11, 0xE0) && name.endsWith(".docx")) {
      throw new Rejected(PASSWORD); // docx mã hóa là tệp OLE bọc ngoài
    }
    throw new Rejected(acceptedName(name) ? BROKEN : ONLY_DOCX_PDF);
  }

  // ---------------------------------------------------------------- docx

  private static Extracted docx(String name, byte[] bytes) {
    byte[] xml = null;
    boolean macro = false;
    boolean sheet = false;
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
      ZipEntry e;
      while ((e = zip.getNextEntry()) != null) {
        String entry = e.getName();
        String lower = entry.toLowerCase(Locale.ROOT);
        if (lower.endsWith("vbaproject.bin")) {
          macro = true;
        } else if (lower.startsWith("xl/")) {
          sheet = true;
        } else if (entry.equals("word/document.xml")) {
          xml = readCapped(zip);
        }
      }
    } catch (IOException | RuntimeException ex) {
      if (xml == null) {
        throw new Rejected(name.endsWith(".docx") ? BROKEN : ONLY_DOCX_PDF);
      }
    }
    if (xml == null) {
      throw new Rejected(sheet || !name.endsWith(".docx") ? ONLY_DOCX_PDF : BROKEN);
    }
    try {
      return new Extracted("docx", paragraphs(xml), macro);
    } catch (XMLStreamException | RuntimeException ex) {
      throw new Rejected(BROKEN);
    }
  }

  private static byte[] readCapped(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[16 * 1024];
    int n;
    while ((n = in.read(buf)) > 0) {
      if (out.size() + n > MAX_XML_BYTES) {
        throw new IOException("document.xml quá lớn");
      }
      out.write(buf, 0, n);
    }
    return out.toByteArray();
  }

  /**
   * Các đoạn văn (w:p) của document.xml; w:tab = dấu cách, w:br / w:cr = xuống dòng thành đoạn mới.
   */
  static List<String> paragraphs(byte[] xml) throws XMLStreamException {
    XMLInputFactory f = XMLInputFactory.newFactory();
    f.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    f.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
    XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(xml));
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inText = false;
    try {
      while (r.hasNext()) {
        int ev = r.next();
        if (ev == XMLStreamConstants.START_ELEMENT) {
          switch (r.getLocalName()) {
            case "t" -> inText = true;
            case "tab" -> cur.append(' ');
            case "br", "cr" -> flush(out, cur);
            default -> {
              // phần tử khác: bỏ qua
            }
          }
        } else if (ev == XMLStreamConstants.END_ELEMENT) {
          switch (r.getLocalName()) {
            case "t" -> inText = false;
            case "p" -> flush(out, cur);
            default -> {
              // bỏ qua
            }
          }
        } else if (inText
            && (ev == XMLStreamConstants.CHARACTERS || ev == XMLStreamConstants.CDATA)) {
          cur.append(r.getText());
        }
      }
    } finally {
      r.close();
    }
    flush(out, cur);
    return out;
  }

  private static void flush(List<String> out, StringBuilder cur) {
    String s = cur.toString().replaceAll("[\\s\\u00A0]+", " ").strip();
    if (!s.isEmpty()) {
      out.add(s);
    }
    cur.setLength(0);
  }

  // ---------------------------------------------------------------- pdf

  private static List<String> pdf(byte[] bytes) {
    String text;
    try (PDDocument doc = Loader.loadPDF(bytes)) {
      if (doc.isEncrypted() && !doc.getCurrentAccessPermission().canExtractContent()) {
        throw new Rejected(PASSWORD);
      }
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setSortByPosition(true);
      text = stripper.getText(doc);
    } catch (InvalidPasswordException e) {
      throw new Rejected(PASSWORD);
    } catch (IOException | RuntimeException e) {
      if (e instanceof Rejected r) {
        throw r;
      }
      throw new Rejected(BROKEN);
    }
    List<String> out = new ArrayList<>();
    for (String line : text.split("\\R")) {
      String s = line.replaceAll("[\\s\\u00A0]+", " ").strip();
      if (!s.isEmpty()) {
        out.add(s);
      }
    }
    if (out.isEmpty()) {
      throw new Rejected(NO_TEXT_LAYER);
    }
    return out;
  }
}
