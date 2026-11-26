import { Icon } from '../../shared/ui/Icon.tsx';
import { verifyIcon } from '../../shared/ui/icons.ts';
const VERIFIED = 'Đã kiểm chứng';
const APPROVED = 'Giáo viên đã duyệt';
const AI = 'AI soạn';

interface Props {
  machineLabel: string;
  machineNote?: string | null;
  humanLabel: string;
  /** Nơi hiện: 'figure' (đầu thẻ hình) hoặc 'box' (hộp duyệt) — hai nơi luôn cùng hiện (BR-S2E5-14). */
  where: 'figure' | 'box' | 'student';
}

/**
 * Nhãn hai trục (PO-02, DQ-07): trục máy "Đã kiểm chứng" / "Cần xem lại" (chỉ engine gắn) và trục người
 * ("AI soạn", "Đang sửa", "Giáo viên đã duyệt", "Bị thay thế"). Cạnh "Đã kiểm chứng" luôn có dòng
 * "Máy đã kiểm tra phần tính toán". Không có nút nào ở đây: người dùng không đổi được nhãn máy.
 */
export function TrustLabels({ machineLabel, machineNote, humanLabel, where }: Props) {
  const human = humanLabel === AI ? 'chip s2e5-pur' : humanLabel === APPROVED ? 'chip grn' : 'chip';
  return (
    <div className="s2e5-labels" data-testid={`s2e5-labels-${where}`}>
      <span className={`chip ${machineLabel === VERIFIED ? 'grn' : 'yel'}`} data-testid={`s2e5-machine-${where}`}><Icon name={verifyIcon(machineLabel === VERIFIED)} />
        {machineLabel}
      </span>
      <span className={human} data-testid={`s2e5-human-${where}`}><Icon name={humanLabel === APPROVED ? 'shield' : humanLabel === AI ? 'spark' : 'pencil'} />
        {humanLabel}
      </span>
      {machineNote && (
        <span className="s2e5-machine-note" data-testid={`s2e5-machine-note-${where}`}>
          {machineNote}
        </span>
      )}
    </div>
  );
}
