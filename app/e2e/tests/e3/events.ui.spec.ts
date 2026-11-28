import { expect, test } from '../../support/fixtures.ts';
import { drag, e3Lane, pinch, gotoWorkspace, laneProgressE3, signInE3Student, snap, tapEdge, tool } from './e3.ts';

/**
 * TC-GEO-0309 (US-GEO-301-FC2; BR-GEO-144) bằng thao tác THẬT trên trình duyệt (soát E3 #4: bản API cũ không xoay gì
 * nên luôn đúng). Làn E3 riêng (k = 5 / 6, chạy lại +6; QF-E3-D01) thay cho Hà ở BG-1 để không lẫn số đếm với
 * TC-GEO-0313 và giữa hai trình duyệt chạy song song.
 * Kiểm cả chiều ngược: sau đó một lần "Chọn" thật thì số thao tác thành 1 — chứng minh đường gửi sự kiện hoạt động, nên
 * số 0 trước đó là do xoay / thu phóng / "Đặt lại" không được tính, không phải do không gửi gì.
 */
test('Xoay và thu phóng không tính là thao tác trên hình @TC-GEO-0309', async ({ page, request }, testInfo) => {
  const lane = e3Lane(testInfo, '0309');
  await signInE3Student(page, request, lane.studentId);
  await gotoWorkspace(page, lane.assignmentId); // mở bài lần đầu: một sự kiện mở bài

  const v0 = (await snap(page)).camera;
  await drag(page, -200, 30); // xoay
  // thu phóng: bánh xe chuột trên máy tính, hai ngón trên iPad (WebKit di động không có bánh xe)
  if (testInfo.project.name === 'ui-chromium') {
    const box = (await snap(page)).viewport;
    await page.mouse.move(box.left + box.width / 2, box.top + box.height / 2);
    await page.mouse.wheel(0, -240);
  } else {
    await pinch(page, 60, 180);
  }
  await expect.poll(async () => (await snap(page)).camera.zoom).not.toBe(v0.zoom);
  expect((await snap(page)).camera.yaw).not.toBe(v0.yaw);
  await tool(page, 'reset'); // "Đặt lại"
  await expect.poll(async () => (await snap(page)).camera.yaw).toBe(v0.yaw);
  expect((await snap(page)).opCount).toBe(0);

  // rời bài (pagehide gửi mọi lô còn chờ), rồi xem Theo dõi lớp (đệm 5 giây)
  await page.waitForTimeout(800);
  await page.goto('about:blank');
  await expect
    .poll(async () => {
      const p = await laneProgressE3(request, lane.assignmentId);
      return { opened: p.opened, toolCount: p.toolCount };
    }, { timeout: 20_000, intervals: [500, 1000, 2000] })
    .toEqual({ opened: true, toolCount: 0 });

  // đối chứng: một lần "Chọn" thật được tính đúng 1
  await gotoWorkspace(page, lane.assignmentId);
  await tool(page, 'select');
  await tapEdge(page, 'SD');
  await expect.poll(async () => (await snap(page)).opCount).toBe(1);
  await page.waitForTimeout(800);
  await page.goto('about:blank');
  await expect
    .poll(async () => (await laneProgressE3(request, lane.assignmentId)).toolCount, { timeout: 20_000, intervals: [500, 1000, 2000] })
    .toBe(1);
});
