import { useState } from 'react';
import { tid } from '../../shared/testid.ts';

/** Một mã (mã người dùng hoặc mã khôi phục) với nút "Sao chép". */
export function CodeBox({ label, code, name }: { label: string; code: string; name: string }) {
  const [copied, setCopied] = useState(false);
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
    } catch {
      setCopied(false); // trình duyệt chặn bộ nhớ tạm: người dùng tự chép tay
    }
  };
  return (
    <div className="s2e1-field">
      <span>{label}</span>
      <div className="s2e1-code-row">
        <div className="s2e1-code" data-testid={tid('s2e1', name)}>
          {code}
        </div>
        <button type="button" className="btn ghost s2e1-noprint" onClick={copy} data-testid={tid('s2e1', 'copy', name)}>
          {copied ? 'Đã chép' : 'Sao chép'}
        </button>
      </div>
    </div>
  );
}

/** Ô tick "đã lưu mã" + nút đi tiếp: nút chỉ mở khi đã tick (DQ-16, US-S2E1-01-SC3). */
export function SavedGate({
  label,
  action,
  onContinue,
  busy = false,
}: {
  label: string;
  action: string;
  onContinue: () => void;
  busy?: boolean;
}) {
  const [saved, setSaved] = useState(false);
  return (
    <>
      <label className="s2e1-check">
        <input type="checkbox" checked={saved} onChange={(e) => setSaved(e.target.checked)} data-testid={tid('s2e1', 'saved')} />
        <span>{label}</span>
      </label>
      <button
        type="button"
        className="btn s2e1-wide s2e1-noprint"
        disabled={!saved || busy}
        aria-disabled={!saved || busy}
        onClick={onContinue}
        data-testid={tid('s2e1', 'continue')}
      >
        {action}
      </button>
    </>
  );
}
