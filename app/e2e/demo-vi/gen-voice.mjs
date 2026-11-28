#!/usr/bin/env node
// T-261128-75 (DEMO-VI): sinh giọng đọc TẠM tiếng Việt cho từng cảnh từ voice/lines.tsv bằng giọng macOS (mặc định Linh, vi_VN).
//   node demo-vi/gen-voice.mjs <ffmpeg> [--force] [--only 05-dung-hinh-tu-de]
// Ra voice/<mã cảnh>.wav (48 kHz mono). Đã có <mã cảnh>.wav hoặc .mp3 (vd. owner thả bản Google TTS) thì GIỮ NGUYÊN, trừ khi --force.
// Đổi giọng: GEO_VI_VOICE=Linh; tốc độ: GEO_VI_RATE (từ/phút; để trống = mặc định của giọng).
import { execFileSync } from 'node:child_process';
import { existsSync, mkdtempSync, rmSync } from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { EVIDENCE, readLines } from './lines.mjs';

const FF = process.argv[2];
if (!FF || !existsSync(FF)) throw new Error('cần đường dẫn ffmpeg làm tham số đầu');
const force = process.argv.includes('--force');
const oi = process.argv.indexOf('--only');
const only = oi > 0 ? process.argv[oi + 1] : null;
const VOICE = process.env.GEO_VI_VOICE ?? 'Linh';
const RATE = process.env.GEO_VI_RATE;
const dir = path.join(EVIDENCE, 'voice');
const tmp = mkdtempSync(path.join(os.tmpdir(), 'geo-vi-voice-'));
let made = 0;
for (const l of readLines()) {
  if (only && l.code !== only) continue;
  const wav = path.join(dir, `${l.code}.wav`);
  const mp3 = path.join(dir, `${l.code}.mp3`);
  if (!force && (existsSync(wav) || existsSync(mp3))) continue;
  const aiff = path.join(tmp, `${l.code}.aiff`);
  execFileSync('say', ['-v', VOICE, ...(RATE ? ['-r', RATE] : []), '-o', aiff, l.voice]);
  execFileSync(FF, ['-y', '-v', 'error', '-i', aiff, '-ar', '48000', '-ac', '1', wav]);
  made++;
  console.log(`giọng ${l.code}`);
}
rmSync(tmp, { recursive: true, force: true });
console.log(`xong: ${made} tệp mới (giọng ${VOICE})`);
