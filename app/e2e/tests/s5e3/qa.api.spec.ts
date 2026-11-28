import { expect, test } from '../../support/fixtures.ts';
import { API, aiCalls, assignSet, bearer, teacherApi, world } from '../s3e1/s3e1.ts';
import { createMd, json, type DocView } from './kb.ts';
import {
  MSG,
  Q_SC1,
  S5E3B_WORLD,
  ack,
  ask,
  askRaw,
  decisionOf,
  lectureApproval,
  mock,
  prompt,
  setHelp,
  setup,
  state,
} from './qa.ts';

/**
 * US-S5E3-03 (hỏi đáp theo kho, luôn kèm nguồn) và US-S5E3-04 (mức trợ giúp, kiểm ở máy chủ trước khi gọi AI) — máy chủ
 * thật, AI giả lập task kb_qa (F-S5-094 và biến thể). "Không gọi AI" kiểm bằng số dòng ai_call_log của học sinh.
 */

// ------------------------------------------------------------------ US-S5E3-03

test('Câu hỏi khái niệm nhận trả lời kèm nguồn TL-01 v2 @TC-S5E3-12', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  const r = await ask(request, an, bc.id, Q_SC1);
  expect(r.kind).toBe('concept');
  expect(r.source).toMatchObject({
    type: 'kb',
    typeLabel: 'Tài liệu của thầy cô',
    docCode: 'TL-01',
    docTitle: 'Ghi chú Khoảng cách 11A1',
    version: 2,
    sectionTitle: 'Hệ thức 1/AH²',
    label: 'Theo: Ghi chú Khoảng cách 11A1 · v2 · mục Hệ thức 1/AH²',
  });
  // thân phản hồi chỉ có tiêu đề tài liệu, phiên bản, mục — không có nội dung mục nào khác
  const body = JSON.stringify(r);
  expect(body).not.toContain('hình chiếu vuông góc của M');
  expect(body).not.toContain('Góc giữa hai mặt phẳng là góc');
  expect(body).not.toContain('1/AH² = 1/SA² + 1/AK². Dùng để tính');
  // dòng AIInteraction lưu câu hỏi, câu trả lời, nguồn; một AIPolicyDecision allowed
  const s = await state(request, w.orgId, { userId: an.id });
  const it = s.interactions.find((i) => i.id === r.interactionId)!;
  expect(it.question).toBe(Q_SC1);
  expect(it.reply).toBe(r.reply);
  expect(JSON.parse(it.source!)).toMatchObject({ docCode: 'TL-01', version: 2, sectionTitle: 'Hệ thức 1/AH²' });
  expect((await decisionOf(request, w.orgId, an.id, r.interactionId)).decision).toBe('allowed');
});

test.describe('Bài giảng mẫu đã duyệt (dùng chung bài giảng e2e, chạy nối tiếp)', () => {
  test.describe.configure({ mode: 'serial' });

  test('Lớp chưa gắn tài liệu nào thì không gọi AI @TC-S5E3-14', async ({ request }) => {
    await lectureApproval(request, false);
    const w = await world(request, S5E3B_WORLD);
    const lan = w.people['Cô Lan']!;
    const bao = w.people['Quốc Bảo']!;
    const set = await assignSet(request, lan, w.classes['11A2']!.id, 'Bộ bài 11A2');
    await ack(request, bao);
    const before = await aiCalls(request, bao.id);
    const r = await ask(request, bao, set.items[0]!.assignmentId, 'Mặt phẳng trung trực là gì?');
    expect(r.kind).toBe('no_source');
    expect(r.reply).toBe(MSG.s20);
    expect(r.message).toBe(MSG.s20);
    expect(r.source).toBeNull();
    expect(await aiCalls(request, bao.id)).toBe(before);
    expect(await prompt(request, r.interactionId)).toBeNull();
    const d = await decisionOf(request, w.orgId, bao.id, r.interactionId);
    expect(d.rule_code).toBe('no_source');
  });

  test('Nguồn là bài giảng mẫu đã duyệt thì ghi loại "Bài giảng mẫu"; bài giảng AI chưa duyệt không được gửi @TC-S5E3-19', async ({
    request,
  }) => {
    const w = await world(request, S5E3B_WORLD);
    const lan = w.people['Cô Lan']!;
    const bao = w.people['Quốc Bảo']!;
    const set = await assignSet(request, lan, w.classes['11A2']!.id, 'Bộ bài 11A2');
    // TL-03 cùng chủ đề, Nháp (chưa duyệt): không gắn được, không làm nguồn
    const tl03 = await json<DocView>(
      await createMd(
        request,
        lan,
        'Bài giảng AI · Mặt phẳng trung trực',
        '# Bài giảng AI · Mặt phẳng trung trực\n\n## Định nghĩa\nTL03-NHAP: mặt phẳng trung trực là tập hợp điểm cách đều hai đầu đoạn thẳng.\n',
      ),
      201,
    );
    expect(tl03.status).toBe('draft');
    const g = await request.post(`${API}/api/v1/teacher/knowledge/${tl03.id}/grants`, {
      headers: bearer(lan.token),
      data: { classId: w.classes['11A2']!.id },
    });
    expect(g.status()).toBe(409);
    await ack(request, bao);
    await lectureApproval(request, true);
    try {
      const r = await ask(request, bao, set.items[0]!.assignmentId, 'Mặt phẳng trung trực là gì?');
      const p = (await prompt(request, r.interactionId)) ?? '';
      expect(p).toContain('[BGM-mat-phang-trung-truc] (Bài giảng mẫu) Mặt phẳng trung trực');
      expect(p).not.toContain('TL03-NHAP');
      expect(p).not.toMatch(/\[TL-\d+/);
      expect(r.kind).toBe('concept');
      expect(r.source).toMatchObject({ type: 'lecture', typeLabel: 'Bài giảng mẫu', title: 'Mặt phẳng trung trực' });
      expect(r.source!.label).toBe('Theo: Bài giảng mẫu · Mặt phẳng trung trực');
    } finally {
      await lectureApproval(request, false);
    }
  });
});

test('AI dẫn nguồn không có trong danh sách thì câu trả lời bị bỏ @TC-S5E3-15', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  await mock(request, an.id, 's5e3-foreign');
  const r = await ask(request, an, bc.id, Q_SC1);
  expect(r.kind).toBe('no_source');
  expect(r.reply).toBe(MSG.s20);
  expect(r.source).toBeNull();
  expect(JSON.stringify(r)).not.toContain('côsin');
  expect(JSON.stringify(r)).not.toContain('nguồn ngoài danh sách');
  expect((await decisionOf(request, w.orgId, an.id, r.interactionId)).rule_code).toBe('invalid_source');
});

test('Học sinh lớp khác không dùng được tài liệu của 11A1 @TC-S5E3-16', async ({ request }) => {
  const { w, bc } = await setup(request);
  const khoi = w.people['Thầy Khôi']!;
  const mina = w.people['Mi Na']!;
  const set = await assignSet(request, khoi, w.classes['10B']!.id, 'Bộ bài 10B');
  await ack(request, mina);
  const r = await ask(request, mina, set.items[0]!.assignmentId, Q_SC1);
  const p = (await prompt(request, r.interactionId)) ?? '';
  expect(p).not.toContain('TL-01');
  expect(p).not.toContain('Hệ thức 1/AH²');
  expect(r.source?.docCode ?? '').not.toBe('TL-01');
  expect(JSON.stringify(r)).not.toContain('Ghi chú Khoảng cách 11A1');
  // hỏi thẳng trên BC-01 của 11A1: không lộ bài có tồn tại
  expect((await askRaw(request, mina, bc.id, Q_SC1)).status()).toBe(404);
});

test('Nội dung gửi AI không chứa danh tính @TC-S5E3-17', async ({ request }) => {
  const { an, bc, c11 } = await setup(request);
  const r = await ask(request, an, bc.id, Q_SC1);
  const p = (await prompt(request, r.interactionId))!;
  expect(p).toContain('Hệ thức 1/AH²');
  expect(p).toContain(Q_SC1);
  for (const s of [an.displayName, 'Bình An', an.userCode, an.id, c11.code, c11.id]) expect(p).not.toContain(s);
});

// ------------------------------------------------------------------ US-S5E3-04

test('Bài có mức "Tắt" thì Hỏi đáp báo đang tắt, không gọi AI @TC-S5E3-24', async ({ request }) => {
  const { w, lan, an, bc } = await setup(request, 'off');
  // bài có chấm mặc định Tắt (QS5-12)
  const h = await json<{ helpLevel: string; isDefault: boolean }>(await request.get(`${API}/api/v1/teacher/assignments/${bc.id}/help-level`, { headers: bearer(lan.token) }), 200);
  expect(h.helpLevel).toBe('off');
  const before = await aiCalls(request, an.id);
  const r = await ask(request, an, bc.id, Q_SC1);
  expect(r.kind).toBe('off');
  expect(r.reply).toBe(MSG.s21);
  expect(await aiCalls(request, an.id)).toBe(before);
  const d = await decisionOf(request, w.orgId, an.id, r.interactionId);
  expect(d.decision).toBe('blocked');
  expect(d.rule_code).toBe('help_level');
});

test('Mức "Chỉ gợi ý" không trả lời khái niệm @TC-S5E3-25', async ({ request }) => {
  const { w, lan, an, c11 } = await setup(request);
  const set = await assignSet(request, lan, c11.id, 'Bộ bài luyện khoảng cách');
  const practice = set.items[0]!.assignmentId;
  // bài luyện mặc định Gợi ý và khái niệm; cô Lan đặt Chỉ gợi ý
  expect((await setHelp(request, lan, practice, 'hints')).summary).toBe('Trợ giúp: Chỉ gợi ý');
  const r = await ask(request, an, practice, 'Định nghĩa khoảng cách từ điểm đến mặt phẳng?');
  expect(r.kind).toBe('hint');
  expect(r.reply).toContain('Gợi ý');
  expect(r.reply).not.toContain('độ dài đoạn AH');
  expect(r.source).toBeNull();
  expect((await decisionOf(request, w.orgId, an.id, r.interactionId)).rule_code).toBe('kind_not_allowed');
});

test('Lớp tắt AI thì chặn trước mọi bước khác @TC-S5E3-27', async ({ request }) => {
  const { w, lan, an, bc, c11 } = await setup(request);
  expect((await teacherApi.settings(request, lan.token, c11.id, { aiEnabled: false })).status()).toBe(200);
  const before = await aiCalls(request, an.id);
  const r = await askRaw(request, an, bc.id, Q_SC1);
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { message: string }).message).toBe(MSG.classOff);
  expect(await aiCalls(request, an.id)).toBe(before);
  const s = await state(request, w.orgId, { userId: an.id, targetId: bc.id });
  expect(s.decisions.at(-1)).toMatchObject({ decision: 'blocked', rule_code: 'class_switch' });
  expect(s.decisions.at(-1)!.interaction_id ?? null).toBeNull();
});

test('Em Đã rời không hỏi được trên bài của lớp @TC-S5E3-28', async ({ request }) => {
  const { w, bc } = await setup(request);
  const tu = w.people['Văn Tú']!;
  await ack(request, tu);
  const before = await aiCalls(request, tu.id);
  const r = await askRaw(request, tu, bc.id, Q_SC1);
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  expect(await aiCalls(request, tu.id)).toBe(before);
});
