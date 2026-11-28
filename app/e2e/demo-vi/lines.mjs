// T-261128-75 (DEMO-VI): đọc voice/lines.tsv (mã cảnh, lời đọc, [phụ đề]) + scenes.json; chia phụ đề ≤ 2 dòng × 42 ký tự.
import { readFileSync } from 'node:fs';
import path from 'node:path';

const here = path.dirname(new URL(import.meta.url).pathname);
export const EVIDENCE = path.resolve(process.env.GEO_VI_OUT ?? path.resolve(here, '../../../artifacts/GEO/evidence/DEMO-VI-20261128'));
export const SCENES = JSON.parse(readFileSync(path.join(here, 'scenes.json'), 'utf8'));

export function readLines() {
  return readFileSync(path.join(EVIDENCE, 'voice', 'lines.tsv'), 'utf8')
    .split('\n')
    .filter((s) => s.trim() && !s.startsWith('#'))
    .map((s) => {
      const [code, voice, sub] = s.split('\t');
      return { code: code.trim(), voice: voice.trim(), sub: (sub || voice).trim() };
    });
}

export const MAX_LINE = 42;

/** Gói chữ thành các dòng ≤ max ký tự (không cắt giữa từ). */
export function wrap(text, max = MAX_LINE) {
  const lines = [];
  let cur = '';
  for (const w of text.split(/\s+/).filter(Boolean)) {
    if (!cur) cur = w;
    else if ((cur + ' ' + w).length <= max) cur += ' ' + w;
    else {
      lines.push(cur);
      cur = w;
    }
  }
  if (cur) lines.push(cur);
  return lines;
}

/** Gói thành ≤ 2 dòng, cân hai dòng cho dễ đọc. */
function balanced(text) {
  const ls = wrap(text);
  if (ls.length !== 2) return ls;
  const words = text.split(/\s+/);
  let best = ls;
  let bestDiff = Math.abs(ls[0].length - ls[1].length);
  for (let i = 1; i < words.length; i++) {
    const a = words.slice(0, i).join(' ');
    const b = words.slice(i).join(' ');
    if (a.length > MAX_LINE || b.length > MAX_LINE) continue;
    const d = Math.abs(a.length - b.length);
    if (d < bestDiff) {
      best = [a, b];
      bestDiff = d;
    }
  }
  return best;
}

/**
 * Chia lời phụ đề thành các khung ≤ 2 dòng × 42 ký tự: tách theo câu (. ? !), câu dài tách tiếp theo dấu phẩy / hai chấm,
 * rồi gói dòng; mỗi khung tối đa 2 dòng.
 */
export function cues(text) {
  const parts = [];
  // hết câu = dấu . ? ! ĐI SAU là khoảng trắng (không cắt "S.ABCD", "0,5")
  for (const sentence of text.split(/(?<=[.?!])\s+/)) {
    const s = sentence.trim();
    if (!s) continue;
    if (wrap(s).length <= 2) {
      parts.push(s);
      continue;
    }
    let cur = '';
    for (const chunk of s.split(/(?<=[,:])\s+/)) {
      const next = cur ? cur + ' ' + chunk : chunk;
      if (wrap(next).length <= 2) cur = next;
      else {
        if (cur) parts.push(cur);
        cur = chunk;
      }
    }
    if (cur) parts.push(cur);
  }
  const out = [];
  for (const p of parts) {
    const ls = wrap(p);
    if (ls.length <= 2) out.push(balanced(p));
    else for (let i = 0; i < ls.length; i += 2) out.push(ls.slice(i, i + 2));
  }
  // gộp khung 1 dòng ngắn vào khung kề nếu vẫn ≤ 2 dòng
  const merged = [];
  for (const c of out) {
    const prev = merged[merged.length - 1];
    if (prev && prev.length === 1 && c.length === 1) {
      const joined = prev[0] + ' ' + c[0];
      if (wrap(joined).length <= 2 && (prev[0].length < 22 || c[0].length < 22)) {
        merged[merged.length - 1] = balanced(joined);
        continue;
      }
    }
    merged.push(c);
  }
  return merged;
}
