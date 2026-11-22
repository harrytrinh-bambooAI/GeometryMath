package vn.geo.server.aigateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import vn.geo.server.aigateway.api.AiRequest;
import vn.geo.server.aigateway.api.AiResponse;
import vn.geo.server.aigateway.internal.AiProperties;
import vn.geo.server.aigateway.internal.AiProvider;
import vn.geo.server.aigateway.internal.InstrumentedAiGateway;
import vn.geo.server.aigateway.internal.MockAiProvider;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;

class AiGatewayTest {

  @Test
  void mockReturnsCannedContentAndRecordsCallsAndLatency() {
    var meters = new SimpleMeterRegistry();
    var props = new AiProperties("mock", Duration.ofSeconds(2), Duration.ofMillis(20));
    var gw = new InstrumentedAiGateway(new MockAiProvider(props), props, meters);

    AiResponse r = gw.complete(new AiRequest("hint", "xin gợi ý"));
    gw.complete(new AiRequest("hint", "lần hai"));

    assertThat(r.provider()).isEqualTo("mock");
    assertThat(r.content()).contains("giả lập");
    assertThat(r.latencyMillis()).isGreaterThanOrEqualTo(20);
    assertThat(meters.get("geo.ai.calls").tag("outcome", "success").counter().count())
        .isEqualTo(2.0);
    assertThat(meters.get("geo.ai.latency").timer().count()).isEqualTo(2);
  }

  @Test
  void slowProviderTimesOutAndIsCounted() {
    var meters = new SimpleMeterRegistry();
    var props = new AiProperties("slow", Duration.ofMillis(50), Duration.ZERO);
    AiProvider slow =
        new AiProvider() {
          public String name() {
            return "slow";
          }

          public String complete(AiRequest r) throws Exception {
            Thread.sleep(2_000);
            return "x";
          }
        };
    var gw = new InstrumentedAiGateway(slow, props, meters);
    assertThatThrownBy(() -> gw.complete(new AiRequest("hint", "p")))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.TIMEOUT));
    assertThat(meters.get("geo.ai.calls").tag("outcome", "timeout").counter().count())
        .isEqualTo(1.0);
  }

  @Test
  void failingProviderMapsToDependencyUnavailable() {
    var meters = new SimpleMeterRegistry();
    var props = new AiProperties("bad", Duration.ofSeconds(1), Duration.ZERO);
    AiProvider bad =
        new AiProvider() {
          public String name() {
            return "bad";
          }

          public String complete(AiRequest r) {
            throw new IllegalStateException("hỏng");
          }
        };
    var gw = new InstrumentedAiGateway(bad, props, meters);
    assertThatThrownBy(() -> gw.complete(new AiRequest("hint", "p")))
        .isInstanceOfSatisfying(
            AppException.class,
            e -> assertThat(e.code()).isEqualTo(ErrorCode.DEPENDENCY_UNAVAILABLE));
    assertThat(meters.get("geo.ai.calls").tag("outcome", "error").counter().count()).isEqualTo(1.0);
  }
}
