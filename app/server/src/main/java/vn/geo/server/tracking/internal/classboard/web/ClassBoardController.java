package vn.geo.server.tracking.internal.classboard.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.tracking.internal.classboard.service.ClassBoardService;
import vn.geo.server.tracking.internal.classboard.service.StuckService;

/**
 * S3-E3 — API Kết quả lớp của giáo viên chủ lớp (C3-04): chọn lớp / bộ, bảng lớp × bài, bằng chứng
 * một ô, "Cả lớp vướng ở đâu". Mọi phản hồi {@code Cache-Control: no-store} (D-GEO-145). Học sinh
 * gọi nhận FORBIDDEN; lớp / bộ / em ngoài quan hệ nhận NOT_FOUND.
 */
@RestController
@RequestMapping("/api/v1/teacher/class-results")
public class ClassBoardController {

  private final ClassBoardService board;
  private final StuckService stuck;

  public ClassBoardController(ClassBoardService board, StuckService stuck) {
    this.board = board;
    this.stuck = stuck;
  }

  @NoStore
  @GetMapping("/classes")
  public Map<String, List<ClassBoardService.ClassOption>> classes() {
    return Map.of("items", board.classes());
  }

  @NoStore
  @GetMapping("/{classId}/sets")
  public ClassBoardService.Sets sets(@PathVariable UUID classId) {
    return board.sets(classId);
  }

  @NoStore
  @GetMapping("/{classId}/sets/{setId}/board")
  public ClassBoardService.Board board(
      @PathVariable UUID classId,
      @PathVariable UUID setId,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return board.board(classId, setId, limit, cursor);
  }

  @NoStore
  @GetMapping("/{classId}/sets/{setId}/evidence")
  public ClassBoardService.Evidence evidence(
      @PathVariable UUID classId,
      @PathVariable UUID setId,
      @RequestParam int item,
      @RequestParam UUID userId,
      @RequestParam(defaultValue = "false") boolean work) {
    // PO-74 (BR-S3E3-40): em xác định bằng userId (UUID), không bằng mã người dùng trong địa chỉ
    return board.evidence(classId, setId, item, userId, work);
  }

  @NoStore
  @GetMapping("/{classId}/sets/{setId}/stuck")
  public StuckService.Stuck stuck(@PathVariable UUID classId, @PathVariable UUID setId) {
    return stuck.stuck(classId, setId);
  }
}
