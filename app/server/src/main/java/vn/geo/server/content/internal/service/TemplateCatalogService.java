package vn.geo.server.content.internal.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.content.internal.repo.SceneRepository;
import vn.geo.server.geometry.api.Figure;
import vn.geo.server.geometry.api.GeometryEngine;
import vn.geo.server.geometry.api.TemplateDto;

/**
 * Danh mục hình mẫu (US-GEO-205): đọc từ {@code solid_template}, nạp có kiểm (BR-GEO-140: mỗi tham
 * số có giá trị mặc định lớn hơn 0; bộ dựng engine biết). Hình không hợp lệ không vào danh mục và
 * được báo bằng câu tiếng Việt (US-GEO-205-FC1). Nội dung hệ thống: mọi trường đọc được
 * (D-GEO-076).
 */
@Service
public class TemplateCatalogService {

  private static final Logger log = LoggerFactory.getLogger(TemplateCatalogService.class);

  /** Kết quả nạp: mã đã vào danh mục, lỗi từng hình. */
  public record LoadReport(List<String> loaded, List<String> errors) {}

  /** Hình mẫu đã kiểm, kèm mã bộ dựng của engine. */
  public record Template(TemplateDto dto, String builder) {}

  private final SceneRepository repo;
  private final GeometryEngine engine;
  private final JsonMapper mapper;

  public TemplateCatalogService(SceneRepository repo, GeometryEngine engine, JsonMapper mapper) {
    this.repo = repo;
    this.engine = engine;
    this.mapper = mapper;
  }

  /** Danh mục hợp lệ, theo thứ tự thẻ. */
  public List<Template> list() {
    List<Template> out = new ArrayList<>();
    for (SceneRepository.TemplateRow r : repo.templates()) {
      JsonNode spec = mapper.readTree(r.paramSpec());
      Optional<String> err = validate(r.name(), spec);
      if (err.isPresent()) {
        log.warn("bỏ hình mẫu không hợp lệ {}: {}", r.code(), err.get());
        continue;
      }
      out.add(toTemplate(r.code(), r.name(), r.solidKind(), spec));
    }
    out.sort(Comparator.comparingInt(t -> t.dto().order()));
    return out;
  }

  public Optional<Template> find(String code) {
    return list().stream().filter(t -> t.dto().code().equals(code)).findFirst();
  }

  /**
   * Nạp danh mục từ JSON: mảng {id, code, solid_kind, name, param_spec}. Hình lỗi không được ghi.
   */
  public LoadReport load(JsonNode items) {
    List<String> loaded = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    for (JsonNode it : items) {
      String name = it.path("name").asString("");
      String code = it.path("code").asString("");
      JsonNode spec = it.path("param_spec");
      Optional<String> err =
          name.isBlank() || code.isBlank()
              ? Optional.of((name.isBlank() ? code : name) + ": thiếu tên hoặc mã")
              : validate(name, spec);
      if (err.isEmpty()) {
        err = validateKind(name, it.path("solid_kind").asString(""), spec);
      }
      if (err.isPresent()) {
        errors.add(err.get());
        continue;
      }
      repo.insertTemplate(
          it.path("id").asInt(),
          code,
          it.path("solid_kind").asString(),
          name,
          mapper.writeValueAsString(spec));
      loaded.add(code);
    }
    return new LoadReport(List.copyOf(loaded), List.copyOf(errors));
  }

  /** Lỗi đầu tiên của một hình mẫu, hoặc rỗng nếu hợp lệ (BR-GEO-140). */
  Optional<String> validate(String name, JsonNode spec) {
    if (spec == null || !spec.isObject()) {
      return Optional.of(name + ": thiếu mô tả tham số");
    }
    if (!engine.knows(spec.path("builder").asString(null))) {
      return Optional.of(name + ": bộ dựng không biết");
    }
    JsonNode params = spec.path("params");
    if (!params.isArray() || params.isEmpty()) {
      return Optional.of(name + ": không có tham số");
    }
    for (JsonNode p : params) {
      String label = p.path("label").asString(p.path("key").asString("?"));
      JsonNode d = p.get("default");
      if (d == null || d.isNull() || !d.isNumber()) {
        return Optional.of(name + ": thiếu giá trị mặc định của " + label);
      }
      if (!(d.asDouble() > 0)) {
        return Optional.of(name + ": giá trị mặc định của " + label + " phải lớn hơn 0");
      }
    }
    // soát E2 #5: khóa tham số phải khớp đúng bộ dựng, và dựng thử bằng giá trị mặc định phải qua
    // kiểm ràng buộc
    String builder = spec.path("builder").asString();
    List<String> keys = new ArrayList<>();
    params.forEach(p -> keys.add(p.path("key").asString("")));
    List<String> need = engine.paramKeys(builder);
    if (keys.size() != need.size()
        || !new java.util.HashSet<>(keys).equals(new java.util.HashSet<>(need))) {
      return Optional.of(
          name + ": tham số " + keys + " không khớp bộ dựng " + builder + " " + need);
    }
    try {
      Map<String, Double> defaults = new LinkedHashMap<>();
      params.forEach(p -> defaults.put(p.path("key").asString(), p.path("default").asDouble()));
      Figure f = engine.build(builder, defaults);
      if (!engine.check(f.points(), f.constraints()).satisfied()) {
        return Optional.of(name + ": hình dựng bằng giá trị mặc định không qua kiểm ràng buộc");
      }
    } catch (RuntimeException e) {
      return Optional.of(name + ": không dựng được bằng giá trị mặc định (" + e.getMessage() + ")");
    }
    return Optional.empty();
  }

  /** Loại hình của danh mục khớp enum solid_kind (V10) và đúng loại của bộ dựng. */
  Optional<String> validateKind(String name, String kind, JsonNode spec) {
    if (!SelfStudyService.SOLIDS.contains(kind)) {
      return Optional.of(name + ": loại hình '" + kind + "' ngoài danh mục");
    }
    try {
      Map<String, Double> defaults = new LinkedHashMap<>();
      spec.path("params")
          .forEach(p -> defaults.put(p.path("key").asString(), p.path("default").asDouble()));
      String built = engine.build(spec.path("builder").asString(), defaults).solidKind();
      if (!kind.equals(built)) {
        return Optional.of(
            name + ": loại hình '" + kind + "' khác loại của bộ dựng '" + built + "'");
      }
    } catch (RuntimeException e) {
      return Optional.of(name + ": không dựng được bằng giá trị mặc định");
    }
    return Optional.empty();
  }

  private static Template toTemplate(String code, String name, String kind, JsonNode spec) {
    List<TemplateDto.Param> ps = new ArrayList<>();
    for (JsonNode p : spec.path("params")) {
      ps.add(
          new TemplateDto.Param(
              p.path("key").asString(), p.path("label").asString(), p.path("default").asDouble()));
    }
    return new Template(
        new TemplateDto(
            code,
            name,
            kind,
            spec.path("card").asBoolean(true),
            spec.path("order").asInt(99),
            List.copyOf(ps)),
        spec.path("builder").asString());
  }
}
