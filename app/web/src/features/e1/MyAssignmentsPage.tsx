import { tid } from '../../shared/testid.ts';
import { ExploreBlock } from '../e4/Explore.tsx';
import { AssignmentCards } from '../e6/AssignmentCards.tsx';
import { getMe } from './e1Session.ts';

/**
 * Màn "Bài của em" (US-GEO-101): lời chào và bài giao của các lớp em đang thuộc, kèm tiến độ k/3. Thẻ bài, giờ mở,
 * dòng "Hôm nay em có n bài cần làm" là bản đầy đủ của E6 (US-GEO-606).
 */
export function MyAssignmentsPage() {
  const me = getMe();
  return (
    <div data-testid={tid('e1', 'my-assignments')}>
      <h2 data-testid={tid('e1', 'greeting')}>Chào {me?.displayName ?? ''}</h2>
      {me?.className ? <p className="muted small">Lớp {me.className}</p> : null}
      {/* E6: dòng "Hôm nay em có n bài cần làm", thẻ bài đầy đủ (US-GEO-606, 612-SC4) */}
      <AssignmentCards />
      {/* E4: khối "Tự khám phá" (US-GEO-403-SC1, SC2) */}
      <ExploreBlock />
    </div>
  );
}
