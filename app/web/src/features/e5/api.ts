import { api } from '../../shared/api/client.ts';
import type { Scene } from '../../shared/scene/types.ts';

/** API Kho đề của E5 (máy chủ: content/internal/web/CatalogController.java). */
export type VerifyStatus = 'verified' | 'needs_review';

export interface CatalogItem {
  id: string;
  code: string;
  level: 'thcs' | 'thpt';
  solidKind: string;
  topic: string;
  topicSlug: string;
  versionId: string;
  statement: string;
  verifyStatus: VerifyStatus;
  label: string;
  aiAuthored: boolean;
  /** Rỗng khi đề chưa có lời giải. */
  stepCount: number | null;
}

/** Một trang: không có tổng số đề, không có số trang (BR-GEO-215). */
export interface CatalogPage {
  items: CatalogItem[];
  hasMore: boolean;
  nextCursor: string | null;
}

export interface CatalogSummary {
  total: number;
  verified: number;
  author: string;
  mvpMet: boolean;
  mvp: string;
}

export interface SolutionStep {
  title: string;
  body: string;
  why: string;
}

export interface ProblemDetail {
  versionId: string;
  code: string;
  level: 'thcs' | 'thpt';
  solidKind: string;
  topic: string;
  topicSlug: string;
  statement: string;
  verifyStatus: VerifyStatus;
  label: string;
  aiAuthored: boolean;
  author: string;
  hints: string[];
  solution: { finalAnswer: string; steps: SolutionStep[]; aiAuthored: boolean; author: string } | null;
  /** Đáp số khi đề "Đã kiểm chứng" (chip "Đáp số khớp tính toán", BR-GEO-209). */
  answerMatch: string | null;
  scene: Scene | null;
}

/** Bộ lọc: rỗng = "Tất cả". */
export interface CatalogFilter {
  level: string;
  solid: string;
  topic: string;
}

export const e5Api = {
  list: (f: CatalogFilter, cursor?: string | null) =>
    api.get<CatalogPage>('/api/v1/catalog/problems', {
      query: {
        ...(f.level ? { level: f.level } : {}),
        ...(f.solid ? { solid: f.solid } : {}),
        ...(f.topic ? { topic: f.topic } : {}),
        ...(cursor ? { cursor } : {}),
      },
    }),
  summary: () => api.get<CatalogSummary>('/api/v1/catalog/summary'),
  detail: (versionId: string) =>
    api.get<ProblemDetail>(`/api/v1/teacher/catalog/problems/${encodeURIComponent(versionId)}`),
};
