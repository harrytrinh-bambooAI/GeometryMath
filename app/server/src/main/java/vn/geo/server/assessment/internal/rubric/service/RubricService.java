package vn.geo.server.assessment.internal.rubric.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository.Criterion;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository.Head;
import vn.geo.server.assessment.internal.rubric.repo.RubricRepository.Version;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Rubric có phiên bản (US-S5E4-01; BR-S5E4-01…03; QS5-15). Chỉ giáo viên; rubric của giáo viên khác
 * là NOT_FOUND. Lưu thay đổi rubric có phiên bản hiện hành đã gắn bài đã giao tạo phiên bản mới
 * (MSG-S5-31); chưa gắn thì sửa tại chỗ.
 */
@Service
public class RubricService {

  /** MSG-S5-30. */
  public static final String MSG_30 = "Tổng điểm các tiêu chí phải bằng thang điểm của bài.";

  /** MSG-S5-31. */
  public static final String MSG_31 =
      "Rubric này đã dùng cho bài đã giao. Lưu sẽ tạo phiên bản mới; bài đã giao giữ phiên bản cũ.";

  static final BigDecimal DEFAULT_SCALE = BigDecimal.TEN;

  /** Thân tạo / sửa rubric. {@code criteria} rỗng khi tạo từ mẫu = lấy tiêu chí của mẫu. */
  public record RubricInput(
      String fromTemplate, String name, BigDecimal scale, List<CriterionInput> criteria) {}

  public record CriterionInput(
      String name, String description, BigDecimal maxPoints, Boolean directionDependent) {}

  private final RubricRepository repo;
  private final JsonMapper json;

  public RubricService(RubricRepository repo, JsonMapper json) {
    this.repo = repo;
    this.json = json;
  }

  public Map<String, Object> templates() {
    Roles.require(Role.TEACHER);
    List<Map<String, Object>> items = new ArrayList<>();
    for (String code : List.of("tinh-toan", "chung-minh")) {
      RubricTemplates.Template t = RubricTemplates.ALL.get(code);
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("code", t.code());
      m.put("label", t.label());
      m.put("criteria", t.criteria().stream().map(RubricService::criterionView).toList());
      m.put("guide", RubricTemplates.GUIDE);
      items.add(m);
    }
    return Map.of("items", items);
  }

  @Transactional
  public Map<String, Object> create(RubricInput in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (in == null) {
      throw AppException.invalidField("name", "Nhập tên rubric.");
    }
    RubricTemplates.Template tpl = null;
    if (in.fromTemplate() != null && !in.fromTemplate().isBlank()) {
      tpl = RubricTemplates.ALL.get(in.fromTemplate());
      if (tpl == null) {
        throw AppException.invalidField("fromTemplate", "Mẫu rubric không có.");
      }
    }
    String name = name(in.name(), tpl);
    BigDecimal scale = in.scale() == null ? DEFAULT_SCALE : in.scale();
    List<Criterion> criteria =
        in.criteria() == null || in.criteria().isEmpty()
            ? fromTemplate(tpl)
            : validate(in.criteria(), scale);
    if (criteria.isEmpty()) {
      throw AppException.invalidField("criteria", "Rubric có từ 1 đến 10 tiêu chí.");
    }
    checkTotal(criteria, scale);
    String code = repo.nextCode(me.orgId(), me.userId());
    UUID rubricId = repo.insertHead(me.orgId(), me.userId(), code);
    UUID v =
        repo.insertVersion(
            me.orgId(),
            rubricId,
            1,
            name,
            tpl == null ? null : tpl.code(),
            scale,
            guideJson(),
            me.userId());
    repo.insertCriteria(me.orgId(), v, criteria);
    return view(
        me.orgId(), repo.owned(me.orgId(), me.userId(), rubricId).orElseThrow(), null, null);
  }

  @Transactional
  public Map<String, Object> update(UUID rubricId, RubricInput in) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Head head =
        repo.lockOwned(me.orgId(), me.userId(), rubricId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    Version cur = repo.version(me.orgId(), rubricId, head.currentVersion()).orElseThrow();
    if (in == null || in.criteria() == null || in.criteria().isEmpty()) {
      throw AppException.invalidField("criteria", "Rubric có từ 1 đến 10 tiêu chí.");
    }
    BigDecimal scale = in.scale() == null ? cur.scale() : in.scale();
    List<Criterion> criteria = validate(in.criteria(), scale);
    checkTotal(criteria, scale);
    String name = in.name() == null || in.name().isBlank() ? cur.name() : name(in.name(), null);
    if (repo.used(me.orgId(), cur.id())) {
      int next = head.currentVersion() + 1;
      UUID v =
          repo.insertVersion(
              me.orgId(), rubricId, next, name, cur.template(), scale, guideJson(), me.userId());
      repo.insertCriteria(me.orgId(), v, criteria);
      repo.setCurrent(me.orgId(), rubricId, next);
      Head h = repo.owned(me.orgId(), me.userId(), rubricId).orElseThrow();
      return view(me.orgId(), h, null, MSG_31);
    }
    repo.deleteCriteria(me.orgId(), cur.id());
    repo.insertCriteria(me.orgId(), cur.id(), criteria);
    repo.updateVersionMeta(me.orgId(), cur.id(), name, scale);
    repo.setCurrent(me.orgId(), rubricId, head.currentVersion());
    return view(me.orgId(), head, null, null);
  }

  public Map<String, Object> get(UUID rubricId, Integer version) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Head head =
        repo.owned(me.orgId(), me.userId(), rubricId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    return view(me.orgId(), head, version, null);
  }

  public Map<String, Object> list() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<Map<String, Object>> items = new ArrayList<>();
    for (Head h : repo.listOwned(me.orgId(), me.userId())) {
      Version v = repo.version(me.orgId(), h.id(), h.currentVersion()).orElseThrow();
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", h.id());
      m.put("code", h.code());
      m.put("name", v.name());
      m.put("version", v.version());
      m.put("versionId", v.id());
      m.put("scale", v.scale().doubleValue());
      items.add(m);
    }
    return Map.of("items", items);
  }

  // ------------------------------------------------------------------ kiểm

  /** Kiểm 1–10 tiêu chí, tên, điểm bội 0,25 (BR-S5E4-01, US-S5E4-01-FC2). */
  static List<Criterion> validate(List<CriterionInput> in, BigDecimal scale) {
    if (scale.signum() <= 0 || !quarter(scale)) {
      throw AppException.invalidField("scale", "Thang điểm phải dương, bước 0,25.");
    }
    if (in.size() > 10) {
      throw AppException.invalidField("criteria", "Rubric có từ 1 đến 10 tiêu chí.");
    }
    List<Criterion> out = new ArrayList<>();
    for (int i = 0; i < in.size(); i++) {
      CriterionInput c = in.get(i);
      String field = "criteria[" + i + "]";
      if (c == null || c.name() == null || c.name().isBlank() || c.name().strip().length() > 60) {
        throw AppException.invalidField(field + ".name", "Tên tiêu chí từ 1 đến 60 ký tự.");
      }
      String desc = c.description() == null ? "" : c.description().strip();
      if (desc.length() > 500) {
        throw AppException.invalidField(field + ".description", "Mô tả tối đa 500 ký tự.");
      }
      BigDecimal p = c.maxPoints();
      if (p == null || p.signum() <= 0 || !quarter(p)) {
        throw AppException.invalidField(
            field + ".maxPoints", "Điểm tối đa của tiêu chí phải lớn hơn 0, bước 0,25.");
      }
      out.add(
          new Criterion(
              i + 1,
              c.name().strip(),
              desc,
              p.stripTrailingZeros(),
              Boolean.TRUE.equals(c.directionDependent())));
    }
    return out;
  }

  static void checkTotal(List<Criterion> criteria, BigDecimal scale) {
    BigDecimal total =
        criteria.stream().map(Criterion::maxPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (total.compareTo(scale) != 0) {
      throw AppException.invalidField("criteria", MSG_30);
    }
  }

  static boolean quarter(BigDecimal v) {
    return v.multiply(BigDecimal.valueOf(4)).stripTrailingZeros().scale() <= 0;
  }

  private static String name(String raw, RubricTemplates.Template tpl) {
    String n = raw == null || raw.isBlank() ? (tpl == null ? null : tpl.label()) : raw.strip();
    if (n == null || n.length() > 120) {
      throw AppException.invalidField("name", "Tên rubric từ 1 đến 120 ký tự.");
    }
    return n;
  }

  private static List<Criterion> fromTemplate(RubricTemplates.Template tpl) {
    if (tpl == null) {
      return List.of();
    }
    List<Criterion> out = new ArrayList<>();
    int i = 1;
    for (RubricTemplates.Criterion c : tpl.criteria()) {
      out.add(new Criterion(i++, c.name(), c.description(), c.maxPoints(), c.direction()));
    }
    return out;
  }

  private String guideJson() {
    return json.writeValueAsString(RubricTemplates.GUIDE);
  }

  private static Map<String, Object> criterionView(RubricTemplates.Criterion c) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("name", c.name());
    m.put("description", c.description());
    m.put("maxPoints", c.maxPoints().doubleValue());
    m.put("directionDependent", c.direction());
    return m;
  }

  /** Thân phản hồi rubric (phía giáo viên). */
  Map<String, Object> view(UUID orgId, Head head, Integer version, String message) {
    int v = version == null ? head.currentVersion() : version;
    Version ver =
        repo.version(orgId, head.id(), v).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    List<Criterion> cs = repo.criteria(orgId, ver.id());
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", head.id());
    m.put("code", head.code());
    m.put("version", ver.version());
    m.put("versionId", ver.id());
    m.put("currentVersion", head.currentVersion());
    m.put("name", ver.name());
    m.put("template", ver.template());
    m.put("scale", ver.scale().doubleValue());
    m.put(
        "total",
        cs.stream()
            .map(Criterion::maxPoints)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue());
    List<Map<String, Object>> list = new ArrayList<>();
    for (Criterion c : cs) {
      Map<String, Object> x = new LinkedHashMap<>();
      x.put("ord", c.ord());
      x.put("name", c.name());
      x.put("description", c.description());
      x.put("maxPoints", c.maxPoints().doubleValue());
      x.put("directionDependent", c.direction());
      list.add(x);
    }
    m.put("criteria", list);
    m.put("guide", json.readValue(ver.guide(), List.class));
    m.put("used", repo.used(orgId, ver.id()));
    if (message != null) {
      m.put("message", message);
    }
    return m;
  }
}
