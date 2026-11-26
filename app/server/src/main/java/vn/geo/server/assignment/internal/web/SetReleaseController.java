package vn.geo.server.assignment.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.AuditEventService;
import vn.geo.server.assignment.internal.service.SetReleaseService;
import vn.geo.server.common.web.NoStore;

/**
 * S3-E4 + US-S3E5-01: Tổng quan giáo viên (C3-05), Theo dõi nhanh (C3-06: em chưa mở bài, Mở ngay,
 * Đổi giờ cho cả bộ), nhật ký AuditEvent của lớp. Người gọi và trường lấy từ thẻ; mọi phản hồi
 * no-store (dữ liệu học sinh, trạng thái khóa; D-GEO-145). Không có đường sửa hay xóa nhật ký.
 */
@NoStore
@RestController
public class SetReleaseController {

  /** clientTime: giờ thiết bị nếu máy khách gửi kèm — KHÔNG dùng, không lưu (BR-S3E4-11). */
  public record OpenNowBody(String requestKey, Integer version, String clientTime) {}

  public record ChangeTimeBody(
      String releaseAt, String requestKey, Integer version, String clientTime) {}

  private final SetReleaseService release;
  private final AuditEventService audit;

  public SetReleaseController(SetReleaseService release, AuditEventService audit) {
    this.release = release;
    this.audit = audit;
  }

  @GetMapping("/api/v1/teacher/overview")
  public SetReleaseService.Overview overview() {
    return release.overview();
  }

  @GetMapping("/api/v1/teacher/assignment-sets/{id}/follow")
  public SetReleaseService.Follow follow(@PathVariable UUID id) {
    return release.follow(id);
  }

  @PostMapping("/api/v1/teacher/assignment-sets/{id}/open-now")
  public SetReleaseService.Follow openNow(
      @PathVariable UUID id, @RequestBody(required = false) OpenNowBody body) {
    return release.openNow(
        id, body == null ? null : body.requestKey(), body == null ? null : body.version());
  }

  @PostMapping("/api/v1/teacher/assignment-sets/{id}/release-time")
  public SetReleaseService.Follow changeTime(
      @PathVariable UUID id, @RequestBody ChangeTimeBody body) {
    return release.changeTime(id, body.releaseAt(), body.requestKey(), body.version());
  }

  @GetMapping("/api/v1/teacher/classes/{classId}/audit-events")
  public Map<String, List<AuditEventService.AuditEventView>> auditEvents(
      @PathVariable UUID classId) {
    return Map.of("items", audit.classLog(classId));
  }
}
