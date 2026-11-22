package vn.geo.server.common.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLTransientConnectionException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.HttpProperties;

class ErrorModelTest {

  @RestController
  static class Thrower {
    @GetMapping("/conflict")
    void conflict() {
      throw new AppException(ErrorCode.CONFLICT);
    }

    @GetMapping("/rate")
    void rate() {
      throw AppException.rateLimited(Duration.ofMillis(1500));
    }

    @GetMapping("/overloaded")
    void overloaded() {
      throw AppException.overloaded(null);
    }

    @GetMapping("/pool")
    void pool() {
      throw new CannotGetJdbcConnectionException(
          "pool", new SQLTransientConnectionException("timeout"));
    }
  }

  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new Thrower())
          .setControllerAdvice(
              new GlobalExceptionHandler(new HttpProperties(0, Duration.ofSeconds(2))))
          .build();

  @Test
  void newCodesHaveAgreedHttpStatus() {
    assertThat(ErrorCode.CONFLICT.status().value()).isEqualTo(409);
    assertThat(ErrorCode.RATE_LIMITED.status().value()).isEqualTo(429);
    assertThat(ErrorCode.SERVICE_OVERLOADED.status().value()).isEqualTo(503);
    assertThat(ErrorCode.forStatus(409)).isEqualTo(ErrorCode.CONFLICT);
  }

  @Test
  void conflictIs409WithoutRetryAfter() throws Exception {
    mvc.perform(get("/conflict"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICT"))
        .andExpect(header().doesNotExist("Retry-After"));
  }

  @Test
  void rateLimitedIs429WithRetryAfterRoundedUp() throws Exception {
    mvc.perform(get("/rate"))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
        .andExpect(header().string("Retry-After", "2"));
  }

  @Test
  void overloadedIs503WithDefaultRetryAfter() throws Exception {
    mvc.perform(get("/overloaded"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("SERVICE_OVERLOADED"))
        .andExpect(header().string("Retry-After", "2"));
  }

  @Test
  void poolTimeoutBecomesServiceOverloaded() throws Exception {
    mvc.perform(get("/pool"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value("SERVICE_OVERLOADED"))
        .andExpect(header().exists("Retry-After"));
  }

  @Test
  void retryAfterIsWholeSecondsAtLeastOne() {
    assertThat(GlobalExceptionHandler.retryAfterSeconds(Duration.ofMillis(1), null)).isEqualTo("1");
    assertThat(GlobalExceptionHandler.retryAfterSeconds(null, Duration.ofSeconds(2)))
        .isEqualTo("2");
    assertThat(GlobalExceptionHandler.retryAfterSeconds(Duration.ofMillis(2001), null))
        .isEqualTo("3");
  }
}
