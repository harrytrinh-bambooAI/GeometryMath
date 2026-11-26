package vn.geo.server.iam.internal.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.web.NoStore;
import vn.geo.server.iam.internal.service.PrivacyService;

/** US-S3E5-02: thông báo riêng tư của chính mình và nút "Đã đọc" (phiên bản do máy chủ quyết). */
@RestController
public class PrivacyController {

  public record AckBody(String version) {}

  private final PrivacyService privacy;

  public PrivacyController(PrivacyService privacy) {
    this.privacy = privacy;
  }

  @NoStore
  @GetMapping("/api/v1/account/privacy")
  public PrivacyService.NoticeView mine() {
    return privacy.mine();
  }

  @NoStore
  @PostMapping("/api/v1/account/privacy-ack")
  public PrivacyService.Acked ack(@RequestBody(required = false) AckBody b) {
    return privacy.ack(b == null ? null : b.version());
  }
}
