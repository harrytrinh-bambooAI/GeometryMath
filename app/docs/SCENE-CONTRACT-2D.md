# Hợp đồng cảnh 2D (SceneSpec 2D) · scene2d-v1 · 26/11/2026

Phiên bản: v1.0 · 2026-11-26 · T-261126-19 (S2-E2, PLAN_code-s23 A3). Hợp đồng 3D của MVP giữ nguyên ở `SCENE-CONTRACT.md`.

Chủ sở hữu: S2-E2 (khung môn học, hình phẳng). Người dùng: màn C2-05 làm bài hình phẳng (`web/src/features/s2e2`), các epic
chặng 2–3 có hình phẳng (S2-E3, S2-E5, S2-E9…).
Mã nguồn:
- Java: `server/src/main/java/vn/geo/server/geometry/internal/plane/PlaneScene.java` (cảnh), `PlaneSpec.java` (mô tả dựng),
  `PlaneEngine.java` (dựng, kiểm ràng buộc, đo), `geometry/internal/GeometrySubject.java` (môn "Hình học" trong khung môn học).
- TypeScript: `web/src/shared/scene2d/geometry2d.ts` (`Scene2D`, đo, định dạng), `Scene2DView.tsx` (vẽ).

Nguồn: `artifacts/GEO/work/epics/S2-E2/stories_ac.md` v0.3 (BR-S2E2-04…11), STAGES Ch2 mục 8–10, PO-66 (QA-S2E2-01, 03, 04).

## 1. Quy tắc
- `contract = "scene2d-v1"`. Chỉ thêm trường tùy chọn; đổi phá vỡ thì tăng phiên bản hợp đồng.
- Cảnh chỉ chứa **điểm, nhãn, đoạn vẽ, đường tròn, ràng buộc đề cho, tọa độ** (BR-S2E2-07). Không có đáp số, giá trị đại
  lượng đề hỏi, bước giải hay gợi ý. Tọa độ là dữ kiện của đề (như D-GEO-154 của 3D).
- Dựng hai lần cùng đề cho chuỗi JSON giống hệt (BR-S2E2-08): engine là hàm thuần, thứ tự trường và phần tử cố định.
- Máy chủ bỏ trường `null` (`value` của ràng buộc không số vắng mặt).

## 2. Mô tả dựng (`plane_problem_spec.spec`, bộ đề chuẩn `spec`)

```json
{ "shape": "triangle", "points": ["A","B","C"], "at": "A", "isosceles": false,
  "given": [ {"type":"length","of":["A","B"],"value":5}, {"type":"angle","of":["B","A","C"],"value":40},
             {"type":"radius","of":["O"],"value":5}, {"type":"distance","of":["O","A","B"],"value":3},
             {"type":"height","of":[],"value":4} ],
  "asked": {"name":"góc A","kind":"angle","of":["B","A","C"]},
  "coords": {"A":[0,0],"B":[4,0],"C":[1,3]},
  "outOfPlane": ["S"] }
```

| Trường | Nghĩa |
|---|---|
| `shape` | dạng hỗ trợ (BR-S2E2-05): `triangle`, `isosceles_triangle` (cân tại `at`), `equilateral_triangle`, `right_triangle` (vuông tại `at`), `rectangle`, `square`, `parallelogram`, `trapezoid` (AB ∥ CD; `isosceles` hoặc vuông tại `at`), `circle_chord` [O, A, B], `circle_tangent` [O, M, T(, T′)]. Dạng khác: nhánh "Cần xem lại", lý do "dạng chưa hỗ trợ", ghi một dòng thống kê `plane.unsupported` |
| `points` | nhãn đỉnh theo thứ tự (mặc định theo dạng) |
| `given` | dữ kiện đề cho: `length` [P,Q], `angle` [P,O,Q] (độ, 0 < góc < 180), `radius` [O], `distance` [P,A,B], `height` (hình thang) |
| `asked` | đại lượng hỏi: `length` [P,Q], `angle` [P,O,Q], `area` [đỉnh…] (rỗng: cả hình), `perimeter`, `distance` [P,A,B]. Chỉ dùng để giải/khóa đo, không vào cảnh |
| `coords` | tọa độ AI dựng (đề lạ): engine vẽ nhưng không kiểm chứng, nhãn máy "Cần xem lại", `coordSource = "ai"` |
| `outOfPlane` | có điểm ngoài mặt phẳng ⇒ `Problem.kind = space`, không vào nhánh dựng phẳng (BR-S2E2-04) |

Không dựng (cảnh `null`, nhãn "Cần xem lại", kèm lý do): "ba cạnh không lập thành tam giác" (kể cả suy biến), "góc không hợp
lệ", "hình chưa xác định, thiếu dữ kiện", "số liệu mâu thuẫn", "dạng chưa hỗ trợ".

## 3. Trường của cảnh

| Trường | Kiểu | Nghĩa |
|---|---|---|
| `contract` | `"scene2d-v1"` | phiên bản hợp đồng |
| `kind` | `"plane"` | loại đề |
| `coordSource` | `"engine"` \| `"ai"` | nguồn tọa độ |
| `points[]` | `{id, label, x, y}` | đỉnh; tọa độ theo đơn vị của đề, trục y hướng lên (bộ vẽ lật khi vẽ) |
| `segments[]` | `[P, Q]` | đoạn cần vẽ (cạnh, đường chéo/bán kính liên quan) |
| `circles[]` | `{center, r}` | đường tròn |
| `constraints[]` | `{type, of, value?}` | ràng buộc đề cho và ràng buộc ngầm của dạng (`equal`, `parallel`, `perpendicular`, `on_circle` không có `value`; góc vuông của hình chữ nhật có `value = 90`) |

"Thỏa ràng buộc" (BR-S2E2-06, M-S2-01): mọi ràng buộc đúng trên tọa độ dựng với sai số tương đối ≤ 1e-6.

## 4. Đo trên thiết bị và khóa đáp số
- Màn học sinh nhận `GET /api/v1/student/assignments/{id}/plane` → `{scene, machineLabel, humanLabel, released, locked[], hints[]}`
  (no-store). `locked[]` = đại lượng đề hỏi `{kind, of}` khi lời giải còn khóa (giờ máy chủ < `release_at`), KHÔNG có giá trị.
- Thiết bị tự đo trên tọa độ: độ dài "AB = 5", góc "A = 90°" (hai chữ số thập phân, dấu phẩy). Đo trúng đại lượng khóa:
  không tính, hiện "Đại lượng này là đáp số, chưa đo được" (BR-S2E2-09).

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-26 | Tạo mới: mô tả dựng, cảnh scene2d-v1, đo và khóa đáp số | runner-T-261126-19 |
