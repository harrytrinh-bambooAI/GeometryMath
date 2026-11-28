#!/usr/bin/env node
// T-261128-77 (DEMO-VI) · v1.0 · 2026-11-28 — bộ video HƯỚNG DẪN SỬ DỤNG.
//   cd app/e2e && node demo-vi/make-guides.mjs <node_modules ffmpeg-static + @ffprobe-installer> [lines|voice|build|index|all] [--only <id>]
//   lines : xuất guides/voice/<id>/lines.tsv từ guides/guides.json (KHÔNG ghi đè tệp đã có — owner sửa lời ở lines.tsv)
//   voice : giọng tạm macOS Linh cho dòng chưa có tệp giọng (thả .mp3/.wav cùng tên để thay)
//   build : dựng guides/<id>.mp4 + guides/<id>.srt (slide mở → các bước cắt theo mốc demo.chapter → slide tóm tắt) + guides/frames/<id>.jpg
//   index : GUIDE.md (mục lục: tên video, vai, thời lượng, các bước, mẹo)
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { buildVideo, probeDur, tools, voiceFile } from './build-lib.mjs';
import { genVoice } from './gen-voice.mjs';
import { EVIDENCE, readLines } from './lines.mjs';

const here = path.dirname(new URL(import.meta.url).pathname);
const t = tools(process.argv[2]);
const cmd = process.argv[3] && !process.argv[3].startsWith('--') ? process.argv[3] : 'all';
const oi = process.argv.indexOf('--only');
const only = oi > 0 ? process.argv[oi + 1] : null;
const G = JSON.parse(readFileSync(path.join(here, 'guides', 'guides.json'), 'utf8')).guides.filter((g) => !only || g.id === only);
const OUT = path.join(EVIDENCE, 'guides');
const RAW = path.resolve(here, '../test-results/demo-vi-guides-raw');
const linesFile = (g) => path.join(OUT, 'voice', g.id, 'lines.tsv');
mkdirSync(path.join(OUT, 'frames'), { recursive: true });

function exportLines() {
  for (const g of G) {
    const f = linesFile(g);
    if (existsSync(f)) continue;
    mkdirSync(path.dirname(f), { recursive: true });
    const rows = [
      `# Phiên bản: v1.0 · 2026-11-28 · T-261128-77 · video ${g.id}. Cột: mã <TAB> lời đọc [<TAB> phụ đề nếu khác]. Giọng: <mã>.wav|.mp3 cùng thư mục (tệp mới nhất thắng).`,
      `00-mo-dau\t${g.intro}`,
      ...g.steps.map((s) => `${s.code}\t${s.voice}`),
      `99-tom-tat\t${g.outro}`,
    ];
    writeFileSync(f, rows.join('\n') + '\n');
    console.log(`lines ${g.id}`);
  }
}

function voices() {
  for (const g of G) genVoice(t.FF, linesFile(g), { log: () => {} });
  console.log(`giọng: ${G.length} video`);
}

function build() {
  const notes = [];
  for (const g of G) {
    const lines = new Map(readLines(linesFile(g)).map((l) => [l.code, l]));
    const vdir = path.dirname(linesFile(g));
    const metaF = path.join(RAW, `${g.id}.json`);
    const meta = existsSync(metaF) ? JSON.parse(readFileSync(metaF, 'utf8')) : { marks: [], end: 0, failures: ['chưa quay'] };
    const webm = path.join(RAW, `${g.id}.webm`);
    // mốc đo theo đồng hồ của kit; WebM bắt đầu muộn hơn một khoảng off = end − độ dài WebM → trừ đi để cắt đúng bước
    const off = meta.firstNav != null ? meta.firstNav : existsSync(webm) && meta.end ? Math.max(0, meta.end - probeDur(t.FP, webm)) : 0;
    const mark = new Map(meta.marks.map((m) => [m.code, Math.max(0, m.t - off)]));
    const segs = [{ code: '00-mo-dau', png: path.join(OUT, 'slides', `${g.id}-mo.png`), voice: voiceFile(vdir, '00-mo-dau'), sub: lines.get('00-mo-dau').sub }];
    g.steps.forEach((s, i) => {
      const from = mark.get(s.code);
      const next = g.steps[i + 1] ? mark.get(g.steps[i + 1].code) : undefined;
      segs.push({
        code: s.code,
        png: path.join(OUT, 'slides', `${g.id}-mo.png`),
        webm: from != null ? webm : undefined,
        from: from != null ? Math.max(0, from - 0.2) : undefined,
        // kết thúc 0,8 s trước mốc bước sau: khung cuối (giữ đứng khi giọng dài hơn) vẫn là của bước này
        to: from != null ? Math.max(from + 0.5, (next ?? Math.max(0, meta.end - off)) - 0.8) : undefined,
        voice: voiceFile(vdir, s.code),
        sub: lines.get(s.code).sub,
      });
      if (from == null) notes.push(`${g.id} ${s.code}: không có mốc → slide`);
    });
    segs.push({ code: '99-tom-tat', png: path.join(OUT, 'slides', `${g.id}-tom-tat.png`), voice: voiceFile(vdir, '99-tom-tat'), sub: lines.get('99-tom-tat').sub });
    const mp4 = path.join(OUT, `${g.id}.mp4`);
    const r = buildVideo({ t, segments: segs, work: path.join(RAW, 'build', g.id), mp4, srt: path.join(OUT, `${g.id}.srt`), tail: { slide: 1.4, clip: 1.0 }, extra: 6, log: () => {} });
    // 1 khung/video (giữa bước cuối) + khung từng bước để soát (thư mục tạm)
    const last = r.timeline[r.timeline.length - 2];
    const at = last.start + last.duration * 0.6;
    execFileSync(t.FF, ['-y', '-v', 'error', '-ss', at.toFixed(2), '-i', mp4, '-frames:v', '1', '-vf', 'scale=960:-1', path.join(OUT, 'frames', `${g.id}.jpg`)]);
    const chk = path.join(RAW, 'check', g.id);
    mkdirSync(chk, { recursive: true });
    for (const s of r.timeline.filter((x) => x.kind === 'clip'))
      execFileSync(t.FF, ['-y', '-v', 'error', '-ss', (s.start + s.duration * 0.7).toFixed(2), '-i', mp4, '-frames:v', '1', '-vf', 'scale=640:-1', path.join(chk, `${s.code}.jpg`)]);
    notes.push(...r.notes.map((n) => `${g.id} ${n}`), ...(meta.failures ?? []).map((f) => `${g.id} lỗi quay: ${f}`));
    console.log(`${g.id}: ${r.total.toFixed(1)} s`);
  }
  writeFileSync(path.join(OUT, 'build-notes.txt'), notes.join('\n') + '\n');
  for (const n of notes) console.log(`ghi chú: ${n}`);
}

function index() {
  const all = JSON.parse(readFileSync(path.join(here, 'guides', 'guides.json'), 'utf8')).guides;
  const mmss = (s) => `${Math.floor(s / 60)}:${String(Math.round(s % 60)).padStart(2, '0')}`;
  const out = [
    '# GUIDE — Bộ video hướng dẫn sử dụng "Hình Học Không Gian Cùng AI"',
    '',
    'Phiên bản: v1.0 · 2026-11-28 · T-261128-77 · sinh bởi `app/e2e/demo-vi/make-guides.mjs index` (sửa lời ở `guides/voice/<id>/lines.tsv`, cấu trúc ở `app/e2e/demo-vi/guides/guides.json`).',
    '',
    'Mỗi video một tác vụ, xem độc lập: slide mở "Hướng dẫn · <tác vụ>" → thao tác từng bước (nhãn "Bước N" + khoanh nút cần bấm) → slide tóm tắt + mẹo. Lời đọc và phụ đề tiếng Việt (phụ đề burn-in + tệp `.srt` riêng). Dữ liệu mẫu, AI giả lập. Giọng tạm macOS "Linh": thay bằng Google TTS bằng cách thả `<mã>.mp3` cùng tên vào `guides/voice/<id>/` rồi chạy `node demo-vi/make-guides.mjs <node_modules> build --only <id>`.',
    '',
    '| # | Video | Vai | Thời lượng | Các bước | Tệp |',
    '|---|---|---|---|---|---|',
  ];
  for (const g of all) {
    const f = path.join(OUT, `${g.id}.mp4`);
    const d = existsSync(f) ? mmss(probeDur(t.FP, f)) : '—';
    out.push(`| ${g.id.slice(0, 2)} | Hướng dẫn · ${g.title} | ${g.role} | ${d} | ${g.steps.map((s, i) => `${i + 1}. ${s.title}`).join('<br>')} | \`guides/${g.id}.mp4\` · \`.srt\` |`);
  }
  out.push('', '## Chi tiết từng video (để đặt vào trang trợ giúp)');
  for (const g of all) {
    out.push('', `### ${g.id.slice(0, 2)} · ${g.title} (${g.role})`, '', g.intro, '');
    g.steps.forEach((s, i) => out.push(`${i + 1}. **${s.title}** — ${s.voice.replace(/^Bước [^.]+\.\s*/, '')}`));
    out.push('', `**Mẹo:** ${g.tips.join(' · ')}`);
  }
  out.push('', '## Lịch sử', '', '| Phiên bản | Ngày | Thay đổi | Ai |', '|---|---|---|---|', '| v1.0 | 2026-11-28 | Bản đầu: 17 video (12 GV, 5 HS) | runner-T-261128-77 |');
  writeFileSync(path.join(EVIDENCE, 'GUIDE.md'), out.join('\n') + '\n');
  console.log('GUIDE.md');
}

if (cmd === 'lines' || cmd === 'all') exportLines();
if (cmd === 'voice' || cmd === 'all') voices();
if (cmd === 'build' || cmd === 'all') build();
if (cmd === 'index' || cmd === 'all') index();
