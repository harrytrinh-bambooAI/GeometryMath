import { expect, test } from '../../support/fixtures.ts';
import { API, MSG, aiCalls, ask, assignSet, bearer, hook, teacherApi, world, type World } from '../s3e1/s3e1.ts';

/** US-S3E5-04 — công tắc trợ lý AI phía học sinh theo lớp (gọi thẳng máy chủ; nhà cung cấp AI giả lập). */

async function setup(request: Parameters<typeof world>[0]) {
  const w = await world(request);
  const set11 = await assignSet(request, w.people['Cô Lan']!, w.classes['11A1']!.id, 'Bộ bài tuần 15');
  return { w, a11: set11.items[0]!.assignmentId };
}

const aiOff = (request: Parameters<typeof world>[0], w: World, key = '11A1', on = false) =>
  teacherApi.settings(request, w.people[key === '10B' ? 'Thầy Khôi' : 'Cô Lan']!.token, w.classes[key]!.id, { aiEnabled: on });

async function expectBlocked(r: Awaited<ReturnType<typeof ask>>) {
  expect(r.status()).toBe(403);
  const e = (await r.json()) as { code: string; message: string; details: Array<{ field: string; message: string }> };
  expect(e.code).toBe('FORBIDDEN');
  expect(e.message).toBe(MSG.s83);
  expect(e.details[0]!.message).toBe('AI_OFF_FOR_CLASS');
}

test('Máy chủ chặn lượt gọi trợ lý và không gọi nhà cung cấp @TC-S3E5-41', async ({ request }) => {
  const { w, a11 } = await setup(request);
  const an = w.people['Bình An']!;
  expect((await aiOff(request, w)).status()).toBe(200);
  const before = await aiCalls(request, an.id);
  await expectBlocked(await ask(request, an.token, a11));
  expect(await aiCalls(request, an.id)).toBe(before);
});

test('Bật lại thì có hiệu lực từ lượt kế tiếp @TC-S3E5-43', async ({ request }) => {
  const { w, a11 } = await setup(request);
  const an = w.people['Bình An']!;
  await aiOff(request, w);
  await expectBlocked(await ask(request, an.token, a11));
  await aiOff(request, w, '11A1', true);
  const r = await ask(request, an.token, a11);
  expect(r.status(), await r.text()).toBe(200);
  const st = await request.get(`${API}/api/v1/student/class-ai/${a11}`, { headers: bearer(an.token) });
  expect(((await st.json()) as { aiEnabled: boolean }).aiEnabled).toBe(true);
});

test('Em ở hai lớp: bộ lớp nào theo công tắc lớp đó @TC-S3E5-44', async ({ request }) => {
  const { w, a11 } = await setup(request);
  const set10 = await assignSet(request, w.people['Thầy Khôi']!, w.classes['10B']!.id, 'Bộ 10B');
  const mai = w.people['Chi Mai']!;
  await aiOff(request, w);
  await expectBlocked(await ask(request, mai.token, a11));
  const ok = await ask(request, mai.token, set10.items[0]!.assignmentId);
  expect(ok.status(), await ok.text()).toBe(200);
});

test('Học sinh không đổi được công tắc @TC-S3E5-46', async ({ request }) => {
  const { w } = await setup(request);
  await aiOff(request, w);
  const r = await teacherApi.settings(request, w.people['Bình An']!.token, w.classes['11A1']!.id, { aiEnabled: true });
  expect(r.status()).toBe(403);
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).json()) as { aiEnabled: boolean };
  expect(d.aiEnabled).toBe(false);
});

test('Giáo viên khác không đổi được công tắc lớp 11A1 @TC-S3E5-47', async ({ request }) => {
  const { w } = await setup(request);
  const r = await teacherApi.settings(request, w.people['Thầy Khôi']!.token, w.classes['11A1']!.id, { aiEnabled: false });
  expect(r.status()).toBe(404);
  const d = (await (await teacherApi.detail(request, w.people['Cô Lan']!.token, w.classes['11A1']!.id)).json()) as { aiEnabled: boolean };
  expect(d.aiEnabled).toBe(true);
});

test('Tắt giữa lúc em đang hỏi: lượt đang chạy hoàn tất, lượt sau bị chặn @TC-S3E5-48', async ({ request }) => {
  const { w, a11 } = await setup(request);
  const an = w.people['Bình An']!;
  await hook(request, 'ai', { userId: an.id, delayMs: 2000 });
  try {
    const first = ask(request, an.token, a11, 'Câu hỏi thứ nhất');
    await new Promise((r) => setTimeout(r, 300));
    expect((await aiOff(request, w)).status()).toBe(200);
    const r1 = await first;
    expect(r1.status(), await r1.text()).toBe(200);
    await expectBlocked(await ask(request, an.token, a11, 'Câu hỏi thứ hai'));
    const log = await request.get(`${API}/api/v1/student/qa-log`, { headers: bearer(an.token) });
    expect(await log.text()).toContain('Câu hỏi thứ nhất');
  } finally {
    await hook(request, 'ai', { userId: an.id, delayMs: 0 });
  }
});

test('Sửa giao diện để hiện lại nút vẫn không gọi được @TC-S3E5-49', async ({ request }) => {
  const { w, a11 } = await setup(request);
  const an = w.people['Bình An']!;
  await aiOff(request, w);
  const before = await aiCalls(request, an.id);
  // khung bị ẩn ở giao diện; gửi thẳng như khi khung được hiện lại bằng công cụ nhà phát triển
  for (let i = 0; i < 3; i++) await expectBlocked(await ask(request, an.token, a11));
  expect(await aiCalls(request, an.id)).toBe(before);
});

test('Tự học không lớp vẫn có trợ lý @TC-S3E5-50', async ({ request }) => {
  const { w } = await setup(request);
  const tu = w.people['Văn Tú']!;
  await aiOff(request, w);
  const s = await request.post(`${API}/api/v1/student/self-study`, {
    headers: bearer(tu.token),
    data: { statement: 'Cho hình chóp S.MNPQ, đáy là hình vuông cạnh a, SM ⊥ (MNPQ), SM = a. Tính góc giữa SQ và mặt đáy.' },
  });
  expect(s.status(), await s.text()).toBeLessThan(300);
  const self = (await s.json()) as { assignmentId?: string; id?: string };
  const id = self.assignmentId ?? self.id!;
  const r = await ask(request, tu.token, id);
  expect(r.status(), await r.text()).toBe(200);
});

test('Giá trị công tắc không phải đúng/sai @TC-S3E5-52', async ({ request }) => {
  const { w } = await setup(request);
  const lan = w.people['Cô Lan']!.token;
  for (const v of ['có', '', 2]) {
    const r = await teacherApi.settings(request, lan, w.classes['11A1']!.id, { aiEnabled: v });
    expect(r.status()).toBe(400);
    expect(((await r.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  }
  const d = (await (await teacherApi.detail(request, lan, w.classes['11A1']!.id)).json()) as { aiEnabled: boolean };
  expect(d.aiEnabled).toBe(true);
});
