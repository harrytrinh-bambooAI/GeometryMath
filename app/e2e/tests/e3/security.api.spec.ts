import { randomUUID } from 'node:crypto';
import type { APIRequestContext } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { API, BG, bearer, devToken } from './e3.ts';

/**
 * An ninh API của E3 (soát E3, FIX2; ca Dev-E3-08…10 ghi ở qc_findings.md mục Dev của E3): bốn đầu API của học sinh
 * — sự kiện thao tác, mở bài, gợi ý, trạng thái khóa — với vai sai (403) và học sinh không thuộc bài (404, không lộ
 * sự tồn tại của bài; BR-GEO-20, BR-GEO-112). Mọi yêu cầu bị từ chối không được ghi gì (kiểm lại bằng Theo dõi lớp).
 */
const calls = (id: string) =>
  [
    { name: 'figure-events', method: 'post', path: `/api/v1/student/assignments/${id}/figure-events`, data: () => ({ batchId: randomUUID(), events: [{ tool: 1, at: new Date().toISOString() }] }) },
    { name: 'open', method: 'post', path: `/api/v1/student/assignments/${id}/open`, data: () => ({ requestId: randomUUID() }) },
    { name: 'hints', method: 'post', path: `/api/v1/student/assignments/${id}/hints/1`, data: () => ({ requestId: randomUUID() }) },
    { name: 'lock', method: 'get', path: `/api/v1/student/assignments/${id}/lock`, data: () => undefined },
  ] as const;

async function call(request: APIRequestContext, token: string, c: ReturnType<typeof calls>[number]) {
  const headers = bearer(token);
  return c.method === 'get' ? request.get(`${API}${c.path}`, { headers }) : request.post(`${API}${c.path}`, { headers, data: c.data() });
}

async function monitorBG1(request: APIRequestContext) {
  const teacher = await devToken(request, 'F-GEO-003');
  const r = await request.get(`${API}/api/v1/teacher/assignments/${BG('BG-1')}/monitor`, { headers: bearer(teacher) });
  expect(r.status()).toBe(200);
  return (await r.json()) as { students: Array<{ displayName: string }> };
}

test('Dev-E3-08 vai giáo viên gọi API học sinh của E3 bị 403', async ({ request }) => {
  const teacher = await devToken(request, 'F-GEO-003');
  for (const c of calls(BG('BG-1'))) {
    const r = await call(request, teacher, c);
    expect(r.status(), `${c.name}: ${await r.text()}`).toBe(403);
  }
});

test('Dev-E3-09 học sinh cùng trường khác lớp (Khánh, 9B) gọi bài BG-1 của 11A1 bị 404', async ({ request }) => {
  const khanh = await devToken(request, 'F-GEO-025-1');
  for (const c of calls(BG('BG-1'))) {
    const r = await call(request, khanh, c);
    expect(r.status(), `${c.name}: ${await r.text()}`).toBe(404);
  }
  expect((await monitorBG1(request)).students.map((s) => s.displayName)).not.toContain('Khánh');
});

test('Dev-E3-10 học sinh trường khác (An, THCS Mẫu C) gọi bài BG-1 bị 404; bài không tồn tại cũng 404', async ({ request }) => {
  const anC = await devToken(request, 'F-GEO-026-1');
  for (const id of [BG('BG-1'), randomUUID()]) {
    for (const c of calls(id)) {
      const r = await call(request, anC, c);
      expect(r.status(), `${c.name} ${id}: ${await r.text()}`).toBe(404);
    }
  }
});
