package vn.geo.server.sample.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.paging.Page;
import vn.geo.server.sample.SampleItem;
import vn.geo.server.sample.service.SampleItemService;

/** Endpoint mẫu. Lưu ý: không có tham số orgId; trường lấy từ thẻ xác thực. */
@RestController
@RequestMapping("/api/v1/sample")
public class SampleItemController {

  private final SampleItemService service;

  public SampleItemController(SampleItemService service) {
    this.service = service;
  }

  @GetMapping("/items")
  public Page<SampleItem> items(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) String cursor) {
    return service.list(limit, cursor);
  }
}
