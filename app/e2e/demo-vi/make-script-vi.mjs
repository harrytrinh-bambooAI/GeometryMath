#!/usr/bin/env node
// T-261128-75 (DEMO-VI) — sinh SCRIPT-VI.md từ scenes.json + voice/lines.tsv (+ timeline nếu đã dựng video).
//   cd app/e2e && node demo-vi/make-script-vi.mjs        (make-video-vi.mjs cũng gọi lại hàm này kèm mốc thời gian)
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { EVIDENCE, MAX_LINE, SCENES, cues, readLines } from './lines.mjs';

const mmss = (t) => `${String(Math.floor(t / 60)).padStart(2, '0')}:${String(Math.floor(t % 60)).padStart(2, '0')}`;

export function writeScript(timeline = null, total = null, notes = []) {
  const lines = new Map(readLines().map((l) => [l.code, l]));
  const tl = new Map((timeline ?? []).map((s) => [s.code, s]));
  const out = [];
  out.push('# SCRIPT-VI — Video giới thiệu "Hình Học Không Gian Cùng AI — presented by BambooAI" (T-261128-75)');
  out.push('');
  out.push(`Phiên bản: v1.0 · 2026-11-28 · sinh tự động bởi \`app/e2e/demo-vi/make-script-vi.mjs\` (không sửa tay: sửa \`voice/lines.tsv\` hoặc \`app/e2e/demo-vi/scenes.json\` rồi chạy lại).`);
  out.push('');
  out.push(`- Tổng thời lượng bản nháp: ${total ? `${mmss(total)} (${total.toFixed(1)} s)` : 'chưa dựng'} · ${SCENES.scenes.length} cảnh · tiếng Việt, phụ đề VI burn-in (≤ 2 dòng × ${MAX_LINE} ký tự).`);
  out.push('- Style: BambooAI như video cũ (`Video/GeometryMath.mp4`, `Video/slides/*.png`): nền xanh lá đậm gradient, nhãn "PRESENTED BY BAMBOOAI" vàng chanh, thẻ kính có icon, slide chương "0N <TÊN>". Thông điệp: "AI is powerful. But thinking is still human." · "AI executes, humans decide" · contact@bamboox.ai.');
  out.push('- Dữ liệu thật: bài HKG-01..06 + gợi ý + lời giải từng bước của `materials/Ngan-hang-bai-tap-Hinh-khong-gian.xlsx` (đã có trong seed); định hướng và tiến trình của `materials/Giao-an-Hinh-khong-gian-11A1.docx`; giáo án chọn PPCT bài 24 "Phép chiếu vuông góc. Góc giữa đường thẳng và mặt phẳng" (đúng chủ đề giáo án 11A1).');
  out.push('- **Ảnh bài làm viết tay là ảnh MINH HOẠ** (`assets/bai-lam-hkg01-p1/p2[-photo].png|jpg`: chữ viết tay font Noteworthy trên giấy kẻ, hình chóp vẽ tay, học sinh giả "Nguyễn Minh Khang"). Owner thay ảnh chụp thật: chép đè `assets/bai-lam-hkg01-p1-photo.jpg`, `-p2-photo.jpg` (và `-p1-photo.png` cho slide 19) CÙNG TÊN, chạy `GEO_VI_KEEP_PHOTOS=1 node demo-vi/make-assets-vi.mjs` (giữ ảnh, chỉ dựng lại slide), quay lại cảnh 18, 20 rồi dựng video.');
  out.push('- AI giả lập (MockAiProvider) trong mọi cảnh. Câu trả lời hỏi đáp (cảnh 11) lấy từ fixture demo riêng `app/e2e/demo-vi/fixtures/qa-hkg01.json` (không đụng fixture e2e).');
  out.push('- Giọng đọc TẠM: macOS "Linh" (vi_VN). Thay giọng: gen bằng Google TTS theo `voice/lines.tsv`, thả `voice/<mã cảnh>.mp3|wav` CÙNG TÊN (tệp mới nhất thắng), chạy lại `node demo-vi/make-video-vi.mjs <node_modules ffmpeg>`: thời lượng cảnh và phụ đề tự khớp.');
  out.push('');
  out.push('## Lệnh dựng lại');
  out.push('');
  out.push('```bash');
  out.push('cd app && bash e2e/demo/stack-up.sh                       # máy chủ dev + web e2e trên cổng của worktree (AI giả lập)');
  out.push('cd e2e && node demo-vi/make-assets-vi.mjs <ffmpeg>        # slide PNG + ảnh viết tay minh hoạ');
  out.push('node demo-vi/gen-voice.mjs <ffmpeg>                       # giọng tạm (bỏ qua cảnh đã có tệp giọng)');
  out.push('GEO_PORT=… GEO_WEB_PORT=… npx playwright test -c demo-vi/demo.config.ts   # quay clip câm');
  out.push('node demo-vi/make-video-vi.mjs <node_modules có ffmpeg-static + @ffprobe-installer>');
  out.push('```');
  out.push('');
  if (notes.length) {
    out.push('## Ghi chú lần dựng');
    out.push('');
    for (const x of notes) out.push(`- ${x}`);
    out.push('');
  }
  out.push('## Bảng cảnh');
  out.push('');
  out.push('| Mốc | Cảnh | Chương | Loại | Hình |');
  out.push('|---|---|---|---|---|');
  for (const sc of SCENES.scenes) {
    const t = tl.get(sc.code);
    out.push(`| ${t ? mmss(t.start) : '—'} | ${sc.code} | ${sc.chapter} | ${t?.kind ?? sc.kind} | ${sc.kind === 'slide' || t?.kind === 'slide' ? `slides/${sc.slide}.png` : `clip (demo-vi)`} |`);
  }
  out.push('');
  out.push('## Kịch bản từng cảnh');
  let chap = '';
  for (const sc of SCENES.scenes) {
    if (sc.chapter !== chap) {
      chap = sc.chapter;
      out.push('');
      out.push(`### ${chap}`);
    }
    const l = lines.get(sc.code);
    const t = tl.get(sc.code);
    out.push('');
    out.push(`#### ${sc.code}${t ? ` · ${mmss(t.start)}–${mmss(t.start + t.duration)} (${t.duration.toFixed(1)} s, giọng ${t.voiceDur.toFixed(1)} s)` : ''}`);
    out.push('');
    out.push(`- **Hình / thao tác quay:** ${sc.action}`);
    out.push(`- **Dữ liệu dùng:** ${sc.data}`);
    out.push(`- **Tệp giọng:** \`voice/${sc.code}.wav\` (thay được bằng \`.mp3\` cùng tên)`);
    out.push(`- **Lời đọc (VI):** ${l?.voice ?? '—'}`);
    out.push('- **Phụ đề (VI):**');
    const cs = t?.cues ?? cues(l?.sub ?? '').map((c) => ({ lines: c }));
    for (const c of cs) out.push(`  - ${c.start != null ? `\`${mmss(c.start)}\` ` : ''}${c.lines.join(' / ')}`);
  }
  out.push('');
  out.push('## Lịch sử');
  out.push('');
  out.push('| Phiên bản | Ngày | Thay đổi | Ai |');
  out.push('|---|---|---|---|');
  out.push('| v1.0 | 2026-11-28 | Bản đầu: 27 cảnh, 12 slide, giọng tạm Linh, bản nháp video | runner-T-261128-75 |');
  writeFileSync(path.join(EVIDENCE, 'SCRIPT-VI.md'), out.join('\n') + '\n');
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const f = path.join(EVIDENCE, 'timeline-vi.json');
  const t = existsSync(f) ? JSON.parse(readFileSync(f, 'utf8')) : null;
  writeScript(t?.scenes ?? null, t?.total ?? null, t?.notes ?? []);
  console.log('SCRIPT-VI.md');
}
