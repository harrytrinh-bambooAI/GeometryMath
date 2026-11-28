import assert from 'node:assert/strict';
import { test } from 'node:test';
import { latexToPlain, looksLikeBareLatex, splitMath } from './math.ts';

// T-261128-74 · npm test (node --test).
test('tách $...$ trong câu văn', () => {
  assert.deepEqual(splitMath('Cho hình chóp $S.ABCD$ có $SA\\perp(ABCD)$.'), [
    { kind: 'text', value: 'Cho hình chóp ' },
    { kind: 'math', value: 'S.ABCD', display: false },
    { kind: 'text', value: ' có ' },
    { kind: 'math', value: 'SA\\perp(ABCD)', display: false },
    { kind: 'text', value: '.' },
  ]);
});

test('phương án là biểu thức trần thì cả chuỗi là toán', () => {
  assert.deepEqual(splitMath('\\dfrac{a^{3}}{3}'), [{ kind: 'math', value: '\\dfrac{a^{3}}{3}', display: false }]);
  assert.deepEqual(splitMath('\\approx 50{,}77^\\circ'), [{ kind: 'math', value: '\\approx 50{,}77^\\circ', display: false }]);
  assert.equal(looksLikeBareLatex('a\\sqrt{3}'), true);
  assert.equal(looksLikeBareLatex('Khoảng cách từ M tới \\perp'), false);
  assert.deepEqual(splitMath('a'), [{ kind: 'text', value: 'a' }]);
});

test('$$...$$, \\(...\\) và \\$ thường', () => {
  assert.deepEqual(splitMath('$$x^2$$'), [{ kind: 'math', value: 'x^2', display: true }]);
  assert.deepEqual(splitMath('\\(x\\)'), [{ kind: 'math', value: 'x', display: false }]);
  assert.deepEqual(splitMath('giá \\$5 và $ lẻ'), [{ kind: 'text', value: 'giá $5 và $ lẻ' }]);
});

test('latexToPlain cho <option>/title', () => {
  assert.equal(latexToPlain('$SA\\perp(ABC)$, góc $\\approx 50{,}77^\\circ$'), 'SA⊥(ABC), góc ≈50,77°');
  assert.equal(latexToPlain('$\\dfrac{a\\sqrt{3}}{2}$'), 'a√3/2');
  assert.equal(latexToPlain('$a^{3}$'), 'a³');
  assert.equal(latexToPlain('chữ thường'), 'chữ thường');
});
