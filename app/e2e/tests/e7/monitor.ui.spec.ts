import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { call } from '../e4/e4.ts';
import { API, NAMES, asg, bearer, box, golden, signInAs, st } from './e7.ts';
import type { Box } from './e7.ts';

/** E7 — màn Theo dõi lớp (US-GEO-701, 702; khung Design/mvp-03), giáo viên trên máy tính (F-GEO-102) và iPad. */

async function openMonitor(page: Page, b: Box, key = 'BG-1') {
  await signInAs(page, b.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto(`/gv/theo-doi/${asg(b, key)}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
}
const names = (page: Page) => page.locator('[data-testid^="e7-name-"]').allTextContents();

test('Bốn thẻ tổng @TC-GEO-0700 @smoke', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  await expect(page.getByTestId('e7-title')).toHaveText('Theo dõi lớp 11A1');
  await expect(page.getByTestId('e7-subtitle')).toContainText(/Bài: .+ · giao lúc \d{2}:\d{2}, \d{2}\/\d{2}\/\d{4}/);
  await expect(page.getByTestId('e7-card-opened')).toHaveText('4/5 đã mở bài');
  await expect(page.getByTestId('e7-card-hints')).toHaveText('3 dùng gợi ý');
  await expect(page.getByTestId('e7-card-questions')).toHaveText('7 câu hỏi');
  await expect(page.getByTestId('e7-card-figure')).toHaveText('12′ thao tác hình');
});

test('Bảng học sinh @TC-GEO-0701 @smoke', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  expect(await names(page)).toEqual(NAMES);
  for (const n of ['An', 'Bình', 'Chi', 'Dũng']) await expect(page.getByTestId(`e7-opened-${tidName(n)}`)).toHaveText('Đã mở bài');
  await expect(page.getByTestId('e7-opened-Ha')).toHaveText('Chưa mở');
  const times = await page.locator('[data-testid^="e7-time-"]').allTextContents();
  expect(times).toEqual(['18 phút', '11 phút', '9 phút', '21 phút', '—']);
});

const tidName = (n: string) => n.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D');
const status = (page: Page) => page.locator('[data-testid^="e7-status-"]').allTextContents();

test('Tình trạng khi bài còn khóa @TC-GEO-0702', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  expect(await status(page)).toEqual(['Đang chờ lời giải', 'Đang làm', 'Tự làm, không cần gợi ý', 'Đang chờ lời giải', 'Chưa bắt đầu']);
});

test('Tình trạng sau khi mở lời giải @TC-GEO-0703', async ({ page, request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  // Giáo viên mẫu "Mở ngay" BG-1; An xem lời giải; Dũng chưa xem
  const r = await call.release(request, b.teacher.token, id, { openNow: true });
  expect(r.status(), await r.text()).toBe(200);
  expect((await call.solution(request, st(b, 'An').token, id)).status()).toBe(200);
  await request.post(`${API}/test-hooks/e7/restart`, { data: { assignmentId: id } });
  await openMonitor(page, b);
  await page.reload();
  await expect.poll(() => status(page)).toEqual(['Đã xem lời giải', 'Đang làm', 'Tự làm, không cần gợi ý', 'Đang làm', 'Chưa bắt đầu']);
  await expect(page.getByTestId('e7-lock')).toHaveCount(0);
});

test('Thẻ vàng khi lời giải khóa @TC-GEO-0704', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b, 'BG-1');
  const lock = page.getByTestId('e7-lock');
  await expect(lock).toContainText('Lời giải đang khóa');
  await expect(lock.getByTestId('e6-release-card-when')).toHaveText(/^Mở lúc \d{2}:\d{2}, \d{2}\/\d{2}\/\d{4}$/);
  await expect(page.getByTestId('e7-lock-left')).toHaveText('còn 1 giờ 0 phút');
  await expect(lock.getByRole('button', { name: 'Mở ngay' })).toBeVisible();
  await expect(lock.getByRole('button', { name: 'Đổi giờ' })).toBeVisible();
  await page.goto(`/gv/theo-doi/${asg(b, 'BG-2')}`);
  await expect(page.getByTestId('e7-lock').getByTestId('e6-release-card-when')).toHaveText('Chưa đặt giờ mở');
  await expect(page.getByTestId('e7-lock').getByRole('button', { name: 'Mở ngay' })).toBeVisible();
  await expect(page.getByTestId('e7-lock').getByRole('button', { name: 'Đổi giờ' })).toBeVisible();
  await page.goto(`/gv/theo-doi/${asg(b, 'BG-3')}`);
  await expect(page.getByTestId('e7-monitor')).toBeVisible();
  await expect(page.getByTestId('e7-lock')).toHaveCount(0);
});

test('Vào trang từ mục "Theo dõi" @TC-GEO-0705', async ({ page, request }) => {
  const b = await box(
    request,
    golden({
      classes: [
        {
          name: '9B',
          level: 'thcs',
          students: ['Khánh'],
          assignments: [
            { key: 'BG-4', code: 'HKG-04' },
            { key: 'BG-5', code: 'HKG-05', release: 'unset' },
          ],
        },
      ],
    }),
  );
  // lớp 9C của giáo viên khác cùng nhóm thử
  await box(request, { classes: [{ name: '9C', level: 'thcs', students: ['Long'], assignments: [{ key: 'BG-C', code: 'HKG-04' }] }] });
  await signInAs(page, b.teacher, 'Giáo viên mẫu', 'teacher');
  await page.goto('/gv');
  await page.getByTestId('e7-nav-theo-doi').click();
  const rows = page.getByTestId('e7-assigned-row');
  await expect(rows).toHaveCount(5);
  const ids = await rows.evaluateAll((els) => els.map((e) => e.getAttribute('data-id')));
  expect(ids).toEqual(['BG-5', 'BG-4', 'BG-3', 'BG-2', 'BG-1'].map((k) => asg(b, k)));
  await expect(rows.nth(0)).toContainText('9B');
  await expect(rows.nth(0)).toContainText('Chưa đặt giờ');
  await expect(rows.nth(2)).toContainText('Đã mở');
  await expect(rows.nth(4)).toContainText('11A1');
  await expect(rows.nth(4)).toContainText('Sắp mở');
  await expect(page.getByTestId('e7-assigned')).not.toContainText('9C');
  await rows.nth(4).getByRole('link').click();
  await expect(page).toHaveURL(new RegExp(`/gv/theo-doi/${asg(b, 'BG-1')}$`));
  await expect(page.getByTestId('e7-card-opened')).toHaveText('4/5 đã mở bài');
});

test('Chưa em nào mở bài @TC-GEO-0707', async ({ page, request }) => {
  const b = await box(request, { classes: [{ name: '11A1', students: NAMES, assignments: [{ key: 'BG-1', code: 'HKG-01' }] }] });
  await openMonitor(page, b);
  await expect(page.getByTestId('e7-card-opened')).toHaveText('0/5 đã mở bài');
  await expect(page.getByTestId('e7-card-hints')).toHaveText('0 dùng gợi ý');
  await expect(page.getByTestId('e7-card-questions')).toHaveText('0 câu hỏi');
  await expect(page.getByTestId('e7-card-figure')).toHaveText('0′ thao tác hình');
  await expect(page.getByTestId('e7-empty-opened')).toHaveText('Chưa có học sinh nào mở bài.');
  expect(await names(page)).toEqual(NAMES);
  for (const n of NAMES) {
    const t = tidName(n);
    await expect(page.getByTestId(`e7-opened-${t}`)).toHaveText('Chưa mở');
    await expect(page.getByTestId(`e7-asks-${t}`)).toHaveText('—');
    await expect(page.getByTestId(`e7-time-${t}`)).toHaveText('—');
    await expect(page.getByTestId(`e7-status-${t}`)).toHaveText('Chưa bắt đầu');
  }
});

test('Không có xếp hạng, không sắp theo kết quả @TC-GEO-0708', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  for (const h of ['Thời gian', 'Gợi ý 1 · 2', 'Hỏi đáp', 'Tình trạng']) {
    await page.getByRole('columnheader', { name: h }).click();
    expect(await names(page)).toEqual(NAMES);
  }
  const text = await page.getByTestId('e7-monitor').innerText();
  for (const s of ['Hạng', 'hạng', '#1', 'Điểm', 'Huy hiệu', 'Top', 'giỏi nhất']) expect(text).not.toContain(s);
});

test('Không có "Nên nhắc lại trên lớp" @TC-GEO-0709', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  const right = page.getByTestId('e7-right');
  await expect(right.getByTestId('e7-lock')).toBeVisible();
  await expect(right.getByTestId('e7-asking')).toContainText('Học sinh đang hỏi gì');
  await expect(page.getByText('Nên nhắc lại trên lớp')).toHaveCount(0);
  await expect(right).not.toContainText('học sinh vướng');
});

test('Thanh bên không có "Tổng quan" và "Lớp học" @TC-GEO-0744', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  const rail = page.getByTestId('gv-rail');
  await expect(rail.getByTestId('e7-nav-theo-doi')).toHaveText('Theo dõi');
  await expect(rail).not.toContainText('Tổng quan');
  await expect(rail).not.toContainText('Lớp học');
});

test('Mức gợi ý của từng em @TC-GEO-0713', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  const lit = async (n: string) => page.getByTestId(`e7-hints-${tidName(n)}`).locator('.e7-dot.on').count();
  expect([await lit('An'), await lit('Bình'), await lit('Chi'), await lit('Dũng'), await lit('Hà')]).toEqual([2, 1, 0, 2, 0]);
});

test('Số câu hỏi của từng em @TC-GEO-0714', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  const asks = await page.locator('[data-testid^="e7-asks-"]:not([data-testid^="e7-asks-btn"])').allTextContents();
  expect(asks).toEqual(['3 câu', '2 câu', '—', '2 câu', '—']);
});

test('Câu hỏi của một em @TC-GEO-0715', async ({ page, request }) => {
  const b = await box(request, golden({ questions: [{ assignment: 'BG-3', student: 'An', text: 'Câu của An trên BG-3?', minutesAgo: 1 }] }));
  await openMonitor(page, b);
  await page.getByTestId('e7-asks-btn-An').click();
  const items = page.getByTestId('e7-asks-list-An').getByTestId('e7-asks-item');
  await expect(items).toHaveCount(3);
  // mới nhất ở trên: F-GEO-057, 056, 055
  await expect(items.nth(0)).toContainText('Cho em xin đáp số luôn được không?');
  await expect(items.nth(1)).toContainText('Góc cần tìm là góc nào trên hình?');
  await expect(items.nth(2)).toContainText('Vì sao A là hình chiếu của S?');
  await expect(page.getByTestId('e7-asks-list-An')).not.toContainText('Câu của An trên BG-3?');
});

test('Khối "Học sinh đang hỏi gì" gộp câu trùng @TC-GEO-0716', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b);
  const groups = page.getByTestId('e7-group');
  await expect(groups).toHaveCount(4);
  const got = await groups.evaluateAll((els) => els.map((e) => (e as HTMLElement).innerText.replace(/\s+/g, ' ').trim()));
  // nhóm có câu hỏi mới nhất ở trên (BR-GEO-275)
  expect(got).toEqual([
    '"Tan của một góc là gì?" Dũng · 1 lượt',
    '"Cho em xin đáp số luôn được không?" An · 1 lượt',
    '"Góc cần tìm là góc nào trên hình?" Bình, An · 3 lượt',
    '"Vì sao A là hình chiếu của S?" An, Dũng · 2 lượt',
  ]);
});

test('Chưa ai hỏi @TC-GEO-0718', async ({ page, request }) => {
  const b = await box(request, golden());
  await openMonitor(page, b, 'BG-2');
  await expect(page.getByTestId('e7-asking-empty')).toHaveText('Chưa có câu hỏi nào.');
  const asks = await page.locator('[data-testid^="e7-asks-"]:not([data-testid^="e7-asks-btn"]):not([data-testid^="e7-asking"])').allTextContents();
  expect(asks).toEqual(['—', '—', '—', '—', '—']);
});

test('Không có nhận định do AI viết @TC-GEO-0720', async ({ page, request }) => {
  const b = await box(request, golden());
  const callsOf = async (userId: string) =>
    ((await (await request.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`)).json()) as unknown[]).length;
  const n = await callsOf(b.teacher.id);
  await openMonitor(page, b);
  await expect(page.getByTestId('e7-group')).toHaveCount(4);
  for (const g of await page.getByTestId('e7-group').all()) {
    await expect(g.getByTestId('e7-group-meta')).toHaveText(/^[^·]+ · \d+ lượt$/);
  }
  const asking = await page.getByTestId('e7-asking').innerText();
  for (const s of ['nên', 'vướng', 'gợi ý cho giáo viên', 'AI']) expect(asking).not.toContain(s);
  expect(await callsOf(b.teacher.id)).toBe(n);
  // dữ liệu trả về của trang không gọi AI cho bất kỳ học sinh nào
  for (const name of NAMES) expect(await callsOf(st(b, name).id)).toBe(0);
  expect((await request.get(`${API}/api/v1/teacher/assignments/${asg(b, 'BG-1')}/monitor`, { headers: bearer(b.teacher.token) })).status()).toBe(200);
});
