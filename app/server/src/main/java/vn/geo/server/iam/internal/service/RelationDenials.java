package vn.geo.server.iam.internal.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;
import vn.geo.server.iam.internal.repo.ClassRepository;

/**
 * Sự kiện bảo mật khi bị từ chối vì quan hệ (BR-S3E1-51, 54; M-S3-06): ghi trong giao dịch riêng
 * (lỗi ném ra sau không làm mất bản ghi) rồi trả ngoại lệ để ném. Bản ghi chỉ có loại, người gọi,
 * loại đối tượng, giờ máy chủ.
 */
@Component
public class RelationDenials {

  public static final String MSG_NOT_FOUND = "Không tìm thấy nội dung này.";

  private final ClassRepository repo;
  private final TransactionTemplate tx;

  public RelationDenials(ClassRepository repo, PlatformTransactionManager tm) {
    this.repo = repo;
    this.tx = new TransactionTemplate(tm);
    this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  public AppException notFound(AuthPrincipal p, String objectType) {
    record(p, "NOT_FOUND", objectType);
    return new AppException(ErrorCode.NOT_FOUND, MSG_NOT_FOUND);
  }

  public AppException forbidden(AuthPrincipal p, String objectType) {
    record(p, "FORBIDDEN", objectType);
    return new AppException(ErrorCode.FORBIDDEN);
  }

  public AppException invalidCursor(AuthPrincipal p, String objectType) {
    record(p, "INVALID_CURSOR", objectType);
    return new AppException(ErrorCode.INVALID_CURSOR);
  }

  /** Danh tính hiện tại nếu đúng vai; sai vai ghi sự kiện rồi FORBIDDEN. */
  public AuthPrincipal require(Role role, String objectType) {
    AuthPrincipal p = RequestContext.principal();
    if (p.role() != role) {
      throw forbidden(p, objectType);
    }
    return p;
  }

  private void record(AuthPrincipal p, String kind, String objectType) {
    UUID id = Uuid7.next();
    tx.executeWithoutResult(s -> repo.recordDenial(p.orgId(), id, p.userId(), kind, objectType));
  }
}
