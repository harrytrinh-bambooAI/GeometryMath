package vn.geo.server.common.export.docx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import vn.geo.server.common.export.ExportDoc;
import vn.geo.server.common.export.ExportDoc.Block;
import vn.geo.server.common.export.ExportDoc.Heading;
import vn.geo.server.common.export.ExportDoc.Para;
import vn.geo.server.common.export.ExportDoc.Table;

/**
 * In {@link ExportDoc} ra DOCX bằng Apache POI (QS5-05): phông Times New Roman 13, tiêu đề đậm,
 * bảng có lưới; đầu trang / chân trang mặc định áp cho mọi trang.
 */
public final class DocxWriter {

  private static final String FONT = "Times New Roman";

  private DocxWriter() {}

  public static byte[] write(ExportDoc d) {
    try (XWPFDocument doc = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      if (d.header() != null) {
        XWPFParagraph p = doc.createHeader(HeaderFooterType.DEFAULT).createParagraph();
        p.setAlignment(ParagraphAlignment.RIGHT);
        run(p, d.header(), true, 11);
      }
      if (d.footer() != null) {
        XWPFParagraph p = doc.createFooter(HeaderFooterType.DEFAULT).createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        run(p, d.footer(), false, 10).setItalic(true);
      }
      XWPFParagraph title = doc.createParagraph();
      title.setAlignment(ParagraphAlignment.CENTER);
      run(title, d.title(), true, 15);
      for (Block b : d.blocks()) {
        switch (b) {
          case Heading h -> run(doc.createParagraph(), h.text(), true, h.level() == 1 ? 14 : 13);
          case Para p -> run(doc.createParagraph(), p.text(), p.bold(), 13);
          case Table t -> table(doc, t);
        }
      }
      doc.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void table(XWPFDocument doc, Table t) {
    int cols = t.head().size();
    XWPFTable table = doc.createTable(t.rows().size() + 1, cols);
    table.setWidth("100%");
    fill(table, 0, t.head(), true);
    for (int i = 0; i < t.rows().size(); i++) {
      fill(table, i + 1, t.rows().get(i), false);
    }
    doc.createParagraph();
  }

  private static void fill(XWPFTable table, int row, List<String> cells, boolean bold) {
    for (int c = 0; c < cells.size(); c++) {
      XWPFTableCell cell = table.getRow(row).getCell(c);
      if (cell == null) {
        continue;
      }
      XWPFParagraph p = cell.getParagraphs().get(0);
      run(p, cells.get(c) == null ? "" : cells.get(c), bold, 12);
    }
  }

  private static XWPFRun run(XWPFParagraph p, String text, boolean bold, int size) {
    XWPFRun r = p.createRun();
    r.setFontFamily(FONT);
    r.setFontSize(size);
    r.setBold(bold);
    String[] lines = text.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        r.addBreak();
      }
      r.setText(lines[i], i);
    }
    return r;
  }
}
