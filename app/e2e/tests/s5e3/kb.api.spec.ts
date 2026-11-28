import { expect, test } from '../../support/fixtures.ts';
import { API, aiCalls, assignSet, bearer, err } from '../s3e1/s3e1.ts';
import { LP } from '../s5e1/s5e1.ts';
import { accept, chatOk, readyPlan } from '../s5e1/patch.ts';
import {
  KB,
  KT,
  MSG,
  type DocView,
  activate,
  classKb,
  edit,
  grant,
  json,
  publish,
  retrieve,
  s5e3World,
  sampleDocx,
  sha256,
  startUpload,
  submit,
  tl01,
  tl02,
  uploadDoc,
  view,
} from './kb.ts';

/**
 * US-S5E3-01 (kho có phiên bản), US-S5E3-02 (gắn lớp / bài, thời hạn), US-S5E3-07 (truy xuất toàn văn bỏ dấu, ưu tiên mã
 * kiến thức, ngưỡng) — máy chủ thật, AI giả lập (không ca nào gọi AI ngoài trợ lý giáo án ở TC-S5E3-03).
 */

// ------------------------------------------------------------------ US-S5E3-01

test('Tải tệp docx tạo tài liệu Nháp tách mục @TC-S5E3-01', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const file = await sampleDocx(request, 240);
  expect(file.length).toBeGreaterThan(200 * 1024);
  const d = await json<DocView>(await uploadDoc(request, lan, 'Phieu-on-chuong-VII.docx', file), 201);
  expect(d.status).toBe('draft');
  expect(d.statusLabel).toBe('Nháp');
  expect(d.version).toBe(1);
  expect(d.ownerId).toBe(lan.id);
  expect(d.sourceKind).toBe('file');
  expect(d.fileName).toBe('Phieu-on-chuong-VII.docx');
  expect(d.sections.length).toBeGreaterThanOrEqual(1);
  expect(d.sections.every((s) => s.title.length > 0)).toBe(true);
  expect(d.sections.map((s) => s.title)).toContain('2. Hệ thức 1/AH²');
  expect(d.sections.find((s) => s.title === '2. Hệ thức 1/AH²')!.codes).toEqual([KT]);
  // tệp gốc giữ nguyên, tải lại được
  const back = await request.get(`${KB}/${d.id}/versions/1/file`, { headers: bearer(lan.token) });
  expect(back.status()).toBe(200);
  expect(back.headers()['cache-control']).toContain('no-store');
  expect(sha256(await back.body())).toBe(sha256(file));
});

test('Duyệt đưa phiên bản mới lên Đang dùng, bản cũ sang Lưu trữ @TC-S5E3-02', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const d = await tl01(request, lan, w.classes['11A1']!.id);
  expect(d.code).toBe('TL-01');
  // cô Lan vừa lưu v3 rồi gửi Chờ duyệt
  const v3 = await json<DocView>(await edit(request, lan, 'TL-01', 2, `${d.content}\nBổ sung: ví dụ hình lập phương.\n`), 201);
  expect(v3.version).toBe(3);
  expect((await json<DocView>(await submit(request, lan, 'TL-01', 3), 200)).status).toBe('pending');
  const a = await json<DocView>(await activate(request, lan, 'TL-01', 3), 200);
  expect(a.status).toBe('active');
  expect(a.activeVersion).toBe(3);
  const st = Object.fromEntries(a.versions.map((v) => [v.version, v.status]));
  expect(st).toEqual({ 1: 'archived', 2: 'archived', 3: 'active' });
  expect(a.versions.filter((v) => v.status === 'active')).toHaveLength(1);
});

test('Giáo án đã duyệt đưa vào kho @TC-S5E3-03', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const { id, code } = await readyPlan(request, lan);
  // chưa duyệt: không đưa vào kho
  const early = await request.post(KB, { headers: bearer(lan.token), data: { fromLessonPlan: code } });
  expect(early.status()).toBe(409);
  expect((await err(early)).code).toBe('CONFLICT');
  // phiên bản 2 (nhận một bản vá HĐ1) rồi duyệt
  const p = await chatOk(request, lan, id, 'HD1', 'Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  expect((await accept(request, lan, id, p.patchId!)).version).toBe(2);
  expect((await request.post(`${LP}/${code}/approve`, { headers: bearer(lan.token) })).status()).toBe(200);
  const d = await json<DocView>(await request.post(KB, { headers: bearer(lan.token), data: { fromLessonPlan: code } }), 201);
  expect(d.status).toBe('draft');
  expect(d.sourceKind).toBe('lesson_plan');
  expect(d.sourceLabel).toBe(`Giáo án ${code} v2`);
  expect(d.sections.map((s) => s.title.split(' — ')[0])).toEqual([
    'Hoạt động 1: Mở đầu',
    'Hoạt động 2: Hình thành kiến thức',
    'Hoạt động 3: Luyện tập',
    'Hoạt động 4: Vận dụng',
  ]);
  // giáo án của người khác: NOT_FOUND
  const khoi = w.people['Thầy Khôi']!;
  const other = await request.post(KB, { headers: bearer(khoi.token), data: { fromLessonPlan: code } });
  expect(other.status()).toBe(404);
});

test('Sửa phiên bản đã dùng tạo phiên bản mới, không ghi đè @TC-S5E3-04', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  await tl01(request, lan);
  const before = await view(request, lan, 'TL-01', 2);
  expect(before.status).toBe('active');
  const r = await edit(request, lan, 'TL-01', 2, `${before.content}\nSửa: thêm ví dụ.\n`);
  const v3 = await json<DocView>(r, 201);
  expect(v3.version).toBe(3);
  expect(v3.status).toBe('draft');
  expect(v3.message).toBe(MSG.s23);
  const after = await view(request, lan, 'TL-01', 2);
  expect(after.contentHash).toBe(before.contentHash);
  expect(after.content).toBe(before.content);
  expect(after.status).toBe('active');
});

test('Tệp 11 MB hoặc định dạng xlsx bị từ chối @TC-S5E3-05', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  for (const [name, size] of [
    ['De-cuong.pdf', 11 * 1024 * 1024],
    ['Diem.xlsx', 50 * 1024],
  ] as const) {
    const r = await startUpload(request, lan, name, size);
    expect(r.status(), name).toBe(400);
    const e = await err(r);
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.message).toBe(MSG.s22);
    expect(e.details![0]!.message).toBe(MSG.s22);
  }
  // đuôi docx nhưng nội dung không phải docx: cũng MSG-S5-22, không tạo tài liệu
  const fake = await uploadDoc(request, lan, 'Phieu.docx', Buffer.from('PK nhưng không phải docx'.repeat(10)));
  expect(fake.status()).toBe(400);
  expect((await err(fake)).code).toBe('VALIDATION_FAILED');
  const list = await json<{ items: unknown[] }>(await request.get(KB, { headers: bearer(lan.token) }), 200);
  expect(list.items).toHaveLength(0);
});

test('Tải lại tệp trùng không tạo bản mới @TC-S5E3-06', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const file = await sampleDocx(request, 240);
  const first = await json<DocView>(await uploadDoc(request, lan, 'Phieu-on-chuong-VII.docx', file), 201);
  const again = await json<{ id: string; code: string; duplicate: boolean; message: string }>(
    await uploadDoc(request, lan, 'Phieu-on-chuong-VII.docx', file),
    200,
  );
  expect(again.id).toBe(first.id);
  expect(again.duplicate).toBe(true);
  expect(again.message).toBe('Tệp này đã có trong kho.');
  const v = await view(request, lan, first.id);
  expect(v.versions).toHaveLength(1);
  const list = await json<{ items: unknown[] }>(await request.get(KB, { headers: bearer(lan.token) }), 200);
  expect(list.items).toHaveLength(1);
});

test('Giáo viên khác và học sinh không đọc được kho của cô Lan @TC-S5E3-07', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const d = await tl01(request, lan, w.classes['11A1']!.id);
  const khoi = w.people['Thầy Khôi']!;
  for (const ref of ['TL-01', d.id]) {
    const r = await request.get(`${KB}/${ref}`, { headers: bearer(khoi.token) });
    expect(r.status()).toBe(404);
    expect((await err(r)).code).toBe('NOT_FOUND');
  }
  const an = await request.get(`${KB}/TL-01`, { headers: bearer(w.people['Bình An']!.token) });
  expect(an.status()).toBe(403);
  expect((await err(an)).code).toBe('FORBIDDEN');
  const anFile = await request.get(`${KB}/${d.id}/versions/2/file`, { headers: bearer(w.people['Bình An']!.token) });
  expect(anFile.status()).toBe(403);
});

// ------------------------------------------------------------------ US-S5E3-02

test('Gắn TL-01 cho lớp 11A1 @TC-S5E3-08', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await tl01(request, lan);
  const g = await json<{ id: string; classId: string; effective: boolean }>(await grant(request, lan, 'TL-01', { classId: c11.id }), 201);
  expect(g.classId).toBe(c11.id);
  expect(g.effective).toBe(true);
  const list = await json<{ items: Array<{ code: string; activeVersion: number }> }>(
    await request.get(classKb(c11.id), { headers: bearer(lan.token) }),
    200,
  );
  expect(list.items.map((i) => [i.code, i.activeVersion])).toEqual([['TL-01', 2]]);
});

test('Gắn kết có hạn tự hết hiệu lực @TC-S5E3-09', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await tl01(request, lan);
  const set = await assignSet(request, lan, c11.id, 'Kiểm tra 15 phút – Khoảng cách');
  const bc01 = set.items[0]!.assignmentId;
  const t0 = Date.now();
  const to = new Date(t0 + 86_400_000).toISOString();
  await json(await grant(request, lan, 'TL-01', { classId: c11.id, assignmentId: bc01, validTo: to }), 201);
  const at = async (ms: number) =>
    (
      await json<{ items: Array<{ code: string }> }>(
        await request.get(`${classKb(c11.id)}?assignmentId=${bc01}&at=${new Date(ms).toISOString()}`, { headers: bearer(lan.token) }),
        200,
      )
    ).items.map((i) => i.code);
  expect(await at(t0)).toEqual(['TL-01']);
  expect(await at(t0 + 86_400_000 + 1_000)).toEqual([]);
  // bài khác của lớp không thấy gắn riêng của BC-01
  const other = set.items[1]!.assignmentId;
  const o = await json<{ items: unknown[] }>(
    await request.get(`${classKb(c11.id)}?assignmentId=${other}&at=${new Date(t0).toISOString()}`, { headers: bearer(lan.token) }),
    200,
  );
  expect(o.items).toEqual([]);
});

test('Không gắn được tài liệu chưa Đang dùng @TC-S5E3-10', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const d = await tl02(request, lan);
  expect(d.status).toBe('pending');
  const r = await grant(request, lan, d.id, { classId: c11.id });
  expect(r.status()).toBe(409);
  expect((await err(r)).code).toBe('CONFLICT');
  const g = await json<{ items: unknown[] }>(await request.get(`${KB}/${d.id}/grants`, { headers: bearer(lan.token) }), 200);
  expect(g.items).toEqual([]);
  const list = await json<{ items: unknown[] }>(await request.get(classKb(c11.id), { headers: bearer(lan.token) }), 200);
  expect(list.items).toEqual([]);
});

test('Không gắn được cho lớp của người khác @TC-S5E3-11', async ({ request }) => {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  await tl01(request, lan);
  const r = await grant(request, lan, 'TL-01', { classId: w.classes['10B']!.id });
  expect(r.status()).toBe(404);
  expect((await err(r)).code).toBe('NOT_FOUND');
  const g = await json<{ items: unknown[] }>(await request.get(`${KB}/TL-01/grants`, { headers: bearer(lan.token) }), 200);
  expect(g.items).toEqual([]);
});

// ------------------------------------------------------------------ US-S5E3-07

async function kb11(request: Parameters<typeof s5e3World>[0]) {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await tl01(request, lan, c11.id);
  const set = await assignSet(request, lan, c11.id, 'Kiểm tra 15 phút – Khoảng cách');
  return { w, lan, c11, bc01: set.items[0]!.assignmentId };
}

test('Gõ không dấu vẫn tìm đúng mục @TC-S5E3-20', async ({ request }) => {
  const { w, c11, bc01 } = await kb11(request);
  const r = await retrieve(request, { orgId: w.orgId, classId: c11.id, assignmentId: bc01, query: 'he thuc 1/AH2 khoang cach' });
  expect(r.items.length).toBeGreaterThan(0);
  expect(r.items.length).toBeLessThanOrEqual(5);
  expect(r.items[0]).toMatchObject({ docCode: 'TL-01', version: 2, title: 'Hệ thức 1/AH²' });
  expect(r.items.every((i) => i.score >= r.minScore)).toBe(true);
});

test('Mục trùng mã kiến thức của đề được xếp trước @TC-S5E3-21', async ({ request }) => {
  const { w, c11, bc01 } = await kb11(request);
  const q = { orgId: w.orgId, classId: c11.id, assignmentId: bc01, query: 'cách tính khoảng cách' };
  const titles = (r: Awaited<ReturnType<typeof retrieve>>) => r.items.map((i) => i.title);
  // không có mã đề: mục "Khoảng cách từ điểm đến mặt phẳng" (chữ ở tiêu đề) điểm cao hơn
  const plain = await retrieve(request, q);
  expect(titles(plain)).toEqual(expect.arrayContaining(['Hệ thức 1/AH²', 'Khoảng cách từ điểm đến mặt phẳng']));
  // đề D-KC-01 mang mã KT11-C07-CT-003: mục gắn mã đứng trước
  const coded = await retrieve(request, { ...q, knowledgeCodes: [KT] });
  expect(coded.items[0]).toMatchObject({ title: 'Hệ thức 1/AH²', boosted: true, codes: [KT] });
  expect(titles(coded).indexOf('Hệ thức 1/AH²')).toBeLessThan(titles(coded).indexOf('Khoảng cách từ điểm đến mặt phẳng'));
});

test('Điểm dưới ngưỡng thì không trả mục nào @TC-S5E3-22', async ({ request }) => {
  const { w, c11, bc01 } = await kb11(request);
  const an = w.people['Bình An']!;
  const before = await aiCalls(request, an.id);
  const r = await retrieve(request, { orgId: w.orgId, classId: c11.id, assignmentId: bc01, query: 'đạo hàm của hàm hợp', knowledgeCodes: [KT] });
  expect(r.items).toEqual([]);
  expect(r.message).toBe(MSG.s20);
  expect(r.aiCalled).toBe(false);
  expect(r.minScore).toBe(0.1);
  expect(await aiCalls(request, an.id)).toBe(before);
});

test('Truy xuất p95 ≤ 150 ms với kho 2 000 mục @TC-S5E3-23', async ({ request }) => {
  test.setTimeout(120_000);
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const b = await json<{ sections: number }>(
    await request.post(`${API}/test-hooks/s5e3/bulk`, {
      data: { orgId: w.orgId, teacherId: lan.id, classId: c11.id, sections: 2000 },
    }),
    200,
  );
  expect(b.sections).toBe(2000);
  const phrases = [
    'khoang cach tu diem den mat phang',
    'góc giữa hai mặt phẳng',
    'the tich khoi chop',
    'hai mặt phẳng vuông góc khi nào',
    'đường thẳng vuông góc mặt phẳng',
    'hinh lang tru dung',
    'phép chiếu vuông góc',
    'goc giua duong thang va mat phang',
    'cách tính khoảng cách',
    'hình chóp đáy hình vuông cạnh a',
  ];
  const times: number[] = [];
  for (let i = 0; i < 200; i++) {
    const r = await retrieve(request, {
      orgId: w.orgId,
      classId: c11.id,
      query: `${phrases[i % phrases.length]} ${i % 3 === 0 ? 'bài số ' + i : ''}`.trim(),
      knowledgeCodes: i % 2 === 0 ? [KT] : [],
    });
    expect(r.items.length).toBeLessThanOrEqual(5);
    times.push(r.tookMs);
  }
  times.sort((a, b2) => a - b2);
  const p95 = times[Math.ceil(times.length * 0.95) - 1]!;
  test.info().annotations.push({ type: 'p95', description: `${p95.toFixed(1)} ms (200 truy vấn, 2 000 mục)` });
  expect(p95).toBeLessThanOrEqual(150);
});
