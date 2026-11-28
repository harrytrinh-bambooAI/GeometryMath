package vn.geo.server.common.export.web;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.export.ExportInspector;

/**
 * MÓC KIỂM THỬ xuất tệp (T-261128-54), chỉ profile dev/test: E2E gửi lại tệp vừa tải về
 * (DOCX/PDF/ZIP) để đọc chữ từng trang, đầu/chân trang, bảng bằng chính POI/pdfbox.
 */
@RestController
@Profile({"dev", "test"})
public class ExportInspectTestHookController {

  @PostMapping(value = "/test-hooks/export/inspect", consumes = "application/octet-stream")
  public Map<String, Object> inspect(
      @RequestBody byte[] body, @RequestParam(defaultValue = "file") String name) {
    return ExportInspector.inspect(name, body);
  }
}
