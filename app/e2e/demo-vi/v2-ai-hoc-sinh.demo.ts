import { FIXTURE, test } from './kit-vi.ts';
import { openWorkspace, sandbox, tab } from '../tests/e4/e4.ts';
import { signIn } from '../tests/s3e1/s3e1.ts';
import { setup as qaSetup } from '../tests/s5e3/qa.ts';

/**
 * T-261128-75 · Chương 02 AI đồng hành cùng học sinh (clip câm).
 * Dữ liệu thật: gợi ý 1–2 của HKG-01 (seed từ ngân hàng bài tập); câu hỏi/câu trả lời theo giáo án 11A1 ở fixtures/qa-hkg01.json
 * (AI giả lập: chỉ chữ trả lời được thay khi hiển thị, nguồn giữ nguyên của máy chủ).
 */

test('09-goi-y-hoi-ai | Học sinh vào lớp, gợi ý từng bậc bài HKG-01', async ({ page, request, demo }) => {
  await demo.step('vào lớp', async () => {
    await page.goto('/hs/vao-lop');
    await demo.hold(800);
    await demo.type(page.getByTestId('e1-join-name'), 'Minh Khang', 110);
    await demo.type(page.getByTestId('e1-join-code'), 'HK7M2A', 110);
    await demo.tap(page.getByTestId('e1-join-submit'));
    await demo.hold(2500);
  });
  await demo.step('bài của em', async () => {
    await page.waitForURL(/\/hs\/bai-cua-em$/, { timeout: 4000 });
    await demo.hold(1500);
  });
  await demo.step('gợi ý', async () => {
    const sb = await sandbox(request, [{ students: ['Khang'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
    await openWorkspace(page, sb.classes[0]!.students['Khang']!, sb.classes[0]!.assignments['BG-1']!, 'Khang');
    await demo.hold(1500);
    await tab(page, 'hint');
    await demo.hold(1000);
    await demo.tap(page.getByTestId('e4-hint-next'));
    await page.getByTestId('e4-hint-1-body').waitFor();
    await demo.mark(page.getByTestId('e4-hint-1-body'), 3500);
    await demo.tap(page.getByTestId('e4-hint-next'));
    await page.getByTestId('e4-hint-2-body').waitFor({ timeout: 5000 }).catch(() => {});
    await demo.mark(page.getByTestId('e4-hint-2-body'), 3000);
    await demo.mark(page.getByTestId('e4-lock').or(page.getByTestId('e4-solution')), 2000);
  });
});

test('10-kho-tri-thuc | Kho tri thức của giáo viên', async ({ page, request, demo }) => {
  const c = await qaSetup(request);
  await demo.step('kho tri thức', async () => {
    await signIn(page, c.lan);
    await page.goto('/gv/kho-tri-thuc');
    await demo.hold(2500);
    await page.mouse.wheel(0, 400);
    await demo.hold(2500);
    await page.mouse.wheel(0, -400);
    await demo.hold(2000);
  });
});

test('11-hoi-dap-nguon | Hỏi đáp có nguồn', async ({ page, request, demo }) => {
  const fx = FIXTURE<{ question: string; reply: string }>('qa-hkg01.json');
  const c = await qaSetup(request);
  await page.route('**/api/v1/student/kb-qa**', async (route) => {
    if (route.request().method() !== 'POST') return route.fallback();
    const res = await route.fetch();
    const body = (await res.json()) as Record<string, unknown>;
    if (typeof body.reply === 'string' && body.reply) body.reply = fx.reply;
    await route.fulfill({ response: res, json: body });
  });
  await demo.step('hỏi đáp', async () => {
    await signIn(page, c.an);
    await page.goto(`/hs/hoi-dap?bai=${c.bc.id}`);
    await demo.hold(1500);
    await demo.type(page.getByTestId('s5e3-qa-question'), fx.question, 40);
    await demo.tap(page.getByTestId('s5e3-qa-send'));
    await page.getByTestId('s5e3-qa-answer').waitFor();
    await demo.mark(page.getByTestId('s5e3-qa-answer'), 4000);
    await demo.tap(page.getByTestId('s5e3-qa-source'));
    await page.getByTestId('s5e3-qa-reader').waitFor();
    await demo.hold(3000);
  });
});
