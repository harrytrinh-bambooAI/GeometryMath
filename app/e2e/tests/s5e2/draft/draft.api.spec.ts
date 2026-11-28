import { expect, test } from '../../../support/fixtures.ts';
import { aiCalls, hook } from '../../s3e1/s3e1.ts';
import { s5e3World, tl01, tl02 } from '../../s5e3/kb.ts';
import {
  ENGINE_ANSWERS,
  MSG,
  aiPurposes,
  aiScenario,
  approve,
  bank,
  batch,
  drafts,
  requestDrafts,
  tl09,
  type DraftItem,
  type Err,
} from './draft.ts';

/**
 * US-S5E2-06 — trợ lý soạn đề nháp từ tài liệu Đang dùng của giáo viên (luồng D) qua API (máy chủ thật, engine thật,
 * AI giả lập exam_draft). Đáp số luôn từ engine; đề không qua kiểm chứng "Cần xem lại", đặt cuối, không chọn sẵn.
 */

async function setup(request: Parameters<typeof s5e3World>[0]) {
  const w = await s5e3World(request);
  const lan = w.people['Cô Lan']!;
  const khoi = w.people['Thầy Khôi']!;
  const c11 = w.classes['11A1']!;
  return { w, lan, khoi, c11 };
}

test('Soạn 5 đề nháp từ TL-01, đề đạt kiểm chứng chọn sẵn @TC-S5E2-24', async ({ request }) => {
  const { lan, c11 } = await setup(request);
  const tl = await tl01(request, lan);
  const b = await drafts(request, lan, tl.id, c11.id, 5);
  expect(b.source).toMatchObject({
    docId: tl.id,
    docCode: tl.code,
    version: 2,
    label: `${tl.code} v2`,
  });
  expect(b.items).toHaveLength(5);
  for (const it of b.items) {
    expect(it.humanLabel).toBe('AI soạn');
    expect(it.status).toBe('draft');
    expect(it.sourceLabel).toBe(`${tl.code} v2`);
    expect(it.source.startsWith(`${tl.code} v2`)).toBe(true);
  }
  const ok = b.items.slice(0, 4);
  expect(ok.map((i) => i.machineLabel)).toEqual(Array(4).fill('Đã kiểm chứng'));
  expect(ok.every((i) => i.preselected)).toBe(true);
  expect(ok.map((i) => i.answer)).toEqual(ENGINE_ANSWERS);
  expect(ok.every((i) => i.answerSource === 'engine')).toBe(true);
  const last = b.items[4]!;
  expect(last.machineLabel).toBe('Cần xem lại');
  expect(last.preselected).toBe(false);
  expect(last.reviewReason).toBe('máy không dựng được hình');
  // máy chủ bỏ khóa null: không có đáp số engine
  expect(last.answer ?? null).toBeNull();
  expect(b.items.map((i) => i.ord)).toEqual([1, 2, 3, 4, 5]);
  expect(b.message).toBe(MSG.s14);
  // một lượt AI task exam_draft
  expect(await aiPurposes(request, lan.id)).toEqual(['exam_draft']);
});

test('Duyệt từng đề đưa vào ngân hàng "Của tôi" @TC-S5E2-25', async ({ request }) => {
  const { lan, c11 } = await setup(request);
  const tl = await tl01(request, lan);
  const b = await drafts(request, lan, tl.id, c11.id, 5);
  expect((await bank(request, lan)).items).toHaveLength(0);
  const picked = b.items.slice(0, 2);
  for (const it of picked) {
    const r = await approve(request, lan, it.id);
    expect(r.status(), await r.text()).toBe(200);
    const a = (await r.json()) as DraftItem;
    expect(a.humanLabel).toBe('Giáo viên đã duyệt');
    expect(a.status).toBe('approved');
    expect(a.answer).toBe(it.answer);
  }
  const mine = (await bank(request, lan)).items;
  expect(mine.map((i) => i.id).sort()).toEqual(picked.map((i) => i.id).sort());
  expect(mine.every((i) => i.humanLabel === 'Giáo viên đã duyệt')).toBe(true);
  // đáp số 2 đề bằng giá trị engine
  expect(mine.map((i) => i.answer).sort()).toEqual(ENGINE_ANSWERS.slice(0, 2).sort());
  // 3 đề còn lại vẫn là nháp
  const again = await batch(request, lan, b.id);
  const rest = again.items.filter((i) => !picked.some((p) => p.id === i.id));
  expect(rest).toHaveLength(3);
  expect(rest.every((i) => i.status === 'draft' && i.humanLabel === 'AI soạn')).toBe(true);
  // đề "Cần xem lại" không duyệt thẳng được (đáp số chưa kiểm chứng)
  const review = again.items.find((i) => i.machineLabel === 'Cần xem lại')!;
  const no = await approve(request, lan, review.id);
  expect(no.status()).toBe(409);
  expect((await bank(request, lan)).items).toHaveLength(2);
});

test('Tài liệu chưa Đang dùng không làm nguồn được @TC-S5E2-26', async ({ request }) => {
  const { lan, c11 } = await setup(request);
  const tl = await tl02(request, lan);
  expect(tl.status).toBe('pending');
  const r = await requestDrafts(request, lan, {
    docId: tl.id,
    classId: c11.id,
    count: 5,
  });
  expect(r.status()).toBe(409);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('CONFLICT');
  expect(e.message).toBe(MSG.s15);
  expect(await aiCalls(request, lan.id)).toBe(0);
});

test('Tài liệu của giáo viên khác không làm nguồn được @TC-S5E2-27', async ({ request }) => {
  const { lan, khoi, c11 } = await setup(request);
  const tl = await tl09(request, khoi);
  expect(tl.activeVersion).toBe(1);
  const r = await requestDrafts(request, lan, {
    docId: tl.id,
    classId: c11.id,
    count: 5,
  });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as Err).code).toBe('NOT_FOUND');
  expect(await aiCalls(request, lan.id)).toBe(0);
});

test('Hết 30 lượt soạn trong ngày thì không soạn đề nháp @TC-S5E2-28', async ({ request }) => {
  const { w, lan, c11 } = await setup(request);
  const tl = await tl01(request, lan);
  // 30/30 lượt soạn hôm nay (móc S5-E1)
  await hook(request, 's5e1/turns', {
    orgId: w.orgId,
    userId: lan.id,
    count: 30,
  });
  const before = await aiCalls(request, lan.id);
  const r = await requestDrafts(request, lan, {
    docId: tl.id,
    classId: c11.id,
    count: 5,
  });
  expect(r.status()).toBe(429);
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('RATE_LIMITED');
  expect(e.message).toBe(MSG.s06);
  expect(await aiCalls(request, lan.id)).toBe(before);
});

test('AI ghi đáp số khác engine thì đáp số engine thắng, đề "Cần xem lại" @TC-S5E2-29', async ({ request }) => {
  const { lan, c11 } = await setup(request);
  const tl = await tl01(request, lan);
  await aiScenario(request, lan, 's5e2-draft-mismatch');
  const b = await drafts(request, lan, tl.id, c11.id, 5);
  const bad = b.items.find((i) => i.aiAnswer === 'a√3/3')!;
  expect(bad).toBeTruthy();
  expect(bad.machineLabel).toBe('Cần xem lại');
  expect(bad.reviewReason).toBe('đáp số AI khác máy tính');
  expect(bad.answer).toBe('a√6/3');
  expect(bad.answerSource).toBe('engine');
  expect(bad.preselected).toBe(false);
  // đề "Cần xem lại" ở cuối danh sách
  const labels = b.items.map((i) => i.machineLabel);
  expect(labels).toEqual(['Đã kiểm chứng', 'Đã kiểm chứng', 'Đã kiểm chứng', 'Cần xem lại', 'Cần xem lại']);
  expect(b.message).toBe(MSG.s14);
});
