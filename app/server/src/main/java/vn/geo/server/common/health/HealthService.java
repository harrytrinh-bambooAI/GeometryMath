package vn.geo.server.common.health;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.geo.server.common.health.repo.HealthRepository;

@Service
public class HealthService {

  private final HealthRepository repo;
  private final StringRedisTemplate redis;

  public HealthService(HealthRepository repo, StringRedisTemplate redis) {
    this.repo = repo;
    this.redis = redis;
  }

  /** Trạng thái từng phụ thuộc; "UP" hoặc "DOWN". */
  public Map<String, String> check() {
    Map<String, String> r = new LinkedHashMap<>();
    r.put("db", attempt(repo::probe));
    r.put(
        "redis",
        attempt(() ->
            "PONG"
                .equalsIgnoreCase(
                    redis.execute(
                        (org.springframework.data.redis.core.RedisCallback<String>)
                            c -> c.ping()))));
    r.put("status", r.containsValue("DOWN") ? "DOWN" : "UP");
    return r;
  }

  private static String attempt(java.util.function.BooleanSupplier probe) {
    try {
      return probe.getAsBoolean() ? "UP" : "DOWN";
    } catch (RuntimeException e) {
      return "DOWN";
    }
  }
}
