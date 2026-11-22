import { expect, test } from '@playwright/test';

// Test khói tối thiểu: máy chủ sống, mô hình lỗi thống nhất. Không cần trình duyệt.
test('health trả UP và có X-Request-Id', async ({ request }) => {
  const res = await request.get('/health');
  expect(res.status()).toBe(200);
  expect(res.headers()['x-request-id']).toBeTruthy();
  expect(await res.json()).toMatchObject({ status: 'UP' });
});

test('API cần đăng nhập trả lỗi UNAUTHENTICATED tiếng Việt', async ({ request }) => {
  const res = await request.get('/api/v1/sample/items');
  expect(res.status()).toBe(401);
  const body = await res.json();
  expect(body.code).toBe('UNAUTHENTICATED');
  expect(body.requestId).toBeTruthy();
});
