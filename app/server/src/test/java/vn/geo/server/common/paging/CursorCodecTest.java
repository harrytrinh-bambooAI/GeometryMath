package vn.geo.server.common.paging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.id.Uuid7;

class CursorCodecTest {

  private static final byte[] KEY = new byte[32];
  private static final ListKind BY_TIME = ListKind.of("test.by-time", KeyType.TIMESTAMP);
  private static final ListKind BY_ID = ListKind.byId("test.by-id");
  private static final ListKind BANK =
      ListKind.of(
          "content.problem-bank", KeyType.LONG, KeyType.TEXT, KeyType.UUID, KeyType.TIMESTAMP);

  private final CursorCodec codec = new CursorCodec(KEY);
  private final UUID org = Uuid7.next();

  @Test
  void roundTripKeepsMicrosecondPrecision() {
    KeysetCursor c = KeysetCursor.of(Uuid7.next(), Instant.parse("2026-01-02T03:04:05.123456Z"));
    assertThat(codec.decode(BY_TIME, codec.encode(BY_TIME, c, org), org)).isEqualTo(c);
  }

  @Test
  void idOnlyCursorRoundTrips() {
    KeysetCursor c = KeysetCursor.ofId(Uuid7.next());
    assertThat(codec.decode(BY_ID, codec.encode(BY_ID, c, org), org)).isEqualTo(c);
  }

  @Test
  void multiColumnSortKeyRoundTrips() {
    KeysetCursor c =
        KeysetCursor.of(
            Uuid7.next(),
            3,
            "Hình chóp S.ABCD",
            Uuid7.next(),
            Instant.parse("2026-05-06T07:08:09Z"));
    KeysetCursor back = codec.decode(BANK, codec.encode(BANK, c, org), org);
    assertThat(back).isEqualTo(c);
    assertThat(back.longKey(0)).isEqualTo(3L);
    assertThat(back.text(1)).isEqualTo("Hình chóp S.ABCD");
  }

  @Test
  void nanosAreTruncatedToMicros() {
    KeysetCursor c = KeysetCursor.of(Uuid7.next(), Instant.parse("2026-01-02T03:04:05.123456789Z"));
    assertThat(codec.decode(BY_TIME, codec.encode(BY_TIME, c, org), org).instant(0))
        .isEqualTo(Instant.parse("2026-01-02T03:04:05.123456Z"));
  }

  @Test
  void handlesInstantsBeforeEpoch() {
    KeysetCursor c = KeysetCursor.of(Uuid7.next(), Instant.parse("1969-12-31T23:59:59.000001Z"));
    assertThat(codec.decode(BY_TIME, codec.encode(BY_TIME, c, org), org)).isEqualTo(c);
  }

  @Test
  void tokenIsOpaqueAndCarriesVersion() {
    UUID id = Uuid7.next();
    String token = codec.encode(BANK, KeysetCursor.of(id, 7, "bí mật", id, Instant.now()), org);
    byte[] bytes = Base64.getUrlDecoder().decode(token);
    String raw = new String(bytes, StandardCharsets.ISO_8859_1);
    assertThat(token).doesNotContain(id.toString());
    assertThat(raw).doesNotContain("2026").doesNotContain("problem-bank");
    assertThat(new String(bytes, StandardCharsets.UTF_8)).doesNotContain("bí mật");
    assertThat(bytes[0]).isEqualTo(CursorCodec.VERSION);
  }

  @Test
  void rejectsTamperedToken() {
    String token = codec.encode(BY_TIME, KeysetCursor.of(Uuid7.next(), Instant.now()), org);
    byte[] raw = Base64.getUrlDecoder().decode(token);
    raw[raw.length / 2] ^= 1;
    assertInvalid(BY_TIME, Base64.getUrlEncoder().withoutPadding().encodeToString(raw), org);
  }

  @Test
  void rejectsOtherVersion() {
    String token = codec.encode(BY_TIME, KeysetCursor.of(Uuid7.next(), Instant.now()), org);
    byte[] raw = Base64.getUrlDecoder().decode(token);
    raw[0] = 1;
    assertInvalid(BY_TIME, Base64.getUrlEncoder().withoutPadding().encodeToString(raw), org);
  }

  @Test
  void rejectsGarbageEmptyAndOversizedTokens() {
    assertInvalid(BY_TIME, "not-base64!!!", org);
    assertInvalid(BY_TIME, "", org);
    assertInvalid(BY_TIME, null, org);
    assertInvalid(BY_TIME, "QUJD", org);
    assertInvalid(BY_TIME, "A".repeat(5000), org);
  }

  @Test
  void rejectsTokenFromAnotherOrg() {
    String token = codec.encode(BY_TIME, KeysetCursor.of(Uuid7.next(), Instant.now()), org);
    assertInvalid(BY_TIME, token, UUID.randomUUID());
  }

  @Test
  void rejectsTokenFromAnotherListEvenWithSameKeyShape() {
    ListKind otherSameShape = ListKind.of("test.other", KeyType.TIMESTAMP);
    String token = codec.encode(BY_TIME, KeysetCursor.of(Uuid7.next(), Instant.now()), org);
    assertInvalid(otherSameShape, token, org);
    assertInvalid(BY_ID, token, org);
  }

  @Test
  void rejectsTokenSignedWithAnotherKey() {
    byte[] other = new byte[32];
    other[0] = 1;
    String token =
        new CursorCodec(other).encode(BY_TIME, KeysetCursor.of(Uuid7.next(), Instant.now()), org);
    assertInvalid(BY_TIME, token, org);
  }

  @Test
  void encodeRefusesKeysThatDoNotMatchListShape() {
    assertThatThrownBy(() -> codec.encode(BY_ID, KeysetCursor.of(Uuid7.next(), Instant.now()), org))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> codec.encode(BY_TIME, KeysetCursor.of(Uuid7.next(), 5L), org))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void keyMustBe32Bytes() {
    assertThatThrownBy(() -> new CursorCodec(new byte[16]))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void listCodeMustBeStableLowercase() {
    assertThatThrownBy(() -> ListKind.byId("Bad Code"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ListKind.byId("")).isInstanceOf(IllegalArgumentException.class);
    assertThat(ListKind.byId("tracking.event-log").code()).isEqualTo("tracking.event-log");
  }

  private void assertInvalid(ListKind kind, String token, UUID orgId) {
    assertThatThrownBy(() -> codec.decode(kind, token, orgId))
        .isInstanceOfSatisfying(
            AppException.class, e -> assertThat(e.code()).isEqualTo(ErrorCode.INVALID_CURSOR));
  }
}
