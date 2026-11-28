import { Icon } from '../../shared/ui/Icon.tsx';
import { chipTone } from './logic.ts';

interface Labels {
  kindLabel: string;
  machineLabel: string;
  humanLabel: string;
  statusLabel?: string;
  status?: string;
}

/** Loại hình + hai nhãn (PO-02: trục máy, trục người) + trạng thái kho của một đề. */
export function CardLabels({ card, testPrefix = 's2e6' }: { card: Labels; testPrefix?: string }) {
  return (
    <div className="s2e6-labels">
      <span className="chip" data-testid={`${testPrefix}-kind`}>
        <Icon name={card.kindLabel === 'Phẳng' ? 'file' : 'cube'} />
        {card.kindLabel}
      </span>
      <span className={`chip ${chipTone(card.machineLabel)}`} data-testid={`${testPrefix}-machine`}>
        <Icon name={card.machineLabel === 'Đã kiểm chứng' ? 'check' : 'warn'} />
        {card.machineLabel}
      </span>
      <span className={`chip ${chipTone(card.humanLabel)}`} data-testid={`${testPrefix}-human`}>
        <Icon name={card.humanLabel === 'Giáo viên đã duyệt' ? 'shield' : card.humanLabel === 'AI soạn' ? 'spark' : 'pencil'} />
        {card.humanLabel}
      </span>
      {card.statusLabel && card.status !== 'mine' ? (
        <span className={`chip ${chipTone(card.statusLabel)}`} data-testid={`${testPrefix}-status`}>
          <Icon name={card.status === 'pending' ? 'clock' : card.status === 'rejected' ? 'x' : 'layers'} />
          {card.statusLabel}
        </span>
      ) : null}
    </div>
  );
}
