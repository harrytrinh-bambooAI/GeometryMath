package vn.geo.server.aigrade.internal.shadow.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.geo.server.aigrade.internal.proposal.repo.ProposalRepository.Candidate;
import vn.geo.server.aigrade.internal.proposal.service.ProposalService;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Chấm nền ở chế độ "Chạy song song" (US-S5E6-05, BR-S5E6-13 "AI vẫn chấm nền"): cùng đường chấm
 * của US-S5E6-01 (lượt hạn mức, task {@code grade}, gọi lại, luật KPI §2, engine, lưu bất biến).
 *
 * <p>Hàng việc của {@code ProposalRepository} chỉ lấy chế độ "AI đề xuất" và đường chấm {@code
 * ProposalService.grade} là package-private (tệp của T-261128-61, ngoài phạm vi task này): gọi qua
 * phản chiếu, giải lúc khởi động (đổi tên là hỏng ngay ở test). FIX: mở {@code grade} public hoặc
 * cho {@code ProposalRepository.pending} lấy cả {@code parallel} — xem qc_findings Dev-S5E6-08.
 */
@Service
public class ShadowGrader {

  private static final Logger log = LoggerFactory.getLogger(ShadowGrader.class);

  private final ShadowRepository repo;
  private final ProposalService proposals;
  private final Method grade;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final Set<String> running = ConcurrentHashMap.newKeySet();

  public ShadowGrader(ShadowRepository repo, ProposalService proposals) {
    this.repo = repo;
    this.proposals = proposals;
    try {
      this.grade = ProposalService.class.getDeclaredMethod("grade", Candidate.class);
      this.grade.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("ProposalService.grade(Candidate) không còn", e);
    }
  }

  /** Xếp chấm nền mọi bài song song đang chờ (job). */
  public int schedulePending(int limit) {
    int n = 0;
    for (Candidate c : repo.pending(limit)) {
      if (submit(c)) {
        n++;
      }
    }
    return n;
  }

  /** Xếp chấm nền một bài song song đang chờ (Thử chấm lại); false nếu không có việc. */
  public boolean schedule(UUID orgId, UUID submissionId) {
    return repo.pendingOf(orgId, submissionId).map(this::submit).orElse(false);
  }

  private boolean submit(Candidate c) {
    String k = c.versionId() + "#" + c.ocrVersion();
    if (!running.add(k)) {
      return false;
    }
    executor.submit(() -> {
      try {
        run(c);
      } catch (RuntimeException e) {
        log.warn("Chấm nền song song lỗi: {}", e.toString());
      } finally {
        running.remove(k);
      }
    });
    return true;
  }

  /** Lượt gọi AI dưới danh tính học sinh nộp (như việc chấm nền của US-S5E6-01). */
  void run(Candidate c) {
    RequestContext.callAs(
        new AuthPrincipal(c.orgId(), c.studentId(), Role.STUDENT),
        () -> {
          try {
            grade.invoke(proposals, c);
          } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
          } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException r) {
              throw r;
            }
            throw new IllegalStateException(e.getCause());
          }
          return null;
        });
  }
}
