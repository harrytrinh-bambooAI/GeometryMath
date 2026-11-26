package vn.geo.server.subject.api;

/** Môn không đăng ký được (BR-S2E2-01, 02): thiếu thao tác, concepts rỗng, trùng mã. */
public class SubjectRegistrationException extends RuntimeException {

  private final String subjectId;

  public SubjectRegistrationException(String subjectId, String message) {
    super("Môn \"" + subjectId + "\" không đăng ký được: " + message);
    this.subjectId = subjectId;
  }

  public String subjectId() {
    return subjectId;
  }
}
