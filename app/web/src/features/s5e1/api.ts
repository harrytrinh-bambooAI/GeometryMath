import { apiRequest } from '../../shared/api/client.ts';
import { buildUrl } from '../../shared/api/url.ts';
import { getToken } from '../../shared/auth/session.ts';
import type { CatalogChapter, Level } from './logic.ts';

/** S5-E1 · trợ lý soạn giáo án (T-261128-53): API /api/v1/teacher/lesson-plans. */
const BASE = '/api/v1/teacher/lesson-plans';

export type { CatalogChapter, CatalogKnowledge, CatalogLesson, CatalogPeriod, Level } from './logic.ts';

/** Form B/C gửi máy chủ (truong.md v1.1). */
export interface LessonFormBody {
  tietPpct: string;
  doiTuong: string;
  phongCach: string;
  kienThuc: string[];
  soTiet: number | null;
  phieuHocTap: boolean;
  soBaiTap: number | null;
  tiLe: Partial<Record<Level, number>> | null;
  phanHoa: string | null;
  phanMem: string | null;
  thietBi: string[];
  ghiChu: string | null;
}

export interface Created {
  draftId: string;
  code: string;
  status: string;
  statusLabel: string;
  humanLabel: string;
}

export interface Activity {
  ma: string;
  nhan: string;
  loai: string;
  loaiLabel: string;
  ten: string;
  thoiLuongPhut: number;
  oNha: boolean;
  mucTieu: string;
  noiDung: string;
  sanPham: string;
  toChuc: { chuyenGiao: string; thucHien: string; baoCao: string; ketLuan: string };
  kienThuc: Array<{ kt: string; phatBieu: string }>;
  chuaSoat: string[];
  unsure: string[];
}

export interface Question {
  so: number;
  nguon: string;
  mucDo: string;
  de: string;
  dapSo: string | null;
  dapSoNguon: string;
  nhanMay: string;
  buocGoiY: string[];
}

export interface Proposal {
  viTri: string;
  vanDe: string;
  goiY: string;
  lyDo: string;
}

export interface LessonBody {
  sections: string[];
  mucTieu: { kienThuc: Array<{ noi_dung: string; kt: string[] }>; nangLuc: string[]; phamChat: string[] };
  thietBi: { giaoVien: string[]; hocSinh: string[] };
  hoatDong: Activity[];
  tongPhut: number;
  phieuHocTap: Array<{ ma: string; cau: Question[] }>;
  unsure: string[];
  deXuat: Proposal[];
}

export interface PlanView {
  draftId: string;
  code: string;
  status: 'drafting' | 'ready' | 'error' | 'question' | 'reply' | 'refused';
  statusLabel: string;
  humanLabel: string;
  version: number;
  title: string;
  periodId: string;
  request: LessonFormBody;
  lockedSections: string[];
  kind?: 'lesson' | 'question' | 'reply';
  lesson?: LessonBody;
  reply?: { text: string; field: string | null };
  message?: string;
  // T-261128-54: duyệt / phiên bản (US-S5E1-04, 05)
  approvedVersion?: number | null;
  approvedBy?: string | null;
  approvedAt?: string | null;
  canUndo?: boolean;
}

export const fetchCatalog = (signal?: AbortSignal) => apiRequest<{ chapters: CatalogChapter[] }>(`${BASE}/catalog`, { signal });
export const createPlan = (body: LessonFormBody) => apiRequest<Created>(BASE, { method: 'POST', body });
export const fetchPlan = (id: string, signal?: AbortSignal) => apiRequest<PlanView>(`${BASE}/${encodeURIComponent(id)}`, { signal });
export const retryPlan = (id: string) => apiRequest<Created>(`${BASE}/${encodeURIComponent(id)}/retry`, { method: 'POST', body: {} });

// ---------------------------------------------------------------- T-261128-54: chat vá, duyệt, xuất, danh sách

export interface PatchLine {
  index: number;
  op: string;
  path: string;
  label: string;
  before: string | null;
  after: string | null;
  lyDo: string;
  decision: 'pending' | 'accepted' | 'skipped';
}

/** Phản hồi chat vá: bản vá (patch), câu hỏi lại (question), trả lời (reply) hoặc lỗi sau gọi lại (error). */
export interface ChatReply {
  kind: 'patch' | 'question' | 'reply' | 'error';
  text?: string;
  patchId?: string;
  section?: string | null;
  version: number;
  approvedVersion?: number | null;
  humanLabel?: string;
  lockedSections?: string[];
  lines?: PatchLine[];
  deXuat: Proposal[];
}

export interface PlanState {
  draftId: string;
  version: number;
  approvedVersion: number | null;
  humanLabel: string;
  lockedSections: string[];
}

export interface MyLesson {
  draftId: string;
  code: string;
  title: string;
  status: PlanView['status'];
  statusLabel: string;
  humanLabel: string;
  version: number;
  approvedVersion: number | null;
  updatedAt: string;
}

const one = (id: string) => `${BASE}/${encodeURIComponent(id)}`;

export const sendChat = (id: string, section: string | null, message: string) =>
  apiRequest<ChatReply>(`${one(id)}/chat`, { method: 'POST', body: { section, message } });
export const acceptPatch = (id: string, patchId: string, lines: number[] | null) =>
  apiRequest<ChatReply>(`${one(id)}/patches/${encodeURIComponent(patchId)}/accept`, { method: 'POST', body: { lines } });
export const skipPatch = (id: string, patchId: string, lines: number[] | null) =>
  apiRequest<ChatReply>(`${one(id)}/patches/${encodeURIComponent(patchId)}/skip`, { method: 'POST', body: { lines } });
export const undoPlan = (id: string) => apiRequest<PlanState>(`${one(id)}/undo`, { method: 'POST', body: {} });
export const lockSection = (id: string, section: string, locked: boolean) =>
  apiRequest<PlanState>(`${one(id)}/locks`, { method: 'PUT', body: { section, locked } });
export const approvePlan = (id: string) => apiRequest<MyLesson>(`${one(id)}/approve`, { method: 'POST', body: {} });
export const fetchMyLessons = (shelf: 'approved' | 'draft' | null, signal?: AbortSignal) =>
  apiRequest<{ items: MyLesson[] }>(BASE, { query: shelf ? { shelf } : undefined, signal });

/** Tên tệp từ Content-Disposition (filename* RFC 5987 hoặc filename). */
export function fileNameOf(disposition: string | null, fallback: string): string {
  if (!disposition) return fallback;
  const star = /filename\*=UTF-8''([^;]+)/i.exec(disposition);
  if (star?.[1]) return decodeURIComponent(star[1]);
  const plain = /filename="?([^";]+)"?/i.exec(disposition);
  return plain?.[1] ?? fallback;
}

/** Tải tệp xuất bằng thẻ rồi lưu về máy (US-S5E1-05). Trả thông điệp lỗi hoặc null. */
export async function downloadExport(id: string, format: 'docx' | 'pdf'): Promise<string | null> {
  try {
    const res = await fetch(buildUrl(`${one(id)}/export`, { format }), {
      headers: { Authorization: `Bearer ${getToken() ?? ''}` },
      cache: 'no-store',
    });
    if (!res.ok) {
      const body = (await res.json().catch(() => null)) as { message?: string } | null;
      return body?.message ?? 'Chưa xuất được, thầy cô thử lại.';
    }
    const name = fileNameOf(res.headers.get('Content-Disposition'), `giao-an.${format}`);
    const url = URL.createObjectURL(await res.blob());
    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    a.dataset.testid = 's5e1-download-link';
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 10_000);
    return null;
  } catch {
    return 'Chưa xuất được. Thầy cô kiểm tra kết nối mạng rồi thử lại.';
  }
}
