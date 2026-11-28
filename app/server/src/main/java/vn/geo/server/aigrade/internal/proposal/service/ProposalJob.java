package vn.geo.server.aigrade.internal.proposal.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job nền AI đề xuất điểm (US-S5E6-01): mỗi {@code geo.s5.ai-grading.poll-ms} quét bài nộp có bản
 * đọc chữ mới (chế độ "AI đề xuất") và xếp chấm. Chỉ chạy khi cờ {@code geo.s5.ai-grading.enabled}
 * bật.
 */
@Component
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class ProposalJob {

  private final ProposalService service;

  public ProposalJob(ProposalService service) {
    this.service = service;
  }

  @Scheduled(
      fixedDelayString = "${geo.s5.ai-grading.poll-ms:2000}",
      initialDelayString = "${geo.s5.ai-grading.poll-ms:2000}")
  public void run() {
    service.schedulePending(50);
  }
}
