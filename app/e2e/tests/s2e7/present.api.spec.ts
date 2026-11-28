import { expect, test } from '../../support/fixtures.ts';
import { API, bearer, slot, token } from '../s2e6/s2e6.ts';
import { K } from './s2e7.ts';

/** S2-E7 — API Trình chiếu: học sinh không vào được (US-S2E7-01-FC3); Tiếp tục không mở khóa máy học sinh (US-S2E7-03-FC4). */

test('@TC-S2E7-011 học sinh không vào được Trình chiếu', async ({ request }, info) => {
  const s = slot(7, 11, info);
  const an = await token(request, s.org, s.an, 'STUDENT');
  for (const tail of ['', '/preflight']) {
    const r = await request.get(`${API}/api/v1/teacher/present/${s.doc(K.FX14)}${tail}`, { headers: bearer(an) });
    expect(r.status()).toBe(403);
    const body = await r.text();
    expect(JSON.parse(body).code).toBe('FORBIDDEN');
    expect(body).not.toContain('hình chiếu');
    expect(body).not.toContain('45°');
  }
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  const ok = await request.get(`${API}/api/v1/teacher/present/${s.doc(K.FX14)}`, { headers: bearer(trang) });
  expect(ok.status()).toBe(200);
  expect(ok.headers()['cache-control']).toContain('no-store');
});

test('@TC-S2E7-033 học sinh không mở khóa được khi giáo viên bấm Tiếp tục', async ({ request }, info) => {
  const s = slot(7, 33, info);
  const trang = await token(request, s.org, s.trang, 'TEACHER');
  const pre = await request.get(`${API}/api/v1/teacher/present/${s.doc(K.FX14)}/preflight`, { headers: bearer(trang) });
  expect((await pre.json()).ask).toBe(true);
  // "Tiếp tục" = tải dữ liệu chiếu (chỉ đọc)
  expect((await request.get(`${API}/api/v1/teacher/present/${s.doc(K.FX14)}`, { headers: bearer(trang) })).status()).toBe(200);
  const an = await token(request, s.org, s.an, 'STUDENT');
  const bg = s.bg(9001);
  const before = await request.get(`${API}/api/v1/student/assignments/${bg}`, { headers: bearer(an) });
  expect(before.status(), await before.text()).toBe(200);
  const releaseBefore = (await before.json()).releaseAt;
  for (let i = 0; i < 3; i++) {
    const sol = await request.get(`${API}/api/v1/student/assignments/${bg}/solution`, { headers: bearer(an) });
    const body = await sol.text();
    for (const leak of ['45°', 'tan SCA', 'hình chiếu', 'a√2 / a√2']) expect(body).not.toContain(leak);
  }
  const after = await request.get(`${API}/api/v1/student/assignments/${bg}`, { headers: bearer(an) });
  expect((await after.json()).releaseAt).toBe(releaseBefore);
});
