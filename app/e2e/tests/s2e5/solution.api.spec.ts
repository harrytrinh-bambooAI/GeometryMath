import { expect, test } from '../../support/fixtures.ts';
import { AN, API, LAN, MSG, RIGHT, S1, S2, S3_RIGHT, S3_V1, S4_RIGHT, S4_V1, TRANG, TRANG_CODE, WRONG_V2, apiApprove, apiCheck, apiSave, apiView, bearer, docId, rawApprove, rawSave, rawView, token } from './s2e5.ts';

/** S2-E5 — API: giới hạn bước, khóa lạc quan, quyền sở hữu, StepCheck không tới học sinh, nhãn máy, duyệt (US-S2E5-01…04). */

type Err = { code: string; message: string; requestId: string };

test('@TC-S2E5-009 bước 2001 ký tự bị chặn VALIDATION_FAILED, không tạo phiên bản', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(9, info);
  const r = await rawSave(request, t, d, 1, [S1, 'x'.repeat(2001), S3_V1, S4_V1]);
  expect(r.status()).toBe(400);
  const body = (await r.json()) as Err;
  expect(body.code).toBe('VALIDATION_FAILED');
  expect(body.message).toBe('Bước tối đa 2000 ký tự');
  expect((await apiView(request, t, d)).latestVersion).toBe(1);
  // đúng 2000 ký tự thì lưu được
  expect((await apiSave(request, t, d, 1, [S1, 'x'.repeat(2000), S3_V1, S4_V1])).latestVersion).toBe(2);
});

test('@TC-S2E5-010 lời giải 30 bước lưu được, 31 bước bị chặn', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(10, info);
  const steps = (n: number) => Array.from({ length: n }, (_, i) => `Bước ${i + 1}: ${i + 1} + 1 = ${i + 2}`);
  const v2 = await apiSave(request, t, d, 1, steps(30));
  expect(v2.version.steps).toHaveLength(30);
  expect(v2.version.steps.map((s) => s.no)).toEqual(Array.from({ length: 30 }, (_, i) => i + 1));
  const r = await rawSave(request, t, d, 2, steps(31));
  expect(r.status()).toBe(400);
  const body = (await r.json()) as Err;
  expect(body.code).toBe('VALIDATION_FAILED');
  expect(body.message).toBe('Tối đa 30 bước');
  expect((await apiView(request, t, d)).latestVersion).toBe(2);
  // 0 bước và bước trống cũng bị chặn
  expect((await rawSave(request, t, d, 2, [])).status()).toBe(400);
  const blank = await rawSave(request, t, d, 2, [S1, '   ']);
  expect(((await blank.json()) as Err).message).toBe('Bước 2 đang trống');
});

test('@TC-S2E5-012 hai tab sửa cùng lúc: tab lưu sau nhận CONFLICT, v2 nguyên vẹn', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(12, info);
  const a = await apiSave(request, t, d, 1, [S1, S2, S3_RIGHT, S4_V1]); // tab A: sửa bước 3 thành v2
  expect(a.version.version).toBe(2);
  const b = await rawSave(request, t, d, 1, [S1, S2, S3_V1, S4_RIGHT]); // tab B vẫn từ v1: sửa bước 4
  expect(b.status()).toBe(409);
  const body = (await b.json()) as Err;
  expect(body.code).toBe('CONFLICT');
  expect(body.message).toBe('Có bản mới hơn (v2)');
  const now = await apiView(request, t, d);
  expect(now.latestVersion).toBe(2);
  expect(now.version.steps.map((s) => s.text)).toEqual([S1, S2, S3_RIGHT, S4_V1]);
});

test('@TC-S2E5-013 giáo viên khác không sửa được lời giải: NOT_FOUND, không lộ đề', async ({ request }, info) => {
  const lan = await token(request, LAN);
  const d = docId(13, info);
  const missing = '00000000-0000-7000-8002-000829999999';
  const r = await rawSave(request, lan, d, 1, RIGHT);
  expect(r.status()).toBe(404);
  const body = (await r.json()) as Err;
  expect(body.code).toBe('NOT_FOUND');
  // cùng câu trả lời với một mã không tồn tại: không lộ đề có tồn tại
  const none = (await (await rawSave(request, lan, missing, 1, RIGHT)).json()) as Err;
  expect({ code: none.code, message: none.message }).toEqual({ code: body.code, message: body.message });
  expect((await rawView(request, lan, d)).status()).toBe(404);
  const list = await request.get(`${API}/api/v1/teacher/solutions`, { headers: bearer(lan) });
  expect(((await list.json()) as { items: unknown[] }).items).toEqual([]);
  expect((await apiView(request, await token(request, TRANG), d)).latestVersion).toBe(1);
});

test('@TC-S2E5-014 học sinh không sửa được lời giải', async ({ request }, info) => {
  const an = await token(request, AN);
  const d = docId(14, info);
  const r = await rawSave(request, an, d, 1, RIGHT);
  expect([403, 404]).toContain(r.status());
  expect(['FORBIDDEN', 'NOT_FOUND']).toContain(((await r.json()) as Err).code);
  expect((await apiView(request, await token(request, TRANG), d)).latestVersion).toBe(1);
});

test('@TC-S2E5-026 học sinh không đọc được StepCheck', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const an = await token(request, AN);
  const d = docId(26, info);
  await apiCheck(request, t, d, 1); // đã có kết quả kiểm
  const urls = [
    `/api/v1/teacher/solutions/${d}/step-checks`,
    `/api/v1/teacher/solutions/${d}/step-checks?version=1`,
    `/api/v1/teacher/solutions/${d}`,
    `/api/v1/teacher/solutions`,
  ];
  for (const u of urls) {
    const r = await request.get(`${API}${u}`, { headers: bearer(an) });
    expect([403, 404], u).toContain(r.status());
    const text = await r.text();
    expect(text, u).not.toMatch(/StepCheck|checkLabel|máy tính lại|Lệch|tan SCA/);
    expect(r.headers()['cache-control'], u).toContain('no-store');
  }
  const post = await request.post(`${API}/api/v1/teacher/solutions/${d}/check`, { headers: bearer(an), data: { version: 1 } });
  expect([403, 404]).toContain(post.status());
  // giáo viên chủ đề thì đọc được
  const mine = await request.get(`${API}/api/v1/teacher/solutions/${d}/step-checks`, { headers: bearer(t) });
  expect(mine.status()).toBe(200);
  expect(((await mine.json()) as { items: Array<{ check: string }> }).items.map((s) => s.check)).toEqual(['unverifiable', 'pass', 'fail', 'pass']);
  expect(mine.headers()['cache-control']).toContain('no-store');
});

test('@TC-S2E5-031 không thao tác nào đặt được nhãn Đã kiểm chứng từ phía người dùng', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(31, info);
  const approved = await apiApprove(request, t, d, 1, true); // nhãn máy "Cần xem lại", Cô Trang đã duyệt
  expect(approved.version.humanLabel).toBe(MSG.approved);
  expect(approved.version.machineLabel).toBe(MSG.needsReview);
  const tries = [
    await rawApprove(request, t, d, 1, true, { machineLabel: MSG.verified }),
    await rawApprove(request, t, d, 1, true, { machineStatus: 'verified' }),
    await rawSave(request, t, d, 1, RIGHT, { verifyStatus: 'verified' }),
    await request.post(`${API}/api/v1/teacher/solutions/${d}/check`, { headers: bearer(t), data: { version: 1, machineLabel: MSG.verified } }),
  ];
  for (const r of tries) {
    expect([400, 403]).toContain(r.status());
    expect(['VALIDATION_FAILED', 'FORBIDDEN']).toContain(((await r.json()) as Err).code);
  }
  for (const method of ['put', 'patch'] as const) {
    const r = await request[method](`${API}/api/v1/teacher/solutions/${d}`, { headers: bearer(t), data: { machineLabel: MSG.verified } });
    expect(r.status()).toBeGreaterThanOrEqual(400);
    expect(r.status()).toBeLessThan(500);
  }
  const now = await apiView(request, t, d);
  expect(now.latestVersion).toBe(1);
  expect(now.version.machineLabel).toBe(MSG.needsReview);
  expect(now.version.humanLabel).toBe(MSG.approved);
});

test('@TC-S2E5-042 còn một bước lệch thì API duyệt trả CONFLICT, không có bản ghi duyệt', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(42, info);
  await apiSave(request, t, d, 1, WRONG_V2);
  const checked = await apiCheck(request, t, d, 2);
  expect(checked.version.failCount).toBe(1);
  expect(checked.approve).toMatchObject({ allowed: false, approved: false, blockedReason: 'Chặn duyệt: còn 1 bước lệch' });
  const r = await rawApprove(request, t, d, 2, true);
  expect(r.status()).toBe(409);
  const body = (await r.json()) as Err;
  expect(body.code).toBe('CONFLICT');
  expect(body.message).toBe('Chặn duyệt: còn 1 bước lệch');
  const now = await apiView(request, t, d);
  expect(now.version.humanLabel).toBe(MSG.editing);
  expect(now.version.approvedAt ?? null).toBeNull();
  expect(now.inMyShelf).toBe(false);
});

test('@TC-S2E5-048 bấm Duyệt hai lần chỉ ghi một lần, lần hai không lỗi', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(48, info);
  await apiSave(request, t, d, 1, RIGHT);
  expect((await apiCheck(request, t, d, 2)).version.machineLabel).toBe(MSG.verified);
  const [a, b] = await Promise.all([rawApprove(request, t, d, 2, false), rawApprove(request, t, d, 2, false)]);
  expect([a.status(), b.status()]).toEqual([200, 200]);
  const [x, y] = [(await a.json()) as Awaited<ReturnType<typeof apiView>>, (await b.json()) as Awaited<ReturnType<typeof apiView>>];
  expect(x.version.approvedAt).toBeTruthy();
  expect(y.version.approvedAt).toBe(x.version.approvedAt); // cùng một bản ghi duyệt
  expect(y.version.approvedCode).toBe(TRANG_CODE);
  const third = await apiApprove(request, t, d, 2, false);
  expect(third.version.approvedAt).toBe(x.version.approvedAt);
  expect(third.history.filter((h) => h.kind === 'version')).toHaveLength(2);
});

test('@TC-S2E5-049 duyệt phiên bản đã bị thay thế nhận CONFLICT "Phiên bản đã cũ"', async ({ request }, info) => {
  const t = await token(request, TRANG);
  const d = docId(49, info);
  await apiSave(request, t, d, 1, RIGHT);
  await apiApprove(request, t, d, 2, true); // v2 đã duyệt
  await apiSave(request, t, d, 2, [S1, S2, S3_RIGHT, 'Vậy góc giữa SC và mặt đáy bằng 45°.']); // v3 hiện hành, chưa duyệt
  const r = await rawApprove(request, t, d, 2, true);
  expect(r.status()).toBe(409);
  const body = (await r.json()) as Err;
  expect(body.code).toBe('CONFLICT');
  expect(body.message).toBe('Phiên bản đã cũ');
  expect((await apiView(request, t, d, 2)).version.humanLabel).toBe(MSG.approved);
  expect((await apiView(request, t, d, 3)).version.humanLabel).toBe(MSG.editing);
});

test('@TC-S2E5-051 giáo viên khác không duyệt được đề của người khác', async ({ request }, info) => {
  const d = docId(51, info);
  const r = await rawApprove(request, await token(request, LAN), d, 1, true);
  expect(r.status()).toBe(404);
  expect(((await r.json()) as Err).code).toBe('NOT_FOUND');
  const now = await apiView(request, await token(request, TRANG), d);
  expect(now.version.humanLabel).toBe(MSG.ai);
  expect(now.version.approvedAt ?? null).toBeNull();
});
