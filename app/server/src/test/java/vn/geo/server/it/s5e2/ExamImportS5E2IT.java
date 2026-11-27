package vn.geo.server.it.s5e2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.geo.server.exam.internal.domain.ExamDataset;
import vn.geo.server.exam.internal.service.ExamImportService;
import vn.geo.server.exam.internal.service.ExamImportService.Report;
import vn.geo.server.exam.internal.service.S5E2TestHookService;
import vn.geo.server.it.AbstractIT;

/**
 * Lệnh nạp dữ liệu đề có kiểm (US-S5E2-01, BR-S5E2-01…03) trên geo_test: đủ 4/110/1 774, lũy đẳng,
 * câu lệch đáp số engine bị loại, ma trận tổng điểm ≠ 10 bị từ chối.
 */
class ExamImportS5E2IT extends AbstractIT {

  @Autowired ExamImportService importer;

  @Test
  @DisplayName(
      "BR-S5E2-01 · nạp de-thi v1.0 đủ 4 ma trận, 110 mẫu, 1 774 câu; chạy lại 0 thêm, 0 đổi")
  void importIsCompleteAndIdempotent() {
    ExamDataset d = importer.loadDataset();
    importer.run(d, "de-thi v1.0");
    Report again = importer.run(importer.loadDataset(), "de-thi v1.0");
    assertThat(again.errors()).isZero();
    assertThat(again.matrices()).isEqualTo(4);
    assertThat(again.templates()).isEqualTo(110);
    assertThat(again.items()).isEqualTo(1774);
    assertThat(again.added()).isZero();
    assertThat(again.changed()).isZero();
    assertThat(again.stored())
        .containsEntry("matrices", 4L)
        .containsEntry("templates", 110L)
        .containsEntry("items", 1774L);
    assertThat(again.summary()).contains("0 thêm, 0 đổi");
  }

  @Test
  @DisplayName("BR-S5E2-02 · câu HHKG đáp số 0,82 (engine 0,8165) bị loại, 1 773 câu vẫn nạp")
  void answerMismatchRejected() {
    ExamDataset d = importer.loadDataset();
    String bad = S5E2TestHookService.corruptAnswer(d, 0.82).path("id").asString();
    Map<String, Object> seen = new HashMap<>();
    Report r = importer.dryRun(d, "de-thi v1.0", true, seen::putAll);
    assertThat(r.errors()).isEqualTo(1);
    assertThat(r.items()).isEqualTo(1773);
    assertThat(r.issues().get(0).id()).isEqualTo(bad);
    assertThat(r.issues().get(0).message()).contains("0.82").contains("0.8164965");
    assertThat(seen).containsEntry("itemStored:" + bad, false);
  }

  @Test
  @DisplayName("BR-S5E2-03 · KT15-HHKG11 tổng điểm 9 bị từ chối, 3 ma trận còn lại được nạp")
  void matrixTotalRejected() {
    ExamDataset d = importer.loadDataset();
    S5E2TestHookService.corruptMatrixTotal(d, "KT15-HHKG11", 1.5);
    Report r = importer.dryRun(d, "de-thi v1.0", true, m -> {});
    assertThat(r.matrices()).isEqualTo(3);
    assertThat(r.issues())
        .singleElement()
        .satisfies(i -> {
          assertThat(i.id()).isEqualTo("KT15-HHKG11");
          assertThat(i.message()).isEqualTo("tổng điểm 9 ≠ 10");
        });
  }
}
