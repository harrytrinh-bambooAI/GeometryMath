import assert from 'node:assert/strict';
import { readFileSync, statSync } from 'node:fs';
import { test } from 'node:test';
import { ICONS, iconGlyphNames, releaseIcon, verifyIcon } from './icons.ts';

const FONT = new URL('../../assets/fonts/material-symbols-rounded-subset.woff2', import.meta.url);
const NAMES = new URL('../../assets/fonts/material-symbols-rounded-subset.txt', import.meta.url);
const DESIGN_23 = ['home', 'cube', 'users', 'layers', 'clock', 'check', 'lock', 'eye', 'redo', 'back', 'file', 'chart', 'pencil',
  'spark', 'shield', 'next', 'prev', 'x', 'plus', 'warn', 'info', 'bolt', 'search'];

test('icon: tên glyph hợp lệ, đủ 23 icon của thiết kế', () => {
  for (const [k, v] of Object.entries(ICONS)) assert.match(v, /^[a-z][a-z0-9_]*$/, k);
  for (const k of DESIGN_23) assert.ok(k in ICONS, k);
});

test('icon: font tự lưu là WOFF2 và danh sách glyph đã tải khớp bảng ICONS', () => {
  assert.equal(readFileSync(FONT).subarray(0, 4).toString('latin1'), 'wOF2');
  assert.ok(statSync(FONT).size > 2000);
  const fetched = readFileSync(NAMES, 'utf8').trim().split(',');
  assert.deepEqual(fetched, iconGlyphNames(), 'chạy lại: node scripts/fetch-icon-font.mjs');
});

test('icon: nhãn máy và trạng thái mở lời giải', () => {
  assert.equal(verifyIcon(true), 'check');
  assert.equal(verifyIcon(false), 'eye');
  assert.equal(releaseIcon('open'), 'unlock');
  assert.equal(releaseIcon('upcoming'), 'schedule');
  assert.equal(releaseIcon('unset'), 'lock');
});
