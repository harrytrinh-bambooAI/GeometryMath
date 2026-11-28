import { expect, test } from '../../support/fixtures.ts';
import {
  active,
  aiCalls,
  bearer,
  board051,
  cellOf,
  event,
  getJson,
  insight,
  openBoard,
  raw,
  rowOf,
  sandbox,
  script,
  setUrl,
  stuckReady,
  student,
  type Cell,
  type Ev,
} from './s3e3.ts';

/** S3-E3 · "Cả lớp vướng ở đâu" (US-S3E3-03): thẻ gộp đúng cấu trúc, biên 4 / 5 / 6 bài làm, sắp thẻ, bài làm căn cứ. */

const T3 = 'Nhiều em xác định sai giao tuyến của (SAB) và (SCD)';
const T4 = 'Nhiều em quên điều kiện đáy là hình vuông';
const BELOW4 = 'Mới có 4 trên 5 bài làm cho đề này, chưa đủ để gộp. Dưới đây là dữ liệu thô.';
/** Ngọc Anh chưa mở Đề 4: Đề 4 còn đúng 4 bài làm (An, Huy, My, Quân). */
const FOUR: Record<string, Cell[]> = { 'Ngọc Anh': ['Đúng', 'Đúng', 'Xem lại', 'Chưa làm'] };
const UPDATED = /^Cập nhật lúc \d\d:\d\d$/;

test('@TC-S3E3-34 đề đủ bài làm hiện thẻ gộp đúng cấu trúc', async ({ page, request }) => {
  const sb = await board051(request);
  await script(request, sb, { 3: [{ text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  const card = insight(page, 3);
  await expect(card.getByTestId('s3e3-insight-title')).toHaveText('Đề 3 · hai mặt phẳng');
  await expect(card.getByTestId('s3e3-insight-text')).toHaveText(T3);
  await expect(card.getByTestId('s3e3-insight-label')).toHaveText(['AI gộp', 'Cần thầy cô xác nhận']);
  await expect(card.getByTestId('s3e3-insight-open')).toHaveText('Xem 6 bài làm');
  await expect(card.getByTestId('s3e3-insight-updated')).toHaveText(UPDATED);
  await expect(page.getByTestId('s3e3-stuck-note')).toContainText('Chỉ gộp khi đủ 5 bài làm cho một đề');
});

test('@TC-S3E3-35 biên 5: đúng 5 bài làm thì gộp, bộ gộp được gọi một lần cho mỗi đề đủ ngưỡng', async ({ page, request }) => {
  const sb = await board051(request);
  await script(request, sb, { 4: [{ text: T4, works: [1, 2, 3] }] });
  const first = await getJson<{ items: Array<{ position: number; works: number; mode: string }> }>(request, `${setUrl(sb)}/stuck`, sb.teacher.token);
  expect(first.items.find((i) => i.position === 4)).toMatchObject({ works: 5, mode: 'insight' });
  expect(await aiCalls(request, sb.teacher.id)).toBe(4);
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(insight(page, 4).getByTestId('s3e3-insight-text')).toHaveText(T4);
  await expect(insight(page, 4).getByTestId('s3e3-insight-open')).toHaveText('Xem 3 bài làm');
  await expect(raw(page, 4)).toHaveCount(0);
  await expect(page.getByTestId('s3e3-msg-below')).toHaveCount(0);
  expect(await aiCalls(request, sb.teacher.id)).toBe(4);
});

test('@TC-S3E3-36 biên 4: 4 bài làm thì chỉ dữ liệu thô với MSG-S3-43, bộ gộp không bị gọi cho đề đó', async ({ page, request }) => {
  const sb = await board051(request, { cells: FOUR, events: [{ student: 'Bình An', item: 4, kind: 'hint', level: 1, at: '15:01' }] });
  const first = await getJson<{ items: Array<{ position: number; works: number; mode: string }> }>(request, `${setUrl(sb)}/stuck`, sb.teacher.token);
  expect(first.items.find((i) => i.position === 4)).toMatchObject({ works: 4, mode: 'raw' });
  expect(await aiCalls(request, sb.teacher.id)).toBe(3);
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(insight(page, 4)).toHaveCount(0);
  await expect(raw(page, 4)).toHaveAttribute('data-mode', 'raw');
  await expect(raw(page, 4).getByTestId('s3e3-msg-below')).toHaveText(BELOW4);
  await expect(raw(page, 4).getByTestId('s3e3-raw-entry')).toHaveCount(1);
  await expect(raw(page, 4)).not.toContainText('AI gộp');
  expect(await aiCalls(request, sb.teacher.id)).toBe(3);
});

test('@TC-S3E3-37 biên 6: 6 bài làm thì gộp', async ({ page, request }) => {
  const sb = await board051(request, { events: [{ student: 'Khánh Linh', item: 4, kind: 'hint', level: 1, at: '15:02' }] });
  await script(request, sb, { 4: [{ text: T4, works: [1, 2, 3] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(insight(page, 4)).toHaveCount(1);
  await expect(raw(page, 4)).toHaveCount(0);
  await expect(page.getByTestId('s3e3-msg-below')).toHaveCount(0);
  const body = await getJson<{ items: Array<{ position: number; works: number }> }>(request, `${setUrl(sb)}/stuck`, sb.teacher.token);
  expect(body.items.find((i) => i.position === 4)!.works).toBe(6);
});

test('@TC-S3E3-38 mỗi đề xét riêng ngưỡng: Đề 3 có thẻ, Đề 4 dữ liệu thô trên cùng một màn', async ({ page, request }) => {
  const sb = await board051(request, { cells: FOUR });
  await script(request, sb, { 3: [{ text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(page.getByTestId('s3e3-insight')).toHaveCount(1);
  await expect(insight(page, 3)).toBeVisible();
  await expect(raw(page, 3)).toHaveCount(0);
  await expect(raw(page, 4).getByTestId('s3e3-msg-below')).toHaveText(BELOW4);
  await expect(page.getByTestId('s3e3-msg-below')).toHaveCount(1);
});

test('@TC-S3E3-39 thẻ sắp theo số bài làm căn cứ giảm dần; cùng n thì đề đứng trước ở trên', async ({ page, request }) => {
  const sb = await board051(request);
  await script(request, sb, {
    1: [{ text: 'Nhiều em nhầm hình chiếu của SC', works: [1, 2, 3, 4] }],
    2: [{ text: 'Một nhóm em dựng đường cao nhưng không chứng minh vuông góc', works: [2, 3, 4, 5] }],
    3: [{ text: T3, works: [1, 2, 3, 4, 5, 6] }],
  });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(page.getByTestId('s3e3-insight-title')).toHaveText(['Đề 3 · hai mặt phẳng', 'Đề 1 · SC-đáy', 'Đề 2 · d(A,SBC)']);
  await expect(page.getByTestId('s3e3-insight-open')).toHaveText(['Xem 6 bài làm', 'Xem 4 bài làm', 'Xem 4 bài làm']);
});

test('@TC-S3E3-40 "Xem 6 bài làm" mở đúng 6 bài làm căn cứ; bấm một dòng mở bằng chứng của ô đó', async ({ page, request }) => {
  const sb = await board051(request);
  await script(request, sb, { 3: [{ text: T3, works: [1, 2, 3, 4, 5, 6] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await insight(page, 3).getByTestId('s3e3-insight-open').click();
  const people = insight(page, 3).getByTestId('s3e3-insight-person');
  const six = ['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Minh Quân'];
  await expect(people).toHaveCount(6);
  await expect(people.locator('b')).toHaveText(six);
  for (const [i, name] of six.entries()) await expect(people.nth(i)).toContainText(student(sb, name).userCode);
  await expect(insight(page, 3)).not.toContainText('Ngọc Anh');
  await people.nth(1).click();
  await expect(page.getByTestId('s3e3-ev-title')).toHaveText('BẰNG CHỨNG · Chi Mai · Đề 3');
  await expect(page.getByTestId('s3e3-ev-code')).toHaveText(student(sb, 'Chi Mai').userCode);
});

test('@TC-S3E3-42 dưới ngưỡng: gợi ý và câu hỏi thô gom theo bước, có loại, nội dung nguyên văn và giờ', async ({ page, request }) => {
  const events: Ev[] = [
    { student: 'Bình An', item: 4, kind: 'hint', level: 1, at: '15:01' },
    { student: 'Minh Quân', item: 4, kind: 'hint', level: 1, at: '15:02' },
    { student: 'Minh Quân', item: 4, kind: 'ask', text: 'Chiều cao là cạnh nào ạ?', at: '15:03' },
    { student: 'Đức Huy', item: 4, kind: 'ask', text: 'Đáy là hình gì ạ?', at: '15:04' },
    { student: 'Bình An', item: 4, kind: 'hint', level: 2, at: '15:05' },
  ];
  const sb = await board051(request, { cells: FOUR, events });
  await openBoard(page, sb);
  await stuckReady(page);
  const block = raw(page, 4);
  await expect(block.getByTestId('s3e3-raw-step')).toHaveCount(3);
  await expect(block.locator('[data-testid="s3e3-raw-entry"][data-kind="hint"]')).toHaveCount(3);
  await expect(block.locator('[data-testid="s3e3-raw-entry"][data-kind="question"]')).toHaveCount(2);
  const step = (n: number) => block.locator(`[data-testid="s3e3-raw-step"][data-step="${n}"]`);
  await expect(step(0)).toContainText('Bước đọc đề');
  await expect(step(0).getByTestId('s3e3-raw-entry')).toHaveText(['15:04 Câu hỏi: Đáy là hình gì ạ?']);
  await expect(step(1).getByTestId('s3e3-raw-entry')).toHaveText([/^15:01 Gợi ý: .+/, /^15:02 Gợi ý: .+/, '15:03 Câu hỏi: Chiều cao là cạnh nào ạ?']);
  await expect(step(2).getByTestId('s3e3-raw-entry')).toHaveText([/^15:05 Gợi ý: .+/]);
  await expect(block).not.toContainText('AI gộp');
  expect(await block.innerText()).not.toMatch(/\d+ em\b|Nhiều em/);
  await expect(insight(page, 4)).toHaveCount(0);
});

test('@TC-S3E3-43 lớp dưới 5 học sinh luôn dữ liệu thô với MSG-S3-44', async ({ page, request }) => {
  const sb = await sandbox(request, [
    {
      name: '11A2',
      students: active(['Quốc Bảo', 'Thảo Vy', 'Tuấn Kiệt']),
      sets: [{ title: 'Bộ bài tuần 15', cells: { 'Quốc Bảo': ['Chưa làm', 'Chưa làm', 'Đúng'], 'Thảo Vy': ['Chưa làm', 'Chưa làm', 'Xem lại'], 'Tuấn Kiệt': ['Chưa làm', 'Chưa làm', 'Đang làm'] } }],
    },
  ]);
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(page.getByTestId('s3e3-msg-small')).toHaveText('Lớp có ít hơn 5 học sinh, chưa gộp được. Dưới đây là dữ liệu thô.');
  await expect(page.getByTestId('s3e3-insight')).toHaveCount(0);
  await expect(raw(page, 3)).toHaveAttribute('data-mode', 'raw');
  await expect(raw(page, 3).getByTestId('s3e3-raw-entry')).toHaveCount(1);
  expect(await aiCalls(request, sb.teacher.id)).toBe(0);
});

test('@TC-S3E3-44 một em làm nhiều lần chỉ tính một bài làm: Đề 4 vẫn 4 bài làm', async ({ page, request }) => {
  const events: Ev[] = [
    { student: 'Minh Quân', item: 4, kind: 'answer', text: '36', correct: false, at: '15:01' },
    { student: 'Minh Quân', item: 4, kind: 'answer', text: '48', correct: false, at: '15:02' },
    { student: 'Minh Quân', item: 4, kind: 'hint', level: 1, at: '15:03' },
    { student: 'Minh Quân', item: 4, kind: 'hint', level: 2, at: '15:04' },
  ];
  const sb = await board051(request, { cells: FOUR, events });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(raw(page, 4).getByTestId('s3e3-msg-below')).toHaveText(BELOW4);
  await expect(insight(page, 4)).toHaveCount(0);
});

test('@TC-S3E3-46 làm mới sau bài làm thứ 5 thì xuất hiện nhận định và ô chuyển Đang làm', async ({ page, request }) => {
  const sb = await board051(request, { cells: FOUR });
  await script(request, sb, { 4: [{ text: T4, works: [1, 2, 3] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(raw(page, 4).getByTestId('s3e3-msg-below')).toHaveText(BELOW4);
  await event(request, sb, { student: 'Ngọc Anh', item: 4, kind: 'open' });
  await page.getByTestId('s3e3-refresh').click();
  await expect(insight(page, 4).getByTestId('s3e3-insight-text')).toHaveText(T4);
  await expect(insight(page, 4).getByTestId('s3e3-insight-updated')).toHaveText(UPDATED);
  await expect(raw(page, 4)).toHaveCount(0);
  await expect(cellOf(page, student(sb, 'Ngọc Anh'), 4)).toHaveText('Đang làm');
});

test('@TC-S3E3-47 học sinh Đã rời không tính vào ngưỡng: 4 em Đang học + Văn Tú vẫn là 4 bài làm', async ({ page, request }) => {
  const sb = await board051(request, {
    students: [{ name: 'Văn Tú', status: 'left' }],
    cells: { ...FOUR, 'Văn Tú': ['Đúng', 'Chưa làm', 'Chưa làm', 'Xem lại'] },
  });
  await script(request, sb, { 1: [{ text: 'Nhiều em nhầm hình chiếu của SC', works: [1, 2, 3, 4, 5, 6, 7] }] });
  await openBoard(page, sb);
  await stuckReady(page);
  await expect(raw(page, 4).getByTestId('s3e3-msg-below')).toHaveText(BELOW4);
  await expect(insight(page, 4)).toHaveCount(0);
  await expect(rowOf(page, student(sb, 'Văn Tú')).getByTestId('s3e3-left-chip')).toHaveText('đã rời');
  await expect(cellOf(page, student(sb, 'Văn Tú'), 4)).toHaveText('Xem lại');
  await expect(insight(page, 1).getByTestId('s3e3-insight-open')).toHaveText('Xem 7 bài làm');
  await insight(page, 1).getByTestId('s3e3-insight-open').click();
  await expect(insight(page, 1).getByTestId('s3e3-insight-person')).toHaveCount(7);
  await expect(insight(page, 1)).not.toContainText('Văn Tú');
  const text = await (await request.get(`${setUrl(sb)}/stuck`, { headers: bearer(sb.teacher.token) })).text();
  expect(text).not.toContain('Văn Tú');
});
