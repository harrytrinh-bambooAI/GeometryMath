package vn.geo.server.aigateway.internal;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;

/**
 * Máy chủ HTTP giả của Claude Messages API cho kiểm thử (không gọi mạng thật): trả lần lượt các
 * phản hồi đã xếp hàng, ghi lại mọi yêu cầu (tiêu đề + thân).
 */
public final class FakeAnthropic implements AutoCloseable {

  /** Một phản hồi xếp hàng. */
  public record Reply(int status, String body, Map<String, String> headers, long delayMs) {
    public static Reply ok(String body) {
      return new Reply(200, body, Map.of(), 0);
    }

    public static Reply status(int status, String errorType) {
      return new Reply(
          status, "{\"type\":\"error\",\"error\":{\"type\":\"" + errorType + "\"}}", Map.of(), 0);
    }
  }

  /** Một yêu cầu đã nhận. */
  public record Seen(Map<String, List<String>> headers, String body) {
    public String header(String name) {
      for (var e : headers.entrySet()) {
        if (e.getKey().equalsIgnoreCase(name)) {
          return e.getValue().isEmpty() ? null : e.getValue().get(0);
        }
      }
      return null;
    }
  }

  private final HttpServer server;
  private final ConcurrentLinkedDeque<Reply> replies = new ConcurrentLinkedDeque<>();
  private final List<Seen> seen = Collections.synchronizedList(new ArrayList<>());
  private volatile Reply fallback = Reply.status(500, "api_error");

  public FakeAnthropic() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    server.createContext(
        "/v1/messages",
        ex -> {
          byte[] in = ex.getRequestBody().readAllBytes();
          seen.add(
              new Seen(Map.copyOf(ex.getRequestHeaders()), new String(in, StandardCharsets.UTF_8)));
          Reply r = replies.pollFirst();
          if (r == null) {
            r = fallback;
          }
          try {
            if (r.delayMs() > 0) {
              Thread.sleep(r.delayMs());
            }
            byte[] out = r.body().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("content-type", "application/json");
            r.headers().forEach((k, v) -> ex.getResponseHeaders().add(k, v));
            ex.sendResponseHeaders(r.status(), out.length);
            try (OutputStream os = ex.getResponseBody()) {
              os.write(out);
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } catch (IOException e) {
            // máy khách đã đóng kết nối (hết giờ)
          } finally {
            ex.close();
          }
        });
    server.start();
  }

  public String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  public FakeAnthropic enqueue(Reply... rs) {
    replies.addAll(List.of(rs));
    return this;
  }

  public void fallback(Reply r) {
    fallback = r;
  }

  public void reset() {
    replies.clear();
    seen.clear();
    fallback = Reply.status(500, "api_error");
  }

  public List<Seen> seen() {
    return List.copyOf(seen);
  }

  /** Thân phản hồi thành công của Messages API với một khối text và usage. */
  public static String message(String text, int in, int out, String stopReason) {
    String esc =
        text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t");
    return "{\"id\":\"msg_gia\",\"type\":\"message\",\"role\":\"assistant\",\"model\":\"claude-sonnet-5-5\","
        + "\"content\":[{\"type\":\"text\",\"text\":\""
        + esc
        + "\"}],\"stop_reason\":\""
        + stopReason
        + "\",\"usage\":{\"input_tokens\":"
        + in
        + ",\"output_tokens\":"
        + out
        + ",\"cache_read_input_tokens\":0,\"cache_creation_input_tokens\":0}}";
  }

  public static String message(String text, int in, int out) {
    return message(text, in, out, "end_turn");
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
