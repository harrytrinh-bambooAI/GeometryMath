import { ASSET, gstep, qaReplyOverride, test } from '../kit-vi.ts';
import { openWorkspace as e3Open, tapPoint as e3Tap, tool as e3Tool } from '../../tests/e3/e3.ts';
import { openWorkspace, sandbox, tab } from '../../tests/e4/e4.ts';
import { signIn } from '../../tests/s3e1/s3e1.ts';
import { setup as qaSetup } from '../../tests/s5e3/qa.ts';
import { setup as s5e4Setup } from '../../tests/s5e4/s5e4.ts';
import { ack } from '../../tests/s5e4/submit/submit.ts';

/** T-261128-77 · v1.0 · Video hướng dẫn cho HỌC SINH (clip câm, mốc bước b1..bN khớp guides.json). */

test('13-hs-vao-lop | Vào lớp bằng mã lớp', async ({ page, demo }) => {
  await gstep(demo, 1, 'Mở trang Vào lớp', async () => {
    await page.goto('/hs/vao-lop');
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Nhập tên và mã lớp', async () => {
    await demo.type(page.getByTestId('e1-join-name'), 'Bình An', 110);
    await demo.type(page.getByTestId('e1-join-code'), 'HK7M2A', 130);
    await demo.tap(page.getByTestId('e1-join-submit'), 1000);
  });
  await gstep(demo, 3, 'Xem Bài của em', async () => {
    await page.waitForURL(/\/hs\/bai-cua-em$/, { timeout: 6000 });
    await demo.hold(2500);
  });
});

test('14-hs-lam-bai-3d | Làm bài trên hình 3D', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở bài', async () => {
    await e3Open(page, request);
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Xoay hình', async () => {
    await demo.drag('viewer', 240, 20);
    await demo.hold(600);
    await demo.drag('viewer', -180, 50);
  });
  await gstep(demo, 3, 'Vẽ thêm đoạn', async () => {
    await e3Tool(page, 'line');
    await e3Tap(page, 'B');
    await e3Tap(page, 'D');
  });
  await gstep(demo, 4, 'Đo độ dài', async () => {
    await e3Tool(page, 'length');
    await e3Tap(page, 'S');
    await e3Tap(page, 'D');
  });
  await gstep(demo, 5, 'Đo góc', async () => {
    await e3Tool(page, 'angle');
    for (const x of ['S', 'A', 'D']) await e3Tap(page, x);
  }, 2500);
});

test('15-hs-goi-y-loi-giai | Xem gợi ý và lời giải', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở thẻ Gợi ý', async () => {
    const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
    await openWorkspace(page, sb.classes[0]!.students['An']!, sb.classes[0]!.assignments['BG-1']!, 'Bình An');
    await demo.hold(1200);
    await tab(page, 'hint');
  });
  await gstep(demo, 2, 'Gợi ý một: hướng nghĩ', async () => {
    await demo.tap(page.getByTestId('e4-hint-next'), 1000);
    await page.getByTestId('e4-hint-1-body').waitFor();
    await demo.mark(page.getByTestId('e4-hint-1-body'), 2500);
  });
  await gstep(demo, 3, 'Gợi ý hai: bước kế tiếp', async () => {
    await demo.tap(page.getByTestId('e4-hint-next'), 1000);
    await page.getByTestId('e4-hint-2-body').waitFor({ timeout: 5000 });
    await demo.mark(page.getByTestId('e4-hint-2-body'), 2500);
  });
  await gstep(demo, 4, 'Lời giải theo giờ mở', async () => {
    await tab(page, 'solution');
    await demo.hold(2500);
  });
});

test('16-hs-hoi-ai | Hỏi AI, đọc nguồn', async ({ page, request, demo }) => {
  const fx = await qaReplyOverride(page);
  const c = await qaSetup(request);
  await gstep(demo, 1, 'Mở Hỏi đáp', async () => {
    await signIn(page, c.an);
    await page.goto(`/hs/hoi-dap?bai=${c.bc.id}`);
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Gõ câu hỏi và gửi', async () => {
    await demo.type(page.getByTestId('s5e3-qa-question'), fx.question, 40);
    await demo.tap(page.getByTestId('s5e3-qa-send'), 900);
    await page.getByTestId('s5e3-qa-answer').waitFor();
  });
  await gstep(demo, 3, 'Đọc câu trả lời', async () => {
    await demo.mark(page.getByTestId('s5e3-qa-answer'), 3500);
  });
  await gstep(demo, 4, 'Bấm nguồn', async () => {
    await demo.tap(page.getByTestId('s5e3-qa-source'), 1000);
    await page.getByTestId('s5e3-qa-reader').waitFor();
    await demo.mark(page.getByTestId('s5e3-qa-reader'), 2500);
  });
});

test('17-hs-nop-bai-viet-tay | Chụp và nộp bài viết tay', async ({ page, request, demo }) => {
  const { w, bc } = await s5e4Setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  const shoot = async (f: string) => {
    await page.getByTestId('s5e4-file').setInputFiles(ASSET(f));
    await page.getByTestId('s5e4-candidate').waitFor();
    await demo.hold(2200);
    await demo.tap(page.getByTestId('s5e4-use'), 900);
    await page.getByTestId('s5e4-candidate').waitFor({ state: 'detached' });
  };
  await gstep(demo, 1, 'Mở bài có chấm', async () => {
    await signIn(page, an);
    await page.goto(`/hs/nop-bai?bc=${bc.id}`);
    await page.getByTestId('s5e4-hs-assignment').waitFor();
    await demo.mark(page.getByTestId('s5e4-hs-assignment'), 2000);
  });
  await gstep(demo, 2, 'Chụp trang một', async () => {
    await demo.tap(page.getByTestId('s5e4-start'), 900);
    await shoot('bai-lam-bn01-p1-photo.jpg');
  });
  await gstep(demo, 3, 'Chụp các trang tiếp', async () => {
    await shoot('bai-lam-bn01-p2-photo.jpg');
  });
  await gstep(demo, 4, 'Nộp bài', async () => {
    await demo.tap(page.getByTestId('s5e4-submit'), 1000);
    await page.getByTestId('s5e4-submitted-msg').waitFor();
    await demo.mark(page.getByTestId('s5e4-submitted-msg'), 2500);
  }, 2000);
});
