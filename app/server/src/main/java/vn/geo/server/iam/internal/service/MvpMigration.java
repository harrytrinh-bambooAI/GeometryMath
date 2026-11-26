package vn.geo.server.iam.internal.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.code.UserCodes;
import vn.geo.server.iam.internal.repo.MvpMigrationRepository;
import vn.geo.server.iam.internal.repo.MvpMigrationRepository.Candidate;

/**
 * Chuyển tài khoản mẫu MVP sang mô hình mã người dùng (US-S2E1-04, BR-S2E1-13…15): mỗi học sinh của
 * lớp MVP (cặp tên + lớp) và mỗi "Giáo viên mẫu" nhận mã GV-/HS-, mật khẩu tạm, mã khôi phục, cờ
 * phải đặt mật khẩu mới. Giữ nguyên id nên lớp, tiến độ, bài làm không đổi. Chạy lại không tạo
 * trùng (hàng đã có mã ghi "đã chuyển trước đó"). Dòng lỗi (tên rỗng) vào báo cáo, không dừng lô;
 * mã thoát ≠ 0. Từ chối chạy khi profile prod bật (PO-51): không đọc, không ghi.
 */
@Service
public class MvpMigration {

  public static final String REFUSED =
      "Từ chối: lệnh chuyển dữ liệu MVP chỉ chạy ở dev/test, không chạy trên prod (PO-51).";
  public static final String ALREADY = "đã chuyển trước đó";

  public enum Status {
    CONVERTED,
    ALREADY,
    ERROR
  }

  /** Một dòng báo cáo; mật khẩu tạm và mã khôi phục chỉ có ở dòng vừa chuyển (in một lần). */
  public record Row(
      Status status,
      UUID userId,
      String role,
      String displayName,
      String className,
      String userCode,
      String tempPassword,
      String recoveryCode,
      String reason) {}

  public record Report(boolean refused, String message, List<Row> rows) {
    public long count(Status s) {
      return rows.stream().filter(r -> r.status() == s).count();
    }

    /** 0: xong không lỗi · 1: có dòng lỗi · 3: từ chối (prod). */
    public int exitCode() {
      return refused ? 3 : count(Status.ERROR) > 0 ? 1 : 0;
    }

    /** Báo cáo dạng CSV (dấu chấm phẩy) cho giáo viên/người vận hành. */
    public String toCsv() {
      StringBuilder b = new StringBuilder();
      b.append("trang_thai;vai;lop;ten;ma_nguoi_dung;mat_khau_tam;ma_khoi_phuc;ly_do\n");
      if (refused) {
        b.append("TU_CHOI;;;;;;;").append(message).append('\n');
      }
      for (Row r : rows) {
        b.append(r.status())
            .append(';')
            .append(nz(r.role()))
            .append(';')
            .append(nz(r.className()))
            .append(';')
            .append(nz(r.displayName()).replace(';', ','))
            .append(';')
            .append(nz(r.userCode()))
            .append(';')
            .append(nz(r.tempPassword()))
            .append(';')
            .append(nz(r.recoveryCode()))
            .append(';')
            .append(nz(r.reason()))
            .append('\n');
      }
      return b.toString();
    }

    private static String nz(String s) {
      return s == null ? "" : s;
    }
  }

  private final MvpMigrationRepository repo;
  private final PasswordHasher hasher;

  public MvpMigration(MvpMigrationRepository repo, PasswordHasher hasher) {
    this.repo = repo;
    this.hasher = hasher;
  }

  /**
   * @param activeProfiles profile Spring đang bật (có "prod" thì từ chối ngay)
   * @param orgId chỉ chuyển một trường (null = mọi trường)
   */
  public Report run(Collection<String> activeProfiles, UUID orgId) {
    if (activeProfiles.contains("prod")) {
      return new Report(true, REFUSED, List.of());
    }
    List<Row> rows = new ArrayList<>();
    for (Candidate c : repo.candidates(orgId)) {
      rows.add(convert(c));
    }
    return new Report(false, "", rows);
  }

  private Row convert(Candidate c) {
    boolean teacher = "teacher".equals(c.role());
    if (c.userCode() != null) {
      return row(Status.ALREADY, c, c.userCode(), null, null, ALREADY);
    }
    if (c.displayName() == null || c.displayName().isBlank()) {
      return row(Status.ERROR, c, null, null, null, "tên hiển thị rỗng");
    }
    String temp = UserCodes.newTempPassword();
    String recovery = UserCodes.newRecoveryCode();
    String hash = hasher.hash(temp);
    byte[] rc = hasher.recoveryHmac(c.id(), recovery);
    for (int attempt = 0; attempt < 20; attempt++) {
      String code = UserCodes.newUserCode(teacher);
      try {
        if (repo.assign(c.orgId(), c.id(), code, hash, rc)) {
          return row(Status.CONVERTED, c, code, temp, recovery, null);
        }
        return row(Status.ALREADY, c, null, null, null, ALREADY); // chạy song song đã gắn trước
      } catch (DuplicateKeyException dup) {
        // mã trùng (rất hiếm): sinh mã khác
      }
    }
    return row(Status.ERROR, c, null, null, null, "không sinh được mã người dùng duy nhất");
  }

  private static Row row(
      Status s, Candidate c, String code, String temp, String recovery, String reason) {
    return new Row(
        s, c.id(), c.role(), c.displayName(), c.className(), code, temp, recovery, reason);
  }
}
