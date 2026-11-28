package vn.geo.server.notify.internal.service.draft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * MÓC KIỂM THỬ S5-E5 (chỉ profile dev/test): tin nhắn {@code class_notice} máy chủ đã gửi cổng AI
 * theo giáo viên, để E2E kiểm "yêu cầu gửi AI không chứa tên hay mã học sinh" (US-S5E5-01-FC4).
 */
@Component
@Profile({"dev", "test"})
public class NoticePromptLog {

  private final Map<UUID, List<String>> sent = new ConcurrentHashMap<>();

  void record(UUID teacherId, String prompt) {
    sent.computeIfAbsent(teacherId, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
        .add(prompt);
  }

  public List<String> of(UUID teacherId) {
    return List.copyOf(sent.getOrDefault(teacherId, List.of()));
  }
}
