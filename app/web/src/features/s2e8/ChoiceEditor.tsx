import { Icon } from '../../shared/ui/Icon.tsx';
import { CHOICE_MAX, CHOICE_MIN, CHOICE_TEXT_MAX, type ChoiceOptionDraft } from './choice.ts';

/**
 * S2-E9 (US-S2E9-03, khung C2-08) — soạn lựa chọn của một câu trắc nghiệm: 2…5 lựa chọn, tick MỘT đáp án đúng.
 * `n` là số thứ tự câu (dùng cho data-testid `s2e9-choice-…-<câu>-<lựa chọn>`).
 */
export function ChoiceEditor({
  n,
  options,
  disabled,
  onChange,
}: {
  n: number;
  options: ChoiceOptionDraft[];
  disabled: boolean;
  onChange: (next: ChoiceOptionDraft[]) => void;
}) {
  const setText = (k: number, text: string) => onChange(options.map((o, i) => (i === k ? { ...o, text } : o)));
  const setCorrect = (k: number) => onChange(options.map((o, i) => ({ ...o, correct: i === k })));
  const remove = (k: number) => onChange(options.filter((_, i) => i !== k));
  return (
    <fieldset className="s2e9-choices" data-testid={`s2e9-choices-${n}`} disabled={disabled}>
      <legend className="s2e8-help">Lựa chọn (tick một đáp án đúng)</legend>
      {options.map((o, k) => (
        <div key={k} className="s2e9-choice-row" data-testid={`s2e9-choice-row-${n}-${k + 1}`}>
          <label className="s2e9-choice-correct">
            <input
              type="radio"
              name={`s2e9-correct-${n}`}
              checked={o.correct}
              onChange={() => setCorrect(k)}
              aria-label={`Lựa chọn ${k + 1} là đáp án đúng`}
              data-testid={`s2e9-choice-correct-${n}-${k + 1}`}
            />
          </label>
          <input
            className="s2e9-choice-text"
            value={o.text}
            maxLength={CHOICE_TEXT_MAX + 20}
            placeholder={`Lựa chọn ${k + 1}`}
            onChange={(e) => setText(k, e.target.value)}
            aria-label={`Nội dung lựa chọn ${k + 1}`}
            data-math
            data-testid={`s2e9-choice-text-${n}-${k + 1}`}
          />
          <button
            type="button"
            className="btn ghost s2e9-choice-del"
            onClick={() => remove(k)}
            disabled={disabled || options.length <= 1}
            aria-label={`Xóa lựa chọn ${k + 1}`}
            data-testid={`s2e9-choice-del-${n}-${k + 1}`}
          >
            <Icon name="x" />
          </button>
        </div>
      ))}
      <div className="s2e8-actions">
        <button
          type="button"
          className="btn ghost"
          onClick={() => onChange([...options, { text: '', correct: false }])}
          disabled={disabled || options.length >= CHOICE_MAX + 1}
          data-testid={`s2e9-choice-add-${n}`}
        >
          <Icon name="plus" />
          Thêm lựa chọn
        </button>
        <span className="s2e8-help">
          {options.length} lựa chọn (cần {CHOICE_MIN} đến {CHOICE_MAX})
        </span>
      </div>
    </fieldset>
  );
}
