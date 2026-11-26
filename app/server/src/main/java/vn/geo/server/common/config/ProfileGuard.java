package vn.geo.server.common.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Chốt khởi động (fail-fast) chống triển khai nhầm cấu hình dev (app/docs/CONFIG.md "Profile"):
 *
 * <ul>
 *   <li>không có profile nào được bật thì từ chối (không còn mặc định {@code dev});
 *   <li>{@code prod} bật cùng {@code dev}/{@code test} thì từ chối;
 *   <li>ngoài dev/test (prod hoặc profile khác): khóa thẻ, pepper mã giáo viên, khóa cursor phải
 *       được đặt qua biến môi trường, không rỗng và không phải giá trị dev;
 *   <li>ngoài dev/test: nhà cung cấp AI giả lập ({@code geo.ai.provider=mock}) bị cấm (PO-60, MVP
 *       M0) — môi trường thật phải nối AI thật;
 *   <li>mọi profile: {@code geo.ai.provider} phải là nhà cung cấp đã biết; {@code anthropic} cần
 *       khóa {@code GEO_AI_API_KEY} (không bao giờ in giá trị khóa ra thông báo).
 * </ul>
 *
 * Chạy ngay sau khi nạp tệp cấu hình, trước khi tạo bean nào (kể cả kết nối CSDL), nên thông báo
 * lỗi là lỗi đầu tiên trong nhật ký.
 */
public class ProfileGuard implements EnvironmentPostProcessor {

  static final Set<String> DEV_PROFILES = Set.of("dev", "test");

  /** Khóa bí mật bắt buộc ngoài dev/test: thuộc tính -> biến môi trường. */
  static final Map<String, String> SECRETS =
      Map.of(
          "geo.auth.secret", "GEO_AUTH_SECRET",
          "geo.auth.code-pepper", "GEO_AUTH_CODE_PEPPER",
          "geo.paging.cursor-key", "GEO_CURSOR_KEY");

  /** Giá trị dev/thử có trong application*.yml; không bao giờ được dùng ở môi trường thật. */
  static final Set<String> DEV_VALUES =
      Set.of(
          "dev-only-secret-change-me-0123456789abcdef",
          "dev-only-code-pepper-0123456789abcdef",
          "ZGV2LW9ubHktY3Vyc29yLWtleS0zMi1ieXRlcyEhISE=");

  /** Nhà cung cấp AI máy chủ có cài (AiProvider): giá trị hợp lệ của geo.ai.provider. */
  static final Set<String> AI_PROVIDERS = Set.of("mock", "anthropic");

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
    List<String> problems = check(env);
    if (!problems.isEmpty()) {
      throw new IllegalStateException(
          "Cấu hình khởi động không an toàn, máy chủ từ chối chạy (xem app/docs/CONFIG.md):\n - "
              + String.join("\n - ", problems));
    }
  }

  /** Danh sách vi phạm (rỗng = hợp lệ). Tách riêng để kiểm thử đơn vị. */
  public static List<String> check(Environment env) {
    List<String> problems = new ArrayList<>();
    List<String> active = Arrays.asList(env.getActiveProfiles());
    if (active.isEmpty()) {
      problems.add(
          "Chưa bật profile nào. Đặt SPRING_PROFILES_ACTIVE=dev (máy dev, E2E) hoặc prod (môi trường"
              + " thật); máy chủ không còn profile mặc định.");
      return problems;
    }
    boolean devLike = active.stream().anyMatch(DEV_PROFILES::contains);
    if (active.contains("prod") && devLike) {
      problems.add("Profile prod không được bật cùng dev/test (đang bật: " + active + ").");
    }
    if (!devLike || active.contains("prod")) {
      SECRETS.forEach((prop, envVar) -> {
        String v;
        try {
          v = env.getProperty(prop);
        } catch (IllegalArgumentException unresolved) {
          v = null; // ${BIEN} không có mặc định và chưa đặt
        }
        if (v == null || v.isBlank()) {
          problems.add(envVar + " (" + prop + ") chưa đặt hoặc rỗng.");
        } else if (DEV_VALUES.contains(v) || v.startsWith("dev-only")) {
          problems.add(envVar + " (" + prop + ") đang là giá trị dev; đặt khóa thật.");
        }
      });
    }
    checkAi(env, !devLike || active.contains("prod"), problems);
    return problems;
  }

  private static void checkAi(Environment env, boolean prodLike, List<String> problems) {
    String provider = safe(env, "geo.ai.provider");
    provider = provider == null || provider.isBlank() ? "mock" : provider.strip();
    if (!AI_PROVIDERS.contains(provider)) {
      problems.add(
          "geo.ai.provider="
              + provider
              + " không có; chọn một trong "
              + AI_PROVIDERS.stream().sorted().toList()
              + " (GEO_AI_PROVIDER).");
      return;
    }
    if (prodLike && "mock".equals(provider)) {
      problems.add(
          "geo.ai.provider=mock (AI GIẢ LẬP) không được dùng ở prod; đặt GEO_AI_PROVIDER=anthropic"
              + " và GEO_AI_API_KEY.");
    }
    if ("anthropic".equals(provider)) {
      String key = safe(env, "geo.ai.api-key");
      if (key == null || key.isBlank()) {
        problems.add(
            "GEO_AI_API_KEY (geo.ai.api-key) chưa đặt hoặc rỗng; bắt buộc khi geo.ai.provider=anthropic.");
      }
    }
  }

  private static String safe(Environment env, String prop) {
    try {
      return env.getProperty(prop);
    } catch (IllegalArgumentException unresolved) {
      return null;
    }
  }
}
