import { createHash } from 'node:crypto';
import type { APIRequestContext, APIResponse } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, world, type ClassSpec, type Person, type World } from '../s3e1/s3e1.ts';

/**
 * Tiện ích E2E S5-E3 kho tri thức (T-261128-56, US-S5E3-01, 02, 07). Mỗi ca dựng thế giới riêng qua móc S3-E1 (chạy song
 * song an toàn): cô Lan chủ 11A1 (Bình An, Chi Mai Đang học), thầy Khôi chủ 10B (Mi Na). Kho F-S5-040 dựng bằng API thật:
 * `TL-01` "Ghi chú Khoảng cách 11A1" v2 Đang dùng (v1 Lưu trữ) gắn 11A1; `TL-02` "Phiếu ôn chương VII" v1 Chờ duyệt.
 * Truy xuất (US-S5E3-07) gọi bộ truy xuất thật qua móc /test-hooks/s5e3/retrieve (chưa có hỏi đáp ở đợt W3).
 */
export const KB = `${API}/api/v1/teacher/knowledge`;
export const classKb = (classId: string) => `${API}/api/v1/teacher/classes/${classId}/knowledge`;

export const S5E3_WORLD: { teachers: string[]; students: string[]; classes: ClassSpec[] } = {
  teachers: ['Cô Lan', 'Thầy Khôi'],
  students: [],
  classes: [
    { key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An', 'Chi Mai'] },
    { key: '10B', teacher: 'Thầy Khôi', name: '10B', grade: 10, active: ['Mi Na'] },
  ],
};

export const s5e3World = (rq: APIRequestContext): Promise<World> => world(rq, S5E3_WORLD);

export const MSG = {
  s20: 'Thầy cô chưa cung cấp tài liệu cho phần này. Em hỏi thầy cô trên lớp nhé.',
  s22: 'Tệp tối đa 10 MB, định dạng docx, pdf hoặc md.',
  s23: 'Phiên bản này đã được dùng, sửa sẽ tạo phiên bản mới.',
} as const;

export const KT = 'KT11-C07-CT-003';

/** TL-01 v1 (bản đầu) và v2 (bản đang dùng): mục "Hệ thức 1/AH²" gắn mã KT11-C07-CT-003. */
export const TL01_V1 = `# Ghi chú Khoảng cách 11A1

## Khoảng cách từ điểm đến mặt phẳng
Khoảng cách từ điểm M đến mặt phẳng (P) bằng độ dài MH, với H là hình chiếu vuông góc của M lên (P).
`;
export const TL01_V2 = `# Ghi chú Khoảng cách 11A1

## Khoảng cách từ điểm đến mặt phẳng
Khoảng cách từ điểm M đến mặt phẳng (P) bằng độ dài MH, với H là hình chiếu vuông góc của M lên (P). Muốn tính khoảng cách, dựng hình chiếu rồi tính độ dài đoạn vuông góc.

## Hệ thức 1/AH²
Mã ${KT}. Nếu SA ⊥ (ABC), AK ⊥ BC, AH ⊥ SK thì AH ⊥ (SBC) và 1/AH² = 1/SA² + 1/AK². Dùng để tính khoảng cách từ A đến (SBC).

## Góc giữa hai mặt phẳng
Góc giữa hai mặt phẳng là góc giữa hai đường thẳng lần lượt vuông góc với giao tuyến, nằm trong hai mặt.
`;
export const TL02 = `# Phiếu ôn chương VII

## Câu 1
Cho hình chóp S.ABC có SA vuông góc với đáy. Chứng minh BC vuông góc với mặt phẳng (SAB).
`;

export interface Section {
  ord: number;
  title: string;
  body: string;
  codes: string[];
}
export interface DocView {
  id: string;
  code: string;
  title: string;
  ownerId: string;
  humanLabel: string;
  activeVersion?: number;
  version: number;
  status: 'draft' | 'pending' | 'active' | 'archived';
  statusLabel: string;
  sourceKind: string;
  sourceLabel?: string;
  fileName?: string;
  contentHash: string;
  content: string;
  sections: Section[];
  versions: Array<{ version: number; status: string; statusLabel: string }>;
  message?: string;
  duplicate?: boolean;
}

export async function json<T>(r: APIResponse, status: number): Promise<T> {
  expect(r.status(), await r.text()).toBe(status);
  return (await r.json()) as T;
}

export const createMd = (rq: APIRequestContext, t: Person, title: string, content: string) =>
  rq.post(KB, { headers: bearer(t.token), data: { title, content } });

export const submit = (rq: APIRequestContext, t: Person, ref: string, v: number) =>
  rq.post(`${KB}/${ref}/versions/${v}/submit`, { headers: bearer(t.token) });
export const activate = (rq: APIRequestContext, t: Person, ref: string, v: number) =>
  rq.post(`${KB}/${ref}/versions/${v}/activate`, { headers: bearer(t.token) });
export const edit = (rq: APIRequestContext, t: Person, ref: string, v: number, content: string) =>
  rq.put(`${KB}/${ref}/versions/${v}`, { headers: bearer(t.token), data: { content } });
export const view = async (rq: APIRequestContext, t: Person, ref: string, v?: number) =>
  json<DocView>(await rq.get(`${KB}/${ref}${v ? `?version=${v}` : ''}`, { headers: bearer(t.token) }), 200);
export const grant = (rq: APIRequestContext, t: Person, ref: string, data: Record<string, unknown>) =>
  rq.post(`${KB}/${ref}/grants`, { headers: bearer(t.token), data });

/** Nháp → Chờ duyệt → Đang dùng. */
export async function publish(rq: APIRequestContext, t: Person, ref: string, v: number) {
  await json(await submit(rq, t, ref, v), 200);
  return json<DocView>(await activate(rq, t, ref, v), 200);
}

/** TL-01 của F-S5-040: v2 Đang dùng, v1 Lưu trữ; gắn lớp nếu có classId. */
export async function tl01(rq: APIRequestContext, lan: Person, classId?: string): Promise<DocView> {
  const d = await json<DocView>(await createMd(rq, lan, 'Ghi chú Khoảng cách 11A1', TL01_V1), 201);
  expect(d.code).toMatch(/^TL-\d{2}$/);
  await publish(rq, lan, d.id, 1);
  const v2 = await json<DocView>(await edit(rq, lan, d.id, 1, TL01_V2), 201);
  expect(v2.version).toBe(2);
  const a = await publish(rq, lan, d.id, 2);
  expect(a.activeVersion).toBe(2);
  if (classId) await json(await grant(rq, lan, d.id, { classId }), 201);
  return a;
}

/** TL-02 của F-S5-040: v1 Chờ duyệt. */
export async function tl02(rq: APIRequestContext, lan: Person): Promise<DocView> {
  const d = await json<DocView>(await createMd(rq, lan, 'Phiếu ôn chương VII', TL02), 201);
  return json<DocView>(await submit(rq, lan, d.id, 1), 200);
}

/** Tệp mẫu "Phiếu ôn chương VII" (docx dựng ở máy chủ, cỡ kb KB, cùng tham số cùng byte). */
export async function sampleDocx(rq: APIRequestContext, kb = 240): Promise<Buffer> {
  const r = await rq.get(`${API}/test-hooks/s5e3/sample-docx?kb=${kb}`);
  expect(r.status()).toBe(200);
  return r.body();
}

export const sha256 = (b: Buffer) => createHash('sha256').update(b).digest('hex');

/** Bắt đầu lượt tải (khai tên + cỡ). */
export const startUpload = (rq: APIRequestContext, t: Person, fileName: string, sizeBytes: number) =>
  rq.post(`${KB}/uploads`, { headers: bearer(t.token), data: { fileName, sizeBytes } });

/** Tải tệp theo đoạn ≤ 768 KB rồi tạo tài liệu; trả phản hồi tạo (201 mới / 200 trùng). */
export async function uploadDoc(rq: APIRequestContext, t: Person, fileName: string, bytes: Buffer): Promise<APIResponse> {
  const u = await json<{ uploadId: string; partBytes: number }>(await startUpload(rq, t, fileName, bytes.length), 201);
  for (let off = 0; off < bytes.length; off += u.partBytes) {
    const part = bytes.subarray(off, Math.min(bytes.length, off + u.partBytes));
    const r = await rq.put(`${KB}/uploads/${u.uploadId}?offset=${off}`, {
      headers: { ...bearer(t.token), 'Content-Type': 'application/octet-stream' },
      data: part,
    });
    expect(r.status(), await r.text()).toBe(200);
  }
  return rq.post(KB, { headers: bearer(t.token), data: { uploadId: u.uploadId } });
}

export interface Retrieved {
  items: Array<{ docCode: string; version: number; ord: number; title: string; codes: string[]; score: number; boosted: boolean }>;
  message?: string;
  aiCalled: boolean;
  minScore: number;
  tookMs: number;
}

/** Bộ truy xuất thật qua móc e2e (như một lượt hỏi đáp của học sinh trong lớp / bài). */
export async function retrieve(
  rq: APIRequestContext,
  body: { orgId: string; classId: string; assignmentId?: string | null; query: string; knowledgeCodes?: string[] },
): Promise<Retrieved> {
  return json<Retrieved>(await rq.post(`${API}/test-hooks/s5e3/retrieve`, { data: body }), 200);
}
