package vn.geo.server.assignment.internal.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.assignment.internal.repo.S3E4HookRepository;

/** MÓC KIỂM THỬ S3-E4 (chỉ dev/test): đọc bản ghi Từ chối trên audit_log của một người gọi. */
@Service
@Profile({"dev", "test"})
public class S3E4HookService {

  private final S3E4HookRepository repo;

  public S3E4HookService(S3E4HookRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> denied(UUID actorId) {
    return repo.denied(actorId);
  }
}
