import { expect, test } from '../../support/fixtures.ts';
import { API, FX10, LAN, MSG, bearer, buildToEnd, createDraft, draftsUrl, jobUrl, jpeg, ok, teacher, tokenFor, upload, type Draft } from './s2e3.ts';

/** S2-E3 — API: độ dài đề, giới hạn thêm đề, ảnh (cỡ, định dạng theo nội dung), một tác vụ, quyền xem tiến trình, chặn duyệt. */

type Err = { code: string; message: string };

test('@TC-S2E3-006 đề 2001 ký tự bị chặn VALIDATION_FAILED, không tạo đề', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(6, info));
  const r = await createDraft(request, t, 'a'.repeat(2001));
  expect(r.status()).toBe(400);
  const body = (await r.json()) as Err;
  expect(body.code).toBe('VALIDATION_FAILED');
  expect(body.message).toBe(MSG.long);
  expect((await ok<{ items: unknown[] }>(await request.get(draftsUrl, { headers: bearer(t) }))).items).toHaveLength(0);
  // nháp đúng 2000 ký tự nhận; sửa thành 2001 ký tự bị chặn, nháp giữ nguyên
  const d = await ok<Draft>(await createDraft(request, t, 'a'.repeat(2000)));
  const put = await request.put(`${draftsUrl}/${d.id}`, { headers: bearer(t), data: { statement: 'b'.repeat(2001) } });
  expect(put.status()).toBe(400);
  expect((await ok<Draft>(await request.get(`${draftsUrl}/${d.id}`, { headers: bearer(t) }))).statement).toBe('a'.repeat(2000));
});

test('@TC-S2E3-009 quá 20 đề trong giờ: RATE_LIMITED kèm Retry-After, không tạo đề', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(9, info));
  for (let i = 0; i < 20; i++) {
    expect((await createDraft(request, t, `${FX10} (${i + 1})`)).status()).toBe(200);
  }
  const r = await createDraft(request, t, `${FX10} (21)`);
  expect(r.status()).toBe(429);
  expect((await r.json()) as Err).toMatchObject({ code: 'RATE_LIMITED', message: MSG.rate });
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  expect((await ok<{ items: unknown[] }>(await request.get(draftsUrl, { headers: bearer(t) }))).items).toHaveLength(20);
});

test('@TC-S2E3-016 ảnh đúng 5 242 880 byte được nhận và đọc chữ', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(16, info));
  const d = await ok<Draft>(await createDraft(request, t, '', 'image'));
  const img = jpeg('S2E3-CLEAR', 5_242_880);
  expect(img.length).toBe(5_242_880);
  const v = await ok<Draft>(await upload(request, t, d.id, img));
  expect(v.statement).toBe(FX10);
  expect(v.cells.every((c) => c.state === 'ok')).toBe(true);
});

test('@TC-S2E3-017 ảnh 5 242 881 byte bị chặn PAYLOAD_TOO_LARGE, không đọc chữ', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(17, info));
  const d = await ok<Draft>(await createDraft(request, t, '', 'image'));
  const r = await upload(request, t, d.id, jpeg('S2E3-CLEAR', 5_242_881));
  expect(r.status()).toBe(413);
  expect((await r.json()) as Err).toMatchObject({ code: 'PAYLOAD_TOO_LARGE', message: MSG.imageTooBig });
  const after = await ok<Draft>(await request.get(`${draftsUrl}/${d.id}`, { headers: bearer(t) }));
  expect(after.cells).toHaveLength(0);
  expect(after.statement).toBe('');
});

test('@TC-S2E3-018 ảnh GIF và PDF đổi đuôi .jpg bị nhận ra theo nội dung và từ chối', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(18, info));
  const d = await ok<Draft>(await createDraft(request, t, '', 'image'));
  for (const bytes of [Buffer.from('GIF89a\x01\x00\x01\x00GEO-S2E3:S2E3-CLEAR', 'latin1'), Buffer.from('%PDF-1.7\nGEO-S2E3:S2E3-CLEAR', 'latin1')]) {
    const r = await upload(request, t, d.id, bytes);
    expect(r.status()).toBe(400);
    expect((await r.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.badType });
  }
  expect((await ok<Draft>(await request.get(`${draftsUrl}/${d.id}`, { headers: bearer(t) }))).cells).toHaveLength(0);
});

test('@TC-S2E3-027 bấm Dựng hình hai lần trong 1 giây chỉ tạo một tác vụ, mỗi bước gọi AI một lần', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(27, info));
  const d = await ok<Draft>(await createDraft(request, t, FX10));
  const [a, b] = await Promise.all([
    request.post(`${draftsUrl}/${d.id}/build`, { headers: bearer(t), data: {} }),
    request.post(`${draftsUrl}/${d.id}/build`, { headers: bearer(t), data: {} }),
  ]);
  const ja = (await ok<Draft>(a)).job!.id;
  const jb = (await ok<Draft>(b)).job!.id;
  expect(ja).toBe(jb);
  await expect.poll(async () => (await ok<{ state: string }>(await request.get(jobUrl(ja), { headers: bearer(t) }))).state, { timeout: 20_000 }).toBe('done');
  const job = await ok<{ stages: Array<{ key: string; aiCalls: number }> }>(await request.get(jobUrl(ja), { headers: bearer(t) }));
  expect(job.stages.map((s) => s.aiCalls)).toEqual([1, 1, 1, 0]);
  const calls = await ok<unknown[]>(await request.get(`${API}/test-hooks/ai/calls?userId=${teacher(27, info)}&limit=20`));
  expect(calls).toHaveLength(3);
});

test('@TC-S2E3-029 giáo viên khác lấy tiến trình theo mã tác vụ: NOT_FOUND, không lộ dữ liệu', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(29, info));
  const d = await buildToEnd(request, t, FX10);
  const lan = await tokenFor(request, LAN);
  for (const url of [jobUrl(d.job!.id), `${draftsUrl}/${d.id}`]) {
    const r = await request.get(url, { headers: bearer(lan) });
    expect(r.status()).toBe(404);
    const body = await r.text();
    expect(body).not.toContain('Tam giác');
    expect(body).not.toContain('"stages"');
    expect(body).not.toContain('"statement"');
  }
  expect((await request.post(`${jobUrl(d.job!.id)}/cancel`, { headers: bearer(lan), data: {} })).status()).toBe(404);
});

test('@TC-S2E3-036 gọi thẳng API duyệt khi ba đáp số còn lệch: CONFLICT, không có nhãn Giáo viên đã duyệt', async ({ request }, info) => {
  const user = teacher(36, info);
  const t = await tokenFor(request, user);
  await request.post(`${API}/test-hooks/ai`, { data: { userId: user, scenario: 's2e3:answer-80' } });
  const d = await buildToEnd(request, t, `${FX10} Đáp số: 90°`);
  const doc = d.result!.docId!;
  expect(d.result!.answers).toMatchObject({ stated: '90°', ai: '80°', computed: '90°', state: 'mismatch' });
  const r = await request.post(`${API}/api/v1/teacher/solutions/${doc}/approve`, { headers: bearer(t), data: { version: 1, confirmNeedsReview: true } });
  expect(r.status()).toBe(409);
  expect(((await r.json()) as Err).code).toBe('CONFLICT');
  const view = await ok<{ version: { humanLabel: string }; approve: { approved: boolean } }>(
    await request.get(`${API}/api/v1/teacher/solutions/${doc}`, { headers: bearer(t) }),
  );
  expect(view.version.humanLabel).toBe(MSG.ai);
  expect(view.approve.approved).toBe(false);
  await request.post(`${API}/test-hooks/ai`, { data: { userId: user, scenario: '' } });
});
