import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, FX10, MSG, bearer, builtDraft, chatUrl, newDraft, ok, sayAndAccept, signIn, teacher, tokenFor, typeAndSend, type Chat } from './s2e4.ts';

/** S2-E4 — C2-04a "Chỉnh nháp với AI" (US-S2E4-01): AI hỏi lại / đề xuất trước–sau, Nhận, Bác, Sửa tay, Hoàn tác, không tự lưu. */

const statement = (page: Page) => page.getByTestId('s2e4-chat-statement');
const proposal = (page: Page) => page.getByTestId('s2e4-proposal');

/** Bấm một nút của khung đề xuất / hoàn tác và chờ máy chủ trả lời. */
async function act(page: Page, testId: string, urlPart: string) {
  const done = page.waitForResponse((r) => r.url().includes(urlPart) && r.request().method() === 'POST');
  await page.getByTestId(testId).click();
  expect((await done).status()).toBe(200);
}

test('@TC-S2E4-001 định hướng rõ cho một đề xuất có trước và sau', async ({ page, request }, info) => {
  const t = await tokenFor(request, teacher(1, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(1, info), `/gv/chinh-nhap/${d.id}`);
  await expect(page.getByTestId('s2e4-chat')).toBeVisible();
  await expect(statement(page)).toHaveText(FX10);
  await typeAndSend(page, 'đổi cạnh BC thành 14');
  await expect(proposal(page)).toBeVisible();
  await expect(page.getByTestId('s2e4-proposal-before')).toHaveText('BC = 13');
  await expect(page.getByTestId('s2e4-proposal-after')).toHaveText('BC = 14');
  await expect(page.getByTestId('s2e4-proposal-source')).toContainText(MSG.source);
  await expect(page.getByTestId('s2e4-proposal-unsure')).toBeVisible();
  // chưa đổi nháp cho tới khi giáo viên nhận
  await expect(statement(page)).toHaveText(FX10);
  const c = await ok<Chat>(await request.get(chatUrl(d.id), { headers: bearer(t) }));
  expect(c.draft.statement).toBe(FX10);
  expect(c.machineLabel).toBe(MSG.verified);
});

test('@TC-S2E4-002 định hướng mơ hồ thì AI hỏi lại, không tự đoán', async ({ page, request }, info) => {
  const t = await tokenFor(request, teacher(2, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(2, info), `/gv/chinh-nhap/${d.id}`);
  await typeAndSend(page, 'làm đề khó hơn');
  const reply = page.getByTestId('s2e4-msg-2');
  await expect(reply).toHaveAttribute('data-kind', 'question');
  await expect(reply).toContainText(MSG.askHarder);
  await expect(proposal(page)).toHaveCount(0);
  await expect(page.getByTestId('s2e4-no-proposal')).toBeVisible();
  await expect(statement(page)).toHaveText(FX10);
  const c = await ok<Chat>(await request.get(chatUrl(d.id), { headers: bearer(t) }));
  expect(c.messages.filter((m) => m.role === 'ai')).toHaveLength(1); // đúng MỘT câu hỏi lại
  expect(c.messages.some((m) => m.proposal)).toBe(false);
});

test('@TC-S2E4-003 nhận đề xuất thì nháp đổi và nhãn máy chuyển Cần xem lại', async ({ page, request }, info) => {
  const t = await tokenFor(request, teacher(3, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(3, info), `/gv/chinh-nhap/${d.id}`);
  await expect(page.getByTestId('s2e4-chat-machine')).toHaveText(MSG.verified);
  await typeAndSend(page, 'đổi cạnh BC thành 14');
  await expect(page.getByTestId('s2e4-chat-machine')).toHaveText(MSG.verified); // đề xuất chưa nhận: nhãn chưa đổi
  await act(page, 's2e4-accept', '/accept');
  await expect(statement(page)).toHaveText(FX10.replace('BC = 13', 'BC = 14'));
  await expect(page.getByTestId('s2e4-chat-machine')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e4-chat-human')).toHaveText(MSG.ai);
  await expect(proposal(page)).toHaveCount(0);
  // máy kiểm lại chưa đạt (lời giải AI vẫn theo số cũ) thì nhãn vẫn "Cần xem lại"
  await act(page, 's2e4-recheck', '/recheck');
  await expect(page.getByTestId('s2e4-chat-machine')).toHaveText(MSG.needsReview);
  await expect(page.getByTestId('s2e4-chat-human')).toHaveText(MSG.ai);
});

test('@TC-S2E4-004 bác đề xuất thì nháp giữ nguyên', async ({ page, request }, info) => {
  const t = await tokenFor(request, teacher(4, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(4, info), `/gv/chinh-nhap/${d.id}`);
  await typeAndSend(page, 'đổi cạnh BC thành 14');
  await expect(proposal(page)).toBeVisible();
  await act(page, 's2e4-reject', '/reject');
  await expect(statement(page)).toHaveText(FX10);
  await expect(page.getByTestId('s2e4-chat-machine')).toHaveText(MSG.verified);
  await expect(page.getByTestId('s2e4-msg-note-2')).toHaveText(MSG.rejected);
  await expect(proposal(page)).toHaveCount(0);
  await expect(page.getByTestId('s2e4-undo')).toBeDisabled(); // không có gì để hoàn tác
});

test('@TC-S2E4-005 sửa tay đề xuất rồi mới nhận', async ({ page, request }, info) => {
  const t = await tokenFor(request, teacher(5, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(5, info), `/gv/chinh-nhap/${d.id}`);
  await typeAndSend(page, 'đổi cạnh BC thành 14');
  await page.getByTestId('s2e4-edit').click();
  await expect(page.getByTestId('s2e4-edit-input')).toHaveValue('BC = 14');
  await page.getByTestId('s2e4-edit-input').fill('BC = 15');
  await act(page, 's2e4-accept', '/accept');
  await expect(statement(page)).toHaveText(FX10.replace('BC = 13', 'BC = 15'));
  await expect(page.getByTestId('s2e4-msg-note-2')).toHaveText('đã sửa tay từ 14 thành 15');
});

test('@TC-S2E4-006 hoàn tác trả nháp về bước trước', async ({ page, request }, info) => {
  const t = await tokenFor(request, teacher(6, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(6, info), `/gv/chinh-nhap/${d.id}`);
  await typeAndSend(page, 'đổi cạnh BC thành 14');
  await act(page, 's2e4-accept', '/accept');
  const afterFirst = FX10.replace('BC = 13', 'BC = 14');
  await expect(statement(page)).toHaveText(afterFirst);
  await typeAndSend(page, 'đổi cạnh AB thành 6');
  await act(page, 's2e4-accept', '/accept');
  await expect(statement(page)).toHaveText(afterFirst.replace('AB = 5', 'AB = 6'));
  await act(page, 's2e4-undo', '/chat/undo');
  await expect(statement(page)).toHaveText(afterFirst); // đúng trạng thái sau đề xuất thứ nhất
  await expect(page.getByTestId('s2e4-undo')).toBeEnabled();
});

test('@TC-S2E4-007 hoàn tác 10 bước được, bước thứ 11 không', async ({ page, request }, info) => {
  test.slow();
  const t = await tokenFor(request, teacher(7, info));
  const d = await newDraft(request, t, FX10);
  for (let i = 0; i < 11; i++) {
    await sayAndAccept(request, t, d.id, `đổi cạnh BC thành ${14 + i}`); // 11 đề xuất liên tiếp: BC = 14 … 24
  }
  await signIn(page, request, teacher(7, info), `/gv/chinh-nhap/${d.id}`);
  await expect(statement(page)).toHaveText(FX10.replace('BC = 13', 'BC = 24'));
  for (let i = 1; i <= 10; i++) {
    await expect(page.getByTestId('s2e4-undo')).toBeEnabled();
    await act(page, 's2e4-undo', '/chat/undo');
    await expect(statement(page)).toHaveText(FX10.replace('BC = 13', `BC = ${24 - i}`));
  }
  // lần 11: nút khóa kèm "Hết bước hoàn tác"; nháp dừng ở trạng thái sau đề xuất thứ nhất
  await expect(page.getByTestId('s2e4-undo')).toBeDisabled();
  await expect(page.getByTestId('s2e4-undo-note')).toHaveText(MSG.noUndo);
  await expect(statement(page)).toHaveText(FX10.replace('BC = 13', 'BC = 14'));
  const r = await request.post(`${chatUrl(d.id)}/undo`, { headers: bearer(t), data: {} });
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { message: string }).message).toBe(MSG.noUndo);
});

test('@TC-S2E4-008 AI không tự lưu: nháp không vào giáo trình khi chưa duyệt', async ({ page, request, context }, info) => {
  const t = await tokenFor(request, teacher(8, info));
  const d = await builtDraft(request, t, FX10);
  await signIn(page, request, teacher(8, info), `/gv/chinh-nhap/${d.id}`);
  await typeAndSend(page, 'đổi cạnh BC thành 14');
  await act(page, 's2e4-accept', '/accept');
  await expect(page.getByTestId('s2e4-chat-saved')).toContainText('chưa lưu');

  // tab khác: "Của tôi" không có đề này như đề đã duyệt
  const tab = await context.newPage();
  await signIn(tab, request, teacher(8, info), '/gv/kho-de-cua-toi');
  await expect(tab.getByTestId('s2e6-page')).toBeVisible();
  await expect(tab.getByTestId('s2e6-list').or(tab.getByTestId('s2e6-empty'))).toBeVisible();
  await expect(tab.getByText(MSG.approved)).toHaveCount(0);
  // … chỉ hiện ở mục "Nháp"
  await tab.goto('/gv/chinh-nhap');
  await expect(tab.getByTestId(`s2e4-draft-${d.id}`)).toBeVisible();
  await expect(tab.getByTestId(`s2e4-draft-label-${d.id}`)).toContainText('Nháp');
  await expect(tab.getByTestId(`s2e4-draft-${d.id}`)).toContainText('BC = 14');

  const c = await ok<Chat>(await request.get(chatUrl(d.id), { headers: bearer(t) }));
  expect(c.saved).toBe(false);
  expect(c.draft.inMyShelf).toBe(false);
  expect(c.humanLabel).toBe(MSG.ai);
  const mine = await ok<{ items: Array<{ humanLabel: string }> }>(await request.get(`${API}/api/v1/teacher/library?shelf=mine`, { headers: bearer(t) }));
  expect(mine.items.filter((i) => i.humanLabel === MSG.approved)).toHaveLength(0);
});
