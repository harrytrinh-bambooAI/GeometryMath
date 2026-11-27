import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { MSG as S3E1, err, join, myClasses, signIn, teacherApi, world, type Person } from '../s3e1/s3e1.ts';

/**
 * US-S3E6-02, ca LOẠI B của `archive.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E"): chỗ bấm Lưu trữ / Bỏ lưu trữ / Hủy
 * do ca loại A 15, 17, 26 giữ; hai ca này chỉ kiểm hệ quả dữ liệu bằng gọi API đúng thứ tự bước, cuối luồng mở màn
 * Lớp của em (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const studentEnd = (p: Person, testId: string) => async (page: Page) => {
  await signIn(page, p);
  await page.goto('/hs/lop-cua-em');
  await expect(page.getByTestId(testId)).toBeVisible();
};

test('Lưu trữ lớp có em Chờ duyệt thì các em đó bị từ chối tự động @TC-S3E6-16 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A1']!;
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  const pending = (await (await teacherApi.pending(request, lan.token, c.id)).json()) as { items: unknown[] };
  expect(pending.items).toHaveLength(0);
  const nam = w.people['Hoàng Nam']!;
  const mine = (await (await myClasses(request, nam.token)).json()) as { items: Array<{ className: string; status: string }> };
  expect(mine.items.find((x) => x.className === '11A1')?.status).toBe('rejected');
  await captureFinalState(browser, testInfo, async (page) => {
    await studentEnd(nam, 's3e1-join-rejected')(page);
    await expect(page.getByTestId('s3e1-join-rejected')).toContainText('11A1');
  });
});

test('Học sinh nhập mã của lớp đã lưu trữ @TC-S3E6-18 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A2']!;
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  const bao = w.people['Gia Bảo']!;
  const r = await join(request, bao.token, c.code);
  expect(r.status()).toBe(400);
  expect((await err(r)).message).toBe(S3E1.s01);
  const mine = (await (await myClasses(request, bao.token)).json()) as { items: unknown[] };
  expect(mine.items).toHaveLength(0);
  await captureFinalState(browser, testInfo, studentEnd(bao, 's3e1-join-code'));
});
