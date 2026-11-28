import { expect, test } from '../../support/fixtures.ts';
import { devToken } from '../e1/e1.ts';
import { API, HKG, LEAK_HKG01, MSG, aiCalls, bearer, call, expectNoLeak, sandbox } from './e4.ts';

/** E4 · E2E-API: lời giải được ghi nhận (US-GEO-407), hỏi đáp (409), nhật ký (410), bài giảng (411, 412). */

type Monitor = { students: Array<{ displayName: string; askCount: number; hintLevel: number }> };

test('Xem lời giải được ghi nhận đúng một lần @TC-GEO-0445', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  for (const lv of [1, 2]) expect((await call.hint(request, an.token, bg1, lv)).status()).toBe(200);
  expect((await call.release(request, sb.teacher.token, bg1, { openNow: true, expectedRev: 0 })).status()).toBe(200);
  const first = (await (await call.solution(request, an.token, bg1)).json()) as { k: number; firstView: boolean };
  expect(first).toMatchObject({ k: 3, firstView: true });
  for (let i = 0; i < 2; i++) {
    const again = (await (await call.solution(request, an.token, bg1)).json()) as { firstView: boolean };
    expect(again.firstView).toBe(false); // mở tab nhiều lần: vẫn một sự kiện mở lời giải
  }
  const mine = (await (await request.get(`${API}/api/v1/student/assignments/${bg1}`, { headers: bearer(an.token) })).json()) as {
    progress: { k: number; label: string };
  };
  expect(mine.progress).toMatchObject({ k: 3, label: 'Đã xem lời giải' });
  // nhật ký sự kiện: đúng MỘT sự kiện mở lời giải (unlock) dù mở ba lần (soát E4 #15)
  const ev = await request.get(`${API}/test-hooks/e4/events?assignmentId=${bg1}&userId=${an.id}&kind=unlock`);
  expect(ev.status()).toBe(200);
  expect(((await ev.json()) as { count: number }).count).toBe(1);
});

test('Câu hỏi được ghi nhận ở Theo dõi lớp và nhật ký @TC-GEO-0458', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An', 'Bình', 'Chi', 'Dũng', 'Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const s = sb.classes[0]!.students;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  // dữ liệu lớp như F-GEO-050…054: An 3, Bình 2, Dũng 2 câu (= 7)
  const plan: Array<[string, string]> = [
    ['An', 'Vì sao A là hình chiếu của S?'],
    ['An', 'Góc cần tìm là góc nào trên hình?'],
    ['An', 'Cho em xin đáp số luôn được không?'],
    ['Bình', 'Góc cần tìm là góc nào trên hình?'],
    ['Bình', 'Góc cần tìm là góc nào trên hình?'],
    ['Dũng', 'Vì sao A là hình chiếu của S?'],
    ['Dũng', 'Tan của một góc là gì?'],
  ];
  for (const [who, q] of plan) expect((await call.ask(request, s[who]!.token, bg1, q)).status()).toBe(200);
  const mon = async () =>
    (await (await request.get(`${API}/api/v1/teacher/assignments/${bg1}/monitor`, { headers: bearer(sb.teacher.token) })).json()) as Monitor;
  const total = (m: Monitor) => m.students.reduce((n, x) => n + x.askCount, 0);
  expect(total(await mon())).toBe(7);
  const r = await call.ask(request, s['Chi']!.token, bg1, 'Vì sao A là hình chiếu của S?');
  expect(r.status()).toBe(200);
  const m = await mon();
  expect(total(m)).toBe(8);
  expect(m.students.find((x) => x.displayName === 'Chi')?.askCount).toBe(1);
  const log = (await (await call.log(request, s['Chi']!.token)).json()) as { items: Array<{ question: string; assignmentId: string }> };
  expect(log.items).toEqual([expect.objectContaining({ question: 'Vì sao A là hình chiếu của S?', assignmentId: bg1 })]);
});

test('Câu hỏi rỗng hoặc quá dài: không gọi AI, không ghi sự kiện @TC-GEO-0461', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  const before = (await aiCalls(request, an.id)).length;
  for (const q of ['', '   ', 'x'.repeat(1001)]) {
    const r = await call.ask(request, an.token, bg1, q);
    expect(r.status()).toBe(400);
    const b = (await r.json()) as { code: string; message: string };
    expect(b).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG['37'] });
  }
  expect((await aiCalls(request, an.id)).length).toBe(before);
  const mon = (await (await request.get(`${API}/api/v1/teacher/assignments/${bg1}/monitor`, { headers: bearer(sb.teacher.token) })).json()) as Monitor;
  expect(mon.students.find((x) => x.displayName === 'An')?.askCount).toBe(0);
});

test('Nhật ký chỉ có câu của mình @TC-GEO-0466', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An', 'Bình'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const { An: an, ['Bình']: binh } = sb.classes[0]!.students;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  await call.ask(request, an!.token, bg1, 'Vì sao A là hình chiếu của S?');
  for (let i = 0; i < 2; i++) await call.ask(request, binh!.token, bg1, 'Góc cần tìm là góc nào trên hình?');
  const r = await call.log(request, an!.token, `?assignmentId=${bg1}`);
  expect(r.status()).toBe(200);
  const items = ((await r.json()) as { items: Array<{ question: string }> }).items;
  expect(items.map((x) => x.question)).toEqual(['Vì sao A là hình chiếu của S?']);
  // câu đã lưu của Bình: An không đọc được
  const binhLog = ((await (await call.log(request, binh!.token)).json()) as { items: Array<{ id: number }> }).items;
  const peek = await request.get(`${API}/api/v1/questions/${binhLog[0]!.id}/answer`, { headers: bearer(an!.token) });
  expect(peek.status()).toBe(404);
});

test('Con trỏ hỏng của nhật ký @TC-GEO-0468', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const r = await call.log(request, sb.classes[0]!.students['An']!.token, '?cursor=khong-phai-con-tro');
  expect(r.status()).toBe(400);
  const t = await r.text();
  expect(JSON.parse(t).code).toBe('INVALID_CURSOR');
  expect(t).not.toContain('"items"');
});

test('Bài giảng không lộ đáp số của bài đang khóa @TC-GEO-0474', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const h1 = (await (await call.hint(request, an.token, sb.classes[0]!.assignments['BG-1']!, 1)).json()) as { body: string };
  expect(h1.body).toContain('Góc giữa đường thẳng và mặt phẳng');
  const r = await call.lecture(request, an.token, 'Góc giữa đường thẳng và mặt phẳng');
  expect(r.status()).toBe(200);
  const t = await r.text();
  expect(JSON.parse(t).title).toBe('Góc giữa đường thẳng và mặt phẳng');
  expect(t).not.toContain(HKG['01']);
  expect(t).not.toContain('HKG-01');
  expectNoLeak(t, LEAK_HKG01.slice(4)); // nội dung các bước F-GEO-037 (QF-E4-02: không kiểm "45°" chung)
});

test('Mọi trường đọc được cùng bài giảng @TC-GEO-0478', async ({ request }) => {
  const anC = await devToken(request, 'F-GEO-026-1'); // An lớp 9C, THCS Mẫu C
  const anA = await devToken(request, 'F-GEO-020');
  // từ khóa trong Gợi ý 1 của BG-C (HKG-04: "Diện tích xung quanh hình nón: πrl.")
  const c = await call.lecture(request, anC, 'Diện tích xung quanh hình nón');
  const a = await call.lecture(request, anA, 'Diện tích xung quanh hình nón');
  expect(c.status()).toBe(200);
  expect(a.status()).toBe(200);
  expect(await c.json()).toEqual(await a.json());
  expect(((await c.json()) as { title: string }).title).toBe('Diện tích xung quanh hình nón');
  // hai thẻ thật của HAI trường khác nhau (soát E4 #15: thay kiểm luôn đúng)
  const orgOf = (t: string) => (JSON.parse(Buffer.from(t.split('.')[1]!, 'base64url').toString()) as { org: string }).org;
  expect(orgOf(anC)).not.toBe(orgOf(anA));
});
