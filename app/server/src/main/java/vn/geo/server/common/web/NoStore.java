package vn.geo.server.common.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu endpoint trả dữ liệu nhạy cảm (lời giải, gợi ý, trạng thái khóa; D-GEO-145): mọi phản
 * hồi, kể cả phản hồi lỗi, mang {@code Cache-Control: no-store} để trình duyệt, proxy và CDN không
 * lưu. Đặt trên phương thức hoặc trên cả lớp controller. Do {@link NoStoreInterceptor} áp dụng.
 *
 * <p>Mã không qua controller (bộ lọc) gọi {@link CacheHeaders#noStore}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NoStore {}
