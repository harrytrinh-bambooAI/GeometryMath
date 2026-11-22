import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { iphone } from '../e2/e2.ts';
import { openWorkspace, sandbox, signIn, tab } from './e4.ts';

/**
 * E4 · E2E-UI: lời giải đồng bộ hình (US-GEO-407) và phần vẽ thêm hiện dần, "Vì sao nghĩ ra bước này?" (US-GEO-408).
 * Kiểm hình bằng móc chỉ đọc `window.__geoScene.solution` (PO-52): `highlight` (đối tượng đang tô) và `shapes` (phần vẽ
 * thêm đã dựng: segment đường phụ, right ký hiệu góc vuông, arc cung góc kèm số đo, angle góc được tô).
 * Bài HKG-01 Đã mở trên lớp riêng (như BG-1 sau "Mở ngay"); dữ liệu bước theo fixture §2 (khung mvp-06).
 */
type Sol = {
  highlight: { points: string[]; edges: string[]; faces: string[]; angles: string[] };
  shapes: Array<{ kind: string; id: string; label: string | null }>;
};
const sol = (page: Page) => page.evaluate(() => (window as unknown as { __geoScene?: { solution?: Sol } }).__geoScene?.solution);

async function openSolution(page: Page, request: APIRequestContext, code = 'HKG-01') {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG', code, release: 'open' }] }]);
  await openWorkspace(page, sb.classes[0]!.students['An']!, sb.classes[0]!.assignments['BG']!);
  await tab(page, 'solution');
  await expect(page.getByTestId('e4-step-1')).toBeVisible();
  return sb;
}
async function step(page: Page, n: number) {
  await page.getByTestId(`e4-step-${n}`).click();
  await expect(page.getByTestId('e4-solution')).toHaveAttribute('data-step', String(n));
}
const kinds = async (page: Page) => ((await sol(page))?.shapes ?? []).map((s) => s.id).sort();

test('Bấm một bước, hình tô đúng đối tượng @TC-GEO-0441', async ({ page, request }) => {
  await openSolution(page, request);
  await page.getByTestId('e4-step-2').click();
  await expect(page.getByTestId('e4-step-2')).toHaveAttribute('aria-current', 'step');
  await expect(page.getByTestId('e4-step-highlight')).toHaveText('Đang tô trên hình: SD, AD, góc SDA');
  await expect.poll(async () => (await sol(page))?.highlight.angles).toEqual(['SDA']);
  expect((await sol(page))!.highlight.edges.sort()).toEqual(['AD', 'SD']);
  await expect(page.getByTestId('viewer-line-SD')).toHaveClass(/gv-hl/);
  await expect(page.getByTestId('viewer-line-AD')).toHaveClass(/gv-hl/);
  expect(await kinds(page)).toContain('angle-SDA');
});

test('Đổi bước thì đổi phần tô @TC-GEO-0442', async ({ page, request }) => {
  await openSolution(page, request);
  await step(page, 2);
  await step(page, 1);
  await expect(page.getByTestId('e4-step-highlight')).toHaveText('Đang tô trên hình: SD, AD');
  await expect.poll(async () => (await sol(page))?.highlight.angles).toEqual([]);
  expect(await kinds(page)).not.toContain('angle-SDA');
  await expect(page.getByTestId('viewer-line-SD')).toHaveClass(/gv-hl/);
});

test('"Bước sau", "Bước trước" @TC-GEO-0443', async ({ page, request }) => {
  await openSolution(page, request);
  const cur = page.getByTestId('e4-solution');
  await expect(cur).toHaveAttribute('data-step', '1');
  await page.getByTestId('e4-step-next').click();
  await expect(cur).toHaveAttribute('data-step', '2');
  await page.getByTestId('e4-step-next').click();
  await expect(cur).toHaveAttribute('data-step', '3');
  await expect(page.getByTestId('e4-step-next')).toBeDisabled();
  await page.getByTestId('e4-step-prev').click();
  await expect(cur).toHaveAttribute('data-step', '2');
});

test('Nhãn của lời giải @TC-GEO-0444', async ({ page, request }) => {
  await openSolution(page, request);
  await expect(page.getByTestId('e4-solution-label-machine')).toHaveText('Đã kiểm chứng');
  await expect(page.getByTestId('e4-solution-label-human')).toHaveText('AI soạn');
  await expect(page.getByText('Giáo viên đã duyệt')).toHaveCount(0);
});

test('Lời giải đồng bộ hình trên iPhone @TC-GEO-0446', async ({ browser, request }) => {
  const ctx = await iphone(browser);
  const page = await ctx.newPage();
  try {
    const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG', code: 'HKG-01', release: 'open' }] }]);
    await signIn(page, sb.classes[0]!.students['An']!, 'An');
    await page.goto(`/hs/lam-bai/${sb.classes[0]!.assignments['BG']!}`);
    await tab(page, 'solution');
    await page.getByTestId('e4-step-2').click();
    await expect.poll(async () => (await sol(page))?.highlight.angles).toEqual(['SDA']);
    await expect(page.getByTestId('e4-step-highlight')).toHaveText('Đang tô trên hình: SD, AD, góc SDA');
    // bảng dưới không che hết hình: phần hình phía trên bảng vẫn còn
    const canvas = (await page.getByTestId('viewer-canvas').boundingBox())!;
    const panel = (await page.getByTestId('e3-panel').boundingBox())!;
    expect(panel.y - canvas.y).toBeGreaterThan(120);
  } finally {
    await ctx.close();
  }
});

test('Bước không có đối tượng để tô @TC-GEO-0447', async ({ page, request }) => {
  const errors: string[] = [];
  page.on('pageerror', (e) => errors.push(e.message));
  await openSolution(page, request, 'HKG-05');
  await page.getByTestId('e4-step-1').click();
  await expect(page.getByTestId('e4-step-highlight')).toHaveCount(0);
  const s = await sol(page);
  expect(s?.highlight).toEqual({ points: [], edges: [], faces: [], angles: [] });
  expect(s?.shapes).toEqual([]);
  expect(errors).toEqual([]);
});

test('Chuyển bước nhanh @TC-GEO-0448', async ({ page, request }) => {
  await openSolution(page, request);
  for (const n of [1, 2, 3]) await page.getByTestId(`e4-step-${n}`).click({ delay: 0 });
  await expect(page.getByTestId('e4-solution')).toHaveAttribute('data-step', '3');
  await expect.poll(async () => (await sol(page))?.highlight).toEqual({ points: [], edges: [], faces: [], angles: ['SDA'] });
  await expect(page.getByTestId('viewer-line-SD')).not.toHaveClass(/gv-hl/);
  await expect(page.getByTestId('viewer-line-AD')).not.toHaveClass(/gv-hl/);
});

// ---------------------------------------------------------------- US-GEO-408
test('Phần vẽ thêm hiện dần theo bước @TC-GEO-0449', async ({ page, request }) => {
  await openSolution(page, request);
  await expect.poll(() => kinds(page)).toEqual(['segment-AD']); // bước 1: đường phụ AD nổi bật
  await page.getByTestId('e4-step-next').click();
  await expect.poll(() => kinds(page)).toEqual(['angle-SDA', 'right-A', 'segment-AD']); // + góc vuông tại A
  await page.getByTestId('e4-step-next').click();
  await expect.poll(() => kinds(page)).toEqual(['angle-SDA', 'arc-D-2', 'right-A', 'segment-AD']); // + cung tại D
  const arc = (await sol(page))!.shapes.find((s) => s.kind === 'arc');
  expect(arc?.label).toBe('45°');
  await expect(page.getByTestId('viewer-sol-arc-D-2-label')).toHaveText('45°');
});

test('"Vì sao nghĩ ra bước này?" @TC-GEO-0450', async ({ page, request }) => {
  await openSolution(page, request);
  await step(page, 2);
  const why = page.getByTestId('e4-step-why');
  await expect(why).toContainText('Vì sao nghĩ ra bước này?');
  await expect(why).toContainText('Có hình chiếu rồi thì góc cần tìm nằm trong một tam giác vuông.');
});

test('Quay lại bước trước thì phần vẽ thêm bớt đi @TC-GEO-0451', async ({ page, request }) => {
  await openSolution(page, request);
  await step(page, 3);
  await expect.poll(() => kinds(page)).toContain('arc-D-2');
  await step(page, 1);
  await expect.poll(() => kinds(page)).toEqual(['segment-AD']);
  await expect(page.getByTestId('viewer-sol-arc-D-2-label')).toHaveCount(0);
});

test('Nhảy thẳng tới bước cuối @TC-GEO-0452', async ({ page, request }) => {
  await openSolution(page, request);
  await expect(page.getByTestId('e4-solution')).toHaveAttribute('data-step', '1');
  await page.getByTestId('e4-step-3').click();
  await expect.poll(() => kinds(page)).toEqual(['angle-SDA', 'arc-D-2', 'right-A', 'segment-AD']);
});

test('Bước không có phần vẽ thêm @TC-GEO-0453', async ({ page, request }) => {
  await openSolution(page, request, 'HKG-03');
  const before = (await sol(page))!.shapes;
  await step(page, 2);
  expect((await sol(page))!.shapes).toEqual(before);
  await expect(page.getByTestId('e4-step-why')).toContainText('Lăng trụ đứng: V = diện tích đáy × chiều cao.');
});
