package vn.geo.server.exam.internal.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vn.geo.server.exam.internal.domain.ExamModel.Cell;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.ExamModel.Opt;
import vn.geo.server.exam.internal.domain.ExamModel.Part;

/**
 * Áp các trường form ra đề lên ma trận chuẩn (US-S5E2-02, BR-S5E2-04, 05, README "Trường của bộ lắp
 * đề"): thời gian, số câu + điểm mỗi phần, chương (bỏ chương → ô chuyển sang chương còn lại cùng
 * loại câu), tỉ lệ NB/TH/VD/VDC (đổi mức độ của ô). Trường cố định (cấu trúc QĐ 764, tổng điểm 10,
 * đảo câu và phương án) không đổi được.
 */
public final class MatrixShaper {

  public static final List<String> LEVELS = List.of("NB", "TH", "VD", "VDC");

  /** Chương "Quan hệ vuông góc trong không gian" (lớp 11) gồm hai chủ đề HHKG của ngân hàng. */
  public static final String QHVG = "quan-he-vuong-goc";

  private static final Map<String, List<String>> GROUPS =
      Map.of(QHVG, List.of("hinh-khong-gian-quan-he", "hinh-khong-gian-the-tich"));

  public record PartInput(String id, Integer count, Double points) {}

  public record Chapter(String id, String name, List<String> topics) {}

  private MatrixShaper() {}

  public static String chapterOf(String topic) {
    for (Map.Entry<String, List<String>> e : GROUPS.entrySet()) {
      if (e.getValue().contains(topic)) {
        return e.getKey();
      }
    }
    return topic;
  }

  /** Chương chọn được: chương có trong ma trận + chương khác của cùng lớp (theo taxonomy). */
  public static List<Chapter> chapters(Matrix mt, Map<String, ExamDataset.Topic> topics) {
    Map<String, Set<String>> byChapter = new LinkedHashMap<>();
    for (Cell c : mt.cells()) {
      for (Opt o : c.opts()) {
        byChapter.computeIfAbsent(chapterOf(o.topic()), k -> new LinkedHashSet<>()).add(o.topic());
      }
    }
    for (ExamDataset.Topic t : topics.values()) {
      if (t.grade() != null && t.grade() == mt.grade()) {
        byChapter.computeIfAbsent(chapterOf(t.id()), k -> new LinkedHashSet<>()).add(t.id());
      }
    }
    List<Chapter> out = new ArrayList<>();
    byChapter.forEach((id, ts) -> {
      List<String> all = GROUPS.getOrDefault(id, List.of(id));
      String name =
          QHVG.equals(id)
              ? "Quan hệ vuông góc trong không gian"
              : topics.containsKey(id) ? topics.get(id).name() : id;
      out.add(new Chapter(id, name, List.copyOf(all)));
    });
    return out;
  }

  public static Matrix shape(
      Matrix mt,
      Map<String, ExamDataset.Topic> topics,
      Double minutes,
      List<PartInput> parts,
      List<String> chapters,
      Map<String, Double> levels) {
    double mins = minutes != null ? minutes : mt.minutes();
    List<Part> ps = new ArrayList<>(mt.parts());
    List<Cell> cells = new ArrayList<>(mt.cells());
    if (parts != null) {
      for (PartInput in : parts) {
        int idx = indexOf(ps, in.id());
        if (idx < 0) {
          continue;
        }
        Part p = ps.get(idx);
        double pts = in.points() != null ? in.points() : p.points();
        int cnt = in.count() != null ? Math.max(1, in.count()) : p.count();
        List<Cell> own = cells.stream().filter(c -> c.part().equals(p.id())).toList();
        List<Cell> resized = new ArrayList<>();
        for (int i = 0; i < cnt; i++) {
          Cell base = own.get(i % own.size());
          resized.add(i < own.size() ? base : base.withId(base.id() + "-" + (i / own.size() + 1)));
        }
        int first = cells.indexOf(own.get(0));
        cells.removeAll(own);
        List<Cell> withPts = resized.stream().map(c -> c.withPoints(pts)).toList();
        cells.addAll(Math.min(first, cells.size()), withPts);
        ps.set(idx, new Part(p.id(), p.name(), p.type(), cnt, pts));
      }
    }
    if (chapters != null && !chapters.isEmpty()) {
      Set<String> keep = new LinkedHashSet<>();
      for (Chapter ch : chapters(mt, topics)) {
        if (chapters.contains(ch.id())) {
          keep.addAll(ch.topics());
        }
      }
      cells = moveChapters(cells, keep);
    }
    if (levels != null && !levels.isEmpty()) {
      cells = relevel(cells, levels);
    }
    double total = 0;
    for (Cell c : cells) {
      total += c.points() * c.count();
    }
    return new Matrix(
        mt.code(), mt.name(), mt.grade(), mins, total, List.copyOf(ps), List.copyOf(cells));
  }

  private static int indexOf(List<Part> ps, String id) {
    for (int i = 0; i < ps.size(); i++) {
      if (ps.get(i).id().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  private static List<Cell> moveChapters(List<Cell> cells, Set<String> keep) {
    List<Cell> kept = new ArrayList<>();
    for (Cell c : cells) {
      List<Opt> opts = c.opts().stream().filter(o -> keep.contains(o.topic())).toList();
      kept.add(opts.isEmpty() ? c.withOpts(List.of()) : c.withOpts(normalize(opts)));
    }
    List<Cell> out = new ArrayList<>();
    for (Cell c : kept) {
      if (!c.opts().isEmpty()) {
        out.add(c);
        continue;
      }
      Cell donor =
          kept.stream()
              .filter(d -> !d.opts().isEmpty() && d.type().equals(c.type()))
              .findFirst()
              .orElse(null);
      List<Opt> opts;
      if (donor != null) {
        // Ô chuyển chương: lấy chủ đề của ô cùng loại câu còn lại, bỏ lọc dạng/YCCĐ (mọi câu cùng
        // chủ đề, loại, mức độ).
        opts =
            donor.opts().stream()
                .map(o -> new Opt(o.topic(), null, List.<String>of(), o.weight()))
                .toList();
      } else {
        opts = keep.stream().map(t -> new Opt(t, null, List.<String>of(), 1.0)).toList();
      }
      out.add(c.withOpts(normalize(opts)));
    }
    return out;
  }

  private static List<Opt> normalize(List<Opt> opts) {
    double s = opts.stream().mapToDouble(Opt::weight).sum();
    if (s <= 0) {
      return opts;
    }
    return opts.stream()
        .map(o -> new Opt(o.topic(), o.dangTx(), o.yccd(), o.weight() / s))
        .toList();
  }

  /** Tỉ lệ mức độ → số ô mỗi mức (dư lớn nhất), gán theo thứ tự mức độ hiện có của ô. */
  private static List<Cell> relevel(List<Cell> cells, Map<String, Double> ratio) {
    int n = cells.size();
    double sum = LEVELS.stream().mapToDouble(l -> ratio.getOrDefault(l, 0.0)).sum();
    if (sum <= 0) {
      return cells;
    }
    int[] cnt = new int[LEVELS.size()];
    double[] rem = new double[LEVELS.size()];
    int used = 0;
    for (int i = 0; i < LEVELS.size(); i++) {
      double exact = n * ratio.getOrDefault(LEVELS.get(i), 0.0) / sum;
      cnt[i] = (int) Math.floor(exact);
      rem[i] = exact - cnt[i];
      used += cnt[i];
    }
    while (used < n) {
      int best = 0;
      for (int i = 1; i < rem.length; i++) {
        best = rem[i] > rem[best] ? i : best;
      }
      cnt[best]++;
      rem[best] = -1;
      used++;
    }
    List<Integer> order = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      order.add(i);
    }
    order.sort(
        Comparator.comparingInt((Integer i) -> LEVELS.indexOf(cells.get(i).level()))
            .thenComparingInt(i -> i));
    List<Cell> out = new ArrayList<>(cells);
    int li = 0;
    int left = cnt[0];
    for (int i : order) {
      while (left == 0 && li < LEVELS.size() - 1) {
        li++;
        left = cnt[li];
      }
      out.set(i, cells.get(i).withLevel(LEVELS.get(li)));
      left--;
    }
    return out;
  }
}
