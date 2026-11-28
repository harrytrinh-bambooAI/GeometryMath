import { api } from '../../../shared/api/client.ts';
import { ApiError, isApiErrorBody, toErrorCode } from '../../../shared/api/errors.ts';
import { buildUrl } from '../../../shared/api/url.ts';
import { getToken } from '../../../shared/auth/session.ts';

/** S5-E4 · nộp bài viết tay (US-S5E4-03, 09; C5-03). T-261128-59. */

const S = '/api/v1/submissions';

export interface Privacy {
  version: string;
  text: string;
  acked: boolean;
}

export interface SubPage {
  pageNo: number;
  contentType: string;
  sha256: string;
  bytes: number;
}

export interface Submission {
  id: string;
  code: string;
  assignmentId: string;
  status: string;
  statusLabel: string;
  message: string;
  version: number;
  versionId: string;
  submittedAt: string;
  late: boolean;
  lateLabel?: string;
  pages: SubPage[];
}

export interface Mine {
  assignmentId: string;
  title: string;
  dueAt: string;
  allowLate: boolean;
  privacyAcked: boolean;
  submission?: Submission;
}

export interface StudentGraded {
  id: string;
  code: string;
  title: string;
  className?: string;
  dueAt: string;
  allowLate: boolean;
}

export const myGraded = () => api.get<{ items: StudentGraded[] }>('/api/v1/student/graded-assignments');
export const privacy = () => api.get<Privacy>(`${S}/privacy`);
export const ackPrivacy = (version: string) => api.post<Privacy>(`${S}/privacy`, { version });
export const mine = (assignmentId: string) => api.get<Mine>(`${S}/mine?assignmentId=${encodeURIComponent(assignmentId)}`);

export interface SubmitPage {
  uploadId: string;
  derivedUploadId?: string;
  clarity: number;
}

export const submit = (assignmentId: string, idempotencyKey: string, pages: SubmitPage[]) =>
  api.post<Submission>(S, { assignmentId, idempotencyKey, pages });

/**
 * Tải một trang theo đoạn (thân yêu cầu của máy chủ ≤ 1 MB): POST …/uploads {contentType, sizeBytes} → PUT
 * …/uploads/{id}?offset= (application/octet-stream). Lỗi mạng ném ApiError code NETWORK.
 */
export async function uploadPage(blob: Blob, contentType: string): Promise<string> {
  const u = await api.post<{ uploadId: string; partBytes: number }>(`${S}/uploads`, { contentType, sizeBytes: blob.size });
  for (let offset = 0; offset < blob.size; offset += u.partBytes) {
    const chunk = blob.slice(offset, Math.min(blob.size, offset + u.partBytes));
    const headers: Record<string, string> = { Accept: 'application/json', 'Content-Type': 'application/octet-stream' };
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    let res: Response;
    try {
      res = await fetch(buildUrl(`${S}/uploads/${encodeURIComponent(u.uploadId)}`, { offset }), { method: 'PUT', headers, body: chunk });
    } catch {
      throw new ApiError({ code: 'NETWORK', status: 0 });
    }
    if (!res.ok) {
      const text = await res.text();
      const data: unknown = text ? JSON.parse(text) : undefined;
      throw isApiErrorBody(data)
        ? new ApiError({ status: res.status, code: toErrorCode(data.code, res.status), message: data.message, requestId: data.requestId, details: data.details })
        : new ApiError({ status: res.status, code: toErrorCode('', res.status) });
    }
  }
  return u.uploadId;
}
