package vn.geo.server.aigateway.internal;

import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Giá trị đang hiệu lực của cổng AI: khởi từ cấu hình ({@link AiProperties}); profile dev/test đổi
 * được lúc chạy qua móc kiểm thử (mô phỏng "đổi cấu hình rồi chạy lại", US-GEO-103-SC2, 105-SC3).
 */
@Component
public class AiSettings {

  private final AiProperties props;
  private volatile String model;
  private volatile String promptVersion;
  private volatile Duration timeout;

  public AiSettings(AiProperties props) {
    this.props = props;
    reset();
  }

  public final void reset() {
    model = props.model();
    promptVersion = props.promptVersion();
    timeout = props.timeout();
  }

  public String model() {
    return model;
  }

  public String promptVersion() {
    return promptVersion;
  }

  public Duration timeout() {
    return timeout;
  }

  public long costMicro() {
    return props.costPerCall().movePointRight(6).longValue();
  }

  public void model(String model) {
    this.model = model;
  }

  public void timeout(Duration timeout) {
    this.timeout = timeout;
  }
}
