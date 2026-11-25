import assert from 'node:assert/strict';
import { test } from 'node:test';
import { liveCounts, mergeRoutes } from './routeMerge.ts';

test('FIX4b m8: epic sau thay route cùng đường dẫn của epic trước', () => {
  const e1 = [{ path: 'theo-doi/:id', n: 'e1' }, { path: 'lop', n: 'e1' }];
  const e7 = [{ path: '/theo-doi/:id', n: 'e7' }];
  const merged = mergeRoutes<{ path?: string; n: string }>([e1, e7]);
  assert.deepEqual(merged.map((r) => r.n).sort(), ['e1', 'e7']);
  assert.equal(merged.find((r) => r.path?.includes('theo-doi'))?.n, 'e7');
  assert.deepEqual(liveCounts<{ path?: string; n: string }>([e1, e7]), [1, 1]);
});

test('FIX4b m8: route index không đè route path rỗng', () => {
  const a = [{ index: true }];
  const b = [{ path: '' }];
  assert.equal(mergeRoutes<{ path?: string; index?: boolean }>([a, b]).length, 2);
});
