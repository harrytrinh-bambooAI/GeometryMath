package vn.geo.server.aigateway.internal.service.quota;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.aigateway.internal.repo.quota.QuotaRepository;

/** Truy cập bộ đếm / nhật ký cho MÓC KIỂM THỬ S5-E5 (chỉ profile dev/test). */
@Service
@Profile({"dev", "test"})
public class QuotaHookSupport {

  private final QuotaRepository repo;

  public QuotaHookSupport(QuotaRepository repo) {
    this.repo = repo;
  }

  public void set(UUID orgId, String scope, UUID scopeId, String metric, LocalDate day, long n) {
    repo.set(orgId, scope, scopeId, metric, day, n);
  }

  public long counter(UUID orgId, String scope, UUID scopeId, String metric, LocalDate day) {
    return repo.counter(orgId, scope, scopeId, metric, day);
  }

  public List<Map<String, Object>> classAudit(UUID orgId, UUID classId) {
    return repo.auditOf(orgId, classId, 20);
  }

  public String jobStatus(UUID orgId, UUID jobId) {
    return repo.jobStatus(orgId, jobId).orElse("missing");
  }

  public void setOcr(UUID orgId, UUID submissionId, int n) {
    repo.setOcr(orgId, submissionId, n);
  }

  public int ocrRetries(UUID orgId, UUID submissionId) {
    return repo.ocrRetries(orgId, submissionId);
  }
}
