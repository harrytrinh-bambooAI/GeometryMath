package vn.geo.server.geometry.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Một hình mẫu của danh mục (US-GEO-205, bảng {@code solid_template}). Xem {@code
 * app/docs/SCENE-CONTRACT.md} §4.
 *
 * @param code mã hình mẫu (cũng là mã bộ dựng của engine)
 * @param name tên hiển thị ("Hình nón")
 * @param solidKind loại khối
 * @param card có thẻ ở mục "Hoặc chọn một hình mẫu" của màn Tự học
 * @param order thứ tự thẻ
 * @param params tham số, mỗi tham số có giá trị mặc định lớn hơn 0 (BR-GEO-140)
 */
public record TemplateDto(
    String code, String name, String solidKind, boolean card, int order, List<Param> params) {

  /** Tham số: khóa (dùng trong {@code params} của cảnh), nhãn ô nhập, giá trị mặc định. */
  public record Param(String key, String label, @JsonProperty("default") double defaultValue) {}
}
