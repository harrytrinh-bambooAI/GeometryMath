// Kiểm bản build production (PO-52, soát E3 #2): không còn móc kiểm thử `window.__geoScene` và trang thử hình.
// Chạy sau `vite build` trong `npm run build` (và vì thế trong `make web-build` / `make verify`).
// Dùng: node scripts/check-dist.mjs [thư mục, mặc định dist]. Thoát 1 nếu thấy chuỗi cấm trong bất kỳ tệp nào
// (kể cả .map, vì sourcesContent chép nguyên mã nguồn của mô-đun được đóng gói).
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';

const dir = process.argv[2] ?? 'dist';
// Tên móc cấm ở MỌI tệp (kể cả .map). Đường dẫn/tiêu đề trang thử hình chỉ cấm trong mã chạy (.map chép nguyên
// mã nguồn của features/e3/index.tsx, nơi viết điều kiện bật).
const FORBIDDEN = ['__geoScene'];
const FORBIDDEN_CODE = ['thu-hinh/lang-tru-l', 'đáy chữ L'];

function* files(d) {
  for (const n of readdirSync(d)) {
    const p = join(d, n);
    if (statSync(p).isDirectory()) yield* files(p);
    else yield p;
  }
}

const hits = [];
for (const f of files(dir)) {
  const text = readFileSync(f, 'utf8');
  const list = f.endsWith('.map') ? FORBIDDEN : [...FORBIDDEN, ...FORBIDDEN_CODE];
  for (const s of list) if (text.includes(s)) hits.push(`${f}: ${s}`);
}
if (hits.length) {
  console.error(`check-dist: bản production còn móc kiểm thử (${hits.length}):\n  ${hits.join('\n  ')}`);
  process.exit(1);
}
console.log(`check-dist: ${dir} sạch (không có ${[...FORBIDDEN, ...FORBIDDEN_CODE].join(', ')})`);
