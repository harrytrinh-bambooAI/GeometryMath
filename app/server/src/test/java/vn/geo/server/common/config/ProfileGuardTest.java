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
  private static final String AI_KEY = "khoa-ai-gia-cua-kiem-thu-khong-that";

  private static MockEnvironment prodWithAllKeys() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");
    env.setProperty("geo.auth.secret", SECRET);
    env.setProperty("geo.auth.code-pepper", PEPPER);
    env.setProperty("geo.paging.cursor-key", CURSOR);
    env.setProperty("geo.ai.provider", "anthropic");
    env.setProperty("geo.ai.api-key", AI_KEY);
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
    // ba khóa + AI giả lập (mặc định mock) đều bị từ chối ngoài dev/test
    assertThat(ProfileGuard.check(env))
        .hasSize(4)
        .anyMatch(p -> p.contains("geo.ai.provider=mock"));
  }

  /** PO-60 / MVP M0: prod với AI giả lập bị từ chối, thông báo rõ. */
  @Test
  void prodWithMockAiIsRejected() {
    MockEnvironment env = prodWithAllKeys();
    env.setProperty("geo.ai.provider", "mock");
    assertThat(ProfileGuard.check(env))
        .singleElement()
        .asString()
        .contains("geo.ai.provider=mock")
        .contains("không được dùng ở prod");
  }

  @Test
  void devMayUseMock() {
    MockEnvironment dev = new MockEnvironment();
    dev.setActiveProfiles("dev");
    dev.setProperty("geo.ai.provider", "mock");
    assertThat(ProfileGuard.check(dev)).isEmpty();
  }

  @Test
  void anthropicWithoutKeyIsRejectedInAnyProfile() {
    MockEnvironment prod = prodWithAllKeys();
    prod.setProperty("geo.ai.api-key", "");
    assertThat(ProfileGuard.check(prod)).singleElement().asString().contains("GEO_AI_API_KEY");
    MockEnvironment dev = new MockEnvironment();
    dev.setActiveProfiles("dev");
    dev.setProperty("geo.ai.provider", "anthropic");
    assertThat(ProfileGuard.check(dev)).singleElement().asString().contains("GEO_AI_API_KEY");
  }

  @Test
  void unknownProviderIsRejected() {
    MockEnvironment dev = new MockEnvironment();
    dev.setActiveProfiles("dev");
    dev.setProperty("geo.ai.provider", "openai");
    assertThat(ProfileGuard.check(dev))
        .singleElement()
        .asString()
        .contains("geo.ai.provider=openai");
  }

  /** Thông báo lỗi không bao giờ in giá trị khóa AI. */
  @Test
  void messagesNeverContainTheAiKey() {
    MockEnvironment env = prodWithAllKeys();
    env.setProperty("geo.auth.secret", "");
    assertThat(String.join("\n", ProfileGuard.check(env))).doesNotContain(AI_KEY);
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

  /**
   * Khởi động thật profile prod (yml thật) với GEO_AI_PROVIDER=mock: từ chối trước khi tạo bean.
   */
  @Test
  void startingProdWithMockAiFails() {
    assertThatThrownBy(() ->
            new SpringApplicationBuilder(GeoServerApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("prod")
                .properties(
                    "GEO_DB_URL=jdbc:postgresql://127.0.0.1:1/none",
                    "GEO_AUTH_SECRET=" + SECRET,
                    "GEO_AUTH_CODE_PEPPER=" + PEPPER,
                    "GEO_CURSOR_KEY=" + CURSOR,
                    "GEO_AI_PROVIDER=mock")
                .run())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("geo.ai.provider=mock")
        .hasMessageContaining("không được dùng ở prod");
  }

  /** prod mặc định nối AI thật: thiếu GEO_AI_API_KEY thì từ chối. */
  @Test
  void startingProdWithoutAiKeyFails() {
    assertThatThrownBy(() ->
            new SpringApplicationBuilder(GeoServerApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("prod")
                .properties(
                    "GEO_DB_URL=jdbc:postgresql://127.0.0.1:1/none",
                    "GEO_AUTH_SECRET=" + SECRET,
                    "GEO_AUTH_CODE_PEPPER=" + PEPPER,
                    "GEO_CURSOR_KEY=" + CURSOR)
                .run())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GEO_AI_API_KEY")
        .hasMessageNotContaining("geo.ai.provider=mock");
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
