# Bộ đo MVP (MVP.md §9) · 2026-11-27T09:06:48.595998+07:00

- Nhà cung cấp AI: `mock` · model `gia-lap-1` · prompt `p1`
- Bộ đề chuẩn: 6 đề (hợp mã của /geometry/problem-specs.json, /content/scene-specs.json, /mock-ai/benchmark.json; đề + tham chiếu từ kho đã nạp) · phạm vi mock: 6/90 đề có câu trả lời mẫu; đủ kho chỉ đo với AI thật
- Giá: geo.ai.pricing (application.yml): bảng giá Anthropic theo skill claude-api, cached 2026-11-03, chép 2026-11-26; mock = chi phí mô phỏng geo.ai.cost-per-call
- Kết luận chỉ số bắt buộc: **ĐẠT**

| Chỉ số | Giá trị | Ngưỡng | Kết quả |
|---|---|---|---|
| Hình thỏa ràng buộc (bắt buộc) | 100.0% | ≥ 95% | Pass |
| Đáp số đúng (bắt buộc) | 100.0% | ≥ 90% | Pass |
| Bài sai mà gắn "Đã kiểm chứng" (bắt buộc) | 0 | 0 | Pass |
| Lộ lời giải/đáp số trước giờ mở (bắt buộc, 22 câu thử) | 0 | 0 | Pass |
| Trung vị gửi đề → có hình | 0.01 s | < 40 s | Pass |
| Chi phí AI mỗi bài | TB 0.360000 USD · trung vị 0.360000 USD · tổng 2.160000 USD | đo | - |

| Mã | Hình | Đáp số | Nhãn | Sai mà ĐKC | AI đáp số | Tham chiếu | Gửi đề→hình (ms) | Lượt AI | Token vào/ra | Chi phí (USD) | Lỗi |
|---|---|---|---|---|---|---|---|---|---|---|---|
| HKG-01 | ✓ | ✓ | Đã kiểm chứng | ✗ | 45° | 45° | 83 | 3 | 84/260 | 0.360000 |  |
| HKG-02 | ✓ | ✓ | Đã kiểm chứng | ✗ | a | a | 7 | 3 | 67/190 | 0.360000 |  |
| HKG-03 | ✓ | ✓ | Đã kiểm chứng | ✗ | 24√3 cm³ | 24√3 cm³ | 6 | 3 | 76/173 | 0.360000 |  |
| HKG-04 | ✓ | ✓ | Đã kiểm chứng | ✗ | Sxq = 15π cm²; V = 12π cm³ | Sxq = 15π cm²; V = 12π cm³ | 9 | 3 | 76/187 | 0.360000 |  |
| HKG-05 | ✓ | ✓ | Đã kiểm chứng | ✗ | 112π cm² | 112π cm² | 5 | 3 | 66/139 | 0.360000 |  |
| HKG-06 | ✓ | ✓ | Đã kiểm chứng | ✗ | R = a√3/2; V = (√3/2)·π·a³ | R = a√3/2; V = (√3/2)·π·a³ | 5 | 3 | 70/216 | 0.360000 |  |
