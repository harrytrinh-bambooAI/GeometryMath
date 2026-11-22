package vn.geo.server.common.redis;

import java.util.StringJoiner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Mọi khóa Redis đều qua đây để có tiền tố riêng cho từng môi trường (test dùng tiền tố khác). */
@Component
public class RedisKeys {

  private final String prefix;

  public RedisKeys(Props props) {
    this.prefix = props.keyPrefix();
  }

  public String key(String... parts) {
    StringJoiner j = new StringJoiner(":", prefix, "");
    for (String p : parts) {
      j.add(p);
    }
    return j.toString();
  }

  public String prefix() {
    return prefix;
  }

  /**
   * @param keyPrefix tiền tố, ví dụ "geo:"
   */
  @ConfigurationProperties("geo.redis")
  public record Props(String keyPrefix) {
    public Props {
      if (keyPrefix == null || keyPrefix.isBlank()) {
        keyPrefix = "geo:";
      }
    }
  }
}
