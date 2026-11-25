import { readFileSync } from 'node:fs';
import path from 'node:path';
import { test as base, expect } from '@playwright/test';
import type { APIRequestContext, APIResponse, Browser, TestInfo } from '@playwright/test';
import { redact, redactText } from './redact.ts';

/**
 * Fixture dùng chung cho mọi test E2E. Luôn import `test`, `expect` từ đây (không từ @playwright/test).
 * - `request` (bọc lại): ghi mọi cặp yêu cầu/phản hồi; cuối test đính kèm `http.json` và một ẢNH CHỤP
 *   bảng yêu cầu/phản hồi (để test API cũng có ảnh làm bằng chứng, đạt hay trượt).
 * - `fixture`: mã cố định của dữ liệu vàng (app/db/seed/fixture-ids.json).
 * - `tokenFor('F-GEO-003')`: thẻ JWT qua /dev/token (chỉ profile dev) cho tài khoản fixture.
 * - `t0`: mốc T0 của lượt chạy (giờ máy chủ lúc reset seed), Date.
 * Quy ước tiêu đề: gắn mã TC bằng tag, ví dụ test('... @TC-GEO-0100', ...) hoặc { tag: '@TC-GEO-0100' }.
 */
type Entity = { id: string; org?: string; role?: string; name?: string; displayName?: string; code?: string };
export type FixtureIds = {
  orgs: Record<string, Entity>;
  users: Record<string, Entity>;
  classes: Record<string, Entity>;
};
export const fixtureIds: FixtureIds = JSON.parse(
  readFileSync(path.resolve(import.meta.dirname, '../../db/seed/fixture-ids.json'), 'utf8'),
);

type Exchange = {
  method: string;
  url: string;
  requestHeaders?: Record<string, string>;
  requestBody?: unknown;
  status: number;
  responseHeaders: Record<string, string>;
  responseBody: string;
  ms: number;
};

const METHODS = ['get', 'post', 'put', 'patch', 'delete', 'head', 'fetch'] as const;

// Che bí mật: chung với reporter bằng chứng (che cả trace.zip, FIX4a) ở support/redact.ts.
export { MASK, redact, redactText } from './redact.ts';

function redactBody(body: string): string {
  try {
    return JSON.stringify(redact(JSON.parse(body)));
  } catch {
    return redactText(body);
  }
}

function wrapRequest(ctx: APIRequestContext, log: Exchange[]): APIRequestContext {
  return new Proxy(ctx, {
    get(target, prop, receiver) {
      const orig = Reflect.get(target, prop, receiver);
      if (typeof prop !== 'string' || !(METHODS as readonly string[]).includes(prop) || typeof orig !== 'function') {
        return typeof orig === 'function' ? orig.bind(target) : orig;
      }
      return async (url: string, options?: { data?: unknown; method?: string; headers?: Record<string, string> }) => {
        const started = Date.now();
        const res: APIResponse = await orig.call(target, url, options);
        let body = '';
        try {
          body = (await res.text()).slice(0, 8000);
        } catch {
          body = '(không đọc được thân)';
        }
        log.push({
          method: (options?.method ?? (prop === 'fetch' ? 'GET' : prop)).toUpperCase(),
          url: res.url(),
          requestHeaders: options?.headers ? (redact(options.headers) as Record<string, string>) : undefined,
          requestBody: redact(options?.data),
          status: res.status(),
          responseHeaders: redact(res.headers()) as Record<string, string>,
          responseBody: redactBody(body),
          ms: Date.now() - started,
        });
        return res;
      };
    },
  });
}

const esc = (s: string) => s.replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c] ?? c);
function pretty(s: string): string {
  try {
    return JSON.stringify(JSON.parse(s), null, 2);
  } catch {
    return s;
  }
}

async function snapshotExchanges(browser: Browser, log: Exchange[], testInfo: TestInfo): Promise<void> {
  await testInfo.attach('http.json', { body: JSON.stringify(log, null, 2), contentType: 'application/json' });
  const rows = log
    .map(
      (x, i) => `<tr><td>${i + 1}</td><td><b>${esc(x.method)}</b> ${esc(x.url)}${
        x.requestHeaders ? `<pre>${esc(JSON.stringify(x.requestHeaders, null, 2))}</pre>` : ''
      }${
        x.requestBody !== undefined ? `<pre>${esc(JSON.stringify(x.requestBody, null, 2).slice(0, 3000))}</pre>` : '' // E4: thân lớn (ảnh base64) cắt như phản hồi
      }</td><td class="s${String(x.status)[0]}">${x.status}<br><small>${x.ms} ms</small><br><small>${esc(
        x.responseHeaders['x-request-id'] ?? '',
      )}</small></td><td><pre>${esc(pretty(x.responseBody)).slice(0, 3000)}</pre></td></tr>`,
    )
    .join('');
  const html = `<!doctype html><meta charset="utf-8"><style>
    body{font:13px system-ui;margin:16px;color:#141B2D} h1{font-size:15px} table{border-collapse:collapse;width:100%}
    td,th{border:1px solid #E3E8F2;padding:6px;vertical-align:top;text-align:left} pre{margin:4px 0;white-space:pre-wrap;font:12px ui-monospace}
    .s2{color:#0F8A5F;font-weight:700} .s4,.s5{color:#D92D20;font-weight:700}</style>
    <h1>${esc(testInfo.title)} — ${esc(testInfo.project.name)} — ${testInfo.status ?? ''}</h1>
    <table><tr><th>#</th><th>Yêu cầu</th><th>Mã</th><th>Phản hồi</th></tr>${rows || '<tr><td colspan=4>(không có yêu cầu HTTP)</td></tr>'}</table>`;
  const page = await browser.newPage({ viewport: { width: 1100, height: 600 } });
  try {
    await page.setContent(html);
    const png = await page.screenshot({ fullPage: true });
    await testInfo.attach('screenshot', { body: png, contentType: 'image/png' });
  } finally {
    await page.close();
  }
}

export const test = base.extend<{
  fixture: FixtureIds;
  t0: Date;
  tokenFor: (userRef: string) => Promise<string>;
}>({
  request: async ({ request, browser }, use, testInfo) => {
    const log: Exchange[] = [];
    await use(wrapRequest(request, log));
    await snapshotExchanges(browser, log, testInfo);
  },
  fixture: async ({}, use) => {
    await use(fixtureIds);
  },
  t0: async ({}, use) => {
    await use(new Date(process.env.GEO_E2E_T0 ?? Date.now()));
  },
  tokenFor: async ({ request }, use) => {
    await use(async (userRef: string) => {
      const u = fixtureIds.users[userRef];
      if (!u?.org) throw new Error(`Không có ${userRef} trong fixture-ids.json`);
      const orgId = fixtureIds.orgs[u.org]?.id;
      const res = await request.post('/dev/token', { data: { orgId, userId: u.id, role: u.role } });
      expect(res.status(), 'cần máy chủ profile dev để lấy thẻ').toBe(200);
      return ((await res.json()) as { token: string }).token;
    });
  },
});

export { expect };
