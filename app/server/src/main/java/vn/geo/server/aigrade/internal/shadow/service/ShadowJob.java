package vn.geo.server.aigrade.internal.shadow.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job nền chạy song song (US-S5E6-05): mỗi {@code geo.s5.ai-grading.poll-ms} quét bài nộp có bản
 * đọc chữ mới ở chế độ "Chạy song song" và xếp chấm. Chỉ chạy khi cờ {@code
 * geo.s5.ai-grading.enabled} bật.
 */
@Component
@ConditionalOnProperty(name = "geo.s5.ai-grading.enabled", havingValue = "true")
public class ShadowJob {

  private final ShadowGrader grader;

  public ShadowJob(ShadowGrader grader) {
    this.grader = grader;
  }

  @Scheduled(
      fixedDelayString = "${geo.s5.ai-grading.poll-ms:2000}",
      initialDelayString = "${geo.s5.ai-grading.poll-ms:2000}")
  public void run() {
    grader.schedulePending(50);
  }
}
