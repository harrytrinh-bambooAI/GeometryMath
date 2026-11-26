import { expect, test } from '../../support/fixtures.ts';
import { F060, call, hints12, selfStudy } from '../e4/e4.ts';
import { API, asg, bearer, box, events, freshMonitor, golden, monitor, row, send, st } from './e7.ts';

/**
 * E7 — ghi sự kiện (US-GEO-703) qua đúng API của thiết bị. "Máy chủ khởi động lại" giả lập bằng móc
 * /test-hooks/e7/restart (bỏ đệm Theo dõi lớp): lần đọc sau chỉ dựa vào CSDL (sự kiện bền ghi đồng bộ, D-GEO-162).
 */

test('Mở bài tạo sự kiện mở bài @TC-GEO-0721', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  expect((await monitor(request, b.teacher.token, id)).openedCount).toBe(4);
  expect((await send.open(request, st(b, 'Hà').token, id)).status()).toBe(200);
  await expect
    .poll(async () => (await monitor(request, b.teacher.token, id)).openedCount, { timeout: 10_000, intervals: [500, 1000] })
    .toBe(5);
  const m = await monitor(request, b.teacher.token, id);
  expect(row(m, 'Hà').opened).toBe(true);
  expect(row(m, 'Hà').status).toBe('Tự làm, không cần gợi ý');
});

test('Mỗi công cụ là một thao tác trên hình @TC-GEO-0722', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  await send.open(request, ha.token, id);
  // Chọn, Điểm, Đường, Mặt phẳng, Mở rộng, Đo dài, Đo góc = mã 1…7, mỗi công cụ đúng một lần
  expect((await send.tools(request, ha.token, id, [1, 2, 3, 4, 5, 6, 7])).status()).toBe(200);
  expect(row(await freshMonitor(request, b.teacher.token, id), 'Hà').toolCount).toBe(7);
  expect((await events(request, id, ha.id)).tool).toBe(7);
});

test('Sự kiện gợi ý ghi bền @TC-GEO-0723', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  await send.open(request, ha.token, id);
  const h = await call.hint(request, ha.token, id, 1);
  expect(h.status()).toBe(200);
  const body1 = (await h.json()) as { body?: string };
  // máy chủ ứng dụng khởi động lại ngay sau đó; Hà tải lại bài
  const m = await freshMonitor(request, b.teacher.token, id);
  expect(row(m, 'Hà').k).toBe(1); // tiến độ 1/3
  expect(m.hintUsers).toBe(4); // An, Bình, Dũng + Hà
  expect((await events(request, id, ha.id)).hint).toBe(1); // sự kiện gợi ý đã ghi bền trước khi trả nội dung
  const again = await call.hint(request, ha.token, id, 1);
  expect(again.status()).toBe(200);
  expect(((await again.json()) as { body?: string }).body).toBe(body1.body); // Gợi ý 1 vẫn hiện
});

test('Bài tự học cũng ghi sự kiện @TC-GEO-0726', async ({ request }) => {
  const b = await box(request, golden());
  const an = st(b, 'An');
  const selfId = await selfStudy(request, an.token, F060);
  expect((await send.open(request, an.token, selfId)).status()).toBe(200);
  expect((await send.tools(request, an.token, selfId, [6])).status()).toBe(200); // "Đo dài" một lần
  await hints12(request, an.token, selfId);
  expect((await call.solution(request, an.token, selfId)).status()).toBe(200);
  const ev = await events(request, selfId, an.id);
  expect(ev).toMatchObject({ open: 1, tool: 1, hint: 2, unlock: 1 });
  // không xuất hiện ở Theo dõi lớp của bài giao nào
  const m = await freshMonitor(request, b.teacher.token, asg(b, 'BG-1'));
  expect(row(m, 'An').toolCount).toBe(4);
  const list = await request.get(`${API}/api/v1/teacher/assigned`, { headers: bearer(b.teacher.token) });
  expect(JSON.stringify(await list.json())).not.toContain(selfId);
  expect((await request.get(`${API}/api/v1/teacher/assignments/${selfId}/monitor`, { headers: bearer(b.teacher.token) })).status()).toBe(404);
});

test('Công cụ không tính @TC-GEO-0727', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  await send.open(request, ha.token, id);
  // Oxyz, góc nhìn, "Đặt lại", xoay, thu phóng không có mã công cụ (bộ xem không phát, TC-GEO-0309); gửi thẳng mã ngoài
  // 1…7 thì máy chủ từ chối, không ghi
  const r = await send.tools(request, ha.token, id, [8, 9, 10]);
  expect(r.status()).toBe(400);
  expect((await r.json()).code).toBe('VALIDATION_FAILED');
  expect(row(await freshMonitor(request, b.teacher.token, id), 'Hà').toolCount).toBe(0);
  expect((await events(request, id, ha.id)).tool ?? 0).toBe(0);
});

test('Gửi lại cùng một lô @TC-GEO-0728', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  const batch = crypto.randomUUID();
  expect((await send.tools(request, ha.token, id, [2, 2, 3], batch)).status()).toBe(200);
  expect((await send.tools(request, ha.token, id, [2, 2, 3], batch)).status()).toBe(200); // gửi lại đúng lô đó
  expect(row(await freshMonitor(request, b.teacher.token, id), 'Hà').toolCount).toBe(3);
});

test('Không ghi sự kiện thay người khác @TC-GEO-0730', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const an = st(b, 'An');
  const binh = st(b, 'Bình');
  // BR-S3E5-34 (PO-61, chặng 3) thay hợp đồng cũ "bỏ qua trường thừa": thân ghi định danh người khác bị từ chối, không ghi gì
  const bad = await send.tools(request, an.token, id, [1, 2], crypto.randomUUID(), { userId: binh.id, studentId: binh.id });
  expect(bad.status()).toBe(400);
  expect((await bad.json()).code).toBe('VALIDATION_FAILED');
  const before = await freshMonitor(request, b.teacher.token, id);
  expect(row(before, 'An').toolCount).toBe(4);
  expect(row(before, 'Bình').toolCount).toBe(2);
  // lô hợp lệ của An chỉ tính cho An
  expect((await send.tools(request, an.token, id, [1, 2], crypto.randomUUID())).status()).toBe(200);
  const m = await freshMonitor(request, b.teacher.token, id);
  expect(row(m, 'An').toolCount).toBe(6); // 4 + 2
  expect(row(m, 'Bình').toolCount).toBe(2);
  expect((await events(request, id, binh.id)).tool ?? 0).toBe(0);
});

test('Sự kiện cho bài không thuộc lớp mình @TC-GEO-0731', async ({ request }) => {
  const b = await box(
    request,
    golden({ classes: [{ name: '9B', level: 'thcs', students: ['Khánh'], assignments: [{ key: 'BG-4', code: 'HKG-04' }] }] }),
  );
  const an = st(b, 'An');
  const r = await send.open(request, an.token, asg(b, 'BG-4'));
  expect(r.status()).toBe(404);
  expect((await r.json()).code).toBe('NOT_FOUND');
  const m = await freshMonitor(request, b.teacher.token, asg(b, 'BG-4'));
  expect(m.students.map((s) => s.displayName)).toEqual(['Khánh']);
});

test('Sự kiện mở bài ghi bền @TC-GEO-0743', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  expect((await monitor(request, b.teacher.token, id)).openedCount).toBe(4);
  const rq = crypto.randomUUID();
  expect((await send.open(request, ha.token, id, rq)).status()).toBe(200);
  // máy chủ ứng dụng khởi động lại ngay sau khi trả thành công; thiết bị có thể gửi lại cùng mã yêu cầu
  expect((await send.open(request, ha.token, id, rq)).status()).toBe(200);
  const m = await freshMonitor(request, b.teacher.token, id);
  expect(m.openedCount).toBe(5);
  expect(row(m, 'Hà').opened).toBe(true);
  expect((await events(request, id, ha.id)).open).toBe(1);
});
