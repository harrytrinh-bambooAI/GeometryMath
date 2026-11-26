package vn.geo.server.content.internal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Nạp gói nội dung Kho đề MVP khi máy chủ khởi động ({@code geo.content.pack.load-on-start}; tắt ở
 * profile test và ở {@code make e2e} để Kho đề e2e giữ sáu đề, TC-GEO-0125). Lỗi nạp (ví dụ CSDL
 * chưa có dữ liệu nền tảng) chỉ ghi nhật ký, không chặn khởi động.
 */
@Component
@ConditionalOnProperty(name = "geo.content.pack.load-on-start", havingValue = "true")
class ContentPackRunner implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(ContentPackRunner.class);

  private final ContentPackLoader loader;

  ContentPackRunner(ContentPackLoader loader) {
    this.loader = loader;
  }

  @Override
  public void run(ApplicationArguments args) {
    try {
      ContentPackLoader.Result r = loader.load();
      LOG.info(
          "gói nội dung Kho đề: {} đề, {} ghi mới, {} bỏ qua (đã có), {} đã kiểm chứng",
          r.codes().size(),
          r.written(),
          r.skipped(),
          r.verified());
    } catch (RuntimeException e) {
      LOG.warn("không nạp được gói nội dung Kho đề: {}", e.toString());
    }
  }
}
