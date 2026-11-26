package vn.geo.server.settings.internal.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.settings.internal.service.PreferenceService;

/** Cài đặt của tài khoản đang đăng nhập (T-261126-50): theme giao diện. */
@RestController
public class SettingsController {

  public record SettingsBody(String theme) {}

  private final PreferenceService service;

  public SettingsController(PreferenceService service) {
    this.service = service;
  }

  @GetMapping("/api/v1/me/settings")
  public PreferenceService.Settings get() {
    return service.get();
  }

  @PutMapping("/api/v1/me/settings")
  public PreferenceService.Settings put(@RequestBody SettingsBody body) {
    return service.save(body == null ? null : body.theme());
  }
}
