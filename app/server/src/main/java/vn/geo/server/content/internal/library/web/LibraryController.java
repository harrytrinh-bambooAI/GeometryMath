package vn.geo.server.content.internal.library.web;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.content.internal.library.service.LibraryService;
import vn.geo.server.content.internal.library.service.LibraryViews.CardPage;
import vn.geo.server.content.internal.library.service.LibraryViews.Created;
import vn.geo.server.content.internal.library.service.LibraryViews.Decision;
import vn.geo.server.content.internal.library.service.LibraryViews.DocDetail;
import vn.geo.server.content.internal.library.service.LibraryViews.Hint;
import vn.geo.server.content.internal.library.service.LibraryViews.LibraryDetail;
import vn.geo.server.content.internal.library.service.LibraryViews.LockedSolution;
import vn.geo.server.content.internal.library.service.LibraryViews.Queue;
import vn.geo.server.content.internal.library.service.LibraryViews.QueueDetail;
import vn.geo.server.content.internal.library.service.LibraryViews.Similar;
import vn.geo.server.content.internal.library.service.LibraryViews.StudentLibraryItem;

/**
 * S2-E6 — Kho đề (C2-01). Mọi phản hồi no-store: Kho chung chứa lời giải, hàng đợi chứa bước giải
 * (D-GEO-145).
 */
@RestController
@NoStore
public class LibraryController {

  private final LibraryService service;

  public LibraryController(LibraryService service) {
    this.service = service;
  }

  public record CreateBody(String title, String statement) {}

  public record SubmitBody(String hint) {}

  public record RejectBody(String reason) {}

  public record SimilarBody(String statement) {}

  @GetMapping("/api/v1/teacher/library")
  public CardPage list(
      @RequestParam(required = false) String shelf,
      @RequestParam(required = false) String kind,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.list(shelf, kind, q, limit, cursor);
  }

  @PostMapping("/api/v1/teacher/library/problems")
  @ResponseStatus(HttpStatus.CREATED)
  public Created create(@RequestBody CreateBody body) {
    return service.create(
        body == null ? null : body.title(), body == null ? null : body.statement());
  }

  @PostMapping("/api/v1/teacher/library/similar")
  public Similar similar(@RequestBody SimilarBody body) {
    return service.similar(body == null ? null : body.statement());
  }

  @GetMapping("/api/v1/teacher/library/problems/{id}")
  public DocDetail doc(@PathVariable UUID id) {
    return service.doc(id);
  }

  @PostMapping("/api/v1/teacher/library/problems/{id}/submit")
  public Decision submit(@PathVariable UUID id, @RequestBody(required = false) SubmitBody body) {
    return service.submit(id, body == null ? null : body.hint());
  }

  @GetMapping("/api/v1/teacher/library/shared/{id}")
  public LibraryDetail shared(@PathVariable UUID id) {
    return service.libraryItem(id);
  }

  @PostMapping("/api/v1/teacher/library/shared/{id}/copy")
  @ResponseStatus(HttpStatus.CREATED)
  public Created copy(@PathVariable UUID id) {
    return service.copy(id);
  }

  @GetMapping("/api/v1/teacher/library/review-queue")
  public Queue queue() {
    return service.queue();
  }

  @GetMapping("/api/v1/teacher/library/review-queue/{id}")
  public QueueDetail queueItem(@PathVariable UUID id) {
    return service.queueItem(id);
  }

  @PostMapping("/api/v1/teacher/library/review-queue/{id}/accept")
  public Decision accept(@PathVariable UUID id) {
    return service.accept(id);
  }

  @PostMapping("/api/v1/teacher/library/review-queue/{id}/reject")
  public Decision reject(@PathVariable UUID id, @RequestBody(required = false) RejectBody body) {
    return service.reject(id, body == null ? null : body.reason());
  }

  @GetMapping("/api/v1/student/library/{id}")
  public StudentLibraryItem studentItem(@PathVariable UUID id) {
    return service.studentItem(id);
  }

  @GetMapping("/api/v1/student/library/{id}/hint")
  public Hint studentHint(@PathVariable UUID id) {
    return service.studentHint(id);
  }

  @GetMapping("/api/v1/student/library/{id}/solution")
  public LockedSolution studentSolution(@PathVariable UUID id) {
    return service.studentSolution(id);
  }
}
