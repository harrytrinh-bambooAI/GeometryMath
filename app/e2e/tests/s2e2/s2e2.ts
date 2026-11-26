import type { APIRequestContext, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, devToken, ids } from '../e1/e1.ts';

/**
 * Tiện ích E2E của S2-E2 (hình học phẳng). Dữ liệu: app/db/seed/e2e/810_s2e2_core.sql + fixture-ids.d/s2e2.json
 * (tổ chức "Nhóm thử S2-E2", học sinh An F-S2-810020, giáo viên Cô Trang F-S2-810003).
 * Bài (đều chỉ đọc, dùng chung cho mọi dự án Playwright): 812301 PL-01 khóa · 812302 PL-01 đã mở · 812303 PL-02 khóa ·
 * 812304 PL-03 · 812305 PL-90 (hình AI dựng, "Cần xem lại") · 812306 PL-04.
 */
export { API, bearer };

export const AN = 'F-S2-810020';
export const TEACHER = 'F-S2-810003';
export const A = {
  PL01_LOCKED: (ids as unknown as { assignments: Record<string, { id: string }> }).assignments['F-S2-812301']!.id,
  PL01_OPEN: '00000000-0000-7000-8002-000000812302',
  PL02_LOCKED: '00000000-0000-7000-8002-000000812303',
  PL03: '00000000-0000-7000-8002-000000812304',
  PL90_AI: '00000000-0000-7000-8002-000000812305',
} as const;

export const MSG_LOCKED = 'Đại lượng này là đáp số, chưa đo được';

export const token = (request: APIRequestContext, ref: string) => devToken(request, ref);

/** Đăng nhập học sinh An trên trình duyệt (thẻ dev, như E3) rồi mở bài phẳng. */
export async function openPlane(page: Page, request: APIRequestContext, assignmentId: string) {
  const t = await devToken(request, AN);
  const me = { role: 'STUDENT', userId: ids.users[AN]!.id, displayName: 'An' };
  await page.addInitScript(
    ([tk, m]) => {
      sessionStorage.setItem('geo.token', tk);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [t, JSON.stringify(me)] as const,
  );
  await page.goto(`/hs/bai-phang/${assignmentId}`);
}

/** Chạm lần lượt các đỉnh trên hình. */
export async function tapVertices(page: Page, ...labels: string[]) {
  for (const l of labels) await page.getByTestId(`s2e2-figure-vertex-${l}`).click();
}

/** Mọi giá trị số trong JSON (đệ quy). */
export function numbers(v: unknown, out: number[] = []): number[] {
  if (typeof v === 'number') out.push(v);
  else if (Array.isArray(v)) v.forEach((x) => numbers(x, out));
  else if (v && typeof v === 'object') Object.values(v).forEach((x) => numbers(x, out));
  return out;
}

/** Mọi khóa trong JSON (đệ quy). */
export function keys(v: unknown, out: string[] = []): string[] {
  if (Array.isArray(v)) v.forEach((x) => keys(x, out));
  else if (v && typeof v === 'object')
    for (const [k, x] of Object.entries(v)) {
      out.push(k);
      keys(x, out);
    }
  return out;
}

export async function planeJson(request: APIRequestContext, assignmentId: string) {
  const t = await devToken(request, AN);
  const r = await request.get(`${API}/api/v1/student/assignments/${assignmentId}/plane`, { headers: bearer(t) });
  expect(r.status(), await r.text()).toBe(200);
  return r;
}
