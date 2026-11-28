import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { clickHintNext } from '../../support/wait.ts';
import { PV } from '../e1/e1.ts';
import {
  API,
  F061,
  F077,
  F078,
  F234,
  F235,
  HINT2_HKG06,
  HKG,
  MSG,
  call,
  expectNoLeak,
  hints12,
  openWorkspace,
  recordResponses,
  sandbox,
  selfStudy,
  tab,
  type Sandbox,
} from './e4.ts';

/** E4 · E2E-UI: tự học kế thừa khóa của bài đang khóa (US-GEO-406; D-GEO-026, 150, 151, 155, 174, 177). */

async function classWith(request: APIRequestContext, assignments: Array<{ key: string; code: string; release?: string }> = []) {
  const sb = await sandbox(request, [{ students: ['An'], assignments }]);
  return { sb, an: sb.classes[0]!.students['An']! };
}
async function lockedInherited(page: Page) {
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-locked')).toHaveText(MSG['31']);
  await expect(page.getByTestId('e4-solution-view')).toHaveCount(0);
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
}
async function viewable(page: Page) {
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-view')).toHaveText('Xem lời giải');
  await expect(page.getByTestId('e4-solution-locked')).toHaveCount(0);
  await expect(page.getByText(MSG['31'])).toHaveCount(0);
}
async function uiHints(page: Page, id: string) {
  await tab(page, 'hint');
  for (const n of [1, 2]) {
    await clickHintNext(page, id, n); // chờ POST gợi ý + GET /help làm mới, không chờ theo thời gian (T-261126-43)
    await expect(page.getByTestId(`e4-hint-${n}`)).toBeVisible();
  }
}

test('Dán lại đề đang khóa thì bị khóa theo bài giao @TC-GEO-0434', async ({ page, request }) => {
  const { an } = await classWith(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = await selfStudy(request, an.token, HKG['01']);
  await openWorkspace(page, an, id);
  await uiHints(page, id);
  await lockedInherited(page);
});

test('Bài giao mở thì bài tự học kế thừa cũng mở @TC-GEO-0435', async ({ page, request }) => {
  const { sb, an } = await classWith(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = await selfStudy(request, an.token, HKG['01']);
  await hints12(request, an.token, id);
  await openWorkspace(page, an, id);
  await lockedInherited(page);
  const r = await call.release(request, sb.teacher.token, sb.classes[0]!.assignments['BG-1']!, { openNow: true, expectedRev: 0 });
  expect(r.status()).toBe(200);
  await page.reload();
  await viewable(page);
  await page.getByTestId('e4-solution-view').click();
  await expect(page.getByTestId('e4-solution-answer')).toContainText('45°');
  await expect(page.getByTestId('e4-step-1')).toBeVisible();
});

async function inheritedVariant(page: Page, request: APIRequestContext, statement: string) {
  const { an } = await classWith(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = await selfStudy(request, an.token, statement);
  await openWorkspace(page, an, id);
  await uiHints(page, id);
  await lockedInherited(page);
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  expectNoLeak(await r.text());
}

test('Đề viết lại với cạnh 2a vẫn kế thừa @TC-GEO-0482', async ({ page, request }) => {
  await inheritedVariant(page, request, F077);
});

test('Đề viết lại câu chữ, thêm chữ vẫn kế thừa khóa @TC-GEO-0483', async ({ page, request }) => {
  await inheritedVariant(page, request, F234);
});

test('Cùng hình nhưng hỏi đại lượng khác thì không kế thừa khóa @TC-GEO-0484', async ({ page, request }) => {
  const { an } = await classWith(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = await selfStudy(request, an.token, F235);
  const responses = recordResponses(page); // chờ đủ thân phản hồi trước khi kiểm (soát E4 #14)
  await openWorkspace(page, an, id);
  await uiHints(page, id);
  await viewable(page);
  const all = await responses();
  expect(all, 'đã gom được phản hồi gợi ý').toContain('"level":2');
  expect(all).not.toContain('45°');
});

test('Cùng hình nhưng khác tham số thì không kế thừa @TC-GEO-0489', async ({ page, request }) => {
  const { an } = await classWith(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const id = await selfStudy(request, an.token, F078);
  await openWorkspace(page, an, id);
  await uiHints(page, id);
  await viewable(page);
});

async function assignLater(request: APIRequestContext, sb: Sandbox, code: string, releaseAt: string | null): Promise<string> {
  const r = await call.assign(request, sb.teacher.token, sb.classes[0]!.id, PV(code), releaseAt);
  expect(r.status(), await r.text()).toBe(201);
  return ((await r.json()) as { id: string }).id;
}

test('Dán đề trước, giáo viên giao sau: lần xin kế tiếp bị khóa @TC-GEO-0485', async ({ page, request }) => {
  const { sb, an } = await classWith(request);
  const id = await selfStudy(request, an.token, HKG['06']);
  await openWorkspace(page, an, id);
  await tab(page, 'hint');
  await clickHintNext(page, id, 1);
  await expect(page.getByTestId('e4-hint-1')).toBeVisible();
  await assignLater(request, sb, 'HKG-06', null); // BG-6: Chưa đặt giờ (F-GEO-048)
  await clickHintNext(page, id, 2);
  await expect(page.getByTestId('e4-hint-2-body')).toHaveText(HINT2_HKG06);
  await lockedInherited(page);
});

test('Đã xem lời giải tự học, sau đó giáo viên giao đúng đề: xin lại thì bị khóa @TC-GEO-0486', async ({ page, request }) => {
  const { sb, an } = await classWith(request);
  const id = await selfStudy(request, an.token, HKG['05']);
  await hints12(request, an.token, id);
  const seen = await call.solution(request, an.token, id);
  expect(((await seen.json()) as { finalAnswer: string }).finalAnswer).toBe('112π cm²');
  // BG-7 (F-GEO-049): giờ mở "T0 + 30 phút"; giờ trôi qua bằng móc điều khiển giờ mở thay vì chờ đồng hồ thật
  // (soát E4 #16: cửa sổ 20 giây dễ đỏ giả trên máy chậm)
  const releaseAt = new Date(Date.now() + 30 * 60_000).toISOString();
  const bg7 = await assignLater(request, sb, 'HKG-05', releaseAt);
  await openWorkspace(page, an, id);
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-locked')).toHaveText(MSG['31']);
  await expect(page.getByTestId('e4-solution-answer')).toHaveCount(0);
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  expect(await r.text()).not.toContain('112π');
  const passed = await request.post(`${API}/test-hooks/e4/release-passed`, { data: { assignmentId: bg7 } });
  expect(passed.status(), await passed.text()).toBe(200);
  await page.reload();
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-answer')).toContainText('112π cm²');
});

test('Đề không dựng được bằng hình mẫu không có lời giải tự học @TC-GEO-0492', async ({ page, request }) => {
  const { an } = await classWith(request);
  const id = await selfStudy(request, an.token, F061);
  await openWorkspace(page, an, id);
  await uiHints(page, id);
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-locked')).toHaveText(MSG['58']);
  await expect(page.getByTestId('e4-solution-view')).toHaveCount(0);
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  const t = await r.text();
  expect(t).not.toContain('a√6/3');
  expect(JSON.parse(t).code).toBe('FORBIDDEN');
});

