package vn.geo.server.iam.internal.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Chạy lệnh chuyển MVP khi bật {@code --geo.s2e1.migrate=true} (đích {@code make s2e1-migrate}): in
 * báo cáo, ghi CSV ra {@code geo.s2e1.migrate-report}, rồi tắt ứng dụng với mã thoát của báo cáo.
 */
@Component
@ConditionalOnProperty(name = "geo.s2e1.migrate", havingValue = "true")
public class MvpMigrationRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(MvpMigrationRunner.class);

  private final MvpMigration migration;
  private final Environment env;
  private final ConfigurableApplicationContext ctx;

  public MvpMigrationRunner(
      MvpMigration migration, Environment env, ConfigurableApplicationContext ctx) {
    this.migration = migration;
    this.env = env;
    this.ctx = ctx;
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    String org = env.getProperty("geo.s2e1.migrate-org", "");
    MvpMigration.Report r =
        migration.run(
            Arrays.asList(env.getActiveProfiles()), org.isBlank() ? null : UUID.fromString(org));
    String csv = r.toCsv();
    Path out = Path.of(env.getProperty("geo.s2e1.migrate-report", ".data/s2e1-migration.csv"));
    if (!r.refused()) {
      Files.createDirectories(out.toAbsolutePath().getParent());
      Files.writeString(out, csv, StandardCharsets.UTF_8);
    }
    log.info(
        "S2-E1 chuyển MVP: {} · chuyển {} · đã chuyển trước đó {} · lỗi {} · báo cáo {}",
        r.refused() ? r.message() : "xong",
        r.count(MvpMigration.Status.CONVERTED),
        r.count(MvpMigration.Status.ALREADY),
        r.count(MvpMigration.Status.ERROR),
        r.refused() ? "-" : out.toAbsolutePath());
    int code = r.exitCode();
    System.exit(SpringApplication.exit(ctx, () -> code));
  }
}
