package vn.geo.server.sample.service;

import org.springframework.stereotype.Service;
import vn.geo.server.common.paging.KeyType;
import vn.geo.server.common.paging.KeysetCursor;
import vn.geo.server.common.paging.ListKind;
import vn.geo.server.common.paging.Page;
import vn.geo.server.common.paging.Paging;
import vn.geo.server.sample.SampleItem;
import vn.geo.server.sample.repo.SampleItemRepository;

@Service
public class SampleItemService {

  /** Loại danh sách mẫu: khóa sắp xếp (created_at, id). Con trỏ chỉ dùng được cho danh sách này. */
  public static final ListKind ITEMS = ListKind.of("sample.items", KeyType.TIMESTAMP);

  private final SampleItemRepository repo;
  private final Paging paging;

  public SampleItemService(SampleItemRepository repo, Paging paging) {
    this.repo = repo;
    this.paging = paging;
  }

  public Page<SampleItem> list(Integer limit, String cursor) {
    var request = paging.request(ITEMS, limit, cursor);
    return paging.page(
        repo.findPage(request), request, i -> KeysetCursor.of(i.id(), i.createdAt()));
  }
}
