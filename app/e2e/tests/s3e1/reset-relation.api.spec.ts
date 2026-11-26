import { fixtureIds } from '../../support/fixtures.ts';
import { expect, test } from '../../support/fixtures.ts';
import {
  API,
  MSG,
  PASSWORD,
  TEMP_PASSWORD,
  assignSet,
  bearer,
  codeLogin,
  err,
  hook,
  practiceSets,
  teacherApi,
  world,
} from './s3e1.ts';

/** US-S3E1-04 (đặt lại mật khẩu) và US-S3E1-05 (quyền theo quan hệ), gọi thẳng máy chủ. */

test('Mật khẩu tạm không lấy lại được lần thứ hai @TC-S3E1-36', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const c = w.classes['11A1']!.id;
  const mai = w.people['Chi Mai']!;
  const r = await teacherApi.resetPassword(request, lan, c, mai.id);
  expect(r.status()).toBe(200);
  const temp = ((await r.json()) as { tempPassword: string }).tempPassword;
  expect(temp).toMatch(TEMP_PASSWORD);
  const compact = temp.replaceAll('-', '');
  for (const res of [
    await teacherApi.students(request, lan, c),
    await teacherApi.student(request, lan, c, mai.id),
    await teacherApi.detail(request, lan, c),
    await teacherApi.list(request, lan),
  ]) {
    const body = await res.text();
    expect(body).not.toContain(temp);
    expect(body).not.toContain(compact);
    expect(body).not.toMatch(/pbkdf2|passwordHash|password_hash/i);
  }
  const again = await teacherApi.resetPassword(request, lan, c, mai.id);
  const temp2 = ((await again.json()) as { tempPassword: string }).tempPassword;
  expect(temp2).not.toBe(temp);
  expect((await codeLogin(request, mai.userCode, temp)).status()).toBe(400); // mật khẩu tạm cũ hết hiệu lực
  expect((await codeLogin(request, mai.userCode, temp2)).status()).toBe(200);
});

test('Em thuộc hai lớp: cả hai giáo viên đều đặt lại được @TC-S3E1-37', async ({ request }) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  const r = await teacherApi.resetPassword(request, w.people['Thầy Khôi']!.token, w.classes['10B']!.id, mai.id);
  expect(r.status()).toBe(200);
  const temp = ((await r.json()) as { tempPassword: string }).tempPassword;
  const lanView = (await (await teacherApi.student(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, mai.id)).json()) as { status: string };
  expect(lanView.status).toBe('active');
  expect(JSON.stringify(lanView)).not.toContain(temp);
  expect((await teacherApi.resetPassword(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, mai.id)).status()).toBe(200);
});

test('Giáo viên không đặt lại được cho em ngoài lớp mình @TC-S3E1-38', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  for (const cls of [w.classes['10B']!.id, w.classes['11A1']!.id]) {
    const r = await teacherApi.resetPassword(request, w.people['Thầy Khôi']!.token, cls, an.id);
    expect(r.status()).toBe(404);
    expect(await r.text()).not.toMatch(/tempPassword/);
  }
  expect((await codeLogin(request, an.userCode, PASSWORD)).status()).toBe(200);
});

test('Em đã rời lớp thì hết quyền đặt lại @TC-S3E1-39', async ({ request }) => {
  const w = await world(request);
  const tu = w.people['Văn Tú']!;
  expect((await teacherApi.resetPassword(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, tu.id)).status()).toBe(404);
  expect((await codeLogin(request, tu.userCode, PASSWORD)).status()).toBe(200);
});

test('Em còn Chờ duyệt thì chưa đặt lại được @TC-S3E1-40', async ({ request }) => {
  const w = await world(request);
  const ha = w.people['Thu Hà']!;
  expect((await teacherApi.resetPassword(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, ha.id)).status()).toBe(404);
  expect((await codeLogin(request, ha.userCode, PASSWORD)).status()).toBe(200);
});

test('Học sinh không đặt lại được mật khẩu của bạn @TC-S3E1-41', async ({ request }) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  const r = await teacherApi.resetPassword(request, w.people['Bình An']!.token, w.classes['11A1']!.id, mai.id);
  expect(r.status()).toBe(403);
  expect((await codeLogin(request, mai.userCode, PASSWORD)).status()).toBe(200);
});

test('Không đặt lại mật khẩu tài khoản giáo viên @TC-S3E1-42', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  expect((await teacherApi.resetPassword(request, w.people['Thầy Khôi']!.token, w.classes['10B']!.id, lan.id)).status()).toBe(404);
  expect((await teacherApi.resetPassword(request, lan.token, w.classes['11A1']!.id, lan.id)).status()).toBe(404);
  expect((await codeLogin(request, lan.userCode, PASSWORD)).status()).toBe(200);
});

test('Mật khẩu tạm hết hạn đúng 24 giờ @TC-S3E1-43', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const c = w.classes['11A1']!.id;
  const mai = w.people['Chi Mai']!;
  const an = w.people['Bình An']!;
  const t1 = ((await (await teacherApi.resetPassword(request, lan, c, mai.id)).json()) as { tempPassword: string }).tempPassword;
  await hook(request, 's3e1/temp-password/age', { userId: mai.id, seconds: 23 * 3600 + 59 * 60 });
  const ok = await codeLogin(request, mai.userCode, t1);
  expect(ok.status()).toBe(200);
  const okBody = (await ok.json()) as { mustChangePassword: boolean; token?: string | null };
  expect(okBody.mustChangePassword).toBe(true);
  expect(okBody.token ?? null).toBeNull();
  const t2 = ((await (await teacherApi.resetPassword(request, lan, c, an.id)).json()) as { tempPassword: string }).tempPassword;
  await hook(request, 's3e1/temp-password/age', { userId: an.id, seconds: 24 * 3600 + 60 });
  const late = await codeLogin(request, an.userCode, t2);
  expect(late.status()).toBe(400);
  expect((await err(late)).message).toBe(MSG.s05);
  expect(await late.text()).not.toContain('token');
});

test('Đặt lại quá 10 lần trong 1 giờ @TC-S3E1-44', async ({ request }) => {
  const w = await world(request, {
    teachers: ['Cô Lan'],
    classes: [{ key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['An 1', 'An 2', 'An 3', 'An 4', 'An 5', 'An 6'] }],
  });
  const lan = w.people['Cô Lan']!.token;
  const c = w.classes['11A1']!.id;
  const kids = ['An 1', 'An 2', 'An 3', 'An 4', 'An 5', 'An 6'].map((k) => w.people[k]!);
  for (let i = 0; i < 10; i++) expect((await teacherApi.resetPassword(request, lan, c, kids[i % 6]!.id)).status()).toBe(200);
  const r = await teacherApi.resetPassword(request, lan, c, kids[0]!.id);
  expect(r.status()).toBe(429);
  expect(r.headers()['retry-after']).toBeTruthy();
  expect((await err(r)).message).toBe(MSG.s08);
  expect(await r.text()).not.toContain('tempPassword');
  await hook(request, 's3e1/password-resets/age', { userId: w.people['Cô Lan']!.id, seconds: 3601 });
  expect((await teacherApi.resetPassword(request, lan, c, kids[0]!.id)).status()).toBe(200);
});

test('Giáo viên chỉ thấy lớp của mình @TC-S3E1-46', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const list = (await (await teacherApi.list(request, lan)).json()) as { items: Array<{ name: string }> };
  expect(list.items.map((c) => c.name).sort()).toEqual(['11A1', '11A2']);
  const all = (await Promise.all(['11A1', '11A2'].map(async (k) => (await teacherApi.students(request, lan, w.classes[k]!.id)).text()))).join();
  expect(all).not.toContain(w.people['Mi Na']!.userCode);
});

test('Học sinh Đang học chỉ thấy bài của lớp mình @TC-S3E1-47', async ({ request }) => {
  const w = await world(request);
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  await assignSet(request, w.people['Cô Lan']!, w.classes['11A2']!.id, 'Bộ tuần 15 của 11A2');
  const r = await practiceSets(request, w.people['Bình An']!.token);
  expect(r.status()).toBe(200);
  const body = await r.text();
  expect(body).toContain('Bộ bài tuần 15');
  expect(body).not.toContain('Bộ tuần 15 của 11A2');
  expect(body).not.toContain(w.people['Chi Mai']!.userCode);
  expect(body).not.toContain('Chi Mai');
});

test('Đổi mã lớp trên đường dẫn @TC-S3E1-48', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const b = w.classes['10B']!.id;
  const mi = w.people['Mi Na']!.id;
  const rs = [
    await teacherApi.detail(request, lan, b),
    await teacherApi.students(request, lan, b),
    await teacherApi.pending(request, lan, b),
    await teacherApi.resetPassword(request, lan, b, mi),
    await teacherApi.settings(request, lan, b, { aiEnabled: false }),
    await teacherApi.resetCode(request, lan, b),
  ];
  for (const r of rs) {
    expect(r.status()).toBe(404);
    const t = await r.text();
    expect(t).not.toContain('10B');
    expect(t).not.toContain('Khôi');
  }
  const d = (await (await teacherApi.detail(request, w.people['Thầy Khôi']!.token, b)).json()) as { aiEnabled: boolean; code: string };
  expect(d).toMatchObject({ aiEnabled: true, code: w.classes['10B']!.code });
});

test('Đổi mã học sinh trên đường dẫn @TC-S3E1-49', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const mi = w.people['Mi Na']!;
  for (const r of [
    await teacherApi.student(request, lan, w.classes['11A1']!.id, mi.id),
    await teacherApi.resetPassword(request, lan, w.classes['11A1']!.id, mi.id),
    await teacherApi.decide(request, lan, w.classes['11A1']!.id, mi.id, 'remove'),
  ]) {
    expect(r.status()).toBe(404);
    const t = await r.text();
    expect(t).not.toContain('Mi Na');
    expect(t).not.toContain(mi.userCode);
  }
});

test('Học sinh gọi API của giáo viên @TC-S3E1-50', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!.token;
  const c = w.classes['11A1']!.id;
  const rs = [
    await teacherApi.list(request, an),
    await teacherApi.pending(request, an, c),
    await teacherApi.students(request, an, c),
    await teacherApi.settings(request, an, c, { aiEnabled: false }),
    await teacherApi.resetPassword(request, an, c, w.people['Chi Mai']!.id),
    await teacherApi.decide(request, an, c, w.people['Thu Hà']!.id, 'approve'),
    await request.post(`${API}/api/v1/teacher/practice-sets`, { headers: bearer(an), data: { title: 'x', items: [] } }),
  ];
  for (const r of rs) expect(r.status()).toBe(403);
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, c)).json()) as { aiEnabled: boolean; pendingCount: number };
  expect(d).toMatchObject({ aiEnabled: true, pendingCount: 2 });
});

test('Học sinh Chờ duyệt, Đã rời hoặc Bị từ chối không thấy bài lớp @TC-S3E1-51', async ({ request }) => {
  const w = await world(request, {
    teachers: ['Cô Lan'],
    classes: [{ key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An'], pending: ['Hoàng Nam'], left: ['Văn Tú'], rejected: ['Lan Chi'] }],
  });
  const set = await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  for (const k of ['Hoàng Nam', 'Văn Tú', 'Lan Chi']) {
    const t = w.people[k]!.token;
    expect(await (await practiceSets(request, t)).text()).not.toContain('Bộ bài tuần 15');
    expect((await request.get(`${API}/api/v1/student/practice-sets/${set.id}`, { headers: bearer(t) })).status()).toBe(404);
    expect((await request.get(`${API}/api/v1/student/assignments/${set.items[0]!.assignmentId}`, { headers: bearer(t) })).status()).toBe(404);
  }
  expect(await (await practiceSets(request, w.people['Bình An']!.token)).text()).toContain('Bộ bài tuần 15');
});

test('Con trỏ phân trang của lớp khác @TC-S3E1-52', async ({ request }) => {
  const w = await world(request, {
    teachers: ['Cô Lan', 'Thầy Khôi'],
    classes: [
      { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['An', 'Bảo', 'Chi'] },
      { key: '10B', teacher: 'Thầy Khôi', name: '10B', active: ['Mi Na', 'Phương Thảo', 'Quân'] },
    ],
  });
  const p = (await (await teacherApi.students(request, w.people['Thầy Khôi']!.token, w.classes['10B']!.id, '?limit=1')).json()) as { nextCursor: string };
  expect(p.nextCursor).toBeTruthy();
  const r = await teacherApi.students(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, `?limit=1&cursor=${encodeURIComponent(p.nextCursor)}`);
  expect(r.status()).toBe(400);
  expect((await err(r)).code).toBe('INVALID_CURSOR');
  const t = await r.text();
  expect(t).not.toContain('Mi Na');
  expect(t).not.toContain('Phương Thảo');
});

test('Dữ liệu mẫu chung F-S3: cô Lan có đúng 11A1 HK-4P7Q và 11A2 HK-8M3C @TC-S3E1-02', async ({ request }) => {
  const lanFx = (fixtureIds.users as Record<string, { userCode?: string; password?: string }>)['F-S3-001']!;
  const r = await codeLogin(request, lanFx.userCode!, lanFx.password!);
  const token = ((await r.json()) as { token: string }).token;
  const list = (await (await teacherApi.list(request, token)).json()) as { items: Array<{ name: string; code: string; academicYear: string }> };
  expect(list.items.map((c) => `${c.name} ${c.code}`).sort()).toEqual(['11A1 HK-4P7Q', '11A2 HK-8M3C']);
  expect(list.items.every((c) => c.academicYear === '2026–2027')).toBe(true);
});
