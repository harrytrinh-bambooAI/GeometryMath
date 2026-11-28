import { randomUUID } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { request as pw, type APIRequestContext, type APIResponse } from '@playwright/test';
import { expect } from '../../../support/fixtures.ts';
import { API, bearer, type Person } from '../../s3e1/s3e1.ts';

/**
 * Tiện ích E2E nộp bài viết tay (T-261128-59; US-S5E4-03, 04, 09): luồng thật học sinh — đọc thông báo riêng tư S5 →
 * tải từng trang theo đoạn (thân yêu cầu ≤ 1 MB) → POST /api/v1/submissions kèm khóa lũy đẳng. Bản đọc chữ chạy nền
 * (task grade_ocr, AI giả lập F-S5-095). Thế giới dựng bằng setup() của S5-E4 (s5e4.ts).
 */
export const SUB = `${API}/api/v1/submissions`;

const here = dirname(fileURLToPath(import.meta.url));
const fx = (n: string) => readFileSync(join(here, 'fixtures', n));
/** Trang rõ, không EXIF (fixtures/make-fixtures.py). */
export const P1 = fx('bn01-p1.jpg');
export const P2 = fx('bn01-p2.jpg');
/** Trang 2 nhoè của Đức Huy (F-S5-055). */
export const BLUR = fx('bn03-p2-mo.jpg');
/** Trang 1 dạng HEIC (US-S5E4-03-SC2). */
export const HEIC = fx('bn01-p1.heic');
export const FIX = (n: string) => join(here, 'fixtures', n);

export const MSG = {
  s33: /^Đã nộp lúc \d{2}:\d{2}, \d{2}\/\d{2}\/\d{4}\. Đang chờ thầy cô chấm\.$/,
  s34: 'Đã quá hạn nộp. Thầy cô không nhận bài muộn cho bài này.',
  s35: 'Mất kết nối. Bài của em đã lưu trên máy và sẽ gửi lại khi có mạng.',
  full: 'Máy em hết chỗ lưu, hãy nộp các trang đã chụp hoặc xoá bớt ảnh',
} as const;

export interface SubPage {
  pageNo: number;
  contentType: string;
  sha256: string;
  bytes: number;
  derivedContentType?: string;
}
export interface SubVersion {
  version: number;
  versionId: string;
  submittedAt: string;
  late: boolean;
  lateLabel?: string;
  pages: SubPage[];
}
export interface Submission extends SubVersion {
  id: string;
  code: string;
  assignmentId: string;
  status: string;
  statusLabel: string;
  message: string;
  gradingStatusLabel?: string;
  versions?: SubVersion[];
}

/** Học sinh bấm "Em đã hiểu" thông báo riêng tư S5. */
export async function ack(rq: APIRequestContext, p: Person) {
  const r = await rq.post(`${SUB}/privacy`, { headers: bearer(p.token), data: { version: 'privacy-s5-1.0' } });
  expect(r.status(), await r.text()).toBe(200);
}

/**
 * Ngữ cảnh HTTP riêng cho đoạn ảnh nhị phân: fixture `request` ghi nhật ký thân yêu cầu (redact → JSON) nên Buffer vài MB
 * làm tràn bộ nhớ worker; đoạn ảnh đi qua ngữ cảnh này (không ghi thân), các lời gọi JSON vẫn qua fixture.
 */
let rawCtx: Promise<APIRequestContext> | null = null;
const raw = () => (rawCtx ??= pw.newContext());

/** Bật AI chấm của lớp (US-S5E5-04; mặc định tắt) để việc đọc chữ nền được nhận lượt. */
export async function aiOn(rq: APIRequestContext, teacher: Person, classId: string) {
  const r = await rq.put(`${API}/api/v1/teacher/my-classes/${classId}/ai-switches`, { headers: bearer(teacher.token), data: { aiGrading: true } });
  expect(r.status(), await r.text()).toBe(200);
}

/** Tải một trang theo đoạn; trả uploadId. */
export async function upload(rq: APIRequestContext, p: Person, bytes: Buffer, contentType = 'image/jpeg'): Promise<string> {
  const s = await rq.post(`${SUB}/uploads`, { headers: bearer(p.token), data: { contentType, sizeBytes: bytes.length } });
  expect(s.status(), await s.text()).toBe(201);
  const u = (await s.json()) as { uploadId: string; partBytes: number };
  for (let off = 0; off < bytes.length; off += u.partBytes) {
    const r = await (await raw()).put(`${SUB}/uploads/${u.uploadId}?offset=${off}`, {
      headers: { ...bearer(p.token), 'Content-Type': 'application/octet-stream' },
      data: Buffer.from(bytes.subarray(off, Math.min(bytes.length, off + u.partBytes))),
    });
    expect(r.status()).toBe(200);
  }
  return u.uploadId;
}

export const submitRaw = (
  rq: APIRequestContext,
  p: Person,
  assignmentId: string,
  pages: Array<{ uploadId: string; derivedUploadId?: string; clarity?: number }>,
  key: string = randomUUID(),
): Promise<APIResponse> =>
  rq.post(SUB, { headers: bearer(p.token), data: { assignmentId, idempotencyKey: key, pages: pages.map((x) => ({ clarity: 0.9, ...x })) } });

/** Tải các trang rồi nộp; trả phản hồi. */
export async function submitFiles(rq: APIRequestContext, p: Person, assignmentId: string, files: Buffer[], key: string = randomUUID()) {
  const pages = [];
  for (const f of files) pages.push({ uploadId: await upload(rq, p, f) });
  return submitRaw(rq, p, assignmentId, pages, key);
}

/** Nộp đạt (201) và trả bài nộp. */
export async function submitOk(rq: APIRequestContext, p: Person, assignmentId: string, files: Buffer[], key?: string) {
  const r = await submitFiles(rq, p, assignmentId, files, key);
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as Submission;
}

export async function getSub(rq: APIRequestContext, p: Person, id: string) {
  const r = await rq.get(`${SUB}/${id}`, { headers: bearer(p.token) });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Submission;
}

/** Dời hạn nộp của bài về giờ máy chủ + secondsFromNow (móc dev/test). */
export async function due(rq: APIRequestContext, orgId: string, assignmentId: string, secondsFromNow: number, allowLate?: boolean) {
  const r = await rq.post(`${API}/test-hooks/s5e4/submit/due`, { data: { orgId, assignmentId, secondsFromNow, allowLate } });
  expect(r.status()).toBe(200);
}

export async function count(rq: APIRequestContext, orgId: string, assignmentId: string, studentId?: string): Promise<number> {
  const q = new URLSearchParams({ orgId, assignmentId });
  if (studentId) q.set('studentId', studentId);
  const r = await rq.get(`${API}/test-hooks/s5e4/submit/count?${q}`);
  expect(r.status()).toBe(200);
  return ((await r.json()) as { submissions: number }).submissions;
}

export async function subState(rq: APIRequestContext, orgId: string, submissionId: string, studentId: string) {
  const r = await rq.get(`${API}/test-hooks/s5e4/submit/state?${new URLSearchParams({ orgId, submissionId, studentId })}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as { submissions: number; keyed_versions: number; versions: number; ocr: number; uploads: number; acks: number };
}

export interface Ocr {
  status: 'pending' | 'done' | 'failed';
  statusLabel: string;
  canRetry: boolean;
  version?: number;
  editedBy?: string;
  aiAuthored?: boolean;
  pages: Array<{ pageNo: number; segments: Array<{ text: string; confidence: number; unsure: boolean; region?: { x: number; y: number; w: number; h: number } }> }>;
  versions: Array<{ version: number; editedBy?: string }>;
}

/** Chờ việc đọc chữ nền xong (status khác pending). */
export async function waitOcr(rq: APIRequestContext, teacher: Person, submissionId: string): Promise<Ocr> {
  let last: Ocr | null = null;
  await expect
    .poll(
      async () => {
        const r = await rq.get(`${SUB}/${submissionId}/ocr`, { headers: bearer(teacher.token) });
        expect(r.status()).toBe(200);
        last = (await r.json()) as Ocr;
        return last.status;
      },
      { timeout: 20_000, intervals: [200, 300, 500, 1000] },
    )
    .not.toBe('pending');
  return last!;
}
