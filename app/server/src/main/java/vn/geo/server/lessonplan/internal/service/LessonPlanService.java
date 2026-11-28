package vn.geo.server.lessonplan.internal.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.config.TimeProperties;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Exercise;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Knowledge;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Lesson;
import vn.geo.server.lessonplan.internal.repo.LessonCatalogRepository.Period;
import vn.geo.server.lessonplan.internal.repo.LessonPlanRepository;
import vn.geo.server.lessonplan.internal.repo.LessonPlanRepository.Row;

/**
 * Trợ lý soạn giáo án (S5-E1, US-S5E1-01…03): danh mục Tiết PPCT cho form, tạo yêu cầu soạn (kiểm
 * form, hạn mức 30 lượt/ngày), đọc giáo án theo quan hệ (chỉ giáo viên chủ; người khác NOT_FOUND,
 * học sinh FORBIDDEN), Thử lại.
 */
@Service
public class LessonPlanService {

  public static final String MSG_QUOTA =
      "Hôm nay thầy cô đã dùng hết 30 lượt soạn với AI. Lượt mới mở lúc 00:00."; // MSG-S5-06

  private final LessonCatalogRepository catalog;
  private final LessonPlanRepository plans;
  private final LessonPlanGenerator generator;
  private final LessonPlanViews views;
  private final JsonMapper mapper;
  private final Clock clock;
  private final int dailyLimit;

  public LessonPlanService(
      LessonCatalogRepository catalog,
      LessonPlanRepository plans,
      LessonPlanGenerator generator,
      JsonMapper mapper,
      Clock clock,
      @Value("${geo.s5.lesson-plan.daily-limit:30}") int dailyLimit) {
    this.catalog = catalog;
    this.plans = plans;
    this.generator = generator;
    this.views = new LessonPlanViews(mapper, catalog);
    this.mapper = mapper;
    this.clock = clock;
    this.dailyLimit = dailyLimit;
  }

  /** Danh mục cho form: Chương → Bài → Tiết, kiến thức của bài (tích sẵn theo tiết). */
  public Map<String, Object> catalog() {
    Roles.require(Role.TEACHER);
    Map<Integer, List<Period>> periods =
        catalog.periods().stream().collect(Collectors.groupingBy(Period::lessonNo));
    Map<Integer, List<Knowledge>> knowledge =
        catalog.knowledge().stream()
            .filter(k -> !"loai-bo".equals(k.status()))
            .collect(Collectors.groupingBy(Knowledge::lessonNo));
    Map<Integer, Map<String, Object>> chapters = new LinkedHashMap<>();
    for (Lesson l : catalog.lessons()) {
      Map<String, Object> ch =
          chapters.computeIfAbsent(
              l.chapterNo(),
              n -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("no", n);
                m.put("label", "Chương " + roman(n));
                m.put("title", l.chapterTitle());
                m.put("lessons", new ArrayList<Map<String, Object>>());
                return m;
              });
      Map<String, Object> lv = new LinkedHashMap<>();
      lv.put("no", l.no());
      lv.put("title", "Bài " + l.no() + ". " + l.title());
      lv.put(
          "periods",
          periods.getOrDefault(l.no(), List.of()).stream()
              .map(p ->
                  Map.<String, Object>of(
                      "id", p.id(), "label", p.label(), "periods", p.periods(), "focus", p.focus()))
              .toList());
      lv.put(
          "knowledge",
          knowledge.getOrDefault(l.no(), List.of()).stream()
              .map(k ->
                  Map.<String, Object>of(
                      "code", k.code(), "statement", k.statement(), "status", k.status()))
              .toList());
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> ls = (List<Map<String, Object>>) ch.get("lessons");
      ls.add(lv);
    }
    return Map.of("chapters", List.copyOf(chapters.values()));
  }

  /** Tạo yêu cầu soạn (US-S5E1-01-SC2): 202 "Đang soạn"; AI chạy nền. */
  public Map<String, Object> create(LessonForm form) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    if (form == null) {
      throw AppException.invalidField("tietPpct", LessonForm.MSG_REQUIRED);
    }
    Period period = form.tietPpct() == null ? null : catalog.period(form.tietPpct()).orElse(null);
    List<Knowledge> lessonKt =
        period == null
            ? List.of()
            : catalog.knowledge().stream().filter(k -> k.lessonNo() == period.lessonNo()).toList();
    LessonForm f = form.validated(period, lessonKt);
    checkQuota(me);
    Row row = plans.insert(me.userId(), period.id(), mapper.writeValueAsString(f));
    start(me, row, f, period);
    return views.created(row);
  }

  /** Thử lại sau "Lỗi" / "Từ chối" / câu hỏi lại: một lượt mới (tính hạn mức). */
  public Map<String, Object> retry(String idOrCode) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    Row row = find(me, idOrCode);
    if ("drafting".equals(row.status())) {
      throw new AppException(ErrorCode.CONFLICT, "Giáo án đang được soạn.");
    }
    LessonForm f = mapper.readValue(row.requestJson(), LessonForm.class);
    Period period = catalog.period(row.periodId()).orElseThrow();
    checkQuota(me);
    plans.restart(row.id());
    start(me, row, f, period);
    return views.created(row);
  }

  /** Đọc giáo án (US-S5E1-03): theo mã GA-nnnn hoặc id; chỉ giáo viên chủ. */
  public Map<String, Object> view(String idOrCode) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    return views.full(find(me, idOrCode));
  }

  private Row find(AuthPrincipal me, String idOrCode) {
    UUID id = null;
    try {
      id = UUID.fromString(idOrCode);
    } catch (IllegalArgumentException e) {
      // mã GA-nnnn
    }
    return (id != null ? plans.byId(me.userId(), id) : plans.byCode(me.userId(), idOrCode))
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  private void checkQuota(AuthPrincipal me) {
    ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM);
    LocalDate today = now.toLocalDate();
    if (plans.turnsOn(me.userId(), today) >= dailyLimit) {
      ZonedDateTime midnight = today.plusDays(1).atStartOfDay(TimeProperties.VIETNAM);
      throw AppException.rateLimited(Duration.between(now, midnight), MSG_QUOTA);
    }
  }

  private void start(AuthPrincipal me, Row row, LessonForm f, Period period) {
    Lesson lesson = catalog.lesson(period.lessonNo()).orElseThrow();
    Set<String> wanted = new java.util.LinkedHashSet<>();
    List<Knowledge> all = catalog.knowledge();
    all.stream().filter(k -> k.lessonNo() == period.lessonNo()).forEach(k -> wanted.add(k.code()));
    wanted.addAll(period.extraKnowledge());
    // chỉ 'da-kiem' và 'nhap-tam' được cấp (BR-S5E1-05, QS5-03)
    List<Knowledge> provided =
        all.stream()
            .filter(k -> wanted.contains(k.code()))
            .filter(k -> "da-kiem".equals(k.status()) || "nhap-tam".equals(k.status()))
            .toList();
    List<Exercise> bank = catalog.exercises(period.lessonNo());
    String message = LessonMessage.build(f, lesson, period, provided, bank);
    LocalDate today =
        ZonedDateTime.now(clock).withZoneSameInstant(TimeProperties.VIETNAM).toLocalDate();
    UUID turn = plans.addTurn(me.userId(), today, "plan", row.id());
    generator.submit(
        me,
        new LessonPlanGenerator.Job(
            row.id(),
            turn,
            message,
            f.soTiet(),
            provided.stream().map(Knowledge::code).collect(Collectors.toSet()),
            bank));
  }

  static String roman(int n) {
    String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    return n > 0 && n < r.length ? r[n] : String.valueOf(n);
  }
}
