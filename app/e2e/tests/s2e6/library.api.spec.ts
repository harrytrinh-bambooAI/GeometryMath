import { expect, test } from '../../support/fixtures.ts';
import {
  API,
  K,
  MSG,
  accept,
  bearer,
  create,
  detail,
  errorCode,
  lib,
  list,
  ok,
  queue,
  reject,
  slot,
  submit,
  token,
  type CardPage,
} from './s2e6.ts';

/**
 * S2-E6 — API Kho đề (US-S2E6-01…03): phân trang theo con trỏ, quyền theo vai, gửi soát / duyệt / từ chối, riêng tư
 * của Của tôi, giới hạn thêm đề, lời giải Kho chung theo vai.
 */

test('@TC-S2E6-008 đúng 20 đề không có trang hai', async ({ request }, info) => {
  const s = slot(6, 8, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const p = await ok<CardPage>(await list(request, t, { shelf: 'mine' }));
  expect(p.items).toHaveLength(20);
  expect(p.nextCursor ?? null).toBeNull();
});

test('@TC-S2E6-009 21 đề có trang hai với đúng 1 đề, không trùng', async ({ request }, info) => {
  const s = slot(6, 9, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const p1 = await ok<CardPage>(await list(request, t, { shelf: 'mine' }));
  expect(p1.items).toHaveLength(20);
  expect(p1.nextCursor).toBeTruthy();
  const p2 = await ok<CardPage>(await list(request, t, { shelf: 'mine', cursor: p1.nextCursor! }));
  expect(p2.items).toHaveLength(1);
  expect(p2.nextCursor ?? null).toBeNull();
  expect(p1.items.map((c) => c.id)).not.toContain(p2.items[0]!.id);
  expect(p1.items[0]!.title).toBe('Đề số 21'); // mới nhất trước
});

test('@TC-S2E6-010 yêu cầu 50 đề nhận, 51 bị từ chối', async ({ request }, info) => {
  const s = slot(6, 10, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const p = await ok<CardPage>(await list(request, t, { shelf: 'mine', limit: 50 }));
  expect(p.items).toHaveLength(50);
  const r = await list(request, t, { shelf: 'mine', limit: 51 });
  expect(r.status()).toBe(400);
  expect((await errorCode(r)).code).toBe('VALIDATION_FAILED');
});

test('@TC-S2E6-011 con trỏ của ngăn Của tôi không dùng được cho Kho chung', async ({ request }, info) => {
  const s = slot(6, 11, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const p = await ok<CardPage>(await list(request, t, { shelf: 'mine' }));
  expect(p.nextCursor).toBeTruthy();
  const r = await list(request, t, { shelf: 'library', cursor: p.nextCursor! });
  expect(r.status()).toBe(400);
  expect((await errorCode(r)).code).toBe('INVALID_CURSOR');
});

test('@TC-S2E6-012 học sinh không vào được Kho đề', async ({ request }, info) => {
  const s = slot(6, 12, info);
  const t = await token(request, s.org, s.an, 'STUDENT');
  for (const shelf of ['mine', 'library']) {
    const r = await list(request, t, { shelf });
    expect(r.status()).toBe(403);
    const body = await r.json();
    expect(body.code).toBe('FORBIDDEN');
    expect(body.items).toBeUndefined();
  }
});

test('@TC-S2E6-016 đề chưa duyệt không gửi được', async ({ request }, info) => {
  const s = slot(6, 16, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const d = await ok<{ canSubmit: boolean; submitBlockedReason: string; humanLabel: string }>(await detail(request, t, s.doc(K.FX11_AI)));
  expect(d.humanLabel).toBe('AI soạn');
  expect(d.canSubmit).toBe(false);
  expect(d.submitBlockedReason).toBe(MSG.approveFirst);
  const r = await submit(request, t, s.doc(K.FX11_AI));
  expect(r.status()).toBe(409);
  expect(await errorCode(r)).toMatchObject({ code: 'CONFLICT', message: MSG.approveFirst });
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  const q = await ok<{ items: Array<{ docId: string }> }>(await queue(request, mai));
  expect(q.items.map((x) => x.docId)).not.toContain(s.doc(K.FX11_AI));
});

test('@TC-S2E6-017 gửi hai lần chỉ giữ một yêu cầu', async ({ request }, info) => {
  const s = slot(6, 17, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  expect((await ok<{ status: string }>(await submit(request, t, s.doc(K.FX14)))).status).toBe('pending');
  expect((await ok<{ status: string }>(await submit(request, t, s.doc(K.FX14)))).status).toBe('pending');
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  const q = await ok<{ items: Array<{ docId: string }> }>(await queue(request, mai));
  expect(q.items.filter((x) => x.docId === s.doc(K.FX14))).toHaveLength(1);
});

test('@TC-S2E6-018 người không phải người soát không thấy hàng đợi', async ({ request }, info) => {
  const s = slot(6, 18, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const r = await queue(request, t);
  expect(r.status()).toBe(403);
  expect((await r.json()).items).toBeUndefined();
  const a = await accept(request, t, s.sub(K.FX14));
  expect(a.status()).toBe(403);
  expect((await errorCode(a)).code).toBe('FORBIDDEN');
});

test('@TC-S2E6-019 người soát không soát đề của chính mình', async ({ request }, info) => {
  const s = slot(6, 19, info);
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  const r = await accept(request, mai, s.sub(K.MAI_FX13));
  expect(r.status()).toBe(403);
  expect(await errorCode(r)).toMatchObject({ code: 'FORBIDDEN', message: MSG.selfReview });
  const d = await ok<{ status: string }>(await detail(request, mai, s.doc(K.MAI_FX13)));
  expect(d.status).toBe('pending');
});

test('@TC-S2E6-021 lý do 500 ký tự nhận, 501 bị chặn', async ({ request }, info) => {
  const s = slot(6, 21, info);
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  expect((await ok<{ status: string }>(await reject(request, mai, s.sub(K.FX10), 'ă'.repeat(500)))).status).toBe('rejected');
  const r = await reject(request, mai, s.sub(K.FX14), 'ă'.repeat(501));
  expect(r.status()).toBe(400);
  expect((await errorCode(r)).code).toBe('VALIDATION_FAILED');
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  expect((await ok<{ status: string }>(await detail(request, trang, s.doc(K.FX14)))).status).toBe('pending');
});

test('@TC-S2E6-022 đề thứ 10 Chờ soát nhận, đề thứ 11 bị chặn', async ({ request }, info) => {
  const s = slot(6, 22, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  expect((await ok<{ status: string }>(await submit(request, t, s.doc(1010)))).status).toBe('pending');
  const r = await submit(request, t, s.doc(1011));
  expect(r.status()).toBe(409);
  expect(await errorCode(r)).toMatchObject({ code: 'CONFLICT', message: MSG.pendingLimit });
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  const q = await ok<{ items: Array<{ docId: string }> }>(await queue(request, mai));
  expect(q.items.map((x) => x.docId)).toContain(s.doc(1010));
  expect(q.items.map((x) => x.docId)).not.toContain(s.doc(1011));
});

test('@TC-S2E6-025 chủ sở hữu thấy đề Của tôi', async ({ request }, info) => {
  const s = slot(6, 25, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  const d = await ok<{ statement: string; owner: boolean; title: string }>(await detail(request, t, s.doc(K.FX10)));
  expect(d.owner).toBe(true);
  expect(d.statement).toBe('Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.');
});

test('@TC-S2E6-026 giáo viên khác nhận NOT_FOUND như đề không có', async ({ request }, info) => {
  const s = slot(6, 26, info);
  const lan = await token(request, s.org, s.lan, 'TEACHER');
  const r = await detail(request, lan, s.doc(K.FX10));
  const none = await detail(request, lan, s.doc(999));
  expect(r.status()).toBe(404);
  expect(none.status()).toBe(404);
  const a = await errorCode(r);
  const b = await errorCode(none);
  expect(a.code).toBe('NOT_FOUND');
  expect(a.message).toBe(b.message);
  expect(JSON.stringify(a)).not.toContain('AB = 5');
});

test('@TC-S2E6-027 đề Của tôi không lộ qua danh sách, tìm và đối chiếu', async ({ request }, info) => {
  const s = slot(6, 27, info);
  const lan = await token(request, s.org, s.lan, 'TEACHER');
  const leak = (body: string) => expect(body).not.toContain(s.doc(K.FX10));
  for (const shelf of ['mine', 'library']) {
    const r = await list(request, lan, { shelf });
    expect(r.status()).toBe(200);
    leak(await r.text());
    const found = await list(request, lan, { shelf, q: '5, 12, 13' });
    expect(found.status()).toBe(200);
    leak(await found.text());
    expect((await found.json()).items).toHaveLength(0);
  }
  const sim = await request.post(lib('/similar'), {
    headers: bearer(lan),
    data: { statement: 'Tam giác ABC có AB = 5, AC = 12, BC = 13' },
  });
  expect(sim.status()).toBe(200);
  leak(await sim.text());
  // Chính Cô Trang tìm thấy đề của mình
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  const own = await ok<CardPage>(await list(request, trang, { shelf: 'mine', q: '5, 12, 13' }));
  expect(own.items.map((c) => c.id)).toContain(s.doc(K.FX10));
});

test('@TC-S2E6-028 học sinh không thấy ngăn Của tôi của giáo viên', async ({ request }, info) => {
  const s = slot(6, 28, info);
  const an = await token(request, s.org, s.an, 'STUDENT');
  const r = await list(request, an, { shelf: 'mine' });
  expect(r.status()).toBe(403);
  expect((await errorCode(r)).code).toBe('FORBIDDEN');
  const d = await request.get(lib(`/problems/${s.doc(K.FX10)}`), { headers: bearer(an) });
  expect(d.status()).toBe(403);
  const mine = await request.get(`${API}/api/v1/student/assignments`, { headers: bearer(an) });
  expect(mine.status()).toBeLessThan(500); // em chỉ thấy đề qua bài giao của lớp
});

test('@TC-S2E6-029 người soát chỉ thấy đề đang Chờ soát', async ({ request }, info) => {
  const s = slot(6, 29, info);
  const mai = await token(request, s.org, s.mai, 'TEACHER');
  const pending = await ok<{ status: string; owner: boolean }>(await detail(request, mai, s.doc(K.FX14)));
  expect(pending.status).toBe('pending');
  expect(pending.owner).toBe(false);
  expect((await detail(request, mai, s.doc(K.FX10))).status()).toBe(404);
  expect((await detail(request, mai, s.doc(K.FX13))).status()).toBe(404);
});

test('@TC-S2E6-030 đề thứ 20 trong giờ nhận, đề thứ 21 bị chặn', async ({ request }, info) => {
  const s = slot(6, 30, info);
  const t = await token(request, s.org, s.trang, 'TEACHER');
  for (let i = 1; i <= 20; i++) expect((await create(request, t, i)).status()).toBe(201);
  const r = await create(request, t, 21);
  expect(r.status()).toBe(429);
  expect((await errorCode(r)).code).toBe('RATE_LIMITED');
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  const all = await ok<CardPage>(await list(request, t, { shelf: 'mine', limit: 50 }));
  expect(all.items).toHaveLength(20);
});

test('@TC-S2E6-033 bộ đếm theo tài khoản, không theo địa chỉ mạng', async ({ request }, info) => {
  const s = slot(6, 33, info);
  const ip = { 'X-Forwarded-For': '203.0.113.33' };
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  const lan = await token(request, s.org, s.lan, 'TEACHER');
  const blocked = await request.post(lib('/problems'), { headers: { ...bearer(trang), ...ip }, data: { statement: 'Đề của Cô Trang' } });
  expect(blocked.status()).toBe(429);
  const r = await request.post(lib('/problems'), { headers: { ...bearer(lan), ...ip }, data: { statement: 'Đề của Cô Lan' } });
  expect(r.status()).toBe(201);
  const again = await request.post(lib('/problems'), { headers: { ...bearer(trang), ...ip }, data: { statement: 'Đề của Cô Trang' } });
  expect(again.status()).toBe(429);
});

test('@TC-S2E6-035 học sinh không xem lời giải đề Kho chung ngoài bài giao', async ({ request }, info) => {
  const s = slot(6, 35, info);
  const an = await token(request, s.org, s.an, 'STUDENT');
  const r = await request.get(`${API}/api/v1/student/library/${s.lib(K.LIB_FX14)}/solution`, { headers: bearer(an) });
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const body = await r.text();
  expect(JSON.parse(body)).toEqual({ locked: true, message: MSG.studentLocked });
  for (const leak of ['45°', 'tan SCA', 'hình chiếu', 'steps']) expect(body).not.toContain(leak);
  const teacherView = await request.get(lib(`/shared/${s.lib(K.LIB_FX14)}`), { headers: bearer(an) });
  expect(teacherView.status()).toBe(403);
});

test('@TC-S2E6-036 tài khoản giáo viên mới không lấy được gì ngoài quyền giáo viên', async ({ request }, info) => {
  const s = slot(6, 36, info);
  const ha = await token(request, s.org, s.ha, 'TEACHER');
  const deny = (st: number) => expect([403, 404]).toContain(st);
  deny((await request.get(`${API}/api/v1/teacher/classes/${s.class11A1}/assignments`, { headers: bearer(ha) })).status());
  deny((await detail(request, ha, s.doc(K.FX10))).status());
  const release = await request.post(`${API}/api/v1/teacher/assignments/${s.bg(9001)}/release`, {
    headers: bearer(ha),
    data: { openNow: true },
  });
  deny(release.status());
  const shared = await ok<CardPage>(await list(request, ha, { shelf: 'library' }));
  expect(shared.items.length).toBeGreaterThan(0);
  expect((await create(request, ha, 1)).status()).toBe(201);
});

test('@TC-S2E6-037 học sinh tự học xem đề Kho chung: khóa lời giải, chỉ có gợi ý', async ({ request }, info) => {
  const s = slot(6, 37, info);
  const an = await token(request, s.org, s.an, 'STUDENT');
  const id = s.lib(K.LIB_FX14);
  const item = await ok<{ statement: string }>(await request.get(`${API}/api/v1/student/library/${id}`, { headers: bearer(an) }));
  expect(item.statement).toContain('hình chóp S.ABCD');
  const hint = await ok<{ hint: string }>(await request.get(`${API}/api/v1/student/library/${id}/hint`, { headers: bearer(an) }));
  expect(hint.hint.length).toBeGreaterThan(5);
  const sol = await request.get(`${API}/api/v1/student/library/${id}/solution`, { headers: bearer(an) });
  const body = await sol.text();
  expect(JSON.parse(body)).toMatchObject({ locked: true, message: MSG.studentLocked });
  expect(body).not.toContain('45°');
  expect(JSON.stringify(item)).not.toContain('45°');
});
