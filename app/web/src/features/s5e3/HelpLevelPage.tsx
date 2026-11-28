import { useEffect, useState } from 'react';
import { ErrorNotice } from '../../shared/ui/ErrorNotice.tsx';
import { HelpLevelPanel } from '../s5e4/assign/HelpLevelPanel.tsx';
import { listGraded, type GradedLine } from './qaApi.ts';

/**
 * "Trợ giúp AI" của giáo viên (US-S5E3-04-SC3): mỗi bài có chấm có mục "Trợ giúp của AI" (mặc định Tắt, QS5-12) đặt mức
 * trợ giúp và hiện "Trợ giúp: …" sau khi Lưu. Form tạo bài có chấm (GradedPage, T-261128-57) không thuộc phạm vi T-58 nên
 * mục này đặt ở màn riêng, cùng thành phần HelpLevelPanel (features/s5e4/assign) để gắn vào form khi chủ form cho phép.
 */
export function HelpLevelPage() {
  const [items, setItems] = useState<GradedLine[] | null>(null);
  const [error, setError] = useState<unknown>(null);

  useEffect(() => {
    listGraded()
      .then((r) => setItems(r.items))
      .catch(setError);
  }, []);

  return (
    <section className="s5e3-page" data-testid="s5e3-help">
      <h1 className="s5e3-title">Trợ giúp của AI theo bài</h1>
      {items && items.length === 0 && <p className="s5e3-muted">Chưa có bài có chấm.</p>}
      <ul className="s5e3-help-list">
        {(items ?? []).map((g) => (
          <li key={g.id} className="card s5e3-card" data-testid={`s5e3-help-${g.code}`}>
            <strong>
              {g.code} · {g.title}
            </strong>
            <span className="s5e3-muted">{g.className}</span>
            <HelpLevelPanel assignmentId={g.id} />
          </li>
        ))}
      </ul>
      {error !== null && <ErrorNotice error={error} />}
    </section>
  );
}
