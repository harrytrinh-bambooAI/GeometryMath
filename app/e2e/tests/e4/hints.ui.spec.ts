import { expect, test } from '../../support/fixtures.ts';
import { API, F060, HINT1_HKG01, HINT2_HKG01, MSG, call, hints12, openWorkspace, sandbox, selfStudy, signIn, tab } from './e4.ts';

/** E4 · E2E-UI: Gợi ý 1, 2 (US-GEO-404) và lời giải tự học (US-GEO-405) trên trang Làm bài (E3 /hs/lam-bai/:id). */

test('Xem Gợi ý 1 @TC-GEO-0419', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  await openWorkspace(page, sb.classes[0]!.students['Hà']!, sb.classes[0]!.assignments['BG-1']!, 'Hà');
  await tab(page, 'hint');
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 0/3');
  await expect(page.getByTestId('e4-hint-next')).toHaveText('Xem gợi ý 1');
  await page.getByTestId('e4-hint-next').click();
  await expect(page.getByTestId('e4-hint-1-title')).toHaveText('Gợi ý 1 · Hướng nghĩ');
  await expect(page.getByTestId('e4-hint-1-body')).toHaveText(HINT1_HKG01);
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 1/3');
  await expect(page.getByTestId('e4-hint-next')).toHaveText('Xem gợi ý 2');
});

test('Xem Gợi ý 2 @TC-GEO-0420', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const ha = sb.classes[0]!.students['Hà']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  expect((await call.hint(request, ha.token, bg1, 1)).status()).toBe(200);
  await openWorkspace(page, ha, bg1, 'Hà');
  await tab(page, 'hint');
  await page.getByTestId('e4-hint-next').click();
  await expect(page.getByTestId('e4-hint-2-title')).toHaveText('Gợi ý 2 · Bước kế tiếp');
  await expect(page.getByTestId('e4-hint-2-body')).toHaveText(HINT2_HKG01);
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 2/3');
  await expect(page.getByTestId('e4-hint-next')).toHaveCount(0);
  // "Bài của em" ghi "Đã xem gợi ý 2"
  await page.goto('/hs/bai-cua-em');
  await expect(page.locator(`[data-assignment-id="${bg1}"]`)).toContainText('Đã xem gợi ý 2');
});

test('Giáo viên thấy mức gợi ý vừa dùng @TC-GEO-0421', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['An', 'Bình', 'Chi', 'Dũng', 'Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const s = sb.classes[0]!.students;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  // F-GEO-050…054: An đã xem 1 và 2, Bình 1, Dũng 1 và 2 ("dùng gợi ý" = 3)
  await hints12(request, s['An']!.token, bg1);
  expect((await call.hint(request, s['Bình']!.token, bg1, 1)).status()).toBe(200);
  await hints12(request, s['Dũng']!.token, bg1);
  expect((await call.hint(request, s['Hà']!.token, bg1, 1)).status()).toBe(200); // Hà vừa xem Gợi ý 1
  await signIn(page, sb.teacher, 'Giáo viên mẫu', '11A1', 'teacher');
  await page.goto(`/gv/theo-doi/${bg1}`);
  const ha = page.getByTestId('e1-monitor-student-Ha');
  await expect(ha).toBeVisible();
  await expect(ha).toContainText('1/3 · Đã xem gợi ý 1'); // đèn "Gợi ý 1" của dòng Hà (Theo dõi lớp tối thiểu của E1)
  const rows = page.locator('[data-testid^="e1-monitor-student-"]');
  await expect(rows).toHaveCount(5);
  // thẻ "dùng gợi ý": số học sinh đã xem ít nhất một gợi ý = 4
  const mon = (await (
    await request.get(`${API}/api/v1/teacher/assignments/${bg1}/monitor`, { headers: { Authorization: `Bearer ${sb.teacher.token}` } })
  ).json()) as { students: Array<{ hintLevel: number }> };
  expect(mon.students.filter((x) => x.hintLevel >= 1)).toHaveLength(4);
});

test('Gợi ý trên bài tự học (gợi ý do AI sinh) @TC-GEO-0423', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F060);
  await openWorkspace(page, an, id);
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-locked')).toHaveText(MSG['30']);
  await tab(page, 'hint');
  await page.getByTestId('e4-hint-next').click();
  await expect(page.getByTestId('e4-hint-1')).toBeVisible();
  await page.getByTestId('e4-hint-next').click();
  await expect(page.getByTestId('e4-hint-2')).toBeVisible();
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 2/3');
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-locked')).toHaveCount(0);
  await expect(page.getByTestId('e4-solution-view')).toHaveText('Xem lời giải');
});

test('Xin Gợi ý 2 khi chưa xem Gợi ý 1 @TC-GEO-0424', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const ha = sb.classes[0]!.students['Hà']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  const r = await call.hint(request, ha.token, bg1, 2);
  expect(r.status()).toBe(403);
  const t = await r.text();
  expect(JSON.parse(t)).toMatchObject({ code: 'FORBIDDEN', message: MSG['34'] });
  expect(t).not.toContain(HINT2_HKG01);
  await openWorkspace(page, ha, bg1, 'Hà');
  await tab(page, 'hint');
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 0/3');
  await expect(page.getByRole('button', { name: 'Xem gợi ý 2' })).toHaveCount(0);
  await expect(page.getByTestId('e4-hint-next')).toHaveText('Xem gợi ý 1');
});

test('Giáo viên đã tắt gợi ý @TC-GEO-0425', async ({ page, request }) => {
  const sb = await sandbox(request, [
    { name: '9B', level: 'thcs', students: ['Khánh'], assignments: [{ key: 'BG-5', code: 'HKG-05', release: 'unset', allowHint: false, allowQa: false }] },
  ]);
  const kh = sb.classes[0]!.students['Khánh']!;
  const bg5 = sb.classes[0]!.assignments['BG-5']!;
  await openWorkspace(page, kh, bg5, 'Khánh');
  await tab(page, 'hint');
  await expect(page.getByTestId('e4-hint-disabled')).toHaveText(MSG['32']);
  await expect(page.getByTestId('e4-hint-next')).toHaveCount(0);
  const r = await call.hint(request, kh.token, bg5, 1);
  expect(r.status()).toBe(403);
  const t = await r.text();
  expect(JSON.parse(t)).toMatchObject({ code: 'FORBIDDEN', message: MSG['32'] });
  expect(t).not.toContain('Diện tích toàn phần gồm');
});

test('Mất mạng lúc bấm xem gợi ý @TC-GEO-0427', async ({ page, request, context }) => {
  const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const ha = sb.classes[0]!.students['Hà']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  await openWorkspace(page, ha, bg1, 'Hà');
  await tab(page, 'hint');
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 0/3');
  await context.setOffline(true);
  await page.getByTestId('e4-hint-next').click();
  await expect(page.getByTestId('e4-hint-error')).toHaveText(MSG['08']);
  await expect(page.getByTestId('e4-hint-1')).toHaveCount(0);
  await context.setOffline(false);
  const h = (await (await call.help(request, ha.token, bg1)).json()) as { k: number };
  expect(h.k).toBe(0);
  await page.reload();
  await tab(page, 'hint');
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 0/3');
});

test('Đủ hai gợi ý thì mở được lời giải tự học @TC-GEO-0428', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F060);
  await hints12(request, an.token, id);
  await openWorkspace(page, an, id);
  await tab(page, 'solution');
  await page.getByTestId('e4-solution-view').click();
  await expect(page.getByTestId('e4-solution-answer')).toContainText('60/√769');
  await expect(page.getByTestId('e4-step-1')).toBeVisible();
  await tab(page, 'hint');
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 3/3');
  const again = (await (await call.solution(request, an.token, id)).json()) as { firstView: boolean };
  expect(again.firstView).toBe(false); // đã có đúng một sự kiện mở lời giải
});

test('Bài tự học chưa xem gợi ý nào: tab Lời giải khóa @TC-GEO-0430', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F060);
  const seen: string[] = [];
  page.on('response', async (r) => {
    if (r.url().includes('/api/')) seen.push(await r.text().catch(() => ''));
  });
  await openWorkspace(page, an, id);
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-solution-locked')).toHaveText(MSG['30']);
  await expect(page.getByTestId('e4-solution-view')).toHaveCount(0);
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  for (const b of seen) {
    expect(b).not.toContain('60/√769');
    expect(b).not.toContain('2,1637');
  }
});
