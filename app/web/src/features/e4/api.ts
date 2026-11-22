import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';

/** Gọi máy chủ của E4 (gợi ý, lời giải, hỏi đáp, nhật ký, bài giảng, đọc ảnh đề). */
const enc = encodeURIComponent;

export type SolutionStateName = 'open' | 'release' | 'hints' | 'no_signature' | 'inherited';
export interface HintView {
  level: number;
  title: string;
  body: string;
}
export interface HelpState {
  kind: 'class' | 'self';
  k: number;
  allowHint: boolean;
  allowQa: boolean;
  hintMessage?: string | null;
  qaMessage?: string | null;
  hints: HintView[];
  solution: { state: SolutionStateName; message?: string | null; releaseAt?: string | null };
}
export interface StepHighlight {
  points?: string[];
  edges?: string[];
  faces?: string[];
  angles?: string[];
}
export type StepDraw =
  | { kind: 'segment'; a: string; b: string }
  | { kind: 'right'; at: string; a: string; b: string }
  | { kind: 'arc'; at: string; a: string; b: string; label?: string };
export interface SolutionStep {
  title: string;
  body: string;
  why?: string;
  highlight?: StepHighlight;
  draw?: StepDraw[];
}
export interface SolutionView {
  finalAnswer: string;
  steps: SolutionStep[];
  machineLabel: string;
  humanLabel: string;
  k: number;
  firstView: boolean;
}
export interface Source {
  slug: string;
  title: string;
  label: string;
}
export interface Asked {
  id: number;
  question: string;
  replyKind: string;
  reply: string;
  source?: Source | null;
}
export interface SavedAnswer extends Asked {
  assignmentId: string;
  askedAt: string;
}
export interface LogItem {
  id: number;
  assignmentId: string;
  problemTitle: string;
  question: string;
  askedAt: string;
}
export interface Lecture {
  slug: string;
  title: string;
  body: string;
  sourceLabel: string;
  sourceRef?: string | null;
  aiLabel?: string | null;
  reviewNote?: string | null;
}
export interface Keyword {
  term: string;
  slug: string;
}

export const e4Api = {
  help: (id: string) => api.get<HelpState>(`/api/v1/student/assignments/${enc(id)}/help`),
  hint: (id: string, level: number) =>
    api.post<HintView & { k: number }>(`/api/v1/student/assignments/${enc(id)}/hints/${level}`, {
      requestId: crypto.randomUUID(),
    }),
  openSolution: (id: string) => api.post<SolutionView>(`/api/v1/student/assignments/${enc(id)}/solution/open`, {}),
  ask: (id: string, question: string, requestId: string) =>
    api.post<Asked>(`/api/v1/student/assignments/${enc(id)}/questions`, { question, requestId }),
  log: (assignmentId: string | null, cursor?: string | null) => {
    const q = new URLSearchParams();
    if (assignmentId) q.set('assignmentId', assignmentId);
    if (cursor) q.set('cursor', cursor);
    const qs = q.toString();
    return api.get<{ items: LogItem[]; nextCursor?: string | null }>(`/api/v1/student/qa-log${qs ? `?${qs}` : ''}`);
  },
  answer: (qaId: number) => api.get<SavedAnswer>(`/api/v1/questions/${qaId}/answer`),
  keywords: () => api.get<{ items: Keyword[] }>('/api/v1/lectures/keywords'),
  lectureByTerm: (term: string) => api.get<Lecture>(`/api/v1/lectures/by-term?term=${enc(term)}`),
  lecture: (slug: string) => api.get<Lecture>(`/api/v1/lectures/${enc(slug)}`),
  ocr: (image: string) => api.post<{ statement: string }>('/api/v1/student/self-study/ocr', { image }),
  sceneShown: (id: string, durationMs: number) =>
    api.post<{ status: string }>(`/api/v1/student/assignments/${enc(id)}/scene-shown`, { durationMs }),
};

/** Thông điệp E4 (nguyên văn work/05_dictionary.md). */
export const M = {
  '04': 'Không tìm thấy nội dung này.',
  '05': 'Danh sách đã thay đổi. Vui lòng tải lại từ đầu.',
  '06': 'Nội dung gửi lên quá lớn (tối đa 1 MB).',
  '08': 'Không kết nối được. Vui lòng kiểm tra mạng và thử lại.',
  '13': 'Đang dựng hình, em chờ một chút…',
  '14': 'Trợ lý đang bận. Em thử lại sau ít phút nhé.',
  '15': 'Trợ lý đang soạn câu trả lời…',
  '20': 'Em hãy nhập đề bài.',
  '21': 'Đề bài tối đa 8000 ký tự.',
  '22': 'Chưa đọc được chữ trong ảnh. Em chụp lại rõ hơn hoặc gõ đề.',
  '30': 'Ở chế độ tự học, em xem gợi ý 1 và 2 trước, sau đó mới mở được lời giải.',
  '31': 'Đề này trùng với bài lớp em đang làm. Lời giải mở theo giờ giáo viên đặt.',
  '32': 'Giáo viên đã tắt gợi ý cho bài này.',
  '33': 'Giáo viên đã tắt hỏi đáp cho bài này.',
  '34': 'Em xem Gợi ý 1 trước nhé.',
  '37': 'Câu hỏi cần từ 1 đến 1000 ký tự.',
  '38': 'Em chưa hỏi câu nào ở bài này.',
  '39': 'Chưa có bài giảng cho từ khóa này.',
  '58': 'Đề này chưa dựng được bằng hình mẫu nên chưa có lời giải tự học. Em dùng gợi ý nhé.',
} as const;

/** Lỗi của trợ lý (AI) hiện MSG-GEO-14 kèm "Thử lại"; mất mạng MSG-GEO-08. */
export function isAssistantError(e: unknown): boolean {
  return (
    e instanceof ApiError &&
    (e.code === 'TIMEOUT' || e.code === 'DEPENDENCY_UNAVAILABLE' || e.code === 'INTERNAL' || e.status >= 500)
  );
}
export function isNetworkError(e: unknown): boolean {
  return e instanceof ApiError && e.code === 'NETWORK';
}
