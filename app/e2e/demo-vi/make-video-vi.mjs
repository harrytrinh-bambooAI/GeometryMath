#!/usr/bin/env node
// T-261128-75 (DEMO-VI) · v1.0 · 2026-11-28 — dựng video giới thiệu tiếng Việt:
//   cd app/e2e && node demo-vi/make-video-vi.mjs <thư mục node_modules có ffmpeg-static + @ffprobe-installer>
// Thứ tự cảnh: demo-vi/scenes.json. Lời đọc: evidence voice/<mã cảnh>.(wav|mp3) — tệp MỚI NHẤT thắng (owner thả bản Google TTS
// cùng tên là xong). Thời lượng mỗi cảnh = ĐỆM_ĐẦU + độ dài giọng + ĐỆM_CUỐI, nên đổi giọng rồi chạy lại là tự khớp.
//   - cảnh slide: ảnh slides/<slide>.png đứng yên;
//   - cảnh clip: WebM câm test-results/demo-vi-raw/<mã>.webm; ngắn hơn giọng thì giữ khung cuối, dài hơn thì cho dài thêm
//     tối đa EXTRA giây rồi tăng tốc (≤ 2,5×) cho vừa; thiếu clip thì dùng slide dự phòng của cảnh (ghi vào báo cáo).
// Ra: subtitles-vi.srt (≤ 2 dòng × 42 ký tự, mốc theo độ dài giọng), GEO-intro-vi-DRAFT-1080p.mp4 (H.264 + AAC, phụ đề burn-in),
//     probe-draft.txt, timeline-vi.json, SCRIPT-VI.md (sinh lại kèm mốc thời gian).
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { EVIDENCE, SCENES, cues, readLines } from './lines.mjs';
import { writeScript } from './make-script-vi.mjs';

const here = path.dirname(new URL(import.meta.url).pathname);
const nm = process.argv[2];
if (!nm) throw new Error('cần đường dẫn node_modules có ffmpeg-static, @ffprobe-installer');
const FF = path.join(nm, 'ffmpeg-static', 'ffmpeg');
const FP = path.join(nm, '@ffprobe-installer', `darwin-${process.arch}`, 'ffprobe');
const RAW = path.resolve(here, '../test-results/demo-vi-raw');
const WORK = path.join(RAW, 'build');
mkdirSync(WORK, { recursive: true });
const LEAD = 0.6;
const TAIL = { slide: 2.0, clip: 1.6 };
const EXTRA = 8; // clip dài hơn giọng: cho cảnh dài thêm tối đa 8 s trước khi tăng tốc
const FADE = 0.25;

const run = (bin, args, opts = {}) =>
  execFileSync(bin, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'], maxBuffer: 64 << 20, timeout: 900_000, killSignal: 'SIGKILL', ...opts });
const dur = (f) => Number(run(FP, ['-v', 'error', '-show_entries', 'format=duration', '-of', 'default=nw=1:nk=1', f]).trim());
const srtTime = (t) => {
  const ms = Math.max(0, Math.round(t * 1000));
  const h = String(Math.floor(ms / 3600000)).padStart(2, '0');
  const m = String(Math.floor((ms % 3600000) / 60000)).padStart(2, '0');
  const s = String(Math.floor((ms % 60000) / 1000)).padStart(2, '0');
  return `${h}:${m}:${s},${String(ms % 1000).padStart(3, '0')}`;
};

const lines = new Map(readLines().map((l) => [l.code, l]));
const voiceFile = (code) => {
  const c = ['mp3', 'wav'].map((e) => path.join(EVIDENCE, 'voice', `${code}.${e}`)).filter(existsSync);
  if (!c.length) throw new Error(`thiếu tệp giọng voice/${code}.wav|mp3 (chạy demo-vi/gen-voice.mjs)`);
  return c.sort((a, b) => statSync(b).mtimeMs - statSync(a).mtimeMs)[0];
};

const timeline = [];
const notes = [];
const srt = [];
let offset = 0;
let n = 0;
for (const sc of SCENES.scenes) {
  const l = lines.get(sc.code);
  if (!l) throw new Error(`lines.tsv thiếu mã cảnh ${sc.code}`);
  const vf = voiceFile(sc.code);
  const vdur = dur(vf);
  const webm = path.join(RAW, `${sc.code}.webm`);
  const useClip = sc.kind === 'clip' && existsSync(webm);
  if (sc.kind === 'clip' && !useClip) notes.push(`${sc.code}: chưa có clip quay → dùng slide dự phòng ${sc.slide}`);
  let D = LEAD + vdur + TAIL[useClip ? 'clip' : 'slide'];
  const out = path.join(WORK, `${sc.code}.mp4`);
  const args = ['-y', '-v', 'error'];
  let vf0;
  if (useClip) {
    const SKIP = 0.6; // bỏ khung trắng lúc mở trang
    const C = Math.max(0.5, dur(webm) - SKIP);
    if (C > D) D = Math.min(C, D + EXTRA);
    const speed = C > D ? Math.min(2.5, C / D) : 1;
    if (speed > 1.001) notes.push(`${sc.code}: clip ${C.toFixed(1)} s > cảnh ${D.toFixed(1)} s → tăng tốc ×${speed.toFixed(2)}`);
    args.push('-ss', String(SKIP), '-i', webm);
    vf0 = `[0:v]setpts=(PTS-STARTPTS)/${speed.toFixed(4)},fps=30,scale=1920:1080:flags=lanczos,tpad=stop_mode=clone:stop_duration=${(D + 1).toFixed(2)},trim=0:${D.toFixed(3)},setpts=PTS-STARTPTS`;
  } else {
    args.push('-loop', '1', '-framerate', '30', '-t', D.toFixed(3), '-i', path.join(EVIDENCE, 'slides', `${sc.slide}.png`));
    vf0 = `[0:v]fps=30,scale=1920:1080:flags=lanczos,trim=0:${D.toFixed(3)},setpts=PTS-STARTPTS`;
  }
  args.push('-i', vf);
  const fc =
    `${vf0},format=yuv420p,fade=t=in:st=0:d=${FADE},fade=t=out:st=${(D - FADE).toFixed(3)}:d=${FADE}[v];` +
    `[1:a]aresample=48000,aformat=channel_layouts=stereo,adelay=${Math.round(LEAD * 1000)}:all=1,apad=whole_dur=${D.toFixed(3)},atrim=0:${D.toFixed(3)}[a]`;
  args.push('-filter_complex', fc, '-map', '[v]', '-map', '[a]', '-t', D.toFixed(3), '-c:v', 'libx264', '-preset', 'medium', '-crf', '21', '-profile:v', 'high',
    '-pix_fmt', 'yuv420p', '-r', '30', '-g', '60', '-c:a', 'aac', '-b:a', '192k', '-ar', '48000', '-ac', '2', out);
  run(FF, args);
  const real = dur(out);

  // phụ đề: chia lời theo khung ≤ 2 dòng × 42 ký tự, rải trong khoảng có giọng theo số ký tự
  const cs = cues(l.sub);
  const total = cs.reduce((s, c) => s + c.join(' ').length, 0);
  let t = offset + LEAD;
  const sceneCues = [];
  for (const c of cs) {
    const len = (vdur * c.join(' ').length) / total;
    const a = t;
    const b = t + len;
    srt.push(`${++n}\n${srtTime(a)} --> ${srtTime(Math.max(b - 0.05, a + 0.8))}\n${c.join('\n')}\n`);
    sceneCues.push({ start: +a.toFixed(2), end: +b.toFixed(2), lines: c });
    t = b;
  }
  timeline.push({ code: sc.code, kind: useClip ? 'clip' : 'slide', start: +offset.toFixed(2), duration: +real.toFixed(2), voice: path.basename(vf), voiceDur: +vdur.toFixed(2), cues: sceneCues });
  console.log(`${sc.code}: ${real.toFixed(1)} s (${useClip ? 'clip' : 'slide'}, giọng ${vdur.toFixed(1)} s)`);
  offset += real;
}

writeFileSync(path.join(EVIDENCE, 'subtitles-vi.srt'), srt.join('\n'));
const list = path.join(WORK, 'concat.txt');
writeFileSync(list, timeline.map((s) => `file '${path.join(WORK, `${s.code}.mp4`)}'`).join('\n') + '\n');
const nosub = path.join(WORK, 'nosub.mp4');
run(FF, ['-y', '-v', 'error', '-f', 'concat', '-safe', '0', '-i', list, '-c', 'copy', nosub]);

// burn-in phụ đề (libass): font Arial (có đủ dấu tiếng Việt), hộp nền mờ, đáy màn
const final = path.join(EVIDENCE, 'GEO-intro-vi-DRAFT-1080p.mp4');
const style = 'FontName=Arial,FontSize=15,Bold=1,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,BackColour=&H96000000,BorderStyle=3,Outline=6,Shadow=0,MarginV=34,Alignment=2';
run(
  FF,
  ['-y', '-v', 'error', '-i', nosub, '-vf', `subtitles=subtitles-vi.srt:fontsdir=/System/Library/Fonts/Supplemental:force_style='${style}'`, '-c:v', 'libx264', '-preset', 'medium',
    '-crf', '21', '-profile:v', 'high', '-pix_fmt', 'yuv420p', '-c:a', 'copy', '-movflags', '+faststart', final],
  { cwd: EVIDENCE },
);
const probe = run(FP, ['-v', 'error', '-show_entries', 'format=filename,duration,size,bit_rate:stream=index,codec_name,codec_type,profile,width,height,pix_fmt,r_frame_rate,sample_rate,channels,bit_rate', '-of', 'default=nw=0', final]);
writeFileSync(path.join(EVIDENCE, 'probe-draft.txt'), probe.replace(EVIDENCE + '/', ''));
const total = dur(final);
writeFileSync(path.join(EVIDENCE, 'timeline-vi.json'), JSON.stringify({ total: +total.toFixed(2), notes, scenes: timeline }, null, 2));
writeScript(timeline, total, notes);
console.log(`tổng ${total.toFixed(1)} s -> ${final}`);
for (const x of notes) console.log(`ghi chú: ${x}`);
