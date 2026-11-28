package vn.geo.server.knowledge.internal.kb.web;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.knowledge.internal.kb.service.KbDocumentService;
import vn.geo.server.knowledge.internal.kb.service.KbDocumentService.CreateRequest;
import vn.geo.server.knowledge.internal.kb.service.KbDocumentService.EditRequest;
import vn.geo.server.knowledge.internal.kb.service.KbDocumentService.FileOut;
import vn.geo.server.knowledge.internal.kb.service.KbDocumentService.Result;
import vn.geo.server.knowledge.internal.kb.service.KbDocumentService.UploadRequest;
import vn.geo.server.knowledge.internal.kb.service.KbGrantService;
import vn.geo.server.knowledge.internal.kb.service.KbGrantService.GrantRequest;

/**
 * Kho tri thức của giáo viên (US-S5E3-01, 02). Chỉ vai giáo viên (học sinh FORBIDDEN), chỉ tài liệu
 * của chính mình (người khác NOT_FOUND). {@code {ref}} = id hoặc mã TL-nn.
 *
 * <ul>
 *   <li>{@code POST /api/v1/teacher/knowledge/uploads} {fileName, sizeBytes}: docx/pdf/md ≤ 10 MB
 *       (MSG-S5-22); {@code PUT …/uploads/{id}?offset=} (application/octet-stream, mỗi đoạn ≤ 768
 *       KB vì thân yêu cầu ≤ 1 MB).
 *   <li>{@code POST /api/v1/teacher/knowledge} {uploadId | title+content | fromLessonPlan}: 201 tài
 *       liệu Nháp v1; tệp trùng → 200 {@code duplicate=true}.
 *   <li>{@code GET …}, {@code GET …/{ref}[?version=]}, {@code GET …/{ref}/versions/{v}/file}.
 *   <li>{@code PUT …/{ref}/versions/{v}} {content}; {@code POST …/versions/{v}/submit|activate};
 *       {@code POST …/{ref}/archive}.
 *   <li>{@code POST|GET …/{ref}/grants}, {@code DELETE …/{ref}/grants/{id}}; {@code GET
 *       /api/v1/teacher/classes/{classId}/knowledge[?assignmentId=&at=]}.
 * </ul>
 */
@RestController
@NoStore
public class KbController {

  static final String BASE = "/api/v1/teacher/knowledge";

  private final KbDocumentService docs;
  private final KbGrantService grants;

  public KbController(KbDocumentService docs, KbGrantService grants) {
    this.docs = docs;
    this.grants = grants;
  }

  @PostMapping(BASE + "/uploads")
  public ResponseEntity<Map<String, Object>> startUpload(@RequestBody UploadRequest body) {
    return ResponseEntity.status(201).body(docs.startUpload(body));
  }

  @PutMapping(path = BASE + "/uploads/{id}", consumes = "application/octet-stream")
  public Map<String, Object> part(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int offset,
      @RequestBody byte[] chunk) {
    return docs.uploadPart(id, offset, chunk);
  }

  @PostMapping(BASE)
  public ResponseEntity<Map<String, Object>> create(@RequestBody CreateRequest body) {
    Result r = docs.create(body);
    return ResponseEntity.status(r.status()).body(r.body());
  }

  @GetMapping(BASE)
  public Map<String, Object> list() {
    return docs.list();
  }

  @GetMapping(BASE + "/{ref}")
  public Map<String, Object> get(
      @PathVariable String ref, @RequestParam(required = false) Integer version) {
    return docs.get(ref, version);
  }

  @GetMapping(BASE + "/{ref}/versions/{version}/file")
  public ResponseEntity<byte[]> file(@PathVariable String ref, @PathVariable int version) {
    FileOut f = docs.file(ref, version);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(f.fileMime()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(f.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(f.bytes());
  }

  @PutMapping(BASE + "/{ref}/versions/{version}")
  public ResponseEntity<Map<String, Object>> edit(
      @PathVariable String ref, @PathVariable int version, @RequestBody EditRequest body) {
    Result r = docs.edit(ref, version, body);
    return ResponseEntity.status(r.status()).body(r.body());
  }

  @PostMapping(BASE + "/{ref}/versions/{version}/submit")
  public Map<String, Object> submit(@PathVariable String ref, @PathVariable int version) {
    return docs.submit(ref, version);
  }

  @PostMapping(BASE + "/{ref}/versions/{version}/activate")
  public Map<String, Object> activate(@PathVariable String ref, @PathVariable int version) {
    return docs.activate(ref, version);
  }

  @PostMapping(BASE + "/{ref}/archive")
  public Map<String, Object> archive(@PathVariable String ref) {
    return docs.archive(ref);
  }

  @PostMapping(BASE + "/{ref}/grants")
  public ResponseEntity<Map<String, Object>> grant(
      @PathVariable String ref, @RequestBody GrantRequest body) {
    return ResponseEntity.status(201).body(grants.grant(ref, body));
  }

  @GetMapping(BASE + "/{ref}/grants")
  public Map<String, Object> grants(@PathVariable String ref) {
    return grants.list(ref);
  }

  @DeleteMapping(BASE + "/{ref}/grants/{id}")
  public Map<String, Object> revoke(@PathVariable String ref, @PathVariable UUID id) {
    return grants.revoke(ref, id);
  }

  @GetMapping("/api/v1/teacher/classes/{classId}/knowledge")
  public Map<String, Object> ofClass(
      @PathVariable UUID classId,
      @RequestParam(required = false) String assignmentId,
      @RequestParam(required = false) String at) {
    return grants.ofClass(classId, assignmentId, at);
  }
}
