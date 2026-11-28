package vn.geo.server.aigateway.internal;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * S2-E3 (T-261126-22): đáp mẫu GIẢ LẬP (D-GEO-047) cho bốn bước soạn nháp đề, đọc từ {@code DATA
 * (bên dưới)}. Không gọi AI thật.
 *
 * <ul>
 *   <li>{@code draft_ocr}: ô chữ + độ tin cậy theo dấu {@code GEO-S2E3:<TÊN>} trong ảnh (ảnh không
 *       có dấu = không đọc được chữ nào).
 *   <li>{@code draft_read}: loại đề (phẳng / không gian) theo quy tắc khớp chữ đề.
 *   <li>{@code draft_build}: mô tả dựng ({@code plane} hoặc {@code catalogCode}) cho engine.
 *   <li>{@code draft_solve}: các bước lời giải; kịch bản của móc kiểm thử chọn biến thể (đáp số
 *       sai, bước tính sai).
 * </ul>
 *
 * Kịch bản lỗi theo bước: {@code s2e3:build-error} (bước Dựng hình báo lỗi), {@code
 * s2e3:slow-build} / {@code s2e3:slow-solve} (trả lời sau 6 giây, để ca kiểm thấy bước đang chạy).
 */
final class MockDraftReplies {

  static final String PREFIX = "draft_";
  static final long SLOW_MS = 6_000;

  private static final Pattern IMAGE = Pattern.compile("Ảnh đề \\(base64\\): ([A-Za-z0-9+/=]+)");
  private static final Pattern MARK = Pattern.compile("GEO-S2E3:([A-Z0-9-]+)");
  private static final Pattern STATEMENT =
      Pattern.compile("Đề bài: (.*?)(?:\\nMô tả hình:|$)", Pattern.DOTALL);

  private final JsonMapper mapper;
  private final JsonNode data;

  MockDraftReplies(JsonMapper mapper) {
    this.mapper = mapper;
    this.data = mapper.readTree(DATA);
  }

  static boolean handles(String task) {
    return task != null && task.startsWith(PREFIX);
  }

  String reply(String task, String prompt, String scenario) throws InterruptedException {
    String sc = scenario == null ? "" : scenario;
    switch (task) {
      case "draft_ocr" -> {
        return mapper.writeValueAsString(ocr(prompt));
      }
      case "draft_read" -> {
        ObjectNode out = mapper.createObjectNode();
        out.put("kind", rule(statement(prompt)).path("kind").asString("plane"));
        return mapper.writeValueAsString(out);
      }
      case "draft_build" -> {
        if (sc.equals("s2e3:build-error")) {
          throw new IllegalStateException("nhà cung cấp giả lập: lỗi ở bước Dựng hình");
        }
        if (sc.equals("s2e3:slow-build")) {
          Thread.sleep(SLOW_MS);
        }
        ObjectNode out = mapper.createObjectNode();
        out.set("figure", rule(statement(prompt)).path("figure").deepCopy());
        return mapper.writeValueAsString(out);
      }
      case "draft_solve" -> {
        if (sc.equals("s2e3:slow-solve")) {
          Thread.sleep(SLOW_MS);
        }
        JsonNode r = rule(statement(prompt));
        JsonNode steps = r.path("variants").has(sc) ? r.path("variants").get(sc) : r.path("steps");
        ObjectNode out = mapper.createObjectNode();
        out.set("steps", steps.deepCopy());
        return mapper.writeValueAsString(out);
      }
      default -> {
        return "{}";
      }
    }
  }

  private static String statement(String prompt) {
    Matcher m = STATEMENT.matcher(prompt);
    return m.find() ? m.group(1).strip() : prompt;
  }

  private JsonNode rule(String statement) {
    for (JsonNode r : data.path("rules")) {
      if (Pattern.compile(r.path("pattern").asString()).matcher(statement).find()) {
        return r;
      }
    }
    return data.path("fallback");
  }

  private ObjectNode ocr(String prompt) {
    ObjectNode out = mapper.createObjectNode();
    Matcher m = IMAGE.matcher(prompt);
    String name = null;
    if (m.find()) {
      byte[] bytes;
      try {
        bytes = Base64.getDecoder().decode(m.group(1));
      } catch (IllegalArgumentException e) {
        bytes = new byte[0];
      }
      Matcher f = MARK.matcher(new String(bytes, StandardCharsets.ISO_8859_1));
      if (f.find()) {
        name = f.group(1);
      }
    }
    JsonNode cells = name == null ? null : data.path("ocr").get(name);
    out.set("cells", cells == null ? mapper.createArrayNode() : cells.deepCopy());
    return out;
  }

  /** Đáp mẫu giả lập (D-GEO-047) của S2-E3, giữ trong mã để thuộc module aigateway. */
  private static final String DATA =
"""
{
 "_doc": "S2-E3 (T-261126-22): đáp mẫu của nhà cung cấp AI GIẢ LẬP (D-GEO-047) cho bốn bước soạn nháp đề. ocr: theo dấu 'GEO-S2E3:<TÊN>' trong ảnh; read/build/solve: quy tắc đầu tiên có pattern khớp chữ đề; variants: ghi đè theo kịch bản của móc kiểm thử /test-hooks/ai (scenario).",
 "ocr": {
  "S2E3-CLEAR": [
   {
    "text": "Tam giác ABC có AB = 5, AC = 12, BC = 13;",
    "confidence": 0.97
   },
   {
    "text": "tính góc A.",
    "confidence": 0.96
   }
  ],
  "S2E3-BLURRY": [
   {
    "text": "Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a,",
    "confidence": 0.95
   },
   {
    "text": "SA ⊥ (ABCD),",
    "confidence": 0.9
   },
   {
    "text": "SA = a√2 ?",
    "confidence": 0.6
   },
   {
    "text": "Tính góc giữa SC và mặt phẳng đáy ?",
    "confidence": 0.79
   }
  ],
  "S2E3-EDGE": [
   {
    "text": "Tam giác ABC có AB = 5, AC = 12,",
    "confidence": 0.8
   },
   {
    "text": "BC = 13; tính góc A.",
    "confidence": 0.79
   }
  ],
  "S2E3-BLANK": []
 },
 "rules": [
  {
   "name": "FX-10",
   "pattern": "AB = 5, AC = 12, BC = 13",
   "kind": "plane",
   "figure": {
    "plane": {
     "shape": "triangle",
     "points": [
      "A",
      "B",
      "C"
     ],
     "given": [
      {
       "type": "length",
       "of": [
        "A",
        "B"
       ],
       "value": 5
      },
      {
       "type": "length",
       "of": [
        "A",
        "C"
       ],
       "value": 12
      },
      {
       "type": "length",
       "of": [
        "B",
        "C"
       ],
       "value": 13
      }
     ],
     "asked": {
      "name": "góc A",
      "kind": "angle",
      "of": [
       "B",
       "A",
       "C"
      ]
     }
    }
   },
   "steps": [
    "AB^2 + AC^2 = 5^2 + 12^2 = 169.",
    "BC^2 = 13^2 = 169.",
    "Vậy tam giác ABC vuông tại A, góc A = 90°."
   ],
   "variants": {
    "s2e3:answer-80": [
     "AB^2 + AC^2 = 5^2 + 12^2 = 169.",
     "BC^2 = 13^2 = 169.",
     "Vậy góc A = 80°."
    ],
    "s2e3:solve-wrong": [
     "AB^2 + AC^2 = 5^2 + 12^2 = 144.",
     "Vậy tam giác ABC vuông tại A, góc A = 90°."
    ]
   }
  },
  {
   "name": "FX-12",
   "pattern": "chữ nhật ABCD có AB = 6, AD = 8",
   "kind": "plane",
   "figure": {
    "plane": {
     "shape": "rectangle",
     "points": [
      "A",
      "B",
      "C",
      "D"
     ],
     "given": [
      {
       "type": "length",
       "of": [
        "A",
        "B"
       ],
       "value": 6
      },
      {
       "type": "length",
       "of": [
        "A",
        "D"
       ],
       "value": 8
      }
     ],
     "asked": {
      "name": "AC",
      "kind": "length",
      "of": [
       "A",
       "C"
      ]
     }
    }
   },
   "steps": [
    "AC^2 = AB^2 + BC^2 = 6^2 + 8^2 = 100.",
    "Vậy AC = 10."
   ]
  },
  {
   "name": "FX-14",
   "pattern": "hình chóp S\\\\.ABCD.*SA = a√2",
   "kind": "space",
   "figure": {
    "catalogCode": "HKN-02"
   },
   "steps": [
    "SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.",
    "ABCD vuông cạnh a nên AC = a√2.",
    "tan SCA = a√2 / a√2 = 1",
    "Vậy góc giữa SC và mặt phẳng đáy bằng 45°."
   ]
  },
  {
   "name": "ELIP",
   "pattern": "[Ee]lip",
   "kind": "plane",
   "figure": {
    "plane": {
     "shape": "ellipse",
     "points": [
      "A",
      "B",
      "M"
     ],
     "coords": {
      "A": [
       -3,
       0
      ],
      "B": [
       3,
       0
      ],
      "M": [
       0,
       2
      ]
     }
    }
   },
   "steps": [
    "Máy chưa có dạng hình này."
   ]
  }
 ],
 "fallback": {
  "kind": "plane",
  "figure": {
   "plane": {
    "shape": "unknown"
   }
  },
  "steps": [
   "Chưa soạn được lời giải cho đề này."
  ]
 }
}
""";
}
