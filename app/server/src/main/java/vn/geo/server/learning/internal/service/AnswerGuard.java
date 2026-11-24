package vn.geo.server.learning.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.learning.api.AnswerGuardApi;
import vn.geo.server.learning.internal.repo.GuardRepository;
import vn.geo.server.learning.internal.repo.HelpRepository;

/**
 * Kiểm soát đáp án đầy đủ của E6 (xem {@link AnswerGuardApi}, quy tắc ở {@link AnswerRules}). Thay
 * bộ tối thiểu "g1-e4" của E4 ở cùng chỗ gọi. Ngữ cảnh lời giải lấy qua fn_guard_context (V60), chỉ
 * dùng phía máy chủ.
 */
@Service
public class AnswerGuard implements AnswerGuardApi {

  static final String VERSION = "g2-e6";

  private final GuardRepository repo;
  private final HelpRepository help;
  private final GuardFaults faults;
  private final JsonMapper mapper;

  public AnswerGuard(
      GuardRepository repo, HelpRepository help, GuardFaults faults, JsonMapper mapper) {
    this.repo = repo;
    this.help = help;
    this.faults = faults;
    this.mapper = mapper;
  }

  @Override
  public String version() {
    return VERSION;
  }

  @Override
  public Screened screen(UUID assignmentId, String question, String reply) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    long started = System.nanoTime();
    String rule;
    try {
      faults.check(p.userId());
      GuardRepository.Context g = repo.context(p.userId(), assignmentId);
      if (!"OK".equals(g.status()) || !g.locked() || faults.bypassed(p.userId())) {
        return new Screened(false, reply, null);
      }
      rule = AnswerRules.check(question, reply, locked(g.finalAnswer(), g.steps(), g.forms()));
      if (rule == null && g.ownFinalAnswer() != null) {
        rule =
            AnswerRules.check(
                question, reply, locked(g.ownFinalAnswer(), g.ownSteps(), g.ownForms()));
      }
    } catch (RuntimeException e) {
      logQuietly(assignmentId, "error_closed", "ERR_GUARD", started);
      throw new GuardFailure("Kiểm soát đáp án lỗi: đóng (KS-5)", e);
    }
    if (rule == null) {
      return new Screened(false, reply, null);
    }
    logQuietly(assignmentId, "blocked", rule, started);
    List<HelpRepository.SeenHint> seen = help.seenHints(p.userId(), assignmentId);
    String highest = seen.isEmpty() ? null : seen.get(seen.size() - 1).body();
    return new Screened(true, HelpMessages.msg35(highest), rule);
  }

  /**
   * Văn bản có lộ đáp số/bước giải của bài khi lời giải còn khóa với học sinh không — dùng cho gợi
   * ý AI của bài tự học trước khi lưu (BR-GEO-179, FIX3). Cùng quy tắc {@link AnswerRules} như hỏi
   * đáp (không có câu hỏi đi kèm).
   */
  boolean leaks(UUID userId, UUID assignmentId, String text) {
    if (text == null) {
      return false;
    }
    GuardRepository.Context g = repo.context(userId, assignmentId);
    if (!"OK".equals(g.status()) || !g.locked()) {
      return false;
    }
    return leaks(g, text);
  }

  boolean leaks(GuardRepository.Context g, String text) {
    return AnswerRules.check(null, text, locked(g.finalAnswer(), g.steps(), g.forms())) != null
        || (g.ownFinalAnswer() != null
            && AnswerRules.check(null, text, locked(g.ownFinalAnswer(), g.ownSteps(), g.ownForms()))
                != null);
  }

  @Override
  public Screened screenPreview(
      UUID problemVersionId, String question, String reply, String highestHint) {
    AuthPrincipal p = Roles.require(Role.TEACHER);
    String rule;
    try {
      faults.check(p.userId());
      GuardRepository.Catalog c = repo.catalog(p.userId(), problemVersionId).orElse(null);
      // không có lời giải để so (đề lạ): vẫn chặn câu hỏi đòi đáp số
      rule =
          c == null
              ? AnswerRules.check(question, reply, new AnswerRules.Locked(null, null, List.of()))
              : AnswerRules.check(question, reply, locked(c.finalAnswer(), c.steps(), c.forms()));
    } catch (RuntimeException e) {
      throw new GuardFailure("Kiểm soát đáp án lỗi: đóng (KS-5)", e);
    }
    return rule == null
        ? new Screened(false, reply, null)
        : new Screened(true, HelpMessages.msg35(highestHint), rule);
  }

  private AnswerRules.Locked locked(String answer, String stepsJson, String formsJson) {
    List<String> bodies = new ArrayList<>();
    if (stepsJson != null) {
      for (JsonNode st : mapper.readTree(stepsJson)) {
        bodies.add(st.path("body").asString(""));
      }
    }
    List<String> forms = new ArrayList<>();
    if (formsJson != null) {
      for (JsonNode f : mapper.readTree(formsJson)) {
        if (f.isString()) {
          forms.add(f.asString());
        }
      }
    }
    return new AnswerRules.Locked(answer, bodies.toArray(String[]::new), forms);
  }

  private void logQuietly(UUID assignmentId, String outcome, String rule, long started) {
    try {
      repo.log(
          assignmentId, outcome, rule, VERSION, (int) ((System.nanoTime() - started) / 1_000_000));
    } catch (RuntimeException e) {
      // nhật ký chặn không được làm hỏng đường trả lời (câu trả lời đã bị thay)
    }
  }
}
