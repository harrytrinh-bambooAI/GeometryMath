package vn.geo.server.content.dedup;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.content.internal.dedup.service.Dedup;
import vn.geo.server.content.internal.dedup.service.DedupMatcher;
import vn.geo.server.content.internal.dedup.service.DedupMatcher.Candidate;
import vn.geo.server.content.internal.dedup.service.DedupMatcher.Hit;

/** S2-E4 — đơn vị (hàm thuần): chuẩn hóa chữ đề và ngưỡng đối chiếu trùng (US-S2E4-03). */
class DedupTest {

  private static List<Candidate> store(String ref, String statement) {
    return List.of(new Candidate("library", UUID.randomUUID(), ref, Dedup.normalize(statement)));
  }

  @Test
  @DisplayName("TC-S2E4-022 Khác khoảng trắng và hoa thường vẫn là trùng")
  void tc022_normalizedEqual() {
    String inStore = "Tam giác ABC có AB = 5.";
    String imported = "tam giác abc có  ab = 5";
    assertThat(Dedup.normalize(imported)).isEqualTo(Dedup.normalize(inStore));
    assertThat(Dedup.key(imported)).isEqualTo(Dedup.key(inStore));
    Hit hit = DedupMatcher.match(imported, store("PLS-01", inStore));
    assertThat(hit.match()).isEqualTo(Dedup.Match.EXISTS);
    assertThat(hit.label()).isEqualTo("đã có trong kho");
    // khoảng trắng đầu cuối, tab, xuống dòng, dấu câu cuối khác nhau cũng không tạo khác biệt
    assertThat(Dedup.normalize("  Tam giác\tABC\ncó AB = 5 ;  "))
        .isEqualTo(Dedup.normalize(inStore));
    // khác số liệu thì KHÔNG trùng
    assertThat(Dedup.normalize("tam giác abc có ab = 6")).isNotEqualTo(Dedup.normalize(inStore));
  }

  /** Chuỗi 100 ký tự khác chuỗi gốc đúng {@code diffs} ký tự (độ giống = 1 − diffs / 100). */
  private static String variant(int diffs) {
    StringBuilder sb = new StringBuilder("a".repeat(100));
    for (int i = 0; i < diffs; i++) {
      sb.setCharAt(i * 7, 'b');
    }
    return sb.toString();
  }

  @Test
  @DisplayName("TC-S2E4-023 Độ giống 0,89 không báo, 0,90 báo gần giống, 1,00 là trùng")
  void tc023_thresholds() {
    assertThat(Dedup.classify(0.89)).isEqualTo(Dedup.Match.NONE);
    assertThat(Dedup.classify(0.90)).isEqualTo(Dedup.Match.SIMILAR);
    assertThat(Dedup.classify(0.9999)).isEqualTo(Dedup.Match.SIMILAR);
    assertThat(Dedup.classify(1.00)).isEqualTo(Dedup.Match.EXISTS);
    assertThat(Dedup.label(Dedup.Match.NONE, "PLS-07")).isNull();
    assertThat(Dedup.label(Dedup.Match.SIMILAR, "PLS-07")).isEqualTo("gần giống bài PLS-07, xem?");
    assertThat(Dedup.label(Dedup.Match.EXISTS, "PLS-07")).isEqualTo("đã có trong kho");

    // ba cặp đề giả lập: 11 / 10 / 0 ký tự khác trên 100 ký tự → 0,89 / 0,90 / 1,00
    String base = variant(0);
    assertThat(Dedup.similarity(base, variant(10))).isEqualTo(0.90);
    assertThat(Dedup.similarity(base, variant(11))).isLessThan(0.90);
    assertThat(Dedup.similarity(base, variant(0))).isEqualTo(1.00);

    List<Candidate> store = store("PLS-07", base);
    assertThat(DedupMatcher.match(variant(11), store).match()).isEqualTo(Dedup.Match.NONE);
    assertThat(DedupMatcher.match(variant(11), store).label()).isNull();
    Hit near = DedupMatcher.match(variant(10), store);
    assertThat(near.match()).isEqualTo(Dedup.Match.SIMILAR);
    assertThat(near.label()).isEqualTo("gần giống bài PLS-07, xem?");
    assertThat(near.refId()).isEqualTo(store.get(0).id());
    Hit same = DedupMatcher.match(base, store);
    assertThat(same.match()).isEqualTo(Dedup.Match.EXISTS);
    assertThat(same.label()).isEqualTo("đã có trong kho");
  }

  @Test
  @DisplayName("S2-E4 đối chiếu: kho rỗng không báo gì; trùng tuyệt đối thắng gần giống")
  void emptyStoreAndPriority() {
    assertThat(DedupMatcher.match("Tam giác ABC có AB = 5.", List.of())).isEqualTo(Hit.NONE);
    String base = variant(0);
    List<Candidate> store =
        List.of(
            new Candidate("mine", UUID.randomUUID(), "gần", Dedup.normalize(variant(5))),
            new Candidate("library", UUID.randomUUID(), "đúng", Dedup.normalize(base)));
    Hit hit = DedupMatcher.match(base, store);
    assertThat(hit.match()).isEqualTo(Dedup.Match.EXISTS);
    assertThat(hit.ref()).isEqualTo("đúng");
    // lệch độ dài quá 10 % thì chắc chắn dưới ngưỡng
    assertThat(Dedup.similarity("a".repeat(100), "a".repeat(80))).isZero();
  }

  /** Khoảng cách Levenshtein đầy đủ (bản tham chiếu, không tối ưu). */
  private static int fullDistance(String a, String b) {
    int[][] d = new int[a.length() + 1][b.length() + 1];
    for (int i = 0; i <= a.length(); i++) {
      d[i][0] = i;
    }
    for (int j = 0; j <= b.length(); j++) {
      d[0][j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
      }
    }
    return d[a.length()][b.length()];
  }

  @Test
  @DisplayName(
      "S2-E4 độ giống tính theo dải khớp cách tính Levenshtein đầy đủ (thêm, xóa, thay ký tự)")
  void bandedMatchesFullDistance() {
    java.util.Random rnd = new java.util.Random(260);
    for (int round = 0; round < 400; round++) {
      int len = 20 + rnd.nextInt(120);
      StringBuilder base = new StringBuilder();
      for (int i = 0; i < len; i++) {
        base.append((char) ('a' + rnd.nextInt(6)));
      }
      StringBuilder other = new StringBuilder(base);
      int edits = rnd.nextInt(len / 5 + 2);
      for (int e = 0; e < edits && other.length() > 2; e++) {
        int at = rnd.nextInt(other.length());
        switch (rnd.nextInt(3)) {
          case 0 -> other.insert(at, (char) ('a' + rnd.nextInt(6)));
          case 1 -> other.deleteCharAt(at);
          default -> other.setCharAt(at, (char) ('a' + rnd.nextInt(6)));
        }
      }
      String a = base.toString();
      String b = other.toString();
      int max = Math.max(a.length(), b.length());
      int budget = (int) Math.floor(max * 0.10 + 1e-9);
      int dist = fullDistance(a, b);
      double expected = dist > budget ? 0.0 : 1.0 - (double) dist / max;
      assertThat(Dedup.similarity(a, b)).as("%s | %s", a, b).isEqualTo(expected);
      assertThat(Dedup.similarity(b, a)).as("%s | %s", b, a).isEqualTo(expected);
    }
  }
}
