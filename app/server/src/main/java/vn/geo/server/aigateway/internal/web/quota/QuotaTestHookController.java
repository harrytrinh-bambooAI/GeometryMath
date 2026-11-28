package vn.geo.server.aigateway.internal.web.quota;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.aigateway.internal.service.killswitch.KillSwitchService;
import vn.geo.server.aigateway.internal.service.quota.AiQuotaService;
import vn.geo.server.aigateway.internal.service.quota.QuotaHookSupport;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * MÓC KIỂM THỬ S5-E5 (chỉ profile dev/test): dựng bộ đếm "đã dùng n lượt", cấp vai Vận hành, nhật
 * ký công tắc, và BỘ CHẠY CHẤM NỀN GIẢ — chấm nền thật (S5-E4/E6) chưa có ở đợt W2, nên bộ chạy này
 * đi đúng đường mà việc chấm nền sẽ dùng: {@link AiQuota#registerJob}, {@link AiQuota#admitGrading}
 * mỗi trang cho {@code grade_ocr} rồi {@code grade}, cổng AI thật (nhà cung cấp giả lập), gọi lại 1
 * lần khi lỗi KHÔNG xin lượt mới, việc bị huỷ thì không lưu đề xuất; "Đọc lại" qua {@link
 * AiQuota#takeOcrRetry}.
 */
@RestController
@Profile({"dev", "test"})
public class QuotaTestHookController {

  /** Tiền tố tin nhắn của bộ chạy: đáp mẫu grade/grade_ocr của MockAiProvider chỉ nhận tin này. */
  public static final String HARNESS = "[s5e5-harness]";

  private final QuotaHookSupport repo;
  private final AiQuotaService quota;
  private final KillSwitchService kill;
  private final AiGateway ai;

  public QuotaTestHookController(
      QuotaHookSupport repo, AiQuotaService quota, KillSwitchService kill, AiGateway ai) {
    this.repo = repo;
    this.quota = quota;
    this.kill = kill;
    this.ai = ai;
  }

  public record Counter(
      UUID orgId, String scope, UUID scopeId, String metric, long n, Integer daysAgo) {}

  /** Đặt bộ đếm (vd lớp đã dùng 300 lượt hỏi đáp hôm nay, 120 lượt kb_qa 3 ngày trước). */
  @PostMapping("/test-hooks/s5e5/counter")
  public Map<String, Object> counter(@RequestBody Counter c) {
    var day = quota.today().minusDays(c.daysAgo() == null ? 0 : c.daysAgo());
    repo.set(c.orgId(), c.scope(), c.scopeId(), c.metric(), day, c.n());
    return Map.of("day", day.toString());
  }

  @GetMapping("/test-hooks/s5e5/counter")
  public Map<String, Object> read(
      @RequestParam UUID orgId,
      @RequestParam String scope,
      @RequestParam UUID scopeId,
      @RequestParam String metric) {
    return Map.of("n", repo.counter(orgId, scope, scopeId, metric, quota.today()));
  }

  public record Grant(UUID userId) {}

  @PostMapping("/test-hooks/s5e5/operator")
  public Map<String, Object> operator(@RequestBody Grant g) {
    kill.grant(g.userId(), "test-hook");
    return Map.of("userId", g.userId());
  }

  @GetMapping("/test-hooks/s5e5/audit")
  public List<Map<String, Object>> audit(
      @RequestParam UUID orgId,
      @RequestParam(required = false) UUID classId,
      @RequestParam(required = false) String task) {
    return classId != null ? repo.classAudit(orgId, classId) : kill.audit(orgId, task);
  }

  public record Tokens(UUID orgId, UUID classId, long tokens) {}

  /** Một lượt hỏi đáp đã dùng {@code tokens} token (đường cộng token thật của bộ lọc hỏi đáp). */
  @PostMapping("/test-hooks/s5e5/tokens")
  public Map<String, Object> tokens(@RequestBody Tokens t) {
    quota.addTokens(t.orgId(), t.classId(), t.tokens());
    return Map.of(
        "n", repo.counter(t.orgId(), AiQuotaService.CLASS, t.classId(), "tokens", quota.today()));
  }

  public record Job(UUID orgId, UUID classId, UUID refId) {}

  @PostMapping("/test-hooks/s5e5/jobs")
  public Map<String, Object> job(@RequestBody Job j) {
    return Map.of("jobId", quota.registerJob(j.orgId(), j.classId(), "grade", j.refId()));
  }

  @GetMapping("/test-hooks/s5e5/jobs/{id}")
  public Map<String, Object> jobStatus(@PathVariable UUID id, @RequestParam UUID orgId) {
    return Map.of("status", repo.jobStatus(orgId, id));
  }

  public record GradingRun(
      UUID orgId, UUID teacherId, UUID classId, UUID submissionId, int pages, UUID jobId) {}

  /** Chấm nền giả một bài nộp {@code pages} trang (chạy dưới danh tính giáo viên chủ lớp). */
  @PostMapping("/test-hooks/s5e5/grading-run")
  public Map<String, Object> grade(@RequestBody GradingRun r) {
    AuthPrincipal as = new AuthPrincipal(r.orgId(), r.teacherId(), Role.TEACHER);
    return RequestContext.callAs(as, () -> run(r));
  }

  private Map<String, Object> run(GradingRun r) {
    UUID job =
        r.jobId() != null
            ? r.jobId()
            : quota.registerJob(r.orgId(), r.classId(), "grade", r.submissionId());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("jobId", job);
    List<String> proposals = new ArrayList<>();
    int calls = 0;
    for (String task : List.of("grade_ocr", "grade")) {
      for (int page = 1; page <= r.pages(); page++) {
        if (!quota.jobActive(r.orgId(), job)) {
          out.put("status", "cancelled");
          out.put("proposals", List.of());
          out.put("aiCalls", calls);
          return out;
        }
        AiQuota.Admission a = quota.admitGrading(r.orgId(), r.classId(), task);
        if (!a.allowed()) {
          quota.finishJob(r.orgId(), job);
          out.put("status", "ai_skipped");
          out.put("flag", "AI không chấm");
          out.put("reason", a.reason());
          out.put("proposals", List.of());
          out.put("aiCalls", calls);
          return out;
        }
        String prompt = HARNESS + " " + task + " trang " + page;
        String reply;
        try {
          calls++;
          reply = ai.complete(new AiRequest(task, prompt)).content();
        } catch (AppException first) {
          calls++; // gọi lại tự động trong CÙNG lượt: không xin lượt mới (BR-S5E5-12)
          reply = ai.complete(new AiRequest(task, prompt)).content();
        }
        if ("grade".equals(task)) {
          proposals.add(reply);
        }
      }
    }
    boolean stillActive = quota.jobActive(r.orgId(), job);
    quota.finishJob(r.orgId(), job);
    out.put("status", stillActive ? "graded" : "cancelled");
    out.put("proposals", stillActive ? proposals : List.of());
    out.put("aiCalls", calls);
    return out;
  }

  public record OcrRetry(UUID orgId, UUID teacherId, UUID submissionId, Integer used) {}

  /** "Đọc lại" bản đọc chữ (tối đa 3 lần); {@code used} = đặt sẵn số lần đã đọc lại. */
  @PostMapping("/test-hooks/s5e5/ocr-retry")
  public Map<String, Object> ocrRetry(@RequestBody OcrRetry o) {
    if (o.used() != null) {
      repo.setOcr(o.orgId(), o.submissionId(), o.used());
      return Map.of("n", o.used());
    }
    AuthPrincipal as = new AuthPrincipal(o.orgId(), o.teacherId(), Role.TEACHER);
    return RequestContext.callAs(
        as,
        () -> {
          quota.takeOcrRetry(o.orgId(), o.submissionId());
          ai.complete(new AiRequest("grade_ocr", HARNESS + " grade_ocr đọc lại"));
          return Map.<String, Object>of("n", repo.ocrRetries(o.orgId(), o.submissionId()));
        });
  }
}
