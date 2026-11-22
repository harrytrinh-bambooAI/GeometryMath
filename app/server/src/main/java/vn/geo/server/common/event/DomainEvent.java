package vn.geo.server.common.event;

import java.time.Instant;
import java.util.UUID;

/** Hợp đồng sự kiện miền: module phát sự kiện qua {@link DomainEvents}, module khác lắng nghe. */
public interface DomainEvent {
  UUID eventId();

  UUID orgId();

  Instant occurredAt();
}
