import { expect, test } from '../../support/fixtures.ts';
import {
  API,
  K,
  MSG,
  TITLE,
  bearer,
  cardTitles,
  cards,
  detail,
  list,
  ok,
  openLibrary,
  queue,
  signIn,
  slot,
  token,
  type CardPage,
} from './s2e6.ts';

/** S2-E6 — giao diện Kho đề (C2-01): hai ngăn, lọc loại hình, trạng thái, gửi soát, hàng đợi người soát. */

test('@TC-S2E6-001 ngăn Của tôi mặc định hiện đề của chủ, mới nhất ở trên', async ({ page, request }, info) => {
  const s = slot(6, 1, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await page.goto('/gv');
  await page.getByTestId('s2e6-nav-kho').click();
  await expect(page.getByTestId('s2e6-page')).toBeVisible();
  await expect(page.getByTestId('s2e6-tab-mine')).toHaveAttribute('aria-selected', 'true');
  await expect(cards(page)).toHaveCount(3);
  expect(await cardTitles(page)).toEqual([TITLE.FX13, TITLE.FX14, TITLE.FX10]);
  for (const c of await cards(page).all()) {
    await expect(c.getByTestId('s2e6-kind')).toBeVisible();
    await expect(c.getByTestId('s2e6-machine')).toBeVisible();
    await expect(c.getByTestId('s2e6-human')).toHaveText('Giáo viên đã duyệt');
  }
  await expect(cards(page).first().getByTestId('s2e6-kind')).toHaveText('Phẳng');
  await expect(cards(page).nth(1).getByTestId('s2e6-kind')).toHaveText('Không gian');
});

test('@TC-S2E6-002 ngăn Kho chung chỉ có đề đã soát', async ({ page, request }, info) => {
  const s = slot(6, 2, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await openLibrary(page);
  await page.getByTestId('s2e6-tab-library').click();
  await expect(page.getByTestId('s2e6-page')).toHaveAttribute('data-shelf', 'library');
  await expect(cards(page)).toHaveCount(2);
  expect((await cardTitles(page)).sort()).toEqual([TITLE.FX11, TITLE.FX14].sort());
  await expect(page.getByText(TITLE.FX12)).toHaveCount(0);
});

test('@TC-S2E6-003 lọc Phẳng chỉ giữ đề phẳng, không tải lại trang', async ({ page, request }, info) => {
  const s = slot(6, 3, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await openLibrary(page);
  await expect(cards(page)).toHaveCount(3);
  await page.evaluate(() => ((window as unknown as { __s2e6Mark: number }).__s2e6Mark = 1));
  await page.getByTestId('s2e6-filter-plane').click();
  await expect(cards(page)).toHaveCount(2);
  expect(await cardTitles(page)).toEqual([TITLE.FX13, TITLE.FX10]);
  await expect(page.getByText(TITLE.FX14)).toHaveCount(0);
  expect(await page.evaluate(() => (window as unknown as { __s2e6Mark?: number }).__s2e6Mark)).toBe(1);
});

test('@TC-S2E6-004 đổi ngăn đưa bộ lọc về Tất cả', async ({ page, request }, info) => {
  const s = slot(6, 4, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await openLibrary(page);
  await page.getByTestId('s2e6-filter-plane').click();
  await expect(page.getByTestId('s2e6-filter-plane')).toHaveAttribute('aria-checked', 'true');
  await page.getByTestId('s2e6-tab-library').click();
  await expect(page.getByTestId('s2e6-filter-all')).toHaveAttribute('aria-checked', 'true');
  await expect(cards(page)).toHaveCount(2);
  await expect(page.locator('[data-testid^="s2e6-card-"][data-kind="space"]')).toHaveCount(1);
  await expect(page.locator('[data-testid^="s2e6-card-"][data-kind="plane"]')).toHaveCount(1);
});

test('@TC-S2E6-005 chưa có đề riêng hiện trạng thái trống', async ({ page, request }, info) => {
  const s = slot(6, 5, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await openLibrary(page);
  await expect(page.getByTestId('s2e6-empty')).toContainText(MSG.emptyMine);
  await expect(page.getByTestId('s2e6-empty-add')).toBeVisible();
  await expect(page.getByTestId('s2e6-list')).toHaveCount(0);
});

test('@TC-S2E6-006 lọc không có kết quả, Xóa bộ lọc trở về danh sách đủ', async ({ page, request }, info) => {
  const s = slot(6, 6, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await openLibrary(page);
  await expect(cards(page)).toHaveCount(2);
  await page.getByTestId('s2e6-filter-space').click();
  await expect(page.getByTestId('s2e6-no-match')).toContainText(MSG.noMatch);
  await page.getByTestId('s2e6-clear-filter').click();
  await expect(page.getByTestId('s2e6-filter-all')).toHaveAttribute('aria-checked', 'true');
  await expect(cards(page)).toHaveCount(2);
});

test('@TC-S2E6-007 trạng thái đang tải hiện khung giữ chỗ, không hiện trống', async ({ page, request }, info) => {
  const s = slot(6, 7, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  let release!: () => void;
  const gate = new Promise<void>((r) => (release = r));
  await page.route('**/api/v1/teacher/library?*', async (route) => {
    await gate;
    await route.continue();
  });
  await page.goto('/gv/kho-de-cua-toi');
  await expect(page.getByTestId('s2e6-skeleton')).toBeVisible();
  await expect(page.getByTestId('s2e6-empty')).toHaveCount(0);
  release();
  await expect(cards(page)).toHaveCount(3);
  await expect(page.getByTestId('s2e6-skeleton')).toHaveCount(0);
});

test('@TC-S2E6-013 giáo viên gửi đề đã duyệt vào hàng đợi', async ({ page, request }, info) => {
  const s = slot(6, 13, info);
  await signIn(page, request, s.org, s.trang, 'Cô Trang');
  await page.goto(`/gv/kho-de-cua-toi/de/${s.doc(K.FX14)}`);
  await expect(page.getByTestId('s2e6-doc-human')).toHaveText('Giáo viên đã duyệt');
  await page.getByTestId('s2e6-submit').click();
  await expect(page.getByTestId('s2e6-doc')).toHaveAttribute('data-status', 'pending');
  await expect(page.getByTestId('s2e6-doc-status')).toHaveText('Chờ soát');
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  const q = await ok<{ items: Array<{ docId: string }> }>(await queue(request, mai));
  expect(q.items.map((x) => x.docId)).toContain(s.doc(K.FX14));
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  expect((await ok<CardPage>(await list(request, trang, { shelf: 'mine' }))).items.map((c) => c.id)).toContain(s.doc(K.FX14));
  expect((await ok<CardPage>(await list(request, trang, { shelf: 'library' }))).items).toHaveLength(0);
});

test('@TC-S2E6-014 người soát duyệt thì đề vào Kho chung', async ({ page, request }, info) => {
  const s = slot(6, 14, info);
  await signIn(page, request, s.org, s.mai, 'Mai');
  await openLibrary(page);
  await page.getByTestId('s2e6-queue-link').click();
  const row = page.getByTestId(`s2e6-queue-${s.doc(K.FX14)}`);
  await expect(row).toBeVisible();
  await row.getByTestId('s2e6-accept').click();
  await expect(row.getByTestId('s2e6-queue-result')).toHaveText('Đã vào Kho chung');
  await openLibrary(page);
  await page.getByTestId('s2e6-tab-library').click();
  await expect(cards(page)).toHaveCount(1);
  await expect(cards(page).first().getByTestId('s2e6-card-title')).toHaveText(TITLE.FX14);
  await expect(cards(page).first().getByTestId('s2e6-human')).toHaveText('Giáo viên đã duyệt');
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  const d = await ok<{ status: string; statusLabel: string }>(await detail(request, trang, s.doc(K.FX14)));
  expect(d).toMatchObject({ status: 'accepted', statusLabel: 'Đã vào Kho chung' });
});

test('@TC-S2E6-015 người soát từ chối có lý do', async ({ page, request }, info) => {
  const s = slot(6, 15, info);
  await signIn(page, request, s.org, s.mai, 'Mai');
  await page.goto('/gv/kho-de-cua-toi/hang-doi-soat');
  const row = page.getByTestId(`s2e6-queue-${s.doc(K.FX14)}`);
  await row.getByTestId('s2e6-reject').click();
  await row.getByTestId('s2e6-reason').fill('Đáp số chưa khớp phần b');
  await row.getByTestId('s2e6-reject-confirm').click();
  await expect(row.getByTestId('s2e6-queue-result')).toHaveText('Bị từ chối');
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  const d = await ok<{ status: string; reason: string }>(await detail(request, trang, s.doc(K.FX14)));
  expect(d).toMatchObject({ status: 'rejected', reason: 'Đáp số chưa khớp phần b' });
  expect((await ok<CardPage>(await list(request, trang, { shelf: 'library' }))).items).toHaveLength(0);
});

test('@TC-S2E6-020 lý do từ chối 4 ký tự bị chặn, 5 ký tự nhận', async ({ page, request }, info) => {
  const s = slot(6, 20, info);
  await signIn(page, request, s.org, s.mai, 'Mai');
  await page.goto('/gv/kho-de-cua-toi/hang-doi-soat');
  const row = page.getByTestId(`s2e6-queue-${s.doc(K.FX14)}`);
  await row.getByTestId('s2e6-reject').click();
  await row.getByTestId('s2e6-reason').fill('abcd');
  await row.getByTestId('s2e6-reject-confirm').click();
  await expect(row.getByTestId('s2e6-reason-error')).toHaveText(MSG.reasonRange);
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  expect((await ok<{ status: string }>(await detail(request, trang, s.doc(K.FX14)))).status).toBe('pending');
  await row.getByTestId('s2e6-reason').fill('abcde');
  await row.getByTestId('s2e6-reject-confirm').click();
  await expect(row.getByTestId('s2e6-queue-result')).toHaveText('Bị từ chối');
});

test('@TC-S2E6-024 đề bị từ chối gửi lại được sau khi sửa, lịch sử giữ dòng từ chối', async ({ page, request }, info) => {
  const s = slot(6, 24, info);
  const t = await signIn(page, request, s.org, s.trang, 'Cô Trang');
  const doc = s.doc(K.FX14);
  const steps = [
    'SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.',
    'ABCD vuông cạnh a nên AC = a√2.',
    'Tam giác SAC vuông cân tại A vì SA = AC = a√2.',
    'Vậy góc giữa SC và mặt phẳng đáy bằng 45°.',
  ];
  const saved = await request.post(`${API}/api/v1/teacher/solutions/${doc}/versions`, { headers: bearer(t), data: { baseVersion: 1, steps } });
  expect(saved.status(), await saved.text()).toBe(200);
  const approved = await request.post(`${API}/api/v1/teacher/solutions/${doc}/approve`, {
    headers: bearer(t),
    data: { version: 2, confirmNeedsReview: true },
  });
  expect(approved.status(), await approved.text()).toBe(200);
  await page.goto(`/gv/kho-de-cua-toi/de/${doc}`);
  await expect(page.getByTestId('s2e6-doc')).toHaveAttribute('data-status', 'rejected');
  await expect(page.getByTestId('s2e6-doc-reason')).toContainText('Đáp số chưa khớp phần b');
  await page.getByTestId('s2e6-submit').click();
  await expect(page.getByTestId('s2e6-doc')).toHaveAttribute('data-status', 'pending');
  await expect(page.getByTestId('s2e6-history-rejected')).toContainText('Đáp số chưa khớp phần b');
  await expect(page.getByTestId('s2e6-history-pending')).toBeVisible();
});

test('@TC-S2E6-034 giáo viên xem được lời giải đề Kho chung kèm nhãn hai trục', async ({ page, request }, info) => {
  const s = slot(6, 34, info);
  await signIn(page, request, s.org, s.lan, 'Cô Lan');
  await openLibrary(page);
  await page.getByTestId('s2e6-tab-library').click();
  await page.getByTestId(`s2e6-card-${s.lib(K.LIB_FX14)}`).getByTestId('s2e6-card-title').click();
  await expect(page.getByTestId('s2e6-shared')).toBeVisible();
  await expect(page.getByTestId('s2e6-shared-machine')).toHaveText('Cần xem lại');
  await expect(page.getByTestId('s2e6-shared-human')).toHaveText('Giáo viên đã duyệt');
  await page.getByTestId('s2e6-shared-solution').click();
  await expect(page.getByTestId('s2e6-shared-steps').locator('li')).toHaveCount(4);
  await expect(page.getByTestId('s2e6-shared-steps')).toContainText('45°');
});
