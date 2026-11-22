# Hợp đồng dữ liệu cảnh (Scene contract) · v1 · 22/11/2026

Chủ sở hữu: E2 (Hình đúng). Người dùng: E3 (bộ xem 3D, công cụ thao tác), E4 (lời giải đồng bộ hình), E5–E7 (màn có hình).
Mã nguồn của hợp đồng:
- Java: `server/src/main/java/vn/geo/server/geometry/api/SceneDto.java` (+ `TemplateDto`, `SelfStudyDto`).
- TypeScript: `web/src/shared/scene/types.ts` (`Scene`, `SolidTemplate`, `SelfStudy`).

Nguồn: `architecture/03_schema.sql` (`scene_build`, `solid_template`, `verification_report`), `SPEC-technical.md` §3.4, §5.1
(`figure.type`, `solid.vertices/edges/faces`), seed `db/seed/e2e/100_e1_core.sql`, `Design/prototype/fig3d.js` (đại lượng ẩn,
mặt cong), `epics/E2/stories_ac.md` (BR-GEO-06, 08, 127…140).

## 1. Quy tắc đổi
- `schemaVersion = 1`. Chỉ được **thêm trường tùy chọn**; không đổi tên, không đổi nghĩa, không bỏ trường. Đổi phá vỡ ⇒ tăng
  `schemaVersion` và ghi vào mục 6.
- Máy chủ bỏ trường `null` khỏi JSON (`default-property-inclusion: non_null`): trường tùy chọn có thể vắng mặt.
- Cảnh **không bao giờ** chứa giá trị tính của báo cáo kiểm chứng hay đáp số (US-GEO-203-FC5, BR-GEO-01). Tọa độ đỉnh là
  dữ kiện của đề, không tính là lộ (D-GEO-154).

## 2. Trường của `Scene`

| Trường | Kiểu | Bắt buộc | Nghĩa |
|---|---|---|---|
| `schemaVersion` | số | có | 1 |
| `figureType` | `"3d"` \| `"2d"` \| `"none"` | có | như `figure.type` (FR-SOL-2). MVP chỉ dựng `3d` (hình phẳng: MSG-GEO-17, không có cảnh) |
| `sceneId` | uuid | không | id `scene_build` đã lưu; vắng ở bản xem trước hình mẫu |
| `coordSource` | `"engine"` \| `"ai"` | có | `engine`: engine tính từ ràng buộc (BR-GEO-06). `ai`: đề lạ, tọa độ AI (BR-GEO-08) |
| `template` | chuỗi | không | mã bộ dựng của engine (mục 3); vắng khi `coordSource = ai` |
| `solidKind` | `pyramid` `prism` `box` `cube` `cone` `cylinder` `sphere` | không | loại khối; vắng với hình lạ |
| `unit` | `"a"` \| `"cm"` \| `""` | có | đơn vị hiện cạnh số đo (công cụ Đo dài của E3) |
| `params` | `{tên: số}` | có | tham số đã dùng để dựng, theo thứ tự khai báo |
| `points` | `[{name, x, y, z}]` | có | đỉnh có tên; **hệ trục toán học, z hướng lên** (bộ xem Three.js tự đổi sang y-lên) |
| `edges` | `[[P, Q]]` | có | cạnh nét của hình |
| `faces` | `[[P, Q, R, …]]` | có | mặt phẳng của khối, đỉnh theo thứ tự vòng, đa giác lồi (chia tam giác kiểu quạt từ đỉnh đầu, FR-3D-1) |
| `surfaces` | `[SceneSurface]` | có (có thể rỗng) | mặt cong: nón, trụ, cầu (mục 2.1) |
| `chips` | `[{text, points}]` | có (có thể rỗng) | thẻ ràng buộc độ dài ở **góc dưới trái** khung hình (BR-GEO-130), ví dụ `"SA = a"` |
| `hiddenMeasures` | `[chuỗi]` | có (có thể rỗng) | đại lượng đề hỏi mà công cụ đo phải ẩn giá trị trước giờ mở (D-GEO-024): `ang:S-D-A` = góc tại D giữa DS và DA; `len:O-O′` = độ dài OO′ (cùng cú pháp `fig3d.js`). Chỉ có biểu thức, không có giá trị |
| `axes` | `{available, origin, up}` | có | hệ trục Oxyz bật được trên mọi hình (US-GEO-205-SC1); gốc tại gốc tọa độ của đề; `up = "z"` |
| `verification` | `{status, label, warning?}` | có | nhãn máy (mục 2.2) |

**Cạnh khuất (nét đứt):** phụ thuộc góc nhìn nên **bộ xem tính** (E3), máy chủ không gửi. Tọa độ và mặt đủ để tính.

### 2.1 `SceneSurface`
| kind | Trường | Nghĩa |
|---|---|---|
| `cone` | `center`, `radius`, `axis`, `apex` | đáy tâm `center` bán kính `radius`, pháp tuyến đáy `axis` (đơn vị), đỉnh `apex` |
| `cylinder` | `center`, `radius`, `axis`, `height` | đáy dưới tâm `center`, trục đơn vị `axis`, chiều cao `height` |
| `sphere` | `center`, `radius` | mặt cầu |

### 2.2 `verification`
| Trường | Giá trị |
|---|---|
| `status` | `verified` \| `needs_review` (cột `problem_version.verify_status`; chỉ engine đặt, BR-GEO-135) |
| `label` | đúng chuỗi `"Đã kiểm chứng"` \| `"Cần xem lại"` trên mọi màn (BR-GEO-136); không dùng "Hình đã kiểm chứng" |
| `warning` | có khi `coordSource = ai`: `{code: "MSG-GEO-16", text: "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề.", dismissible: false}`. Hiện **cố định trên khung hình**, không có nút đóng (BR-GEO-137) |

Bảng nhãn (US-GEO-203): engine thỏa ràng buộc + mọi đáp số khớp + nguồn engine ⇒ `verified`; còn lại `needs_review`;
đề lạ (`ai`) luôn `needs_review` kèm `warning`.

## 3. Bộ dựng của engine (`template`)
Ràng buộc kiểm theo BR-GEO-127: sai số ≤ 1e-6 + 1e-12 trên hình đã quy cạnh lớn nhất về 1. Tham số phải > 0 (hình suy biến
hoặc ràng buộc mâu thuẫn ⇒ không có cảnh, MSG-GEO-18). Engine là hàm thuần (BR-GEO-129).

| template | params | Đỉnh | Ghi chú |
|---|---|---|---|
| `pyramid` | `a` cạnh đáy, `h` chiều cao | S, A, B, C, D | chóp tứ giác đều (thẻ "Hình chóp") |
| `pyramid_square_sa` | `a`, `sa` | S, A, B, C, D | đáy vuông cạnh a, SA ⊥ đáy (HKG-01) |
| `prism` | `a`, `h` | A, B, C, A′, B′, C′ | lăng trụ đứng đáy tam giác đều (thẻ "Lăng trụ", HKG-03) |
| `prism_tri_right` | `ab`, `ac`, `h` | A, B, C, A′, B′, C′ | đáy tam giác vuông tại A (F-GEO-060) |
| `prism_tri_sides` | `ab`, `bc`, `ca`, `h` | A, B, C, A′, B′, C′ | đáy tam giác cho ba cạnh (bất đẳng thức tam giác sai ⇒ mâu thuẫn) |
| `box` | `a`, `b`, `c` | A, B, C, D, A′, B′, C′, D′ | hình hộp chữ nhật (thẻ "Hình hộp") |
| `cube` | `a` | như `box` | hộp ba cạnh bằng nhau (HKG-02), không có thẻ riêng |
| `cone` | `r`, `h` | S, O, M | thẻ "Hình nón"; M trên đường tròn đáy, SM là đường sinh |
| `cone_rl` | `r`, `l` | S, O, M | nón cho bán kính và đường sinh (HKG-04) |
| `cylinder` | `r`, `h` | O, O′, M, M′ | thẻ "Hình trụ" (HKG-05) |
| `sphere` | `r` | O, M | thẻ "Hình cầu" |
| `sphere_circum_cube` | `a` | A…D′, I | mặt cầu ngoại tiếp hình lập phương (HKG-06) |

Tọa độ mẫu: A tại gốc, AB theo trục x, AD (hoặc AC) theo trục y, cạnh bên theo trục z.

## 4. Điểm cuối (đều cần thẻ; lỗi dạng `{code, message, requestId, details[]}`)

| Điểm cuối | Ai | Trả về |
|---|---|---|
| `GET /api/v1/geometry/templates` | mọi vai, mọi trường (D-GEO-076) | `{items: SolidTemplate[]}` sắp theo `order` |
| `POST /api/v1/geometry/templates/{code}/scene` thân `{params: {r: 3, h: 4}}` | mọi vai | `Scene` (engine, **không gọi AI**, không lưu). Tham số ≤ 0 / không phải số ⇒ `VALIDATION_FAILED` (MSG-GEO-19) |
| `POST /api/v1/student/self-study` thân `{statement}` | học sinh | `SelfStudy` (tạo đề tự nhập + bài tự học). Hình phẳng ⇒ `VALIDATION_FAILED`, `details[0].code = "MSG-GEO-17"`; không dựng được ⇒ `details[0].code = "MSG-GEO-18"`; không tạo bài |
| `GET /api/v1/student/self-study/{assignmentId}` | học sinh chủ bài | `SelfStudy` (tải lại trang vẫn có cảnh và cảnh báo) |
| `GET /api/v1/student/assignments/{id}/scene` | học sinh của lớp / chủ bài tự học | `Scene` của đề trong bài |
| `GET /api/v1/teacher/problems/{versionId}` | giáo viên | `{versionId, code, statement, verifyStatus, label, scene}` (trang chi tiết đề) |
| `PUT /api/v1/problems/{versionId}/verify-status` | không ai | luôn `FORBIDDEN` (MSG-GEO-03): nhãn máy chỉ do engine tính (BR-GEO-135) |

## 5. Ví dụ JSON

### 5.1 HKG-01 (bài giao BG-1, engine, đã kiểm chứng)
```json
{
  "schemaVersion": 1,
  "figureType": "3d",
  "sceneId": "00000000-0000-7000-8000-000000300031",
  "coordSource": "engine",
  "template": "pyramid_square_sa",
  "solidKind": "pyramid",
  "unit": "a",
  "params": { "a": 1.0, "sa": 1.0 },
  "points": [
    { "name": "S", "x": 0, "y": 0, "z": 1 },
    { "name": "A", "x": 0, "y": 0, "z": 0 },
    { "name": "B", "x": 1, "y": 0, "z": 0 },
    { "name": "C", "x": 1, "y": 1, "z": 0 },
    { "name": "D", "x": 0, "y": 1, "z": 0 }
  ],
  "edges": [["A","B"],["B","C"],["C","D"],["D","A"],["S","A"],["S","B"],["S","C"],["S","D"]],
  "faces": [["A","B","C","D"],["S","A","B"],["S","B","C"],["S","C","D"],["S","D","A"]],
  "surfaces": [],
  "chips": [ { "text": "SA = a", "points": ["S","A"] }, { "text": "AD = a", "points": ["A","D"] } ],
  "hiddenMeasures": ["ang:S-D-A"],
  "axes": { "available": true, "origin": [0, 0, 0], "up": "z" },
  "verification": { "status": "verified", "label": "Đã kiểm chứng" }
}
```

### 5.2 Hình nón mẫu r = 3, h = 4 (xem trước, không lưu)
```json
{
  "schemaVersion": 1, "figureType": "3d", "coordSource": "engine", "template": "cone", "solidKind": "cone", "unit": "",
  "params": { "r": 3.0, "h": 4.0 },
  "points": [ { "name": "S", "x": 0, "y": 0, "z": 4 }, { "name": "O", "x": 0, "y": 0, "z": 0 }, { "name": "M", "x": 3, "y": 0, "z": 0 } ],
  "edges": [["S","O"],["O","M"],["S","M"]], "faces": [],
  "surfaces": [ { "kind": "cone", "center": [0,0,0], "radius": 3.0, "axis": [0,0,1], "apex": [0,0,4] } ],
  "chips": [], "hiddenMeasures": [],
  "axes": { "available": true, "origin": [0,0,0], "up": "z" },
  "verification": { "status": "verified", "label": "Đã kiểm chứng" }
}
```
(Hình mẫu không có đáp số để so; engine dựng thỏa ràng buộc nên nhãn là "Đã kiểm chứng".)

### 5.3 Đề lạ (bát diện đều, F-GEO-110, tọa độ AI)
```json
{
  "schemaVersion": 1, "figureType": "3d", "sceneId": "…", "coordSource": "ai", "unit": "a", "params": {},
  "points": [ { "name": "A", "x": 0.7071, "y": 0, "z": 0 }, "… 6 đỉnh …" ],
  "edges": [["A","C"], "… 12 cạnh …"], "faces": [["A","C","E"], "… 8 mặt …"], "surfaces": [],
  "chips": [], "hiddenMeasures": [],
  "axes": { "available": true, "origin": [0,0,0], "up": "z" },
  "verification": {
    "status": "needs_review", "label": "Cần xem lại",
    "warning": { "code": "MSG-GEO-16", "text": "Hình này do AI dựng và chưa qua kiểm tra bằng tính toán. Em hãy đối chiếu với đề.", "dismissible": false }
  }
}
```

## 6. Lưu trữ (`scene_build`) và lịch sử
- `scene_build.spec` (jsonb, `schema_version = 1`): `{schema_version, solid, params, template?, unit?, chips?, hidden?,
  topology?: {edges, faces}}`. `scene_build.coords`: `{tên: [x, y, z]}`. Bản dựng của seed E1 (chỉ có `solid`, `params`)
  được engine dựng lại theo mã bài khi đọc (hàm thuần, cùng kết quả).
- Lịch sử: v1 (22/11/2026, E2) bản đầu.
