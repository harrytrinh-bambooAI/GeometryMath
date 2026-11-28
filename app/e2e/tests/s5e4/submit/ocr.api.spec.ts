import { expect, test } from '../../../support/fixtures.ts';
import { API, PASSWORD, bearer, hook } from '../../s3e1/s3e1.ts';
import { deleteAccount } from '../../s3e5/account.ts';
import { pageUrl, setup, sha256 } from '../s5e4.ts';
import { P1, P2, SUB, ack, aiOn, getSub, subState, submitOk, waitOcr, type Ocr } from './submit.ts';

/**
 * US-S5E4-04 — bản đọc chữ (OCR) của bài nộp thật: việc nền grade_ocr (giả lập F-S5-095: mỗi trang 1 đoạn "chưa chắc"),
 * giáo viên sửa tạo phiên bản mới, AI lỗi vẫn chấm tay được, học sinh không xem/sửa. Thêm: job xoá theo tài khoản xoá cả
 * dữ liệu bài nộp của luồng thật (TC-S5E4-43, SubmissionDataEraser "ocr" + "submission").
 */

const aiCalls = async (rq: Parameters<typeof hook>[0], userId: string) =>
  ((await (await rq.get(`${API}/test-hooks/ai/calls?userId=${userId}&limit=100`)).json()) as Array<{ purpose: string; status?: string }>).filter(
    (c) => c.purpose === 'grade_ocr',
  );

test('Bản đọc chữ 2 trang có đoạn tô chưa chắc @TC-S5E4-19', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await aiOn(request, lan, c11.id);
  const bn01 = await submitOk(request, an, bc.id, [P1, P2]);
  const o = await waitOcr(request, lan, bn01.id);
  expect(o.status).toBe('done');
  expect(o.version).toBe(1);
  expect(o.aiAuthored).toBe(true);
  expect(o.pages.map((p) => p.pageNo)).toEqual([1, 2]);
  const unsure = o.pages.flatMap((p) => p.segments).filter((s) => s.unsure);
  expect(unsure).toHaveLength(2);
  for (const s of unsure) {
    expect(s.confidence).toBeLessThan(0.6);
    expect(s.region).toEqual(expect.objectContaining({ x: expect.any(Number), y: expect.any(Number), w: expect.any(Number), h: expect.any(Number) }));
  }
  // AiCallLog task grade_ocr: mỗi trang một lượt
  expect(await aiCalls(request, an.id)).toHaveLength(2);
  // nội dung gửi không có tên hay mã người dùng, mã lớp
  for (const n of ['1', '2']) {
    const r = await request.get(`${API}/test-hooks/s5e4/submit/ocr-prompt?${new URLSearchParams({ orgId: w.orgId, versionId: bn01.versionId, pageNo: n })}`);
    const prompt = ((await r.json()) as { prompt: string }).prompt;
    for (const bad of [an.displayName, an.userCode, an.id, w.classes['11A1']!.id, '11A1', lan.displayName, w.orgId]) {
      expect(prompt).not.toContain(bad);
    }
  }
});

test('Giáo viên sửa bản đọc chữ tạo phiên bản mới @TC-S5E4-20', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await aiOn(request, lan, c11.id);
  const bn01 = await submitOk(request, an, bc.id, [P1, P2]);
  const v1 = await waitOcr(request, lan, bn01.id);
  const p2 = v1.pages.find((p) => p.pageNo === 2)!;
  expect(p2.segments.map((s) => s.text)).toContain('AH = a√6/2');
  const before = (await getSub(request, lan, bn01.id)).pages.map((p) => p.sha256);
  // cô Lan sửa "a√6/2" thành "a√6/3"
  const r = await request.put(`${SUB}/${bn01.id}/ocr`, {
    headers: bearer(lan.token),
    data: { baseVersion: 1, pages: [{ pageNo: 2, segments: p2.segments.map((s) => ({ text: s.text.replace('a√6/2', 'a√6/3') })) }] },
  });
  expect(r.status(), await r.text()).toBe(200);
  const v2 = (await r.json()) as Ocr;
  expect(v2.version).toBe(2);
  expect(v2.editedBy).toBe(lan.id);
  expect(v2.versions.map((v) => v.version)).toEqual([1, 2]);
  const fixed = v2.pages.find((p) => p.pageNo === 2)!.segments.find((s) => s.text === 'AH = a√6/3')!;
  expect(fixed.unsure).toBe(false);
  // phiên bản 1 vẫn đọc được
  const old = await request.get(`${SUB}/${bn01.id}/ocr?version=1`, { headers: bearer(lan.token) });
  expect(old.status()).toBe(200);
  const o1 = (await old.json()) as Ocr;
  expect(o1.version).toBe(1);
  expect(o1.pages.find((p) => p.pageNo === 2)!.segments.map((s) => s.text)).toContain('AH = a√6/2');
  // băm ảnh gốc không đổi
  expect((await getSub(request, lan, bn01.id)).pages.map((p) => p.sha256)).toEqual(before);
  expect(before).toEqual([sha256(P1), sha256(P2)]);
  // sửa trên bản cũ bị CONFLICT
  const stale = await request.put(`${SUB}/${bn01.id}/ocr`, { headers: bearer(lan.token), data: { baseVersion: 1, pages: [{ pageNo: 1, segments: [{ text: 'x' }] }] } });
  expect(stale.status()).toBe(409);
});

test('AI lỗi thì bài vẫn chấm tay được @TC-S5E4-21', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await aiOn(request, lan, c11.id);
  // giả lập grade_ocr lỗi cả hai lần (lượt đầu và lần gọi lại)
  const set = await request.post(`${API}/test-hooks/ai`, { data: { userId: an.id, mode: 'error' } });
  expect(set.status()).toBe(200);
  try {
    const bn01 = await submitOk(request, an, bc.id, [P1]);
    const o = await waitOcr(request, lan, bn01.id);
    expect(o.status).toBe('failed');
    expect(o.statusLabel).toBe('Chưa đọc được');
    expect(o.canRetry).toBe(true);
    // cô Lan mở BN-01: trạng thái Chờ chấm, ảnh gốc vẫn xem được
    const s = await getSub(request, lan, bn01.id);
    expect(s.gradingStatusLabel).toBe('Chờ chấm');
    const u = await request.get(pageUrl(s.versionId), { headers: bearer(lan.token) });
    expect(u.status(), await u.text()).toBe(200);
    const img = await request.get(`${API}${((await u.json()) as { url: string }).url}`);
    expect(img.status()).toBe(200);
    // Đọc lại (AI đã hồi) → có bản đọc chữ
    await request.post(`${API}/test-hooks/ai`, { data: { userId: an.id, mode: 'ok' } });
    const re = await request.post(`${SUB}/${bn01.id}/ocr/retry`, { headers: bearer(lan.token) });
    expect(re.status(), await re.text()).toBe(200);
    await expect.poll(async () => ((await (await request.get(`${SUB}/${bn01.id}/ocr`, { headers: bearer(lan.token) })).json()) as Ocr).status, { timeout: 20_000 }).toBe('done');
  } finally {
    await request.post(`${API}/test-hooks/ai`, { data: { userId: an.id, mode: 'ok' } });
  }
});

test('Học sinh không xem hay sửa được bản đọc chữ @TC-S5E4-22', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await aiOn(request, lan, c11.id);
  const bn01 = await submitOk(request, an, bc.id, [P1, P2]);
  await waitOcr(request, lan, bn01.id);
  const g = await request.get(`${SUB}/${bn01.id}/ocr`, { headers: bearer(an.token) });
  expect(g.status()).toBe(403);
  expect(((await g.json()) as { code: string }).code).toBe('FORBIDDEN');
  const p = await request.put(`${SUB}/${bn01.id}/ocr`, { headers: bearer(an.token), data: { pages: [{ pageNo: 1, segments: [{ text: 'x' }] }] } });
  expect(p.status()).toBe(403);
  expect(((await p.json()) as { code: string }).code).toBe('FORBIDDEN');
  // giáo viên không chủ lớp: không lộ bài
  const khoi = w.people['Thầy Khôi']!;
  expect((await request.get(`${SUB}/${bn01.id}/ocr`, { headers: bearer(khoi.token) })).status()).toBe(404);
});

test('Xoá tài khoản học sinh xoá bản đọc chữ và dữ liệu bài nộp của luồng thật @TC-S5E4-43', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await aiOn(request, lan, c11.id);
  const bn01 = await submitOk(request, an, bc.id, [P1, P2]);
  await waitOcr(request, lan, bn01.id);
  const before = await subState(request, w.orgId, bn01.id, an.id);
  expect(before.ocr).toBe(1);
  expect(before.keyed_versions).toBe(1);
  expect(before.acks).toBe(1);
  const d = await deleteAccount(request, an.token, an.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(d.status(), await d.text()).toBe(200);
  const out = (await hook(request, 's5e4/purge-accounts', { assignmentId: bc.id })) as { images: number; rows: number };
  expect(out.images).toBe(2);
  expect(out.rows).toBeGreaterThanOrEqual(3);
  const after = await subState(request, w.orgId, bn01.id, an.id);
  expect(after.ocr).toBe(0);
  expect(after.uploads).toBe(0);
  expect(after.acks).toBe(0);
  expect(after.keyed_versions).toBe(0);
});

test('Xoá tài khoản: bài nộp không còn ảnh vẫn được xoá bản đọc chữ và dữ liệu (REVIEW-S5 #5) @TC-S5E4-43', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await aiOn(request, lan, c11.id);
  const bn01 = await submitOk(request, an, bc.id, [P1, P2]);
  await waitOcr(request, lan, bn01.id);
  // ảnh đã bị xoá riêng trước đó: job không còn ảnh nào để suy ra học sinh cần xoá
  const dropped = (await hook(request, 's5e4/drop-images', { orgId: w.orgId, submissionId: bn01.versionId })) as { images: number };
  expect(dropped.images).toBe(2);
  expect((await subState(request, w.orgId, bn01.id, an.id)).ocr).toBe(1);
  const d = await deleteAccount(request, an.token, an.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(d.status(), await d.text()).toBe(200);
  const out = (await hook(request, 's5e4/purge-accounts', { assignmentId: bc.id })) as { images: number; students: number; rows: number };
  expect(out.images).toBe(0);
  expect(out.students).toBe(1);
  expect(out.rows).toBeGreaterThanOrEqual(3);
  const after = await subState(request, w.orgId, bn01.id, an.id);
  expect(after.ocr).toBe(0);
  expect(after.acks).toBe(0);
  expect(after.keyed_versions).toBe(0);
  // chạy lại không còn gì để xoá
  expect(((await hook(request, 's5e4/purge-accounts', { assignmentId: bc.id })) as { students: number }).students).toBe(0);
});
