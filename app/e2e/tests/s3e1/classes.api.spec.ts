import { expect, test } from '../../support/fixtures.ts';
import { MSG, err, hook, join, myClasses, teacherApi, world } from './s3e1.ts';

/** US-S3E1-01, 02, 03 — lớp, vào lớp bằng mã, duyệt (gọi thẳng máy chủ). */

test('Học sinh không tạo được lớp @TC-S3E1-09', async ({ request }) => {
  const w = await world(request);
  const r = await teacherApi.create(request, w.people['Bình An']!.token, { name: 'Lớp lậu', grade: 11, academicYear: '2026–2027' });
  expect(r.status()).toBe(403);
  expect((await err(r)).code).toBe('FORBIDDEN');
  const lan = await teacherApi.list(request, w.people['Cô Lan']!.token);
  expect(JSON.stringify(await lan.json())).not.toContain('Lớp lậu');
});

test('Bấm gửi hai lần không tạo hai yêu cầu @TC-S3E1-14', async ({ request }) => {
  const w = await world(request);
  const nam = w.people['Hoàng Nam']!;
  const c = w.classes['11A1']!;
  for (let i = 0; i < 2; i++) {
    const r = await join(request, nam.token, c.code);
    expect(r.status()).toBe(200);
    expect(((await r.json()) as { message: string }).message).toBe(MSG.s02);
  }
  const p = (await (await teacherApi.pending(request, w.people['Cô Lan']!.token, c.id)).json()) as { items: Array<{ userCode: string }> };
  expect(p.items.filter((x) => x.userCode === nam.userCode)).toHaveLength(1);
});

test('Dò mã: quá 10 lần sai trong 15 phút theo tài khoản @TC-S3E1-20', async ({ request }) => {
  const w = await world(request);
  const ha = w.people['Gia Bảo']!;
  for (let i = 0; i < 10; i++) {
    const code = i % 2 === 0 ? 'HK-ZZZZ' : 'HK-ZZZY'; // đổi mã đích không reset bộ đếm
    expect((await join(request, ha.token, code)).status()).toBe(400);
  }
  const blocked = await join(request, ha.token, w.classes['11A1']!.code);
  expect(blocked.status()).toBe(429);
  expect(blocked.headers()['retry-after']).toBeTruthy();
  expect((await err(blocked)).message).toBe(MSG.s08);
  expect(((await (await myClasses(request, ha.token)).json()) as { items: unknown[] }).items).toHaveLength(0);
  await hook(request, 's3e1/join-attempts/age', { userId: ha.id, seconds: 16 * 60 });
  expect((await join(request, ha.token, w.classes['11A1']!.code)).status()).toBe(200);
});

test('Chín lần sai vẫn chưa bị khóa (biên dưới) @TC-S3E1-21', async ({ request }) => {
  const w = await world(request);
  const ha = w.people['Gia Bảo']!;
  for (let i = 0; i < 9; i++) expect((await join(request, ha.token, 'HK-ZZZZ')).status()).toBe(400);
  const r = await join(request, ha.token, w.classes['11A1']!.code);
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { status: string }).status).toBe('pending');
});

test('Dò mã chậm: đủ 30 lần sai trong 24 giờ thì khóa theo tổng lượt @TC-S3E1-55', async ({ request }) => {
  const w = await world(request);
  const ha = w.people['Gia Bảo']!;
  // 30 lần sai rải đều trong 24 giờ, mỗi 15 phút dưới 10 lần (mỗi lần cách nhau 48 phút, cũ nhất 23 giờ 32 phút)
  await hook(request, 's3e1/join-attempts/seed', { userId: ha.id, minutesAgo: Array.from({ length: 30 }, (_, i) => 20 + i * 48) });
  const r = await join(request, ha.token, w.classes['11A1']!.code);
  expect(r.status()).toBe(429);
  expect((await err(r)).message).toBe(MSG.s08);
  const other = await join(request, ha.token, w.classes['11A2']!.code); // đổi mã đích không reset
  expect(other.status()).toBe(429);
  await hook(request, 's3e1/join-attempts/age', { userId: ha.id, seconds: 40 * 60 }); // lần cũ nhất ra khỏi 24 giờ
  expect((await join(request, ha.token, w.classes['11A1']!.code)).status()).toBe(200);
});

test('29 lần sai trong 24 giờ chưa bị khóa theo tổng lượt (biên dưới) @TC-S3E1-56', async ({ request }) => {
  const w = await world(request);
  const ha = w.people['Gia Bảo']!;
  await hook(request, 's3e1/join-attempts/seed', { userId: ha.id, minutesAgo: Array.from({ length: 29 }, (_, i) => 20 + i * 48) });
  const r = await join(request, ha.token, w.classes['11A1']!.code);
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { status: string }).status).toBe('pending');
});

test('Tài khoản giáo viên không nhập được mã lớp @TC-S3E1-23', async ({ request }) => {
  const w = await world(request);
  const r = await join(request, w.people['Cô Lan']!.token, w.classes['10B']!.code);
  expect(r.status()).toBe(403);
  const s = (await (await teacherApi.students(request, w.people['Thầy Khôi']!.token, w.classes['10B']!.id)).json()) as { items: Array<{ displayName: string }> };
  expect(s.items.map((x) => x.displayName)).not.toContain('Cô Lan');
});

test('Em thuộc hai lớp của hai giáo viên, mỗi giáo viên chỉ thấy lớp mình @TC-S3E1-28', async ({ request }) => {
  const w = await world(request);
  const mai = w.people['Chi Mai']!;
  const lan = (await (await teacherApi.students(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).json()) as { items: Array<{ userId: string }> };
  const khoi = (await (await teacherApi.students(request, w.people['Thầy Khôi']!.token, w.classes['10B']!.id)).json()) as { items: Array<{ userId: string }> };
  expect(lan.items.map((x) => x.userId)).toContain(mai.id);
  expect(khoi.items.map((x) => x.userId)).toContain(mai.id);
  const lanList = await (await teacherApi.list(request, w.people['Cô Lan']!.token)).text();
  expect(lanList).not.toContain('10B');
  expect((await teacherApi.student(request, w.people['Cô Lan']!.token, w.classes['10B']!.id, mai.id)).status()).toBe(404);
});

test('Giáo viên khác không duyệt được lớp của cô Lan @TC-S3E1-29', async ({ request }) => {
  const w = await world(request);
  const ha = w.people['Thu Hà']!;
  const r = await teacherApi.decide(request, w.people['Thầy Khôi']!.token, w.classes['11A1']!.id, ha.id, 'approve');
  expect(r.status()).toBe(404);
  const body = await r.text();
  expect(body).not.toContain('Thu Hà');
  expect(body).not.toContain(ha.userCode);
  expect(body).not.toContain('11A1');
  const p = await (await teacherApi.pending(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).text();
  expect(p).toContain(ha.userCode);
});

test('Duyệt lặp cùng quyết định @TC-S3E1-31', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!.token;
  const c = w.classes['11A1']!.id;
  const ha = w.people['Thu Hà']!;
  for (let i = 0; i < 3; i++) {
    const r = await teacherApi.decide(request, lan, c, ha.id, 'approve');
    expect(r.status()).toBe(200);
    expect(((await r.json()) as { status: string }).status).toBe('active');
  }
  const s = (await (await teacherApi.students(request, lan, c)).json()) as { items: Array<{ userId: string; status: string }> };
  expect(s.items.filter((x) => x.userId === ha.id && x.status === 'active')).toHaveLength(1);
  const reject = await teacherApi.decide(request, lan, c, ha.id, 'reject'); // quyết định khác: CONFLICT
  expect(reject.status()).toBe(409);
});

test('Học sinh không duyệt được @TC-S3E1-32', async ({ request }) => {
  const w = await world(request);
  const r = await teacherApi.decide(request, w.people['Bình An']!.token, w.classes['11A1']!.id, w.people['Thu Hà']!.id, 'approve');
  expect(r.status()).toBe(403);
  const p = await (await teacherApi.pending(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).text();
  expect(p).toContain(w.people['Thu Hà']!.userCode);
});

test('Duyệt em đã xóa tài khoản @TC-S3E1-33', async ({ request }) => {
  const w = await world(request);
  const nam = w.people['Hoàng Nam']!;
  await hook(request, 's3e1/deactivate', { userId: nam.id });
  const r = await teacherApi.decide(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id, nam.id, 'approve');
  expect(r.status()).toBe(404);
  const p = await (await teacherApi.pending(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).text();
  expect(p).not.toContain(nam.userCode);
  const s = await (await teacherApi.students(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).text();
  expect(s).not.toContain(nam.userCode);
});

test('Mọi lớp bắt buộc duyệt: API không nhận tham số tắt duyệt (phần API) @TC-S3E1-15', async ({ request }) => {
  const w = await world(request);
  const r = await teacherApi.settings(request, w.people['Cô Lan']!.token, w.classes['11A2']!.id, { approvalRequired: false });
  expect(r.status()).toBe(400);
  expect((await err(r)).code).toBe('VALIDATION_FAILED');
  const j = await join(request, w.people['Gia Bảo']!.token, w.classes['11A2']!.code);
  expect(((await j.json()) as { status: string }).status).toBe('pending');
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A2']!.id)).json()) as { activeCount: number; pendingCount: number; approvalRequired: boolean };
  expect(d).toMatchObject({ activeCount: 1, pendingCount: 1, approvalRequired: true });
});
