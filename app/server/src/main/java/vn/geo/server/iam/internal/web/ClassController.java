package vn.geo.server.iam.internal.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.iam.internal.service.ClassAiPolicyService;
import vn.geo.server.iam.internal.service.ClassService;

/**
 * S3-E1: lớp, mã lớp, vào lớp chờ duyệt, duyệt / từ chối / cho rời lớp, đặt lại mật khẩu học sinh;
 * US-S3E5-04: công tắc trợ lý AI theo lớp. Quyền theo quan hệ kiểm ở {@link ClassService} (ngoài
 * quan hệ NOT_FOUND, sai vai FORBIDDEN).
 */
@RestController
public class ClassController {

  public record CreateBody(String name, Object grade, String academicYear) {}

  public record JoinBody(String code) {}

  private final ClassService classes;
  private final ClassAiPolicyService ai;

  public ClassController(ClassService classes, ClassAiPolicyService ai) {
    this.classes = classes;
    this.ai = ai;
  }

  @NoStore
  @GetMapping("/api/v1/teacher/my-classes")
  public Map<String, List<ClassService.ClassLine>> list() {
    return Map.of("items", classes.myClasses());
  }

  @NoStore
  @PostMapping("/api/v1/teacher/my-classes")
  @ResponseStatus(HttpStatus.CREATED)
  public ClassService.ClassView create(@RequestBody CreateBody b) {
    return classes.create(b.name(), b.grade(), b.academicYear());
  }

  @NoStore
  @GetMapping("/api/v1/teacher/my-classes/{id}")
  public ClassService.ClassDetail detail(@PathVariable UUID id) {
    return classes.detail(id);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/my-classes/{id}/code/reset")
  public ClassService.ClassDetail resetCode(@PathVariable UUID id) {
    return classes.resetCode(id);
  }

  @NoStore
  @PutMapping("/api/v1/teacher/my-classes/{id}/settings")
  public ClassService.ClassDetail settings(
      @PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
    return classes.settings(id, body);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/my-classes/{id}/archive")
  public ClassService.ClassDetail archive(@PathVariable UUID id) {
    return classes.archive(id);
  }

  /** US-S3E6-02 SC7: bỏ lưu trữ, lớp về Đang dạy với mã lớp mới. */
  @NoStore
  @PostMapping("/api/v1/teacher/my-classes/{id}/unarchive")
  public ClassService.ClassDetail unarchive(@PathVariable UUID id) {
    return classes.unarchive(id);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/my-classes/{id}/pending")
  public Map<String, List<ClassService.Pending>> pending(@PathVariable UUID id) {
    return Map.of("items", classes.pending(id));
  }

  @NoStore
  @GetMapping("/api/v1/teacher/my-classes/{id}/students")
  public ClassService.StudentPage students(
      @PathVariable UUID id,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return classes.students(id, limit, cursor);
  }

  @NoStore
  @GetMapping("/api/v1/teacher/my-classes/{id}/students/{userId}")
  public ClassService.Student student(@PathVariable UUID id, @PathVariable UUID userId) {
    return classes.student(id, userId);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/my-classes/{id}/members/{userId}/{decision}")
  public ClassService.Decision decide(
      @PathVariable UUID id, @PathVariable UUID userId, @PathVariable String decision) {
    return classes.decide(id, userId, decision);
  }

  @NoStore
  @PostMapping("/api/v1/teacher/my-classes/{id}/students/{userId}/password-reset")
  public ClassService.TempPassword resetPassword(@PathVariable UUID id, @PathVariable UUID userId) {
    return classes.resetPassword(id, userId);
  }

  @NoStore
  @PostMapping("/api/v1/student/class-joins")
  public ClassService.Joined join(@RequestBody JoinBody b) {
    return classes.join(b.code());
  }

  @NoStore
  @GetMapping("/api/v1/student/classes")
  public Map<String, List<ClassService.MyClass>> myClasses() {
    return Map.of("items", classes.studentClasses());
  }

  @NoStore
  @GetMapping("/api/v1/student/class-ai/{id}")
  public ClassService.AiStatus classAi(@PathVariable UUID id) {
    return ai.statusFor(id);
  }
}
