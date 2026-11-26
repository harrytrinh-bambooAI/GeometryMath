/**
 * S2-E4 · hàm thuần của màn "Chỉnh nháp với AI" (C2-04a) và "Nhập từ tệp" (C2-04b…d). Máy chủ là nguồn sự thật;
 * các kiểm ở đây chỉ để báo sớm, câu chữ trùng với máy chủ.
 */
export const MAX_MESSAGE_CHARS = 1000;
export const MAX_FILE_BYTES = 10_485_760;
/** Mỗi đoạn tải tệp (thân yêu cầu của máy chủ ≤ 1 MB). */
export const CHUNK_BYTES = 1_000_000;

export const MSG = {
  tooLong: 'Tin tối đa 1000 ký tự',
  turnsFull: 'Nháp này đã đủ 30 lượt chat, hãy duyệt hoặc tạo nháp mới',
  noUndo: 'Hết bước hoàn tác',
  onlyDocxPdf: 'Chỉ nhận docx và pdf',
  fileTooBig: 'Tệp tối đa 10 MB',
  oneFile: 'Mỗi lần tải một tệp',
  theory: 'Phần lý thuyết chưa hỗ trợ ở chặng này',
  exists: 'đã có trong kho',
  update: 'cập nhật khi lưu',
  generic: 'Có lỗi, vui lòng thử lại.',
};

/** Số ký tự theo điểm mã (ký hiệu toán √, ⊥ đếm một). */
export const charCount = (s: string): number => [...s].length;

/** Lý do chưa gửi được tin (null = gửi được). Tin rỗng: không gửi, không báo lỗi (US-S2E4-01-BV2). */
export function messageIssue(text: string): { sendable: boolean; message: string | null } {
  const t = text.trim();
  if (t.length === 0) return { sendable: false, message: null };
  if (charCount(t) > MAX_MESSAGE_CHARS) return { sendable: false, message: MSG.tooLong };
  return { sendable: true, message: null };
}

/** Chọn tệp import: chỉ một tệp, đuôi docx / pdf, tối đa 10 MB. */
export function pickFile<T extends { name: string; size: number }>(files: T[]): { file: T | null; message: string | null } {
  const f = files[0];
  if (!f) return { file: null, message: null };
  const name = f.name.toLowerCase();
  if (!name.endsWith('.docx') && !name.endsWith('.pdf')) return { file: null, message: MSG.onlyDocxPdf };
  if (f.size > MAX_FILE_BYTES) return { file: null, message: MSG.fileTooBig };
  return { file: f, message: files.length > 1 ? MSG.oneFile : null };
}

export type ItemState = 'new' | 'similar' | 'exists' | 'update' | 'too_long' | 'created';

export interface ItemLike {
  index: number;
  state: ItemState;
  selectable: boolean;
  checked: boolean;
  draftId?: string | null;
}

/** Các mục tick sẵn theo mặc định của máy chủ (mục đã có trong kho KHÔNG tick, BR-S2E4-13). */
export const defaultSelection = (items: ItemLike[]): number[] =>
  items.filter((i) => i.selectable && i.checked && !i.draftId).map((i) => i.index);

/** Tóm tắt cột "Sẽ nhập": số bài sẽ thành nháp, số bài trùng chưa chọn, số bài sẽ cập nhật. */
export function summarize(items: ItemLike[], selected: number[]): { toCreate: number; toUpdate: number; duplicates: number; done: number } {
  const sel = new Set(selected);
  let toCreate = 0;
  let toUpdate = 0;
  let duplicates = 0;
  let done = 0;
  for (const i of items) {
    if (i.draftId) done++;
    else if (i.state === 'exists') duplicates++;
    else if (sel.has(i.index) && i.selectable) {
      if (i.state === 'update') toUpdate++;
      else toCreate++;
    }
  }
  return { toCreate, toUpdate, duplicates, done };
}

/** Đường dẫn mở đề trong kho từ kết quả đối chiếu (null = không có gì để mở). */
export function refLink(kind: string | null | undefined, id: string | null | undefined): string | null {
  if (!kind || !id) return null;
  const e = encodeURIComponent(id);
  if (kind === 'library') return `/gv/kho-de-cua-toi/kho-chung/${e}`;
  if (kind === 'mine') return `/gv/kho-de-cua-toi/de/${e}`;
  if (kind === 'draft') return `/gv/chinh-nhap/${e}`;
  return null;
}

/** Dung lượng tệp hiển thị kiểu Việt Nam (dấu phẩy thập phân). */
export function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1_048_576) return `${(bytes / 1024).toFixed(1).replace('.', ',')} KB`;
  return `${(bytes / 1_048_576).toFixed(1).replace('.', ',')} MB`;
}
