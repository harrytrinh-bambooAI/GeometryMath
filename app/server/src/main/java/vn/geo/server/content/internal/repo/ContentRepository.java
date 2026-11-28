package vn.geo.server.content.internal.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.PlatformOrg;

/**
 * Nạp kho đề nền tảng và bộ đề chuẩn (vai geo_content, không ở đường người dùng). Mã trường của mọi
 * câu lệnh là tổ chức nền tảng (:orgId = platform_org_id()). Lời giải và báo cáo kiểm chứng chỉ qua
 * hàm cổng (fn_content_put_solution, fn_benchmark_reference, fn_record_verification).
 */
@Repository
public class ContentRepository {

  public record ExistingProblem(UUID problemId, UUID versionId, int version, String statement) {}

  public record SetRow(int version, byte[] hash) {}

  public record Reference(
      String code,
      UUID problemId,
      UUID versionId,
      String statement,
      boolean missingReference,
      String finalAnswer) {}

  private final JdbcClient jdbc;

  public ContentRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public UUID platformAuthor() {
    return jdbc.sql(
            """
            SELECT id FROM app_user WHERE org_id = :orgId AND account_kind = 'system' ORDER BY id LIMIT 1
            -- sqllint:allow=ownership tác giả nền tảng (D-GEO-160) đứng tên đề kho
            """)
        .param("orgId", PlatformOrg.ID)
        .query(UUID.class)
        .single();
  }

  public Optional<Short> topicId(String name) {
    return jdbc.sql("SELECT id FROM problem_topic WHERE subject_id = 1 AND name = :name LIMIT 1")
        .param("name", name)
        .query(Short.class)
        .optional();
  }

  public Optional<ExistingProblem> problemByCode(String code) {
    return jdbc.sql(
            """
            SELECT p.id, pv.id, pv.version, pv.statement
            FROM problem p JOIN problem_version pv ON pv.org_id = :orgId AND pv.id = p.current_version_id
            WHERE p.org_id = :orgId AND p.code = :code
            LIMIT 1
            """)
        .param("orgId", PlatformOrg.ID)
        .param("code", code)
        .query((rs, i) ->
            new ExistingProblem(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getInt(3),
                rs.getString(4)))
        .optional();
  }

  public void insertProblem(
      UUID id, String code, String level, String solidKind, Short topicId, UUID author) {
    jdbc.sql(
            """
            INSERT INTO problem (id, org_id, subject_id, source, author_user_id, level, solid_kind, topic_id, title, code)
            VALUES (:id, :orgId, 1, 'catalog', :author, CAST(:level AS edu_level), CAST(:kind AS solid_kind), :topic,
                    :code, :code)
            """)
        .param("id", id)
        .param("orgId", PlatformOrg.ID)
        .param("author", author)
        .param("level", level)
        .param("kind", solidKind)
        .param("topic", topicId)
        .param("code", code)
        .update();
  }

  /** Đề kho mới thuộc một bộ đề (E5: móc kiểm thử nạp đề vào bộ riêng). */
  public void insertProblem(
      UUID id,
      String code,
      String level,
      String solidKind,
      Short topicId,
      UUID author,
      String bank) {
    insertProblem(id, code, level, solidKind, topicId, author);
    jdbc.sql("UPDATE problem SET bank = :bank WHERE org_id = :orgId AND id = :id")
        .param("bank", bank)
        .param("orgId", PlatformOrg.ID)
        .param("id", id)
        .update();
  }

  /** Số đề kho của một bộ (đặt mã cho đề mới của móc kiểm thử). */
  /** Khóa giao dịch theo bộ đề: các lần thêm đề song song (móc kiểm thử E5) cấp mã lần lượt. */
  public void lockBank(String bank) {
    jdbc.sql("SELECT pg_advisory_xact_lock(hashtext('geo:bank:' || :bank))")
        .param("bank", bank)
        .query()
        .singleRow();
  }

  public long countInBank(String bank) {
    return jdbc.sql(
            """
            SELECT count(*) FROM problem WHERE org_id = :orgId AND bank = :bank
            -- sqllint:allow=unbounded một bộ đề ≤ 60 đề (móc kiểm thử E5)
            """)
        .param("orgId", PlatformOrg.ID)
        .param("bank", bank)
        .query(Long.class)
        .single();
  }

  /** Đưa đề ra khỏi Kho đề (đề thiếu đáp số, US-GEO-504-FC1) hoặc đưa lại khi đã đủ. */
  public void setArchived(String code, boolean archived) {
    jdbc.sql(
            """
            UPDATE problem SET archived_at = CASE WHEN :archived THEN COALESCE(archived_at, now()) END
            WHERE org_id = :orgId AND code = :code
            """)
        .param("archived", archived)
        .param("orgId", PlatformOrg.ID)
        .param("code", code)
        .update();
  }

  /** Lời giải của version trùng lời giải trong tệp nạp (BR-GEO-11: khác thì tạo version mới). */
  public boolean solutionSame(UUID versionId, String finalAnswer, String stepsJson) {
    return Boolean.TRUE.equals(
        jdbc.sql("SELECT fn_content_solution_same(:v, :final, CAST(:steps AS jsonb))")
            .param("v", versionId)
            .param("final", finalAnswer)
            .param("steps", stepsJson)
            .query(Boolean.class)
            .single());
  }

  /** Một version hiện hành của đề kho (mọi bộ) cho bộ kiểm chứng cả kho. */
  public record CatalogRef(
      String code, String bank, UUID problemId, UUID versionId, String finalAnswer) {}

  public List<CatalogRef> catalogReference() {
    return jdbc.sql(
            "SELECT code, bank, problem_id, problem_version_id, final_answer FROM fn_catalog_reference()")
        .query((rs, i) ->
            new CatalogRef(
                rs.getString(1),
                rs.getString(2),
                rs.getObject(3, UUID.class),
                rs.getObject(4, UUID.class),
                rs.getString(5)))
        .list();
  }

  /** Mã đề gốc mà bản dựng của version trỏ tới (đề thử của kho thử, seed E5); không có: rỗng. */
  public Optional<String> specCode(UUID versionId) {
    return jdbc.sql(
            """
            SELECT spec->>'spec_code' FROM scene_build WHERE org_id = :orgId AND problem_version_id = :v
            ORDER BY build_no DESC LIMIT 1
            """)
        .param("orgId", PlatformOrg.ID)
        .param("v", versionId)
        .query((rs, i) -> Optional.ofNullable(rs.getString(1)))
        .optional()
        .flatMap(x -> x);
  }

  public void insertVersion(
      UUID id, UUID problemId, int version, String statement, byte[] hash, UUID author) {
    jdbc.sql(
            """
            INSERT INTO problem_version (id, org_id, problem_id, version, statement, statement_hash, created_by)
            VALUES (:id, :orgId, :problemId, :version, :statement, :hash, :author)
            """)
        .param("id", id)
        .param("orgId", PlatformOrg.ID)
        .param("problemId", problemId)
        .param("version", version)
        .param("statement", statement)
        .param("hash", hash)
        .param("author", author)
        .update();
    jdbc.sql("UPDATE problem SET current_version_id = :v WHERE org_id = :orgId AND id = :problemId")
        .param("v", id)
        .param("orgId", PlatformOrg.ID)
        .param("problemId", problemId)
        .update();
  }

  public void insertHint(UUID versionId, int level, String body) {
    jdbc.sql(
            """
            INSERT INTO hint (org_id, problem_version_id, level, body) VALUES (:orgId, :v, :level, :body)
            ON CONFLICT DO NOTHING
            """)
        .param("orgId", PlatformOrg.ID)
        .param("v", versionId)
        .param("level", (short) level)
        .param("body", body)
        .update();
  }

  public void putSolution(
      UUID versionId,
      String finalAnswer,
      String stepsJson,
      String formsJson,
      String model,
      String prompt) {
    jdbc.sql(
            """
            SELECT fn_content_put_solution(:v, :final, CAST(:steps AS jsonb), CAST(:forms AS jsonb), :model, :prompt)
            """)
        .param("v", versionId)
        .param("final", finalAnswer)
        .param("steps", stepsJson)
        .param("forms", formsJson)
        .param("model", model)
        .param("prompt", prompt)
        .query(String.class)
        .single();
  }

  public Optional<SetRow> latestSet() {
    return jdbc.sql("SELECT version, content_hash FROM benchmark_set ORDER BY version DESC LIMIT 1")
        .query((rs, i) -> new SetRow(rs.getInt(1), rs.getBytes(2)))
        .optional();
  }

  public void insertSet(int version, byte[] hash, String source) {
    jdbc.sql(
            "INSERT INTO benchmark_set (version, content_hash, source) VALUES (:v, :hash, :source)")
        .param("v", version)
        .param("hash", hash)
        .param("source", source)
        .update();
  }

  public void insertItem(int set, String code, UUID problemId, UUID versionId, boolean missing) {
    jdbc.sql(
            """
            INSERT INTO benchmark_item (set_version, code, org_id, problem_id, problem_version_id, missing_reference)
            VALUES (:set, :code, :orgId, :problemId, :v, :missing)
            """)
        .param("set", set)
        .param("code", code)
        .param("orgId", PlatformOrg.ID)
        .param("problemId", problemId)
        .param("v", versionId)
        .param("missing", missing)
        .update();
  }

  public List<Reference> reference(int set) {
    return jdbc.sql(
            """
            SELECT code, problem_id, problem_version_id, statement, missing_reference, final_answer
            FROM fn_benchmark_reference(:set)
            """)
        .param("set", set)
        .query((rs, i) ->
            new Reference(
                rs.getString(1),
                rs.getObject(2, UUID.class),
                rs.getObject(3, UUID.class),
                rs.getString(4),
                rs.getBoolean(5),
                rs.getString(6)))
        .list();
  }

  public String recordVerification(
      UUID versionId, UUID reportId, boolean sceneOk, boolean answerOk, String checksJson) {
    return jdbc.sql(
            "SELECT fn_record_verification(:v, :r, :scene, :answer, CAST(:checks AS jsonb))::text")
        .param("v", versionId)
        .param("r", reportId)
        .param("scene", sceneOk)
        .param("answer", answerOk)
        .param("checks", checksJson)
        .query(String.class)
        .single();
  }

  public void insertRun(
      UUID id,
      Integer set,
      String model,
      String promptVersion,
      boolean simulated,
      int n,
      int sceneOk,
      int answerOk,
      int wrongVerified,
      int verified,
      String itemsJson,
      String report) {
    jdbc.sql(
            """
            INSERT INTO benchmark_run (id, set_version, model, prompt_version, simulated, n, scene_ok, answer_ok,
                                       wrong_verified, verified, items, report)
            VALUES (:id, :set, :model, :prompt, :sim, :n, :scene, :answer, :wrong, :verified, CAST(:items AS jsonb), :report)
            """)
        .param("id", id)
        .param("set", set)
        .param("model", model)
        .param("prompt", promptVersion)
        .param("sim", simulated)
        .param("n", n)
        .param("scene", sceneOk)
        .param("answer", answerOk)
        .param("wrong", wrongVerified)
        .param("verified", verified)
        .param("items", itemsJson)
        .param("report", report)
        .update();
  }
}
