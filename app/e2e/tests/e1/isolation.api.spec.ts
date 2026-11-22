import { execFileSync } from 'node:child_process';
import type { APIRequestContext } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { AN_AI_LOCK, BG, CLASS, MSG, ORG_C, PV, USER, bearer, devToken, withLock } from './e1.ts';

/** US-GEO-104 — Cách ly dữ liệu theo trường và phân quyền ở máy chủ (gọi thẳng máy chủ). */

type Item = { id: string; problemCode?: string; code?: string };
const A_NAMES = ['An', 'Bình', 'Chi', 'Dũng', 'Hà'];

async function json<T>(r: Awaited<ReturnType<APIRequestContext['get']>>): Promise<T> {
  return (await r.json()) as T;
}

test('Học sinh chỉ nhận bài của lớp mình @TC-GEO-0123', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const r = await request.get('/api/v1/student/assignments', { headers: bearer(an) });
  expect(r.status()).toBe(200);
  const text = await r.text();
  const items = (JSON.parse(text) as { items: Item[] }).items;
  expect(items.map((i) => i.id).sort()).toEqual([BG('BG-1'), BG('BG-2'), BG('BG-3')].sort());
  for (const other of ['BG-4', 'BG-5', 'BG-C']) expect(text).not.toContain(BG(other));
  for (const leak of ['HKG-04', 'HKG-05', 'Hình nón', 'Hình trụ', '9B', '9C']) expect(text).not.toContain(leak);
});

test('Nội dung hệ thống đọc được từ mọi trường @TC-GEO-0125', async ({ request }) => {
  const a = await devToken(request, 'F-GEO-003');
  const c = await devToken(request, 'F-GEO-005');
  const ra = await request.get('/api/v1/catalog/problems?limit=50', { headers: bearer(a) });
  const rc = await request.get('/api/v1/catalog/problems?limit=50', { headers: bearer(c) });
  expect(rc.status()).toBe(200);
  const ia = (await json<{ items: Item[] }>(ra)).items;
  const textC = await rc.text();
  const ic = (JSON.parse(textC) as { items: Item[] }).items;
  expect(ic.map((p) => p.code)).toEqual(['HKG-01', 'HKG-02', 'HKG-03', 'HKG-04', 'HKG-05', 'HKG-06']);
  expect(ic).toEqual(ia); // cùng nội dung
  for (const leak of ['11A1', '9B', 'THPT Mẫu A', ...A_NAMES.map((n) => `"${n}"`)]) expect(textC).not.toContain(leak);
  const classesC = await json<{ items: Array<{ name: string }> }>(await request.get('/api/v1/teacher/classes', { headers: bearer(c) }));
  expect(classesC.items.map((x) => x.name)).toEqual(['9C']);
});

test('Bản ghi mới mang trường của người tạo @TC-GEO-0126', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const question = `Vì sao A là hình chiếu của S? (${Date.now()})`;
  const r = await withLock(AN_AI_LOCK, () =>
    request.post(`/api/v1/student/assignments/${BG('BG-1')}/questions`, {
      headers: bearer(an),
      data: { question, orgId: ORG_C, schoolId: ORG_C }, // cố ý kèm định danh trường THCS Mẫu C
    }),
  );
  expect(r.status()).toBe(200);
  const a = await devToken(request, 'F-GEO-003');
  await expect
    .poll(
      async () => {
        const m = await json<{ questions: Array<{ question: string; askerName: string }> }>(
          await request.get(`/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(a) }),
        );
        return m.questions.some((q) => q.question === question && q.askerName === 'An');
      },
      { timeout: 15_000 },
    )
    .toBe(true);
  const c = await devToken(request, 'F-GEO-005');
  const rc = await request.get(`/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(c) });
  expect(rc.status()).toBe(404);
  expect(await rc.text()).not.toContain(question);
});

test('Học sinh gọi bài của trường khác @TC-GEO-0129', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const other = await request.get(`/api/v1/student/assignments/${BG('BG-C')}`, { headers: bearer(an) });
  const none = await request.get('/api/v1/student/assignments/00000000-0000-7000-8000-000000099999', { headers: bearer(an) });
  expect(other.status()).toBe(404);
  expect(none.status()).toBe(404);
  const bo = await json<{ code: string; message: string }>(other);
  const bn = await json<{ code: string; message: string }>(none);
  expect(bo.code).toBe('NOT_FOUND');
  expect({ code: bo.code, message: bo.message }).toEqual({ code: bn.code, message: bn.message });
  // giao diện hiện MSG-GEO-04 cho mã NOT_FOUND (đối chiếu ở TC-GEO-0128)
  expect(MSG['04']).toBe('Không tìm thấy nội dung này.');
});

test('Học sinh gọi dữ liệu của bạn cùng lớp @TC-GEO-0130', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const binh = USER('F-GEO-021');
  for (const path of [`/api/v1/students/${binh}/questions`, `/api/v1/students/${binh}/progress`]) {
    const r = await request.get(path, { headers: bearer(an) });
    expect(r.status(), path).toBe(404);
    const t = await r.text();
    expect(JSON.parse(t)).toMatchObject({ code: 'NOT_FOUND' });
    expect(t).not.toContain('Góc cần tìm');
    expect(t).not.toContain('Bình');
  }
});

test('Giáo viên trường khác đổi giờ mở @TC-GEO-0131', async ({ request, t0 }) => {
  const c = await devToken(request, 'F-GEO-005');
  const r = await request.post(`/api/v1/teacher/assignments/${BG('BG-1')}/release`, {
    headers: bearer(c),
    data: { openNow: true, expectedRev: 1 },
  });
  expect(r.status()).toBe(404);
  expect(await json<{ code: string }>(r)).toMatchObject({ code: 'NOT_FOUND' });
  const an = await devToken(request, 'F-GEO-020');
  const bg1 = await json<{ releaseState: string; releaseAt: string }>(
    await request.get(`/api/v1/student/assignments/${BG('BG-1')}`, { headers: bearer(an) }),
  );
  expect(bg1.releaseState).toBe('upcoming');
  expect(Math.abs(new Date(bg1.releaseAt).getTime() - (t0.getTime() + 60 * 60_000))).toBeLessThan(1000);
  // không có lần đổi giờ nào: số lần đổi (release_rev) của BG-1 vẫn 1 như seed
  const a = await devToken(request, 'F-GEO-003');
  const list = await json<{ items: Array<{ id: string; releaseRev: number }> }>(
    await request.get(`/api/v1/teacher/classes/${CLASS('F-GEO-010')}/assignments?limit=50`, { headers: bearer(a) }),
  );
  expect(list.items.find((x) => x.id === BG('BG-1'))?.releaseRev).toBe(1);
});

test('Giáo viên trường khác xem Theo dõi lớp @TC-GEO-0132', async ({ request }) => {
  const c = await devToken(request, 'F-GEO-005');
  const r = await request.get(`/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(c) });
  expect(r.status()).toBe(404);
  const t = await r.text();
  for (const n of A_NAMES) expect(t).not.toContain(n);
});

test('Giao bài chéo trường @TC-GEO-0133', async ({ request }) => {
  const a = await devToken(request, 'F-GEO-003');
  const r = await request.post('/api/v1/teacher/assignments', {
    headers: bearer(a),
    data: { classId: CLASS('F-GEO-012'), problemVersionId: PV('HKG-01') },
  });
  expect(r.status()).toBe(404);
  expect(await json<{ code: string }>(r)).toMatchObject({ code: 'NOT_FOUND' });
  const c = await devToken(request, 'F-GEO-005');
  const list = await json<{ items: Item[] }>(
    await request.get(`/api/v1/teacher/classes/${CLASS('F-GEO-012')}/assignments?limit=50`, { headers: bearer(c) }),
  );
  expect(list.items.map((x) => x.id)).toEqual([BG('BG-C')]);
  const an9c = await devToken(request, 'F-GEO-026-1');
  const mine = await json<{ items: Item[] }>(await request.get('/api/v1/student/assignments', { headers: bearer(an9c) }));
  expect(mine.items.map((x) => x.problemCode)).toEqual(['HKG-04']);
});

test('Thẻ xác thực bị sửa @TC-GEO-0134', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const [h, p, s] = an.split('.');
  const claims = JSON.parse(Buffer.from(p!, 'base64url').toString('utf8')) as Record<string, unknown>;
  claims.org = ORG_C;
  const forged = `${h}.${Buffer.from(JSON.stringify(claims)).toString('base64url')}.${s}`;
  const r = await request.get('/api/v1/student/assignments', { headers: bearer(forged) });
  expect(r.status()).toBe(401);
  const t = await r.text();
  expect(JSON.parse(t)).toMatchObject({ code: 'UNAUTHENTICATED' });
  expect(t).not.toContain('items');
});

test('Bộ kiểm cách ly duyệt mọi điểm cuối bằng thẻ của trường khác @TC-GEO-0170', async ({ request }) => {
  const teacherC = await devToken(request, 'F-GEO-005');
  const studentC = await devToken(request, 'F-GEO-026-1');
  const an = USER('F-GEO-020');
  const anQa = 1; // lượt hỏi đầu tiên của seed (An, F-GEO-055)
  // Danh mục điểm cuối của E1 (chi tiết, ghi: phải NOT_FOUND; danh sách: không có bản ghi của THPT Mẫu A)
  const detail: Array<[string, string, string, unknown?]> = [
    ['GET', `/api/v1/student/assignments/${BG('BG-1')}`, studentC],
    ['GET', `/api/v1/student/assignments/${BG('BG-1')}/solution`, studentC],
    ['POST', `/api/v1/student/assignments/${BG('BG-1')}/questions`, studentC, { question: 'Hỏi thử' }],
    ['GET', `/api/v1/questions/${anQa}/answer`, studentC],
    ['GET', `/api/v1/students/${an}/questions`, studentC],
    ['GET', `/api/v1/students/${an}/progress`, studentC],
    ['GET', `/api/v1/students/${an}/questions`, teacherC],
    ['GET', `/api/v1/students/${an}/progress`, teacherC],
    ['GET', `/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, teacherC],
    ['POST', `/api/v1/teacher/assignments/${BG('BG-1')}/release`, teacherC, { openNow: true, expectedRev: 1 }],
    ['POST', '/api/v1/teacher/assignments', teacherC, { classId: CLASS('F-GEO-010'), problemVersionId: PV('HKG-02') }],
    ['GET', `/api/v1/teacher/classes/${CLASS('F-GEO-010')}/assignments`, teacherC],
  ];
  const lists: Array<[string, string]> = [
    ['/api/v1/student/assignments', studentC],
    ['/api/v1/teacher/classes', teacherC],
    ['/api/v1/catalog/problems?limit=50', teacherC], // nội dung hệ thống dùng chung (D-GEO-076)
  ];
  const leaks: string[] = [];
  const bg13 = [BG('BG-1'), BG('BG-2'), BG('BG-3')];
  for (const [method, path, token, data] of detail) {
    const r = await request.fetch(path, { method, headers: bearer(token), data });
    const t = await r.text();
    if (r.status() !== 404 || (JSON.parse(t) as { code: string }).code !== 'NOT_FOUND') leaks.push(`${method} ${path} -> ${r.status()}`);
    for (const s of ['11A1', ...A_NAMES.slice(1), 'Đã xem gợi ý', ...bg13]) if (t.includes(s)) leaks.push(`${method} ${path} chứa ${s}`);
  }
  for (const [path, token] of lists) {
    const r = await request.get(path, { headers: bearer(token) });
    const t = await r.text();
    expect(r.status(), path).toBe(200);
    for (const s of ['11A1', '9B', ...A_NAMES.slice(1), ...bg13, '"THPT Mẫu A"']) if (t.includes(s)) leaks.push(`${path} chứa ${s}`);
  }
  expect(leaks, 'số điểm cuối rò rỉ = 0').toEqual([]);
});

test('Giáo viên không đọc được câu trả lời đã lưu của học sinh @TC-GEO-0178', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const asked = await json<{ id: number; reply: string }>(
    await withLock(AN_AI_LOCK, () =>
      request.post(`/api/v1/student/assignments/${BG('BG-1')}/questions`, {
        headers: bearer(an),
        data: { question: 'Vì sao A là hình chiếu của S?' },
      }),
    ),
  );
  const own = await request.get(`/api/v1/questions/${asked.id}/answer`, { headers: bearer(an) });
  expect(own.status()).toBe(200);
  expect((await json<{ reply: string }>(own)).reply).toBe(asked.reply);
  const a = await devToken(request, 'F-GEO-003');
  const r = await request.get(`/api/v1/questions/${asked.id}/answer`, { headers: bearer(a) });
  expect(r.status()).toBe(403);
  const t = await r.text();
  expect(JSON.parse(t)).toMatchObject({ code: 'FORBIDDEN', message: MSG['03'] });
  expect(t).not.toContain(asked.reply);
  await expect
    .poll(async () => {
      const m = await json<{ questions: Array<{ id: number }> }>(
        await request.get(`/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(a) }),
      );
      return m.questions.some((q) => q.id === asked.id);
    }, { timeout: 15_000 })
    .toBe(true);
});

test('Con trỏ phân trang của trường này dùng với thẻ của trường khác @TC-GEO-0800', async ({ request }) => {
  const a = await devToken(request, 'F-GEO-003');
  const page1 = await json<{ items: Item[]; nextCursor?: string }>(
    await request.get('/api/v1/catalog/problems?limit=2', { headers: bearer(a) }),
  );
  expect(page1.nextCursor).toBeTruthy();
  const c = await devToken(request, 'F-GEO-005');
  const r = await request.get(
    `/api/v1/teacher/classes/${CLASS('F-GEO-012')}/assignments?cursor=${encodeURIComponent(page1.nextCursor!)}`,
    { headers: bearer(c) },
  );
  expect(r.status()).toBe(400);
  const t = await r.text();
  expect(JSON.parse(t)).toMatchObject({ code: 'INVALID_CURSOR' });
  expect(t).not.toContain('items');
});

test('Khóa bộ nhớ đệm của trường này không đọc được bằng thẻ trường khác @TC-GEO-0804', async ({ request }) => {
  const a = await devToken(request, 'F-GEO-003');
  const url = `/api/v1/teacher/assignments/${BG('BG-1')}/monitor`;
  expect((await request.get(url, { headers: bearer(a) })).status()).toBe(200); // tạo dữ liệu đệm
  const prefix = process.env.GEO_E2E_REDIS_PREFIX ?? 'geoe2e:';
  const keys = execFileSync('redis-cli', ['-p', process.env.GEO_REDIS_PORT ?? '6389', '--scan', '--pattern', `${prefix}*`], {
    encoding: 'utf8',
  })
    .split('\n')
    .filter(Boolean);
  const monitorKeys = keys.filter((k) => k.includes(':mon:'));
  expect(monitorKeys.length).toBeGreaterThan(0);
  for (const k of monitorKeys) expect(k, 'khóa đệm mang tiền tố trường').toMatch(new RegExp(`^${prefix}t:[0-9a-f-]{36}:mon:`));
  expect(monitorKeys.filter((k) => k.includes(BG('BG-1'))).every((k) => k.includes(`t:00000000-0000-7000-8000-000000000001:`))).toBe(true);
  const c = await devToken(request, 'F-GEO-005');
  const rc = await request.get(url, { headers: bearer(c) });
  expect(rc.status()).toBe(404);
  const t = await rc.text();
  for (const n of A_NAMES) expect(t).not.toContain(n);
});
