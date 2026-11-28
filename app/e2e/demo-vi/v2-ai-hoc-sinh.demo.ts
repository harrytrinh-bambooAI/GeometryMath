import { qaReplyOverride, test } from './kit-vi.ts';
import { openWorkspace, sandbox, tab } from '../tests/e4/e4.ts';
import { signIn } from '../tests/s3e1/s3e1.ts';
import { setup as qaSetup } from '../tests/s5e3/qa.ts';
import { assigned, openPlay, sandbox as s2e8Sandbox, signIn as s2e8SignIn, submit as s2e8Submit } from '../tests/s2e8/s2e8.ts';

/**
 * T-261128-76 · v2.0 · Chương 02 AI đồng hành cùng học sinh (clip câm).
 * Hỏi đáp: câu hỏi/câu trả lời ở fixtures/qa-hkg01.json (AI giả lập; chỉ chữ trả lời được thay khi hiển thị, nguồn của máy chủ).
 */

const FIVE = ['LC-01', 'SP-01', 'LA-01', 'LB-01', 'HCN-01'];

test('11-vao-lop-goi-y | Học sinh vào lớp, gợi ý từng bậc bài HKG-01', async ({ page, request, demo }) => {
  await demo.step('vào lớp', async () => {
    await page.goto('/hs/vao-lop');
    await demo.hold(800);
    await demo.type(page.getByTestId('e1-join-name'), 'Bình An', 110);
    await demo.type(page.getByTestId('e1-join-code'), 'HK7M2A', 110);
    await demo.tap(page.getByTestId('e1-join-submit'));
    await demo.hold(2500);
  });
  await demo.step('bài của em', async () => {
    await page.waitForURL(/\/hs\/bai-cua-em$/, { timeout: 4000 });
    await demo.hold(1500);
  });
  await demo.step('gợi ý', async () => {
    const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
    await openWorkspace(page, sb.classes[0]!.students['An']!, sb.classes[0]!.assignments['BG-1']!, 'Bình An');
    await demo.hold(1500);
    await tab(page, 'hint');
    await demo.hold(1000);
    await demo.tap(page.getByTestId('e4-hint-next'));
    await page.getByTestId('e4-hint-1-body').waitFor();
    await demo.mark(page.getByTestId('e4-hint-1-body'), 3500);
    await demo.tap(page.getByTestId('e4-hint-next'));
    await page.getByTestId('e4-hint-2-body').waitFor({ timeout: 5000 }).catch(() => {});
    await demo.mark(page.getByTestId('e4-hint-2-body'), 3000);
    await tab(page, 'solution').catch(() => {});
    await demo.hold(2500);
  });
});

test('12-bai-luyen | Bài luyện chấm tự động', async ({ page, request, demo }) => {
  await demo.step('soạn bài luyện', async () => {
    const sb = await s2e8Sandbox(request);
    await s2e8SignIn(page, sb.teacher, 'TEACHER', 'Cô Lan');
    await page.goto('/gv/bai-luyen/moi');
    await page.getByTestId('s2e8-compose').waitFor();
    await demo.hold(1200);
    for (const c of FIVE) {
      await page.getByTestId(`s2e8-problem-tick-${c}`).check();
      await demo.hold(350);
    }
    await demo.type(page.getByTestId('s2e8-compose-title'), 'Luyện tập góc và khoảng cách', 50);
    await demo.tap(page.getByTestId('s2e8-compose-save'));
    await page.getByTestId('s2e8-compose-saved').waitFor();
    await demo.hold(1500);
  });
  await demo.step('học sinh luyện', async () => {
    const sb = await s2e8Sandbox(request);
    const set = await assigned(request, sb);
    await openPlay(page, sb, set.id);
    await demo.hold(1800);
    await s2e8Submit(page, '7');
    await demo.hold(2500);
    await s2e8Submit(page, '6');
    await demo.hold(2500);
  });
});

test('13-kho-tri-thuc | Kho tri thức của giáo viên', async ({ page, request, demo }) => {
  const c = await qaSetup(request);
  await demo.step('kho tri thức', async () => {
    await signIn(page, c.lan);
    await page.goto('/gv/kho-tri-thuc');
    await demo.hold(3000);
    await page.mouse.wheel(0, 400);
    await demo.hold(3000);
    await page.mouse.wheel(0, -400);
    await demo.hold(2000);
  });
});

test('14-wow-hoi-dap | Hỏi đáp có nguồn', async ({ page, request, demo }) => {
  const fx = await qaReplyOverride(page);
  const c = await qaSetup(request);
  await demo.step('hỏi đáp', async () => {
    await signIn(page, c.an);
    await page.goto(`/hs/hoi-dap?bai=${c.bc.id}`);
    await demo.hold(1500);
    await demo.type(page.getByTestId('s5e3-qa-question'), fx.question, 40);
    await demo.tap(page.getByTestId('s5e3-qa-send'));
    await page.getByTestId('s5e3-qa-answer').waitFor();
    await demo.mark(page.getByTestId('s5e3-qa-answer'), 5000);
    await demo.tap(page.getByTestId('s5e3-qa-source'));
    await page.getByTestId('s5e3-qa-reader').waitFor();
    await demo.mark(page.getByTestId('s5e3-qa-reader'), 3500);
  });
});
