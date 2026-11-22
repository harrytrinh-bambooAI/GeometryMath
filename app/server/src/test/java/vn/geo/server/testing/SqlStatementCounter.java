package vn.geo.server.testing;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.slf4j.MDC;
import vn.geo.server.common.web.RequestIdFilter;

/**
 * Đếm câu lệnh SQL theo từng yêu cầu HTTP (NFR-GEO-009: đọc ≤ 4, ghi ≤ 6, không phụ thuộc số dòng).
 *
 * <p>Gắn vào DataSource bằng datasource-proxy ({@link StatementCountingConfig}). Mỗi câu lệnh được
 * ghi theo mã yêu cầu trong MDC ({@code requestId}, do {@code RequestIdFilter} đặt trên luồng xử lý
 * yêu cầu), nên kiểm thử gửi {@code X-Request-Id} tự chọn rồi hỏi {@link #count(String)}; không lẫn
 * với câu lệnh của yêu cầu khác chạy song song. Câu lệnh ngoài yêu cầu HTTP (Flyway, dữ liệu mẫu do
 * kiểm thử chèn) ghi vào khóa {@link #NO_REQUEST}. Một lô (batch) tính là số câu lệnh trong lô.
 */
public final class SqlStatementCounter implements QueryExecutionListener {

  public static final String NO_REQUEST = "<none>";

  private final Map<String, AtomicInteger> byRequest = new ConcurrentHashMap<>();
  private final Map<String, List<String>> lastSql = new ConcurrentHashMap<>();

  @Override
  public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {}

  @Override
  public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    String rid = MDC.get(RequestIdFilter.MDC_KEY);
    String key = rid == null ? NO_REQUEST : rid;
    int n = execInfo.isBatch() ? Math.max(1, execInfo.getBatchSize()) : queryInfoList.size();
    byRequest.computeIfAbsent(key, k -> new AtomicInteger()).addAndGet(n);
    if (rid != null) {
      lastSql
          .computeIfAbsent(key, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
          .addAll(queryInfoList.stream().map(QueryInfo::getQuery).toList());
    }
  }

  /** Số câu lệnh đã chạy cho mã yêu cầu này (0 nếu không có). */
  public int count(String requestId) {
    AtomicInteger c = byRequest.get(requestId);
    return c == null ? 0 : c.get();
  }

  /** Các câu SQL của yêu cầu, để in khi kiểm thử đỏ. */
  public List<String> statements(String requestId) {
    return List.copyOf(lastSql.getOrDefault(requestId, List.of()));
  }

  public void reset() {
    byRequest.clear();
    lastSql.clear();
  }
}
