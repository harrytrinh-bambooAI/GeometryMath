export const API_BASE = '/api/v1';

export type Query = Record<string, string | number | boolean | undefined>;

/**
 * Dựng URL cùng nguồn cho máy khách API. Chỉ nhận đường dẫn tương đối của chính máy chủ:
 * từ chối `//host` (URL không giao thức => trình duyệt gửi sang nguồn khác, kèm thẻ Bearer),
 * mọi chuỗi có `://`, và dấu `\` (trình duyệt coi `/\host` như `//host`).
 */
export function buildUrl(path: string, query?: Query): string {
  if (path.startsWith('//') || path.includes('://') || path.includes('\\')) {
    throw new Error(`apiRequest chỉ nhận đường dẫn tương đối cùng nguồn, nhận: ${path}`);
  }
  const url = path.startsWith('/') ? path : `${API_BASE}/${path}`;
  if (!query) return url;
  const qs = new URLSearchParams();
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined) qs.set(k, String(v));
  }
  const s = qs.toString();
  return s ? `${url}?${s}` : url;
}
