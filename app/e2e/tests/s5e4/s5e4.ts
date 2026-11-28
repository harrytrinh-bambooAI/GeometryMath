import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, hook, world, type ClassSpec, type Person, type World } from '../s3e1/s3e1.ts';

/**
 * Tiện ích E2E S5-E4 (T-261128-57): rubric có phiên bản (US-S5E4-01), bài có chấm (US-S5E4-02), kho ảnh bài làm
 * (US-S5E4-08). Mỗi ca dựng thế giới riêng qua móc S3-E1 (chạy song song an toàn): cô Lan chủ 11A1 với 8 em Đang học
 * (F-S5-010) + Văn Tú Đã rời; thầy Khôi chủ 10B. Đề: đề kho 's3_tuan' TUAN-11 (bộ s5_core của F-S5-050 chỉ cấp cho trường
 * mẫu F-S2-100, thế giới riêng không thấy). Học sinh "nộp ảnh" qua móc /test-hooks/s5e4/image (luồng chụp nộp US-S5E4-03 là
 * của T-261128-59). AI giả lập.
 */
export const EIGHT = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Khánh Linh', 'Minh Quân', 'Ngọc Anh'];

export const S5E4_WORLD: { teachers: string[]; students: string[]; classes: ClassSpec[] } = {
  teachers: ['Cô Lan', 'Thầy Khôi'],
  students: [],
  classes: [
    { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: EIGHT, left: ['Văn Tú'] },
    { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na'] },
  ],
};

export const s5e4World = (rq: APIRequestContext): Promise<World> => world(rq, S5E4_WORLD);

export const RUBRICS = `${API}/api/v1/teacher/rubrics`;
export const GA = `${API}/api/v1/teacher/graded-assignments`;
export const STUDENT_GA = `${API}/api/v1/student/graded-assignments`;
export const pageUrl = (submissionId: string, page = 1) => `${API}/api/v1/submissions/${submissionId}/pages/${page}/url`;

/** BC-01: hạn T6 11/12/2026 20:00 giờ Việt Nam (F-S5-052). */
export const BC01_DUE = '2026-12-11T13:00:00Z';
export const BC01_TITLE = 'Kiểm tra 15 phút – Khoảng cách';

export const MSG = {
  s30: 'Tổng điểm các tiêu chí phải bằng thang điểm của bài.',
  s31: 'Rubric này đã dùng cho bài đã giao. Lưu sẽ tạo phiên bản mới; bài đã giao giữ phiên bản cũ.',
} as const;

export interface Criterion {
  ord?: number;
  name: string;
  description?: string;
  maxPoints: number;
  directionDependent?: boolean;
}
export interface Rubric {
  id: string;
  code: string;
  version: number;
  versionId: string;
  currentVersion: number;
  name: string;
  scale: number;
  total: number;
  criteria: Criterion[];
  guide: string[];
  used: boolean;
  message?: string;
}
export interface Graded {
  id: string;
  code: string;
  title: string;
  classId: string;
  rubric: { id: string; code: string; version: number; versionId: string; name: string };
  dueAt: string;
  allowLate: boolean;
  gradingMode: string;
  gradingModeLabel: string;
  releaseMode: string;
  status: string;
  statusLabel: string;
  submissions: number;
}
export type Err = { code: string; message: string; details?: Array<{ field: string; message: string }> };

/** RB-01 "Tính toán – khoảng cách" từ mẫu Tính toán (F-S5-051). */
export async function rb01(rq: APIRequestContext, t: Person): Promise<Rubric> {
  const r = await rq.post(RUBRICS, { headers: bearer(t.token), data: { fromTemplate: 'tinh-toan', name: 'Tính toán – khoảng cách' } });
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as Rubric;
}

export async function problemVersion(rq: APIRequestContext, t: Person, code = 'TUAN-11'): Promise<string> {
  const p = await rq.get(`${API}/api/v1/teacher/practice/problems`, { headers: bearer(t.token) });
  expect(p.status()).toBe(200);
  const items = ((await p.json()) as { items: Array<{ code: string; problemVersionId: string }> }).items;
  const pv = items.find((i) => i.code === code)?.problemVersionId;
  expect(pv, `đề kho ${code}`).toBeTruthy();
  return pv!;
}

export function bc01Body(classId: string, rubricId: string, pv: string, extra: Record<string, unknown> = {}) {
  return {
    title: BC01_TITLE,
    classId,
    problemVersionId: pv,
    rubricId,
    dueAt: BC01_DUE,
    allowLate: true,
    gradingMode: 'manual',
    releaseMode: 'on_return',
    ...extra,
  };
}

/** Giao BC-01 (F-S5-052) cho lớp, trả bài đã tạo. */
export async function bc01(rq: APIRequestContext, t: Person, classId: string, rubricId: string, extra: Record<string, unknown> = {}) {
  const pv = await problemVersion(rq, t);
  const r = await rq.post(GA, { headers: bearer(t.token), data: bc01Body(classId, rubricId, pv, extra) });
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as Graded;
}

/** Bài có chấm BC-01 với RB-01 v1 của cô Lan trên 11A1 (thế giới mới). */
export async function setup(rq: APIRequestContext) {
  const w = await s5e4World(rq);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const rb = await rb01(rq, lan);
  const bc = await bc01(rq, lan, c11.id, rb.id);
  return { w, lan, c11, rb, bc };
}

const here = dirname(fileURLToPath(import.meta.url));
/** Ảnh mẫu exif-gps.jpg: GPS 21.0285,105.8542, Make Apple, Model iPhone 15 (fixtures/make-exif-gps.py). */
export const EXIF_JPG = readFileSync(join(here, 'fixtures', 'exif-gps.jpg'));

export interface StoredImage {
  submissionId: string;
  submissionIds: string[];
  ids: string[];
  sha256: string;
  bytes: number;
}

/** Học sinh "nộp" ảnh một trang (móc; luồng chụp nộp thật là của T-261128-59). */
export async function submitImage(
  rq: APIRequestContext,
  w: World,
  assignmentId: string,
  student: Person,
  opts: { submissionId?: string; pageNo?: number; count?: number; schoolYear?: string; bytes?: Buffer } = {},
): Promise<StoredImage> {
  return (await hook(rq, 's5e4/image', {
    orgId: w.orgId,
    assignmentId,
    studentId: student.id,
    submissionId: opts.submissionId ?? null,
    pageNo: opts.pageNo ?? 1,
    base64: (opts.bytes ?? EXIF_JPG).toString('base64'),
    count: opts.count ?? 1,
    schoolYear: opts.schoolYear ?? null,
  })) as unknown as StoredImage;
}

export const sha256 = (b: Buffer) => createHash('sha256').update(b).digest('hex');

/** Có đoạn APP1 (EXIF/XMP) hoặc chuỗi EXIF/GPS/thiết bị trong tệp ảnh không. */
export function hasMetadata(b: Buffer): boolean {
  if (b.includes(Buffer.from('Exif\0\0', 'latin1'))) return true;
  if (b.includes(Buffer.from('iPhone', 'latin1')) || b.includes(Buffer.from('Apple', 'latin1'))) return true;
  if (b.includes(Buffer.from('http://ns.adobe.com/xap', 'latin1'))) return true;
  // duyệt các đoạn JPEG trước SOS
  let i = 2;
  while (i + 4 <= b.length && b[i] === 0xff) {
    const m = b[i + 1]!;
    if (m === 0xda) break;
    if (m >= 0xe1 && m <= 0xef) return true;
    i += 2 + b.readUInt16BE(i + 2);
  }
  return false;
}

export const isJpeg = (b: Buffer) => b.length > 3 && b[0] === 0xff && b[1] === 0xd8 && b[2] === 0xff;

export async function state(rq: APIRequestContext, orgId: string, q: { assignmentId?: string; submissionId?: string; schoolYear?: string; ids?: string[] }) {
  const p = new URLSearchParams({ orgId });
  if (q.assignmentId) p.set('assignmentId', q.assignmentId);
  if (q.submissionId) p.set('submissionId', q.submissionId);
  if (q.schoolYear) p.set('schoolYear', q.schoolYear);
  if (q.ids) p.set('ids', q.ids.join(','));
  const r = await rq.get(`${API}/test-hooks/s5e4/state?${p}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as { images: number; objects: number; audits: Array<{ action: string; targetId: string | null; detail: string; n: number }> };
}
