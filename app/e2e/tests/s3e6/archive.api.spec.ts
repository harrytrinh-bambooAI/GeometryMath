import { expect, test } from '../../support/fixtures.ts';
import { API, assignSet, bearer, codeLogin, err, teacherApi, world } from '../s3e1/s3e1.ts';
import { setRelease } from './s3e6.ts';

/** US-S3E6-02 qua API: lớp đã lưu trữ không nhận bộ mới, không đặt lại mật khẩu, không tính vào 20 lớp. */

test('Giao bộ mới cho lớp đã lưu trữ @TC-S3E6-19', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const c = w.classes['11A2']!;
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  const before = (await (await request.get(`${API}/api/v1/teacher/assignment-sets`, { headers: bearer(lan.token) })).text()).length;
  const d = await request.post(`${API}/api/v1/teacher/assignment-sets`, {
    headers: bearer(lan.token),
    data: { title: 'Bộ bài tuần 16', classId: c.id },
  });
  expect(d.status()).toBe(409);
  expect((await err(d)).code).toBe('CONFLICT');
  const notes = await request.get(`${API}/api/v1/notifications`, { headers: bearer(w.people['Quốc Bảo']!.token) });
  expect(await notes.text()).not.toContain('Bộ bài tuần 16');
  const after = (await (await request.get(`${API}/api/v1/teacher/assignment-sets`, { headers: bearer(lan.token) })).text()).length;
  expect(after).toBe(before);
});

test('Lưu trữ không tính vào giới hạn 20 lớp @TC-S3E6-23', async ({ request }) => {
  const w = await world(request, { teachers: ['Cô Lan'], students: [], classes: [] });
  const lan = w.people['Cô Lan']!;
  const ids: string[] = [];
  for (let i = 1; i <= 20; i++) {
    const r = await teacherApi.create(request, lan.token, { name: `Lớp ${i}`, grade: 10 });
    expect(r.status(), await r.text()).toBe(201);
    ids.push(((await r.json()) as { id: string }).id);
  }
  expect((await teacherApi.create(request, lan.token, { name: 'Lớp 21', grade: 10 })).status()).toBe(400);
  expect((await teacherApi.archive(request, lan.token, ids[0]!)).status()).toBe(200);
  const made = await teacherApi.create(request, lan.token, { name: 'Lớp 21', grade: 10 });
  expect(made.status()).toBe(201);
  const list = (await (await teacherApi.list(request, lan.token)).json()) as { items: Array<{ status: string }> };
  expect(list.items).toHaveLength(21);
  expect(list.items.filter((x) => x.status === 'archived')).toHaveLength(1);
  expect(list.items.filter((x) => x.status === 'active')).toHaveLength(20);
});

test('Lớp đã lưu trữ không đặt lại mật khẩu @TC-S3E6-24', async ({ request }) => {
  const w = await world(request, {
    teachers: ['Cô Lan'],
    students: [],
    classes: [{ key: '11A2', teacher: 'Cô Lan', name: '11A2', active: ['Quốc Bảo', 'Thảo Vy'] }],
  });
  const lan = w.people['Cô Lan']!;
  const vy = w.people['Thảo Vy']!;
  const c = w.classes['11A2']!;
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  const r = await teacherApi.resetPassword(request, lan.token, c.id, vy.id);
  expect(r.status()).toBe(404);
  expect((await err(r)).code).toBe('NOT_FOUND');
  // mật khẩu không đổi
  expect((await codeLogin(request, vy.userCode, vy.password)).status()).toBe(200);
});

test('Lịch mở lời giải của lớp đã lưu trữ vẫn chạy @TC-S3E6-25', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const bao = w.people['Quốc Bảo']!;
  const c = w.classes['11A2']!;
  const set = await assignSet(request, lan, c.id, 'Bộ bài tuần 15');
  const solution = () =>
    request.get(`${API}/api/v1/student/assignment-sets/${set.id}/items/1/solution`, { headers: bearer(bao.token) });
  expect((await teacherApi.archive(request, lan.token, c.id)).status()).toBe(200);
  // trước giờ mở: vẫn khóa
  await setRelease(request, set.id, 60);
  expect((await solution()).status()).not.toBe(200);
  // tới giờ mở: lời giải trả đầy đủ, chỉ đọc
  await setRelease(request, set.id, -1);
  const open = await solution();
  expect(open.status(), await open.text()).toBe(200);
});
