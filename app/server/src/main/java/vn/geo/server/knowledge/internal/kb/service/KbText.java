package vn.geo.server.knowledge.internal.kb.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

/**
 * Đọc chữ của tệp tải lên thành Markdown có tiêu đề (BR-S5E3-03): docx (kiểu đoạn Heading n / Title
 * → {@code #}), pdf (lớp chữ; dòng ngắn dạng "1.", "I.", "Bài", "Mục", "Phần", "Chương" coi là tiêu
 * đề), md (UTF-8). Loại tệp nhận theo đuôi VÀ nội dung; sai = {@link Rejected} (MSG-S5-22). Không
 * chạy mã nào trong tệp.
 */
public final class KbText {

  private KbText() {}

  /** Tệp bị từ chối kèm câu báo. */
  public static final class Rejected extends RuntimeException {
    public Rejected(String message) {
      super(message);
    }
  }

  public static final String BROKEN = "Không mở được tệp.";
  public static final String NO_TEXT = "Tệp không có chữ để đưa vào kho.";

  static final Pattern PDF_HEADING =
      Pattern.compile(
          "^(?:[IVX]{1,5}\\.|\\d{1,2}(?:\\.\\d{1,2})*\\.?|Bài|Mục|Phần|Chương|Hoạt động)\\s+\\S.*$");

  /** Đuôi tệp được nhận (chữ thường) hoặc null. */
  public static String extension(String fileName) {
    if (fileName == null) {
      return null;
    }
    int dot = fileName.lastIndexOf('.');
    String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    return switch (ext) {
      case "docx", "pdf", "md" -> ext;
      default -> null;
    };
  }

  public static String mime(String ext) {
    return switch (ext) {
      case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case "pdf" -> "application/pdf";
      default -> "text/markdown; charset=utf-8";
    };
  }

  public static String toMarkdown(String ext, byte[] bytes, String sizeOrTypeMessage) {
    String md =
        switch (ext) {
          case "docx" -> {
            if (!isZip(bytes)) {
              throw new Rejected(sizeOrTypeMessage);
            }
            yield docx(bytes);
          }
          case "pdf" -> {
            if (!(bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D')) {
              throw new Rejected(sizeOrTypeMessage);
            }
            yield pdf(bytes);
          }
          case "md" -> utf8(bytes, sizeOrTypeMessage);
          default -> throw new Rejected(sizeOrTypeMessage);
        };
    if (md.isBlank()) {
      throw new Rejected(NO_TEXT);
    }
    return md;
  }

  private static boolean isZip(byte[] b) {
    return b.length > 4 && b[0] == 'P' && b[1] == 'K' && b[2] == 3 && b[3] == 4;
  }

  private static String utf8(byte[] bytes, String msg) {
    try {
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes))
          .toString()
          .replace("﻿", "");
    } catch (CharacterCodingException e) {
      throw new Rejected(msg);
    }
  }

  static String docx(byte[] bytes) {
    StringBuilder out = new StringBuilder();
    try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
      for (IBodyElement el : doc.getBodyElements()) {
        if (el instanceof XWPFParagraph p) {
          String text = p.getText().replaceAll("[\\s\\u00A0]+", " ").strip();
          if (text.isEmpty()) {
            continue;
          }
          int level = headingLevel(p.getStyle());
          if (level > 0) {
            out.append("\n").append("#".repeat(level)).append(' ').append(text).append("\n\n");
          } else {
            out.append(text).append("\n\n");
          }
        } else if (el instanceof XWPFTable t) {
          for (XWPFTableRow row : t.getRows()) {
            StringBuilder line = new StringBuilder("|");
            for (XWPFTableCell c : row.getTableCells()) {
              line.append(' ').append(c.getText().replaceAll("\\s+", " ").strip()).append(" |");
            }
            out.append(line).append('\n');
          }
          out.append('\n');
        }
      }
    } catch (IOException | RuntimeException e) {
      throw new Rejected(BROKEN);
    }
    return out.toString().strip();
  }

  static int headingLevel(String style) {
    if (style == null) {
      return 0;
    }
    String s = style.toLowerCase(Locale.ROOT).replace(" ", "");
    if (s.equals("title")) {
      return 1;
    }
    if (s.startsWith("heading") && s.length() == 8 && Character.isDigit(s.charAt(7))) {
      int n = s.charAt(7) - '0';
      return n >= 1 && n <= 6 ? n : 0;
    }
    return 0;
  }

  static String pdf(byte[] bytes) {
    String text;
    try (PDDocument doc = Loader.loadPDF(bytes)) {
      if (doc.isEncrypted()) {
        throw new Rejected(BROKEN);
      }
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setSortByPosition(true);
      text = stripper.getText(doc);
    } catch (IOException | RuntimeException e) {
      if (e instanceof Rejected r) {
        throw r;
      }
      throw new Rejected(BROKEN);
    }
    StringBuilder out = new StringBuilder();
    for (String line : text.split("\\R")) {
      String s = line.replaceAll("[\\s\\u00A0]+", " ").strip();
      if (s.isEmpty()) {
        continue;
      }
      if (s.length() <= 80 && PDF_HEADING.matcher(s).matches()) {
        out.append("\n## ").append(s).append("\n\n");
      } else {
        out.append(s).append('\n');
      }
    }
    return out.toString().strip();
  }
}
