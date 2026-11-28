import { ApiError } from '../api/errors.ts';
import { tid } from '../testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

/** Hiển thị lỗi theo mô hình lỗi máy chủ: câu tiếng Việt + mã yêu cầu (để tra log), nút Thử lại nếu hợp lý. */
export function ErrorNotice({ error, onRetry, testId = tid('shell', 'error') }: { error: unknown; onRetry?: () => void; testId?: string }) {
  const e = error instanceof ApiError ? error : null;
  const message = e?.message ?? 'Đã có lỗi xảy ra, vui lòng thử lại.';
  return (
    <div className="alert" role="alert" data-testid={testId} data-error-code={e?.code ?? 'UNKNOWN'}>
      <Icon name="warn" />
      {message}
      {e?.requestId ? <span className="small muted"> (mã yêu cầu: {e.requestId})</span> : null}
      {onRetry && (!e || e.retryable) ? (
        <>
          {' '}
          <button type="button" className="btn ghost" onClick={onRetry} data-testid={`${testId}-retry`}><Icon name="redo" />
            Thử lại
          </button>
        </>
      ) : null}
    </div>
  );
}
