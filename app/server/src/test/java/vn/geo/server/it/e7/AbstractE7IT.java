package vn.geo.server.it.e7;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.it.e1.AbstractE1IT;

/**
 * Nền IT của E7: dựng trường, giáo viên, lớp, học sinh, bài giao riêng cho từng ca (đi qua đúng hàm
 * cổng fn_student_login, fn_create_assignment) và ghi sự kiện qua fn_record_batch,
 * fn_record_durable với thời điểm cho trước.
 */
abstract class AbstractE7IT extends AbstractE1IT {

  record Klass(UUID org, UUID teacher, UUID id, List<UUID> students) {}

  /** Trường thử riêng (kiểu group) + Giáo viên mẫu. */
  UUID[] newOrg() {
    UUID org = Uuid7.next();
    UUID teacher = Uuid7.next();
    jdbc.sql(
            "INSERT INTO org (id, parent_id, kind, name) VALUES (:id, platform_org_id(), 'group', 'Trường thử E7 IT')")
        .param("id", org)
        .update();
    jdbc.sql(
            "INSERT INTO app_user (id, org_id, role, account_kind, display_name) VALUES (:id, :org, 'teacher', 'sample', 'Giáo viên mẫu')")
        .param("id", teacher)
        .param("org", org)
        .update();
    return new UUID[] {org, teacher};
  }

  Klass newClass(UUID org, UUID teacher, int students) {
    UUID cls = Uuid7.next();
    StringBuilder b = new StringBuilder();
    java.util.Random rnd = new java.security.SecureRandom();
    for (int i = 0; i < 6; i++) {
      b.append("ABCDEFGHJKLMNPQRSTUVWXYZ23456789".charAt(rnd.nextInt(32)));
    }
    String code = b.toString();
    jdbc.sql(
            """
            INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id)
            VALUES (:id, :org, :name, 'thpt', 11, :code, :teacher)
            """)
        .param("id", cls)
        .param("org", org)
        .param("name", "Lớp E7 " + students)
        .param("code", code)
        .param("teacher", teacher)
        .update();
    List<UUID> ids = new ArrayList<>();
    for (int i = 1; i <= students; i++) {
      ids.add(
          jdbc.sql("SELECT user_id FROM fn_student_login(:code, :key, :name, :id)")
              .param("code", code)
              .param("key", "hoc sinh " + i)
              .param("name", "Học sinh " + i)
              .param("id", Uuid7.next())
              .query(UUID.class)
              .single());
    }
    return new Klass(org, teacher, cls, ids);
  }

  UUID assign(Klass k) {
    UUID id = Uuid7.next();
    String s =
        jdbc.sql(
                "SELECT status FROM fn_create_assignment(:org, :teacher, :id, :cls, :pv, now() + interval '1 hour', true, true, NULL)")
            .param("org", k.org())
            .param("teacher", k.teacher())
            .param("id", id)
            .param("cls", k.id())
            .param("pv", HKG01_V)
            .query(String.class)
            .single();
    if (!"OK".equals(s)) {
      throw new IllegalStateException("không giao được bài thử: " + s);
    }
    return id;
  }

  void open(UUID org, UUID user, UUID asg, Instant at) {
    jdbc.sql("SELECT fn_record_durable(:org, :user, :asg, :rq, 'open', :at)")
        .param("org", org)
        .param("user", user)
        .param("asg", asg)
        .param("rq", Uuid7.next())
        .param("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
        .query(String.class)
        .single();
  }

  void tool(UUID org, UUID user, UUID asg, Instant at) {
    jdbc.sql(
            "SELECT status FROM fn_record_batch(:org, :user, :asg, ARRAY[CAST(:at AS timestamptz)], ARRAY['tool'::event_kind], ARRAY[6::smallint])")
        .param("org", org)
        .param("user", user)
        .param("asg", asg)
        .param("at", OffsetDateTime.ofInstant(at, ZoneOffset.UTC))
        .query(String.class)
        .single();
  }

  int activeSeconds(UUID org, UUID asg, UUID user) {
    return jdbc.sql(
            "SELECT active_seconds FROM assignment_progress WHERE org_id = :org AND assignment_id = :asg AND user_id = :user")
        .param("org", org)
        .param("asg", asg)
        .param("user", user)
        .query(Integer.class)
        .single();
  }
}
