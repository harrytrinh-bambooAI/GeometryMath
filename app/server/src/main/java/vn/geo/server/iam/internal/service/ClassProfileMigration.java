package vn.geo.server.iam.internal.service;

import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.iam.internal.repo.ClassRepository;

/**
 * Chuyển hồ sơ lớp MVP đã chuyển ở US-S2E1-04 sang mô hình lớp chặng 3 (US-S3E1-01-SC6,
 * BR-S3E1-16): chủ lớp giữ nguyên (Giáo viên mẫu có mã GV-), sinh mã lớp HK-, lớp nhận học sinh mới
 * bằng mã + duyệt; thành viên hiện có giữ trạng thái Đang học, tiến độ và bài làm không đổi. Chạy
 * lại không tạo thêm lớp, mã hay thành viên. Từ chối khi profile prod bật.
 */
@Service
public class ClassProfileMigration {

  public record Result(boolean refused, int converted) {}

  private final ClassRepository repo;
  private final ClassService classes;

  public ClassProfileMigration(ClassRepository repo, ClassService classes) {
    this.repo = repo;
    this.classes = classes;
  }

  public Result run(Collection<String> activeProfiles, UUID orgId) {
    if (activeProfiles.contains("prod")) {
      return new Result(true, 0);
    }
    int n = 0;
    for (UUID[] c : repo.mvpClassesToConvert(orgId)) {
      n += repo.convertToCodeClass(c[0], c[1], classes.uniqueCode(ClassService::randomCode));
    }
    return new Result(false, n);
  }
}
