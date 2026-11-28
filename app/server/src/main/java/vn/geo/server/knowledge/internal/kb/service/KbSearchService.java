package vn.geo.server.knowledge.internal.kb.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.geo.server.knowledge.api.KbRetriever;
import vn.geo.server.knowledge.internal.kb.repo.KbSearchRepository;
import vn.geo.server.knowledge.internal.kb.repo.KbSearchRepository.Row;

/**
 * Bộ truy xuất mục (US-S5E3-07, BR-S5E3-24, QS5-09): từ của câu hỏi (đã bỏ dấu) trừ từ dừng tiếng
 * Việt nối OR; điểm = {@code ts_rank_cd} chuẩn hoá; chỉ giữ mục điểm ≥ {@code geo.kb.min-score}
 * (mặc định 0,1); mục có mã kiến thức trùng mã của đề được cộng {@link #CODE_BONUS} (= 1, nên luôn
 * đứng trước mục không trùng mã) khi xếp; tối đa {@link #TOP} mục.
 */
@Service
public class KbSearchService implements KbRetriever {

  public static final int TOP = 5;
  public static final double CODE_BONUS = 1.0;
  static final int CANDIDATES = 50;

  /** Từ dừng (đã bỏ dấu): hư từ, đại từ xưng hô thường gặp trong câu hỏi của học sinh. */
  static final Set<String> STOP =
      Set.of(
          "a", "ai", "anh", "ban", "bang", "bao", "bi", "cac", "cai", "chi", "cho", "co", "cua",
          "cung", "da", "dang", "day", "de", "den", "deu", "di", "do", "duoc", "em", "gi", "hay",
          "hoac", "khi", "khong", "la", "lam", "lai", "lay", "ma", "minh", "moi", "mot", "nao",
          "nay", "neu", "nhe", "nhu", "nhung", "nua", "o", "oi", "phai", "ra", "roi", "sao", "se",
          "sau", "thay", "the", "thi", "tren", "trong", "tu", "va", "vay", "ve", "vi", "voi",
          "vua");

  private final KbSearchRepository repo;
  private final double minScore;

  public KbSearchService(
      KbSearchRepository repo, @Value("${geo.kb.min-score:0.1}") double minScore) {
    this.repo = repo;
    this.minScore = minScore;
  }

  public double minScore() {
    return minScore;
  }

  @Override
  public List<Hit> retrieve(
      UUID orgId,
      UUID classId,
      UUID assignmentId,
      String query,
      Collection<String> knowledgeCodes,
      Instant at) {
    if (query == null || query.isBlank() || classId == null) {
      return List.of();
    }
    List<String> terms = new ArrayList<>();
    for (String t : repo.terms(query.length() > 2000 ? query.substring(0, 2000) : query)) {
      if (t.matches("[a-z0-9]{1,40}") && !STOP.contains(t)) {
        terms.add(t);
      }
    }
    if (terms.isEmpty()) {
      return List.of();
    }
    String tsq = String.join(" | ", terms);
    Set<String> codes = knowledgeCodes == null ? Set.of() : Set.copyOf(knowledgeCodes);
    List<Hit> hits = new ArrayList<>();
    for (Row r : repo.search(orgId, classId, assignmentId, tsq, at, CANDIDATES)) {
      if (r.rank() < minScore) {
        continue;
      }
      boolean boosted = r.codes().stream().anyMatch(codes::contains);
      hits.add(
          new Hit(
              r.sectionId(),
              r.docId(),
              r.docCode(),
              r.docTitle(),
              r.version(),
              r.ord(),
              r.title(),
              r.body(),
              r.codes(),
              round(r.rank() + (boosted ? CODE_BONUS : 0)),
              boosted));
    }
    hits.sort(
        Comparator.comparingDouble(Hit::score)
            .reversed()
            .thenComparing(Hit::docCode)
            .thenComparingInt(Hit::ord));
    return hits.size() > TOP ? List.copyOf(hits.subList(0, TOP)) : List.copyOf(hits);
  }

  private static double round(double d) {
    return Math.round(d * 10_000) / 10_000.0;
  }
}
