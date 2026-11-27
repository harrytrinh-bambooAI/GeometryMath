package vn.geo.server.exam.internal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import vn.geo.server.exam.internal.domain.ExamDataset;
import vn.geo.server.exam.internal.domain.ExamModel.Item;
import vn.geo.server.exam.internal.repo.ExamBankRepository;

/**
 * Dữ liệu đề đã nạp giữ trong bộ nhớ (ma trận, ngân hàng, chủ đề) cho form ra đề và bộ lắp đề; nạp
 * lại khi phiên bản/băm dữ liệu đổi (lệnh nạp gọi {@link #invalidate()}).
 */
@Service
public class ExamCatalogService {

  public record Snapshot(
      String hash,
      List<ExamBankRepository.MatrixRow> matrices,
      Map<String, JsonNode> matrixBodies,
      List<Item> bank,
      Map<String, ExamDataset.Topic> topics) {}

  private final ExamBankRepository repo;
  private final JsonMapper mapper;
  private volatile Snapshot cached;

  public ExamCatalogService(ExamBankRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  public void invalidate() {
    cached = null;
  }

  public Snapshot snapshot() {
    ExamBankRepository.Version v = repo.latestVersion().orElse(null);
    String hash = v == null ? "" : v.hash();
    Snapshot s = cached;
    if (s != null && s.hash().equals(hash)) {
      return s;
    }
    List<ExamBankRepository.MatrixRow> ms = repo.matrices();
    Map<String, JsonNode> bodies = new LinkedHashMap<>();
    ms.forEach(m -> bodies.put(m.code(), mapper.readTree(m.body())));
    List<Item> bank = new ArrayList<>();
    for (String b : repo.itemBodies()) {
      bank.add(Item.of((ObjectNode) mapper.readTree(b)));
    }
    Map<String, ExamDataset.Topic> topics = new LinkedHashMap<>();
    if (v != null) {
      for (Map.Entry<String, JsonNode> e : mapper.readTree(v.meta()).path("topics").properties()) {
        int lop = e.getValue().path("lop").asInt();
        topics.put(
            e.getKey(),
            new ExamDataset.Topic(
                e.getKey(), e.getValue().path("ten").asString(), lop == 0 ? null : lop));
      }
    }
    s = new Snapshot(hash, ms, bodies, List.copyOf(bank), topics);
    cached = s;
    return s;
  }
}
