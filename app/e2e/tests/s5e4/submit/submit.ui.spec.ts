import type { Page } from '@playwright/test';
import { expect, test } from '../../../support/fixtures.ts';
import { API, bearer, signIn, type Person } from '../../s3e1/s3e1.ts';
import { setup } from '../s5e4.ts';
import { FIX, MSG, SUB, ack, count, getSub, type Submission } from './submit.ts';

/**
 * US-S5E4-03 (màn C5-03 trên trình duyệt) + US-S5E4-09-FC1: học sinh chụp trang (input capture của iPhone; Playwright
 * nạp tệp ảnh mẫu), xếp trang, kiểm độ rõ, thông báo riêng tư, hết chỗ lưu, mất mạng rồi tự gửi lại. Thiết bị iOS thật
 * (camera, HEIC từ thư viện) chạy tay ở vòng pilot.
 */

async function open(page: Page, p: Person, assignmentId: string) {
  await signIn(page, p);
  await page.goto(`/hs/nop-bai?bc=${assignmentId}`);
  await expect(page.getByTestId('s5e4-hs-assignment')).toBeVisible();
  await page.getByTestId('s5e4-start').click();
}

/** Chụp một trang (nạp tệp vào input capture) rồi bấm "Dùng ảnh này". */
async function shoot(page: Page, file: string, retakeOf?: number) {
  if (retakeOf) {
    const chooser = page.waitForEvent('filechooser');
    await page.getByTestId(`s5e4-retake-${retakeOf}`).click();
    await (await chooser).setFiles(FIX(file));
  } else {
    await page.getByTestId('s5e4-file').setInputFiles(FIX(file));
  }
  await expect(page.getByTestId('s5e4-candidate')).toBeVisible();
  await page.getByTestId('s5e4-use').click();
  await expect(page.getByTestId('s5e4-candidate')).toHaveCount(0);
  await expect(page.getByTestId('s5e4-capture-area')).toHaveAttribute('data-busy', 'false');
}

const shaOf = (page: Page, n: number) => page.getByTestId(`s5e4-page-${n}`).getAttribute('data-sha');

test('Chụp 2 trang, xếp lại, nộp @TC-S5E4-12', async ({ page, request }) => {
  const { w, lan, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an); // đã đọc thông báo riêng tư
  await open(page, an, bc.id);
  await expect(page.getByTestId('s5e4-privacy')).toHaveCount(0);
  await shoot(page, 'bn01-p1.jpg');
  await shoot(page, 'bn01-p2.jpg');
  await expect(page.getByTestId('s5e4-page-2')).toHaveAttribute('data-uploaded', 'true');
  const sha1 = await shaOf(page, 1);
  const sha2 = await shaOf(page, 2);
  // kéo trang 2 lên trước rồi kéo lại
  await page.getByTestId('s5e4-up-2').click();
  expect(await shaOf(page, 1)).toBe(sha2);
  await page.getByTestId('s5e4-down-1').click();
  expect(await shaOf(page, 1)).toBe(sha1);
  await page.getByTestId('s5e4-submit').click();
  // MSG-S5-33 với giờ máy chủ
  await expect(page.getByTestId('s5e4-submitted-msg')).toHaveText(MSG.s33);
  // BN-01 có 2 trang đúng thứ tự cuối cùng; băm ảnh gốc trên máy chủ trùng băm tệp đã tải lên
  const mine = (await (await request.get(`${SUB}/mine?assignmentId=${bc.id}`, { headers: bearer(an.token) })).json()) as { submission: Submission };
  const s = await getSub(request, lan, mine.submission.id);
  expect(s.pages.map((p) => p.sha256)).toEqual([sha1, sha2]);
  expect(s.message).toBe(await page.getByTestId('s5e4-submitted-msg').textContent());
});

test('Lần nộp đầu phải xác nhận thông báo riêng tư có nói xử lý bằng AI @TC-S5E4-14', async ({ page, request }) => {
  const { w, bc } = await setup(request);
  const mai = w.people['Chi Mai']!;
  await open(page, mai, bc.id);
  const text = page.getByTestId('s5e4-privacy-text');
  await expect(text).toContainText('được xử lý bằng AI');
  await expect(text).toContainText('xoá cuối năm học');
  // trước khi bấm, nút Chụp mờ
  await expect(page.getByTestId('s5e4-capture')).toBeDisabled();
  await page.getByTestId('s5e4-ack').click();
  await expect(page.getByTestId('s5e4-privacy')).toHaveCount(0);
  await expect(page.getByTestId('s5e4-capture')).toBeEnabled();
  // cờ đã đọc lưu phiên bản privacy-s5-1.0
  const r = await request.get(`${SUB}/privacy`, { headers: bearer(mai.token) });
  expect(await r.json()).toEqual(expect.objectContaining({ version: 'privacy-s5-1.0', acked: true }));
});

test('Ảnh mờ bị yêu cầu chụp lại ngay @TC-S5E4-15', async ({ page, request }) => {
  const { w, bc } = await setup(request);
  const huy = w.people['Đức Huy']!;
  await ack(request, huy);
  await open(page, huy, bc.id);
  await shoot(page, 'bn01-p1.jpg');
  // chụp trang 2 nhoè rồi bấm "Dùng ảnh này"
  await shoot(page, 'bn03-p2-mo.jpg');
  expect(Number(await page.getByTestId('s5e4-page-2').getAttribute('data-clarity'))).toBeLessThan(0.5);
  await expect(page.getByTestId('s5e4-msg32-2')).toHaveText('Ảnh trang 2 hơi mờ. Em chụp lại trang này nhé.');
  await expect(page.getByTestId('s5e4-submit')).toBeDisabled();
  // chụp lại trang 2 đạt → hết cảnh báo, nút Nộp bật
  await shoot(page, 'bn01-p2.jpg', 2);
  expect(Number(await page.getByTestId('s5e4-page-2').getAttribute('data-clarity'))).toBeGreaterThanOrEqual(0.5);
  await expect(page.getByTestId('s5e4-msg32-2')).toHaveCount(0);
  await expect(page.getByTestId('s5e4-submit')).toBeEnabled();
});

test('Máy không đủ chỗ lưu nháp thì báo, trang đã tải không mất @TC-S5E4-17', async ({ page, request }) => {
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await open(page, an, bc.id);
  await shoot(page, 'bn01-p1.jpg');
  await expect(page.getByTestId('s5e4-page-1')).toHaveAttribute('data-uploaded', 'true');
  // trình duyệt báo hết hạn mức lưu trữ từ đây
  await page.evaluate(() => {
    IDBObjectStore.prototype.put = function () {
      throw new DOMException('Hết hạn mức lưu trữ', 'QuotaExceededError');
    };
  });
  await shoot(page, 'bn01-p2.jpg');
  await expect(page.getByTestId('s5e4-full')).toHaveText(MSG.full);
  // trang 1 vẫn còn trên máy chủ (lượt tải chưa nộp)
  const st = await request.get(`${API}/test-hooks/s5e4/submit/state?${new URLSearchParams({ orgId: w.orgId, submissionId: bc.id, studentId: an.id })}`);
  expect(((await st.json()) as { uploads: number }).uploads).toBeGreaterThanOrEqual(1);
  await expect(page.getByTestId('s5e4-page-1')).toBeVisible();
});

test('Mất mạng khi nộp: giữ nháp, tự gửi lại @TC-S5E4-49', async ({ page, context, request }) => {
  const { w, bc } = await setup(request);
  const an = w.people['Bình An']!;
  await ack(request, an);
  await open(page, an, bc.id);
  await shoot(page, 'bn01-p1.jpg');
  await shoot(page, 'bn01-p2.jpg');
  await expect(page.getByTestId('s5e4-page-2')).toHaveAttribute('data-uploaded', 'true');
  // mạng tắt rồi bấm Nộp
  await context.setOffline(true);
  await page.getByTestId('s5e4-submit').click();
  await expect(page.getByTestId('s5e4-offline')).toHaveText(MSG.s35);
  await expect(page.getByTestId('s5e4-draft-saved')).toBeVisible();
  expect(await count(request, w.orgId, bc.id, an.id)).toBe(0);
  // bật lại mạng: bài tự gửi, đúng 1 bài nộp trên máy chủ
  await context.setOffline(false);
  await expect(page.getByTestId('s5e4-submitted-msg')).toHaveText(MSG.s33, { timeout: 15_000 });
  expect(await count(request, w.orgId, bc.id, an.id)).toBe(1);
});
