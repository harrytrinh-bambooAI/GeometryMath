package vn.geo.server.content.internal.draft.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.draft.service.DraftService;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftSummary;
import vn.geo.server.content.internal.draft.service.DraftViews.DraftView;
import vn.geo.server.content.internal.draft.service.DraftViews.JobView;

/**
 * S2-E3: màn C2-02 "Thêm đề" (giáo viên chủ nháp; người khác NOT_FOUND, học sinh FORBIDDEN).
 * No-store (D-GEO-145): kết quả có lời giải nháp.
 *
 * <ul>
 *   <li>{@code POST /api/v1/teacher/problem-drafts} tạo nháp ({source, statement}); quá 20 nháp mỗi
 *       giờ → RATE_LIMITED + Retry-After.
 *   <li>{@code GET …/current} nháp đang soạn gần nhất (204 khi không có); {@code GET …} ngăn "Của
 *       tôi".
 *   <li>{@code PUT …/{id}} lưu chữ / ô chữ; {@code PUT …/{id}/image?offset=&total=} đoạn ảnh
 *       (application/octet-stream, mỗi đoạn ≤ 1 MB, ảnh ≤ 5 MB, chỉ JPG/PNG theo nội dung).
 *   <li>{@code POST …/{id}/confirm-text}, {@code /build}, {@code /supplement}, {@code /text-only},
 *       {@code /assign}.
 *   <li>{@code GET|POST /api/v1/teacher/problem-draft-jobs/{jobId}[/retry|/cancel]} tiến trình bốn
 *       bước.
 * </ul>
 */
@RestController
@NoStore
public class DraftController {

  private static final String BASE = "/api/v1/teacher/problem-drafts";
  private static final String JOBS = "/api/v1/teacher/problem-draft-jobs";

  private final DraftService drafts;

  public DraftController(DraftService drafts) {
    this.drafts = drafts;
  }

  public record CreateRequest(String source, String statement) {}

  public record UpdateRequest(String statement, List<Map<String, Object>> cells) {}

  public record SupplementRequest(String condition) {}

  public record AssignRequest(Boolean confirmAiFigure) {}

  @PostMapping(BASE)
  public DraftView create(@RequestBody(required = false) CreateRequest body) {
    return drafts.create(
        body == null ? null : body.source(), body == null ? null : body.statement());
  }

  @GetMapping(BASE)
  public Map<String, List<DraftSummary>> mine() {
    return Map.of("items", drafts.mine());
  }

  @GetMapping(BASE + "/current")
  public ResponseEntity<DraftView> current() {
    DraftView v = drafts.current();
    return v == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(v);
  }

  @GetMapping(BASE + "/{id}")
  public DraftView view(@PathVariable UUID id) {
    return drafts.view(id);
  }

  @PutMapping(BASE + "/{id}")
  public DraftView update(@PathVariable UUID id, @RequestBody UpdateRequest body) {
    return drafts.update(id, body.statement(), body.cells());
  }

  @PutMapping(path = BASE + "/{id}/image", consumes = "application/octet-stream")
  public Object image(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam int total,
      @RequestBody byte[] chunk) {
    Object out = drafts.uploadImage(id, offset, total, chunk);
    if (out instanceof Map<?, ?> m) {
      return new HashMap<>(m);
    }
    return out;
  }

  @PostMapping(BASE + "/{id}/confirm-text")
  public DraftView confirmText(@PathVariable UUID id) {
    return drafts.confirmText(id);
  }

  @PostMapping(BASE + "/{id}/build")
  public DraftView build(@PathVariable UUID id) {
    return drafts.build(id);
  }

  @PostMapping(BASE + "/{id}/supplement")
  public DraftView supplement(@PathVariable UUID id, @RequestBody SupplementRequest body) {
    return drafts.supplement(id, body.condition());
  }

  @PostMapping(BASE + "/{id}/text-only")
  public DraftView textOnly(@PathVariable UUID id) {
    return drafts.saveTextOnly(id);
  }

  @PostMapping(BASE + "/{id}/assign")
  public DraftView assign(
      @PathVariable UUID id, @RequestBody(required = false) AssignRequest body) {
    return drafts.assign(id, body != null && Boolean.TRUE.equals(body.confirmAiFigure()));
  }

  @GetMapping(JOBS + "/{jobId}")
  public JobView job(@PathVariable UUID jobId) {
    return drafts.job(jobId);
  }

  @PostMapping(JOBS + "/{jobId}/retry")
  public JobView retry(@PathVariable UUID jobId) {
    return drafts.retry(jobId);
  }

  @PostMapping(JOBS + "/{jobId}/cancel")
  public JobView cancel(@PathVariable UUID jobId) {
    return drafts.cancel(jobId);
  }
}
