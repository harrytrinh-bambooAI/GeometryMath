import { expect, test } from '../../support/fixtures.ts';
import { bearer, signIn } from '../s3e1/s3e1.ts';
import { SUB } from '../s5e4/submit/submit.ts';
import { MSG, RB01, mock, setup, submit, view, waitAi } from './s5e6b.ts';

/**
 * S5-E6b (T-261128-62) — màn chấm có đề xuất AI /gv/cham-ai/:id (US-S5E6-04) và chạy song song (US-S5E6-05-FC2).
 * Bài nộp đi luồng thật; AI giả lập (F-S5-090 BN-01: 2/4/2/1/1, vùng ảnh {trang, 40, 60+80i, 480, 60}; F-S5-091 BN-02:
 * 2/3/0/1/1).
 */

test('Bấm tiêu chí khoanh vùng ảnh, nhận từng tiêu chí rồi duyệt @TC-S5E6-18', async ({ page, request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
  const { sub } = await submit(request, c, 'Bình An');
  await waitAi(request, c.lan, sub.id);

  await signIn(page, c.lan);
  await page.goto(`/gv/cham-ai/${sub.id}`);
  await expect(page.getByTestId('s5e6-ai-points-2')).toHaveText('AI 4');
  await expect(page.getByTestId('s5e6-page-1')).toBeVisible();
  await page.getByTestId('s5e6-crit-name-2').click();
  const region = page.getByTestId('s5e6-region');
  await expect(region).toBeVisible();
  await expect(region).toHaveAttribute('data-page', '1');
  await expect(region).toHaveAttribute('data-x', '40');
  await expect(region).toHaveAttribute('data-y', '140');
  await expect(region).toHaveAttribute('data-w', '480');
  await expect(region).toHaveAttribute('data-h', '60');
  await expect(page.getByTestId('s5e6-approve')).toBeDisabled();
  for (let k = 1; k <= 5; k++) {
    await page.getByTestId(`s5e6-accept-${k}`).click();
    await expect(page.getByTestId(`s5e6-accepted-${k}`)).toBeVisible();
    await expect(page.getByTestId('s5e6-toast')).toHaveText(MSG.s49(RB01[k - 1]!));
  }
  await expect(page.getByTestId('s5e6-total')).toHaveText('Tổng: 10 / 10');
  await page.getByTestId('s5e6-approve').click();
  await expect(page.getByTestId('s5e6-status')).toHaveText('Đã duyệt');
  const v = await view(request, c.lan, sub.id);
  expect(v.grade).toMatchObject({ total: 10 });
  expect(v.ai).toMatchObject({ source: MSG.source });
  expect(v.ai!.actions!.map((a) => a.action)).toEqual(['accept', 'accept', 'accept', 'accept', 'accept']);
});

test('Sửa một tiêu chí, bỏ một tiêu chí rồi nhập tay @TC-S5E6-19', async ({ page, request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Chi Mai']!, ['F-S5-091']);
  const { sub } = await submit(request, c, 'Chi Mai');
  await waitAi(request, c.lan, sub.id);

  await signIn(page, c.lan);
  await page.goto(`/gv/cham-ai/${sub.id}`);
  await expect(page.getByTestId('s5e6-ai-points-2')).toHaveText('AI 3');
  for (const k of [1, 3, 4]) await page.getByTestId(`s5e6-accept-${k}`).click();
  await page.getByTestId('s5e6-edit-2').click();
  await page.getByTestId('s5e6-input-2').fill('2,5');
  await page.getByTestId('s5e6-drop-5').click();
  await expect(page.getByTestId('s5e6-input-5')).toHaveValue('');
  // bước 3: Duyệt mờ khi còn tiêu chí bị bỏ chưa nhập
  await expect(page.getByTestId('s5e6-approve')).toBeDisabled();
  await page.getByTestId('s5e6-input-5').fill('0,5');
  await expect(page.getByTestId('s5e6-approve')).toBeEnabled();
  // F-S5-091 = 7; Các bước 3 → 2,5, Trình bày 1 → 0,5 ⇒ 6 (đặc tả ghi 6,5: lệch số học, qc_findings Dev-S5E6-09)
  await expect(page.getByTestId('s5e6-total')).toHaveText('Tổng: 6 / 10');
  await page.getByTestId('s5e6-approve').click();
  await expect(page.getByTestId('s5e6-status')).toHaveText('Đã duyệt');
  const v = await view(request, c.lan, sub.id);
  expect(v.grade).toMatchObject({ total: 6 });
  const acts = Object.fromEntries(v.ai!.actions!.map((a) => [a.name, [a.action, a.aiPoints, a.teacherPoints]]));
  expect(acts['Các bước']).toEqual(['edit', 3, 2.5]);
  expect(acts['Trình bày']).toEqual(['drop', 1, 0.5]);
  expect(acts['Hướng']).toEqual(['accept', 2, 2]);
});

test('Nhận cả bài và phím tắt cho bài không cờ @TC-S5E6-20', async ({ page, request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
  await mock(request, c.w.people['Chi Mai']!, ['F-S5-090']);
  const bn01 = (await submit(request, c, 'Bình An')).sub;
  const other = (await submit(request, c, 'Chi Mai')).sub;
  await waitAi(request, c.lan, bn01.id);

  await signIn(page, c.lan);
  await page.goto(`/gv/cham-ai/${bn01.id}`);
  await page.getByTestId('s5e6-accept-all').click();
  for (let k = 1; k <= 5; k++) await expect(page.getByTestId(`s5e6-accepted-${k}`)).toBeVisible();
  await page.getByTestId('s5e6-review-title').click();
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(new RegExp(`/gv/cham-ai/${other.id}$`));
  const v = await view(request, c.lan, bn01.id);
  expect(v.status).toBe('approved');
  expect(v.grade).toMatchObject({ total: 10 });
  expect(v.ai).toMatchObject({ source: MSG.source });
});

test('Bài có cờ không có "Nhận cả bài" @TC-S5E6-21', async ({ page, request }) => {
  const c = await setup(request, 'ai_proposal');
  await mock(request, c.w.people['Chi Mai']!, ['s5e6-kq2']);
  const { sub } = await submit(request, c, 'Chi Mai');
  const put = await request.put(`${SUB}/${sub.id}/ocr`, { headers: bearer(c.lan.token), data: { baseVersion: 1, pages: [{ pageNo: 2, segments: [{ text: 'Vậy d(A,(SBC)) = a√3/3' }] }] } });
  expect(put.status(), await put.text()).toBe(200);
  await expect.poll(async () => (await view(request, c.lan, sub.id)).ai?.aiFlags ?? [], { timeout: 30_000 }).toContain('AI và engine lệch');

  await signIn(page, c.lan);
  await page.goto(`/gv/cham-ai/${sub.id}`);
  await expect(page.getByTestId('s5e6-flags')).toContainText('AI và engine lệch');
  await expect(page.getByTestId('s5e6-engine')).toHaveText('Đáp số trong bài khác kết quả máy tính (a√6/3).');
  await expect(page.getByTestId('s5e6-ai-points-1')).toBeVisible();
  await expect(page.getByTestId('s5e6-accept-all')).toHaveCount(0);
  await page.getByTestId('s5e6-review-title').click();
  await page.keyboard.press('Shift+A');
  await expect(page.locator('[data-testid^="s5e6-accepted-"]')).toHaveCount(0);
  await expect(page.getByTestId('s5e6-approve')).toBeDisabled();
});

test('Màn chấm ở chế độ song song không hiện khung đề xuất @TC-S5E6-27', async ({ page, request }) => {
  const c = await setup(request, 'parallel');
  await mock(request, c.w.people['Chi Mai']!, ['F-S5-091']);
  const { sub } = await submit(request, c, 'Chi Mai');
  await waitAi(request, c.lan, sub.id);

  await signIn(page, c.lan);
  await page.goto(`/gv/cham-ai/${sub.id}`);
  await expect(page.getByTestId('s5e6-shadow-msg')).toHaveText(MSG.s47);
  await expect(page.getByTestId('s5e6-input-1')).toBeVisible();
  await expect(page.locator('[data-testid^="s5e6-ai-points-"]')).toHaveCount(0);
  await expect(page.getByTestId('s5e6-region')).toHaveCount(0);
  await expect(page.getByTestId('s5e6-rubric')).not.toContainText('Tin cậy');
  // chấm tay rồi duyệt → hiện so sánh
  const pts = ['2', '2,5', '0', '1', '0,5'];
  for (let k = 1; k <= 5; k++) await page.getByTestId(`s5e6-input-${k}`).fill(pts[k - 1]!);
  await page.getByTestId('s5e6-approve').click();
  await expect(page.getByTestId('s5e6-comparison')).toBeVisible();
  await expect(page.getByTestId('s5e6-cmp-2')).toContainText('3');
  await expect(page.getByTestId('s5e6-cmp-2')).toContainText('2,5');
});
