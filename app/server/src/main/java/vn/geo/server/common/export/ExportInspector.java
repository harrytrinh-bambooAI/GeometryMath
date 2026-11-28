package vn.geo.server.common.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

/**
 * Đọc lại tệp xuất (DOCX, PDF, ZIP) thành chữ để kiểm thử: mở được hay không, chữ từng trang, chữ
 * đầu/chân trang, các bảng. Dùng ở móc kiểm thử (profile dev/test) và test đơn vị.
 */
public final class ExportInspector {

  private ExportInspector() {}

  public static Map<String, Object> inspect(String name, byte[] bytes) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("name", name);
    m.put("size", bytes.length);
    try {
      if (bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P') {
        pdf(bytes, m);
      } else if (bytes.length > 4 && bytes[0] == 'P' && bytes[1] == 'K') {
        if (isDocx(bytes)) {
          docx(bytes, m);
        } else {
          zip(bytes, m);
        }
      } else {
        m.put("kind", "unknown");
      }
    } catch (IOException | RuntimeException e) {
      m.put("kind", "broken");
      m.put("error", e.getClass().getSimpleName());
    }
    return m;
  }

  private static boolean isDocx(byte[] bytes) throws IOException {
    try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(bytes))) {
      for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
        if ("word/document.xml".equals(e.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  private static void pdf(byte[] bytes, Map<String, Object> m) throws IOException {
    try (PDDocument doc = Loader.loadPDF(bytes)) {
      m.put("kind", "pdf");
      PDFTextStripper s = new PDFTextStripper();
      List<String> pages = new ArrayList<>();
      for (int i = 1; i <= doc.getNumberOfPages(); i++) {
        s.setStartPage(i);
        s.setEndPage(i);
        pages.add(s.getText(doc));
      }
      m.put("pages", pages);
      m.put("text", String.join("\n", pages));
    }
  }

  private static void docx(byte[] bytes, Map<String, Object> m) throws IOException {
    try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
      m.put("kind", "docx");
      List<String> paragraphs = new ArrayList<>();
      List<List<List<String>>> tables = new ArrayList<>();
      StringBuilder text = new StringBuilder();
      for (IBodyElement el : doc.getBodyElements()) {
        if (el instanceof XWPFParagraph p) {
          paragraphs.add(p.getText());
          text.append(p.getText()).append('\n');
        } else if (el instanceof XWPFTable t) {
          List<List<String>> rows = new ArrayList<>();
          t.getRows()
              .forEach(r -> rows.add(r.getTableCells().stream().map(c -> c.getText()).toList()));
          tables.add(rows);
          rows.forEach(r -> text.append(String.join(" | ", r)).append('\n'));
        }
      }
      m.put("paragraphs", paragraphs);
      m.put("tables", tables);
      m.put("text", text.toString());
      m.put("headers", doc.getHeaderList().stream().map(XWPFHeader::getText).toList());
      m.put("footers", doc.getFooterList().stream().map(XWPFFooter::getText).toList());
    }
  }

  private static void zip(byte[] bytes, Map<String, Object> m) throws IOException {
    m.put("kind", "zip");
    List<Map<String, Object>> entries = new ArrayList<>();
    try (ZipInputStream z =
        new ZipInputStream(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
      for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
        entries.add(inspect(e.getName(), z.readAllBytes()));
      }
    }
    m.put("entries", entries);
  }
}
