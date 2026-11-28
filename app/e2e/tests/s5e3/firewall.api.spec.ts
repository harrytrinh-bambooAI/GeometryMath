import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { expect, test } from '../../support/fixtures.ts';
import { API, aiCalls, bearer } from '../s3e1/s3e1.ts';
import { deleteAccount } from '../s3e5/account.ts';
import { edit, json, publish, type DocView } from './kb.ts';
import {
  ANSWER_FORMS,
  MSG,
  PRIVATE_NOTE,
  Q_SC1,
  RUBRIC_LINE,
  ask,
  decisionOf,
  firewall,
  leak,
  mock,
  prompt,
  release,
  setup,
  state,
  warningsUrl,
} from './qa.ts';

/**
 * US-S5E3-05 — kiểm soát đáp án đầy đủ KS-1…KS-5 cho hỏi đáp theo kho: BC-01 (đề D-KC-01, đáp số engine a√6/3) còn khoá,
 * AI giả lập trả câu chứa đáp số (F-S5-094b, s5e3-leak). Bộ câu thử cố định: app/e2e/fixtures/firewall-s5.json.
 */

const here = dirname(fileURLToPath(import.meta.url));
const SUITE = JSON.parse(readFileSync(join(here, '..', '..', 'fixtures', 'firewall-s5.json'), 'utf8')) as {
  version: string;
  items: Array<{ id: string; group: string; q: string }>;
};

/** TL-01 v3: thêm mục có đáp số của bài đang khoá, gắn 11A1 (Đang dùng). */
async function tl01v3(rq: Parameters<typeof edit>[0], lan: Parameters<typeof edit>[1], v2: DocView) {
  const v3 = await json<DocView>(
    await edit(rq, lan, 'TL-01', 2, `${v2.content}\n## Đáp án bài 15 phút\nKhoảng cách từ A đến (SBC): AH = a√6/3.\n`),
    201,
  );
  expect(v3.version).toBe(3);
  return publish(rq, lan, 'TL-01', 3);
}

test('Câu trả lời chứa đáp số bài đang khoá bị thay bằng gợi ý an toàn @TC-S5E3-29', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  await mock(request, an.id, 'F-S5-094b');
  const r = await ask(request, an, bc.id, 'Khoảng cách từ A đến (SBC) bằng bao nhiêu?');
  expect(r.kind).toBe('refused_answer');
  for (const f of ['a√6/3', '√6/3', '0,816']) expect(r.reply).not.toContain(f);
  expect(JSON.stringify(r)).not.toContain('√6');
  const d = await decisionOf(request, w.orgId, an.id, r.interactionId);
  expect(d).toMatchObject({ decision: 'blocked', rule_code: 'KS-1' });
  // không tính thêm lượt gợi ý (PO-44): câu từ chối dẫn tới gợi ý sẵn có, không mở gợi ý mới
  expect(r.reply).toContain('gợi ý của bài');
});

test('Học sinh dán đáp số nhờ xác nhận bị chặn trước khi gọi AI @TC-S5E3-30', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  await mock(request, an.id, 's5e3-confirm');
  const before = await aiCalls(request, an.id);
  const r = await ask(request, an, bc.id, 'Có phải đáp số là a√6/3 không?');
  expect(r.kind).toBe('refused_answer');
  const plain = r.reply.toLowerCase();
  for (const word of ['đúng', 'sai', 'chính xác', 'không phải', 'phải rồi', 'vâng', 'a√6/3']) expect(plain).not.toContain(word);
  expect(await aiCalls(request, an.id)).toBe(before);
  expect(await prompt(request, r.interactionId)).toBeNull();
  expect((await decisionOf(request, w.orgId, an.id, r.interactionId)).rule_code).toBe('KS-4');
});

test('Sau giờ công bố thì không chặn đáp số của chính bài @TC-S5E3-31', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  await release(request, w.orgId, bc.id); // cô Lan bấm Trả bài
  await mock(request, an.id, 'F-S5-094b');
  const r = await ask(request, an, bc.id, 'Khoảng cách từ A đến (SBC) bằng bao nhiêu?');
  expect(r.kind).toBe('concept');
  expect(r.reply).toContain('a√6/3');
  expect((await decisionOf(request, w.orgId, an.id, r.interactionId)).decision).toBe('allowed');
});

test('Mục tài liệu chứa đáp số của bài đang khoá không được gửi AI @TC-S5E3-32', async ({ request }) => {
  const { w, lan, an, bc, tl } = await setup(request);
  await tl01v3(request, lan, tl);
  const r = await ask(request, an, bc.id, 'Khoảng cách từ A đến (SBC) tính bằng hệ thức nào?');
  const p = (await prompt(request, r.interactionId)) ?? '';
  expect(p).toContain('Hệ thức 1/AH²');
  expect(p).not.toContain('Đáp án bài 15 phút');
  expect(p).not.toContain('a√6/3');
  expect(r.kind).toBe('concept');
  const ws = await json<{ items: Array<{ message: string; docCode: string; version: number }> }>(
    await request.get(warningsUrl(bc.id), { headers: bearer(lan.token) }),
    200,
  );
  expect(ws.items.map((i) => i.message)).toContain('Tài liệu TL-01 v3 có đáp số của bài đang khoá');
  // giáo viên khác không xem được cảnh báo của BC-01
  expect((await request.get(warningsUrl(bc.id), { headers: bearer(w.people['Thầy Khôi']!.token) })).status()).toBe(404);
});

test('Ghi chú riêng, rubric và bài của bạn không bao giờ tới AI hay học sinh @TC-S5E3-33', async ({ request }) => {
  const { an, bc } = await setup(request, 'hints_concepts', { privateNote: PRIVATE_NOTE });
  await mock(request, an.id, 's5e3-leak');
  for (const q of ['Bạn Mai làm bài này thế nào?', 'Rubric chấm bài này ra sao?', Q_SC1]) {
    const r = await ask(request, an, bc.id, q);
    const p = (await prompt(request, r.interactionId)) ?? '';
    for (const s of [PRIVATE_NOTE, RUBRIC_LINE, 'a√3/3', 'Chi Mai']) expect(p, `yêu cầu AI của "${q}"`).not.toContain(s);
    expect(leak(JSON.stringify(r)), `phản hồi của "${q}"`).toBeNull();
  }
});

test('Bộ kiểm lỗi thì đóng, không trả nội dung dở @TC-S5E3-34', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  await firewall(request, an.id, { fault: true });
  try {
    const r = await ask(request, an, bc.id, Q_SC1);
    expect(r.kind).toBe('error_closed');
    expect(r.message).toBe(MSG.s24);
    expect(r.retry).toBe(true);
    expect(r.source).toBeNull();
    expect(JSON.stringify(r)).not.toContain('độ dài đoạn AH');
    expect(JSON.stringify(r)).not.toContain('1/AH²');
    const d = await decisionOf(request, w.orgId, an.id, r.interactionId);
    expect(d).toMatchObject({ decision: 'error_closed', rule_code: 'ERR_GUARD' });
  } finally {
    await firewall(request, an.id, { fault: false });
  }
});

test('Bộ câu thử lộ đáp án chạy trọn: số lần lộ = 0 @TC-S5E3-35', async ({ request }, testInfo) => {
  test.setTimeout(180_000);
  const { w, lan, an, bc, tl } = await setup(request, 'hints_concepts', { privateNote: PRIVATE_NOTE });
  await tl01v3(request, lan, tl); // nhóm "qua tài liệu giáo viên có đáp số"
  await mock(request, an.id, 's5e3-leak');
  expect(SUITE.items.length).toBeGreaterThanOrEqual(60);
  const count = (g: string) => SUITE.items.filter((i) => i.group === g).length;
  expect([count('direct'), count('teacher_doc'), count('roleplay'), count('confirm'), count('private')]).toEqual([20, 15, 10, 10, 5]);
  const report: Record<string, { n: number; leaked: number; rules: Record<string, number> }> = {};
  const leaks: string[] = [];
  const ids: string[] = [];
  for (const it of SUITE.items) {
    const r = await ask(request, an, bc.id, it.q);
    ids.push(r.interactionId);
    const l = leak(JSON.stringify(r));
    if (l) leaks.push(`${it.id}: ${l}`);
    // phần mục tài liệu của yêu cầu (bỏ câu hỏi của chính em trong thẻ dữ liệu)
    const p = ((await prompt(request, r.interactionId)) ?? '').split('<cau_hoi_hoc_sinh>')[0]!;
    for (const s of [PRIVATE_NOTE, RUBRIC_LINE, 'a√6/3', 'Đáp án bài 15 phút']) if (p.includes(s)) leaks.push(`${it.id}: yêu cầu AI chứa "${s}"`);
    const g = (report[it.group] ??= { n: 0, leaked: 0, rules: {} });
    g.n++;
    if (l) g.leaked++;
  }
  const s = await state(request, w.orgId, { userId: an.id, targetId: bc.id });
  for (const id of ids) {
    const ds = s.decisions.filter((d) => d.interaction_id === id);
    expect(ds, `một AIPolicyDecision cho lượt ${id}`).toHaveLength(1);
    const item = SUITE.items[ids.indexOf(id)]!;
    const key = `${ds[0]!.decision}:${ds[0]!.rule_code ?? '-'}`;
    report[item.group]!.rules[key] = (report[item.group]!.rules[key] ?? 0) + 1;
  }
  await testInfo.attach('firewall-s5-report.json', {
    body: JSON.stringify({ version: SUITE.version, total: SUITE.items.length, leaked: leaks.length, groups: report }, null, 2),
    contentType: 'application/json',
  });
  expect(leaks, 'số lần lộ').toEqual([]);
  // mọi dạng đáp số đều được coi là lộ khi đếm (tự kiểm của bộ đếm)
  for (const f of ANSWER_FORMS) expect(leak(`x ${f} y`)).not.toBeNull();
});

test('Bộ kiểm chạy quá 200 ms thì đóng @TC-S5E3-36', async ({ request }) => {
  const { w, an, bc } = await setup(request);
  await mock(request, an.id, 'F-S5-094', 3_000); // AI trả sau 3 giây: không tính vào 200 ms của bộ kiểm
  try {
    await firewall(request, an.id, { delayMs: 250 });
    const slow = await ask(request, an, bc.id, Q_SC1);
    expect(slow.kind).toBe('error_closed');
    expect(slow.message).toBe(MSG.s24);
    expect(await decisionOf(request, w.orgId, an.id, slow.interactionId)).toMatchObject({
      decision: 'error_closed',
      rule_code: 'ERR_TIMEOUT',
    });
    await firewall(request, an.id, { delayMs: 150 });
    const ok = await ask(request, an, bc.id, Q_SC1);
    expect(ok.kind).toBe('concept');
    expect(ok.source?.sectionTitle).toBe('Hệ thức 1/AH²');
    expect((await decisionOf(request, w.orgId, an.id, ok.interactionId)).decision).toBe('allowed');
  } finally {
    await firewall(request, an.id, { delayMs: 0 });
    await mock(request, an.id, '', 0);
  }
});

test('Xoá tài khoản học sinh xoá lịch sử hỏi đáp của em @TC-S5E3-37', async ({ request }) => {
  const { w, lan, an, bc } = await setup(request);
  const mai = w.people['Chi Mai']!;
  await json(await request.post(`${API}/api/v1/student/kb-qa/privacy`, { headers: bearer(mai.token), data: { version: 'privacy-qa-s5-1.0' } }), 200);
  for (let i = 0; i < 6; i++) await ask(request, an, bc.id, i % 2 ? Q_SC1 : 'Hệ thức 1/AH² dùng khi nào?');
  await ask(request, mai, bc.id, Q_SC1);
  const before = await state(request, w.orgId, { targetId: bc.id });
  expect(before.interactions.filter((i) => i.student_id === an.id)).toHaveLength(6);
  expect(before.decisions.filter((d) => d.student_id === an.id)).toHaveLength(6);
  // Bình An xoá tài khoản; chạy job xoá
  const del = await deleteAccount(request, an.token, an.id, { password: an.password, confirm: 'XÓA' });
  expect(del.status(), await del.text()).toBe(200);
  const purge = await request.post(`${API}/test-hooks/s3e5/account/purge`, { data: { days: 31 } });
  expect(purge.status()).toBe(200);
  const after = await state(request, w.orgId, { targetId: bc.id });
  expect(after.interactions.filter((i) => i.student_id === an.id)).toHaveLength(0);
  expect(after.decisions.filter((d) => d.student_id === an.id)).toHaveLength(0);
  // lịch sử BC-01 còn lượt của Chi Mai; bộ đếm theo luật không đổi
  expect(after.interactions.filter((i) => i.student_id === mai.id)).toHaveLength(1);
  // (trường dùng chung giữa các ca chạy song song: bộ đếm chỉ có thể tăng, không giảm vì xoá tài khoản)
  for (const c of before.counters) {
    const a = after.counters.find((x) => x.decision === c.decision && x.rule_code === c.rule_code);
    expect(Number(a?.n ?? 0), `bộ đếm ${c.decision}:${c.rule_code}`).toBeGreaterThanOrEqual(Number(c.n));
  }
  expect(lan.role).toBe('TEACHER');
});
