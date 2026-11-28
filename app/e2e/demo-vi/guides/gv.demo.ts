import { gstep, handwrittenImages, lessonPlanOverride, test } from '../kit-vi.ts';
import { PV } from '../../tests/e1/e1.ts';
import { signInPage } from '../../tests/e1/e1.ts';
import { signIn as e6SignIn, twoClasses } from '../../tests/e6/e6.ts';
import { asg, box, golden, signInAs } from '../../tests/e7/e7.ts';
import { signIn as s2e3SignIn, teacher as s2e3Teacher } from '../../tests/s2e3/s2e3.ts';
import { present } from '../../tests/s2e7/s2e7.ts';
import { signIn, world } from '../../tests/s3e1/s3e1.ts';
import { KT15, LAN, createSet, importBank, lanToken, type ExamSet } from '../../tests/s5e2/s5e2.ts';
import { S5E3_WORLD, tl01 } from '../../tests/s5e3/kb.ts';
import { setup as qaSetup } from '../../tests/s5e3/qa.ts';
import { rb01, s5e4World } from '../../tests/s5e4/s5e4.ts';
import { P1, P2, ack, submitOk } from '../../tests/s5e4/submit/submit.ts';
import { gradingWorld, sc1 } from '../../tests/s5e4/review/grading.ts';
import { graded, summary } from '../../tests/s5e5/insights.ts';
import { mock, setup as s5e6Setup, submit as s5e6Submit, waitAi } from '../../tests/s5e6/s5e6b.ts';

/**
 * T-261128-77 · v1.0 · Video hướng dẫn cho GIÁO VIÊN (clip câm, mốc bước b1..bN khớp guides.json). AI giả lập; dữ liệu seed + bài thật HKG.
 * Tiêu đề test: "<id> | <tác vụ>" — id trùng guides.json.
 */

const DE = 'Cho hình chóp S.ABCD có đáy là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng (ABCD).';

test('01-gv-tao-de-dung-hinh | Tạo đề và dựng hình 3D', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở trang Thêm đề', async () => {
    await s2e3SignIn(page, request, s2e3Teacher(31, test.info()), '/gv/them-de');
    await page.getByTestId('s2e3-page').waitFor();
    await demo.mark(page.getByTestId('s2e3-tab-text'), 1800);
  });
  await gstep(demo, 2, 'Gõ hoặc dán đề bài', async () => {
    await demo.type(page.getByTestId('s2e3-statement'), DE, 40);
    await demo.mark(page.getByTestId('s2e3-length'), 1500);
  });
  await gstep(demo, 3, 'Bấm Dựng hình', async () => {
    await demo.tap(page.getByTestId('s2e3-build'), 1200);
    await page.getByTestId('s2e3-stages').waitFor();
    await demo.mark(page.getByTestId('s2e3-stages'), 2500);
    await page.locator('[data-testid="s2e3-stage-verify"][data-state="done"]').waitFor({ timeout: 25_000 }).catch(() => {});
  });
  await gstep(demo, 4, 'Soát hình, lời giải và nhãn', async () => {
    await page.getByTestId('s2e3-result').waitFor({ timeout: 25_000 });
    await page.getByTestId('s2e3-result').scrollIntoViewIfNeeded();
    await demo.mark(page.getByTestId('s2e3-machine'), 2000);
    await demo.mark(page.getByTestId('s2e3-human'), 2000);
    await page.mouse.wheel(0, 400);
  }, 2500);
});

test('02-gv-giao-bai | Giao bài cho lớp', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở đề cần giao', async () => {
    const sb = await twoClasses(request);
    await e6SignIn(page, sb.teacher, 'Cô Lan', 'teacher');
    await page.goto(`/gv/giao-bai/${PV('HKG-01')}`);
    await page.getByTestId('e6-assign').waitFor();
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Chọn lớp', async () => {
    await demo.tap(page.getByTestId('e6-class-11A1'), 1200);
  });
  await gstep(demo, 3, 'Đặt gợi ý và giờ mở lời giải', async () => {
    await page.mouse.wheel(0, 300);
    await demo.mark(page.getByTestId('e6-assign'), 3000);
  });
  await gstep(demo, 4, 'Bấm Giao bài', async () => {
    await demo.tap(page.getByTestId('e6-assign-submit'), 1200);
    await page.getByTestId('e6-assign-done').waitFor();
    await demo.mark(page.getByTestId('e6-assign-done'), 2500);
  });
});

test('03-gv-theo-doi-lop | Theo dõi lớp', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở trang Theo dõi', async () => {
    const b = await box(request, golden());
    await signInAs(page, b.teacher, 'Cô Lan', 'teacher');
    await page.goto(`/gv/theo-doi/${asg(b, 'BG-1')}`);
    await page.getByTestId('e7-monitor').waitFor();
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Đọc bốn thẻ tổng hợp', async () => {
    for (const c of ['e7-card-opened', 'e7-card-hints', 'e7-card-questions', 'e7-card-time']) await demo.mark(page.getByTestId(c), 1000);
  });
  await gstep(demo, 3, 'Xem từng học sinh', async () => {
    await page.mouse.wheel(0, 450);
    await demo.hold(3000);
  });
});

test('04-gv-soan-de-tu-tai-lieu | Soạn đề bằng AI từ tài liệu', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở tài liệu nguồn', async () => {
    const w = await world(request, S5E3_WORLD);
    const lan = w.people['Cô Lan']!;
    const c11 = w.classes['11A1']!;
    const doc = await tl01(request, lan, c11.id);
    await signIn(page, lan);
    await page.goto(`/gv/ra-de/tu-tai-lieu/${doc.id}?lop=${c11.id}`);
    await page.getByTestId('s5e2-draft').waitFor();
    await demo.hold(2000);
  });
  await gstep(demo, 2, 'Bấm soạn đề nháp', async () => {
    await demo.tap(page.getByTestId('s5e2-draft-run'), 1200);
    await page.locator('[data-testid^="s5e2-draft-item-"]').first().waitFor({ timeout: 20_000 });
  });
  await gstep(demo, 3, 'Xem nhãn kiểm chứng', async () => {
    await demo.mark(page.locator('[data-testid^="s5e2-draft-item-"]').first(), 2500);
    await page.mouse.wheel(0, 500);
    await demo.hold(1500);
  });
  await gstep(demo, 4, 'Chọn và duyệt', async () => {
    await demo.mark(page.getByTestId('s5e2-draft-count'), 1500);
    await demo.mark(page.getByTestId('s5e2-draft-approve'), 2000);
  });
});

test('05-gv-ra-de-ma-tran | Ra đề theo ma trận', async ({ page, request, demo }) => {
  await importBank(request);
  await gstep(demo, 1, 'Mở Ra đề', async () => {
    await signInPage(page, request, LAN);
    await page.goto('/gv/ra-de');
    await page.getByTestId('s5e2-form').waitFor();
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Chọn loại đề', async () => {
    await demo.mark(page.getByTestId('s5e2-loai-de'), 1000);
    await page.getByTestId('s5e2-loai-de').selectOption(KT15);
    await demo.mark(page.getByTestId('s5e2-tong-diem'), 2000);
  });
  await gstep(demo, 3, 'Xem bộ đề và mã đề', async () => {
    const r = await createSet(request, await lanToken(request), { matrixCode: KT15, variantCount: 4, seed: 2026 });
    const set = (await r.json()) as ExamSet;
    await page.goto(`/gv/ra-de/${set.id}`);
    await page.getByTestId('s5e2-nhan-nguoi').waitFor();
    await demo.mark(page.getByTestId('s5e2-nhan-nguoi'), 1500);
    await demo.tap(page.getByTestId('s5e2-ma-101'), 1000);
  });
  await gstep(demo, 4, 'Đổi một câu', async () => {
    await demo.mark(page.getByTestId('s5e2-cau-3'), 1200);
    await demo.tap(page.getByTestId('s5e2-cau-3-thay'), 1200);
  }, 2500);
});

test('06-gv-soan-giao-an | Soạn giáo án theo PPCT', async ({ page, request, demo }) => {
  await lessonPlanOverride(page);
  await gstep(demo, 1, 'Mở Giáo án mới', async () => {
    const w = await world(request);
    await signIn(page, w.people['Cô Lan']!);
    await page.goto('/gv/giao-an/moi');
    await page.getByTestId('s5e1-form').waitFor();
    await demo.hold(1200);
  });
  await gstep(demo, 2, 'Chọn chương, bài, tiết', async () => {
    await demo.mark(page.getByTestId('s5e1-chapter'), 600);
    await page.getByTestId('s5e1-chapter').selectOption('7');
    await demo.mark(page.getByTestId('s5e1-lesson'), 600);
    await page.getByTestId('s5e1-lesson').selectOption('24');
    await page.getByTestId('s5e1-period').selectOption({ index: 1 }).catch(() => {});
    await demo.mark(page.getByTestId('s5e1-period'), 1200);
  });
  await gstep(demo, 3, 'Chọn đối tượng, bấm soạn', async () => {
    await demo.tap(page.getByTestId('s5e1-doituong-kha'), 900);
    await demo.tap(page.getByTestId('s5e1-submit'), 900);
    await page.waitForURL(/\/gv\/giao-an\/[0-9a-f-]{36}$/);
    await page.locator('[data-testid="s5e1-result"]:not([data-status="drafting"])').waitFor({ timeout: 20_000 });
  });
  await gstep(demo, 4, 'Đọc và sửa từng mục', async () => {
    for (let i = 0; i < 3; i++) {
      await page.mouse.wheel(0, 550);
      await demo.hold(1800);
    }
    await demo.mark(page.getByText('chọn để sửa').first(), 1500);
  });
  await gstep(demo, 5, 'Duyệt và tải về', async () => {
    await page.mouse.wheel(0, -5000);
    await demo.hold(800);
    await demo.mark(page.getByText('Duyệt giáo án').first(), 1500);
    await demo.mark(page.getByText('Tải Word').first(), 1200);
  });
});

test('07-gv-kho-tri-thuc | Quản lý kho tri thức', async ({ page, request, demo }) => {
  const c = await qaSetup(request);
  await gstep(demo, 1, 'Mở Kho tri thức', async () => {
    await signIn(page, c.lan);
    await page.goto('/gv/kho-tri-thuc');
    await demo.hold(2000);
  });
  await gstep(demo, 2, 'Xem tài liệu và phiên bản', async () => {
    await page.mouse.wheel(0, 300);
    await demo.hold(3000);
  });
  await gstep(demo, 3, 'Duyệt hoặc lưu trữ', async () => {
    await page.mouse.wheel(0, 300);
    await demo.hold(3000);
  });
});

test('08-gv-rubric | Tạo rubric chấm điểm', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở Rubric', async () => {
    const w = await s5e4World(request);
    const lan = w.people['Cô Lan']!;
    const rb = await rb01(request, lan);
    await signIn(page, lan);
    await page.goto(`/gv/rubric?id=${rb.id}`);
    await page.getByTestId('s5e4-editor').waitFor();
    await demo.hold(1500);
  });
  await gstep(demo, 2, 'Sửa điểm từng tiêu chí', async () => {
    await demo.mark(page.getByTestId('s5e4-crit-points-2'), 800);
    await page.getByTestId('s5e4-crit-points-2').fill('3');
  }, 2500);
  await gstep(demo, 3, 'Kiểm tổng điểm', async () => {
    await page.getByTestId('s5e4-crit-points-2').fill('2');
    await demo.hold(2000);
  });
  await gstep(demo, 4, 'Lưu rubric', async () => {
    await demo.mark(page.getByRole('button', { name: /Lưu/ }).first(), 2000);
  });
});

test('09-gv-cham-bai | Chấm bài từ hàng chờ', async ({ page, request, demo }) => {
  const c = await gradingWorld(request);
  await sc1(request, c);
  await gstep(demo, 1, 'Mở Hàng chờ', async () => {
    await signIn(page, c.lan);
    await page.goto('/gv/hang-cho');
    await page.getByTestId('s5e4-queue-class').selectOption(c.c11.id);
    await page.getByTestId('s5e4-queue-assignment').selectOption(c.bc.id);
    await page.getByTestId('s5e4-queue-summary').waitFor();
    await demo.mark(page.locator('.s5e4-queue-row').first(), 2000);
  });
  await gstep(demo, 2, 'Mở bài làm', async () => {
    // thế giới chấm mới (hàng chờ ở bước 1 đã quá hạn nộp): Bình An + Chi Mai nộp đúng hạn
    const c2 = await gradingWorld(request);
    const an = c2.w.people['Bình An']!;
    const mai = c2.w.people['Chi Mai']!;
    await ack(request, an);
    await submitOk(request, an, c2.bc.id, [P1, P2]);
    await ack(request, mai);
    const sub = await submitOk(request, mai, c2.bc.id, [P1, P2]);
    await signIn(page, c2.lan);
    await page.goto(`/gv/cham/${sub.id}`);
    await page.getByTestId('s5e4-review-rubric').waitFor();
    await demo.hold(2500);
  });
  await gstep(demo, 3, 'Nhập điểm từng tiêu chí', async () => {
    const pts = ['2', '3', '2', '1', '1'];
    for (let k = 0; k < 5; k++) {
      await page.getByTestId(`s5e4-crit-input-${k + 1}`).fill(pts[k]!);
      await demo.hold(450);
    }
    await demo.mark(page.getByTestId('s5e4-review-total'), 1500);
  });
  await gstep(demo, 4, 'Nhận xét và duyệt', async () => {
    await demo.type(page.getByTestId('s5e4-review-comment'), 'Lập luận tốt, xem lại bước tính AH.', 45);
    await demo.tap(page.getByTestId('s5e4-review-approve'), 1000);
    await page.getByTestId('s5e4-review-toast').waitFor();
  }, 2500);
});

test('10-gv-nhan-ai-de-xuat-diem | Nhận điểm AI đề xuất', async ({ page, request, demo }) => {
  await handwrittenImages(page);
  const c = await s5e6Setup(request, 'ai_proposal');
  await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
  const { sub } = await s5e6Submit(request, c, 'Bình An');
  await waitAi(request, c.lan, sub.id);
  await gstep(demo, 1, 'Mở bài có đề xuất của AI', async () => {
    await signIn(page, c.lan);
    await page.goto(`/gv/cham-ai/${sub.id}`);
    await page.getByTestId('s5e6-ai-points-2').waitFor();
    await demo.mark(page.getByTestId('s5e6-ai-points-2'), 2000);
  });
  await gstep(demo, 2, 'Xem vùng AI dựa vào', async () => {
    await demo.tap(page.getByTestId('s5e6-crit-name-2'), 1000);
    await page.getByTestId('s5e6-region').waitFor();
    await demo.mark(page.getByTestId('s5e6-region'), 2500);
  });
  await gstep(demo, 3, 'Nhận, sửa hoặc bỏ', async () => {
    for (let k = 1; k <= 5; k++) await demo.tap(page.getByTestId(`s5e6-accept-${k}`), 600);
  });
  await gstep(demo, 4, 'Duyệt điểm', async () => {
    await demo.tap(page.getByTestId('s5e6-approve'), 1200);
  }, 2500);
});

test('11-gv-xem-bai-viet-tay | Xem bài viết tay và lỗi chung của lớp', async ({ page, request, demo }) => {
  const c = await graded(request);
  await summary(request, c.lan, c.bc.id);
  await gstep(demo, 1, 'Mở trang Lỗi của lớp', async () => {
    await signIn(page, c.lan);
    await page.goto(`/gv/bai-co-cham/${c.bc.id}/loi-cua-lop`);
    await page.getByTestId('s5e5-insight-1').waitFor({ timeout: 20_000 });
    await demo.mark(page.getByTestId('s5e5-insight-1'), 2500);
  });
  await gstep(demo, 2, 'Mở bằng chứng', async () => {
    await demo.tap(page.getByTestId('s5e5-evidence-1-2'), 1000);
    await page.getByTestId('s5e4-review').waitFor();
    await demo.hold(3500);
  });
  await gstep(demo, 3, 'Gợi ý bộ luyện nhắc lại', async () => {
    await page.goBack();
    await page.getByTestId('s5e5-insight-1').waitFor({ timeout: 20_000 });
    // danh sách nhắc lại còn LaTeX thô (ClassErrorsPage, ngoài T-74) → chỉ khoanh nút Nhắc lại
    await demo.mark(page.getByTestId('s5e5-remind-1'), 3500);
  });
});

test('12-gv-trinh-chieu | Trình chiếu lời giải trên lớp', async ({ page, request, demo }) => {
  await gstep(demo, 1, 'Mở chế độ trình chiếu', async () => {
    await present(page, request, test.info(), 1);
    await demo.hold(2500);
  });
  await gstep(demo, 2, 'Mở từng bước', async () => {
    await demo.tap(page.getByTestId('s2e7-next'), 1200);
    await demo.hold(2500);
  });
  await gstep(demo, 3, 'Dùng phím mũi tên', async () => {
    await page.evaluate(() => (document.activeElement as HTMLElement | null)?.blur());
    await page.keyboard.press('ArrowRight');
    await demo.hold(2500);
    await page.keyboard.press('ArrowLeft');
    await demo.hold(1500);
    await page.keyboard.press('ArrowRight');
  }, 2500);
});
