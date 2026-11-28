import { expect, test } from '../../support/fixtures.ts';
import { bearer } from '../s3e1/s3e1.ts';
import {
  BC01_DUE,
  BC01_TITLE,
  EIGHT,
  GA,
  RUBRICS,
  STUDENT_GA,
  bc01Body,
  problemVersion,
  rb01,
  s5e4World,
  setup,
  submitImage,
  type Err,
  type Graded,
  type Rubric,
} from './s5e4.ts';

/** US-S5E4-02 — giao bài có chấm: đề, rubric, chế độ chấm, hạn nộp, nộp muộn, lịch công bố (máy chủ thật, AI giả lập). */

type StudentItem = { id: string; code: string; title: string; dueAt: string; allowLate: boolean };

async function studentItems(request: Parameters<typeof s5e4World>[0], token: string) {
  const r = await request.get(STUDENT_GA, { headers: bearer(token) });
  expect(r.status()).toBe(200);
  return { text: await r.text(), items: ((await r.json()) as { items: StudentItem[] }).items };
}

test('Giao bài BC-01 chế độ Chấm tay @TC-S5E4-07', async ({ request }) => {
  const { w, bc } = await setup(request);
  expect(bc.code).toBe('BC-01');
  expect(bc.title).toBe(BC01_TITLE);
  expect(bc.statusLabel).toBe('Đã giao');
  expect(bc.gradingMode).toBe('manual');
  expect(bc.gradingModeLabel).toBe('Chấm tay');
  expect(bc.dueAt).toBe(BC01_DUE);
  expect(bc.allowLate).toBe(true);
  expect(bc.releaseMode).toBe('on_return');
  // 8 em Đang học thấy bài trong "Bài cần nộp"; thân phản hồi không có rubric chi tiết, hướng dẫn chấm, lời giải
  for (const name of EIGHT) {
    const { text, items } = await studentItems(request, w.people[name]!.token);
    expect(items.map((i) => i.id), name).toEqual([bc.id]);
    expect(items[0]!.dueAt).toBe(BC01_DUE);
    for (const k of ['criteria', 'guide', 'rubric', 'maxPoints', 'solution', 'finalAnswer', 'steps', 'Sai hướng']) {
      expect(text, `${name}: ${k}`).not.toContain(k);
    }
  }
  // Văn Tú (Đã rời) không thấy
  expect((await studentItems(request, w.people['Văn Tú']!.token)).items).toHaveLength(0);
});

test('Kéo dài hạn nộp sau khi đã có bài nộp @TC-S5E4-08', async ({ request }) => {
  const { w, lan, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await submitImage(request, w, bc.id, an);
  const r = await request.patch(`${GA}/${bc.id}`, { headers: bearer(lan.token), data: { dueAt: '2026-12-12T13:00:00Z' } });
  expect(r.status(), await r.text()).toBe(200);
  const g = (await r.json()) as Graded;
  expect(g.dueAt).toBe('2026-12-12T13:00:00Z');
  expect(g.submissions).toBe(1);
  // hạn mới hiện cho học sinh
  const { items } = await studentItems(request, an.token);
  expect(items[0]!.dueAt).toBe('2026-12-12T13:00:00Z');
});

test('Chọn "AI đề xuất" khi lớp chưa đạt cổng bị từ chối @TC-S5E4-09', async ({ request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const rb = await rb01(request, lan);
  const pv = await problemVersion(request, lan);
  const r = await request.post(GA, { headers: bearer(lan.token), data: bc01Body(c11.id, rb.id, pv, { gradingMode: 'ai_proposal' }) });
  expect(r.status()).toBe(409);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('CONFLICT');
  expect(e.details?.[0]?.field).toBe('gradingMode');
  expect(e.details?.[0]?.message).toContain('lớp chưa đạt ngưỡng chạy song song');
  // không có bài nào được tạo
  const list = (await (await request.get(GA, { headers: bearer(lan.token) })).json()) as { items: unknown[] };
  expect(list.items).toHaveLength(0);
  // rubric chưa bị gắn bài nào
  const v1 = (await (await request.get(`${RUBRICS}/${rb.id}`, { headers: bearer(lan.token) })).json()) as Rubric;
  expect(v1.used).toBe(false);
});

test('Rút ngắn hạn hoặc đổi rubric sau khi có bài nộp bị từ chối @TC-S5E4-10', async ({ request }) => {
  const { w, lan, rb, bc } = await setup(request);
  await submitImage(request, w, bc.id, w.people['Bình An']!);
  // 1) hạn sớm hơn 1 ngày
  const r1 = await request.patch(`${GA}/${bc.id}`, { headers: bearer(lan.token), data: { dueAt: '2026-12-10T13:00:00Z' } });
  expect(r1.status()).toBe(409);
  expect(((await r1.json()) as Err).code).toBe('CONFLICT');
  // 2) RB-01 v2 (sửa rubric đã giao tạo v2) rồi đổi bài sang v2
  const crit = rb.criteria.map((c) => ({ name: c.name, maxPoints: c.name === 'Trình bày' ? 0.5 : c.name === 'Hình' ? 1.5 : c.maxPoints }));
  const v2 = await request.put(`${RUBRICS}/${rb.id}`, { headers: bearer(lan.token), data: { criteria: crit } });
  expect(((await v2.json()) as Rubric).version).toBe(2);
  const r2 = await request.patch(`${GA}/${bc.id}`, { headers: bearer(lan.token), data: { rubricVersion: 2 } });
  expect(r2.status()).toBe(409);
  expect(((await r2.json()) as Err).code).toBe('CONFLICT');
  // bài giữ nguyên
  const g = (await (await request.get(`${GA}/${bc.id}`, { headers: bearer(lan.token) })).json()) as Graded;
  expect(g.dueAt).toBe(BC01_DUE);
  expect(g.rubric.version).toBe(1);
});

test('Không giao bài có chấm cho lớp của người khác @TC-S5E4-11', async ({ request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const rb = await rb01(request, lan);
  const pv = await problemVersion(request, lan);
  const r = await request.post(GA, { headers: bearer(lan.token), data: bc01Body(w.classes['10B']!.id, rb.id, pv) });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as Err).code).toBe('NOT_FOUND');
  const list = (await (await request.get(GA, { headers: bearer(lan.token) })).json()) as { items: unknown[] };
  expect(list.items).toHaveLength(0);
});
