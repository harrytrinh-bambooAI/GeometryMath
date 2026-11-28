package vn.geo.server.common.export;

import java.util.ArrayList;
import java.util.List;

/**
 * Mô hình tài liệu xuất dùng chung (T-261128-54, PLAN_code-s5 P2): giáo án (US-S5E1-05) và mã đề
 * (US-S5E2-05) dựng một {@code ExportDoc} rồi in ra DOCX ({@code docx.DocxWriter}, Apache POI) hoặc
 * PDF ({@code pdf.PdfWriter}, pdfbox). {@code header} in ở đầu mọi trang (ví dụ "Mã đề 101"),
 * {@code footer} ở chân mọi trang (ví dụ "Bản nháp AI soạn – chưa duyệt", BR-S5E1-14); null = không
 * có.
 */
public record ExportDoc(String title, List<Block> blocks, String header, String footer) {

  public ExportDoc {
    blocks = List.copyOf(blocks);
  }

  /** Một khối nội dung. */
  public sealed interface Block permits Heading, Para, Table {}

  /** Tiêu đề mục; {@code level} 1 = mục lớn (I, II…), 2 = mục con. */
  public record Heading(String text, int level) implements Block {}

  /** Đoạn văn; xuống dòng trong {@code text} giữ nguyên. */
  public record Para(String text, boolean bold) implements Block {}

  /** Bảng có hàng tiêu đề. */
  public record Table(List<String> head, List<List<String>> rows) implements Block {
    public Table {
      head = List.copyOf(head);
      rows = rows.stream().map(List::copyOf).toList();
    }
  }

  /** Dựng tài liệu từng khối. */
  public static Builder builder(String title) {
    return new Builder(title);
  }

  /** Bộ dựng {@link ExportDoc}. */
  public static final class Builder {
    private final String title;
    private final List<Block> blocks = new ArrayList<>();
    private String header;
    private String footer;

    private Builder(String title) {
      this.title = title;
    }

    public Builder heading(String text) {
      blocks.add(new Heading(text, 1));
      return this;
    }

    public Builder subheading(String text) {
      blocks.add(new Heading(text, 2));
      return this;
    }

    public Builder para(String text) {
      blocks.add(new Para(text, false));
      return this;
    }

    public Builder bold(String text) {
      blocks.add(new Para(text, true));
      return this;
    }

    public Builder table(List<String> head, List<List<String>> rows) {
      blocks.add(new Table(head, rows));
      return this;
    }

    public Builder header(String text) {
      this.header = text;
      return this;
    }

    public Builder footer(String text) {
      this.footer = text;
      return this;
    }

    public ExportDoc build() {
      return new ExportDoc(title, blocks, header, footer);
    }
  }
}
