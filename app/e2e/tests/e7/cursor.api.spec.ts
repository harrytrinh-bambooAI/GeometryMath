import { expect, test } from '../../support/fixtures.ts';
import { API, asg, bearer, box, golden, st } from './e7.ts';

/** E7 — đọc sự kiện theo con trỏ (thời điểm, mã sự kiện), cận thời gian, không tổng số (US-GEO-704). */

interface EvPage {
  items: Array<{ id: number; occurredAt: string; kind: string }>;
  nextCursor: string | null;
  [k: string]: unknown;
}

/** 100 sự kiện thao tác của Hà trên BG-1; {@code tieAt}: hai sự kiện thứ 50 và 51 (mới nhất trước) cùng mốc thời gian. */
async function hundred(request: import('@playwright/test').APIRequestContext, tie: boolean) {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  const now = Date.now() - 5000;
  const evs = Array.from({ length: 100 }, (_, i) => {
    // i = 0 là sự kiện mới nhất; i = 49 và i = 50 cùng mốc khi tie
    const back = tie && i === 50 ? 49 : i;
    return { tool: (i % 7) + 1, at: new Date(now - back * 1000).toISOString() };
  });
  const r = await request.post(`${API}/api/v1/student/assignments/${id}/figure-events`, {
    headers: bearer(ha.token),
    data: { batchId: crypto.randomUUID(), events: evs.reverse() },
  });
  expect(r.status(), await r.text()).toBe(200);
  return { b, id, ha };
}

async function readAll(request: import('@playwright/test').APIRequestContext, token: string, url: string) {
  const pages: EvPage[] = [];
  let cursor: string | null = null;
  do {
    const r = await request.get(`${url}&limit=50${cursor ? `&cursor=${encodeURIComponent(cursor)}` : ''}`, { headers: bearer(token) });
    expect(r.status(), await r.text()).toBe(200);
    const p = (await r.json()) as EvPage;
    pages.push(p);
    cursor = p.nextCursor;
  } while (cursor && pages.length < 5);
  return pages;
}

test('Đọc sự kiện theo con trỏ, không trùng không sót @TC-GEO-0734', async ({ request }) => {
  const { b, id, ha } = await hundred(request, false);
  const from = new Date(Date.now() - 3600_000).toISOString();
  const to = new Date(Date.now() + 1000).toISOString();
  const url = `${API}/api/v1/teacher/assignments/${id}/students/${ha.id}/events?from=${from}&to=${to}`;
  const pages = await readAll(request, b.teacher.token, url);
  expect(pages.map((p) => p.items.length).filter((n) => n > 0)).toEqual([50, 50]);
  const all = pages.flatMap((p) => p.items);
  expect(new Set(all.map((e) => e.id)).size).toBe(100);
  const keys = all.map((e) => [Date.parse(e.occurredAt), e.id] as const);
  for (let i = 1; i < keys.length; i++) {
    const [ta, ia] = keys[i - 1]!;
    const [tb, ib] = keys[i]!;
    expect(ta > tb || (ta === tb && ia > ib), 'mới nhất trước').toBe(true);
  }
  for (const p of pages) {
    expect(Object.keys(p).filter((k) => !['items', 'nextCursor'].includes(k))).toEqual([]); // không có tổng số
  }
});

test('Hai sự kiện trùng mốc thời gian ở ranh giới trang không lặp, không sót @TC-GEO-0739', async ({ request }) => {
  const { b, id, ha } = await hundred(request, true);
  const from = new Date(Date.now() - 3600_000).toISOString();
  const url = `${API}/api/v1/teacher/assignments/${id}/students/${ha.id}/events?from=${from}`;
  const pages = await readAll(request, b.teacher.token, url);
  const p1 = pages[0]!.items;
  const p2 = pages[1]!.items;
  expect(p1).toHaveLength(50);
  expect(p2).toHaveLength(50);
  expect(p1[49]!.occurredAt).toBe(p2[0]!.occurredAt); // cặp trùng mốc nằm hai bên ranh giới trang
  const ids = [...p1, ...p2].map((e) => e.id);
  expect(new Set(ids).size).toBe(100);
});

test('Con trỏ hỏng @TC-GEO-0738', async ({ request }) => {
  const b = await box(request, golden());
  const id = asg(b, 'BG-1');
  const ha = st(b, 'Hà');
  const r = await request.get(`${API}/api/v1/teacher/assignments/${id}/students/${ha.id}/events?cursor=khong-phai-con-tro`, {
    headers: bearer(b.teacher.token),
  });
  expect(r.status()).toBe(400);
  const body = (await r.json()) as Record<string, unknown>;
  expect(body.code).toBe('INVALID_CURSOR');
  expect(body.items).toBeUndefined();
});
