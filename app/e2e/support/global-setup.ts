import { execFileSync } from 'node:child_process';
import path from 'node:path';

/**
 * Chạy một lần trước cả lượt E2E:
 * 1. Kiểm máy chủ sống (thông báo rõ nếu chưa bật).
 * 2. Reset seed vàng (scripts/seed-reset.sh geo_e2e) và ghi T0 vào GEO_E2E_T0 cho mọi worker.
 *    Bỏ qua khi GEO_E2E_RESET=0 (ví dụ chạy vào máy chủ dev không dùng geo_e2e).
 */
export default async function globalSetup(): Promise<void> {
  const base = process.env.GEO_E2E_BASE_URL ?? `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;
  try {
    const res = await fetch(`${base}/health`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
  } catch (e) {
    throw new Error(`Máy chủ ${base} chưa sẵn sàng (${String(e)}). Dùng \`make e2e\` để tự bật máy chủ + web.`);
  }

  if (process.env.GEO_E2E_RESET === '0') {
    process.env.GEO_E2E_T0 ??= new Date().toISOString();
    return;
  }
  const script = path.resolve(import.meta.dirname, '../../scripts/seed-reset.sh');
  const out = execFileSync('bash', [script, 'geo_e2e'], { encoding: 'utf8' });
  const t0 = /^T0=(.+)$/m.exec(out)?.[1];
  if (!t0) throw new Error(`seed-reset không in T0:\n${out}`);
  process.env.GEO_E2E_T0 = t0;
  console.log(`[seed] trạng thái vàng đã nạp, T0=${t0}`);
}
