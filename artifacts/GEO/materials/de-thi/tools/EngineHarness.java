// Phiên bản: v1.0 · 27/11/2026 — gọi DefaultGeometryEngine thật (biên dịch từ app/server/src/main/java ra thư mục tạm, không sửa app/).
// Vào: tệp JSON [{"id":..., "spec":{template, params, unit, asked, ...}}]; ra: stdout JSON [{"id":..., "values":[...]}]
// Mỗi spec dựng tại a = 1 (build(spec, 1.0)) rồi measure từng đại lượng trong asked (góc tính theo độ).
import java.nio.file.Files;
import java.nio.file.Path;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine.AskedQuantity;
import vn.geo.server.geometry.api.GeometryEngine.ProblemSpec;
import vn.geo.server.geometry.internal.DefaultGeometryEngine;

public class EngineHarness {
  public static void main(String[] args) throws Exception {
    JsonMapper m = JsonMapper.builder().build();
    DefaultGeometryEngine eng = new DefaultGeometryEngine(m);
    JsonNode root = m.readTree(Files.readString(Path.of(args[0])));
    StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (JsonNode it : root) {
      ProblemSpec spec = eng.specFrom(it.get("spec"));
      Figure f = eng.build(spec, 1.0);
      StringBuilder vs = new StringBuilder();
      for (AskedQuantity q : spec.asked()) {
        if (vs.length() > 0) vs.append(",");
        vs.append(Double.toString(eng.measure(f, q)));
      }
      if (!first) sb.append(",\n");
      first = false;
      sb.append("{\"id\":\"").append(it.get("id").asString()).append("\",\"values\":[").append(vs).append("]}");
    }
    sb.append("]");
    System.out.println(sb);
  }
}
