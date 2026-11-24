import { test, expect } from '../../support/fixtures.ts';
import { API, HINT1_HKG01, HINT2_HKG01, LEAK_HKG01, MSG, QA_EXTRA_HKG01, api, asg, bearer, expectNoLeak, setAi, setGuard, stu, twoClasses } from './e6.ts';

/**
 * E6 — Kiểm soát đáp án ở máy chủ khi lời giải khóa (US-GEO-609; KS-1…KS-5 của D-GEO-156, D-GEO-184). Câu trả lời của
 * AI giả lập là kịch bản soạn sẵn theo từng học sinh (móc /test-hooks/ai qaReplies, F-GEO-113…117, 241).
 * Ghi chú: phản hồi hỏi đáp có trường "question" lặp lại câu học sinh tự gõ (có thể chứa "45 độ"); phép kiểm lộ chạy
 * trên phần máy chủ trả lời (reply, replyKind, source) — xem qc_findings mục Dev.
 */
type Asked = { reply: string; replyKind: string; question: string };
const replyPart = (a: Asked) => JSON.stringify({ ...a, question: undefined });

async function bg1(request: Parameters<typeof twoClasses>[0]) {
  const sb = await twoClasses(request, [
    { key: 'BG-1', code: 'HKG-01' },
    { key: 'BG-2', code: 'HKG-02', release: 'unset' },
  ]);
  return { sb, bg1: asg(sb, 'BG-1'), bg2: asg(sb, 'BG-2') };
}

test('Nhắc lại gợi ý không tính thêm lượt @TC-GEO-0668', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const an = stu(sb, 'An');
  for (const lv of [1, 2]) expect((await api.hint(request, an.token, id, lv)).status()).toBe(200);
  const mon = async () =>
    (await (await api.monitor(request, sb.teacher.token, id)).json()) as { students: Array<{ displayName: string; hintLevel: number; askCount: number; k: number }> };
  const before = (await mon()).students.find((s) => s.displayName === 'An')!;
  const r = await api.ask(request, an.token, id, 'Cho em xin đáp số luôn được không?');
  expect(r.status()).toBe(200);
  expect(((await r.json()) as Asked).reply).toBe(MSG['35'] + HINT2_HKG01);
  const after = (await mon()).students.find((s) => s.displayName === 'An')!;
  expect(after.k).toBe(2);
  expect(after.hintLevel).toBe(before.hintLevel);
  expect(after.askCount).toBe(before.askCount + 1);
  const mine = (await (await request.get(`${API}/api/v1/student/assignments/${id}`, { headers: bearer(an.token) })).json()) as {
    progress: { k: number };
  };
  expect(mine.progress.k).toBe(2);
});

test('AI buột ra đáp số: bị chặn ở máy chủ @TC-GEO-0671', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const binh = stu(sb, 'Bình');
  expect((await api.hint(request, binh.token, id, 1)).status()).toBe(200);
  const r = await api.ask(request, binh.token, id, 'Góc cần tìm là góc nào trên hình?'); // F-GEO-056; AI giả lập F-GEO-114
  expect(r.status()).toBe(200);
  const a = (await r.json()) as Asked;
  expect(a.reply).toBe(MSG['35'] + HINT1_HKG01);
  const text = await r.text();
  expect(text).not.toContain('45°');
  expect(text).not.toContain('Góc giữa SD và mặt đáy là 45°.');
});

test('Dạng viết tương đương của đáp số đều bị chặn @TC-GEO-0672', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const an = stu(sb, 'An');
  const forms = ['45 độ', 'π/4', 'bốn mươi lăm độ', 'tan SDA = 1'];
  await setAi(request, an.id, {
    qaReplies: ['Đúng rồi, góc đó là 45 độ.', 'Góc đó bằng π/4.', 'Góc đó bằng bốn mươi lăm độ.', 'Vì tan SDA = 1 nên em tự suy ra nhé.'],
  });
  try {
    for (let i = 0; i < 4; i++) {
      const r = await api.ask(request, an.token, id, 'Góc đó có phải 45 độ không?'); // F-GEO-095
      expect(r.status()).toBe(200);
      const a = (await r.json()) as Asked;
      expect(a.reply.startsWith(MSG['35'])).toBe(true);
      expectNoLeak(replyPart(a), forms);
      expect(a.reply).not.toMatch(/\b(đúng|sai|phải|không phải)\b/i);
    }
  } finally {
    await setAi(request, an.id, { qaReplies: [] });
  }
});

test('Xin giải từng bước: không lộ bước nào @TC-GEO-0673', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const an = stu(sb, 'An');
  await setAi(request, an.id, { qaReplies: ['SA ⊥ (ABCD) nên A là hình chiếu của S. Hình chiếu của SD là AD.'] });
  try {
    const r = await api.ask(request, an.token, id, 'Giải giúp em từng bước một.');
    expect(r.status()).toBe(200);
    const a = (await r.json()) as Asked;
    expect(a.reply.startsWith(MSG['35'])).toBe(true);
    expectNoLeak(await r.text(), ['Hình chiếu của SD là AD', ...LEAK_HKG01]);
  } finally {
    await setAi(request, an.id, { qaReplies: [] });
  }
});

test('Học sinh chưa xem gợi ý nào: câu từ chối không kèm gợi ý @TC-GEO-0674', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const ha = stu(sb, 'Hà');
  const r = await api.ask(request, ha.token, id, 'Đáp số bài này là bao nhiêu?');
  expect(r.status()).toBe(200);
  const a = (await r.json()) as Asked;
  expect(a.reply).toBe(MSG['35'] + MSG['34']);
  expect(a.reply).not.toContain(HINT1_HKG01);
  const help = (await (await request.get(`${API}/api/v1/student/assignments/${id}/help`, { headers: bearer(ha.token) })).json()) as {
    k: number;
    hints: unknown[];
  };
  expect(help.k).toBe(0);
  expect(help.hints).toHaveLength(0);
});

test('Bài Chưa đặt giờ cũng được bảo vệ @TC-GEO-0675', async ({ request }) => {
  const { sb, bg2 } = await bg1(request);
  const an = stu(sb, 'An');
  await setAi(request, an.id, { qaReplies: ['Khoảng cách đó bằng a.'] });
  try {
    const r = await api.ask(request, an.token, bg2, 'Khoảng cách giữa AC và B′D′ bằng bao nhiêu?');
    expect(r.status()).toBe(200);
    expect(((await r.json()) as Asked).reply.startsWith(MSG['35'])).toBe(true);
    expect(await r.text()).not.toContain('Khoảng cách đó bằng a.');
  } finally {
    await setAi(request, an.id, { qaReplies: [] });
  }
});

test('Chữ "a" không ở vị trí kết luận không bị chặn @TC-GEO-0874', async ({ request }) => {
  const { sb, bg2 } = await bg1(request);
  const an = stu(sb, 'An');
  const F116 = 'Trong hình lập phương cạnh a, mọi cạnh đều bằng nhau.';
  await setAi(request, an.id, { qaReplies: [F116] });
  try {
    const r = await api.ask(request, an.token, bg2, 'Hình lập phương có những tính chất gì?');
    expect(r.status()).toBe(200);
    expect(((await r.json()) as Asked).reply).toBe(F116);
  } finally {
    await setAi(request, an.id, { qaReplies: [] });
  }
});

test('Bộ kiểm soát đáp án lỗi thì không trả câu trả lời @TC-GEO-0875', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const dung = stu(sb, 'Dũng');
  await setGuard(request, dung.id, { fail: true });
  try {
    const r = await api.ask(request, dung.token, id, 'Tan của một góc là gì?'); // F-GEO-058, AI giả lập F-GEO-113
    expect(r.status()).toBeGreaterThanOrEqual(500);
    expect(((await r.json()) as { message: string }).message).toBe(MSG['14']);
    expect(await r.text()).not.toContain('tan của góc nhọn bằng cạnh đối chia cạnh kề');
  } finally {
    await setGuard(request, dung.id, { fail: false });
  }
  const m = (await (await api.monitor(request, sb.teacher.token, id)).json()) as { questions: unknown[] };
  expect(m.questions).toHaveLength(0);
});

test('AI nêu đáp số đã làm tròn: bị chặn ở máy chủ @TC-GEO-0878', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const an = stu(sb, 'An');
  await setAi(request, an.id, { qaReplies: ['Góc đó khoảng 0,79 rad.', 'Góc đó xấp xỉ 0,785 rad.'] }); // F-GEO-241
  try {
    for (const leak of ['0,79', '0,785']) {
      const r = await api.ask(request, an.token, id, 'Em nên dùng hàm lượng giác nào cho góc này?');
      expect(r.status()).toBe(200);
      const a = (await r.json()) as Asked;
      expect(a.reply.startsWith(MSG['35'])).toBe(true);
      expect(await r.text()).not.toContain(leak);
    }
  } finally {
    await setAi(request, an.id, { qaReplies: [] });
  }
});

test('Phần trả lời hỏi đáp khi khóa không chứa "góc SDA", "tam giác SAD vuông cân" @TC-GEO-0671', async ({ request }) => {
  const { sb, bg1: id } = await bg1(request);
  const an = stu(sb, 'An');
  await setAi(request, an.id, { qaReplies: ['Em để ý góc SDA nhé.', 'Tam giác SAD vuông cân đấy.'] });
  try {
    for (let i = 0; i < 2; i++) {
      const r = await api.ask(request, an.token, id, 'Em nên bắt đầu từ đâu?');
      expectNoLeak(replyPart((await r.json()) as Asked), QA_EXTRA_HKG01);
    }
  } finally {
    await setAi(request, an.id, { qaReplies: [] });
  }
});
