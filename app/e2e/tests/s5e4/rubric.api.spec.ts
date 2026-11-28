import { expect, test } from '../../support/fixtures.ts';
import { bearer, hook } from '../s3e1/s3e1.ts';
import { GA, MSG, RUBRICS, rb01, s5e4World, setup, type Err, type Graded, type Rubric } from './s5e4.ts';

/** US-S5E4-01 — rubric có phiên bản, mẫu sẵn theo dạng bài, bất biến khi đã dùng (máy chủ thật, cờ grading bật ở dev). */

const points = (r: Rubric) => r.criteria.map((c) => c.maxPoints);

test('Tạo rubric từ mẫu Tính toán @TC-S5E4-01', async ({ request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const rb = await rb01(request, lan);
  expect(rb.code).toBe('RB-01');
  expect(rb.version).toBe(1);
  expect(rb.name).toBe('Tính toán – khoảng cách');
  expect(rb.criteria.map((c) => c.name)).toEqual(['Hướng', 'Các bước', 'Kết quả', 'Hình', 'Trình bày']);
  expect(points(rb)).toEqual([2, 4, 2, 1, 1]);
  expect(rb.total).toBe(10);
  expect(rb.scale).toBe(10);
  // tiêu chí "Bước" có cờ phụ thuộc hướng; các tiêu chí khác không
  expect(rb.criteria.find((c) => c.name === 'Các bước')!.directionDependent).toBe(true);
  expect(rb.criteria.filter((c) => c.directionDependent)).toHaveLength(1);
  // hướng dẫn chấm có 5 luật chặn KPI §2
  expect(rb.guide).toHaveLength(5);
  expect(rb.guide[0]).toContain('Sai hướng');
});

test('Sửa rubric đã giao tạo phiên bản mới, bài đã giao giữ bản cũ @TC-S5E4-02', async ({ request }) => {
  const { lan, rb, bc } = await setup(request);
  expect(bc.rubric.version).toBe(1);
  const crit = rb.criteria.map((c) => ({
    name: c.name,
    description: c.description,
    directionDependent: c.directionDependent,
    maxPoints: c.name === 'Trình bày' ? 0.5 : c.name === 'Hình' ? 1.5 : c.maxPoints,
  }));
  const r = await request.put(`${RUBRICS}/${rb.id}`, { headers: bearer(lan.token), data: { criteria: crit } });
  expect(r.status(), await r.text()).toBe(200);
  const v2 = (await r.json()) as Rubric;
  expect(v2.version).toBe(2);
  expect(v2.message).toBe(MSG.s31);
  expect(points(v2)).toEqual([2, 4, 2, 1.5, 0.5]);
  // BC-01 vẫn trỏ RB-01 v1
  const g = (await (await request.get(`${GA}/${bc.id}`, { headers: bearer(lan.token) })).json()) as Graded;
  expect(g.rubric.version).toBe(1);
  expect(g.rubric.versionId).toBe(rb.versionId);
  // nội dung v1 không đổi
  const v1 = (await (await request.get(`${RUBRICS}/${rb.id}?version=1`, { headers: bearer(lan.token) })).json()) as Rubric;
  expect(points(v1)).toEqual([2, 4, 2, 1, 1]);
  expect(v1.used).toBe(true);
});

test('Tổng điểm khác thang bị từ chối @TC-S5E4-04', async ({ request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const criteria = [
    { name: 'Hướng', maxPoints: 2 },
    { name: 'Các bước', maxPoints: 3.75 },
    { name: 'Kết quả', maxPoints: 2 },
    { name: 'Hình', maxPoints: 1 },
    { name: 'Trình bày', maxPoints: 1 },
  ];
  const r = await request.post(RUBRICS, { headers: bearer(lan.token), data: { name: 'Lệch tổng', criteria } });
  expect(r.status()).toBe(400);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.message).toBe(MSG.s30);
  expect(e.details?.[0]?.field).toBe('criteria');
  const list = (await (await request.get(RUBRICS, { headers: bearer(lan.token) })).json()) as { items: unknown[] };
  expect(list.items).toHaveLength(0);
});

test('Điểm không phải bội 0,25 hoặc 11 tiêu chí bị từ chối @TC-S5E4-05', async ({ request }) => {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  // 1) tiêu chí điểm 0,3
  const odd = [
    { name: 'Hướng', maxPoints: 2 },
    { name: 'Các bước', maxPoints: 4 },
    { name: 'Kết quả', maxPoints: 2 },
    { name: 'Hình', maxPoints: 1 },
    { name: 'Trình bày', maxPoints: 0.3 },
  ];
  const r1 = await request.post(RUBRICS, { headers: bearer(lan.token), data: { name: 'Bội 0,3', criteria: odd } });
  expect(r1.status()).toBe(400);
  const e1 = (await r1.json()) as Err;
  expect(e1.code).toBe('VALIDATION_FAILED');
  expect(e1.details?.[0]?.field).toBe('criteria[4].maxPoints');
  // 2) 11 tiêu chí
  const eleven = Array.from({ length: 11 }, (_, i) => ({ name: `Tiêu chí ${i + 1}`, maxPoints: i === 0 ? 0.25 : 0.975 }));
  const r2 = await request.post(RUBRICS, { headers: bearer(lan.token), data: { name: '11 tiêu chí', criteria: eleven } });
  expect(r2.status()).toBe(400);
  const e2 = (await r2.json()) as Err;
  expect(e2.code).toBe('VALIDATION_FAILED');
  expect(e2.details?.[0]?.field).toBe('criteria');
});

test('Cập nhật thẳng phiên bản đã dùng bị CSDL từ chối @TC-S5E4-06', async ({ request }) => {
  const { w, rb } = await setup(request);
  const out = (await hook(request, 's5e4/rubric-raw-update', { orgId: w.orgId, versionId: rb.versionId, points: 3 })) as {
    updated: number;
    error: string | null;
    criteria: Array<{ name: string; maxPoints: number }>;
  };
  expect(out.updated).toBe(0);
  expect(out.error).toContain('bất biến');
  expect(out.criteria.map((c) => c.maxPoints)).toEqual([2, 4, 2, 1, 1]);
});
