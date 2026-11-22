package vn.geo.server.content.internal.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import vn.geo.server.content.internal.repo.ContentRepository;

/**
 * Nạp Kho đề của Giáo viên mẫu (US-GEO-504): nạp tệp nguồn bằng {@link CatalogImporter} (một bản
 * ghi cho cả kho đề và bộ đề chuẩn, lời giải đổi thì version mới), bỏ khỏi Kho đề đề thiếu đáp số,
 * rồi cho engine tính lại nhãn máy của các đề vừa nạp. Kết quả nạp nêu đề bị bỏ và mọi chỗ nhãn
 * engine tính khác nhãn ghi trong tệp (cột J chỉ dùng để so, không bao giờ thành nhãn: BR-GEO-213).
 */
@Service
public class CatalogLoader {

  /** Kết quả nạp: mã đã vào kho, các dòng ghi chú, nhãn máy cuối cùng theo mã. */
  public record Result(
      int setVersion, List<String> loaded, List<String> lines, Map<String, String> labels) {}

  private final CatalogImporter importer;
  private final CatalogVerifier verifier;
  private final ContentRepository repo;

  public CatalogLoader(CatalogImporter importer, CatalogVerifier verifier, ContentRepository repo) {
    this.importer = importer;
    this.verifier = verifier;
    this.repo = repo;
  }

  public Result load(Path file) throws IOException {
    Map<String, String> fileLabel = new LinkedHashMap<>();
    Set<String> missing = new HashSet<>();
    List<Map<String, String>> rows = XlsxSheets.read(file).getOrDefault("Bai tap", List.of());
    for (Map<String, String> r : rows.isEmpty() ? rows : rows.subList(1, rows.size())) {
      String code = r.getOrDefault("A", "");
      if (code.isEmpty()) {
        continue;
      }
      fileLabel.put(code, r.getOrDefault("J", "").strip());
      if (r.getOrDefault("H", "").isBlank()) {
        missing.add(code);
      }
    }
    CatalogImporter.Result imported = importer.importXlsx(file);
    List<String> lines = new ArrayList<>();
    List<String> loaded = new ArrayList<>();
    for (String code : imported.codes()) {
      if (missing.contains(code)) {
        repo.setArchived(code, true);
        lines.add(code + ": thiếu đáp số");
      } else {
        loaded.add(code);
      }
    }
    Set<String> verifyCodes = Set.copyOf(loaded);
    Map<String, String> labels = new LinkedHashMap<>();
    for (CatalogVerifier.Item it :
        verifier.verify(verifier.engineVersion(), verifyCodes::contains)) {
      labels.put(it.code(), it.label());
      String inFile = fileLabel.getOrDefault(it.code(), "");
      if (!inFile.isEmpty() && !inFile.equals(it.label())) {
        lines.add(it.code() + ": tệp ghi " + inFile + ", engine tính " + it.label());
      }
    }
    return new Result(imported.setVersion(), loaded, lines, labels);
  }
}
