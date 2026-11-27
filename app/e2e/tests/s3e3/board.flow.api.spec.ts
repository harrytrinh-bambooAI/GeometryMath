import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { R, WEEK15, bearer, board051, openBoard, setUrl } from './s3e3.ts';

/**
 * C3-04, ca LOẠI B của `board.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): ô chọn bộ và bảng lớp × bài
 * do các ca loại A cùng tệp giữ; ca này chỉ kiểm dữ liệu "bộ Nháp không có trong danh sách" bằng gọi API, cuối luồng mở
 * màn Kết quả lớp (không click), kiểm các lựa chọn của ô chọn bộ và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

const WEEK16 = 'Bộ bài tuần 16';

test('@TC-S3E3-14 @loai-B bộ Nháp không có trong danh sách chọn bộ; gọi thẳng API nhận NOT_FOUND', async ({ request, browser }, testInfo) => {
  const sb = await board051(request, { moreSets: [{ title: WEEK16, status: 'draft', items: 2 }] });
  const sets = await request.get(`${R}/${sb.classes[0]!.id}/sets`, { headers: bearer(sb.teacher.token) });
  expect(sets.status(), await sets.text()).toBe(200);
  expect(((await sets.json()) as { items: Array<{ title: string }> }).items.map((s) => s.title)).toEqual([WEEK15]);
  const r = await request.get(`${setUrl(sb, 0, 1)}/board`, { headers: bearer(sb.teacher.token) });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  await captureFinalState(browser, testInfo, async (page) => {
    await openBoard(page, sb, 0, null);
    await expect(page.getByTestId('s3e3-set-select').locator('option')).toHaveText([WEEK15]);
  });
});
