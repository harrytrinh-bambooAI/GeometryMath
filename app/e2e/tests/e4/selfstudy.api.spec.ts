import { expect, test } from '../../support/fixtures.ts';
import { PV } from '../e1/e1.ts';
import {
  API,
  F060,
  F063,
  HINT1_HKG01,
  HINT2_HKG01,
  HINT2_HKG06,
  HKG,
  LEAK_HKG01,
  MSG,
  aiCalls,
  bearer,
  call,
  expectNoLeak,
  hints12,
  sandbox,
  selfStudy,
} from './e4.ts';

/** E4 · E2E-API: tự học (US-GEO-401, 402, 403), gợi ý (404), lời giải tự học (405), kế thừa khóa (406). */

const selfCount = async (rq: import('@playwright/test').APIRequestContext, userId: string) =>
  ((await (await rq.get(`${API}/test-hooks/e2/self-study-count?userId=${userId}`)).json()) as { count: number }).count;
const F060_LEAK = ['60/√769', '2,1637', '769/3600', 'tứ diện vuông tại A.'];

test('Bài tự học mới bắt đầu ở mức 0 @TC-GEO-0402', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F060);
  const h = (await (await call.help(request, an.token, id)).json()) as Record<string, any>;
  expect(h.k).toBe(0);
  expect(h.kind).toBe('self');
  expect(h.allowHint).toBe(true);
  expect(h.hints).toEqual([]); // nút "Xem gợi ý 1": mức kế tiếp = 1
  expect(h.solution.state).toBe('hints');
  expect(h.solution.message).toBe(MSG['30']);
});

test('Đề 8001 ký tự bị từ chối, 8000 được @TC-GEO-0405', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const pad = (n: number) => (F060 + ' Ghi chú: ' + 'x'.repeat(n)).slice(0, n);
  const before = await selfCount(request, an.id);
  const r = await call.selfStudy(request, an.token, pad(8001));
  expect(r.status()).toBe(400);
  const b = (await r.json()) as { code: string; details: Array<{ field: string; message: string }> };
  expect(b.code).toBe('VALIDATION_FAILED');
  expect(b.details[0]).toMatchObject({ field: 'statement', message: MSG['21'] });
  expect(await selfCount(request, an.id)).toBe(before);
  const ok = await call.selfStudy(request, an.token, pad(8000));
  expect(ok.status()).toBe(200);
  expect(await selfCount(request, an.id)).toBe(before + 1);
});

test('Đề không phải hình học @TC-GEO-0407', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const before = await selfCount(request, an.id);
  const r = await call.selfStudy(request, an.token, 'Thủ đô của nước Pháp là gì?');
  expect(r.status()).toBe(400);
  expect(((await r.json()) as { message: string }).message).toBe(MSG['18']);
  expect(await selfCount(request, an.id)).toBe(before);
});

test('Bạn khác không thấy bài tự học của mình @TC-GEO-0408', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An', 'Bình'] }]);
  const { An: an, ['Bình']: binh } = sb.classes[0]!.students as Record<string, { id: string; token: string }>;
  const id = await selfStudy(request, an!.token, F060);
  for (const path of [`/api/v1/student/self-study/${id}`, `/api/v1/student/assignments/${id}/help`, `/api/v1/student/assignments/${id}/scene`]) {
    const r = await request.get(`${API}${path}`, { headers: bearer(binh!.token) });
    expect(r.status(), path).toBe(404);
    const t = await r.text();
    expect(JSON.parse(t).code).toBe('NOT_FOUND');
    expect(t).not.toContain('lăng trụ');
    expect(t).not.toContain('A′');
  }
});

// ---------------------------------------------------------------- ảnh đề (US-GEO-402)
const png = (marker: string, size: number) => {
  const head = Buffer.from(`\x89PNG\r\n\x1a\n${marker}`, 'latin1');
  return Buffer.concat([head, Buffer.alloc(Math.max(0, size - head.length), 7)]).toString('base64');
};

test('Ảnh mờ: chưa đọc được chữ @TC-GEO-0412', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const r = await request.post(`${API}/api/v1/student/self-study/ocr`, { headers: bearer(an.token), data: { image: png('nhoe', 400_000) } });
  expect(r.status()).toBe(400);
  const b = (await r.json()) as { code: string; message: string };
  expect(b.code).toBe('VALIDATION_FAILED');
  expect(b.message).toBe(MSG['22']);
});

test('Ảnh vượt 1 MB @TC-GEO-0413', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const before = (await aiCalls(request, an.id)).length;
  const r = await request.post(`${API}/api/v1/student/self-study/ocr`, {
    headers: bearer(an.token),
    data: { image: png('GEO-FIXTURE:F-GEO-066', 1_500_000) },
  });
  expect(r.status()).toBe(413);
  expect(((await r.json()) as { code: string }).code).toBe('PAYLOAD_TOO_LARGE');
  expect((await aiCalls(request, an.id)).length).toBe(before);
});

test('Trợ lý lỗi khi đọc ảnh @TC-GEO-0414', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  await request.post(`${API}/test-hooks/ai`, { data: { userId: an.id, mode: 'error' } });
  const r = await request.post(`${API}/api/v1/student/self-study/ocr`, {
    headers: bearer(an.token),
    data: { image: png('GEO-FIXTURE:F-GEO-066', 400_000) },
  });
  expect(r.status()).toBe(503);
  expect(((await r.json()) as { code: string }).code).toBe('DEPENDENCY_UNAVAILABLE');
  const calls = await aiCalls(request, an.id);
  expect(calls[0]).toMatchObject({ purpose: 'scene', status: 'error' });
});

test('Chưa vào lớp: Tự học trả UNAUTHENTICATED @TC-GEO-0418', async ({ request }) => {
  for (const r of [
    await request.post(`${API}/api/v1/student/self-study`, { data: { statement: F060 } }),
    await request.get(`${API}/api/v1/geometry/templates`),
    await request.post(`${API}/api/v1/student/self-study/ocr`, { data: { image: 'AAAA' } }),
  ]) {
    expect(r.status()).toBe(401);
    expect(((await r.json()) as { code: string }).code).toBe('UNAUTHENTICATED');
  }
});

// ---------------------------------------------------------------- gợi ý (US-GEO-404)
test('Xem lại gợi ý không làm tăng tiến độ @TC-GEO-0422', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['Bình'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const binh = sb.classes[0]!.students['Bình']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  const rid = crypto.randomUUID();
  expect((await call.hint(request, binh.token, bg1, 1, rid)).status()).toBe(200);
  // rời bài, mở lại: Gợi ý 1 hiện sẵn, tiến độ 1/3; gửi lại cùng yêu cầu (mạng chập chờn) không đếm thêm
  for (let i = 0; i < 2; i++) {
    const h = (await (await call.help(request, binh.token, bg1)).json()) as Record<string, any>;
    expect(h.k).toBe(1);
    expect(h.hints).toEqual([{ level: 1, title: 'Gợi ý 1 · Hướng nghĩ', body: HINT1_HKG01 }]);
  }
  const again = (await (await call.hint(request, binh.token, bg1, 1, rid)).json()) as Record<string, any>;
  expect(again).toMatchObject({ level: 1, body: HINT1_HKG01, k: 1 });
  const p = (await (await request.get(`${API}/api/v1/students/${binh.id}/progress`, { headers: bearer(binh.token) })).json()) as {
    items: Array<{ assignmentId: string; k: number }>;
  };
  expect(p.items.find((x) => x.assignmentId === bg1)?.k).toBe(1);
});

test('Gợi ý chưa xin thì chưa được gửi xuống @TC-GEO-0426', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const ha = sb.classes[0]!.students['Hà']!;
  const bg1 = sb.classes[0]!.assignments['BG-1']!;
  let all = '';
  for (const path of ['', '/scene', '/help', '/lock']) {
    const r = await request.get(`${API}/api/v1/student/assignments/${bg1}${path}`, { headers: bearer(ha.token) });
    expect(r.status(), path).toBe(200);
    all += await r.text();
  }
  expect(all).not.toContain(HINT1_HKG01);
  expect(all).not.toContain(HINT2_HKG01);
  expect(all).not.toContain('hình chiếu của nó');
});

test('Phản hồi gợi ý không cho lưu đệm @TC-GEO-0488', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const ha = sb.classes[0]!.students['Hà']!;
  const r = await call.hint(request, ha.token, sb.classes[0]!.assignments['BG-1']!, 1);
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
});

// ---------------------------------------------------------------- lời giải tự học (US-GEO-405)
test('Mới xem Gợi ý 1 mà gọi thẳng lời giải @TC-GEO-0431', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F060);
  expect((await call.hint(request, an.token, id, 1)).status()).toBe(200);
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { code: string }).code).toBe('FORBIDDEN');
  expectNoLeak(await r.text(), F060_LEAK);
});

test('Trang bài tự học không mang sẵn lời giải @TC-GEO-0432', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const created = await call.selfStudy(request, an.token, F060);
  const id = ((await created.json()) as { assignmentId: string }).assignmentId;
  let all = await created.text();
  for (const path of [`/api/v1/student/self-study/${id}`, `/api/v1/student/assignments/${id}/help`, `/api/v1/student/assignments/${id}/scene`]) {
    all += await (await request.get(`${API}${path}`, { headers: bearer(an.token) })).text();
  }
  all += await hints12(request, an.token, id);
  expectNoLeak(all, F060_LEAK);
});

test('Hỏi đáp không mở trước lời giải của bài tự học @TC-GEO-0433', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F060);
  const h1 = (await (await call.hint(request, an.token, id, 1)).json()) as { body: string };
  const r = await call.ask(request, an.token, id, 'Đáp số bài này là bao nhiêu?');
  expect(r.status()).toBe(200);
  const a = (await r.json()) as { reply: string; replyKind: string };
  expect(a.reply).toBe(MSG['35'] + h1.body);
  expectNoLeak(await r.text(), F060_LEAK);
  expect(((await (await call.help(request, an.token, id)).json()) as { k: number }).k).toBe(1);
});

// ---------------------------------------------------------------- kế thừa khóa (US-GEO-406)
test('Đề trùng bài đã mở thì không kế thừa @TC-GEO-0436', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-3', code: 'HKG-03', release: 'open' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, HKG['03']);
  await hints12(request, an.token, id);
  const h = (await (await call.help(request, an.token, id)).json()) as Record<string, any>;
  expect(h.solution.state).toBe('open');
  expect(JSON.stringify(h)).not.toContain(MSG['31']);
});

test('Bài của lớp khác không gây kế thừa @TC-GEO-0437', async ({ request }) => {
  const sb = await sandbox(request, [
    { name: '11A1', students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] },
    { name: '9B', level: 'thcs', students: ['Khánh'] },
  ]);
  const khanh = sb.classes[1]!.students['Khánh']!;
  const id = await selfStudy(request, khanh.token, HKG['01']);
  await hints12(request, khanh.token, id);
  const r = await call.solution(request, khanh.token, id);
  expect(r.status()).toBe(200);
  expect(((await r.json()) as { finalAnswer: string }).finalAnswer).toBe('45°');
});

test('Đề gần trùng (đổi tên điểm) cũng kế thừa @TC-GEO-0438', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F063);
  await hints12(request, an.token, id);
  const h = (await (await call.help(request, an.token, id)).json()) as Record<string, any>;
  expect(h.solution).toMatchObject({ state: 'inherited', message: MSG['31'] });
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  expectNoLeak(await r.text());
});

test('Gọi thẳng lời giải của bài kế thừa khóa @TC-GEO-0439', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, HKG['01']);
  await hints12(request, an.token, id);
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { code: string }).code).toBe('FORBIDDEN');
  expectNoLeak(await r.text());
});

test('Giáo viên dời giờ mở: bài tự học kế thừa theo giờ mới @TC-GEO-0440', async ({ request }) => {
  test.setTimeout(240_000);
  const sb = await sandbox(request, [{ name: '9B', level: 'thcs', students: ['Khánh'], assignments: [{ key: 'BG-4', code: 'HKG-04', release: '60' }] }]);
  const t0 = Date.now();
  const khanh = sb.classes[0]!.students['Khánh']!;
  const bg4 = sb.classes[0]!.assignments['BG-4']!;
  const id = await selfStudy(request, khanh.token, HKG['04']);
  await hints12(request, khanh.token, id);
  const at = async (sec: number) => {
    const wait = t0 + sec * 1000 - Date.now();
    if (wait > 0) await new Promise((r) => setTimeout(r, wait));
  };
  await at(30);
  const r1 = await call.release(request, sb.teacher.token, bg4, { releaseAt: new Date(t0 + 120_000).toISOString(), expectedRev: 0 });
  expect(r1.status(), await r1.text()).toBe(200);
  await at(61);
  const locked = await call.solution(request, khanh.token, id);
  expect(locked.status()).toBe(403);
  expect(((await locked.json()) as { message: string }).message).toBe(MSG['31']);
  await at(122);
  const open = await call.solution(request, khanh.token, id);
  expect(open.status()).toBe(200);
  expect(((await open.json()) as { finalAnswer: string }).finalAnswer).toBe('Sxq = 15π cm²; V = 12π cm³');
});

test('Bài kế thừa khóa dùng gợi ý của đề kho, không gọi AI sinh gợi ý @TC-GEO-0487', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F063);
  const h1 = (await (await call.hint(request, an.token, id, 1)).json()) as { body: string };
  const h2 = (await (await call.hint(request, an.token, id, 2)).json()) as { body: string };
  expect(h1.body).toBe(HINT1_HKG01);
  expect(h2.body).toBe(HINT2_HKG01);
  expect((await aiCalls(request, an.id)).filter((c) => c.purpose === 'hint')).toEqual([]);
});

test('Dán trước, giao sau: gọi thẳng lời giải bị từ chối @TC-GEO-0490', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, HKG['06']);
  expect((await call.hint(request, an.token, id, 1)).status()).toBe(200);
  const g = await call.assign(request, sb.teacher.token, sb.classes[0]!.id, PV('HKG-06'), null);
  expect(g.status(), await g.text()).toBe(201);
  const h2 = (await (await call.hint(request, an.token, id, 2)).json()) as { body: string };
  expect(h2.body).toBe(HINT2_HKG06);
  const r = await call.solution(request, an.token, id);
  expect(r.status()).toBe(403);
  expectNoLeak(await r.text(), ['a√3/2', 'a√3 / 2', '(√3/2)·π·a³']);
});

test('Dữ liệu gợi ý của bài kế thừa khóa không lộ @TC-GEO-0491', async ({ request }) => {
  const sb = await sandbox(request, [{ students: ['An'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
  const an = sb.classes[0]!.students['An']!;
  const id = await selfStudy(request, an.token, F063);
  expectNoLeak(await hints12(request, an.token, id), LEAK_HKG01);
});
