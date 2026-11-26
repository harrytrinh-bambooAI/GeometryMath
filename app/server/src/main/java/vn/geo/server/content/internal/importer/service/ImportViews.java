package vn.geo.server.content.internal.importer.service;

import java.util.List;
import java.util.UUID;

/** S2-E4: dữ liệu màn C2-04b "Nhập từ tệp" (chỉ giáo viên chủ lượt import nhận được). */
public final class ImportViews {

  private ImportViews() {}

  /** Trần dung lượng tệp: 10 MB (BR-S2E4-07, đã chốt PO-66). */
  public static final int MAX_BYTES = 10 * 1024 * 1024;

  public static final String TOO_BIG = "Tệp tối đa 10 MB";
  public static final String THEORY = "Phần lý thuyết chưa hỗ trợ ở chặng này";
  public static final String NO_EXERCISE =
      "Không tìm thấy bài tập nào trong tệp (mỗi bài cần mở đầu bằng \"Bài 1.\", \"Câu 1.\"…)";
  public static final String TOO_LONG = "Bài dài quá 2000 ký tự, hãy nhập tay";
  public static final String IN_DRAFT = "đã vào nháp";
  public static final String HUMAN_AI = "AI soạn";
  public static final String MACHINE_NEEDS_REVIEW = "Cần xem lại";

  public static String overLimit(int found) {
    return "Tệp có " + found + " bài, chỉ lấy 50 bài đầu";
  }

  /**
   * Một bài tập đã tách. state = new | similar | exists | update | too_long | created; note = "đã
   * có trong kho" / "gần giống bài …, xem?" / "cập nhật khi lưu" / "đã vào nháp" (null khi không có
   * gì để báo); refKind = library | mine | draft + refId = đề trong kho để mở xem; checked = có
   * tick sẵn không (đề đã có trong kho mặc định KHÔNG tick, BR-S2E4-13).
   */
  public record ItemView(
      int index,
      String title,
      String text,
      String state,
      String note,
      String refKind,
      UUID refId,
      boolean checked,
      boolean selectable,
      UUID draftId,
      String humanLabel,
      String machineLabel) {}

  /**
   * Một lượt import. status = uploading | parsed | rejected; notes = các dòng báo của tệp; created
   * / updated / skipped chỉ khác 0 ngay sau lệnh "Tạo nháp".
   */
  public record ImportView(
      UUID id,
      String fileName,
      String fileType,
      String status,
      int sizeBytes,
      int received,
      int found,
      List<String> notes,
      String error,
      List<ItemView> items,
      int created,
      int updated,
      int skipped) {}
}
