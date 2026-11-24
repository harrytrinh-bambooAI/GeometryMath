package vn.geo.server.content.internal.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.content.api.BankAccess;
import vn.geo.server.content.internal.repo.CatalogRepository;

/**
 * Hiện thực guard quyền bộ đề (FIX3): một truy vấn theo khóa chính version + catalog_bank_access.
 */
@Service
public class BankAccessService implements BankAccess {

  private final CatalogRepository repo;

  public BankAccessService(CatalogRepository repo) {
    this.repo = repo;
  }

  @Override
  public boolean visible(UUID problemVersionId) {
    return problemVersionId != null && repo.bankVisible(problemVersionId);
  }

  @Override
  public void requireVisible(UUID problemVersionId) {
    if (!visible(problemVersionId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
  }
}
