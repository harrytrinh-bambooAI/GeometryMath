import { Icon } from '../../shared/ui/Icon.tsx';
import { labelTone } from './exportModel.ts';

/** Loại hình + hai nhãn của đề (PO-02: trục máy chỉ do engine gắn, trục người do giáo viên). */
export function Labels({ machine, human, kind, testPrefix = 's2e9' }: { machine: string; human: string; kind?: string; testPrefix?: string }) {
  return (
    <div className="s2e9-labels">
      {kind ? (
        <span className="chip">
          <Icon name={kind === 'plane' ? 'file' : 'cube'} />
          {kind === 'plane' ? 'Phẳng' : 'Không gian'}
        </span>
      ) : null}
      <span className={`chip ${labelTone(machine)}`} data-testid={`${testPrefix}-machine`}>
        <Icon name={machine === 'Đã kiểm chứng' ? 'check' : 'warn'} />
        {machine}
      </span>
      <span className={`chip ${labelTone(human)}`} data-testid={`${testPrefix}-human`}>
        <Icon name={human === 'Giáo viên đã duyệt' ? 'shield' : human === 'AI soạn' ? 'spark' : 'pencil'} />
        {human}
      </span>
    </div>
  );
}
