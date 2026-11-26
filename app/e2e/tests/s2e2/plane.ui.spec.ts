import { expect, test } from '../../support/fixtures.ts';
import { A, MSG_LOCKED, numbers, openPlane, tapVertices } from './s2e2.ts';

/**
 * US-S2E2-03 — C2-05 làm bài hình phẳng (dự án ui-chromium: web; ui-webkit: iPad gen 7 ngang).
 * Đo độ dài/góc trên hình; đại lượng là đáp số bị khóa tới giờ mở lời giải; nhãn hai trục; tải chậm / mất mạng.
 */

test('@TC-S2E2-022 mở bài phẳng hiện hình, nhãn đỉnh, công cụ đo, gợi ý và nhãn hai trục', async ({ page, request }) => {
  await openPlane(page, request, A.PL01_LOCKED);
  const fig = page.getByTestId('s2e2-figure');
  await expect(fig).toBeVisible();
  for (const v of ['A', 'B', 'C']) await expect(page.getByTestId(`s2e2-figure-vertex-${v}`)).toHaveText(v);
  await expect(page.getByTestId('s2e2-tool-length')).toHaveText('Đo độ dài');
  await expect(page.getByTestId('s2e2-tool-angle')).toHaveText('Đo góc');
  await expect(page.getByTestId('s2e2-hints')).toContainText('Gợi ý');
  await page.getByTestId('s2e2-hint-open').click();
  await expect(page.getByTestId('s2e2-hint-1')).toBeVisible();
  // nhãn hai trục ở đầu thẻ hình (trên đề và hình)
  await expect(page.getByTestId('s2e2-label-machine')).toHaveText('Đã kiểm chứng');
  await expect(page.getByTestId('s2e2-label-human')).toHaveText('Giáo viên đã duyệt');
  const labelsBox = (await page.getByTestId('s2e2-labels').boundingBox())!;
  const figBox = (await fig.boundingBox())!;
  expect(labelsBox.y).toBeLessThan(figBox.y);
  // vùng chạm đỉnh ≥ 44 điểm (BR-S2E2-11)
  const vb = (await page.getByTestId('s2e2-figure-vertex-A').boundingBox())!;
  expect(Math.min(vb.width, vb.height)).toBeGreaterThanOrEqual(44);
});

test('@TC-S2E2-023 đo cạnh AB cho đúng 5', async ({ page, request }) => {
  await openPlane(page, request, A.PL01_LOCKED);
  await page.getByTestId('s2e2-tool-length').click();
  await tapVertices(page, 'A', 'B');
  await expect(page.getByTestId('s2e2-measure-length-AB')).toHaveText('AB = 5');
  await expect(page.getByTestId('s2e2-locked')).toHaveCount(0);
});

test('@TC-S2E2-024 đo đại lượng là đáp số (góc A) bị khóa', async ({ page, request }) => {
  const resp = page.waitForResponse((r) => r.url().includes(`/assignments/${A.PL01_LOCKED}/plane`));
  await openPlane(page, request, A.PL01_LOCKED);
  const body: unknown = await (await resp).json();
  // dữ liệu trả về không chứa giá trị góc A
  expect(numbers(body).filter((n) => Math.abs(n - 90) < 1e-6)).toEqual([]);
  await page.getByTestId('s2e2-tool-angle').click();
  await tapVertices(page, 'B', 'A', 'C');
  await expect(page.getByTestId('s2e2-locked')).toHaveText(MSG_LOCKED);
  await expect(page.getByTestId('s2e2-measures')).not.toContainText('°');
  await expect(page.getByTestId('s2e2-page')).not.toContainText('90');
});

test('@TC-S2E2-025 sau release_at công cụ đo mở cho đáp số', async ({ page, request }) => {
  await openPlane(page, request, A.PL01_OPEN);
  await page.getByTestId('s2e2-tool-angle').click();
  await tapVertices(page, 'B', 'A', 'C');
  await expect(page.getByTestId('s2e2-measure-angle-BAC')).toHaveText('A = 90°');
  await expect(page.getByTestId('s2e2-locked')).toHaveCount(0);
});

test('@TC-S2E2-027 hình chưa kiểm chứng hiện nhãn "Cần xem lại" và "AI soạn", vẫn làm bài được', async ({ page, request }) => {
  await openPlane(page, request, A.PL90_AI);
  await expect(page.getByTestId('s2e2-label-machine')).toHaveText('Cần xem lại');
  await expect(page.getByTestId('s2e2-label-human')).toHaveText('AI soạn');
  await expect(page.getByTestId('s2e2-figure')).toHaveAttribute('data-coord-source', 'ai');
  await page.getByTestId('s2e2-tool-length').click();
  await tapVertices(page, 'A', 'B');
  await expect(page.getByTestId('s2e2-measure-length-AB')).toHaveText('AB = 4');
});

test('@TC-S2E2-029 trạng thái đang tải có khung giữ chỗ; mất mạng hiện thông báo và Thử lại', async ({ page, request }) => {
  let release: () => void = () => {};
  const gate = new Promise<void>((r) => (release = r));
  let offline = true;
  await page.route('**/api/v1/student/assignments/*/plane', async (route) => {
    await gate; // mạng chậm
    if (offline) await route.abort('internetdisconnected');
    else await route.continue();
  });
  await openPlane(page, request, A.PL03);
  const loading = page.getByTestId('s2e2-loading');
  await expect(loading).toBeVisible();
  await expect(loading).toHaveAttribute('aria-busy', 'true');
  await expect(page.getByTestId('s2e2-figure')).toHaveCount(0);
  release(); // mạng ngắt giữa chừng
  await expect(page.getByTestId('s2e2-error')).toBeVisible();
  await expect(page.getByTestId('s2e2-retry')).toHaveText('Thử lại');
  await expect(page.getByTestId('s2e2-figure')).toHaveCount(0); // không hiện hình nửa vời
  offline = false;
  await page.getByTestId('s2e2-retry').click();
  await expect(page.getByTestId('s2e2-figure')).toBeVisible();
  for (const v of ['A', 'B', 'C', 'D']) await expect(page.getByTestId(`s2e2-figure-vertex-${v}`)).toBeVisible();
});
