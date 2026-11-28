package vn.geo.server.it.s3e1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.http.HttpResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import vn.geo.server.iam.internal.service.ClassProfileMigration;
import vn.geo.server.iam.internal.service.ClassService;
import vn.geo.server.iam.internal.service.ClassTestWorld;
import vn.geo.server.iam.internal.service.ClassTestWorld.ClassSpec;
import vn.geo.server.iam.internal.service.ClassTestWorld.Spec;
import vn.geo.server.iam.internal.service.ClassTestWorld.World;
import vn.geo.server.iam.internal.service.MvpMigration;
import vn.geo.server.it.e1.AbstractE1IT;

/** S3-E1 (T-261126-28): mã lớp không trùng, chuyển hồ sơ lớp MVP, sự kiện bảo mật vì quan hệ. */
class ClassS3E1IT extends AbstractE1IT {

  @Autowired ClassService classes;
  @Autowired ClassTestWorld worlds;
  @Autowired ClassProfileMigration classMigration;
  @Autowired MvpMigration mvpMigration;

  private World world() {
    return worlds.build(
        new Spec(
            List.of("Cô Lan", "Thầy Khôi"),
            List.of(),
            List.of(
                new ClassSpec(
                    "11A1",
                    "Cô Lan",
                    "11A1",
                    11,
                    List.of("Bình An", "Chi Mai"),
                    List.of("Thu Hà"),
                    List.of(),
                    List.of()),
                new ClassSpec(
                    "10B",
                    "Thầy Khôi",
                    "10B",
                    10,
                    List.of("Mi Na"),
                    List.of(),
                    List.of(),
                    List.of()))));
  }

  @Test
  @DisplayName("TC-S3E1-11 Mã lớp sinh không trùng mã đang có")
  void codeGeneratorSkipsTakenCode() {
    World w = world();
    String taken = w.classes().get("11A1").code();
    String fresh;
    do {
      fresh = "HK-" + randomTail();
    } while (count(
            "SELECT count(*) FROM class WHERE join_code = '" + ClassService.storeForm(fresh) + "'")
        > 0);
    Iterator<String> gen = List.of(taken, fresh).iterator();
    String got = classes.uniqueCode(gen::next);
    assertThat(got)
        .isEqualTo(ClassService.storeForm(fresh))
        .isNotEqualTo(ClassService.storeForm(taken));
    UUID teacher = w.people().get("Cô Lan").id();
    assertThatThrownBy(() ->
            jdbc.sql(
                    """
                    INSERT INTO class (id, org_id, name, level, grade, join_code, teacher_id, join_mode)
                    VALUES (:id, :org, 'Trùng', 'thpt', 11, :code, :t, 'code')
                    """)
                .param("id", UUID.randomUUID())
                .param("org", w.orgId())
                .param("code", ClassService.storeForm(taken))
                .param("t", teacher)
                .update())
        .isInstanceOf(DuplicateKeyException.class);
  }

  private static String randomTail() {
    String a = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    StringBuilder b = new StringBuilder();
    java.util.Random r = new java.util.Random();
    for (int i = 0; i < 4; i++) {
      b.append(a.charAt(r.nextInt(a.length())));
    }
    return b.toString();
  }

  @Test
  @DisplayName("TC-S3E1-54 Lớp MVP đã chuyển thành lớp thật có chủ lớp và mã lớp")
  void mvpClassBecomesCodeClass() {
    List<String[]> oldCodes =
        jdbc.sql("SELECT id::text, join_code FROM class WHERE org_id = :org")
            .param("org", ORG_A)
            .query((rs, i) -> new String[] {rs.getString(1), rs.getString(2)})
            .list();
    long active =
        count(
            "SELECT count(*) FROM class_member WHERE class_id = '"
                + CLASS_11A1
                + "' AND status = 'active'");
    long progress =
        count("SELECT count(*) FROM assignment_progress WHERE org_id = '" + ORG_A + "'");
    try {
      mvpMigration.run(List.of("test"), ORG_A);
      ClassProfileMigration.Result first = classMigration.run(List.of("test"), ORG_A);
      assertThat(first.converted()).isGreaterThanOrEqualTo(1);
      String code =
          jdbc.sql(
                  "SELECT join_code || '|' || join_mode || '|' || teacher_id FROM class WHERE id = :id")
              .param("id", CLASS_11A1)
              .query(String.class)
              .single();
      assertThat(code).matches("HK[A-HJ-NP-Z2-9]{4}\\|code\\|" + TEACHER_A);
      assertThat(
              count(
                  "SELECT count(*) FROM class_member WHERE class_id = '"
                      + CLASS_11A1
                      + "' AND status = 'active'"))
          .isEqualTo(active);
      assertThat(count("SELECT count(*) FROM assignment_progress WHERE org_id = '" + ORG_A + "'"))
          .isEqualTo(progress);
      long classesBefore = count("SELECT count(*) FROM class");
      ClassProfileMigration.Result second = classMigration.run(List.of("test"), ORG_A);
      assertThat(second.converted()).isZero();
      assertThat(count("SELECT count(*) FROM class")).isEqualTo(classesBefore);
      assertThat(classMigration.run(List.of("prod"), ORG_A).refused()).isTrue();
    } finally {
      for (String[] c : oldCodes) {
        jdbc.sql("UPDATE class SET join_code = :c, join_mode = 'name' WHERE id = CAST(:id AS uuid)")
            .param("c", c[1])
            .param("id", c[0])
            .update();
      }
      jdbc.sql(
              """
              UPDATE app_user SET user_code = NULL, password_hash = NULL, recovery_hmac = NULL,
                     must_change_password = false WHERE org_id = :org
              """)
          .param("org", ORG_A)
          .update();
    }
  }

  @Test
  @DisplayName("TC-S3E1-53 Mỗi lần từ chối vì quan hệ được ghi để đếm")
  void relationDenialsAreCounted() throws Exception {
    World w = world();
    UUID c11 = w.classes().get("11A1").id();
    UUID c10 = w.classes().get("10B").id();
    String lan = w.people().get("Cô Lan").token();
    String khoi = w.people().get("Thầy Khôi").token();
    String binh = w.people().get("Bình An").token();
    Set<UUID> actors =
        Set.of(
            w.people().get("Cô Lan").id(),
            w.people().get("Thầy Khôi").id(),
            w.people().get("Bình An").id());
    String in = "'" + String.join("','", actors.stream().map(UUID::toString).toList()) + "'";
    assertThat(count("SELECT count(*) FROM relation_denial WHERE actor_id IN (" + in + ")"))
        .isZero();

    List<HttpResponse<String>> rs =
        List.of(
            get("/api/v1/teacher/my-classes/" + c11 + "/students", khoi),
            get("/api/v1/teacher/my-classes", binh),
            get("/api/v1/teacher/my-classes/" + c10 + "/pending", lan),
            get("/api/v1/teacher/my-classes/" + c10, lan),
            get("/api/v1/teacher/my-classes/" + c11 + "/students?cursor=bGE.bGE", lan));
    assertThat(rs.stream().map(HttpResponse::statusCode).toList())
        .containsExactly(404, 403, 404, 404, 400);
    for (HttpResponse<String> r : rs) {
      assertThat(r.body()).doesNotContain("10B").doesNotContain("Mi Na").doesNotContain("Thu Hà");
    }
    assertThat(count("SELECT count(*) FROM relation_denial WHERE actor_id IN (" + in + ")"))
        .as("PO-76: 5 lượt từ chối, tối đa 1 hàng mỗi (người gọi, loại đối tượng) mỗi giờ")
        .isEqualTo(4);
    assertThat(
            count(
                "SELECT count(*) FROM (SELECT DISTINCT actor_id, object_type FROM relation_denial"
                    + " WHERE actor_id IN ("
                    + in
                    + ")) d"))
        .isEqualTo(4);
    Set<String> cols =
        new TreeSet<>(
            jdbc.sql(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = 'relation_denial'")
                .query(String.class)
                .list());
    assertThat(cols)
        .containsExactlyInAnyOrder("id", "org_id", "actor_id", "kind", "object_type", "at");
  }
}
