package vn.geo.server.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.mock.env.MockEnvironment;
import vn.geo.server.GeoServerApplication;

/** Chốt khởi động: không profile, prod thiếu/còn khóa dev, prod cùng dev/test đều bị từ chối. */
class ProfileGuardTest {

  private static final String SECRET = "khoa-that-cua-moi-truong-that-0123456789";
  private static final String PEPPER = "pepper-that-cua-moi-truong-that-0123456789";
  private static final String CURSOR = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

  private static MockEnvironment prodWithAllKeys() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");
    env.setProperty("geo.auth.secret", SECRET);
    env.setProperty("geo.auth.code-pepper", PEPPER);
    env.setProperty("geo.paging.cursor-key", CURSOR);
    return env;
  }

  @Test
  void devAndTestProfilesPass() {
    MockEnvironment dev = new MockEnvironment();
    dev.setActiveProfiles("dev");
    assertThat(ProfileGuard.check(dev)).isEmpty();
    MockEnvironment test = new MockEnvironment();
    test.setActiveProfiles("test");
    assertThat(ProfileGuard.check(test)).isEmpty();
  }

  @Test
  void noProfileIsRejected() {
    assertThat(ProfileGuard.check(new MockEnvironment()))
        .singleElement()
        .asString()
        .contains("Chưa bật profile");
  }

  @Test
  void prodWithAllRealKeysPasses() {
    assertThat(ProfileGuard.check(prodWithAllKeys())).isEmpty();
  }

  @Test
  void prodWithoutPepperIsRejected() {
    MockEnvironment env = prodWithAllKeys();
    env.setProperty("geo.auth.code-pepper", "");
    assertThat(ProfileGuard.check(env)).singleElement().asString().contains("GEO_AUTH_CODE_PEPPER");
  }

  @Test
  void prodWithDevValuesIsRejected() {
    MockEnvironment env = prodWithAllKeys();
    env.setProperty("geo.auth.secret", "dev-only-secret-change-me-0123456789abcdef");
    env.setProperty("geo.paging.cursor-key", "ZGV2LW9ubHktY3Vyc29yLWtleS0zMi1ieXRlcyEhISE=");
    assertThat(ProfileGuard.check(env))
        .hasSize(2)
        .anyMatch(p -> p.contains("GEO_AUTH_SECRET"))
        .anyMatch(p -> p.contains("GEO_CURSOR_KEY"));
  }

  @Test
  void prodTogetherWithDevIsRejected() {
    MockEnvironment env = prodWithAllKeys();
    env.setActiveProfiles("prod", "dev");
    assertThat(ProfileGuard.check(env)).anyMatch(p -> p.contains("không được bật cùng dev/test"));
  }

  @Test
  void otherProfileNeedsRealKeysToo() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("staging");
    assertThat(ProfileGuard.check(env)).hasSize(3);
  }

  /** Khởi động thật (application*.yml thật, không cần CSDL): lỗi xảy ra trước khi tạo bean nào. */
  @Test
  void startingWithoutProfileFailsWithClearMessage() {
    assertThatThrownBy(() ->
            new SpringApplicationBuilder(GeoServerApplication.class)
                .web(WebApplicationType.NONE)
                .run())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Chưa bật profile");
  }

  @Test
  void startingProdWithoutPepperFails() {
    assertThatThrownBy(() ->
            new SpringApplicationBuilder(GeoServerApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("prod")
                .properties(
                    "GEO_DB_URL=jdbc:postgresql://127.0.0.1:1/none",
                    "GEO_AUTH_SECRET=" + SECRET,
                    "GEO_CURSOR_KEY=" + CURSOR)
                .run())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GEO_AUTH_CODE_PEPPER")
        .hasMessageNotContaining("GEO_AUTH_SECRET");
  }
}
