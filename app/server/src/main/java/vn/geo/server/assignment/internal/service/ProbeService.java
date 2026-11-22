package vn.geo.server.assignment.internal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.geo.server.assignment.internal.repo.ProbeRepository;

/**
 * Bộ câu thử moi đáp án (US-GEO-611): nạp có phiên bản (thêm câu thì phiên bản tăng, BR-GEO-261),
 * từ chối câu trùng cho cùng bài và câu không gắn bài, mỗi câu dẫn tới đáp số tham chiếu và các
 * dạng viết tương đương của bài (BR-GEO-260).
 */
@Service
public class ProbeService {

  /** Một câu nạp vào bộ. targetCode: mã đề kho (HKG-nn) của bài áp dụng. */
  public record Item(
      String code,
      String group,
      String prompt,
      String targetCode,
      String channel,
      Boolean blocking) {}

  /** status: "đã nạp" | "câu trùng" | "không gắn bài" | "thiếu dữ liệu". */
  public record ItemResult(String code, String status) {}

  public record LoadResult(int version, int promptCount, List<ItemResult> items) {}

  public record Reference(
      String code, String group, String targetCode, String answer, List<String> forms) {}

  private final ProbeRepository repo;
  private final JsonMapper mapper;

  public ProbeService(ProbeRepository repo, JsonMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  /** Khóa chống trùng: kênh|câu chữ thường, gộp khoảng trắng (như seed 600_e6_core.sql). */
  public static String key(String channel, String prompt) {
    return channel + "|" + prompt.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }

  @Transactional
  public LoadResult load(List<Item> items) {
    List<ItemResult> out = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    int version = repo.currentVersion();
    int added = 0;
    for (Item it : items) {
      String code = it.code() == null ? "" : it.code();
      if (it.prompt() == null
          || it.prompt().isBlank()
          || it.group() == null
          || it.group().isBlank()) {
        out.add(new ItemResult(code, "thiếu dữ liệu"));
        continue;
      }
      Optional<UUID> pv =
          it.targetCode() == null || it.targetCode().isBlank()
              ? Optional.empty()
              : repo.catalogVersion(it.targetCode());
      if (pv.isEmpty()) {
        out.add(new ItemResult(code, "không gắn bài"));
        continue;
      }
      String channel = it.channel() == null ? "qa" : it.channel();
      String key = key(channel, it.prompt());
      if (!seen.add(pv.get() + key) || repo.duplicate(pv.get(), key) || repo.exists(code)) {
        out.add(new ItemResult(code, "câu trùng"));
        continue;
      }
      repo.insert(
          new ProbeRepository.Prompt(
              code,
              it.group(),
              it.prompt().strip(),
              pv.get(),
              it.targetCode(),
              channel,
              Boolean.TRUE.equals(it.blocking())),
          key,
          version + 1);
      out.add(new ItemResult(code, "đã nạp"));
      added++;
    }
    int count = repo.active().size();
    if (added > 0) {
      version = version + 1;
      repo.newVersion(version, count);
    }
    return new LoadResult(version, count, out);
  }

  public List<ProbeRepository.Prompt> prompts() {
    return repo.active();
  }

  public int version() {
    return repo.currentVersion();
  }

  public Optional<Reference> reference(String code) {
    return repo.reference(code)
        .map(r -> {
          List<String> forms = new ArrayList<>();
          if (r.forms() != null) {
            for (JsonNode f : mapper.readTree(r.forms())) {
              forms.add(f.asString());
            }
          }
          return new Reference(r.code(), r.group(), r.targetCode(), r.finalAnswer(), forms);
        });
  }

  /** Chuỗi bị coi là lộ của một bài: đáp số, dạng tương đương, nội dung từng bước (M-GEO-06). */
  public List<String> leakStrings(String code) {
    List<String> out = new ArrayList<>();
    repo.reference(code)
        .ifPresent(r -> {
          if (r.finalAnswer() != null) {
            out.add(r.finalAnswer());
          }
          if (r.forms() != null) {
            for (JsonNode f : mapper.readTree(r.forms())) {
              out.add(f.asString());
            }
          }
          if (r.steps() != null) {
            for (JsonNode s : mapper.readTree(r.steps())) {
              String b = s.path("body").asString("");
              if (!b.isBlank()) {
                out.add(b);
              }
            }
          }
        });
    return out;
  }

  public long save(ProbeRepository.Report r) {
    return repo.saveReport(r);
  }

  public List<ProbeRepository.Report> reports(int limit) {
    return repo.reports(limit);
  }
}
