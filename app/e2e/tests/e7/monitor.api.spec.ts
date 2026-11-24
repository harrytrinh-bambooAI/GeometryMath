import { expect, test } from '../../support/fixtures.ts';
import { LEAK_HKG01, call } from '../e4/e4.ts';
import { API, asg, bearer, box, freshMonitor, golden, monitor, row, st } from './e7.ts';

/** E7 — Theo dõi lớp qua API: quyền, dữ liệu trả về, không lẫn câu hỏi bài khác/lớp khác (US-GEO-701, 702). */

test('Học sinh gọi Theo dõi lớp @TC-GEO-0710', async ({ request }) => {
  const b = await box(request, golden());
  const r = await request.get(`${API}/api/v1/teacher/assignments/${asg(b, 'BG-1')}/monitor`, { headers: bearer(st(b, 'An').token) });
  expect(r.status()).toBe(403);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('FORBIDDEN');
  for (const s of ['Bình', 'Chi', 'Dũng', 'Hà', 'Vì sao A', 'phút']) expect(text).not.toContain(s);
});

test('Trang không mang lời giải @TC-GEO-0711', async ({ request }) => {
  const b = await box(request, golden());
  const r = await request.get(`${API}/api/v1/teacher/assignments/${asg(b, 'BG-1')}/monitor`, { headers: bearer(b.teacher.token) });
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const text = await r.text();
  expect(JSON.parse(text).releaseState).toBe('upcoming');
  for (const s of LEAK_HKG01) expect(text, `không chứa "${s}"`).not.toContain(s);
});

test('Không lẫn câu hỏi của bài khác, lớp khác @TC-GEO-0719', async ({ request }) => {
  const b = await box(
    request,
    golden({
      classes: [{ name: '9B', level: 'thcs', students: ['Khánh'], assignments: [{ key: 'BG-4', code: 'HKG-04' }] }],
      questions: [
        { assignment: 'BG-3', student: 'An', text: 'Câu của An trên BG-3?', minutesAgo: 1 },
        { assignment: 'BG-4', student: 'Khánh', text: 'Câu của Khánh trên BG-4?', minutesAgo: 1 },
      ],
    }),
  );
  const m = await monitor(request, b.teacher.token, asg(b, 'BG-1'));
  expect(m.questionCount).toBe(7);
  const all = JSON.stringify(m);
  expect(all).not.toContain('Câu của An trên BG-3?');
  expect(all).not.toContain('Câu của Khánh trên BG-4?');
  expect(m.questionGroups.map((g) => g.count).reduce((a, x) => a + x, 0)).toBe(7);
});

test('Gửi lại câu hỏi sau khi hết thời gian chờ không đếm hai lần @TC-GEO-0742', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const an = st(b, 'An');
  const before = await monitor(request, b.teacher.token, id);
  expect(before.questionCount).toBe(7);
  const rq = crypto.randomUUID();
  const q = 'Vì sao A là hình chiếu của S?';
  const r1 = await call.ask(request, an.token, id, q, rq);
  expect([200, 504]).toContain(r1.status());
  const r2 = await call.ask(request, an.token, id, q, rq); // thiết bị gửi lại đúng yêu cầu đó (cùng mã yêu cầu)
  expect(r2.status(), await r2.text()).toBe(200);
  const m = await freshMonitor(request, b.teacher.token, id);
  expect(m.questionCount).toBe(8);
  expect(row(m, 'An').askCount).toBe(4);
  expect(m.questionGroups.find((g) => g.question === q)!.count).toBe(3);
});
