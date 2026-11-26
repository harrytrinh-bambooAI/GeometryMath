import { expect, test } from '../../support/fixtures.ts';
import { untilApi } from '../../support/wait.ts';
import { API, BG, MSG, bearer, devToken, studentJoinUI } from './e1.ts';

/** US-GEO-101 — Học sinh vào lớp bằng tên hiển thị và mã lớp (màn "Vào lớp", "Bài của em"). */

const cards = (page: import('@playwright/test').Page) => page.locator('[data-assignment-id]');

test('Vào lớp bằng tên và mã lớp đúng @TC-GEO-0100', async ({ page }) => {
  await studentJoinUI(page, 'An', 'HK7M2A');
  await expect(page).toHaveURL(/\/hs\/bai-cua-em$/);
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
  await expect(cards(page)).toHaveCount(3);
  for (const code of ['HKG-01', 'HKG-02', 'HKG-03']) {
    await expect(page.getByTestId(`e1-assignment-${code}`)).toBeVisible();
  }
});

test('Tên chưa có trong lớp thì thành học sinh mới của lớp đó @TC-GEO-0101', async ({ page, request }) => {
  try {
    await studentJoinUI(page, 'Yến', 'HK7M2A');
    await expect(page.getByTestId('e1-greeting')).toHaveText('Chào Yến');
    await expect(cards(page)).toHaveCount(3);
    for (const code of ['HKG-01', 'HKG-02', 'HKG-03']) {
      await expect(page.getByTestId(`e1-assignment-${code}-progress`)).toHaveText('0/3 · Chưa bắt đầu');
    }
    // Theo dõi lớp 11A1 của Giáo viên mẫu từ lúc đó có sáu học sinh (đệm Theo dõi lớp sống 5 giây)
    const teacher = await devToken(request, 'F-GEO-003');
    await expect
      .poll(
        async () => {
          const r = await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) });
          const m = (await r.json()) as { studentCount: number; students: Array<{ displayName: string }> };
          return { count: m.studentCount, hasYen: m.students.some((s) => s.displayName === 'Yến') };
        },
        { timeout: 20_000, intervals: [500, 1000, 2000] },
      )
      .toEqual({ count: 6, hasYen: true });
  } finally {
    // Khôi phục (QF-E1-02): KHÔNG xóa Yến giữa lượt vì cùng test chạy song song trên hai dự án (chromium, webkit)
    // dùng chung học sinh Yến; seed vàng được nạp lại đầu mỗi lượt (global-setup) nên lượt sau lại đủ 5 em.
  }
});

test('Vào lại với cùng tên thì giữ đúng dữ liệu của mình @TC-GEO-0102', async ({ page, request }) => {
  // thiết bị khác = ngữ cảnh trình duyệt mới của test này (không có phiên cũ).
  // Lời chào hiện ngay, thẻ bài chờ GET danh sách bài: chờ phản hồi đó thay vì 5 s của expect (T-261126-43)
  await untilApi(page, [{ method: 'GET', path: /^\/api\/v1\/student\/assignments$/ }], () => studentJoinUI(page, 'an', 'HK7M2A'));
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
  await expect(page.getByTestId('e1-assignment-HKG-01-progress')).toHaveText('2/3 · Đã xem gợi ý 2');
  const teacher = await devToken(request, 'F-GEO-003');
  const m = (await (
    await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) })
  ).json()) as { students: Array<{ displayName: string }> };
  expect(m.students.filter((s) => s.displayName.toLowerCase() === 'an')).toHaveLength(1);
});

test('Mã lớp quyết định trường và lớp @TC-GEO-0103', async ({ page }) => {
  await studentJoinUI(page, 'An', 'MC9C6R');
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
  await expect(cards(page)).toHaveCount(1);
  await expect(cards(page).first()).toHaveAttribute('data-assignment-id', BG('BG-C'));
  for (const code of ['HKG-01', 'HKG-02', 'HKG-03']) {
    await expect(page.getByTestId(`e1-assignment-${code}`)).toHaveCount(0);
  }
  // tiến độ của An lớp 11A1 (2/3 trên BG-1) không hiện ở đây
  await expect(page.getByTestId('e1-assignment-HKG-04-progress')).toHaveText('0/3 · Chưa bắt đầu');
});

test('Màn "Vào lớp" chỉ có hai ô nhập @TC-GEO-0104', async ({ page }) => {
  await page.goto('/hs/vao-lop');
  const form = page.getByTestId('e1-join');
  await expect(form.locator('input')).toHaveCount(2);
  await expect(form.getByText('Tên hiển thị', { exact: true })).toBeVisible();
  await expect(form.getByText('Mã lớp', { exact: true })).toBeVisible();
  await expect(page.getByTestId('e1-join-submit')).toHaveText('Vào lớp');
  await expect(page.getByTestId('e1-join-teacher-link')).toHaveText('Tôi là giáo viên');
  await expect(form.locator('input[type=email], input[type=tel], input[type=date]')).toHaveCount(0);
  await expect(form).not.toContainText(/email|điện thoại|ngày sinh/i);
  const req = page.waitForRequest((r) => r.url().endsWith('/api/v1/auth/student') && r.method() === 'POST');
  await page.getByTestId('e1-join-name').fill('An');
  await page.getByTestId('e1-join-code').fill('HK7M2A');
  await page.getByTestId('e1-join-submit').click();
  const body = JSON.parse((await req).postData() ?? '{}') as Record<string, unknown>;
  expect(Object.keys(body).sort()).toEqual(['displayName', 'joinCode']);
});

test('Tên hiển thị rỗng @TC-GEO-0107', async ({ page }) => {
  let posted = 0;
  page.on('request', (r) => {
    if (r.url().endsWith('/api/v1/auth/student')) posted++;
  });
  for (const blank of ['', '   ']) {
    await page.goto('/hs/vao-lop');
    await page.getByTestId('e1-join-name').fill(blank);
    await page.getByTestId('e1-join-code').fill('HK7M2A');
    await page.getByTestId('e1-join-submit').click();
    await expect(page.getByTestId('e1-join-name-error')).toHaveText(MSG['09']);
    await expect(page).toHaveURL(/\/hs\/vao-lop$/);
  }
  expect(posted, 'không gửi yêu cầu nên không có học sinh nào được tạo').toBe(0);
});

test('Tên hiển thị 61 ký tự @TC-GEO-0108', async ({ page, request }) => {
  const name = 'A'.repeat(61);
  await page.goto('/hs/vao-lop');
  await page.getByTestId('e1-join-name').fill(name);
  await page.getByTestId('e1-join-code').fill('HK7M2A');
  const res = page.waitForResponse((r) => r.url().endsWith('/api/v1/auth/student'));
  await page.getByTestId('e1-join-submit').click();
  const r = await res;
  expect(r.status()).toBe(400);
  expect(((await r.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  await expect(page.getByTestId('e1-join-name-error')).toHaveText(MSG['10']);
  await expect(page).toHaveURL(/\/hs\/vao-lop$/);
  const teacher = await devToken(request, 'F-GEO-003');
  const m = (await (
    await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) })
  ).json()) as { students: Array<{ displayName: string }> };
  expect(m.students.some((s) => s.displayName.startsWith('AAAAAAAAAA'))).toBe(false);
});

test('Mã lớp không tồn tại @TC-GEO-0109', async ({ page }) => {
  await page.goto('/hs/vao-lop');
  await page.getByTestId('e1-join-name').fill('An');
  await page.getByTestId('e1-join-code').fill('ZZ9ZZ9');
  const res = page.waitForResponse((r) => r.url().endsWith('/api/v1/auth/student'));
  await page.getByTestId('e1-join-submit').click();
  const body = await (await res).text();
  await expect(page.getByTestId('e1-join-code-error')).toHaveText(MSG['11']);
  await expect(page.getByTestId('e1-join-name')).toHaveValue('An');
  for (const leak of ['11A1', '9B', '9C', 'THPT', 'THCS', 'Mẫu']) expect(body).not.toContain(leak);
});

test('Phiên hết hạn @TC-GEO-0110', async ({ page, request }) => {
  await studentJoinUI(page, 'An', 'HK7M2A');
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
  // thẻ hết hạn (thời hạn thẻ: giả định của ARCH, NFR-GEO-059): thay bằng thẻ của An đã quá hạn
  const expired = await devToken(request, 'F-GEO-020', -60);
  await page.evaluate((t) => sessionStorage.setItem('geo.token', t), expired);
  // FIX4b: danh sách Bài của em (E6) tự hỏi lại mỗi 5 giây; với thẻ hết hạn, lượt hỏi đó có thể nhận 401 và chuyển trang
  // trước cú bấm. Chấp nhận 401 đầu tiên của bất kỳ yêu cầu học sinh nào (bấm mở bài hoặc lượt hỏi lại).
  const res = page.waitForResponse((r) => r.url().includes('/api/v1/student/assignments') && r.status() === 401);
  await page.getByTestId('e1-assignment-HKG-01').click({ timeout: 5000 }).catch(() => undefined);
  const r = await res;
  expect(r.status()).toBe(401);
  expect(((await r.json()) as { code: string }).code).toBe('UNAUTHENTICATED');
  await expect(page).toHaveURL(/\/hs\/vao-lop$/);
  await expect(page.getByTestId('e1-session-expired')).toHaveText(MSG['02']);
  await page.getByTestId('e1-join-name').fill('An');
  await page.getByTestId('e1-join-code').fill('HK7M2A');
  await page.getByTestId('e1-join-submit').click();
  await expect(page.getByTestId('e1-assignment-HKG-01-progress')).toHaveText('2/3 · Đã xem gợi ý 2');
});

test('Gõ sai mã lớp nhiều lần không bị khóa riêng @TC-GEO-0173', async ({ page }) => {
  const statuses: number[] = [];
  page.on('response', (r) => {
    if (r.url().endsWith('/api/v1/auth/student')) statuses.push(r.status());
  });
  await page.goto('/hs/vao-lop');
  await page.getByTestId('e1-join-name').fill('An');
  await page.getByTestId('e1-join-code').fill('ZZ9ZZ9');
  for (let i = 1; i <= 11; i++) {
    const res = page.waitForResponse((r) => r.url().endsWith('/api/v1/auth/student'));
    await page.getByTestId('e1-join-submit').click();
    await res;
    await expect(page.getByTestId('e1-join-code-error')).toHaveText(MSG['11']);
  }
  expect(statuses).toHaveLength(11);
  expect(statuses.every((s) => s === 400), `không lần nào 429: ${statuses.join(',')}`).toBe(true);
  await page.getByTestId('e1-join-code').fill('HK7M2A');
  await page.getByTestId('e1-join-submit').click();
  await expect(page).toHaveURL(/\/hs\/bai-cua-em$/);
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
});
