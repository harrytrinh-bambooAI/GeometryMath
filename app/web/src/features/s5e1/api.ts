import { apiRequest } from '../../shared/api/client.ts';
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
}

export const fetchCatalog = (signal?: AbortSignal) => apiRequest<{ chapters: CatalogChapter[] }>(`${BASE}/catalog`, { signal });
export const createPlan = (body: LessonFormBody) => apiRequest<Created>(BASE, { method: 'POST', body });
export const fetchPlan = (id: string, signal?: AbortSignal) => apiRequest<PlanView>(`${BASE}/${encodeURIComponent(id)}`, { signal });
export const retryPlan = (id: string) => apiRequest<Created>(`${BASE}/${encodeURIComponent(id)}/retry`, { method: 'POST', body: {} });
