package vn.geo.server.iam.internal.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.iam.internal.repo.ClassTestHookRepository;

/** Dịch vụ của móc kiểm thử S3-E1 / S3-E5 (chỉ dev/test). */
@Service
@Profile({"dev", "test"})
public class ClassTestHookService {

  private final ClassTestHookRepository repo;
  private final PrivacyService privacy;
  private final Clock clock;

  public ClassTestHookService(ClassTestHookRepository repo, PrivacyService privacy, Clock clock) {
    this.repo = repo;
    this.privacy = privacy;
    this.clock = clock;
  }

  public int ageJoinAttempts(UUID userId, long seconds) {
    return repo.ageJoinAttempts(userId, seconds);
  }

  public int seedJoinAttempts(UUID userId, List<Long> minutesAgo) {
    UUID org = repo.orgOf(userId);
    Instant now = clock.instant();
    int n = 0;
    for (Long m : minutesAgo == null ? List.<Long>of() : minutesAgo) {
      repo.seedJoinAttempt(org, Uuid7.next(), userId, now.minusSeconds(m * 60));
      n++;
    }
    return n;
  }

  public int ageTempPassword(UUID userId, long seconds) {
    return repo.ageTempPassword(userId, seconds);
  }

  public int ageResets(UUID teacherId, long seconds) {
    return repo.ageResets(teacherId, seconds);
  }

  public int deactivate(UUID userId) {
    return repo.deactivate(userId, clock.instant());
  }

  public long denials(UUID actorId) {
    return repo.denials(actorId);
  }

  public void overridePrivacy(UUID userId, String version) {
    privacy.overrideForTest(repo.orgOf(userId), userId, version);
  }

  public int clearPrivacy(UUID userId) {
    return privacy.clearAckForTest(repo.orgOf(userId), userId);
  }

  public Optional<String> privacyAck(UUID userId) {
    return privacy.ackFor(repo.orgOf(userId), userId);
  }
}
