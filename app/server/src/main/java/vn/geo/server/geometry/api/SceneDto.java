package vn.geo.server.geometry.api;

import java.util.List;
import java.util.Map;

/**
 * HỢP ĐỒNG DỮ LIỆU CẢNH v1 (E2 sở hữu; E3 vẽ và thao tác, E4 đồng bộ lời giải). Tài liệu đầy đủ, ví
 * dụ JSON và quy tắc đổi: {@code app/docs/SCENE-CONTRACT.md}; kiểu TypeScript tương ứng: {@code
 * app/web/src/shared/scene/types.ts}.
 *
 * <p>Quy tắc: chỉ THÊM trường tùy chọn, không đổi tên, không đổi nghĩa, không bỏ trường (đổi phá vỡ
 * thì tăng {@link #SCHEMA_VERSION}). Tọa độ là dữ kiện của đề (D-GEO-154), KHÔNG BAO GIỜ chứa giá
 * trị tính của báo cáo kiểm chứng hay đáp số (US-GEO-203-FC5).
 *
 * @param schemaVersion phiên bản lược đồ cảnh (hiện 1)
 * @param figureType "3d" | "2d" | "none" (SPEC-technical FR-SOL-2); MVP chỉ dựng "3d"
 * @param sceneId id bản dựng {@code scene_build} đã lưu; rỗng khi là bản xem trước hình mẫu
 * @param coordSource "engine" (engine tính từ ràng buộc, BR-GEO-06) | "ai" (đề lạ, BR-GEO-08)
 * @param template mã bộ dựng của engine (vd. "cone", "pyramid_square_sa"); rỗng khi coordSource =
 *     ai
 * @param solidKind pyramid | prism | box | cube | cone | cylinder | sphere; rỗng khi AI dựng hình
 *     lạ
 * @param unit đơn vị hiện cạnh số đo: "a" (theo tham số a), "cm", hoặc "" (không đơn vị)
 * @param params tham số đã dùng để dựng (theo thứ tự khai báo của bộ dựng)
 * @param points đỉnh có tên, tọa độ thực, hệ trục toán học z hướng lên
 * @param edges cạnh nét của hình: cặp tên đỉnh [P, Q]
 * @param faces mặt phẳng của khối: danh sách tên đỉnh theo thứ tự vòng (đa giác lồi)
 * @param surfaces mặt cong (nón, trụ, cầu) kèm thông số hình học
 * @param chips thẻ ràng buộc độ dài hiện ở góc dưới trái khung hình (BR-GEO-130)
 * @param hiddenMeasures đại lượng công cụ đo phải ẩn giá trị trước giờ mở, dạng "ang:S-D-A" (góc
 *     tại D giữa DS, DA) hoặc "len:O-O′" (độ dài OO′), như Design/prototype/fig3d.js
 * @param axes hệ trục Oxyz bật được trên mọi hình (US-GEO-205-SC1)
 * @param verification nhãn máy và cảnh báo phải hiện cùng hình
 */
public record SceneDto(
    int schemaVersion,
    String figureType,
    String sceneId,
    String coordSource,
    String template,
    String solidKind,
    String unit,
    Map<String, Double> params,
    List<ScenePoint> points,
    List<List<String>> edges,
    List<List<String>> faces,
    List<SceneSurface> surfaces,
    List<SceneChip> chips,
    List<String> hiddenMeasures,
    SceneAxes axes,
    SceneVerification verification) {

  public static final int SCHEMA_VERSION = 1;

  /** Một đỉnh có tên. */
  public record ScenePoint(String name, double x, double y, double z) {}

  /**
   * Mặt cong. kind = "cone": đáy tâm {@code center}, bán kính {@code radius}, pháp tuyến đáy {@code
   * axis}, đỉnh {@code apex}. kind = "cylinder": đáy dưới tâm {@code center}, bán kính, trục {@code
   * axis} (véc tơ đơn vị), chiều cao {@code height}. kind = "sphere": tâm, bán kính.
   */
  public record SceneSurface(
      String kind,
      List<Double> center,
      double radius,
      List<Double> axis,
      List<Double> apex,
      Double height) {}

  /** Thẻ ràng buộc, ví dụ text "SA = a", points ["S", "A"]. */
  public record SceneChip(String text, List<String> points) {}

  /** Hệ trục: gốc tại gốc tọa độ của đề (không phải tâm hình), trục "lên" của dữ liệu. */
  public record SceneAxes(boolean available, List<Double> origin, String up) {}

  /**
   * Nhãn máy (BR-GEO-135, 136): status "verified" | "needs_review"; label đúng chuỗi "Đã kiểm
   * chứng" | "Cần xem lại". warning khác rỗng khi hình do AI dựng (MSG-GEO-16), luôn dismissible =
   * false.
   */
  public record SceneVerification(String status, String label, SceneWarning warning) {}

  /** Cảnh báo cố định trên khung hình. */
  public record SceneWarning(String code, String text, boolean dismissible) {}
}
