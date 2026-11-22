package vn.geo.server.geometry.api;

import java.util.List;

/**
 * Một ràng buộc của hình (BR-GEO-127, 128).
 *
 * <ul>
 *   <li>{@code len [P, Q]} và {@code value}: |PQ| = value;
 *   <li>{@code perp [P, Q, R, S]}: PQ ⊥ RS, sai số |u·v| / (|u|·|v|);
 *   <li>{@code eq [P, Q, R, S]}: |PQ| = |RS|.
 * </ul>
 */
public record Constraint(String kind, List<String> points, double value) {

  public static Constraint len(String p, String q, double value) {
    return new Constraint("len", List.of(p, q), value);
  }

  public static Constraint perp(String p, String q, String r, String s) {
    return new Constraint("perp", List.of(p, q, r, s), 0);
  }

  public static Constraint eq(String p, String q, String r, String s) {
    return new Constraint("eq", List.of(p, q, r, s), 0);
  }
}
