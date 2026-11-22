/** Kiểu dữ liệu API phía học sinh và giáo viên của E1 (khớp máy chủ: AssignmentService, TrackingService…). */
export interface Progress {
  k: number;
  label: string;
}

export interface StudentAssignment {
  id: string;
  problemCode: string;
  statement: string;
  className: string;
  releaseAt?: string;
  releaseState: 'unset' | 'upcoming' | 'open';
  allowHint: boolean;
  allowQa: boolean;
  verifyStatus: 'verified' | 'needs_review';
  progress: Progress;
}

export interface Page<T> {
  items: T[];
  nextCursor?: string;
}

export interface Asked {
  id: number;
  question: string;
  replyKind: string;
  reply: string;
}

export interface CatalogProblem {
  id: string;
  code: string;
  level: string;
  solidKind: string;
  topic?: string;
  versionId: string;
  statement: string;
  verifyStatus: 'verified' | 'needs_review';
}

export interface ClassItem {
  id: string;
  name: string;
  level: string;
}

export interface MonitorStudent {
  userId: string;
  displayName: string;
  opened: boolean;
  k: number;
  label: string;
  askCount: number;
  toolCount: number;
  activeMinutes: number;
}

export interface MonitorQuestion {
  id: number;
  question: string;
  askedAt: string;
  askerName: string;
}

export interface Monitor {
  assignmentId: string;
  problemCode: string;
  className: string;
  releaseState: string;
  studentCount: number;
  openedCount: number;
  students: MonitorStudent[];
  questions: MonitorQuestion[];
}

export const RELEASE_LABEL: Record<string, string> = {
  unset: 'Chưa đặt giờ',
  upcoming: 'Sắp mở',
  open: 'Đã mở',
};

export const VERIFY_LABEL: Record<string, string> = {
  verified: 'Đã kiểm chứng',
  needs_review: 'Cần xem lại',
};
