package vn.geo.server.content.internal.export.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.export.service.ExportService;
import vn.geo.server.content.internal.export.service.ShareLinkService;

/**
 * Giáo viên xuất đề (nội dung cho tệp PNG / PDF dựng ở giao diện) và quản lý liên kết chỉ xem
 * (US-S2E9-02). Trường và người gọi lấy từ thẻ. Đường công khai của liên kết:
 * ShareLinkPublicFilter.
 */
@RestController
public class ExportController {

  public record ExportBody(List<UUID> docIds, Boolean withSolution, Boolean confirmLocked) {}

  public record LinkBody(UUID docId, Integer days) {}

  private final ExportService exports;
  private final ShareLinkService links;

  public ExportController(ExportService exports, ShareLinkService links) {
    this.exports = exports;
    this.links = links;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/exports/problems")
  public Map<String, List<ExportService.DocOption>> options() {
    return Map.of("items", exports.options());
  }

  @NoStore
  @PostMapping("/api/v1/teacher/exports")
  public ExportService.ExportView export(@RequestBody ExportBody body) {
    return exports.export(
        body.docIds(),
        Boolean.TRUE.equals(body.withSolution()),
        Boolean.TRUE.equals(body.confirmLocked()));
  }

  @NoStore
  @GetMapping("/api/v1/teacher/exports/confirmations")
  public Map<String, Long> confirmations() {
    return Map.of("count", exports.confirmations());
  }

  @NoStore
  @PostMapping("/api/v1/teacher/share-links")
  @ResponseStatus(HttpStatus.CREATED)
  public ShareLinkService.Link create(@RequestBody LinkBody body) {
    return links.create(body.docId(), body.days());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/share-links")
  public Map<String, List<ShareLinkService.Link>> list(@RequestParam UUID docId) {
    return Map.of("items", links.list(docId));
  }

  @DeleteMapping("/api/v1/teacher/share-links/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revoke(@PathVariable UUID id) {
    links.revoke(id);
  }
}
