import { expect, test } from '../../../support/fixtures.ts';
import { API, bearer, hook } from '../../s3e1/s3e1.ts';
import {
  MSG25,
  REFUSED,
  feedbackUrl,
  historySetup,
  list,
  mineUrl,
  qaUrl,
  reportUrl,
  seed,
  summaryCount,
  type QaPage,
  type Report,
} from './history.ts';

/**
 * US-S5E3-06 — giáo viên xem lịch sử hỏi đáp theo bài, báo "chặn sai" / "lẽ ra phải chặn" (máy chủ thật). Không cột
 * xếp hạng hay đếm theo học sinh (PO-04); lớp khác NOT_FOUND; học sinh chỉ thấy lượt của chính mình.
 */

test('Lọc các lượt bị chặn của bài BC-01 @TC-S5E3-38', async ({ request }) => {
  const { w, lan, an, mai, c11, bc } = await historySetup(request);
  await seed(request, w.orgId, c11.id, bc.id, [
    { studentId: an.id, sourceType: 'kb' },
    { studentId: mai.id, sourceType: 'kb' },
    { studentId: an.id, sourceType: 'lecture' },
    { studentId: mai.id, sourceType: null },
    {
      studentId: mai.id,
      decision: 'blocked',
      rule: 'KS-1',
      question: 'Đáp số cuối cùng là bao nhiêu ạ?',
    },
    {
      studentId: an.id,
      decision: 'blocked',
      rule: 'KS-4',
      question: 'Kết quả có phải a√6/3 không ạ?',
    },
  ]);
  expect((await list(request, lan, bc.id)).items).toHaveLength(6);
  const p = await list(request, lan, bc.id, '?decision=blocked');
  expect(p.items).toHaveLength(2);
  expect(p.items.map((i) => i.rule).sort()).toEqual(['KS-1', 'KS-4']);
  for (const row of p.items) {
    expect(row.decision).toBe('blocked');
    expect(row.question.length).toBeGreaterThan(0);
    expect(row.reply).toBe(REFUSED);
    expect(row.replyKind).toBe('refused_answer');
    expect(['Bình An', 'Chi Mai']).toContain(row.studentName);
    // không có cột xếp hạng hay đếm theo học sinh
    // máy chủ bỏ khóa null (nguồn, đánh dấu chưa có)
    expect(Object.keys(row).sort()).toEqual(
      ['at', 'decision', 'id', 'question', 'reply', 'replyKind', 'rule', 'studentName'].sort(),
    );
  }
  expect(Object.keys(p).filter((k) => k !== 'nextCursor')).toEqual(['items']);
  // quyết định lạ bị từ chối
  const bad = await request.get(`${qaUrl(bc.id)}?decision=rank`, {
    headers: bearer(lan.token),
  });
  expect(bad.status()).toBe(400);
});

test('Báo "chặn sai" một lượt @TC-S5E3-39', async ({ request }) => {
  const { w, lan, khoi, an, c11, bc } = await historySetup(request);
  const [id] = await seed(request, w.orgId, c11.id, bc.id, [
    {
      studentId: an.id,
      decision: 'blocked',
      rule: 'KS-4',
      question: 'a√6/3 lớn hơn 1 hay nhỏ hơn 1 ạ?',
    },
  ]);
  // người vận hành: tài khoản được cấp vai Vận hành (móc S5-E5)
  await hook(request, 's5e5/operator', { userId: khoi.id });
  const before = await summaryCount(request, khoi, w.orgId, 'KS-4', 'false_block');
  const replyUrl = `${API}/test-hooks/s5e3/history/reply?orgId=${w.orgId}&interactionId=${id}`;
  const sent = ((await (await request.get(replyUrl)).json()) as { reply: string }).reply;
  const r = await request.post(feedbackUrl(id!), {
    headers: bearer(lan.token),
    data: {
      type: 'false_block',
      note: 'Em hỏi so sánh với 1, không phải đáp số',
    },
  });
  expect(r.status(), await r.text()).toBe(200);
  const f = (await r.json()) as { message: string; type: string; rule: string };
  expect(f.message).toBe(MSG25);
  expect(f).toMatchObject({ type: 'false_block', rule: 'KS-4' });
  expect(await summaryCount(request, khoi, w.orgId, 'KS-4', 'false_block')).toBe(before + 1);
  // câu trả lời đã gửi không đổi
  expect(((await (await request.get(replyUrl)).json()) as { reply: string }).reply).toBe(sent);
  // mỗi lượt một đánh dấu, sửa được: gửi lại cùng loại không đếm thêm; ghi chú quá 200 ký tự bị từ chối
  const again = await request.post(feedbackUrl(id!), {
    headers: bearer(lan.token),
    data: { type: 'false_block', note: 'Sửa ghi chú' },
  });
  expect(again.status()).toBe(200);
  expect(await summaryCount(request, khoi, w.orgId, 'KS-4', 'false_block')).toBe(before + 1);
  const long = await request.post(feedbackUrl(id!), {
    headers: bearer(lan.token),
    data: { type: 'false_block', note: 'x'.repeat(201) },
  });
  expect(long.status()).toBe(400);
  const row = (await list(request, lan, bc.id)).items[0]!;
  expect(row.feedback).toEqual({
    type: 'false_block',
    typeLabel: 'Chặn sai',
    note: 'Sửa ghi chú',
  });
  // giáo viên không có vai Vận hành không xem được tổng hợp
  expect(
    (
      await request.get(`${API}/api/v1/admin/ai/qa-feedback`, {
        headers: bearer(lan.token),
      })
    ).status(),
  ).toBe(403);
});

test('Thầy Khôi không xem được lịch sử của BC-01 @TC-S5E3-40', async ({ request }) => {
  const { w, khoi, an, c11, bc } = await historySetup(request);
  const [id] = await seed(request, w.orgId, c11.id, bc.id, [{ studentId: an.id, decision: 'blocked', rule: 'KS-1' }]);
  const r = await request.get(qaUrl(bc.id), { headers: bearer(khoi.token) });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  expect((await request.get(reportUrl(bc.id), { headers: bearer(khoi.token) })).status()).toBe(404);
  const fb = await request.post(feedbackUrl(id!), {
    headers: bearer(khoi.token),
    data: { type: 'false_block' },
  });
  expect(fb.status()).toBe(404);
});

test('Học sinh chỉ thấy lượt hỏi của chính mình @TC-S5E3-41', async ({ request }) => {
  const { w, an, mai, c11, bc } = await historySetup(request);
  await seed(request, w.orgId, c11.id, bc.id, [
    { studentId: an.id, sourceType: 'kb', question: 'Câu của Bình An 1' },
    { studentId: mai.id, sourceType: 'kb', question: 'Câu của Chi Mai' },
    {
      studentId: an.id,
      decision: 'blocked',
      rule: 'KS-1',
      question: 'Câu của Bình An 2',
    },
  ]);
  const r = await request.get(mineUrl(bc.id), { headers: bearer(an.token) });
  expect(r.status(), await r.text()).toBe(200);
  const p = (await r.json()) as QaPage;
  expect(p.items.map((i) => i.question).sort()).toEqual(['Câu của Bình An 1', 'Câu của Bình An 2']);
  for (const row of p.items) {
    expect(row.studentName).toBeUndefined();
    expect(row.rule).toBeUndefined();
  }
  const t = await request.get(qaUrl(bc.id), { headers: bearer(an.token) });
  expect(t.status()).toBe(403);
  expect(((await t.json()) as { code: string }).code).toBe('FORBIDDEN');
});

test('Báo cáo tỉ lệ câu trả lời theo loại nguồn @TC-S5E3-42', async ({ request }) => {
  const { w, lan, an, mai, c11, bc } = await historySetup(request);
  await seed(request, w.orgId, c11.id, bc.id, [
    ...Array.from({ length: 6 }, (_, i) => ({
      studentId: i % 2 ? an.id : mai.id,
      sourceType: 'kb' as const,
    })),
    { studentId: an.id, sourceType: 'lecture' },
    { studentId: mai.id, sourceType: null },
    { studentId: an.id, decision: 'blocked', rule: 'KS-1' },
    { studentId: mai.id, decision: 'blocked', rule: 'KS-4' },
  ]);
  const r = await request.get(reportUrl(bc.id), { headers: bearer(lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  const text = await r.text();
  const rep = JSON.parse(text) as Report;
  expect(rep.n).toBe(10);
  expect(rep.teacherDocs).toMatchObject({
    label: 'Tài liệu của thầy cô',
    count: 6,
    percent: 60,
  });
  expect(rep.lectures).toMatchObject({
    label: 'Bài giảng mẫu',
    count: 1,
    percent: 10,
  });
  expect(rep.noSource).toMatchObject({ count: 1, percent: 10 });
  expect(rep.blocked).toMatchObject({ count: 2, percent: 20 });
  expect(rep.blockedByRule.map((x) => [x.rule, x.percent])).toEqual([
    ['KS-1', 10],
    ['KS-4', 10],
  ]);
  // không có tên học sinh
  expect(text).not.toContain('Bình An');
  expect(text).not.toContain('Chi Mai');
  expect(text).not.toContain(an.id);
});
