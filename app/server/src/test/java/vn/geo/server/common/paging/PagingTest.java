package vn.geo.server.common.paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;

class PagingTest {

  record Row(Instant at, UUID id) {
    KeysetCursor key() {
      return KeysetCursor.of(id, at);
    }
  }

  private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);
  private static final ListKind LIST = ListKind.of("test.rows", KeyType.TIMESTAMP);
  private static final ListKind BANK = ListKind.byId("content.problem-bank");
  private final Paging paging =
      new Paging(new PagingProperties(20, 100, Map.of("content.problem-bank", 50), KEY));
  private final UUID org = Uuid7.next();

  @Test
  void defaultAndClampedLimit() {
    assertThat(paging.request(LIST, null, null, org).limit()).isEqualTo(20);
    assertThat(paging.request(LIST, 5, null, org).limit()).isEqualTo(5);
    assertThat(paging.request(LIST, 100, null, org).limit()).isEqualTo(100);
    assertThat(paging.request(LIST, 10_000, null, org).limit()).isEqualTo(100);
  }

  @Test
  void perListCapIsLowerThanGlobalCap() {
    assertThat(paging.request(BANK, null, null, org).limit()).isEqualTo(20);
    assertThat(paging.request(BANK, 50, null, org).limit()).isEqualTo(50);
    assertThat(paging.request(BANK, 100, null, org).limit()).isEqualTo(50);
  }

  @Test
  void perListCapCannotRaiseGlobalCap() {
    var p = new PagingProperties(20, 100, Map.of("x.y", 500), KEY);
    assertThat(p.maxLimitFor("x.y")).isEqualTo(100);
    assertThatThrownBy(() -> new PagingProperties(20, 100, Map.of("x.y", 0), KEY))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void cursorOfOneListIsRejectedByAnotherList() {
    Page<Row> p = paging.page(rows(6, false), paging.request(LIST, 5, null, org), Row::key, org);
    ListKind sameShapeOtherList = ListKind.of("test.other-rows", KeyType.TIMESTAMP);
    assertThatThrownBy(() -> paging.request(sameShapeOtherList, 5, p.nextCursor(), org))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_CURSOR));
    assertThatThrownBy(() -> paging.request(LIST, 5, p.nextCursor(), Uuid7.next()))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_CURSOR));
  }

  @Test
  void rejectsNonPositiveLimit() {
    for (int bad : new int[] {0, -1, Integer.MIN_VALUE}) {
      assertThatThrownBy(() -> paging.request(LIST, bad, null, org))
          .isInstanceOfSatisfying(
              AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }
  }

  @Test
  void fetchSizeIsLimitPlusOne() {
    assertThat(paging.request(LIST, 7, null, org).fetchSize()).isEqualTo(8);
  }

  @Test
  void emptyAndBlankCursorMeanFirstPage() {
    assertThat(paging.request(LIST, null, null, org).isFirstPage()).isTrue();
    assertThat(paging.request(LIST, null, "", org).isFirstPage()).isTrue();
  }

  @Test
  void badCursorIsRejected() {
    assertThatThrownBy(() -> paging.request(LIST, null, "rác", org))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_CURSOR));
  }

  @Test
  void emptyResultIsLastPage() {
    Page<Row> p = paging.page(List.of(), paging.request(LIST, 5, null, org), Row::key, org);
    assertThat(p.items()).isEmpty();
    assertThat(p.nextCursor()).isNull();
  }

  @Test
  void exactlyLimitRowsIsLastPage() {
    Page<Row> p = paging.page(rows(5, false), paging.request(LIST, 5, null, org), Row::key, org);
    assertThat(p.items()).hasSize(5);
    assertThat(p.nextCursor()).isNull();
  }

  @Test
  void limitPlusOneRowsGivesCursorAtLastKeptRow() {
    List<Row> fetched = rows(6, false);
    PageRequest req = paging.request(LIST, 5, null, org);
    Page<Row> p = paging.page(fetched, req, Row::key, org);
    assertThat(p.items()).hasSize(5).isEqualTo(fetched.subList(0, 5));
    PageRequest next = paging.request(LIST, 5, p.nextCursor(), org);
    assertThat(next.after()).isEqualTo(fetched.get(4).key());
  }

  /** Mô phỏng truy vấn: sắp (at, id) giảm dần, lấy dòng "nhỏ hơn" cursor, cắt limit+1. */
  @Test
  void walksAllRowsWithoutLossOrDuplicateEvenWithEqualTimestamps() {
    List<Row> table = rows(103, true); // nhiều dòng trùng thời điểm
    List<Row> seen = new ArrayList<>();
    String cursor = null;
    int pages = 0;
    do {
      PageRequest req = paging.request(LIST, 10, cursor, org);
      Page<Row> p = paging.page(query(table, req), req, Row::key, org);
      seen.addAll(p.items());
      cursor = p.nextCursor();
      pages++;
    } while (cursor != null);
    assertThat(pages).isEqualTo(11);
    assertThat(seen).hasSize(103).doesNotHaveDuplicates();
    assertThat(seen).isSortedAccordingTo(order());
  }

  @Test
  void maxLimitPropertyNeverBelowDefault() {
    assertThat(new PagingProperties(50, 10, null, KEY).maxLimit()).isGreaterThanOrEqualTo(50);
  }

  @Test
  void cursorKeyMustBe32Bytes() {
    String k16 = Base64.getEncoder().encodeToString(new byte[16]);
    assertThatThrownBy(() -> new PagingProperties(20, 100, null, k16))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static Comparator<Row> order() {
    return Comparator.comparing(Row::at).thenComparing(Row::id).reversed();
  }

  private static List<Row> query(List<Row> table, PageRequest req) {
    return table.stream()
        .sorted(order())
        .filter(r ->
            req.after() == null
                || order().compare(r, new Row(req.after().instant(0), req.after().id())) > 0)
        .limit(req.fetchSize())
        .toList();
  }

  private static List<Row> rows(int n, boolean sameTimestamps) {
    Instant base = Instant.parse("2026-03-01T00:00:00Z");
    List<Row> out = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      Instant at = base.plusSeconds(sameTimestamps ? i / 7 : i);
      out.add(new Row(at, Uuid7.next()));
    }
    out.sort(order());
    return out;
  }
}
