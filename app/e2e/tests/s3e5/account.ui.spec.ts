import { createHash } from 'node:crypto';
import type { TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, PASSWORD, ask, assignSet, bearer, codeLogin, err, signIn, world } from '../s3e1/s3e1.ts';
import { LOGIN_MSG, deleteAccount } from './account.ts';

/** US-S3E5-05 trên trình duyệt (Cài đặt › Xóa tài khoản) và US-S3E5-03 FC1 (màn đăng ký). */

const ipOf = (t: TestInfo) => {
  const h = createHash('sha256').update(`s3e5-acct|${t.titlePath.join('/')}|${t.project.name}`).digest();
  return `10.${110 + (h[0]! % 40)}.${h[1]}.${(h[2]! % 250) + 1}`;
};

test('Học sinh xóa tài khoản của mình @TC-S3E5-53', async ({ page, request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!.id;
  // Bình An có bài làm ở bộ tuần 15
  const set = await assignSet(request, lan, c11);
  expect((await ask(request, an.token, set.items[0]!.assignmentId)).status()).toBeLessThan(500);
  const board = `${API}/api/v1/teacher/class-results/${c11}/sets/${set.id}/board`;
  const before = await request.get(board, { headers: bearer(lan.token) });
  expect(before.status()).toBe(200);
  expect(await before.text()).toContain('Bình An');

  await signIn(page, an);
  await page.goto('/hs/cai-dat');
  await page.getByTestId('s3e5-delete-open').click();
  await page.getByTestId('s3e5-delete-password').fill(PASSWORD);
  await page.getByTestId('s3e5-delete-confirm').fill('XÓA');
  const resp = page.waitForResponse((r) => r.url().includes(`/api/v1/account/users/${an.id}/delete`));
  await page.getByTestId('s3e5-delete-submit').click();
  expect((await resp).status()).toBe(200);
  await expect(page).toHaveURL(/\/\?da-xoa-tai-khoan=1$/);

  // đăng nhập lại bị từ chối với câu chung
  const login = await codeLogin(request, an.userCode, PASSWORD);
  expect(login.status()).toBe(400);
  expect((await err(login)).message).toBe(LOGIN_MSG);
  // bảng lớp không còn dòng Bình An; số liệu tính lại không có em
  const after = await request.get(board, { headers: bearer(lan.token) });
  expect(after.status()).toBe(200);
  expect(await after.text()).not.toContain('Bình An');
  const students = await request.get(`${API}/api/v1/teacher/my-classes/${c11}/students`, { headers: bearer(lan.token) });
  expect(await students.text()).not.toContain('Bình An');
});

test('Văn bản thông báo riêng tư nêu chính sách lưu và xóa @TC-S3E5-55', async ({ page, request }) => {
  const w = await world(request);
  await signIn(page, w.people['Bình An']!);
  await page.goto('/hs/rieng-tu');
  await expect(page.getByTestId('s3e5-notice-title')).toContainText('Thông báo riêng tư (bản 1.0)');
  const body = page.getByTestId('s3e5-notice-body');
  await expect(body).toContainText('Bài làm được giữ tới khi bạn xóa tài khoản');
  await expect(body).toContainText('không quá 7 ngày');
  await expect(body).toContainText('tối đa 30 ngày');
  const link = page.getByTestId('s3e5-privacy-delete-link');
  await expect(link).toContainText('Xóa tài khoản');
  await link.click();
  await expect(page).toHaveURL(/\/hs\/cai-dat#xoa-tai-khoan$/);
  await expect(page.getByTestId('s3e5-delete-open')).toBeVisible();
});

test('Chưa gõ XÓA thì nút xác nhận bị khóa @TC-S3E5-59', async ({ page, request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  await signIn(page, an);
  await page.goto('/hs/cai-dat');
  await page.getByTestId('s3e5-delete-open').click();
  await page.getByTestId('s3e5-delete-password').fill(PASSWORD);
  const submit = page.getByTestId('s3e5-delete-submit');
  await expect(submit).toBeDisabled();
  await page.getByTestId('s3e5-delete-confirm').fill('xoa');
  await expect(submit).toBeDisabled();
  await page.getByTestId('s3e5-delete-confirm').fill('XÓA');
  await expect(submit).toBeEnabled();
  // gọi API thẳng không có chữ xác nhận
  const r = await deleteAccount(request, an.token, an.id, { password: PASSWORD });
  expect(r.status()).toBe(400);
  const e = await err(r);
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.details?.[0]?.field).toBe('confirm');
  expect((await codeLogin(request, an.userCode, PASSWORD)).status()).toBe(200);
});
