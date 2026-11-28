import type { APIRequestContext, APIResponse, Page, TestInfo } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, devToken, signInPage } from '../e1/e1.ts';

/**
 * Tiện ích E2E của S2-E5 (soát và duyệt lời giải, khung C2-03). Dữ liệu: app/db/seed/e2e/820_s2e5_core.sql +
 * fixture-ids.d/s2e5.json. Mỗi ca làm bẩn dữ liệu nên có sổ lời giải riêng theo (mã TC, làn = dự án Playwright).
 * Chủ sổ: Cô Trang FX-01 (F-S2-001, GV-7K2M-9Q4D); TC 36 và 65: Cô Trang F-S2-820003 của "Nhóm thử S2-E5".
 */
export { API, bearer };

export const TRANG = 'F-S2-001';
export const TRANG_CODE = 'GV-7K2M-9Q4D';
export const LAN = 'F-S2-820002';
export const AN = 'F-S2-002';
export const TRANG_S5 = 'F-S2-820003';
export const AN_S5 = 'F-S2-820020';
export const ASSIGNED_BG = '00000000-0000-7000-8002-000000823600';

const LANES = ['api', 'ui-chromium', 'ui-webkit', 'ui-iphone', 'mobile-iphone', 'mobile-pixel'];
const f = (n: number) => `00000000-0000-7000-8002-${String(n).padStart(12, '0')}`;
const lane = (info: TestInfo) => Math.max(0, LANES.indexOf(info.project.name));

/** Sổ lời giải riêng của ca (TC, dự án). */
export const docId = (tc: number, info: TestInfo) => f(820000000 + tc * 100 + lane(info));
/** Lớp 11A1 riêng của làn (TC 65: mỗi làn giao đề một lần). */
export const classId = (info: TestInfo) => f(820010 + lane(info));
/** Version đề kho SP-01 riêng của làn (một giáo viên chỉ có một sổ lời giải cho một version đề). */
export const sp01Version = (info: TestInfo) => f(820101 + 10 * lane(info));
/** Sổ chỉ đọc đã giao (TC 36): v2 đã duyệt + ghim vào bài giao, v3 mới hơn chưa áp dụng. */
export const ASSIGNED_DOC = f(820003600);

export const S1 = 'SA ⊥ (ABCD) nên AC là hình chiếu của SC, góc cần tìm là SCA.';
export const S2 = 'ABCD vuông cạnh a nên AC = a√2.';
export const S3_V1 = 'tan SCA = SA / AC = √2.';
export const S4_V1 = 'Vậy góc giữa SC và mặt phẳng đáy bằng 54,7°.';
export const S3_RIGHT = 'tan SCA = a√2 / a√2 = 1';
export const S4_RIGHT = 'Vậy góc giữa SC và mặt phẳng đáy bằng 45°.';
/** v2 vẫn sai ở bước 3 (US-S2E5-02-SC1). */
export const WRONG_V2 = [S1, S2, 'tan SCA = √2.', S4_V1];
export const RIGHT = [S1, S2, S3_RIGHT, S4_RIGHT];

export const MSG = {
  machineNote: 'Máy đã kiểm tra phần tính toán',
  verified: 'Đã kiểm chứng',
  needsReview: 'Cần xem lại',
  ai: 'AI soạn',
  editing: 'Đang sửa',
  approved: 'Giáo viên đã duyệt',
  superseded: 'Bị thay thế',
  confirm: 'Nhãn máy vẫn Cần xem lại. Vẫn duyệt?',
  emptyBox: 'Không có gì chờ duyệt',
};

export interface Doc {
  latestVersion: number;
  inMyShelf: boolean;
  saved: boolean;
  version: {
    version: number;
    machineLabel: string;
    machineNote?: string | null;
    humanLabel: string;
    failCount: number;
    approvedCode?: string | null;
    approvedAt?: string | null;
    steps: Array<{ no: number; text: string; check: string; checkLabel: string; reason?: string | null }>;
  };
  approve: { allowed: boolean; approved: boolean; needsConfirm: boolean; blockedReason?: string | null };
  history: Array<{ kind: string; version?: number | null; description: string }>;
  proposals: Array<{ id: string; afterStep: number; content: string }>;
  assignments: Array<{ className: string; version: number }>;
}

export const token = (request: APIRequestContext, ref: string) => devToken(request, ref);
const url = (doc: string, tail = '') => `${API}/api/v1/teacher/solutions/${doc}${tail}`;

async function ok(r: APIResponse): Promise<Doc> {
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Doc;
}

export const rawSave = (request: APIRequestContext, t: string, doc: string, base: number, steps: string[], extra: Record<string, unknown> = {}) =>
  request.post(url(doc, '/versions'), { headers: bearer(t), data: { baseVersion: base, steps, ...extra } });
export const rawApprove = (request: APIRequestContext, t: string, doc: string, version: number, confirm = true, extra: Record<string, unknown> = {}) =>
  request.post(url(doc, '/approve'), { headers: bearer(t), data: { version, confirmNeedsReview: confirm, ...extra } });
export const rawView = (request: APIRequestContext, t: string, doc: string, version?: number) =>
  request.get(url(doc, version ? `?version=${version}` : ''), { headers: bearer(t) });

export const apiSave = async (request: APIRequestContext, t: string, doc: string, base: number, steps: string[]) => ok(await rawSave(request, t, doc, base, steps));
export const apiCheck = async (request: APIRequestContext, t: string, doc: string, version: number) =>
  ok(await request.post(url(doc, '/check'), { headers: bearer(t), data: { version } }));
export const apiApprove = async (request: APIRequestContext, t: string, doc: string, version: number, confirm = true) => ok(await rawApprove(request, t, doc, version, confirm));
export const apiView = async (request: APIRequestContext, t: string, doc: string, version?: number) => ok(await rawView(request, t, doc, version));

/** Đăng nhập giáo viên bằng thẻ dev rồi mở C2-03 của một sổ. */
export async function openReview(page: Page, request: APIRequestContext, doc: string, teacher = TRANG) {
  await signInPage(page, request, teacher);
  await page.goto(`/gv/soat-loi-giai/${doc}`);
  await expect(page.getByTestId('s2e5-page')).toBeVisible();
}

export const pageEl = (page: Page) => page.getByTestId('s2e5-page');
/** Chờ màn chuyển sang phiên bản n (sau Lưu, Nhận, Dùng bản này, hoặc bấm lịch sử). */
export const expectVersion = (page: Page, n: number) => expect(pageEl(page)).toHaveAttribute('data-version', String(n));
export const stepText = (page: Page, no: number) => page.getByTestId(`s2e5-step-text-${no}`);
export const stepCheck = (page: Page, no: number) => page.getByTestId(`s2e5-step-check-${no}`);
export const stepCount = (page: Page) => page.locator('[data-testid^="s2e5-step-text-"]');

/** Bấm một nút gọi API ghi và chờ phản hồi của đúng lệnh đó. */
export async function clickAndWait(page: Page, testId: string, pathTail: string) {
  const resp = page.waitForResponse((r) => r.url().includes('/api/v1/teacher/solutions/') && r.url().endsWith(pathTail) && r.request().method() === 'POST');
  await page.getByTestId(testId).click();
  return resp;
}
