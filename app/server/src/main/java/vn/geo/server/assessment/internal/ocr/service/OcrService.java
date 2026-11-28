package vn.geo.server.assessment.internal.ocr.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.aigateway.api.AiGateway;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.quota.AiQuota;
import vn.geo.server.assessment.internal.ocr.repo.OcrRepository;
import vn.geo.server.assessment.internal.ocr.repo.OcrRepository.Artifact;
import vn.geo.server.assessment.internal.storage.service.EncryptedObjectStore;
import vn.geo.server.assessment.internal.storage.service.ImageStoreService;
import vn.geo.server.assessment.internal.storage.service.MetadataStripper;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Sub;
import vn.geo.server.assessment.internal.submit.repo.SubmissionRepository.Version;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.common.security.Role;

/**
 * Bản đọc chữ của bài nộp (US-S5E4-04; BR-S5E4-12…14): việc nền sau khi nộp gọi task {@code
 * grade_ocr} mỗi trang một lượt (xin lượt qua {@link AiQuota#admitGrading}: hạn mức lớp + tắt
 * khẩn), gọi lại một lần khi lỗi; ảnh gửi AI là bản đã gỡ EXIF (bản phái sinh JPEG nếu ảnh gốc là
 * HEIC) và KHÔNG kèm tên, mã người dùng, mã lớp (BR-S5E4-29). Đoạn tin cậy &lt; 0,6 tô "chưa chắc".
 * Giáo viên sửa tạo phiên bản mới (ảnh gốc không đổi). Lỗi sau gọi lại: "Chưa đọc được" + Đọc lại
 * (tối đa 3 lần mỗi bài, {@link AiQuota#takeOcrRetry}). Học sinh không xem, không sửa.
 */
@Service
public class OcrService {

  private static final Logger log = LoggerFactory.getLogger(OcrService.class);

  /** Ngưỡng "chưa chắc" (QS5-20). */
  public static final double UNSURE_BELOW = 0.6;

  /**
   * Tiền tố prompt để giả lập nhận biết đọc chữ bài nộp thật (mốc T-261128-59 của MockAiProvider).
   */
  public static final String MARK = "[s5e4b-ocr]";

  public static final String LABEL_PENDING = "Đang đọc chữ";
  public static final String LABEL_DONE = "Đã đọc chữ";
  public static final String LABEL_FAILED = "Chưa đọc được";

  private final OcrRepository repo;
  private final SubmissionRepository subs;
  private final ImageStoreService images;
  private final EncryptedObjectStore store;
  private final AiQuota quota;
  private final AiGateway ai;
  private final JsonMapper mapper;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final Set<UUID> running = ConcurrentHashMap.newKeySet();

  public OcrService(
      OcrRepository repo,
      SubmissionRepository subs,
      ImageStoreService images,
      EncryptedObjectStore store,
      AiQuota quota,
      AiGateway ai,
      JsonMapper mapper) {
    this.repo = repo;
    this.subs = subs;
    this.images = images;
    this.store = store;
    this.quota = quota;
    this.ai = ai;
    this.mapper = mapper;
  }

  // ------------------------------------------------------------------ việc nền

  /** Xếp việc đọc chữ phiên bản {@code versionId} (gọi sau khi giao dịch nộp đã ghi). */
  public void schedule(AuthPrincipal as, UUID submissionId, UUID versionId) {
    running.add(versionId);
    executor.submit(() -> {
      try {
        RequestContext.callAs(
            as,
            () -> {
              run(as.orgId(), submissionId, versionId);
              return null;
            });
      } catch (RuntimeException e) {
        log.warn("Đọc chữ bài nộp lỗi: {}", e.toString());
      } finally {
        running.remove(versionId);
      }
    });
  }

  /** Đọc chữ mọi trang của một phiên bản và ghi một bản (đọc được hoặc "Chưa đọc được"). */
  void run(UUID orgId, UUID submissionId, UUID versionId) {
    Sub sub = subs.submission(orgId, submissionId).orElseThrow();
    Version ver =
        subs.versions(orgId, submissionId).stream()
            .filter(v -> v.id().equals(versionId))
            .findFirst()
            .orElseThrow();
    ArrayNode pages = mapper.createArrayNode();
    boolean ok = true;
    for (int p = 1; p <= ver.pages(); p++) {
      AiQuota.Admission a = quota.admitGrading(orgId, sub.classId(), "grade_ocr");
      if (!a.allowed()) {
        ok = false;
        break;
      }
      String prompt = prompt(p, aiImage(orgId, versionId, p));
      JsonNode reply = null;
      for (int attempt = 0; attempt < 2 && reply == null; attempt++) { // gọi lại 1 lần (PO-78)
        try {
          reply = mapper.readTree(ai.complete(new AiRequest("grade_ocr", prompt)).content());
        } catch (RuntimeException e) {
          log.info(
              "grade_ocr trang {} lần {} lỗi: {}", p, attempt + 1, e.getClass().getSimpleName());
        }
      }
      if (reply == null) {
        ok = false;
        break;
      }
      pages.add(page(p, reply));
    }
    if (ok) {
      repo.deleteFailed(orgId, versionId);
    }
    repo.append(
        orgId,
        Uuid7.next(),
        submissionId,
        versionId,
        sub.studentId(),
        ok ? "done" : "failed",
        ok ? mapper.writeValueAsString(pages) : "[]",
        null,
        sub.schoolYear());
  }

  /**
   * Ảnh gửi AI: bản phái sinh JPEG nếu có (ảnh gốc HEIC), không thì ảnh gốc; luôn gỡ metadata lại.
   */
  public byte[] aiImage(UUID orgId, UUID versionId, int pageNo) {
    return subs.derivedKey(orgId, versionId, pageNo)
        .map(k -> MetadataStripper.strip(store.read(k)).bytes())
        .orElseGet(() -> images.forAi(orgId, versionId, pageNo));
  }

  /** Prompt một trang: chỉ số trang và ảnh (JPEG/PNG base64) — không tên, mã người dùng, mã lớp. */
  public static String prompt(int pageNo, byte[] image) {
    String type = image.length > 1 && (image[0] & 0xFF) == 0x89 ? "image/png" : "image/jpeg";
    return MARK
        + " trang "
        + pageNo
        + ". Đọc chữ viết tay (bài làm hình học, ký hiệu toán √ ⊥ ²) thành các đoạn, mỗi đoạn có"
        + " độ tin cậy 0–1 và vùng ảnh {x, y, w, h}. Không giải bài, không chấm. Ảnh: data:"
        + type
        + ";base64,"
        + Base64.getEncoder().encodeToString(image);
  }

  /** Chuẩn hoá trả lời AI thành trang {pageNo, segments[{text, confidence, unsure, region}]}. */
  ObjectNode page(int pageNo, JsonNode reply) {
    ObjectNode out = mapper.createObjectNode();
    out.put("pageNo", pageNo);
    ArrayNode segs = out.putArray("segments");
    JsonNode in = reply.path("segments");
    if (in.isArray()) {
      for (JsonNode s : in) {
        double c = s.path("confidence").asDouble(1.0);
        ObjectNode o = segs.addObject();
        o.put("text", s.path("text").asString(""));
        o.put("confidence", c);
        o.put("unsure", c < UNSURE_BELOW);
        if (s.has("region")) {
          o.set("region", s.get("region"));
        }
      }
    } else if (reply.has("text")) {
      ObjectNode o = segs.addObject();
      o.put("text", reply.path("text").asString(""));
      o.put("confidence", 1.0);
      o.put("unsure", false);
    }
    return out;
  }

  // ------------------------------------------------------------------ API giáo viên

  /** Bài nộp mà giáo viên chủ lớp được xem; học sinh FORBIDDEN; ngoài quan hệ NOT_FOUND. */
  private Sub teacherSub(UUID submissionId) {
    AuthPrincipal me = RequestContext.principal();
    if (me.role() != Role.TEACHER) {
      throw new AppException(ErrorCode.FORBIDDEN);
    }
    Sub s =
        subs.submission(me.orgId(), submissionId)
            .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    if (!subs.classTeacher(me.orgId(), s.classId()).map(me.userId()::equals).orElse(false)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return s;
  }

  private Version current(UUID orgId, Sub s) {
    return subs.version(orgId, s.id(), s.currentVersion())
        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }

  /** Bản đọc chữ của phiên bản chấm; {@code version} khác null = phiên bản bản đọc chữ đó. */
  public Map<String, Object> get(UUID submissionId, Integer version) {
    Sub s = teacherSub(submissionId);
    UUID org = RequestContext.principal().orgId();
    Version v = current(org, s);
    List<Artifact> all = repo.ofVersion(org, v.id());
    List<Artifact> done = all.stream().filter(a -> "done".equals(a.status())).toList();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("submissionId", s.id().toString());
    m.put("submissionVersion", v.version());
    Artifact pick = null;
    if (version != null) {
      pick =
          done.stream()
              .filter(a -> a.version() == version)
              .findFirst()
              .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    } else if (!all.isEmpty()) {
      pick = all.get(all.size() - 1);
    }
    if (pick == null) {
      m.put("status", "pending");
      m.put("statusLabel", LABEL_PENDING);
      m.put("canRetry", !running.contains(v.id()));
      m.put("pages", List.of());
      m.put("versions", List.of());
      return m;
    }
    boolean failed = "failed".equals(pick.status());
    m.put("status", failed ? "failed" : "done");
    m.put("statusLabel", failed ? LABEL_FAILED : LABEL_DONE);
    m.put("canRetry", failed && !running.contains(v.id()));
    if (!failed) {
      m.put("version", pick.version());
      m.put("editedBy", pick.editedBy() == null ? null : pick.editedBy().toString());
      m.put("aiAuthored", pick.editedBy() == null);
      m.put("createdAt", pick.createdAt().toString());
    }
    m.put("pages", mapper.readTree(pick.pagesJson()));
    List<Map<String, Object>> vs = new ArrayList<>();
    for (Artifact a : done) {
      Map<String, Object> x = new LinkedHashMap<>();
      x.put("version", a.version());
      x.put("editedBy", a.editedBy() == null ? null : a.editedBy().toString());
      x.put("createdAt", a.createdAt().toString());
      vs.add(x);
    }
    m.put("versions", vs);
    return m;
  }

  /**
   * Thân sửa: các trang với các đoạn (văn bản) theo thứ tự; {@code baseVersion} là bản đang sửa.
   */
  public record EditRequest(Integer baseVersion, List<EditPage> pages) {}

  public record EditPage(Integer pageNo, List<EditSegment> segments) {}

  public record EditSegment(String text) {}

  /** Giáo viên sửa bản đọc chữ: tạo phiên bản mới, ghi người sửa (BR-S5E4-13). */
  public Map<String, Object> edit(UUID submissionId, EditRequest req) {
    Sub s = teacherSub(submissionId);
    AuthPrincipal me = RequestContext.principal();
    Version v = current(me.orgId(), s);
    Artifact base =
        repo.latest(me.orgId(), v.id())
            .filter(a -> "done".equals(a.status()))
            .orElseThrow(() -> new AppException(ErrorCode.CONFLICT, "Bản đọc chữ chưa có để sửa."));
    if (req == null || req.pages() == null || req.pages().isEmpty()) {
      throw AppException.invalidField("pages", "Cần ít nhất một trang.");
    }
    if (req.baseVersion() != null && req.baseVersion() != base.version()) {
      throw new AppException(
          ErrorCode.CONFLICT, "Bản đọc chữ đã được sửa ở phiên bản mới hơn. Tải lại rồi sửa tiếp.");
    }
    JsonNode old = mapper.readTree(base.pagesJson());
    ArrayNode pages = mapper.createArrayNode();
    for (JsonNode op : old) {
      int no = op.path("pageNo").asInt();
      EditPage ep =
          req.pages().stream()
              .filter(x -> x.pageNo() != null && x.pageNo() == no)
              .findFirst()
              .orElse(null);
      if (ep == null || ep.segments() == null) {
        pages.add(op);
        continue;
      }
      ObjectNode np = mapper.createObjectNode();
      np.put("pageNo", no);
      ArrayNode segs = np.putArray("segments");
      JsonNode os = op.path("segments");
      for (int i = 0; i < ep.segments().size(); i++) {
        String text =
            ep.segments().get(i).text() == null ? "" : ep.segments().get(i).text().strip();
        if (text.length() > 2000) {
          throw AppException.invalidField("pages", "Mỗi đoạn tối đa 2000 ký tự.");
        }
        JsonNode prev = i < os.size() ? os.get(i) : null;
        ObjectNode seg = segs.addObject();
        seg.put("text", text);
        boolean same = prev != null && text.equals(prev.path("text").asString(""));
        if (same) {
          seg.put("confidence", prev.path("confidence").asDouble(1.0));
          seg.put("unsure", prev.path("unsure").asBoolean(false));
        } else {
          seg.put("confidence", 1.0);
          seg.put("unsure", false);
          seg.put("edited", true);
        }
        if (prev != null && prev.has("region")) {
          seg.set("region", prev.get("region"));
        }
      }
      pages.add(np);
    }
    repo.append(
        me.orgId(),
        Uuid7.next(),
        s.id(),
        v.id(),
        s.studentId(),
        "done",
        mapper.writeValueAsString(pages),
        me.userId(),
        s.schoolYear());
    return get(submissionId, null);
  }

  /** "Đọc lại" sau khi lỗi: tối đa 3 lần mỗi bài (lần thứ 4 RATE_LIMITED, không gọi AI). */
  public Map<String, Object> retry(UUID submissionId) {
    Sub s = teacherSub(submissionId);
    AuthPrincipal me = RequestContext.principal();
    Version v = current(me.orgId(), s);
    if (running.contains(v.id())) {
      throw new AppException(ErrorCode.CONFLICT, "Bài đang được đọc chữ.");
    }
    boolean failed =
        repo.latest(me.orgId(), v.id()).map(a -> "failed".equals(a.status())).orElse(true);
    if (!failed) {
      throw new AppException(ErrorCode.CONFLICT, "Bản đọc chữ đã có.");
    }
    quota.takeOcrRetry(me.orgId(), s.id());
    schedule(me, s.id(), v.id());
    return get(submissionId, null);
  }

  /** Việc đọc chữ của phiên bản còn đang chạy. */
  public boolean running(UUID versionId) {
    return running.contains(versionId);
  }
}
