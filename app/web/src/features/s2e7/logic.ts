/** S2-E7 · logic thuần của Trình chiếu (chạy được bằng node --test). */

export const MSG = {
  preloaded: 'Đã tải sẵn · mất mạng vẫn chiếu',
  aiOffline: 'Hỏi đáp AI tạm tắt vì mất mạng',
  needNetwork: 'Cần mạng để mở lần đầu',
  partial: 'Chưa tải sẵn đủ, cần mạng',
  noFullscreen: 'Trình duyệt không cho toàn màn hình, nhấn F11',
  studentLocked: 'Máy học sinh vẫn khóa lời giải',
  ask: 'Lớp sẽ thấy lời giải trên màn chiếu, tiếp tục?',
  lastStep: 'Hết bước',
  noSolution: 'Chưa có lời giải',
  expired: 'Phiên đã hết. Vui lòng vào lại.',
};

/** Trạng thái điều khiển bước: số bước đang hiện và cờ che (BR-S2E7-02, 03). */
export interface StepState {
  shown: number;
  covered: boolean;
}

export const START: StepState = { shown: 0, covered: false };

export function next(s: StepState, total: number): StepState {
  return s.shown >= total ? s : { ...s, shown: s.shown + 1 };
}

export function prev(s: StepState): StepState {
  return s.shown <= 0 ? s : { ...s, shown: s.shown - 1 };
}

/** "Che lời giải" ẩn mọi bước nhưng nhớ vị trí; bấm lần nữa hiện lại đúng tới bước đã hiện. */
export function toggleCover(s: StepState): StepState {
  return { ...s, covered: !s.covered };
}

/** Số bước đang thấy trên màn (0 khi đang che). */
export const visible = (s: StepState) => (s.covered ? 0 : s.shown);

export interface FigureHighlight {
  edges: string[];
  angles: string[];
  faces: string[];
}

/**
 * Phần hình của một bước: đoạn (hai chữ hoa liền: AC, SC), góc ("góc SCA" hoặc ba chữ hoa liền), mặt ("(ABCD)").
 * Đoạn trả cả hai chiều (AC, CA) vì id cạnh của bộ xem theo thứ tự đề.
 */
export function highlightOf(step: string | undefined): FigureHighlight {
  const out: FigureHighlight = { edges: [], angles: [], faces: [] };
  if (!step) return out;
  for (const m of step.matchAll(/\(([A-Z]{3,4})\)/g)) out.faces.push(m[1]!);
  const cleaned = step.replace(/\([A-Z]{3,4}\)/g, ' ');
  for (const m of cleaned.matchAll(/(?<![A-Za-z])([A-Z]{2,3})(?![A-Za-z])/g)) {
    const w = m[1]!;
    if (w.length === 3) out.angles.push(w);
    else out.edges.push(w, `${w[1]}${w[0]}`);
  }
  out.edges = [...new Set(out.edges)];
  out.angles = [...new Set(out.angles)];
  return out;
}

/** Thời gian chờ tối đa giữa hai lần kiểm mạng khi đang mất mạng: không quá hạn bật lại (QA-S2E7-02). */
export function pollMs(reconnectSeconds: number): number {
  return Math.max(250, Math.min(1000, reconnectSeconds * 1000));
}
