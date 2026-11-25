package vn.geo.server.tracking.internal.domain;

import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Hàm thuần của Theo dõi lớp (E7, US-GEO-701, 702, 703): tình trạng một học sinh (BR-GEO-268), gộp
 * câu hỏi trùng nguyên văn (BR-GEO-275, D-GEO-116), thẻ "thao tác hình" (M-GEO-23), phiên làm bài
 * và tỉ lệ phiên có thao tác trên hình (BR-GEO-278, M-GEO-08, D-GEO-059, 144). Không gọi AI.
 */
public final class MonitorView {

  /** Khoảng nghỉ từ 30 phút 0 giây trở lên mở phiên mới (D-GEO-144). */
  public static final Duration SESSION_GAP = Duration.ofMinutes(30);

  public static final String NOT_STARTED = "Chưa bắt đầu";
  public static final String SOLUTION_VIEWED = "Đã xem lời giải";
  public static final String WAITING = "Đang chờ lời giải";
  public static final String SELF = "Tự làm, không cần gợi ý";
  public static final String WORKING = "Đang làm";

  private MonitorView() {}

  /**
   * BR-GEO-268, xét theo thứ tự. {@code locked}: lời giải của bài còn khóa (Chưa đặt giờ/Sắp mở).
   */
  public static String status(
      boolean opened, boolean solutionViewed, int hintLevel, int askCount, boolean locked) {
    if (!opened) {
      return NOT_STARTED;
    }
    if (solutionViewed) {
      return SOLUTION_VIEWED;
    }
    if (hintLevel >= 2 && locked) {
      return WAITING;
    }
    if (hintLevel == 0 && askCount == 0) {
      return SELF;
    }
    return WORKING;
  }

  /** Thẻ "thao tác hình": tổng thời gian (phút) của cả lớp chia N, làm tròn phút (M-GEO-23). */
  public static int averageMinutes(List<Integer> activeSeconds, int classSize) {
    if (classSize <= 0) {
      return 0;
    }
    long total = 0;
    for (int s : activeSeconds) {
      total += s;
    }
    return (int) Math.round(total / 60.0 / classSize);
  }

  /** Số phút của một học sinh: làm tròn phút (29 phút 59 giây → 30, D-GEO-144). */
  public static int minutes(int activeSeconds) {
    return (activeSeconds + 30) / 60;
  }

  /** Khóa gộp: bỏ khác biệt hoa thường và khoảng trắng (D-GEO-116), chuẩn hóa Unicode NFC. */
  public static String groupKey(String question) {
    return clean(question).toLowerCase(new Locale("vi"));
  }

  /** Câu hiển thị: gộp khoảng trắng, bỏ khoảng trắng đầu/cuối. */
  public static String clean(String question) {
    String n = Normalizer.normalize(question == null ? "" : question, Normalizer.Form.NFC);
    return n.strip().replaceAll("\\s+", " ");
  }

  /** Một câu hỏi (đọc theo bài): mã tăng dần theo thời gian hỏi. */
  public record Asked(long id, UUID userId, String name, String question, Instant askedAt) {}

  /**
   * Một nhóm của khối "Học sinh đang hỏi gì": câu (bản hỏi đầu tiên), tên học sinh theo thứ tự hỏi
   * lần đầu, số lượt, giờ câu mới nhất.
   */
  public record Group(String question, List<String> names, int count, Instant lastAskedAt) {}

  /** BR-GEO-275: gộp câu trùng nguyên văn sau chuẩn hóa; nhóm có câu mới nhất ở trên. */
  public static List<Group> groups(List<Asked> asked) {
    List<Asked> byId = new ArrayList<>(asked);
    byId.sort(Comparator.comparingLong(Asked::id));
    Map<String, List<Asked>> m = new LinkedHashMap<>();
    for (Asked a : byId) {
      m.computeIfAbsent(groupKey(a.question()), k -> new ArrayList<>()).add(a);
    }
    List<Group> out = new ArrayList<>();
    List<Long> lastIds = new ArrayList<>();
    for (List<Asked> g : m.values()) {
      LinkedHashSet<String> names = new LinkedHashSet<>();
      g.forEach(a -> names.add(a.name()));
      Asked last = g.get(g.size() - 1);
      out.add(new Group(clean(g.get(0).question()), List.copyOf(names), g.size(), last.askedAt()));
      lastIds.add(last.id());
    }
    List<Integer> idx = new ArrayList<>();
    for (int i = 0; i < out.size(); i++) {
      idx.add(i);
    }
    idx.sort(Comparator.comparing((Integer i) -> lastIds.get(i)).reversed());
    return idx.stream().map(out::get).toList();
  }

  /** Một sự kiện để tính phiên: học sinh, bài, loại, thời điểm. */
  public record Ev(UUID userId, UUID assignmentId, String kind, Instant at) {}

  /** Một phiên làm bài: bắt đầu, kết thúc, có thao tác trên hình không. */
  public record Session(UUID userId, UUID assignmentId, Instant start, Instant end, boolean tool) {}

  /**
   * Phiên làm bài (BR-GEO-278): theo (học sinh, bài), sự kiện xếp theo thời gian; khoảng nghỉ ≥ 30
   * phút 0 giây mở phiên mới, 29 phút 59 giây vẫn cùng phiên.
   */
  public static List<Session> sessions(List<Ev> events) {
    Map<String, List<Ev>> byKey = new LinkedHashMap<>();
    for (Ev e : events) {
      byKey.computeIfAbsent(e.userId() + "/" + e.assignmentId(), k -> new ArrayList<>()).add(e);
    }
    List<Session> out = new ArrayList<>();
    for (List<Ev> evs : byKey.values()) {
      evs.sort(Comparator.comparing(Ev::at));
      Ev first = evs.get(0);
      Instant start = first.at();
      Instant end = first.at();
      boolean tool = "tool".equals(first.kind());
      for (int i = 1; i < evs.size(); i++) {
        Ev e = evs.get(i);
        if (Duration.between(end, e.at()).compareTo(SESSION_GAP) >= 0) {
          out.add(new Session(first.userId(), first.assignmentId(), start, end, tool));
          start = e.at();
          tool = false;
        }
        end = e.at();
        tool |= "tool".equals(e.kind());
      }
      out.add(new Session(first.userId(), first.assignmentId(), start, end, tool));
    }
    return out;
  }

  /** Thời gian (giây) trên bài: cộng các khoảng nghỉ dưới 30 phút giữa hai sự kiện liên tiếp. */
  public static long activeSeconds(List<Ev> events) {
    List<Ev> evs = new ArrayList<>(events);
    evs.sort(Comparator.comparing(Ev::at));
    long s = 0;
    for (int i = 1; i < evs.size(); i++) {
      Duration d = Duration.between(evs.get(i - 1).at(), evs.get(i).at());
      if (d.compareTo(SESSION_GAP) < 0) {
        s += d.getSeconds();
      }
    }
    return s;
  }

  /** M-GEO-08: "{x,y}%" một chữ số thập phân, dấu phẩy; không có phiên nào thì "—". */
  public static String toolSessionRate(List<Session> sessions) {
    if (sessions.isEmpty()) {
      return "—";
    }
    long withTool = sessions.stream().filter(Session::tool).count();
    long permille = Math.round(withTool * 1000.0 / sessions.size());
    return (permille / 10) + "," + (permille % 10) + "%";
  }

  /** Ngưỡng đề xuất của M-GEO-08 (Q-CDT-09). */
  public static final String TOOL_SESSION_THRESHOLD = "ngưỡng đề xuất ≥ 60,0%";
}
