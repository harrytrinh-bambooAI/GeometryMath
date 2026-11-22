package vn.geo.server.aigateway.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.geo.server.aigateway.api.AiRequest;

/** Nhà cung cấp GIẢ LẬP: trả nội dung soạn sẵn, chưa gọi AI thật (chưa có khóa AI; giả định PO). */
@Component
@ConditionalOnProperty(name = "geo.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {

  private final AiProperties props;

  public MockAiProvider(AiProperties props) {
    this.props = props;
  }

  @Override
  public String name() {
    return "mock";
  }

  @Override
  public String complete(AiRequest request) throws InterruptedException {
    if (!props.mockLatency().isZero()) {
      Thread.sleep(props.mockLatency());
    }
    return "[Nội dung giả lập] Đây là phản hồi soạn sẵn cho tác vụ \"" + request.task() + "\".";
  }
}
