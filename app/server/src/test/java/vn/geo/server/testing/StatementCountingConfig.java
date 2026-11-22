package vn.geo.server.testing;

import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Bọc DataSource của ứng dụng bằng datasource-proxy và gắn {@link SqlStatementCounter}. Chỉ dùng
 * trong kiểm thử tích hợp (import ở {@code AbstractIT}); không có ở mã chạy thật.
 */
@TestConfiguration(proxyBeanMethods = false)
public class StatementCountingConfig {

  @Bean
  public static SqlStatementCounter sqlStatementCounter() {
    return new SqlStatementCounter();
  }

  @Bean
  public static BeanPostProcessor statementCountingDataSourceWrapper(
      org.springframework.beans.factory.ObjectProvider<SqlStatementCounter> counter) {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource ds
            && !(bean instanceof net.ttddyy.dsproxy.support.ProxyDataSource)) {
          return ProxyDataSourceBuilder.create(ds)
              .name("geo-counted")
              .listener(counter.getObject())
              .build();
        }
        return bean;
      }
    };
  }
}
