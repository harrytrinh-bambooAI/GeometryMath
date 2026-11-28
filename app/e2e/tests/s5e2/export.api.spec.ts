import { expect, test } from '../../support/fixtures.ts';
import { download, inspect, type Inspected } from '../s5e1/patch.ts';
import { API, CLASS_11A1, KT15, bearer, createSet, devToken, importBank, lanToken, type ExamSet, type Variant } from './s5e2.ts';

/**
 * US-S5E2-05 — xuất mã đề (T-261128-54): đề, đáp án, hướng dẫn chấm, bảng đặc tả; DOCX/PDF dùng chung bộ dựng
 * common/export của giáo án. Tệp tải về được đọc lại bằng móc /test-hooks/export/inspect (POI / pdfbox).
 */

test.beforeAll(async ({ request }) => {
  await importBank(request);
});

async function makeSet(request: import('@playwright/test').APIRequestContext, token: string, approve: boolean): Promise<ExamSet> {
  const r = await createSet(request, token, { matrixCode: KT15, classId: CLASS_11A1, variantCount: 4, seed: 2026 });
  expect(r.status(), await r.text()).toBe(201);
  const set = (await r.json()) as ExamSet;
  if (approve) {
    const a = await request.post(`${API}/api/exams/${set.id}/approve`, { headers: bearer(token) });
    expect(a.status()).toBe(200);
  }
  return set;
}

const exportUrl = (id: string, code: string, q: string) => `${API}/api/exams/${id}/codes/${code}/export?${q}`;

/** Đáp án hiện ra trong phần Đáp án của từng câu (cột 3 của bảng). */
function answers(dapAn: Inspected): Map<number, string> {
  return new Map(dapAn.tables![0]!.slice(1).map((r) => [Number(r[0]), r[2]!]));
}

test('Xuất 4 phần của mã 101 @TC-S5E2-20', async ({ request }) => {
  const token = await lanToken(request);
  const set = await makeSet(request, token, true);
  const f = await download(request, exportUrl(set.id, '101', 'format=docx&parts=all'), token);
  expect(f.res.headers()['content-type']).toContain('application/zip');
  expect(f.res.headers()['cache-control']).toContain('no-store');
  const z = await inspect(request, f.body, f.name);
  expect(z.kind).toBe('zip');
  const names = z.entries!.map((e) => e.name);
  expect(names).toEqual([`${KT15}_101_de.docx`, `${KT15}_101_dap-an.docx`, `${KT15}_101_huong-dan-cham.docx`, `${KT15}_101_dac-ta.docx`]);
  for (const e of z.entries!) expect(e.kind).toBe('docx');
  const [de, dapAn, hdc, dacTa] = z.entries!;
  const v = set.variants.find((x) => x.code === '101') as Variant & { cau: Array<Variant['cau'][number] & { de_bai: string; o: string }> };
  // đề: đủ câu, không có phần đáp án, không chứa đáp án của câu nào (trừ khi chính đề bài có chuỗi đó)
  expect(de!.text).not.toContain('Đáp án');
  for (const q of v.cau) expect(de!.text).toContain(`Câu ${q.so} (`);
  const ans = answers(dapAn!);
  expect(ans.size).toBe(v.cau.length);
  // khối chữ của từng câu trong tệp đề (từ "Câu n (" tới câu kế)
  const block = (so: number) => {
    const t = de!.text!;
    const a = t.indexOf(`Câu ${so} (`);
    const b = t.indexOf(`Câu ${so + 1} (`, a + 1);
    return t.slice(a, b < 0 ? undefined : b);
  };
  for (const q of v.cau) {
    const a = ans.get(q.so)!;
    expect(a.length).toBeGreaterThan(0);
    if (q.loai_cau === 'tn4') {
      expect(a).toBe(String(q.dap_an));
    } else if (!q.de_bai.includes(a) && !Object.values(q.phuong_an ?? {}).some((o) => o.includes(a))) {
      expect(block(q.so), `đáp án câu ${q.so}`).not.toContain(a);
    }
  }
  // hướng dẫn chấm cộng đủ 10
  const rows = hdc!.tables![0]!.slice(1);
  const total = rows.find((r) => r[0] === 'Tổng')!;
  const sum = rows.filter((r) => r[0] !== 'Tổng').reduce((s, r) => s + Number(r[2]), 0);
  expect(Math.round(sum * 1e4) / 1e4).toBe(10);
  expect(Number(total[2])).toBe(10);
  // bảng đặc tả: ô, YCCĐ, mức độ, điểm của từng câu
  const spec = dacTa!.tables![0]!;
  expect(spec[0]).toEqual(['Câu', 'Ô ma trận', 'Chủ đề', 'Dạng', 'YCCĐ', 'Mức độ', 'Loại câu', 'Điểm']);
  expect(spec.slice(1).map((r) => [r[1], r[5]])).toEqual(v.cau.map((q) => [q.o, q.muc_do]));
  // bộ đã duyệt: không chân trang nháp
  for (const e of z.entries!) expect(e.footers!.join(' ')).not.toContain('Bản nháp');
});

test('Xuất PDF chỉ phần đề cho cả 4 mã @TC-S5E2-21', async ({ request }) => {
  const token = await lanToken(request);
  const set = await makeSet(request, token, true);
  expect(set.variants.map((v) => v.code)).toEqual(['101', '102', '103', '104']);
  for (const code of ['101', '102', '103', '104']) {
    const f = await download(request, exportUrl(set.id, code, 'format=pdf&parts=de'), token);
    expect(f.name).toBe(`${KT15}_${code}_de.pdf`);
    expect(f.res.headers()['content-type']).toContain('application/pdf');
    const d = await inspect(request, f.body, f.name);
    expect(d.kind).toBe('pdf');
    for (const page of d.pages!) {
      expect(page).toContain(`Mã đề ${code}`);
      expect(page).not.toContain('ĐÁP ÁN');
      expect(page).not.toContain('HƯỚNG DẪN CHẤM');
    }
    expect(d.pages![0]).toContain('ĐỀ KIỂM TRA');
  }
});

test('Học sinh không tải được đáp án @TC-S5E2-22', async ({ request }) => {
  const token = await lanToken(request);
  const set = await makeSet(request, token, true);
  const binhAn = await devToken(request, 'F-S3-020');
  for (const parts of ['dap-an', 'de', 'all']) {
    const r = await request.get(exportUrl(set.id, '101', `format=docx&parts=${parts}`), { headers: bearer(binhAn) });
    expect(r.status()).toBe(403);
    const text = await r.text();
    expect((JSON.parse(text) as { code: string }).code).toBe('FORBIDDEN');
    const v = set.variants[0]!;
    for (const q of v.cau as Array<{ de_bai?: string }>) if (q.de_bai) expect(text).not.toContain(q.de_bai.slice(0, 30));
    expect(text).not.toContain('Đáp án');
  }
  // giáo viên khác: NOT_FOUND
  const khoi = await devToken(request, 'F-S5-002');
  expect((await request.get(exportUrl(set.id, '101', 'parts=de'), { headers: bearer(khoi) })).status()).toBe(404);
});

test('Bộ đề nháp xuất được nhưng có chân trang nháp @TC-S5E2-23', async ({ request }) => {
  const token = await lanToken(request);
  const set = await makeSet(request, token, false);
  const docx = await inspect(request, (await download(request, exportUrl(set.id, '101', 'format=docx&parts=de'), token)).body);
  expect(docx.kind).toBe('docx');
  expect(docx.footers!.join(' ')).toContain('Bản nháp – chưa duyệt');
  const pdf = await inspect(request, (await download(request, exportUrl(set.id, '101', 'format=pdf&parts=de'), token)).body);
  expect(pdf.pages!.length).toBeGreaterThan(0);
  for (const page of pdf.pages!) expect(page).toContain('Bản nháp – chưa duyệt');
});
