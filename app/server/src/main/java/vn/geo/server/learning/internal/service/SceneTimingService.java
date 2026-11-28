package vn.geo.server.learning.internal.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.learning.internal.repo.HelpRepository;
import vn.geo.server.learning.internal.repo.SceneTimingRepository;

/**
 * BR-GEO-169, M-GEO-09: thiết bị gửi 'scene_shown' kèm thời lượng từ lúc gửi đề tới lúc hình hiện
 * (sự kiện không bền, mức A; không tính vào thao tác, tiến độ hay Theo dõi lớp). Báo cáo kỳ đo:
 * trung vị thời lượng, số đề lỗi đếm riêng, ghi rõ ngưỡng chỉ có nghĩa trên AI thật (D-GEO-060,
 * 165).
 */
@Service
public class SceneTimingService {

  public static final String NOTE = "ngưỡng đề xuất < 40 giây, chỉ có nghĩa trên AI thật";
  private static final long MAX_MS = 3_276_700; // arg smallint, đơn vị 100 ms

  public record Report(int shown, String medianSeconds, long failed, String note) {}

  private final HelpRepository help;
  private final SceneTimingRepository repo;

  public SceneTimingService(HelpRepository help, SceneTimingRepository repo) {
    this.help = help;
    this.repo = repo;
  }

  public void record(UUID assignmentId, Long durationMs) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    if (durationMs == null || durationMs < 0) {
      throw AppException.invalidField("durationMs", "Thời lượng không hợp lệ.");
    }
    int tenths = (int) (Math.min(durationMs, MAX_MS) / 100);
    if (!"OK".equals(help.recordSceneShown(p.userId(), assignmentId, tenths))) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
  }

  public Report report(Instant from, Instant to) {
    return summarize(repo.sceneShownTenths(from, to), repo.failedBuilds(from, to));
  }

  /** Trung vị (giây, một chữ số thập phân, dấu phẩy) của các thời lượng (100 ms). */
  public static Report summarize(List<Long> tenths, long failed) {
    if (tenths.isEmpty()) {
      return new Report(0, "—", failed, NOTE);
    }
    List<Long> s = tenths.stream().sorted().toList();
    int n = s.size();
    double med = n % 2 == 1 ? s.get(n / 2) : (s.get(n / 2 - 1) + s.get(n / 2)) / 2.0;
    String text = String.format(Locale.ROOT, "%.1f", med / 10.0).replace('.', ',');
    return new Report(n, text, failed, NOTE);
  }
}
