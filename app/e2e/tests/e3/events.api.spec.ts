import { randomUUID } from 'node:crypto';
import type { APIRequestContext } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, BG, bearer, devToken } from './e3.ts';

/**
 * Sự kiện thao tác trên hình (US-GEO-301-SC9, US-GEO-302-SC3; BR-GEO-144) và danh sách đại lượng ẩn (BR-GEO-155).
 * TC-GEO-0313 dùng Hà (F-GEO-024) ở BG-1, đếm theo chênh lệch. TC-GEO-0309 (xoay/thu phóng thật trên trình duyệt) ở
 * events.ui.spec.ts, trên làn E3 riêng.
 */

async function haProgress(request: APIRequestContext) {
  const teacher = await devToken(request, 'F-GEO-003');
  const r = await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) });
  expect(r.status()).toBe(200);
  const m = (await r.json()) as { students: Array<{ displayName: string; opened: boolean; toolCount: number }> };
  return m.students.find((s) => s.displayName === 'Hà')!;
}

test('Mỗi lần chọn là một thao tác trên hình @TC-GEO-0313', async ({ request }) => {
  const ha = await devToken(request, 'F-GEO-024');
  // đếm theo chênh lệch so với lúc bắt đầu: chạy lại (retry) không vỡ vì số đã có từ lượt trước (soát E3 #5)
  const before = (await haProgress(request)).toolCount;
  const at = new Date().toISOString();
  // Hà dùng "Chọn" chạm cạnh SD rồi mặt SAD: trang gửi một lô hai sự kiện công cụ Chọn (mã 1, D-GEO-058)
  const body = { batchId: randomUUID(), events: [{ tool: 1, at }, { tool: 1, at }] };
  const r = await request.post(`${API}/api/v1/student/assignments/${BG('BG-1')}/figure-events`, { headers: bearer(ha), data: body });
  expect(r.status(), await r.text()).toBe(200);
  expect(((await r.json()) as { inserted: number }).inserted).toBe(2);
  // gửi lại đúng lô (mạng chập chờn) không tính hai lần
  const again = await request.post(`${API}/api/v1/student/assignments/${BG('BG-1')}/figure-events`, { headers: bearer(ha), data: body });
  expect(again.status()).toBe(200);
  expect(((await again.json()) as { inserted: number }).inserted).toBe(0);
  await expect
    .poll(async () => (await haProgress(request)).toolCount, { timeout: 20_000, intervals: [500, 1000, 2000] })
    .toBe(before + 2);
});

test('Danh sách đại lượng ẩn không mang giá trị @TC-GEO-0340', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  // giá trị 45 / 45° / π/4 (0,785 rad) đứng riêng; bỏ qua chữ số nằm trong mã định danh hay giờ (":45", "a45b")
  const forbidden = /(^|[^\w.:-])45(\.0+)?(?!\d)|45°|π\/4|pi\/4|0[.,]785/i;
  const lock = await request.get(`${API}/api/v1/student/assignments/${BG('BG-1')}/lock`, { headers: bearer(an) });
  expect(((await lock.json()) as { locked: boolean }).locked).toBe(true); // BG-1 Sắp mở
  const r = await request.get(`${API}/api/v1/student/assignments/${BG('BG-1')}/scene`, { headers: bearer(an) });
  expect(r.status()).toBe(200);
  const text = await r.text();
  const scene = JSON.parse(text) as { hiddenMeasures: string[] };
  // chỉ nêu đối tượng: góc tại D tạo bởi S, D, A; góc tại S tạo bởi A, S, D
  expect([...scene.hiddenMeasures].sort()).toEqual(['ang:A-S-D', 'ang:S-D-A']);
  expect(text).not.toMatch(forbidden);
  for (const path of ['', '/lock']) {
    const x = await request.get(`${API}/api/v1/student/assignments/${BG('BG-1')}${path}`, { headers: bearer(an) });
    expect(await x.text()).not.toMatch(forbidden);
  }
});
