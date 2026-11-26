import { expect, test } from '../../support/fixtures.ts';
import { API, api, assignedSet, bearer, err, hoursRound, klass, lan, notesOf, ok, seedNotes, smallWorld, unreadOf, vnAt, who, type Note } from './s3e4.ts';

/** S3-E4 · US-S3E4-03 Notification trong ứng dụng qua API: nội dung, biên 99 / 100, quyền, phân trang 20 dòng. */

test('@TC-S3E4-32 đổi giờ: thông báo ghi giờ mới theo giờ Việt Nam, không lộ đáp số, bước giải hay tên bạn', async ({ request }) => {
  const w = await smallWorld(request);
  const set = await assignedSet(request, lan(w), klass(w, '11A1').id);
  const at = hoursRound(31);
  await ok(await api.changeTime(request, lan(w).token, set.id, { releaseAt: at }));
  const r = await api.notes(request, who(w, 'Bình An').token);
  const items = (await ok<{ items: Note[] }>(r)).items;
  expect(items[0]!.body).toBe(`Giờ mở lời giải Bộ bài tuần 15 đổi sang ${vnAt(at)}`);
  expect(items[0]!).toMatchObject({ kind: 'release_changed', setId: set.id, read: false });
  expect(r.headers()['cache-control']).toContain('no-store');
  const text = await r.text();
  for (const leak of ['Chi Mai', 'Khánh Linh', 'Đáp số', 'finalAnswer', 'steps', 'answer']) expect(text, leak).not.toContain(leak);
  for (const n of items) expect(Object.keys(n).sort()).toEqual(['body', 'createdAt', 'id', 'kind', 'read', 'setId']);
});

test('@TC-S3E4-34 biên chấm số: 99 rồi 100 thông báo chưa đọc, thân phản hồi giữ số đúng', async ({ request }) => {
  const w = await smallWorld(request);
  const an = who(w, 'Bình An');
  await seedNotes(request, an.id, 99);
  expect(await unreadOf(request, an.token)).toBe(99);
  await seedNotes(request, an.id, 1);
  expect(await unreadOf(request, an.token), 'số đúng là 100 (giao diện mới rút thành "99+")').toBe(100);
});

test('@TC-S3E4-35 học sinh không đọc được thông báo của bạn', async ({ request }) => {
  const w = await smallWorld(request);
  await assignedSet(request, lan(w), klass(w, '11A1').id);
  const mai = (await notesOf(request, who(w, 'Chi Mai').token))[0]!;
  const an = who(w, 'Bình An').token;
  const detail = await request.get(`${API}/api/v1/notifications/${mai.id}`, { headers: bearer(an) });
  expect(detail.status()).toBe(404);
  expect((await err(detail)).code).toBe('NOT_FOUND');
  const read = await request.post(`${API}/api/v1/notifications/${mai.id}/read`, { headers: bearer(an) });
  expect(read.status()).toBe(404);
  const mine = await notesOf(request, an);
  expect(mine).toHaveLength(1);
  expect(mine.map((n) => n.id)).not.toContain(mai.id);
  expect((await notesOf(request, who(w, 'Chi Mai').token))[0]!.read, 'thông báo của Chi Mai vẫn Chưa đọc').toBe(false);
});

test('@TC-S3E4-38 danh sách thông báo phân trang 20, 20, 5; mới nhất trước; con trỏ bị sửa trả INVALID_CURSOR; không có tổng số', async ({ request }) => {
  const w = await smallWorld(request);
  const an = who(w, 'Bình An');
  await seedNotes(request, an.id, 45);
  const pages: Array<{ items: Note[]; nextCursor?: string | null }> = [];
  let cursor: string | undefined;
  for (let i = 0; i < 3; i++) {
    const p = await ok<{ items: Note[]; nextCursor?: string | null }>(await api.notes(request, an.token, cursor));
    expect(Object.keys(p).sort().filter((k) => k !== 'nextCursor')).toEqual(['items']);
    pages.push(p);
    cursor = p.nextCursor ?? undefined;
  }
  expect(pages.map((p) => p.items.length)).toEqual([20, 20, 5]);
  expect(pages[2]!.nextCursor ?? null).toBeNull();
  const all = pages.flatMap((p) => p.items);
  expect(new Set(all.map((n) => n.id)).size).toBe(45);
  const times = all.map((n) => new Date(n.createdAt).getTime());
  expect(times).toEqual([...times].sort((a, b) => b - a));
  const good = pages[0]!.nextCursor!;
  const tampered = `${good.slice(0, -4)}${good.slice(-4) === 'AAAA' ? 'BBBB' : 'AAAA'}`;
  for (const bad of [tampered, 'khong-phai-con-tro']) {
    const r = await api.notes(request, an.token, bad);
    expect(r.status()).toBe(400);
    expect((await err(r)).code).toBe('INVALID_CURSOR');
  }
});

test('@TC-S3E4-41 học sinh không tạo được thông báo cho bạn', async ({ request }) => {
  const w = await smallWorld(request);
  const mai = who(w, 'Chi Mai');
  const r = await request.post(`${API}/api/v1/notifications`, {
    headers: bearer(who(w, 'Bình An').token),
    data: { userId: mai.id, kind: 'new_set', body: 'Có bài mới: Bộ giả' },
  });
  expect(r.status()).toBe(403);
  expect((await err(r)).code).toBe('FORBIDDEN');
  expect(await notesOf(request, mai.token)).toEqual([]);
  expect(await unreadOf(request, mai.token)).toBe(0);
});
