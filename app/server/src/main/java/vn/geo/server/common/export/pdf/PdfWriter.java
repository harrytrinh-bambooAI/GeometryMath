package vn.geo.server.common.export.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import vn.geo.server.common.export.ExportDoc;
import vn.geo.server.common.export.ExportDoc.Block;
import vn.geo.server.common.export.ExportDoc.Heading;
import vn.geo.server.common.export.ExportDoc.Para;
import vn.geo.server.common.export.ExportDoc.Table;

/**
 * In {@link ExportDoc} ra PDF A4 bằng pdfbox (PO-77) với phông DejaVu Sans nhúng (đủ chữ tiếng
 * Việt, √, ⊥). Đầu trang và chân trang vẽ trên MỌI trang (BR-S5E1-14). Ký tự phông không có được
 * thay bằng "?" để không bao giờ hỏng cả tệp.
 */
public final class PdfWriter {

  private static final String FONT_DIR = "/net/sf/jasperreports/fonts/dejavu/";
  private static final float MARGIN = 50;
  private static final float TOP = PDRectangle.A4.getHeight() - MARGIN - 14;
  private static final float BOTTOM = MARGIN + 18;
  private static final float WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;
  private static final float BODY = 11;

  private PdfWriter() {}

  public static byte[] write(ExportDoc d) {
    try (PDDocument doc = new PDDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      new Layout(doc, load(doc, "DejaVuSans.ttf"), load(doc, "DejaVuSans-Bold.ttf")).run(d);
      doc.save(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static PDType0Font load(PDDocument doc, String name) throws IOException {
    try (InputStream in = PdfWriter.class.getResourceAsStream(FONT_DIR + name)) {
      if (in == null) {
        throw new IOException("thiếu phông " + name + " (jasperreports-fonts)");
      }
      return PDType0Font.load(doc, in);
    }
  }

  /** Dàn trang tuần tự: đoạn văn xuống dòng theo bề rộng, bảng kẻ lưới, sang trang khi hết chỗ. */
  private static final class Layout {
    private final PDDocument doc;
    private final PDType0Font font;
    private final PDType0Font bold;
    private PDPageContentStream cs;
    private float y;

    Layout(PDDocument doc, PDType0Font font, PDType0Font bold) {
      this.doc = doc;
      this.font = font;
      this.bold = bold;
    }

    void run(ExportDoc d) throws IOException {
      newPage();
      text(d.title(), bold, 15, 0, true);
      y -= 6;
      for (Block b : d.blocks()) {
        switch (b) {
          case Heading h -> {
            y -= 4;
            text(h.text(), bold, h.level() == 1 ? 13 : 12, 0, false);
          }
          case Para p -> text(p.text(), p.bold() ? bold : font, BODY, 0, false);
          case Table t -> table(t);
        }
      }
      cs.close();
      int n = doc.getNumberOfPages();
      for (int i = 0; i < n; i++) {
        PDPage page = doc.getPage(i);
        try (PDPageContentStream c =
            new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true)) {
          if (d.header() != null) {
            String h = safe(bold, d.header());
            float w = bold.getStringWidth(h) / 1000 * 10;
            show(
                c,
                bold,
                10,
                PDRectangle.A4.getWidth() - MARGIN - w,
                PDRectangle.A4.getHeight() - 32,
                h);
          }
          String f =
              (d.footer() == null ? "" : d.footer() + "   ·   ") + "Trang " + (i + 1) + "/" + n;
          f = safe(font, f);
          float w = font.getStringWidth(f) / 1000 * 9;
          show(c, font, 9, (PDRectangle.A4.getWidth() - w) / 2, 30, f);
        }
      }
    }

    private void newPage() throws IOException {
      if (cs != null) {
        cs.close();
      }
      PDPage page = new PDPage(PDRectangle.A4);
      doc.addPage(page);
      cs = new PDPageContentStream(doc, page);
      y = TOP;
    }

    private void text(String s, PDType0Font f, float size, float indent, boolean center)
        throws IOException {
      float lead = size * 1.35f;
      for (String line : wrap(f, size, s, WIDTH - indent)) {
        if (y - lead < BOTTOM) {
          newPage();
        }
        y -= lead;
        float x = MARGIN + indent;
        if (center) {
          x = MARGIN + (WIDTH - f.getStringWidth(line) / 1000 * size) / 2;
        }
        show(cs, f, size, x, y, line);
      }
    }

    private void table(Table t) throws IOException {
      int cols = t.head().size();
      float[] w = widths(t, cols);
      row(t.head(), w, bold);
      for (List<String> r : t.rows()) {
        row(r, w, font);
      }
      y -= 6;
    }

    /** Bề rộng cột theo độ dài chữ dài nhất (tối thiểu 8 %, tổng = bề rộng trang). */
    private static float[] widths(Table t, int cols) {
      float[] len = new float[cols];
      List<List<String>> all = new ArrayList<>(t.rows());
      all.add(t.head());
      for (List<String> r : all) {
        for (int c = 0; c < cols && c < r.size(); c++) {
          String s = r.get(c) == null ? "" : r.get(c);
          len[c] = Math.max(len[c], Math.min(60, s.length()) + 3);
        }
      }
      float sum = 0;
      for (float l : len) {
        sum += l;
      }
      float[] w = new float[cols];
      float min = WIDTH * 0.08f;
      float extra = 0;
      for (int c = 0; c < cols; c++) {
        w[c] = Math.max(min, WIDTH * len[c] / sum);
        extra += w[c];
      }
      for (int c = 0; c < cols; c++) {
        w[c] = w[c] * WIDTH / extra;
      }
      return w;
    }

    private void row(List<String> cells, float[] w, PDType0Font f) throws IOException {
      float size = 9.5f;
      float lead = size * 1.3f;
      List<List<String>> lines = new ArrayList<>();
      int max = 1;
      for (int c = 0; c < w.length; c++) {
        String s = c < cells.size() && cells.get(c) != null ? cells.get(c) : "";
        List<String> l = wrap(f, size, s, w[c] - 6);
        lines.add(l);
        max = Math.max(max, l.size());
      }
      float h = max * lead + 6;
      if (y - h < BOTTOM) {
        newPage();
      }
      float x = MARGIN;
      for (int c = 0; c < w.length; c++) {
        cs.addRect(x, y - h, w[c], h);
        float ty = y - 3;
        for (String line : lines.get(c)) {
          ty -= lead;
          show(cs, f, size, x + 3, ty + 2, line);
        }
        x += w[c];
      }
      cs.setLineWidth(0.5f);
      cs.stroke();
      y -= h;
    }

    private List<String> wrap(PDType0Font f, float size, String s, float width) throws IOException {
      List<String> out = new ArrayList<>();
      for (String para : (s == null ? "" : s).split("\n", -1)) {
        String p = safe(f, para.replace('\t', ' '));
        StringBuilder line = new StringBuilder();
        for (String word : p.split(" ", -1)) {
          String cand = line.isEmpty() ? word : line + " " + word;
          if (f.getStringWidth(cand) / 1000 * size <= width || line.isEmpty()) {
            line.setLength(0);
            line.append(cand);
            // một từ quá dài: cắt theo ký tự
            while (f.getStringWidth(line.toString()) / 1000 * size > width && line.length() > 1) {
              int cut = line.length() - 1;
              while (cut > 1 && f.getStringWidth(line.substring(0, cut)) / 1000 * size > width) {
                cut--;
              }
              out.add(line.substring(0, cut));
              line.delete(0, cut);
            }
          } else {
            out.add(line.toString());
            line.setLength(0);
            line.append(word);
          }
        }
        out.add(line.toString());
      }
      return out;
    }
  }

  private static void show(
      PDPageContentStream c, PDType0Font f, float size, float x, float y, String s)
      throws IOException {
    c.beginText();
    c.setFont(f, size);
    c.newLineAtOffset(x, y);
    c.showText(s);
    c.endText();
  }

  /** Thay ký tự phông không có (và ký tự điều khiển) bằng "?". */
  static String safe(PDType0Font f, String s) {
    StringBuilder b = new StringBuilder(s.length());
    s.codePoints()
        .forEach(cp -> {
          String ch = new String(Character.toChars(cp));
          if (Character.isISOControl(cp)) {
            b.append(' ');
            return;
          }
          try {
            f.encode(ch);
            b.append(ch);
          } catch (IOException | IllegalArgumentException e) {
            b.append('?');
          }
        });
    return b.toString();
  }
}
