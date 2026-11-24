import { test, expect } from '../../support/fixtures.ts';
import {
  API,
  HINT1_HKG01,
  HINT2_HKG01,
  LEAK_HKG01,
  LEAK_HKG03,
  LEAK_HKG04,
  MSG,
  PV,
  api,
  asg,
  bearer,
  cls,
  expectNoLeak,
  stu,
  twoClasses,
} from './e6.ts';

/** E6 — máy chủ khóa lời giải và đáp số trước giờ mở, ở mọi kênh (US-GEO-606-FC2, 607, 608, 612-FC2). */

test('Danh sách "Bài của em" không mang lời giải @TC-GEO-0646', async ({ request }) => {
  const sb = await twoClasses(request, [
    { key: 'BG-1', code: 'HKG-01' },
    { key: 'BG-3', code: 'HKG-03', release: 'open' },
  ]);
  const r = await api.mine(request, stu(sb, 'An').token);
  expect(r.status()).toBe(200);
  const text = await r.text();
  expectNoLeak(text, [...LEAK_HKG01, ...LEAK_HKG03]);
  const items = ((await r.json()) as { items: Array<Record<string, unknown>> }).items;
  for (const it of items) {
    expect(Object.keys(it).sort()).toEqual(
      ['allowHint', 'allowQa', 'className', 'id', 'problemCode', 'progress', 'releaseState', 'solidKind', 'statement', 'title', 'topic', 'verifyStatus', ...('releaseAt' in it ? ['releaseAt'] : [])].sort(),
    );
  }
});

test('Con trỏ hỏng: INVALID_CURSOR @TC-GEO-0648', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const r = await api.mine(request, stu(sb, 'An').token, '?cursor=khong-phai-con-tro');
  expect(r.status()).toBe(400);
  expect(await r.json()).toMatchObject({ code: 'INVALID_CURSOR' });
  expect(MSG['05']).toBe('Danh sách đã thay đổi. Vui lòng tải lại từ đầu.');
});

test('Dữ liệu của màn xem trước không chứa lời giải @TC-GEO-0653', async ({ request }) => {
  const sb = await twoClasses(request);
  const t = sb.teacher.token;
  const pv = PV('HKG-01');
  let all = '';
  all += await (await api.preview(request, t, pv)).text();
  const page = await request.get(`${API}/api/v1/teacher/problems/${pv}`, { headers: bearer(t) });
  expect(page.status()).toBe(200);
  const body = await page.text();
  all += body;
  // hai lần xin gợi ý: gợi ý có sẵn trong dữ liệu trang (không gọi đường gợi ý của học sinh, không ghi sự kiện)
  expect(body).toContain(HINT1_HKG01);
  expect(body).toContain(HINT2_HKG01);
  all += await (await api.preview(request, t, pv, `?classId=${cls(sb, '11A1').id}`)).text();
  expectNoLeak(all, LEAK_HKG01);
});

test('Hỏi đáp trong xem trước cũng không lộ, không ghi vào Theo dõi lớp @TC-GEO-0654', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const t = sb.teacher.token;
  const r = await api.previewAsk(request, t, PV('HKG-01'), 'Đáp số bài này là bao nhiêu?');
  expect(r.status()).toBe(200);
  const a = (await r.json()) as { reply: string };
  expect(a.reply.startsWith(MSG['35'])).toBe(true);
  expectNoLeak(await r.text(), LEAK_HKG01);
  const m = (await (await api.monitor(request, t, asg(sb, 'BG-1'))).json()) as { questions: unknown[] };
  expect(m.questions).toHaveLength(0);
});

test('Chưa đặt giờ: gửi thẳng yêu cầu lời giải bị FORBIDDEN @TC-GEO-0660', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }]);
  const r = await api.solution(request, stu(sb, 'An').token, asg(sb, 'BG-2'));
  expect(r.status()).toBe(403);
  const text = await r.text();
  expect(text).not.toContain('B′D′ = a');
  expect(text).not.toContain('finalAnswer');
});

test('Dữ liệu trang Làm bài của bài đang khóa không chứa đáp số @TC-GEO-0662', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const an = stu(sb, 'An').token;
  const id = asg(sb, 'BG-1');
  let all = '';
  for (const p of ['', '/scene', '/help', '/lock']) {
    const r = await request.get(`${API}/api/v1/student/assignments/${id}${p}`, { headers: bearer(an) });
    expect(r.status()).toBe(200);
    all += await r.text();
  }
  for (const lv of [1, 2]) all += await (await api.hint(request, an, id, lv)).text();
  all += await (await api.openSolution(request, an, id)).text();
  expect(all).toContain('"releaseState":"upcoming"');
  expect(all).toContain('ang:S-D-A'); // đại lượng đề hỏi chỉ nêu đối tượng, không giá trị
  expectNoLeak(all, LEAK_HKG01);
});

test('Lời giải đã mở cho lớp này không lọt sang lớp còn khóa @TC-GEO-0664', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }], [{ key: 'BG-3-9B', code: 'HKG-03', release: 'unset' }]);
  expect((await api.solution(request, stu(sb, 'An').token, asg(sb, 'BG-3'))).status()).toBe(200);
  const r = await api.solution(request, stu(sb, 'Khánh').token, asg(sb, 'BG-3-9B'));
  expect(r.status()).toBe(403);
  expect(r.headers()['cache-control']).toContain('no-store');
  expectNoLeak(await r.text(), LEAK_HKG03);
});

test('Học sinh lớp khác gọi lời giải của bài đã mở: NOT_FOUND @TC-GEO-0665', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  const bg3 = asg(sb, 'BG-3');
  expect((await api.solution(request, stu(sb, 'An').token, bg3)).status()).toBe(200); // quyết định "được xem" của An
  const r = await api.solution(request, stu(sb, 'Khánh').token, bg3);
  expect(r.status()).toBe(404);
  expectNoLeak(await r.text(), LEAK_HKG03);
});

test('Trước giờ mở trang không giữ sẵn lời giải; sau R mới có @TC-GEO-0692', async ({ request }) => {
  const sb = await twoClasses(request, [], [{ key: 'BG-4', code: 'HKG-04', release: '6' }]);
  const kh = stu(sb, 'Khánh').token;
  const id = asg(sb, 'BG-4');
  let before = '';
  for (;;) {
    const l = await api.lock(request, kh, id);
    const lj = (await l.json()) as { locked: boolean; serverNow: string; releaseAt: string };
    if (!lj.locked) break;
    before += JSON.stringify(lj);
    before += await (await api.solution(request, kh, id)).text();
    before += await (await request.get(`${API}/api/v1/student/assignments/${id}`, { headers: bearer(kh) })).text();
    await new Promise((r) => setTimeout(r, 2_100));
  }
  expectNoLeak(before, LEAK_HKG04);
  const s = await api.solution(request, kh, id);
  expect(s.status()).toBe(200);
  expect(await s.text()).toContain('15π');
});

test('Phản hồi lời giải không cho lưu đệm @TC-GEO-0698', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  const an = stu(sb, 'An').token;
  const r = await api.solution(request, an, asg(sb, 'BG-3'));
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const o = await api.openSolution(request, an, asg(sb, 'BG-3'));
  expect(o.headers()['cache-control']).toContain('no-store');
});

test('Vượt giới hạn yêu cầu lời giải thì bị chặn, dùng bình thường thì không @TC-GEO-0861', async ({ request }) => {
  test.setTimeout(90_000);
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  const an = stu(sb, 'An').token;
  const id = asg(sb, 'BG-3');
  for (let i = 0; i < 10; i++) {
    const r = await api.solution(request, an, id);
    expect(r.status(), `lượt ${i + 1}`).toBe(200);
    await new Promise((x) => setTimeout(x, 3_000));
  }
  await new Promise((x) => setTimeout(x, 10_000));
  const codes: number[] = [];
  let last;
  for (let i = 0; i < 6; i++) {
    last = await api.solution(request, an, id);
    codes.push(last.status());
  }
  expect(codes).toEqual([200, 200, 200, 200, 200, 429]);
  expect(await last!.json()).toMatchObject({ code: 'RATE_LIMITED' });
  expect(Number(last!.headers()['retry-after'])).toBeGreaterThan(0);
  await new Promise((x) => setTimeout(x, 10_500));
  expect((await api.solution(request, an, id)).status()).toBe(200);
});

test('Xem trước bài đã Đã mở không tải lời giải @TC-GEO-0872', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  const t = sb.teacher.token;
  const r = await api.preview(request, t, PV('HKG-03'), `?classId=${cls(sb, '11A1').id}`);
  expect(r.status()).toBe(200);
  const p = (await r.json()) as { lock: { state: string } };
  expect(p.lock.state).toBe('open');
  let all = await r.text();
  all += await (await request.get(`${API}/api/v1/teacher/problems/${PV('HKG-03')}`, { headers: bearer(t) })).text();
  expectNoLeak(all, LEAK_HKG03);
});

test('Tọa độ đỉnh trong dữ liệu trang là dữ kiện, không tính là lộ @TC-GEO-0873', async ({ request }) => {
  const sb = await twoClasses(request, [{ key: 'BG-1', code: 'HKG-01' }]);
  const an = stu(sb, 'An').token;
  const id = asg(sb, 'BG-1');
  let all = '';
  for (const p of ['', '/scene', '/help', '/lock']) {
    all += await (await request.get(`${API}/api/v1/student/assignments/${id}${p}`, { headers: bearer(an) })).text();
  }
  const scene = JSON.parse(await (await request.get(`${API}/api/v1/student/assignments/${id}/scene`, { headers: bearer(an) })).text()) as {
    points?: Array<{ name: string; x: number; y: number; z: number }>;
  };
  expect(JSON.stringify(scene)).toMatch(/"S"/);
  // phép đếm lộ M-GEO-06 chỉ đếm giá trị đáp số và bước giải, không đếm tọa độ
  const leaks = LEAK_HKG01.filter((s) => all.includes(s)).length;
  expect(leaks).toBe(0);
});
