/** S3-E3 — kiểu dữ liệu của API Kết quả lớp (không import mã chạy: dùng được trong kiểm thử đơn vị). */

export type CellStatus = 'correct' | 'review' | 'doing' | 'todo';

export interface ClassOption {
  id: string;
  name: string;
}
export interface SetOption {
  id: string;
  title: string;
}
export interface Sets {
  className: string;
  hasActiveStudents: boolean;
  items: SetOption[];
}
export interface ColumnView {
  position: number;
  title: string;
}
export interface Row {
  displayName: string;
  userCode: string;
  left: boolean;
  cells: CellStatus[];
}
export interface Board {
  setId: string;
  title: string;
  className: string;
  columns: ColumnView[];
  rows: Row[];
  nextCursor?: string | null;
  updatedAt: string;
}
export interface Evidence {
  displayName: string;
  userCode: string;
  left: boolean;
  item: ColumnView;
  status: CellStatus;
  openedAt?: string | null;
  attempts: Array<{ at: string; value: string; matched: boolean }>;
  hints: Array<{ at: string; level: number }>;
  questions: Array<{ at: string; text: string }>;
  stoppedStep: number;
  statement?: string | null;
  updatedAt: string;
}
export interface Person {
  displayName: string;
  userCode: string;
}
export interface Insight {
  position: number;
  title: string;
  step: number;
  text: string;
  n: number;
  labels: string[];
  evidence: Person[];
}
export interface RawEntry {
  step: number;
  kind: 'hint' | 'question';
  text: string;
  at: string;
}
export interface StuckItem {
  position: number;
  title: string;
  mode: 'insight' | 'raw' | 'failed' | 'none';
  works: number;
  raw: RawEntry[];
}
export interface Stuck {
  minAggregate: number;
  classTooSmall: boolean;
  insights: Insight[];
  items: StuckItem[];
  updatedAt: string;
}
