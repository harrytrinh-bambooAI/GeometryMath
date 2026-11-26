import { expect, test } from '../../support/fixtures.ts';
import { submit } from '../s2e8/s2e8.ts';
import { API, assigned, attempt, home, hours, noHorizontalScroll, sandbox, studentPage } from './s3e2.ts';

/** S3-E2 · C3-08 Bài của em theo bộ (học sinh trên iPad / trình duyệt): hạn, tiến độ, giờ mở, quá hạn, hai lớp, chưa có bài. */

test('@TC-S3E2-29 Bài của em hiện theo lớp → bộ với hạn, tiến độ 2 / 4, giờ mở và Sắp mở', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, {
    title: 'Bộ bài tuần 15',
    dueAt: '2026-12-04T13:00:00Z',
    releaseAt: '2026-12-06T01:00:00Z',
  });
  const an = sb.classes[0]!.students['Bình An']!;
  for (const [k, ans] of [[0, '45'], [1, '12/5']] as const) expect((await attempt(request, an.token, set.items[k]!.assignmentId!, ans)).status()).toBe(200);
  await studentPage(page, an, 'Bình An', '/hs/bo-bai');
  const group = page.getByTestId('s3e2-group-11A1');
  await expect(group).toContainText('Bộ bài tuần 15');
  await expect(page.getByTestId(`s3e2-due-${set.id}`)).toHaveText('Hạn T6 04/12/2026 20:00');
  await expect(page.getByTestId(`s3e2-progress-${set.id}`)).toHaveText('2 / 4');
  await expect(page.getByTestId(`s3e2-release-${set.id}`)).toContainText('06/12/2026 08:00');
  await expect(page.getByTestId(`s3e2-release-${set.id}`)).toContainText('Sắp mở');
  await page.getByTestId(`s3e2-hs-open-${set.id}`).click();
  await expect(page.getByTestId('s3e2-hs-items').locator('> li')).toHaveCount(4);
  await expect(page.getByTestId('s3e2-hs-state-1')).toHaveText('Đúng');
  await expect(page.getByTestId('s3e2-hs-item-4')).toContainText('đáy là hình vuông cạnh 3');
  await noHorizontalScroll(page);
});

test('@TC-S3E2-05 quá hạn chỉ là nhãn: chip Quá hạn, em vẫn gửi đáp số và được tính tiến độ', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, { items: [1, 2], dueAt: hours(-0.05) });
  const an = sb.classes[0]!.students['Bình An']!;
  await studentPage(page, an, 'Bình An', `/hs/bo-bai/${set.id}`);
  await expect(page.getByTestId(`s3e2-overdue-${set.id}`)).toHaveText('Quá hạn');
  await page.getByTestId('s3e2-hs-play').click();
  await expect(page.getByTestId('s2e8-question')).toBeVisible();
  await submit(page, '45');
  await expect(page.getByTestId('s2e8-feedback')).toContainText('Đúng');
  const h = await home(request, an.token);
  expect(h.groups[0]!.sets[0]!).toMatchObject({ overdue: true, done: 1, total: 2 });
});

test('@TC-S3E2-30 Chi Mai Đang học 11A1 và 10B thấy hai nhóm, mỗi nhóm chỉ bộ của lớp đó', async ({ page, request }) => {
  // dữ liệu mẫu chung F-S3-021 (Chi Mai, 11A1 của cô Lan và 10B của thầy Khôi) + bộ F-S3-050 và F-S3E2-050 (seed 920)
  const tok = await request.post(`${API}/dev/token`, {
    data: { orgId: '00000000-0000-7000-8002-000000000100', userId: '00000000-0000-7000-8003-000000000021', role: 'STUDENT' },
  });
  expect(tok.status()).toBe(200);
  const token = ((await tok.json()) as { token: string }).token;
  await studentPage(page, { id: '00000000-0000-7000-8003-000000000021', token }, 'Chi Mai', '/hs/bo-bai');
  const a1 = page.getByTestId('s3e2-group-11A1');
  const b10 = page.getByTestId('s3e2-group-10B');
  await expect(a1).toContainText('Bộ bài tuần 15');
  await expect(a1).not.toContainText('lớp 10B');
  await expect(b10).toContainText('Bộ bài tuần 15 lớp 10B');
  await expect(b10.locator('li')).toHaveCount(1);
  const text = await page.getByTestId('s3e2-home').innerText();
  expect(text).not.toMatch(/Cô Lan|Thầy Khôi/);
});

test('@TC-S3E2-31 chưa có bài: hiện MSG-S3-27, không có nhóm nào', async ({ page, request }) => {
  const sb = await sandbox(request, [{ name: '11A2', students: ['Quốc Bảo'] }], []);
  await studentPage(page, sb.classes[0]!.students['Quốc Bảo']!, 'Quốc Bảo', '/hs/bo-bai');
  await expect(page.getByTestId('s3e2-empty')).toHaveText('Chưa có bài nào. Khi thầy cô giao bài, bài sẽ hiện ở đây.');
  await expect(page.locator('[data-testid^="s3e2-group-"]')).toHaveCount(0);
});
