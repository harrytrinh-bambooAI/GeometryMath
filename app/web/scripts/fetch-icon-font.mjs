// Tải lại font icon tự lưu (CHỈ chạy tay lúc phát triển, khi đổi bảng ICONS; app lúc chạy không gọi Google).
// Dùng: cd app/web && node scripts/fetch-icon-font.mjs
// Lấy Material Symbols Rounded, trục cố định opsz 24 · wght 500 · FILL 1 · GRAD 0, chỉ các glyph trong
// src/shared/ui/icons.ts (tham số icon_names của Google Fonts). Giấy phép font: Apache-2.0 (Google).
// Ghi: src/assets/fonts/material-symbols-rounded-subset.woff2 + .txt (danh sách glyph, test đơn vị đối chiếu).
import { writeFileSync } from 'node:fs';
import { iconGlyphNames } from '../src/shared/ui/icons.ts';

const names = iconGlyphNames();
const UA = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36';
const cssUrl = `https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@24,500,1,0&icon_names=${names.join(',')}&display=block`;
const css = await (await fetch(cssUrl, { headers: { 'User-Agent': UA } })).text();
const m = css.match(/url\((https:\/\/fonts\.gstatic\.com\/[^)]+)\)\s*format\('woff2'\)/);
if (!m) throw new Error(`Không thấy URL woff2 trong CSS:\n${css.slice(0, 400)}`);
const font = Buffer.from(await (await fetch(m[1])).arrayBuffer());
if (font.subarray(0, 4).toString('latin1') !== 'wOF2') throw new Error('Tệp tải về không phải WOFF2');
const base = new URL('../src/assets/fonts/material-symbols-rounded-subset', import.meta.url).href;
writeFileSync(new URL(`${base}.woff2`), font);
writeFileSync(new URL(`${base}.txt`), `${names.join(',')}\n`);
console.log(`Đã ghi ${names.length} glyph, ${font.length} byte -> src/assets/fonts/material-symbols-rounded-subset.woff2`);
