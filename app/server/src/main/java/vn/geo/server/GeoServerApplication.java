package vn.geo.server;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Điểm khởi động. Mọi thời điểm lưu và truyền ở UTC; giao diện luôn hiển thị giờ Việt Nam
 * (CONFIG.md §5).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class GeoServerApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    SpringApplication.run(GeoServerApplication.class, args);
  }
}
