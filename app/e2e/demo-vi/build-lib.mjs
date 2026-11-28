// T-261128-76/77 (DEMO-VI) · v2.0 — thư viện dựng video dùng chung cho video giới thiệu và bộ video hướng dẫn.
// Mỗi đoạn (segment) = hình (slide PNG đứng yên, hoặc một khúc WebM [from, to]) + một tệp giọng; thời lượng đoạn căn theo giọng:
//   D = LEAD + giọng + TAIL; khúc video ngắn hơn D thì giữ khung cuối, dài hơn thì cho dài thêm ≤ EXTRA rồi tăng tốc ≤ 2,5×.
// Ra MP4 (H.264 1920x1080 30 fps + AAC 48 kHz, phụ đề burn-in bằng libass) + SRT (≤ 2 dòng × 42 ký tự) + probe.
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';
import { cues } from './lines.mjs';

export function tools(nm) {
  if (!nm) throw new Error('cần đường dẫn node_modules có ffmpeg-static, @ffprobe-installer');
  return { FF: path.join(nm, 'ffmpeg-static', 'ffmpeg'), FP: path.join(nm, '@ffprobe-installer', `darwin-${process.arch}`, 'ffprobe') };
}

const run = (bin, args, opts = {}) =>
  execFileSync(bin, args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'], maxBuffer: 64 << 20, timeout: 900_000, killSignal: 'SIGKILL', ...opts });

export const probeDur = (FP, f) => Number(run(FP, ['-v', 'error', '-show_entries', 'format=duration', '-of', 'default=nw=1:nk=1', f]).trim());

const srtTime = (t) => {
  const ms = Math.max(0, Math.round(t * 1000));
  const h = String(Math.floor(ms / 3600000)).padStart(2, '0');
  const m = String(Math.floor((ms % 3600000) / 60000)).padStart(2, '0');
  const s = String(Math.floor((ms % 60000) / 1000)).padStart(2, '0');
  return `${h}:${m}:${s},${String(ms % 1000).padStart(3, '0')}`;
};

/** Tệp giọng của một mã trong thư mục: .mp3 hoặc .wav, tệp MỚI NHẤT thắng (owner thả bản Google TTS cùng tên). */
export function voiceFile(dir, code) {
  const c = ['mp3', 'wav'].map((e) => path.join(dir, `${code}.${e}`)).filter(existsSync);
  if (!c.length) throw new Error(`thiếu tệp giọng ${path.join(dir, code)}.wav|mp3 (chạy gen-voice)`);
  return c.sort((a, b) => statSync(b).mtimeMs - statSync(a).mtimeMs)[0];
}

/**
 * @param {object} o
 * @param {{FF:string,FP:string}} o.t
 * @param {Array<{code:string, png?:string, webm?:string, from?:number, to?:number, voice:string, sub:string}>} o.segments
 * @param {string} o.work thư mục tạm
 * @param {string} o.mp4 tệp ra
 * @param {string} o.srt tệp SRT ra (burn-in từ tệp này)
 * @param {string} [o.probe] tệp probe ra
 */
export function buildVideo({ t, segments, work, mp4, srt, probe, lead = 0.6, tail = { slide: 2.0, clip: 1.6 }, extra = 8, log = console.log }) {
  const { FF, FP } = t;
  mkdirSync(work, { recursive: true });
  const FADE = 0.25;
  const timeline = [];
  const notes = [];
  const srtOut = [];
  let offset = 0;
  let n = 0;
  for (const sg of segments) {
    const vdur = probeDur(FP, sg.voice);
    const isClip = !!(sg.webm && existsSync(sg.webm));
    if (sg.webm && !isClip) notes.push(`${sg.code}: thiếu clip ${path.basename(sg.webm)} → slide dự phòng`);
    let D = lead + vdur + tail[isClip ? 'clip' : 'slide'];
    const out = path.join(work, `${sg.code}.mp4`);
    const args = ['-y', '-v', 'error'];
    let v0;
    if (isClip) {
      const from = Math.max(0, sg.from ?? 0.6);
      const total = probeDur(FP, sg.webm);
      const to = Math.min(total, sg.to ?? total);
      const C = Math.max(0.5, to - from);
      if (C > D) D = Math.min(C, D + extra);
      const speed = C > D ? Math.min(2.5, C / D) : 1;
      if (speed > 1.001) notes.push(`${sg.code}: khúc ${C.toFixed(1)} s > đoạn ${D.toFixed(1)} s → tăng tốc ×${speed.toFixed(2)}`);
      args.push('-ss', from.toFixed(3), '-t', C.toFixed(3), '-i', sg.webm);
      v0 = `[0:v]setpts=(PTS-STARTPTS)/${speed.toFixed(4)},fps=30,scale=1920:1080:flags=lanczos,tpad=stop_mode=clone:stop_duration=${(D + 1).toFixed(2)},trim=0:${D.toFixed(3)},setpts=PTS-STARTPTS`;
    } else {
      args.push('-loop', '1', '-framerate', '30', '-t', D.toFixed(3), '-i', sg.png);
      v0 = `[0:v]fps=30,scale=1920:1080:flags=lanczos,trim=0:${D.toFixed(3)},setpts=PTS-STARTPTS`;
    }
    args.push('-i', sg.voice);
    const fc =
      `${v0},format=yuv420p,fade=t=in:st=0:d=${FADE},fade=t=out:st=${(D - FADE).toFixed(3)}:d=${FADE}[v];` +
      `[1:a]aresample=48000,aformat=channel_layouts=stereo,adelay=${Math.round(lead * 1000)}:all=1,apad=whole_dur=${D.toFixed(3)},atrim=0:${D.toFixed(3)}[a]`;
    args.push('-filter_complex', fc, '-map', '[v]', '-map', '[a]', '-t', D.toFixed(3), '-c:v', 'libx264', '-preset', 'medium', '-crf', '21', '-profile:v', 'high',
      '-pix_fmt', 'yuv420p', '-r', '30', '-g', '60', '-c:a', 'aac', '-b:a', '192k', '-ar', '48000', '-ac', '2', out);
    run(FF, args);
    const real = probeDur(FP, out);
    const cs = cues(sg.sub);
    const totalChars = cs.reduce((s, c) => s + c.join(' ').length, 0) || 1;
    let x = offset + lead;
    const sceneCues = [];
    for (const c of cs) {
      const len = (vdur * c.join(' ').length) / totalChars;
      srtOut.push(`${++n}\n${srtTime(x)} --> ${srtTime(Math.max(x + len - 0.05, x + 0.8))}\n${c.join('\n')}\n`);
      sceneCues.push({ start: +x.toFixed(2), end: +(x + len).toFixed(2), lines: c });
      x += len;
    }
    timeline.push({ code: sg.code, kind: isClip ? 'clip' : 'slide', start: +offset.toFixed(2), duration: +real.toFixed(2), voice: path.basename(sg.voice), voiceDur: +vdur.toFixed(2), cues: sceneCues });
    log(`${sg.code}: ${real.toFixed(1)} s (${isClip ? 'clip' : 'slide'}, giọng ${vdur.toFixed(1)} s)`);
    offset += real;
  }
  writeFileSync(srt, srtOut.join('\n'));
  const list = path.join(work, 'concat.txt');
  writeFileSync(list, timeline.map((s) => `file '${path.join(work, `${s.code}.mp4`)}'`).join('\n') + '\n');
  const nosub = path.join(work, 'nosub.mp4');
  run(FF, ['-y', '-v', 'error', '-f', 'concat', '-safe', '0', '-i', list, '-c', 'copy', nosub]);
  const style = 'FontName=Arial,FontSize=15,Bold=1,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,BackColour=&H96000000,BorderStyle=3,Outline=6,Shadow=0,MarginV=34,Alignment=2';
  run(
    FF,
    ['-y', '-v', 'error', '-i', nosub, '-vf', `subtitles=${path.basename(srt)}:fontsdir=/System/Library/Fonts/Supplemental:force_style='${style}'`, '-c:v', 'libx264', '-preset', 'medium',
      '-crf', '21', '-profile:v', 'high', '-pix_fmt', 'yuv420p', '-c:a', 'copy', '-movflags', '+faststart', mp4],
    { cwd: path.dirname(srt) },
  );
  if (probe) {
    const p = run(FP, ['-v', 'error', '-show_entries', 'format=filename,duration,size,bit_rate:stream=index,codec_name,codec_type,profile,width,height,pix_fmt,r_frame_rate,sample_rate,channels,bit_rate', '-of', 'default=nw=0', mp4]);
    writeFileSync(probe, p.replace(path.dirname(mp4) + '/', ''));
  }
  return { total: probeDur(FP, mp4), timeline, notes };
}

/** Trích 1 khung/đoạn (giữa đoạn) vào thư mục frames/ để soát LaTeX thô / hình mất. */
export function extractFrames(t, mp4, timeline, dir) {
  mkdirSync(dir, { recursive: true });
  for (const s of timeline) {
    const at = s.start + Math.min(s.duration - 0.3, Math.max(0.5, s.duration * 0.7));
    run(t.FF, ['-y', '-v', 'error', '-ss', at.toFixed(2), '-i', mp4, '-frames:v', '1', '-vf', 'scale=960:-1', path.join(dir, `${s.code}.jpg`)]);
  }
}
