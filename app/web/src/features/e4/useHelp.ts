import { useCallback, useEffect, useMemo, useState } from 'react';
import type { ViewerHighlight } from '../../shared/viewer/Figure3DViewer.tsx';
import type { ViewerOverlay } from '../../shared/viewer/overlay.tsx';
import { rankNames } from '../../shared/viewer/model.ts';
import type { ViewerSolid } from '../../shared/viewer/types.ts';
import { e4Api, type Asked, type HelpState, type SolutionStep, type SolutionView } from './api.ts';

/**
 * Trạng thái bảng Gợi ý / Lời giải / Hỏi của MỘT bài, giữ ở cấp trang (tab đổi qua lại hay bảng trượt điện thoại thu
 * vào không mất gợi ý đã xem, bước đang chọn, câu đã hỏi). Bước đang chọn quyết định phần tô và phần vẽ thêm trên hình
 * (US-GEO-407, 408): tô đúng đối tượng của bước đó; vẽ thêm cộng dồn các bước 1…n (BR-GEO-187).
 */
export interface AskTurn {
  key: string;
  question: string;
  requestId: string;
  pending: boolean;
  error?: unknown;
  answer?: Asked;
}

export function useHelp(assignmentId: string) {
  const [help, setHelp] = useState<HelpState | null>(null);
  const [helpError, setHelpError] = useState<unknown>(null);
  const [solution, setSolution] = useState<SolutionView | null>(null);
  const [step, setStep] = useState(0);
  const [asks, setAsks] = useState<AskTurn[]>([]);

  const refresh = useCallback(async () => {
    try {
      const h = await e4Api.help(assignmentId);
      setHelp(h);
      setHelpError(null);
      return h;
    } catch (e) {
      setHelpError(e);
      return null;
    }
  }, [assignmentId]);

  // đổi bài: bỏ trạng thái của bài cũ (đặt state trong lúc render, theo mẫu "điều chỉnh state khi prop đổi" của React)
  const [forId, setForId] = useState(assignmentId);
  if (forId !== assignmentId) {
    setForId(assignmentId);
    setHelp(null);
    setSolution(null);
    setStep(0);
    setAsks([]);
  }
  useEffect(() => {
    let alive = true;
    e4Api
      .help(assignmentId)
      .then((h) => {
        if (!alive) return;
        setHelp(h);
        setHelpError(null);
      })
      .catch((e: unknown) => alive && setHelpError(e));
    return () => {
      alive = false;
    };
  }, [assignmentId]);

  const current: SolutionStep | undefined = solution?.steps[step];
  const highlight = useMemo<ViewerHighlight | undefined>(() => {
    const h = current?.highlight;
    if (!h) return undefined;
    return { points: h.points, edges: h.edges, faces: h.faces, angles: h.angles };
  }, [current]);
  const overlays = useMemo<ViewerOverlay[] | undefined>(() => {
    if (!solution) return undefined;
    return solution.steps.slice(0, step + 1).flatMap((s) => s.draw ?? []);
  }, [solution, step]);

  return { help, helpError, refresh, solution, setSolution, step, setStep, asks, setAsks, highlight, overlays };
}
export type HelpModel = ReturnType<typeof useHelp>;

/**
 * Tên cạnh của lời giải ("AD") → id cạnh của bộ xem: hai tên đỉnh xếp theo thứ tự của bộ xem (S trước, rồi chữ cái, rồi
 * số dấu phẩy trên; model.ts rankNames), ví dụ "AD", "SD", "AA′".
 */
export function resolveHighlight(h: ViewerHighlight | undefined, solid: ViewerSolid | null): ViewerHighlight | undefined {
  if (!h || !solid) return h;
  const names = solid.points.map((p) => p.name);
  const rank = rankNames(names);
  const byLen = [...names].sort((a, b) => b.length - a.length);
  const edges = (h.edges ?? []).map((e) => {
    const first = byLen.find((n) => e.startsWith(n));
    const second = first ? e.slice(first.length) : '';
    if (!first || !names.includes(second)) return e;
    return (rank.get(first) ?? 0) <= (rank.get(second) ?? 0) ? first + second : second + first;
  });
  return { ...h, edges };
}

/** Dòng "Đang tô trên hình: SD, AD, góc SDA" (BR-GEO-184); rỗng khi bước không gắn đối tượng nào. */
export function highlightLine(h: SolutionStep['highlight']): string | null {
  if (!h) return null;
  const parts = [...(h.points ?? []), ...(h.edges ?? []), ...(h.faces ?? []), ...(h.angles ?? []).map((a) => `góc ${a}`)];
  return parts.length ? parts.join(', ') : null;
}
