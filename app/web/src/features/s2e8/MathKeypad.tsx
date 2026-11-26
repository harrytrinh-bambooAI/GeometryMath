import type { ClipboardEvent, KeyboardEvent, MouseEvent } from 'react';
import { ALLOWED, BACKSPACE, KEY_ROWS, MSG_MAX, backspace, emptyField, insert, keyName, paste, setCaret, type Field } from './keypad.ts';

/**
 * Ô đáp số của bài luyện (BR-S2E8-02): KHÔNG phải thẻ input nên chạm vào không bật bàn phím hệ thống; chạm giữa hai
 * ký tự để đặt con trỏ; dán thì lọc theo bảng phím (BR-S2E8-05). Nội dung toán dùng --font-math.
 */
export function AnswerBox({
  field,
  onChange,
  onSubmit,
  disabled = false,
  invalid = false,
}: {
  field: Field;
  onChange: (f: Field) => void;
  onSubmit?: () => void;
  disabled?: boolean;
  invalid?: boolean;
}) {
  const cs = [...field.value];
  const caret = Math.max(0, Math.min(field.caret, cs.length));

  const place = (e: MouseEvent<HTMLDivElement>) => {
    if (disabled) return;
    const el = (e.target as HTMLElement).closest<HTMLElement>('[data-i]');
    if (!el) {
      onChange(setCaret(field, cs.length));
      return;
    }
    const i = Number(el.dataset.i);
    const r = el.getBoundingClientRect();
    onChange(setCaret(field, e.clientX < r.left + r.width / 2 ? i : i + 1));
  };
  const onPaste = (e: ClipboardEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (!disabled) onChange(paste(field, e.clipboardData.getData('text')));
  };
  const onKey = (e: KeyboardEvent<HTMLDivElement>) => {
    if (disabled || e.ctrlKey || e.metaKey || e.altKey) return;
    if (e.key === 'Backspace') onChange(backspace(field));
    else if (e.key === 'ArrowLeft') onChange(setCaret(field, caret - 1));
    else if (e.key === 'ArrowRight') onChange(setCaret(field, caret + 1));
    else if (e.key === 'Enter') onSubmit?.();
    else if (e.key === '*') onChange(insert(field, '×'));
    else if (e.key === '.') onChange(insert(field, ','));
    else if (ALLOWED.has(e.key)) onChange(insert(field, e.key));
    else return;
    e.preventDefault();
  };

  return (
    <div className="s2e8-answer-wrap">
      <div
        className={`s2e8-answer${invalid ? ' bad' : ''}${disabled ? ' off' : ''}`}
        role="textbox"
        aria-label="Đáp số của em"
        aria-readonly="true"
        aria-disabled={disabled}
        aria-invalid={invalid}
        tabIndex={disabled ? -1 : 0}
        inputMode="none"
        data-math
        data-value={field.value}
        data-caret={caret}
        data-testid="s2e8-answer"
        onClick={place}
        onPaste={onPaste}
        onKeyDown={onKey}
      >
        {cs.map((c, i) => (
          <span key={i} data-i={i} className={`s2e8-ch${i === caret && !disabled ? ' cur' : ''}`}>
            {c}
          </span>
        ))}
        {!disabled && caret === cs.length ? <span className="s2e8-caret" aria-hidden="true" /> : null}
      </div>
      {field.full ? (
        <p className="s2e8-max" role="status" data-testid="s2e8-max">
          {MSG_MAX}
        </p>
      ) : null}
    </div>
  );
}

/**
 * Bàn phím toán riêng (US-S2E8-01, DQ-11): 6 cột, cùng bố cục trên iPad và iPhone (iPhone nén chiều cao hàng), mọi
 * phím cao ≥ 44 điểm. "Kiểm tra" khóa khi ô trống (BR-S2E8-04).
 */
export function MathKeypad({
  field,
  onChange,
  onSubmit,
  disabled = false,
  busy = false,
}: {
  field: Field;
  onChange: (f: Field) => void;
  onSubmit: () => void;
  disabled?: boolean;
  busy?: boolean;
}) {
  const press = (k: string) => onChange(k === BACKSPACE ? backspace(field) : insert(field, k));
  const empty = field.value.length === 0;
  return (
    <div className="s2e8-kbd" role="group" aria-label="Bàn phím toán" data-testid="s2e8-keypad" aria-disabled={disabled}>
      {KEY_ROWS.flat().map((k) => (
        <button
          key={k}
          type="button"
          className={`s2e8-key${k === BACKSPACE ? ' fn' : ''}`}
          disabled={disabled}
          onClick={() => press(k)}
          aria-label={k === BACKSPACE ? 'Xóa lùi' : k}
          data-math
          data-testid={`s2e8-key-${keyName(k)}`}
        >
          {k === '-' ? '−' : k}
        </button>
      ))}
      <button type="button" className="s2e8-key fn w2" disabled={disabled} onClick={() => press('x')} data-math data-testid="s2e8-key-x">
        x
      </button>
      <button
        type="button"
        className="s2e8-key fn w2"
        disabled={disabled}
        onClick={() => onChange(emptyField)}
        data-testid="s2e8-key-xoa-het"
      >
        Xóa hết
      </button>
      <button
        type="button"
        className="s2e8-key ok w2"
        disabled={disabled || empty || busy}
        onClick={onSubmit}
        data-testid="s2e8-check"
      >
        {busy ? 'Đang gửi…' : 'Kiểm tra'}
      </button>
    </div>
  );
}
