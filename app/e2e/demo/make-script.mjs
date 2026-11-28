#!/usr/bin/env node
// T-261128-73 (DEMO): sinh SCRIPT.md từ test-results/demo-raw/timeline.json (mốc thời gian thật trong video tổng) + mô tả
// tĩnh từng kịch bản bên dưới. node demo/make-script.mjs [outDir]
import { readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const here = path.dirname(new URL(import.meta.url).pathname);
const OUT = path.resolve(process.argv[2] ?? path.resolve(here, '../../../artifacts/GEO/evidence/DEMO-20261128'));
const tl = JSON.parse(readFileSync(path.resolve(here, '../test-results/demo-raw/timeline.json'), 'utf8'));
const voice = JSON.parse(readFileSync(path.resolve(here, '../test-results/demo-raw/00-intro.json'), 'utf8')).voice ?? 'Samantha';

/** Mô tả tĩnh: mục tiêu, vai, tính năng, dữ liệu, epic. */
const META = {
  '00-intro': { stage: '—', epic: '—', goal: 'Giới thiệu sản phẩm và BambooAI: vấn đề, người dùng, nguyên tắc, lộ trình chặng 1–5', role: '—', feat: 'Màn tiêu đề "Hình Học Không Gian Cùng AI — Geometry Math Support", "by BambooAI"', data: 'DOMAIN-PROFILE.md §1 + bảng chặng; BambooAI chỉ dùng tên (không có logo trong repo, không số liệu/khách hàng)' },
  '01-gv-kho-de-giao-bai': { stage: '1 (MVP)', epic: 'E5, E2/E3, E6', goal: 'Giáo viên tìm đề, xem chi tiết đề, giao bài', role: 'Giáo viên', feat: 'Kho đề + bộ lọc; chi tiết đề (hình 3D xoay, đáp số khớp tính toán, lời giải từng bước, gợi ý); giao bài cho lớp', data: 'Kho thử F-GEO-080 (HKG-01…48); sandbox E6 (lớp 11A1, 9B, HKG-06)' },
  '02-hs-vao-lop-lam-bai': { stage: '1 (MVP)', epic: 'E1, E3, E4', goal: 'Học sinh vào lớp, làm bài trên hình 3D, xem gợi ý', role: 'Học sinh', feat: 'Vào lớp bằng tên + mã lớp; "Bài của em"; màn làm bài; gợi ý mức 1', data: 'Lớp mẫu mã HK7M2A (seed vàng); sandbox E4 (HS "Hà", HKG-01)' },
  '03-gv-theo-doi-lop': { stage: '1 (MVP)', epic: 'E7', goal: 'Giáo viên theo dõi lớp', role: 'Giáo viên', feat: 'Bốn thẻ tổng, bảng học sinh (không xếp hạng)', data: 'Hộp E7 golden() (5 HS)' },
  '04-gv-hs-bai-luyen': { stage: '2', epic: 'S2-E8', goal: 'Soạn bài luyện chấm tự động; học sinh luyện', role: 'Giáo viên, Học sinh', feat: 'Chọn đề có đáp số, lưu nháp; bàn phím toán, chấm ngay, gợi ý mức 1, trừ lượt', data: 'Sandbox S2-E8 (Cô Hạnh, HS An, bộ BL-1 "Góc trong hình chóp")' },
  '05-gv-trinh-chieu': { stage: '2', epic: 'S2-E7', goal: 'Trình chiếu lời giải từng bước', role: 'Giáo viên', feat: 'Toàn màn, che lời giải, hiện bước + tô màu hình, phím mũi tên', data: 'Đề FX-14 của làn S2-E7 (Cô Trang)' },
  '06-gv-lop-hoc-ket-qua': { stage: '3', epic: 'S3-E1, S3-E3', goal: 'Quản lý lớp và bảng kết quả', role: 'Giáo viên', feat: 'Lớp có mã HK-xxxx; Đang học / Chờ duyệt / Đã rời; bảng lớp × bài, bằng chứng', data: 'Thế giới S3-E1 (Cô Lan, 11A1, 11A2); sandbox S3-E3 class051 ("Bộ bài tuần 15")' },
  '07-gv-tong-quan': { stage: '3', epic: 'S3-E4', goal: 'Tổng quan giáo viên', role: 'Giáo viên', feat: 'Ba thẻ: bài đang giao, lời giải đang khóa, em chờ duyệt', data: 'fullWorld S3-E4 (11A1, 8 HS Đang học)' },
  '08-s5e1-giao-an': { stage: '5', epic: 'S5-E1', goal: 'Trợ lý soạn giáo án theo PPCT', role: 'Giáo viên', feat: 'Form C5-08 (chương 7, bài 26, tiết 1/2) tự điền số tiết + kiến thức; AI soạn nháp; nhãn "AI soạn"', data: 'Thế giới S3-E1 (Cô Lan); AI giả lập lesson_plan mặc định (F-S5-093)' },
  '09-s5e2-ra-de-ma-tran': { stage: '5', epic: 'S5-E2', goal: 'Ra đề theo ma trận, nhiều mã đề', role: 'Giáo viên', feat: 'Form ma trận KT15-HHKG11; bộ đề "Máy lắp" 4 mã; nhãn hai trục; thay câu cùng ô', data: 'Ngân hàng materials/de-thi (móc s5e2/import); Cô Lan F-S5-001; seed 2026' },
  '10-s5e2-de-nhap-tai-lieu': { stage: '5', epic: 'S5-E2', goal: 'Đề nháp từ tài liệu', role: 'Giáo viên', feat: 'AI soạn đề từ TL-01 v2; engine tính đáp số; đề chưa kiểm chứng xếp cuối, không chọn sẵn', data: 'Thế giới S5-E3 (Cô Lan, 11A1); TL-01 "Ghi chú Khoảng cách 11A1" v2; AI giả lập exam_draft' },
  '11-s5e3-kho-tri-thuc-hoi-dap': { stage: '5', epic: 'S5-E3', goal: 'Kho tri thức và hỏi đáp có nguồn', role: 'Giáo viên, Học sinh', feat: 'Kho tài liệu có phiên bản; học sinh hỏi, trả lời kèm nguồn, mở đúng mục', data: 'qa.setup (Cô Lan, Bình An, BC-01, TL-01); AI giả lập F-S5-094' },
  '12-s5e4-rubric-nop-bai': { stage: '5', epic: 'S5-E4', goal: 'Rubric và nộp bài giấy', role: 'Giáo viên, Học sinh', feat: 'Rubric C5-02 tổng sống, chặn lưu khi lệch; học sinh chụp 2 trang (kiểm độ rõ), nộp (MSG-S5-33)', data: 'Thế giới S5-E4; RB-01; BC-01; ảnh mẫu bn01-p1/p2.jpg', trial: true },
  '13-s5e4-hang-cho-cham-tay': { stage: '5', epic: 'S5-E4', goal: 'Hàng chờ và chấm tay theo rubric', role: 'Giáo viên', feat: 'Hàng chờ C5-04 (cờ trước, không xếp hạng); màn chấm C5-05, tổng tự tính 7/10, Duyệt → bài kế', data: 'gradingWorld + sc1 (BN-01…05); BN-02 của Chi Mai', trial: true },
  '14-s5e5-thong-bao-cong-tac': { stage: '5', epic: 'S5-E5', goal: 'Thông báo do trợ lý soạn, công tắc AI, hạn mức', role: 'Giáo viên', feat: 'Nháp "AI soạn" nhắc hạn BC-01, sửa câu cuối, gửi 8 em; khối "AI của lớp": công tắc + lượt 30 ngày', data: 'Thế giới S5-E5 (11A1, 8 HS Đang học + Văn Tú Đã rời); AI giả lập class_notice' },
  '15-s5e5-loi-sai-lop': { stage: '5', epic: 'S5-E5', goal: 'Tổng hợp lỗi sai của lớp', role: 'Giáo viên', feat: '"Cả lớp hay sai ở đâu" kèm căn cứ; gợi ý bộ luyện nhắc lại; bấm căn cứ mở bài làm', data: 'insights.graded() — 6 bài đã chấm qua luồng thật S5-E4/E6', trial: true },
  '16-s5e6-ai-de-xuat-diem': { stage: '5', epic: 'S5-E6', goal: 'AI đề xuất điểm, giáo viên nhận từng tiêu chí', role: 'Giáo viên', feat: 'Điểm AI từng tiêu chí, khoanh vùng ảnh, nhận 5 tiêu chí, Duyệt 10/10', data: 's5e6b.setup("ai_proposal"); AI giả lập F-S5-090 (BN-01 Bình An)', trial: true },
  '17-s5e6-song-song-bao-cao': { stage: '5', epic: 'S5-E6', goal: 'Chạy song song và báo cáo khớp AI–GV', role: 'Giáo viên', feat: 'Song song: ẩn điểm AI, chấm tay, so sánh sau duyệt; báo cáo KPI khớp (API /ai-grading/report hiển thị dạng bảng — chưa có màn riêng)', data: 's5e6b.setup("parallel"), F-S5-091; móc s5e6/kpi-seed 20 bài (15 khớp + 5 lệch 1 điểm)', trial: true },
  '99-outro': { stage: '—', epic: '—', goal: 'Tóm tắt + BambooAI', role: '—', feat: 'Màn kết', data: '—' },
};

const mmss = (s) => `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(Math.floor(s % 60)).padStart(2, '0')}`;
const esc = (s) => String(s).replace(/\|/g, '\\|');
const L = [];
L.push('# SCRIPT — Video giới thiệu MVP + chặng 2, 3, 5 (T-261128-73)');
L.push('');
L.push(`Phiên bản: v1.0 · 2026-11-28 · runner-T-261128-73`);
L.push('');
L.push(`- Video tổng: \`GEO-MVP-demo-full-1080p.mp4\` — ${mmss(tl.total)} (${tl.total.toFixed(1)} s), H.264 1920x1080 30 fps yuv420p, AAC 192k 48 kHz. Clip: \`clips/NN-*.mp4\` (${tl.clips.length} clip).`);
L.push(`- Giọng đọc tiếng Anh: macOS \`say\` offline, giọng **${voice}** (en_US), tốc độ 178 từ/phút; mỗi cảnh giữ màn đúng độ dài lời đọc (đo AIFF trước, \`afinfo\`). Phụ đề trên màn = lời đọc (tiếng Anh); nhãn giao diện app giữ tiếng Việt.`);
L.push('- Môi trường: worktree demo-video, profile dev, DB geo_e2e (seed vàng + móc /test-hooks), AI giả lập (MockAiProvider, không gọi AI thật). Cờ `geo.s5.grading` / `geo.s5.ai-grading` bật sẵn trong application-dev.yml (chỉ phiên dev local); clip E4/E6 (và E5 lỗi sai lớp) có nhãn góc "Stage 5 preview — off in release builds".');
L.push('- Dữ liệu: chỉ tên giả của fixture (Cô Lan, Bình An, Chi Mai, …); không dữ liệu học sinh thật.');
L.push('- Quay: Playwright `app/e2e/demo/*.demo.ts` (cấu hình riêng `demo.config.ts`, không thuộc bộ e2e), ghép bằng `make-video.mjs` (ffmpeg-static ngoài repo).');
L.push('');
L.push('## Bảng phủ epic → clip');
L.push('');
L.push('| Chặng | Epic | Clip | Mốc trong video tổng |');
L.push('|---|---|---|---|');
for (const c of tl.clips) {
  const m = META[c.id] ?? {};
  L.push(`| ${m.stage ?? ''} | ${esc(m.epic ?? '')} | ${c.id} | ${mmss(c.start)}–${mmss(c.start + c.duration)} |`);
}
L.push('');
L.push('Chặng 5 đủ 6 epic: S5-E1 → 08 · S5-E2 → 09, 10 · S5-E3 → 11 · S5-E4 → 12, 13 · S5-E5 → 14, 15 · S5-E6 → 16, 17. Chặng 4 (môn thứ hai) chưa làm nên không có clip.');
L.push('');
L.push('## Kịch bản từng clip');
for (const c of tl.clips) {
  const m = META[c.id] ?? {};
  L.push('');
  L.push(`### ${c.id} — ${c.title}`);
  L.push('');
  L.push(`- Mốc: **${mmss(c.start)}–${mmss(c.start + c.duration)}** (dài ${c.duration.toFixed(1)} s) · Chặng ${m.stage ?? '?'} · Epic ${m.epic ?? '?'}${m.trial ? ' · nhãn bản thử' : ''}`);
  L.push(`- Mục tiêu: ${m.goal ?? ''}`);
  L.push(`- Vai: ${m.role ?? ''}`);
  L.push(`- Tính năng thể hiện: ${m.feat ?? ''}`);
  L.push(`- Dữ liệu dùng: ${m.data ?? ''}`);
  L.push('');
  L.push('| Mốc (tổng) | Mốc (clip) | Voice-over (EN) = phụ đề |');
  L.push('|---|---|---|');
  for (const q of c.cues) L.push(`| ${mmss(q.abs)} | ${q.t.toFixed(1)} s | ${esc(q.text)} |`);
  if (c.failures?.length) {
    L.push('');
    L.push(`Bước lỗi khi quay: ${c.failures.map(esc).join('; ')}`);
  }
}
L.push('');
L.push('## Không quay được');
L.push('');
const fails = tl.clips.flatMap((c) => c.failures ?? []);
L.push(fails.length ? fails.map((f) => `- ${f}`).join('\n') : '- Không có màn nào lỗi khi quay. Ghi chú: báo cáo khớp AI–GV (US-S5E6-06) chưa có màn giao diện riêng — clip 17 hiển thị dữ liệu thật từ API `/ai-grading/report` dạng bảng.');
L.push('');
L.push('## Lịch sử');
L.push('');
L.push('| Phiên bản | Ngày | Thay đổi | Ai |');
L.push('|---|---|---|---|');
L.push('| v1.0 | 2026-11-28 | Bản đầu: 19 clip (mở đầu, chặng 1–3, chặng 5 đủ 6 epic, kết), giọng đọc tiếng Anh, mở đầu "by BambooAI" | runner-T-261128-73 |');
writeFileSync(path.join(OUT, 'SCRIPT.md'), L.join('\n') + '\n');
console.log('SCRIPT.md ok');
