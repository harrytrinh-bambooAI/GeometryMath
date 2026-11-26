Phiên bản: v1.0 · 2026-11-26 · GEO (từ delivery-lead/profile_template v0.2)

# Profile dự án — GeometryMath (GEO)

`install.sh` chép tệp này vào `<repo>/.claude/delivery-profile.md`; điền tại đó. Skill KHÔNG chứa dữ kiện dự án; mọi dữ kiện nằm ở đây.
Ô để trống = dùng mặc định ghi trong ngoặc. **Bắt buộc** cho L0: §2 `check.build/test/lint`, §4. Còn lại tuỳ chọn (trống = mặc định / bỏ qua).
Profile do agent đọc; máy chỉ đọc: §4 (`queue.py brief` chèn vùng cấm), §2 + §4 + §7 `outputs` (`check_project.py`).

| status | approved 2026-11-26 Harry (chọn "Cài" + phương án Đủ phần máy) |
|---|---|

## 1. Dự án
| Khoá | Giá trị | Ghi chú |
|---|---|---|
| name | GEO | Hình Học Không Gian Cùng AI |
| repo | /Users/harry/IdeaProjects/GeometryMathSupport/.claude/worktrees/mvp-wave0 | nhánh tích hợp `worktree-mvp-wave0`; agent con ở `.claude/worktrees/<tên>` |
| owner | Harry (chủ đầu tư) | |
| reviewers | agent soát độc lập (Sonnet) | |
| language | (vi) | ngôn ngữ trả lời; thuật ngữ giữ tiếng Anh |
| alert | | lệnh đưa cửa sổ lên trước khi hỏi, ví dụ `osascript -e 'tell application "<IDE>" to activate'` |

## 2. Stack + lệnh kiểm
| Khoá | Giá trị |
|---|---|
| stack | BE Java Spring Boot (Maven) + PostgreSQL + Redis · FE React/Vite/TS · E2E Playwright |
| check.build | `cd app && make verify && make web-build` |
| check.test | `cd app && make e2e` (hai lượt liên tiếp) |
| check.lint | `cd app/web && npm run typecheck && npm run lint` |
| check.audit | `cd app && make traceability ARGS=--all` (0 TC tự động thiếu test) |

Chưa có code: ghi lệnh dự kiến kèm `(giả định)`, ví dụ `npm run build (giả định)`; task L4 thay bằng lệnh thật. Không để trống, không ghi TBD.

## 3. Môi trường
| Môi trường | URL / host | Quyền của Claude | Ghi chú |
|---|---|---|---|
| local | docker PG/Redis theo `app/.env.ports` | đọc / ghi | cổng 54329/6389/8080 là của checkout chính: không dùng |
| dev / test | | đọc / ghi trong scope dữ liệu | |
| staging (dùng chung) | | **chỉ đọc**, ghi cần owner | |
| prod | chưa có | **cấm** | |

## 4. Vùng cấm (dừng hỏi ngay, kể cả khi tự chạy)
| # | Vùng | Đọc? | Ghi? |
|---|---|---|---|
| F1 | prod | hỏi | cấm |
| F2 | môi trường dùng chung với team khác | (hỏi / được) | hỏi |
| F3 | dữ liệu ngoài scope (tenant / store / khách khác) | cấm | cấm |
| F4 | xoá không khôi phục được | — | hỏi |
| F5 | gửi ra ngoài (email, chat, PMS, khách hàng; push/merge git) | — | cấm trừ khi owner yêu cầu |
| F7 | gọi API AI thật (tốn phí) | — | chỉ khi owner đã đặt `GEO_AI_API_KEY` và giao task đo thật; không bao giờ commit key |
| F6 | vượt ngân sách token / thời gian của task | — | dừng |

## 5. Scope tự chạy khi owner vắng
| Khoá | Giá trị |
|---|---|
| auto_scope | danh sách epic / loại việc được tự chạy (ví dụ: review TC epic A, B; unit test module X) |
| away_trigger_min | (15) |
| night_window | (00:00–08:00) |
| data_scope | trỏ tới `scope.json` của tester (bản có hiệu lực); không chép dữ liệu scope vào đây |

## 6. Điều phối
| Khoá | Giá trị |
|---|---|
| small_task | delegate (Harry 26/11: mọi việc code → agent con trong worktree riêng; phiên chính chỉ điều phối) |
| max_agents | (Sonnet 4 · Opus 2) |
| default_model | opus cho code (PO-59: 2 agent song song), sonnet cho soát |
| task_id_source | (internal `T-YYMMDD-NN`) · PMS: tên + cách lấy ID |
| commit_policy | (commit sau mỗi bước đạt) · `none` = không commit, dùng phiên bản trong tệp |

## 7. Chỗ lưu (mặc định ở `framework.md` §2; chỉ ghi dòng đổi khi dự án đã có quy ước khác)
| Chỗ | Đường dẫn |
|---|---|
| outputs | (OUTPUTS.md) |

## 8. Skill liên quan (trống = không dùng; dự án khác thay bằng bộ của mình, vai cổng giữ nguyên)
| Skill | Đường dẫn | Dùng ở cổng | Lệnh lint / kiểm | Đường dẫn brief / input |
|---|---|---|---|---|
| bộ spec PO/BA/QC (ví dụ sg-po-ba-qc) | | L1–L3, change loop | | (brief.md) |
| goal-run | | L3 | | |
| tester (+ domain profile, scope.json) | | L6 | | |
| queue | (`<skill>/scripts/queue.py`) | L4–L6 | `queue.py validate` | (.claude/queue.md) |

## Lịch sử
| Phiên bản | Ngày | Thay đổi | Ai |
|---|---|---|---|
| v1.0 | 2026-11-26 | Điền cho GEO | phiên chính |
| v0.2 | 2026-11-26 | Cột bắt buộc / tuỳ chọn; status draft; lệnh kiểm khi chưa có code = `(giả định)`; check.audit gợi ý; data_scope trỏ scope.json; §7 chỉ ghi dòng đổi; §8 thêm lệnh lint + đường dẫn brief (review #26, #27, #31; tryout) | agent skill-fix |
| v0.1 | 2026-11-26 | Tạo | agent skill-author |
