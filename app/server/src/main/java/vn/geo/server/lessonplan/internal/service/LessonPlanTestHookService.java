package vn.geo.server.lessonplan.internal.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.lessonplan.internal.repo.LessonPlanRepository;

/**
 * MÓC KIỂM THỬ S5-E1 (chỉ profile dev/test): ghi lại tin nhắn lesson_plan máy chủ đã gửi cổng AI
 * theo giáo án (kiểm "ghi chú nằm trong thẻ dữ liệu", "lần 2 kèm danh sách lỗi" từ E2E), số liệu
 * nội bộ (fix_keys, số lần gọi), thêm lượt đã dùng trong ngày (kiểm hạn mức 30).
 */
@Service
@Profile({"dev", "test"})
public class LessonPlanTestHookService {

  private static final int KEEP = 200;

  private final LessonPlanRepository plans;
  private final Clock clock;
  private final Map<UUID, List<String>> sent =
      java.util.Collections.synchronizedMap(
          new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, List<String>> e) {
              return size() > KEEP;
            }
          });

  public LessonPlanTestHookService(LessonPlanRepository plans, Clock clock) {
    this.plans = plans;
    this.clock = clock;
  }

  void recordSent(UUID planId, String message) {
    sent.computeIfAbsent(planId, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
        .add(message);
  }

  public Map<String, Object> plan(UUID orgId, UUID planId) {
    Map<String, Object> m =
        new LinkedHashMap<>(
            plans
                .internals(orgId, planId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND)));
    m.put("messages", List.copyOf(sent.getOrDefault(planId, List.of())));
    return m;
  }

  public Map<String, Object> seedTurns(UUID orgId, UUID teacherId, int count) {
    LocalDate today =
        ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM).toLocalDate();
    plans.seedTurns(orgId, teacherId, today, Math.max(0, Math.min(count, 100)));
    return Map.of("day", today.toString());
  }
}
