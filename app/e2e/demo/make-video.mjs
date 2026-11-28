#!/usr/bin/env node
// T-261128-73 (DEMO): WebM (Playwright) + lời đọc AIFF (macOS `say`) -> clip MP4 + video tổng + probe.txt + timeline.json.
//   node demo/make-video.mjs <thư mục ffmpeg-static/ffprobe-static node_modules> [outDir]
// Mỗi clip: H.264 yuv420p 1920x1080 30 fps, AAC 192k 48 kHz; lời đọc đặt đúng mốc phụ đề (adelay) rồi trộn.
// Video tổng = nối các clip theo thứ tự mã (00 mở đầu … 99 kết). Không cài gì vào dự án.
import { execFileSync } from 'node:child_process';
import { appendFileSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const here = path.dirname(new URL(import.meta.url).pathname);
const nm = process.argv[2];
if (!nm) throw new Error('cần đường dẫn node_modules có ffmpeg-static, ffprobe-static');
const FF = path.join(nm, 'ffmpeg-static', 'ffmpeg');
// ffprobe-static chỉ có bản x86_64 cho darwin (máy arm64 không Rosetta) -> dùng @ffprobe-installer/darwin-arm64.
const FP = path.join(nm, '@ffprobe-installer', `darwin-${process.arch}`, 'ffprobe');
const PROGRESS = process.env.GEO_DEMO_PROGRESS;
const RAW = path.resolve(here, '../test-results/demo-raw');
const OUT = path.resolve(process.argv[3] ?? path.resolve(here, '../../../artifacts/GEO/evidence/DEMO-20261128'));
const CLIPS = path.join(OUT, 'clips');
mkdirSync(CLIPS, { recursive: true });

// Mỗi lệnh ffmpeg có hạn 10 phút (tránh treo).
const run = (bin, args) => execFileSync(bin, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'], maxBuffer: 64 << 20, timeout: 600_000, killSignal: 'SIGKILL' });
const dur = (f) => Number(run(FP, ['-v', 'error', '-show_entries', 'format=duration', '-of', 'default=nw=1:nk=1', f]).trim());

const metas = readdirSync(RAW)
  .filter((n) => /^\d\d-.*\.json$/.test(n))
  .sort()
  .map((n) => JSON.parse(readFileSync(path.join(RAW, n), 'utf8')));

const timeline = [];
let offset = 0;
for (const m of metas) {
  const webm = path.join(RAW, `${m.id}.webm`);
  const out = path.join(CLIPS, `${m.id}.mp4`);
  const cues = m.cues.filter((c) => c.audio);
  const vdur = dur(webm); // độ dài hình: audio đệm đúng bằng độ dài này (không dùng apad vô hạn + -shortest: treo)
  const args = ['-y', '-v', 'error', '-i', webm];
  for (const c of cues) args.push('-i', c.audio);
  let fc = '';
  if (cues.length) {
    cues.forEach((c, i) => {
      const ms = Math.max(0, Math.round(c.t * 1000));
      fc += `[${i + 1}:a]aresample=48000,aformat=channel_layouts=stereo,adelay=${ms}:all=1[a${i}];`;
    });
    fc += `${cues.map((_, i) => `[a${i}]`).join('')}amix=inputs=${cues.length}:normalize=0,apad=whole_dur=${vdur.toFixed(3)},atrim=0:${vdur.toFixed(3)}[aout]`;
  } else {
    args.push('-f', 'lavfi', '-t', vdur.toFixed(3), '-i', 'anullsrc=r=48000:cl=stereo');
    fc += '[1:a]anull[aout]';
  }
  fc += ';[0:v]fps=30,scale=1920:1080:flags=lanczos,format=yuv420p[vout]';
  args.push('-filter_complex', fc, '-map', '[vout]', '-map', '[aout]', '-t', vdur.toFixed(3), '-c:v', 'libx264', '-preset', 'slow', '-crf', '23', '-tune', 'stillimage', '-g', '60',
    '-profile:v', 'high', '-r', '30', '-c:a', 'aac', '-b:a', '192k', '-ar', '48000', '-ac', '2', '-movflags', '+faststart', out);
  run(FF, args);
  const d = dur(out);
  timeline.push({ id: m.id, title: m.title, start: offset, duration: d, cues: m.cues.map((c) => ({ t: c.t, abs: offset + c.t, text: c.text })), failures: m.failures });
  offset += d;
  console.log(`${m.id}: ${d.toFixed(1)} s`);
  if (PROGRESS) appendFileSync(PROGRESS, `${new Date().toTimeString().slice(0, 5)} · mã hoá clip ${m.id} (${d.toFixed(1)} s) · đạt · -\n`);
}

const list = path.join(RAW, 'concat.txt');
writeFileSync(list, timeline.map((t) => `file '${path.join(CLIPS, `${t.id}.mp4`)}'`).join('\n') + '\n');
const full = path.join(OUT, 'GEO-MVP-demo-full-1080p.mp4');
run(FF, ['-y', '-v', 'error', '-f', 'concat', '-safe', '0', '-i', list, '-c', 'copy', '-movflags', '+faststart', full]);
const probe = run(FP, ['-v', 'error', '-show_entries', 'format=filename,duration,size,bit_rate:stream=index,codec_name,codec_type,profile,width,height,pix_fmt,r_frame_rate,sample_rate,channels,bit_rate', '-of', 'default=nw=0', full]);
writeFileSync(path.join(OUT, 'probe.txt'), probe.replace(OUT + '/', ''));
writeFileSync(path.join(RAW, 'timeline.json'), JSON.stringify({ total: dur(full), clips: timeline }, null, 2));
console.log(`tổng ${dur(full).toFixed(1)} s -> ${full}`);
