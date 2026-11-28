import { ASSET, handwrittenImages, test } from './kit-vi.ts';
import { signIn } from '../tests/s3e1/s3e1.ts';
import { rb01, s5e4World, setup as s5e4Setup } from '../tests/s5e4/s5e4.ts';
import { ack } from '../tests/s5e4/submit/submit.ts';
import { graded, summary } from '../tests/s5e5/insights.ts';
import { mock, setup as s5e6Setup, submit as s5e6Submit, waitAi } from '../tests/s5e6/s5e6b.ts';
import { asg, box, golden, signInAs } from '../tests/e7/e7.ts';

/**
 * T-261128-75 · Chương 04 Chấm điểm và nhận dạng bài làm (clip câm). Cờ chặng 5 (geo.s5.grading, geo.s5.ai-grading) chỉ bật
 * trong phiên dev local của demo. Ảnh bài làm: ảnh viết tay MINH HOẠ (assets/bai-lam-bn01-p*-photo.jpg, tên HS giả) —
 * owner có thể thay ảnh chụp thật cùng tên tệp.
 */

test('17-rubric | Rubric 5 tiêu chí, tổng điểm, cảnh báo', async ({ page, request, demo }) => {
  await demo.step('rubric', async () => {
    const w = await s5e4World(request);
    const lan = w.people['Cô Lan']!;
    const rb = await rb01(request, lan);
    await signIn(page, lan);
    await page.goto(`/gv/rubric?id=${rb.id}`);
    await page.getByTestId('s5e4-editor').waitFor();
    await demo.hold(2500);
    await page.getByTestId('s5e4-crit-points-2').fill('3');
    await demo.hold(3000);
    await page.getByTestId('s5e4-crit-points-2').fill('2');
    await demo.hold(2500);
  });
});

test('18-nop-bai-viet-tay | Học sinh chụp bài viết tay và nộp', async ({ page, request, demo }) => {
  await demo.step('nộp bài', async () => {
    const { w, bc } = await s5e4Setup(request);
    const an = w.people['Bình An']!;
    await ack(request, an);
    await signIn(page, an);
    await page.goto(`/hs/nop-bai?bc=${bc.id}`);
    await page.getByTestId('s5e4-hs-assignment').waitFor();
    await demo.hold(1200);
    await demo.tap(page.getByTestId('s5e4-start'));
    for (const f of ['bai-lam-bn01-p1-photo.jpg', 'bai-lam-bn01-p2-photo.jpg']) {
      await page.getByTestId('s5e4-file').setInputFiles(ASSET(f));
      await page.getByTestId('s5e4-candidate').waitFor();
      await demo.hold(2200);
      await demo.tap(page.getByTestId('s5e4-use'));
      await page.getByTestId('s5e4-candidate').waitFor({ state: 'detached' });
      await demo.hold(1000);
    }
    await demo.tap(page.getByTestId('s5e4-submit'));
    await page.getByTestId('s5e4-submitted-msg').waitFor();
    await demo.mark(page.getByTestId('s5e4-submitted-msg'), 2500);
  });
});

test('20-ai-de-xuat-diem | AI đề xuất điểm từng tiêu chí, giáo viên nhận', async ({ page, request, demo }) => {
  await handwrittenImages(page);
  await demo.step('đề xuất', async () => {
    const c = await s5e6Setup(request, 'ai_proposal');
    await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
    const { sub } = await s5e6Submit(request, c, 'Bình An');
    await waitAi(request, c.lan, sub.id);
    await signIn(page, c.lan);
    await page.goto(`/gv/cham-ai/${sub.id}`);
    await page.getByTestId('s5e6-ai-points-2').waitFor();
    await demo.hold(2000);
    await demo.tap(page.getByTestId('s5e6-crit-name-2'));
    await page.getByTestId('s5e6-region').waitFor();
    await demo.hold(2500);
    for (let k = 1; k <= 5; k++) {
      await page.getByTestId(`s5e6-accept-${k}`).click();
      await demo.hold(500);
    }
    await demo.hold(800);
    await demo.tap(page.getByTestId('s5e6-approve'));
    await demo.hold(2500);
  });
});

test('21-loi-sai-lop | Lỗi sai chung của lớp, bộ luyện nhắc lại', async ({ page, request, demo }) => {
  await demo.step('lỗi của lớp', async () => {
    const c = await graded(request);
    await summary(request, c.lan, c.bc.id);
    await signIn(page, c.lan);
    await page.goto(`/gv/bai-co-cham/${c.bc.id}/loi-cua-lop`);
    await page.getByTestId('s5e5-insight-1').waitFor({ timeout: 20_000 });
    await demo.hold(2000);
    await demo.mark(page.getByTestId('s5e5-insight-1'), 2000);
    await demo.tap(page.getByTestId('s5e5-remind-1'));
    await page.getByTestId('s5e5-remind').waitFor();
    await demo.hold(3000);
  });
});

test('22-theo-doi-lop | Theo dõi lớp theo thời gian thực', async ({ page, request, demo }) => {
  await demo.step('theo dõi', async () => {
    const b = await box(request, golden());
    await signInAs(page, b.teacher, 'Cô Lan', 'teacher');
    await page.goto(`/gv/theo-doi/${asg(b, 'BG-1')}`);
    await page.getByTestId('e7-monitor').waitFor();
    await demo.hold(2000);
    await demo.mark(page.getByTestId('e7-card-hints'), 1800);
    await page.mouse.wheel(0, 400);
    await demo.hold(3500);
  });
});
