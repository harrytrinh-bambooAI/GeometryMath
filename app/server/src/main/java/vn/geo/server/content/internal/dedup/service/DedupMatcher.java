package vn.geo.server.content.internal.dedup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import vn.geo.server.content.internal.dedup.repo.DedupRepository;

/**
 * S2-E4 (US-S2E4-03): đối chiếu một chữ đề với kho trong phạm vi BR-S2E4-12 (Kho chung + Của tôi
 * của chính giáo viên). Trùng tuyệt đối thắng gần giống; nhiều đề gần giống thì lấy đề giống nhất.
 */
@Component
public class DedupMatcher {

  /** Một đề trong kho đã chuẩn hóa sẵn để so nhiều lần. */
  public record Candidate(String kind, UUID id, String ref, String norm) {}

  /**
   * Kết quả đối chiếu: match NONE thì các trường còn lại null. refKind = library | mine | draft.
   */
  public record Hit(
      Dedup.Match match, String label, String refKind, UUID refId, String ref, double similarity) {
    public static final Hit NONE = new Hit(Dedup.Match.NONE, null, null, null, null, 0);
  }

  private final DedupRepository repo;

  public DedupMatcher(DedupRepository repo) {
    this.repo = repo;
  }

  /** Kho để so của một giáo viên (gọi một lần cho cả tệp import). */
  public List<Candidate> candidates(UUID owner) {
    List<Candidate> out = new ArrayList<>();
    for (DedupRepository.Row r : repo.library()) {
      out.add(new Candidate(r.kind(), r.id(), r.ref(), Dedup.normalize(r.statement())));
    }
    for (DedupRepository.Row r : repo.mineDocs(owner)) {
      out.add(new Candidate(r.kind(), r.id(), r.ref(), Dedup.normalize(r.statement())));
    }
    for (DedupRepository.Row r : repo.mineDrafts(owner)) {
      out.add(new Candidate(r.kind(), r.id(), r.ref(), Dedup.normalize(r.statement())));
    }
    return out;
  }

  /** So một chữ đề với kho; {@code skipDraft} = nháp không tính (chính nháp sinh từ mục này). */
  public static Hit match(String text, List<Candidate> candidates) {
    String norm = Dedup.normalize(text);
    if (norm.isEmpty()) {
      return Hit.NONE;
    }
    Candidate best = null;
    double bestSim = 0;
    for (Candidate c : candidates) {
      double sim = Dedup.similarity(norm, c.norm());
      if (sim > bestSim) {
        best = c;
        bestSim = sim;
        if (sim >= 1.0) {
          break;
        }
      }
    }
    Dedup.Match m = Dedup.classify(bestSim);
    if (best == null || m == Dedup.Match.NONE) {
      return Hit.NONE;
    }
    return new Hit(m, Dedup.label(m, best.ref()), best.kind(), best.id(), best.ref(), bestSim);
  }
}
