package vn.geo.server.content.internal.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.content.internal.repo.SignatureUpgradeRepository;
import vn.geo.server.content.internal.repo.SignatureUpgradeRepository.Stale;
import vn.geo.server.geometry.api.GeometryEngine;

/**
 * Câu mở sau FIX T-05 (inbox 27/11/2026): chữ ký chuẩn đổi bản 1 → 2 ({@link
 * GeometryEngine#SIG_VERSION}). Hàng {@code problem_signature} bản cũ của đề tự nhập không còn khớp
 * đề kho bản mới, nên tính lại từ mô tả dựng đã lưu (scene_build.spec) và ghi hàng bản mới. Chạy
 * khi máy chủ sẵn sàng ({@code geo.signature.upgrade-on-start}, mặc định bật) trước khi có lượt tự
 * học mới đáng kể; lũy đẳng (lượt sau không còn gì để làm). Đề không dựng lại được (spec hỏng, bộ
 * dựng không còn) giữ hàng cũ và được đếm vào {@code failed}.
 */
@Service
public class SignatureUpgrade {

  public record Report(int upgraded, int skipped, int failed) {}

  static final int PAGE = 500;

  private static final Logger log = LoggerFactory.getLogger(SignatureUpgrade.class);

  private final SignatureUpgradeRepository repo;
  private final GeometryEngine engine;
  private final JsonMapper mapper;
  private final boolean onStart;

  public SignatureUpgrade(
      SignatureUpgradeRepository repo,
      GeometryEngine engine,
      JsonMapper mapper,
      @Value("${geo.signature.upgrade-on-start:true}") boolean onStart) {
    this.repo = repo;
    this.engine = engine;
    this.mapper = mapper;
    this.onStart = onStart;
  }

  /** Không chặn khởi động: chạy nền sau ApplicationReadyEvent. */
  @EventListener(ApplicationReadyEvent.class)
  public void onReady() {
    if (!onStart) {
      return;
    }
    Thread t = new Thread(this::runInBackground, "signature-upgrade");
    t.setDaemon(true);
    t.start();
  }

  void runInBackground() {
    try {
      Report r = repo.exclusive(this::run).orElse(null);
      if (r == null) {
        log.info("tính lại chữ ký chuẩn: nút khác đang chạy, bỏ qua");
      } else if (r.upgraded() + r.failed() > 0) {
        log.info(
            "tính lại chữ ký chuẩn bản {}: {} version, bỏ qua {}, không dựng lại được {}",
            GeometryEngine.SIG_VERSION,
            r.upgraded(),
            r.skipped(),
            r.failed());
      }
    } catch (RuntimeException e) {
      log.warn("tính lại chữ ký chuẩn thất bại, thử lại ở lần khởi động sau", e);
    }
  }

  /** Một lượt trọn: duyệt theo khóa tới hết các version còn thiếu chữ ký bản mới. */
  public Report run() {
    int upgraded = 0;
    int skipped = 0;
    int failed = 0;
    Stale after = null;
    List<Stale> page;
    do {
      page = repo.stale(GeometryEngine.SIG_VERSION, after, PAGE);
      for (Stale s : page) {
        Optional<GeometryEngine.Signature> sig = signatureOf(s.spec());
        if (sig.isEmpty()) {
          failed++;
          continue;
        }
        GeometryEngine.Signature g = sig.get();
        String r =
            repo.put(
                s.orgId(),
                s.problemVersionId(),
                GeometryEngine.SIG_VERSION,
                g.solidKind(),
                mapper.writeValueAsString(g.paramsNorm()),
                g.asked(),
                g.sha256());
        if ("OK".equals(r)) {
          upgraded++;
        } else {
          skipped++;
        }
      }
      after = page.isEmpty() ? after : page.getLast();
    } while (page.size() == PAGE);
    return new Report(upgraded, skipped, failed);
  }

  private Optional<GeometryEngine.Signature> signatureOf(String spec) {
    if (spec == null || spec.isBlank()) {
      return Optional.empty();
    }
    try {
      return engine.signature(engine.specFrom(mapper.readTree(spec)));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }
}
