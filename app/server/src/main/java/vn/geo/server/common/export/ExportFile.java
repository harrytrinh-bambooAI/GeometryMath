package vn.geo.server.common.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import vn.geo.server.common.export.docx.DocxWriter;
import vn.geo.server.common.export.pdf.PdfWriter;

/** Một tệp xuất (tên, kiểu, nội dung) và phản hồi tải về (no-store, tên tệp UTF-8). */
public record ExportFile(String filename, String contentType, byte[] bytes) {

  public static final String DOCX =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  public static final String PDF = "application/pdf";
  public static final String ZIP = "application/zip";

  /** Định dạng xuất hợp lệ của API ({@code format=docx|pdf}). */
  public enum Format {
    DOCX,
    PDF;

    /** null/rỗng = docx; giá trị lạ = null (dịch vụ báo VALIDATION_FAILED). */
    public static Format parse(String s) {
      if (s == null || s.isBlank() || "docx".equalsIgnoreCase(s)) {
        return DOCX;
      }
      return "pdf".equalsIgnoreCase(s) ? PDF : null;
    }

    public String ext() {
      return this == DOCX ? "docx" : "pdf";
    }
  }

  /** In {@code doc} theo định dạng, tên tệp = {@code baseName}.docx|pdf. */
  public static ExportFile render(ExportDoc doc, Format format, String baseName) {
    return format == Format.DOCX
        ? new ExportFile(baseName + ".docx", DOCX, DocxWriter.write(doc))
        : new ExportFile(baseName + ".pdf", PDF, PdfWriter.write(doc));
  }

  /** Gộp nhiều tệp vào một tệp nén. */
  public static ExportFile zip(String filename, List<ExportFile> files) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (ZipOutputStream z = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
      for (ExportFile f : files) {
        z.putNextEntry(new ZipEntry(f.filename()));
        z.write(f.bytes());
        z.closeEntry();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new ExportFile(filename, ZIP, out.toByteArray());
  }

  /** Phản hồi tải về: Content-Disposition attachment (tên UTF-8), Cache-Control no-store. */
  public ResponseEntity<byte[]> response() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.parseMediaType(contentType));
    h.setContentDisposition(
        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
    h.setCacheControl("no-store");
    h.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
    return ResponseEntity.ok().headers(h).body(bytes);
  }
}
