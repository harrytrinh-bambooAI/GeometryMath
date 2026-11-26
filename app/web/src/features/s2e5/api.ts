import { apiRequest } from '../../shared/api/client.ts';
import type { Scene } from '../../shared/scene/types.ts';

/** S2-E5 · dữ liệu màn C2-03 "Soát và duyệt lời giải" (GET /api/v1/teacher/solutions/{id}). */
export type CheckState = 'pass' | 'fail' | 'unverifiable' | 'unchecked';

export interface StepView {
  no: number;
  text: string;
  check: CheckState;
  checkLabel: string;
  reason?: string | null;
}

export interface VersionView {
  version: number;
  latest: boolean;
  steps: StepView[];
  machineLabel: string;
  machineNote?: string | null;
  humanLabel: string;
  checked: boolean;
  failCount: number;
  summary: string;
  finalState?: 'match' | 'mismatch' | 'unknown' | null;
  finalNote?: string | null;
  approvedCode?: string | null;
  approvedAt?: string | null;
}

export interface ApproveView {
  allowed: boolean;
  approved: boolean;
  needsConfirm: boolean;
  blockedReason?: string | null;
}

export interface HistoryItem {
  kind: 'version' | 'proposal';
  version?: number | null;
  description: string;
  author?: string | null;
  authorCode?: string | null;
  humanLabel?: string | null;
  at?: string | null;
}

export interface ProposalView {
  id: string;
  afterStep: number;
  content: string;
}

export interface DocView {
  id: string;
  code?: string | null;
  title: string;
  statement: string;
  latestVersion: number;
  inMyShelf: boolean;
  saved: boolean;
  notice?: string | null;
  /** Cảnh 3D của đề (hợp đồng cảnh v1, không có đáp số); không có khi máy không dựng được hình. */
  scene?: Scene | null;
  version: VersionView;
  approve: ApproveView;
  history: HistoryItem[];
  proposals: ProposalView[];
  assignments: Array<{ className: string; version: number }>;
}

export interface DocSummary {
  id: string;
  code?: string | null;
  title: string;
  latestVersion: number;
  inMyShelf: boolean;
}

/** Nhãn hai trục của phiên bản đã giao (màn học sinh; không có bước giải hay StepCheck). */
export interface PinnedLabels {
  assignmentId: string;
  version: number;
  machineLabel: string;
  machineNote?: string | null;
  humanLabel: string;
}

const base = (id: string) => `/api/v1/teacher/solutions/${encodeURIComponent(id)}`;

export function fetchDocs(signal?: AbortSignal): Promise<{ items: DocSummary[] }> {
  return apiRequest('/api/v1/teacher/solutions', { signal });
}

export function fetchDoc(id: string, version?: number, signal?: AbortSignal): Promise<DocView> {
  return apiRequest(base(id), { query: version ? { version } : undefined, signal });
}

export function saveVersion(id: string, baseVersion: number, steps: string[], proposalId?: string): Promise<DocView> {
  return apiRequest(`${base(id)}/versions`, { method: 'POST', body: { baseVersion, steps, proposalId } });
}

export function reuseVersion(id: string, version: number): Promise<DocView> {
  return apiRequest(`${base(id)}/versions/${version}/reuse`, { method: 'POST', body: {} });
}

export function checkVersion(id: string, version: number): Promise<DocView> {
  return apiRequest(`${base(id)}/check`, { method: 'POST', body: { version } });
}

export function approveVersion(id: string, version: number, confirmNeedsReview: boolean): Promise<DocView> {
  return apiRequest(`${base(id)}/approve`, { method: 'POST', body: { version, confirmNeedsReview } });
}

export function decideProposal(id: string, itemId: string, action: 'accept' | 'dismiss'): Promise<DocView> {
  return apiRequest(`${base(id)}/proposals/${encodeURIComponent(itemId)}/${action}`, { method: 'POST', body: {} });
}

export function fetchPinnedLabels(assignmentId: string, signal?: AbortSignal): Promise<PinnedLabels> {
  return apiRequest(`/api/v1/student/assignments/${encodeURIComponent(assignmentId)}/solution-labels`, { signal });
}
