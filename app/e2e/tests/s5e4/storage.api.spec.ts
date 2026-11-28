import { expect, test } from '../../support/fixtures.ts';
import { API, PASSWORD, bearer, hook } from '../s3e1/s3e1.ts';
import { deleteAccount } from '../s3e5/account.ts';
import { EXIF_JPG, hasMetadata, isJpeg, pageUrl, setup, sha256, state, submitImage } from './s5e4.ts';

/**
 * US-S5E4-08 — kho ảnh bài làm: mã hoá khi lưu, URL ký 5 phút, gỡ EXIF/GPS, xoá theo tài khoản và năm học, ảnh gửi AI.
 * Ảnh mẫu exif-gps.jpg (GPS 21.0285,105.8542; Apple iPhone 15). AI giả lập grade_ocr ghi lại yêu cầu đã nhận (F-S5-095).
 */

test('Ảnh lưu đã mã hoá và không còn EXIF/GPS @TC-S5E4-40', async ({ request }) => {
  const { w, lan, bc } = await setup(request);
  expect(hasMetadata(EXIF_JPG), 'ảnh mẫu có EXIF').toBe(true);
  const an = w.people['Bình An']!;
  const s = await submitImage(request, w, bc.id, an);
  // đối tượng thô trong kho không giải được thành JPEG
  const raw = (await hook(request, 's5e4/raw', { orgId: w.orgId, submissionId: s.submissionId, pageNo: 1 })) as { raw: string; sha256: string };
  const obj = Buffer.from(raw.raw, 'base64');
  expect(isJpeg(obj)).toBe(false);
  expect(obj.includes(Buffer.from('JFIF', 'latin1'))).toBe(false);
  expect(obj.includes(Buffer.from('Exif', 'latin1'))).toBe(false);
  expect(obj.includes(Buffer.from('iPhone', 'latin1'))).toBe(false);
  // ảnh tải qua URL ký không có thẻ EXIF/GPS
  const u = await request.get(pageUrl(s.submissionId), { headers: bearer(lan.token) });
  expect(u.status(), await u.text()).toBe(200);
  expect(u.headers()['cache-control']).toContain('no-store');
  const { url } = (await u.json()) as { url: string };
  const img = await request.get(`${API}${url}`);
  expect(img.status()).toBe(200);
  expect(img.headers()['content-type']).toContain('image/jpeg');
  const body = await img.body();
  expect(isJpeg(body)).toBe(true);
  expect(hasMetadata(body)).toBe(false);
  // băm lưu = băm ảnh đã gỡ metadata (khác băm tệp gốc)
  expect(raw.sha256).toBe(sha256(body));
  expect(s.sha256).toBe(sha256(body));
  expect(raw.sha256).not.toBe(sha256(EXIF_JPG));
});

test('URL ký hết hạn sau 5 phút @TC-S5E4-41', async ({ request }) => {
  const { w, lan, bc } = await setup(request);
  const s = await submitImage(request, w, bc.id, w.people['Bình An']!);
  const sign = async (ageSeconds: number) =>
    ((await hook(request, 's5e4/sign', { orgId: w.orgId, userId: lan.id, role: 'TEACHER', submissionId: s.submissionId, pageNo: 1, ageSeconds })) as {
      url: string;
    }).url;
  // T0 + 299 s: còn hạn
  const ok = await request.get(`${API}${await sign(299)}`);
  expect(ok.status()).toBe(200);
  expect(isJpeg(await ok.body())).toBe(true);
  // T0 + 301 s: hết hạn
  const late = await request.get(`${API}${await sign(301)}`);
  expect(late.status()).toBe(403);
  // chữ ký bị sửa cũng 403
  const forged = (await sign(0)).replace(/sig=[^&]+/, 'sig=AAAA');
  expect((await request.get(`${API}${forged}`)).status()).toBe(403);
});

test('Không lấy được URL ký ảnh của bài ngoài quan hệ (IDOR) @TC-S5E4-42', async ({ request }) => {
  const { w, bc } = await setup(request);
  const s = await submitImage(request, w, bc.id, w.people['Bình An']!);
  for (const who of ['Thầy Khôi', 'Chi Mai']) {
    const r = await request.get(pageUrl(s.submissionId), { headers: bearer(w.people[who]!.token) });
    expect(r.status(), who).toBe(404);
    const text = await r.text();
    expect(JSON.parse(text).code).toBe('NOT_FOUND');
    expect(text).not.toContain('url');
    expect(text).not.toContain('/files/');
  }
  // chính Bình An lấy được
  const mine = await request.get(pageUrl(s.submissionId), { headers: bearer(w.people['Bình An']!.token) });
  expect(mine.status()).toBe(200);
});

test('Xoá tài khoản học sinh xoá ảnh, bản đọc chữ, đề xuất AI @TC-S5E4-43', async ({ request }) => {
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  const p1 = await submitImage(request, w, bc.id, an, { pageNo: 1 });
  const p2 = await submitImage(request, w, bc.id, an, { pageNo: 2, submissionId: p1.submissionId });
  const ids = [...p1.ids, ...p2.ids];
  const before = await state(request, w.orgId, { submissionId: p1.submissionId, ids });
  expect(before.images).toBe(2);
  expect(before.objects).toBe(2);
  // Bình An xoá tài khoản (US-S3E5-05)
  const d = await deleteAccount(request, an.token, an.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(d.status(), await d.text()).toBe(200);
  // chạy job xoá (chỉ ảnh của bài trong ca: E2E song song dùng chung trường)
  const out = (await hook(request, 's5e4/purge-accounts', { assignmentId: bc.id })) as { images: number; students: number };
  expect(out.images).toBe(2);
  expect(out.students).toBe(1);
  const after = await state(request, w.orgId, { submissionId: p1.submissionId, ids });
  expect(after.images).toBe(0);
  expect(after.objects).toBe(0);
  // AuditEvent ghi số đối tượng đã xoá, không nội dung
  const ev = after.audits.filter((a) => a.action === 'image_purge_account' && a.targetId === an.id);
  expect(ev).toHaveLength(1);
  expect(ev[0]!.n).toBe(2);
  expect(ev[0]!.targetId).toBe(an.id);
  // Bản đọc chữ, GradingProposal, CriterionAssessment, ApprovedGrade: bảng của T-261128-59/60/61 chưa có ở đợt này; job gọi
  // mọi SubmissionDataEraser (assessment.api) — xem qc_findings.md S5-E4 (Dev-S5E4-02).
});

test('Hết năm học job xoá ảnh và bản đọc chữ @TC-S5E4-44', async ({ request }) => {
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  const old = await submitImage(request, w, bc.id, an, { count: 37, schoolYear: '2026-2027' });
  const next = await submitImage(request, w, bc.id, an, { count: 3, schoolYear: '2027-2028' });
  expect((await state(request, w.orgId, { assignmentId: bc.id, schoolYear: '2026-2027' })).images).toBe(37);
  // đồng hồ của job đặt 31/07/2027 (00:05 giờ VN)
  const out = (await hook(request, 's5e4/purge-year', { assignmentId: bc.id, today: '2027-07-31' })) as { images: number; years: string[] };
  expect(out.images).toBe(37);
  expect(out.years).toEqual(['2026-2027']);
  const s = await state(request, w.orgId, { assignmentId: bc.id, schoolYear: '2026-2027', ids: old.ids });
  expect(s.images).toBe(0);
  expect(s.objects).toBe(0);
  // năm 2027–2028 không bị đụng
  const keep = await state(request, w.orgId, { assignmentId: bc.id, schoolYear: '2027-2028', ids: next.ids });
  expect(keep.images).toBe(3);
  expect(keep.objects).toBe(3);
  // một AuditEvent với số lượng 37
  const ev = s.audits.filter((a) => a.action === 'image_purge_year');
  expect(ev).toHaveLength(1);
  expect(ev[0]!.n).toBe(37);
  // 30/07/2028 (trước 31/07) chưa xoá năm 2027–2028
  const early = (await hook(request, 's5e4/purge-year', { assignmentId: bc.id, today: '2028-07-30' })) as { images: number };
  expect(early.images).toBe(0);
});

test('Yêu cầu gửi AI không có EXIF và danh tính @TC-S5E4-45', async ({ request }) => {
  const { w, lan, c11, bc } = await setup(request);
  const an = w.people['Bình An']!;
  // AI chấm của 11A1 bật (US-S5E5-04; mặc định tắt)
  const on = await request.put(`${API}/api/v1/teacher/my-classes/${c11.id}/ai-switches`, { headers: bearer(lan.token), data: { aiGrading: true } });
  expect(on.status(), await on.text()).toBe(200);
  const s = await submitImage(request, w, bc.id, an);
  const out = (await hook(request, 's5e4/ocr', { orgId: w.orgId, userId: lan.id, role: 'TEACHER', submissionId: s.submissionId, pageNo: 1 })) as {
    status: string;
    reply: string;
  };
  expect(out.status).toBe('done');
  const reply = JSON.parse(out.reply) as { text: string; unsure: unknown[]; request: string };
  expect(reply.unsure).toHaveLength(2); // F-S5-095
  const req = reply.request;
  // ảnh trong yêu cầu ở giả lập không có EXIF/GPS
  const b64 = req.split('base64,')[1]!;
  const img = Buffer.from(b64, 'base64');
  expect(isJpeg(img)).toBe(true);
  expect(hasMetadata(img)).toBe(false);
  // không danh tính: tên, mã người dùng, mã lớp, id
  for (const bad of ['Bình An', an.userCode, c11.code, an.id, c11.id, w.orgId, 'Cô Lan', '11A1']) {
    expect(req, bad).not.toContain(bad);
  }
});
