package vn.geo.server.tracking.internal.errors.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * MÓC KIỂM THỬ (chỉ dev/test): tin nhắn đã gửi task {@code class_errors} theo giáo viên, để E2E
 * kiểm không có tên / mã học sinh (US-S5E5-02-SC3). Không có ở prod.
 */
@Component
@Profile({"dev", "test"})
public class ErrorPromptLog {

  private final Map<UUID, List<String>> sent = new ConcurrentHashMap<>();

  void record(UUID teacherId, String prompt) {
    sent.computeIfAbsent(teacherId, k -> java.util.Collections.synchronizedList(new ArrayList<>()))
        .add(prompt);
  }

  public List<String> of(UUID teacherId) {
    return List.copyOf(sent.getOrDefault(teacherId, List.of()));
  }
}
