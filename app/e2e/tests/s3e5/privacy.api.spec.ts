import { expect, test } from '../../support/fixtures.ts';
import { API, MSG, bearer, hook, join, teacherApi, world } from '../s3e1/s3e1.ts';

/** US-S3E5-02 — cờ "đã đọc" thông báo riêng tư theo phiên bản (gọi thẳng máy chủ). */

test('Chưa bấm "Đã đọc" thì không dùng được API khác @TC-S3E5-23', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  await hook(request, 's3e5/privacy/clear', { userId: an.id });
  for (const r of [
    await request.get(`${API}/api/v1/student/practice-sets`, { headers: bearer(an.token) }),
    await request.get(`${API}/api/v1/student/assignments`, { headers: bearer(an.token) }),
    await join(request, an.token, w.classes['11A2']!.code),
  ]) {
    expect(r.status()).toBe(403);
    const e = (await r.json()) as { code: string; message: string; details: Array<{ field: string; message: string }> };
    expect(e.code).toBe('FORBIDDEN');
    expect(e.message).toBe(MSG.s84);
    expect(e.details[0]).toEqual({ field: 'privacy', message: 'PRIVACY_ACK_REQUIRED' });
  }
  const mine = await request.get(`${API}/api/v1/account/privacy`, { headers: bearer(an.token) });
  expect(((await mine.json()) as { required: boolean }).required).toBe(true);
  expect((await request.post(`${API}/api/v1/account/privacy-ack`, { headers: bearer(an.token), data: {} })).status()).toBe(200);
  expect((await request.get(`${API}/api/v1/student/practice-sets`, { headers: bearer(an.token) })).status()).toBe(200);
});

test('Thiết bị gửi sai phiên bản @TC-S3E5-24', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  await hook(request, 's3e5/privacy/current', { userId: an.id, version: '1.1' });
  for (const v of ['9.9', '1.0']) {
    const r = await request.post(`${API}/api/v1/account/privacy-ack`, { headers: bearer(an.token), data: { version: v } });
    expect(r.status()).toBe(400);
    expect(((await r.json()) as { code: string }).code).toBe('VALIDATION_FAILED');
  }
  const flag = async () => ((await (await request.get(`${API}/test-hooks/s3e5/privacy?userId=${an.id}`)).json()) as { version: string }).version;
  expect(await flag()).toBe('1.0');
  const ok = await request.post(`${API}/api/v1/account/privacy-ack`, { headers: bearer(an.token), data: { version: '1.1' } });
  expect(ok.status()).toBe(200);
  expect(await flag()).toBe('1.1');
  await hook(request, 's3e5/privacy/current', { userId: an.id, version: '' });
});

test('Người khác không thấy cờ của mình @TC-S3E5-25', async ({ request }) => {
  const w = await world(request);
  const an = w.people['Bình An']!;
  const lan = w.people['Cô Lan']!.token;
  for (const r of [
    await teacherApi.student(request, lan, w.classes['11A1']!.id, an.id),
    await teacherApi.students(request, lan, w.classes['11A1']!.id),
  ]) {
    expect(r.status()).toBe(200);
    expect(await r.text()).not.toMatch(/privacy|ackVersion|ackedVersion/i);
  }
  const mai = await teacherApi.student(request, w.people['Chi Mai']!.token, w.classes['11A1']!.id, an.id);
  expect(mai.status()).toBe(403);
});
