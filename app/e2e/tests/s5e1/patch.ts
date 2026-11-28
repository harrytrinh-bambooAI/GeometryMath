import type { APIRequestContext, APIResponse } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, hook, type Person } from '../s3e1/s3e1.ts';
import { F020, LP, awaitPlan, createPlan, type PlanJson } from './s5e1.ts';

/**
 * Tiện ích E2E chat vá / duyệt / xuất giáo án (T-261128-54, US-S5E1-04, 05). AI giả lập `lesson_patch` (F-S5-096):
 * mặc định bản vá 7 trường của HĐ đang chọn; biến thể patch-2, patch-locked, patch-badbefore, patch-question,
 * patch-reply; kịch bản theo lượt gọi qua `qaReplies`.
 */

export interface PatchLine {
  index: number;
  op: string;
  path: string;
  label: string;
  before: string | null;
  after: string | null;
  lyDo: string;
  decision: 'pending' | 'accepted' | 'skipped';
}
export interface ChatReply {
  kind: 'patch' | 'question' | 'reply' | 'error';
  text?: string;
  patchId?: string;
  version: number;
  approvedVersion?: number | null;
  humanLabel?: string;
  lines?: PatchLine[];
}
export interface Inspected {
  kind: 'docx' | 'pdf' | 'zip' | 'broken' | 'unknown';
  name: string;
  text?: string;
  pages?: string[];
  headers?: string[];
  footers?: string[];
  tables?: string[][][];
  entries?: Inspected[];
}

/** Giáo án F-S5-021 "Sẵn sàng" (phiên bản 1) của giáo viên. */
export async function readyPlan(rq: APIRequestContext, p: Person): Promise<{ id: string; code: string; plan: PlanJson }> {
  const c = await createPlan(rq, p, F020);
  const plan = await awaitPlan(rq, p, c.draftId);
  expect(plan.status).toBe('ready');
  return { id: c.draftId, code: c.code, plan };
}

/** Kịch bản lesson_patch theo lượt gọi (tên biến thể hoặc JSON nguyên văn). */
export async function patchScript(rq: APIRequestContext, p: Person, replies: string[]) {
  await hook(rq, 'ai', { userId: p.id, qaReplies: replies });
}

export function chat(rq: APIRequestContext, p: Person, id: string, section: string | null, message: string): Promise<APIResponse> {
  return rq.post(`${LP}/${id}/chat`, { headers: bearer(p.token), data: { section, message } });
}

export async function chatOk(rq: APIRequestContext, p: Person, id: string, section: string | null, message: string): Promise<ChatReply> {
  const r = await chat(rq, p, id, section, message);
  expect(r.status(), await r.text()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  return (await r.json()) as ChatReply;
}

export async function accept(rq: APIRequestContext, p: Person, id: string, patchId: string, lines: number[] | null = null): Promise<ChatReply> {
  const r = await rq.post(`${LP}/${id}/patches/${patchId}/accept`, { headers: bearer(p.token), data: { lines } });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as ChatReply;
}

export async function lock(rq: APIRequestContext, p: Person, id: string, section: string, locked = true) {
  const r = await rq.put(`${LP}/${id}/locks`, { headers: bearer(p.token), data: { section, locked } });
  expect(r.status(), await r.text()).toBe(200);
}

/** GET giáo án (màn kết quả) kèm trường duyệt / phiên bản. */
export async function view(rq: APIRequestContext, p: Person, id: string) {
  const r = await rq.get(`${LP}/${id}`, { headers: bearer(p.token) });
  expect(r.status()).toBe(200);
  return (await r.json()) as PlanJson & {
    version: number;
    approvedVersion: number | null;
    approvedBy: string | null;
    approvedAt: string | null;
    lesson: PlanJson['lesson'] & { hoatDong: Array<{ ma: string; ten: string; noiDung: string; thoiLuongPhut: number }> };
  };
}

/** Tải tệp xuất rồi đọc lại bằng móc kiểm thử (POI / pdfbox phía máy chủ). */
export async function download(rq: APIRequestContext, url: string, token: string) {
  const r = await rq.get(url, { headers: bearer(token) });
  expect(r.status(), r.status() === 200 ? '' : await r.text()).toBe(200);
  const cd = r.headers()['content-disposition'] ?? '';
  const name = decodeURIComponent(/filename\*=UTF-8''([^;]+)/.exec(cd)?.[1] ?? /filename="([^"]+)"/.exec(cd)?.[1] ?? '');
  return { res: r, name, body: await r.body() };
}

export async function inspect(rq: APIRequestContext, body: Buffer, name = 'file'): Promise<Inspected> {
  const r = await rq.post(`${API}/test-hooks/export/inspect?name=${encodeURIComponent(name)}`, {
    headers: { 'Content-Type': 'application/octet-stream' },
    data: body,
  });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Inspected;
}

export const DRAFT_FOOTER = 'Bản nháp AI soạn – chưa duyệt';
export const MSG05 = 'Mục này đang khoá. Mở khoá trước khi sửa.';
