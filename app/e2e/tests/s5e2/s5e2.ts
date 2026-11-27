import { execFileSync } from 'node:child_process';
import { existsSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';
import type { APIRequestContext, APIResponse } from '@playwright/test';
import { expect } from '../../support/fixtures.ts';
import { API, bearer, devToken, ids } from '../e1/e1.ts';

export { API, bearer, devToken };

/**
 * Tiện ích E2E của S5-E2 (T-261128-52). Dữ liệu đề (materials/de-thi, F-S5-032) nạp bằng móc /test-hooks/s5e2/import
 * (gọi lệnh nạp thật, lũy đẳng); ca lỗi nạp chạy trong giao dịch hoàn tác nên không đổi ngân hàng dùng chung.
 * Giáo viên: cô Lan F-S5-001 (chủ 11A1); lớp 10B của thầy Khôi (F-S3-012).
 */
export const LAN = 'F-S5-001';
export const LAN_ID = (ids.users as Record<string, { id: string }>)[LAN]!.id;
export const ORG_ID = (ids.orgs as Record<string, { id: string }>)['F-S2-100']!.id;
export const CLASS_11A1 = (ids.classes as Record<string, { id: string }>)['F-S5-010']!.id;
export const CLASS_10B = (ids.classes as Record<string, { id: string }>)['F-S3-012']!.id;
export const KT15 = 'KT15-HHKG11';
export const TN764 = 'TN-THPT-764';

export const MSG = {
  s10: 'Tổng điểm phải bằng 10.',
  s11: 'Tổng thời gian dự kiến của các câu vượt thời gian làm bài.',
  s12: (o: string) => `Ngân hàng chưa đủ câu cho ô ${o}. Thầy cô bỏ bớt ràng buộc hoặc giảm số mã đề.`,
  s13: 'Số mã đề từ 1 đến 8.',
} as const;

export interface Report {
  version: string;
  matrices: number;
  templates: number;
  items: number;
  errors: number;
  added: number;
  changed: number;
  issues: Array<{ kind: string; id: string; message: string }>;
  stored: { matrices: number; templates: number; items: number };
  summary: string;
}

export async function importBank(request: APIRequestContext, mode = 'commit'): Promise<Record<string, unknown> & { report: Report }> {
  const r = await request.post(`${API}/test-hooks/s5e2/import`, { data: { mode }, timeout: 60_000 });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Record<string, unknown> & { report: Report };
}

export async function lanToken(request: APIRequestContext): Promise<string> {
  return devToken(request, LAN);
}

export interface Question {
  so: number;
  phan: string;
  o: string;
  id_cau: string;
  chu_de: string;
  dang_tx: string;
  muc_do: string;
  loai_cau: string;
  diem: number;
  dap_an: unknown;
  phuong_an?: Record<string, string>;
  nhan_may: string;
}
export interface Variant {
  code: string;
  difficulty: number;
  cau: Question[];
  rang_buoc: Record<string, { dat: boolean; chi_tiet: unknown }>;
}
export interface ExamSet {
  id: string;
  status: string;
  humanLabel: string;
  approvedBy: string | null;
  approvedAt: string | null;
  difficultySpread: number;
  variants: Variant[];
}

export async function createSet(request: APIRequestContext, token: string, body: Record<string, unknown>): Promise<APIResponse> {
  return request.post(`${API}/api/exams`, { headers: bearer(token), data: body, timeout: 60_000 });
}

export async function mySets(request: APIRequestContext, token: string, shelf?: string): Promise<Array<{ id: string; matrixCode: string; classId: string | null }>> {
  const r = await request.get(`${API}/api/exams${shelf ? `?shelf=${shelf}` : ''}`, { headers: bearer(token) });
  expect(r.status()).toBe(200);
  return ((await r.json()) as { items: Array<{ id: string; matrixCode: string; classId: string | null }> }).items;
}

/** Độ lệch độ khó các mã = (max − min) / trung bình (lech_do_kho của lap_de.py). */
export function spread(ds: number[]): number {
  const avg = ds.reduce((a, b) => a + b, 0) / ds.length;
  return (Math.max(...ds) - Math.min(...ds)) / avg;
}

/** Thư mục dữ liệu đề trong repo (artifacts/GEO/materials/de-thi). */
export function deThiDir(): string {
  for (let d = path.resolve(import.meta.dirname); d !== path.dirname(d); d = path.dirname(d)) {
    const p = path.join(d, 'artifacts', 'GEO', 'materials', 'de-thi');
    if (existsSync(p)) return p;
  }
  throw new Error('không thấy artifacts/GEO/materials/de-thi');
}

/**
 * Bộ kiểm đối chứng `check.py` (Kiểm 6, kiem_doc_lap — độc lập với bộ lắp đề) chạy trên các mã đề máy chủ trả về,
 * kèm schema đề và độ khó các mã lệch ≤ 5 %. Trả stdout; ném lỗi khi python thoát khác 0.
 */
export function checkPy(variants: Array<Variant & { seed?: number }>, matrix: string, seed: number): string {
  const dir = mkdtempSync(path.join(tmpdir(), 's5e2-'));
  const files = variants.map((v) => {
    const f = path.join(dir, `${matrix}-${seed}-${v.code}.json`);
    writeFileSync(f, JSON.stringify({ ...v, ma_tran: matrix, ma_de: v.code, seed, do_kho_du_kien: v.difficulty, thoi_gian_phut: 0, tong_diem: 10 }));
    return f;
  });
  return execFileSync('python3', ['-I', path.join(import.meta.dirname, 'doi_chung.py'), deThiDir(), ...files], { encoding: 'utf8' });
}
