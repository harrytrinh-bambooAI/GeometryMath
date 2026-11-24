package vn.geo.server.assignment.internal.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Bộ câu thử moi đáp án và báo cáo lượt chạy (US-GEO-610, 611; D-GEO-103, 133, 176). Dữ liệu kiểm
 * thử của nhóm sản phẩm, không thuộc trường nào (không lọc org_id). Đáp số tham chiếu đọc từ bảng
 * lời giải riêng: chỉ công cụ của nhóm sản phẩm dùng, không bao giờ trả cho học sinh.
 */
@Repository
public class ProbeRepository {

  public record Prompt(
      String code,
      String group,
      String prompt,
      UUID targetVersionId,
      String targetCode,
      String channel,
      boolean blocking) {}

  public record Reference(
      String code,
      String group,
      String targetCode,
      String finalAnswer,
      String forms,
      String steps) {}

  public record Report(
      long id,
      String runLabel,
      String model,
      String promptVersion,
      String guardVersion,
      int setVersion,
      int promptCount,
      int groupCount,
      int leakCount,
      String verdict,
      String report) {}

  private final JdbcClient jdbc;

  public ProbeRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public int currentVersion() {
    return jdbc.sql(
            "SELECT COALESCE(max(version), 0) FROM probe_set_version -- sqllint:allow=tenant dữ liệu kiểm thử")
        .query(Integer.class)
        .single();
  }

  public List<Prompt> active() {
    return jdbc.sql(
            """
            SELECT q.code, q.technique_group, q.prompt, q.target_problem_version_id, p.code, q.channel, q.blocking
            FROM probe_prompt q
            JOIN problem_version pv ON pv.org_id = q.target_org_id AND pv.id = q.target_problem_version_id
            JOIN problem p ON p.org_id = pv.org_id AND p.id = pv.problem_id
            WHERE q.active ORDER BY q.code LIMIT 500
            -- sqllint:allow=ownership,tenant bộ câu thử (dữ liệu kiểm thử, không thuộc trường) + đề kho nền tảng
            """)
        .query((rs, i) ->
            new Prompt(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getObject(4, UUID.class),
                rs.getString(5),
                rs.getString(6),
                rs.getBoolean(7)))
        .list();
  }

  public Optional<UUID> catalogVersion(String problemCode) {
    return jdbc.sql(
            """
            SELECT current_version_id FROM problem
            WHERE org_id = platform_org_id() AND code = :code AND source = 'catalog' LIMIT 1
            -- sqllint:allow=ownership,tenant đề kho nền tảng
            """)
        .param("code", problemCode)
        .query(UUID.class)
        .optional();
  }

  public boolean exists(String code) {
    return jdbc.sql(
                "SELECT count(*) FROM probe_prompt WHERE code = :code -- sqllint:allow=tenant dữ liệu kiểm thử")
            .param("code", code)
            .query(Long.class)
            .single()
        > 0;
  }

  public boolean duplicate(UUID targetVersionId, String key) {
    return jdbc.sql(
                "SELECT count(*) FROM probe_prompt WHERE active AND target_problem_version_id = :pv AND prompt_key = :k"
                    + " -- sqllint:allow=tenant dữ liệu kiểm thử")
            .param("pv", targetVersionId)
            .param("k", key)
            .query(Long.class)
            .single()
        > 0;
  }

  public void insert(Prompt p, String key, int version) {
    jdbc.sql(
            """
            INSERT INTO probe_prompt (code, technique_group, prompt, target_org_id, target_problem_version_id, channel,
                                      blocking, prompt_key, added_version)
            VALUES (:code, :grp, :prompt, platform_org_id(), :pv, :channel, :blocking, :key, :version)
            -- sqllint:allow=ownership,tenant dữ liệu kiểm thử của nhóm sản phẩm
            """)
        .param("code", p.code())
        .param("grp", p.group())
        .param("prompt", p.prompt())
        .param("pv", p.targetVersionId())
        .param("channel", p.channel())
        .param("blocking", p.blocking())
        .param("key", key)
        .param("version", version)
        .update();
  }

  public void newVersion(int version, int count) {
    jdbc.sql(
            "INSERT INTO probe_set_version (version, prompt_count) VALUES (:v, :n) -- sqllint:allow=tenant kiểm thử")
        .param("v", version)
        .param("n", count)
        .update();
  }

  public Optional<Reference> reference(String code) {
    return jdbc.sql(
            """
            SELECT code, technique_group, target_code, final_answer, answer_forms::text, steps::text
            FROM fn_probe_reference(:code)
            -- sqllint:allow=tenant công cụ của nhóm sản phẩm: đáp số tham chiếu qua hàm cổng
            """)
        .param("code", code)
        .query((rs, i) ->
            new Reference(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)))
        .optional();
  }

  public long saveReport(Report r) {
    return jdbc.sql(
            """
            INSERT INTO probe_report (run_label, model, prompt_version, guard_version, set_version, prompt_count,
                                      group_count, leak_count, verdict, report)
            VALUES (:label, :model, :pv, :gv, :sv, :pc, :gc, :lc, :verdict, CAST(:report AS jsonb))
            RETURNING id
            -- sqllint:allow=tenant báo cáo bộ câu thử
            """)
        .param("label", r.runLabel())
        .param("model", r.model())
        .param("pv", r.promptVersion())
        .param("gv", r.guardVersion())
        .param("sv", r.setVersion())
        .param("pc", r.promptCount())
        .param("gc", r.groupCount())
        .param("lc", r.leakCount())
        .param("verdict", r.verdict())
        .param("report", r.report())
        .query(Long.class)
        .single();
  }

  public List<Report> reports(int limit) {
    return jdbc.sql(
            """
            SELECT id, run_label, model, prompt_version, guard_version, set_version, prompt_count, group_count,
                   leak_count, verdict, report::text
            FROM probe_report ORDER BY id DESC LIMIT :limit
            -- sqllint:allow=tenant báo cáo bộ câu thử
            """)
        .param("limit", Math.min(Math.max(limit, 1), 100))
        .query((rs, i) ->
            new Report(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getInt(6),
                rs.getInt(7),
                rs.getInt(8),
                rs.getInt(9),
                rs.getString(10),
                rs.getString(11)))
        .list();
  }
}
