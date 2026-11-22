import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { iphone } from '../e2/e2.ts';
import { API, F060, HKG, MSG, aiCalls, bearer, sandbox, setAiFor, signIn, type Person } from './e4.ts';

/** E4 · E2E-UI: nhập đề tự học bằng chữ (US-GEO-401), bằng ảnh (402), khám phá hình mẫu (403). */

const selfCount = async (rq: APIRequestContext, userId: string) =>
  ((await (await rq.get(`${API}/test-hooks/e2/self-study-count?userId=${userId}`)).json()) as { count: number }).count;

async function student(request: APIRequestContext): Promise<Person> {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  return sb.classes[0]!.students['An']!;
}
async function openSelf(page: Page, an: Person) {
  await signIn(page, an, 'An');
  await page.goto('/hs/tu-hoc');
  await expect(page.getByTestId('e2-statement')).toBeVisible();
}
/** Ảnh đề dữ liệu vàng: PNG giả mang dấu "GEO-FIXTURE:<mã F>" mà nhà cung cấp AI giả lập đọc được (D-GEO-047). */
const image = (marker: string, size: number) => {
  const head = Buffer.from(`\x89PNG\r\n\x1a\n${marker}`, 'latin1');
  return { name: 'de.png', mimeType: 'image/png', buffer: Buffer.concat([head, Buffer.alloc(Math.max(0, size - head.length), 7)]) };
};

test('Dán đề và nhận hình @TC-GEO-0400', async ({ page, request }) => {
  const an = await student(request);
  await openSelf(page, an);
  await expect(page.getByText('Đề bài của em')).toBeVisible();
  await page.getByTestId('e4-level').selectOption('thpt');
  await page.getByTestId('e2-statement').fill(F060);
  await page.getByTestId('e2-build').click();
  await expect(page.getByTestId('e2-self-study-result')).toBeVisible();
  await expect(page.getByTestId('e2-self-study-statement')).toContainText(F060);
  await expect(page.getByTestId('e2-problem-label')).toBeVisible();
  const scene = page.getByTestId('e2-scene');
  await expect(scene).toHaveAttribute('data-coord-source', 'engine');
  await expect(scene).toHaveAttribute('data-points', '6');
  const id = page.url().split('/').pop()!;
  const s = (await (await request.get(`${API}/api/v1/student/self-study/${id}`, { headers: bearer(an.token) })).json()) as {
    scene: { params: Record<string, number> };
  };
  expect(s.scene.params).toMatchObject({ ab: 3, ac: 4, h: 5 });
});

test('Màn chờ rõ ràng khi trợ lý trả lời chậm @TC-GEO-0401', async ({ page, request }) => {
  const an = await student(request);
  await setAiFor(request, an.id, { delayMs: 5000 });
  await openSelf(page, an);
  await page.getByTestId('e2-statement').fill(F060);
  await page.getByTestId('e2-build').click();
  await expect(page.getByTestId('e2-ai-wait')).toHaveText(MSG['13']);
  await expect(page.getByTestId('e2-build')).toBeDisabled();
  await expect(page.getByTestId('e2-scene')).toHaveCount(0);
  await expect(page.getByTestId('e4-steps')).toHaveCount(0);
  await expect(page.getByTestId('e2-self-study-result')).toBeVisible({ timeout: 20_000 });
});

test('Đề rỗng @TC-GEO-0404', async ({ page, request }) => {
  const an = await student(request);
  await openSelf(page, an);
  const calls = (await aiCalls(request, an.id)).length;
  const count = await selfCount(request, an.id);
  for (const v of ['', '    ']) {
    await page.getByTestId('e2-statement').fill(v);
    await page.getByTestId('e2-build').click();
    await expect(page.getByTestId('e2-statement-error')).toHaveText(MSG['20']);
  }
  expect((await aiCalls(request, an.id)).length).toBe(calls);
  expect(await selfCount(request, an.id)).toBe(count);
});

test('Trợ lý lỗi khi dựng hình @TC-GEO-0406', async ({ page, request }) => {
  const an = await student(request);
  await setAiFor(request, an.id, { mode: 'error' });
  await openSelf(page, an);
  await page.getByTestId('e2-statement').fill(F060);
  await page.getByTestId('e2-build').click();
  await expect(page.getByTestId('e2-statement-error')).toHaveText(MSG['14']);
  await expect(page.getByTestId('e4-retry')).toHaveText('Thử lại');
  await expect(page.getByTestId('e2-ai-wait')).toHaveCount(0);
  await expect(page.getByTestId('e2-statement')).toHaveValue(F060);
  expect(await selfCount(request, an.id)).toBe(0);
  // trợ lý trở lại bình thường: "Thử lại" dựng được (không phải gõ lại)
  await setAiFor(request, an.id, { mode: 'ok' });
  await page.getByTestId('e4-retry').click();
  await expect(page.getByTestId('e2-self-study-result')).toBeVisible();
});

test('Ảnh rõ được đọc thành chữ để sửa (iPhone) @TC-GEO-0409', async ({ browser, request }) => {
  const ctx = await iphone(browser);
  const page = await ctx.newPage();
  try {
    const an = await student(request);
    await openSelf(page, an);
    await expect(page.getByTestId('e4-photo')).toHaveText('Chụp hoặc dán ảnh đề');
    await page.getByTestId('e4-photo-input').setInputFiles(image('GEO-FIXTURE:F-GEO-066', 400_000));
    await expect(page.getByTestId('e2-statement')).toHaveValue(HKG['03']);
    await expect(page.getByTestId('e2-self-study-result')).toHaveCount(0);
    await expect(page.getByTestId('e2-scene')).toHaveCount(0);
    expect(await selfCount(request, an.id)).toBe(0);
  } finally {
    await ctx.close();
  }
});

test('Sửa chữ đọc từ ảnh rồi dựng hình @TC-GEO-0410', async ({ browser, request }) => {
  const ctx = await iphone(browser);
  const page = await ctx.newPage();
  try {
    const an = await student(request);
    await openSelf(page, an);
    await page.getByTestId('e4-photo-input').setInputFiles(image('GEO-FIXTURE:F-GEO-066', 400_000));
    await expect(page.getByTestId('e2-statement')).toHaveValue(HKG['03']);
    const edited = HKG['03'].replace('4 cm', '5 cm');
    await page.getByTestId('e2-statement').fill(edited);
    await page.getByTestId('e2-build').click();
    await expect(page.getByTestId('e2-self-study-result')).toBeVisible();
    await expect(page.getByTestId('e2-self-study-statement')).toContainText(edited);
    const id = page.url().split('/').pop()!;
    const s = (await (await request.get(`${API}/api/v1/student/self-study/${id}`, { headers: bearer(an.token) })).json()) as {
      statement: string;
      scene: { params: Record<string, number> };
    };
    expect(s.statement).toBe(edited);
    expect(s.scene.params.a).toBe(5);
  } finally {
    await ctx.close();
  }
});

// ---------------------------------------------------------------- US-GEO-403
test('Vào màn khám phá từ "Tự khám phá" của Bài của em @TC-GEO-0415', async ({ page, request }) => {
  const an = await student(request);
  await signIn(page, an, 'An');
  await page.goto('/hs/bai-cua-em');
  await expect(page.getByTestId('e4-explore-block')).toContainText('Tự khám phá');
  await page.getByTestId('e4-explore-pyramid').click();
  await expect(page).toHaveURL(/\/hs\/kham-pha\/pyramid$/);
  await expect(page.getByTestId('e4-explore')).toBeVisible();
  const scene = () => page.evaluate(() => (window as unknown as { __geoScene?: { camera: { yaw: number }; points: Array<{ name: string }> } }).__geoScene);
  await expect.poll(async () => (await scene())?.points.map((p) => p.name)).toContain('S');
  const yaw = (await scene())!.camera.yaw;
  const box = (await page.getByTestId('viewer-canvas').boundingBox())!;
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
  await page.mouse.down();
  await page.mouse.move(box.x + box.width / 2 + 90, box.y + box.height / 2 + 5, { steps: 6 });
  await page.mouse.up();
  await expect.poll(async () => (await scene())!.camera.yaw).not.toBe(yaw);
});

test('"Mở chế độ tự học" @TC-GEO-0416', async ({ page, request }) => {
  const an = await student(request);
  await signIn(page, an, 'An');
  await page.goto('/hs/bai-cua-em');
  await page.getByTestId('e4-open-self-study').click();
  await expect(page).toHaveURL(/\/hs\/tu-hoc$/);
  await expect(page.getByText('Đề bài của em')).toBeVisible();
  await expect(page.getByTestId('e2-statement')).toBeVisible();
  await expect(page.getByTestId('e2-templates').getByRole('button')).toHaveCount(6);
});

test('Màn khám phá không có gợi ý, lời giải, hỏi đáp @TC-GEO-0417', async ({ page, request }) => {
  const an = await student(request);
  await signIn(page, an, 'An');
  await page.goto('/hs/kham-pha/box');
  await expect(page.getByTestId('e4-explore')).toBeVisible();
  await expect(page.getByTestId('viewer-canvas')).toBeVisible();
  await expect(page.getByTestId('viewer-toolbar')).toBeVisible();
  for (const k of ['a', 'b', 'c']) await expect(page.getByTestId(`e4-param-${k}`)).toBeVisible();
  for (const t of ['hint', 'solution', 'ask']) await expect(page.getByTestId(`e3-tab-${t}`)).toHaveCount(0);
  for (const name of ['Gợi ý', 'Lời giải', 'Hỏi đáp']) await expect(page.getByRole('tab', { name })).toHaveCount(0);
  await expect(page.getByPlaceholder('Hỏi về bài này…')).toHaveCount(0);
});
