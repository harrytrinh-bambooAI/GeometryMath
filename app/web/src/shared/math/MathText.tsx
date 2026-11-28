import katex from 'katex';
import 'katex/dist/katex.min.css';
import { createElement, memo } from 'react';
import type { ReactNode } from 'react';
import { latexToPlain, splitMath } from './math.ts';
import './math.css';

/**
 * T-261128-74 · Component render toán DÙNG CHUNG (KaTeX) cho mọi màn giáo viên + học sinh: đề, phương án, lời giải,
 * gợi ý, kiến thức trọng tâm, giáo án, rubric, thông báo, lỗi sai, trình chiếu... Đoạn `$...$` -> KaTeX; chuỗi là
 * biểu thức trần (phương án "\dfrac{a}{2}") -> KaTeX cả chuỗi; đoạn chữ còn sót lệnh -> ký hiệu Unicode.
 * KaTeX lỗi cú pháp không ném lỗi (throwOnError: false) mà hiện biểu thức màu đỏ; `trust` tắt nên không có HTML lạ.
 * Chỗ không chứa được HTML (<option>, title, aria-label) dùng latexToPlain.
 */
const cache = new Map<string, string>();
function render(src: string, display: boolean): string {
  const key = `${display ? 'D' : 'I'}${src}`;
  let html = cache.get(key);
  if (html === undefined) {
    html = katex.renderToString(src, { throwOnError: false, displayMode: display, strict: 'ignore', output: 'htmlAndMathml' });
    if (cache.size > 2000) cache.clear();
    cache.set(key, html);
  }
  return html;
}

export function mathNodes(text: string | null | undefined): ReactNode[] {
  return splitMath(text).map((s, i) =>
    s.kind === 'text' ? (
      latexToPlain(s.value)
    ) : (
      <span key={i} className={s.display ? 'math math-block' : 'math'} dangerouslySetInnerHTML={{ __html: render(s.value, s.display) }} />
    ),
  );
}

type Props = {
  text: string | null | undefined;
  /** Thẻ bao (mặc định span). */
  as?: 'span' | 'div' | 'p' | 'li' | 'strong' | 'label' | 'h1' | 'h2' | 'h3' | 'h4' | 'td' | 'dd';
  className?: string;
  'data-testid'?: string;
};

export const MathText = memo(function MathText({ text, as = 'span', className, ...rest }: Props) {
  return createElement(as, { className, 'data-testid': rest['data-testid'] }, ...mathNodes(text));
});

export { latexToPlain };
