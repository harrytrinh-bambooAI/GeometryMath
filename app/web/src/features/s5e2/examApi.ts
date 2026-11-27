import { api } from '../../shared/api/client.ts';

/** S5-E2 (T-261128-52): kiểu dữ liệu và lời gọi API ra đề theo ma trận. */
export interface PartDto {
  id: string;
  name: string;
  type: string;
  count: number;
  points: number;
}
export interface ChapterDto {
  id: string;
  name: string;
  selected: boolean;
  topics: string[];
}
export type Levels = Record<'NB' | 'TH' | 'VD' | 'VDC', number>;
export interface MatrixDto {
  code: string;
  id: string;
  name: string;
  kind: string;
  grade: number;
  minutes: number;
  totalPoints: number;
  defaultVariantCount: number;
  parts: PartDto[];
  levels: Levels;
  chapters: ChapterDto[];
}
export interface FormInput {
  matrixCode: string;
  classId?: string | null;
  chapters?: string[];
  minutes?: number;
  parts?: Array<{ id: string; count?: number; points?: number }>;
  levels?: Partial<Levels>;
  variantCount?: number;
  seed?: number | null;
  name?: string;
}
export interface PreviewDto {
  matrixCode: string;
  minutes: number;
  totalPoints: number;
  estimatedMinutes: number;
  variantCount: number;
  parts: PartDto[];
  cells: Array<{ id: string; part: string; type: string; level: string; points: number; topics: string[]; chapters: string[] }>;
}
export interface QuestionDto {
  so: number;
  phan: string;
  o: string;
  id_cau: string;
  chu_de: string;
  dang_tx: string;
  muc_do: string;
  loai_cau: string;
  diem: number;
  de_bai: string;
  dap_an: unknown;
  phuong_an?: Record<'A' | 'B' | 'C' | 'D', string>;
  y?: Array<{ nhan: string; menh_de: string }>;
  nhan_may: string;
}
export interface VariantDto {
  code: string;
  difficulty: number;
  cau: QuestionDto[];
}
export interface ExamSetDto {
  id: string;
  name: string;
  matrixCode: string;
  seed: number;
  status: 'draft' | 'approved';
  humanLabel: string;
  humanLabelCode: string;
  approvedAt: string | null;
  createdAt: string;
  difficultySpread?: number;
  variants?: VariantDto[];
}

export const examApi = {
  catalog: () => api.get<{ items: MatrixDto[] }>('/api/exam/catalog'),
  preview: (body: FormInput) => api.post<PreviewDto>('/api/exams/preview', body),
  create: (body: FormInput) => api.post<ExamSetDto>('/api/exams', body),
  list: (shelf?: 'mine' | 'draft') => api.get<{ items: ExamSetDto[] }>(`/api/exams${shelf ? `?shelf=${shelf}` : ''}`),
  get: (id: string) => api.get<ExamSetDto>(`/api/exams/${encodeURIComponent(id)}`),
  replace: (id: string, code: string, so: number) =>
    api.post<ExamSetDto>(`/api/exams/${encodeURIComponent(id)}/replace`, { code, so }),
  approve: (id: string) => api.post<ExamSetDto>(`/api/exams/${encodeURIComponent(id)}/approve`),
};

export { fieldError, MACHINE_LABEL, TYPE_LABEL, vn } from './format.ts';
