import { expect, test } from '../../support/fixtures.ts';
import { API, KT15, LAN_ID, MSG, ORG_ID, bearer, createSet, importBank, lanToken, mySets, type ExamSet } from './s5e2.ts';

/** US-S5E2-04 — bộ đề nháp: duyệt vào "Của tôi", thay câu khi ô hết câu, bộ đề chưa duyệt không giao được. */

test.beforeAll(async ({ request }) => {
  await importBank(request);
});

async function draft(request: import('@playwright/test').APIRequestContext, token: string): Promise<ExamSet> {
  const r = await createSet(request, token, { matrixCode: KT15, variantCount: 4, seed: 2026 });
  expect(r.status(), await r.text()).toBe(201);
  return (await r.json()) as ExamSet;
}

test('Duyệt bộ đề vào "Của tôi" @TC-S5E2-17', async ({ request }) => {
  const token = await lanToken(request);
  const set = await draft(request, token);
  expect(set.humanLabel).toBe('Máy lắp');
  expect((await mySets(request, token, 'mine')).map((s) => s.id)).not.toContain(set.id);
  const r = await request.post(`${API}/api/exams/${set.id}/approve`, { headers: bearer(token) });
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const ok = (await r.json()) as ExamSet;
  expect(ok.status).toBe('approved');
  expect(ok.humanLabel).toBe('Giáo viên đã duyệt');
  expect(ok.approvedBy).toBe(LAN_ID);
  expect(Date.now() - Date.parse(ok.approvedAt!)).toBeLessThan(5 * 60_000);
  expect((await mySets(request, token, 'mine')).map((s) => s.id)).toContain(set.id);
});

test('Hết lượt bốc thì giữ câu cũ và báo @TC-S5E2-18', async ({ request }) => {
  const token = await lanToken(request);
  const set = await draft(request, token);
  const q3 = set.variants[0]!.cau[2]!;
  // Móc: ô của câu 3 chỉ còn đúng câu đang dùng trong ngân hàng (bộ lọc ngân hàng), chạy đúng đường thay câu của máy chủ.
  const r = await request.post(`${API}/test-hooks/s5e2/replace-last-item`, {
    data: { orgId: ORG_ID, teacherId: LAN_ID, setId: set.id, code: '101', so: 3 },
  });
  expect(r.status()).toBe(409);
  const e = (await r.json()) as { code: string; message: string };
  expect(e.code).toBe('CONFLICT');
  expect(e.message).toBe(MSG.s12(q3.o));
  const after = (await (await request.get(`${API}/api/exams/${set.id}`, { headers: bearer(token) })).json()) as ExamSet;
  expect(after.variants[0]!.cau[2]!.id_cau).toBe(q3.id_cau);
});

test('Bộ đề chưa duyệt không giao được @TC-S5E2-19', async ({ request }) => {
  const token = await lanToken(request);
  const set = await draft(request, token);
  // Cổng tạo bài có chấm (US-S5E4-02, module assessment của T-57) gọi exam.api.ExamSetGuard; móc giả lập lời gọi đó.
  const r = await request.post(`${API}/test-hooks/s5e2/graded-assignment`, { data: { orgId: ORG_ID, teacherId: LAN_ID, setId: set.id } });
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { code: string }).code).toBe('CONFLICT');
  await request.post(`${API}/api/exams/${set.id}/approve`, { headers: bearer(token) });
  const ok = await request.post(`${API}/test-hooks/s5e2/graded-assignment`, { data: { orgId: ORG_ID, teacherId: LAN_ID, setId: set.id } });
  expect(ok.status()).toBe(200);
  expect(((await ok.json()) as { allowed: boolean }).allowed).toBe(true);
});
