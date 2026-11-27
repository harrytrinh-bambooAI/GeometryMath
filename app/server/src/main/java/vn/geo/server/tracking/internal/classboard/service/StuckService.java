package vn.geo.server.tracking.internal.classboard.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.tracking.internal.classboard.domain.BoardRules;
import vn.geo.server.tracking.internal.classboard.domain.StuckDigest;
import vn.geo.server.tracking.internal.classboard.domain.StuckDigest.Insight;
import vn.geo.server.tracking.internal.classboard.domain.StuckDigest.Sheet;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Column;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Member;
import vn.geo.server.tracking.internal.classboard.repo.ClassBoardRepository.Work;
import vn.geo.server.tracking.internal.classboard.service.ClassBoardService.ColumnView;
import vn.geo.server.tracking.internal.classboard.service.ClassBoardService.Scope;

/**
 * S3-E3 — "Cả lớp vướng ở đâu" (US-S3E3-03). Mỗi đề xét riêng: đủ {@code MIN_AGGREGATE} bài làm (số
 * học sinh Đang học khác nhau có tương tác) thì gọi bộ gộp qua cổng AI với dữ liệu ẩn danh
 * (BR-S3E3-36); chưa đủ, lớp nhỏ, bộ gộp lỗi hoặc quá giờ thì chỉ trả dữ liệu thô (BR-S3E3-34, 37).
 * Đầu vào không đổi thì dùng kết quả đã có, không gọi lại (BR-S3E3-38). Thẻ luôn mang hai nhãn cố
 * định, không bao giờ "Đã kiểm chứng" (BR-S3E3-33).
 */
@Service
public class StuckService {

  static final String TASK = "class_stuck";

  static final int CACHE_MAX = 5_000;

  /** userId (UUID) để mở bằng chứng; mã người dùng chỉ để hiện (BR-S3E3-40, PO-74). */
  public record Person(UUID userId, String displayName, String userCode) {}

  public record InsightView(
      int position,
      String title,
      int step,
      String text,
      int n,
      List<String> labels,
      List<Person> evidence) {}

  public record RawEntry(int step, String kind, String text, Instant at) {}

  /**
   * mode: insight (đã gộp) | raw (chưa đủ ngưỡng) | failed (bộ gộp lỗi/quá giờ) | ai_off (lớp tắt
   * trợ lý AI: dữ liệu thô kèm MSG-S3-98, không "Thử lại", PO-85) | none (chưa ai làm).
   */
  public record ItemView(int position, String title, String mode, int works, List<RawEntry> raw) {}

  /** aiOff: lớp đã tắt trợ lý AI nên không gộp tự động (BR-S3E3-41, MSG-S3-98). */
  public record Stuck(
      int minAggregate,
      boolean classTooSmall,
      boolean aiOff,
      List<InsightView> insights,
      List<ItemView> items,
      Instant updatedAt) {}

  private record Cached(String hash, List<Insight> insights) {}

  private final ClassBoardService board;
  private final ClassBoardRepository repo;
  private final AiGateway ai;
  private final StuckSettings settings;
  private final Map<String, Cached> cache =
      Collections.synchronizedMap(
          new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Cached> eldest) {
              return size() > CACHE_MAX;
            }
          });

  public StuckService(
      ClassBoardService board, ClassBoardRepository repo, AiGateway ai, StuckSettings settings) {
    this.board = board;
    this.repo = repo;
    this.ai = ai;
    this.settings = settings;
  }

  public Stuck stuck(UUID classId, UUID setId) {
    Scope s = board.scope(classId, setId); // kiểm vai + quan hệ trước mọi lượt gọi bộ gộp
    int min = settings.minAggregate();
    // PO-85 (BR-S3E3-41): lớp tắt trợ lý AI thì không gọi bộ gộp, không phát sinh chi phí AI
    boolean aiOff = !repo.aiEnabled(classId);
    Set<UUID> active = ClassBoardService.activeIds(s.members());
    boolean small = active.size() < min;
    List<UUID> ids = s.assignmentIds();
    Instant from = Instant.now().minus(ClassBoardService.EVENT_SPAN);

    Map<UUID, Map<UUID, Work>> works = new HashMap<>(); // đề → em → bài làm
    for (Work w : repo.works(ids)) {
      if (active.contains(w.userId())) { // em Đã rời không tính ngưỡng, không vào n (BR-S3E3-35)
        works.computeIfAbsent(w.assignmentId(), k -> new HashMap<>()).put(w.userId(), w);
      }
    }
    var attempts = repo.attempts(ids, null);
    var hints = repo.hints(ids, null, from);
    var questions = repo.questions(ids, null);
    Map<String, String> bodies = new HashMap<>();
    for (var b : repo.hintBodies(s.setId())) {
      bodies.put(b.assignmentId() + "|" + b.level(), b.body());
    }

    List<InsightView> insights = new ArrayList<>();
    List<ItemView> items = new ArrayList<>();
    long deadline = System.nanoTime() + settings.deadline().toNanos();
    boolean gaveUp = false;
    for (Column c : s.columns()) {
      ColumnView cv = ClassBoardService.view(c);
      Map<UUID, Work> mine = works.getOrDefault(c.assignmentId(), Map.of());
      List<Member> workers =
          s.members().stream().filter(m -> mine.containsKey(m.userId())).toList();
      List<Sheet> sheets = new ArrayList<>();
      List<RawEntry> raw = new ArrayList<>();
      for (Member m : workers) {
        Sheet sheet = new Sheet(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (var h : hints) {
          if (h.assignmentId().equals(c.assignmentId()) && h.userId().equals(m.userId())) {
            sheet.hintLevels().add(h.level());
            String body = bodies.get(c.assignmentId() + "|" + h.level());
            raw.add(
                new RawEntry(
                    h.level(), "hint", body == null ? "Gợi ý mức " + h.level() : body, h.at()));
          }
        }
        for (var q : questions) {
          if (q.assignmentId().equals(c.assignmentId()) && q.userId().equals(m.userId())) {
            int step = 0;
            for (var h : hints) {
              if (h.assignmentId().equals(c.assignmentId())
                  && h.userId().equals(m.userId())
                  && !h.at().isAfter(q.at())) {
                step = Math.max(step, h.level());
              }
            }
            sheet.questions().add(q.text());
            raw.add(new RawEntry(step, "question", q.text(), q.at()));
          }
        }
        for (var a : attempts) {
          if (a.assignmentId().equals(c.assignmentId())
              && a.userId().equals(m.userId())
              && "wrong".equals(a.outcome())) {
            sheet.wrongAnswers().add(a.value());
          }
        }
        sheets.add(sheet);
      }
      raw.sort(Comparator.comparingInt(RawEntry::step).thenComparing(RawEntry::at));
      int m = workers.size();
      if (m == 0) {
        items.add(new ItemView(cv.position(), cv.title(), "none", 0, List.of()));
        continue;
      }
      if (aiOff) {
        items.add(new ItemView(cv.position(), cv.title(), "ai_off", m, raw));
        continue;
      }
      if (small || m < min) {
        items.add(new ItemView(cv.position(), cv.title(), "raw", m, raw));
        continue;
      }
      long left = deadline - System.nanoTime();
      gaveUp = gaveUp || left <= 0;
      List<Insight> found =
          gaveUp ? null : aggregate(s, c, workers, sheets, Duration.ofNanos(left));
      if (found == null) {
        items.add(new ItemView(cv.position(), cv.title(), "failed", m, raw));
        gaveUp = gaveUp || System.nanoTime() >= deadline; // quá trần chờ: không gọi tiếp các đề sau
        continue;
      }
      items.add(new ItemView(cv.position(), cv.title(), "insight", m, List.of()));
      for (Insight in : found) {
        List<Person> evidence =
            in.works().stream()
                .map(i -> workers.get(i - 1))
                .map(w -> new Person(w.userId(), w.displayName(), ClassBoardService.codeKey(w)))
                .toList();
        insights.add(
            new InsightView(
                cv.position(),
                cv.title(),
                in.step(),
                in.text(),
                evidence.size(),
                BoardRules.LABELS,
                evidence));
      }
    }
    insights.sort(
        Comparator.comparingInt(InsightView::n)
            .reversed()
            .thenComparingInt(InsightView::position)
            .thenComparingInt(InsightView::step));
    return new Stuck(min, small, aiOff, insights, items, Instant.now());
  }

  /** Nhận định đã lọc của một đề; null = bộ gộp lỗi, quá giờ hoặc trả lời không đọc được. */
  private List<Insight> aggregate(
      Scope s, Column c, List<Member> workers, List<Sheet> sheets, Duration maxWait) {
    String prompt = StuckDigest.prompt(c.position(), sheets);
    StringBuilder who = new StringBuilder(prompt);
    for (Member m : workers) {
      who.append('|').append(m.userId()); // chỉ vào khóa băm, không gửi đi
    }
    String hash = StuckDigest.sha256(who.toString());
    String key = s.teacher().orgId() + "|" + c.assignmentId();
    Cached hit = cache.get(key);
    if (hit != null && hit.hash().equals(hash)) {
      return hit.insights(); // đầu vào không đổi: không gọi lại bộ gộp (BR-S3E3-38)
    }
    try {
      // PO-83: chỉ chờ phần còn lại của trần cả màn (không theo geo.ai.timeout)
      AiResponse r = ai.complete(new AiRequest(TASK, prompt), maxWait);
      String content = r.content();
      if ("mock".equals(r.provider())) {
        String scripted = settings.scripted(s.teacher().userId(), c.position());
        content = scripted != null ? scripted : StuckDigest.mockReply(sheets);
      }
      List<Insight> parsed = StuckDigest.parse(content, sheets.size());
      if (parsed == null) {
        return null;
      }
      cache.put(key, new Cached(hash, parsed));
      return parsed;
    } catch (AppException e) {
      if (e.code() == ErrorCode.FORBIDDEN || e.code() == ErrorCode.UNAUTHENTICATED) {
        throw e;
      }
      return null; // lùi về dữ liệu thô kèm "Thử lại" (BR-S3E3-37); không dùng thẻ cũ
    }
  }
}
