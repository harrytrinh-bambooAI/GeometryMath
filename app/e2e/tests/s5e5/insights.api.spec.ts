import { expect, test } from '../../support/fixtures.ts';
import { aiCalls, bearer, hook } from '../s3e1/s3e1.ts';
import { GA, rb01, problemVersion, s5e4World } from '../s5e4/s5e4.ts';
import { tl01 } from '../s5e3/kb.ts';
import { EIGHT, switches } from './s5e5.ts';
import { MSG, SIX, bank, kcBank, errorPrompts, errorRuns, graded, suggestions, summary, summaryUrl, type Summary } from './insights.ts';

/**
 * US-S5E5-02 (tổng hợp lỗi sai của lớp, COP-02) và US-S5E5-03 ("Nên nhắc lại trên lớp") — chỉ API, máy chủ thật, AI giả
 * lập. Điểm duyệt, đề xuất AI và loại lỗi đi đường chấm thật (S5-E4/E6); không móc ghi điểm.
 */

const noNames = (s: Summary, codes: string[]) => {
  const text = JSON.stringify(s.insights);
  for (const n of [...EIGHT, 'Văn Tú', 'HS-']) expect(text).not.toContain(n);
  for (const c of codes) expect(text).not.toContain(c);
};

test('Tổng hợp BC-01 với 6 bài đã duyệt @TC-S5E5-09', async ({ request }) => {
  const c = await graded(request);
  const s = await summary(request, c.lan, c.bc.id);
  expect(s.status).toBe('ok');
  expect(s.approved).toBe(6);
  expect(s.mode).toBe('insight');
  const steps = s.raw!.criteria.find((x) => x.name === 'Các bước')!;
  expect([steps.lost, steps.works]).toEqual([4, 6]);
  const labels = Object.fromEntries(s.raw!.labels.map((l) => [l.label, l.works]));
  expect(labels).toMatchObject({ 'sai-tinh-toan': 3, 'sai-hinh-dung': 1, 'sai-dinh-li': 0, 'nham-dieu-kien': 0 });
  expect(s.insights).toHaveLength(2);
  for (const i of s.insights) {
    expect(i.evidence.length).toBeGreaterThan(0);
    for (const e of i.evidence) {
      expect(e.label).toBe(`Bài ${e.work}`);
      expect(c.subs).toContain(e.submissionId);
    }
  }
  expect(s.insights[0]!.label).toBe('sai-tinh-toan');
  expect(s.insights[0]!.evidence.map((e) => e.work)).toEqual([2, 3, 4]);
  noNames(s, Object.values(c.w.people).map((p) => p.id));
});

test('Nhận xét của giáo viên có tên học sinh được gỡ trước khi gửi AI @TC-S5E5-11', async ({ request }) => {
  const works = SIX.map((w) => ({ ...w }));
  works[1] = { ...works[1]!, comment: 'Chi Mai nhầm SB, xem lại' };
  works[5] = { name: 'Minh Quân', reply: 's5e6-thieu', pts: [2, 1, 2, 1, 1], comment: 'minh quan thieu buoc dung AH' };
  const c = await graded(request, works);
  const before = (await errorPrompts(request, c.lan.id)).length;
  const s = await summary(request, c.lan, c.bc.id);
  expect(s.mode).toBe('insight');
  const sent = (await errorPrompts(request, c.lan.id)).slice(before);
  expect(sent).toHaveLength(1);
  const p = sent[0]!;
  expect(p).toContain('[HS] nhầm SB');
  expect(p).toContain('[HS] thieu buoc dung AH');
  expect(p).not.toContain('Chi Mai');
  expect(p.toLowerCase()).not.toContain('minh quan');
  expect(p).not.toMatch(/HS-[A-Z0-9]{4}/);
  for (const person of Object.values(c.w.people)) expect(p).not.toContain(person.id);
});

test('Dưới 5 bài thì không tổng hợp @TC-S5E5-12', async ({ request }) => {
  const c = await graded(request, SIX.slice(0, 4));
  const calls = await aiCalls(request, c.lan.id);
  const prompts = (await errorPrompts(request, c.lan.id)).length;
  const s = await summary(request, c.lan, c.bc.id);
  expect(s.status).toBe('too_few');
  expect(s.approved).toBe(4);
  expect(s.message).toBe(MSG.s57);
  expect(s.raw ?? null).toBeNull();
  expect(s.insights).toEqual([]);
  expect(await aiCalls(request, c.lan.id)).toBe(calls); // không gọi AI
  expect(await errorPrompts(request, c.lan.id)).toHaveLength(prompts);
});

test('Nhận định có tên học sinh hoặc phần trăm bị loại @TC-S5E5-13', async ({ request }) => {
  const c = await graded(request);
  await hook(request, 'ai', { userId: c.lan.id, scenario: 's5e5-errors-name' });
  try {
    const s = await summary(request, c.lan, c.bc.id);
    expect(s.mode).toBe('insight');
    const text = JSON.stringify(s.insights);
    expect(text).not.toContain('Chi Mai');
    expect(text).not.toContain('%');
    expect(s.insights).toHaveLength(2); // nhận định hỏng bị loại, hai nhận định còn lại giữ
    expect(s.raw!.criteria.length).toBe(5); // số liệu thô vẫn hiện
    expect(s.check).toBe('loại 1 nhận định');
    const runs = await errorRuns(request, c.w.orgId, c.bc.id);
    expect(runs[0]).toMatchObject({ mode: 'insight', check: 'loại 1 nhận định', removed: 1 });
  } finally {
    await hook(request, 'ai', { userId: c.lan.id, scenario: '' });
  }
});

test('Lớp tắt trợ lý AI thì chỉ có số liệu thô @TC-S5E5-14', async ({ request }) => {
  const c = await graded(request);
  const off = await request.put(switches(c.c11.id), {
    headers: bearer(c.lan.token),
    data: { assistant: false },
  });
  expect(off.status(), await off.text()).toBe(200);
  const calls = await aiCalls(request, c.lan.id);
  const s = await summary(request, c.lan, c.bc.id);
  expect(s.mode).toBe('ai_off');
  expect(s.message).toBe(MSG.s58);
  expect(s.raw!.criteria.find((x) => x.name === 'Các bước')!.lost).toBe(4);
  expect(s.insights).toEqual([]);
  expect(await aiCalls(request, c.lan.id)).toBe(calls); // không có dòng AiCallLog mới
});

test('Học sinh không xem được tổng hợp lỗi @TC-S5E5-15', async ({ request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const rb = await rb01(request, lan);
  const pv = await problemVersion(request, lan);
  const r = await request.post(GA, {
    headers: bearer(lan.token),
    data: { title: 'BC-01', classId: w.classes['11A1']!.id, problemVersionId: pv, rubricId: rb.id, dueAt: '2026-12-11T13:00:00Z', allowLate: true, gradingMode: 'manual', releaseMode: 'on_return' },
  });
  expect(r.status(), await r.text()).toBe(201);
  const bc = (await r.json()) as { id: string };
  const an = await request.get(summaryUrl(bc.id), { headers: bearer(w.people['Bình An']!.token) });
  expect(an.status()).toBe(403);
  expect(((await an.json()) as { code: string }).code).toBe('FORBIDDEN');
  // giáo viên khác: không lộ bài có tồn tại
  const khoi = await request.get(summaryUrl(bc.id), { headers: bearer(w.people['Thầy Khôi']!.token) });
  expect(khoi.status()).toBe(404);
});

test('Câu có nhiễu cùng loại lỗi được xếp trước @TC-S5E5-17', async ({ request }) => {
  await bank(request);
  const c = await graded(request);
  await kcBank(request, c.w.orgId);
  await tl01(request, c.lan, c.c11.id); // TL-01 "Ghi chú Khoảng cách 11A1" Đang dùng, gắn 11A1
  const s = await summary(request, c.lan, c.bc.id);
  expect(s.insights[0]!.label).toBe('sai-tinh-toan');
  const calls = await aiCalls(request, c.lan.id);
  const g = await suggestions(request, c.lan, c.bc.id, 1);
  expect(g.label).toBe('sai-tinh-toan');
  expect(g.family).toBe('kc-diem-mp');
  expect(g.items.length).toBeGreaterThanOrEqual(2);
  expect(g.items.length).toBeLessThanOrEqual(3);
  const flags = g.items.map((i) => i.distractorMatch);
  expect(flags[0]).toBe(true);
  // mọi câu có nhiễu sai-tinh-toan đứng trước mọi câu không có
  expect(flags.indexOf(false) === -1 || flags.lastIndexOf(true) < flags.indexOf(false)).toBe(true);
  for (const i of g.items) {
    expect(i.statement.toLowerCase()).toContain('khoảng cách');
    if (i.distractorMatch) expect(i.distractor).toBeTruthy();
  }
  expect(g.docs.length).toBeGreaterThanOrEqual(1);
  expect(g.docs.length).toBeLessThanOrEqual(2);
  expect(g.docs[0]!.title).toBe('Ghi chú Khoảng cách 11A1');
  expect(await aiCalls(request, c.lan.id)).toBe(calls); // AiCallLog không có dòng mới
});

test('Kho không có câu cùng dạng thì nói rõ @TC-S5E5-18', async ({ request }) => {
  await bank(request);
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const rb = await rb01(request, lan);
  // TUAN-13: góc giữa hai mặt phẳng — ngân hàng de-thi chưa có dạng này
  const pv = await problemVersion(request, lan, 'TUAN-13');
  const r = await request.post(GA, {
    headers: bearer(lan.token),
    data: { title: 'Kiểm tra góc', classId: c11.id, problemVersionId: pv, rubricId: rb.id, dueAt: '2026-12-11T13:00:00Z', allowLate: true, gradingMode: 'manual', releaseMode: 'on_return' },
  });
  expect(r.status(), await r.text()).toBe(201);
  const bc = (await r.json()) as { id: string };
  await tl01(request, lan, c11.id); // có mục "Góc giữa hai mặt phẳng"
  const g = await suggestions(request, lan, bc.id, 1);
  expect(g.items).toEqual([]);
  expect(g.message).toBe(MSG.noBank);
  expect(g.docs.map((d) => d.title)).toEqual(['Ghi chú Khoảng cách 11A1']); // mục tài liệu vẫn hiện
});
