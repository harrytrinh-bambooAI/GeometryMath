import { expect, test } from '../../support/fixtures.ts';
import { drag, snap, tapEdge, tapPoint, tool, waitScene } from '../e3/e3.ts';
import { API, bearer, token } from '../s2e6/s2e6.ts';
import { K, MSG, STEP1, blur, clickPresent, nextBtn, openDoc, present, prevBtn, shown, stage } from './s2e7.ts';

/** S2-E7 — Trình chiếu (C2-04): bước, che, phím, toàn màn hình, nhãn, hình (US-S2E7-01); tải sẵn, mất mạng (US-S2E7-02). */

test('@TC-S2E7-001 vào Trình chiếu thấy hình lớn, đề và lời giải che sẵn', async ({ page, request }, info) => {
  await present(page, request, info, 1);
  await shown(page, 0);
  await expect(page.getByTestId('s2e7-statement')).toContainText('hình chóp S.ABCD');
  await expect(page.getByTestId('viewer-canvas')).toBeVisible();
  await expect(page.locator('[data-testid^="s2e7-step-"]')).toHaveCount(0);
  for (const id of ['s2e7-next', 's2e7-prev', 's2e7-cover']) await expect(page.getByTestId(id)).toBeVisible();
  await expect(nextBtn(page)).toHaveText(/Hiện bước kế/);
  await expect(page.getByTestId('s2e7-cover')).toHaveText(/Che lời giải/);
  await expect(stage(page)).not.toHaveAttribute('data-fullscreen', 'pending');
  const box = (await stage(page).boundingBox())!;
  const vp = page.viewportSize()!;
  expect(box.width).toBeGreaterThanOrEqual(vp.width - 1);
  expect(box.height).toBeGreaterThanOrEqual(vp.height - 1);
});

test('@TC-S2E7-002 hiện bước kế hiện đúng một bước và tô màu hình', async ({ page, request }, info) => {
  await present(page, request, info, 2);
  await nextBtn(page).click();
  await shown(page, 1);
  await expect(page.getByTestId('s2e7-step-1')).toHaveText(STEP1);
  await expect(page.getByTestId('s2e7-step-2')).toHaveCount(0);
  const hl = (await stage(page).getAttribute('data-highlight'))!.split(',');
  expect(hl).toEqual(expect.arrayContaining(['AC', 'SC', 'SCA']));
});

test('@TC-S2E7-003 phím mũi tên và Space điều khiển bước', async ({ page, request }, info) => {
  await present(page, request, info, 3);
  await nextBtn(page).click();
  await shown(page, 1);
  await blur(page);
  await page.keyboard.press('ArrowRight');
  await shown(page, 2);
  await page.keyboard.press('Space');
  await shown(page, 3);
  await page.keyboard.press('ArrowLeft');
  await shown(page, 2);
  await expect(page.getByTestId('s2e7-step-3')).toHaveCount(0);
});

test('@TC-S2E7-004 che lời giải ẩn tất cả, bỏ che hiện lại đúng bước', async ({ page, request }, info) => {
  await present(page, request, info, 4);
  for (let i = 0; i < 3; i++) await nextBtn(page).click();
  await shown(page, 3);
  await page.getByTestId('s2e7-cover').click();
  await shown(page, 0);
  await expect(stage(page)).toHaveAttribute('data-covered', 'true');
  await expect(stage(page)).toHaveAttribute('data-highlight', '');
  await expect(page.locator('[data-testid^="s2e7-step-"]')).toHaveCount(0);
  await page.getByTestId('s2e7-cover').click();
  await shown(page, 3);
  await expect(page.locator('[data-testid^="s2e7-step-"]')).toHaveCount(3);
});

test('@TC-S2E7-005 xoay hình, đo, thêm đường phụ thì hình đổi ngay', async ({ page, request }, info) => {
  const s = await present(page, request, info, 5);
  await waitScene(page);
  const yaw0 = (await snap(page)).camera.yaw;
  await drag(page, 120);
  await expect.poll(async () => (await snap(page)).camera.yaw).not.toBe(yaw0);
  await tool(page, 'length');
  await tapPoint(page, 'A');
  await tapPoint(page, 'C');
  await expect.poll(async () => (await snap(page)).measures.map((m) => m.text).join(' ')).toContain('a√2'); // AC = a√2
  const ops0 = (await snap(page)).opCount;
  await tool(page, 'point');
  await tapEdge(page, 'BC', 0.45);
  await page.getByTestId('viewer-point-mid').click();
  await expect.poll(async () => (await snap(page)).points.some((p) => p.user)).toBe(true);
  const mid = (await snap(page)).points.find((p) => p.user)!.name;
  await tool(page, 'line');
  await tapPoint(page, 'A');
  await tapPoint(page, mid);
  await expect.poll(async () => (await snap(page)).opCount).toBeGreaterThan(ops0 + 1);
  // các thao tác không đổi dữ liệu đề đã lưu
  const again = await request.get(`${API}/api/v1/teacher/present/${s.doc(K.FX14)}`, { headers: bearer(s.token) });
  const body = (await again.json()) as { steps: string[]; statement: string };
  expect(body.steps).toHaveLength(4);
  expect(body.statement).toContain('hình chóp S.ABCD');
});

test('@TC-S2E7-006 ở bước cuối nút Hiện bước kế khóa', async ({ page, request }, info) => {
  await present(page, request, info, 6);
  for (let i = 0; i < 4; i++) await nextBtn(page).click();
  await shown(page, 4);
  await expect(nextBtn(page)).toBeDisabled();
  await expect(nextBtn(page)).toHaveText(new RegExp(MSG.lastStep));
  await blur(page);
  await page.keyboard.press('ArrowRight');
  await shown(page, 4);
});

test('@TC-S2E7-007 ở bước 0 nút Bước trước khóa', async ({ page, request }, info) => {
  await present(page, request, info, 7);
  await expect(prevBtn(page)).toBeDisabled();
  await blur(page);
  await page.keyboard.press('ArrowLeft');
  await shown(page, 0);
});

test('@TC-S2E7-008 lời giải 30 bước chiếu đủ 30 bước, chuyển bước dưới 200 ms', async ({ page, request }, info) => {
  await present(page, request, info, 8, K.STEPS30);
  const times = await page.evaluate(async () => {
    const btn = document.querySelector<HTMLButtonElement>('[data-testid="s2e7-next"]')!;
    const out: number[] = [];
    for (let i = 1; i <= 30; i++) {
      const t0 = performance.now();
      btn.click();
      while (!document.querySelector(`[data-testid="s2e7-step-${i}"]`)) await new Promise((r) => requestAnimationFrame(r));
      out.push(performance.now() - t0);
    }
    return out;
  });
  expect(times).toHaveLength(30);
  expect(Math.max(...times)).toBeLessThan(200);
  await shown(page, 30);
  await expect(nextBtn(page)).toBeDisabled();
});

test('@TC-S2E7-009 Esc thoát về màn đề và che lại lời giải', async ({ page, request }, info) => {
  await present(page, request, info, 9);
  await nextBtn(page).click();
  await nextBtn(page).click();
  await shown(page, 2);
  await blur(page);
  await page.keyboard.press('Escape');
  await expect(page.getByTestId('s2e6-doc')).toBeVisible();
  await expect(stage(page)).toHaveCount(0);
  await clickPresent(page);
  await shown(page, 0);
});

test('@TC-S2E7-010 trình duyệt từ chối toàn màn hình vẫn chiếu được', async ({ page, request }, info) => {
  await page.addInitScript(() => {
    Element.prototype.requestFullscreen = function () {
      return Promise.reject(new Error('denied'));
    };
  });
  await present(page, request, info, 10);
  await expect(page.getByTestId('s2e7-no-fullscreen')).toHaveText(MSG.noFullscreen);
  await expect(stage(page)).toHaveAttribute('data-fullscreen', 'fallback');
  await nextBtn(page).click();
  await shown(page, 1);
  await page.getByTestId('s2e7-cover').click();
  await shown(page, 0);
});

test('@TC-S2E7-012 đề chưa có lời giải thì khóa nút hiện bước', async ({ page, request }, info) => {
  await present(page, request, info, 12, K.FX12_NONE);
  await expect(page.getByTestId('s2e7-statement')).toContainText('Hình chữ nhật ABCD');
  await expect(page.getByTestId('s2e7-figure')).toBeVisible();
  await expect(nextBtn(page)).toBeDisabled();
  await expect(nextBtn(page)).toHaveText(new RegExp(MSG.noSolution));
});

test('@TC-S2E7-013 hai nhãn luôn nằm ở góc màn hình', async ({ page, request }, info) => {
  await present(page, request, info, 13);
  const labels = page.getByTestId('s2e7-labels');
  const check = async () => {
    await expect(labels.getByTestId('s2e7-machine')).toHaveText('Cần xem lại');
    await expect(labels.getByTestId('s2e7-human')).toHaveText('Giáo viên đã duyệt');
    const b = (await labels.boundingBox())!;
    expect(b.y).toBeLessThan(120); // dải trên cùng
    expect(b.x + b.width).toBeGreaterThan(page.viewportSize()!.width * 0.5); // nửa phải
  };
  await check();
  await nextBtn(page).click();
  await check();
  await page.getByTestId('s2e7-cover').click();
  await check();
});

test('@TC-S2E7-015 vào Trình chiếu có mạng thì hiện chip Đã tải sẵn', async ({ page, request }, info) => {
  await present(page, request, info, 15);
  await expect(page.getByTestId('s2e7-preloaded')).toHaveText(MSG.preloaded);
});

test('@TC-S2E7-016 mất mạng giữa giờ vẫn hiện bước và xoay hình', async ({ page, request, context }, info) => {
  await present(page, request, info, 16);
  await waitScene(page);
  await context.setOffline(true);
  await nextBtn(page).click();
  await nextBtn(page).click();
  await shown(page, 2);
  const yaw0 = (await snap(page)).camera.yaw;
  await drag(page, 120);
  await expect.poll(async () => (await snap(page)).camera.yaw).not.toBe(yaw0);
  await tool(page, 'length');
  await tapPoint(page, 'S');
  await tapPoint(page, 'A');
  await expect.poll(async () => (await snap(page)).measures.length).toBeGreaterThan(0);
  await expect(page.getByTestId('s2e7-error')).toHaveCount(0);
  await expect(page.getByTestId('shell-error')).toHaveCount(0);
  await context.setOffline(false);
});

test('@TC-S2E7-017 mất mạng thì hỏi đáp AI tắt kèm thông báo, không gửi yêu cầu', async ({ page, request, context }, info) => {
  await present(page, request, info, 17);
  await context.setOffline(true);
  const sent: string[] = [];
  page.on('request', (r) => sent.push(r.url()));
  await expect(page.getByTestId('s2e7-ai')).toBeDisabled();
  await page.getByTestId('s2e7-ai').click({ force: true });
  await expect(page.getByTestId('s2e7-ai-offline')).toHaveText(MSG.aiOffline);
  await expect(page.getByTestId('s2e7-ai-panel')).toHaveCount(0);
  expect(sent.filter((u) => u.includes('/api/'))).toHaveLength(0);
  await context.setOffline(false);
});

test('@TC-S2E7-018 có mạng lại thì hỏi đáp AI bật lại trong 5 giây', async ({ page, request, context }, info) => {
  await present(page, request, info, 18);
  await context.setOffline(true);
  await expect(page.getByTestId('s2e7-ai')).toBeDisabled();
  await context.setOffline(false);
  const t0 = Date.now();
  await expect(page.getByTestId('s2e7-ai')).toBeEnabled({ timeout: 5000 });
  expect(Date.now() - t0).toBeLessThanOrEqual(5000);
  await expect(page.getByTestId('s2e7-ai-offline')).toHaveCount(0);
});

test('@TC-S2E7-020 vào Trình chiếu khi đã mất mạng và chưa tải sẵn', async ({ page, request, context }, info) => {
  await openDoc(page, request, info, 20);
  await context.setOffline(true);
  await clickPresent(page);
  await expect(page.getByTestId('s2e7-network-msg')).toHaveText(MSG.needNetwork);
  await expect(page.getByTestId('s2e7-retry')).toBeVisible();
  await expect(stage(page)).toHaveCount(0);
  await expect(page.getByTestId('viewer-canvas')).toHaveCount(0);
  await context.setOffline(false);
});

test('@TC-S2E7-021 mất mạng khi đang tải sẵn thì báo chưa đủ', async ({ page, request }, info) => {
  const s = await openDoc(page, request, info, 21);
  await page.route(`**/api/v1/teacher/present/${s.doc(K.FX14)}`, (route) => route.abort('internetdisconnected'));
  await clickPresent(page);
  await expect(page.getByTestId('s2e7-network-msg')).toHaveText(MSG.partial);
  await expect(page.getByTestId('s2e7-retry')).toBeVisible();
  await expect(stage(page)).toHaveCount(0);
});

test('@TC-S2E7-022 thoát Trình chiếu xóa dữ liệu tải sẵn khỏi bộ nhớ trình duyệt', async ({ page, request }, info) => {
  await present(page, request, info, 22);
  for (let i = 0; i < 4; i++) await nextBtn(page).click();
  await shown(page, 4);
  await blur(page);
  await page.keyboard.press('Escape');
  await expect(page.getByTestId('s2e6-doc')).toBeVisible();
  const stored = await page.evaluate(async () => {
    const parts: string[] = [];
    for (const st of [localStorage, sessionStorage]) for (let i = 0; i < st.length; i++) parts.push(st.getItem(st.key(i)!) ?? '');
    if ('caches' in window) {
      for (const name of await caches.keys()) {
        const c = await caches.open(name);
        for (const req of await c.keys()) parts.push(await (await c.match(req))!.text());
      }
    }
    return parts.join('\n');
  });
  for (const leak of ['tan SCA', 'hình chiếu của SC', '45°']) expect(stored).not.toContain(leak);
  await expect(page.getByText('tan SCA')).toHaveCount(0);
});

test('@TC-S2E7-023 phiên hết hạn lúc mất mạng thì chiếu tiếp, về lại thì đăng nhập', async ({ page, request, context }, info) => {
  test.setTimeout(90_000);
  const s = await present(page, request, info, 23, K.FX14, 12);
  await context.setOffline(true);
  await page.waitForTimeout(13_000); // thẻ hết hạn trong lúc mất mạng
  await nextBtn(page).click();
  await shown(page, 1);
  await context.setOffline(false);
  const r = await request.get(`${API}/api/v1/teacher/present/${s.doc(K.FX14)}/preflight`, { headers: bearer(s.token) });
  expect(r.status()).toBe(401);
  expect((await r.json()).code).toBe('UNAUTHENTICATED');
  await expect(stage(page)).toBeVisible(); // không ngắt buổi chiếu
  await nextBtn(page).click();
  await shown(page, 2);
  await page.getByTestId('s2e7-exit').click();
  await expect(page.getByTestId('s2e7-expired')).toContainText(MSG.expired);
});

test('@TC-S2E7-024 đề đang giao và lời giải khóa thì hỏi lại', async ({ page, request }, info) => {
  await openDoc(page, request, info, 24);
  await clickPresent(page);
  const ask = page.getByTestId('s2e7-ask');
  await expect(ask).toContainText(MSG.ask);
  await expect(page.getByTestId('s2e7-ask-classes')).toHaveText('11A1');
  await expect(page.getByTestId('s2e7-ask-continue')).toHaveText(/Tiếp tục/);
  await expect(page.getByTestId('s2e7-ask-cancel')).toHaveText(/Hủy/);
  await expect(stage(page)).toHaveCount(0);
});

test('@TC-S2E7-025 Tiếp tục thì vào chiếu, máy học sinh vẫn khóa', async ({ page, request }, info) => {
  const s = await openDoc(page, request, info, 25);
  await clickPresent(page);
  await page.getByTestId('s2e7-ask-continue').click();
  await expect(stage(page)).toBeVisible();
  await shown(page, 0);
  await expect(page.getByTestId('s2e7-student-locked')).toHaveText(MSG.studentLocked);
  await nextBtn(page).click();
  await nextBtn(page).click();
  await shown(page, 2);
  const an = await token(request, s.org, s.an, 'STUDENT');
  const sol = await request.get(`${API}/api/v1/student/assignments/${s.bg(9001)}/solution`, { headers: bearer(an) });
  const body = await sol.text();
  for (const leak of ['45°', 'tan SCA', 'hình chiếu']) expect(body).not.toContain(leak);
});

test('@TC-S2E7-026 Hủy thì ở lại màn đề và không thay đổi gì', async ({ page, request }, info) => {
  await openDoc(page, request, info, 26);
  await clickPresent(page);
  await page.getByTestId('s2e7-ask-cancel').click();
  await expect(page.getByTestId('s2e6-doc')).toBeVisible();
  await expect(stage(page)).toHaveCount(0);
  await expect.poll(() => page.evaluate(() => Boolean(document.fullscreenElement))).toBe(false);
});

test('@TC-S2E7-027 đề chưa giao thì không hỏi', async ({ page, request }, info) => {
  await openDoc(page, request, info, 27, K.FX12_NONE);
  await clickPresent(page);
  await expect(stage(page)).toBeVisible();
  await expect(page.getByTestId('s2e7-ask')).toHaveCount(0);
});

test('@TC-S2E7-028 bài giao đã mở lời giải thì không hỏi', async ({ page, request }, info) => {
  await openDoc(page, request, info, 28);
  await clickPresent(page);
  await expect(stage(page)).toBeVisible();
  await expect(page.getByTestId('s2e7-ask')).toHaveCount(0);
  await expect(page.getByTestId('s2e7-student-locked')).toHaveCount(0);
});

test('@TC-S2E7-030 chưa đặt giờ mở thì cũng hỏi', async ({ page, request }, info) => {
  await openDoc(page, request, info, 30);
  await clickPresent(page);
  await expect(page.getByTestId('s2e7-ask')).toContainText(MSG.ask);
  await expect(page.getByTestId('s2e7-ask-classes')).toHaveText('11A3');
});

test('@TC-S2E7-031 nhiều lớp thì chỉ nêu tên lớp còn khóa', async ({ page, request }, info) => {
  await openDoc(page, request, info, 31);
  await clickPresent(page);
  await expect(page.getByTestId('s2e7-ask-classes')).toHaveText('11A1');
  await expect(page.getByTestId('s2e7-ask')).not.toContainText('11A2');
});
