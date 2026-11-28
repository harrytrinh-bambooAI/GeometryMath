import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { ELIP, FX10, MSG, aiScenario, bearer, buildToEnd, draftsUrl, ok, signIn, stage, teacher, tokenFor, type Draft } from './s2e3.ts';

/** S2-E3 — bốn bước có màn chờ (US-S2E3-03) và ngoại lệ: không dựng được, bản nháp chữ, lệch đáp số (US-S2E3-04). */

async function typeAndBuild(page: Page, text: string) {
  const saved = page.waitForResponse((r) => r.url().includes('/api/v1/teacher/problem-drafts') && ['POST', 'PUT'].includes(r.request().method()));
  await page.getByTestId('s2e3-statement').fill(text);
  await saved;
  await page.getByTestId('s2e3-build').click();
  await expect(page.getByTestId('s2e3-stages')).toBeVisible();
}

test('@TC-S2E3-021 bốn bước chạy đúng thứ tự, kết thúc có hình, lời giải nháp, Đã kiểm chứng và AI soạn', async ({ page, request }, info) => {
  await aiScenario(request, teacher(21, info), 's2e3:slow-solve');
  await signIn(page, request, teacher(21, info), '/gv/them-de');
  await typeAndBuild(page, FX10);
  await expect(page.locator('[data-testid^="s2e3-stage-"][data-state]')).toHaveText([/Đọc đề/, /Dựng hình/, /Giải/, /Kiểm chứng/]);
  // đang ở bước Giải: hai bước đầu xong, bước cuối còn chờ
  await expect(stage(page, 'solve')).toHaveAttribute('data-state', 'running');
  await expect(stage(page, 'read')).toHaveAttribute('data-state', 'done');
  await expect(stage(page, 'build')).toHaveAttribute('data-state', 'done');
  await expect(stage(page, 'verify')).toHaveAttribute('data-state', 'waiting');
  await expect(stage(page, 'verify')).toHaveAttribute('data-state', 'done', { timeout: 20_000 });
  const result = page.getByTestId('s2e3-result');
  await expect(result).toBeVisible();
  await expect(page.getByTestId('s2e3-scene2d')).toBeVisible();
  await expect(page.locator('[data-testid^="s2e3-step-"]')).toHaveCount(3);
  await expect(result.getByTestId('s2e3-machine')).toHaveText(MSG.verified);
  await expect(result.getByTestId('s2e3-human')).toHaveText(MSG.ai);
  await aiScenario(request, teacher(21, info), '');
});

test('@TC-S2E3-026 đóng tab rồi mở lại vẫn thấy tiến trình, không tạo tác vụ thứ hai', async ({ page, context, request }, info) => {
  const user = teacher(26, info);
  await aiScenario(request, user, 's2e3:slow-solve');
  await signIn(page, request, user, '/gv/them-de');
  await typeAndBuild(page, FX10);
  await expect(stage(page, 'solve')).toHaveAttribute('data-state', 'running');
  const t = await tokenFor(request, user);
  const before = (await ok<{ items: Draft[] }>(await request.get(draftsUrl, { headers: bearer(t) }))).items[0]!;
  const jobBefore = (await ok<Draft>(await request.get(`${draftsUrl}/${before.id}`, { headers: bearer(t) }))).job!.id;
  await page.close();
  await new Promise((r) => setTimeout(r, 3_000));
  const again = await context.newPage();
  await signIn(again, request, user, '/gv/them-de');
  await expect(again.getByTestId('s2e3-stages')).toBeVisible();
  await expect(stage(again, 'solve')).toHaveAttribute('data-state', /running|done/);
  await expect(again.getByTestId('s2e3-statement')).toHaveValue(FX10);
  const after = await ok<Draft>(await request.get(`${draftsUrl}/${before.id}`, { headers: bearer(t) }));
  expect(after.job!.id).toBe(jobBefore);
  expect((await ok<{ items: unknown[] }>(await request.get(draftsUrl, { headers: bearer(t) }))).items).toHaveLength(1);
  await aiScenario(request, user, '');
});

test('@TC-S2E3-028 hủy tác vụ đang dựng: dừng, "Đã hủy", đề vẫn là nháp chữ', async ({ page, request }, info) => {
  const user = teacher(28, info);
  await aiScenario(request, user, 's2e3:slow-build');
  await signIn(page, request, user, '/gv/them-de');
  await typeAndBuild(page, FX10);
  await expect(stage(page, 'build')).toHaveAttribute('data-state', 'running');
  await page.getByTestId('s2e3-cancel').click();
  await expect(page.getByTestId('s2e3-job-state')).toHaveText('Đã hủy');
  await expect(stage(page, 'build')).not.toHaveAttribute('data-state', 'running');
  await expect(stage(page, 'solve')).toHaveAttribute('data-state', 'waiting');
  await expect(page.getByTestId('s2e3-statement')).toHaveValue(FX10);
  await expect(page.getByTestId('s2e3-statement')).toBeEditable();
  const t = await tokenFor(request, user);
  await new Promise((r) => setTimeout(r, 7_000)); // bước Dựng hình giả lập (6 giây) không ghi tiếp sau khi hủy
  const items = (await ok<{ items: Array<{ status: string; docId?: string | null }> }>(await request.get(draftsUrl, { headers: bearer(t) }))).items;
  expect(items).toHaveLength(1);
  expect(items[0]).toMatchObject({ status: 'editing' });
  expect(items[0]!.docId ?? null).toBeNull();
  await aiScenario(request, user, '');
});

test('@TC-S2E3-030 engine không dựng thì lưu "Cần xem lại · hình do AI dựng" với ba lối đi', async ({ page, request }, info) => {
  await signIn(page, request, teacher(30, info), '/gv/them-de');
  await typeAndBuild(page, ELIP);
  await expect(stage(page, 'build')).toHaveAttribute('data-state', 'error', { timeout: 15_000 });
  await expect(page.getByTestId('s2e3-stage-state-build')).toContainText('không dựng được');
  await expect(stage(page, 'solve')).toHaveAttribute('data-state', 'waiting');
  await expect(page.getByTestId('s2e3-ai-figure')).toHaveText(MSG.aiFigure);
  await expect(page.getByTestId('s2e3-way-more')).toContainText('Bổ sung điều kiện');
  await expect(page.getByTestId('s2e3-way-manual')).toContainText('Dựng tay');
  await expect(page.getByTestId('s2e3-way-text')).toContainText('Lưu bản nháp chữ');
});

test('@TC-S2E3-031 lưu bản nháp chữ thì đề không chọn được để giao, kèm "bản nháp chữ"', async ({ page, request }, info) => {
  const user = teacher(31, info);
  await signIn(page, request, user, '/gv/them-de');
  await typeAndBuild(page, ELIP);
  await expect(page.getByTestId('s2e3-ai-figure')).toBeVisible({ timeout: 15_000 });
  await page.getByTestId('s2e3-way-text').click();
  await page.getByTestId('s2e3-save-text-only').click();
  await expect(page.getByTestId('s2e3-notice')).toContainText('Đã lưu bản nháp chữ');
  await page.goto('/gv/de-cua-toi');
  const item = page.locator('[data-testid^="s2e3-item-"]');
  await expect(item).toHaveAttribute('data-status', 'text_only');
  await expect(item.locator('[data-testid^="s2e3-assign-note-"]')).toHaveText('bản nháp chữ');
  await expect(item.locator('button[data-testid^="s2e3-assign-"]')).toBeDisabled();
});

test('@TC-S2E3-032 giao đề "Cần xem lại · hình do AI dựng" phải bấm xác nhận riêng', async ({ page, request }, info) => {
  const user = teacher(32, info);
  const t = await tokenFor(request, user);
  const d = await buildToEnd(request, t, ELIP);
  expect(d.status).toBe('ai_figure');
  await signIn(page, request, user, '/gv/de-cua-toi');
  await page.getByTestId(`s2e3-assign-${d.id}`).click();
  await expect(page.getByTestId('s2e3-assign-confirm')).toContainText(MSG.askAiFigure);
  await page.getByTestId('s2e3-assign-no').click();
  await expect(page.getByTestId('s2e3-assign-confirm')).toHaveCount(0);
  await expect(page.getByTestId(`s2e3-assign-done-${d.id}`)).toHaveCount(0);
  expect((await ok<{ assignConfirmed: boolean }>(await request.get(`${draftsUrl}/${d.id}`, { headers: bearer(t) }))).assignConfirmed).toBe(false);
  // gọi thẳng API không xác nhận cũng bị chặn
  const r = await request.post(`${draftsUrl}/${d.id}/assign`, { headers: bearer(t), data: {} });
  expect(r.status()).toBe(409);
  await page.getByTestId(`s2e3-assign-${d.id}`).click();
  await page.getByTestId('s2e3-assign-yes').click();
  await expect(page.getByTestId(`s2e3-assign-done-${d.id}`)).toBeVisible();
});

test('@TC-S2E3-033 lệch đáp số hiện bảng Đề ghi / AI giải / Máy tính lại và chặn Duyệt', async ({ page, request }, info) => {
  const user = teacher(33, info);
  const t = await tokenFor(request, user);
  await aiScenario(request, user, 's2e3:answer-80');
  const d = await buildToEnd(request, t, `${FX10} Đáp số: 90°`);
  await aiScenario(request, user, '');
  await signIn(page, request, user, `/gv/soat-loi-giai/${d.result!.docId}`);
  await expect(page.getByTestId('s2e5-answers')).toBeVisible();
  await expect(page.getByTestId('s2e5-answer-stated')).toHaveText('90°');
  await expect(page.getByTestId('s2e5-answer-ai')).toHaveText('80°');
  await expect(page.getByTestId('s2e5-answer-computed')).toHaveText('90°');
  await expect(page.getByTestId('s2e5-approve')).toBeDisabled();
  await expect(page.getByTestId('s2e5-blocked')).toContainText(MSG.blocked);
});

test('@TC-S2E3-035 sửa đáp số đề ghi cho khớp rồi Kiểm tra lại thì mở Duyệt', async ({ page, request }, info) => {
  const user = teacher(35, info);
  const t = await tokenFor(request, user);
  const d = await buildToEnd(request, t, `${FX10} Đáp số: 80°`);
  expect(d.result!.answers).toMatchObject({ stated: '80°', ai: '90°', computed: '90°', state: 'mismatch' });
  await signIn(page, request, user, `/gv/soat-loi-giai/${d.result!.docId}`);
  await expect(page.getByTestId('s2e5-approve')).toBeDisabled();
  await page.getByTestId('s2e5-edit-stated').click();
  await page.getByTestId('s2e5-stated-input').fill('90');
  await page.getByTestId('s2e5-stated-save').click();
  await expect(page.getByTestId('s2e5-answer-stated')).toHaveText('90°');
  await page.getByTestId('s2e5-check').click();
  await expect(page.getByTestId('s2e5-answers')).toHaveAttribute('data-state', 'match');
  await expect(page.getByTestId('s2e5-approve')).toBeEnabled();
});
