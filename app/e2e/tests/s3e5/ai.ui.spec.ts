import { expect, test } from '../../support/fixtures.ts';
import { API, MSG, aiCalls, assignSet, bearer, signIn, teacherApi, world } from '../s3e1/s3e1.ts';

/** US-S3E5-04 trên trình duyệt: công tắc ở cài đặt lớp, khung trợ lý ở màn bài của học sinh. */

test('Tắt công tắc thì khung trợ lý ẩn ở học sinh @TC-S3E5-40', async ({ page, browser, request }) => {
  const w = await world(request);
  const set = await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  await signIn(page, w.people['Cô Lan']!);
  await page.goto(`/gv/lop-hoc/${w.classes['11A1']!.id}`);
  await page.getByTestId('s3e5-switch-ai').uncheck();
  await expect(page.getByTestId('s3e5-switch-ai')).not.toBeChecked();
  const ctx = await browser.newContext();
  const hs = await ctx.newPage();
  await signIn(hs, w.people['Bình An']!);
  await hs.goto(`/hs/bai/${set.items[0]!.assignmentId}`);
  await expect(hs.getByTestId('s3e5-ai-off')).toHaveText(MSG.s83);
  await expect(hs.getByTestId('e1-assignment-page-statement')).toBeVisible();
  await expect(hs.getByTestId('e1-qa')).toBeHidden();
  await ctx.close();
});

test('Luồng học cơ bản vẫn chạy khi tắt @TC-S3E5-42', async ({ page, request }) => {
  const w = await world(request);
  const set = await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  const an = w.people['Bình An']!;
  await teacherApi.settings(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, { aiEnabled: false });
  const before = await aiCalls(request, an.id);
  await signIn(page, an);
  await page.goto('/hs/bai-luyen');
  await expect(page.getByText('Bộ bài tuần 15')).toBeVisible();
  const a1 = set.items[0]!.assignmentId;
  await page.goto(`/hs/bai/${a1}`);
  await expect(page.getByTestId('e1-assignment-page-statement')).toBeVisible();
  await expect(page.getByTestId('s3e5-ai-off')).toBeVisible();
  const hint = await request.post(`${API}/api/v1/student/assignments/${a1}/hints/1`, {
    headers: bearer(an.token),
    data: { requestId: crypto.randomUUID() },
  });
  expect(hint.status(), await hint.text()).toBe(200);
  const att = await request.post(`${API}/api/v1/student/practice-items/${a1}/attempts`, {
    headers: bearer(an.token),
    data: { answer: '45', requestKey: crypto.randomUUID() },
  });
  expect(att.status(), await att.text()).toBe(200);
  expect(await aiCalls(request, an.id)).toBe(before);
});

test('Lớp mới mặc định bật @TC-S3E5-45', async ({ page, request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
  await signIn(page, w.people['Cô Lan']!);
  await page.goto('/gv/lop-hoc');
  await page.getByTestId('s3e1-create-name').fill('11B3');
  await page.getByTestId('s3e1-create-submit').click();
  await page.getByTestId('s3e1-class-card').filter({ hasText: '11B3' }).click();
  await expect(page.getByTestId('s3e5-switch-ai')).toBeChecked();
});
