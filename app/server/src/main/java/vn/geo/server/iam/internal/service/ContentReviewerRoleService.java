package vn.geo.server.iam.internal.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import vn.geo.server.iam.api.ContentReviewerRole;
import vn.geo.server.iam.internal.repo.ContentReviewerRoleRepository;

/** Cài đặt {@link ContentReviewerRole} (S2-E6). */
@Service
public class ContentReviewerRoleService implements ContentReviewerRole {

  private final ContentReviewerRoleRepository repo;

  public ContentReviewerRoleService(ContentReviewerRoleRepository repo) {
    this.repo = repo;
  }

  @Override
  public boolean isContentReviewer(UUID userId) {
    return userId != null && repo.hasReviewerRole(userId);
  }
}
