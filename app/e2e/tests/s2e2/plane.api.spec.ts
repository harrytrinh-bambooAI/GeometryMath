import { expect, test } from '../../support/fixtures.ts';
import { A, API, AN, TEACHER, bearer, keys, numbers, planeJson, token } from './s2e2.ts';

/** S2-E2 — API: khung môn học (US-S2E2-01-FC3), SceneSpec 2D không lộ đáp số (US-S2E2-02-SC6, US-S2E2-03-FC2). */

const FORBIDDEN_KEYS = /^(answer|answers|finalAnswer|solution|solutions|steps|step|value_asked|result)$/i;

test('@TC-S2E2-004 gọi môn không tồn tại trả NOT_FOUND, không lỗi nội bộ', async ({ request }) => {
  const t = await token(request, TEACHER);
  const cat = await request.get(`${API}/api/v1/subjects`, { headers: bearer(t) });
  expect(cat.status()).toBe(200);
  const items = ((await cat.json()) as { items: Array<{ id: string; name: string }> }).items;
  expect(items.map((s) => s.name)).toEqual(['Hình học']);
  const r = await request.post(`${API}/api/v1/subjects/vat-ly/solve`, {
    headers: bearer(t),
    data: { statement: 'Một vật rơi tự do từ độ cao 20 m. Tính thời gian rơi.', data: null },
  });
  expect(r.status()).toBe(404);
  const body = (await r.json()) as { code: string; requestId: string };
  expect(body.code).toBe('NOT_FOUND');
  expect(body.requestId).toBeTruthy();
  // môn có thật vẫn phục vụ
  const ok = await request.post(`${API}/api/v1/subjects/geometry/solve`, {
    headers: bearer(t),
    data: {
      statement: 'Hình chữ nhật ABCD có AB = 6, AD = 8. Tính AC.',
      data: { shape: 'rectangle', given: [{ type: 'length', of: ['A', 'B'], value: 6 }, { type: 'length', of: ['A', 'D'], value: 8 }], asked: { kind: 'length', of: ['A', 'C'] } },
    },
  });
  expect(ok.status()).toBe(200);
  expect(((await ok.json()) as { solution: { answer: string } }).solution.answer).toBe('10');
});

test('@TC-S2E2-013 SceneSpec 2D gửi xuống thiết bị không chứa đáp số hay bước giải', async ({ request }) => {
  const r = await planeJson(request, A.PL01_LOCKED);
  const body = (await r.json()) as { scene: Record<string, unknown>; locked: Array<{ kind: string; of: string[] }> };
  const scene = body.scene;
  expect(Object.keys(scene).sort()).toEqual(['circles', 'constraints', 'contract', 'coordSource', 'kind', 'points', 'segments']);
  expect(scene.contract).toBe('scene2d-v1');
  // chỉ điểm, nhãn, ràng buộc đề cho, tọa độ
  for (const p of scene.points as Array<Record<string, unknown>>) expect(Object.keys(p).sort()).toEqual(['id', 'label', 'x', 'y']);
  const cons = scene.constraints as Array<{ type: string; of: string[]; value: number | null }>;
  expect(cons.map((c) => `${c.type}:${c.of.join('')}=${c.value}`).sort()).toEqual(['length:AB=5', 'length:AC=12', 'length:BC=13']);
  // không có khóa đáp số / bước giải / gợi ý trong cảnh; không có giá trị góc A (90) ở bất kỳ đâu
  expect(keys(scene).filter((k) => FORBIDDEN_KEYS.test(k) || /hint/i.test(k))).toEqual([]);
  expect(numbers(body).filter((n) => Math.abs(n - 90) < 1e-6)).toEqual([]);
  // đại lượng bị khóa chỉ có loại và điểm
  expect(body.locked).toEqual([{ kind: 'angle', of: ['B', 'A', 'C'] }]);
  expect(r.headers()['cache-control']).toContain('no-store');
});

test('@TC-S2E2-026 lời giải và đáp số khóa trong dữ liệu trả về (PL-02 đang khóa)', async ({ request }) => {
  const t = await token(request, AN);
  const urls = [
    `/api/v1/student/assignments/${A.PL02_LOCKED}/plane`,
    `/api/v1/student/assignments/${A.PL02_LOCKED}/lock`,
    `/api/v1/student/assignments/${A.PL02_LOCKED}/solution`,
  ];
  for (const u of urls) {
    const r = await request.get(`${API}${u}`, { headers: bearer(t) });
    const text = await r.text();
    expect(r.status(), `${u}: ${text}`).toBeLessThan(500);
    // không có 70° ở bất kỳ dạng viết nào
    expect(text, u).not.toMatch(/70\s*(°|độ|deg)/i);
    expect(text, u).not.toMatch(/bảy mươi/i);
    if (r.headers()['content-type']?.includes('json') && text) {
      const json: unknown = JSON.parse(text);
      expect(numbers(json).filter((n) => Math.abs(n - 70) < 1e-6), u).toEqual([]);
      expect(keys(json).filter((k) => /^(steps|solution|finalAnswer|answer)$/i.test(k)), u).toEqual([]);
    }
    expect(r.headers()['cache-control'] ?? '', u).toContain('no-store');
  }
  const body = (await (await planeJson(request, A.PL02_LOCKED)).json()) as { released: boolean; hints: Array<{ text: string }> };
  expect(body.released).toBe(false);
  for (const h of body.hints) expect(h.text).not.toMatch(/70/);
});
