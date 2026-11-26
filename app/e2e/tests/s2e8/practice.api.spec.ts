import { expect, test } from '../../support/fixtures.ts';
import {
  API,
  BL1,
  HINT_Q2,
  assign,
  assigned,
  attempt,
  attemptRaw,
  bearer,
  createDraft,
  exhaust,
  expectNoAnswer45,
  hours,
  play,
  report,
  result,
  sandbox,
  setBody,
  student,
  walk,
  type SetDetail,
} from './s2e8.ts';

/**
 * S2-E8 — API làm bài, kết quả, soạn bài và báo chấm sai (US-S2E8-03, 04, 05): cách ly lớp, bài đã đóng, khóa lời giải
 * tới giờ mở, biên của cấu hình bài, báo "Em nghĩ đáp số này đúng".
 */

test('@TC-S2E8-066 bài của lớp khác không mở được', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb); // giao cho 11A1
  const chi = student(sb, 'Chi').token; // học sinh 11A2
  const r = await request.get(`${API}/api/v1/student/practice-sets/${set.id}`, { headers: bearer(chi) });
  expect(r.status()).toBe(404);
  const body = (await r.json()) as { code: string };
  expect(body.code).toBe('NOT_FOUND');
  expect(walk(body).keys).not.toContain('items');
  expect((await attemptRaw(request, chi, set.items[1]!.assignmentId, '45')).status()).toBe(404);
  expect((await request.get(`${API}/api/v1/student/practice-sets/${set.id}/result`, { headers: bearer(chi) })).status()).toBe(404);
  const list = (await (await request.get(`${API}/api/v1/student/practice-sets`, { headers: bearer(chi) })).json()) as { items: unknown[] };
  expect(list.items).toEqual([]);
});

test('@TC-S2E8-067 chưa đăng nhập không mở được bài', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  for (const path of [`practice-sets/${set.id}`, `practice-sets/${set.id}/result`, 'practice-sets']) {
    const r = await request.get(`${API}/api/v1/student/${path}`);
    expect(r.status(), path).toBe(401);
    const body = (await r.json()) as { code: string };
    expect(body.code).toBe('UNAUTHENTICATED');
    expect(walk(body).keys).not.toContain('items');
  }
});

test('@TC-S2E8-069 bài đã đóng chỉ xem kết quả', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await attempt(request, an, set.items[0]!.assignmentId, '6');
  const closed = await request.post(`${API}/api/v1/teacher/practice-sets/${set.id}/close`, { headers: bearer(sb.teacher.token) });
  expect(closed.status()).toBe(200);
  expect(((await closed.json()) as SetDetail).status).toBe('closed');
  const r = await attemptRaw(request, an, set.items[1]!.assignmentId, '45');
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { code: string }).code).toBe('CONFLICT');
  expect((await play(request, an, set.id)).status).toBe('closed'); // giao diện chuyển tới màn kết quả
  const res = await result(request, an, set.id);
  expect(res.summary).toBe('1 câu đúng · 0 câu cần xem lại');
});

test('@TC-S2E8-073 gợi ý mức 1 không nêu đáp số ở mọi dạng viết', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const before = await play(request, an, set.id);
  expect(before.items[1]!.hint ?? null, 'chưa Sai thì chưa có gợi ý').toBeNull();
  const r = await attempt(request, an, set.items[1]!.assignmentId, '60');
  expect(r.hint).toBe(HINT_Q2);
  expect(r.hint!).not.toMatch(/45|90\s*\/\s*2|bốn mươi lăm|tan\s*=\s*1|SCA\s*=|AC\s*=/i);
  const after = await play(request, an, set.id);
  expect(after.items[1]!.hint).toBe(HINT_Q2);
  expectNoAnswer45(after, 'mở bài sau khi Sai');
});

test('@TC-S2E8-113 bộ câu thử moi đáp số cho bài luyện: dò bằng đáp số, lặp gợi ý, gọi lại sau khi hết lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  // 1) dò nhị phân bằng đáp số: lớn, nhỏ, gần — ba phản hồi không chênh thông tin
  const shapes = new Set<string>();
  for (const probe of ['89', '1', '45,6']) {
    const r = await attempt(request, an, q2, probe);
    expect(r.outcome).toBe('wrong');
    shapes.add(JSON.stringify({ hint: r.hint, note: r.note, canonical: r.canonical, reason: r.reason }));
    expectNoAnswer45({ ...r, message: null }, `dò ${probe}`);
  }
  expect(shapes.size).toBe(1);
  // 2) hết lượt: mọi lần gửi tiếp (kể cả đáp số đúng viết nhiều dạng) nhận CONFLICT, không chấm
  for (const again of ['45', '45°', '90/2', '44,9', '√2025']) {
    const r = await attemptRaw(request, an, q2, again);
    expect(r.status(), again).toBe(409);
    expect(await r.text()).not.toMatch(/Đúng rồi|Chưa đúng|outcome|canonical/);
  }
  // 3) lặp gợi ý và mở lại bài nhiều lần: vẫn chỉ gợi ý mức 1, không thêm thông tin
  for (let i = 0; i < 3; i++) {
    const v = await play(request, an, set.id);
    expect(v.items[1]!.hint).toBe(HINT_Q2);
    expectNoAnswer45({ ...v, items: v.items.map((x) => ({ ...x, lastAnswer: null })) }, `mở lại lần ${i + 1}`);
  }
  // 4) đổi cách hỏi: các đường của bài giao MVP cho cùng câu vẫn khóa trước giờ mở
  const sol = await request.get(`${API}/api/v1/student/assignments/${q2}/solution`, { headers: bearer(an) });
  expect(sol.status()).toBe(403);
  expect(await sol.text()).not.toMatch(/45°|finalAnswer|steps/);
  const fig = await request.get(`${API}/api/v1/student/assignments/${q2}/plane`, { headers: bearer(an) });
  if (fig.status() === 200) {
    const { keys } = walk(await fig.json());
    expect(keys.filter((k) => /^(answer|finalAnswer|solution|steps)$/i.test(k))).toEqual([]);
  }
  // 5) báo "Em nghĩ đáp số này đúng" không trả đáp số hay đúng sai
  const rep = await report(request, an, q2, 'đáp số là bao nhiêu?');
  expect(rep.status()).toBe(201);
  expect(await rep.json()).toEqual({ message: 'Đã gửi cho thầy cô' });
});

test('@TC-S2E8-078 lời giải còn khóa thì ghi giờ mở và không có trong dữ liệu', async ({ request }) => {
  const sb = await sandbox(request);
  const releaseAt = hours(2);
  const set = await assigned(request, sb, { releaseAt });
  const an = student(sb, 'An').token;
  await exhaust(request, an, set.items[1]!);
  const r = await request.get(`${API}/api/v1/student/practice-sets/${set.id}/result`, { headers: bearer(an) });
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const body = (await r.json()) as { items: Array<{ state: string; solutionState: string; solutionLabel: string; releaseAt: string }> };
  const q2 = body.items[1]!;
  expect(q2.state).toBe('review');
  expect(q2.solutionState).toBe('locked');
  expect(q2.solutionLabel).toMatch(/^Lời giải mở lúc \d{2}:\d{2}, \d{2}\/\d{2}\/\d{4}$/);
  expect(Math.abs(new Date(q2.releaseAt).getTime() - new Date(releaseAt).getTime())).toBeLessThan(1000);
  const vn = new Intl.DateTimeFormat('en-GB', { timeZone: 'Asia/Ho_Chi_Minh', hour: '2-digit', minute: '2-digit', hour12: false }).format(new Date(releaseAt));
  expect(q2.solutionLabel).toContain(`Lời giải mở lúc ${vn},`);
  const { keys } = walk(body);
  expect(keys.filter((k) => /^(answer|finalAnswer|solution|steps|canonical|tolerance)$/i.test(k))).toEqual([]);
});

test('@TC-S2E8-082 học sinh khác không xem kết quả bạn', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An');
  await attempt(request, an.token, set.items[0]!.assignmentId, '6');
  await exhaust(request, an.token, set.items[1]!);
  const binh = student(sb, 'Bình').token; // cùng lớp 11A1
  // không có đường nào gọi kết quả theo mã của bạn
  for (const path of [`practice-sets/${set.id}/result/${an.id}`, `practice-sets/${set.id}/results/${an.id}`, `students/${an.id}/practice-sets/${set.id}/result`]) {
    const r = await request.get(`${API}/api/v1/student/${path}`, { headers: bearer(binh) });
    expect(r.status(), path).toBe(404);
    expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  }
  const viaQuery = await request.get(`${API}/api/v1/student/practice-sets/${set.id}/result?userId=${an.id}&studentId=${an.id}`, { headers: bearer(binh) });
  // tham số bị bỏ qua: Bình chỉ nhận kết quả của chính mình (chưa làm câu nào)
  expect(((await viaQuery.json()) as { summary: string }).summary).toBe('0 câu đúng · 0 câu cần xem lại');
  expect((await result(request, binh, set.id)).remaining).toBe(5);
  expect((await result(request, an.token, set.id)).summary).toBe('1 câu đúng · 1 câu cần xem lại');
});

test('@TC-S2E8-086 lời giải không nằm trong bộ nhớ trình duyệt trước giờ mở', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  await exhaust(request, an, set.items[1]!);
  // mọi phản hồi của bài luyện cấm lưu đệm và không chứa lời giải, nên trình duyệt không có gì để giữ lại
  for (const path of [`practice-sets`, `practice-sets/${set.id}`, `practice-sets/${set.id}/result`]) {
    const r = await request.get(`${API}/api/v1/student/${path}`, { headers: bearer(an) });
    expect(r.status(), path).toBe(200);
    expect(r.headers()['cache-control'], path).toContain('no-store');
    const { keys } = walk(await r.json());
    expect(keys.filter((k) => /^(answer|finalAnswer|solution|steps|tolerance)$/i.test(k)), path).toEqual([]);
    expect(await r.text(), path).not.toMatch(/Đáp số:|Lập luận|Thay số vào công thức/);
  }
  const sol = await request.get(`${API}/api/v1/student/assignments/${set.items[1]!.assignmentId}/solution`, { headers: bearer(an) });
  expect(sol.status()).toBe(403);
  expect(sol.headers()['cache-control']).toContain('no-store');
  expect(await sol.text()).not.toMatch(/45°|steps/);
});

test('@TC-S2E8-093 bài đã giao không sửa được', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const body = await setBody(request, sb.teacher.token, set.title, BL1.map((i) => (i.code === 'SP-01' ? { ...i, tolerance: 5 } : i)), hours(2));
  const r = await request.put(`${API}/api/v1/teacher/practice-sets/${set.id}`, { headers: bearer(sb.teacher.token), data: body });
  expect(r.status()).toBe(409);
  const err = (await r.json()) as { code: string; message: string };
  expect(err.code).toBe('CONFLICT');
  expect(err.message).toBe('Bài đã giao không sửa được');
  const again = (await (await request.get(`${API}/api/v1/teacher/practice-sets/${set.id}`, { headers: bearer(sb.teacher.token) })).json()) as SetDetail;
  expect(again.items[1]!.tolerance).toBe(0.5);
  // chấm vẫn theo ảnh chụp ±0,5: 47 là Sai (với ±5 thì đã Đúng)
  expect((await attempt(request, student(sb, 'An').token, set.items[1]!.assignmentId, '47')).outcome).toBe('wrong');
});

test('@TC-S2E8-096 số câu 1 và 20 nhận, 0 và 21 bị chặn', async ({ request }) => {
  const sb = await sandbox(request);
  const t = sb.teacher.token;
  const rect = Array.from({ length: 16 }, (_, i) => ({ code: `HCN-${String(i + 1).padStart(2, '0')}` }));
  const all21 = [...BL1, { code: 'LE-01' }, { code: 'LF-01' }, { code: 'LN-01' }, ...rect.slice(1)].slice(0, 20).concat([{ code: 'HCN-16' }]);
  expect(new Set(all21.map((i) => i.code)).size).toBe(21);
  expect((await createDraft(request, t, 'Một câu', [BL1[0]!])).status()).toBe(201);
  const twenty = await createDraft(request, t, 'Hai mươi câu', all21.slice(0, 20));
  expect(twenty.status(), await twenty.text()).toBe(201);
  expect(((await twenty.json()) as SetDetail).items).toHaveLength(20);
  const zero = await createDraft(request, t, 'Không câu', []);
  expect(zero.status()).toBe(400);
  expect(((await zero.json()) as { code: string; message: string }).message).toBe('Chọn ít nhất 1 câu');
  const many = await createDraft(request, t, 'Quá nhiều', all21);
  expect(many.status()).toBe(400);
  expect(await many.json()).toMatchObject({ code: 'VALIDATION_FAILED', message: 'Tối đa 20 câu' });
});

test('@TC-S2E8-097 số lượt 1 và 5 nhận, 0 và 6 bị chặn', async ({ request }) => {
  const sb = await sandbox(request);
  const t = sb.teacher.token;
  for (const n of [1, 5]) {
    const r = await createDraft(request, t, `Lượt ${n}`, [{ code: 'LC-01', maxAttempts: n }]);
    expect(r.status(), `lượt ${n}`).toBe(201);
    expect(((await r.json()) as SetDetail).items[0]!.maxAttempts).toBe(n);
  }
  for (const n of [0, 6]) {
    const r = await createDraft(request, t, `Lượt ${n}`, [{ code: 'LC-01', maxAttempts: n }]);
    expect(r.status(), `lượt ${n}`).toBe(400);
    expect(await r.json()).toMatchObject({ code: 'VALIDATION_FAILED', message: 'Số lượt từ 1 đến 5' });
  }
});

test('@TC-S2E8-098 sai số 0 và 10 nhận, âm và 10,01 bị chặn', async ({ request }) => {
  const sb = await sandbox(request);
  const t = sb.teacher.token;
  for (const tol of [0, 10]) {
    const r = await createDraft(request, t, `Sai số ${tol}`, [{ code: 'SP-01', answerKind: 'approx', tolerance: tol }]);
    expect(r.status(), `sai số ${tol}`).toBe(201);
    expect(((await r.json()) as SetDetail).items[0]!.tolerance).toBe(tol);
  }
  for (const tol of [-0.1, 10.01]) {
    const r = await createDraft(request, t, `Sai số ${tol}`, [{ code: 'SP-01', answerKind: 'approx', tolerance: tol }]);
    expect(r.status(), `sai số ${tol}`).toBe(400);
    expect(((await r.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  }
  const three = await createDraft(request, t, 'Ba chữ số thập phân', [{ code: 'SP-01', answerKind: 'approx', tolerance: 0.555 }]);
  expect(three.status()).toBe(400);
  expect(await three.json()).toMatchObject({ code: 'VALIDATION_FAILED', message: 'Tối đa 2 chữ số thập phân' });
});

test('@TC-S2E8-099 sai số không áp dụng cho kiểu chính xác', async ({ request }) => {
  const sb = await sandbox(request);
  const d = await createDraft(request, sb.teacher.token, 'Chính xác', [{ code: 'LC-01', answerKind: 'exact', tolerance: 0.5 }]);
  expect(d.status()).toBe(201);
  const draft = (await d.json()) as SetDetail;
  expect(draft.items[0]!.tolerance ?? null, 'API bỏ qua sai số của kiểu chính xác').toBeNull();
  expect((await assign(request, sb.teacher.token, draft.id, sb.classes[0]!.id)).status()).toBe(200);
  const set = (await (await request.get(`${API}/api/v1/teacher/practice-sets/${draft.id}`, { headers: bearer(sb.teacher.token) })).json()) as SetDetail;
  // đáp số mẫu 6: 6,4 nằm trong ±0,5 nhưng kiểu chính xác vẫn chấm Sai
  expect((await attempt(request, student(sb, 'An').token, set.items[0]!.assignmentId, '6,4')).outcome).toBe('wrong');
});

test('@TC-S2E8-102 nút báo không dùng được khi chưa bị chấm Sai', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  expect((await play(request, an, set.id)).items[1]!.canReport).toBe(false);
  const r = await report(request, an, q2, 'Em nghĩ đúng');
  expect(r.status()).toBe(409);
  expect(((await r.json()) as { code: string }).code).toBe('CONFLICT');
  // sai cú pháp chưa phải bị chấm Sai
  await attempt(request, an, q2, '√');
  expect((await report(request, an, q2)).status()).toBe(409);
  const reports = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(sb.teacher.token) })).json()) as { items: unknown[] };
  expect(reports.items).toEqual([]);
});

test('@TC-S2E8-103 mỗi em một báo mỗi câu', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  await attempt(request, an, q2, '44,4');
  expect((await report(request, an, q2, 'lần một')).status()).toBe(201);
  const second = await report(request, an, q2, 'lần hai');
  expect(second.status()).toBe(409);
  expect(await second.json()).toMatchObject({ code: 'CONFLICT', message: 'Em đã báo câu này' });
  const reports = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ note: string }> };
  expect(reports.items).toHaveLength(1);
  expect(reports.items[0]!.note).toBe('lần một');
  const item = (await play(request, an, set.id)).items[1]!;
  expect(item.reported).toBe(true);
  expect(item.canReport).toBe(false);
});

test('@TC-S2E8-104 ghi chú 200 ký tự nhận, 201 bị chặn, rỗng được', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  for (const i of [1, 2, 3]) await attempt(request, an, set.items[i]!.assignmentId, '999');
  expect((await report(request, an, set.items[1]!.assignmentId, 'a'.repeat(200))).status()).toBe(201);
  const long = await report(request, an, set.items[2]!.assignmentId, 'a'.repeat(201));
  expect(long.status()).toBe(400);
  expect(await long.json()).toMatchObject({ code: 'VALIDATION_FAILED', message: 'Ghi chú tối đa 200 ký tự' });
  expect((await report(request, an, set.items[3]!.assignmentId)).status()).toBe(201);
  const reports = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ position: number; note: string | null }> };
  expect(reports.items.map((r) => r.position).sort()).toEqual([2, 4]);
  expect(reports.items.find((r) => r.position === 4)!.note ?? null).toBeNull();
});

test('@TC-S2E8-108 giáo viên khác không thấy báo của lớp mình', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  await attempt(request, an, q2, '44,4');
  expect((await report(request, an, q2, 'Em đo ra 44,4')).status()).toBe(201);
  const mine = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(sb.teacher.token) })).json()) as { items: Array<{ id: string }> };
  expect(mine.items).toHaveLength(1);
  const lan = sb.otherTeacher.token; // Cô Lan, cùng trường, không dạy lớp này
  const hers = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(lan) })).json()) as { items: unknown[] };
  expect(hers.items).toEqual([]);
  const r = await request.post(`${API}/api/v1/teacher/practice-reports/${mine.items[0]!.id}/decision`, { headers: bearer(lan), data: { decision: 'confirm' } });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  expect((await play(request, an, set.id)).items[1]!.state).toBe('doing');
  expect((await request.get(`${API}/api/v1/teacher/practice-sets/${set.id}`, { headers: bearer(lan) })).status()).toBe(404);
});

test('@TC-S2E8-109 học sinh không gửi báo cho bài không thuộc mình', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb); // giao cho 11A1
  const chi = student(sb, 'Chi').token; // 11A2
  const r = await report(request, chi, set.items[1]!.assignmentId, 'thử');
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  const reports = (await (await request.get(`${API}/api/v1/teacher/practice-reports`, { headers: bearer(sb.teacher.token) })).json()) as { items: unknown[] };
  expect(reports.items).toEqual([]);
});

test('@TC-S2E8-110 báo không làm lộ đáp số cho học sinh', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  await attempt(request, an, q2, '44,4');
  const r = await report(request, an, q2, 'Em đo ra 44,4');
  expect(r.status()).toBe(201);
  expect(r.headers()['cache-control']).toContain('no-store');
  expect(await r.json()).toEqual({ message: 'Đã gửi cho thầy cô' });
  const after = await play(request, an, set.id);
  expect(after.items[1]!.attemptsLeft).toBe(2);
  expect(after.items[1]!.state).toBe('doing');
  const { keys } = walk(after);
  expect(keys.filter((k) => /^(answer|finalAnswer|tolerance|answerKind|acceptedForms)$/i.test(k))).toEqual([]);
  expect((after.items[1] as unknown as { canonical?: string }).canonical ?? null).toBeNull();
});

test('@TC-S2E8-115 giáo viên không phải chủ lớp không giao được cho lớp đó', async ({ request }) => {
  const sb = await sandbox(request);
  const lan = sb.otherTeacher.token; // không phải chủ lớp 11A1
  const d = await createDraft(request, lan, 'Bài của Cô Lan', BL1);
  expect(d.status(), await d.text()).toBe(201);
  const draft = (await d.json()) as SetDetail;
  const r = await assign(request, lan, draft.id, sb.classes[0]!.id);
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  const still = (await (await request.get(`${API}/api/v1/teacher/practice-sets/${draft.id}`, { headers: bearer(lan) })).json()) as SetDetail;
  expect(still.status).toBe('draft');
  expect(still.items.every((i) => i.assignmentId == null)).toBe(true);
  const list = (await (await request.get(`${API}/api/v1/student/practice-sets`, { headers: bearer(student(sb, 'An').token) })).json()) as { items: unknown[] };
  expect(list.items).toEqual([]);
  // lớp của trường khác (mã bất kỳ) cũng NOT_FOUND
  expect((await assign(request, sb.teacher.token, draft.id, sb.classes[0]!.id)).status()).toBe(404); // bài của người khác
});
