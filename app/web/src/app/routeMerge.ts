/**
 * Gộp route con của các epic trong một vùng (FIX4b m8, soát E7): epic sau khai báo cùng đường dẫn thì THAY route của
 * epic trước (theo thứ tự trong features/index.ts). Route `index` có khóa riêng, không đụng route có path rỗng.
 */
export interface RouteLike {
  path?: string;
  index?: boolean;
}

const keyOf = (r: RouteLike) => (r.index ? '\u0000index' : (r.path ?? '').replace(/^\/+|\/+$/g, ''));

export function mergeRoutes<R extends RouteLike>(lists: R[][]): R[] {
  const byKey = new Map<string, R>();
  for (const r of lists.flat()) byKey.set(keyOf(r), r);
  return [...byKey.values()];
}

/** Số màn còn dùng của từng epic sau khi gộp (màn bị epic sau thay không được tính). */
export function liveCounts<R extends RouteLike>(lists: R[][]): number[] {
  const live = new Set(mergeRoutes(lists));
  return lists.map((rs) => rs.filter((r) => live.has(r)).length);
}
