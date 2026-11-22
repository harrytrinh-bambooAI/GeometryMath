package vn.geo.server.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Cổng phát sự kiện miền trong tiến trình (đồng bộ). Khi cần độ bền (outbox, hàng đợi) thì thay
 * phần cài đặt tại đây, không đổi nơi gọi.
 */
@Component
public class DomainEvents {

  private final ApplicationEventPublisher publisher;

  public DomainEvents(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publish(DomainEvent event) {
    publisher.publishEvent(event);
  }
}
