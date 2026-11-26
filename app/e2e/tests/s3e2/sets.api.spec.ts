import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { assigned as s2e8Assigned, attempt as s2e8Attempt, result as s2e8Result, sandbox as s2e8Sandbox, student as s2e8Student } from '../s2e8/s2e8.ts';
import { assigned, assignRaw, attempt, bearer, body, createRaw, draft, home, hours, releaseIn, S, sandbox, T, teacherSet, V, walk } from './s3e2.ts';

/** S3-E2 · ca API: biên 1–20 đề, quyền, giao lặp, lịch mở lời giải, Bài của em, đóng và lưu trữ bộ. */

const MSG23 = 'Bộ bài cần từ 1 đến 20 đề.';
const MSG26 = 'Lời giải đã mở, không đổi giờ được.';

test('@TC-S3E2-06 bộ đúng 20 đề được giao, học sinh thấy 20 đề đúng thứ tự', async ({ request }) => {
  const sb = await sandbox(request);
  const order = Array.from({ length: 20 }, (_, k) => 20 - k); // đề 20, 19, …, 1
  const set = await assigned(request, sb, { items: order });
  expect(set.status).toBe('assigned');
  expect(set.items).toHaveLength(20);
  const r = await request.get(`${S}/${set.id}`, { headers: bearer(sb.classes[0]!.students['Bình An']!.token) });
  expect(r.status()).toBe(200);
  const view = (await r.json()) as { total: number; items: Array<{ position: number; assignmentId: string }> };
  expect(view.total).toBe(20);
  expect(view.items.map((i) => i.position)).toEqual(Array.from({ length: 20 }, (_, k) => k + 1));
  expect(view.items.map((i) => i.assignmentId)).toEqual(set.items.map((i) => i.assignmentId));
  expect(set.items.map((i) => i.code)).toEqual(order.map((n) => `BOB-${String(n).padStart(2, '0')}`));
});

test('@TC-S3E2-08 đề thứ 21 bị chặn: VALIDATION_FAILED, bộ vẫn 20 đề', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: Array.from({ length: 20 }, (_, k) => k + 1) });
  const r = await request.post(`${T}/${d.id}/items`, { headers: bearer(sb.teacher.token), data: { problemVersionId: V(21) } });
  expect(r.status()).toBe(400);
  const err = (await r.json()) as { code: string; message: string };
  expect(err.code).toBe('VALIDATION_FAILED');
  expect(err.message + JSON.stringify(err)).toContain(MSG23);
  expect((await teacherSet(request, sb.teacher.token, d.id)).items).toHaveLength(20);
  const big = await createRaw(request, sb.teacher.token, body({ items: Array.from({ length: 21 }, (_, k) => k + 1) }));
  expect(big.status()).toBe(400);
  expect(await big.text()).toContain(MSG23);
});

test('@TC-S3E2-11 tạo bộ với lớp 10B của thầy Khôi: NOT_FOUND, không tạo bộ, không lộ tên lớp', async ({ request }) => {
  const sb = await sandbox(request);
  const r = await createRaw(request, sb.teacher.token, body({ classId: sb.otherClasses[0]!.id }));
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  expect(text).not.toContain('10B');
  const list = await request.get(T, { headers: bearer(sb.teacher.token) });
  expect(((await list.json()) as { items: unknown[] }).items).toHaveLength(0);
});

test('@TC-S3E2-12 thêm đề Của tôi / ngoài Kho chung của giáo viên khác: NOT_FOUND, bộ không đổi', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { items: [1, 2] });
  // đề luyện riêng của nhóm S2-E8 (bộ trường này không thấy) và sổ "Của tôi" của Cô Lan nhóm S2-E4 (F-S2-860111)
  for (const foreign of ['00000000-0000-7000-8002-000000831101', '00000000-0000-7000-8002-000000860111']) {
    const r = await request.post(`${T}/${d.id}/items`, { headers: bearer(sb.teacher.token), data: { problemVersionId: foreign } });
    expect(r.status(), foreign).toBe(404);
    expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  }
  expect((await teacherSet(request, sb.teacher.token, d.id)).items.map((i) => i.code)).toEqual(['BOB-01', 'BOB-02']);
});

test('@TC-S3E2-14 bấm Giao hai lần cùng khóa yêu cầu chỉ giao một lần', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { releaseAt: hours(48), dueAt: hours(24) });
  const key = randomUUID();
  const [a, b] = [await assignRaw(request, sb.teacher.token, d.id, key), await assignRaw(request, sb.teacher.token, d.id, key)];
  expect(a.status(), await a.text()).toBe(200);
  expect(b.status(), await b.text()).toBe(200);
  expect(((await b.json()) as { status: string }).status).toBe('assigned');
  const list = (await (await request.get(T, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ status: string }> };
  expect(list.items.filter((s) => s.status === 'assigned')).toHaveLength(1);
  for (const name of ['Bình An', 'Chi Mai', 'Đức Huy']) {
    const h = await home(request, sb.classes[0]!.students[name]!.token);
    expect(h.groups.flatMap((g) => g.sets)).toHaveLength(1);
  }
  // mỗi item vẫn đúng một hàng assignment (không sinh lần hai)
  const set = await teacherSet(request, sb.teacher.token, d.id);
  expect(new Set(set.items.map((i) => i.assignmentId)).size).toBe(4);
});

test('@TC-S3E2-17 biên giờ mở: trước giờ mở là Sắp mở không có lời giải; tới giờ mở có lời giải; luôn no-store', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = sb.classes[0]!.students['Bình An']!.token;
  await releaseIn(request, set.items[0]!.assignmentId!, 3);
  const url = `${S}/${set.id}/items/1/solution`;
  const before = await request.get(url, { headers: bearer(an) });
  expect(before.status()).toBe(200);
  expect(before.headers()['cache-control']).toContain('no-store');
  const b = (await before.json()) as Record<string, unknown>;
  expect(b.releaseState).toBe('upcoming');
  expect(b.finalAnswer ?? null).toBeNull();
  expect(b.steps ?? null).toBeNull();
  await expect
    .poll(async () => ((await (await request.get(url, { headers: bearer(an) })).json()) as { releaseState: string }).releaseState, { timeout: 15_000 })
    .toBe('open');
  const after = await request.get(url, { headers: bearer(an) });
  expect(after.headers()['cache-control']).toContain('no-store');
  const a = (await after.json()) as { finalAnswer: string; steps: unknown[] };
  expect(a.finalAnswer).toBe('45');
  expect(a.steps.length).toBeGreaterThan(0);
});

test('@TC-S3E2-18 bộ đã giao chưa đặt giờ: mọi đề Chưa đặt giờ, không thân nào có đáp số', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb, { releaseAt: null });
  const an = sb.classes[0]!.students['Bình An']!.token;
  const detail = await request.get(`${S}/${set.id}`, { headers: bearer(an) });
  const d = (await detail.json()) as { releaseState: string; items: Array<{ releaseState: string }> };
  expect(d.releaseState).toBe('unset');
  expect(d.items.every((i) => i.releaseState === 'unset')).toBe(true);
  for (let p = 1; p <= 4; p++) {
    const r = await request.get(`${S}/${set.id}/items/${p}/solution`, { headers: bearer(an) });
    const v = (await r.json()) as Record<string, unknown>;
    expect(v.releaseState).toBe('unset');
    expect(v.finalAnswer ?? null).toBeNull();
    const strings = walk(v).strings.join(' ');
    expect(strings).not.toMatch(/Lập luận đầy đủ|Đáp số/);
  }
});

test('@TC-S3E2-23 giờ thiết bị bị bỏ qua: tiêu đề giờ tương lai vẫn Sắp mở', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = sb.classes[0]!.students['Bình An']!.token;
  const r = await request.get(`${S}/${set.id}/items/1/solution`, {
    headers: { ...bearer(an), 'X-Device-Time': '2026-12-10T08:00:00+07:00', Date: 'Thu, 10 Dec 2026 01:00:00 GMT' },
  });
  const v = (await r.json()) as Record<string, unknown>;
  expect(v.releaseState).toBe('upcoming');
  expect(v.finalAnswer ?? null).toBeNull();
});

test('@TC-S3E2-24 đổi giờ khi lời giải đã mở: CONFLICT MSG-S3-26, giờ mở không đổi', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  for (const i of set.items) await releaseIn(request, i.assignmentId!, -1);
  const opened = await teacherSet(request, sb.teacher.token, set.id);
  const r = await request.put(`${T}/${set.id}/schedule`, {
    headers: bearer(sb.teacher.token),
    data: { releaseAt: hours(72), set: true, version: opened.scheduleVersion },
  });
  expect(r.status()).toBe(409);
  const err = (await r.json()) as { code: string; message: string };
  expect(err.code).toBe('CONFLICT');
  expect(err.message).toBe(MSG26);
  const after = await teacherSet(request, sb.teacher.token, set.id);
  expect(after.items.map((i) => i.releaseAt)).toEqual(opened.items.map((i) => i.releaseAt));
});

test('@TC-S3E2-25 hai lần đổi giờ chồng nhau: lần đến sau CONFLICT, giờ của lần đầu được giữ', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const v = (await teacherSet(request, sb.teacher.token, set.id)).scheduleVersion;
  const first = hours(50);
  const one = await request.put(`${T}/${set.id}/schedule`, { headers: bearer(sb.teacher.token), data: { releaseAt: first, set: true, version: v } });
  expect(one.status(), await one.text()).toBe(200);
  const two = await request.put(`${T}/${set.id}/schedule`, { headers: bearer(sb.teacher.token), data: { releaseAt: hours(52), set: true, version: v } });
  expect(two.status()).toBe(409);
  const now = await teacherSet(request, sb.teacher.token, set.id);
  expect(new Date(now.releaseAt!).getTime()).toBe(new Date(first).getTime());
  for (const i of now.items) expect(new Date(i.releaseAt!).getTime()).toBe(new Date(first).getTime());
});

test('@TC-S3E2-26 giáo viên khác đặt lịch bộ của lớp cô Lan: NOT_FOUND, giờ mở không đổi', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const r = await request.put(`${T}/${set.id}/schedule`, { headers: bearer(sb.otherTeacher.token), data: { releaseAt: hours(60), set: true, version: 0 } });
  expect(r.status()).toBe(404);
  expect((await teacherSet(request, sb.teacher.token, set.id)).releaseAt).toBe(set.releaseAt);
});

test('@TC-S3E2-27 học sinh đặt lịch: FORBIDDEN, giờ mở không đổi', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const r = await request.put(`${T}/${set.id}/schedule`, {
    headers: bearer(sb.classes[0]!.students['Bình An']!.token),
    data: { releaseAt: hours(60), set: true, version: 0 },
  });
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { code: string }).code).toBe('FORBIDDEN');
  expect((await teacherSet(request, sb.teacher.token, set.id)).releaseAt).toBe(set.releaseAt);
});

test('@TC-S3E2-28 giờ mở sai định dạng hoặc rỗng kèm cờ đặt giờ: VALIDATION_FAILED chỉ ra trường giờ mở', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb, { releaseAt: hours(30) });
  for (const releaseAt of ['31/02/2026 08:00', '']) {
    const r = await request.put(`${T}/${d.id}/schedule`, { headers: bearer(sb.teacher.token), data: { releaseAt, set: true } });
    expect(r.status(), releaseAt).toBe(400);
    const err = (await r.json()) as { code: string; details?: Array<{ field: string }> };
    expect(err.code).toBe('VALIDATION_FAILED');
    expect(err.details?.map((x) => x.field)).toContain('releaseAt');
  }
  expect((await teacherSet(request, sb.teacher.token, d.id)).releaseAt).toBe(d.releaseAt);
});

test('@TC-S3E2-34 học sinh Chờ duyệt không thấy bộ: Bài của em trống, chi tiết NOT_FOUND', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const nam = sb.classes[0]!.pending['Hoàng Nam']!.token;
  expect((await home(request, nam)).groups).toEqual([]);
  expect((await request.get(`${S}/${set.id}`, { headers: bearer(nam) })).status()).toBe(404);
});

test('@TC-S3E2-35 bộ Nháp không hiện cho học sinh: không có trong Bài của em, chi tiết NOT_FOUND', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb);
  const an = sb.classes[0]!.students['Bình An']!.token;
  expect((await home(request, an)).groups.flatMap((g) => g.sets).map((s) => s.id)).not.toContain(d.id);
  expect((await request.get(`${S}/${d.id}`, { headers: bearer(an) })).status()).toBe(404);
});

test('@TC-S3E2-36 tiến độ của bạn không lộ: thân phản hồi chỉ có tiến độ của chính em', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const st = sb.classes[0]!.students;
  await attempt(request, st['Chi Mai']!.token, set.items[0]!.assignmentId!, '45');
  await attempt(request, st['Đức Huy']!.token, set.items[1]!.assignmentId!, '12/5');
  await attempt(request, st['Bình An']!.token, set.items[0]!.assignmentId!, '45');
  const bodies = [await request.get(S, { headers: bearer(st['Bình An']!.token) }), await request.get(`${S}/${set.id}`, { headers: bearer(st['Bình An']!.token) })];
  for (const r of bodies) {
    const json = await r.json();
    const { keys, strings } = walk(json);
    const all = strings.join(' ');
    for (const other of ['Chi Mai', 'Đức Huy', 'Hoàng Nam', st['Chi Mai']!.id, st['Đức Huy']!.id]) expect(all).not.toContain(other);
    expect(keys.filter((k) => /student|member|rank|average|classTotal|count/i.test(k))).toEqual([]);
  }
  const d = (await bodies[1]!.json()) as { done: number; items: Array<{ state: string }> };
  expect(d.done).toBe(1);
  expect(d.items.map((i) => i.state)).toEqual(['correct', 'todo', 'todo', 'todo']);
});

test('@TC-S3E2-38 bài luyện giao ở chặng 2 hiện như một bộ Đã giao, kết quả của An còn nguyên', async ({ request }) => {
  const sb = await s2e8Sandbox(request);
  const practice = await s2e8Assigned(request, sb);
  const an = s2e8Student(sb, 'An');
  expect((await s2e8Attempt(request, an.token, practice.items[0]!.assignmentId, '6')).outcome).toBe('correct');
  const list = (await (await request.get(T, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ id: string; status: string; itemCount: number }> };
  expect(list.items).toHaveLength(1);
  expect(list.items[0]).toMatchObject({ id: practice.id, status: 'assigned', itemCount: 5 });
  const set = await teacherSet(request, sb.teacher.token, practice.id);
  expect(set.items.map((i) => i.assignmentId)).toEqual(practice.items.map((i) => i.assignmentId));
  const mine = (await (await request.get(`${S}/${practice.id}`, { headers: bearer(an.token) })).json()) as { done: number; items: Array<{ state: string }> };
  expect(mine.items[0]!.state).toBe('correct');
  expect(mine.done).toBe(1);
  expect((await s2e8Result(request, an.token, practice.id)).correct).toBe(1);
});

test('@TC-S3E2-39 đóng bộ: Đã đóng, em xem được kết quả Đề 1, gửi Đề 2 CONFLICT không ghi lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = sb.classes[0]!.students['Bình An']!.token;
  expect((await attempt(request, an, set.items[0]!.assignmentId!, '45')).status()).toBe(200);
  const c = await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(c.status(), await c.text()).toBe(200);
  expect(((await c.json()) as { status: string }).status).toBe('closed');
  const r = await attempt(request, an, set.items[1]!.assignmentId!, '12/5');
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { code: string }).code).toBe('CONFLICT');
  const d = (await (await request.get(`${S}/${set.id}`, { headers: bearer(an) })).json()) as { status: string; items: Array<{ state: string }> };
  expect(d.status).toBe('closed');
  expect(d.items.map((i) => i.state)).toEqual(['correct', 'todo', 'todo', 'todo']);
});

test('@TC-S3E2-40 bộ Đã đóng vẫn mở lời giải khi tới giờ mở', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = sb.classes[0]!.students['Bình An']!.token;
  await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  await releaseIn(request, set.items[1]!.assignmentId!, 1);
  const url = `${S}/${set.id}/items/2/solution`;
  await expect.poll(async () => ((await (await request.get(url, { headers: bearer(an) })).json()) as { releaseState: string }).releaseState, { timeout: 15_000 }).toBe('open');
  const v = (await (await request.get(url, { headers: bearer(an) })).json()) as { finalAnswer: string };
  expect(v.finalAnswer).toBe('12/5');
  const d = (await (await request.get(`${S}/${set.id}`, { headers: bearer(an) })).json()) as { status: string };
  expect(d.status).toBe('closed'); // màn em không có nút nhập đáp số
});

test('@TC-S3E2-42 bộ Nháp không đóng được: CONFLICT, vẫn Nháp', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await draft(request, sb);
  const r = await request.post(`${T}/${d.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(r.status()).toBe(409);
  expect((await teacherSet(request, sb.teacher.token, d.id)).status).toBe('draft');
});

test('@TC-S3E2-43 bộ Đã giao chưa đóng thì không lưu trữ được: CONFLICT, vẫn Đã giao', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const r = await request.post(`${T}/${set.id}/archive`, { headers: bearer(sb.teacher.token), data: { requestKey: randomUUID() } });
  expect(r.status()).toBe(409);
  expect((await teacherSet(request, sb.teacher.token, set.id)).status).toBe('assigned');
});

test('@TC-S3E2-44 giáo viên khác không đóng được bộ: NOT_FOUND, bộ không đổi', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const r = await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.otherTeacher.token), data: { requestKey: randomUUID() } });
  expect(r.status()).toBe(404);
  expect((await teacherSet(request, sb.teacher.token, set.id)).status).toBe('assigned');
});

test('@TC-S3E2-45 bấm Đóng lặp cùng khóa: đóng một lần, lần hai trả cùng kết quả', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const key = randomUUID();
  const a = await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: key } });
  const b = await request.post(`${T}/${set.id}/close`, { headers: bearer(sb.teacher.token), data: { requestKey: key } });
  expect(a.status()).toBe(200);
  expect(b.status()).toBe(200);
  const [x, y] = [(await a.json()) as { status: string; closedAt?: string }, (await b.json()) as { status: string; closedAt?: string }];
  expect(x.status).toBe('closed');
  expect(y.status).toBe('closed');
  expect(y.closedAt).toBe(x.closedAt);
});
