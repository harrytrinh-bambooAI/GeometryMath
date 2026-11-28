import type { APIRequestContext, Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, answerBox, assigned, attempt, bearer, noHorizontalScroll, openPlay, play, report, sandbox, signIn, student, submit, type Sandbox, type SetDetail } from './s2e8.ts';

/**
 * S2-E8 · US-S2E8-05 soạn bài luyện (C2-08), giao bài, báo "Em nghĩ đáp số này đúng" và màn "Báo chấm sai" của giáo
 * viên. Giáo viên dùng web (ui-chromium, ui-webkit iPad).
 */

async function openCompose(page: Page, sb: Sandbox) {
  await signIn(page, sb.teacher, 'TEACHER', 'Cô Hạnh');
  await page.goto('/gv/bai-luyen/moi');
  await expect(page.getByTestId('s2e8-compose')).toBeVisible();
}
const tick = (page: Page, code: string) => page.getByTestId(`s2e8-problem-tick-${code}`).check();
const FIVE = ['LC-01', 'SP-01', 'LA-01', 'LB-01', 'HCN-01'];

async function teacherSets(request: APIRequestContext, sb: Sandbox): Promise<SetDetail[]> {
  const list = (await (await request.get(`${API}/api/v1/teacher/practice-sets`, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ id: string }> };
  const out: SetDetail[] = [];
  for (const s of list.items) out.push((await (await request.get(`${API}/api/v1/teacher/practice-sets/${s.id}`, { headers: bearer(sb.teacher.token) })).json()) as SetDetail);
  return out;
}

/** Giá trị cho ô datetime-local theo giờ của trình duyệt đang chạy ca kiểm. */
function localInput(page: Page, offsetHours: number): Promise<string> {
  return page.evaluate((h) => {
    const d = new Date(Date.now() + h * 3_600_000);
    const p = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
  }, offsetHours);
}

test('@TC-S2E8-088 chọn đề có đáp số và tạo bài nháp', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  for (const c of FIVE) await tick(page, c);
  await expect(page.getByTestId('s2e8-compose-count')).toHaveText('Chọn đề (5)');
  await page.getByTestId('s2e8-compose-title').fill('Góc trong hình chóp');
  await page.getByTestId('s2e8-compose-save').click();
  await expect(page.getByTestId('s2e8-compose-saved')).toHaveText('Đã lưu nháp');
  await expect(page.getByTestId('s2e8-compose-status')).toHaveText('Nháp');
  const sets = await teacherSets(request, sb);
  expect(sets).toHaveLength(1);
  expect(sets[0]!.status).toBe('draft');
  expect(sets[0]!.title).toBe('Góc trong hình chóp');
  expect(sets[0]!.items).toHaveLength(5);
  for (const i of sets[0]!.items) {
    expect(i.maxAttempts).toBe(3);
    expect(i.releaseAt ?? null).toBeNull();
  }
  expect(sets[0]!.releaseAt ?? null).toBeNull();
  await noHorizontalScroll(page);
});

test('@TC-S2E8-089 đề chưa có đáp số máy chấm hiện nhưng không chọn được', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  for (const code of ['LX-01', 'LU-01']) {
    const row = page.getByTestId(`s2e8-problem-${code}`);
    await expect(row).toBeVisible();
    await expect(row).toHaveAttribute('data-gradable', 'false');
    await expect(page.getByTestId(`s2e8-problem-tick-${code}`)).toBeDisabled();
    await expect(row).toContainText('Chưa có đáp số máy chấm được');
    await expect(row).toContainText('Thêm đáp số ở Soát lời giải');
    expect(await row.evaluate((el) => getComputedStyle(el).borderTopStyle), 'viền nét đứt').toBe('dashed');
    const chip = await row.locator('.chip.yel').evaluate((el) => {
      const probe = document.createElement('i');
      probe.className = 'chip grn';
      document.body.append(probe);
      const other = getComputedStyle(probe).backgroundColor;
      probe.remove();
      return { mine: getComputedStyle(el).backgroundColor, other };
    });
    expect(chip.mine, 'chip vàng khác chip xanh').not.toBe(chip.other);
  }
  await page.getByTestId('s2e8-problem-tick-LX-01').click({ force: true });
  await expect(page.getByTestId('s2e8-compose-count')).toHaveText('Chọn đề (0)');
  await expect(page.getByTestId('s2e8-problem-LC-01')).toHaveAttribute('data-gradable', 'true');
});

test('@TC-S2E8-090 đặt kiểu xấp xỉ, sai số, lượt và giờ mở', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  for (const c of FIVE) await tick(page, c);
  await page.getByTestId('s2e8-compose-title').fill('Góc trong hình chóp');
  await expect(page.getByTestId('s2e8-item-tolerance-2')).toBeDisabled(); // kiểu chính xác: sai số bị khóa
  await page.getByTestId('s2e8-item-kind-2').selectOption('approx');
  await page.getByTestId('s2e8-item-tolerance-2').fill('0,5');
  await page.getByTestId('s2e8-item-attempts-2').selectOption('3');
  const at = await localInput(page, 2);
  await page.getByTestId('s2e8-item-release-2').fill(at);
  await page.getByTestId('s2e8-compose-save').click();
  await expect(page.getByTestId('s2e8-compose-saved')).toHaveText('Đã lưu nháp');
  const set = (await teacherSets(request, sb))[0]!;
  const q2 = set.items[1]!;
  expect(q2.answerKind).toBe('approx');
  expect(q2.tolerance).toBe(0.5);
  expect(q2.maxAttempts).toBe(3);
  const want = await page.evaluate((v) => new Date(v).getTime(), at);
  expect(new Date(q2.releaseAt!).getTime()).toBe(want);
  expect(set.items[0]!.answerKind).toBe('exact');
  expect(set.items[0]!.tolerance ?? null).toBeNull();
});

test('@TC-S2E8-095 tên bài 80 ký tự nhận, 81 bị chặn, rỗng bị chặn', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  await tick(page, 'LC-01');
  const title = page.getByTestId('s2e8-compose-title');
  const save = page.getByTestId('s2e8-compose-save');
  const error = page.getByTestId('s2e8-compose-error');
  await save.click();
  await expect(error).toHaveText('Hãy đặt tên bài');
  await title.fill('T'.repeat(81));
  await save.click();
  await expect(error).toHaveText('Tên tối đa 80 ký tự');
  expect(await teacherSets(request, sb)).toHaveLength(0);
  await title.fill('T'.repeat(80));
  await save.click();
  await expect(page.getByTestId('s2e8-compose-saved')).toHaveText('Đã lưu nháp');
  await expect(error).toHaveCount(0);
  const sets = await teacherSets(request, sb);
  expect(sets).toHaveLength(1);
  expect(sets[0]!.title).toHaveLength(80);
});

test('@TC-S2E8-100 giờ mở đã qua cần xác nhận', async ({ page, request }) => {
  const sb = await sandbox(request);
  await openCompose(page, sb);
  await tick(page, 'LC-01');
  await page.getByTestId('s2e8-compose-title').fill('Giờ mở đã qua');
  await page.getByTestId('s2e8-compose-release').fill(await localInput(page, -1));
  await page.getByTestId('s2e8-compose-class').selectOption({ label: '11A1' });
  await page.getByTestId('s2e8-compose-assign').click();
  await expect(page.getByTestId('s2e8-assign-past')).toHaveText('Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?');
  // chưa xác nhận thì chưa giao
  expect((await teacherSets(request, sb)).filter((s) => s.status !== 'draft')).toHaveLength(0);
  await page.getByTestId('s2e8-assign-no').click();
  await expect(page.getByTestId('s2e8-assign-confirm')).toHaveCount(0);
  expect((await teacherSets(request, sb)).filter((s) => s.status !== 'draft')).toHaveLength(0);
  // xác nhận: giao, lời giải mở ngay
  await page.getByTestId('s2e8-compose-assign').click();
  await page.getByTestId('s2e8-assign-yes').click();
  await expect(page.getByTestId('s2e8-compose-status')).toHaveText('Đã giao');
  const set = (await teacherSets(request, sb)).find((s) => s.status === 'assigned')!;
  expect(set).toBeTruthy();
  const an = student(sb, 'An').token;
  const sol = await request.get(`${API}/api/v1/student/assignments/${set.items[0]!.assignmentId}/solution`, { headers: bearer(an) });
  expect(sol.status(), 'lời giải mở ngay cho học sinh').toBe(200);
  // máy chủ cũng chặn khi thiết bị không xác nhận
  const d = await request.post(`${API}/api/v1/teacher/practice-sets`, {
    headers: bearer(sb.teacher.token),
    data: { title: 'Qua API', releaseAt: new Date(Date.now() - 3_600_000).toISOString(), items: [{ problemVersionId: set.items[0]!.problemVersionId }] },
  });
  const draft = (await d.json()) as SetDetail;
  const blocked = await request.post(`${API}/api/v1/teacher/practice-sets/${draft.id}/assign`, { headers: bearer(sb.teacher.token), data: { classId: sb.classes[1]!.id } });
  expect(blocked.status()).toBe(409);
  expect(((await blocked.json()) as { message: string }).message).toBe('Giờ mở đã qua, lời giải mở ngay cho học sinh. Vẫn giao?');
});

test('@TC-S2E8-101 học sinh gửi báo Em nghĩ đáp số này đúng sau khi bị chấm Sai', async ({ page, request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  await openPlay(page, sb, set.id);
  await expect(page.getByTestId('s2e8-report-open')).toHaveCount(0); // chưa bị chấm Sai thì chưa có nút
  await submit(page, '44,4');
  await expect(page.getByTestId('s2e8-attempts')).toHaveText('Còn 2/3 lượt');
  await page.getByTestId('s2e8-report-open').click();
  await page.getByTestId('s2e8-report-note').fill('Em đo ra 44,4');
  await page.getByTestId('s2e8-report-send').click();
  await expect(page.getByTestId('s2e8-report-sent')).toHaveText('Đã gửi cho thầy cô');
  await expect(page.getByTestId('s2e8-report-open')).toHaveCount(0);
  await expect(page.getByTestId('s2e8-attempts')).toHaveText('Còn 2/3 lượt');
  await expect(answerBox(page)).toHaveText('44,4');
  for (const id of ['s2e8-feedback', 's2e8-hint', 's2e8-report-sent']) await expect(page.getByTestId(id)).not.toContainText(/45|Dạng chuẩn|Đúng rồi/);
  const item = (await play(request, an, set.id)).items[1]!;
  expect(item).toMatchObject({ attemptsLeft: 2, state: 'doing', reported: true });
  const reports = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ submitted: string; note: string; createdAt: string }> };
  expect(reports.items).toHaveLength(1);
  expect(reports.items[0]).toMatchObject({ submitted: '44,4', note: 'Em đo ra 44,4' });
  expect(Math.abs(Date.now() - new Date(reports.items[0]!.createdAt).getTime())).toBeLessThan(120_000);
});

async function reported(request: APIRequestContext) {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An');
  await attempt(request, an.token, set.items[1]!.assignmentId, '44,4');
  expect((await report(request, an.token, set.items[1]!.assignmentId, 'Em đo ra 44,4')).status()).toBe(201);
  return { sb, set, an };
}

async function openReports(page: Page, sb: Sandbox) {
  await signIn(page, sb.teacher, 'TEACHER', 'Cô Hạnh');
  await page.goto('/gv/bai-luyen/bao-cham-sai');
  await expect(page.getByTestId('s2e8-reports')).toBeVisible();
}

test('@TC-S2E8-105 giáo viên thấy báo kèm đáp số và dạng chuẩn', async ({ page, request }) => {
  const { sb, an } = await reported(request);
  await openReports(page, sb);
  const row = page.locator('[data-testid^="s2e8-report-"][data-status]');
  await expect(row).toHaveCount(1);
  await expect(row.getByTestId('s2e8-report-student')).toHaveText(`An (${an.userCode})`);
  expect(an.userCode).toMatch(/^HS-[A-Z2-9]{4}-[A-Z2-9]{4}$/);
  await expect(row.getByTestId('s2e8-report-where')).toContainText('Góc trong hình chóp · Câu 2');
  await expect(row.getByTestId('s2e8-report-submitted')).toHaveText('Đáp số em nhập: 44,4');
  await expect(row.getByTestId('s2e8-report-note')).toHaveText('Ghi chú: Em đo ra 44,4');
  await expect(row.getByTestId('s2e8-report-canonical')).toHaveText('Dạng chuẩn: 45°');
  await expect(row.getByTestId('s2e8-report-confirm')).toHaveText('Xác nhận đúng');
  await expect(row.getByTestId('s2e8-report-keep')).toHaveText('Giữ nguyên');
  await noHorizontalScroll(page);
});
