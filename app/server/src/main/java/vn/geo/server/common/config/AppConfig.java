package vn.geo.server.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  /** Đồng hồ duy nhất của ứng dụng (UTC); tiêm vào để kiểm thử được. */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
