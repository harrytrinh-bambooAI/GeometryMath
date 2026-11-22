import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { PV } from '../e1/e1.ts';
import { addMidpointSD, snap } from '../e3/e3.ts';
import {
  API,
  MSG,
  aiCalls,
  bearer,
  call,
  openWorkspace,
  recordResponses,
  sandbox,
  setAiFor,
  signIn,
  tab,
  type Person,
  type Sandbox,
} from './e4.ts';

/** E4 · E2E-UI: hỏi đáp có nguồn (US-GEO-409), nhật ký (410), từ khóa và hộp bài giảng (411, 412). */

const F055 = 'Vì sao A là hình chiếu của S?';
const F056 = 'Góc cần tìm là góc nào trên hình?';
const F057 = 'Cho em xin đáp số luôn được không?';
const F058 = 'Tan của một góc là gì?';
const F059 = 'Thủ đô của nước Pháp là gì?';
const BG1_TITLE = 'Hình chóp S.ABCD: tính góc giữa SD và mặt đáy';
const KHO = [
  'Đường thẳng vuông góc với mặt phẳng',
  'Hình chiếu vuông góc',
  'Góc giữa đường thẳng và mặt phẳng',
  'Khoảng cách giữa hai mặt phẳng song song',
  'Thể tích khối lăng trụ',
  'Diện tích xung quanh hình nón',
  'Diện tích toàn phần hình trụ',
];
const KHO_AI = ['Tỉ số lượng giác trong tam giác vuông', 'Hai đường thẳng chéo nhau', 'Mặt cầu ngoại tiếp'];

async function bg1(request: APIRequestContext, students = ['An'], extra: Array<{ key: string; code: string; release?: string }> = []) {
  return sandbox(request, [{ students, assignments: [{ key: 'BG-1', code: 'HKG-01' }, ...extra] }]);
}
const who = (sb: Sandbox, n: string): Person => sb.classes[0]!.students[n]!;
const as = (sb: Sandbox, k: string) => sb.classes[0]!.assignments[k]!;

async function askUi(page: Page, q: string) {
  await page.getByTestId('e4-qa-input').fill(q);
  await page.getByTestId('e4-qa-send').click();
}

// ---------------------------------------------------------------- US-GEO-409
test('Câu trả lời có nguồn "Giáo án mẫu" @TC-GEO-0454', async ({ page, request }) => {
  const sb = await bg1(request);
  await openWorkspace(page, who(sb, 'An'), as(sb, 'BG-1'));
  await tab(page, 'ask');
  await expect(page.getByTestId('e4-qa-input')).toHaveAttribute('placeholder', 'Hỏi về bài này…');
  await askUi(page, F055);
  const a = page.getByTestId('e4-qa-answer-1');
  await expect(a).toContainText('hình chiếu vuông góc');
  await expect(page.getByTestId('e4-qa-answer-1-source')).toHaveText('Nguồn: Giáo án mẫu · Hình chiếu vuông góc');
  await expect(page.getByTestId('e4-qa-answer-1-source-link')).toHaveRole('button');
});

test('Câu trả lời có nguồn "AI soạn" @TC-GEO-0455', async ({ page, request }) => {
  const sb = await bg1(request, ['Dũng']);
  await openWorkspace(page, who(sb, 'Dũng'), as(sb, 'BG-1'), 'Dũng');
  await tab(page, 'ask');
  await askUi(page, F058);
  await expect(page.getByTestId('e4-qa-answer-1-source')).toHaveText('Nguồn: AI soạn · Tỉ số lượng giác trong tam giác vuông');
});

test('Câu hỏi ngoài giáo trình @TC-GEO-0456', async ({ page, request }) => {
  const sb = await bg1(request);
  await openWorkspace(page, who(sb, 'An'), as(sb, 'BG-1'));
  await tab(page, 'ask');
  await askUi(page, F059);
  await expect(page.getByTestId('e4-qa-answer-1-text')).toHaveText(MSG['36']);
  await expect(page.getByTestId('e4-qa-answer-1-source')).toHaveCount(0);
  await expect(page.getByTestId('e4-qa-answer-1')).not.toContainText('Nguồn');
});

test('Màn chờ khi trợ lý đang trả lời @TC-GEO-0457', async ({ page, request }) => {
  const sb = await bg1(request);
  await setAiFor(request, who(sb, 'An').id, { delayMs: 5000 });
  await openWorkspace(page, who(sb, 'An'), as(sb, 'BG-1'));
  await tab(page, 'ask');
  await askUi(page, F055);
  await expect(page.getByTestId('e4-qa-wait')).toHaveText(MSG['15']);
  await expect(page.getByTestId('e4-qa-input')).toBeDisabled();
  await expect(page.getByTestId('e4-qa-send')).toBeDisabled();
  await expect(page.getByTestId('e4-qa-answer-1')).toHaveCount(0);
  await expect(page.getByTestId('e4-qa-answer-1')).toHaveCount(1, { timeout: 15_000 });
  await expect(page.getByTestId('e4-qa-wait')).toHaveCount(0);
  await expect(page.locator('[data-testid^="e4-qa-answer-"][data-kind]')).toHaveCount(1);
});

test('Nguồn nêu ra luôn mở được @TC-GEO-0459', async ({ page, request }) => {
  const sb = await bg1(request);
  await openWorkspace(page, who(sb, 'An'), as(sb, 'BG-1'));
  await tab(page, 'ask');
  for (const q of [F055, F058, F059, F057]) {
    await askUi(page, q);
    await expect(page.getByTestId('e4-qa-wait')).toHaveCount(0);
  }
  const titles = await page.locator('[data-testid$="-source-link"]').allTextContents();
  expect(titles.length).toBeGreaterThan(0);
  for (const t of titles) expect([...KHO, ...KHO_AI]).toContain(t);
  await page.getByTestId('e4-qa-answer-1-source-link').click();
  await expect(page.getByTestId('e4-lecture-title')).toHaveText('Hình chiếu vuông góc');
});

test('Trợ lý lỗi khi hỏi @TC-GEO-0460', async ({ page, request }) => {
  const sb = await bg1(request);
  const an = who(sb, 'An');
  await setAiFor(request, an.id, { mode: 'error' });
  await openWorkspace(page, an, as(sb, 'BG-1'));
  await tab(page, 'ask');
  await askUi(page, F055);
  await expect(page.getByTestId('e4-qa-error')).toContainText(MSG['14']);
  await expect(page.getByTestId('e4-qa-retry')).toHaveText('Thử lại');
  await expect(page.getByTestId('e4-qa-question-1')).toHaveText(F055);
  const mon = (await (await request.get(`${API}/api/v1/teacher/assignments/${as(sb, 'BG-1')}/monitor`, { headers: bearer(sb.teacher.token) })).json()) as {
    students: Array<{ displayName: string; askCount: number }>;
  };
  expect(mon.students.find((s) => s.displayName === 'An')?.askCount).toBe(0);
});

test('Giáo viên đã tắt hỏi đáp @TC-GEO-0462', async ({ page, request }) => {
  const sb = await sandbox(request, [
    { name: '9B', level: 'thcs', students: ['Khánh'], assignments: [{ key: 'BG-5', code: 'HKG-05', release: 'unset', allowHint: false, allowQa: false }] },
  ]);
  const kh = who(sb, 'Khánh');
  await openWorkspace(page, kh, as(sb, 'BG-5'), 'Khánh');
  await tab(page, 'ask');
  await expect(page.getByTestId('e4-qa-disabled')).toHaveText(MSG['33']);
  await expect(page.getByTestId('e4-qa-input')).toHaveCount(0);
  const before = (await aiCalls(request, kh.id)).length;
  const r = await call.ask(request, kh.token, as(sb, 'BG-5'), F055);
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { code: string }).code).toBe('FORBIDDEN');
  expect((await aiCalls(request, kh.id)).length).toBe(before);
});

// ---------------------------------------------------------------- US-GEO-410
async function openHistory(page: Page, p: Person) {
  await signIn(page, p, 'An');
  await page.goto('/hs/bai-cua-em');
  await page.getByTestId('e4-nav-lich-su').click();
  await expect(page.getByTestId('e4-history')).toBeVisible();
}

test('Xem nhật ký của mình @TC-GEO-0463', async ({ page, request }) => {
  const sb = await bg1(request);
  const an = who(sb, 'An');
  for (const q of [F055, F056, F057]) expect((await call.ask(request, an.token, as(sb, 'BG-1'), q)).status()).toBe(200);
  await openHistory(page, an);
  const rows = page.locator('[data-testid^="e4-log-row-"]');
  await expect(rows).toHaveCount(3);
  await expect(rows.getByTestId('e4-log-question')).toHaveText([F057, F056, F055]);
  for (const t of await rows.getByTestId('e4-log-title').allTextContents()) expect(t).toBe(BG1_TITLE);
  for (const t of await rows.getByTestId('e4-log-time').allTextContents()) expect(t).toMatch(/^\d{2}:\d{2}, \d{2}\/\d{2}\/\d{4}$/);
});

test('Lọc nhật ký theo bài @TC-GEO-0464', async ({ page, request }) => {
  const sb = await bg1(request, ['An'], [{ key: 'BG-3', code: 'HKG-03', release: 'open' }]);
  const an = who(sb, 'An');
  for (const q of [F055, F056, F057]) await call.ask(request, an.token, as(sb, 'BG-1'), q);
  await call.ask(request, an.token, as(sb, 'BG-3'), 'Thể tích lăng trụ tính thế nào?');
  await openHistory(page, an);
  await expect(page.locator('[data-testid^="e4-log-row-"]')).toHaveCount(4);
  await page.getByTestId('e4-log-filter').selectOption(as(sb, 'BG-1'));
  const rows = page.locator('[data-testid^="e4-log-row-"]');
  await expect(rows).toHaveCount(3);
  for (const id of await rows.evaluateAll((els) => els.map((e) => e.getAttribute('data-assignment-id')))) expect(id).toBe(as(sb, 'BG-1'));
});

test('Mở lại câu trả lời đã lưu, không gọi AI lại @TC-GEO-0465', async ({ page, request }) => {
  const sb = await bg1(request);
  const an = who(sb, 'An');
  for (const q of [F055, F056, F057]) await call.ask(request, an.token, as(sb, 'BG-1'), q);
  const n = (await aiCalls(request, an.id)).length;
  await openHistory(page, an);
  const row = page.locator('[data-testid^="e4-log-row-"]').filter({ hasText: F055 });
  await row.locator('[data-testid^="e4-log-open-"]').click();
  const ans = row.locator('[data-testid^="e4-log-answer-"][data-kind]');
  await expect(ans).toContainText('hình chiếu vuông góc');
  await expect(ans).toContainText('Nguồn: Giáo án mẫu');
  expect((await aiCalls(request, an.id)).length).toBe(n);
});

test('Bài chưa có câu hỏi @TC-GEO-0467', async ({ page, request }) => {
  const sb = await bg1(request, ['An'], [{ key: 'BG-2', code: 'HKG-02', release: 'unset' }]);
  const an = who(sb, 'An');
  await call.ask(request, an.token, as(sb, 'BG-1'), F055);
  await openHistory(page, an);
  await page.getByTestId('e4-log-filter').selectOption(as(sb, 'BG-2'));
  await expect(page.getByTestId('e4-log-empty')).toHaveText(MSG['38']);
});

test('Câu trả lời từng bị chặn mở lại vẫn là câu thay thế @TC-GEO-0493', async ({ page, request }) => {
  const sb = await bg1(request, ['Bình']);
  const binh = who(sb, 'Bình');
  expect((await call.hint(request, binh.token, as(sb, 'BG-1'), 1)).status()).toBe(200);
  const asked = (await (await call.ask(request, binh.token, as(sb, 'BG-1'), F056)).json()) as { reply: string };
  expect(asked.reply.startsWith(MSG['35'])).toBe(true);
  // chờ ĐỦ thân mọi phản hồi API (đọc bất đồng bộ) rồi mới kiểm — tránh đạt giả khi thân chưa kịp gom (soát E4 #14)
  const responses = recordResponses(page);
  await openHistory(page, binh);
  const row = page.locator('[data-testid^="e4-log-row-"]').filter({ hasText: F056 });
  await row.locator('[data-testid^="e4-log-open-"]').click();
  await expect(row.locator('[data-testid$="-text"]')).toHaveText(asked.reply);
  const all = await responses();
  expect(all, 'đã gom được câu trả lời của nhật ký').toContain(F056);
  expect(all).not.toContain('45°');
  expect(all).not.toContain('Góc giữa SD và mặt đáy là 45°.');
});

// ---------------------------------------------------------------- US-GEO-411, 412
async function hint1Workspace(page: Page, request: APIRequestContext, name = 'An') {
  const sb = await bg1(request, [name]);
  const p = who(sb, name);
  expect((await call.hint(request, p.token, as(sb, 'BG-1'), 1)).status()).toBe(200);
  await openWorkspace(page, p, as(sb, 'BG-1'), name);
  await tab(page, 'hint');
  return sb;
}

test('Bấm từ khóa trong gợi ý @TC-GEO-0469', async ({ page, request }) => {
  await hint1Workspace(page, request);
  const kw = page.getByTestId('e4-hint-1-body').getByRole('button', { name: 'hình chiếu', exact: true });
  await expect(kw).toHaveClass(/e4-kw/);
  await kw.click();
  await expect(page.getByTestId('e4-lecture-title')).toHaveText('Hình chiếu vuông góc');
  await expect(page.getByTestId('e4-lecture-body')).not.toBeEmpty();
  await expect(page.getByTestId('e4-lecture-source')).toContainText('Nguồn: Giáo án mẫu');
});

test('Bài giảng do AI viết mang nhãn chờ duyệt @TC-GEO-0470', async ({ page, request }) => {
  const sb = await bg1(request, ['Dũng']);
  await openWorkspace(page, who(sb, 'Dũng'), as(sb, 'BG-1'), 'Dũng');
  await tab(page, 'ask');
  await askUi(page, F058);
  await page.getByTestId('e4-qa-answer-1-text').getByRole('button', { name: 'Tỉ số lượng giác trong tam giác vuông' }).click();
  await expect(page.getByTestId('e4-lecture-source')).toContainText('Nguồn: AI soạn');
  await expect(page.getByTestId('e4-lecture-ai-label')).toHaveText('AI soạn · chờ giáo viên duyệt');
});

test('Từ khóa trong lời giải @TC-GEO-0471', async ({ page, request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01', release: 'open' }] }]);
  await openWorkspace(page, who(sb, 'An'), as(sb, 'BG-1'));
  await tab(page, 'solution');
  await page.getByTestId('e4-step-body').getByRole('button', { name: 'hình chiếu', exact: true }).first().click();
  await expect(page.getByTestId('e4-lecture-title')).toHaveText('Hình chiếu vuông góc');
  await expect(page.getByTestId('e4-lecture-source')).toContainText('Nguồn: Giáo án mẫu');
});

test('Đóng hộp bài giảng: về đúng chỗ, điểm M và tiến độ không đổi @TC-GEO-0472', async ({ page, request }) => {
  await hint1Workspace(page, request);
  await addMidpointSD(page);
  await tab(page, 'hint');
  await page.getByTestId('e4-hint-1-body').getByRole('button', { name: 'hình chiếu', exact: true }).click();
  await expect(page.getByTestId('e4-lecture')).toBeVisible();
  await page.getByTestId('e4-lecture-close').click();
  await expect(page.getByTestId('e4-lecture')).toHaveCount(0);
  await expect(page.getByTestId('e3-tabpanel-hint')).toBeVisible();
  await expect(page.getByTestId('e4-hint-1-body')).toBeVisible();
  await expect(page.getByTestId('e4-progress')).toHaveText('Tiến độ của em 1/3');
  expect((await snap(page)).points.some((p) => p.name === 'M')).toBe(true);
});

test('Cụm từ không có bài giảng @TC-GEO-0473', async ({ page, request }) => {
  const sb = await bg1(request);
  const an = who(sb, 'An');
  await openWorkspace(page, an, as(sb, 'BG-1'));
  await tab(page, 'ask');
  await askUi(page, 'Thiết diện của hình chóp là gì?');
  const text = page.getByTestId('e4-qa-answer-1-text');
  await expect(text).toContainText('thiết diện');
  await expect(text.getByRole('button', { name: /thiết diện/i })).toHaveCount(0);
  const r = await call.lecture(request, an.token, 'thiết diện');
  expect(r.status()).toBe(404);
  expect(await r.json()).toMatchObject({ code: 'NOT_FOUND', message: MSG['39'] });
});

test('Không dùng nhãn "Giáo viên đã duyệt" cho bảy bài Giáo án mẫu @TC-GEO-0475', async ({ page, request }) => {
  const sb = await hint1Workspace(page, request);
  const an = who(sb, 'An');
  // mở trên màn: hai bài có từ khóa trong Gợi ý 1 của BG-1
  for (const name of ['Góc giữa đường thẳng và mặt phẳng', 'hình chiếu']) {
    await page.getByTestId('e4-hint-1-body').getByRole('button', { name, exact: true }).click();
    await expect(page.getByTestId('e4-lecture-source')).toContainText('Nguồn: Giáo án mẫu');
    await expect(page.getByTestId('e4-lecture')).not.toContainText('Giáo viên đã duyệt');
    await page.getByTestId('e4-lecture-close').click();
  }
  // cả bảy bài: dữ liệu hộp bài giảng
  for (const title of KHO) {
    const t = await (await call.lecture(request, an.token, title)).text();
    expect(JSON.parse(t)).toMatchObject({ title, sourceLabel: 'Giáo án mẫu' });
    expect(t).not.toContain('Giáo viên đã duyệt');
  }
});

test('Cờ chờ soát hiện cho học sinh và giáo viên @TC-GEO-0494', async ({ page, request, browser }) => {
  await hint1Workspace(page, request);
  await page.getByTestId('e4-hint-1-body').getByRole('button', { name: 'hình chiếu', exact: true }).click();
  await expect(page.getByTestId('e4-lecture-source')).toContainText('Nguồn: Giáo án mẫu');
  await expect(page.getByTestId('e4-lecture-review')).toHaveText('Nội dung đang chờ giáo viên soát');
  // Giáo viên mẫu: trang chi tiết HKG-01, Gợi ý 1
  const ctx = await browser.newContext();
  const tp = await ctx.newPage();
  try {
    const sb = await sandbox(request, [{ students: [] }]);
    await signIn(tp, sb.teacher, 'Giáo viên mẫu', '', 'teacher');
    await tp.goto(`/gv/de/${PV('HKG-01')}`);
    await tp.getByTestId('e4-teacher-hint-1').getByRole('button', { name: 'hình chiếu', exact: true }).click();
    await expect(tp.getByTestId('e4-lecture-source')).toContainText('Nguồn: Giáo án mẫu');
    await expect(tp.getByTestId('e4-lecture-review')).toHaveText('Nội dung đang chờ giáo viên soát');
    // ba bài "AI soạn" không có dòng chờ soát
    for (const title of KHO_AI) {
      const r = (await (await call.lecture(request, sb.teacher.token, title)).json()) as Record<string, unknown>;
      expect(r.reviewNote ?? null).toBeNull();
    }
  } finally {
    await ctx.close();
  }
});
