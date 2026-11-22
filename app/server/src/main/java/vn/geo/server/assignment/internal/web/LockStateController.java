package vn.geo.server.assignment.internal.web;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.assignment.internal.service.LockStateService;
import vn.geo.server.assignment.internal.service.LockStateService.LockState;
import vn.geo.server.common.web.NoStore;

/**
 * Trạng thái khóa lời giải cho công cụ đo (E3, BR-GEO-156, 157): {@code locked} theo
 * fn_solution_gate, áp cho bài lớp (giờ mở) và bài tự học (kế thừa khóa, D-GEO-026). Trang Làm bài
 * hỏi lại mỗi 5 giây (US-GEO-305-SC6, không muộn hơn 10 giây). Không chứa lời giải hay đáp số.
 */
@RestController
public class LockStateController {

  private final LockStateService service;

  public LockStateController(LockStateService service) {
    this.service = service;
  }

  @NoStore
  @GetMapping("/api/v1/student/assignments/{id}/lock")
  public LockState lock(@PathVariable UUID id) {
    return service.lock(id);
  }
}
