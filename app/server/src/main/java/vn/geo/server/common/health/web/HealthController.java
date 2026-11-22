package vn.geo.server.common.health.web;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.health.HealthService;

@RestController
public class HealthController {

  private final HealthService service;

  public HealthController(HealthService service) {
    this.service = service;
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> body = service.check();
    return "UP".equals(body.get("status"))
        ? ResponseEntity.ok(body)
        : ResponseEntity.status(503).body(body);
  }
}
