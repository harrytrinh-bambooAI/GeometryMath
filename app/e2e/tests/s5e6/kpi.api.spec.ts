import type { APIRequestContext } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, hook, type Person, type World } from '../s3e1/s3e1.ts';
import { GA, bc01Body, rb01, s5e4World } from '../s5e4/s5e4.ts';
import { aiOn } from '../s5e4/submit/submit.ts';
import { DKC01_PV } from './s5e6b.ts';

/**
 * S5-E6c (T-261128-65) — US-S5E6-06: báo cáo khớp AI–giáo viên theo KPI §2–§4 (KTC Wilson / t) và cổng bật, đóng,
 * mở lại "AI đề xuất" cho lớp. Số liệu 20–40 bài song song nạp qua móc `s5e6/kpi-seed` (mỗi bài: tiêu chí RB-01
 * {tối đa, điểm AI, điểm GV}, dạng bài, Kết quả đúng chiều engine, cờ bịa kiến thức); móc xét lại cổng sau mỗi lần nạp
 * như "sau bài cuối của đợt". AI không được gọi.
 */

const MAX = [2, 4, 2, 1, 1];
const FULL = [2, 4, 2, 1, 1];

interface Crit {
  max: number;
  ai?: number;
  gv?: number;
  edited?: boolean;
}
interface Paper {
  kind?: 'shadow' | 'proposal';
  topic?: string;
  criteria: Crit[];
  resultOk?: boolean | null;
  invented?: number;
  noGrade?: boolean;
  reviewSeconds?: number;
}
interface Rate {
  k: number;
  n: number;
  value: number | null;
  lower: number | null;
  upper: number | null;
  uncertain?: boolean;
}
interface Report {
  className: string;
  n_bai: number;
  n_cap: number;
  gateOpen: boolean;
  reasons: string[];
  message: string | null;
  defaultMode: string;
  metrics: {
    a: { n: number; value: number; lower: number; topics: Array<{ topic: string; n: number; value: number }> };
    b: Rate;
    c: Rate;
    d: { caught: Rate; falseAlarm: Rate };
    e: { invented: number; blocked: number; n: number };
    f: Rate;
    g: { medianSeconds: number | null; n: number };
    h: Rate;
  };
}

interface Ctx {
  w: World;
  lan: Person;
  c11: { id: string };
}

const p = (gv: number[], ai: number[], topic = 'khoảng cách', extra: Partial<Paper> = {}): Paper => ({
  topic,
  criteria: MAX.map((max, i) => ({ max, gv: gv[i]!, ai: ai[i]! })),
  resultOk: true,
  ...extra,
});
const times = (n: number, f: (i: number) => Paper): Paper[] => Array.from({ length: n }, (_, i) => f(i));

/**
 * Bộ 40 bài như US-S5E6-06-SC2: 200 cặp, (b) 190/200, (c) 40/40, (d) bắt 45/50 · báo nhầm 3/150, (e) 0.
 * Tuỳ chọn tạo các biến thể FC2–FC5.
 */
function sc2(o: { extraFa?: number; goc66?: boolean; invented?: number; resultBad?: number } = {}): Paper[] {
  const exact: Paper[] = times(7, (i) => p(FULL, FULL, i % 2 ? 'thể tích' : 'khoảng cách'));
  let fa = o.extraFa ?? 0;
  for (const e of exact) {
    for (const c of e.criteria) {
      if (fa > 0) {
        c.ai = c.max - 0.25;
        fa--;
      }
    }
  }
  if (o.goc66) {
    exact[0] = p([2, 0.6, 2, 1, 1], FULL, 'góc');
    exact[1] = p([2, 0.6, 2, 1, 1], FULL, 'góc');
  }
  const all = [
    ...exact,
    ...times(20, (i) => p([1, 3, 2, 1, 1], [1, 3, 2, 1, 1], i % 2 ? 'thể tích' : 'khoảng cách')), // 2 bước sai, AI bắt đúng
    ...times(5, () => p([2, 2, 2, 1, 1], [2, 3, 2, 1, 1], 'thể tích')), // bắt đúng nhưng lệch 1 điểm
    ...times(5, () => p([2, 3, 2, 1, 1], FULL, 'thể tích')), // bỏ sót, lệch 1 điểm
    ...times(3, () => p(FULL, [2, 4, 2, 1, 0.75])), // báo nhầm trong ±0,25
  ];
  if (o.invented) all[10]!.invented = o.invented;
  for (let i = 0; i < (o.resultBad ?? 0); i++) all[i + 7]!.resultOk = false;
  return all;
}

/** 20 bài đạt: (b) 95/100, bắt 5/5, báo nhầm 0. */
const good20 = (): Paper[] => [...times(15, () => p(FULL, FULL)), ...times(5, () => p([2, 2, 2, 1, 1], [2, 3, 2, 1, 1]))];

async function world(rq: APIRequestContext): Promise<Ctx> {
  const w = await s5e4World(rq);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await hook(rq, 's5e6/setup', { orgId: w.orgId });
  await aiOn(rq, lan, c11.id);
  return { w, lan, c11 };
}

async function seed(rq: APIRequestContext, c: Ctx, papers: Paper[]) {
  return (await hook(rq, 's5e6/kpi-seed', { orgId: c.w.orgId, classId: c.c11.id, papers })) as { inserted: number; gateOpen: boolean };
}

async function report(rq: APIRequestContext, c: Ctx, who: Person = c.lan): Promise<Report> {
  const r = await rq.get(`${API}/api/v1/teacher/my-classes/${c.c11.id}/ai-grading/report`, { headers: bearer(who.token) });
  expect(r.status(), await r.text()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  return (await r.json()) as Report;
}

async function assignAiProposal(rq: APIRequestContext, c: Ctx) {
  const rb = await rb01(rq, c.lan);
  return rq.post(GA, { headers: bearer(c.lan.token), data: bc01Body(c.c11.id, rb.id, DKC01_PV, { gradingMode: 'ai_proposal' }) });
}

test('Báo cáo khớp của 11A1 có n và KTC Wilson @TC-S5E6-28', async ({ request }) => {
  const c = await world(request);
  // 20 bài, 100 cặp: 12 cặp lệch 0,5 (ngoài ±0,25) → 88 trong ±0,25
  await seed(request, c, [...times(12, () => p([2, 3.5, 2, 1, 1], FULL)), ...times(8, () => p(FULL, FULL))]);
  const r = await report(request, c);
  expect(r).toMatchObject({ className: '11A1', n_bai: 20, n_cap: 100, gateOpen: false });
  expect(r.metrics.b).toMatchObject({ k: 88, n: 100, value: 0.88 });
  expect(r.metrics.b.lower).toBeCloseTo(0.802, 3);
  expect(r.metrics.b.upper).toBeCloseTo(0.93, 3);
  expect(r.reasons.join(' | ')).toContain('cận dưới 80,2 % < 85 %');
  for (const k of ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'] as const) expect(r.metrics).toHaveProperty(k);
  expect(r.metrics.a.n).toBe(20);
  expect(r.metrics.c.n).toBe(20);
  expect(r.metrics.d.caught.n).toBe(12);
  expect(r.metrics.h.n).toBe(20);
  const raw = JSON.stringify(r);
  for (const name of Object.keys(c.w.people).filter((n) => n !== 'Cô Lan')) expect(raw).not.toContain(name);
  expect(raw).not.toMatch(/rank|xếp hạng|studentId/i);
  // giáo viên khác không xem được báo cáo lớp người khác
  const khoi = c.w.people['Thầy Khôi'];
  if (khoi) {
    const x = await request.get(`${API}/api/v1/teacher/my-classes/${c.c11.id}/ai-grading/report`, { headers: bearer(khoi.token) });
    expect(x.status()).toBe(404);
  }
});

test('Đạt đủ 5 điều kiện bằng cận dưới KTC thì mở được chế độ AI đề xuất @TC-S5E6-29', async ({ request }) => {
  const c = await world(request);
  await seed(request, c, sc2());
  const r = await report(request, c);
  expect(r).toMatchObject({ n_bai: 40, n_cap: 200, gateOpen: true, reasons: [], defaultMode: 'ai_proposal' });
  expect(r.metrics.b).toMatchObject({ k: 190, n: 200 });
  expect(r.metrics.b.lower).toBeCloseTo(0.91, 2);
  expect(r.metrics.c).toMatchObject({ k: 40, n: 40 });
  expect(r.metrics.d.caught).toMatchObject({ k: 45, n: 50 });
  expect(r.metrics.d.falseAlarm).toMatchObject({ k: 3, n: 150 });
  expect(r.metrics.e).toMatchObject({ invented: 0, blocked: 0 });
  expect(r.metrics.a.lower).toBeGreaterThanOrEqual(0.85);
  expect(Math.min(...r.metrics.a.topics.map((t) => t.value))).toBeGreaterThanOrEqual(0.7);
  const a = await assignAiProposal(request, c);
  expect(a.status(), await a.text()).toBe(201);
  expect(((await a.json()) as { gradingMode: string }).gradingMode).toBe('ai_proposal');
});

test('Chưa đủ 20 bài thì cổng đóng @TC-S5E6-30', async ({ request }) => {
  const c = await world(request);
  await seed(request, c, times(12, () => p(FULL, FULL)));
  const r = await report(request, c);
  expect(r).toMatchObject({ n_bai: 12, gateOpen: false, message: 'Lớp chưa đủ 12/20 bài chạy song song để bật AI đề xuất.' });
  const a = await assignAiProposal(request, c);
  expect(a.status()).toBe(409);
  expect(((await a.json()) as { code: string }).code).toBe('CONFLICT');
  // biên: 19 bài vẫn đóng, 20 bài xét tiếp (khớp hoàn toàn → mở)
  await seed(request, c, times(7, () => p(FULL, FULL)));
  expect((await report(request, c)).message).toBe('Lớp chưa đủ 19/20 bài chạy song song để bật AI đề xuất.');
  await seed(request, c, [p(FULL, FULL)]);
  expect((await report(request, c)).gateOpen).toBe(true);
});

test('Một dạng bài dưới 70 % thì cổng đóng dù macro đạt @TC-S5E6-31', async ({ request }) => {
  const c = await world(request);
  await seed(request, c, sc2({ goc66: true }));
  const r = await report(request, c);
  expect(r.gateOpen).toBe(false);
  expect(r.reasons).toContain('dạng góc 66 % < 70 %');
  expect(r.metrics.a.topics.find((t) => t.topic === 'góc')).toMatchObject({ n: 2, value: 0.66 });
});

test('Báo nhầm vượt 10 % thì cổng đóng @TC-S5E6-32', async ({ request }) => {
  const c = await world(request);
  await seed(request, c, sc2({ extraFa: 22 }));
  const r = await report(request, c);
  expect(r.metrics.d.falseAlarm).toMatchObject({ k: 25, n: 150 });
  expect(r.gateOpen).toBe(false);
  expect(r.reasons).toContain('báo nhầm (d) 16,7 % > 10 %');
});

test('Một lần bịa kiến thức thì cổng đóng @TC-S5E6-33', async ({ request }) => {
  const c = await world(request);
  await seed(request, c, sc2({ invented: 1 }));
  const r = await report(request, c);
  expect(r.gateOpen).toBe(false);
  expect(r.reasons).toEqual(['bịa kiến thức: 1']);
  expect(r.metrics.e.invented).toBe(1);
});

test('Kết quả AI đúng chiều engine dưới 95 % thì cổng đóng @TC-S5E6-34', async ({ request }) => {
  const c = await world(request);
  await seed(request, c, sc2({ resultBad: 4 }));
  const r = await report(request, c);
  expect(r.metrics.c).toMatchObject({ k: 36, n: 40, value: 0.9, uncertain: true });
  expect(r.gateOpen).toBe(false);
  expect(r.reasons).toEqual(['kết quả đúng chiều engine (c) 90 % < 95 %']);
});

/** Một đợt 10 bài, 50 cặp, (b) 35/50. */
const badBatch = (): Paper[] => [...times(5, () => p(FULL, [2, 3, 1, 1, 1])), ...times(5, () => p(FULL, [2, 3, 2, 1, 1]))];

async function closeByTwoBatches(rq: APIRequestContext, c: Ctx) {
  await seed(rq, c, good20());
  expect((await report(rq, c)).gateOpen).toBe(true);
  await seed(rq, c, badBatch());
  // sau một đợt đầu không đạt, cổng vẫn mở
  expect((await report(rq, c)).gateOpen).toBe(true);
  await seed(rq, c, badBatch());
}

test('Hai đợt liên tiếp không đạt thì cổng tự đóng và báo giáo viên @TC-S5E6-35', async ({ request }) => {
  const c = await world(request);
  await closeByTwoBatches(request, c);
  const r = await report(request, c);
  expect(r).toMatchObject({ gateOpen: false, defaultMode: 'parallel', n_bai: 0 });
  const n = await request.get(`${API}/api/v1/notifications`, { headers: bearer(c.lan.token) });
  expect(n.status()).toBe(200);
  const items = ((await n.json()) as { items: Array<{ kind: string; body: string }> }).items.filter((x) => x.kind === 'ai_gate');
  expect(items.map((x) => x.body)).toEqual(['AI đề xuất đã tạm tắt cho 11A1']);
  // bài kế tiếp: "AI đề xuất" không chọn được nữa → mặc định Chạy song song
  expect((await assignAiProposal(request, c)).status()).toBe(409);
});

test('Mở lại cổng khi đạt lại trên 20 bài song song mới @TC-S5E6-36', async ({ request }) => {
  const c = await world(request);
  await closeByTwoBatches(request, c);
  expect((await report(request, c)).gateOpen).toBe(false);
  await seed(request, c, good20());
  const r = await report(request, c);
  expect(r).toMatchObject({ gateOpen: true, n_bai: 20, n_cap: 100 });
  expect(r.metrics.b).toMatchObject({ k: 95, n: 100 });
});

test('Báo cáo có tỉ lệ sửa và thời gian duyệt @TC-S5E6-37', async ({ request }) => {
  const c = await world(request);
  let edits = 14;
  const proposals: Paper[] = times(20, (i) => ({
    kind: 'proposal',
    reviewSeconds: 60 + i * 10,
    criteria: MAX.map((max) => ({ max, edited: edits-- > 0 })),
  }));
  await seed(request, c, [...proposals, ...times(18, () => p(FULL, FULL)), ...times(2, () => ({ criteria: [], noGrade: true, resultOk: null }))]);
  const r = await report(request, c);
  expect(r.metrics.f).toMatchObject({ k: 14, n: 100, value: 0.14 });
  expect(r.metrics.f.lower).toBeLessThan(0.14);
  expect(r.metrics.g).toMatchObject({ medianSeconds: 155, n: 20 });
  expect(r.metrics.h).toMatchObject({ k: 2, n: 20, value: 0.1 });
});
