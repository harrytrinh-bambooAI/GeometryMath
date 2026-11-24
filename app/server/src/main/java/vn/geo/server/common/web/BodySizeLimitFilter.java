package vn.geo.server.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.ErrorResponseWriter;

/**
 * Từ chối thân yêu cầu quá cỡ: theo Content-Length ngay, hoặc cắt giữa chừng khi truyền chunked.
 * Đường gửi ảnh (đọc ảnh đề) mang ảnh dạng base64 (to hơn ảnh 4/3): trần thân = cỡ base64 của một
 * ảnh {@code maxBodyBytes} cộng phần JSON; trần 1 MB của chính ảnh kiểm ở dịch vụ sau khi giải mã
 * (BR-GEO-171, FIX3).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class BodySizeLimitFilter extends OncePerRequestFilter {

  private final HttpProperties props;
  private final ErrorResponseWriter writer;

  public BodySizeLimitFilter(HttpProperties props, ErrorResponseWriter writer) {
    this.props = props;
    this.writer = writer;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    long limit = limitFor(request);
    if (request.getContentLengthLong() > limit) {
      writer.write(response, ErrorCode.PAYLOAD_TOO_LARGE);
      return;
    }
    chain.doFilter(new Limited(request, limit), response);
  }

  /** Đường nhận ảnh base64 trong thân JSON. */
  static final java.util.Set<String> IMAGE_PATHS =
      java.util.Set.of("/api/v1/student/self-study/ocr");

  long limitFor(HttpServletRequest request) {
    long base = props.maxBodyBytes();
    return IMAGE_PATHS.contains(request.getRequestURI()) ? (base + 2) / 3 * 4 + 16_384 : base;
  }

  private static final class Limited extends HttpServletRequestWrapper {
    private final long limit;

    Limited(HttpServletRequest request, long limit) {
      super(request);
      this.limit = limit;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      ServletInputStream in = super.getInputStream();
      return new ServletInputStream() {
        private long count;

        @Override
        public int read() throws IOException {
          int b = in.read();
          if (b >= 0 && ++count > limit) {
            throw new BodyTooLargeException(limit);
          }
          return b;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
          int n = in.read(buf, off, len);
          if (n > 0 && (count += n) > limit) {
            throw new BodyTooLargeException(limit);
          }
          return n;
        }

        @Override
        public boolean isFinished() {
          return in.isFinished();
        }

        @Override
        public boolean isReady() {
          return in.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
          in.setReadListener(listener);
        }
      };
    }
  }
}
