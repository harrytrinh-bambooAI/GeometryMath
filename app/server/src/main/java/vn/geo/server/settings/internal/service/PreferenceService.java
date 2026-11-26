package vn.geo.server.settings.internal.service;

import java.util.Set;
import org.springframework.stereotype.Service;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.security.AuthPrincipal;
import vn.geo.server.common.security.RequestContext;
import vn.geo.server.settings.internal.repo.PreferenceRepository;

/**
 * Tùy chọn giao diện theo tài khoản (T-261126-50): theme 'a' | 'b' | 'c' | 'd', mặc định 'd' "Bảng
 * điều khiển gọn" (DECISIONS 26/11). Mọi vai đã đăng nhập đọc/ghi được tùy chọn của chính mình.
 */
@Service
public class PreferenceService {

  public static final String DEFAULT_THEME = "c";
  static final Set<String> THEMES = Set.of("a", "b", "c", "d");

  public record Settings(String theme) {}

  private final PreferenceRepository repo;

  public PreferenceService(PreferenceRepository repo) {
    this.repo = repo;
  }

  public Settings get() {
    AuthPrincipal p = RequestContext.principal();
    return new Settings(repo.findTheme(p.userId()).orElse(DEFAULT_THEME));
  }

  public Settings save(String theme) {
    if (!isTheme(theme)) {
      throw new AppException(ErrorCode.VALIDATION_FAILED);
    }
    AuthPrincipal p = RequestContext.principal();
    if (!repo.saveTheme(p.userId(), theme)) {
      throw new AppException(ErrorCode.NOT_FOUND);
    }
    return new Settings(theme);
  }

  public static boolean isTheme(String theme) {
    return theme != null && THEMES.contains(theme);
  }
}
