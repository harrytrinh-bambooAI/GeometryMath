package vn.geo.server.tracking.internal.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.redis.RedisKeys;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.learning.api.HintApi;
import vn.geo.server.tracking.internal.repo.FigureEventRepository;

/**
 * Thao tác trên hình (E3): chỉ công cụ Chọn, Điểm, Đường, Mặt phẳng, Mở rộng, Đo dài, Đo góc (mã
 * 1…7) là "thao tác trên hình"; xoay, thu phóng, đặt lại không gửi sự kiện (BR-GEO-144, 147;
 * D-GEO-058). Lô lũy đẳng theo batchId (ADR-GEO-11, khóa Redis một ngày). Mở bài: sự kiện 'open'
 * lũy đẳng theo requestId. Gợi ý: ủy quyền cho HintApi của E4 (module learning: fn_get_hint, gợi ý
 * AI của bài tự học, thông điệp MSG-GEO-32, 34).
 */
@Service
public class FigureEventService {

  public record ToolEvent(Integer tool, Instant at) {}

  public record Recorded(int inserted) {}

  static final int MAX_BATCH = 200;
  private static final Duration BATCH_TTL = Duration.ofDays(1);
  private static final Duration PENDING_TTL = Duration.ofMinutes(2);
  private static final String PENDING = "pending";
  private static final String DONE = "done";

  private final FigureEventRepository repo;
  private final StringRedisTemplate redis;
  private final RedisKeys keys;
  private final JsonMapper mapper;
  private final HintApi hints;

  public FigureEventService(
      FigureEventRepository repo,
      StringRedisTemplate redis,
      RedisKeys keys,
      JsonMapper mapper,
      HintApi hints) {
    this.repo = repo;
    this.redis = redis;
    this.keys = keys;
    this.mapper = mapper;
    this.hints = hints;
  }

  public Recorded recordTools(UUID assignmentId, UUID batchId, List<ToolEvent> events) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    if (batchId == null) {
      throw AppException.invalidField("batchId", "Thiếu mã lô.");
    }
    if (events == null || events.isEmpty() || events.size() > MAX_BATCH) {
      throw AppException.invalidField("events", "Lô có từ 1 đến 200 sự kiện.");
    }
    List<Map<String, Object>> rows = new ArrayList<>();
    Instant now = Instant.now();
    for (ToolEvent e : events) {
      if (e == null || e.tool() == null || e.tool() < 1 || e.tool() > 7) {
        throw AppException.invalidField("events", "Mã công cụ từ 1 đến 7.");
      }
      rows.add(Map.of("tool", e.tool(), "at", (e.at() == null ? now : e.at()).toString()));
    }
    // Chống ghi trùng khi gửi lại (soát E3 #6): giữ chỗ ngắn hạn "pending" để hai lần gửi đồng thời
    // không cùng ghi;
    // chỉ đánh dấu "done" (TTL 1 ngày) SAU KHI ghi DB thành công. Ghi lỗi (DB, lô không thuộc học
    // sinh) thì gỡ khóa
    // để lần gửi lại (cùng batchId) được ghi.
    String key = keys.key("figbatch", p.orgId().toString(), batchId.toString());
    Boolean fresh = redis.opsForValue().setIfAbsent(key, PENDING, PENDING_TTL);
    if (Boolean.FALSE.equals(fresh)) {
      if (DONE.equals(redis.opsForValue().get(key))) {
        return new Recorded(0); // lô đã ghi (gửi lại do mạng): không ghi trùng
      }
      throw new AppException(ErrorCode.CONFLICT, "Lô đang được ghi, gửi lại sau.");
    }
    FigureEventRepository.BatchResult r;
    try {
      r = repo.recordTools(p.userId(), assignmentId, mapper.writeValueAsString(rows));
    } catch (RuntimeException ex) {
      redis.delete(key);
      throw ex;
    }
    if (!"OK".equals(r.status())) {
      redis.delete(key);
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    redis.opsForValue().set(key, DONE, BATCH_TTL);
    return new Recorded(r.inserted());
  }

  public Map<String, String> recordOpen(UUID assignmentId, UUID requestId) {
    AuthPrincipal p = Roles.require(Role.STUDENT);
    if (requestId == null) {
      throw AppException.invalidField("requestId", "Thiếu mã yêu cầu.");
    }
    String s = repo.recordOpen(p.userId(), assignmentId, requestId);
    if ("NOT_FOUND".equals(s)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return Map.of("status", "DUPLICATE".equals(s) ? "DUPLICATE" : "OK");
  }

  /** Gợi ý: logic đầy đủ của E4 (module learning, US-GEO-404, 406) qua HintApi. */
  public HintApi.HintResult hint(UUID assignmentId, int level, UUID requestId) {
    return hints.hint(assignmentId, level, requestId);
  }
}
