package vn.geo.server.geometry.internal.stepcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Bộ tính biểu thức số của một bước lời giải (S2-E5, US-S2E5-02): số thập phân dấu phẩy hoặc chấm,
 * tham số {@code a}, căn {@code √}, lũy thừa, nhân ngầm ({@code a√2}, {@code 2a}), hàm lượng giác
 * theo độ, tên đoạn thẳng ({@code AC}) và tên góc ({@code SCA}) tra qua {@link Names}. Nhân ngầm
 * buộc chặt hơn phép chia: {@code a√2 / a√2} = 1.
 *
 * <p>Hàm thuần, không ném lỗi hệ thống: tên chưa có giá trị ném {@link Missing}; chữ không đọc được
 * ném {@link Unreadable}.
 */
final class Expr {

  private Expr() {}

  /** Tra giá trị theo tên; trả null khi chưa biết. */
  interface Names {
    Double segment(String name);

    /** Góc theo độ. */
    Double angle(String name);

    /** Giá trị hàm lượng giác đã ghi ở bước trước cho góc này (vd tan SCA), null khi chưa có. */
    Double trig(String fn, String angle);

    Double symbol(String name);
  }

  /** Biểu thức dùng một tên chưa có giá trị. */
  static final class Missing extends RuntimeException {
    private static final long serialVersionUID = 1L;
    final String name;

    Missing(String name) {
      super(name, null, false, false);
      this.name = name;
    }
  }

  /** Không đọc được biểu thức. */
  static final class Unreadable extends RuntimeException {
    private static final long serialVersionUID = 1L;

    Unreadable(String why) {
      super(why, null, false, false);
    }
  }

  private enum T {
    NUM,
    SYM,
    NAME,
    FUNC,
    SQRT,
    ADD,
    SUB,
    MUL,
    DIV,
    POW,
    SQ,
    CUBE,
    LP,
    RP,
    UNIT
  }

  private record Tok(T t, String s, double v) {}

  private static final Set<String> FUNCS =
      Set.of("sin", "cos", "tan", "tg", "cot", "cotg", "arctan", "arcsin", "arccos", "sqrt");
  private static final Set<String> UNITS = Set.of("cm", "dm", "mm", "m", "độ", "đvdt", "đvtt");

  static double eval(String text, Names names) {
    List<Tok> toks = tokenize(text);
    if (toks.isEmpty()) {
      throw new Unreadable("rỗng");
    }
    Parser p = new Parser(toks, names);
    double v = p.expr();
    if (p.pos != toks.size()) {
      throw new Unreadable("thừa ký hiệu");
    }
    if (Double.isNaN(v) || Double.isInfinite(v)) {
      throw new Unreadable("không ra số");
    }
    return v;
  }

  private static boolean nameChar(char c) {
    return Character.isLetter(c) && c != '√' && c != 'π';
  }

  private static List<Tok> tokenize(String s) {
    List<Tok> out = new ArrayList<>();
    int i = 0;
    int n = s.length();
    while (i < n) {
      char c = s.charAt(i);
      if (Character.isWhitespace(c) || c == ' ') {
        i++;
      } else if (c >= '0' && c <= '9') {
        int j = i;
        while (j < n && Character.isDigit(s.charAt(j))) {
          j++;
        }
        if (j + 1 < n
            && (s.charAt(j) == ',' || s.charAt(j) == '.')
            && Character.isDigit(s.charAt(j + 1))) {
          j++;
          while (j < n && Character.isDigit(s.charAt(j))) {
            j++;
          }
        }
        out.add(new Tok(T.NUM, "", Double.parseDouble(s.substring(i, j).replace(',', '.'))));
        i = j;
      } else if (c == '√') {
        out.add(new Tok(T.SQRT, "", 0));
        i++;
      } else if (c == 'π') {
        out.add(new Tok(T.NUM, "", Math.PI));
        i++;
      } else if (c == '+') {
        out.add(new Tok(T.ADD, "", 0));
        i++;
      } else if (c == '-' || c == '−' || c == '–') {
        out.add(new Tok(T.SUB, "", 0));
        i++;
      } else if (c == '*' || c == '·' || c == '×' || c == '⋅') {
        out.add(new Tok(T.MUL, "", 0));
        i++;
      } else if (c == '/' || c == ':' || c == '÷') {
        out.add(new Tok(T.DIV, "", 0));
        i++;
      } else if (c == '^') {
        out.add(new Tok(T.POW, "", 0));
        i++;
      } else if (c == '²') {
        out.add(new Tok(T.SQ, "", 0));
        i++;
      } else if (c == '³') {
        out.add(new Tok(T.CUBE, "", 0));
        i++;
      } else if (c == '(') {
        out.add(new Tok(T.LP, "", 0));
        i++;
      } else if (c == ')') {
        out.add(new Tok(T.RP, "", 0));
        i++;
      } else if (c == '°') {
        out.add(new Tok(T.UNIT, "°", 0));
        i++;
      } else if (nameChar(c)) {
        int j = i;
        while (j < n && (nameChar(s.charAt(j)) || s.charAt(j) == '\'' || s.charAt(j) == '′')) {
          j++;
        }
        String w = s.substring(i, j);
        i = j;
        String lower = w.toLowerCase(java.util.Locale.ROOT);
        if (FUNCS.contains(lower)) {
          out.add(new Tok(T.FUNC, lower, 0));
        } else if (UNITS.contains(lower) && !out.isEmpty()) {
          out.add(new Tok(T.UNIT, lower, 0));
        } else if (lower.equals("pi")) {
          out.add(new Tok(T.NUM, "", Math.PI));
        } else if (Character.isUpperCase(w.charAt(0)) && w.replaceAll("['′]", "").length() >= 2) {
          if (!w.replaceAll("['′]", "").chars().allMatch(Character::isUpperCase)) {
            throw new Unreadable(w);
          }
          out.add(new Tok(T.NAME, w.replace('′', '\''), 0));
        } else {
          out.add(new Tok(T.SYM, w, 0));
        }
      } else {
        throw new Unreadable(String.valueOf(c));
      }
    }
    return out;
  }

  private static final class Parser {
    final List<Tok> toks;
    final Names names;
    int pos;

    Parser(List<Tok> toks, Names names) {
      this.toks = toks;
      this.names = names;
    }

    T peek() {
      return pos < toks.size() ? toks.get(pos).t() : null;
    }

    double expr() {
      double v = term();
      while (peek() == T.ADD || peek() == T.SUB) {
        T op = toks.get(pos++).t();
        double r = term();
        v = op == T.ADD ? v + r : v - r;
      }
      return v;
    }

    double term() {
      double v = juxta();
      while (peek() == T.MUL || peek() == T.DIV) {
        T op = toks.get(pos++).t();
        double r = juxta();
        v = op == T.MUL ? v * r : v / r;
      }
      return v;
    }

    /** Nhân ngầm: 2a, a√2, 2(a + 1). */
    double juxta() {
      double v = unary();
      while (startsAtom(peek())) {
        v *= power();
      }
      return v;
    }

    boolean startsAtom(T t) {
      return t == T.NUM || t == T.SYM || t == T.NAME || t == T.FUNC || t == T.SQRT || t == T.LP;
    }

    double unary() {
      if (peek() == T.SUB) {
        pos++;
        return -unary();
      }
      if (peek() == T.ADD) {
        pos++;
        return unary();
      }
      return power();
    }

    double power() {
      double v = postfix();
      if (peek() == T.POW) {
        pos++;
        v = Math.pow(v, unary());
      }
      return v;
    }

    double postfix() {
      double v = atom();
      while (true) {
        T t = peek();
        if (t == T.SQ) {
          v = v * v;
        } else if (t == T.CUBE) {
          v = v * v * v;
        } else if (t != T.UNIT) {
          return v;
        }
        pos++;
      }
    }

    double atom() {
      if (pos >= toks.size()) {
        throw new Unreadable("thiếu vế");
      }
      Tok t = toks.get(pos++);
      switch (t.t()) {
        case NUM:
          return t.v();
        case SQRT:
          return Math.sqrt(postfix());
        case LP:
          {
            double v = expr();
            if (peek() != T.RP) {
              throw new Unreadable("thiếu ngoặc");
            }
            pos++;
            return v;
          }
        case SYM:
          {
            Double v = names.symbol(t.s());
            if (v == null) {
              throw new Unreadable(t.s());
            }
            return v;
          }
        case NAME:
          {
            String bare = t.s().replace("'", "");
            if (bare.length() > 3) {
              throw new Unreadable(t.s());
            }
            Double v = bare.length() == 3 ? names.angle(t.s()) : names.segment(t.s());
            if (v == null) {
              throw new Missing(t.s());
            }
            return v;
          }
        case FUNC:
          return func(t.s());
        default:
          throw new Unreadable("sai vị trí");
      }
    }

    double func(String fn) {
      if (fn.equals("sqrt")) {
        return Math.sqrt(postfix());
      }
      if (fn.startsWith("arc")) {
        double x = postfix();
        double r =
            switch (fn) {
              case "arctan" -> Math.atan(x);
              case "arcsin" -> Math.asin(x);
              default -> Math.acos(x);
            };
        return Math.toDegrees(r);
      }
      String f = fn.equals("tg") ? "tan" : fn.equals("cotg") ? "cot" : fn;
      double deg;
      if (peek() == T.NAME) {
        String ang = toks.get(pos++).s();
        Double known = names.trig(f, ang);
        if (known != null) {
          return known;
        }
        Double a = names.angle(ang);
        if (a == null) {
          throw new Missing(f + " " + ang);
        }
        deg = a;
      } else {
        deg = postfix();
      }
      return trig(f, deg);
    }
  }

  static double trig(String f, double deg) {
    double r = Math.toRadians(deg);
    return switch (f) {
      case "sin" -> Math.sin(r);
      case "cos" -> Math.cos(r);
      case "tan" -> Math.tan(r);
      default -> 1 / Math.tan(r);
    };
  }

  /** Góc (độ) từ giá trị hàm lượng giác. */
  static double inverse(String f, double v) {
    return Math.toDegrees(
        switch (f) {
          case "sin" -> Math.asin(v);
          case "cos" -> Math.acos(v);
          case "tan" -> Math.atan(v);
          default -> Math.atan(1 / v);
        });
  }
}
