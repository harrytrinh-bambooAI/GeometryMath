package vn.geo.server.exam.internal.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.exam.internal.domain.Assembler.InsufficientBankException;
import vn.geo.server.exam.internal.domain.ExamModel.Cell;
import vn.geo.server.exam.internal.domain.ExamModel.Item;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.ExamModel.Opt;

/**
 * Thay một câu bằng câu khác cùng ô (BR-S5E2-09, US-S5E2-04): cùng chủ đề, dạng, mức độ, loại,
 * điểm; kiểm lại 6 ràng buộc của cả mã và độ khó bộ đề lệch ≤ 5 %; vi phạm thì bốc lại, hết 600
 * lượt (hoặc hết câu) thì giữ câu cũ và báo ô thiếu (MSG-S5-12). Câu TN4 giữ vị trí đáp án đúng
 * (giữ cân bằng A/B/C/D).
 */
public final class Replacer {

  private Replacer() {}

  public static ObjectNode replace(
      Matrix mt,
      List<Item> bank,
      ObjectNode variant,
      int so,
      List<Double> otherDifficulties,
      long seed,
      Predicate<Item> poolFilter) {
    ArrayNode qs = (ArrayNode) variant.path("cau");
    if (so < 1 || so > qs.size()) {
      throw new IllegalArgumentException("so");
    }
    JsonNode old = qs.get(so - 1);
    Cell cell =
        mt.cells().stream()
            .filter(c -> c.id().equals(old.path("o").asString()))
            .findFirst()
            .orElseThrow(() -> new InsufficientBankException(old.path("o").asString()));
    Set<String> dang = new HashSet<>();
    Set<String> ctx = new HashSet<>();
    Set<String> mau = new HashSet<>();
    for (JsonNode q : qs) {
      if (q != old) {
        dang.add(q.path("dang").asString());
        ctx.add(q.path("boi_canh_key").asString());
        mau.add(q.path("mau_cau").asString());
      }
    }
    String oldId = old.path("id_cau").asString();
    String dangTx = old.path("dang_tx").asString();
    List<Item> pool = new ArrayList<>();
    List<Opt> opts = new ArrayList<>();
    for (Item it : bank) {
      if (it.id().equals(oldId)
          || !it.topic().equals(old.path("chu_de").asString())
          || !it.dangTx().equals(dangTx)
          || dang.contains(it.dang())
          || ctx.contains(it.ctx())
          || mau.contains(it.template())
          || !poolFilter.test(it)) {
        continue;
      }
      Opt o =
          cell.opts().stream().filter(x -> ExamModel.fits(it, cell, x)).findFirst().orElse(null);
      if (o != null) {
        pool.add(it);
        opts.add(o);
      }
    }
    Random rng = new Random(seed);
    String letter = old.path("dap_an").asString();
    for (int lan = 0; lan < Assembler.MAX_DRAWS && !pool.isEmpty(); lan++) {
      int i = rng.nextInt(pool.size());
      ObjectNode q = Assembler.question(so, cell, opts.get(i), pool.get(i), letter, rng);
      ObjectNode next = variant.deepCopy();
      ((ArrayNode) next.get("cau")).set(so - 1, q);
      Map<String, VariantChecker.Check> r = VariantChecker.check(next, mt);
      double dk = VariantChecker.difficulty(next.path("cau"));
      List<Double> all = new ArrayList<>(otherDifficulties);
      all.add(dk);
      if (!VariantChecker.allOk(r) || VariantChecker.spread(all) > 0.05) {
        pool.remove(i);
        opts.remove(i);
        continue;
      }
      next.put("do_kho_du_kien", Math.round(dk * 1e4) / 1e4);
      ObjectNode rb = next.putObject("rang_buoc");
      r.forEach((k, v) -> rb.putObject(k).put("dat", v.ok()).put("chi_tiet", v.detail()));
      return next;
    }
    throw new InsufficientBankException(cell.id());
  }
}
