import { expect, test } from '../../support/fixtures.ts';
import { AN_AI_LOCK, ORG_A, bearer, devToken, lane, withLock } from '../e1/e1.ts';
import { ALL_CODES, API, F037, LANE_LOAD, PV, allPages, laneToken } from './e5.ts';
import type { PageBody } from './e5.ts';

/** US-GEO-501-FC4, 502-FC1, 503-FC3…FC6, 505 — gọi thẳng máy chủ. */

const F060 =
  'Cho hình lăng trụ đứng ABC.A′B′C′ có đáy là tam giác vuông tại A, AB = 3, AC = 4, AA′ = 5. Tính khoảng cách từ A đến mặt phẳng (A′BC).';
const NO_SUCH = '00000000-0000-7000-8000-0000e5999999';

type ErrorBody = { code: string; message: string; requestId: string; details?: Array<{ field: string; message: string }> };
const shape = (e: ErrorBody) => ({ code: e.code, message: e.message, details: e.details ?? [] });

test('Giá trị lọc không hợp lệ: VALIDATION_FAILED nêu trường @TC-GEO-0509', async ({ request }) => {
  const t = await laneToken(request);
  for (const bad of ['thiet-dien', 'Thiết diện']) {
    const r = await request.get(`${API}/api/v1/catalog/problems?topic=${encodeURIComponent(bad)}`, { headers: bearer(t) });
    expect(r.status()).toBe(400);
    const body = await r.text();
    const e = JSON.parse(body) as ErrorBody;
    expect(e.code).toBe('VALIDATION_FAILED');
    expect(e.details?.map((d) => d.field)).toEqual(['topic']);
    expect(body).not.toMatch(/HKG-|"items"/);
  }
});

test('Đề vừa nạp, engine chưa chạy: nhãn Cần xem lại @TC-GEO-0513', async ({ request }) => {
  const add = await request.post(`${API}/test-hooks/e5/problems`, {
    data: { statement: `Đề mới nạp chưa kiểm chứng (${Date.now()}): cho hình chóp S.ABC, tính góc giữa SA và (ABC).` },
  });
  expect(add.status(), await add.text()).toBe(200);
  const { code } = (await add.json()) as { code: string };
  const t = await laneToken(request, LANE_LOAD);
  const items = (await allPages(request, t)).flatMap((p) => p.items);
  const it = items.find((x) => x.code === code)!;
  expect(it, code).toBeTruthy();
  expect(it.verifyStatus).toBe('needs_review');
  expect(it.label).toBe('Cần xem lại');
  expect(it.aiAuthored).toBe(true);
});

test('Học sinh gọi trang chi tiết và lời giải giáo viên: FORBIDDEN, không lộ @TC-GEO-0522', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  for (const url of [`/api/v1/teacher/catalog/problems/${PV('HKG-01')}`, `/api/v1/teacher/problems/${PV('HKG-01')}/solution`]) {
    const r = await request.get(`${API}${url}`, { headers: bearer(an) });
    expect(r.status(), url).toBe(403);
    const body = await r.text();
    expect((JSON.parse(body) as ErrorBody).code).toBe('FORBIDDEN');
    expect(body).not.toContain('45°');
    for (const step of F037) expect(body).not.toContain(step);
  }
});

test('Mã đề không tồn tại: NOT_FOUND @TC-GEO-0523 @smoke', async ({ request }) => {
  const t = await laneToken(request);
  const r = await request.get(`${API}/api/v1/teacher/catalog/problems/${NO_SUCH}`, { headers: bearer(t) });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as ErrorBody).code).toBe('NOT_FOUND');
});

/** An (11A1) có bài tự học từ đề F-GEO-060; trả version đề tự nhập. Giữ AN_AI_LOCK như mọi ca tạo bài tự học của An. */
async function anSelfStudyVersion(request: Parameters<typeof laneToken>[0], testInfo: Parameters<typeof lane>[0]) {
  const an = await devToken(request, 'F-GEO-020');
  const r = await withLock(AN_AI_LOCK, () =>
    request.post(`${API}/api/v1/student/self-study`, {
      headers: { ...bearer(an), 'X-Geo-Test-Lane': lane(testInfo) },
      data: { statement: F060 },
    }),
  );
  expect(r.status(), await r.text()).toBe(200);
  const s = (await r.json()) as { assignmentId: string; problemVersionId: string };
  // Mở lời giải của bài tự học thuộc luồng E4 (chưa có trên nhánh này); điều cần kiểm là phía giáo viên, không phụ thuộc bước đó.
  return s.problemVersionId;
}

async function expectSameAsMissing(request: Parameters<typeof laneToken>[0], token: string, pv: string, init: { data?: unknown } = {}) {
  for (const [path, suffix] of [['/api/v1/teacher/catalog/problems/', ''], ['/api/v1/teacher/problems/', '/solution']] as const) {
    const got = await request.fetch(`${API}${path}${pv}${suffix}`, { method: 'GET', headers: bearer(token), ...init });
    const ref = await request.fetch(`${API}${path}${NO_SUCH}${suffix}`, { method: 'GET', headers: bearer(token), ...init });
    expect(got.status()).toBe(404);
    expect(ref.status()).toBe(404);
    const body = await got.text();
    expect(shape(JSON.parse(body) as ErrorBody)).toEqual(shape((await ref.json()) as ErrorBody));
    for (const leak of ['60/√769', '2,1637', '"steps"', 'finalAnswer']) expect(body).not.toContain(leak);
  }
}

test('Giáo viên cùng trường gọi lời giải đề tự nhập của học sinh: NOT_FOUND @TC-GEO-0542', async ({ request }, testInfo) => {
  const pv = await anSelfStudyVersion(request, testInfo);
  const teacher = await devToken(request, 'F-GEO-003');
  await expectSameAsMissing(request, teacher, pv);
});

test('Giáo viên trường khác gọi lời giải đề tự nhập kèm định danh trường: NOT_FOUND như mã không có @TC-GEO-0541', async ({ request }, testInfo) => {
  const pv = await anSelfStudyVersion(request, testInfo);
  const c = await devToken(request, 'F-GEO-005');
  await expectSameAsMissing(request, c, pv, { data: { orgId: ORG_A, schoolId: ORG_A } });
});

test('Dữ liệu đổi giữa chừng: không lặp, 28 đề còn lại đủ một lần @TC-GEO-0533', async ({ request }) => {
  const t = await laneToken(request, LANE_LOAD);
  const r1 = await request.get(`${API}/api/v1/catalog/problems`, { headers: bearer(t) });
  const first = (await r1.json()) as PageBody;
  expect(first.items).toHaveLength(20);
  const firstCodes = first.items.map((x) => x.code);
  // hai đề mới được nạp vào kho ngay sau trang đầu: một đề sắp SAU con trỏ (HKM-…) và một đề sắp TRƯỚC con trỏ
  // (HKA-… < HKG-01, BR-GEO-217): phân trang theo khóa không lặp, không đẩy lệch đề cũ sang trang sau
  const add = await request.post(`${API}/test-hooks/e5/problems`, {
    data: { statement: `Đề nạp giữa chừng (${Date.now()}): cho hình chóp S.ABCD, tính khoảng cách từ A đến (SBC).` },
  });
  expect(add.status()).toBe(200);
  const before = await request.post(`${API}/test-hooks/e5/problems`, {
    data: { statement: `Đề nạp trước con trỏ (${Date.now()}): cho hình chóp S.ABCD, tính góc giữa SC và (ABCD).`, prefix: 'HKA' },
  });
  expect(before.status()).toBe(200);
  const beforeCode = ((await before.json()) as { code: string }).code;
  expect(beforeCode < firstCodes[firstCodes.length - 1]!, `${beforeCode} sắp trước con trỏ`).toBe(true);
  const rest: string[] = [];
  let cursor = first.nextCursor;
  while (cursor) {
    const r = await request.get(`${API}/api/v1/catalog/problems?cursor=${encodeURIComponent(cursor)}`, { headers: bearer(t) });
    expect(r.status(), await r.text()).toBe(200);
    const p = (await r.json()) as PageBody;
    rest.push(...p.items.map((x) => x.code));
    cursor = p.nextCursor;
  }
  for (const c of firstCodes) expect(rest, `${c} của trang đầu không lặp lại`).not.toContain(c);
  expect(rest, 'đề chèn trước con trỏ không làm lặp hay lệch trang sau').not.toContain(beforeCode);
  const remaining = ALL_CODES.filter((c) => !firstCodes.includes(c));
  // lượt chạy lại (retry) có thể còn đề HKA-… của lượt trước ở đầu trang: vẫn đủ 48 đề kho thử chia hai phía con trỏ
  expect(remaining).toHaveLength(48 - firstCodes.filter((c) => ALL_CODES.includes(c)).length);
  for (const c of remaining) expect(rest.filter((x) => x === c), c).toHaveLength(1);
  expect(new Set(rest).size).toBe(rest.length);
});

test('Phân trang cùng bộ lọc THPT: 20 và 4 đề @TC-GEO-0534 @smoke', async ({ request }) => {
  const pages = await allPages(request, await laneToken(request), 'level=thpt');
  expect(pages.map((p) => p.items.length)).toEqual([20, 4]);
  const items = pages.flatMap((p) => p.items);
  for (const it of items) expect(it.level).toBe('thpt');
  expect(new Set(items.map((x) => x.code)).size).toBe(24);
});

test('Phản hồi không có tổng số @TC-GEO-0535', async ({ request }) => {
  const r = await request.get(`${API}/api/v1/catalog/problems`, { headers: bearer(await laneToken(request)) });
  const body = (await r.json()) as Record<string, unknown>;
  expect(Object.keys(body).sort()).toEqual(['hasMore', 'items', 'nextCursor']);
  expect(body.hasMore).toBe(true);
  expect(typeof body.nextCursor).toBe('string');
  expect(JSON.stringify(body)).not.toMatch(/"total|"count"|"totalPages|"pages"/i);
});

test('Hết danh sách: cờ còn trang sau là không @TC-GEO-0537', async ({ request }) => {
  const pages = await allPages(request, await laneToken(request));
  expect(pages.map((p) => p.items.length)).toEqual([20, 20, 8]);
  const last = pages.at(-1)!;
  expect(last.hasMore).toBe(false);
  expect(last.nextCursor ?? null, 'không có con trỏ trang kế').toBeNull();
});

test('Con trỏ hỏng: INVALID_CURSOR, không có đề nào @TC-GEO-0539', async ({ request }) => {
  const r = await request.get(`${API}/api/v1/catalog/problems?cursor=khong-phai-con-tro`, { headers: bearer(await laneToken(request)) });
  expect(r.status()).toBe(400);
  const body = await r.text();
  expect((JSON.parse(body) as ErrorBody).code).toBe('INVALID_CURSOR');
  expect(body).not.toMatch(/HKG-|"items"/);
});

test('Xin trang quá lớn: kẹp về trần 50, đúng 48 đề, không con trỏ @TC-GEO-0540', async ({ request }) => {
  const r = await request.get(`${API}/api/v1/catalog/problems?limit=10000`, { headers: bearer(await laneToken(request)) });
  expect(r.status()).toBe(200);
  const p = (await r.json()) as PageBody;
  expect(p.items).toHaveLength(48);
  expect(p.hasMore).toBe(false);
  expect(p.nextCursor ?? null, 'không có con trỏ trang kế').toBeNull();
  expect(p.items.map((x) => x.code)).toEqual(ALL_CODES);
});
