import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, importBank, lanToken } from './s5e2.ts';

/** US-S5E2-01 — lệnh nạp dữ liệu đề có kiểm schema + đáp số engine (móc /test-hooks/s5e2/import gọi đúng lệnh nạp). */

test('Nạp đủ dữ liệu đề v1.0 @TC-S5E2-01', async ({ request }) => {
  // CSDL thử chưa có dữ liệu đề: móc xóa dữ liệu đề rồi nạp trong một giao dịch (hoàn tác sau khi đọc báo cáo).
  const { report } = await importBank(request, 'fresh');
  expect(report.version).toBe('de-thi v1.0');
  expect([report.matrices, report.templates, report.items, report.errors]).toEqual([4, 110, 1774, 0]);
  expect(report.stored).toEqual({ matrices: 4, templates: 110, items: 1774 });
  expect(report.summary).toContain('4 ma trận, 110 mẫu, 1774 câu, 0 lỗi');
  await importBank(request);
  const cat = await request.get(`${API}/api/exam/catalog`, { headers: bearer(await lanToken(request)) });
  expect(cat.status()).toBe(200);
  const codes = ((await cat.json()) as { items: Array<{ code: string }> }).items.map((m) => m.code).sort();
  expect(codes).toEqual(['CK2-L11', 'GK2-L11', 'KT15-HHKG11', 'TN-THPT-764']);
});

test('Chạy lại cùng phiên bản là lũy đẳng @TC-S5E2-02', async ({ request }) => {
  await importBank(request);
  const { report } = await importBank(request);
  expect(report.stored).toEqual({ matrices: 4, templates: 110, items: 1774 });
  expect([report.added, report.changed]).toEqual([0, 0]);
  expect(report.summary).toContain('0 thêm, 0 đổi');
});

test('Câu lệch đáp số engine bị loại, phần còn lại vẫn nạp @TC-S5E2-03', async ({ request }) => {
  const out = await importBank(request, 'bad-answer');
  const bad = out.badItem as string;
  const { report } = out;
  expect(report.errors).toBe(1);
  expect(report.issues[0]!.id).toBe(bad);
  expect(report.issues[0]!.message).toMatch(/0\.82 ≠ engine 0\.8164965/);
  expect(report.items).toBe(1773);
  expect(report.stored.items).toBe(1773);
  expect(out[`itemStored:${bad}`]).toBe(false);
});

test('Ma trận tổng điểm khác 10 bị từ chối @TC-S5E2-04', async ({ request }) => {
  const { report } = await importBank(request, 'bad-matrix');
  expect(report.issues).toEqual([{ kind: 'ma_tran', id: 'KT15-HHKG11', message: 'tổng điểm 9 ≠ 10' }]);
  expect(report.matrices).toBe(3);
  expect(report.stored.matrices).toBe(3);
});
