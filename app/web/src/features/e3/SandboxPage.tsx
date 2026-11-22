import { tid } from '../../shared/testid.ts';
import { Figure3DViewer } from '../../shared/viewer/Figure3DViewer.tsx';
import type { ViewerSolid } from '../../shared/viewer/types.ts';
import './e3.css';

/**
 * Lăng trụ đứng đáy chữ L (TC-GEO-0350, mặt lõm): sáu đỉnh đáy (0,0), (2,0), (2,1), (1,1), (1,2), (0,2), cao 1.
 * Không có đề nào trong kho dùng hình này nên trang chỉ có khi bật móc kiểm thử (dev / VITE_GEO_TEST_HOOKS=1).
 */
const L_PRISM: ViewerSolid = (() => {
  const base: Array<[string, number, number]> = [
    ['A', 0, 0],
    ['B', 2, 0],
    ['C', 2, 1],
    ['D', 1, 1],
    ['E', 1, 2],
    ['F', 0, 2],
  ];
  const top = base.map(([n, x, y]) => [`${n}′`, x, y] as [string, number, number]);
  const points = [...base.map(([n, x, y]) => ({ name: n, pos: [x, y, 0] as [number, number, number] })),
    ...top.map(([n, x, y]) => ({ name: n, pos: [x, y, 1] as [number, number, number] }))];
  const names = base.map((b) => b[0]);
  const edges: Array<[string, string]> = [];
  const faces: string[][] = [names, names.map((n) => `${n}′`)];
  names.forEach((n, i) => {
    const m = names[(i + 1) % names.length]!;
    edges.push([n, m], [`${n}′`, `${m}′`], [n, `${n}′`]);
    faces.push([n, m, `${m}′`, `${n}′`]);
  });
  return { points, edges, faces };
})();

export default function LPrismSandboxPage() {
  return (
    <div className="e3-solo" data-testid={tid('e3', 'sandbox')}>
      <header className="e3-head">
        <h2>Thử hình: lăng trụ đáy chữ L</h2>
      </header>
      <div className="e3-main">
        <Figure3DViewer solid={L_PRISM} lock="open" className="e3-viewer" />
      </div>
    </div>
  );
}
