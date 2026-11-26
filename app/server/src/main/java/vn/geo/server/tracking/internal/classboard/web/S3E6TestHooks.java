package vn.geo.server.tracking.internal.classboard.web;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.tracking.internal.classboard.service.ClassExportService;
import vn.geo.server.tracking.internal.classboard.service.PresentFromResultsService;

/** Móc kiểm thử S3-E6 (chỉ dev/test): giả lập thời gian trôi cho giới hạn xuất 10 lần/giờ. */
@RestController
@Profile({"dev", "test"})
public class S3E6TestHooks {

  public record Age(UUID teacherId, long seconds) {}

  public record Release(UUID setId, Long minutesFromNow) {}

  private final ClassExportService export;
  private final PresentFromResultsService present;

  public S3E6TestHooks(ClassExportService export, PresentFromResultsService present) {
    this.export = export;
    this.present = present;
  }

  /** Giờ mở lời giải của bộ (sandbox S3-E3 mặc định Chưa đặt giờ): âm = Đã mở, dương = Sắp mở. */
  @PostMapping("/test-hooks/s3e6/set-release")
  public Map<String, Object> release(@RequestBody Release b) {
    return Map.of("updated", present.setReleaseForTest(b.setId(), b.minutesFromNow()));
  }

  @PostMapping("/test-hooks/s3e6/exports/age")
  public Map<String, Object> age(@RequestBody Age b) {
    return Map.of("updated", export.ageExportsForTest(b.teacherId(), b.seconds()));
  }
}
