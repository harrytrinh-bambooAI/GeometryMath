package vn.geo.server.subject.internal.grade;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.geo.server.subject.internal.grade.AnswerGrader.Mode;
import vn.geo.server.subject.internal.grade.AnswerGrader.Outcome;
import vn.geo.server.subject.internal.grade.AnswerGrader.Result;

/** US-S2E8-02 — chấm đáp số dạng tương đương (hàm thuần). Câu A…F theo mục "Dữ liệu" của story. */
class AnswerGraderTest {

  static final String A = "4√3";
  static final String B = "1/2";
  static final String C = "6";
  static final String D = "45°";
  static final String E = "a√2";
  static final String F = "1";
  static final BigDecimal HALF = new BigDecimal("0.5");

  static Result exact(String ref, String sub) {
    return AnswerGrader.grade(ref, sub, Mode.EXACT, null);
  }

  static Result approx(String sub) {
    return AnswerGrader.grade(D, sub, Mode.APPROX, HALF);
  }

  static void correct(Result r, String canonical) {
    assertThat(r.outcome()).isEqualTo(Outcome.CORRECT);
    assertThat(r.canonical()).isEqualTo(canonical);
  }

  static void wrong(Result r) {
    assertThat(r.outcome()).isEqualTo(Outcome.WRONG);
    assertThat(r.canonical()).isNull();
    assertThat(r.message()).isNull();
  }

  static void syntax(Result r, String message) {
    assertThat(r.outcome()).isEqualTo(Outcome.SYNTAX);
    assertThat(r.message()).startsWith(message);
    assertThat(r.canonical()).isNull();
  }

  @Test
  @DisplayName("TC-S2E8-015 Đáp số đúng đúng dạng chuẩn: không có dòng dạng chuẩn")
  void tc015() {
    correct(exact(A, "4√3"), null);
  }

  @Test
  @DisplayName("TC-S2E8-016 Căn chưa rút gọn vẫn đúng và hiện dạng chuẩn")
  void tc016() {
    correct(exact(A, "√48"), "4√3");
  }

  @Test
  @DisplayName("TC-S2E8-017 Phân số chưa rút gọn vẫn đúng")
  void tc017() {
    correct(exact(B, "2/4"), "1/2");
  }

  @Test
  @DisplayName("TC-S2E8-018 Thập phân hữu hạn bằng số hữu tỉ")
  void tc018() {
    correct(exact(B, "0,5"), "1/2");
    correct(exact(B, "0.5"), "1/2");
  }

  @Test
  @DisplayName("TC-S2E8-019 Số nguyên viết nhiều cách")
  void tc019() {
    for (String s : new String[] {"12/2", "6,0", "06", "2×3", "(1+5)"}) {
      assertThat(exact(C, s).outcome()).as(s).isEqualTo(Outcome.CORRECT);
    }
  }

  @Test
  @DisplayName("TC-S2E8-020 Lũy thừa và căn của số chính phương")
  void tc020() {
    assertThat(exact(C, "2^3-2").outcome()).isEqualTo(Outcome.CORRECT);
    assertThat(exact(C, "√36").outcome()).isEqualTo(Outcome.CORRECT);
  }

  @Test
  @DisplayName("TC-S2E8-021 Đáp số có tham số a so khớp theo giá trị")
  void tc021() {
    correct(exact(E, "a√2"), null);
    correct(exact(E, "√2×a"), "a√2");
    correct(exact(E, "√(2a^2)"), "a√2");
    wrong(exact(E, "a√3"));
    wrong(exact(E, "x√2"));
  }

  @Test
  @DisplayName("TC-S2E8-022 Kiểu xấp xỉ nhận 45, 45° và 45,0; 90/2 kèm dạng chuẩn")
  void tc022() {
    correct(approx("45"), null);
    correct(approx("45°"), null);
    correct(approx("45,0"), null);
    correct(approx("90/2"), "45°");
  }

  @Test
  @DisplayName("TC-S2E8-023 Sát biên trên 45,5 là Đúng")
  void tc023() {
    assertThat(approx("45,5").outcome()).isEqualTo(Outcome.CORRECT);
  }

  @Test
  @DisplayName("TC-S2E8-024 Vượt biên trên 45,51 là Sai")
  void tc024() {
    wrong(approx("45,51"));
  }

  @Test
  @DisplayName("TC-S2E8-025 Sát biên dưới 44,5 là Đúng")
  void tc025() {
    assertThat(approx("44,5").outcome()).isEqualTo(Outcome.CORRECT);
  }

  @Test
  @DisplayName("TC-S2E8-026 Vượt biên dưới 44,49 là Sai")
  void tc026() {
    wrong(approx("44,49"));
  }

  @Test
  @DisplayName("TC-S2E8-027 Biên không bị sai số dấu phẩy động: 45,50 và 44,50")
  void tc027() {
    assertThat(approx("45,50").outcome()).isEqualTo(Outcome.CORRECT);
    assertThat(approx("44,50").outcome()).isEqualTo(Outcome.CORRECT);
    wrong(approx("45,500000000001"));
    wrong(approx("44,499999999999"));
  }

  @Test
  @DisplayName("TC-S2E8-028 Sai số 0 cho kiểu xấp xỉ nghĩa là bằng đúng")
  void tc028() {
    assertThat(AnswerGrader.grade(D, "45", Mode.APPROX, BigDecimal.ZERO).outcome())
        .isEqualTo(Outcome.CORRECT);
    wrong(AnswerGrader.grade(D, "45,001", Mode.APPROX, BigDecimal.ZERO));
  }

  @Test
  @DisplayName("TC-S2E8-030 Thập phân làm tròn của số vô tỉ là Sai ở kiểu chính xác")
  void tc030() {
    Result r = exact(A, "6,93");
    assertThat(r.outcome()).isEqualTo(Outcome.WRONG);
    assertThat(r.note()).isEqualTo("Em hãy viết đáp số ở dạng chính xác (có căn)");
    assertThat(exact(A, "5").note()).isEqualTo(r.note()); // lời nhắc không phụ thuộc gần hay xa
    assertThat(exact(C, "7").note()).isNull();
  }

  @Test
  @DisplayName("TC-S2E8-032 Căn thiếu số")
  void tc032() {
    Result r = exact(A, "√");
    syntax(r, "Thiếu số sau dấu căn (ký tự 1)");
    assertThat(r.position()).isEqualTo(1);
  }

  @Test
  @DisplayName("TC-S2E8-033 Ngoặc không cân")
  void tc033() {
    syntax(exact(C, "((1+2)"), "Thiếu dấu ) (ký tự 1)");
    syntax(exact(C, "1+2)"), "Thừa dấu ) (ký tự 4)");
  }

  @Test
  @DisplayName("TC-S2E8-034 Chia cho 0")
  void tc034() {
    syntax(exact(C, "1/0"), "Chia cho 0 không có nghĩa");
    syntax(exact(C, "5÷(2-2)"), "Chia cho 0 không có nghĩa");
  }

  @Test
  @DisplayName("TC-S2E8-035 Chữ lạ")
  void tc035() {
    syntax(exact(C, "abc"), "Chữ chưa dùng được");
    syntax(exact(C, "45 độ"), "Chữ chưa dùng được");
  }

  @Test
  @DisplayName("TC-S2E8-036 Hai số liền nhau")
  void tc036() {
    syntax(exact(C, "5 5"), "Hai số liền nhau, thiếu phép tính");
    correct(exact(A, "4 √3"), null);
  }

  @Test
  @DisplayName("TC-S2E8-037 Hai dấu phẩy trong một số")
  void tc037() {
    syntax(exact(B, "1,2,3"), "Số có hai dấu thập phân");
    syntax(exact(B, "3..5"), "Số có hai dấu thập phân");
  }

  @Test
  @DisplayName("TC-S2E8-038 Dấu cộng, nhân liền nhau")
  void tc038() {
    syntax(exact(C, "++3"), "Thừa dấu + (ký tự 1)");
    syntax(exact(C, "2××3"), "Thừa dấu × (ký tự 3)");
    syntax(exact(C, "--3"), "Thừa dấu - (ký tự 2)");
    syntax(exact(C, "2+"), "Thiếu số sau dấu + (ký tự 2)");
  }

  @Test
  @DisplayName("TC-S2E8-039 Dấu âm đứng đầu hợp lệ")
  void tc039() {
    correct(exact("-3", "-3"), null);
    correct(exact("-3", "0-3"), "-3");
  }

  @Test
  @DisplayName("TC-S2E8-040 Đáp số dài đúng 50 ký tự được chấm")
  void tc040() {
    String s = "6" + "+0".repeat(24) + "0"; // 1 + 48 + 1 = 50 ký tự, giá trị 6
    assertThat(s).hasSize(50);
    assertThat(exact(C, s).outcome()).isEqualTo(Outcome.CORRECT);
    syntax(exact(C, s + "0"), "Đáp số tối đa 50 ký tự");
  }

  @Test
  @DisplayName("TC-S2E8-042 Số 15 chữ số nhận, 16 chữ số là Sai cú pháp")
  void tc042() {
    wrong(exact(C, "100000000000000"));
    syntax(exact(C, "1000000000000000"), "Số quá lớn");
  }

  @Test
  @DisplayName("TC-S2E8-043 Dấu chấm là dấu thập phân, không phải ngăn nghìn")
  void tc043() {
    assertThat(exact(F, "1.000").outcome()).isEqualTo(Outcome.CORRECT);
    assertThat(exact(C, "6.0").outcome()).isEqualTo(Outcome.CORRECT);
    wrong(exact("1000", "1.000"));
  }

  @Test
  @DisplayName("TC-S2E8-054 Sai cú pháp báo đúng vị trí ký tự")
  void tc054() {
    Result r = exact(C, "2+*3");
    assertThat(r.outcome()).isEqualTo(Outcome.SYNTAX);
    assertThat(r.message()).isEqualTo("Thừa dấu * (ký tự 3)");
    assertThat(r.position()).isEqualTo(3);
  }

  /** Một dòng của bảng TC-S2E8-111: mẫu, kiểu ("=" chính xác, "~" xấp xỉ ±0,5), nộp, kết quả. */
  record Row(String ref, String mode, String sub, Outcome want, String canonical) {}

  static Row ok(String ref, String mode, String sub, String canonical) {
    return new Row(ref, mode, sub, Outcome.CORRECT, canonical);
  }

  static Row no(String ref, String mode, String sub) {
    return new Row(ref, mode, sub, Outcome.WRONG, null);
  }

  static Row bad(String ref, String mode, String sub) {
    return new Row(ref, mode, sub, Outcome.SYNTAX, null);
  }

  static final Row[] TABLE = {
    // 20 dòng Đúng
    ok(A, "=", "4√3", null),
    ok(A, "=", "√48", A),
    ok(A, "=", "2√12", A),
    ok(A, "=", "12/√3", A),
    ok(A, "=", "√3×4", A),
    ok(B, "=", "1/2", null),
    ok(B, "=", "2/4", B),
    ok(B, "=", "0,5", B),
    ok(B, "=", "1÷2", B),
    ok(B, "=", "2^-1", B),
    ok(C, "=", "6", null),
    ok(C, "=", "6,0", null),
    ok(C, "=", "√36", C),
    ok(C, "=", "(1+5)", C),
    ok(D, "~", "45", null),
    ok(D, "~", "44,5", null),
    ok(D, "~", "90/2", D),
    ok(E, "=", "√2a", E),
    ok(E, "=", "2a/√2", E),
    ok("-3", "=", "0-3", "-3"),
    // 20 dòng Sai hoặc Sai cú pháp
    no(A, "=", "6,93"),
    no(A, "=", "3√4"),
    no(A, "=", "√47"),
    no(A, "=", "4√2"),
    no(B, "=", "0,49"),
    no(B, "=", "2"),
    no(B, "=", "-1/2"),
    no(C, "=", "6,000001"),
    no(C, "=", "5"),
    no(C, "=", "2π"),
    no(D, "~", "45,51"),
    no(D, "~", "44,49"),
    no(D, "~", "60°"),
    no(E, "=", "a"),
    no(E, "=", "2a"),
    no("-3", "=", "3"),
    bad(A, "=", "√"),
    bad(C, "=", "1/0"),
    bad(C, "=", "5 5"),
    bad(D, "~", "45 độ"),
  };

  @Test
  @DisplayName(
      "TC-S2E8-111 Bảng dữ liệu tương đương: 20 dòng đúng và 20 dòng sai, mỗi dòng dưới 50 ms")
  void tc111() {
    assertThat(TABLE).hasSize(40);
    exact(A, "4√3"); // làm nóng
    for (Row r : TABLE) {
      long t0 = System.nanoTime();
      Result got =
          AnswerGrader.grade(
              r.ref(), r.sub(), "~".equals(r.mode()) ? Mode.APPROX : Mode.EXACT, HALF);
      long ms = (System.nanoTime() - t0) / 1_000_000;
      assertThat(got.outcome()).as("%s ← %s", r.ref(), r.sub()).isEqualTo(r.want());
      assertThat(got.canonical())
          .as("dạng chuẩn %s ← %s", r.ref(), r.sub())
          .isEqualTo(r.canonical());
      assertThat(ms).as("thời gian chấm %s", r.sub()).isLessThan(50);
    }
  }

  @Test
  @DisplayName("BR-S2E8-27 đáp số mẫu có đơn vị chữ hoặc nhiều vế thì không chấm được bằng máy")
  void gradable() {
    assertThat(AnswerGrader.gradable("45°")).isTrue();
    assertThat(AnswerGrader.gradable("a√6/3")).isTrue();
    assertThat(AnswerGrader.gradable("24√3 cm³")).isFalse();
    assertThat(AnswerGrader.gradable("Sxq = 15π cm²; V = 12π cm³")).isFalse();
    assertThat(AnswerGrader.gradable(null)).isFalse();
  }
}
