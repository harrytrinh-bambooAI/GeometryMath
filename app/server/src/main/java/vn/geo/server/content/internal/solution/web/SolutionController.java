package vn.geo.server.content.internal.solution.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.solution.service.SolutionReviewService;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocSummary;
import vn.geo.server.content.internal.solution.service.SolutionViews.DocView;
import vn.geo.server.content.internal.solution.service.SolutionViews.PinnedLabels;
import vn.geo.server.content.internal.solution.service.SolutionViews.StepView;

/**
 * S2-E5: màn C2-03 "Soát và duyệt lời giải" (giáo viên chủ đề) và nhãn hai trục của phiên bản đã
 * giao (học sinh). No-store (D-GEO-145). Thân yêu cầu không có trường nào đặt được nhãn máy: gửi
 * kèm {@code machineLabel}/{@code machineStatus}/{@code verifyStatus} bị từ chối (BR-S2E5-15).
 */
@RestController
@NoStore
public class SolutionController {

  private final SolutionReviewService solutions;

  public SolutionController(SolutionReviewService solutions) {
    this.solutions = solutions;
  }

  /** Lưu thành phiên bản mới từ phiên bản gốc {@code baseVersion}. */
  public record SaveRequest(
      Integer baseVersion,
      List<String> steps,
      UUID proposalId,
      Object machineLabel,
      Object machineStatus,
      Object verifyStatus) {}

  /** Kiểm tra lại / duyệt một phiên bản. */
  public record VersionRequest(
      Integer version,
      Boolean confirmNeedsReview,
      Object machineLabel,
      Object machineStatus,
      Object verifyStatus) {}

  private static void noMachineLabel(Object... fields) {
    for (Object f : fields) {
      if (f != null) {
        throw AppException.invalidField(
            "machineLabel", "Nhãn máy chỉ do máy gắn, không đặt được từ yêu cầu");
      }
    }
  }

  @GetMapping("/api/v1/teacher/solutions")
  public Map<String, List<DocSummary>> list() {
    return Map.of("items", solutions.list());
  }

  @GetMapping("/api/v1/teacher/solutions/{id}")
  public DocView view(@PathVariable UUID id, @RequestParam(required = false) Integer version) {
    return solutions.view(id, version);
  }

  @GetMapping("/api/v1/teacher/solutions/{id}/step-checks")
  public Map<String, List<StepView>> stepChecks(
      @PathVariable UUID id, @RequestParam(required = false) Integer version) {
    return Map.of("items", solutions.stepChecks(id, version));
  }

  @PostMapping("/api/v1/teacher/solutions/{id}/versions")
  public DocView save(@PathVariable UUID id, @RequestBody SaveRequest body) {
    noMachineLabel(body.machineLabel(), body.machineStatus(), body.verifyStatus());
    return solutions.save(id, body.baseVersion(), body.steps(), body.proposalId());
  }

  @PostMapping("/api/v1/teacher/solutions/{id}/versions/{version}/reuse")
  public DocView reuse(@PathVariable UUID id, @PathVariable int version) {
    return solutions.reuse(id, version);
  }

  @PostMapping("/api/v1/teacher/solutions/{id}/check")
  public DocView check(@PathVariable UUID id, @RequestBody VersionRequest body) {
    noMachineLabel(body.machineLabel(), body.machineStatus(), body.verifyStatus());
    return solutions.check(id, body.version());
  }

  @PostMapping("/api/v1/teacher/solutions/{id}/approve")
  public DocView approve(@PathVariable UUID id, @RequestBody VersionRequest body) {
    noMachineLabel(body.machineLabel(), body.machineStatus(), body.verifyStatus());
    return solutions.approve(id, body.version(), Boolean.TRUE.equals(body.confirmNeedsReview()));
  }

  @GetMapping("/api/v1/student/assignments/{id}/solution-labels")
  public PinnedLabels pinnedLabels(@PathVariable UUID id) {
    return solutions.pinnedLabels(id);
  }
}
