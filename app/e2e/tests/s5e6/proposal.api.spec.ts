import type { APIRequestContext } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, PASSWORD, bearer, hook, type Person, type World } from '../s3e1/s3e1.ts';
import { deleteAccount } from '../s3e5/account.ts';
import { GA, bc01Body, rb01, s5e4World } from '../s5e4/s5e4.ts';
import { P1, P2, SUB, ack, aiOn, submitOk, waitOcr } from '../s5e4/submit/submit.ts';

/**
 * S5-E6 (T-261128-61) — AI đề xuất điểm theo tiêu chí (US-S5E6-01) và engine làm tín hiệu thứ hai (US-S5E6-03), chỉ API.
 * Thế giới riêng mỗi ca (S5-E4: cô Lan chủ 11A1, 8 em). Bài có chấm BC-01 trên đề F-S5-050 D-KC-01 (đáp số engine a√6/3,
 * bộ 's5_core' cấp qua móc), rubric RB-01 (F-S5-051), chế độ "AI đề xuất" đặt qua móc (cổng lớp US-S5E6-06 còn là stub
 * tới T-261128-65). Học sinh nộp thật (US-S5E4-03) → đọc chữ nền (giả lập F-S5-095) → job nền chấm task `grade` (giả lập
 * F-S5-090 mặc định; biến thể theo kịch bản qaReplies của học sinh nộp) → luật chặn KPI §2 → tín hiệu engine.
 */

const DKC01_PV = '00000000-0000-7000-8005-200000000050';

interface Criterion {
  ord: number;
  name: string;
  maxPoints: number;
  aiPoints: number;
  points: number;
  reason: string;
  region: { trang: number; x: number; y: number; w: number; h: number } | null;
  confidence: number;
  errorLabel: string | null;
  adjustments: Array<{ rule: string; from: number; to: number; note: string }>;
  flags: string[];
}
interface Proposal {
  submissionId: string;
  status: 'pending' | 'proposed' | 'no_grade';
  statusLabel: string;
  reason?: string;
  message?: string;
  total?: number;
  scale?: number;
  confidence?: number;
  criteria?: Criterion[];
  steps?: Array<{ code: string; criterionOrd: number; aiPoints: number; points: number; correct: boolean; rule: string | null }>;
  flags: string[];
  engine?: { status: string; statusLabel: string; engineAnswer: string | null; studentAnswer: string | null; rule: string | null; disagree: boolean; message?: string; note?: string };
  versions?: { problemVersionId: string; rubricVersionId: string; rubricVersion: number; submissionVersion: number; ocrVersion: number; task: string; model: string; promptVersion: string; tokens: number };
}

/** BC-01 chế độ "AI đề xuất" trên D-KC-01 cho 11A1 của thế giới mới; AI chấm của lớp bật. */
async function setup(rq: APIRequestContext) {
  const w = await s5e4World(rq);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await hook(rq, 's5e6/setup', { orgId: w.orgId });
  await aiOn(rq, lan, c11.id);
  const rb = await rb01(rq, lan);
  const r = await rq.post(GA, { headers: bearer(lan.token), data: bc01Body(c11.id, rb.id, DKC01_PV) });
  expect(r.status(), await r.text()).toBe(201);
  const bc = (await r.json()) as { id: string };
  expect((await hook(rq, 's5e6/setup', { orgId: w.orgId, assignmentId: bc.id })).aiProposal).toBe(1);
  return { w, lan, c11, bc, rb };
}

/** Kịch bản trả lời task `grade` của giả lập cho lượt chấm nền của học sinh này (theo lượt gọi, lặp câu cuối). */
async function mock(rq: APIRequestContext, student: Person, replies: string[]) {
  await hook(rq, 'ai', { userId: student.id, qaReplies: replies });
}

/** Học sinh nộp 2 trang rõ (BN-xx), chờ bản đọc chữ v1. */
async function submit(rq: APIRequestContext, w: World, lan: Person, bcId: string, name: string) {
  const st = w.people[name]!;
  await ack(rq, st);
  const sub = await submitOk(rq, st, bcId, [P1, P2]);
  const o = await waitOcr(rq, lan, sub.id);
  expect(o.status).toBe('done');
  return { st, sub };
}

/** Giáo viên sửa trang 2 của bản đọc chữ (tạo v2: dòng kết luận cho tín hiệu engine). */
async function setConclusion(rq: APIRequestContext, lan: Person, subId: string, lines: string[]) {
  const r = await rq.put(`${SUB}/${subId}/ocr`, { headers: bearer(lan.token), data: { baseVersion: 1, pages: [{ pageNo: 2, segments: lines.map((text) => ({ text })) }] } });
  expect(r.status(), await r.text()).toBe(200);
}

/** Chờ đề xuất của bản đọc chữ phiên bản ocrVersion (mặc định: bất kỳ) được chấm xong. */
async function waitProposal(rq: APIRequestContext, lan: Person, subId: string, ocrVersion?: number): Promise<Proposal> {
  let last: Proposal | null = null;
  await expect
    .poll(
      async () => {
        const r = await rq.get(`${SUB}/${subId}/proposal`, { headers: bearer(lan.token) });
        expect(r.status(), await r.text()).toBe(200);
        expect(r.headers()['cache-control']).toContain('no-store');
        last = (await r.json()) as Proposal;
        if (last.status === 'pending') return false;
        return ocrVersion === undefined || last.versions?.ocrVersion === ocrVersion;
      },
      { timeout: 30_000, intervals: [300, 500, 1000] },
    )
    .toBe(true);
  return last!;
}

const crit = (p: Proposal, name: string) => p.criteria!.find((c) => c.name === name)!;

const gradeCalls = async (rq: APIRequestContext, userId: string) =>
  ((await (await rq.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`)).json()) as Array<{ purpose: string }>).filter((c) => c.purpose === 'grade');

async function signals(rq: APIRequestContext, lan: Person, bcId: string) {
  const r = await rq.get(`${API}/api/v1/teacher/graded-assignments/${bcId}/ai-signals`, { headers: bearer(lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  return ((await r.json()) as { items: Array<{ submissionId: string; flags: string[]; flagged: boolean; status: string }> }).items;
}

// ------------------------------------------------------------------ US-S5E6-01

test('Đề xuất cho BN-01 đủ 5 tiêu chí, tổng 10 @TC-S5E6-01', async ({ request }) => {
  const s = await setup(request);
  const an = s.w.people['Bình An']!;
  await mock(request, an, ['F-S5-090']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  const p = await waitProposal(request, s.lan, sub.id);
  expect(p.status).toBe('proposed');
  expect(p.criteria!.map((c) => [c.name, c.points])).toEqual([
    ['Hướng', 2],
    ['Các bước', 4],
    ['Kết quả', 2],
    ['Hình', 1],
    ['Trình bày', 1],
  ]);
  expect(p.total).toBe(10);
  expect(p.confidence).toBe(0.86);
  for (const c of p.criteria!) {
    expect(c.reason.length).toBeGreaterThan(0);
    expect(c.reason.length).toBeLessThanOrEqual(300);
    expect(c.region).toEqual(expect.objectContaining({ trang: expect.any(Number), x: expect.any(Number), y: expect.any(Number), w: expect.any(Number), h: expect.any(Number) }));
    expect(c.adjustments).toEqual([]);
  }
  // phiên bản: rubric RB-01 v1, bài nộp v1, bản đọc chữ, task grade, model, prompt
  expect(p.versions).toEqual(
    expect.objectContaining({ problemVersionId: DKC01_PV, rubricVersionId: s.rb.versionId, rubricVersion: 1, submissionVersion: 1, ocrVersion: 1, task: 'grade' }),
  );
  expect(p.versions!.model.length).toBeGreaterThan(0);
  expect(p.versions!.promptVersion).toMatch(/^grade-v1\//);
  // yêu cầu gửi AI không có tên, mã người dùng, mã lớp
  const pr = await request.get(`${API}/test-hooks/s5e6/prompt?${new URLSearchParams({ orgId: s.w.orgId, submissionId: sub.id })}`);
  expect(pr.status()).toBe(200);
  const prompt = ((await pr.json()) as { prompt: string }).prompt;
  expect(prompt).toContain('[s5e6-grade]');
  expect(prompt).toContain('Đáp số engine: a√6/3');
  for (const bad of ['Bình An', an.userCode, an.id, s.w.classes['11A1']!.id, '11A1', s.lan.displayName, s.w.orgId, sub.id]) {
    expect(prompt).not.toContain(bad);
  }
});

test('Luật chặn 2: đúng kết quả nhưng lập luận thiếu thì kết quả bị hạ @TC-S5E6-02', async ({ request }) => {
  const s = await setup(request);
  // BN-07 của Minh Quân (F-S5-057): giả lập Hướng 2, Bước 1, Kết quả 2, Hình 1, Trình bày 1
  await mock(request, s.w.people['Minh Quân']!, ['s5e6-thieu']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Minh Quân');
  const p = await waitProposal(request, s.lan, sub.id);
  expect(p.status).toBe('proposed');
  const kq = crit(p, 'Kết quả');
  expect(kq.aiPoints).toBe(2);
  expect(kq.points).toBe(1);
  expect(kq.adjustments).toEqual([expect.objectContaining({ rule: 'luật KPI §2.2', from: 2, to: 1 })]);
  expect(p.total).toBe(6);
});

test('Luật chặn 1: sai hướng thì bước phụ thuộc hướng = 0 @TC-S5E6-03', async ({ request }) => {
  const s = await setup(request);
  // F-S5-091 biến thể Hướng 0, Bước 3
  await mock(request, s.w.people['Chi Mai']!, ['s5e6-huong0']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Chi Mai');
  const p = await waitProposal(request, s.lan, sub.id);
  expect(crit(p, 'Hướng').points).toBe(0);
  const b = crit(p, 'Các bước');
  expect(b.aiPoints).toBe(3);
  expect(b.points).toBe(0);
  expect(b.adjustments.map((a) => a.rule)).toEqual(['luật KPI §2.1']);
});

test('Luật chặn 3: bước sai kéo bước phụ thuộc về 0, bước độc lập giữ @TC-S5E6-04', async ({ request }) => {
  const s = await setup(request);
  // BN-02 của Chi Mai (F-S5-054, F-S5-091): B2 sai, B3 phu_thuoc [B2], B4 độc lập, Bước 3/4
  await mock(request, s.w.people['Chi Mai']!, ['s5e6-buoc-sai']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Chi Mai');
  const p = await waitProposal(request, s.lan, sub.id);
  const step = (c: string) => p.steps!.find((x) => x.code === c)!;
  expect(step('B3').aiPoints).toBe(1);
  expect(step('B3').points).toBe(0);
  expect(step('B3').rule).toBe('luật KPI §2.3');
  expect(step('B4').points).toBe(1);
  expect(step('B4').rule ?? null).toBeNull();
  const b = crit(p, 'Các bước');
  expect(b.aiPoints).toBe(3);
  expect(b.points).toBe(2); // giảm đúng phần B3
  expect(b.adjustments).toEqual([expect.objectContaining({ rule: 'luật KPI §2.3', from: 3, to: 2 })]);
  expect(b.errorLabel).toBe('sai-tinh-toan');
});

test('Luật chặn 4: hình sai thì mất phần hình và bước dựa trên hình @TC-S5E6-05', async ({ request }) => {
  const s = await setup(request);
  // F-S5-053 biến thể hình sai: hinh "hình sai", B1 dua_tren_hinh, Hình 1
  await mock(request, s.w.people['Bình An']!, ['s5e6-hinh-sai']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  const p = await waitProposal(request, s.lan, sub.id);
  const h = crit(p, 'Hình');
  expect(h.aiPoints).toBe(1);
  expect(h.points).toBe(0);
  expect(h.adjustments.map((a) => a.rule)).toEqual(['luật KPI §2.4']);
  const b1 = p.steps!.find((x) => x.code === 'B1')!;
  expect(b1.points).toBe(0);
  expect(b1.rule).toBe('luật KPI §2.4');
  expect(crit(p, 'Các bước').points).toBe(3);
  expect(p.total).toBe(8);
});

test('Điểm vượt tối đa: gọi lại một lần rồi AI không chấm @TC-S5E6-06', async ({ request }) => {
  const s = await setup(request);
  const an = s.w.people['Bình An']!;
  // Bước = 5 (tối đa 4) cả hai lần
  await mock(request, an, ['s5e6-vuot', 's5e6-vuot']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  const p = await waitProposal(request, s.lan, sub.id);
  expect(p.status).toBe('no_grade');
  expect(p.flags).toContain('AI không chấm');
  expect(p.reason).toBe('điểm ngoài thang');
  expect(p.message).toBe('AI không chấm bài này: điểm ngoài thang. Thầy cô chấm tay nhé.');
  // không có đề xuất điểm hợp lệ
  expect(p.total).toBeUndefined();
  expect(p.criteria).toBeUndefined();
  // AiCallLog: đúng 2 dòng task grade (gọi lại một lần)
  expect(await gradeCalls(request, an.id)).toHaveLength(2);
});

test('Học sinh không đọc được đề xuất @TC-S5E6-07', async ({ request }) => {
  const s = await setup(request);
  const an = s.w.people['Bình An']!;
  await mock(request, an, ['F-S5-090']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  const p = await waitProposal(request, s.lan, sub.id);
  expect(p.total).toBe(10);
  const r = await request.get(`${SUB}/${sub.id}/proposal`, { headers: bearer(an.token) });
  expect(r.status()).toBe(403);
  const body = await r.text();
  expect((JSON.parse(body) as { code: string }).code).toBe('FORBIDDEN');
  for (const k of ['total', 'confidence', 'criteria', 'points']) expect(body).not.toContain(`"${k}"`);
  // thầy Khôi (không quan hệ với 11A1): không lộ bài tồn tại
  const khoi = s.w.people['Thầy Khôi']!;
  expect((await request.get(`${SUB}/${sub.id}/proposal`, { headers: bearer(khoi.token) })).status()).toBe(404);
});

test('Bịa kiến thức bị gắn cờ và hạ độ tin cậy @TC-S5E6-08', async ({ request }) => {
  const s = await setup(request);
  // tiêu chí Bước dẫn "định lí ba đường cao không gian" (không có trong kho)
  await mock(request, s.w.people['Bình An']!, ['s5e6-bia']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  const p = await waitProposal(request, s.lan, sub.id);
  expect(p.status).toBe('proposed');
  const b = crit(p, 'Các bước');
  expect(b.flags).toEqual(['bịa kiến thức']);
  expect(b.confidence).toBeLessThanOrEqual(0.5);
  expect(b.adjustments.map((a) => a.rule)).toContain('luật KPI §2.5');
  expect(p.flags).toContain('bịa kiến thức');
  // bài có cờ trong hàng chờ
  const items = await signals(request, s.lan, s.bc.id);
  expect(items).toEqual([expect.objectContaining({ submissionId: sub.id, flagged: true, flags: expect.arrayContaining(['bịa kiến thức']) })]);
});

// ------------------------------------------------------------------ US-S5E6-03

test('BN-01 đáp số khớp engine @TC-S5E6-14', async ({ request }) => {
  const s = await setup(request);
  await mock(request, s.w.people['Bình An']!, ['F-S5-090']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  await setConclusion(request, s.lan, sub.id, ['1/AH² = 1/SA² + 1/AB²', 'Vậy d(A,(SBC)) = a√6/3']);
  const p = await waitProposal(request, s.lan, sub.id, 2);
  expect(p.engine).toEqual(
    expect.objectContaining({ status: 'match', statusLabel: 'Khớp', engineAnswer: 'a√6/3', studentAnswer: 'a√6/3', rule: 'KS-1', disagree: false }),
  );
  expect(crit(p, 'Kết quả').points).toBe(2);
  expect(p.flags).not.toContain('AI và engine lệch');
});

test('Đáp số viết thập phân vẫn khớp @TC-S5E6-15', async ({ request }) => {
  const s = await setup(request);
  await mock(request, s.w.people['Bình An']!, ['F-S5-090']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  await setConclusion(request, s.lan, sub.id, ['1/AH² = 1/SA² + 1/AB²', 'd ≈ 0,816a']);
  const p = await waitProposal(request, s.lan, sub.id, 2);
  expect(p.engine).toEqual(expect.objectContaining({ status: 'match', statusLabel: 'Khớp', studentAnswer: '0,816a', rule: 'KS-2', disagree: false }));
});

test('AI cho điểm kết quả nhưng engine báo lệch thì gắn cờ @TC-S5E6-16', async ({ request }) => {
  const s = await setup(request);
  // BN-01 của Bình An: không có dòng kết luận → không cờ; BN-02 của Chi Mai: đáp số a√3/3, giả lập Kết quả 2/2
  await mock(request, s.w.people['Bình An']!, ['F-S5-090']);
  await mock(request, s.w.people['Chi Mai']!, ['s5e6-kq2']);
  const bn01 = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  await waitProposal(request, s.lan, bn01.sub.id);
  const bn02 = await submit(request, s.w, s.lan, s.bc.id, 'Chi Mai');
  await setConclusion(request, s.lan, bn02.sub.id, ['SB = a√2', 'Vậy d(A,(SBC)) = a√3/3']);
  const p = await waitProposal(request, s.lan, bn02.sub.id, 2);
  expect(crit(p, 'Kết quả').points).toBe(2);
  expect(p.engine).toEqual(
    expect.objectContaining({ status: 'mismatch', statusLabel: 'Lệch', studentAnswer: 'a√3/3', engineAnswer: 'a√6/3', disagree: true }),
  );
  expect(p.engine!.message).toBe('Đáp số trong bài khác kết quả máy tính (a√6/3).');
  expect(p.flags).toContain('AI và engine lệch');
  // hàng chờ: bài có cờ "AI và engine lệch" xếp đầu
  const items = await signals(request, s.lan, s.bc.id);
  expect(items.map((i) => i.submissionId)).toEqual([bn02.sub.id, bn01.sub.id]);
  expect(items[0]!.flags).toContain('AI và engine lệch');
  expect(items[1]!.flagged).toBe(false);
});

test('Không tìm thấy đáp số cuối thì không gắn cờ lệch @TC-S5E6-17', async ({ request }) => {
  const s = await setup(request);
  // bản đọc chữ v1 (F-S5-095 theo trang) không có dòng kết luận
  await mock(request, s.w.people['Bình An']!, ['F-S5-090']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  const p = await waitProposal(request, s.lan, sub.id, 1);
  expect(p.engine).toEqual(expect.objectContaining({ status: 'not_found', statusLabel: 'Không tìm thấy', disagree: false }));
  expect(p.engine!.studentAnswer ?? null).toBeNull();
  expect(p.engine!.note).toBe('Không thấy đáp số cuối trong bản đọc chữ');
  expect(p.flags).not.toContain('AI và engine lệch');
});

// ------------------------------------------------------------------ BR-S5E4-28 (SubmissionDataEraser "ai_grade")

test('Xoá tài khoản học sinh xoá đề xuất AI và tín hiệu engine của bài nộp @TC-S5E4-43', async ({ request }) => {
  const s = await setup(request);
  const an = s.w.people['Bình An']!;
  await mock(request, an, ['F-S5-090']);
  const { sub } = await submit(request, s.w, s.lan, s.bc.id, 'Bình An');
  await waitProposal(request, s.lan, sub.id);
  const counts = async () =>
    (await (await request.get(`${API}/test-hooks/s5e6/counts?${new URLSearchParams({ orgId: s.w.orgId, submissionId: sub.id })}`)).json()) as {
      proposals: number;
      signals: number;
    };
  expect(await counts()).toEqual({ proposals: 1, signals: 1 });
  const d = await deleteAccount(request, an.token, an.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(d.status(), await d.text()).toBe(200);
  await hook(request, 's5e4/purge-accounts', { assignmentId: s.bc.id });
  expect(await counts()).toEqual({ proposals: 0, signals: 0 });
});
