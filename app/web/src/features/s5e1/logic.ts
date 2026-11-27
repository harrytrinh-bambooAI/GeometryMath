/** S5-E1 · logic thuần của form "Soạn giáo án mới" (giao-dien.md §1; máy chủ kiểm lại mọi ràng buộc). */

export interface CatalogPeriod {
  id: string;
  label: string;
  periods: number;
  focus: string[];
}

export interface CatalogKnowledge {
  code: string;
  statement: string;
  status: 'da-kiem' | 'nhap-tam';
}

export interface CatalogLesson {
  no: number;
  title: string;
  periods: CatalogPeriod[];
  knowledge: CatalogKnowledge[];
}

export interface CatalogChapter {
  no: number;
  label: string;
  title: string;
  lessons: CatalogLesson[];
}

export type Level = 'NB' | 'TH' | 'VD' | 'VDC';

export const MSG_REQUIRED = 'Chọn Tiết PPCT, Đối tượng và ít nhất một kiến thức trọng tâm.'; // MSG-S5-01
export const MSG_RATIO = 'Tổng tỉ lệ NB/TH/VD/VDC phải bằng số bài tập.'; // MSG-S5-02
export const MSG_NOTE = 'Ghi chú tối đa 500 ký tự.'; // MSG-S5-07
export const MSG_FAILED = 'Chưa soạn xong, thầy cô bấm Thử lại.'; // MSG-S5-03
export const MSG_REFUSED = 'AI không xử lý được yêu cầu này.'; // MSG-S5-04
export const NOTE_MAX = 500;
export const LEVELS: Level[] = ['NB', 'TH', 'VD', 'VDC'];

export const AUDIENCES = [
  { code: 'co-ban', label: 'Cơ bản' },
  { code: 'kha', label: 'Khá' },
  { code: 'chuyen', label: 'Chuyên' },
] as const;

export const STYLES = [
  { code: 'truyen-thong', label: 'Truyền thống' },
  { code: 'kham-pha', label: 'Khám phá' },
  { code: 'lop-hoc-dao-nguoc', label: 'Lớp học đảo ngược' },
  { code: 'du-an-stem', label: 'Dự án STEM' },
  { code: 'tro-choi', label: 'Trò chơi' },
] as const;

/** Trạng thái form phía máy khách. */
export interface FormState {
  chapter: number | null;
  lesson: number | null;
  period: string | null;
  soTiet: number | null;
  doiTuong: string | null;
  phongCach: string;
  kienThuc: string[];
  phieuHocTap: boolean;
  soBaiTap: number | null;
  tiLe: Partial<Record<Level, number>>;
  ghiChu: string;
}

export const EMPTY: FormState = {
  chapter: null,
  lesson: null,
  period: null,
  soTiet: null,
  doiTuong: null,
  phongCach: 'truyen-thong',
  kienThuc: [],
  phieuHocTap: true,
  soBaiTap: null,
  tiLe: {},
  ghiChu: '',
};

export function findLesson(chapters: CatalogChapter[], no: number | null): CatalogLesson | undefined {
  for (const c of chapters) for (const l of c.lessons) if (l.no === no) return l;
  return undefined;
}

export function findPeriod(chapters: CatalogChapter[], id: string | null): { lesson: CatalogLesson; period: CatalogPeriod; chapter: number } | undefined {
  for (const c of chapters)
    for (const l of c.lessons)
      for (const p of l.periods) if (p.id === id) return { lesson: l, period: p, chapter: c.no };
  return undefined;
}

/** Đổi Tiết PPCT: số tiết và các ô kiến thức tích sẵn được điền lại (giao-dien.md §1 quy tắc 2). */
export function choosePeriod(s: FormState, chapters: CatalogChapter[], id: string | null): FormState {
  const found = findPeriod(chapters, id);
  if (!found) return { ...s, period: null, soTiet: null, kienThuc: [] };
  return { ...s, chapter: found.chapter, lesson: found.lesson.no, period: id, soTiet: found.period.periods, kienThuc: [...found.period.focus] };
}

/** Tổng tỉ lệ (ô trống = 0). */
export function ratioSum(t: Partial<Record<Level, number>>): number {
  return LEVELS.reduce((n, l) => n + (t[l] ?? 0), 0);
}

export function hasRatio(t: Partial<Record<Level, number>>): boolean {
  return LEVELS.some((l) => t[l] !== undefined);
}

/** Lỗi theo trường (rỗng = gửi được). Khoá trùng tên trường của máy chủ. */
export function validate(s: FormState): Record<string, string> {
  const e: Record<string, string> = {};
  if (!s.period) e.tietPpct = MSG_REQUIRED;
  if (!s.doiTuong) e.doiTuong = MSG_REQUIRED;
  if (s.kienThuc.length === 0) e.kienThuc = MSG_REQUIRED;
  if (s.soTiet !== null && (s.soTiet < 1 || s.soTiet > 5)) e.soTiet = 'Số tiết từ 1 đến 5.';
  if (s.soBaiTap !== null && (s.soBaiTap < 0 || s.soBaiTap > 20)) e.soBaiTap = 'Số bài tập từ 0 đến 20.';
  if (hasRatio(s.tiLe) && (s.soBaiTap === null || ratioSum(s.tiLe) !== s.soBaiTap)) e.tiLe = MSG_RATIO;
  if ([...s.ghiChu].length > NOTE_MAX) e.ghiChu = MSG_NOTE;
  return e;
}

/** Số nguyên từ ô nhập ('' = null). */
export function intOrNull(v: string): number | null {
  if (v.trim() === '') return null;
  const n = Number(v);
  return Number.isInteger(n) ? n : null;
}
