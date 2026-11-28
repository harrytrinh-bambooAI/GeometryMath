package vn.geo.server.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.tracking.internal.domain.MonitorView;
import vn.geo.server.tracking.internal.domain.MonitorView.Asked;
import vn.geo.server.tracking.internal.domain.MonitorView.Group;

/** E7: hàm thuần của Theo dõi lớp (gộp câu hỏi, tình trạng, thẻ "thao tác hình"). */
class MonitorViewE7Test {

  private static final UUID AN = UUID.randomUUID();
  private static final UUID BINH = UUID.randomUUID();
  private static final UUID CHI = UUID.randomUUID();
  private static final UUID DUNG = UUID.randomUUID();
  private static final Instant T0 = Instant.parse("2026-11-25T03:00:00Z");

  /** F-GEO-055…058: bảy câu của lớp trên BG-1, theo thứ tự hỏi. */
  static List<Asked> sc4() {
    List<Asked> a = new ArrayList<>();
    a.add(q(1, AN, "An", "Vì sao A là hình chiếu của S?"));
    a.add(q(2, DUNG, "Dũng", "Vì sao A là hình chiếu của S?"));
    a.add(q(3, BINH, "Bình", "Góc cần tìm là góc nào trên hình?"));
    a.add(q(4, BINH, "Bình", "Góc cần tìm là góc nào trên hình?"));
    a.add(q(5, AN, "An", "Góc cần tìm là góc nào trên hình?"));
    a.add(q(6, AN, "An", "Cho em xin đáp số luôn được không?"));
    a.add(q(7, DUNG, "Dũng", "Tan của một góc là gì?"));
    return a;
  }

  static Asked q(long id, UUID u, String name, String text) {
    return new Asked(id, u, name, text, T0.plusSeconds(id * 60));
  }

  @Test
  @DisplayName("TC-GEO-0717 Gộp không phân biệt hoa thường và khoảng trắng (US-GEO-702-SC5)")
  void tc0717_groupIgnoresCaseAndSpaces() {
    List<Asked> asked = sc4();
    asked.add(q(8, CHI, "Chi", "vì sao  A là hình chiếu của S? "));
    List<Group> g = MonitorView.groups(asked);
    assertThat(g).hasSize(4); // không có nhóm mới
    Group first = g.get(0); // nhóm có câu mới nhất ở trên
    assertThat(first.question()).isEqualTo("Vì sao A là hình chiếu của S?");
    assertThat(first.names()).containsExactly("An", "Dũng", "Chi");
    assertThat(first.count()).isEqualTo(3);
  }

  @Test
  @DisplayName("US-GEO-702-SC4 bốn nhóm, tên theo thứ tự hỏi, nhóm mới nhất ở trên")
  void fourGroups() {
    List<Group> g = MonitorView.groups(sc4());
    assertThat(g)
        .extracting(Group::question)
        .containsExactly(
            "Tan của một góc là gì?",
            "Cho em xin đáp số luôn được không?",
            "Góc cần tìm là góc nào trên hình?",
            "Vì sao A là hình chiếu của S?");
    assertThat(g.get(2).names()).containsExactly("Bình", "An");
    assertThat(g.get(2).count()).isEqualTo(3);
    assertThat(g.get(3).names()).containsExactly("An", "Dũng");
    assertThat(g.get(3).count()).isEqualTo(2);
  }

  @Test
  @DisplayName("US-GEO-701-SC1, SC3, SC4 thẻ 12′ và tình trạng theo BR-GEO-268")
  void cardsAndStatus() {
    assertThat(MonitorView.averageMinutes(List.of(18 * 60, 11 * 60, 9 * 60, 21 * 60), 5))
        .isEqualTo(12);
    assertThat(MonitorView.averageMinutes(List.of(), 5)).isZero();
    assertThat(MonitorView.status(true, false, 2, 3, true)).isEqualTo("Đang chờ lời giải");
    assertThat(MonitorView.status(true, false, 1, 2, true)).isEqualTo("Đang làm");
    assertThat(MonitorView.status(true, false, 0, 0, true)).isEqualTo("Tự làm, không cần gợi ý");
    assertThat(MonitorView.status(false, false, 0, 0, true)).isEqualTo("Chưa bắt đầu");
    assertThat(MonitorView.status(true, true, 2, 3, false)).isEqualTo("Đã xem lời giải");
    assertThat(MonitorView.status(true, false, 2, 2, false)).isEqualTo("Đang làm");
    assertThat(MonitorView.minutes(29 * 60 + 59)).isEqualTo(30);
  }
}
