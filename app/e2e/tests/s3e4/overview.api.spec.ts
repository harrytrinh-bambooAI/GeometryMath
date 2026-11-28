import { expect, test } from '../../support/fixtures.ts';
import { join, world } from '../s3e1/s3e1.ts';
import { api, assignedSet, interact, khoi, klass, lan, ok, smallWorld, who, type Overview } from './s3e4.ts';

/** S3-E4 · US-S3E4-01 Tổng quan giáo viên qua API: biên 99 / 100, quyền theo quan hệ, không tên học sinh. */

test('@TC-S3E4-08 biên số em chờ duyệt: 99 rồi 100, thân phản hồi giữ số đúng', async ({ request }) => {
  test.setTimeout(120_000);
  const pending = Array.from({ length: 99 }, (_, i) => `Em thử ${String(i + 1).padStart(3, '0')}`);
  const w = await world(request, {
    teachers: ['Cô Lan'],
    students: ['Em thử 100'],
    classes: [{ key: '11A1', teacher: 'Cô Lan', name: '11A1', active: ['Bình An'], pending }],
  });
  const first = await ok<Overview>(await api.overview(request, lan(w).token));
  expect(first.pending.total).toBe(99);
  expect(first.pending.byClass).toEqual([{ classId: klass(w, '11A1').id, className: '11A1', count: 99 }]);
  const j = await join(request, who(w, 'Em thử 100').token, klass(w, '11A1').code);
  expect(j.status(), await j.text()).toBeLessThan(300);
  const second = await ok<Overview>(await api.overview(request, lan(w).token));
  expect(second.pending.total, 'số đúng là 100 (giao diện mới rút thành "99+")').toBe(100);
  expect(second.pending.byClass[0]!.count).toBe(100);
});

test('@TC-S3E4-10 giáo viên khác chỉ thấy lớp của mình', async ({ request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15' });
  await assignedSet(request, khoi(w), klass(w, '10B').id, { title: 'Bộ ôn của 10B', items: [3] });
  const r = await api.overview(request, khoi(w).token);
  const o = await ok<Overview>(r);
  expect(o.assigned.map((s) => [s.title, s.className])).toEqual([['Bộ ôn của 10B', '10B']]);
  expect(o.pending.total).toBe(0);
  const text = await r.text();
  for (const hidden of ['11A1', '11A2', 'Bộ bài tuần 15', klass(w, '11A1').id]) expect(text).not.toContain(hidden);
});

test('@TC-S3E4-11 tổng quan không chứa tên hay mã người dùng của học sinh; no-store', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  await interact(request, who(w, 'Bình An'), set.items[0]!.assignmentId!);
  const r = await api.overview(request, lan(w).token);
  const o = await ok<Overview>(r);
  expect(r.headers()['cache-control']).toContain('no-store');
  expect(o.assigned[0]!.notOpenedCount).toBe(2);
  expect(o.pending.total).toBe(1);
  const text = await r.text();
  expect(text).not.toMatch(/HS-[A-Z0-9]{4}-[A-Z0-9]{4}/);
  for (const name of ['Bình An', 'Chi Mai', 'Khánh Linh', 'Thu Hà', 'Văn Tú']) {
    expect(text, name).not.toContain(name);
    expect(text).not.toContain(who(w, name).userCode);
    expect(text).not.toContain(who(w, name).id);
  }
});

test('@TC-S3E4-12 học sinh không gọi được Tổng quan', async ({ request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id);
  const r = await api.overview(request, who(w, 'Bình An').token);
  expect(r.status()).toBe(403);
  const body = (await r.json()) as Record<string, unknown>;
  expect(body.code).toBe('FORBIDDEN');
  expect(body).not.toHaveProperty('assigned');
  expect(body).not.toHaveProperty('pending');
});
