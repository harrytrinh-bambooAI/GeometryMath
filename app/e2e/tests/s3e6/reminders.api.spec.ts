import { expect, test } from '../../support/fixtures.ts';
import { API, assignedSet, hoursRound, klass, lan, notesOf, ok, smallWorld, vnAt, who } from '../s3e4/s3e4.ts';

/**
 * S3-E6 · US-S3E6-03 nhắc việc trong ứng dụng qua API (T-261126-31, mở rộng). Tác vụ nhắc chạy bằng móc kiểm thử
 * /test-hooks/s3e6/reminders/run với đồng hồ giả lập (chỉ profile dev/test). Tên tệp riêng `reminders.*` để không
 * đụng phần S3-E6-01/02 của task khác.
 */

test('@TC-S3E6-38 nhắc sắp hết hạn chỉ có tên bộ, giờ hạn và loại; không đáp số, tiến độ, tên bạn hay so sánh với lớp', async ({ request }) => {
  const w = await smallWorld(request);
  const due = hoursRound(48);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id, { dueAt: due, releaseAt: null });
  // Một giờ trước hạn: đã qua mốc 24 giờ trước hạn kể cả khi mốc đó bị hoãn vì giờ yên lặng (tối đa 9 giờ).
  const at = new Date(new Date(due).getTime() - 3_600_000).toISOString();
  const run = await request.post(`${API}/test-hooks/s3e6/reminders/run`, { data: { at } });
  expect((await ok<{ dueSoonEvents: number }>(run)).dueSoonEvents).toBeGreaterThanOrEqual(1);

  const an = who(w, 'Bình An');
  const r = await request.get(`${API}/api/v1/notifications`, { headers: { Authorization: `Bearer ${an.token}` } });
  const items = (await ok<{ items: Array<Record<string, unknown>> }>(r)).items;
  const due15 = items.filter((n) => n.kind === 'due_soon' && n.setId === set.id);
  expect(due15).toHaveLength(1);
  expect(due15[0]!.body).toBe(`Bộ bài tuần 15 sắp hết hạn: ${vnAt(due)}`);
  expect(Object.keys(due15[0]!).sort()).toEqual(['body', 'createdAt', 'id', 'kind', 'read', 'setId']);
  const text = JSON.stringify(due15[0]);
  for (const leak of ['Chi Mai', 'Khánh Linh', 'Đáp số', 'đáp số', 'finalAnswer', 'answer', 'tiến độ', '%', 'Chưa làm', 'Đang làm', 'cả lớp', 'bạn', 'xếp hạng']) {
    expect(text, leak).not.toContain(leak);
  }
  // Chạy lại cùng giờ không thêm thông báo thứ hai.
  await ok(await request.post(`${API}/test-hooks/s3e6/reminders/run`, { data: { at } }));
  expect((await notesOf(request, an.token)).filter((n) => n.kind === 'due_soon' && n.setId === set.id)).toHaveLength(1);
});
