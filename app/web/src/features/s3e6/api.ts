import { api } from '../../shared/api/client.ts';
import { buildUrl } from '../../shared/api/url.ts';
import { getToken } from '../../shared/auth/session.ts';

/** S3-E6 — máy khách API: chữa bài từ Kết quả lớp (US-S3E6-01), xuất kết quả, lưu trữ lớp (US-S3E6-02). */

const enc = encodeURIComponent;
const R = '/api/v1/teacher/class-results';
const itemPath = (classId: string, setId: string, position: number) =>
  `${R}/${enc(classId)}/sets/${enc(setId)}/items/${position}/present`;

export const MSG = {
  s91: 'Lớp sẽ thấy lời giải trên màn chiếu. Máy học sinh vẫn khóa lời giải. Tiếp tục?',
  s92: 'Đề đã được sửa. Mở Trình chiếu từ đầu đề.',
  s93: (name: string) => `Lưu trữ lớp ${name}? Lớp chỉ còn xem, mã lớp ngừng dùng.`,
  s94: 'Đã xuất quá nhiều lần. Vui lòng thử lại sau ít phút.',
  s96: 'Lớp này đã lưu trữ. Em chỉ xem được.',
  chip: 'Máy học sinh vẫn khóa lời giải',
  offline: 'Chưa mở được Trình chiếu. Hãy kiểm tra kết nối mạng rồi thử lại.',
} as const;

export interface Preflight {
  locked: boolean;
  releaseAt?: string | null;
  serverNow: string;
}

export interface Presentation {
  sessionId: string;
  title: string;
  statement: string;
  steps: string[];
  startStep: number;
  locked: boolean;
  notice?: string | null;
}

export interface MyClass {
  className: string;
  status: string;
  archived: boolean;
}

export const s3e6Api = {
  preflight: (classId: string, setId: string, position: number) =>
    api.get<Preflight>(`${itemPath(classId, setId, position)}/preflight`),
  present: (classId: string, setId: string, position: number, step: number | null) =>
    api.post<Presentation>(itemPath(classId, setId, position), step ? { step } : {}),
  archive: (classId: string) => api.post<{ status: string }>(`/api/v1/teacher/my-classes/${enc(classId)}/archive`),
  unarchive: (classId: string) => api.post<{ status: string; code: string }>(`/api/v1/teacher/my-classes/${enc(classId)}/unarchive`),
  classDetail: (classId: string) =>
    api.get<{ id: string; name: string; status: 'active' | 'archived'; code: string }>(`/api/v1/teacher/my-classes/${enc(classId)}`),
  myClasses: () => api.get<{ items: MyClass[] }>('/api/v1/student/classes'),
};

export type ExportResult = { ok: true; fileName: string; blob: Blob } | { ok: false; message: string };

/** Tên tệp từ Content-Disposition (filename* RFC 5987 hoặc filename). */
export function fileNameOf(disposition: string | null): string {
  if (!disposition) return 'ket-qua.csv';
  const star = /filename\*=UTF-8''([^;]+)/i.exec(disposition);
  if (star?.[1]) return decodeURIComponent(star[1]);
  const plain = /filename="?([^";]+)"?/i.exec(disposition);
  return plain?.[1] ?? 'ket-qua.csv';
}

/** Xuất CSV (US-S3E6-02): tải bằng thẻ, trả blob và tên tệp do máy chủ đặt. */
export async function exportCsv(classId: string, setId: string): Promise<ExportResult> {
  try {
    const res = await fetch(buildUrl(`${R}/${enc(classId)}/sets/${enc(setId)}/export`), {
      headers: { Authorization: `Bearer ${getToken() ?? ''}` },
      cache: 'no-store',
    });
    if (res.status === 429) return { ok: false, message: MSG.s94 };
    if (!res.ok) {
      const body = (await res.json().catch(() => null)) as { message?: string } | null;
      return { ok: false, message: body?.message ?? 'Chưa xuất được, vui lòng thử lại.' };
    }
    return { ok: true, fileName: fileNameOf(res.headers.get('Content-Disposition')), blob: await res.blob() };
  } catch {
    return { ok: false, message: 'Chưa xuất được. Hãy kiểm tra kết nối mạng rồi thử lại.' };
  }
}
