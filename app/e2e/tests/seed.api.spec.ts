import { expect, test } from '../support/fixtures.ts';

/**
 * Test khung (@TC-KHUNG-02): seed vàng đã nạp và tách theo trường. Chạy trên bảng mẫu sample_item
 * (db/seed/e2e/900_sample_scaffold.sql); bỏ khi gói sample bị xóa. Cơ chế con trỏ gắn trường ở đây là cơ chế
 * mà TC-GEO-0800 sẽ kiểm trên danh sách thật (Theo dõi lớp, E7).
 */
type Items = { items: Array<{ id: string; title: string }>; nextCursor?: string };

test('seed vàng: mỗi trường chỉ thấy dữ liệu của mình @TC-KHUNG-02 @smoke', async ({ request, tokenFor }) => {
  const teacherA = await tokenFor('F-GEO-003');
  const teacherC = await tokenFor('F-GEO-005');

  const a = await request.get('/api/v1/sample/items?limit=20', { headers: { Authorization: `Bearer ${teacherA}` } });
  expect(a.status()).toBe(200);
  const aBody = (await a.json()) as Items;
  expect(aBody.items.map((i) => i.title)).toEqual(['Mẫu A · 3', 'Mẫu A · 2', 'Mẫu A · 1']);

  const c = await request.get('/api/v1/sample/items?limit=20', { headers: { Authorization: `Bearer ${teacherC}` } });
  expect(((await c.json()) as Items).items.map((i) => i.title)).toEqual(['Mẫu C · 1']);
});

test('con trỏ của trường A dùng với thẻ trường C bị từ chối @TC-KHUNG-02', async ({ request, tokenFor }) => {
  const teacherA = await tokenFor('F-GEO-003');
  const teacherC = await tokenFor('F-GEO-005');
  const page1 = (await (
    await request.get('/api/v1/sample/items?limit=2', { headers: { Authorization: `Bearer ${teacherA}` } })
  ).json()) as Items;
  expect(page1.nextCursor).toBeTruthy();

  const res = await request.get(`/api/v1/sample/items?limit=2&cursor=${encodeURIComponent(page1.nextCursor!)}`, {
    headers: { Authorization: `Bearer ${teacherC}` },
  });
  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.code).toBe('INVALID_CURSOR');
  expect(JSON.stringify(body)).not.toContain('Mẫu A');
});
