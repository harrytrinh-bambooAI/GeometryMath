import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, MSG, PASSWORD, assignSet, codeLogin, err, join, myClasses, practiceSets, signIn, teacherApi, world, type Person } from './s3e1.ts';

/**
 * US-S3E1-01…04, ca LOẠI B (DECISIONS 27/11 "Chiến lược E2E"): các ca của `classes.ui.spec.ts` chỉ còn kiểm dữ liệu /
 * nghiệp vụ (đã có ca loại A cùng màn kiểm đúng chỗ bấm), nay chạy bằng gọi API theo đúng thứ tự bước của luồng E2E;
 * cuối luồng mở trang trạng thái cuối bằng Playwright (không click) và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

/** Trang cuối: giáo viên mở màn Lớp học. */
const teacherEnd = (p: Person, path: string, testId: string) => async (page: Page) => {
  await signIn(page, p);
  await page.goto(path);
  await expect(page.getByTestId(testId).first()).toBeVisible();
};
/** Trang cuối của em: màn Lớp của em (trạng thái xin vào lớp / thông báo). */
const studentEnd = (p: Person, testId: string) => async (page: Page) => {
  await signIn(page, p);
  await page.goto('/hs/lop-cua-em');
  await expect(page.getByTestId(testId)).toBeVisible();
};

test('Tên lớp đúng 60 ký tự được nhận @TC-S3E1-03 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
  const lan = w.people['Cô Lan']!;
  const r = await teacherApi.create(request, lan.token, { name: 'A'.repeat(60), grade: 10, academicYear: '2026–2027' });
  expect(r.status()).toBe(201);
  const list = (await (await teacherApi.list(request, lan.token)).json()) as { items: Array<{ name: string }> };
  expect(list.items.map((c) => c.name)).toEqual(['A'.repeat(60)]);
  await captureFinalState(browser, testInfo, teacherEnd(lan, '/gv/lop-hoc', 's3e1-class-card'));
});

for (const [title, name, year, field, msg] of [
  ['Tên lớp 61 ký tự @TC-S3E1-07', 'A'.repeat(61), '2026–2027', 'name', MSG.s04],
  ['Năm học sai quy tắc @TC-S3E1-08', '11B4', '2026–2028', 'academicYear', 'Năm học dạng 2026–2027, năm sau bằng năm trước cộng 1.'],
] as const) {
  test(title, async ({ request, browser }, testInfo) => {
    const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
    const lan = w.people['Cô Lan']!;
    const r = await teacherApi.create(request, lan.token, { name, grade: 11, academicYear: year });
    expect(r.status()).toBe(400);
    const e = await err(r);
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.details![0]!.field).toBe(field);
    expect(JSON.stringify(e)).toContain(msg);
    const list = (await (await teacherApi.list(request, lan.token)).json()) as { items: unknown[] };
    expect(list.items).toHaveLength(0);
    await captureFinalState(browser, testInfo, teacherEnd(lan, '/gv/lop-hoc', 's3e1-create-name'));
  });
}

test('Chặn quá 20 lớp Đang dạy @TC-S3E1-10 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
  const lan = w.people['Cô Lan']!;
  const t = lan.token;
  let first = '';
  for (let i = 1; i <= 20; i++) {
    const r = await teacherApi.create(request, t, { name: `Lớp ${i}`, grade: 10, academicYear: '2026–2027' });
    expect(r.status()).toBe(201);
    if (i === 1) first = ((await r.json()) as { id: string }).id;
  }
  const over = await teacherApi.create(request, t, { name: 'Lớp 21', grade: 10, academicYear: '2026–2027' });
  expect(over.status()).toBe(400);
  expect(((await over.json()) as { details: Array<{ message: string }> }).details[0]!.message).toContain('20 lớp');
  expect((await teacherApi.archive(request, t, first)).status()).toBe(200);
  const again = await teacherApi.create(request, t, { name: 'Lớp 21', grade: 10, academicYear: '2026–2027' });
  expect(again.status()).toBe(201);
  await captureFinalState(browser, testInfo, teacherEnd(lan, '/gv/lop-hoc', 's3e1-create-name'));
});

test('Mã gõ chữ thường và có khoảng trắng vẫn được nhận @TC-S3E1-13 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const bao = w.people['Gia Bảo']!;
  const r = await join(request, bao.token, `  ${w.classes['11A1']!.code.toLowerCase()} `);
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { status: string }).status).toBe('pending');
  const mine = (await (await myClasses(request, bao.token)).json()) as { items: Array<{ className: string; status: string }> };
  expect(mine.items.find((x) => x.className === '11A1')?.status).toBe('pending');
  await captureFinalState(browser, testInfo, studentEnd(bao, 's3e1-join-waiting'));
});

test('Mọi lớp bắt buộc giáo viên duyệt: mã đúng luôn vào Chờ duyệt @TC-S3E1-15 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const bao = w.people['Gia Bảo']!;
  const r = await join(request, bao.token, w.classes['11A2']!.code);
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { status: string }).status).toBe('pending');
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A2']!.id)).json()) as { activeCount: number; pendingCount: number };
  expect(d).toMatchObject({ activeCount: 1, pendingCount: 1 });
  await captureFinalState(browser, testInfo, studentEnd(bao, 's3e1-join-waiting'));
});

test('Em đã rời xin vào lại lớp @TC-S3E1-16 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const tu = w.people['Văn Tú']!;
  const r = await join(request, tu.token, w.classes['11A1']!.code);
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { status: string }).status).toBe('pending');
  const p = await (await teacherApi.pending(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).text();
  expect(p).toContain(tu.userCode);
  await captureFinalState(browser, testInfo, studentEnd(tu, 's3e1-join-waiting'));
});

for (const [tc, title] of [
  ['18', 'Mã đã khóa (công tắc tắt) @TC-S3E1-18'],
  ['19', 'Mã cũ sau khi cô đặt lại mã @TC-S3E1-19'],
] as const) {
  test(title, async ({ request, browser }, testInfo) => {
    const w = await world(request);
    const lan = w.people['Cô Lan']!.token;
    const bao = w.people['Gia Bảo']!;
    let code = '';
    if (tc === '18') {
      expect((await teacherApi.settings(request, lan, w.classes['11A2']!.id, { codeJoinEnabled: false })).status()).toBe(200);
      code = w.classes['11A2']!.code;
    } else {
      code = w.classes['11A1']!.code;
      expect((await teacherApi.resetCode(request, lan, w.classes['11A1']!.id)).status()).toBe(200);
    }
    const shut = await join(request, bao.token, code);
    expect(shut.status()).toBe(400);
    const e = await err(shut);
    expect(e.message).toBe(MSG.s01);
    expect(e.message).not.toContain('11A'); // không lộ tên lớp
    expect(((await (await myClasses(request, bao.token)).json()) as { items: unknown[] }).items).toHaveLength(0);
    if (tc === '19') {
      const fresh = ((await (await teacherApi.detail(request, lan, w.classes['11A1']!.id)).json()) as { code: string }).code;
      expect((await join(request, bao.token, fresh)).status()).toBe(200);
    }
    await captureFinalState(browser, testInfo, studentEnd(bao, tc === '19' ? 's3e1-join-waiting' : 's3e1-join-code'));
  });
}

test('Em đã Đang học lớp này nhập lại mã @TC-S3E1-22 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  const r = await join(request, an.token, w.classes['11A1']!.code);
  expect(r.status()).toBe(409);
  expect((await err(r)).message).toBe(MSG.s03);
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).json()) as { activeCount: number };
  expect(d.activeCount).toBe(2);
  await captureFinalState(browser, testInfo, studentEnd(an, 's3e1-join-code'));
});

test('Từ chối một em @TC-S3E1-26 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const nam = w.people['Hoàng Nam']!;
  const c = w.classes['11A1']!;
  await assignSet(request, lan, c.id, 'Bộ bài tuần 15');
  expect((await teacherApi.decide(request, lan.token, c.id, nam.id, 'reject')).status()).toBe(200);
  expect(await (await teacherApi.pending(request, lan.token, c.id)).text()).not.toContain(nam.userCode);
  expect(await (await practiceSets(request, nam.token)).text()).not.toContain('Bộ bài tuần 15');
  const mine = (await (await myClasses(request, nam.token)).json()) as { items: Array<{ className: string; status: string }> };
  expect(mine.items.find((x) => x.className === '11A1')?.status).toBe('rejected');
  await captureFinalState(browser, testInfo, studentEnd(nam, 's3e1-join-rejected'));
});

test('Em đăng nhập bằng mật khẩu tạm và phải đặt mật khẩu mới @TC-S3E1-35 @loai-B', async ({ request, browser }, testInfo) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  const temp = ((await (await teacherApi.resetPassword(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, mai.id)).json()) as { tempPassword: string }).tempPassword;
  const login = await codeLogin(request, mai.userCode, temp);
  expect(login.status()).toBe(200);
  expect(((await login.json()) as { mustChangePassword: boolean }).mustChangePassword).toBe(true); // buộc đặt mật khẩu mới
  const change = await request.post(`${API}/api/v1/auth/first-password`, { data: { userCode: mai.userCode, password: temp, newPassword: 'MaiMoi#2026' } });
  expect(change.status(), await change.text()).toBe(200);
  expect((await codeLogin(request, mai.userCode, 'MaiMoi#2026')).status()).toBe(200);
  expect((await codeLogin(request, mai.userCode, temp)).status()).toBe(400);
  expect((await codeLogin(request, mai.userCode, PASSWORD)).status()).toBe(400);
  await captureFinalState(browser, testInfo, async (page) => {
    await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
    await page.goto('/hs/dang-nhap-ma');
    await expect(page.getByTestId('s2e1-login-code')).toBeVisible();
  });
});
