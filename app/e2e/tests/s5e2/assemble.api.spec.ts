import { expect, test } from '../../support/fixtures.ts';
import {
  API,
  CLASS_10B,
  CLASS_11A1,
  KT15,
  LAN_ID,
  MSG,
  TN764,
  checkPy,
  createSet,
  importBank,
  lanToken,
  mySets,
  spread,
  type ExamSet,
} from './s5e2.ts';

/** US-S5E2-03 — lắp nhiều mã đề tương đương (bộ lắp Java theo lap_de.py), tái lập theo seed, đối chứng check.py. */

test.beforeAll(async ({ request }) => {
  await importBank(request);
});

const aiCalls = async (request: import('@playwright/test').APIRequestContext) =>
  ((await (await request.get(`${API}/test-hooks/ai/calls?userId=${LAN_ID}&limit=100`)).json()) as unknown[]).length;

test('Lắp 4 mã đề 15 phút đạt mọi ràng buộc @TC-S5E2-10', async ({ request }) => {
  const token = await lanToken(request);
  const ai0 = await aiCalls(request);
  const r = await createSet(request, token, { matrixCode: KT15, classId: CLASS_11A1, variantCount: 4, seed: 2026 });
  expect(r.status(), await r.text()).toBe(201);
  const set = (await r.json()) as ExamSet;
  expect(set.variants.map((v) => v.code)).toEqual(['101', '102', '103', '104']);
  for (const v of set.variants) {
    expect(v.cau).toHaveLength(8);
    expect(v.cau.reduce((s, q) => s + q.diem, 0)).toBeCloseTo(10, 9);
    expect(Object.values(v.rang_buoc).every((c) => c.dat)).toBe(true);
  }
  expect(checkPy(set.variants, KT15, 2026)).toContain('ĐẠT 4 mã');
  expect(await aiCalls(request)).toBe(ai0);
});

test('Cùng seed cho ra cùng bộ đề @TC-S5E2-11', async ({ request }) => {
  const token = await lanToken(request);
  const body = { matrixCode: KT15, variantCount: 4, seed: 2026 };
  const a = (await (await createSet(request, token, body)).json()) as ExamSet;
  const b = (await (await createSet(request, token, body)).json()) as ExamSet;
  expect(b.id).not.toBe(a.id);
  const shape = (s: ExamSet) => s.variants.map((v) => v.cau.map((q) => [q.id_cau, q.dap_an, q.phuong_an ?? null]));
  expect(shape(b)).toEqual(shape(a));
});

test('Đề TN THPT đúng cấu trúc QĐ 764 @TC-S5E2-12', async ({ request }) => {
  const r = await createSet(request, await lanToken(request), { matrixCode: TN764, variantCount: 2, seed: 2026 });
  expect(r.status(), await r.text()).toBe(201);
  const set = (await r.json()) as ExamSet;
  expect(set.variants).toHaveLength(2);
  for (const v of set.variants) {
    const n = (t: string) => v.cau.filter((q) => q.loai_cau === t).length;
    expect([n('tn4'), n('dung-sai'), n('tra-loi-ngan')]).toEqual([12, 4, 6]);
    for (const q of v.cau.filter((x) => x.loai_cau === 'tra-loi-ngan')) expect(String(q.dap_an).length).toBeLessThanOrEqual(4);
    const abcd = ['A', 'B', 'C', 'D'].map((L) => v.cau.filter((q) => q.loai_cau === 'tn4' && q.dap_an === L).length);
    expect(Math.max(...abcd) - Math.min(...abcd)).toBeLessThanOrEqual(1);
  }
  expect(spread(set.variants.map((v) => v.difficulty))).toBeLessThanOrEqual(0.05);
  expect(checkPy(set.variants, TN764, 2026)).toContain('ĐẠT 2 mã');
});

test('Lắp đủ 8 mã trong ≤ 5 giây, độ khó lệch ≤ 5 % @TC-S5E2-13', async ({ request }) => {
  const token = await lanToken(request);
  const t0 = Date.now();
  const r = await createSet(request, token, { matrixCode: KT15, variantCount: 8, seed: 2026 });
  const ms = Date.now() - t0;
  expect(r.status(), await r.text()).toBe(201);
  expect(ms).toBeLessThanOrEqual(5000);
  const set = (await r.json()) as ExamSet;
  expect(set.variants.map((v) => v.code)).toEqual(['101', '102', '103', '104', '105', '106', '107', '108']);
  expect(spread(set.variants.map((v) => v.difficulty))).toBeLessThanOrEqual(0.05);
  expect(set.difficultySpread).toBeLessThanOrEqual(0.05);
  expect(checkPy(set.variants, KT15, 2026)).toContain('ĐẠT 8 mã');
});

test('Ngân hàng không đủ câu thì báo ô thiếu, không trả đề thiếu @TC-S5E2-14', async ({ request }) => {
  const token = await lanToken(request);
  const ck2 = async () => (await mySets(request, token)).filter((x) => x.matrixCode === 'CK2-L11').length;
  const before = await ck2();
  const r = await createSet(request, token, { matrixCode: 'CK2-L11', chapters: ['luong-giac'], variantCount: 8, seed: 2026 });
  expect(r.status()).toBe(409);
  const e = (await r.json()) as { code: string; message: string };
  expect(e.code).toBe('CONFLICT');
  const cell = /cho ô (\S+)\./.exec(e.message)?.[1] ?? '';
  expect(cell).not.toBe('');
  expect(e.message).toBe(MSG.s12(cell));
  expect(await ck2()).toBe(before);
});

test('Giáo viên không lắp đề cho lớp không phải của mình @TC-S5E2-15', async ({ request }) => {
  const token = await lanToken(request);
  const r = await createSet(request, token, { matrixCode: KT15, classId: CLASS_10B, variantCount: 4, seed: 2026 });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  expect((await mySets(request, token)).filter((x) => x.classId === CLASS_10B)).toHaveLength(0);
});
