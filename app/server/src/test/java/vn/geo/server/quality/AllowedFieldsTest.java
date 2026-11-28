package vn.geo.server.quality;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.iam.internal.code.FieldNames;

/**
 * US-S3E5-03 phần quét tĩnh (chạy trong {@code mvn test}, không cần hạ tầng): tệp danh sách, mẫu
 * tên cấm, mọi khóa JSON của API thật. Phần cơ sở dữ liệu và gọi API thật: {@code
 * it/s3e5/AllowedFieldsS3E5IT}.
 */
class AllowedFieldsTest {

  /** DTO giả có khóa ngoài danh sách (TC-S3E5-28). */
  record FakeReply(String displayName, String parentName) {}

  @Test
  @DisplayName("TC-S3E5-30 Mỗi dòng danh sách phải có lý do và mã quyết định")
  void everyLineHasReasonAndDecision() throws Exception {
    List<String> lines = new ArrayList<>(Files.readAllLines(AllowedFields.FILE));
    assertThat(AllowedFields.parse(lines).lint()).as("tệp hiện tại").isEmpty();
    assertThat(AllowedFields.parse(lines).entries())
        .allSatisfy(e -> {
          assertThat(e.reason()).isNotBlank();
          assertThat(e.decision()).matches("PO-\\d{2,3}");
        });
    lines.add("db\tclass\tnote_one\tGhi chú của lớp\t");
    lines.add("db\tclass\tnote_two\t\tPO-61");
    int n = lines.size();
    List<String> problems = AllowedFields.parse(lines).lint();
    assertThat(problems)
        .containsExactlyInAnyOrder(
            "dòng " + (n - 1) + " (class.note_one): thiếu mã quyết định dạng PO-nn",
            "dòng " + n + " (class.note_two): thiếu lý do");
  }

  @Test
  @DisplayName("TC-S3E5-31 Tên trường cấm bị chặn mọi dạng viết")
  void forbiddenNamesAreBlockedInEveryForm() {
    AllowedFields allowed = AllowedFields.load();
    List<String> names =
        List.of(
            "Email",
            "e-mail",
            "phoneNumber",
            "so_dien_thoai",
            "dob",
            "ngay_sinh",
            "birthday",
            "truong",
            "school_name",
            "anh_dai_dien",
            "avatarUrl",
            "ipAddress",
            "deviceId");
    for (String name : names) {
      assertThat(FieldNames.forbiddenPattern(name)).as(name).isPresent();
      // làm cột giả của bảng người dùng
      Map<String, String> asColumn = allowed.outsideDb(List.of("app_user." + name));
      assertThat(asColumn).as("cột " + name).containsOnlyKeys("app_user." + name);
      assertThat(asColumn.get("app_user." + name)).contains("mẫu cấm");
      // làm khóa phản hồi giả
      Map<String, String> asKey = allowed.outsideApi(List.of(name));
      assertThat(asKey).as("khóa " + name).containsOnlyKeys(name);
      assertThat(asKey.get(name)).contains("mẫu cấm");
    }
    // tên thường không bị bắt nhầm
    for (String ok : List.of("displayName", "description", "tip", "canh", "shipping", "title")) {
      assertThat(FieldNames.forbiddenPattern(ok)).as(ok).isEmpty();
    }
  }

  @Test
  @DisplayName("TC-S3E5-28 Khóa parentName của DTO giả ngoài danh sách thì test đỏ")
  void fakeDtoKeyIsReported() {
    AllowedFields allowed = AllowedFields.load();
    Map<String, String> fake = ApiKeyScanner.keysOf(FakeReply.class);
    assertThat(fake).containsOnlyKeys("displayName", "parentName");
    assertThat(allowed.outsideApi(fake.keySet())).containsOnlyKeys("parentName");
    // bỏ DTO giả: khóa thật của API xanh
    assertThat(allowed.outsideApi(ApiKeyScanner.scan().keySet())).isEmpty();
  }

  @Test
  @DisplayName(
      "US-S3E5-03 Mọi khóa phản hồi và thân yêu cầu của API (quét tĩnh) có trong danh sách")
  void everyApiKeyIsAllowed() throws Exception {
    Map<String, String> keys = ApiKeyScanner.scan();
    AllowedFields allowed = AllowedFields.load();
    Map<String, String> outside = allowed.outsideApi(keys.keySet());
    String dump = System.getProperty("allowedFields.dump");
    if (dump != null) {
      List<String> rows = new ArrayList<>();
      keys.forEach((k, where) -> rows.add(k + "\t" + where));
      Files.write(Path.of(dump), rows);
    }
    StringBuilder msg = new StringBuilder("khóa ngoài danh sách:\n");
    outside.forEach((k, why) ->
        msg.append("  ")
            .append(k)
            .append(" — ")
            .append(why)
            .append(" (")
            .append(keys.get(k))
            .append(")\n"));
    System.out.println(
        "allowed_fields: quét "
            + keys.size()
            + " khóa API, "
            + outside.size()
            + " ngoài danh sách");
    assertThat(outside).as(msg.toString()).isEmpty();
    assertThat(keys.size()).isGreaterThan(100);
  }
}
