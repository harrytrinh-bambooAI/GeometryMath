package vn.geo.server.aigrade.internal.shadow.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository;
import vn.geo.server.aigrade.internal.shadow.repo.ShadowRepository.SubInfo;
import vn.geo.server.assessment.api.ClassAiGateQuery;

/**
 * Luật hiển thị của chạy song song (US-S5E6-05, BR-S5E6-13) và lấy mẫu sau khi bật AI đề xuất
 * (QS5-24): bài "song song" = bài có chấm chế độ "Chạy song song", hoặc bài lấy mẫu ({@code
 * shadow-sample-pct} % theo mã bài nộp, tối thiểu 2 bài đầu, cố định) của chế độ "AI đề xuất" khi
 * lớp đã đạt cổng. Trước khi bài Đã duyệt mọi phản hồi cho giáo viên ẩn điểm, lý do, độ tin cậy và
 * cờ suy từ AI.
 */
@Service
public class ShadowPolicy {

  /** MSG-S5-47. */
  public static final String MSG_47 =
      "Chế độ chạy song song: thầy cô chấm trước, đề xuất của AI hiện sau khi duyệt.";

  private final ShadowRepository repo;
  private final ObjectProvider<ClassAiGateQuery> gate;
  private final int samplePct;

  public ShadowPolicy(
      ShadowRepository repo,
      ObjectProvider<ClassAiGateQuery> gate,
      @Value("${geo.s5.ai-grading.shadow-sample-pct:10}") int samplePct) {
    this.repo = repo;
    this.gate = gate;
    this.samplePct = Math.max(0, Math.min(100, samplePct));
  }

  public Optional<SubInfo> info(UUID orgId, UUID submissionId) {
    return repo.info(orgId, submissionId);
  }

  /** Lấy mẫu cố định theo mã bài nộp (phần ngẫu nhiên của UUIDv7). */
  public static boolean inSample(UUID submissionId, int pct) {
    return pct > 0 && Math.floorMod(submissionId.getLeastSignificantBits(), 100) < pct;
  }

  /** Tối thiểu số bài lấy mẫu mỗi bài có chấm (BR-S5E6-17). */
  public static final int MIN_SAMPLE = 2;

  /**
   * Lấy mẫu "{@code pct} % mỗi bài, tối thiểu 2 bài" (BR-S5E6-17): bài trong mẫu theo mã, hoặc là
   * một trong {@link #MIN_SAMPLE} bài nộp đầu tiên của bài có chấm ({@code earlier} = số bài nộp
   * trước nó). Cố định theo bài nộp.
   */
  public static boolean inSample(UUID submissionId, int pct, int earlier) {
    return pct > 0 && (earlier < MIN_SAMPLE || inSample(submissionId, pct));
  }

  /** Bài lấy mẫu chạy song song sau khi lớp đã bật AI đề xuất (QS5-24, BR-S5E6-17). */
  public boolean sampled(UUID orgId, SubInfo s) {
    if (!"ai_proposal".equals(s.gradingMode())) {
      return false;
    }
    if (!inSample(s.id(), samplePct)
        && !inSample(s.id(), samplePct, repo.earlier(orgId, s.assignmentId(), s.id()))) {
      return false;
    }
    ClassAiGateQuery g = gate.getIfAvailable();
    return g != null && g.aiProposalOpen(orgId, s.classId());
  }

  public boolean shadow(UUID orgId, SubInfo s) {
    return "parallel".equals(s.gradingMode()) || sampled(orgId, s);
  }

  /** Đề xuất AI phải ẩn với giáo viên lúc này (song song, chưa duyệt). */
  public boolean hidden(UUID orgId, SubInfo s) {
    return !s.approved() && shadow(orgId, s);
  }

  /**
   * Bài của giáo viên {@code teacherId} (null = bất kỳ) đang phải ẩn đề xuất; bài không thấy hoặc
   * không phải của giáo viên → false (để controller trả NOT_FOUND như cũ).
   */
  public boolean hiddenFor(UUID orgId, UUID teacherId, UUID submissionId) {
    return repo.info(orgId, submissionId)
        .filter(s -> teacherId == null || teacherId.equals(s.teacherId()))
        .map(s -> hidden(orgId, s))
        .orElse(false);
  }
}
