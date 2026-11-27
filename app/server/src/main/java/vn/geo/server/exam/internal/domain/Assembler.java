package vn.geo.server.exam.internal.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.exam.internal.domain.ExamModel.Cell;
import vn.geo.server.exam.internal.domain.ExamModel.Item;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.ExamModel.Opt;
import vn.geo.server.exam.internal.domain.ExamModel.Part;

/**
 * Bộ lắp đề viết lại từ {@code materials/de-thi/lap_de.py} (QS5-06, US-S5E2-03): mỗi mã bốc chủ đề
 * cho ô xoay vòng theo trọng số → điền từng ô (ưu tiên câu chưa dùng ở mã trước, khác họ bối cảnh)
 * → kiểm 6 ràng buộc ({@link VariantChecker}); mã sau lệch độ khó mã đầu quá 2,5 % thì bốc lại, để
 * cả bộ lệch ≤ 5 %. Bốc lại tối đa 600 lần/mã rồi báo ô thiếu. Tái lập theo seed: một {@link
 * Random} gieo bằng seed, duyệt theo thứ tự cố định (BR-S5E2-07). Không gọi AI (BR-S5E2-08).
 */
public final class Assembler {

  public static final int MAX_DRAWS = 600;
  private static final List<String> LETTERS = List.of("A", "B", "C", "D");
  private static final JsonNodeFactory F = JsonNodeFactory.instance;

  /** Ngân hàng không đủ câu cho ô {@code cell} (MSG-S5-12). */
  public static final class InsufficientBankException extends RuntimeException {
    private final String cell;

    public InsufficientBankException(String cell) {
      super("Ngân hàng chưa đủ câu cho ô " + cell);
      this.cell = cell;
    }

    public String cell() {
      return cell;
    }
  }

  public record Result(List<ObjectNode> variants, ObjectNode report) {}

  private final Matrix mt;
  private final Map<String, List<List<Item>>> cand = new HashMap<>();

  public Assembler(Matrix mt, List<Item> bank) {
    this.mt = mt;
    for (Cell c : mt.cells()) {
      List<List<Item>> perOpt = new ArrayList<>();
      for (Opt o : c.opts()) {
        perOpt.add(bank.stream().filter(it -> ExamModel.fits(it, c, o)).toList());
      }
      cand.put(c.id(), perOpt);
    }
  }

  public Result assemble(int count, long seed) {
    Random rng = new Random(seed);
    List<ObjectNode> des = new ArrayList<>();
    Set<String> used = new HashSet<>();
    Double ref = null;
    for (int k = 0; k < count; k++) {
      ObjectNode de = one(rng, used, ref);
      de.put("ma_de", String.valueOf(101 + k));
      de.put("seed", seed);
      if (ref == null) {
        ref = de.path("do_kho_du_kien").doubleValue();
      }
      de.path("cau").forEach(q -> used.add(q.path("id_cau").asString()));
      des.add(de);
    }
    return new Result(des, report(des, seed));
  }

  public ObjectNode report(List<ObjectNode> des, long seed) {
    List<Double> dks = des.stream().map(d -> d.path("do_kho_du_kien").doubleValue()).toList();
    double lech = VariantChecker.spread(dks);
    ObjectNode r = F.objectNode();
    r.put("ma_tran", mt.code());
    r.put("so_ma", des.size());
    r.put("seed", seed);
    ArrayNode dk = r.putArray("do_kho");
    dks.forEach(dk::add);
    r.put("lech_do_kho", Math.round(lech * 1e4) / 1e4);
    r.put("dat_do_kho_5pt", lech <= 0.05);
    ObjectNode rb = r.putObject("rang_buoc_tung_ma");
    for (ObjectNode d : des) {
      ObjectNode m = rb.putObject(d.path("ma_de").asString());
      d.path("rang_buoc")
          .properties()
          .forEach(e -> m.put(e.getKey(), e.getValue().path("dat").asBoolean()));
    }
    return r;
  }

  private ObjectNode one(Random rng, Set<String> used, Double ref) {
    Map<String, Integer> fail = new LinkedHashMap<>();
    for (int lan = 0; lan < MAX_DRAWS; lan++) {
      Map<String, Integer> chon = new HashMap<>();
      for (Cell c : mt.cells()) {
        List<Integer> ok = new ArrayList<>();
        for (int i = 0; i < c.opts().size(); i++) {
          if (!cand.get(c.id()).get(i).isEmpty()) {
            ok.add(i);
          }
        }
        if (ok.isEmpty()) {
          throw new InsufficientBankException(c.id());
        }
        chon.put(c.id(), weighted(rng, c, ok));
      }
      List<Cell> order = new ArrayList<>(mt.cells());
      order.sort(Comparator.comparingInt(c -> cand.get(c.id()).get(chon.get(c.id())).size()));
      Set<String> dang = new HashSet<>();
      Set<String> ctx = new HashSet<>();
      Set<String> mau = new HashSet<>();
      Set<String> ho = new HashSet<>();
      Map<String, Item> picked = new HashMap<>();
      String failed = null;
      for (Cell c : order) {
        List<Item> pool = new ArrayList<>();
        for (Item it : cand.get(c.id()).get(chon.get(c.id()))) {
          if (!dang.contains(it.dang())
              && !ctx.contains(it.ctx())
              && !mau.contains(it.template())) {
            pool.add(it);
          }
        }
        List<Item> moi = pool.stream().filter(it -> !used.contains(it.id())).toList();
        pool = moi.isEmpty() ? pool : moi;
        List<Item> khacHo = pool.stream().filter(it -> !ho.contains(it.family())).toList();
        pool = khacHo.isEmpty() ? pool : khacHo;
        if (pool.isEmpty()) {
          failed = c.id();
          break;
        }
        Item it = pool.get(rng.nextInt(pool.size()));
        picked.put(c.id(), it);
        dang.add(it.dang());
        ctx.add(it.ctx());
        mau.add(it.template());
        ho.add(it.family());
      }
      if (failed != null) {
        fail.merge(failed, 1, Integer::sum);
        continue;
      }
      ObjectNode de = build(picked, chon, rng);
      Map<String, VariantChecker.Check> r = VariantChecker.check(de, mt);
      if (!VariantChecker.allOk(r)) {
        continue;
      }
      double dk = VariantChecker.difficulty(de.path("cau"));
      if (ref != null && Math.abs(dk - ref) / ref > 0.025) {
        continue;
      }
      de.put("do_kho_du_kien", Math.round(dk * 1e4) / 1e4);
      ObjectNode rb = de.putObject("rang_buoc");
      r.forEach((k, v) -> rb.putObject(k).put("dat", v.ok()).put("chi_tiet", v.detail()));
      de.put("so_lan_boc", lan + 1);
      return de;
    }
    String worst =
        fail.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(mt.cells().get(0).id());
    throw new InsufficientBankException(worst);
  }

  private static int weighted(Random rng, Cell c, List<Integer> ok) {
    double total = 0;
    for (int i : ok) {
      total += c.opts().get(i).weight();
    }
    double x = rng.nextDouble() * total;
    for (int i : ok) {
      x -= c.opts().get(i).weight();
      if (x < 0) {
        return i;
      }
    }
    return ok.get(ok.size() - 1);
  }

  private ObjectNode build(Map<String, Item> picked, Map<String, Integer> chon, Random rng) {
    List<String> tn =
        mt.cells().stream().filter(c -> "tn4".equals(c.type())).map(Cell::id).toList();
    List<String> letters = new ArrayList<>();
    for (int i = 0; i < tn.size() / 4; i++) {
      letters.addAll(LETTERS);
    }
    List<String> extra = new ArrayList<>(LETTERS);
    Collections.shuffle(extra, rng);
    letters.addAll(extra.subList(0, tn.size() % 4));
    Collections.shuffle(letters, rng);
    Map<String, String> pos = new HashMap<>();
    for (int i = 0; i < tn.size(); i++) {
      pos.put(tn.get(i), letters.get(i));
    }
    ObjectNode de = F.objectNode();
    de.put("ma_tran", mt.code());
    de.put("ten", mt.name());
    de.put("thoi_gian_phut", mt.minutes());
    de.put("tong_diem", mt.totalPoints());
    ArrayNode phan = de.putArray("phan");
    ArrayNode qs = de.putArray("cau");
    int so = 0;
    for (Part p : mt.parts()) {
      phan.addObject()
          .put("id", p.id())
          .put("ten", p.name())
          .put("loai_cau", p.type())
          .put("so_cau", p.count())
          .put("diem_moi_cau", p.points());
      List<Cell> cs =
          new ArrayList<>(mt.cells().stream().filter(c -> c.part().equals(p.id())).toList());
      Collections.shuffle(cs, rng);
      for (Cell c : cs) {
        so++;
        Opt o = c.opts().get(chon.get(c.id()));
        qs.add(question(so, c, o, picked.get(c.id()), pos.get(c.id()), rng));
      }
    }
    ObjectNode xv = de.putObject("chu_de_xoay_vong");
    for (Cell c : mt.cells()) {
      if ("xoay-vong".equals(c.kind())) {
        xv.put(c.id(), c.opts().get(chon.get(c.id())).topic());
      }
    }
    return de;
  }

  /** Một câu của mã đề (khóa giống lap_de.py để check.py kiểm độc lập được). */
  public static ObjectNode question(int so, Cell c, Opt o, Item it, String letter, Random rng) {
    ObjectNode b = it.body();
    ObjectNode q = F.objectNode();
    q.put("so", so).put("phan", c.part()).put("o", c.id()).put("loai_o", c.kind());
    q.put("id_cau", it.id()).put("mau_cau", it.template()).put("chu_de", it.topic());
    q.put("dang", it.dang()).put("dang_tx", it.dangTx());
    ArrayNode y = q.putArray("yccd");
    Set<String> ys = new TreeSet<>(it.yccd());
    if (!o.yccd().isEmpty()) {
      ys.retainAll(o.yccd());
    }
    ys.forEach(y::add);
    q.put("muc_do", it.level()).put("loai_cau", it.type());
    q.set("nang_luc", b.path("nang_luc").deepCopy());
    q.put("diem", c.points()).put("thoi_gian_phut", it.time()).put("do_kho", it.difficulty());
    q.put("boi_canh_key", it.ctx()).put("de_bai", b.path("de_bai").asString());
    q.put("dap_so_hien", b.path("dap_so").path("gia_tri").asString(""));
    q.set("nguon", b.path("nguon").deepCopy());
    q.set("dap_so_engine", b.path("dap_so_engine").deepCopy());
    q.put("nhan_may", "da-kiem-chung");
    switch (it.type()) {
      case "tn4" -> {
        List<ObjectNode> sai = new ArrayList<>();
        for (int i = 1; i < b.path("lua_chon").size(); i++) {
          sai.add((ObjectNode) b.path("lua_chon").get(i));
        }
        Collections.shuffle(sai, rng);
        ObjectNode pa = q.putObject("phuong_an");
        ArrayNode nh = F.arrayNode();
        int k = 0;
        for (String x : LETTERS) {
          if (x.equals(letter)) {
            pa.put(x, b.path("lua_chon").get(0).path("noi_dung").asString());
          } else {
            ObjectNode s = sai.get(k++);
            pa.put(x, s.path("noi_dung").asString());
            nh.addObject()
                .put("phuong_an", x)
                .put("noi_dung", s.path("noi_dung").asString())
                .set("loi", s.path("loi").deepCopy());
          }
        }
        q.put("dap_an", letter);
        q.set("nhieu", nh);
      }
      case "dung-sai" -> {
        ArrayNode ya = q.putArray("y");
        for (var yy : b.path("y")) {
          ObjectNode n = ya.addObject();
          for (String key : List.of("nhan", "menh_de", "dung", "muc_do", "loi")) {
            if (yy.has(key)) {
              n.set(key, yy.get(key).deepCopy());
            }
          }
        }
        q.set("dap_an", b.path("dap_an").deepCopy());
        q.set("nhieu", b.path("nhieu").deepCopy());
      }
      case "tra-loi-ngan" -> q.set("dap_an", b.path("dap_an").deepCopy());
      default -> {
        double sum = 0;
        for (var st : b.path("huong_dan_cham")) {
          sum += st.path("diem").doubleValue();
        }
        double f = sum == 0 ? 0 : c.points() / sum;
        q.set("dap_an", b.path("dap_an").deepCopy());
        ArrayNode hd = q.putArray("huong_dan_cham");
        for (var st : b.path("huong_dan_cham")) {
          ObjectNode n = ((ObjectNode) st).deepCopy();
          n.put("diem", Math.round(st.path("diem").doubleValue() * f * 1e4) / 1e4);
          hd.add(n);
        }
      }
    }
    return q;
  }
}
