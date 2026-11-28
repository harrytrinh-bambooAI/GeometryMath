import assert from 'node:assert/strict';
import { test } from 'node:test';
import { MAX_RETRIES, nextAttempt } from './toolRetry.ts';

test('FIX4b m1: lỗi 4xx (400, 403, 404, 409, 422) bỏ lô, không gửi lại', () => {
  for (const status of [400, 403, 404, 409, 422]) assert.equal(nextAttempt({ status }, 0, true), null);
});

test('FIX4b m1: lỗi mạng, 5xx, 408, 429 gửi lại có giới hạn', () => {
  for (const status of [0, 500, 503, 408, 429]) assert.equal(nextAttempt({ status }, 0, true), 1);
  assert.equal(nextAttempt(new Error('mất mạng'), 3, true), 4);
  assert.equal(nextAttempt({ status: 503 }, MAX_RETRIES, true), null);
});

test('FIX4b m1: lỗi mạng khi ngoại tuyến không tính vào số lần', () => {
  assert.equal(nextAttempt({ status: 0 }, MAX_RETRIES, false), MAX_RETRIES);
});
