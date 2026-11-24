import type { APIRequestContext, Page } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API } from '../e1/e1.ts';
import { HINT1_HKG01, HINT2_HKG01, HKG, LEAK_HKG01, bearer, expectNoLeak } from '../e4/e4.ts';

/**
 * Tiện ích E2E của E6 (giao bài và khóa lời giải). Dữ liệu vàng: artifacts/GEO/work/fixture.md (22/11/2026) §2, §3;
 * QC F-GEO-210…241 (review/QC_test_design.md §6).
 * Ca đổi trạng thái (giao bài, đổi giờ, "Mở ngay", hỏi đáp) chạy trên LỚP RIÊNG do móc `POST /test-hooks/e6/sandbox`
 * dựng: mỗi lần một Giáo viên mẫu mới (bước "1 · Chọn lớp" chỉ có lớp của ca đó), tổ chức thử "Nhóm thử E6".
 * Ca "tới giờ thì tự mở": giờ mở đặt vài giây sau giờ máy chủ (móc `POST /test-hooks/e6/release`, D-GEO-169) thay vì
 * chờ 60 giây / 60 phút thật.
 */
export { API, HINT1_HKG01, HINT2_HKG01, HKG, LEAK_HKG01, bearer, expectNoLeak };

export const MSG = {
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '14': 'Trợ lý đang bận. Em thử lại sau ít phút nhé.',
  '24': 'Đây là đại lượng đề hỏi. Giá trị sẽ hiện khi lời giải mở.',
  '28': 'Lời giải đang khóa',
  '28b': 'Giáo viên của em chưa đặt giờ mở.',
  '29': 'Lời giải đã mở',
  '32': 'Giáo viên đã tắt gợi ý cho bài này.',
  '33': 'Giáo viên đã tắt hỏi đáp cho bài này.',
  '34': 'Em xem Gợi ý 1 trước nhé.',
  '35': 'Lời giải đang khóa nên mình chưa nêu đáp số hay các bước giải. Em xem lại gợi ý nhé: ',
  '43': 'Giờ mở lời giải phải sau thời điểm hiện tại. Muốn mở luôn, hãy chọn “Ngay bây giờ”.',
  '45': 'Chưa đặt giờ mở: lời giải và đáp số vẫn khóa cho tới khi đặt giờ hoặc mở ngay.',
  '48': 'Lời giải đã mở nên không đổi giờ hay khóa lại được.',
  '49': 'Giờ mở vừa được thay đổi ở nơi khác. Hãy tải lại để xem giờ mới.',
  '50': 'Lớp em chưa có bài được giao.',
  '51': 'Đang xem như học sinh. Lời giải theo đúng giờ mở của bài.',
  '55': 'Lời giải đã mở. Em xem ngay được rồi.',
} as const;
export const msg41 = (c: string) => `Đã giao bài cho lớp ${c}.`;
export const msg42 = (c: string) => `Đề này đã được giao cho lớp ${c}.`;
export const msg46 = (c: string) => `Đã mở lời giải cho lớp ${c}.`;
export const msg47 = (t: string) => `Đã đổi giờ mở lời giải thành ${t}.`;
export const confirmText = (c: string) => `Mở lời giải ngay cho lớp ${c}? Sau khi mở không khóa lại được.`;

/** Chuỗi lộ khác của bộ đề (fixture §2). */
export const LEAK_HKG04 = ['15π', '12π'];
export const LEAK_HKG03 = ['24√3 cm³', '24√3', 'S = 4²·√3 / 4 = 4√3 cm².'];
export const QA_EXTRA_HKG01 = ['góc SDA', 'tam giác SAD vuông cân'];

// ---------------------------------------------------------------- lớp riêng của ca kiểm
export interface Person {
  id: string;
  token: string;
}
export interface SbClass {
  id: string;
  name: string;
  joinCode: string;
  students: Record<string, Person>;
  assignments: Record<string, string>;
}
export interface Sb {
  orgId: string;
  teacher: Person;
  classes: SbClass[];
}
export interface ASpec {
  key: string;
  code: string;
  /** upcoming (sau 60 phút) | unset | open | số giây */
  release?: string;
  allowHint?: boolean;
  allowQa?: boolean;
}
export const A11 = ['An', 'Bình', 'Chi', 'Dũng', 'Hà'];
export const B9 = ['Khánh', 'Linh', 'Minh', 'Ngân', 'Phúc'];

export async function sandbox(
  rq: APIRequestContext,
  classes: Array<{ name?: string; level?: 'thcs' | 'thpt'; students: string[]; assignments?: ASpec[] }>,
): Promise<Sb> {
  const r = await rq.post(`${API}/test-hooks/e6/sandbox`, { data: { classes } });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Sb;
}
/** Lớp 11A1 (5 HS) và 9B (5 HS) của một giáo viên mới, như dữ liệu mẫu. */
export function twoClasses(rq: APIRequestContext, a11: ASpec[] = [], b9: ASpec[] = []) {
  return sandbox(rq, [
    { name: '11A1', level: 'thpt', students: A11, assignments: a11 },
    { name: '9B', level: 'thcs', students: B9, assignments: b9 },
  ]);
}
export const cls = (sb: Sb, name: string) => sb.classes.find((c) => c.name === name)!;
export const stu = (sb: Sb, name: string) => {
  for (const c of sb.classes) if (c.students[name]) return c.students[name]!;
  throw new Error(`không có học sinh ${name}`);
};
export const asg = (sb: Sb, key: string) => {
  for (const c of sb.classes) if (c.assignments[key]) return c.assignments[key]!;
  throw new Error(`không có bài ${key}`);
};

/** D-GEO-169: giờ mở = giờ máy chủ + n giây. */
export async function releaseIn(rq: APIRequestContext, assignmentId: string, seconds: number): Promise<Date> {
  const r = await rq.post(`${API}/test-hooks/e6/release`, { data: { assignmentId, seconds } });
  expect(r.status()).toBe(200);
  return new Date(((await r.json()) as { releaseAt: string }).releaseAt);
}
export async function setAi(rq: APIRequestContext, userId: string, data: Record<string, unknown>) {
  const r = await rq.post(`${API}/test-hooks/ai`, { data: { userId, ...data } });
  expect(r.status()).toBe(200);
}
export async function setGuard(rq: APIRequestContext, userId: string, data: { fail?: boolean; bypass?: boolean }) {
  const r = await rq.post(`${API}/test-hooks/e6/guard`, { data: { userId, ...data } });
  expect(r.status()).toBe(200);
}
export { BG, PV, USER, CLASS } from '../e1/e1.ts';

// ---------------------------------------------------------------- gọi API
export const api = {
  ask: (rq: APIRequestContext, t: string, id: string, question: string) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/questions`, { headers: bearer(t), data: { question, requestId: crypto.randomUUID() } }),
  hint: (rq: APIRequestContext, t: string, id: string, level: number) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/hints/${level}`, { headers: bearer(t), data: { requestId: crypto.randomUUID() } }),
  solution: (rq: APIRequestContext, t: string, id: string) => rq.get(`${API}/api/v1/student/assignments/${id}/solution`, { headers: bearer(t) }),
  openSolution: (rq: APIRequestContext, t: string, id: string) =>
    rq.post(`${API}/api/v1/student/assignments/${id}/solution/open`, { headers: bearer(t), data: {} }),
  lock: (rq: APIRequestContext, t: string, id: string) => rq.get(`${API}/api/v1/student/assignments/${id}/lock`, { headers: bearer(t) }),
  mine: (rq: APIRequestContext, t: string, query = '') => rq.get(`${API}/api/v1/student/assignments${query}`, { headers: bearer(t) }),
  todo: (rq: APIRequestContext, t: string) => rq.get(`${API}/api/v1/student/assignments/todo`, { headers: bearer(t) }),
  assign: (rq: APIRequestContext, t: string, data: Record<string, unknown>) =>
    rq.post(`${API}/api/v1/teacher/assignments`, { headers: bearer(t), data: { assignmentId: crypto.randomUUID(), ...data } }),
  release: (rq: APIRequestContext, t: string, id: string, data: Record<string, unknown>) =>
    rq.post(`${API}/api/v1/teacher/assignments/${id}/release`, { headers: bearer(t), data }),
  releaseView: (rq: APIRequestContext, t: string, id: string) =>
    rq.get(`${API}/api/v1/teacher/assignments/${id}/release`, { headers: bearer(t) }),
  releaseLog: (rq: APIRequestContext, t: string, id: string) =>
    rq.get(`${API}/api/v1/teacher/assignments/${id}/release-log`, { headers: bearer(t) }),
  options: (rq: APIRequestContext, t: string, pv: string) =>
    rq.get(`${API}/api/v1/teacher/problems/${pv}/assign-options`, { headers: bearer(t) }),
  preview: (rq: APIRequestContext, t: string, pv: string, query = '') =>
    rq.get(`${API}/api/v1/teacher/preview/${pv}${query}`, { headers: bearer(t) }),
  previewAsk: (rq: APIRequestContext, t: string, pv: string, question: string) =>
    rq.post(`${API}/api/v1/teacher/preview/${pv}/questions`, { headers: bearer(t), data: { question } }),
  monitor: (rq: APIRequestContext, t: string, id: string) =>
    rq.get(`${API}/api/v1/teacher/assignments/${id}/monitor`, { headers: bearer(t) }),
};

// ---------------------------------------------------------------- trình duyệt
/** Đăng nhập trình duyệt bằng thẻ có sẵn (học sinh hoặc giáo viên của lớp riêng). */
export async function signIn(page: Page, p: Person, displayName: string, role: 'student' | 'teacher', className?: string) {
  const me = { role: role === 'student' ? 'STUDENT' : 'TEACHER', userId: p.id, displayName, className };
  await page.addInitScript(
    ([t, m]) => {
      sessionStorage.setItem('geo.token', t);
      sessionStorage.setItem('geo.e1.me', m);
    },
    [p.token, JSON.stringify(me)] as const,
  );
}

/** "HH:mm, dd/MM/yyyy" giờ Việt Nam. */
export function vn(d: Date): string {
  const p = Object.fromEntries(
    new Intl.DateTimeFormat('en-GB', {
      timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
    })
      .formatToParts(d)
      .map((x) => [x.type, x.value]),
  );
  return `${p.hour}:${p.minute}, ${p.day}/${p.month}/${p.year}`;
}
/** Giá trị cho ô ngày (yyyy-MM-dd) và ô giờ (HH:mm) theo giờ Việt Nam. */
export function vnInputs(d: Date): { date: string; time: string } {
  const p = Object.fromEntries(
    new Intl.DateTimeFormat('en-GB', {
      timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
    })
      .formatToParts(d)
      .map((x) => [x.type, x.value]),
  );
  return { date: `${p.year}-${p.month}-${p.day}`, time: `${p.hour}:${p.minute}` };
}
/** Giờ máy chủ hiện tại (đọc từ phản hồi trạng thái khóa). */
export async function serverNow(rq: APIRequestContext, t: string, id: string): Promise<Date> {
  const r = await api.lock(rq, t, id);
  return new Date(((await r.json()) as { serverNow: string }).serverNow);
}
