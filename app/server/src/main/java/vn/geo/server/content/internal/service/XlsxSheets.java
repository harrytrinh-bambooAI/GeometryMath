package vn.geo.server.content.internal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Đọc tệp .xlsx tối thiểu (không thêm thư viện): tên trang → các hàng, mỗi hàng là map cột (A, B…)
 * → chữ. Hỗ trợ chuỗi nội tuyến, chuỗi dùng chung và số. Đủ cho tệp nguồn kho đề {@code
 * materials/}.
 */
final class XlsxSheets {

  private XlsxSheets() {}

  static Map<String, List<Map<String, String>>> read(Path file) throws IOException {
    try (ZipFile zip = new ZipFile(file.toFile())) {
      List<String> shared = sharedStrings(zip);
      Document wb = xml(zip, "xl/workbook.xml");
      Document rels = xml(zip, "xl/_rels/workbook.xml.rels");
      Map<String, String> relTarget = new HashMap<>();
      NodeList rl = rels.getElementsByTagNameNS("*", "Relationship");
      for (int i = 0; i < rl.getLength(); i++) {
        Element r = (Element) rl.item(i);
        relTarget.put(r.getAttribute("Id"), r.getAttribute("Target"));
      }
      Map<String, List<Map<String, String>>> out = new LinkedHashMap<>();
      NodeList sheets = wb.getElementsByTagNameNS("*", "sheet");
      for (int i = 0; i < sheets.getLength(); i++) {
        Element s = (Element) sheets.item(i);
        String rid =
            s.getAttributeNS(
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
        String target = relTarget.get(rid);
        if (target == null) {
          continue;
        }
        String path = target.startsWith("/") ? target.substring(1) : "xl/" + target;
        out.put(s.getAttribute("name"), rows(xml(zip, path), shared));
      }
      return out;
    }
  }

  private static List<Map<String, String>> rows(Document sheet, List<String> shared) {
    List<Map<String, String>> rows = new ArrayList<>();
    NodeList rl = sheet.getElementsByTagNameNS("*", "row");
    for (int i = 0; i < rl.getLength(); i++) {
      Map<String, String> row = new LinkedHashMap<>();
      NodeList cells = ((Element) rl.item(i)).getElementsByTagNameNS("*", "c");
      for (int j = 0; j < cells.getLength(); j++) {
        Element c = (Element) cells.item(j);
        String col = c.getAttribute("r").replaceAll("[0-9]", "");
        String type = c.getAttribute("t");
        String value;
        if ("inlineStr".equals(type)) {
          value = text(c, "t");
        } else {
          String v = text(c, "v");
          value = "s".equals(type) && !v.isEmpty() ? shared.get(Integer.parseInt(v)) : v;
        }
        row.put(col, value.strip());
      }
      rows.add(row);
    }
    return rows;
  }

  private static String text(Element e, String tag) {
    NodeList ts = e.getElementsByTagNameNS("*", tag);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ts.getLength(); i++) {
      sb.append(ts.item(i).getTextContent());
    }
    return sb.toString();
  }

  private static List<String> sharedStrings(ZipFile zip) throws IOException {
    if (zip.getEntry("xl/sharedStrings.xml") == null) {
      return List.of();
    }
    Document d = xml(zip, "xl/sharedStrings.xml");
    List<String> out = new ArrayList<>();
    NodeList si = d.getElementsByTagNameNS("*", "si");
    for (int i = 0; i < si.getLength(); i++) {
      out.add(text((Element) si.item(i), "t"));
    }
    return out;
  }

  private static Document xml(ZipFile zip, String name) throws IOException {
    ZipEntry e = zip.getEntry(name);
    if (e == null) {
      throw new IOException("thiếu " + name + " trong tệp xlsx");
    }
    try (InputStream in = zip.getInputStream(e)) {
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      f.setExpandEntityReferences(false);
      return f.newDocumentBuilder().parse(in);
    } catch (javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException ex) {
      throw new IOException("tệp xlsx hỏng: " + name, ex);
    }
  }
}
