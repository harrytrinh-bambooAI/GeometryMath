package vn.geo.server.exam.internal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Lệnh nạp dữ liệu đề (US-S5E2-01, đích {@code make exam-import}): bật bằng {@code
 * --geo.s5.exam.import=true}, in báo cáo JSON, rồi tắt ứng dụng (mã thoát 0 khi không lỗi, 1 khi có
 * bản ghi bị loại; phần đạt vẫn được nạp).
 */
@Component
@ConditionalOnProperty(name = "geo.s5.exam.import", havingValue = "true")
public class ExamImportRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(ExamImportRunner.class);

  private final ExamImportService importer;
  private final Environment env;
  private final ConfigurableApplicationContext ctx;
  private final JsonMapper mapper;

  public ExamImportRunner(
      ExamImportService importer,
      Environment env,
      ConfigurableApplicationContext ctx,
      JsonMapper mapper) {
    this.importer = importer;
    this.env = env;
    this.ctx = ctx;
    this.mapper = mapper;
  }

  @Override
  public void run(ApplicationArguments args) {
    String version = env.getProperty("geo.s5.exam.import-version", importer.defaultVersion());
    ExamImportService.Report r = importer.run(importer.loadDataset(), version);
    log.info("S5-E2 nạp dữ liệu đề {}: {}", version, r.summary());
    System.out.println(mapper.writeValueAsString(r));
    int code = r.errors() == 0 ? 0 : 1;
    System.exit(SpringApplication.exit(ctx, () -> code));
  }
}
