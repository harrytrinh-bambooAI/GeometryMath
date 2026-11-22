package vn.geo.server.content.internal.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.content.internal.repo.ContentRepository;

/**
 * MÓC KIỂM THỬ của E5 (chỉ profile dev/test): nạp một đề mới chưa chạy kiểm chứng vào bộ đề
 * 'kho_moi' — bộ chỉ tổ chức thử "Nhóm thử E5 nạp thêm" (seed 500_e5_catalog.sql) thấy, nên số đếm
 * của các ca khác không đổi (US-GEO-505-SC2, US-GEO-502-FC1).
 */
@Service
@Profile({"dev", "test"})
public class CatalogTestHooks {

  static final String BANK = "kho_moi";

  private final ContentRepository repo;

  public CatalogTestHooks(ContentRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public Map<String, Object> addNewProblem(String statement) {
    String code = String.format("HKM-%02d", repo.countInBank(BANK) + 1);
    UUID author = repo.platformAuthor();
    UUID problemId = Uuid7.next();
    UUID versionId = Uuid7.next();
    Short topic = repo.topicId("Góc").orElse(null);
    repo.insertProblem(problemId, code, "thpt", "pyramid", topic, author, BANK);
    repo.insertVersion(
        versionId, problemId, 1, statement, CatalogImporter.statementHash(statement), author);
    return Map.of("code", code, "problemId", problemId, "versionId", versionId);
  }
}
