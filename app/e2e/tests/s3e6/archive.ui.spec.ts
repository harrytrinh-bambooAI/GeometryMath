import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { MSG as S3E1, myClasses, signIn, teacherApi, world, type Person } from '../s3e1/s3e1.ts';
import { MSG } from './s3e6.ts';

/** US-S3E6-02 trên trình duyệt: lưu trữ / bỏ lưu trữ lớp ở màn một lớp; học sinh thấy "Lớp đã lưu trữ". */

async function openClass(page: Page, teacher: Person, classId: string) {
  await signIn(page, teacher);
  await page.goto(`/gv/lop-hoc/${classId}`);
  await expect(page.getByTestId('s3e1-class')).toBeVisible();
}

async function archiveUI(page: Page, name: string) {
  await page.getByTestId('s3e6-archive').click();
  await expect(page.getByTestId('s3e6-archive-dialog')).toContainText(MSG.s93(name));
  await page.getByTestId('s3e6-archive-confirm').click();
  await expect(page.getByTestId('s3e6-archived')).toBeVisible();
}

test('Lưu trữ lớp: trạng thái, mã và hiển thị của học sinh @TC-S3E6-15', async ({ page, request, browser }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A2']!;
  await openClass(page, lan, c.id);
  await archiveUI(page, '11A2');
  // rời danh sách lớp Đang dạy
  const list = (await (await teacherApi.list(request, lan.token)).json()) as { items: Array<{ id: string; status: string }> };
  expect(list.items.find((x) => x.id === c.id)?.status).toBe('archived');
  // Quốc Bảo mở Bài của em
  const ctx = await browser.newContext();
  const student = await ctx.newPage();
  await signIn(student, w.people['Quốc Bảo']!);
  await student.goto('/hs/bai-cua-em');
  const box = student.getByTestId('s3e6-archived-classes');
  await expect(box).toContainText('11A2');
  await expect(box).toContainText(MSG.s96);
  await ctx.close();
});

test('Lưu trữ lớp có em Chờ duyệt thì các em đó bị từ chối tự động @TC-S3E6-16', async ({ page, request, browser }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A1']!;
  await openClass(page, lan, c.id);
  await archiveUI(page, '11A1');
  const pending = (await (await teacherApi.pending(request, lan.token, c.id)).json()) as { items: unknown[] };
  expect(pending.items).toHaveLength(0);
  const nam = w.people['Hoàng Nam']!;
  const mine = (await (await myClasses(request, nam.token)).json()) as { items: Array<{ className: string; status: string }> };
  expect(mine.items.find((x) => x.className === '11A1')?.status).toBe('rejected');
  const ctx = await browser.newContext();
  const p = await ctx.newPage();
  await signIn(p, nam);
  await p.goto('/hs/lop-cua-em');
  await expect(p.getByTestId('s3e1-join-rejected')).toContainText('11A1');
  await ctx.close();
});

test('Bỏ lưu trữ thì lớp về Đang dạy với mã lớp mới @TC-S3E6-17', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A2']!;
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  await openClass(page, lan, c.id);
  await page.getByTestId('s3e6-unarchive').click();
  await page.getByTestId('s3e6-archive-confirm').click();
  await expect(page.getByTestId('s3e6-archive')).toBeVisible();
  const d = (await (await teacherApi.detail(request, lan.token, c.id)).json()) as { status: string; code: string };
  expect(d.status).toBe('active');
  expect(d.code).not.toBe(c.code);
  await expect(page.getByTestId('s3e1-class-code')).toHaveText(d.code);
  // em Đang học thấy lại lớp
  const bao = w.people['Quốc Bảo']!;
  const mine = (await (await myClasses(request, bao.token)).json()) as { items: Array<{ className: string; archived: boolean }> };
  expect(mine.items.find((x) => x.className === '11A2')?.archived).toBe(false);
});

test('Học sinh nhập mã của lớp đã lưu trữ @TC-S3E6-18', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A2']!;
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  const bao = w.people['Gia Bảo']!;
  await signIn(page, bao);
  await page.goto('/hs/lop-cua-em');
  await page.getByTestId('s3e1-join-code').fill(c.code);
  await page.getByTestId('s3e1-join-submit').click();
  await expect(page.getByTestId('s3e1-join-error')).toHaveText(S3E1.s01);
  const mine = (await (await myClasses(request, bao.token)).json()) as { items: unknown[] };
  expect(mine.items).toHaveLength(0);
});

test('Hủy ở hộp lưu trữ không đổi gì @TC-S3E6-26', async ({ page, request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A1']!;
  await openClass(page, lan, c.id);
  await page.getByTestId('s3e6-archive').click();
  await expect(page.getByTestId('s3e6-archive-dialog')).toContainText(MSG.s93('11A1'));
  await page.getByTestId('s3e6-archive-cancel').click();
  await expect(page.getByTestId('s3e6-archive-dialog')).toHaveCount(0);
  const d = (await (await teacherApi.detail(request, lan.token, c.id)).json()) as { status: string; code: string };
  expect(d.status).toBe('active');
  expect(d.code).toBe(c.code);
});
