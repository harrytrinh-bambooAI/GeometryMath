import assert from 'node:assert/strict';
import { test } from 'node:test';
import { buildUrl } from './url.ts';

test('đường dẫn tuyệt đối của máy chủ giữ nguyên, kèm query', () => {
  assert.equal(buildUrl('/api/v1/student/assignments'), '/api/v1/student/assignments');
  assert.equal(buildUrl('/api/v1/x', { limit: 20, cursor: undefined, a: true }), '/api/v1/x?limit=20&a=true');
});

test('đường dẫn tương đối ghép vào /api/v1', () => {
  assert.equal(buildUrl('catalog/problems'), '/api/v1/catalog/problems');
});

test('từ chối URL sang nguồn khác (thẻ Bearer không được rời máy chủ)', () => {
  for (const bad of [
    '//evil.example/api',
    'https://evil.example/api',
    'http://127.0.0.1:8080/api/v1/x',
    'javascript://x',
    '/redirect?to=https://evil.example',
    '/\\evil.example/api',
    '\\\\evil.example',
  ]) {
    assert.throws(() => buildUrl(bad), /cùng nguồn/, bad);
  }
});
