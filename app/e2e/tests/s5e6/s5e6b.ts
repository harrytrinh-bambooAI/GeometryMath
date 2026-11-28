import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, hook, type Person, type World } from '../s3e1/s3e1.ts';
import { GA, bc01Body, rb01, s5e4World } from '../s5e4/s5e4.ts';
import { P1, P2, SUB, ack, aiOn, submitOk, waitOcr } from '../s5e4/submit/submit.ts';

/**
 * Tiện ích E2E S5-E6b (T-261128-62): AI không chấm (US-S5E6-02), màn chấm có đề xuất (US-S5E6-04), chạy song song
 * (US-S5E6-05). Thế giới riêng mỗi ca (S5-E4: cô Lan chủ 11A1, 8 em). BC-01 trên D-KC-01 (đáp số engine a√6/3), rubric
 * RB-01 (1 Hướng 2 · 2 Các bước 4 · 3 Kết quả 2 · 4 Hình 1 · 5 Trình bày 1). Chế độ "AI đề xuất" đặt qua móc của T-61
 * (cổng lớp còn stub); "Chạy song song" chọn thẳng khi giao bài. AI chỉ giả lập: kịch bản qaReplies theo học sinh nộp.
 */
export const DKC01_PV = '00000000-0000-7000-8005-200000000050';
export const QUEUE = `${API}/api/v1/teacher/grading/queue`;
export const RB01 = ['Hướng', 'Các bước', 'Kết quả', 'Hình', 'Trình bày'];

export const MSG = {
  s45: (reason: string) => `AI không chấm bài này: ${reason}. Thầy cô chấm tay nhé.`,
  s47: 'Chế độ chạy song song: thầy cô chấm trước, đề xuất của AI hiện sau khi duyệt.',
  s49: (name: string) => `Đã nhận đề xuất của tiêu chí ${name}.`,
  source: 'AI đề xuất, giáo viên duyệt',
} as const;

export type Mode = 'ai_proposal' | 'parallel';

export interface Ctx {
  w: World;
  lan: Person;
  c11: { id: string };
  bc: { id: string };
}

/** BC-01 ở chế độ mode cho 11A1 của thế giới mới; AI chấm của lớp bật. */
export async function setup(rq: APIRequestContext, mode: Mode): Promise<Ctx> {
  const w = await s5e4World(rq);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await hook(rq, 's5e6/setup', { orgId: w.orgId });
  await aiOn(rq, lan, c11.id);
  const rb = await rb01(rq, lan);
  const r = await rq.post(GA, { headers: bearer(lan.token), data: bc01Body(c11.id, rb.id, DKC01_PV, { gradingMode: mode === 'parallel' ? 'parallel' : 'manual' }) });
  expect(r.status(), await r.text()).toBe(201);
  const bc = (await r.json()) as { id: string; gradingMode: string };
  if (mode === 'ai_proposal') expect((await hook(rq, 's5e6/setup', { orgId: w.orgId, assignmentId: bc.id })).aiProposal).toBe(1);
  else expect(bc.gradingMode).toBe('parallel');
  return { w, lan, c11, bc };
}

/** Kịch bản trả lời task `grade` của giả lập cho học sinh này (theo lượt gọi, lặp câu cuối). */
export async function mock(rq: APIRequestContext, student: Person, replies: string[]) {
  await hook(rq, 'ai', { userId: student.id, qaReplies: replies });
}

/** Đầu ra `grade` hợp lệ dạng F-S5-090 với điểm và độ tin cậy cho trước (nguyên văn cho giả lập). */
export function gradeJson(pts: number[] = [2, 4, 2, 1, 1], conf: number | number[] = 0.86): string {
  const why = ['Chọn đúng hướng.', 'Các bước tính SB, AH.', 'Đáp số cuối của bài.', 'Hình vẽ đủ.', 'Trình bày rõ.'];
  const confs = Array.isArray(conf) ? conf : pts.map(() => conf);
  return JSON.stringify({
    kind: 'grade',
    hinh: 'đúng',
    criteria: pts.map((d, i) => ({
      ma: `C${i + 1}`,
      diem: d,
      ly_do: why[i],
      vung: { trang: i < 2 ? 1 : 2, x: 40, y: 60 + 80 * i, w: 480, h: 60 },
      tin_cay: confs[i],
    })),
    tong: pts.reduce((a, b) => a + b, 0),
    buoc: [1, 2, 3, 4].map((i) => ({ ma: `B${i}`, tieu_chi: 'C2', diem: i <= pts[1]! ? 1 : 0, dung: i <= pts[1]!, phu_thuoc: [], dua_tren_hinh: false })),
  });
}

/** Học sinh nộp 2 trang rõ, chờ bản đọc chữ. */
export async function submit(rq: APIRequestContext, c: Ctx, name: string) {
  const st = c.w.people[name]!;
  await ack(rq, st);
  const sub = await submitOk(rq, st, c.bc.id, [P1, P2]);
  const o = await waitOcr(rq, c.lan, sub.id);
  expect(o.status).toBe('done');
  return { st, sub };
}

export interface AiExtras {
  aiDone?: boolean;
  shadow?: boolean;
  message?: string;
  aiStatus?: string;
  aiStatusLabel?: string;
  aiFlags?: string[];
  reason?: string;
  retryable?: boolean;
  acceptAll?: boolean;
  source?: string;
  actions?: Array<{ ord: number; name: string; action: string; aiPoints: number; teacherPoints: number }>;
}

export interface View {
  submissionId: string;
  status: string;
  statusLabel: string;
  version: number;
  grade?: { seq: number; total: number; criteria: Array<{ ord: number; name: string; points: number }> };
  ai?: AiExtras;
  nextSubmissionId?: string | null;
  [k: string]: unknown;
}

/** Màn chấm (GET) của giáo viên. */
export async function view(rq: APIRequestContext, t: Person, subId: string): Promise<View> {
  const r = await rq.get(`${API}/api/v1/teacher/grading/submissions/${subId}`, { headers: bearer(t.token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as View;
}

/** Chờ AI chấm nền xong (ai.aiDone) cho bài nộp. */
export async function waitAi(rq: APIRequestContext, t: Person, subId: string): Promise<View> {
  let last: View | null = null;
  await expect
    .poll(
      async () => {
        last = await view(rq, t, subId);
        return last.ai?.aiDone === true;
      },
      { timeout: 30_000, intervals: [300, 500, 1000] },
    )
    .toBe(true);
  return last!;
}

export const gradeRaw = (rq: APIRequestContext, t: Person, subId: string, body: unknown) =>
  rq.post(`${SUB}/${subId}/grade`, { headers: bearer(t.token), data: body });

/** Duyệt với điểm theo thứ tự RB-01 (và hành động AI nếu có). */
export async function approve(rq: APIRequestContext, t: Person, subId: string, pts: number[], extra: Record<string, unknown> = {}): Promise<View> {
  const v = await view(rq, t, subId);
  const r = await gradeRaw(rq, t, subId, { version: v.version, criteria: pts.map((p, k) => ({ ord: k + 1, points: p })), comment: '', ...extra });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as View;
}

export async function proposalRaw(rq: APIRequestContext, t: Person, subId: string) {
  return rq.get(`${SUB}/${subId}/proposal`, { headers: bearer(t.token) });
}

export const gradeCalls = async (rq: APIRequestContext, userId: string) =>
  ((await (await rq.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`)).json()) as Array<{ purpose: string }>).filter((c) => c.purpose === 'grade');

export async function queue(rq: APIRequestContext, t: Person, assignmentId: string) {
  const r = await rq.get(`${QUEUE}?assignmentId=${assignmentId}`, { headers: bearer(t.token) });
  expect(r.status(), await r.text()).toBe(200);
  return ((await r.json()) as { items: Array<Record<string, unknown> & { submissionId: string; ai?: AiExtras }> }).items;
}
