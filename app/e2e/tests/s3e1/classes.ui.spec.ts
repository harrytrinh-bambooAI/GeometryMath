import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import {
  CLASS_CODE,
  MSG,
  PASSWORD,
  TEMP_PASSWORD,
  assignSet,
  codeLogin,
  join,
  practiceSets,
  signIn,
  teacherApi,
  world,
  type World,
} from './s3e1.ts';

/** US-S3E1-01…04 trên trình duyệt: màn Lớp học, một lớp, trang in, Lớp của em. */

async function openClass(page: Page, w: World, key = '11A1', teacher = 'Cô Lan') {
  await signIn(page, w.people[teacher]!);
  await page.goto(`/gv/lop-hoc/${w.classes[key]!.id}`);
  await expect(page.getByTestId('s3e1-class-name')).toHaveText(w.classes[key]!.name);
}

async function createClass(page: Page, name: string, grade: string, year: string) {
  await page.getByTestId('s3e1-create-name').fill(name);
  await page.getByTestId('s3e1-create-grade').selectOption({ label: grade });
  await page.getByTestId('s3e1-create-year').fill(year);
  await page.getByTestId('s3e1-create-submit').click();
}

test('Tạo lớp hợp lệ, nhận mã lớp và trang in @TC-S3E1-01', async ({ page, request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
  const api = await teacherApi.create(request, w.people['Cô Lan']!.token, { name: 'Lớp API', grade: 11, academicYear: '2026–2027' });
  expect(Object.keys((await api.json()) as object).sort()).toEqual(['academicYear', 'code', 'grade', 'id', 'name', 'status']);
  await signIn(page, w.people['Cô Lan']!);
  await page.goto('/gv/lop-hoc');
  await createClass(page, '11B3', 'Lớp 11', '2026–2027');
  const card = page.getByTestId('s3e1-class-card').filter({ hasText: '11B3' });
  await expect(card.getByTestId('s3e1-class-card-status')).toHaveText('Đang dạy');
  await expect(card.getByTestId('s3e1-class-card-count')).toHaveText('0');
  await expect(card.getByTestId('s3e1-class-card-code')).toHaveText(CLASS_CODE);
  const code = await card.getByTestId('s3e1-class-card-code').innerText();
  await card.click();
  await page.getByTestId('s3e1-print').click();
  await expect(page.getByTestId('s3e1-print-page-name')).toHaveText('Lớp 11B3');
  await expect(page.getByTestId('s3e1-print-page-qr')).toBeVisible();
  await expect(page.getByTestId('s3e1-print-page-code')).toHaveText(code);
  await expect(page.getByTestId('s3e1-print-page-steps').locator('li')).toHaveCount(4);
});

test('Tên lớp đúng 60 ký tự được nhận @TC-S3E1-03', async ({ page, request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
  await signIn(page, w.people['Cô Lan']!);
  await page.goto('/gv/lop-hoc');
  await createClass(page, 'A'.repeat(60), 'Lớp 10', '2026–2027');
  await expect(page.getByTestId('s3e1-class-card-name')).toHaveText('A'.repeat(60));
});

test('Đặt lại mã lớp: mã cũ hết hiệu lực, thành viên giữ nguyên @TC-S3E1-04', async ({ page, request }) => {
  const w = await world(request);
  const old = w.classes['11A1']!.code;
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  await openClass(page, w);
  await page.getByTestId('s3e1-code-reset').click();
  await page.getByTestId('s3e1-dialog-ok').click();
  await expect(page.getByTestId('s3e1-class-code')).not.toHaveText(old);
  await expect(page.getByTestId('s3e1-class-code')).toHaveText(CLASS_CODE);
  const fresh = await page.getByTestId('s3e1-class-code').innerText();
  expect((await join(request, w.people['Gia Bảo']!.token, old)).status()).toBe(400);
  expect(await (await practiceSets(request, w.people['Bình An']!.token)).text()).toContain('Bộ bài tuần 15');
  await expect(page.getByTestId('s3e1-students-count')).toHaveText('2');
  await page.getByTestId('s3e1-print').click();
  await expect(page.getByTestId('s3e1-print-page-code')).toHaveText(fresh);
});

test('Tắt "Cho vào lớp bằng mã" thì mã bị khóa, bật lại thì dùng được @TC-S3E1-05', async ({ page, request }) => {
  const w = await world(request);
  const bao = w.people['Gia Bảo']!;
  await openClass(page, w, '11A2');
  // công tắc đổi ngay trên màn (lạc quan); chờ máy chủ lưu xong rồi mới thử mã (dưới tải, PUT có thể về sau lệnh join)
  const saved = () => page.waitForResponse((r) => r.request().method() === 'PUT' && /\/settings$/.test(new URL(r.url()).pathname));
  await Promise.all([saved(), page.getByTestId('s3e1-switch-code-join').uncheck()]);
  await expect(page.getByTestId('s3e1-switch-code-join')).not.toBeChecked();
  const shut = await join(request, bao.token, w.classes['11A2']!.code);
  expect(shut.status()).toBe(400);
  expect(((await shut.json()) as { message: string }).message).toBe(MSG.s01);
  await Promise.all([saved(), page.getByTestId('s3e1-switch-code-join').check()]);
  await expect(page.getByTestId('s3e1-switch-code-join')).toBeChecked();
  expect((await join(request, bao.token, w.classes['11A2']!.code)).status()).toBe(200);
  await page.reload();
  await expect(page.getByTestId('s3e1-pending-count')).toHaveText('1');
});

for (const [title, name, year, field, msg] of [
  ['Tên lớp rỗng @TC-S3E1-06', '', '2026–2027', 'name', MSG.s04],
  ['Tên lớp 61 ký tự @TC-S3E1-07', 'A'.repeat(61), '2026–2027', 'name', MSG.s04],
  ['Năm học sai quy tắc @TC-S3E1-08', '11B4', '2026–2028', 'year', 'Năm học dạng 2026–2027, năm sau bằng năm trước cộng 1.'],
] as const) {
  test(title, async ({ page, request }) => {
    const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
    const direct = await teacherApi.create(request, w.people['Cô Lan']!.token, { name, grade: 11, academicYear: year });
    expect(direct.status()).toBe(400);
    const e = (await direct.json()) as { code: string; details: Array<{ field: string }> };
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.details[0]!.field).toBe(field === 'name' ? 'name' : 'academicYear');
    await signIn(page, w.people['Cô Lan']!);
    await page.goto('/gv/lop-hoc');
    await createClass(page, name, 'Lớp 11', year);
    await expect(page.getByTestId(`s3e1-create-${field}-error`)).toHaveText(msg);
    await expect(page.getByTestId('s3e1-class-card')).toHaveCount(0);
  });
}

test('Chặn quá 20 lớp Đang dạy @TC-S3E1-10', async ({ page, request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], classes: [] });
  const t = w.people['Cô Lan']!.token;
  let first = '';
  for (let i = 1; i <= 20; i++) {
    const r = await teacherApi.create(request, t, { name: `Lớp ${i}`, grade: 10, academicYear: '2026–2027' });
    expect(r.status()).toBe(201);
    if (i === 1) first = ((await r.json()) as { id: string }).id;
  }
  const over = await teacherApi.create(request, t, { name: 'Lớp 21', grade: 10, academicYear: '2026–2027' });
  expect(over.status()).toBe(400);
  expect(((await over.json()) as { details: Array<{ message: string }> }).details[0]!.message).toContain('20 lớp');
  await signIn(page, w.people['Cô Lan']!);
  await page.goto('/gv/lop-hoc');
  await createClass(page, 'Lớp 21', 'Lớp 10', '2026–2027');
  await expect(page.getByTestId('s3e1-create-error')).toContainText('20 lớp');
  expect((await teacherApi.archive(request, t, first)).status()).toBe(200);
  await page.getByTestId('s3e1-create-submit').click();
  await expect(page.getByTestId('s3e1-class-card').filter({ hasText: 'Lớp 21' })).toHaveCount(1);
});

async function joinUI(page: Page, w: World, who: string, code: string) {
  await signIn(page, w.people[who]!);
  await page.goto('/hs/lop-cua-em');
  await page.getByTestId('s3e1-join-code').fill(code);
  await page.getByTestId('s3e1-join-submit').click();
}

test('Nhập mã đúng, vào trạng thái Chờ duyệt @TC-S3E1-12', async ({ page, request }) => {
  const w = await world(request);
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  await joinUI(page, w, 'Gia Bảo', w.classes['11A1']!.code);
  await expect(page.getByTestId('s3e1-join-waiting')).toContainText(MSG.s02);
  const p = (await (await teacherApi.pending(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).json()) as { items: unknown[] };
  expect(p.items).toHaveLength(3);
  expect(await (await practiceSets(request, w.people['Gia Bảo']!.token)).text()).not.toContain('Bộ bài tuần 15');
  await page.goto('/hs/tu-hoc');
  await expect(page).toHaveURL(/\/hs\/tu-hoc/);
});

test('Mã gõ chữ thường và có khoảng trắng vẫn được nhận @TC-S3E1-13', async ({ page, request }) => {
  const w = await world(request);
  await joinUI(page, w, 'Gia Bảo', `  ${w.classes['11A1']!.code.toLowerCase()} `);
  await expect(page.getByTestId('s3e1-join-waiting')).toContainText('11A1');
});

test('Mọi lớp bắt buộc giáo viên duyệt: mã đúng luôn vào Chờ duyệt @TC-S3E1-15', async ({ page, request }) => {
  const w = await world(request);
  await joinUI(page, w, 'Gia Bảo', w.classes['11A2']!.code);
  await expect(page.getByTestId('s3e1-join-waiting')).toContainText(MSG.s02);
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A2']!.id)).json()) as { activeCount: number; pendingCount: number };
  expect(d).toMatchObject({ activeCount: 1, pendingCount: 1 });
});

test('Em đã rời xin vào lại lớp @TC-S3E1-16', async ({ page, request }) => {
  const w = await world(request);
  await joinUI(page, w, 'Văn Tú', w.classes['11A1']!.code);
  await expect(page.getByTestId('s3e1-join-waiting')).toContainText(MSG.s02);
  const p = await (await teacherApi.pending(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).text();
  expect(p).toContain(w.people['Văn Tú']!.userCode);
});

for (const [tc, title] of [
  ['17', 'Mã sai @TC-S3E1-17'],
  ['18', 'Mã đã khóa (công tắc tắt) @TC-S3E1-18'],
  ['19', 'Mã cũ sau khi cô đặt lại mã @TC-S3E1-19'],
] as const) {
  test(title, async ({ page, request }) => {
    const w = await world(request);
    const lan = w.people['Cô Lan']!.token;
    let code = 'HK-ZZZZ';
    if (tc === '18') {
      await teacherApi.settings(request, lan, w.classes['11A2']!.id, { codeJoinEnabled: false });
      code = w.classes['11A2']!.code;
    }
    if (tc === '19') {
      code = w.classes['11A1']!.code;
      await teacherApi.resetCode(request, lan, w.classes['11A1']!.id);
    }
    await joinUI(page, w, 'Gia Bảo', code);
    await expect(page.getByTestId('s3e1-join-error')).toHaveText(MSG.s01);
    await expect(page.getByTestId('s3e1-join-error')).not.toContainText('11A');
    await expect(page.getByTestId('s3e1-join-waiting')).toHaveCount(0);
    if (tc === '19') {
      const fresh = ((await (await teacherApi.detail(request, lan, w.classes['11A1']!.id)).json()) as { code: string }).code;
      expect((await join(request, w.people['Gia Bảo']!.token, fresh)).status()).toBe(200);
    }
  });
}

test('Em đã Đang học lớp này nhập lại mã @TC-S3E1-22', async ({ page, request }) => {
  const w = await world(request);
  await joinUI(page, w, 'Bình An', w.classes['11A1']!.code);
  await expect(page.getByTestId('s3e1-join-error')).toHaveText(MSG.s03);
  expect((await join(request, w.people['Bình An']!.token, w.classes['11A1']!.code)).status()).toBe(409);
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).json()) as { activeCount: number };
  expect(d.activeCount).toBe(2);
});

test('Duyệt một em, em thấy bài của lớp @TC-S3E1-24', async ({ page, request }) => {
  const w = await world(request);
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  await openClass(page, w);
  await expect(page.getByTestId('s3e1-pending-count')).toHaveText('2');
  await page.getByTestId('s3e1-pending-row').filter({ hasText: 'Thu Hà' }).getByTestId('s3e1-approve').click();
  await expect(page.getByTestId('s3e1-pending-count')).toHaveText('1');
  await expect(page.getByTestId('s3e1-students-count')).toHaveText('3');
  await expect(page.getByTestId('s3e1-student-row').filter({ hasText: 'Thu Hà' })).toHaveCount(1);
  expect(await (await practiceSets(request, w.people['Thu Hà']!.token)).text()).toContain('Bộ bài tuần 15');
});

test('Hai em trùng tên được phân biệt bằng mã người dùng @TC-S3E1-25', async ({ page, request }) => {
  const w = await world(request, {
    teachers: ['Cô Lan'],
    classes: [{ key: '11A1', teacher: 'Cô Lan', name: '11A1', pending: ['Hoàng Nam', 'Hoàng Nam#2'] }],
  });
  const second = w.people['Hoàng Nam#2']!;
  const first = w.people['Hoàng Nam']!;
  await openClass(page, w);
  await expect(page.getByTestId('s3e1-pending-row')).toHaveCount(2);
  await expect(page.getByTestId('s3e1-pending-row').filter({ hasText: first.userCode })).toHaveCount(1);
  await page.locator(`[data-testid="s3e1-pending-row"][data-code="${second.userCode}"]`).getByTestId('s3e1-approve').click();
  await expect(page.locator(`[data-testid="s3e1-student-row"][data-code="${second.userCode}"]`)).toHaveCount(1);
  await expect(page.locator(`[data-testid="s3e1-pending-row"][data-code="${first.userCode}"]`)).toHaveCount(1);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  // em đầu cũng được duyệt: hai em cùng tên Đang học cùng lớp
  expect((await teacherApi.decide(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, first.id, 'approve')).status()).toBe(200);
});

test('Từ chối một em @TC-S3E1-26', async ({ page, request }) => {
  const w = await world(request);
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  await openClass(page, w);
  await page.getByTestId('s3e1-pending-row').filter({ hasText: 'Hoàng Nam' }).getByTestId('s3e1-reject').click();
  await expect(page.getByTestId('s3e1-pending-row').filter({ hasText: 'Hoàng Nam' })).toHaveCount(0);
  expect(await (await practiceSets(request, w.people['Hoàng Nam']!.token)).text()).not.toContain('Bộ bài tuần 15');
  const p2 = await page.context().newPage();
  await signIn(p2, w.people['Hoàng Nam']!);
  await p2.goto('/hs/lop-cua-em');
  await expect(p2.getByTestId('s3e1-join-rejected')).toContainText('hỏi lại thầy cô');
});

test('Cho em rời lớp, dữ liệu cũ vẫn ở Kết quả lớp @TC-S3E1-27', async ({ page, request }) => {
  const w = await world(request);
  await openClass(page, w);
  await page.getByTestId('s3e1-student-row').filter({ hasText: 'Bình An' }).getByTestId('s3e1-remove').click();
  await page.getByTestId('s3e1-dialog-ok').click();
  await expect(page.getByTestId('s3e1-student-left').filter({ hasText: 'Bình An' })).toContainText('đã rời');
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ mới sau khi rời');
  expect(await (await practiceSets(request, w.people['Bình An']!.token)).text()).not.toContain('Bộ mới sau khi rời');
});

test('Hai tab cùng duyệt một em @TC-S3E1-30', async ({ page, request }) => {
  const w = await world(request);
  await openClass(page, w);
  const tab2 = await page.context().newPage();
  await signIn(tab2, w.people['Cô Lan']!);
  await tab2.goto(`/gv/lop-hoc/${w.classes['11A1']!.id}`);
  await expect(tab2.getByTestId('s3e1-pending-row').filter({ hasText: 'Thu Hà' })).toHaveCount(1);
  await page.getByTestId('s3e1-pending-row').filter({ hasText: 'Thu Hà' }).getByTestId('s3e1-approve').click();
  await expect(page.getByTestId('s3e1-student-row').filter({ hasText: 'Thu Hà' })).toHaveCount(1);
  await tab2.getByTestId('s3e1-pending-row').filter({ hasText: 'Thu Hà' }).getByTestId('s3e1-reject').click();
  await expect(tab2.getByTestId('s3e1-toast')).toBeVisible();
  await expect(tab2.getByTestId('s3e1-pending-row').filter({ hasText: 'Thu Hà' })).toHaveCount(0);
  const s = await (await teacherApi.student(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, w.people['Thu Hà']!.id)).json();
  expect((s as { status: string }).status).toBe('active');
});

test('Đặt lại mật khẩu, mật khẩu tạm hiện một lần @TC-S3E1-34', async ({ page, request }) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  await openClass(page, w);
  await page.getByTestId('s3e1-student-row').filter({ hasText: 'Chi Mai' }).getByTestId('s3e1-reset-password').click();
  await expect(page.getByTestId('s3e1-dialog')).toContainText('Đặt lại mật khẩu cho Chi Mai?');
  await page.getByTestId('s3e1-dialog-ok').click();
  await expect(page.getByTestId('s3e1-temp-password')).toHaveText(TEMP_PASSWORD);
  await expect(page.getByTestId('s3e1-temp')).toContainText('Chỉ hiện một lần');
  expect((await codeLogin(request, mai.userCode, PASSWORD)).status()).toBe(400);
  const after = await join(request, mai.token, 'HK-ZZZZ'); // phiên cũ bị hủy ở yêu cầu kế tiếp
  expect(after.status()).toBe(401);
  await page.getByTestId('s3e1-temp-close').click();
  await expect(page.getByTestId('s3e1-temp')).toHaveCount(0);
});

test('Em đăng nhập bằng mật khẩu tạm và phải đặt mật khẩu mới @TC-S3E1-35', async ({ page, request }) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  const temp = ((await (await teacherApi.resetPassword(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, mai.id)).json()) as { tempPassword: string }).tempPassword;
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  await page.goto('/hs/dang-nhap-ma');
  await page.getByTestId('s2e1-login-code').fill(mai.userCode);
  await page.getByTestId('s2e1-login-password').fill(temp);
  await page.getByTestId('s2e1-login-submit').click();
  await expect(page.getByTestId('s2e1-first-password')).toBeVisible();
  await page.getByTestId('s2e1-first-password-input').fill('MaiMoi#2026');
  await page.getByTestId('s2e1-first-password-submit').click();
  await expect(page).toHaveURL(/\/hs\/bai-cua-em/);
  expect((await codeLogin(request, mai.userCode, 'MaiMoi#2026')).status()).toBe(200);
  expect((await codeLogin(request, mai.userCode, temp)).status()).toBe(400);
});

test('Hủy ở hộp xác nhận thì không đổi gì @TC-S3E1-45', async ({ page, request }) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  await openClass(page, w);
  await page.getByTestId('s3e1-student-row').filter({ hasText: 'Chi Mai' }).getByTestId('s3e1-reset-password').click();
  await page.getByTestId('s3e1-dialog-cancel').click();
  await expect(page.getByTestId('s3e1-dialog')).toHaveCount(0);
  expect((await codeLogin(request, mai.userCode, PASSWORD)).status()).toBe(200);
  expect((await practiceSets(request, mai.token)).status()).toBe(200); // phiên không bị đăng xuất
});
