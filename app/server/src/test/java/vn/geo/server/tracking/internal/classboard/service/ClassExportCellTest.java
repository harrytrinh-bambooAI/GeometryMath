package vn.geo.server.tracking.internal.classboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Dev-REV-B3 (REVIEW-T20-T32 mục 5): ô CSV chống công thức theo OWASP, gồm TAB và CR. */
class ClassExportCellTest {

  @Test
  @DisplayName("Dev-REV-B3 Ô bắt đầu bằng = + - @ TAB CR được thêm nháy đơn; ô thường giữ nguyên")
  void formulaLeadIsNeutralised() {
    assertThat(ClassExportService.cell("=1+1")).isEqualTo("'=1+1");
    assertThat(ClassExportService.cell("+84")).isEqualTo("'+84");
    assertThat(ClassExportService.cell("-2")).isEqualTo("'-2");
    assertThat(ClassExportService.cell("@SUM(A1)")).isEqualTo("'@SUM(A1)");
    assertThat(ClassExportService.cell("\t=HYPERLINK(\"http://x\";\"Bấm\")"))
        .isEqualTo("\"'\t=HYPERLINK(\"\"http://x\"\";\"\"Bấm\"\")\"");
    assertThat(ClassExportService.cell("\t=1")).isEqualTo("'\t=1");
    assertThat(ClassExportService.cell("\r=1")).isEqualTo("\"'\r=1\"");
    assertThat(ClassExportService.cell("Hà My")).isEqualTo("Hà My");
    assertThat(ClassExportService.cell("HS-7T7B-98QZ")).isEqualTo("HS-7T7B-98QZ");
    assertThat(ClassExportService.cell(null)).isEmpty();
  }
}
