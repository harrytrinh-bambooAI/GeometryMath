package vn.geo.server.assessment.internal.queue.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository;
import vn.geo.server.assessment.internal.assign.repo.GradedAssignmentRepository.Row;
import vn.geo.server.assessment.internal.review.repo.GradingRepository;
import vn.geo.server.assessment.internal.review.repo.GradingRepository.Item;
import vn.geo.server.assessment.internal.review.service.GradingSupport;
import vn.geo.server.assessment.internal.review.service.ReviewRules;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;

/**
 * Hàng chờ chấm C5-04 (US-S5E4-05; T-261128-60): bài Chờ chấm và Đang xử lý của lớp mình
 * (BR-S5E4-15), bài có cờ "Chưa đọc được" / "Nộp muộn" trước rồi theo giờ nộp tăng dần; không có
 * cột điểm hay xếp hạng (BR-S5E4-16). Lọc lớp ngoài quan hệ → NOT_FOUND.
 */
@Service
public class QueueService {

  private final GradedAssignmentRepository assignments;
  private final GradingRepository repo;
  private final GradingSupport support;

  public QueueService(
      GradedAssignmentRepository assignments, GradingRepository repo, GradingSupport support) {
    this.assignments = assignments;
    this.repo = repo;
    this.support = support;
  }

  private List<Row> scope(AuthPrincipal me, UUID classId, UUID assignmentId) {
    if (classId != null && !support.owns(me, classId)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    if (assignmentId != null) {
      Row r = support.ownedAssignment(me, assignmentId);
      if (classId != null && !classId.equals(r.classId())) {
        throw new AppException(ErrorCode.NOT_FOUND);
      }
      return List.of(r);
    }
    return assignments.ofTeacher(me.orgId(), me.userId(), classId).stream()
        .filter(r -> support.owns(me, r.classId()))
        .toList();
  }

  /** GET hàng chờ theo lớp và/hoặc bài. */
  public Map<String, Object> queue(UUID classId, UUID assignmentId) {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<Row> rows = scope(me, classId, assignmentId);
    List<Item> queued = new ArrayList<>();
    int submitted = 0;
    int waiting = 0;
    int processing = 0;
    int approved = 0;
    int returned = 0;
    int notSubmitted = 0;
    List<Map<String, Object>> list = new ArrayList<>();
    for (Row r : rows) {
      List<Item> items = repo.items(me.orgId(), r.id());
      submitted += items.size();
      for (Item i : items) {
        switch (GradingSupport.status(i)) {
          case GradingSupport.PROCESSING -> processing++;
          case "approved" -> approved++;
          case "returned" -> returned++;
          default -> waiting++;
        }
        if (GradingSupport.queued(i)) {
          queued.add(i);
        }
      }
      notSubmitted += repo.notSubmitted(me.orgId(), r.classId(), r.id());
      Map<String, Object> a = new LinkedHashMap<>();
      a.put("id", r.id().toString());
      a.put("code", r.code());
      a.put("title", r.title());
      a.put("classId", r.classId().toString());
      a.put("className", r.className());
      a.put("dueAt", r.dueAt().toString());
      a.put("status", r.status());
      list.add(a);
    }
    queued.sort(GradingSupport.ORDER);
    Map<String, Object> m = new LinkedHashMap<>();
    String state;
    if (queued.isEmpty()) {
      state = submitted == 0 ? "empty" : "done";
    } else if (processing == queued.size()) {
      state = "processing";
    } else {
      state = "ready";
    }
    m.put("state", state);
    if (rows.size() == 1) {
      m.put("dueAt", rows.get(0).dueAt().toString());
      m.put("assignmentId", rows.get(0).id().toString());
      m.put("returnable", approved > 0);
    }
    Map<String, Object> sum = new LinkedHashMap<>();
    sum.put("waiting", waiting);
    sum.put("processing", processing);
    sum.put("notSubmitted", notSubmitted);
    sum.put("approved", approved);
    sum.put("returned", returned);
    m.put("summary", sum);
    String line = ReviewRules.summary(waiting, notSubmitted, approved);
    m.put("summaryLine", processing > 0 ? line + " · " + processing + " đang xử lý" : line);
    m.put("assignments", list);
    m.put("items", queued.stream().map(GradingSupport::view).toList());
    return m;
  }

  /** Số bài chờ theo từng bài cho màn điện thoại (US-S5E4-05-SC2): không tên học sinh. */
  public Map<String, Object> summary() {
    AuthPrincipal me = Roles.require(Role.TEACHER);
    List<Map<String, Object>> out = new ArrayList<>();
    int tw = 0;
    int tp = 0;
    for (Row r : scope(me, null, null)) {
      int w = 0;
      int p = 0;
      for (Item i : repo.items(me.orgId(), r.id())) {
        String st = GradingSupport.status(i);
        if (GradingSupport.PROCESSING.equals(st)) {
          p++;
        } else if ("waiting".equals(st)) {
          w++;
        }
      }
      tw += w;
      tp += p;
      Map<String, Object> a = new LinkedHashMap<>();
      a.put("assignmentId", r.id().toString());
      a.put("code", r.code());
      a.put("title", r.title());
      a.put("className", r.className());
      a.put("waiting", w);
      a.put("processing", p);
      out.add(a);
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("waiting", tw);
    m.put("processing", tp);
    m.put("items", out);
    return m;
  }
}
