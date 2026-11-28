import type { APIRequestContext, APIResponse } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, world, type ClassSpec, type Person, type World } from '../s3e1/s3e1.ts';
import { bc01, rb01, type Graded } from '../s5e4/s5e4.ts';
import { KT, json, tl01, type DocView } from './kb.ts';

/**
 * Tiện ích E2E S5-E3 hỏi đáp theo kho, mức trợ giúp, kiểm soát đáp án (T-261128-58, US-S5E3-03, 04, 05). Mỗi ca dựng
 * thế giới riêng qua móc S3-E1: cô Lan chủ 11A1 (Bình An, Chi Mai Đang học; Văn Tú Đã rời) và 11A2 (Quốc Bảo), thầy Khôi
 * chủ 10B (Mi Na). BC-01 (F-S5-052) là bài có chấm thật của S5-E4 trên đề D-KC-01 (F-S5-050, đáp số engine a√6/3),
 * TL-01 v2 (F-S5-040) gắn cả lớp 11A1 (gắn kết kho theo bài chỉ nhận bài luyện S3: bài có chấm dùng gắn lớp).
 * AI giả lập (MockAiProvider task kb_qa: F-S5-094, F-S5-094b, s5e3-*); kịch bản đặt theo học sinh qua /test-hooks/ai.
 */
export const QA = `${API}/api/v1/student/kb-qa`;
export const helpUrl = (id: string) => `${API}/api/v1/teacher/assignments/${id}/help-level`;
export const warningsUrl = (id: string) => `${API}/api/v1/teacher/assignments/${id}/kb-warnings`;

/** Phiên bản đề F-S5-050 D-KC-01 (s5_000_core.sql). */
export const DKC01_PV = '00000000-0000-7000-8005-200000000050';
export const PRIVACY_VERSION = 'privacy-qa-s5-1.0';
export const LECTURE_SLUG = 'mat-phang-trung-truc';

export const MSG = {
  s20: 'Thầy cô chưa cung cấp tài liệu cho phần này. Em hỏi thầy cô trên lớp nhé.',
  s21: 'Thầy cô đang tắt Hỏi đáp cho bài này. Em vẫn làm bài và xem gợi ý như thường.',
  s24: 'Câu trả lời chưa an toàn để hiện. Em thử hỏi cách khác hoặc xem gợi ý.',
  s26: 'Câu em hỏi AI và câu trả lời được lưu tới hết năm học; thầy cô lớp em đọc được để giúp em.',
  classOff: 'Lớp này đã tắt trợ lý AI. Em vẫn làm bài và xem gợi ý có sẵn.',
} as const;

export const Q_SC1 = 'Công thức tính khoảng cách từ A đến (SBC) khi SA vuông góc đáy là gì?';

/** Mọi dạng của đáp số a√6/3 đang khoá (KS-1/KS-2) dùng để đếm lộ. */
export const ANSWER_FORMS = ['a√6/3', '√6/3', 'a*sqrt(6)/3', 'sqrt(6)/3', 'căn 6', '0,816', '0.816', '0,8165', '0.8165'];
export const PRIVATE_NOTE = 'Em Mai hay nhầm SB';
export const RUBRIC_LINE = 'Chọn đúng hướng giải';
export const BN02 = 'a√3/3';

export const S5E3B_WORLD: { teachers: string[]; students: string[]; classes: ClassSpec[] } = {
  teachers: ['Cô Lan', 'Thầy Khôi'],
  students: [],
  classes: [
    { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An', 'Chi Mai'], left: ['Văn Tú'] },
    { key: '11A2', teacher: 'Cô Lan', name: '11A2', active: ['Quốc Bảo'] },
    { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na'] },
  ],
};

export interface QaReply {
  interactionId: string;
  kind: string;
  reply: string;
  message?: string;
  retry: boolean;
  source: null | {
    type: 'kb' | 'lecture';
    typeLabel: string;
    docCode?: string;
    docTitle?: string;
    version?: number;
    sectionId?: string;
    sectionTitle?: string;
    title?: string;
    label: string;
  };
}

export interface Decision {
  interaction_id: string | null;
  student_id: string;
  target_id: string;
  decision: 'allowed' | 'blocked' | 'error_closed';
  rule_code: string | null;
  check_ms: number | null;
}
export interface Interaction {
  id: string;
  student_id: string;
  question: string;
  reply_kind: string;
  reply: string;
  source: string | null;
}
export interface QaState {
  interactions: Interaction[];
  decisions: Decision[];
  counters: Array<{ decision: string; rule_code: string; n: number }>;
}

export interface Ctx {
  w: World;
  lan: Person;
  an: Person;
  c11: { id: string; code: string };
  bc: Graded;
  tl: DocView;
}

/** Thế giới + BC-01 trên D-KC-01 (mức Gợi ý và khái niệm, mã KT của đề) + TL-01 v2 gắn 11A1; Bình An đã đọc MSG-S5-26. */
export async function setup(rq: APIRequestContext, level = 'hints_concepts', extra: Record<string, unknown> = {}): Promise<Ctx> {
  const w = await world(rq, S5E3B_WORLD);
  const lan = w.people['Cô Lan']!;
  const an = w.people['Bình An']!;
  const c11 = w.classes['11A1']!;
  const rb = await rb01(rq, lan);
  const bc = await bc01(rq, lan, c11.id, rb.id, { problemVersionId: DKC01_PV });
  const tl = await tl01(rq, lan, c11.id);
  await setHelp(rq, lan, bc.id, level, { knowledgeCodes: [KT], ...extra });
  await ack(rq, an);
  return { w, lan, an, c11, bc, tl };
}

export const setHelp = async (rq: APIRequestContext, t: Person, id: string, helpLevel: string, extra: Record<string, unknown> = {}) =>
  json<{ helpLevel: string; summary: string }>(
    await rq.put(helpUrl(id), { headers: bearer(t.token), data: { helpLevel, ...extra } }),
    200,
  );

export const ack = async (rq: APIRequestContext, s: Person) =>
  json(await rq.post(`${QA}/privacy`, { headers: bearer(s.token), data: { version: PRIVACY_VERSION } }), 200);

export const askRaw = (rq: APIRequestContext, s: Person, assignmentId: string, question: string): Promise<APIResponse> =>
  rq.post(QA, { headers: bearer(s.token), data: { assignmentId, question } });

export async function ask(rq: APIRequestContext, s: Person, assignmentId: string, question: string): Promise<QaReply> {
  const r = await json<QaReply>(await askRaw(rq, s, assignmentId, question), 200);
  return { ...r, source: r.source ?? null }; // máy chủ bỏ khóa null khỏi JSON
}

/** Kịch bản AI giả lập cho mọi lượt của học sinh này (F-S5-094 mặc định; "" = bỏ). */
export async function mock(rq: APIRequestContext, userId: string, scenario: string, delayMs?: number) {
  const r = await rq.post(`${API}/test-hooks/ai`, { data: { userId, scenario, ...(delayMs !== undefined ? { delayMs } : {}) } });
  expect(r.status()).toBe(200);
}

export async function firewall(rq: APIRequestContext, userId: string, data: { delayMs?: number; fault?: boolean }) {
  const r = await rq.post(`${API}/test-hooks/s5e3/qa/firewall`, { data: { userId, ...data } });
  expect(r.status()).toBe(200);
}

/** Yêu cầu đã gửi AI của một lượt (null = lượt không gọi AI). */
export async function prompt(rq: APIRequestContext, interactionId: string): Promise<string | null> {
  return (await json<{ prompt: string | null }>(await rq.get(`${API}/test-hooks/s5e3/qa/prompt?interactionId=${interactionId}`), 200))
    .prompt ?? null;
}

export async function state(rq: APIRequestContext, orgId: string, q: { userId?: string; targetId?: string } = {}): Promise<QaState> {
  const p = new URLSearchParams({ orgId, ...(q.userId ? { userId: q.userId } : {}), ...(q.targetId ? { targetId: q.targetId } : {}) });
  return json<QaState>(await rq.get(`${API}/test-hooks/s5e3/qa/state?${p}`), 200);
}

/** Quyết định của một lượt. */
export async function decisionOf(rq: APIRequestContext, orgId: string, userId: string, interactionId: string): Promise<Decision> {
  const s = await state(rq, orgId, { userId });
  const d = s.decisions.find((x) => x.interaction_id === interactionId);
  expect(d, `AIPolicyDecision của lượt ${interactionId}`).toBeTruthy();
  return { ...d!, rule_code: d!.rule_code ?? null };
}

export async function release(rq: APIRequestContext, orgId: string, gradedId: string) {
  const r = await json<{ updated: number }>(await rq.post(`${API}/test-hooks/s5e3/qa/release`, { data: { orgId, gradedId } }), 200);
  expect(r.updated).toBe(1);
}

export async function lectureApproval(rq: APIRequestContext, approved: boolean) {
  const r = await json<{ updated: number }>(
    await rq.post(`${API}/test-hooks/s5e3/qa/lecture`, { data: { slug: LECTURE_SLUG, approved } }),
    200,
  );
  expect(r.updated).toBe(1);
}

/** Văn bản có lộ nội dung cấm (đáp số, ghi chú riêng, rubric, bài của bạn): trả mục lộ đầu tiên hoặc null. */
export function leak(text: string): string | null {
  const t = text.replace(/\s+/g, ' ');
  for (const f of [...ANSWER_FORMS, PRIVATE_NOTE, RUBRIC_LINE, BN02]) if (t.includes(f)) return f;
  return null;
}
