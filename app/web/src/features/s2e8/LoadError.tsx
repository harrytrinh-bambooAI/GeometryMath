/** Báo lỗi tải: không tìm thấy (không lộ dữ liệu) hoặc lỗi mạng kèm nút "Thử lại". */
import { Icon } from '../../shared/ui/Icon.tsx';
export function LoadError({ notFound, onRetry, what }: { notFound: boolean; onRetry: () => void; what: string }) {
  return (
    <div className="alert" role="alert" data-testid="s2e8-error">
      {notFound ? `Không tìm thấy ${what}.` : `Không tải được ${what}. Kiểm tra mạng rồi bấm Thử lại.`}{' '}
      {notFound ? null : (
        <button type="button" className="btn" onClick={onRetry} data-testid="s2e8-reload"><Icon name="redo" />
          Thử lại
        </button>
      )}
    </div>
  );
}
