import { fitView, toPercent, type Scene2D } from './geometry2d.ts';
import './scene2d.css';

/**
 * Vẽ cảnh 2D (SCENE-CONTRACT-2D.md): SVG cho đoạn và đường tròn, nút HTML cho đỉnh (vùng chạm 44 × 44 px, BR-S2E2-11).
 * Khung giữ đúng tỉ lệ của hình nên co giãn theo màn (xoay iPad vẽ lại cân đối, trạng thái đo do trang giữ).
 * Màu, bo góc, cỡ chữ chỉ qua token theme (scene2d.css).
 */
export function Scene2DView({
  scene,
  selected = [],
  onTapPoint,
  testId = 'scene2d',
}: {
  scene: Scene2D;
  selected?: string[];
  onTapPoint?: (id: string) => void;
  testId?: string;
}) {
  const view = fitView(scene);
  const ratio = view.width / view.height;
  const pos = (id: string) => {
    const p = scene.points.find((q) => q.id === id)!;
    return toPercent(view, p.x, p.y);
  };
  return (
    <div
      className="s2d"
      data-testid={testId}
      data-coord-source={scene.coordSource}
      style={{ aspectRatio: `${ratio}`, width: `min(100%, calc(52vh * ${ratio}))` }}
    >
      <svg className="s2d-svg" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
        {scene.circles.map((c) => {
          const o = pos(c.center);
          return (
            <ellipse
              key={`c-${c.center}`}
              className="s2d-circle"
              cx={o.left}
              cy={o.top}
              rx={(c.r / view.width) * 100}
              ry={(c.r / view.height) * 100}
              vectorEffect="non-scaling-stroke"
            />
          );
        })}
        {scene.segments.map(([a, b]) => {
          const p = pos(a!);
          const q = pos(b!);
          return (
            <line
              key={`s-${a}-${b}`}
              className="s2d-seg"
              x1={p.left}
              y1={p.top}
              x2={q.left}
              y2={q.top}
              vectorEffect="non-scaling-stroke"
            />
          );
        })}
      </svg>
      {scene.points.map((p) => {
        const at = pos(p.id);
        const on = selected.includes(p.id);
        return (
          <button
            key={p.id}
            type="button"
            className={`s2d-vertex${on ? ' on' : ''}`}
            style={{ left: `${at.left}%`, top: `${at.top}%` }}
            onClick={() => onTapPoint?.(p.id)}
            aria-pressed={on}
            aria-label={`Đỉnh ${p.label}`}
            data-testid={`${testId}-vertex-${p.id}`}
          >
            <span className="s2d-dot" />
            <span className="s2d-label" data-math>
              {p.label}
            </span>
          </button>
        );
      })}
    </div>
  );
}
