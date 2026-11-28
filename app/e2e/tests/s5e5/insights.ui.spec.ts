import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, signIn } from '../s3e1/s3e1.ts';
import { tl01 } from '../s5e3/kb.ts';
import { bank, graded, kcBank, summary } from './insights.ts';

/**
 * US-S5E5-02-SC2 (bấm căn cứ mở đúng bài làm) và US-S5E5-03-SC1 (từ nhận định chọn 2 câu, tạo bộ luyện nháp) trên
 * trình duyệt: /gv/bai-co-cham/:id/loi-cua-lop. Dữ liệu đi đường chấm thật (S5-E4/E6), AI giả lập.
 */

test('Bấm căn cứ mở đúng bài làm @TC-S5E5-10', async ({ page, request }) => {
  const c = await graded(request);
  const s = await summary(request, c.lan, c.bc.id);
  const ev = s.insights[0]!.evidence.find((e) => e.work === 2)!;
  expect(ev.submissionId).toBe(c.subs[1]); // Bài 2 = bài nộp thứ hai (Chi Mai)
  await signIn(page, c.lan);
  await page.goto(`/gv/bai-co-cham/${c.bc.id}/loi-cua-lop`);
  const card = page.getByTestId('s5e5-errors');
  await expect(card.getByRole('heading', { name: 'Cả lớp hay sai ở đâu' })).toBeVisible();
  await expect(page.getByTestId('s5e5-insight-1')).toBeVisible();
  // thẻ không có cột xếp hạng hay phần trăm
  const headers = await page.getByTestId('s5e5-errors-raw').locator('th').allTextContents();
  expect(headers).toEqual(['Tiêu chí', 'Số bài mất điểm', 'Điểm trung bình']);
  expect(await card.textContent()).not.toMatch(/%|[Xx]ếp hạng/);
  await page.getByTestId('s5e5-evidence-1-2').click();
  await expect(page).toHaveURL(new RegExp(`/gv/cham/${ev.submissionId}$`));
  await expect(page.getByTestId('s5e4-review')).toBeVisible();
  await expect(page.getByTestId('s5e4-review-pages')).toBeVisible(); // ảnh bài làm
});

test('Từ nhận định chọn 2 câu và tạo bộ luyện nháp @TC-S5E5-16', async ({ page, request }) => {
  await bank(request);
  const c = await graded(request);
  await kcBank(request, c.w.orgId);
  await tl01(request, c.lan, c.c11.id);
  await signIn(page, c.lan);
  await page.goto(`/gv/bai-co-cham/${c.bc.id}/loi-cua-lop`);
  await page.getByTestId('s5e5-remind-1').click();
  const panel = page.getByTestId('s5e5-remind');
  await expect(panel).toBeVisible();
  const boxes = panel.locator('input[type=checkbox]');
  await expect(boxes.first()).toBeVisible();
  const n = await boxes.count();
  expect(n).toBeGreaterThanOrEqual(2);
  expect(n).toBeLessThanOrEqual(3); // tối đa 3 câu cùng chủ đề khoảng cách
  const docs = await page.getByTestId('s5e5-remind-docs').locator('li').count();
  expect(docs).toBeGreaterThanOrEqual(1);
  expect(docs).toBeLessThanOrEqual(2); // tối đa 2 mục tài liệu
  await boxes.nth(0).check();
  await boxes.nth(1).check();
  await page.getByTestId('s5e5-create-set').click();
  const done = page.getByTestId('s5e5-created');
  await expect(done).toContainText('Đã tạo bộ luyện nháp (2 câu), chưa giao lớp nào.');
  const href = await done.getByRole('link').getAttribute('href');
  const setId = href!.split('/').pop()!;
  const r = await request.get(`${API}/api/v1/teacher/assignment-sets/${setId}`, { headers: bearer(c.lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  const set = (await r.json()) as { status: string; classId: string | null; items: unknown[] };
  expect(set.status).toBe('draft');
  expect(set.items).toHaveLength(2);
  expect(set.classId ?? null).toBeNull();
});
