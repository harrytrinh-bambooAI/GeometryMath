import type { APIRequestContext, APIResponse, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';

/**
 * Tiện ích E2E của S2-E3 (thêm đề có AI soạn nháp, khung C2-02). Dữ liệu: app/db/seed/e2e/840_s2e3_core.sql +
 * fixture-ids.d/s2e3.json. Mỗi ca một giáo viên riêng theo (mã TC, làn = dự án Playwright): nháp tính giới hạn 20 đề
 * mỗi giờ và kịch bản AI giả lập đặt theo người dùng (móc /test-hooks/ai).
 */
export { API, bearer };

export const ORG = '00000000-0000-7000-8002-000000840001';
export const LAN = '00000000-0000-7000-8002-000000840002';
const LANES = ['api', 'ui-chromium', 'ui-webkit', 'ui-iphone', 'mobile-iphone', 'mobile-pixel'];
export const lane = (info: TestInfo) => Math.max(0, LANES.indexOf(info.project.name));
const f = (n: number) => `00000000-0000-7000-8002-${String(n).padStart(12, '0')}`;

/** Giáo viên riêng của ca (TC, làn). */
export const teacher = (tc: number, info: TestInfo) => f(841000000 + tc * 100 + lane(info));

export const FX10 = 'Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.';
export const FX14 =
  'Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng đáy.';
export const ELIP = 'Cho elip (E) có hai tiêu điểm A, B với AB = 6 và điểm M thuộc (E); tính chu vi tam giác MAB.';

export const MSG = {
  empty: 'Hãy dán hoặc gõ đề',
  short: 'Đề quá ngắn (tối thiểu 10 ký tự)',
  long: 'Đề tối đa 2000 ký tự',
  oneImage: 'Mỗi lần tải một ảnh',
  imageTooBig: 'Ảnh tối đa 5 MB',
  badType: 'Chỉ nhận ảnh JPG hoặc PNG',
  noText: 'Máy không đọc được chữ trong ảnh, hãy gõ lại đề',
  rate: 'Bạn thêm đề hơi nhanh, thử lại sau',
  aiFigure: 'Cần xem lại · hình do AI dựng',
  askAiFigure: 'Hình do AI dựng, chưa kiểm chứng. Vẫn giao?',
  verified: 'Đã kiểm chứng',
  needsReview: 'Cần xem lại',
  ai: 'AI soạn',
  blocked: 'Chặn duyệt: đáp số chưa khớp',
};

export async function tokenFor(request: APIRequestContext, userId: string, role: 'TEACHER' | 'STUDENT' = 'TEACHER'): Promise<string> {
  const r = await request.post(`${API}/dev/token`, { data: { orgId: ORG, userId, role } });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { token: string }).token;
}

/** Đặt kịch bản AI giả lập cho một người dùng ("" = bỏ). */
export async function aiScenario(request: APIRequestContext, userId: string, scenario: string): Promise<void> {
  const r = await request.post(`${API}/test-hooks/ai`, { data: { userId, scenario } });
  expect(r.status(), await r.text()).toBe(200);
}

/** Đăng nhập giáo viên của ca trên trình duyệt (thẻ dev) rồi mở một màn. */
export async function signIn(page: Page, request: APIRequestContext, userId: string, path: string): Promise<void> {
  const token = await tokenFor(request, userId);
  const me = { role: 'TEACHER', userId, displayName: 'Cô Trang' };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [token, JSON.stringify(me)] as const,
  );
  await page.goto(path);
}

const BASE = `${API}/api/v1/teacher/problem-drafts`;
export const draftsUrl = BASE;
export const jobUrl = (id: string) => `${API}/api/v1/teacher/problem-draft-jobs/${id}`;

export interface Draft {
  id: string;
  status: string;
  statement: string;
  machineLabel: string;
  cells: Array<{ index: number; text: string; state: string; confidence: number }>;
  job?: { id: string; state: string; stages: Array<{ key: string; state: string; error?: string | null; aiCalls: number }> } | null;
  result?: { docId?: string | null; machineLabel: string; humanLabel: string; answers?: { stated?: string | null; ai?: string | null; computed?: string | null; state: string } | null } | null;
}

export async function ok<T>(r: APIResponse): Promise<T> {
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as T;
}

export const createDraft = (request: APIRequestContext, t: string, statement: string, source = 'text') =>
  request.post(BASE, { headers: bearer(t), data: { source, statement } });

/** Tạo nháp chữ, dựng và chờ tác vụ kết thúc (tối đa 20 giây). */
export async function buildToEnd(request: APIRequestContext, t: string, statement: string): Promise<Draft> {
  const d = await ok<Draft>(await createDraft(request, t, statement));
  const b = await ok<Draft>(await request.post(`${BASE}/${d.id}/build`, { headers: bearer(t), data: {} }));
  const jobId = b.job!.id;
  await expect
    .poll(async () => ((await ok<{ state: string }>(await request.get(jobUrl(jobId), { headers: bearer(t) }))).state), { timeout: 20_000 })
    .not.toBe('running');
  return ok<Draft>(await request.get(`${BASE}/${d.id}`, { headers: bearer(t) }));
}

/** Tải ảnh theo đoạn 1 000 000 byte như màn hình (thân yêu cầu của máy chủ ≤ 1 MB). */
export async function upload(request: APIRequestContext, t: string, draftId: string, bytes: Buffer): Promise<APIResponse> {
  let last: APIResponse | null = null;
  for (let off = 0; off < bytes.length; off += 1_000_000) {
    const chunk = bytes.subarray(off, Math.min(bytes.length, off + 1_000_000));
    last = await request.put(`${BASE}/${draftId}/image?offset=${off}&total=${bytes.length}`, {
      headers: { ...bearer(t), 'Content-Type': 'application/octet-stream' },
      data: chunk,
    });
    if (last.status() !== 200) return last;
  }
  return last!;
}

/** Ảnh JPEG mẫu: EXIF có GPS + dấu đề cho AI giả lập (GEO-S2E3:<TÊN>), độn tới đúng {@code size} byte. */
export function jpeg(marker: string, size?: number): Buffer {
  const seg = (m: number, body: Buffer) => Buffer.concat([Buffer.from([0xff, m, (body.length + 2) >> 8, (body.length + 2) & 0xff]), body]);
  const head = Buffer.concat([
    Buffer.from([0xff, 0xd8]),
    seg(0xe0, Buffer.from('JFIF\0', 'latin1')),
    seg(0xe1, Buffer.from('Exif\0\0GPSLatitude=21.0285', 'latin1')),
    seg(0xfe, Buffer.from(`GEO-S2E3:${marker}`, 'latin1')),
    Buffer.from([0xff, 0xda, 0x00, 0x02]),
  ]);
  const tail = Buffer.from([0xff, 0xd9]);
  const pad = Math.max(0, (size ?? head.length + 64) - head.length - tail.length);
  return Buffer.concat([head, Buffer.alloc(pad, 0x11), tail]);
}

export const stage = (page: Page, key: string) => page.getByTestId(`s2e3-stage-${key}`);
