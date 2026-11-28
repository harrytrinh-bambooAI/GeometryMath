package vn.geo.server.assessment.internal.assign.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import vn.geo.server.assessment.api.ClassAiGateQuery;

/**
 * Stub cổng "AI đề xuất" LUÔN ĐÓNG (vòng mềm US-S5E4-02 ↔ US-S5E6-06, R5 của PLAN_code-s5): chưa có
 * kết quả chạy song song nên không lớp nào đạt ngưỡng. T-261128-65 thay bằng bean {@code @Primary}.
 */
@Component
public class ClosedClassAiGate implements ClassAiGateQuery {

  @Override
  public boolean aiProposalReady(UUID orgId, UUID classId) {
    return false;
  }
}
