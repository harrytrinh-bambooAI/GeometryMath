import { expect, test } from '../../support/fixtures.ts';
import { aiCalls, bearer, world } from '../s3e1/s3e1.ts';
import { LP } from './s5e1.ts';
import {
  DRAFT_FOOTER,
  MSG05,
  accept,
  chat,
  chatOk,
  download,
  inspect,
  lock,
  patchScript,
  readyPlan,
  view,
} from './patch.ts';

/** US-S5E1-04 (chat vá) và US-S5E1-05 (duyệt, xuất) qua API: máy chủ thật, AI giả lập lesson_patch (F-S5-096). */

test('Yêu cầu mơ hồ nhận câu hỏi lại, không vá @TC-S5E1-22', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  await patchScript(request, lan, ['patch-question']);
  const r = await chatOk(request, lan, id, 'HD2', 'làm hay hơn');
  expect(r.kind).toBe('question');
  // đúng một câu: một dấu kết câu, ở cuối
  expect(r.text!.trim()).toMatch(/^[^.?!]+\?$/);
  expect(r.version).toBe(1);
  expect((await view(request, lan, id)).version).toBe(1);
});

test('Bản vá chạm mục khoá bị máy chủ loại @TC-S5E1-23', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  await lock(request, lan, id, 'muc_tieu');
  await lock(request, lan, id, 'thiet_bi');
  // lần 1 sửa muc_tieu (khoá) → loại, gọi lại; lần 2 bản vá HĐ1 hợp lệ
  await patchScript(request, lan, ['patch-locked', 'F-S5-096']);
  const before = await aiCalls(request, lan.id);
  const r = await chatOk(request, lan, id, null, 'Thêm mục tiêu về năng lực mô hình hoá');
  expect(r.kind).toBe('patch');
  expect(r.lines!.length).toBeGreaterThan(0);
  expect(r.lines!.some((l) => l.path.startsWith('muc_tieu'))).toBe(false);
  expect(r.lines!.every((l) => l.path.startsWith('hoat_dong[0].'))).toBe(true);
  expect((await aiCalls(request, lan.id)) - before).toBe(2);
  // lần 2 vẫn chạm mục khoá → reply kèm MSG-S5-05, không bản vá
  await patchScript(request, lan, ['patch-locked', 'patch-locked']);
  const again = await chatOk(request, lan, id, null, 'Thêm mục tiêu về năng lực mô hình hoá');
  expect(again.kind).toBe('reply');
  expect(again.text).toBe(MSG05);
  expect(again.lines).toBeUndefined();
  // chọn chính mục khoá để sửa: CONFLICT MSG-S5-05, không gọi AI
  const n = await aiCalls(request, lan.id);
  const locked = await chat(request, lan, id, 'muc_tieu', 'Thêm mục tiêu');
  expect(locked.status()).toBe(409);
  expect(((await locked.json()) as { code: string; message: string }).message).toBe(MSG05);
  expect(await aiCalls(request, lan.id)).toBe(n);
});

test('`before` không trùng nguyên văn thì bản vá bị loại @TC-S5E1-24', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  const cur = await view(request, lan, id);
  expect(cur.lesson.hoatDong[0]!.ten).toBe('Khởi động: Khoảng cách từ điểm');
  await patchScript(request, lan, ['patch-badbefore', 'F-S5-096']);
  const before = await aiCalls(request, lan.id);
  const r = await chatOk(request, lan, id, 'HD1', 'Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  expect((await aiCalls(request, lan.id)) - before).toBe(2); // gọi lại đúng một lần
  expect(r.kind).toBe('patch');
  expect(r.lines!.some((l) => l.before === 'Khởi động: Khoảng cách')).toBe(false);
  const ten = r.lines!.find((l) => l.path === 'hoat_dong[0].ten')!;
  expect(ten.before).toBe('Khởi động: Khoảng cách từ điểm');
  // cả hai lần lệch: không bản vá nào được trả về
  await patchScript(request, lan, ['patch-badbefore', 'patch-badbefore']);
  const bad = await chatOk(request, lan, id, 'HD1', 'Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  expect(bad.kind).toBe('error');
  expect(bad.lines).toBeUndefined();
});

test('Yêu cầu tự tính đáp số nhận `reply` giải thích luật, không vá @TC-S5E1-25', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id, plan } = await readyPlan(request, lan);
  const answers = plan.lesson!.phieuHocTap[0]!.cau.map((q) => [q.nguon, q.dapSo, q.dapSoNguon]);
  await patchScript(request, lan, ['patch-reply']);
  const r = await chatOk(request, lan, id, 'PHT1', 'Tính luôn đáp số cho 3 câu PHT1');
  expect(r.kind).toBe('reply');
  expect(r.text).toContain('A3');
  const after = await view(request, lan, id);
  expect(after.version).toBe(1);
  expect(after.lesson.phieuHocTap[0]!.cau.map((q) => [q.nguon, q.dapSo, q.dapSoNguon])).toEqual(answers);
  for (const q of after.lesson.phieuHocTap[0]!.cau) if (q.dapSo !== null) expect(q.dapSoNguon).toBe('kho');
});

test('Hai giáo viên không vá được bản nháp của nhau @TC-S5E1-26', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const khoi = w.people['Thầy Khôi']!;
  const { id, code } = await readyPlan(request, lan);
  const before = await aiCalls(request, khoi.id);
  const lanBefore = await aiCalls(request, lan.id);
  for (const key of [code, id]) {
    const r = await chat(request, khoi, key, 'HD1', 'Đổi thành trò chơi');
    expect(r.status()).toBe(404);
    expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  }
  expect(await aiCalls(request, khoi.id)).toBe(before);
  expect(await aiCalls(request, lan.id)).toBe(lanBefore);
  const an = await chat(request, w.people['Bình An']!, id, 'HD1', 'x');
  expect(an.status()).toBe(403);
});

test('Duyệt giáo án đổi nhãn và khoá phiên bản @TC-S5E1-27', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id, code } = await readyPlan(request, lan);
  // phiên bản 2 "Sẵn sàng" (nhận một bản vá HĐ1)
  const p = await chatOk(request, lan, id, 'HD1', 'Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  expect((await accept(request, lan, id, p.patchId!)).version).toBe(2);
  const t0 = Date.now();
  const r = await request.post(`${LP}/${code}/approve`, { headers: bearer(lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  const a = (await r.json()) as { humanLabel: string; approvedVersion: number; approvedBy: string; approvedAt: string };
  expect(a.humanLabel).toBe('Giáo viên đã duyệt');
  expect(a.approvedBy).toBe(lan.id);
  expect(Math.abs(Date.parse(a.approvedAt) - t0)).toBeLessThan(5 * 60_000);
  const v = await view(request, lan, id);
  expect(v.approvedVersion).toBe(2);
  expect(v.humanLabel).toBe('Giáo viên đã duyệt');
  const mine = await request.get(`${LP}?shelf=approved`, { headers: bearer(lan.token) });
  expect(mine.status()).toBe(200);
  const items = ((await mine.json()) as { items: Array<{ draftId: string; approvedVersion: number }> }).items;
  expect(items.find((i) => i.draftId === id)?.approvedVersion).toBe(2);
  const drafts = ((await (await request.get(`${LP}?shelf=draft`, { headers: bearer(lan.token) })).json()) as { items: Array<{ draftId: string }> }).items;
  expect(drafts.map((i) => i.draftId)).not.toContain(id);
});

test('Xuất DOCX đủ khung 5512 @TC-S5E1-28', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id, code } = await readyPlan(request, lan);
  expect((await request.post(`${LP}/${id}/approve`, { headers: bearer(lan.token) })).status()).toBe(200);
  const f = await download(request, `${LP}/${code}/export?format=docx`, lan.token);
  expect(f.res.headers()['cache-control']).toContain('no-store');
  expect(f.name).toMatch(/^GA_26_T1_\d{8}\.docx$/);
  const d = await inspect(request, f.body, f.name);
  expect(d.kind).toBe('docx');
  for (const h of ['I. Mục tiêu', 'II. Thiết bị', 'III. Tiến trình', 'IV. Phiếu học tập']) expect(d.text).toContain(h);
  const acts = d.tables![0]!;
  expect(acts.slice(1).map((r) => r[1])).toEqual(["5'", "20'", "15'", "5'"]);
  const pht = d.tables![1]!.slice(1);
  expect(pht).toHaveLength(3);
  const plan = await view(request, lan, id);
  const qs = plan.lesson.phieuHocTap[0]!.cau;
  pht.forEach((row, i) => {
    expect(row[2]).toBe(qs[i]!.nguon);
    expect(row[4]).toBe(qs[i]!.dapSo ?? '(đáp số chưa kiểm chứng)');
  });
  expect(qs.filter((q) => q.dapSoNguon === 'kho').length).toBeGreaterThan(0);
  expect(d.footers!.join(' ')).not.toContain('Bản nháp');
  expect(d.text).not.toContain('Bản nháp');
  // không chứa tên hay mã người dùng (BR-S5E1-15)
  expect(d.text).not.toContain(lan.displayName);
  expect(d.text).not.toContain(lan.userCode);
});

test('Xuất PDF của bản chưa duyệt có chân trang nháp @TC-S5E1-29', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  const f = await download(request, `${LP}/${id}/export?format=pdf`, lan.token);
  expect(f.name).toMatch(/^GA_26_T1_\d{8}\.pdf$/);
  expect(f.res.headers()['content-type']).toContain('application/pdf');
  const d = await inspect(request, f.body, f.name);
  expect(d.kind).toBe('pdf');
  expect(d.pages!.length).toBeGreaterThan(0);
  for (const page of d.pages!) expect(page).toContain(DRAFT_FOOTER);
  expect(d.text).toContain('Khoảng cách');
});

test('Sửa sau khi duyệt tạo phiên bản mới mang nhãn "AI soạn" @TC-S5E1-30', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  const p1 = await chatOk(request, lan, id, 'HD1', 'Đổi thành trò chơi Ai nhanh hơn, giữ 5 phút');
  await accept(request, lan, id, p1.patchId!);
  expect((await request.post(`${LP}/${id}/approve`, { headers: bearer(lan.token) })).status()).toBe(200);
  const p2 = await chatOk(request, lan, id, 'HD3', 'Thêm trò chơi cho phần luyện tập');
  expect(p2.lines!.every((l) => l.path.startsWith('hoat_dong[2].'))).toBe(true);
  const a = await accept(request, lan, id, p2.patchId!);
  expect(a.version).toBe(3);
  expect(a.humanLabel).toBe('AI soạn');
  expect(a.approvedVersion).toBe(2);
  const v = await view(request, lan, id);
  expect(v.version).toBe(3);
  expect(v.humanLabel).toBe('AI soạn');
  expect(v.approvedVersion).toBe(2);
  // xuất mặc định lấy phiên bản 3 kèm chân trang nháp
  const f = await download(request, `${LP}/${id}/export?format=docx`, lan.token);
  const d = await inspect(request, f.body, f.name);
  expect(d.footers!.join(' ')).toContain(DRAFT_FOOTER);
  expect(d.tables![0]![3]![0]).toContain('Trò chơi: Ai nhanh hơn');
  // phiên bản 2 (đã duyệt) vẫn xuất được, không chân trang nháp
  const old = await inspect(request, (await download(request, `${LP}/${id}/export?format=docx&version=2`, lan.token)).body);
  expect(old.footers!.join(' ')).not.toContain('Bản nháp');
});

test('Câu chưa kiểm chứng không in đáp số @TC-S5E1-31', async ({ request }) => {
  const w = await world(request);
  const lan = w.people['Cô Lan']!;
  const { id } = await readyPlan(request, lan);
  // thêm vào PHT1 một câu "moi" (engine không tính được → nhãn máy "Cần xem lại")
  const moi = {
    so: 4,
    nguon: 'moi',
    muc_do: 'VD',
    de: 'Cho hình chóp S.ABC có SA ⊥ (ABC), SA = a, tam giác ABC đều cạnh a. Tính d(A, (SBC)).',
    kt: ['KT11-C07-CT-003'],
    dap_so: null,
    buoc_goi_y: ['Dựng AK ⊥ BC, AH ⊥ SK.'],
  };
  await patchScript(request, lan, [
    JSON.stringify({
      kind: 'patch',
      patches: [{ op: 'add', path: 'phieu_hoc_tap[0].cau', before: null, after: moi, ly_do: 'Thêm câu vận dụng mới.' }],
      de_xuat: [],
      unsure: [],
    }),
  ]);
  const p = await chatOk(request, lan, id, 'PHT1', 'Thêm một câu vận dụng mới');
  expect(p.kind).toBe('patch');
  await accept(request, lan, id, p.patchId!);
  const v = await view(request, lan, id);
  const qs = v.lesson.phieuHocTap[0]!.cau;
  expect(qs).toHaveLength(4);
  expect(qs[3]!.dapSoNguon).toBe('can-xem-lai');
  const f = await download(request, `${LP}/${id}/export?format=docx`, lan.token);
  const rows = (await inspect(request, f.body, f.name)).tables![1]!.slice(1);
  expect(rows[3]![2]).toBe('moi');
  expect(rows[3]![4]).toBe('(đáp số chưa kiểm chứng)');
  rows.slice(0, 3).forEach((r, i) => {
    if (qs[i]!.dapSoNguon === 'kho') expect(r[4]).toBe(qs[i]!.dapSo);
  });
  expect(qs.slice(0, 3).some((q) => q.dapSoNguon === 'kho')).toBe(true);
});
