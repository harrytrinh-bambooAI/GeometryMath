import { createHash } from 'node:crypto';
import type { Page, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, MSG, PASSWORD, hook, signIn, world } from '../s3e1/s3e1.ts';

/** US-S3E5-02 trên trình duyệt: tick ở màn đăng ký, màn thông báo riêng tư ở lần vào đầu. */

const ipOf = (t: TestInfo) => {
  const h = createHash('sha256').update(`s3e5|${t.titlePath.join('/')}|${t.project.name}`).digest();
  return `10.${150 + (h[0]! % 40)}.${h[1]}.${(h[2]! % 250) + 1}`;
};

async function loginUI(page: Page, userCode: string) {
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await page.goto('/hs/dang-nhap-ma');
  await page.getByTestId('s2e1-login-code').fill(userCode);
  await page.getByTestId('s2e1-login-password').fill(PASSWORD);
  await page.getByTestId('s2e1-login-submit').click();
  await expect(page).toHaveURL(/\/hs\/bai-cua-em/);
}

test('Đăng ký có tick thì lưu cờ và phiên bản @TC-S3E5-16', async ({ page, request }, testInfo) => {
  await page.setExtraHTTPHeaders({ 'X-Forwarded-For': ipOf(testInfo) });
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await page.goto('/hs/dang-ky');
  await page.getByTestId('s2e1-role-student').click();
  await page.getByTestId('s2e1-name').fill('Thu Hà');
  await page.getByTestId('s2e1-password').fill('ThuHa#2026x');
  await expect(page.getByTestId('s2e1-register')).toContainText('bản 1.0');
  await page.getByTestId('s2e1-privacy').check();
  const resp = page.waitForResponse((r) => r.url().includes('/api/v1/auth/register'));
  await page.getByTestId('s2e1-register-submit').click();
  const reg = (await (await resp).json()) as { userId: string; userCode: string };
  expect(reg.userCode).toMatch(/^HS-/);
  await expect(page.getByTestId('s2e1-codes')).toContainText(reg.userCode);
  const flag = (await (await request.get(`${API}/test-hooks/s3e5/privacy?userId=${reg.userId}`)).json()) as { version: string };
  expect(flag.version).toBe('1.0');
});

test('Tài khoản MVP chuyển sang thấy thông báo ở lần vào đầu tiên @TC-S3E5-18', async ({ page, request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  await hook(request, 's3e5/privacy/clear', { userId: an.id });
  await loginUI(page, an.userCode);
  await expect(page.getByTestId('s3e5-privacy-title')).toHaveText(/Thông báo riêng tư \(bản 1\.0\)/);
  await expect(page.getByTestId('e1-my-assignments')).toHaveCount(0);
  await page.getByTestId('s3e5-privacy-ack').click();
  await expect(page.getByTestId('e1-my-assignments')).toBeVisible();
  const p2 = await page.context().newPage();
  await loginUI(p2, an.userCode);
  await expect(p2.getByTestId('e1-my-assignments')).toBeVisible();
  await expect(p2.getByTestId('s3e5-privacy')).toHaveCount(0);
});

test('Phiên bản văn bản tăng thì hiện lại một lần @TC-S3E5-19', async ({ page, request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  await hook(request, 's3e5/privacy/current', { userId: an.id, version: '1.1' });
  try {
    await loginUI(page, an.userCode);
    await expect(page.getByTestId('s3e5-privacy-title')).toHaveText(/bản 1\.1/);
    await page.getByTestId('s3e5-privacy-ack').click();
    await expect(page.getByTestId('e1-my-assignments')).toBeVisible();
    const flag = (await (await request.get(`${API}/test-hooks/s3e5/privacy?userId=${an.id}`)).json()) as { version: string };
    expect(flag.version).toBe('1.1');
    const p2 = await page.context().newPage();
    await loginUI(p2, an.userCode);
    await expect(p2.getByTestId('s3e5-privacy')).toHaveCount(0);
  } finally {
    await hook(request, 's3e5/privacy/current', { userId: an.id, version: '' });
  }
});

test('Văn bản nêu đủ các ý bắt buộc @TC-S3E5-20', async ({ page, request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  await hook(request, 's3e5/privacy/clear', { userId: an.id });
  await signIn(page, an);
  await page.goto('/hs/bai-cua-em');
  const body = page.getByTestId('s3e5-privacy-body');
  await expect(body).toContainText('chỉ thu tên hiển thị, mã người dùng, vai (giáo viên hoặc học sinh), lớp và bài làm');
  await expect(body).toContainText('không thu email, số điện thoại, ngày sinh, trường hay ảnh');
  await expect(body).toContainText('Học sinh quên mật khẩu thì dùng mã khôi phục của mình hoặc nhờ giáo viên của lớp đặt lại');
  await expect(body).toContainText('Giáo viên mất cả mật khẩu lẫn mã khôi phục thì không lấy lại được');
});

test('Đã đọc bản hiện hành thì không hiện lại @TC-S3E5-21', async ({ page, request }) => {
  const w = await world(request);
  await loginUI(page, w.people['Bình An']!.userCode);
  await expect(page.getByTestId('e1-my-assignments')).toBeVisible();
  await expect(page.getByTestId('s3e5-privacy')).toHaveCount(0);
});

test('Đăng ký không tick bị từ chối @TC-S3E5-22', async ({ page, request }, testInfo) => {
  const ip = ipOf(testInfo);
  await page.setExtraHTTPHeaders({ 'X-Forwarded-For': ip });
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await page.goto('/hs/dang-ky');
  await page.getByTestId('s2e1-role-student').click();
  await page.getByTestId('s2e1-name').fill('Thu Hà');
  await page.getByTestId('s2e1-password').fill('ThuHa#2026x');
  await expect(page.getByTestId('s2e1-register-submit')).toBeDisabled(); // giao diện S2-E1 khóa nút khi chưa tick
  const r = await request.post(`${API}/api/v1/auth/register`, {
    headers: { 'X-Forwarded-For': ip },
    data: { displayName: 'Thu Hà', role: 'student', password: 'ThuHa#2026x', privacyAck: false, privacyVersion: '1.0' },
  });
  expect(r.status()).toBe(400);
  const e = (await r.json()) as { code: string; details: Array<{ field: string; message: string }> };
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.details[0]).toEqual({ field: 'privacyAck', message: MSG.s84 });
  expect(await r.text()).not.toMatch(/HS-/);
});
