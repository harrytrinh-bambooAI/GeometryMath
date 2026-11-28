import type { APIRequestContext } from '@playwright/test';
import { expect, test } from '../../../support/fixtures.ts';
import { API, PASSWORD, bearer, hook } from '../../s3e1/s3e1.ts';
import { deleteAccount } from '../../s3e5/account.ts';
import { BC01_TITLE } from '../s5e4.ts';
import { ack, count, submitFiles, submitOk, P1, P2 } from '../submit/submit.ts';
import { approve, gradingWorld, hookPost, MSG, notes, ok, queue, result, returnAll, solution, undo, type Ctx, type Queue } from '../review/grading.ts';

/**
 * US-S5E4-07 — Trả bài (T-261128-60): chỉ bài Đã duyệt sang Đã trả; trước khi trả học sinh không thấy điểm hay dữ liệu
 * AI; lời giải chỉ mở cho em có bài Đã trả; Trả bài đóng nhận bài mới; thu hồi trong 15 phút; sửa điểm bài đã trả báo
 * học sinh; giờ công bố cố định theo đồng hồ CSDL. Thêm: xoá tài khoản giữ điểm duyệt ẩn danh (TC-S5E4-43, BR-S5E4-28).
 */

interface Res {
  status: string;
  statusLabel: string;
  message: string;
  score?: number;
  comment?: string;
  criteria?: Array<{ name: string; points: number; maxPoints: number }>;
  solution?: { status: string; finalAnswer?: string };
}

/** BC-01: BN-01 Bình An Đã duyệt 10 ("Làm tốt"), BN-02 Chi Mai Đã duyệt 7, BN-03 Đức Huy Chờ chấm; Gia Bảo chưa nộp. */
async function prep(rq: APIRequestContext) {
  const c = await gradingWorld(rq);
  const sub = async (name: string, files: Buffer[]) => {
    const p = c.w.people[name]!;
    await ack(rq, p);
    return submitOk(rq, p, c.bc.id, files);
  };
  const bn01 = await sub('Bình An', [P1, P2]);
  const bn02 = await sub('Chi Mai', [P1, P2, P2]);
  const bn03 = await sub('Đức Huy', [P1, P2]);
  await approve(rq, c.lan, bn01.id, [2, 4, 2, 1, 1], 'Làm tốt');
  await approve(rq, c.lan, bn02.id, [2, 3, 0, 1, 1], 'Xem lại cách tính SB');
  return { c, bn01, bn02, bn03, an: c.w.people['Bình An']!, mai: c.w.people['Chi Mai']!, huy: c.w.people['Đức Huy']!, bao: c.w.people['Gia Bảo']! };
}

test('Trả bài cho cả lớp: bài Đã duyệt sang Đã trả, học sinh xem điểm, nhận xét, lời giải, có thông báo @TC-S5E4-32', async ({ request }) => {
  const { c, bn01, bn02, bn03, an } = await prep(request);
  const r = await ok<{ returned: number; status: string }>(await returnAll(request, c.lan, c.bc.id));
  expect(r.returned).toBe(2);
  expect(r.status).toBe('returned');
  const q = await ok<Queue>(await queue(request, c.lan, { assignmentId: c.bc.id }));
  expect(q.items.map((i) => i.submissionId)).toEqual([bn03.id]);
  expect(q.items[0]!.statusLabel).toMatch(/Chờ chấm|Đang xử lý/);
  expect(q.summary).toMatchObject({ returned: 2, approved: 0 });
  const x = await ok<Res>(await result(request, an, bn01.id));
  expect(x.status).toBe('returned');
  expect(x.criteria).toHaveLength(5);
  expect(x.score).toBe(10);
  expect(x.comment).toBe('Làm tốt');
  expect(x.solution?.status).toBe('OK');
  expect(x.solution?.finalAnswer).toBeTruthy();
  const n = await notes(request, an);
  expect(n.map((i) => i.body)).toContain(MSG.s39(BC01_TITLE));
  expect(n.find((i) => i.body === MSG.s39(BC01_TITLE))!.kind).toBe('graded_returned');
  expect(bn02.id).toBeTruthy();
});

test('Lịch công bố giờ cố định mở đúng ranh giới theo đồng hồ CSDL @TC-S5E4-33', async ({ request }) => {
  const { c, bn01, an } = await prep(request);
  // R = giờ CSDL + 4 giây; gọi trước R: chỉ trạng thái
  await hookPost(request, 'release-in', { orgId: c.w.orgId, assignmentId: c.bc.id, seconds: 4 });
  const before = await result(request, an, bn01.id);
  const text = await before.text();
  expect(before.status(), text).toBe(200);
  const b = JSON.parse(text) as Res;
  expect(b.message).toBe(MSG.waitReturn);
  expect(text).not.toContain('"score"');
  expect(text).not.toContain('"criteria"');
  expect(((await ok<{ status: string }>(await solution(request, an, c.bc.id))).status)).toBe('LOCKED');
  // tại / sau R: có điểm và lời giải
  await expect
    .poll(async () => ((await (await result(request, an, bn01.id)).json()) as Res).status, { timeout: 15_000, intervals: [500, 1000] })
    .toBe('returned');
  const after = await ok<Res>(await result(request, an, bn01.id));
  expect(after.score).toBe(10);
  expect(after.solution?.status).toBe('OK');
});

test('Trước khi trả, phản hồi không chứa điểm hay dữ liệu AI, có no-store @TC-S5E4-34', async ({ request }) => {
  const { bn01, an } = await prep(request);
  for (const path of [`${API}/api/v1/submissions/${bn01.id}`, `${API}/api/v1/submissions/${bn01.id}/result`]) {
    const r = await request.get(path, { headers: bearer(an.token) });
    const text = await r.text();
    expect(r.status(), text).toBe(200);
    for (const k of ['score', 'criteria', 'proposal', 'confidence', 'ocr']) expect(text).not.toContain(`"${k}"`);
    expect(r.headers()['cache-control']).toContain('no-store');
  }
  const x = await ok<Res>(await result(request, an, bn01.id));
  expect(x.message).toBe(MSG.waitReturn);
});

test('Học sinh không xem được điểm của bạn sau khi trả: NOT_FOUND @TC-S5E4-35', async ({ request }) => {
  const { c, bn02, an } = await prep(request);
  await ok(await returnAll(request, c.lan, c.bc.id));
  const r = await result(request, an, bn02.id);
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
});

test('Em chưa nộp hoặc bài Chờ chấm không thấy lời giải sau khi lớp được trả bài @TC-S5E4-36', async ({ request }) => {
  const { c, bn01, an, huy, bao } = await prep(request);
  await ok(await returnAll(request, c.lan, c.bc.id));
  const open = await ok<Res>(await result(request, an, bn01.id));
  const answer = open.solution!.finalAnswer!;
  expect(answer).toBeTruthy();
  for (const p of [bao, huy]) {
    const r = await solution(request, p, c.bc.id);
    const text = await r.text();
    expect(r.status(), text).toBe(200);
    const body = JSON.parse(text) as Record<string, unknown>;
    expect(body.status).toBe('LOCKED');
    // chỉ trạng thái, thông điệp, mã và tên bài: không có nội dung lời giải hay đáp số
    expect(Object.keys(body).sort()).toEqual(['assignmentId', 'message', 'status', 'title']);
    if (answer.length >= 3) expect(text).not.toContain(answer);
    expect(text).not.toContain('finalAnswer');
    expect(text).not.toContain('steps');
  }
});

test('Sau khi trả bài, nộp mới bị từ chối dù bài cho nộp muộn @TC-S5E4-37', async ({ request }) => {
  const { c, bao } = await prep(request);
  await ok(await returnAll(request, c.lan, c.bc.id));
  await ack(request, bao);
  const r = await submitFiles(request, bao, c.bc.id, [P1]);
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { code: string }).code).toBe('CONFLICT');
  expect(await count(request, c.w.orgId, c.bc.id, bao.id)).toBe(0);
});

test('Thu hồi Trả bài trong 15 phút; quá 15 phút thì CONFLICT @TC-S5E4-38', async ({ request }) => {
  const { c, bn01, an } = await prep(request);
  await ok(await returnAll(request, c.lan, c.bc.id));
  expect((await notes(request, an)).map((i) => i.body)).toContain(MSG.s39(BC01_TITLE));
  // T0 + 10 phút
  await hookPost(request, 'age', { orgId: c.w.orgId, assignmentId: c.bc.id, seconds: 600 });
  const u = await ok<{ undone: number; status: string }>(await undo(request, c.lan, c.bc.id));
  expect(u.undone).toBe(2);
  expect(u.status).toBe('assigned');
  const x = await ok<Res>(await result(request, an, bn01.id));
  expect(x.status).toBe('approved');
  expect(x.message).toBe(MSG.waitReturn);
  expect(x.score).toBeUndefined();
  expect((await ok<{ status: string }>(await solution(request, an, c.bc.id))).status).toBe('LOCKED');
  expect((await notes(request, an)).map((i) => i.body)).not.toContain(MSG.s39(BC01_TITLE));
  // trả lại, rồi T0 + 16 phút: thu hồi bị từ chối
  await ok(await returnAll(request, c.lan, c.bc.id));
  await hookPost(request, 'age', { orgId: c.w.orgId, assignmentId: c.bc.id, seconds: 960 });
  const late = await undo(request, c.lan, c.bc.id);
  expect(late.status()).toBe(409);
  expect(((await late.json()) as { code: string }).code).toBe('CONFLICT');
  expect((await ok<Res>(await result(request, an, bn01.id))).status).toBe('returned');
});

test('Sửa điểm bài đã trả thì học sinh được báo, kết quả hiện tổng mới @TC-S5E4-39', async ({ request }) => {
  const { c, bn02, mai } = await prep(request);
  await ok(await returnAll(request, c.lan, c.bc.id));
  expect((await ok<Res>(await result(request, mai, bn02.id))).score).toBe(7);
  await approve(request, c.lan, bn02.id, [2, 3.5, 0, 1, 1], 'Xem lại cách tính SB');
  const n = await notes(request, mai);
  expect(n.map((i) => i.body)).toContain(MSG.updated(BC01_TITLE));
  const x = await ok<Res>(await result(request, mai, bn02.id));
  expect(x.status).toBe('returned');
  expect(x.score).toBe(7.5);
});

test('Xoá tài khoản học sinh: điểm duyệt còn nhưng không gắn học sinh (dữ liệu bài nộp luồng thật) @TC-S5E4-43', async ({ request }) => {
  const { c, bn01, an } = await prep(request);
  const before = await gradesOf(request, c, bn01.id);
  expect(before).toEqual({ grades: 1, withStudent: 1 });
  const d = await deleteAccount(request, an.token, an.id, { password: PASSWORD, confirm: 'XÓA' });
  expect(d.status(), await d.text()).toBe(200);
  const out = (await hook(request, 's5e4/purge-accounts', { assignmentId: c.bc.id })) as { images: number; rows: number };
  expect(out.images).toBe(2);
  expect(await gradesOf(request, c, bn01.id)).toEqual({ grades: 1, withStudent: 0 });
});

async function gradesOf(rq: APIRequestContext, c: Ctx, submissionId: string) {
  const r = await rq.get(`${API}/test-hooks/s5e4/return/grades?${new URLSearchParams({ orgId: c.w.orgId, submissionId })}`);
  expect(r.status()).toBe(200);
  return (await r.json()) as { grades: number; withStudent: number };
}
