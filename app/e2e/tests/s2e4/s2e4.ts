import { readFileSync } from 'node:fs';
import path from 'node:path';
import type { APIRequestContext, APIResponse, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer } from '../e1/e1.ts';

/**
 * Tiện ích E2E của S2-E4 (chat chỉnh nháp, import docx/pdf, đối chiếu trùng). Dữ liệu: app/db/seed/e2e/860_s2e4_core.sql +
 * fixture-ids.d/s2e4.json. Mỗi ca một giáo viên riêng theo (mã TC, làn = dự án Playwright): nháp, lượt chat và giới hạn
 * thêm đề tính theo người. Tổ chức ORG: Kho chung rỗng; ORG_LIB: Kho chung có FX-10, Cô Lan có FX-12 trong "Của tôi".
 */
export { API, bearer };

export const ORG = '00000000-0000-7000-8002-000000860001';
export const ORG_LIB = '00000000-0000-7000-8002-000000860002';
export const LAN = '00000000-0000-7000-8002-000000860003';
export const LAN_DOC = '00000000-0000-7000-8002-000000860111';
export const LIB_FX10 = '00000000-0000-7000-8002-000000860120';
const LANES = ['api', 'ui-chromium', 'ui-webkit', 'ui-iphone', 'mobile-iphone', 'mobile-pixel'];
export const lane = (info: TestInfo) => Math.max(0, LANES.indexOf(info.project.name));
const f = (n: number) => `00000000-0000-7000-8002-${String(n).padStart(12, '0')}`;

/** Giáo viên riêng của ca (TC, làn) ở tổ chức kho rỗng / tổ chức có kho. */
export const teacher = (tc: number, info: TestInfo) => f(861000000 + tc * 100 + lane(info));
export const teacherLib = (tc: number, info: TestInfo) => f(862000000 + tc * 100 + lane(info));

export const FX10 = 'Tam giác ABC có AB = 5, AC = 12, BC = 13; tính góc A.';
export const FX11 = 'Tam giác ABC cân tại A, góc A = 40°; tính góc B.';
export const FX12 = 'Hình chữ nhật ABCD có AB = 6, AD = 8; tính AC.';

export const MSG = {
  tooLong: 'Tin tối đa 1000 ký tự',
  turnsFull: 'Nháp này đã đủ 30 lượt chat, hãy duyệt hoặc tạo nháp mới',
  noUndo: 'Hết bước hoàn tác',
  askHarder: 'Khó hơn theo hướng nào: thêm ý hỏi, đổi số, hay thêm bước?',
  source: 'nháp hiện tại',
  rejected: 'đã bác',
  verified: 'Đã kiểm chứng',
  needsReview: 'Cần xem lại',
  ai: 'AI soạn',
  approved: 'Giáo viên đã duyệt',
  theory: 'Phần lý thuyết chưa hỗ trợ ở chặng này',
  noTextLayer: 'Tệp không có lớp chữ. Hãy dùng Tải ảnh cho từng trang',
  broken: 'Không mở được tệp',
  password: 'Tệp đặt mật khẩu, hãy bỏ mật khẩu rồi tải lại',
  tooBig: 'Tệp tối đa 10 MB',
  onlyDocxPdf: 'Chỉ nhận docx và pdf',
  exists: 'đã có trong kho',
};

export async function tokenFor(request: APIRequestContext, userId: string, orgId = ORG): Promise<string> {
  const r = await request.post(`${API}/dev/token`, { data: { orgId, userId, role: 'TEACHER' } });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { token: string }).token;
}

/** Đăng nhập giáo viên của ca trên trình duyệt (thẻ dev) rồi mở một màn. */
export async function signIn(page: Page, request: APIRequestContext, userId: string, pathname: string, orgId = ORG): Promise<void> {
  const token = await tokenFor(request, userId, orgId);
  const me = { role: 'TEACHER', userId, displayName: 'Cô Trang' };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [token, JSON.stringify(me)] as const,
  );
  await page.goto(pathname);
}

export async function ok<T>(r: APIResponse): Promise<T> {
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as T;
}

// ---------------------------------------------------------------- nháp + chat

const DRAFTS = `${API}/api/v1/teacher/problem-drafts`;
export const draftsUrl = DRAFTS;

export interface Draft {
  id: string;
  status: string;
  statement: string;
  machineLabel: string;
  inMyShelf: boolean;
  job?: { id: string; state: string } | null;
  result?: { docId?: string | null; machineLabel: string; humanLabel: string } | null;
}

export interface Message {
  id: string;
  seq: number;
  role: string;
  kind: string;
  text: string;
  proposal?: { target: string; before: string; after: string; source: string; unsure?: string | null; state: string; note?: string | null } | null;
}

export interface Chat {
  draft: Draft;
  machineLabel: string;
  humanLabel: string;
  saved: boolean;
  messages: Message[];
  turns: number;
  canSend: boolean;
  sendNote?: string | null;
  undoSteps: number;
  canUndo: boolean;
  undoNote?: string | null;
}

/** Tạo nháp chữ (chưa dựng). */
export async function newDraft(request: APIRequestContext, t: string, statement: string): Promise<Draft> {
  return ok<Draft>(await request.post(DRAFTS, { headers: bearer(t), data: { source: 'text', statement } }));
}

/** Tạo nháp chữ, dựng và chờ bốn bước xong (tối đa 20 giây): nháp "Đã kiểm chứng" với FX-10. */
export async function builtDraft(request: APIRequestContext, t: string, statement: string): Promise<Draft> {
  const d = await newDraft(request, t, statement);
  const b = await ok<Draft>(await request.post(`${DRAFTS}/${d.id}/build`, { headers: bearer(t), data: {} }));
  const jobUrl = `${API}/api/v1/teacher/problem-draft-jobs/${b.job!.id}`;
  await expect.poll(async () => (await ok<{ state: string }>(await request.get(jobUrl, { headers: bearer(t) }))).state, { timeout: 20_000 }).not.toBe('running');
  return ok<Draft>(await request.get(`${DRAFTS}/${d.id}`, { headers: bearer(t) }));
}

export const chatUrl = (draftId: string) => `${DRAFTS}/${draftId}/chat`;
export const say = (request: APIRequestContext, t: string, draftId: string, text: string) =>
  request.post(`${chatUrl(draftId)}/messages`, { headers: bearer(t), data: { text } });
export const lastMessage = (c: Chat) => c.messages[c.messages.length - 1]!;

/** Gửi một yêu cầu rõ và nhận luôn đề xuất AI trả về (qua API). */
export async function sayAndAccept(request: APIRequestContext, t: string, draftId: string, text: string): Promise<Chat> {
  const c = await ok<Chat>(await say(request, t, draftId, text));
  const m = lastMessage(c);
  expect(m.kind, text).toBe('proposal');
  return ok<Chat>(await request.post(`${chatUrl(draftId)}/proposals/${m.id}/accept`, { headers: bearer(t), data: {} }));
}

/** Gõ một yêu cầu trên màn chat và chờ máy chủ trả lời. */
export async function typeAndSend(page: Page, text: string): Promise<void> {
  await page.getByTestId('s2e4-chat-input').fill(text);
  const done = page.waitForResponse((r) => r.url().includes('/chat/messages') && r.request().method() === 'POST');
  await page.getByTestId('s2e4-chat-send').click();
  await done;
}

// ---------------------------------------------------------------- tệp mẫu

const CRC_TABLE = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();
function crc32(buf: Buffer): number {
  let c = 0xffffffff;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]!) & 0xff]! ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

/** Zip tối thiểu (các mục lưu nguyên, không nén). */
export function zip(entries: Array<{ name: string; data: Buffer }>): Buffer {
  const locals: Buffer[] = [];
  const centrals: Buffer[] = [];
  let offset = 0;
  for (const e of entries) {
    const name = Buffer.from(e.name, 'utf8');
    const crc = crc32(e.data);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(20, 4);
    local.writeUInt16LE(0x0800, 6);
    local.writeUInt16LE(0, 8);
    local.writeUInt32LE(0x00210000, 10);
    local.writeUInt32LE(crc, 14);
    local.writeUInt32LE(e.data.length, 18);
    local.writeUInt32LE(e.data.length, 22);
    local.writeUInt16LE(name.length, 26);
    local.writeUInt16LE(0, 28);
    const central = Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50, 0);
    central.writeUInt16LE(20, 4);
    central.writeUInt16LE(20, 6);
    central.writeUInt16LE(0x0800, 8);
    central.writeUInt16LE(0, 10);
    central.writeUInt32LE(0x00210000, 12);
    central.writeUInt32LE(crc, 16);
    central.writeUInt32LE(e.data.length, 20);
    central.writeUInt32LE(e.data.length, 24);
    central.writeUInt16LE(name.length, 28);
    central.writeUInt32LE(offset, 42);
    locals.push(local, name, e.data);
    centrals.push(central, name);
    offset += 30 + name.length + e.data.length;
  }
  const cd = Buffer.concat(centrals);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(entries.length, 8);
  end.writeUInt16LE(entries.length, 10);
  end.writeUInt32LE(cd.length, 12);
  end.writeUInt32LE(offset, 16);
  return Buffer.concat([...locals, cd, end]);
}

const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
const documentXml = (paragraphs: string[]) =>
  Buffer.from(
    `<?xml version="1.0" encoding="UTF-8" standalone="yes"?><w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>${paragraphs
      .map((p) => `<w:p><w:r><w:t xml:space="preserve">${esc(p)}</w:t></w:r></w:p>`)
      .join('')}</w:body></w:document>`,
    'utf8',
  );

/** docx tối thiểu từ các đoạn văn; {@code size}: độn (một mục ảnh giả) tới ĐÚNG số byte đó. */
export function docx(paragraphs: string[], size?: number): Buffer {
  const base = [
    { name: '[Content_Types].xml', data: Buffer.from('<Types/>', 'utf8') },
    { name: 'word/document.xml', data: documentXml(paragraphs) },
  ];
  if (size === undefined) return zip(base);
  const padName = 'word/media/pad.bin';
  const empty = zip([...base, { name: padName, data: Buffer.alloc(0) }]);
  const out = zip([...base, { name: padName, data: Buffer.alloc(size - empty.length, 0x20) }]);
  if (out.length !== size) throw new Error(`docx độn sai cỡ: ${out.length} ≠ ${size}`);
  return out;
}

/** Tệp xlsx tối thiểu (zip có xl/workbook.xml). */
export const xlsx = (): Buffer =>
  zip([
    { name: '[Content_Types].xml', data: Buffer.from('<Types/>', 'utf8') },
    { name: 'xl/workbook.xml', data: Buffer.from('<workbook/>', 'utf8') },
  ]);

/** Tệp T: 3 bài tập (FX-10, FX-11, FX-12) + 1 đoạn lý thuyết. */
export const THREE = ['Lý thuyết: Định lý Pythagore đảo.', 'Nếu bình phương một cạnh bằng tổng bình phương hai cạnh kia thì tam giác vuông.', 'Bài tập', `Bài 1. ${FX10}`, `Bài 2. ${FX11}`, `Bài 3. ${FX12}`];

/** n bài tập khác hẳn nhau (không bài nào gần giống bài nào). */
export const exercises = (n: number): string[] =>
  Array.from({ length: n }, (_, i) => `Bài ${i + 1}. ${'xyzuvwklmn'[i % 10]!.repeat(12 + (i % 7))} hình số ${(i + 1) * 7919} có cạnh ${(i + 3) * 13}; tính chu vi ${i + 1}.`);

/** Tệp pdf mẫu sinh bằng PDFBox (ImportFilesTest.writeE2eFixtures): hai-bai | anh-quet | mat-khau. */
export const pdf = (name: 'hai-bai' | 'anh-quet' | 'mat-khau'): Buffer => readFileSync(path.join(import.meta.dirname, 'fixtures', `${name}.pdf`));

// ---------------------------------------------------------------- import

const IMPORTS = `${API}/api/v1/teacher/problem-imports`;
export const importsUrl = IMPORTS;

export interface ImportItem {
  index: number;
  title: string;
  text: string;
  state: string;
  note?: string | null;
  refKind?: string | null;
  refId?: string | null;
  checked: boolean;
  draftId?: string | null;
}
export interface Import {
  id: string;
  status: string;
  found: number;
  notes: string[];
  error?: string | null;
  items: ImportItem[];
  created: number;
  updated: number;
  skipped: number;
}

export const createImport = (request: APIRequestContext, t: string, fileName: string, sizeBytes: number) =>
  request.post(IMPORTS, { headers: bearer(t), data: { fileName, sizeBytes } });

/** Tạo lượt import rồi gửi tệp theo đoạn 1 000 000 byte như màn hình; trả phản hồi cuối (hoặc phản hồi lỗi đầu tiên). */
export async function upload(request: APIRequestContext, t: string, fileName: string, bytes: Buffer): Promise<APIResponse> {
  const c = await createImport(request, t, fileName, bytes.length);
  if (c.status() !== 200) return c;
  const id = ((await c.json()) as Import).id;
  let last: APIResponse = c;
  for (let off = 0; off < bytes.length; off += 1_000_000) {
    const end = Math.min(bytes.length, off + 1_000_000);
    const url = `${IMPORTS}/${id}/file?offset=${off}&total=${bytes.length}`;
    const headers = { ...bearer(t), 'Content-Type': 'application/octet-stream' };
    if (end < bytes.length) {
      // Đoạn giữa gửi bằng fetch của Node: fixture `request` ghi lại nguyên thân yêu cầu vào bằng chứng, tệp 10 MB làm
      // tiến trình test hết bộ nhớ. Đoạn CUỐI (có kết quả tách bài) vẫn đi qua `request` để có trong bằng chứng.
      const mid = await fetch(url, { method: 'PUT', headers, body: new Uint8Array(bytes.subarray(off, end)) });
      expect(mid.status, `đoạn ${off}: ${await mid.text()}`).toBe(200);
      continue;
    }
    last = await request.put(url, { headers, data: bytes.subarray(off, end) });
  }
  return last;
}

/** Chọn tệp trên màn "Nhập từ tệp" và chờ máy chủ xử lý xong đoạn cuối. */
export async function pickFile(page: Page, name: string, buffer: Buffer, mimeType = 'application/octet-stream'): Promise<void> {
  await page.getByTestId('s2e4-import-file').setInputFiles({ name, mimeType, buffer });
  await expect(page.getByTestId('s2e4-import-busy')).toHaveCount(0, { timeout: 20_000 });
}
