package vn.geo.server.knowledge.internal.kb.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.geo.server.common.id.Uuid7;
import vn.geo.server.knowledge.api.KbRetriever;
import vn.geo.server.knowledge.api.KbRetriever.Hit;
import vn.geo.server.knowledge.internal.kb.repo.KbRepository;
import vn.geo.server.knowledge.internal.kb.repo.KbTestHookRepository;

/**
 * Dịch vụ của móc kiểm thử S5-E3 (T-261128-56), chỉ profile dev/test (không có ở prod):
 *
 * <ul>
 *   <li>{@code GET /test-hooks/s5e3/sample-docx?kb=240} — tệp "Phiếu ôn chương VII" dựng bằng POI
 *       (tiêu đề Heading 1, mục "Hệ thức 1/AH²" có mã KT11-C07-CT-003), đệm ảnh giả tới cỡ yêu cầu;
 *       cùng tham số = cùng byte.
 *   <li>{@code POST /test-hooks/s5e3/retrieve} — gọi thẳng bộ truy xuất {@link KbRetriever}
 *       (US-S5E3-07) như lượt hỏi đáp; danh sách rỗng trả MSG-S5-20 và {@code aiCalled=false}.
 *   <li>{@code POST /test-hooks/s5e3/bulk} — kho thử N mục gắn lớp (US-S5E3-07-FC2).
 * </ul>
 */
@Service
@Profile({"dev", "test"})
public class KbTestHookService {

  public record Retrieve(
      UUID orgId,
      UUID classId,
      UUID assignmentId,
      String query,
      List<String> knowledgeCodes,
      String at) {}

  public record Bulk(UUID orgId, UUID teacherId, UUID classId, int sections) {}

  private final KbRetriever retriever;
  private final KbSearchService search;
  private final KbRepository docs;
  private final KbTestHookRepository hooks;
  private final Clock clock;

  public KbTestHookService(
      KbRetriever retriever,
      KbSearchService search,
      KbRepository docs,
      KbTestHookRepository hooks,
      Clock clock) {
    this.retriever = retriever;
    this.search = search;
    this.docs = docs;
    this.hooks = hooks;
    this.clock = clock;
  }

  public byte[] sampleDocx(int kb) {
    return docx(Math.max(1, Math.min(kb, 2048)));
  }

  public Map<String, Object> retrieve(Retrieve r) {
    Instant at = r.at() == null || r.at().isBlank() ? clock.instant() : Instant.parse(r.at());
    long t0 = System.nanoTime();
    List<Hit> hits =
        retriever.retrieve(
            r.orgId(),
            r.classId(),
            r.assignmentId(),
            r.query(),
            r.knowledgeCodes() == null ? List.of() : r.knowledgeCodes(),
            at);
    double tookMs = (System.nanoTime() - t0) / 1_000_000.0;
    Map<String, Object> m = new LinkedHashMap<>();
    m.put(
        "items",
        hits.stream()
            .map(h -> {
              Map<String, Object> x = new LinkedHashMap<>();
              x.put("docCode", h.docCode());
              x.put("version", h.version());
              x.put("ord", h.ord());
              x.put("title", h.title());
              x.put("codes", h.codes());
              x.put("score", h.score());
              x.put("boosted", h.boosted());
              return x;
            })
            .toList());
    if (hits.isEmpty()) {
      m.put("message", KbRetriever.NO_SECTION);
    }
    m.put("aiCalled", false);
    m.put("minScore", search.minScore());
    m.put("tookMs", tookMs);
    return m;
  }

  @Transactional
  public Map<String, Object> bulk(Bulk b) {
    UUID id = Uuid7.next();
    String code = docs.nextCode(b.orgId(), b.teacherId());
    hooks.bulk(
        b.orgId(), id, b.teacherId(), code, b.classId(), b.sections(), KbSplitter.hash(code + id));
    return Map.of("id", id.toString(), "code", code, "sections", b.sections());
  }

  static byte[] docx(int kb) {
    try (XWPFDocument doc = new XWPFDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      para(doc, "Title", "Phiếu ôn chương VII");
      para(doc, "Heading1", "1. Khoảng cách từ một điểm đến một mặt phẳng");
      para(
          doc,
          null,
          "Khoảng cách từ điểm M đến mặt phẳng (P) là độ dài MH, với H là hình chiếu vuông góc"
              + " của M lên (P). Mã KT11-C07-DN-010.");
      para(doc, "Heading1", "2. Hệ thức 1/AH²");
      para(
          doc,
          null,
          "Nếu SA ⊥ (ABC), AK ⊥ BC, AH ⊥ SK thì AH ⊥ (SBC) và 1/AH² = 1/SA² + 1/AK²."
              + " Mã KT11-C07-CT-003.");
      para(doc, "Heading1", "3. Bài tập ôn");
      para(
          doc,
          null,
          "Cho hình chóp S.ABCD đáy là hình vuông cạnh a, SA ⊥ (ABCD). Tính khoảng cách từ A"
              + " đến (SBD).");
      byte[] pad = new byte[kb * 1024];
      new Random(20261128L + kb).nextBytes(pad);
      doc.addPictureData(pad, Document.PICTURE_TYPE_PNG);
      doc.write(out);
      return out.toByteArray();
    } catch (IOException | org.apache.poi.openxml4j.exceptions.InvalidFormatException e) {
      throw new UncheckedIOException(new IOException(e));
    }
  }

  private static void para(XWPFDocument doc, String style, String text) {
    XWPFParagraph p = doc.createParagraph();
    if (style != null) {
      p.setStyle(style);
    }
    p.createRun().setText(text);
  }
}
