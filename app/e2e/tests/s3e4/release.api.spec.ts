import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { api, assignedSet, auditOf, bearer, deniedOf, err, follow, hours, hoursRound, khoi, klass, lan, ms, MSG, notesOf, ok, SETS, smallWorld, who, type Follow } from './s3e4.ts';

/** S3-E4 · US-S3E4-02 "Mở ngay" và "Đổi giờ" qua API: biên 1 phút, đã mở, chống lặp, hai nơi cùng đổi, quyền, giờ thiết bị. */

const OPENED = 'Lời giải Bộ bài tuần 15 đã mở';

test('@TC-S3E4-17 biên giờ mới: sớm hơn 1 phút kể từ giờ máy chủ bị từ chối, từ 1 phút trở lên được nhận', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const before = await follow(request, lan(w).token, set.id);
  // Biên chính xác 21:01:00 / 21:00:59 kiểm ở SetReleaseServiceTest (đồng hồ cố định); ở đây đi qua API thật.
  const near = await api.changeTime(request, lan(w).token, set.id, { releaseAt: new Date(Date.now() + 45_000).toISOString() });
  expect(near.status()).toBe(400);
  const e = await err(near);
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(JSON.stringify(e)).toContain(MSG.s63);
  expect(ms((await follow(request, lan(w).token, set.id)).releaseAt), 'giờ mở giữ nguyên').toBe(ms(before.releaseAt));
  const fine = new Date(Date.now() + 80_000).toISOString();
  const good = await ok<Follow>(await api.changeTime(request, lan(w).token, set.id, { releaseAt: fine }));
  expect(ms(good.releaseAt)).toBe(ms(fine));
  expect(good.releaseState).toBe('upcoming');
});

test('@TC-S3E4-19 lời giải đã mở thì Mở ngay và Đổi giờ đều CONFLICT, không thêm AuditEvent hay thông báo', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const opened = await ok<Follow>(await api.openNow(request, lan(w).token, set.id));
  expect(opened.releaseState).toBe('open');
  const again = await api.openNow(request, lan(w).token, set.id);
  const change = await api.changeTime(request, lan(w).token, set.id, { releaseAt: hours(30) });
  for (const r of [again, change]) {
    expect(r.status()).toBe(409);
    const e = await err(r);
    expect(e.code).toBe('CONFLICT');
    expect(e.message).toBe(MSG.s67);
  }
  expect(ms((await follow(request, lan(w).token, set.id)).releaseAt)).toBe(ms(opened.releaseAt));
  expect((await auditOf(request, lan(w).token, klass(w, '11A1').id)).map((x) => x.type)).toEqual(['EARLY_RELEASE']);
  expect((await notesOf(request, who(w, 'Bình An').token)).map((n) => n.body)).toEqual([OPENED, 'Có bài mới: Bộ bài tuần 15']);
});

test('@TC-S3E4-20 hai yêu cầu Mở ngay cùng khóa chống lặp: một AuditEvent, mỗi em một thông báo, hai phản hồi cùng thành công', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const key = randomUUID();
  const v = (await follow(request, lan(w).token, set.id)).version;
  const a = await ok<Follow>(await api.openNow(request, lan(w).token, set.id, { requestKey: key, version: v }));
  await new Promise((r) => setTimeout(r, 1000));
  const b = await ok<Follow>(await api.openNow(request, lan(w).token, set.id, { requestKey: key, version: v }));
  expect(b.releaseState).toBe('open');
  expect(ms(b.releaseAt)).toBe(ms(a.releaseAt));
  expect((await auditOf(request, lan(w).token, klass(w, '11A1').id)).filter((x) => x.type === 'EARLY_RELEASE')).toHaveLength(1);
  for (const name of ['Bình An', 'Chi Mai', 'Khánh Linh']) {
    const bodies = (await notesOf(request, who(w, name).token)).map((n) => n.body);
    expect(bodies.filter((x) => x === OPENED), name).toHaveLength(1);
  }
});

test('@TC-S3E4-22 web đổi giờ trước, điện thoại Mở ngay với phiên bản lịch cũ bị CONFLICT; chỉ một AuditEvent', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const seen = await follow(request, lan(w).token, set.id); // hai thiết bị cùng đọc lịch ở phiên bản này
  const ten = hoursRound(26);
  const web = await request.put(`${SETS}/${set.id}/schedule`, { headers: bearer(lan(w).token), data: { releaseAt: ten, set: true, version: seen.version } });
  expect(web.status(), await web.text()).toBe(200);
  const phone = await api.openNow(request, lan(w).token, set.id, { requestKey: randomUUID(), version: seen.version });
  expect(phone.status()).toBe(409);
  expect((await err(phone)).code).toBe('CONFLICT');
  const reloaded = await follow(request, lan(w).token, set.id);
  expect(reloaded.releaseState).toBe('upcoming');
  expect(ms(reloaded.releaseAt), 'điện thoại tải lại thấy giờ mới').toBe(ms(ten));
  expect(reloaded.version).not.toBe(seen.version);
  expect((await auditOf(request, lan(w).token, klass(w, '11A1').id)).map((x) => x.type)).toEqual(['RELEASE_TIME_CHANGE']);
});

test('@TC-S3E4-23 giáo viên khác gọi Mở ngay / Đổi giờ: NOT_FOUND, giờ mở không đổi, không thông báo, một bản ghi Từ chối mỗi (người gọi, loại đối tượng) mỗi giờ', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const before = await follow(request, lan(w).token, set.id);
  const a = await api.openNow(request, khoi(w).token, set.id);
  const b = await api.changeTime(request, khoi(w).token, set.id, { releaseAt: hours(30) });
  for (const r of [a, b]) {
    expect(r.status()).toBe(404);
    expect((await err(r)).code).toBe('NOT_FOUND');
  }
  const after = await follow(request, lan(w).token, set.id);
  expect(ms(after.releaseAt)).toBe(ms(before.releaseAt));
  expect(after.version).toBe(before.version);
  expect((await notesOf(request, who(w, 'Bình An').token)).map((n) => n.body)).toEqual(['Có bài mới: Bộ bài tuần 15']);
  expect(await auditOf(request, lan(w).token, klass(w, '11A1').id)).toEqual([]);
  // PO-76: một hàng Từ chối mỗi (người gọi, loại đối tượng) trong mỗi giờ, không phải mỗi yêu cầu
  const denied = await deniedOf(request, khoi(w).id);
  expect(denied.map((d) => d.type)).toEqual(['EARLY_RELEASE']);
  for (const d of denied) expect(d).toMatchObject({ actorCode: khoi(w).userCode, targetType: 'assignment_set', result: 'DENIED', bare: true });
});

test('@TC-S3E4-24 học sinh gọi Mở ngay: FORBIDDEN, lời giải vẫn khóa, không có EARLY_RELEASE', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const r = await api.openNow(request, who(w, 'Bình An').token, set.id);
  expect(r.status()).toBe(403);
  expect((await err(r)).code).toBe('FORBIDDEN');
  expect((await follow(request, lan(w).token, set.id)).releaseState).toBe('upcoming');
  expect(await auditOf(request, lan(w).token, klass(w, '11A1').id)).toEqual([]);
  const sol = await request.get(`${API_SETS_STUDENT}/${set.id}/items/1/solution`, { headers: bearer(who(w, 'Bình An').token) });
  expect(((await sol.json()) as { finalAnswer?: string | null }).finalAnswer ?? null).toBeNull();
});

test('@TC-S3E4-25 giờ điện thoại chạy nhanh 3 giờ gửi kèm bị bỏ qua: giờ mở là giờ máy chủ, không vào AuditEvent', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const clientTime = new Date(Date.now() + 3 * 3_600_000).toISOString();
  const t0 = Date.now();
  // BR-S3E5-34: khóa ngoài hợp đồng (releaseAt) bị từ chối, không mở bộ; giờ điện thoại đi qua trường clientTime (BR-S3E4-11)
  const bad = await api.openNow(request, lan(w).token, set.id, { requestKey: randomUUID(), clientTime, releaseAt: clientTime });
  expect(bad.status()).toBe(400);
  expect((await err(bad)).code).toBe('VALIDATION_FAILED');
  expect((await follow(request, lan(w).token, set.id)).releaseState).toBe('upcoming');
  const opened = await ok<Follow>(await api.openNow(request, lan(w).token, set.id, { requestKey: randomUUID(), clientTime }));
  const at = ms(opened.releaseAt)!;
  expect(Math.abs(at - t0), 'giờ mở = giờ máy chủ lúc xử lý').toBeLessThan(60_000);
  const r = await api.audit(request, lan(w).token, klass(w, '11A1').id);
  const [event] = (await ok<{ items: Array<{ after: string; at: string }> }>(r)).items;
  expect(ms(event!.after)).toBe(at);
  expect(Math.abs(ms(event!.at)! - t0)).toBeLessThan(60_000);
  expect(await r.text()).not.toContain(clientTime.slice(0, 13));
});

const API_SETS_STUDENT = SETS.replace('/teacher/', '/student/');
