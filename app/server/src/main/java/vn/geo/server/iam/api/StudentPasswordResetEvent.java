package vn.geo.server.iam.api;

import java.time.Instant;
import java.util.UUID;
import vn.geo.server.common.event.DomainEvent;

/**
 * Giáo viên vừa đặt lại mật khẩu cho học sinh lớp mình (US-S3E1-04, BR-S3E1-37). Không chứa mật
 * khẩu hay băm. Module assignment ghi AuditEvent PASSWORD_RESET từ sự kiện này (US-S3E5-01, task
 * 31). Phát TRONG giao dịch đổi mật khẩu (Dev-S3E5-08): người nghe phải đồng bộ, lỗi thì quay lui.
 */
public record StudentPasswordResetEvent(
    UUID eventId, UUID orgId, Instant occurredAt, UUID actorId, UUID classId, UUID studentId)
    implements DomainEvent {}
