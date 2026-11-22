import { expect, test } from '../../support/fixtures.ts';
import { CODE, MSG, PV, ipFor, resetAuthFailures, signInPage, studentJoinUI, withLock } from './e1.ts';

/** US-GEO-102 — Giáo viên mẫu vào hệ thống; US-GEO-104-SC2 lớp của giáo viên ở bước "1 · Chọn lớp". */

test.beforeEach(async ({ page }, testInfo) => {
  // mỗi test một địa chỉ IP (X-Forwarded-For, máy chủ dev tin) để bộ đếm thử sai không giẫm nhau
  await page.setExtraHTTPHeaders({ 'X-Forwarded-For': ipFor(testInfo) });
});

async function teacherLogin(page: import('@playwright/test').Page, code: string) {
  await page.goto('/gv/dang-nhap');
  await page.getByTestId('e1-teacher-login-code').fill(code);
  await page.getByTestId('e1-teacher-login-submit').click();
}

const classChips = (page: import('@playwright/test').Page) => page.getByTestId('e1-my-classes').locator('.chip');

/** Đăng nhập đúng bằng F-GEO-003 trong khóa chung với TC-GEO-0179 (dò mã cùng tài khoản). */
async function loginAsSampleTeacher(page: import('@playwright/test').Page) {
  await withLock('teacher-F-GEO-003', async () => {
    await teacherLogin(page, CODE['F-GEO-003']!);
    await expect(page).toHaveURL(/\/gv\/kho-de$/);
  });
}

test('Mã giáo viên đúng @TC-GEO-0111', async ({ page, request }, testInfo) => {
  await resetAuthFailures(request, { ips: [ipFor(testInfo)] });
  await loginAsSampleTeacher(page);
  await expect(page.getByTestId('e1-catalog')).toBeVisible();
  await expect(classChips(page)).toHaveText(['11A1', '9B']);
  await expect(page.getByTestId('e1-teacher-name')).toHaveText('Giáo viên mẫu');
});

test('Vào được từ điện thoại @TC-GEO-0112', async ({ page, request }, testInfo) => {
  await resetAuthFailures(request, { ips: [ipFor(testInfo)] });
  await page.setViewportSize({ width: 390, height: 844 }); // điện thoại màn dọc (F-GEO-103)
  await loginAsSampleTeacher(page);
  await expect(page.getByTestId('e1-problem-HKG-01')).toBeVisible();
  const assign = page.getByTestId('e1-problem-HKG-01-assign');
  await expect(assign).toHaveText('Giao bài');
  await expect(assign).toBeInViewport({ ratio: 1 });
  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
  expect(overflow, 'không phải cuộn ngang').toBeLessThanOrEqual(0);
});

test('Mã giáo viên sai @TC-GEO-0113', async ({ page }) => {
  await teacherLogin(page, CODE['F-GEO-004']!);
  await expect(page.getByTestId('e1-teacher-login-error')).toHaveText(MSG['12']);
  await expect(page).toHaveURL(/\/gv\/dang-nhap$/);
  expect(await page.evaluate(() => sessionStorage.getItem('geo.token'))).toBeNull();
});

test('Không có công tắc đổi vai @TC-GEO-0115', async ({ page }) => {
  await studentJoinUI(page, 'An', 'HK7M2A');
  await expect(page.getByTestId('e1-greeting')).toHaveText('Chào An');
  // toàn bộ màn và thanh bên: không có công tắc "Giáo viên / Học sinh", không lối tắt sang vùng giáo viên
  await expect(page.getByRole('switch')).toHaveCount(0);
  await expect(page.getByText(/Giáo viên\s*\/\s*Học sinh/)).toHaveCount(0);
  await expect(page.getByTestId('hs-rail')).not.toContainText('Giáo viên');
  await expect(page.locator('a[href^="/gv"]')).toHaveCount(0);
  // đường duy nhất: ra màn "Vào lớp", bấm "Tôi là giáo viên" rồi nhập mã
  await page.goto('/hs/vao-lop');
  await page.getByTestId('e1-join-teacher-link').click();
  await expect(page).toHaveURL(/\/gv\/dang-nhap$/);
  await expect(page.getByTestId('e1-teacher-login-code')).toBeVisible();
});

test('Mỗi mã vào đúng tài khoản được cấp @TC-GEO-0174', async ({ page, request }, testInfo) => {
  await resetAuthFailures(request, { ips: [ipFor(testInfo)] });
  await teacherLogin(page, CODE['F-GEO-005']!);
  await expect(page).toHaveURL(/\/gv\/kho-de$/);
  await expect(page.getByTestId('e1-teacher-name')).toHaveText('Giáo viên C');
  await expect(classChips(page)).toHaveText(['9C']);
  await expect(page.getByTestId('e1-catalog')).not.toContainText(/11A1|9B|THPT Mẫu A/);
});

test('Mã đã thu hồi @TC-GEO-0175', async ({ page }) => {
  await teacherLogin(page, CODE['F-GEO-006']!);
  await expect(page.getByTestId('e1-teacher-login-error')).toHaveText(MSG['12']);
  expect(await page.evaluate(() => sessionStorage.getItem('geo.token'))).toBeNull();
});

test('Giáo viên chỉ thấy lớp mình dạy @TC-GEO-0124', async ({ page, request }) => {
  await signInPage(page, request, 'F-GEO-003');
  await page.goto(`/gv/giao-bai/${PV('HKG-01')}`);
  const step = page.getByTestId('e1-assign-step-class');
  await expect(step.getByRole('heading')).toHaveText('1 · Chọn lớp');
  await expect(step.locator('label')).toHaveText(['11A1', '9B']);
  await expect(step).not.toContainText('9C');
});
