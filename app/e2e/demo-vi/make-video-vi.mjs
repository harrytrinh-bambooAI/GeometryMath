#!/usr/bin/env node
// T-261128-76 (DEMO-VI) · v2.0 · 2026-11-28 — dựng video giới thiệu tiếng Việt bản cuối:
//   cd app/e2e && node demo-vi/make-video-vi.mjs <node_modules có ffmpeg-static + @ffprobe-installer> [--draft]
// Thứ tự cảnh: demo-vi/scenes.json; lời: evidence voice/lines.tsv; giọng: voice/<mã>.(wav|mp3), tệp mới nhất thắng.
// Ra: GEO-intro-vi-1080p.mp4 (--draft: GEO-intro-vi-DRAFT-1080p.mp4 + probe-draft.txt), subtitles-vi.srt, probe.txt,
//     timeline-vi.json, frames/<mã>.jpg (1 khung/cảnh để soát), SCRIPT-VI.md.
import path from 'node:path';
import { writeFileSync } from 'node:fs';
import { buildVideo, extractFrames, tools, voiceFile } from './build-lib.mjs';
import { EVIDENCE, SCENES, readLines } from './lines.mjs';
import { writeScript } from './make-script-vi.mjs';

const here = path.dirname(new URL(import.meta.url).pathname);
const t = tools(process.argv[2]);
const draft = process.argv.includes('--draft');
const RAW = path.resolve(here, '../test-results/demo-vi-raw');
const lines = new Map(readLines().map((l) => [l.code, l]));
const segments = SCENES.scenes.map((sc) => {
  const l = lines.get(sc.code);
  if (!l) throw new Error(`lines.tsv thiếu mã cảnh ${sc.code}`);
  return {
    code: sc.code,
    png: path.join(EVIDENCE, 'slides', `${sc.slide}.png`),
    webm: sc.kind === 'clip' ? path.join(RAW, `${sc.code}.webm`) : undefined,
    voice: voiceFile(path.join(EVIDENCE, 'voice'), sc.code),
    sub: l.sub,
  };
});
const mp4 = path.join(EVIDENCE, draft ? 'GEO-intro-vi-DRAFT-1080p.mp4' : 'GEO-intro-vi-1080p.mp4');
const { total, timeline, notes } = buildVideo({
  t,
  segments,
  work: path.join(RAW, 'build'),
  mp4,
  srt: path.join(EVIDENCE, 'subtitles-vi.srt'),
  probe: path.join(EVIDENCE, draft ? 'probe-draft.txt' : 'probe.txt'),
});
extractFrames(t, mp4, timeline, path.join(EVIDENCE, 'frames'));
writeFileSync(path.join(EVIDENCE, 'timeline-vi.json'), JSON.stringify({ total: +total.toFixed(2), notes, scenes: timeline }, null, 2));
writeScript(timeline, total, notes);
console.log(`tổng ${total.toFixed(1)} s -> ${mp4}`);
for (const x of notes) console.log(`ghi chú: ${x}`);
