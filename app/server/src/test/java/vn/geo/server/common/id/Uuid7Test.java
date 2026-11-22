package vn.geo.server.common.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class Uuid7Test {

  @Test
  void hasVersion7AndRfcVariant() {
    UUID id = Uuid7.next();
    assertThat(id.version()).isEqualTo(7);
    assertThat(id.variant()).isEqualTo(2);
  }

  @Test
  void embedsCurrentTimestamp() {
    long before = System.currentTimeMillis();
    Instant ts = Uuid7.timestamp(Uuid7.next());
    assertThat(ts.toEpochMilli()).isBetween(before, System.currentTimeMillis());
  }

  @Test
  void isStrictlyIncreasingEvenWithFrozenClock() {
    Uuid7.Generator g = new Uuid7.Generator(() -> 1_700_000_000_000L);
    UUID prev = g.next();
    for (int i = 0; i < 10_000; i++) { // vượt 4096 để thử tràn bộ đếm
      UUID next = g.next();
      assertThat(compare(next, prev)).isPositive();
      prev = next;
    }
  }

  @Test
  void survivesClockGoingBackwards() {
    long[] now = {2_000L};
    Uuid7.Generator g = new Uuid7.Generator(() -> now[0]);
    UUID a = g.next();
    now[0] = 1_000L;
    UUID b = g.next();
    assertThat(compare(b, a)).isPositive();
  }

  @Test
  void uniqueAcrossThreads() throws Exception {
    Set<UUID> all = ConcurrentHashMap.newKeySet();
    try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<?>> fs = new ArrayList<>();
      for (int t = 0; t < 16; t++) {
        fs.add(
            ex.submit(() -> {
              for (int i = 0; i < 5_000; i++) {
                all.add(Uuid7.next());
              }
            }));
      }
      for (Future<?> f : fs) {
        f.get();
      }
    }
    assertThat(all).hasSize(16 * 5_000);
  }

  @Test
  void timestampRejectsOtherVersions() {
    assertThatThrownBy(() -> Uuid7.timestamp(UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static int compare(UUID a, UUID b) {
    int c = Long.compareUnsigned(a.getMostSignificantBits(), b.getMostSignificantBits());
    return c != 0
        ? c
        : Long.compareUnsigned(a.getLeastSignificantBits(), b.getLeastSignificantBits());
  }
}
