import { expect, test } from '../../support/fixtures.ts';
import { gotoWorkspace, snap, tapEdge, tool } from '../e3/e3.ts';
import { asg, box, freshMonitor, golden, row, signInAs, st } from './e7.ts';

/**
 * TC-GEO-0729 (US-GEO-703-FC3): Hà mất mạng 20 giây, trong lúc đó dùng "Điểm" hai lần; trang vẫn mở. Khi có mạng: hình
 * và hai điểm còn trên màn, hai thao tác được gửi và tính trong vòng 10 giây, không có thông báo lỗi chặn việc đang làm.
 */
test('Mất mạng khi đang thao tác @TC-GEO-0729', async ({ page, request, context }) => {
  test.setTimeout(90_000);
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  await signInAs(page, ha, 'Hà', 'student');
  await gotoWorkspace(page, id);
  await expect.poll(async () => row(await freshMonitor(request, b.teacher.token, id), 'Hà').opened).toBe(true);

  await context.setOffline(true);
  const t0 = Date.now();
  await tool(page, 'point');
  await tapEdge(page, 'SD');
  await page.getByTestId('viewer-point-mid').click();
  await tool(page, 'point');
  await tapEdge(page, 'SB');
  await page.getByTestId('viewer-point-mid').click();
  await expect.poll(async () => (await snap(page)).points.filter((p) => p.user).length).toBe(2);
  await page.waitForTimeout(Math.max(0, 20_000 - (Date.now() - t0)));
  await expect(page.getByRole('alertdialog')).toHaveCount(0);

  await context.setOffline(false);
  const back = Date.now();
  await expect
    .poll(async () => row(await freshMonitor(request, b.teacher.token, id), 'Hà').toolCount, { timeout: 10_000, intervals: [500, 1000] })
    .toBe(2);
  expect(Date.now() - back).toBeLessThanOrEqual(10_000);
  await expect(page.getByTestId('e3-workspace')).toBeVisible();
  expect((await snap(page)).points.filter((p) => p.user).length).toBe(2);
  await expect(page.getByRole('alertdialog')).toHaveCount(0);
});
