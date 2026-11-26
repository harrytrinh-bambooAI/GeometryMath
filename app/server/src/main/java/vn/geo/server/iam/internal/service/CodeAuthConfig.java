package vn.geo.server.iam.internal.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import vn.geo.server.common.security.TokenService;
import vn.geo.server.iam.internal.code.PasswordHasher;
import vn.geo.server.iam.internal.code.RegistrationLimiter;
import vn.geo.server.iam.internal.repo.CodeAccountRepository;

/**
 * Cấu hình S2-E1: {@code geo.s2e1.pbkdf2-iterations} (số vòng băm mật khẩu), {@code
 * geo.s2e1.community-org-id} (trường "Cộng đồng" chứa tài khoản tự đăng ký). Pepper dùng chung
 * {@code geo.auth.code-pepper} (prod đặt bằng biến môi trường).
 */
@Configuration
public class CodeAuthConfig {

  @Bean
  PasswordHasher passwordHasher(
      @Value("${geo.s2e1.pbkdf2-iterations:60000}") int iterations,
      @Value("${geo.auth.code-pepper}") String pepper) {
    return new PasswordHasher(iterations, pepper.getBytes(StandardCharsets.UTF_8));
  }

  @Bean
  RegistrationLimiter registrationLimiter(Clock clock) {
    return new RegistrationLimiter(clock);
  }

  @Bean
  CodeAuthService codeAuthService(
      CodeAccountRepository repo,
      TokenService tokens,
      PasswordHasher hasher,
      RegistrationLimiter limiter,
      SessionRevocations revocations,
      TransactionTemplate tx,
      Clock clock,
      @Value("${geo.s2e1.community-org-id:00000000-0000-7000-8002-000000000100}") UUID community) {
    return new CodeAuthService(repo, tokens, hasher, limiter, revocations, tx, clock, community);
  }
}
