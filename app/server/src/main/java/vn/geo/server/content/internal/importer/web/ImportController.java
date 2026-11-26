package vn.geo.server.content.internal.importer.web;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.importer.service.ImportService;
import vn.geo.server.content.internal.importer.service.ImportViews.ImportView;

/**
 * S2-E4: màn C2-04b "Nhập từ tệp" (giáo viên chủ lượt import; người khác NOT_FOUND, học sinh
 * FORBIDDEN).
 *
 * <ul>
 *   <li>{@code POST /api/v1/teacher/problem-imports} {fileName, sizeBytes}: chỉ docx / pdf
 *       (VALIDATION_FAILED "Chỉ nhận docx và pdf"), tối đa 10 MB (PAYLOAD_TOO_LARGE "Tệp tối đa 10
 *       MB").
 *   <li>{@code PUT …/{id}/file?offset=&total=} (application/octet-stream, mỗi đoạn ≤ 1 MB): đoạn
 *       cuối trả các mục đã tách kèm đối chiếu trùng; tệp bị từ chối → VALIDATION_FAILED kèm lý do.
 *   <li>{@code GET …/{id}} xem lại lượt import (đối chiếu tính lại theo kho hiện tại).
 *   <li>{@code POST …/{id}/drafts} {indexes}: chỉ mục được tick thành nháp; vượt giới hạn thêm đề →
 *       RATE_LIMITED + Retry-After, không mục nào được ghi.
 * </ul>
 */
@RestController
@NoStore
public class ImportController {

  private static final String BASE = "/api/v1/teacher/problem-imports";

  private final ImportService imports;

  public ImportController(ImportService imports) {
    this.imports = imports;
  }

  public record CreateRequest(String fileName, Long sizeBytes) {}

  public record DraftsRequest(List<Integer> indexes) {}

  @PostMapping(BASE)
  public ImportView create(@RequestBody CreateRequest body) {
    return imports.create(body.fileName(), body.sizeBytes() == null ? 0 : body.sizeBytes());
  }

  @PutMapping(path = BASE + "/{id}/file", consumes = "application/octet-stream")
  public ImportView file(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam int total,
      @RequestBody byte[] chunk) {
    return imports.upload(id, offset, total, chunk);
  }

  @GetMapping(BASE + "/{id}")
  public ImportView view(@PathVariable UUID id) {
    return imports.view(id);
  }

  @PostMapping(BASE + "/{id}/drafts")
  public ImportView drafts(@PathVariable UUID id, @RequestBody DraftsRequest body) {
    return imports.createDrafts(id, body.indexes());
  }
}
