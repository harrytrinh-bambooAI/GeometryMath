import { expect, test } from '../../support/fixtures.ts';
import { walk } from '../s2e8/s2e8.ts';
import {
  API,
  K,
  MSG,
  bearer,
  createLink,
  createVariants,
  docOptions,
  expectError,
  exportDocs,
  listLinks,
  listVariants,
  ok,
  openShare,
  revokeLink,
  slot,
  token,
  type Batch,
  type ExportView,
  type Link,
} from './s2e9.ts';

/**
 * S2-E9 — API biến thể đề (US-S2E9-01), xuất và liên kết chỉ xem (US-S2E9-02): biên số lượng, quyền theo chủ đề,
 * giới hạn thêm đề, dữ liệu trả về của liên kết công khai.
 */

test('@TC-S2E9-008 tạo 1 và 10 biến thể nhận, 0 và 11 bị chặn', async ({ request }, info) => {
  const s = slot(8, info);
  const t = await token(request, s.org, s.trang);
  const one = await ok<Batch>(await createVariants(request, t, s.doc(K.FX12), 1), 201);
  expect(one.created).toBe(1);
  expect(one.items).toHaveLength(1);
  const ten = await ok<Batch>(await createVariants(request, t, s.doc(K.FX12), 10), 201);
  expect(ten.created).toBe(10);
  expect(new Set(ten.items.map((v) => v.statement)).size).toBe(10);
  for (const v of ten.items) {
    expect(v.statement).not.toBe('Hình chữ nhật ABCD có AB = 6, AD = 8. Tính AC.');
    expect(v.answerSource).toBe('engine');
  }
  const zero = await expectError(await createVariants(request, t, s.doc(K.FX12), 0), 400, 'VALIDATION_FAILED', MSG.variantCount);
  expect(zero.message).toBe('Chọn từ 1 đến 10');
  await expectError(await createVariants(request, t, s.doc(K.FX12), 11), 400, 'VALIDATION_FAILED', MSG.variantCount);
  expect(await listVariants(request, t, s.doc(K.FX12))).toHaveLength(11);
  // đề thuần chữ và đề thiết diện động bị từ chối, không tạo gì (US-S2E9-01-FC1, FC2 qua API)
  await expectError(await createVariants(request, t, s.doc(K.TEXT_ONLY), 3), 409, 'CONFLICT', MSG.noNumbers);
  await expectError(await createVariants(request, t, s.doc(K.SECTION), 3), 409, 'CONFLICT', MSG.unsupported);
});

test('@TC-S2E9-011 giáo viên khác không tạo biến thể từ đề Của tôi của người khác', async ({ request }, info) => {
  const s = slot(11, info);
  const lan = await token(request, s.org, s.lan);
  await expectError(await createVariants(request, lan, s.doc(K.FX12), 3), 404, 'NOT_FOUND');
  const listByLan = await request.get(`${API}/api/v1/teacher/variants?sourceId=${s.doc(K.FX12)}`, { headers: bearer(lan) });
  expect(listByLan.status()).toBe(404);
  const trang = await token(request, s.org, s.trang);
  expect(await listVariants(request, trang, s.doc(K.FX12))).toHaveLength(0);
  expect((await docOptions(request, trang)).filter((d) => d.variant)).toHaveLength(0);
  // học sinh không có đường tạo biến thể
  const an = await token(request, s.org, s.an, 'STUDENT');
  await expectError(await createVariants(request, an, s.doc(K.FX12), 1), 403, 'FORBIDDEN');
});

test('@TC-S2E9-012 biến thể tính vào giới hạn thêm đề: 20 nhận, 21 bị chặn', async ({ request }, info) => {
  const s = slot(12, info); // seed: Cô Trang đã thêm 18 đề trong 60 phút
  const t = await token(request, s.org, s.trang);
  const r = await createVariants(request, t, s.doc(K.FX12), 3);
  const body = await expectError(r, 429, 'RATE_LIMITED');
  expect(body.message).toContain('tạo được 2 biến thể');
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  const made = await listVariants(request, t, s.doc(K.FX12));
  expect(made).toHaveLength(2);
  // đã đủ 20: thêm nữa bị chặn ngay, không tạo thêm
  await expectError(await createVariants(request, t, s.doc(K.FX12), 1), 429, 'RATE_LIMITED');
  expect(await listVariants(request, t, s.doc(K.FX12))).toHaveLength(2);
});

test('@TC-S2E9-017 lời giải chưa duyệt không kèm được', async ({ request }, info) => {
  const s = slot(17, info);
  const t = await token(request, s.org, s.trang);
  const option = (await docOptions(request, t)).find((d) => d.id === s.doc(K.FX11_AI))!;
  expect(option.humanLabel).toBe('AI soạn');
  expect(option.canAttachSolution).toBe(false); // ô "Kèm lời giải" khóa kèm "Cần duyệt lời giải trước" ở giao diện
  const r = await exportDocs(request, t, [s.doc(K.FX11_AI)], true);
  await expectError(r, 409, 'CONFLICT', MSG.needApproval);
  expect(await r.text()).not.toContain('70°'); // không tệp, không bước giải nào lọt ra
  // lẫn một đề đã duyệt với đề chưa duyệt cũng bị chặn
  await expectError(await exportDocs(request, t, [s.doc(K.FX10), s.doc(K.FX11_AI)], true), 409, 'CONFLICT', MSG.needApproval);
  // không kèm lời giải thì xuất được và không có lời giải
  const plain = await ok<ExportView>(await exportDocs(request, t, [s.doc(K.FX11_AI)], false));
  expect(plain.items[0]!.solution ?? null).toBeNull();
});

test('@TC-S2E9-019 xuất 1 và 20 đề nhận, 0 và 21 bị chặn', async ({ request }, info) => {
  const s = slot(19, info); // seed: 25 "Đề số i"
  const t = await token(request, s.org, s.trang);
  const ids = Array.from({ length: 25 }, (_, i) => s.doc(K.NUMBERED + i));
  const one = await ok<ExportView>(await exportDocs(request, t, ids.slice(0, 1)));
  expect(one.items.map((i) => i.title)).toEqual(['Đề số 1']);
  const twenty = await ok<ExportView>(await exportDocs(request, t, ids.slice(0, 20)));
  expect(twenty.items.map((i) => i.id)).toEqual(ids.slice(0, 20)); // đúng thứ tự đã chọn
  expect(twenty.items.every((i) => i.solution == null)).toBe(true);
  await expectError(await exportDocs(request, t, []), 400, 'VALIDATION_FAILED', MSG.exportMin);
  await expectError(await exportDocs(request, t, ids.slice(0, 21)), 400, 'VALIDATION_FAILED', MSG.exportMax);
});

test('@TC-S2E9-021 mở liên kết không đăng nhập chỉ thấy đề và hình', async ({ request }, info) => {
  const s = slot(21, info);
  const t = await token(request, s.org, s.trang);
  const link = await ok<Link>(await createLink(request, t, s.doc(K.FX10)), 201);
  expect(link.token).toMatch(/^[A-Za-z0-9_-]{43}$/); // 256 bit ngẫu nhiên
  const r = await openShare(request, link.token!);
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const body = (await r.json()) as { statement: string; scene: { points: unknown[]; segments: unknown[] }; readOnly: boolean };
  expect(body.statement).toBe('Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.');
  expect(body.scene.points).toHaveLength(3);
  expect(body.scene.segments).toHaveLength(3);
  expect(body.readOnly).toBe(true);
  const { keys, strings } = walk(body);
  expect(keys.filter((k) => /solution|step|answer|owner|teacher|author|user|org|hint/i.test(k)), 'khóa lộ lời giải hay danh tính').toEqual([]);
  const text = strings.join('\n') + (await r.text());
  for (const secret of ['Cô Trang', 'GV-', s.trang, s.org, '90°', 'vuông tại A']) expect(text).not.toContain(secret);
  // trình duyệt (Accept: text/html) nhận trang chỉ xem: có chữ đề và hình, không nút, không biểu mẫu, không kịch bản
  const html = await (await openShare(request, link.token!, { accept: 'text/html' })).text();
  expect(html).toContain('Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.');
  expect(html).toContain('<svg');
  expect((html.match(/<line /g) ?? []).length).toBe(3);
  for (const banned of ['<button', '<form', '<script', '<input', 'Cô Trang', 'GV-', '90°', 'Sửa']) expect(html).not.toContain(banned);
});

test('@TC-S2E9-023 thu hồi thì liên kết như mã sai', async ({ request }, info) => {
  const s = slot(23, info);
  const t = await token(request, s.org, s.trang);
  const link = await ok<Link>(await createLink(request, t, s.doc(K.FX10)), 201);
  expect((await openShare(request, link.token!)).status()).toBe(200);
  expect((await revokeLink(request, t, link.id)).status()).toBe(204);
  const revoked = await openShare(request, link.token!);
  const wrong = await openShare(request, 'A'.repeat(43));
  const a = await expectError(revoked, 404, 'NOT_FOUND', MSG.linkGone);
  const b = await expectError(wrong, 404, 'NOT_FOUND', MSG.linkGone);
  expect(Object.keys(a).sort()).toEqual(Object.keys(b).sort()); // không phân biệt đã thu hồi hay sai
  expect(await listLinks(request, t, s.doc(K.FX10))).toHaveLength(0);
  // thu hồi lần hai: không còn liên kết để thu hồi
  expect((await revokeLink(request, t, link.id)).status()).toBe(404);
});

test('@TC-S2E9-025 liên kết thứ 5 nhận, thứ 6 bị chặn', async ({ request }, info) => {
  const s = slot(25, info); // seed: FX-10 đã có 4 liên kết đang dùng
  const t = await token(request, s.org, s.trang);
  expect(await listLinks(request, t, s.doc(K.FX10))).toHaveLength(4);
  const fifth = await ok<Link>(await createLink(request, t, s.doc(K.FX10)), 201);
  expect(fifth.days).toBe(7);
  await expectError(await createLink(request, t, s.doc(K.FX10)), 409, 'CONFLICT', MSG.linkMax);
  expect(await listLinks(request, t, s.doc(K.FX10))).toHaveLength(5);
  // thu hồi một liên kết thì tạo lại được (giới hạn tính theo liên kết ĐANG dùng)
  expect((await revokeLink(request, t, fifth.id)).status()).toBe(204);
  await ok<Link>(await createLink(request, t, s.doc(K.FX10), 30), 201);
  // hạn ngoài 1 / 7 / 30 ngày bị chặn
  await expectError(await createLink(request, t, s.doc(K.FX11), 0), 400, 'VALIDATION_FAILED');
  await expectError(await createLink(request, t, s.doc(K.FX11), 365), 400, 'VALIDATION_FAILED');
});

test('@TC-S2E9-026 liên kết chỉ xem không có thao tác ghi', async ({ request }, info) => {
  const s = slot(26, info);
  const t = await token(request, s.org, s.trang);
  const link = await ok<Link>(await createLink(request, t, s.doc(K.FX10)), 201);
  const url = `${API}/api/v1/share/${link.token}`;
  await expectError(await request.put(url, { data: { statement: 'Đề đã bị sửa' } }), 403, 'FORBIDDEN');
  await expectError(await request.patch(url, { data: { title: 'x' } }), 403, 'FORBIDDEN');
  await expectError(await request.post(`${url}/assign`, { data: { classId: s.org } }), 403, 'FORBIDDEN');
  await expectError(await request.delete(url), 403, 'FORBIDDEN');
  const after = (await (await openShare(request, link.token!)).json()) as { statement: string };
  expect(after.statement).toBe('Tam giác ABC có AB = 5, AC = 12, BC = 13. Tính góc A.'); // đề không đổi
  expect(await listLinks(request, t, s.doc(K.FX10))).toHaveLength(1); // liên kết vẫn còn
});

test('@TC-S2E9-027 giáo viên khác không xuất hay tạo liên kết cho đề Của tôi của người khác', async ({ request }, info) => {
  const s = slot(27, info);
  const lan = await token(request, s.org, s.lan);
  await expectError(await exportDocs(request, lan, [s.doc(K.FX10)]), 404, 'NOT_FOUND');
  await expectError(await exportDocs(request, lan, [s.doc(K.FX10)], true, true), 404, 'NOT_FOUND');
  await expectError(await createLink(request, lan, s.doc(K.FX10)), 404, 'NOT_FOUND');
  expect(await docOptions(request, lan)).toHaveLength(0); // Cô Lan không thấy đề của Cô Trang
  const trang = await token(request, s.org, s.trang);
  expect(await listLinks(request, trang, s.doc(K.FX10))).toHaveLength(0);
  // Cô Lan không thu hồi được liên kết của Cô Trang
  const link = await ok<Link>(await createLink(request, trang, s.doc(K.FX10)), 201);
  expect((await revokeLink(request, lan, link.id)).status()).toBe(404);
  expect((await openShare(request, link.token!)).status()).toBe(200);
});
