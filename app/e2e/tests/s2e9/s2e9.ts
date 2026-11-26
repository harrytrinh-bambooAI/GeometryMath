import { readFileSync } from 'node:fs';
import type { APIRequestContext, APIResponse, Download, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';
import { assign, createDraft, type ItemSpec, type Sandbox, type SetDetail } from '../s2e8/s2e8.ts';

/**
 * Tiện ích E2E của S2-E9. Biến thể / xuất / liên kết chỉ xem: dữ liệu app/db/seed/e2e/870_s2e9_core.sql +
 * fixture-ids.d/s2e9.json — mỗi (TC, làn = dự án Playwright) MỘT tổ chức riêng, gốc B = 870000000 + TC × 100000 +
 * làn × 10000. Câu trắc nghiệm: mỗi ca tự dựng lớp qua móc sandbox của S2-E8 rồi soạn, giao bằng API thật.
 */
export { API, bearer };

const LANES = ['api', 'ui-chromium', 'ui-webkit', 'ui-iphone', 'mobile-iphone', 'mobile-pixel'];
export const f = (n: number) => `00000000-0000-7000-8002-${String(n).padStart(12, '0')}`;
export const laneOf = (info: TestInfo) => Math.max(0, LANES.indexOf(info.project.name));

/** Mã đề theo seed 870. */
export const K = {
  FX10: 100,
  FX11: 101,
  FX12: 102,
  FX13: 103,
  FX14: 104,
  TEXT_ONLY: 105,
  SECTION: 106,
  FX11_AI: 107,
  SQUARE_1: 108,
  SQUARE_100: 109,
  NARROW: 110,
  /** "Đề số 1…25" của TC 19 */
  NUMBERED: 200,
};

export interface Slot {
  org: string;
  trang: string;
  lan: string;
  an: string;
  doc: (k: number) => string;
}

export function slot(tc: number, info: TestInfo): Slot {
  const b = 870000000 + tc * 100000 + laneOf(info) * 10000;
  return { org: f(b + 1), trang: f(b + 2), lan: f(b + 4), an: f(b + 5), doc: (k) => f(b + k) };
}

export const MSG = {
  variantCount: 'Chọn từ 1 đến 10',
  noNumbers: 'Đề này không có số liệu để đổi',
  unsupported: 'Dạng này chưa hỗ trợ biến thể',
  exportMin: 'Chọn ít nhất 1 đề',
  exportMax: 'Tối đa 20 đề mỗi tệp',
  needApproval: 'Cần duyệt lời giải trước',
  confirmLocked: 'File xuất có lời giải, lớp có thể thấy nếu giáo viên chia sẻ. Vẫn xuất?',
  linkGone: 'Liên kết không còn dùng được',
  linkMax: 'Tối đa 5 liên kết đang dùng',
  choiceCount: 'Cần từ 2 đến 5 lựa chọn',
  choiceEmpty: 'Lựa chọn đang trống',
  choiceNoCorrect: 'Hãy chọn một đáp án đúng',
  choiceManyCorrect: 'Chỉ một đáp án đúng',
  choiceDuplicate: 'Hai lựa chọn giống nhau',
  wrongLast: 'Chưa đúng. Em xem lại cùng thầy cô nhé.',
  right: 'Đúng rồi!',
};

export async function token(request: APIRequestContext, org: string, userId: string, role: 'TEACHER' | 'STUDENT' = 'TEACHER') {
  const res = await request.post(`${API}/dev/token`, { data: { orgId: org, userId, role } });
  expect(res.status()).toBe(200);
  return ((await res.json()) as { token: string }).token;
}

/** Đăng nhập sẵn trên trình duyệt bằng thẻ dev (ca không kiểm chính luồng đăng nhập). */
export async function signInTeacher(page: Page, request: APIRequestContext, s: Slot, userId = s.trang, displayName = 'Cô Trang') {
  const t = await token(request, s.org, userId);
  await page.addInitScript(
    ([tk, m]) => {
      sessionStorage.setItem('geo.token', tk);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [t, JSON.stringify({ role: 'TEACHER', userId, displayName })] as const,
  );
  await page.route(/fonts\.(googleapis|gstatic)\.com/, (r) => r.abort());
  return t;
}

export async function ok<T>(r: APIResponse, status = 200): Promise<T> {
  expect(r.status(), await r.text()).toBe(status);
  return (await r.json()) as T;
}

export async function expectError(r: APIResponse, status: number, code: string, message?: string) {
  expect(r.status(), await r.text()).toBe(status);
  const body = (await r.json()) as { code: string; message: string };
  expect(body.code).toBe(code);
  if (message !== undefined) expect(body.message).toBe(message);
  return body;
}

// ---------------------------------------------------------------- biến thể

export interface Variant {
  id: string;
  docId: string;
  title: string;
  statement: string;
  seed: number;
  params: Array<{ name: string; value: string }>;
  answer: string;
  approx: string | null;
  answerSource: string;
  machineLabel: string;
  humanLabel: string;
}
export interface Batch {
  requested: number;
  created: number;
  seed: number;
  message: string | null;
  items: Variant[];
}

export const createVariants = (request: APIRequestContext, t: string, docId: string, count: unknown, seed?: number) =>
  request.post(`${API}/api/v1/teacher/variants`, { headers: bearer(t), data: { docId, count, seed } });
export const listVariants = async (request: APIRequestContext, t: string, sourceId: string) =>
  (await ok<{ items: Variant[] }>(await request.get(`${API}/api/v1/teacher/variants?sourceId=${sourceId}`, { headers: bearer(t) }))).items;

// ---------------------------------------------------------------- xuất và liên kết chỉ xem

export interface ExportItem {
  id: string;
  title: string;
  statement: string;
  kind: string;
  sceneKind: string;
  scene: { points: Array<{ id: string; x: number; y: number }>; segments: string[][] } | null;
  machineLabel: string;
  humanLabel: string;
  lockedAssigned: boolean;
  solution: { steps: string[]; machineLabel: string; humanLabel: string } | null;
}
export interface ExportView {
  withSolution: boolean;
  confirmed: boolean;
  items: ExportItem[];
}
export interface DocOption {
  id: string;
  title: string;
  canAttachSolution: boolean;
  lockedAssigned: boolean;
  variant: boolean;
  machineLabel: string;
  humanLabel: string;
}

export const exportDocs = (request: APIRequestContext, t: string, docIds: string[], withSolution = false, confirmLocked = false) =>
  request.post(`${API}/api/v1/teacher/exports`, { headers: bearer(t), data: { docIds, withSolution, confirmLocked } });
export const docOptions = async (request: APIRequestContext, t: string) =>
  (await ok<{ items: DocOption[] }>(await request.get(`${API}/api/v1/teacher/exports/problems`, { headers: bearer(t) }))).items;
export const confirmations = async (request: APIRequestContext, t: string) =>
  (await ok<{ count: number }>(await request.get(`${API}/api/v1/teacher/exports/confirmations`, { headers: bearer(t) }))).count;

export interface Link {
  id: string;
  docId: string;
  token: string | null;
  hint: string;
  days: number;
  daysLabel: string;
  createdAt: string;
  expiresAt: string;
}
export const createLink = (request: APIRequestContext, t: string, docId: string, days?: number) =>
  request.post(`${API}/api/v1/teacher/share-links`, { headers: bearer(t), data: days === undefined ? { docId } : { docId, days } });
export const listLinks = async (request: APIRequestContext, t: string, docId: string) =>
  (await ok<{ items: Link[] }>(await request.get(`${API}/api/v1/teacher/share-links?docId=${docId}`, { headers: bearer(t) }))).items;
export const revokeLink = (request: APIRequestContext, t: string, id: string) =>
  request.delete(`${API}/api/v1/teacher/share-links/${id}`, { headers: bearer(t) });
/** Mở liên kết KHÔNG đăng nhập (không gửi thẻ). */
export const openShare = (request: APIRequestContext, shareToken: string, headers: Record<string, string> = {}) =>
  request.get(`${API}/api/v1/share/${shareToken}`, { headers });

/** Mở công cụ của một đề trên trình duyệt. */
export async function openDoc(page: Page, docId: string) {
  await page.goto(`/gv/xuat-de/de/${docId}`);
  await expect(page.getByTestId('s2e9-doc')).toBeVisible();
}

/** Bấm một nút rồi nhận tệp tải về; trả nội dung tệp. */
export async function downloadBy(page: Page, click: () => Promise<void>): Promise<{ download: Download; bytes: Buffer }> {
  const [download] = await Promise.all([page.waitForEvent('download'), click()]);
  const path = await download.path();
  return { download, bytes: readFileSync(path) };
}

/** Đếm trang và kiểm khung của tệp PDF do giao diện ghi (mỗi trang một ảnh JPEG). */
export function pdfInfo(bytes: Buffer): { pages: number; images: number; count: number } {
  const text = bytes.toString('latin1');
  expect(text.startsWith('%PDF-1.4'), 'đầu tệp PDF').toBe(true);
  expect(text.trimEnd().endsWith('%%EOF'), 'cuối tệp PDF').toBe(true);
  return {
    pages: (text.match(/\/Type \/Page[^s]/g) ?? []).length,
    images: (text.match(/\/Filter \/DCTDecode/g) ?? []).length,
    count: Number(/\/Count (\d+)/.exec(text)?.[1] ?? -1),
  };
}

/** Kích thước ảnh PNG (đọc khối IHDR). */
export function pngSize(bytes: Buffer): { width: number; height: number } {
  expect(bytes.subarray(0, 8).toString('latin1'), 'chữ ký PNG').toBe('\u0089PNG\r\n\u001a\n');
  return { width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}

/**
 * Số điểm ảnh có MỰC (khác màu giấy ở góc trên trái) trong từng dải ngang của ảnh PNG, tính trong trình duyệt:
 * chứng minh tệp có chữ (dải trên) và có hình (dải dưới), không phải trang trắng.
 */
export async function inkByBand(page: Page, png: Buffer, bands: Array<[number, number]>): Promise<number[]> {
  return page.evaluate(
    async ([b64, ranges]) => {
      const img = new Image();
      img.src = `data:image/png;base64,${b64}`;
      await img.decode();
      const c = document.createElement('canvas');
      c.width = img.width;
      c.height = img.height;
      const ctx = c.getContext('2d')!;
      ctx.drawImage(img, 0, 0);
      const paper = ctx.getImageData(4, 4, 1, 1).data;
      return ranges.map(([from, to]) => {
        const d = ctx.getImageData(0, from, c.width, to - from).data;
        let n = 0;
        for (let i = 0; i < d.length; i += 4) {
          if (Math.abs(d[i]! - paper[0]!) + Math.abs(d[i + 1]! - paper[1]!) + Math.abs(d[i + 2]! - paper[2]!) > 90) n++;
        }
        return n;
      });
    },
    [png.toString('base64'), bands] as const,
  );
}

// ---------------------------------------------------------------- câu trắc nghiệm (trên bộ bài S2-E8)

export interface TeacherChoice {
  position: number;
  maxAttempts: number;
  options: Array<{ id: string; text: string; correct: boolean }>;
}
export interface StudentChoice {
  assignmentId: string;
  position: number;
  options: Array<{ id: string; text: string }>;
  dimmed: string[];
  chosen: string | null;
}
export interface ChoiceAttempt {
  outcome: 'correct' | 'wrong';
  message: string;
  attemptsLeft: number;
  maxAttempts: number;
  state: string;
  dimmed: string[];
  canReport: boolean;
}

/** Lựa chọn của FX-14 (đáp án đúng 45°). */
export const ANGLES = ['30°', '45°', '60°', '90°'];
export const options = (texts: string[], correct: string | number | null = '45°') =>
  texts.map((text, i) => ({ text, correct: typeof correct === 'number' ? i === correct : text === correct }));

export const putChoice = (request: APIRequestContext, t: string, setId: string, position: number, opts: unknown, maxAttempts?: number) =>
  request.put(`${API}/api/v1/teacher/practice-sets/${setId}/items/${position}/choice`, {
    headers: bearer(t),
    data: maxAttempts === undefined ? { options: opts } : { options: opts, maxAttempts },
  });
export const teacherChoices = async (request: APIRequestContext, t: string, setId: string) =>
  (await ok<{ items: TeacherChoice[] }>(await request.get(`${API}/api/v1/teacher/practice-sets/${setId}/choices`, { headers: bearer(t) }))).items;
export const studentChoices = (request: APIRequestContext, t: string, setId: string) =>
  request.get(`${API}/api/v1/student/practice-sets/${setId}/choices`, { headers: bearer(t) });
export const choiceAttempt = (request: APIRequestContext, t: string, assignmentId: string, choiceId: string, requestKey: string) =>
  request.post(`${API}/api/v1/student/practice-items/${assignmentId}/choice-attempts`, { headers: bearer(t), data: { choiceId, requestKey } });

export interface ChoiceSpec {
  position: number;
  texts: string[];
  correct: string;
  maxAttempts?: number;
}

/** Bài nháp có câu trắc nghiệm (chưa giao). */
export async function draftWithChoices(request: APIRequestContext, sb: Sandbox, items: ItemSpec[], choices: ChoiceSpec[], title = 'Bài có trắc nghiệm') {
  const draft = await ok<SetDetail>(await createDraft(request, sb.teacher.token, title, items), 201);
  const saved: TeacherChoice[] = [];
  for (const c of choices) {
    saved.push(await ok<TeacherChoice>(await putChoice(request, sb.teacher.token, draft.id, c.position, options(c.texts, c.correct), c.maxAttempts)));
  }
  return { draft, choices: saved };
}

/** Soạn, đặt câu trắc nghiệm rồi giao cho lớp đầu tiên của sandbox. */
export async function assignedWithChoices(request: APIRequestContext, sb: Sandbox, items: ItemSpec[], choices: ChoiceSpec[], title?: string) {
  const d = await draftWithChoices(request, sb, items, choices, title);
  const set = await ok<SetDetail>(await assign(request, sb.teacher.token, d.draft.id, sb.classes[0]!.id));
  return { set, choices: d.choices };
}

/** Mã của một lựa chọn theo chữ (phía giáo viên biết). */
export const optionId = (c: TeacherChoice, text: string) => c.options.find((o) => o.text === text)!.id;
