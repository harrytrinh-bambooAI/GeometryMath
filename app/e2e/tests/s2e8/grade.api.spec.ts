import { randomUUID } from 'node:crypto';
import { expect, test } from '../../support/fixtures.ts';
import { API, HINT_Q2, assigned, attempt, attemptRaw, bearer, expectNoAnswer45, play, sandbox, student } from './s2e8.ts';

/**
 * S2-E8 — API chấm đáp số (US-S2E8-02): chỉ Sai trừ lượt, Sai cú pháp không trừ, không lộ đáp số hay hướng, CONFLICT
 * sau khi Đúng / hết lượt, mã chống lặp. Câu D = câu 2 của BL-1 (xấp xỉ 45 ±0,5°, 3 lượt).
 */

test('@TC-S2E8-029 giá trị sai trừ một lượt và không lộ hướng', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const r = await attempt(request, student(sb, 'An').token, set.items[1]!.assignmentId, '60°');
  expect(r.outcome).toBe('wrong');
  expect(r.attemptsLeft).toBe(2);
  expect(r.maxAttempts).toBe(3);
  expect(r.message).toBe('Chưa đúng. Còn 2 lượt.');
  expect(r.hint).toBe(HINT_Q2);
  expect(JSON.stringify(r)).not.toMatch(/lớn hơn|nhỏ hơn|cao hơn|thấp hơn|gần đúng|lệch/i);
  expectNoAnswer45({ ...r, message: null }, 'phản hồi Sai');
});

test('@TC-S2E8-031 ô trống hoặc chỉ khoảng trắng là Sai cú pháp, không trừ lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  for (const empty of ['', '   ']) {
    const r = await attempt(request, an, set.items[1]!.assignmentId, empty);
    expect(r.outcome).toBe('syntax');
    expect(r.message).toBe('Em hãy nhập đáp số.');
    expect(r.attemptsLeft).toBe(3);
  }
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(3);
});

test('@TC-S2E8-041 đáp số 51 ký tự là Sai cú pháp, không trừ lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const long = '6' + '+0'.repeat(25); // 51 ký tự, giá trị 6
  expect(long).toHaveLength(51);
  const r = await attempt(request, an, set.items[0]!.assignmentId, long);
  expect(r.outcome).toBe('syntax');
  expect(r.reason).toBe('Đáp số tối đa 50 ký tự');
  expect(r.attemptsLeft).toBe(3);
  // đúng 50 ký tự thì được chấm bình thường
  const ok = await attempt(request, an, set.items[0]!.assignmentId, long.slice(0, 49) + '0');
  expect(ok.outcome).toBe('correct');
});

test('@TC-S2E8-044 lượt thử đếm đúng khi Sai lần lượt 3 lần', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  expect((await attempt(request, an, q2, '60')).attemptsLeft).toBe(2);
  expect((await attempt(request, an, q2, '61')).attemptsLeft).toBe(1);
  const last = await attempt(request, an, q2, '62');
  expect(last.attemptsLeft).toBe(0);
  expect(last.state).toBe('review');
  expect(last.message).toBe('Chưa đúng. Em xem lại cùng thầy cô nhé.');
  expect((await play(request, an, set.id)).items[1]!.state).toBe('review');
});

test('@TC-S2E8-045 đúng ở lượt cuối vẫn đúng', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  await attempt(request, an, q2, '60');
  await attempt(request, an, q2, '61');
  const r = await attempt(request, an, q2, '45');
  expect(r.outcome).toBe('correct');
  expect(r.state).toBe('correct');
  expect(r.message).toBe('Đúng rồi!');
  const item = (await play(request, an, set.id)).items[1]!;
  expect(item.state).toBe('correct');
  expect(item.maxAttempts - item.attemptsLeft + 1, 'lượt đã dùng 3/3').toBe(3);
});

test('@TC-S2E8-046 sai cú pháp ở lượt cuối không trừ và không hết lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  await attempt(request, an, q2, '60');
  await attempt(request, an, q2, '61');
  const bad = await attempt(request, an, q2, '√');
  expect(bad.outcome).toBe('syntax');
  expect(bad.attemptsLeft).toBe(1);
  expect(bad.reason).toBe('Thiếu số sau dấu căn (ký tự 1)');
  expect(bad.message).toBe('Đáp số chưa viết đúng: thiếu số sau dấu căn (ký tự 1). Lượt thử chưa bị trừ.');
  expect((await attempt(request, an, q2, '45')).outcome).toBe('correct');
});

test('@TC-S2E8-047 hết lượt rồi gửi tiếp thì CONFLICT và không chấm', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  for (const a of ['60', '61', '62']) await attempt(request, an, q2, a);
  const r = await attemptRaw(request, an, q2, '45'); // đáp số đúng
  expect(r.status()).toBe(409);
  const body = (await r.json()) as { code: string; message: string };
  expect(body.code).toBe('CONFLICT');
  expect(JSON.stringify(body)).not.toMatch(/Đúng rồi|Chưa đúng|outcome/);
  expect((await play(request, an, set.id)).items[1]!.state).toBe('review');
});

test('@TC-S2E8-048 đã Đúng rồi gửi tiếp thì CONFLICT', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  await attempt(request, an, q2, '45');
  const r = await attemptRaw(request, an, q2, '46');
  expect(r.status()).toBe(409);
  const body = (await r.json()) as { code: string; message: string };
  expect(body.code).toBe('CONFLICT');
  expect(body.message).toBe('Câu này đã đúng');
  const item = (await play(request, an, set.id)).items[1]!;
  expect(item.state).toBe('correct');
  expect(item.attemptsLeft).toBe(3);
});

test('@TC-S2E8-049 bấm đôi chỉ trừ một lượt', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  const key = randomUUID();
  const [a, b] = await Promise.all([attemptRaw(request, an, q2, '60', key), attemptRaw(request, an, q2, '60', key)]);
  expect(a.status()).toBe(200);
  expect(b.status()).toBe(200);
  expect(await a.json()).toEqual(await b.json());
  expect(((await a.json()) as { attemptsLeft: number }).attemptsLeft).toBe(2);
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(2);
});

test('@TC-S2E8-050 mạng chập chờn gửi lại cùng mã không trừ hai lần', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  const key = randomUUID();
  const first = await attemptRaw(request, an, q2, '60', key);
  expect(first.status()).toBe(200);
  await attempt(request, an, q2, '61'); // một lượt khác xen giữa
  const again = await attemptRaw(request, an, q2, '60', key); // thiết bị gửi lại lượt đầu
  expect(again.status()).toBe(200);
  expect(await again.json()).toEqual(await first.json());
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(1);
});

test('@TC-S2E8-051 đáp số mẫu không có trong dữ liệu trả về của bài luyện', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  const open = await request.get(`${API}/api/v1/student/practice-sets/${set.id}`, { headers: bearer(an) });
  expect(open.headers()['cache-control']).toContain('no-store');
  expectNoAnswer45(await open.json(), 'mở bài');
  for (const wrong of ['60', '61', '62']) {
    const r = await attemptRaw(request, an, q2, wrong);
    expect(r.headers()['cache-control']).toContain('no-store');
    expectNoAnswer45(await r.json(), `sau lần Sai ${wrong}`);
    expectNoAnswer45(await play(request, an, set.id), `mở lại sau ${wrong}`);
  }
  const list = await request.get(`${API}/api/v1/student/practice-sets`, { headers: bearer(an) });
  expectNoAnswer45(await list.json(), 'danh sách bài luyện');
  const res = await request.get(`${API}/api/v1/student/practice-sets/${set.id}/result`, { headers: bearer(an) });
  expect(res.headers()['cache-control']).toContain('no-store');
  expectNoAnswer45({ ...((await res.json()) as object), items: [] }, 'kết quả sau khi hết lượt');
  // hình của câu (màn hình phẳng S2-E2) và lời giải qua cổng giờ mở của MVP: không có đáp số trước giờ mở
  const sol = await request.get(`${API}/api/v1/student/assignments/${q2}/solution`, { headers: bearer(an) });
  expect(sol.status()).toBe(403);
});

test('@TC-S2E8-052 ba lần Sai liên tiếp không để lộ hướng', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  const shapes: string[] = [];
  for (const [i, a] of ['40', '50', '46'].entries()) {
    const r = await attempt(request, an, q2, a);
    expect(r.outcome).toBe('wrong');
    expect(JSON.stringify(r)).not.toMatch(/lớn hơn|nhỏ hơn|cao hơn|thấp hơn|gần đúng|lệch|quá xa/i);
    expect(r.attemptsLeft).toBe(2 - i);
    // ngoài số lượt còn và trạng thái, ba phản hồi giống hệt nhau (không chênh lệch thông tin giữa nhỏ, lớn, gần)
    shapes.push(JSON.stringify({ ...r, message: null, attemptsLeft: null, state: null }));
  }
  expect(new Set(shapes).size).toBe(1);
});

test('@TC-S2E8-053 gọi chấm không đăng nhập', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const q2 = set.items[1]!.assignmentId;
  const r = await attemptRaw(request, null, q2, '45');
  expect(r.status()).toBe(401);
  expect(((await r.json()) as { code: string }).code).toBe('UNAUTHENTICATED');
  expect((await play(request, student(sb, 'An').token, set.id)).items[1]!.attemptsLeft).toBe(3);
});

test('@TC-S2E8-055 đổi sai số hoặc kiểu từ thiết bị bị bỏ qua', async ({ request }) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An').token;
  const q2 = set.items[1]!.assignmentId;
  // BR-S3E5-34 (PO-61, chặng 3) thay hợp đồng cũ "bỏ qua": thân có trường thừa bị từ chối, không tốn lượt
  const bad = await attemptRaw(request, an, q2, '60', randomUUID(), { tol: 100, tolerance: 100, type: 'approx', answerKind: 'approx', maxAttempts: 99 });
  expect(bad.status()).toBe(400);
  expect(((await bad.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  expect((await play(request, an, set.id)).items[1]!.attemptsLeft).toBe(3);
  const r = await attemptRaw(request, an, q2, '60', randomUUID());
  expect(r.status()).toBe(200);
  const body = (await r.json()) as { outcome: string; attemptsLeft: number; maxAttempts: number };
  expect(body.outcome).toBe('wrong'); // sai số vẫn là ±0,5 của câu
  expect(body.attemptsLeft).toBe(2);
  expect(body.maxAttempts).toBe(3);
});
