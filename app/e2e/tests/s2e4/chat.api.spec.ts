import { expect, test } from '../../support/fixtures.ts';
import { API, FX10, MSG, bearer, chatUrl, newDraft, ok, say, teacher, tokenFor, type Chat } from './s2e4.ts';

/** S2-E4 — API chat chỉnh nháp (US-S2E4-01): độ dài tin 1…1000 ký tự, tối đa 30 lượt mỗi nháp (lượt 31 không gọi AI). */

type Err = { code: string; message: string };

test('@TC-S2E4-009 tin dài 1000 ký tự nhận, 1001 bị chặn, rỗng bị chặn', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(9, info));
  const d = await newDraft(request, t, FX10);
  const okSend = await ok<Chat>(await say(request, t, d.id, 'a'.repeat(1000)));
  expect(okSend.turns).toBe(1);
  expect(okSend.messages[0]!.text).toHaveLength(1000);

  const long = await say(request, t, d.id, 'a'.repeat(1001));
  expect(long.status()).toBe(400);
  expect((await long.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.tooLong });

  for (const empty of ['', '   ']) {
    const r = await say(request, t, d.id, empty);
    expect(r.status()).toBe(400);
    expect(((await r.json()) as Err).code).toBe('VALIDATION_FAILED');
  }
  // tin bị chặn không được ghi, không tính lượt
  const c = await ok<Chat>(await request.get(chatUrl(d.id), { headers: bearer(t) }));
  expect(c.turns).toBe(1);
  expect(c.messages.filter((m) => m.role === 'teacher')).toHaveLength(1);
});

test('@TC-S2E4-010 lượt chat thứ 30 nhận, lượt 31 bị chặn', async ({ request }, info) => {
  test.slow();
  const user = teacher(10, info);
  const t = await tokenFor(request, user);
  const d = await newDraft(request, t, FX10);
  for (let i = 1; i <= 29; i++) {
    expect((await say(request, t, d.id, `làm đề khó hơn (${i})`)).status()).toBe(200);
  }
  const c30 = await ok<Chat>(await say(request, t, d.id, 'làm đề khó hơn (30)'));
  expect(c30.turns).toBe(30);
  expect(c30.canSend).toBe(false);
  expect(c30.sendNote).toBe(MSG.turnsFull);
  const calls = async () => ((await ok<unknown[]>(await request.get(`${API}/test-hooks/ai/calls?userId=${user}&limit=100`))) as unknown[]).length;
  const before = await calls();
  expect(before).toBe(30);

  const r31 = await say(request, t, d.id, 'làm đề khó hơn (31)');
  expect(r31.status()).toBe(409);
  expect((await r31.json()) as Err).toMatchObject({ code: 'CONFLICT', message: MSG.turnsFull });
  expect(await calls()).toBe(before); // không gọi AI
  const c = await ok<Chat>(await request.get(chatUrl(d.id), { headers: bearer(t) }));
  expect(c.turns).toBe(30);
  expect(c.messages.filter((m) => m.role === 'teacher')).toHaveLength(30);
});
