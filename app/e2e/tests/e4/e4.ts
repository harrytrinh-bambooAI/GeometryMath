import type { APIRequestContext, APIResponse, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API } from '../e1/e1.ts';

/**
 * Tiện ích E2E của E4 (Gợi ý, lời giải đồng bộ hình, hỏi đáp, tự học). Dữ liệu vàng: artifacts/GEO/work/fixture.md
 * (22/11/2026), QC F-GEO-233…235 (review/QC_test_design.md §6).
 * Ca đổi trạng thái (xem gợi ý, hỏi, "Mở ngay", giao đề) chạy trên LỚP RIÊNG do móc `POST /test-hooks/e4/sandbox` dựng
 * cho từng ca (tổ chức thử "Nhóm thử E4", seed 401): không giẫm dữ liệu vàng mà E1…E3 đọc, chạy song song an toàn.
 */
export { API };

export const MSG = {
  '02': 'Phiên đã hết. Vui lòng vào lại.',
  '04': 'Không tìm thấy nội dung này.',
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '06': 'Nội dung gửi lên quá lớn (tối đa 1 MB).',
  '08': 'Không kết nối được. Vui lòng kiểm tra mạng và thử lại.',
  '13': 'Đang dựng hình, em chờ một chút…',
  '14': 'Trợ lý đang bận. Em thử lại sau ít phút nhé.',
  '15': 'Trợ lý đang soạn câu trả lời…',
  '18': 'Chưa dựng được hình từ đề này. Em kiểm tra lại đề hoặc chọn một hình mẫu.',
  '20': 'Em hãy nhập đề bài.',
  '21': 'Đề bài tối đa 8000 ký tự.',
  '22': 'Chưa đọc được chữ trong ảnh. Em chụp lại rõ hơn hoặc gõ đề.',
  '30': 'Ở chế độ tự học, em xem gợi ý 1 và 2 trước, sau đó mới mở được lời giải.',
  '31': 'Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt.',
  '32': 'Giáo viên đã tắt gợi ý cho bài này.',
  '33': 'Giáo viên đã tắt hỏi đáp cho bài này.',
  '34': 'Em xem Gợi ý 1 trước nhé.',
  '35': 'Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: ',
  '36': 'Giáo trình của lớp chưa có phần này. Em hãy hỏi giáo viên nhé.',
  '37': 'Câu hỏi cần từ 1 đến 1000 ký tự.',
  '38': 'Em chưa hỏi câu nào ở bài này.',
  '39': 'Chưa có bài giảng cho từ khóa này.',
  '58': 'Đề này chưa dựng được bằng hình mẫu nên chưa có lời giải tự học. Em dùng gợi ý nhé.',
} as const;

// ---------------------------------------------------------------- đề (nguyên văn fixture)
export const HKG = {
  '01': 'Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính góc giữa SD và mặt đáy.',
  '02': 'Cho hình lập phương ABCD.A′B′C′D′ cạnh a. Tính khoảng cách giữa AC và B′D′.',
  '03': 'Lăng trụ đứng tam giác có đáy là tam giác đều cạnh 4 cm, chiều cao 6 cm. Tính thể tích.',
  '04': 'Hình nón có bán kính đáy 3 cm, đường sinh 5 cm. Tính diện tích xung quanh và thể tích.',
  '05': 'Hình trụ có chiều cao 10 cm, bán kính đáy 4 cm. Tính diện tích toàn phần.',
  '06': 'Mặt cầu ngoại tiếp hình lập phương cạnh a. Tính bán kính và thể tích khối cầu.',
} as const;
export const F060 =
  'Cho hình lăng trụ đứng ABC.A′B′C′ có đáy là tam giác vuông tại A, AB = 3, AC = 4, AA′ = 5. Tính khoảng cách từ A đến mặt phẳng (A′BC).';
export const F061 = 'Cho hình bát diện đều cạnh a. Tính khoảng cách giữa hai mặt đối diện.';
export const F063 = 'Cho hình chóp S.MNPQ, đáy là hình vuông cạnh a, SM ⊥ (MNPQ), SM = a. Tính góc giữa SQ và mặt đáy.';
export const F077 =
  'Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh 2a, cạnh SA vuông góc với mặt phẳng (ABCD) và SA = 2a. Hãy tính góc tạo bởi đường thẳng SD và mặt phẳng đáy.';
export const F078 = 'Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√3. Tính góc giữa SD và mặt đáy.';
export const F234 =
  'Bài tập về nhà số 3. Cho hình chóp S.ABCD có đáy ABCD là hình vuông cạnh a, cạnh bên SA vuông góc với mặt phẳng (ABCD) và SA = a. Hãy tính góc giữa đường thẳng SD và mặt phẳng đáy.';
export const F235 = 'Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a. Tính thể tích khối chóp S.ABCD.';
export const HINT1_HKG01 = 'Góc giữa đường thẳng và mặt phẳng là góc giữa đường đó và hình chiếu của nó trên mặt phẳng.';
export const HINT2_HKG01 = 'Hình chiếu của S lên mặt đáy là điểm nào? Từ đó suy ra hình chiếu của SD.';
export const HINT2_HKG06 = 'Bán kính bằng nửa đường chéo của hình lập phương.';
/** Chuỗi lộ đáp số của HKG-01 khi đang khóa, mọi kênh (fixture §2) + nội dung các bước F-GEO-037. */
export const LEAK_HKG01 = [
  '45°',
  '45 độ',
  'π/4',
  'bốn mươi lăm độ',
  'tan SDA = 1',
  'SA ⊥ (ABCD) nên A là hình chiếu của S. Hình chiếu của SD là AD.',
  'Góc giữa SD và (ABCD) là góc SDA. Tam giác SAD vuông tại A.',
  'tan SDA = SA / AD = a / a = 1, suy ra góc SDA = 45°.',
];

// ---------------------------------------------------------------- lớp riêng của ca kiểm
export interface Person {
  id: string;
  token: string;
}
export interface SandboxClass {
  id: string;
  name: string;
  students: Record<string, Person>;
  assignments: Record<string, string>;
}
export interface Sandbox {
  orgId: string;
  teacher: Person;
  classes: SandboxClass[];
}
export interface AssignmentSpec {
  key: string;
  code: string;
  /** upcoming (giờ mở sau 60 phút) | unset | open | số giây */
  release?: string;
  allowHint?: boolean;
  allowQa?: boolean;
}
export async function sandbox(
  request: APIRequestContext,
  classes: Array<{ name?: string; level?: 'thcs' | 'thpt'; students: string[]; assignments?: AssignmentSpec[] }>,
): Promise<Sandbox> {
  const r = await request.post(`${API}/test-hooks/e4/sandbox`, { data: { classes } });
  expect(r.status()).toBe(200);
  return (await r.json()) as Sandbox;
}

export const bearer = (t: string) => ({ Authorization: `Bearer ${t}` });

export async function body<T = Record<string, unknown>>(r: APIResponse): Promise<T> {
  return (await r.json()) as T;
}

/** Gọi API học sinh / giáo viên bằng thẻ (đường tuyệt đối, chạy được cả ở dự án UI). */
export const call = {
  help: (rq: APIRequestContext, t: string, id: string) => rq.get(`${API}/api/v1/student/assignments/${id}/help`, { headers: bearer(t) }),
  hint: (rq: APIRequestContext, t: string, id: string, level: number, requestId = crypto.randomUUID()) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/hints/${level}`, { headers: bearer(t), data: { requestId } }),
  solution: (rq: APIRequestContext, t: string, id: string) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/solution/open`, { headers: bearer(t), data: {} }),
  ask: (rq: APIRequestContext, t: string, id: string, question: string, requestId = crypto.randomUUID()) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/questions`, { headers: bearer(t), data: { question, requestId } }),
  page: (rq: APIRequestContext, t: string, id: string) => rq.get(`${API}/api/v1/student/assignments/${id}`, { headers: bearer(t) }),
  selfStudy: (rq: APIRequestContext, t: string, statement: string) =>
    rq.post(`${API}/api/v1/student/self-study`, { headers: bearer(t), data: { statement } }),
  release: (rq: APIRequestContext, t: string, id: string, data: Record<string, unknown>) =>
    rq.post(`${API}/api/v1/teacher/assignments/${id}/release`, { headers: bearer(t), data }),
  assign: (rq: APIRequestContext, t: string, classId: string, problemVersionId: string, releaseAt: string | null) =>
    rq.post(`${API}/api/v1/teacher/assignments`, {
      headers: bearer(t),
      data: { assignmentId: crypto.randomUUID(), classId, problemVersionId, releaseAt, allowHint: true, allowQa: true },
    }),
  log: (rq: APIRequestContext, t: string, query = '') => rq.get(`${API}/api/v1/student/qa-log${query}`, { headers: bearer(t) }),
  lecture: (rq: APIRequestContext, t: string, term: string) =>
    rq.get(`${API}/api/v1/lectures/by-term?term=${encodeURIComponent(term)}`, { headers: bearer(t) }),
};

/** Tạo bài tự học (dựng hình) và trả mã bài. */
export async function selfStudy(rq: APIRequestContext, t: string, statement: string): Promise<string> {
  const r = await call.selfStudy(rq, t, statement);
  expect(r.status(), await r.text()).toBe(200);
  return ((await r.json()) as { assignmentId: string }).assignmentId;
}
export async function hints12(rq: APIRequestContext, t: string, id: string): Promise<string> {
  let all = '';
  for (const level of [1, 2]) {
    const r = await call.hint(rq, t, id, level);
    expect(r.status(), await r.text()).toBe(200);
    all += await r.text();
  }
  return all;
}

/** Kịch bản nhà cung cấp AI giả lập cho riêng một học sinh: mode ok|error|timeout, trễ (ms). */
export async function setAiFor(rq: APIRequestContext, userId: string, opts: { mode?: string; delayMs?: number }) {
  const r = await rq.post(`${API}/test-hooks/ai`, { data: { userId, ...opts } });
  expect(r.status()).toBe(200);
}
export async function aiCalls(rq: APIRequestContext, userId: string): Promise<Array<{ purpose: string; status: string }>> {
  const r = await rq.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`);
  expect(r.status()).toBe(200);
  return (await r.json()) as Array<{ purpose: string; status: string }>;
}

/** Đăng nhập trình duyệt bằng thẻ của học sinh lớp riêng rồi mở trang. */
export async function signIn(page: Page, p: Person, displayName: string, className = '11A1', role = 'student') {
  const me = { role, userId: p.id, displayName, className };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [p.token, JSON.stringify(me)] as const,
  );
}
export async function openWorkspace(page: Page, p: Person, assignmentId: string, name = 'An') {
  await signIn(page, p, name);
  await page.goto(`/hs/lam-bai/${assignmentId}`);
  await expect(page.getByTestId('e3-workspace')).toBeVisible();
}
export async function tab(page: Page, t: 'hint' | 'solution' | 'ask') {
  await page.getByTestId(`e3-tab-${t}`).click();
}

/** Toàn văn mọi phản hồi trang nhận (kiểm "dữ liệu trả về không có …"). */
export function recordResponses(page: Page): () => Promise<string> {
  const bodies: Promise<string>[] = [];
  page.on('response', (r) => {
    if (r.url().includes('/api/')) bodies.push(r.text().catch(() => ''));
  });
  return async () => (await Promise.all(bodies)).join('\n');
}
export function expectNoLeak(text: string, leaks: string[] = LEAK_HKG01) {
  for (const s of leaks) expect(text, `không chứa "${s}"`).not.toContain(s);
}
