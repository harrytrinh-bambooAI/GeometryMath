Phiên bản: v1.0 · 2026-11-28 · runner-T-261128-66 (Tester L6)

# E2E trọn bộ chặng 5 (S5-E1…E6)

Nhánh `s5-test` tách từ `worktree-mvp-wave0` @ b283230 (nhánh tích hợp đã gộp W0–W7 chặng 5 và công cụ E2E cách lai T-261128-69). Cờ `geo.s5.grading.enabled` và `geo.s5.ai-grading.enabled` bật ở hồ sơ `dev` mà `scripts/e2e.sh` dùng (`application-dev.yml`). AI giả lập mặc định (không đặt `GEO_AI_PROVIDER`). Cổng `app/.env.ports`: PG 54879, Redis 6909, máy chủ 8217, web 5297. Nguồn TC: `artifacts/GEO/work/epics/S5-E1…E6/test_cases.md` (219 TC).

## Lệnh đã chạy
| # | Lệnh (ở `app/`) | Kết quả | Thời gian |
|---|---|---|---|
| 1 | `GEO_E2E_RUN=s5-ui GEO_E2E_EVIDENCE=fail GEO_E2E_RETRIES=1 make e2e-full ARGS="--project=ui-chromium --project=ui-webkit --project=ui-iphone --project=mobile-iphone --project=mobile-pixel s5e[1-6]/"` | exit 0 · 52/52 test đạt · 26/26 mã TC · chập chờn 0 · trượt 0 | Playwright 46,2 giây (5 worker), cộng build máy chủ + web |
| 2 | `GEO_E2E_SKIP_BUILD=1 GEO_E2E_RUN=s5-api GEO_E2E_EVIDENCE=fail make e2e-full ARGS="--project=api s5e[1-6]/"` | exit 0 · 200/200 test đạt · 196/196 mã TC (gồm nhóm `_khong-ma-TC`) · chập chờn 0 · trượt 0 | 1,8 phút |
| 3 | `make traceability ARGS="--strict --epic MVP,S2,S3,S5"` | ĐẠT: thiếu test 0, trượt 0, hoãn 0 (S5-E1…E6 đủ test, đều đạt) | — |
| 4 | Gộp kết quả hai lượt theo mã TC và cột "Tự động hóa" trong `test_cases.md` | đạt + trượt + bị chặn + ngoài E2E = tổng (219 + 0 + 0 + 0 = 219) | — |

Theo DECISIONS 28/11 "E2E cách lai": chạy UI + tương tác bằng Playwright trước (lượt 1), phần còn lại qua dự án `api` (lượt 2). Chặng 5 không có spec `*.iphone.spec.ts` hay `*.p0m.ui.spec.ts`, nên `ui-iphone`, `mobile-iphone`, `mobile-pixel` có 0 test. Số test UI theo dự án: ui-chromium 26, ui-webkit 26 (tổng 52).

## Kết quả theo epic (đếm theo mã TC)
| Epic | TC trong test_cases.md | UI đạt/tổng | API đạt/tổng | Trượt | Bị chặn | Ngoài E2E (IT/UT/Manual) |
|---|---|---|---|---|---|---|
| S5-E1 | 31 | 5/5 | 26/26 | 0 | 0 | 0 |
| S5-E2 | 29 | 2/2 | 27/27 | 0 | 0 | 0 |
| S5-E3 | 42 | 3/3 | 39/39 | 0 | 0 | 0 |
| S5-E4 | 51 | 8/8 | 43/43 | 0 | 0 | 0 |
| S5-E5 | 29 | 3/3 | 26/26 | 0 | 0 | 0 |
| S5-E6 | 37 | 5/5 | 32/32 | 0 | 0 | 0 |
| **Tổng** | **219** | **26/26** | **193/193** | **0** | **0** | **0** |

- "UI" = TC có cột Tự động hóa E2E-UI, chạy ở ui-chromium + ui-webkit; "API" = TC E2E-API, chạy ở dự án `api`. Cả 219 TC chặng 5 đều gắn E2E, nên cột "Ngoài E2E" = 0.
- Hai TC E2E-UI có thêm test API bổ trợ: TC-S5E4-23 (`queue.api.spec.ts`), TC-S5E6-20 (`review.api.spec.ts`); đều đạt, tính một lần ở cột UI.
- Mọi TC S5 có status Đạt; status ghi vào `artifacts/GEO/work/epics/S5-E1…E6/status.md` (dòng Tester).

## Ca trọng điểm của chặng 5
| Nhóm | TC (đều đạt) |
|---|---|
| Quyền theo quan hệ (giáo viên khác, học sinh lớp khác) | TC-S5E1-06, TC-S5E2-27, TC-S5E3-07, TC-S5E3-11, TC-S5E3-16, TC-S5E4-11, TC-S5E4-25, TC-S5E5-08, TC-S5E5-22 |
| Không lộ đáp án (bộ `app/e2e/fixtures/firewall-s5.json` v1.0) | TC-S5E3-35: số lần lộ = 0 |
| Không lộ gì của AI / lời giải cho học sinh hoặc trước khi duyệt | TC-S5E4-36, TC-S5E5-02, TC-S5E6-23, TC-S5E6-25 |

## Ca trượt, chạy lại, chập chờn
- Trượt lần đầu: 0 (UI) và 0 (API). Không có ca cần chạy lại để tách lỗi thật / chập chờn.
- Chập chờn (đạt sau retry trong lượt, `GEO_E2E_RETRIES=1`): 0.
- Nhật ký máy chủ lượt API (`app/.data/e2e/server.log`): 0 dòng ERROR. Không có cảnh báo máy ngủ.

## Lỗi
Không phát hiện lỗi. `BUG-LIST.md` / `BUG-LIST.html` cùng thư mục ghi 0 mục; `check_buglist.py` ĐẠT.

| Mức | Số lỗi |
|---|---|
| Blocker | 0 |
| Major | 0 |
| Minor | 0 |
| **Tổng lỗi** | **0** |

## Manual (ngoài lượt máy)
- Chụp và nộp ảnh HEIC trên iPhone thật (US-S5E4-03): TC-S5E4-13 đã đạt qua API bằng tệp HEIC mẫu; phần thiết bị thật (camera iOS, Safari thật) ghi **Manual**, chưa chạy trong lượt này, cần người kiểm trên máy thật.

## Ghi chú
- Nhóm `_khong-ma-TC` của lượt API: 1 test `tests/s5e4/review/heic.api.spec.ts:45` "Ảnh gốc HEIC lưu đã gỡ EXIF/GPS, băm tính sau khi gỡ" không gắn mã TC, đạt. Không tính vào bảng theo epic; QC có thể gắn mã TC nếu muốn truy vết.
- Bằng chứng Playwright: `app/e2e/evidence/s5-ui/`, `app/e2e/evidence/s5-api/` (index.html, summary.json; không vào git).
- Không sửa code app, test, seed hay hạ tầng trong lượt này.

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-28 | Bản đầu: lượt s5-ui + s5-api, nhánh s5-test @ b283230 | runner-T-261128-66 |
