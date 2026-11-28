import { api } from '../../../shared/api/client.ts';

/** S5-E6b · màn chấm có đề xuất AI (US-S5E6-04), AI không chấm (US-S5E6-02), chạy song song (US-S5E6-05). T-261128-62. */

export interface Region {
  trang: number;
  x: number;
  y: number;
  w: number;
  h: number;
}

/** Khóa `ai` do điểm cắm AiProposalSlot thêm vào màn chấm / hàng chờ. */
export interface AiExtras {
  aiDone?: boolean;
  shadow?: boolean;
  message?: string;
  aiStatus?: 'pending' | 'off' | 'proposed' | 'no_grade';
  aiStatusLabel?: string;
  aiFlags?: string[];
  reason?: string;
  retryable?: boolean;
  acceptAll?: boolean;
  source?: string;
  actions?: Array<{ ord: number; name: string; action: string; aiPoints: number; teacherPoints: number }>;
  comparisonReady?: boolean;
}

export interface Criterion {
  ord: number;
  name: string;
  maxPoints: number;
}

export interface ReviewView {
  submissionId: string;
  code: string;
  assignmentId: string;
  studentName: string;
  status: string;
  statusLabel: string;
  flags: Array<{ code: string; label: string }>;
  pages: number;
  version: number;
  versionId: string;
  assignment: { title: string; scale: number; rubricCode: string; rubricVersion: number };
  criteria: Criterion[];
  grade?: { seq: number; total: number; criteria: Array<Criterion & { points: number }> };
  nextSubmissionId: string | null;
  prevSubmissionId: string | null;
  message?: string;
  ai?: AiExtras;
}

export interface ProposalCriterion {
  ord: number;
  name: string;
  maxPoints: number;
  points: number;
  reason: string;
  region: Region | null;
  confidence: number;
  flags: string[];
}

export interface Proposal {
  status: 'pending' | 'proposed' | 'no_grade';
  statusLabel: string;
  flags: string[];
  total?: number;
  confidence?: number;
  criteria?: ProposalCriterion[];
  engine?: { status: string; statusLabel: string; message?: string; note?: string };
}

export interface Comparison {
  criteria: Array<{ ord: number; name: string; maxPoints: number; aiPoints: number | null; teacherPoints: number; differs: boolean }>;
  pairs: number;
  differing: number;
  aiDone: boolean;
}

export const review = (id: string) => api.get<ReviewView>(`/api/v1/teacher/grading/submissions/${id}`);
export const proposal = (id: string) => api.get<Proposal>(`/api/v1/submissions/${id}/proposal`);
export const comparison = (id: string) => api.get<Comparison>(`/api/v1/submissions/${id}/comparison`);
export const retry = (id: string) => api.post<{ status: string; message: string }>(`/api/v1/submissions/${id}/proposal/retry`);
export const pageUrl = (versionId: string, n: number) => api.get<{ url: string }>(`/api/v1/submissions/${versionId}/pages/${n}/url`);

export interface GradeBody {
  version: number;
  criteria?: Array<{ ord: number; points: number }>;
  aiActions?: Array<{ ord: number; action: string }>;
  acceptAll?: boolean;
  comment: string;
}

export const grade = (id: string, body: GradeBody) => api.post<ReviewView>(`/api/v1/submissions/${id}/grade`, body);
