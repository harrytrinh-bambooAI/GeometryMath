import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { attemptRaw, createDraft, play, report, result, sandbox, student, walk, type SetDetail } from '../s2e8/s2e8.ts';
import {
  ANGLES,
  MSG,
  assignedWithChoices,
  choiceAttempt,
  expectError,
  ok,
  optionId,
  options,
  putChoice,
  studentChoices,
  teacherChoices,
  type ChoiceAttempt,
  type StudentChoice,
  type TeacherChoice,
} from './s2e9.ts';

/**
 * S2-E9 — API câu trắc nghiệm trong bài luyện (US-S2E9-03): biên số lựa chọn và độ dài, đúng một đáp án đúng, số lượt
 * tối đa, dữ liệu trả cho học sinh không có cờ đúng, mã lựa chọn lạ không trừ lượt, bấm đôi chỉ trừ một lượt.
 */

async function draft(request: Parameters<typeof sandbox>[0]) {
  const sb = await sandbox(request);
  const d = await ok<SetDetail>(await createDraft(request, sb.teacher.token, 'Trắc nghiệm góc', [{ code: 'SP-01' }]), 201);
  return { sb, t: sb.teacher.token, setId: d.id };
}

test('@TC-S2E9-034 2 và 5 lựa chọn nhận, 1 và 6 bị chặn', async ({ request }) => {
  const { t, setId } = await draft(request);
  await expectError(await putChoice(request, t, setId, 1, options(['45°'])), 400, 'VALIDATION_FAILED', MSG.choiceCount);
  expect(await teacherChoices(request, t, setId)).toHaveLength(0);
  const two = await ok<TeacherChoice>(await putChoice(request, t, setId, 1, options(['45°', '60°'])));
  expect(two.options).toHaveLength(2);
  expect(two.maxAttempts).toBe(1);
  const five = await ok<TeacherChoice>(await putChoice(request, t, setId, 1, options(['30°', '45°', '60°', '90°', '120°'])));
  expect(five.options.map((o) => o.text)).toEqual(['30°', '45°', '60°', '90°', '120°']);
  await expectError(await putChoice(request, t, setId, 1, options(['30°', '45°', '60°', '90°', '120°', '135°'])), 400, 'VALIDATION_FAILED', MSG.choiceCount);
  // lần lưu hỏng không đổi cấu hình đã lưu
  expect((await teacherChoices(request, t, setId))[0]!.options).toHaveLength(5);
});

test('@TC-S2E9-035 lựa chọn 100 ký tự nhận, 101 bị chặn, rỗng bị chặn', async ({ request }) => {
  const { t, setId } = await draft(request);
  const c100 = 'x'.repeat(100);
  const saved = await ok<TeacherChoice>(await putChoice(request, t, setId, 1, options([c100, '45°'])));
  expect(saved.options[0]!.text).toHaveLength(100);
  await expectError(await putChoice(request, t, setId, 1, options(['x'.repeat(101), '45°'])), 400, 'VALIDATION_FAILED');
  await expectError(await putChoice(request, t, setId, 1, options(['', '45°'])), 400, 'VALIDATION_FAILED', MSG.choiceEmpty);
  await expectError(await putChoice(request, t, setId, 1, options(['   ', '45°'])), 400, 'VALIDATION_FAILED', MSG.choiceEmpty);
  expect((await teacherChoices(request, t, setId))[0]!.options[0]!.text).toBe(c100);
});

test('@TC-S2E9-037 hai lựa chọn đúng bị chặn', async ({ request }) => {
  const { sb, t, setId } = await draft(request);
  const twoCorrect = ANGLES.map((text, i) => ({ text, correct: i < 2 }));
  await expectError(await putChoice(request, t, setId, 1, twoCorrect), 400, 'VALIDATION_FAILED', MSG.choiceManyCorrect);
  await expectError(await putChoice(request, t, setId, 1, options(ANGLES, null)), 400, 'VALIDATION_FAILED', MSG.choiceNoCorrect);
  await expectError(await putChoice(request, t, setId, 1, options(['45°', ' 45° ', '60°'], 0)), 400, 'VALIDATION_FAILED', MSG.choiceDuplicate);
  expect(await teacherChoices(request, t, setId)).toHaveLength(0); // không lưu
  // giáo viên khác không soạn được câu của bộ này
  await expectError(await putChoice(request, sb.otherTeacher.token, setId, 1, options(ANGLES)), 404, 'NOT_FOUND');
});

test('@TC-S2E9-039 dữ liệu trả về không chứa cờ lựa chọn đúng', async ({ request }) => {
  const sb = await sandbox(request);
  const { set, choices } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°', maxAttempts: 2 }]);
  const an = student(sb, 'An').token;
  const item = set.items[0]!;
  const correctId = optionId(choices[0]!, '45°');
  const wrongId = optionId(choices[0]!, '60°');

  const opened = await play(request, an, set.id);
  const listRes = await studentChoices(request, an, set.id);
  expect(listRes.headers()['cache-control']).toContain('no-store');
  const list = (await ok<{ items: StudentChoice[] }>(listRes)).items;
  expect(list).toHaveLength(1);
  expect(list[0]!.options.map((o) => o.text)).toEqual(ANGLES);
  const wrong = await ok<ChoiceAttempt>(await choiceAttempt(request, an, item.assignmentId, wrongId, randomUUID()));
  expect(wrong.outcome).toBe('wrong');
  expect(wrong.attemptsLeft).toBe(1);
  const after = (await ok<{ items: StudentChoice[] }>(await studentChoices(request, an, set.id))).items;
  expect(after[0]!.dimmed).toEqual([wrongId]);

  const SECRET = /correct|right|isAnswer|answer|solution|index|key$/i;
  for (const [where, body] of [['mở bài', opened], ['lựa chọn', list], ['sau khi sai', after], ['phản hồi Sai', wrong]] as const) {
    const { keys } = walk(body);
    expect(keys.filter((k) => SECRET.test(k) && k !== 'lastAnswer'), `${where}: khóa lộ lựa chọn đúng`).toEqual([]);
  }
  // phản hồi Sai không nêu lựa chọn đúng (không chữ, không mã)
  const wrongText = JSON.stringify(wrong);
  expect(wrongText).not.toContain(correctId);
  expect(wrongText).not.toContain('45°');
  // mỗi lựa chọn chỉ có mã + chữ; mã không theo thứ tự đoán được
  for (const o of after[0]!.options) expect(Object.keys(o).sort()).toEqual(['id', 'text']);
  // đường điền đáp số không nhận câu trắc nghiệm (không dò đáp số bằng cách gõ tay), không trừ lượt
  const typed = await attemptRaw(request, an, item.assignmentId, '45');
  await expectError(typed, 400, 'VALIDATION_FAILED');
  expect((await play(request, an, set.id)).items[0]!.attemptsLeft).toBe(1);
});

test('@TC-S2E9-040 gửi mã lựa chọn không thuộc câu không trừ lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const { set, choices } = await assignedWithChoices(
    request,
    sb,
    [{ code: 'SP-01' }, { code: 'LB-01' }],
    [
      { position: 1, texts: ANGLES, correct: '45°' },
      { position: 2, texts: ['1/2', '1/3', '2/3'], correct: '1/2' },
    ],
  );
  const an = student(sb, 'An').token;
  const q1 = set.items[0]!;
  expect(q1.maxAttempts).toBe(1);
  const other = optionId(choices[1]!, '1/3'); // lựa chọn của câu khác
  await expectError(await choiceAttempt(request, an, q1.assignmentId, other, randomUUID()), 400, 'VALIDATION_FAILED');
  await expectError(await choiceAttempt(request, an, q1.assignmentId, randomUUID(), randomUUID()), 400, 'VALIDATION_FAILED');
  const view = await play(request, an, set.id);
  expect(view.items[0]!.attemptsLeft).toBe(1); // lượt vẫn 1
  expect(view.items[0]!.state).toBe('todo'); // không có Attempt nào
  // lựa chọn hợp lệ vẫn chấm được sau đó
  const good = await ok<ChoiceAttempt>(await choiceAttempt(request, an, q1.assignmentId, optionId(choices[0]!, '45°'), randomUUID()));
  expect(good.outcome).toBe('correct');
  expect(good.message).toBe(MSG.right);
});

test('@TC-S2E9-041 bấm đôi chỉ trừ một lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const { set, choices } = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°', maxAttempts: 2 }]);
  const an = student(sb, 'An').token;
  const item = set.items[0]!;
  const key = randomUUID();
  const wrongId = optionId(choices[0]!, '60°');
  const [a, b] = await Promise.all([
    choiceAttempt(request, an, item.assignmentId, wrongId, key),
    choiceAttempt(request, an, item.assignmentId, wrongId, key),
  ]);
  const first = await ok<ChoiceAttempt>(a);
  const second = await ok<ChoiceAttempt>(b);
  expect(first.outcome).toBe('wrong');
  expect(second).toEqual(first);
  expect(first.attemptsLeft).toBe(1);
  const view = await play(request, an, set.id);
  expect(view.items[0]!.attemptsLeft).toBe(1); // một Attempt, lượt còn 1
  // gửi lại lần ba cùng mã vẫn không trừ thêm
  expect((await ok<ChoiceAttempt>(await choiceAttempt(request, an, item.assignmentId, wrongId, key))).attemptsLeft).toBe(1);
});

test('@TC-S2E9-044 số lượt trắc nghiệm đặt tối đa bằng số lựa chọn trừ 1', async ({ request }) => {
  const { sb, t, setId } = await draft(request);
  const three = await ok<TeacherChoice>(await putChoice(request, t, setId, 1, options(ANGLES), 3));
  expect(three.maxAttempts).toBe(3);
  await expectError(await putChoice(request, t, setId, 1, options(ANGLES), 4), 400, 'VALIDATION_FAILED', 'Tối đa 3 lượt cho câu 4 lựa chọn');
  await expectError(await putChoice(request, t, setId, 1, options(ANGLES), 0), 400, 'VALIDATION_FAILED', 'Tối đa 3 lượt cho câu 4 lựa chọn');
  await expectError(await putChoice(request, t, setId, 1, options(['45°', '60°']), 2), 400, 'VALIDATION_FAILED', 'Tối đa 1 lượt cho câu 2 lựa chọn');
  expect((await teacherChoices(request, t, setId))[0]!.maxAttempts).toBe(3);
  // hết lượt ở câu trắc nghiệm: báo "Em nghĩ đáp số này đúng" bị từ chối (BR-S2E9-14), kết quả đếm "cần xem lại"
  const full = await assignedWithChoices(request, sb, [{ code: 'SP-01' }], [{ position: 1, texts: ANGLES, correct: '45°' }], 'Một lượt');
  const an = student(sb, 'An').token;
  const q = full.set.items[0]!;
  const wrong = await ok<ChoiceAttempt>(await choiceAttempt(request, an, q.assignmentId, optionId(full.choices[0]!, '90°'), randomUUID()));
  expect(wrong.message).toBe(MSG.wrongLast);
  expect(wrong.state).toBe('review');
  expect((await report(request, an, q.assignmentId, 'em nghĩ đúng')).status()).toBe(409);
  expect((await result(request, an, full.set.id)).summary).toBe('0 câu đúng · 1 câu cần xem lại');
});
