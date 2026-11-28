Phiên bản: v0.1 · 2026-11-26
# S2-E8 — trạng thái

| Ngày | Vai | Nội dung |
|---|---|---|
| 2026-11-26 | Dev/Test (runner-T-261126-21) | AC 112, TC 115: tự động 110 (UT 28, IT 6, E2E-API 34, E2E-UI 42), Manual 5 (TC-S2E8-011, 013, 014, 074, 087; phần đo được có kiểm máy ở `keypad.p0m.ui.spec.ts`). Sau khi gộp `worktree-mvp-wave0` (1377925): `make verify` exit 0 (đơn vị 257/257, IT 167/167, trong đó `AnswerGraderTest` 29/29, `PracticeS2E8IT` 6/6); `make web-build` exit 0 (53/53); `make e2e-affected` MỘT lượt, mức **L1** (S2-E8 + E6, S2-E2, E7, E3, E4, E2), lượt 20261126-221717: 557 đạt / 557, 0 trượt, 0 bỏ qua (TC 381/381), `app/e2e/evidence/20261126-221717/summary.json`; `make traceability ARGS="--strict --epic MVP,S2-E1,S2-E2,S2-E8"` exit 0 (thiếu test 0, trượt 0); `make bench-mvp` exit 0; `check_ac_tc.py S2-E8` 0 lỗi. Migration V180. Phát hiện: `qc_findings.md` mục Dev (15), đáng chú ý Dev-S2E8-01 (cầu nối `grade` TẠM, cần FIX `subject.api`), Dev-S2E8-08/09 (hình trong bài luyện: 2D, xem trước chưa có hình). |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v0.1 | 2026-11-26 | Dòng Dev/Test đầu tiên | runner-T-261126-21 |
