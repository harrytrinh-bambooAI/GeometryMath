package vn.geo.server.knowledge.internal.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.knowledge.api.LectureCatalog;
import vn.geo.server.knowledge.internal.repo.LectureRepository;
import vn.geo.server.knowledge.internal.repo.LectureRepository.LectureRow;

/**
 * Bài giảng mẫu (US-GEO-411, 412): hộp bài giảng gồm tên khái niệm, nội dung, dòng "Nguồn: …"
 * (BR-GEO-198). Bài "AI soạn" mang nhãn "AI soạn · chờ giáo viên duyệt" (BR-GEO-199); bài "Giáo án
 * mẫu" chưa có người soát hiện "Nội dung đang chờ giáo viên soát" cho cả học sinh và giáo viên
 * (BR-GEO-296). Không có nhãn "Giáo viên đã duyệt" ở MVP (D-GEO-028, 111).
 */
@Service
public class LectureService implements LectureCatalog {

  public static final String MSG_39 = "Chưa có bài giảng cho từ khóa này.";
  public static final String AI_PENDING = "AI soạn · chờ giáo viên duyệt";
  public static final String REVIEW_PENDING = "Nội dung đang chờ giáo viên soát";

  /** Hộp bài giảng (COM_HOP_BAI_GIANG). */
  public record LectureView(
      String slug,
      String title,
      String body,
      String sourceLabel,
      String sourceRef,
      String aiLabel,
      String reviewNote) {}

  public record Keyword(String term, String slug) {}

  private final LectureRepository repo;

  public LectureService(LectureRepository repo) {
    this.repo = repo;
  }

  public List<Keyword> keywords() {
    RequestContext.principal();
    return repo.keywords().stream().map(k -> new Keyword(k.term(), k.slug())).toList();
  }

  /** Bấm từ khóa: cụm từ không có bài giảng => NOT_FOUND (MSG-GEO-39, US-GEO-411-FC1). */
  public LectureView byTerm(String term) {
    RequestContext.principal();
    return repo.byTerm(termKey(term))
        .map(LectureService::view)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, MSG_39));
  }

  public LectureView bySlugView(String slug) {
    RequestContext.principal();
    return repo.bySlug(slug)
        .map(LectureService::view)
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, MSG_39));
  }

  @Override
  public Optional<LectureRef> bySlug(String slug) {
    return slug == null || slug.isBlank()
        ? Optional.empty()
        : repo.bySlug(slug).map(LectureService::ref);
  }

  @Override
  public Optional<LectureRef> byId(UUID id) {
    return id == null ? Optional.empty() : repo.byId(id).map(LectureService::ref);
  }

  /** Khóa tra từ khóa: chuẩn hóa NFC, gộp khoảng trắng, chữ thường. */
  public static String termKey(String term) {
    String t = term == null ? "" : Normalizer.normalize(term, Normalizer.Form.NFC);
    return t.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }

  static String sourceLabel(String sourceKind) {
    return "teacher_authored".equals(sourceKind) ? "Giáo án mẫu" : "AI soạn";
  }

  private static LectureRef ref(LectureRow r) {
    return new LectureRef(r.id(), r.slug(), r.title(), r.sourceKind(), sourceLabel(r.sourceKind()));
  }

  private static LectureView view(LectureRow r) {
    boolean teacher = "teacher_authored".equals(r.sourceKind());
    return new LectureView(
        r.slug(),
        r.title(),
        r.body(),
        sourceLabel(r.sourceKind()),
        r.sourceRef(),
        teacher ? null : AI_PENDING,
        teacher && !r.humanReviewed() ? REVIEW_PENDING : null);
  }
}
