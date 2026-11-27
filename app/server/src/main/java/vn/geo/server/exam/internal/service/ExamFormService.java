package vn.geo.server.exam.internal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.security.Role;
import vn.geo.server.common.security.Roles;
import vn.geo.server.exam.internal.domain.DatasetValidator;
import vn.geo.server.exam.internal.domain.ExamModel;
import vn.geo.server.exam.internal.domain.ExamModel.Cell;
import vn.geo.server.exam.internal.domain.ExamModel.Item;
import vn.geo.server.exam.internal.domain.ExamModel.Matrix;
import vn.geo.server.exam.internal.domain.ExamModel.Opt;
import vn.geo.server.exam.internal.domain.ExamModel.Part;
import vn.geo.server.exam.internal.domain.MatrixShaper;
import vn.geo.server.exam.internal.repo.ExamBankRepository.MatrixRow;

/**
 * Form ra đề (US-S5E2-02): danh mục loại đề, xem trước bảng ô sau khi áp trường form, kiểm tổng
 * điểm = 10 (MSG-S5-10), tổng thời gian dự kiến ≤ thời gian làm bài (MSG-S5-11), số mã đề 1–8
 * (MSG-S5-13, QS5-07).
 */
@Service
public class ExamFormService {

  public static final String MSG_10 = "Tổng điểm phải bằng 10.";
  public static final String MSG_11 = "Tổng thời gian dự kiến của các câu vượt thời gian làm bài.";
  public static final String MSG_13 = "Số mã đề từ 1 đến 8.";
  public static final int DEFAULT_VARIANTS = 4;

  public record FormInput(
      String matrixCode,
      UUID classId,
      List<String> chapters,
      Double minutes,
      List<MatrixShaper.PartInput> parts,
      Map<String, Double> levels,
      Integer variantCount,
      Long seed,
      String name) {

    public FormInput withSeed(long s) {
      return new FormInput(
          matrixCode, classId, chapters, minutes, parts, levels, variantCount, s, name);
    }
  }

  public record Shaped(Matrix matrix, int variantCount, double estimatedMinutes) {}

  private final ExamCatalogService catalog;
  private final JsonMapper mapper;

  public ExamFormService(ExamCatalogService catalog, JsonMapper mapper) {
    this.catalog = catalog;
    this.mapper = mapper;
  }

  /** GET /api/exam/catalog: các loại đề (ma trận chuẩn) kèm giá trị mặc định của form. */
  public ObjectNode catalog() {
    Roles.require(Role.TEACHER);
    ExamCatalogService.Snapshot s = catalog.snapshot();
    ObjectNode out = mapper.createObjectNode();
    ArrayNode arr = out.putArray("items");
    for (MatrixRow r : s.matrices()) {
      Matrix mt = Matrix.of(s.matrixBodies().get(r.code()));
      ObjectNode n = arr.addObject();
      n.put("code", r.code());
      n.put("id", r.id().toString());
      n.put("name", r.name());
      n.put("kind", r.kind());
      n.put("grade", r.grade());
      n.put("minutes", mt.minutes());
      n.put("totalPoints", mt.totalPoints());
      n.put("defaultVariantCount", DEFAULT_VARIANTS);
      n.set("parts", parts(mt));
      n.set("levels", levels(mt));
      n.set("chapters", chapters(mt, s));
      n.put("fixed", "Cấu trúc phần, tổng điểm 10, đảo câu và phương án cố định");
    }
    return out;
  }

  /** POST /api/exams/preview: bảng ô sau khi áp form (không lắp đề). */
  public ObjectNode preview(FormInput in) {
    Roles.require(Role.TEACHER);
    Shaped sh = shape(in);
    Matrix mt = sh.matrix();
    ObjectNode out = mapper.createObjectNode();
    out.put("matrixCode", mt.code());
    out.put("name", mt.name());
    out.put("minutes", mt.minutes());
    out.put("totalPoints", Math.round(mt.totalPoints() * 1e6) / 1e6);
    out.put("estimatedMinutes", Math.round(sh.estimatedMinutes() * 100) / 100.0);
    out.put("variantCount", sh.variantCount());
    out.set("parts", parts(mt));
    out.set("levels", levels(mt));
    ArrayNode cells = out.putArray("cells");
    for (Cell c : mt.cells()) {
      ObjectNode n = cells.addObject();
      n.put("id", c.id()).put("part", c.part()).put("type", c.type()).put("level", c.level());
      n.put("points", c.points()).put("kind", c.kind()).put("spec", c.spec());
      ArrayNode ts = n.putArray("topics");
      ArrayNode ch = n.putArray("chapters");
      for (Opt o : c.opts()) {
        ts.add(o.topic());
        String cid = MatrixShaper.chapterOf(o.topic());
        boolean seen = false;
        for (JsonNode x : ch) {
          seen |= x.asString().equals(cid);
        }
        if (!seen) {
          ch.add(cid);
        }
      }
    }
    return out;
  }

  /** Áp form lên ma trận chuẩn và kiểm; lỗi → VALIDATION_FAILED gắn ô nhập. */
  public Shaped shape(FormInput in) {
    ExamCatalogService.Snapshot s = catalog.snapshot();
    JsonNode body = in.matrixCode() == null ? null : s.matrixBodies().get(in.matrixCode());
    if (body == null) {
      throw AppException.invalidField("matrixCode", "Chưa chọn loại đề.");
    }
    int n = in.variantCount() == null ? DEFAULT_VARIANTS : in.variantCount();
    if (n < 1 || n > 8) {
      throw AppException.invalidField("variantCount", MSG_13);
    }
    if (in.minutes() != null && in.minutes() <= 0) {
      throw AppException.invalidField("minutes", MSG_11);
    }
    Matrix mt =
        MatrixShaper.shape(
            Matrix.of(body), s.topics(), in.minutes(), in.parts(), in.chapters(), in.levels());
    if (Math.abs(mt.totalPoints() - DatasetValidator.TOTAL_POINTS) > 1e-6) {
      throw AppException.invalidField("parts", MSG_10);
    }
    double est = estimate(mt, s.bank());
    if (est > mt.minutes() + 1e-9) {
      throw AppException.invalidField("minutes", MSG_11);
    }
    return new Shaped(mt, n, est);
  }

  /**
   * Thời gian dự kiến tối thiểu: mỗi ô lấy câu ngắn nhất hợp ô (không có câu thì theo loại + mức).
   */
  static double estimate(Matrix mt, List<Item> bank) {
    Map<String, Double> byTypeLevel = new HashMap<>();
    for (Item it : bank) {
      byTypeLevel.merge(it.type() + "|" + it.level(), it.time(), Math::min);
    }
    double t = 0;
    for (Cell c : mt.cells()) {
      double best = Double.MAX_VALUE;
      for (Opt o : c.opts()) {
        for (Item it : bank) {
          if (it.time() < best && ExamModel.fits(it, c, o)) {
            best = it.time();
          }
        }
      }
      if (best == Double.MAX_VALUE) {
        best = byTypeLevel.getOrDefault(c.type() + "|" + c.level(), 0.0);
      }
      t += best * c.count();
    }
    return t;
  }

  private ArrayNode parts(Matrix mt) {
    ArrayNode a = mapper.createArrayNode();
    for (Part p : mt.parts()) {
      a.addObject()
          .put("id", p.id())
          .put("name", p.name())
          .put("type", p.type())
          .put("count", p.count())
          .put("points", p.points());
    }
    return a;
  }

  private ObjectNode levels(Matrix mt) {
    ObjectNode o = mapper.createObjectNode();
    for (String l : MatrixShaper.LEVELS) {
      long k = mt.cells().stream().filter(c -> c.level().equals(l)).count();
      o.put(l, mt.cells().isEmpty() ? 0 : Math.round(1000.0 * k / mt.cells().size()) / 1000.0);
    }
    return o;
  }

  private ArrayNode chapters(Matrix mt, ExamCatalogService.Snapshot s) {
    ArrayNode a = mapper.createArrayNode();
    List<String> inMatrix = new ArrayList<>();
    mt.cells().forEach(c -> c.opts().forEach(o -> inMatrix.add(MatrixShaper.chapterOf(o.topic()))));
    for (MatrixShaper.Chapter ch : MatrixShaper.chapters(mt, s.topics())) {
      ObjectNode n = a.addObject();
      n.put("id", ch.id()).put("name", ch.name()).put("selected", inMatrix.contains(ch.id()));
      ArrayNode ts = n.putArray("topics");
      ch.topics().forEach(ts::add);
    }
    return a;
  }
}
