import { AskTab } from './AskTab.tsx';
import { HintTab } from './HintTab.tsx';
import { SolutionTab } from './SolutionTab.tsx';
import type { HelpModel } from './useHelp.ts';

/** Nội dung ba tab của bảng trang Làm bài (chỗ cắm `slots` của E3, WorkspacePanel). */
export function e4Slots(id: string, model: HelpModel) {
  return {
    hint: <HintTab id={id} model={model} />,
    solution: <SolutionTab id={id} model={model} />,
    ask: <AskTab id={id} model={model} />,
  };
}
