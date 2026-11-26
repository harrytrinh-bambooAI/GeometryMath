import type { APIRequestContext, TestInfo } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { AN_AI_LOCK, BG, bearer, devToken, lane, withLock } from '../e1/e1.ts';
import { F061, HKG01, MSG18, selfStudyCount, setScenario, walk } from './e2.ts';

/** US-GEO-203-FC3, FC5; US-GEO-204-FC1, FC2; US-GEO-205-SC3 — chỉ HTTP. */

type SelfStudy = { assignmentId: string; statement: string; scene: { coordSource: string; verification: { status: string; label: string } } };

async function build(request: APIRequestContext, testInfo: TestInfo, statement: string) {
  const t = await devToken(request, 'F-GEO-020');
  return request.post('/api/v1/student/self-study', {
    headers: { ...bearer(t), 'X-Geo-Test-Lane': lane(testInfo) },
    data: { statement },
  });
}

test.afterEach(async ({ request }, testInfo) => {
  await setScenario(request, testInfo, '');
});

test('AI tự khai nhãn bị bỏ qua @TC-GEO-0221', async ({ request }, testInfo) => {
  await setScenario(request, testInfo, 'F-GEO-112'); // đáp số "60°" + tự ghi "Đã kiểm chứng"
  const r = await withLock(AN_AI_LOCK, () => build(request, testInfo, HKG01));
  expect(r.status()).toBe(200);
  const s = (await r.json()) as SelfStudy;
  expect(s.scene.coordSource).toBe('engine');
  expect(s.scene.verification.label).toBe('Cần xem lại');
  expect(s.scene.verification.status).toBe('needs_review');
  expect(await r.text()).not.toContain('Đã kiểm chứng');
  // mọi màn đọc lại bài: không có trạng thái AI tự khai
  const t = await devToken(request, 'F-GEO-020');
  for (const path of [`/api/v1/student/self-study/${s.assignmentId}`, `/api/v1/student/assignments/${s.assignmentId}/scene`]) {
    const g = await request.get(path, { headers: bearer(t) });
    expect(g.status(), path).toBe(200);
    const text = await g.text();
    expect(text, path).not.toContain('Đã kiểm chứng');
    expect(text, path).toContain('Cần xem lại');
  }
});

test('Nhãn không kéo theo giá trị tính xuống thiết bị học sinh @TC-GEO-0223', async ({ request }) => {
  const t = await devToken(request, 'F-GEO-020');
  // dữ liệu trả về của trang Làm bài BG-1 (Sắp mở): bài + hình
  const bodies: unknown[] = [];
  for (const path of [`/api/v1/student/assignments/${BG('BG-1')}`, `/api/v1/student/assignments/${BG('BG-1')}/scene`]) {
    const r = await request.get(path, { headers: bearer(t) });
    expect(r.status(), path).toBe(200);
    bodies.push(await r.json());
  }
  const a = bodies[0] as { releaseState: string; verifyStatus: string };
  expect(a.releaseState).toBe('upcoming');
  expect(a.verifyStatus).toBe('verified');
  expect((bodies[1] as { verification: { label: string } }).verification.label).toBe('Đã kiểm chứng');
  const leaks: string[] = [];
  for (const b of bodies) {
    walk(b, (k, v) => {
      if (typeof v === 'number' && Math.abs(v - 45) < 1e-6) leaks.push(`${k}=${v}`);
      if (typeof v === 'string' && (v === '45' || v.includes('45°'))) leaks.push(`${k}=${v}`);
      if (/^(computed|answer|finalAnswer|answerForms|checks|report|param_sets)$/i.test(k)) leaks.push(`trường ${k}`);
    });
  }
  expect(leaks, 'không có giá trị tính, không có đáp số').toEqual([]);
});

test('Đề lạ không bao giờ "Đã kiểm chứng" @TC-GEO-0227', async ({ request }, testInfo) => {
  for (const scenario of ['', 'F-GEO-112']) {
    // đáp số AI "a√6/3" (F-GEO-110), rồi thêm trạng thái tự khai "Đã kiểm chứng"
    await setScenario(request, testInfo, scenario);
    const r = await withLock(AN_AI_LOCK, () => build(request, testInfo, F061));
    expect(r.status()).toBe(200);
    const s = (await r.json()) as SelfStudy;
    expect(s.scene.coordSource).toBe('ai');
    expect(s.scene.verification.label).toBe('Cần xem lại');
    expect(await r.text()).not.toContain('Đã kiểm chứng');
  }
});

test('Tọa độ AI hỏng @TC-GEO-0228', async ({ request }, testInfo) => {
  await setScenario(request, testInfo, 'F-GEO-111'); // sáu đỉnh trùng (0; 0; 0)
  // mọi ca tạo bài tự học của An giữ AN_AI_LOCK: đếm trước/sau trong cùng khóa
  const [r, before, after] = await withLock(AN_AI_LOCK, async () => {
    const b = await selfStudyCount(request);
    const res = await build(request, testInfo, F061);
    return [res, b, await selfStudyCount(request)] as const;
  });
  expect(r.status()).toBe(400);
  const body = (await r.json()) as { code: string; message: string; details: Array<{ field: string; message: string }> };
  expect(body.code).toBe('VALIDATION_FAILED');
  expect(body.message).toBe(MSG18);
  expect(body.details[0]).toEqual({ field: 'statement', message: MSG18 });
  expect(JSON.stringify(body)).not.toContain('scene');
  expect(after).toBe(before); // không vẽ, không lưu
});

test('Trường nào cũng đọc được danh mục @TC-GEO-0232 @smoke', async ({ request }) => {
  const names = async (ref: string) => {
    const r = await request.get('/api/v1/geometry/templates', { headers: bearer(await devToken(request, ref)) });
    expect(r.status()).toBe(200);
    return ((await r.json()) as { items: Array<{ name: string; card: boolean }> }).items.filter((i) => i.card).map((i) => i.name);
  };
  const c = await names('F-GEO-026-1'); // An lớp 9C, THCS Mẫu C
  const a = await names('F-GEO-020'); // An lớp 11A1
  expect(c).toEqual(['Hình chóp', 'Lăng trụ', 'Hình hộp', 'Hình nón', 'Hình trụ', 'Hình cầu']);
  expect(c).toEqual(a);
});
