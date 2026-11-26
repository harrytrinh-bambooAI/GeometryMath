package vn.geo.server.subject.internal.grade;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Đọc và tính biểu thức đáp số của bài luyện (BR-S2E8-10, BR-S2E8-11). Hàm thuần, không trạng thái.
 *
 * <p>Bảng ký hiệu: số nguyên, thập phân (dấu phẩy hoặc dấu chấm), {@code + - × * ÷ / ^ √ π ( )},
 * chữ {@code a} và {@code x}, {@code °}. Dấu cách bị bỏ qua. Nhân ngầm ({@code 4√3}, {@code a√2},
 * {@code 2a^2}) được nhận; hai số đứng liền nhau thì không. Số hữu tỉ tính chính xác bằng phân số
 * số nguyên lớn; căn không khai được, π và chữ thì tính bằng số thực.
 */
final class Expression {

  static final int MAX_LENGTH = 50;
  static final int MAX_DIGITS = 15;

  /** Lỗi cú pháp: câu tiếng Việt và vị trí ký tự tính từ 1 (0 = không gắn vị trí). */
  static final class SyntaxError extends RuntimeException {
    private static final long serialVersionUID = 1L;
    final int position;

    SyntaxError(String message, int position) {
      super(message, null, false, false);
      this.position = position;
    }

    String text() {
      return position > 0 ? getMessage() + " (ký tự " + position + ")" : getMessage();
    }
  }

  /** Giá trị: phân số chính xác n/d (d &gt; 0) hoặc số thực gần đúng. */
  record Value(BigInteger n, BigInteger d, double approx, boolean exact) {

    static Value of(BigInteger n, BigInteger d) {
      if (d.signum() < 0) {
        n = n.negate();
        d = d.negate();
      }
      BigInteger g = n.gcd(d);
      if (g.signum() != 0 && !g.equals(BigInteger.ONE)) {
        n = n.divide(g);
        d = d.divide(g);
      }
      if (n.bitLength() > 4000 || d.bitLength() > 4000) {
        throw new SyntaxError("Số quá lớn", 0);
      }
      return new Value(
          n,
          d,
          new java.math.BigDecimal(n)
              .divide(new java.math.BigDecimal(d), java.math.MathContext.DECIMAL64)
              .doubleValue(),
          true);
    }

    static Value real(double v) {
      if (!Double.isFinite(v)) {
        throw new SyntaxError("Số quá lớn", 0);
      }
      return new Value(null, null, v, false);
    }

    boolean isZero() {
      return exact ? n.signum() == 0 : approx == 0.0;
    }
  }

  private enum Kind {
    NUM,
    PLUS,
    MINUS,
    TIMES,
    DIVIDE,
    POWER,
    ROOT,
    PI,
    VAR,
    LPAREN,
    RPAREN,
    DEGREE
  }

  private record Token(Kind kind, String text, int pos) {}

  /** Cây biểu thức. */
  sealed interface Node permits Num, Var, Pi, Unary, Binary {}

  record Num(Value value) implements Node {}

  record Var(char name) implements Node {}

  record Pi() implements Node {}

  record Unary(char op, Node arg, int pos) implements Node {}

  record Binary(char op, Node left, Node right, int pos) implements Node {}

  /** Biểu thức đã đọc. */
  record Parsed(Node root, boolean plainNumber, boolean hasRoot, boolean hasPi, boolean hasVar) {}

  private final List<Token> tokens;
  private int at;
  private boolean lastWasNumber;
  private boolean hasRoot;
  private boolean hasPi;
  private boolean hasVar;

  private Expression(List<Token> tokens) {
    this.tokens = tokens;
  }

  /** Đọc biểu thức; ném {@link SyntaxError} khi không đọc được. */
  static Parsed parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new SyntaxError("Em hãy nhập đáp số.", 0);
    }
    if (raw.codePointCount(0, raw.length()) > MAX_LENGTH) {
      throw new SyntaxError("Đáp số tối đa 50 ký tự", 0);
    }
    List<Token> tokens = tokenize(raw);
    Expression p = new Expression(tokens);
    Node root = p.expr();
    if (p.at < tokens.size()) {
      Token t = tokens.get(p.at);
      throw new SyntaxError("Thừa dấu " + t.text(), t.pos());
    }
    boolean plain =
        tokens.stream().allMatch(t -> t.kind() == Kind.NUM || t.kind() == Kind.DEGREE)
            && tokens.stream().filter(t -> t.kind() == Kind.NUM).count() == 1;
    return new Parsed(root, plain, p.hasRoot, p.hasPi, p.hasVar);
  }

  private static List<Token> tokenize(String s) {
    List<Token> out = new ArrayList<>();
    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      int pos = i + 1;
      if (Character.isWhitespace(c) || c == ' ') {
        i++;
        continue;
      }
      if (c >= '0' && c <= '9' || c == ',' || c == '.') {
        int j = i;
        int digits = 0;
        int seps = 0;
        while (j < s.length()) {
          char k = s.charAt(j);
          if (k >= '0' && k <= '9') {
            digits++;
          } else if (k == ',' || k == '.') {
            seps++;
            if (seps > 1) {
              throw new SyntaxError("Số có hai dấu thập phân", j + 1);
            }
          } else {
            break;
          }
          j++;
        }
        String text = s.substring(i, j);
        char first = text.charAt(0);
        char last = text.charAt(text.length() - 1);
        if (first == ',' || first == '.' || last == ',' || last == '.') {
          throw new SyntaxError(
              "Thiếu chữ số cạnh dấu thập phân", first == ',' || first == '.' ? pos : j);
        }
        if (digits > MAX_DIGITS) {
          throw new SyntaxError("Số quá lớn", pos);
        }
        out.add(new Token(Kind.NUM, text, pos));
        i = j;
        continue;
      }
      Kind k =
          switch (c) {
            case '+' -> Kind.PLUS;
            case '-', '−', '–' -> Kind.MINUS;
            case '×', '*', '·' -> Kind.TIMES;
            case '÷', '/', ':' -> Kind.DIVIDE;
            case '^' -> Kind.POWER;
            case '√' -> Kind.ROOT;
            case 'π' -> Kind.PI;
            case 'a', 'x' -> Kind.VAR;
            case '(' -> Kind.LPAREN;
            case ')' -> Kind.RPAREN;
            case '°' -> Kind.DEGREE;
            default -> null;
          };
      if (k == null) {
        if (Character.isLetter(c)) {
          throw new SyntaxError("Chữ chưa dùng được", pos);
        }
        throw new SyntaxError("Ký hiệu chưa dùng được", pos);
      }
      out.add(new Token(k, String.valueOf(c), pos));
      i++;
    }
    if (out.isEmpty()) {
      throw new SyntaxError("Em hãy nhập đáp số.", 0);
    }
    return out;
  }

  private Token peek() {
    return at < tokens.size() ? tokens.get(at) : null;
  }

  private Node expr() {
    Node left;
    Token t = peek();
    if (t != null && t.kind() == Kind.MINUS) {
      at++;
      left = new Unary('-', term(t), t.pos());
    } else {
      left = term(null);
    }
    while (true) {
      t = peek();
      if (t == null || (t.kind() != Kind.PLUS && t.kind() != Kind.MINUS)) {
        return left;
      }
      at++;
      left = new Binary(t.kind() == Kind.PLUS ? '+' : '-', left, term(t), t.pos());
    }
  }

  private Node term(Token after) {
    Node left = power(after);
    while (true) {
      Token t = peek();
      if (t == null) {
        return left;
      }
      if (t.kind() == Kind.TIMES || t.kind() == Kind.DIVIDE) {
        at++;
        left = new Binary(t.kind() == Kind.TIMES ? '*' : '/', left, power(t), t.pos());
      } else if (t.kind() == Kind.NUM) {
        if (lastWasNumber) {
          throw new SyntaxError("Hai số liền nhau, thiếu phép tính", t.pos());
        }
        left = new Binary('*', left, power(null), t.pos());
      } else if (t.kind() == Kind.ROOT
          || t.kind() == Kind.PI
          || t.kind() == Kind.VAR
          || t.kind() == Kind.LPAREN) {
        left = new Binary('*', left, power(null), t.pos());
      } else {
        return left;
      }
    }
  }

  private Node power(Token after) {
    Node base = postfix(after);
    Token t = peek();
    if (t != null && t.kind() == Kind.POWER) {
      at++;
      Token s = peek();
      Node exp;
      if (s != null && s.kind() == Kind.MINUS) {
        at++;
        exp = new Unary('-', power(s), s.pos());
      } else {
        exp = power(t);
      }
      lastWasNumber = false;
      return new Binary('^', base, exp, t.pos());
    }
    return base;
  }

  private Node postfix(Token after) {
    Node n = primary(after);
    Token t = peek();
    if (t != null && t.kind() == Kind.DEGREE) {
      at++; // ° chỉ là đơn vị, không đổi giá trị (BR-S2E8-09)
      Token next = peek();
      if (next != null && next.kind() == Kind.DEGREE) {
        throw new SyntaxError("Thừa dấu °", next.pos());
      }
    }
    return n;
  }

  private Node primary(Token after) {
    Token t = peek();
    if (t == null) {
      throw missingAfter(after);
    }
    at++;
    switch (t.kind()) {
      case NUM -> {
        lastWasNumber = true;
        return new Num(number(t.text()));
      }
      case PI -> {
        lastWasNumber = false;
        hasPi = true;
        return new Pi();
      }
      case VAR -> {
        lastWasNumber = false;
        hasVar = true;
        return new Var(t.text().charAt(0));
      }
      case ROOT -> {
        hasRoot = true;
        Token next = peek();
        if (next == null
            || !(next.kind() == Kind.NUM
                || next.kind() == Kind.PI
                || next.kind() == Kind.VAR
                || next.kind() == Kind.LPAREN
                || next.kind() == Kind.ROOT)) {
          throw new SyntaxError("Thiếu số sau dấu căn", t.pos());
        }
        Node arg = primary(t);
        lastWasNumber = false;
        return new Unary('√', arg, t.pos());
      }
      case LPAREN -> {
        Token inner = peek();
        if (inner != null && inner.kind() == Kind.RPAREN) {
          throw new SyntaxError("Thiếu số trong ngoặc", inner.pos());
        }
        Node e = expr();
        Token close = peek();
        if (close == null || close.kind() != Kind.RPAREN) {
          if (close != null) {
            throw new SyntaxError("Thừa dấu " + close.text(), close.pos());
          }
          throw new SyntaxError("Thiếu dấu )", t.pos());
        }
        at++;
        lastWasNumber = false;
        return e;
      }
      default -> throw new SyntaxError("Thừa dấu " + t.text(), t.pos());
    }
  }

  private static SyntaxError missingAfter(Token after) {
    if (after == null) {
      return new SyntaxError("Thiếu số", 0);
    }
    return switch (after.kind()) {
      case ROOT -> new SyntaxError("Thiếu số sau dấu căn", after.pos());
      case POWER -> new SyntaxError("Thiếu số mũ sau dấu ^", after.pos());
      default -> new SyntaxError("Thiếu số sau dấu " + after.text(), after.pos());
    };
  }

  private static Value number(String text) {
    String t = text.replace(',', '.');
    int dot = t.indexOf('.');
    if (dot < 0) {
      return Value.of(new BigInteger(t), BigInteger.ONE);
    }
    String frac = t.substring(dot + 1);
    return Value.of(new BigInteger(t.substring(0, dot) + frac), BigInteger.TEN.pow(frac.length()));
  }

  /** Tính giá trị với a, x cho trước; ném {@link SyntaxError} khi phép tính không có nghĩa. */
  static Value eval(Node node, double a, double x) {
    return switch (node) {
      case Num n -> n.value();
      case Pi p -> Value.real(Math.PI);
      case Var v -> Value.real(v.name() == 'a' ? a : x);
      case Unary u -> {
        Value v = eval(u.arg(), a, x);
        if (u.op() == '-') {
          yield v.exact() ? Value.of(v.n().negate(), v.d()) : Value.real(-v.approx());
        }
        yield sqrt(v, u.pos());
      }
      case Binary b -> {
        Value l = eval(b.left(), a, x);
        Value r = eval(b.right(), a, x);
        yield switch (b.op()) {
          case '+' -> add(l, r, 1);
          case '-' -> add(l, r, -1);
          case '*' ->
              l.exact() && r.exact()
                  ? Value.of(l.n().multiply(r.n()), l.d().multiply(r.d()))
                  : Value.real(l.approx() * r.approx());
          case '/' -> {
            if (r.isZero() || (!r.exact() && Math.abs(r.approx()) < 1e-300)) {
              throw new SyntaxError("Chia cho 0 không có nghĩa", 0);
            }
            yield l.exact() && r.exact()
                ? Value.of(l.n().multiply(r.d()), l.d().multiply(r.n()))
                : Value.real(l.approx() / r.approx());
          }
          default -> pow(l, r, b.pos());
        };
      }
    };
  }

  private static Value add(Value l, Value r, int sign) {
    if (l.exact() && r.exact()) {
      BigInteger rn = sign > 0 ? r.n() : r.n().negate();
      return Value.of(l.n().multiply(r.d()).add(rn.multiply(l.d())), l.d().multiply(r.d()));
    }
    return Value.real(l.approx() + sign * r.approx());
  }

  private static Value sqrt(Value v, int pos) {
    if (v.approx() < 0) {
      throw new SyntaxError("Căn của số âm không có nghĩa", pos);
    }
    if (v.exact()) {
      BigInteger rn = v.n().sqrt();
      BigInteger rd = v.d().sqrt();
      if (rn.multiply(rn).equals(v.n()) && rd.multiply(rd).equals(v.d())) {
        return Value.of(rn, rd);
      }
    }
    return Value.real(Math.sqrt(v.approx()));
  }

  private static Value pow(Value base, Value exp, int pos) {
    if (base.exact() && exp.exact() && exp.d().equals(BigInteger.ONE) && exp.n().bitLength() < 8) {
      int e = exp.n().intValue();
      if (e == 0 && base.isZero()) {
        throw new SyntaxError("Phép lũy thừa 0^0 không có nghĩa", pos);
      }
      if (e < 0 && base.isZero()) {
        throw new SyntaxError("Chia cho 0 không có nghĩa", 0);
      }
      BigInteger n = base.n().pow(Math.abs(e));
      BigInteger d = base.d().pow(Math.abs(e));
      return e >= 0 ? Value.of(n, d) : Value.of(d, n);
    }
    double v = Math.pow(base.approx(), exp.approx());
    if (Double.isNaN(v)) {
      throw new SyntaxError("Phép lũy thừa không có nghĩa", pos);
    }
    return Value.real(v);
  }
}
