import { test, expect } from '../../support/fixtures.ts';
import { API, MSG, PV, api, asg, bearer, cls, msg42, sandbox, stu, twoClasses } from './e6.ts';

/** E6 — giao bài, công tắc, giờ mở, đổi giờ qua API (US-GEO-602, 603, 604, 605). Lớp riêng mỗi ca (móc e6/sandbox). */

test('Học sinh vào lớp sau vẫn thấy bài @TC-GEO-0608', async ({ request }) => {
  const sb = await sandbox(request, [{ name: '11A1', students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const c = cls(sb, '11A1');
  const r = await request.post(`${API}/api/v1/auth/student`, { data: { joinCode: c.joinCode, displayName: 'Yến' } });
  expect(r.status(), await r.text()).toBe(200);
  const yen = ((await r.json()) as { token: string }).token;
  const list = (await (await api.mine(request, yen)).json()) as { items: Array<{ id: string; problemCode: string }> };
  expect(list.items.map((x) => x.id)).toContain(asg(sb, 'BG-1'));
});

test('Hai yêu cầu giao cùng đề gửi đồng thời: đúng một bài @TC-GEO-0696', async ({ request }) => {
  const sb = await twoClasses(request);
  const b9 = cls(sb, '9B').id;
  const [a, b] = await Promise.all([
    api.assign(request, sb.teacher.token, { classId: b9, problemVersionId: PV('HKG-06') }),
    api.assign(request, sb.teacher.token, { classId: b9, problemVersionId: PV('HKG-06') }),
  ]);
  const st = [a.status(), b.status()].sort();
  expect(st).toEqual([201, 409]);
  const conflict = a.status() === 409 ? a : b;
  expect(await conflict.json()).toMatchObject({ code: 'CONFLICT', message: msg42('9B') });
  const khanh = stu(sb, 'Khánh');
  const list = (await (await api.mine(request, khanh.token)).json()) as { items: Array<{ problemCode: string }> };
  expect(list.items.filter((x) => x.problemCode === 'HKG-06')).toHaveLength(1);
});

test('Cùng đề nhưng lời giải đã có phiên bản mới vẫn là cùng đề @TC-GEO-0865', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const v = await request.post(`${API}/test-hooks/e6/new-version?code=HKG-01`);
  expect(v.status()).toBe(200);
  const v2 = ((await v.json()) as { versionId: string }).versionId;
  expect(v2).not.toBe(PV('HKG-01'));
  const r = await api.assign(request, sb.teacher.token, { classId: cls(sb, '11A1').id, problemVersionId: v2 });
  expect(r.status()).toBe(409);
  expect(await r.json()).toMatchObject({ code: 'CONFLICT', message: msg42('11A1') });
  const an = stu(sb, 'An');
  const list = (await (await api.mine(request, an.token)).json()) as { items: Array<{ problemCode: string; id: string }> };
  expect(list.items.filter((x) => x.problemCode === 'HKG-01').map((x) => x.id)).toEqual([asg(sb, 'BG-1')]);
});

test('Giờ mở độc lập với công tắc @TC-GEO-0616', async ({ request }) => {
  const sb = await twoClasses(request, [], [{ key: 'BG-5', code: 'HKG-05', release: 'unset', allowHint: false, allowQa: false }]);
  const bg5 = asg(sb, 'BG-5');
  const khanh = stu(sb, 'Khánh');
  const v = (await (await api.releaseView(request, sb.teacher.token, bg5)).json()) as { releaseRev: number };
  expect((await api.release(request, sb.teacher.token, bg5, { openNow: true, expectedRev: v.releaseRev })).status()).toBe(200);
  const s = await api.solution(request, khanh.token, bg5);
  expect(s.status()).toBe(200);
  expect(((await s.json()) as { finalAnswer: string }).finalAnswer).toContain('112π cm²');
  expect((await api.hint(request, khanh.token, bg5, 1)).status()).toBe(403);
  const q = await api.ask(request, khanh.token, bg5, 'Tan của một góc là gì?');
  expect(q.status()).toBe(403);
  expect(((await q.json()) as { message: string }).message).toBe(MSG['33']);
});

test('Đồng hồ máy giáo viên sai: máy chủ so bằng giờ máy chủ @TC-GEO-0625', async ({ request }) => {
  const sb = await twoClasses(request);
  // máy giáo viên chạy nhanh 1 giờ: giờ T0 + 30 phút là "quá khứ" với máy đó nhưng còn ở tương lai với máy chủ
  const at = new Date(Date.now() + 30 * 60_000).toISOString();
  const r = await api.assign(request, sb.teacher.token, { classId: cls(sb, '11A1').id, problemVersionId: PV('HKG-06'), releaseAt: at });
  expect(r.status(), await r.text()).toBe(201);
  const id = ((await r.json()) as { id: string }).id;
  const v = (await (await api.releaseView(request, sb.teacher.token, id)).json()) as { releaseState: string };
  expect(v.releaseState).toBe('upcoming');
});

test('Dời giờ muộn hơn thì giờ cũ không còn hiệu lực @TC-GEO-0633', async ({ request }) => {
  const sb = await twoClasses(request, [], [{ key: 'BG-4', code: 'HKG-04', release: '4' }]);
  const bg4 = asg(sb, 'BG-4');
  const khanh = stu(sb, 'Khánh');
  const v = (await (await api.releaseView(request, sb.teacher.token, bg4)).json()) as { releaseRev: number; serverNow: string };
  const later = new Date(new Date(v.serverNow).getTime() + 9_000).toISOString();
  const ch = await api.release(request, sb.teacher.token, bg4, { releaseAt: later, expectedRev: v.releaseRev });
  expect(ch.status(), await ch.text()).toBe(200);
  await new Promise((r) => setTimeout(r, 4_500)); // qua R cũ
  expect((await api.solution(request, khanh.token, bg4)).status()).toBe(403);
  await expect
    .poll(async () => (await api.solution(request, khanh.token, bg4)).status(), { timeout: 15_000, intervals: [2_100] })
    .toBe(200);
});

test('Đổi giờ về quá khứ bị từ chối, giờ cũ giữ nguyên @TC-GEO-0636', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const bg1 = asg(sb, 'BG-1');
  const before = (await (await api.releaseView(request, sb.teacher.token, bg1)).json()) as { releaseAt: string; releaseRev: number; serverNow: string };
  const past = new Date(new Date(before.serverNow).getTime() - 60_000).toISOString();
  const r = await api.release(request, sb.teacher.token, bg1, { releaseAt: past, expectedRev: before.releaseRev });
  expect(r.status()).toBe(400);
  expect(await r.json()).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG['43'] });
  const after = (await (await api.releaseView(request, sb.teacher.token, bg1)).json()) as { releaseAt: string };
  expect(after.releaseAt).toBe(before.releaseAt);
});

test('Hai lần đổi chồng nhau: lần sau dựa trên dữ liệu cũ bị từ chối @TC-GEO-0637', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const bg1 = asg(sb, 'BG-1');
  const t = sb.teacher.token;
  const seen = (await (await api.releaseView(request, t, bg1)).json()) as { releaseAt: string; releaseRev: number };
  const r0 = new Date(seen.releaseAt).getTime();
  const a = await api.release(request, t, bg1, { releaseAt: new Date(r0 + 30 * 60_000).toISOString(), expectedRev: seen.releaseRev });
  expect(a.status()).toBe(200);
  const b = await api.release(request, t, bg1, { releaseAt: new Date(r0 - 30 * 60_000).toISOString(), expectedRev: seen.releaseRev });
  expect(b.status()).toBe(409);
  expect(await b.json()).toMatchObject({ code: 'CONFLICT', message: MSG['49'] });
  const now = (await (await api.releaseView(request, t, bg1)).json()) as { releaseAt: string };
  expect(new Date(now.releaseAt).getTime()).toBe(r0 + 30 * 60_000);
  const log = (await (await api.releaseLog(request, t, bg1)).json()) as { items: unknown[] };
  expect(log.items).toHaveLength(1);
});
