import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { api, assignDraft, assignedSet, auditOf, bearer, deniedOf, draftSet, err, follow, hoursRound, khoi, klass, lan, ms, ok, resetPassword, SETS, smallWorld, who, type AuditEvent, type Follow } from '../s3e4/s3e4.ts';

/**
 * US-S3E5-01 · AuditEvent (EARLY_RELEASE, RELEASE_TIME_CHANGE, PASSWORD_RESET) qua API nhật ký của lớp
 * (T-261126-31). Mọi ca dựng thế giới riêng; giờ là tương đối so với giờ máy chủ.
 */

const ALLOWED = ['id', 'type', 'actorCode', 'actorRole', 'targetType', 'targetId', 'classId', 'before', 'after', 'result', 'at'];
const ISO_UTC = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?Z$/;

test('@TC-S3E5-01 Mở ngay ghi đúng một EARLY_RELEASE: người thực hiện, đối tượng, giờ trước / sau, Thành công, giờ máy chủ UTC', async ({ request }) => {
  const w = await smallWorld(request);
  const release = hoursRound(48);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { releaseAt: release });
  const t0 = Date.now();
  const opened = await ok<Follow>(await api.openNow(request, lan(w).token, set.id, { requestKey: randomUUID() }));
  const events = await auditOf(request, lan(w).token, klass(w, '11A1').id);
  expect(events).toHaveLength(1);
  const e = events[0]!;
  expect(e).toMatchObject({
    type: 'EARLY_RELEASE',
    actorCode: lan(w).userCode,
    actorRole: 'TEACHER',
    targetType: 'assignment_set',
    targetId: set.id,
    classId: klass(w, '11A1').id,
    result: 'SUCCESS',
  });
  expect(ms(e.before), 'giá trị trước = giờ mở đã đặt').toBe(ms(release));
  expect(ms(e.after), 'giá trị sau = giờ máy chủ lúc xử lý').toBe(ms(opened.releaseAt));
  expect(Math.abs(ms(e.after)! - t0)).toBeLessThan(60_000);
  expect(e.at).toMatch(ISO_UTC);
});

test('@TC-S3E5-02 Đổi giờ ghi RELEASE_TIME_CHANGE với giá trị trước và sau', async ({ request }) => {
  const w = await smallWorld(request);
  const release = hoursRound(48);
  const next = hoursRound(59);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { releaseAt: release });
  await ok(await api.changeTime(request, lan(w).token, set.id, { releaseAt: next, requestKey: randomUUID() }));
  const events = await auditOf(request, lan(w).token, klass(w, '11A1').id);
  expect(events).toHaveLength(1);
  expect(events[0]).toMatchObject({ type: 'RELEASE_TIME_CHANGE', actorCode: lan(w).userCode, targetId: set.id, result: 'SUCCESS' });
  expect(ms(events[0]!.before)).toBe(ms(release));
  expect(ms(events[0]!.after)).toBe(ms(next));
});

test('@TC-S3E5-03 Đặt lại mật khẩu ghi PASSWORD_RESET, bản ghi không chứa mật khẩu tạm', async ({ request }) => {
  const w = await smallWorld(request);
  const mai = who(w, 'Chi Mai');
  const reset = await resetPassword(request, lan(w), klass(w, '11A1').id, mai.id);
  expect(reset.status(), await reset.text()).toBe(200);
  const temp = ((await reset.json()) as { tempPassword: string }).tempPassword;
  const r = await api.audit(request, lan(w).token, klass(w, '11A1').id);
  const events = (await ok<{ items: AuditEvent[] }>(r)).items;
  expect(events).toHaveLength(1);
  expect(events[0]).toMatchObject({ type: 'PASSWORD_RESET', actorCode: lan(w).userCode, actorRole: 'TEACHER', targetType: 'user', targetId: mai.userCode, result: 'SUCCESS' });
  const text = (await r.text()).toLowerCase();
  expect(text).not.toContain(temp.toLowerCase());
  expect(text).not.toContain(temp.replaceAll('-', '').toLowerCase());
  for (const word of ['password', 'hash', 'pbkdf2', 'matkhau', mai.password.toLowerCase()]) expect(text.replaceAll('password_reset', ''), word).not.toContain(word);
});

test('@TC-S3E5-04 bản ghi của cả ba loại chỉ có 11 trường được phép; không tên học sinh, IP hay thiết bị', async ({ request }) => {
  const w = await smallWorld(request);
  const c = klass(w, '11A1').id;
  const a = await assignedSet(request, lan(w), c, { title: 'Bộ bài tuần 15', items: [1] });
  const b = await assignedSet(request, lan(w), c, { title: 'Bộ bài tuần 16', items: [2] });
  await ok(await api.openNow(request, lan(w).token, a.id));
  await ok(await api.changeTime(request, lan(w).token, b.id, { releaseAt: hoursRound(60) }));
  expect((await resetPassword(request, lan(w), c, who(w, 'Chi Mai').id)).status()).toBe(200);
  const r = await api.audit(request, lan(w).token, c);
  const events = (await ok<{ items: AuditEvent[] }>(r)).items;
  expect(events.map((e) => e.type).sort()).toEqual(['EARLY_RELEASE', 'PASSWORD_RESET', 'RELEASE_TIME_CHANGE']);
  for (const e of events) for (const key of Object.keys(e)) expect(ALLOWED, `${e.type}.${key}`).toContain(key);
  const text = await r.text();
  for (const hidden of ['Chi Mai', 'Bình An', 'displayName', 'ip', 'userAgent', 'device', 'requestKey']) expect(text, hidden).not.toContain(`"${hidden}`);
  expect(text).not.toContain('Chi Mai');
  expect(r.headers()['cache-control']).toContain('no-store');
});

test('@TC-S3E5-05 giờ ghi là giờ máy chủ UTC dạng ISO-8601; giờ của điện thoại lệch 3 giờ không có trong bản ghi', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const clientTime = new Date(Date.now() + 3 * 3_600_000).toISOString();
  const t0 = Date.now();
  await ok(await api.openNow(request, lan(w).token, set.id, { clientTime }));
  const r = await api.audit(request, lan(w).token, klass(w, '11A1').id);
  const [e] = (await ok<{ items: AuditEvent[] }>(r)).items;
  expect(e!.at).toMatch(ISO_UTC);
  expect(e!.after).toMatch(ISO_UTC);
  expect(Math.abs(ms(e!.at)! - t0), 'giờ sự kiện = giờ máy chủ').toBeLessThan(60_000);
  expect(await r.text()).not.toContain(clientTime.slice(0, 13));
  // Màn nhật ký hiện "HH:mm, dd/MM/yyyy" giờ Việt Nam: hàm auditLine / vnAt, kiểm ở web/src/features/s3e4/format.test.ts.
});

test('@TC-S3E5-06 chủ lớp đọc nhật ký từng lớp của mình: 11A1 ba bản ghi, 11A2 một, không lẫn lớp khác', async ({ request }) => {
  const w = await smallWorld(request);
  const a1 = klass(w, '11A1').id;
  const a2 = klass(w, '11A2').id;
  const s1 = await assignedSet(request, lan(w), a1, { items: [1] });
  await ok(await api.changeTime(request, lan(w).token, s1.id, { releaseAt: hoursRound(60) }));
  await ok(await api.openNow(request, lan(w).token, s1.id));
  expect((await resetPassword(request, lan(w), a1, who(w, 'Bình An').id)).status()).toBe(200);
  expect((await resetPassword(request, lan(w), a2, who(w, 'Quốc Bảo').id)).status()).toBe(200);
  const first = await auditOf(request, lan(w).token, a1);
  const second = await auditOf(request, lan(w).token, a2);
  expect(first.map((e) => e.type)).toEqual(['PASSWORD_RESET', 'EARLY_RELEASE', 'RELEASE_TIME_CHANGE']);
  expect(first.every((e) => e.classId === a1)).toBe(true);
  expect(second).toHaveLength(1);
  expect(second[0]).toMatchObject({ type: 'PASSWORD_RESET', classId: a2, targetId: who(w, 'Quốc Bảo').userCode });
});

test('@TC-S3E5-08 đặt và đổi giờ khi bộ còn Nháp không ghi AuditEvent; chỉ lần đổi sau khi giao mới ghi', async ({ request }) => {
  const w = await smallWorld(request);
  const c = klass(w, '11A1').id;
  const d = await draftSet(request, lan(w), c, { title: 'Bộ bài tuần 16', releaseAt: null });
  const drafts = [40, 44, 48].map((h) => hoursRound(h));
  for (const at of drafts) {
    const r = await request.put(`${SETS}/${d.id}/schedule`, { headers: bearer(lan(w).token), data: { releaseAt: at, set: true } });
    expect(r.status(), await r.text()).toBe(200);
  }
  await assignDraft(request, lan(w), d.id);
  expect(await auditOf(request, lan(w).token, c), 'ba lần đặt khi còn Nháp và việc giao không có bản ghi').toEqual([]);
  const later = hoursRound(52);
  const r = await request.put(`${SETS}/${d.id}/schedule`, { headers: bearer(lan(w).token), data: { releaseAt: later, set: true } });
  expect(r.status(), await r.text()).toBe(200);
  const events = await auditOf(request, lan(w).token, c);
  expect(events.map((e) => e.type)).toEqual(['RELEASE_TIME_CHANGE']);
  expect(ms(events[0]!.before)).toBe(ms(drafts[2]));
  expect(ms(events[0]!.after)).toBe(ms(later));
});

test('@TC-S3E5-09 thao tác bị từ chối ghi một bản ghi Từ chối chỉ có loại, người gọi, loại đối tượng', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  expect(await deniedOf(request, khoi(w).id)).toEqual([]);
  const r = await api.openNow(request, khoi(w).token, set.id, { requestKey: randomUUID(), note: 'nội dung yêu cầu' });
  expect(r.status()).toBe(404);
  expect((await err(r)).code).toBe('NOT_FOUND');
  const denied = await deniedOf(request, khoi(w).id);
  expect(denied).toEqual([{ type: 'EARLY_RELEASE', actorCode: khoi(w).userCode, actorRole: 'teacher', targetType: 'assignment_set', result: 'DENIED', bare: true }]);
  expect(JSON.stringify(denied)).not.toContain('11A1');
  expect(await auditOf(request, lan(w).token, klass(w, '11A1').id), 'không có EARLY_RELEASE thành công').toEqual([]);
  expect((await follow(request, lan(w).token, set.id)).releaseState).toBe('upcoming');
});

test('@TC-S3E5-11 học sinh không đọc được nhật ký', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  await ok(await api.openNow(request, lan(w).token, set.id));
  const r = await api.audit(request, who(w, 'Bình An').token, klass(w, '11A1').id);
  expect(r.status()).toBe(403);
  const body = (await r.json()) as Record<string, unknown>;
  expect(body.code).toBe('FORBIDDEN');
  expect(body).not.toHaveProperty('items');
  expect(JSON.stringify(body)).not.toContain('EARLY_RELEASE');
});

test('@TC-S3E5-12 giáo viên khác không đọc được nhật ký lớp 11A1', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  await ok(await api.openNow(request, lan(w).token, set.id));
  const r = await api.audit(request, khoi(w).token, klass(w, '11A1').id);
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text)).not.toHaveProperty('items');
  for (const hidden of [lan(w).userCode, 'EARLY_RELEASE', set.id, '11A1']) expect(text).not.toContain(hidden);
});

test('@TC-S3E5-13 gửi Mở ngay hai lần cùng khóa chống lặp chỉ có một EARLY_RELEASE', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const key = randomUUID();
  await ok(await api.openNow(request, lan(w).token, set.id, { requestKey: key }));
  await ok(await api.openNow(request, lan(w).token, set.id, { requestKey: key }));
  expect((await auditOf(request, lan(w).token, klass(w, '11A1').id)).map((e) => e.type)).toEqual(['EARLY_RELEASE']);
});

test('@TC-S3E5-14 đặt lại mật khẩu lần thứ 11 trong một giờ bị RATE_LIMITED: nhật ký có đúng 10 PASSWORD_RESET thành công', async ({ request }) => {
  const w = await smallWorld(request);
  const c = klass(w, '11A1').id;
  const mai = who(w, 'Chi Mai');
  for (let i = 0; i < 10; i++) {
    const r = await resetPassword(request, lan(w), c, mai.id);
    expect(r.status(), `lần ${i + 1}`).toBe(200);
  }
  const eleventh = await resetPassword(request, lan(w), c, mai.id);
  expect(eleventh.status()).toBe(429);
  expect((await err(eleventh)).code).toBe('RATE_LIMITED');
  expect(eleventh.headers()['retry-after']).toBeTruthy();
  const events = await auditOf(request, lan(w).token, c);
  expect(events.filter((e) => e.type === 'PASSWORD_RESET' && e.result === 'SUCCESS'), 'lần thứ 11 không có bản ghi thành công').toHaveLength(10);
  // Bản ghi "Từ chối" của lần thứ 11 thuộc luồng đặt lại mật khẩu của iam (T-261126-28): xem Dev-S3E5-09 ở qc_findings.md.
});
