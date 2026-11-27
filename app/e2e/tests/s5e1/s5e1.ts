import type { APIRequestContext } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, hook, type Person } from '../s3e1/s3e1.ts';

/**
 * Tiện ích E2E S5-E1 (T-261128-53). Mỗi ca dựng giáo viên riêng qua móc thế giới S3-E1 (chạy song song an toàn);
 * nhà cung cấp AI giả lập: kịch bản lesson_plan theo người dùng (F-S5-093 mặc định; 'unsure', 'question', …).
 */
export const LP = `${API}/api/v1/teacher/lesson-plans`;

/** F-S5-020: Bài 26 · Tiết 1/2 · Khá · Truyền thống · 2 kiến thức trọng tâm · Phiếu học tập Bật. */
export const F020 = {
  tietPpct: 'B26-T1',
  doiTuong: 'kha',
  phongCach: 'truyen-thong',
  kienThuc: ['KT11-C07-DN-010', 'KT11-C07-CT-003'],
  soTiet: null,
  phieuHocTap: true,
};

export async function aiScenario(rq: APIRequestContext, p: Person, scenario: string) {
  await hook(rq, 'ai', { userId: p.id, scenario });
}

export async function createPlan(rq: APIRequestContext, p: Person, body: Record<string, unknown> = F020) {
  const r = await rq.post(LP, { headers: bearer(p.token), data: body });
  expect(r.status(), await r.text()).toBe(202);
  return (await r.json()) as { draftId: string; code: string; status: string; statusLabel: string; humanLabel: string };
}

/** Phản hồi GET giáo án (phần dùng trong test). */
export interface PlanJson {
  status: string;
  statusLabel: string;
  humanLabel: string;
  title: string;
  kind?: string;
  message?: string;
  lesson?: {
    sections: string[];
    tongPhut: number;
    hoatDong: Array<{ ma: string; nhan: string; loai: string; thoiLuongPhut: number; chuaSoat: string[]; unsure: string[] }>;
    phieuHocTap: Array<{ ma: string; cau: Array<{ nguon: string; dapSo: string | null; dapSoNguon: string }> }>;
  };
}

/** Móc kiểm thử S5-E1: tin nhắn đã gửi cổng AI + số liệu nội bộ của giáo án. */
export async function internals(rq: APIRequestContext, orgId: string, id: string) {
  const r = await rq.get(`${API}/test-hooks/s5e1/plans/${id}?orgId=${orgId}`);
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as { messages: string[]; fixKeys: number; aiCalls: number; content: string | null };
}

/** Chờ giáo án rời "Đang soạn" (AI chạy nền). */
export async function awaitPlan(rq: APIRequestContext, p: Person, id: string): Promise<PlanJson> {
  let v = {} as PlanJson;
  await expect
    .poll(
      async () => {
        const r = await rq.get(`${LP}/${id}`, { headers: bearer(p.token) });
        v = (await r.json()) as PlanJson;
        return v.status;
      },
      { timeout: 15_000 },
    )
    .not.toBe('drafting');
  return v;
}
