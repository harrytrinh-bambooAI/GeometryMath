// contrast.js · v0.1 · 2026-11-26 · kiểm tương phản WCAG các cặp màu chính của 4 hướng: node Design/styles/contrast.js
const l = (h) => { const c = [1, 3, 5].map((i) => parseInt(h.slice(i, i + 2), 16) / 255).map((v) => (v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4)); return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]; };
const cr = (a, b) => { const x = l(a), y = l(b); return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05); };
const P = {
  'A nút chính chữ trắng/#00796B': ['#FFFFFF', '#00796B'], 'A chữ phụ/nền': ['#4A635C', '#F4FAF7'], 'A chip đúng': ['#1B6E3A', '#CDEFD6'], 'A chip cần xem': ['#7A4F00', '#FFE9A8'], 'A chip sai': ['#B3261E', '#FFDAD6'],
  'B nút chính chữ trắng/#0F62FE': ['#FFFFFF', '#0F62FE'], 'B chữ/nền': ['#F4F4F4', '#161616'], 'B chữ phụ/lớp1': ['#C6C6C6', '#262626'], 'B phấn vàng/nền': ['#FFD966', '#161616'], 'B link/lớp1': ['#78A9FF', '#262626'], 'B chip cần xem': ['#F1C21B', '#3D3000'], 'B chip đúng': ['#6FDC8C', '#0E3B1B'],
  'C nút chính chữ trắng/#0A62C9': ['#FFFFFF', '#0A62C9'], 'C chữ phụ/giấy': ['#4F4F54', '#FBF8EE'], 'C chip đúng': ['#1E7A34', '#DDF3E1'], 'C chip cần xem': ['#8A5300', '#FFEFC2'], 'C chip sai': ['#C4271C', '#FFE1DE'],
  'D nút chính chữ trắng/#1D4ED8': ['#FFFFFF', '#1D4ED8'], 'D chữ phụ/nền': ['#475569', '#F8FAFC'], 'D chip đúng': ['#15803D', '#DCFCE7'], 'D chip cần xem': ['#92400E', '#FEF3C7'], 'D chip sai': ['#B91C1C', '#FEE2E2'],
};
let bad = 0;
for (const k in P) { const r = cr(...P[k]); if (r < 4.5) bad++; console.log((r >= 4.5 ? 'OK  ' : 'THẤP') + ' ' + r.toFixed(1) + ' ' + k); }
console.log(bad ? 'Có cặp < 4,5:1' : 'Tất cả ≥ 4,5:1 (AA)');
