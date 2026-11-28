import { expect, test } from '../support/fixtures.ts';

// Test khung (@TC-KHUNG-01): máy chủ sống, mô hình lỗi thống nhất. Không thuộc test_cases.md của QC.
test('health trả UP và có X-Request-Id @TC-KHUNG-01 @smoke', async ({ request }) => {
  const res = await request.get('/health');
  expect(res.status()).toBe(200);
  expect(res.headers()['x-request-id']).toBeTruthy();
  expect(await res.json()).toMatchObject({ status: 'UP' });
});

test('API cần đăng nhập trả lỗi UNAUTHENTICATED tiếng Việt @TC-KHUNG-01 @smoke', async ({ request }) => {
  const res = await request.get('/api/v1/sample/items');
  expect(res.status()).toBe(401);
  const body = await res.json();
  expect(body.code).toBe('UNAUTHENTICATED');
  expect(body.message).toBe('Bạn cần đăng nhập để tiếp tục.');
  expect(body.requestId).toBeTruthy();
});
