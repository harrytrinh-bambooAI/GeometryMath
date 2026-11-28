import { api } from '../../shared/api/client.ts';
import { ApiError, isApiErrorBody, toErrorCode } from '../../shared/api/errors.ts';
import { buildUrl } from '../../shared/api/url.ts';
import { getToken } from '../../shared/auth/session.ts';

/** S5-E3 · kho tri thức của giáo viên (US-S5E3-01, 02). T-261128-56. */

import type { KbStatus } from './logic.ts';

export type { KbStatus };

export interface KbListItem {
  id: string;
  code: string;
  title: string;
  humanLabel: string;
  activeVersion?: number;
  latestVersion: number;
  status: KbStatus;
  statusLabel: string;
  archived: boolean;
  updatedAt: string;
}

export interface KbSection {
  ord: number;
  title: string;
  body: string;
  codes: string[];
}

export interface KbDoc {
  id: string;
  code: string;
  title: string;
  humanLabel: string;
  archived: boolean;
  activeVersion?: number;
  version: number;
  status: KbStatus;
  statusLabel: string;
  wasActive: boolean;
  sourceKind: string;
  sourceLabel?: string;
  fileName?: string;
  content: string;
  sections: KbSection[];
  versions: Array<{ version: number; status: KbStatus; statusLabel: string; sourceLabel?: string; createdAt: string }>;
  message?: string;
  duplicate?: boolean;
}

export interface KbGrant {
  id: string;
  classId: string;
  assignmentId?: string;
  validFrom?: string;
  validTo?: string;
  effective: boolean;
}

export interface ClassLine {
  id: string;
  name: string;
}

const KB = '/api/v1/teacher/knowledge';
const ref = (r: string) => `${KB}/${encodeURIComponent(r)}`;

export const myClasses = () => api.get<{ items: ClassLine[] }>('/api/v1/teacher/my-classes');
export const listDocs = () => api.get<{ items: KbListItem[] }>(KB);
export const getDoc = (r: string, version?: number) =>
  api.get<KbDoc>(ref(r), version ? { query: { version } } : undefined);
export const createManual = (title: string, content: string) => api.post<KbDoc>(KB, { title, content });
export const fromLessonPlan = (code: string) => api.post<KbDoc>(KB, { fromLessonPlan: code });
export const editVersion = (r: string, v: number, content: string) => api.put<KbDoc>(`${ref(r)}/versions/${v}`, { content });
export const submitVersion = (r: string, v: number) => api.post<KbDoc>(`${ref(r)}/versions/${v}/submit`);
export const activateVersion = (r: string, v: number) => api.post<KbDoc>(`${ref(r)}/versions/${v}/activate`);
export const archiveDoc = (r: string) => api.post<KbDoc>(`${ref(r)}/archive`);
export const listGrants = (r: string) => api.get<{ items: KbGrant[] }>(`${ref(r)}/grants`);
export const addGrant = (r: string, body: { classId: string; validTo?: string | null }) =>
  api.post<KbGrant>(`${ref(r)}/grants`, body);
export const revokeGrant = (r: string, id: string) => api.delete<{ id: string }>(`${ref(r)}/grants/${id}`);

/**
 * Tải tệp theo đoạn (thân yêu cầu của máy chủ ≤ 1 MB): POST …/uploads {fileName, sizeBytes} → PUT …/uploads/{id}?offset=
 * (application/octet-stream) → POST …/knowledge {uploadId}. Lỗi ném ApiError như apiRequest.
 */
export async function uploadFile(file: File): Promise<KbDoc> {
  const u = await api.post<{ uploadId: string; partBytes: number }>(`${KB}/uploads`, { fileName: file.name, sizeBytes: file.size });
  for (let offset = 0; offset < file.size; offset += u.partBytes) {
    const chunk = file.slice(offset, Math.min(file.size, offset + u.partBytes));
    const headers: Record<string, string> = { Accept: 'application/json', 'Content-Type': 'application/octet-stream' };
    const token = getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    let res: Response;
    try {
      res = await fetch(buildUrl(`${KB}/uploads/${encodeURIComponent(u.uploadId)}`, { offset }), { method: 'PUT', headers, body: chunk });
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
  return api.post<KbDoc>(KB, { uploadId: u.uploadId });
}
