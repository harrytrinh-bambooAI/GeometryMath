package vn.geo.server.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import vn.geo.server.tracking.internal.classboard.domain.BoardRules;
import vn.geo.server.tracking.internal.classboard.domain.StuckDigest;
import vn.geo.server.tracking.internal.classboard.domain.StuckDigest.Sheet;

/** S3-E3 — quy tắc thuần của Kết quả lớp và bộ gộp "Cả lớp vướng ở đâu". */
class BoardRulesS3E3Test {

  @Test
  void statusFollowsBrS3E302() {
    assertThat(BoardRules.status(false, false, false)).isEqualTo("todo");
    assertThat(BoardRules.status(true, false, false)).isEqualTo("doing");
    assertThat(BoardRules.status(true, false, true)).isEqualTo("review");
    assertThat(BoardRules.status(true, true, true)).isEqualTo("correct");
  }

  @Test
  void vietnameseOrderPutsDBeforeDWithStrokeAndTiesByCode() {
    List<String[]> rows =
        new ArrayList<>(
            List.of(
                new String[] {"Đức Huy", "HS-3"},
                new String[] {"Hoàng Nam", "HS-9B2L-85VG"},
                new String[] {"Đạt", "HS-5"},
                new String[] {"Bình An", "HS-4"},
                new String[] {"Dũng", "HS-2"},
                new String[] {"Hoàng Nam", "HS-7Q8Z-48DF"},
                new String[] {"Chi Mai", "HS-8"}));
    Function<String[], String> name = r -> r[0];
    Function<String[], String> code = r -> r[1];
    rows.sort(BoardRules.byVietnameseName(name, code));
    assertThat(rows.stream().map(r -> r[0] + "|" + r[1]))
        .containsExactly(
            "Bình An|HS-4",
            "Chi Mai|HS-8",
            "Dũng|HS-2",
            "Đạt|HS-5",
            "Đức Huy|HS-3",
            "Hoàng Nam|HS-7Q8Z-48DF",
            "Hoàng Nam|HS-9B2L-85VG");
  }

  @Test
  void maskHidesEmailPhoneAndCodes() {
    assertThat(BoardRules.mask("Em là Mai, số em 0912345678, cô giải giúp em"))
        .isEqualTo("Em là Mai, số em [đã ẩn], cô giải giúp em");
    assertThat(BoardRules.mask("gửi vào mai@example.org nhé")).isEqualTo("gửi vào [đã ẩn] nhé");
    assertThat(BoardRules.mask("mã em HS-8L2K-77AB lớp HK-4P7Q")).doesNotContain("HS-8L2K");
    assertThat(BoardRules.mask("AB = 12, SA = 2√2")).isEqualTo("AB = 12, SA = 2√2");
  }

  @Test
  void insightDropsPercentRankingAndVerifiedLabel() {
    assertThat(BoardRules.cleanInsight("75% lớp sai bước này. Nhiều em quên điều kiện đáy."))
        .isEqualTo("Nhiều em quên điều kiện đáy.");
    assertThat(BoardRules.cleanInsight("Đây là đề khó nhất lớp")).isEmpty();
    assertThat(BoardRules.cleanInsight("Đã kiểm chứng: nhiều em sai giao tuyến")).isEmpty();
    assertThat(BoardRules.shortTitle("Đề 3 hai mặt phẳng")).isEqualTo("hai mặt phẳng");
  }

  @Test
  void parseKeepsOnlyInsightsWithExistingWorks() {
    String reply =
        """
        {"insights":[
          {"step":1,"text":"Nhiều em quên điều kiện đáy là hình vuông","works":[1,2,3],"label":"Đã kiểm chứng"},
          {"step":2,"text":"Dẫn bài làm không tồn tại","works":[9]},
          {"step":1,"text":"75% lớp sai bước này","works":[1]}]}
        """;
    var out = StuckDigest.parse(reply, 5);
    assertThat(out).hasSize(1);
    assertThat(out.getFirst().works()).containsExactly(1, 2, 3);
    assertThat(StuckDigest.parse("Em thử nhìn lại hình nhé.", 5)).isNull();
  }

  @Test
  void promptCarriesOnlyItemStepAndMaskedContent() {
    Sheet s = new Sheet(List.of(1), List.of("số em 0912345678"), List.of("SB"));
    String prompt = StuckDigest.prompt(3, List.of(s));
    assertThat(prompt).contains("Mã đề: D3").contains("bước dừng 1").contains("[đã ẩn]");
    assertThat(prompt).doesNotContain("0912345678");
    assertThat(StuckDigest.parse(StuckDigest.mockReply(List.of(s, s)), 2).getFirst().works())
        .containsExactly(1, 2);
  }
}
