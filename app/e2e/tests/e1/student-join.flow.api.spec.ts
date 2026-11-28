import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, BG, MSG, bearer, devToken } from './e1.ts';

/**
 * US-GEO-101, ca LOẠI B của `student-join.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E"): chỗ nhập và nút "Vào lớp"
 * do ca loại A 0100, 0104, 0107 giữ; các ca này chỉ kiểm quy tắc tên/mã lớp bằng gọi API đúng bước, cuối luồng mở màn
 * "Vào lớp" (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const joinPage = (browser: Parameters<typeof captureFinalState>[0], testInfo: Parameters<typeof captureFinalState>[1]) =>
  captureFinalState(browser, testInfo, async (page) => {
    await page.goto('/hs/vao-lop');
    await expect(page.getByTestId('e1-join-submit')).toBeVisible();
  });
const join = (request: import('@playwright/test').APIRequestContext, displayName: string, joinCode: string) =>
  request.post(`${API}/api/v1/auth/student`, { data: { displayName, joinCode } });

test('Tên hiển thị 61 ký tự @TC-GEO-0108 @loai-B', async ({ request, browser }, testInfo) => {
  const r = await join(request, 'A'.repeat(61), 'HK7M2A');
  expect(r.status()).toBe(400);
  const e = (await r.json()) as { code: string };
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(JSON.stringify(e)).toContain(MSG['10']);
  const teacher = await devToken(request, 'F-GEO-003');
  const m = (await (await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) })).json()) as {
    students: Array<{ displayName: string }>;
  };
  expect(m.students.some((s) => s.displayName.startsWith('AAAAAAAAAA'))).toBe(false);
  await joinPage(browser, testInfo);
});

test('Mã lớp không tồn tại @TC-GEO-0109 @loai-B', async ({ request, browser }, testInfo) => {
  const r = await join(request, 'An', 'ZZ9ZZ9');
  const body = await r.text();
  expect(r.status()).toBe(400);
  expect(body).toContain(MSG['11']);
  for (const leak of ['11A1', '9B', '9C', 'THPT', 'THCS', 'Mẫu']) expect(body).not.toContain(leak);
  await joinPage(browser, testInfo);
});

test('Gõ sai mã lớp nhiều lần không bị khóa riêng @TC-GEO-0173 @loai-B', async ({ request, browser }, testInfo) => {
  const statuses: number[] = [];
  for (let i = 1; i <= 11; i++) {
    const r = await join(request, 'An', 'ZZ9ZZ9');
    statuses.push(r.status());
    expect(await r.text()).toContain(MSG['11']);
  }
  expect(statuses.every((s) => s === 400), `không lần nào 429: ${statuses.join(',')}`).toBe(true);
  const ok = await join(request, 'An', 'HK7M2A');
  expect(ok.status()).toBe(200);
  await captureFinalState(browser, testInfo, async (page) => {
    await page.goto('/hs/vao-lop');
    await expect(page.getByTestId('e1-join-submit')).toBeVisible();
  });
});

// Đợt 2 (E2E-SPLIT2): chỗ nhập + nút "Vào lớp" do ca A 0100, 0101 giữ; ca này chỉ kiểm dữ liệu giữ đúng của mình.
test('Vào lại với cùng tên thì giữ đúng dữ liệu của mình @TC-GEO-0102 @loai-B', async ({ request, browser }, testInfo) => {
  // thiết bị khác = lời gọi vào lớp mới (không có phiên cũ); chữ thường "an" vẫn là An của lớp
  const r = await join(request, 'an', 'HK7M2A');
  expect(r.status(), await r.text()).toBe(200);
  const me = (await r.json()) as { token: string; userId: string; displayName: string; role: string; className?: string };
  const mine = (await (await request.get(`${API}/api/v1/student/assignments`, { headers: bearer(me.token) })).json()) as {
    items: Array<{ problemCode: string; progress: { k: number; label: string } }>;
  };
  expect(mine.items.find((a) => a.problemCode === 'HKG-01')?.progress).toMatchObject({ k: 2, label: 'Đã xem gợi ý 2' });
  const teacher = await devToken(request, 'F-GEO-003');
  const m = (await (await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) })).json()) as {
    students: Array<{ displayName: string }>;
  };
  expect(m.students.filter((s) => s.displayName.toLowerCase() === 'an')).toHaveLength(1);
  await captureFinalState(browser, testInfo, async (page) => {
    const meJson = JSON.stringify({ role: me.role, userId: me.userId, displayName: me.displayName, className: me.className });
    await page.addInitScript(
      ([t, m2]) => {
        sessionStorage.setItem('geo.token', t);
        sessionStorage.setItem('geo.e1.me', m2);
      },
      [me.token, meJson] as const,
    );
    await page.goto('/hs/bai-cua-em');
    await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
    await expect(page.getByTestId('e1-assignment-HKG-01-progress')).toHaveText('2/3 · Đã xem gợi ý 2');
  });
});
