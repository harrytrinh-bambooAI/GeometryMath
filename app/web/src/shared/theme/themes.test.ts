import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import { DEFAULT_THEME, THEMES, isThemeId, normalizeTheme } from './themes.ts';

test('mặc định là D và đứng đầu danh sách', () => {
  assert.equal(DEFAULT_THEME, 'c');
  assert.equal(THEMES[0]!.id, 'c');
  assert.deepEqual(THEMES.map((t) => t.id).sort(), ['a', 'b', 'c', 'd']);
});

test('chỉ nhận a|b|c|d, giá trị lạ về mặc định', () => {
  assert.ok(isThemeId('a'));
  assert.ok(!isThemeId('e'));
  assert.ok(!isThemeId(null));
  assert.equal(normalizeTheme('b'), 'b');
  assert.equal(normalizeTheme('x'), 'c');
  assert.equal(normalizeTheme(undefined), 'c');
});

test('mỗi theme có khối token trong tokens.css', () => {
  const css = readFileSync(new URL('../../styles/tokens.css', import.meta.url), 'utf8');
  for (const t of THEMES) assert.ok(css.includes(`[data-theme='${t.id}']`), `thiếu [data-theme='${t.id}']`);
});
