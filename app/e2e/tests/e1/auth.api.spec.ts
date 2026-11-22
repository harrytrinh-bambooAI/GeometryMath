import { expect, test } from '../../support/fixtures.ts';
import { BG, CODE, MSG, PV, bearer, devToken, ipFor, leaveClass, resetAuthFailures } from './e1.ts';

/** US-GEO-101-SC6, US-GEO-102 (mã giáo viên, dò mã, phân quyền vai) — gọi thẳng máy chủ. */
test.describe.configure({ mode: 'serial' }); // các ca dò mã dùng chung bộ đếm theo tài khoản F-GEO-003

type Err = { code: string; message: string; details?: Array<{ field: string; message: string }> };

test('Tên dài đúng 60 ký tự được nhận @TC-GEO-0105', async ({ request }) => {
  const name = 'A'.repeat(60);
  try {
    const r = await request.post('/api/v1/auth/student', { data: { displayName: name, joinCode: 'HK7M2A' } });
    expect(r.status()).toBe(200);
    const s = (await r.json()) as { token: string; displayName: string };
    expect(s.displayName).toBe(name);
    const list = await request.get('/api/v1/student/assignments', { headers: bearer(s.token) });
    expect(list.status()).toBe(200);
    expect(((await list.json()) as { items: unknown[] }).items).toHaveLength(3);
  } finally {
    await leaveClass(request, 'HK7M2A', name); // khôi phục số học sinh của 11A1 (QF-E1-02)
  }
});

test('Học sinh không làm được việc của giáo viên @TC-GEO-0114', async ({ request, t0 }) => {
  const an = await devToken(request, 'F-GEO-020');
  const r = await request.post(`/api/v1/teacher/assignments/${BG('BG-2')}/release`, {
    headers: bearer(an),
    data: { openNow: false, releaseAt: new Date(t0.getTime() + 60_000).toISOString(), expectedRev: 0 },
  });
  expect(r.status()).toBe(403);
  expect(((await r.json()) as Err)).toMatchObject({ code: 'FORBIDDEN', message: MSG['03'] });
  const bg2 = (await (await request.get(`/api/v1/student/assignments/${BG('BG-2')}`, { headers: bearer(an) })).json()) as {
    releaseState: string;
  };
  expect(bg2.releaseState).toBe('unset');
  const sol = await request.get(`/api/v1/student/assignments/${BG('BG-2')}/solution`, { headers: bearer(an) });
  expect(sol.status()).toBe(403); // lời giải BG-2 vẫn khóa
});

test('Dò mã: lần thử thứ 11 trong 15 phút bị chặn @TC-GEO-0171', async ({ request }, testInfo) => {
  const ip = ipFor(testInfo);
  await resetAuthFailures(request);
  try {
    for (let i = 1; i <= 10; i++) {
      const r = await request.post('/api/v1/auth/teacher', {
        headers: { 'X-Forwarded-For': ip },
        data: { code: CODE['F-GEO-004'] },
      });
      expect(r.status(), `lần ${i}`).toBe(400);
      expect(((await r.json()) as Err).message).toBe(MSG['12']);
    }
    const r = await request.post('/api/v1/auth/teacher', { headers: { 'X-Forwarded-For': ip }, data: { code: CODE['F-GEO-003'] } });
    expect(r.status()).toBe(429);
    expect(r.headers()['retry-after']).toMatch(/^\d+$/);
    const body = (await r.json()) as Err & { token?: string };
    expect(body).toMatchObject({ code: 'RATE_LIMITED', message: MSG['59'] });
    expect(body.token).toBeUndefined();
  } finally {
    await resetAuthFailures(request);
  }
});

test('Mã quá hạn dùng @TC-GEO-0176', async ({ request }, testInfo) => {
  await resetAuthFailures(request);
  const r = await request.post('/api/v1/auth/teacher', {
    headers: { 'X-Forwarded-For': ipFor(testInfo) },
    data: { code: CODE['F-GEO-007'] },
  });
  expect(r.status()).toBe(400);
  const body = (await r.json()) as Err & { token?: string };
  expect(body.message).toBe(MSG['12']);
  expect(body.token).toBeUndefined();
  await resetAuthFailures(request);
});

test('Phiên học sinh không dùng được đường lời giải của giáo viên @TC-GEO-0177', async ({ request }) => {
  const an = await devToken(request, 'F-GEO-020');
  const r = await request.get(`/api/v1/teacher/problems/${PV('HKG-01')}/solution`, { headers: bearer(an) });
  expect(r.status()).toBe(403);
  const body = await r.text();
  expect(JSON.parse(body)).toMatchObject({ code: 'FORBIDDEN', message: MSG['03'] });
  for (const leak of ['45°', 'Tìm hình chiếu của SD', 'tan SDA', 'Xác định góc cần tìm']) expect(body).not.toContain(leak);
});

test('Dò mã từ nhiều địa chỉ IP: lần thử thứ 11 trên cùng một tài khoản bị chặn @TC-GEO-0179', async ({ request }) => {
  await resetAuthFailures(request);
  try {
    const send = (ip: string, code: string) =>
      request.post('/api/v1/auth/teacher', { headers: { 'X-Forwarded-For': ip }, data: { code } });
    for (const [ip, code] of [
      ['203.0.113.10', 'GQ7KAAAAAAAA'],
      ['203.0.113.20', 'GQ7KBBBBBBBB'],
    ] as const) {
      for (let i = 0; i < 5; i++) {
        const r = await send(ip, code);
        expect(r.status()).toBe(400);
        expect((await r.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG['12'] });
      }
    }
    const r = await send('203.0.113.30', CODE['F-GEO-003']!);
    expect(r.status()).toBe(429);
    expect(r.headers()['retry-after']).toMatch(/^\d+$/);
    const body = (await r.json()) as Err & { token?: string };
    expect(body).toMatchObject({ code: 'RATE_LIMITED', message: MSG['59'] });
    expect(body.token).toBeUndefined();
  } finally {
    await resetAuthFailures(request); // móc PO-51: không để ca sau bị chặn
  }
});
