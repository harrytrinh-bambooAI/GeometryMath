package vn.geo.server.iam.internal.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.iam.api.ClassAiPolicy;
import vn.geo.server.iam.internal.repo.ClassRepository;

/** Công tắc trợ lý AI theo lớp (US-S3E5-04): đọc ở mỗi lượt gọi, không cache (BR-S3E5-45). */
@Service
public class ClassAiPolicyService implements ClassAiPolicy {

  private final ClassRepository repo;
  private final RelationDenials denials;

  public ClassAiPolicyService(ClassRepository repo, RelationDenials denials) {
    this.repo = repo;
    this.denials = denials;
  }

  @Override
  public boolean blockedForStudent(UUID orgId, UUID userId, UUID assignmentId) {
    return repo.aiBlocked(orgId, userId, assignmentId);
  }

  /** Màn làm bài của học sinh hỏi để ẩn khung trợ lý (US-S3E5-04-SC1). */
  public ClassService.AiStatus statusFor(UUID assignmentId) {
    AuthPrincipal p = denials.require(Role.STUDENT, "class_ai");
    return new ClassService.AiStatus(!repo.aiBlocked(p.orgId(), p.userId(), assignmentId));
  }
}
