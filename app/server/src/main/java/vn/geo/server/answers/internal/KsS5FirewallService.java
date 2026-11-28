package vn.geo.server.answers.internal;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import vn.geo.server.answers.api.KsS5Firewall;
import vn.geo.server.learning.api.AnswerGuardApi;

/** Cài đặt {@link KsS5Firewall} (T-261128-58). Phần giá trị dùng lõi V60 qua leakRule. */
@Service
public class KsS5FirewallService implements KsS5Firewall {

  /** Câu hỏi xác nhận / so sánh (so trên chữ không dấu, chữ thường). */
  private static final Pattern CONFIRM_Q =
      Pattern.compile(
          "co phai|phai khong|dung khong|dung chua|dung ko|chinh xac khong|co dung|kiem tra giup"
              + "|xac nhan|lon hon|nho hon|bang .{1,40} (khong|ko|a)\\b|is it|right\\?|correct");

  /** Câu hỏi có một giá trị ứng viên: chữ số, căn, phân số, "can", "sqrt", chữ a nhân hệ số. */
  private static final Pattern VALUE =
      Pattern.compile("\\d|√|sqrt|\\bcan\\s*\\d|\\bcan (hai|ba|sau)\\b|π|\\bpi\\b");

  /** Câu trả lời mở đầu bằng xác nhận / phủ nhận. */
  private static final Pattern CONFIRM_HEAD =
      Pattern.compile(
          "^\\W*(dung|dung roi|chinh xac|phai|khong phai|khong|sai|chua dung|yes|no|vang|da dung"
              + "|em lam dung|ket qua dung|ket qua sai)\\b");

  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

  private final AnswerGuardApi guard;

  public KsS5FirewallService(AnswerGuardApi guard) {
    this.guard = guard;
  }

  @Override
  public String preCheck(String question, Locked lk) {
    if (lk == null || question == null) {
      return null;
    }
    if (valueRule(question, lk) != null) {
      return "KS-4";
    }
    String q = plain(question);
    return CONFIRM_Q.matcher(q).find() && VALUE.matcher(q).find() ? "KS-4" : null;
  }

  @Override
  public String check(String question, String reply, Locked lk, List<String> secrets) {
    String r = reply == null ? "" : reply;
    if (secrets != null) {
      String rn = squash(r);
      for (String s : secrets) {
        if (s == null) {
          continue;
        }
        for (String line : s.split("\\R")) {
          String sn = squash(line);
          if (sn.length() >= 10 && rn.contains(sn)) {
            return "private_content";
          }
        }
      }
    }
    if (lk == null) {
      return null;
    }
    String v = valueRule(r, lk);
    if (v != null) {
      return v;
    }
    String q = question == null ? "" : plain(question);
    if (VALUE.matcher(q).find() && CONFIRM_HEAD.matcher(plain(r)).find()) {
      return "KS-4";
    }
    return null;
  }

  @Override
  public String leaks(String text, Locked lk) {
    return lk == null ? null : valueRule(text, lk);
  }

  private String valueRule(String text, Locked lk) {
    if (text == null || text.isBlank()) {
      return null;
    }
    String rule = guard.leakRule(text, lk.finalAnswer(), lk.stepBodies(), forms(lk));
    if (rule == null) {
      return null;
    }
    return switch (rule) {
      case "R2" -> "KS-1";
      case "R3" -> "KS-2";
      case "R4" -> "KS-3";
      default -> "KS-1";
    };
  }

  private static final Pattern ROOT = Pattern.compile("√\\(?(\\d+)\\)?");
  private static final Pattern LEAD_PARAM = Pattern.compile("^[a-zA-Z](?=[√\\d(])");

  /**
   * Dạng tương đương thêm cho đáp số có căn và tham số (BR-S5E3-15; F-S5-050 "a√6/3"): bỏ tham số
   * ("√6/3"), viết sqrt, viết "căn", "trên"/"chia". Bộ kiểm chứng chưa tính answer_forms cho đề
   * chặng 5 nên bộ kiểm tự thêm (lệch về phía chặn).
   */
  static List<String> forms(Locked lk) {
    java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
    if (lk.forms() != null) {
      out.addAll(lk.forms());
    }
    String ans = lk.finalAnswer();
    if (ans != null) {
      for (String part : ans.split(";")) {
        String v = part.contains("=") ? part.substring(part.lastIndexOf('=') + 1) : part;
        v = v.strip();
        if (!v.contains("√")) {
          continue;
        }
        for (String w : List.of(v, LEAD_PARAM.matcher(v).replaceFirst(""))) {
          out.add(w);
          out.add(ROOT.matcher(w).replaceAll("sqrt($1)"));
          out.add(ROOT.matcher(w).replaceAll("sqrt$1"));
          String can = ROOT.matcher(w).replaceAll("căn $1");
          out.add(can);
          out.add(can.replace("/", " trên "));
          out.add(can.replace("/", " chia "));
          out.add(ROOT.matcher(w).replaceAll("can $1"));
          out.add(ROOT.matcher(w).replaceAll("can $1").replace("/", " tren "));
        }
      }
    }
    out.removeIf(s -> s.strip().length() < 3);
    return List.copyOf(out);
  }

  static String plain(String s) {
    String n =
        Normalizer.normalize(s.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
    return n.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").strip();
  }

  /** Chữ không dấu, chỉ [a-z0-9] (so chuỗi bí mật không phụ thuộc dấu câu, khoảng trắng). */
  static String squash(String s) {
    return NON_ALNUM.matcher(plain(s)).replaceAll("");
  }
}
