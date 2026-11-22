import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { formatVnDate, formatVnTime, vnParts } from '../../shared/time/vnTime.ts';

/** "HH:mm, dd/MM/yyyy" giờ Việt Nam — định dạng giờ mở của E6 (BR-GEO-233, MSG-GEO-27, 47). */
export function vnDateTime(v: string | number | Date): string {
  return `${formatVnTime(v)}, ${formatVnDate(v)}`;
}

/** Gọi máy chủ của E6 (giao bài, giờ mở, xem trước, "Bài của em"). Thông điệp nguyên văn work/05_dictionary.md. */
const enc = encodeURIComponent;

export const M = {
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '14': 'Trợ lý đang bận. Em thử lại sau ít phút nhé.',
  '27sub': 'Do giáo viên của em đặt.',
  '28': 'Lời giải đang khóa',
  '28sub': 'Giáo viên của em chưa đặt giờ mở.',
  '29': 'Lời giải đã mở',
  '45': 'Chưa đặt giờ mở: lời giải và đáp số vẫn khóa cho tới khi đặt giờ hoặc mở ngay.',
  '50': 'Lớp em chưa có bài được giao.',
  '51': 'Đang xem như học sinh. Lời giải theo đúng giờ mở của bài.',
  '55': 'Lời giải đã mở. Em xem ngay được rồi.',
} as const;
export const msg27 = (at: string) => `Lời giải mở lúc ${vnDateTime(at)}`;
export const msg41 = (c: string) => `Đã giao bài cho lớp ${c}.`;
export const msg44 = (at: string) => {
  const p = vnParts(at);
  return `Trước ${p.hour}:${p.minute} ngày ${p.day}/${p.month}, học sinh chỉ thấy đề, hình và gợi ý. Lời giải và đáp số được giữ ở máy chủ.`;
};
export const msg46 = (c: string) => `Đã mở lời giải cho lớp ${c}.`;
export const msg47 = (at: string) => `Đã đổi giờ mở lời giải thành ${vnDateTime(at)}.`;
export const confirmOpen = (c: string) => `Mở lời giải ngay cho lớp ${c}? Sau khi mở không khóa lại được.`;

/** "Còn 1 giờ 0 phút" / "Còn 5 phút 3 giây"; không bao giờ âm (US-GEO-612-SC2). */
export function remaining(ms: number): string {
  const s = Math.max(0, Math.ceil(ms / 1000));
  if (s >= 600) {
    // từ 10 phút trở lên: làm tròn lên theo phút ("Còn 1 giờ 0 phút" khi còn 59 phút 58 giây)
    const total = Math.ceil(s / 60);
    const h = Math.floor(total / 60);
    const m = total % 60;
    return h > 0 ? `Còn ${h} giờ ${m} phút` : `Còn ${m} phút`;
  }
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return m > 0 ? `Còn ${m} phút ${sec} giây` : `Còn ${sec} giây`;
}

/** Ô ngày (yyyy-MM-dd) + ô giờ (HH:mm) theo giờ Việt Nam → ISO UTC (BR-GEO-233: thiết bị ở múi giờ nào cũng vậy). */
export function vnToIso(date: string, time: string): string | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || !/^\d{2}:\d{2}$/.test(time)) return null;
  const d = new Date(`${date}T${time}:00+07:00`);
  return Number.isNaN(d.getTime()) ? null : d.toISOString();
}

export function errText(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.code === 'NETWORK') return 'Không kết nối được. Vui lòng kiểm tra mạng và thử lại.';
    return e.message;
  }
  return 'Đã có lỗi xảy ra, vui lòng thử lại.';
}

export interface ClassChoice {
  id: string;
  name: string;
  level: string;
  studentCount: number;
  label: string;
  assignment?: { id: string; releaseState: string; releaseAt?: string } | null;
}
export interface AssignOptions {
  problem: { versionId: string; code: string; title: string; statement: string };
  classes: ClassChoice[];
  serverNow: string;
}
export interface ReleaseView {
  id: string;
  classId: string;
  className: string;
  problemCode: string;
  releaseState: 'unset' | 'upcoming' | 'open';
  releaseAt?: string;
  releaseRev: number;
  serverNow: string;
}
export interface PreviewLock {
  state: 'unset' | 'upcoming' | 'open';
  releaseAt?: string;
  serverNow: string;
}
export interface PreviewPage {
  problem: { versionId: string; code: string; title: string; statement: string };
  lock: PreviewLock;
}
export interface LockInfo {
  locked: boolean;
  kind: 'class' | 'self';
  releaseAt?: string;
  state: 'unset' | 'upcoming' | 'open' | 'locked';
  serverNow: string;
}

export const e6Api = {
  options: (pv: string) => api.get<AssignOptions>(`/api/v1/teacher/problems/${enc(pv)}/assign-options`),
  assign: (body: Record<string, unknown>) => api.post<{ id: string; releaseRev: number }>('/api/v1/teacher/assignments', body),
  releaseView: (id: string) => api.get<ReleaseView>(`/api/v1/teacher/assignments/${enc(id)}/release`),
  setRelease: (id: string, body: { openNow?: boolean; releaseAt?: string | null; expectedRev: number }) =>
    api.post<{ releaseRev: number; releaseAt: string }>(`/api/v1/teacher/assignments/${enc(id)}/release`, body),
  preview: (pv: string, query: Record<string, string | undefined>) =>
    api.get<PreviewPage>(`/api/v1/teacher/preview/${enc(pv)}`, { query: Object.fromEntries(Object.entries(query).filter(([, v]) => v)) as Record<string, string> }),
  previewAsk: (pv: string, question: string, highestHint?: string) =>
    api.post<{ question: string; replyKind: string; reply: string }>(`/api/v1/teacher/preview/${enc(pv)}/questions`, { question, highestHint }),
  todo: () => api.get<{ count: number }>('/api/v1/student/assignments/todo'),
  lock: (id: string) => api.get<LockInfo>(`/api/v1/student/assignments/${enc(id)}/lock`),
};
