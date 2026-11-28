package vn.geo.server.aigateway.internal.repo.killswitch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import vn.geo.server.common.id.Uuid7;

/**
 * SQL của công tắc tắt khẩn toàn hệ thống (BR-S5E5-16) và vai Vận hành. ai_kill_switch,
 * ai_ops_grant không theo trường (QS5-02); nhật ký đổi ghi ai_switch_audit theo trường của người
 * vận hành.
 */
@Repository
public class KillSwitchRepository {

  private final JdbcClient jdbc;

  public KillSwitchRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public boolean anyOff(Set<String> tasks, String lane) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM ai_kill_switch k
                           WHERE k.task IN (:tasks) AND k.off AND (k.lane = '' OR k.lane = :lane) LIMIT 1)
            -- sqllint:allow=tenant công tắc tắt khẩn toàn hệ thống, không theo trường (QS5-02, BR-S5E5-16)
            """)
        .param("tasks", tasks)
        .param("lane", lane)
        .query(Boolean.class)
        .single();
  }

  public Map<String, Boolean> list(String lane) {
    Map<String, Boolean> out = new LinkedHashMap<>();
    jdbc.sql(
            """
            SELECT k.task, k.off FROM ai_kill_switch k WHERE k.lane = :lane ORDER BY k.task LIMIT 50
            -- sqllint:allow=tenant công tắc tắt khẩn toàn hệ thống (QS5-02)
            """)
        .param("lane", lane)
        .query((rs, i) -> out.put(rs.getString(1), rs.getBoolean(2)))
        .list();
    return out;
  }

  public boolean isOff(String task, String lane) {
    return jdbc.sql(
            """
            SELECT k.off FROM ai_kill_switch k WHERE k.task = :task AND k.lane = :lane LIMIT 1
            -- sqllint:allow=tenant công tắc tắt khẩn toàn hệ thống (QS5-02)
            """)
        .param("task", task)
        .param("lane", lane)
        .query(Boolean.class)
        .optional()
        .orElse(false);
  }

  public void set(String task, String lane, boolean off, UUID actor) {
    jdbc.sql(
            """
            INSERT INTO ai_kill_switch (task, lane, off, updated_by, updated_at)
            VALUES (:task, :lane, :off, :actor, now())
            ON CONFLICT (task, lane) DO UPDATE
              SET off = EXCLUDED.off, updated_by = EXCLUDED.updated_by, updated_at = now()
            -- sqllint:allow=tenant công tắc tắt khẩn toàn hệ thống (QS5-02)
            """)
        .param("task", task)
        .param("lane", lane)
        .param("off", off)
        .param("actor", actor)
        .update();
  }

  /** AuditEvent của người vận hành (bật = true, tắt = false). */
  public void audit(UUID orgId, UUID actor, String task, boolean beforeOn, boolean afterOn) {
    jdbc.sql(
            """
            INSERT INTO ai_switch_audit (id, org_id, actor_id, actor_role, class_id, switch_name, before_on, after_on)
            VALUES (:id, :orgId, :actor, 'operator', NULL, :name, :before, :after)
            """)
        .param("id", Uuid7.next())
        .param("orgId", orgId)
        .param("actor", actor)
        .param("name", "kill." + task)
        .param("before", beforeOn)
        .param("after", afterOn)
        .update();
  }

  public List<Map<String, Object>> auditOf(UUID orgId, String task) {
    return jdbc.sql(
            """
            SELECT a.actor_id, a.actor_role, a.before_on, a.after_on FROM ai_switch_audit a
            WHERE a.org_id = :orgId AND a.class_id IS NULL AND a.switch_name = :name
            ORDER BY a.at DESC, a.id DESC LIMIT 20
            """)
        .param("orgId", orgId)
        .param("name", "kill." + task)
        .query((rs, i) ->
            Map.<String, Object>of(
                "actorId", rs.getObject(1, UUID.class),
                "actorRole", rs.getString(2),
                "before", rs.getBoolean(3),
                "after", rs.getBoolean(4)))
        .list();
  }

  public boolean isOperator(UUID userId) {
    return jdbc.sql(
            """
            SELECT EXISTS (SELECT 1 FROM ai_ops_grant g WHERE g.user_id = :userId LIMIT 1)
            -- sqllint:allow=tenant vai Vận hành toàn hệ thống (QS5-02)
            """)
        .param("userId", userId)
        .query(Boolean.class)
        .single();
  }

  public void grant(UUID userId, String by) {
    jdbc.sql(
            """
            INSERT INTO ai_ops_grant (user_id, granted_by) VALUES (:userId, :by) ON CONFLICT DO NOTHING
            -- sqllint:allow=tenant vai Vận hành toàn hệ thống (QS5-02)
            """)
        .param("userId", userId)
        .param("by", by)
        .update();
  }
}
