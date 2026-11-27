import { expect, test } from '../../support/fixtures.ts';
import { API, KT15, MSG, bearer, createSet, importBank, lanToken } from './s5e2.ts';

/** US-S5E2-02 — form ra đề: xem trước bảng ô, kiểm tổng điểm, thời gian, số mã đề. */

test.beforeAll(async ({ request }) => {
  await importBank(request);
});

type Err = { code: string; message: string; details: Array<{ field: string; message: string }> };

test('Bỏ một chương thì ô chuyển sang chương còn lại @TC-S5E2-06', async ({ request }) => {
  const h = bearer(await lanToken(request));
  const full = await request.post(`${API}/api/exams/preview`, { headers: h, data: { matrixCode: 'GK2-L11' } });
  expect(full.status()).toBe(200);
  type P = { totalPoints: number; parts: Array<{ id: string; count: number }>; cells: Array<{ type: string; chapters: string[] }> };
  const before = (await full.json()) as P;
  expect(new Set(before.cells.flatMap((c) => c.chapters))).toContain('ham-so-mu-logarit');
  const r = await request.post(`${API}/api/exams/preview`, { headers: h, data: { matrixCode: 'GK2-L11', chapters: ['quan-he-vuong-goc'] } });
  expect(r.status()).toBe(200);
  const after = (await r.json()) as P;
  expect(new Set(after.cells.flatMap((c) => c.chapters))).toEqual(new Set(['quan-he-vuong-goc']));
  const byType = (p: P) => p.cells.reduce<Record<string, number>>((m, c) => ({ ...m, [c.type]: (m[c.type] ?? 0) + 1 }), {});
  expect(byType(after)).toEqual(byType(before));
  expect(after.parts).toEqual(before.parts);
  expect(after.totalPoints).toBe(10);
});

test('Tổng điểm khác 10 bị từ chối @TC-S5E2-07', async ({ request }) => {
  const r = await request.post(`${API}/api/exams/preview`, {
    headers: bearer(await lanToken(request)),
    data: { matrixCode: KT15, parts: [{ id: 'P2', points: 1.5 }] },
  });
  expect(r.status()).toBe(400);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.message).toBe(MSG.s10);
  expect(e.details[0]!.field).toBe('parts');
});

test('Thời gian dự kiến vượt thời gian làm bài bị từ chối @TC-S5E2-08', async ({ request }) => {
  const r = await request.post(`${API}/api/exams/preview`, { headers: bearer(await lanToken(request)), data: { matrixCode: KT15, minutes: 8 } });
  expect(r.status()).toBe(400);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.message).toBe(MSG.s11);
  expect(e.details[0]!.field).toBe('minutes');
});

test('Số mã đề 9 bị từ chối @TC-S5E2-09', async ({ request }) => {
  const token = await lanToken(request);
  const r = await createSet(request, token, { matrixCode: KT15, variantCount: 9, seed: 2026 });
  expect(r.status()).toBe(400);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.message).toBe(MSG.s13);
  expect(e.details[0]!.field).toBe('variantCount');
});
