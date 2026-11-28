import { expect, test } from '../../../support/fixtures.ts';
import { API, bearer, world, type Person } from '../../s3e1/s3e1.ts';
import { bc01, pageUrl, rb01, setup, sha256 } from '../s5e4.ts';
import { HEIC, MSG, P1, P2, SUB, ack, count, due, getSub, submitFiles, submitOk, submitRaw, upload, type Submission } from './submit.ts';

/**
 * US-S5E4-03 (API) + US-S5E4-09 — nộp bài viết tay qua luồng thật: tải theo đoạn, HEIC + bản phái sinh JPEG, trần
 * 8 MB/10 trang, quyền xem, khóa lũy đẳng, nộp muộn có nhãn, nộp lại tạo phiên bản, quá hạn, tải dồn 45 em.
 */

/** JPEG hợp lệ đúng n byte (đệm sau dữ liệu nén; máy chủ giữ nguyên phần sau SOS). */
const jpegOf = (n: number) => Buffer.concat([P1, Buffer.alloc(n - P1.length, 0)]);

test('Ảnh HEIC được nhận, bản phái sinh JPEG dùng cho đọc chữ @TC-S5E4-13', async ({ request }) => {
  const { w, lan, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  const heic = await upload(request, an, HEIC, 'image/heic');
  const jpg = await upload(request, an, P1, 'image/jpeg');
  const r = await submitRaw(request, an, bc.id, [{ uploadId: heic, derivedUploadId: jpg }]);
  expect(r.status(), await r.text()).toBe(201);
  const s = (await r.json()) as Submission;
  expect(s.pages).toHaveLength(1);
  // ảnh gốc lưu đúng tệp HEIC (so băm), có bản phái sinh image/jpeg
  expect(s.pages[0]!.contentType).toBe('image/heic');
  expect(s.pages[0]!.sha256).toBe(sha256(HEIC));
  expect(s.pages[0]!.bytes).toBe(HEIC.length);
  expect(s.pages[0]!.derivedContentType).toBe('image/jpeg');
  // task grade_ocr nhận bản JPEG
  const p = await request.get(`${API}/test-hooks/s5e4/submit/ocr-prompt?${new URLSearchParams({ orgId: w.orgId, versionId: s.versionId, pageNo: '1' })}`);
  expect(p.status()).toBe(200);
  const prompt = (await p.json()) as { prompt: string; jpeg: boolean };
  expect(prompt.jpeg).toBe(true);
  expect(prompt.prompt).toContain('data:image/jpeg;base64,/9j/');
  // HEIC thiếu bản JPEG bị từ chối
  const lone = await upload(request, an, HEIC, 'image/heic');
  const bad = await submitRaw(request, an, bc.id, [{ uploadId: lone }]);
  expect(bad.status()).toBe(400);
  expect(((await bad.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  // giáo viên xem được ảnh gốc qua URL ký
  const u = await request.get(pageUrl(s.versionId), { headers: bearer(lan.token) });
  expect(u.status(), await u.text()).toBe(200);
});

test('Trang đúng 8 MB được nhận; 8 MB + 1 byte hoặc 11 trang bị từ chối @TC-S5E4-16', async ({ request }) => {
  test.setTimeout(120_000);
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  // lần 1: đúng 8 388 608 byte → 201
  const eight = jpegOf(8_388_608);
  const ok = await submitOk(request, an, bc.id, [eight]);
  expect(ok.pages[0]!.bytes).toBe(8_388_608);
  // lần 2: 8 388 609 byte → VALIDATION_FAILED
  const big = await request.post(`${SUB}/uploads`, { headers: bearer(an.token), data: { contentType: 'image/jpeg', sizeBytes: 8_388_609 } });
  expect(big.status()).toBe(400);
  expect(((await big.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  // lần 3: 11 trang 500 KB → VALIDATION_FAILED, không có phiên bản mới
  const half = jpegOf(500 * 1024);
  const r = await submitFiles(request, an, bc.id, Array.from({ length: 11 }, () => half));
  expect(r.status()).toBe(400);
  expect(((await r.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  expect((await getSub(request, an, ok.id)).version).toBe(1);
});

test('Học sinh không đọc được bài nộp của bạn @TC-S5E4-18', async ({ request }) => {
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  const mai = w.people['Chi Mai']!;
  await ack(request, mai);
  const bn02 = await submitOk(request, mai, bc.id, [P1, P2, P1]);
  const r = await request.get(`${SUB}/${bn02.id}`, { headers: bearer(an.token) });
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  expect(text).not.toContain('/api/v1/files/images/');
  expect(text).not.toContain('sha256');
  // URL ký ảnh của bạn cũng không cấp
  const u = await request.get(pageUrl(bn02.versionId), { headers: bearer(an.token) });
  expect(u.status()).toBe(404);
  // chính Chi Mai xem được
  expect((await getSub(request, mai, bn02.id)).code).toBe(bn02.code);
});

test('Gửi lại cùng khóa lũy đẳng không tạo bài trùng @TC-S5E4-46', async ({ request }) => {
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  const bn01 = await submitOk(request, an, bc.id, [P1, P2], 'k-01');
  expect(bn01.code).toMatch(/^BN-\d{2}$/);
  expect(bn01.message).toMatch(MSG.s33);
  // gửi lại cùng khóa k-01 (máy tự gửi lại sau khi mất phản hồi)
  const again = await submitFiles(request, an, bc.id, [P1, P2], 'k-01');
  expect(again.status(), await again.text()).toBe(200);
  const body = (await again.json()) as Submission;
  expect(body.id).toBe(bn01.id);
  expect(body.version).toBe(1);
  expect(await count(request, w.orgId, bc.id, an.id)).toBe(1);
});

test('Nộp muộn có nhãn khi bài cho nộp muộn @TC-S5E4-47', async ({ request }) => {
  const { w, bc } = await setup(request);
  const my = w.people['Hà My']!;
  await ack(request, my);
  // giờ máy chủ sau hạn 6 phút 40 giây (nộp trong vòng 20 giây → "Nộp muộn 7 phút", làm tròn lên phút)
  await due(request, w.orgId, bc.id, -400, true);
  const bn05 = await submitOk(request, my, bc.id, [P1]);
  expect(bn05.status).toBe('submitted');
  expect(bn05.statusLabel).toBe('Đã nộp');
  expect(bn05.late).toBe(true);
  expect(bn05.lateLabel).toBe('Nộp muộn 7 phút');
  // trước hạn thì không có nhãn
  const an = w.people['Bình An']!;
  await ack(request, an);
  await due(request, w.orgId, bc.id, 3600, true);
  const ontime = await submitOk(request, an, bc.id, [P1]);
  expect(ontime.late).toBe(false);
  expect(ontime.lateLabel).toBeUndefined();
});

test('Nộp lại trước hạn tạo phiên bản mới, bản cũ giữ @TC-S5E4-48', async ({ request }) => {
  test.setTimeout(90_000);
  const { w, lan, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  const v1 = await submitOk(request, an, bc.id, [P1, P2]);
  const page15 = Buffer.concat([P2, Buffer.alloc(1_500_000 - P2.length, 0)]);
  const r = await submitFiles(request, an, bc.id, [page15, page15, page15]);
  expect(r.status(), await r.text()).toBe(201);
  const v2 = (await r.json()) as Submission;
  expect(v2.id).toBe(v1.id);
  expect(v2.version).toBe(2);
  expect(v2.pages).toHaveLength(3);
  // giáo viên: phiên bản 2 là bản chấm, phiên bản 1 vẫn đọc được
  const t = await getSub(request, lan, v1.id);
  expect(t.version).toBe(2);
  expect(t.versions!.map((v) => v.version)).toEqual([1, 2]);
  const old = t.versions!.find((v) => v.version === 1)!;
  expect(old.pages.map((p) => p.sha256)).toEqual([sha256(P1), sha256(P2)]);
  // ảnh của phiên bản 1 không bị xoá
  for (const n of [1, 2]) {
    const u = await request.get(pageUrl(old.versionId, n), { headers: bearer(lan.token) });
    expect(u.status(), await u.text()).toBe(200);
    const img = await request.get(`${API}${((await u.json()) as { url: string }).url}`);
    expect(img.status()).toBe(200);
    expect(sha256(Buffer.from(await img.body()))).toBe(old.pages[n - 1]!.sha256);
  }
  expect(await count(request, w.orgId, bc.id, an.id)).toBe(1);
});

test('Quá hạn khi bài không cho nộp muộn @TC-S5E4-50', async ({ request }) => {
  const { w, bc } = await setup(request);
  const bao = w.people['Gia Bảo']!;
  await ack(request, bao);
  // giờ máy chủ sau hạn 1 giây, bài không cho nộp muộn
  await due(request, w.orgId, bc.id, -1, false);
  const r = await submitFiles(request, bao, bc.id, [P1]);
  expect(r.status()).toBe(409);
  const err = (await r.json()) as { code: string; message: string };
  expect(err.code).toBe('CONFLICT');
  expect(err.message).toBe(MSG.s34);
  expect(await count(request, w.orgId, bc.id, bao.id)).toBe(0);
});

test('45 em nộp dồn trong 5 phút @TC-S5E4-51', async ({ request }) => {
  test.setTimeout(300_000);
  const names = Array.from({ length: 45 }, (_, i) => `HS thử ${String(i + 1).padStart(2, '0')}`);
  const w = await world(request, { teachers: ['Cô Lan'], students: [], classes: [{ key: 'T45', teacher: 'Cô Lan', name: '11T', active: names }] });
  const lan = w.people['Cô Lan']!;
  const rb = await rb01(request, lan);
  const bc = await bc01(request, lan, w.classes['T45']!.id, rb.id);
  const page2mb = Buffer.concat([P1, Buffer.alloc(2 * 1024 * 1024 - P1.length, 0)]);
  const times: number[] = [];
  const statuses: number[] = [];
  const started = Date.now();
  const one = async (p: Person) => {
    await ack(request, p);
    const pages = [];
    for (let i = 0; i < 3; i++) {
      const t0 = Date.now();
      pages.push({ uploadId: await upload(request, p, page2mb) });
      times.push(Date.now() - t0);
    }
    const r = await submitRaw(request, p, bc.id, pages);
    statuses.push(r.status());
  };
  // 45 em đến rải trong 60 giây (kịch bản đặc tả rải trong 5 phút; dồn gấp 5 lần để test ngắn)
  await Promise.all(names.map((n, i) => new Promise((r) => setTimeout(r, (i * 60_000) / names.length)).then(() => one(w.people[n]!))));
  const elapsed = Date.now() - started;
  times.sort((a, b) => a - b);
  const p95 = times[Math.ceil(times.length * 0.95) - 1]!;
  test.info().annotations.push({ type: 'tải', description: `45 em × 3 trang 2 MB: ${elapsed} ms, p95 một trang ${p95} ms` });
  expect(elapsed).toBeLessThan(300_000);
  expect(p95).toBeLessThanOrEqual(3_000);
  expect(statuses.filter((s) => s >= 500)).toHaveLength(0);
  expect(statuses.every((s) => s === 201)).toBe(true);
  expect(await count(request, w.orgId, bc.id)).toBe(45);
  const t = await getSub(request, lan, ((await (await request.get(`${SUB}/mine?assignmentId=${bc.id}`, { headers: bearer(w.people[names[0]!]!.token) })).json()) as { submission: Submission }).submission.id);
  expect(t.pages).toHaveLength(3);
});
