package vn.geo.server.it.s3e5;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import vn.geo.server.common.security.Role;
import vn.geo.server.it.e1.AbstractE1IT;
import vn.geo.server.quality.AllowedFields;
import vn.geo.server.quality.ApiKeyScanner;

/**
 * US-S3E5-03: quét cột cơ sở dữ liệu (information_schema), gọi API chặng 3 thật bằng dữ liệu mẫu
 * F-S3 và quét nhật ký / giá trị, đối chiếu với {@code quality/allowed_fields.tsv}.
 */
class AllowedFieldsS3E5IT extends AbstractE1IT {

  static final UUID COMMUNITY = UUID.fromString("00000000-0000-7000-8002-000000000100");
  static final UUID LAN = s3(1);
  static final UUID BINH_AN = s3(20);
  static final UUID C11A1 = s3(10);
  static final UUID SET15 = s3(50);
  static final Pattern UUIDISH = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-.*|\\d+");
  static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
  static final Pattern PHONE = Pattern.compile("(?<![0-9])0[0-9]{9}(?![0-9])");

  @Autowired TransactionTemplate tx;

  @Value("${geo.s3e5.access-log-max-days}")
  int accessLogMaxDays;

  @Value("${server.tomcat.accesslog.enabled:false}")
  boolean tomcatAccessLog;

  static UUID s3(long n) {
    return UUID.fromString(String.format("00000000-0000-7000-8003-%012d", n));
  }

  /** Đăng ký từ một địa chỉ mạng riêng (bộ đếm đăng ký theo mạng không chặn ca kiểm). */
  HttpResponse<String> reg(String body) throws Exception {
    java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
    String ip = "10." + r.nextInt(1, 255) + "." + r.nextInt(1, 255) + "." + r.nextInt(1, 255);
    return postJson("/api/v1/auth/register", null, body, "X-Forwarded-For", ip);
  }

  List<String> columns() {
    return jdbc.sql(
            """
            SELECT c.table_name || '.' || c.column_name FROM information_schema.columns c
            JOIN information_schema.tables t USING (table_schema, table_name)
            WHERE c.table_schema = 'public' AND t.table_type = 'BASE TABLE'
            """)
        .query(String.class)
        .list();
  }

  /** Gọi các API GET chặng 3 bằng dữ liệu mẫu, gom mọi khóa (bỏ khóa động kiểu mã/ số). */
  Map<String, String> liveKeys() throws Exception {
    String lan = token(COMMUNITY, LAN, Role.TEACHER);
    String an = token(COMMUNITY, BINH_AN, Role.STUDENT);
    List<String[]> calls =
        List.of(
            new String[] {"/api/v1/account/me", lan},
            new String[] {"/api/v1/account/me", an},
            new String[] {"/api/v1/account/privacy", an},
            new String[] {"/api/v1/auth/privacy-notice", null},
            new String[] {"/api/v1/teacher/my-classes", lan},
            new String[] {"/api/v1/teacher/my-classes/" + C11A1, lan},
            new String[] {"/api/v1/teacher/my-classes/" + C11A1 + "/pending", lan},
            new String[] {"/api/v1/teacher/my-classes/" + C11A1 + "/students", lan},
            new String[] {"/api/v1/teacher/my-classes/" + C11A1 + "/students/" + BINH_AN, lan},
            new String[] {"/api/v1/student/classes", an},
            new String[] {"/api/v1/student/class-ai/" + C11A1, an},
            new String[] {"/api/v1/teacher/assignment-sets", lan},
            new String[] {"/api/v1/teacher/assignment-sets/options", lan},
            new String[] {"/api/v1/teacher/assignment-sets/" + SET15, lan},
            new String[] {"/api/v1/teacher/assignment-sets/" + SET15 + "/follow", lan},
            new String[] {"/api/v1/student/assignment-sets", an},
            new String[] {"/api/v1/student/assignment-sets/" + SET15, an},
            new String[] {"/api/v1/teacher/overview", lan},
            new String[] {"/api/v1/teacher/classes/" + C11A1 + "/audit-events", lan},
            new String[] {"/api/v1/teacher/class-results/classes", lan},
            new String[] {"/api/v1/teacher/class-results/" + C11A1 + "/sets", lan},
            new String[] {
              "/api/v1/teacher/class-results/" + C11A1 + "/sets/" + SET15 + "/board", lan
            },
            new String[] {
              "/api/v1/teacher/class-results/" + C11A1 + "/sets/" + SET15 + "/stuck", lan
            },
            new String[] {"/api/v1/notifications", an},
            new String[] {"/api/v1/notifications", lan},
            new String[] {"/api/v1/notifications/unread-count", an});
    Map<String, String> keys = new TreeMap<>();
    for (String[] c : calls) {
      HttpResponse<String> r = get(c[0], c[1]);
      assertThat(r.statusCode()).as(c[0] + " " + r.body()).isLessThan(500);
      collect(mapper.readTree(r.body()), keys, c[0]);
    }
    return keys;
  }

  static void collect(JsonNode n, Map<String, String> out, String where) {
    if (n == null) {
      return;
    }
    if (n.isArray()) {
      n.forEach(x -> collect(x, out, where));
    } else if (n.isObject()) {
      for (String k : n.propertyNames()) {
        if (!UUIDISH.matcher(k).matches()) {
          out.putIfAbsent(k, where);
        }
        collect(n.get(k), out, where);
      }
    }
  }

  @Test
  @DisplayName("TC-S3E5-27 Mọi cột, khóa phản hồi và khóa yêu cầu đều trong danh sách")
  void everyColumnAndKeyIsAllowed() throws Exception {
    AllowedFields allowed = AllowedFields.load();
    List<String> cols = columns();
    Map<String, String> outsideDb = allowed.outsideDb(cols);
    Map<String, String> staticKeys = ApiKeyScanner.scan();
    Map<String, String> live = liveKeys();
    Set<String> keys = new TreeSet<>(staticKeys.keySet());
    keys.addAll(live.keySet());
    Map<String, String> outsideApi = allowed.outsideApi(keys);
    System.out.printf(
        "allowed_fields: quét %d cột, %d khóa API (tĩnh %d, gọi thật %d): %d + %d ngoài danh sách%n",
        cols.size(),
        keys.size(),
        staticKeys.size(),
        live.size(),
        outsideDb.size(),
        outsideApi.size());
    assertThat(outsideDb).as("cột ngoài danh sách").isEmpty();
    assertThat(outsideApi).as("khóa API ngoài danh sách (gọi thật: %s)", live).isEmpty();
    assertThat(cols.size()).isGreaterThan(500);
    assertThat(live).doesNotContainKeys("orgId", "org_id", "schoolName", "email", "phone");
  }

  @Test
  @DisplayName("TC-S3E5-28 Cột contact_note thêm vào bảng người dùng thì test đỏ; bỏ đi thì xanh")
  void fakeColumnIsReported() {
    AllowedFields allowed = AllowedFields.load();
    Map<String, String> during =
        tx.execute(s -> {
          jdbc.sql("ALTER TABLE app_user ADD COLUMN contact_note text").update();
          Map<String, String> r = allowed.outsideDb(columns());
          s.setRollbackOnly();
          return r;
        });
    assertThat(during).containsOnlyKeys("app_user.contact_note");
    assertThat(during.get("app_user.contact_note")).contains("ngoài danh sách");
    assertThat(allowed.outsideDb(columns())).isEmpty();
  }

  @Test
  @DisplayName("TC-S3E5-32 Nhật ký ứng dụng không chứa email hay số điện thoại")
  void logsHaveNoEmailOrPhone() throws Exception {
    Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    ListAppender<ILoggingEvent> logs = new ListAppender<>();
    logs.start();
    root.addAppender(logs);
    String question = "Em hỏi bí mật số " + UUID.randomUUID();
    try {
      String base =
          "\"role\":\"student\",\"password\":\"MatKhau#2026\",\"privacyAck\":true,\"privacyVersion\":\"1.0\"";
      HttpResponse<String> withEmail =
          reg("{\"displayName\":\"Mai\"," + base + ",\"email\":\"mai@example.org\"}");
      HttpResponse<String> withPhone =
          reg("{\"displayName\":\"Mai\"," + base + ",\"phone\":\"0912345678\"}");
      HttpResponse<String> nameEmail = reg("{\"displayName\":\"mai@example.org\"," + base + "}");
      assertThat(List.of(withEmail.statusCode(), withPhone.statusCode(), nameEmail.statusCode()))
          .containsOnly(400);
      post(
          "/api/v1/student/assignments/" + BG1 + "/questions",
          an(),
          "{\"question\":\"" + question + "\",\"requestId\":\"" + UUID.randomUUID() + "\"}");
    } finally {
      root.detachAppender(logs);
    }
    List<String> lines = new ArrayList<>();
    for (ILoggingEvent e : logs.list) {
      lines.add(
          e.getFormattedMessage()
              + (e.getThrowableProxy() == null ? "" : " " + e.getThrowableProxy().getMessage()));
    }
    assertThat(lines).noneMatch(l -> EMAIL.matcher(l).find());
    assertThat(lines).noneMatch(l -> PHONE.matcher(l).find());
    assertThat(lines).noneMatch(l -> l.contains(question));
    assertThat(count("SELECT count(*) FROM app_user WHERE display_name LIKE '%example.org%'"))
        .isZero();
  }

  /** Mọi cột chữ của mọi bảng ứng dụng: số hàng khớp mẫu. */
  Map<String, Long> valueMatches(String regex) {
    List<String[]> textCols =
        jdbc.sql(
                """
                SELECT c.table_name, c.column_name FROM information_schema.columns c
                JOIN information_schema.tables t USING (table_schema, table_name)
                WHERE c.table_schema = 'public' AND t.table_type = 'BASE TABLE'
                  AND c.data_type IN ('text', 'character varying', 'jsonb', 'json')
                  AND c.table_name <> 'flyway_schema_history'
                """)
            .query((rs, i) -> new String[] {rs.getString(1), rs.getString(2)})
            .list();
    Map<String, Long> hits = new TreeMap<>();
    for (String[] tc : textCols) {
      long n =
          jdbc.sql("SELECT count(*) FROM \"" + tc[0] + "\" WHERE \"" + tc[1] + "\"::text ~ :re")
              .param("re", regex)
              .query(Long.class)
              .single();
      if (n > 0) {
        hits.put(tc[0] + "." + tc[1], n);
      }
    }
    return hits;
  }

  @Test
  @DisplayName("TC-S3E5-37 Không bảng ứng dụng nào lưu IP hay thiết bị")
  void noIpOrDeviceStored() throws Exception {
    // một lượt đăng ký thật có địa chỉ mạng và chuỗi trình duyệt
    postJson(
        "/api/v1/auth/register",
        null,
        "{\"displayName\":\"Thử IP\",\"role\":\"student\",\"password\":\"MatKhau#2026\",\"privacyAck\":true,\"privacyVersion\":\"1.0\"}",
        "X-Forwarded-For",
        "10.251.17.3",
        "User-Agent",
        "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15");
    assertThat(columns())
        .noneMatch(c ->
            vn.geo.server.iam.internal.code.FieldNames.tokens(c).contains("ip")
                && !c.equals("auth_failure.ip_key"))
        .noneMatch(c -> c.contains("device") || c.contains("user_agent") || c.contains("browser"));
    assertThat(valueMatches("(^|[^0-9.])([0-9]{1,3}\\.){3}[0-9]{1,3}($|[^0-9.])"))
        .as("địa chỉ IP")
        .isEmpty();
    assertThat(valueMatches("Mozilla/|AppleWebKit|iPhone OS|CPU OS"))
        .as("chuỗi trình duyệt")
        .isEmpty();
    // máy chủ web: nhật ký truy cập tắt hoặc giữ không quá 7 ngày
    assertThat(accessLogMaxDays).isBetween(1, 7);
    assertThat(tomcatAccessLog).isFalse();
  }

  @Test
  @DisplayName("TC-S3E5-38 Bảng sao lưu và sự kiện đo cũng bị quét")
  void measurementTablesAreScanned() {
    AllowedFields allowed = AllowedFields.load();
    List<String> measure =
        columns().stream()
            .filter(c ->
                c.matches(
                    "(event_log|problem_create_event|export_confirm_log|ai_call_log|answer_guard_log|notification|audit_log)[a-z0-9_]*\\..*"))
            .toList();
    assertThat(measure).isNotEmpty();
    assertThat(allowed.outsideDb(measure)).isEmpty();
    // sự kiện đo không chứa tên hiển thị hay nội dung câu hỏi
    assertThat(
            count(
                """
                SELECT count(*) FROM event_log e JOIN app_user u ON length(u.display_name) >= 4
                 AND e.arg IS NOT NULL AND position(u.display_name IN e.arg::text) > 0
                """))
        .isZero();
    assertThat(
            count(
                """
                SELECT count(*) FROM event_log e JOIN qa_turn q ON length(q.question) >= 8
                 AND e.arg IS NOT NULL AND position(q.question IN e.arg::text) > 0
                """))
        .isZero();
    // sự kiện đo tạo đề chỉ mang mã người dùng của giáo viên
    assertThat(
            count(
                "SELECT count(*) FROM problem_create_event e JOIN app_user u ON u.id = e.user_id WHERE u.role <> 'teacher'"))
        .isZero();
  }

  @Test
  @DisplayName(
      "US-S3E5-03 SC3/FC1–FC4/FC7: thân yêu cầu có trường thừa hay tên hiển thị như email/điện thoại bị từ chối")
  void requestsWithExtraFieldsAreRejected() throws Exception {
    String base =
        "\"role\":\"student\",\"password\":\"MatKhau#2026\",\"privacyAck\":true,\"privacyVersion\":\"1.0\"";
    long before = count("SELECT count(*) FROM app_user");
    HttpResponse<String> email =
        reg("{\"displayName\":\"Mai\"," + base + ",\"email\":\"mai@example.org\"}");
    assertThat(email.statusCode()).isEqualTo(400);
    assertThat(json(email).get("code").asString()).isEqualTo("VALIDATION_FAILED");
    assertThat(json(email).get("details").get(0).get("field").asString()).isEqualTo("email");
    HttpResponse<String> phone =
        reg("{\"displayName\":\"Mai\"," + base + ",\"phone\":\"0912345678\"}");
    assertThat(json(phone).get("details").get(0).get("field").asString()).isEqualTo("phone");
    HttpResponse<String> nine = reg("{\"displayName\":\"0912 345 678\"," + base + "}");
    assertThat(json(nine).get("details").get(0).get("message").asString())
        .isEqualTo("Tên hiển thị không nên là email hay số điện thoại.");
    assertThat(count("SELECT count(*) FROM app_user")).isEqualTo(before);
    for (String ok :
        List.of("Lớp 12345678", "Minh 10A", "Nguyễn Thị Ánh Tuyết", "Đức-Huy (11A1)")) {
      HttpResponse<String> r = reg("{\"displayName\":\"" + ok + "\"," + base + "}");
      assertThat(r.statusCode()).as(ok + " " + r.body()).isEqualTo(200);
      assertThat(json(r).get("displayName").asString()).isEqualTo(ok);
    }
    String lan = token(COMMUNITY, LAN, Role.TEACHER);
    long classes = count("SELECT count(*) FROM class");
    for (String extra :
        List.of(
            "\"schoolName\":\"THPT Mẫu A\"",
            "\"Truong\":\"THPT Mẫu A\"",
            "\"org_id\":\"" + COMMUNITY + "\"")) {
      HttpResponse<String> r =
          post("/api/v1/teacher/my-classes", lan, "{\"name\":\"12C\",\"grade\":12," + extra + "}");
      assertThat(r.statusCode()).as(extra + " " + r.body()).isEqualTo(400);
      assertThat(json(r).get("code").asString()).isEqualTo("VALIDATION_FAILED");
    }
    assertThat(count("SELECT count(*) FROM class")).isEqualTo(classes);
    assertThat(count("SELECT count(*) FROM class WHERE name LIKE '%Mẫu A%'")).isZero();
  }
}
