import { getToken, signalUnauthenticated } from '../auth/session.ts';
import { ApiError, isApiErrorBody, toErrorCode } from './errors.ts';

/**
 * Máy khách HTTP duy nhất của giao diện.
 * - Đường dẫn tương đối ('/api/v1/...'): dev đi qua proxy Vite, bản build cùng nguồn với máy chủ.
 * - Tự gắn `Authorization: Bearer <JWT>` nếu đã có thẻ.
 * - Mọi lỗi ném ra là `ApiError` (mã ổn định, câu tiếng Việt từ máy chủ, requestId để tra log).
 * - 401: xóa thẻ và phát sự kiện để khung app đưa người dùng về trang chủ.
 */
export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  body?: unknown;
  query?: Record<string, string | number | boolean | undefined>;
  signal?: AbortSignal;
  /** Bỏ gắn thẻ (gọi công khai như /health). */
  anonymous?: boolean;
}

export const API_BASE = '/api/v1';

function buildUrl(path: string, query?: RequestOptions['query']): string {
  const url = path.startsWith('/') ? path : `${API_BASE}/${path}`;
  if (!query) return url;
  const qs = new URLSearchParams();
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined) qs.set(k, String(v));
  }
  const s = qs.toString();
  return s ? `${url}?${s}` : url;
}

export async function apiRequest<T>(path: string, opts: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  const token = opts.anonymous ? null : getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  let body: string | undefined;
  if (opts.body !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(opts.body);
  }

  let res: Response;
  try {
    res = await fetch(buildUrl(path, opts.query), {
      method: opts.method ?? 'GET',
      headers,
      body,
      signal: opts.signal,
    });
  } catch (e) {
    if (e instanceof DOMException && e.name === 'AbortError') throw e;
    throw new ApiError({ code: 'NETWORK', status: 0 });
  }

  const requestIdHeader = res.headers.get('X-Request-Id') ?? undefined;
  const text = await res.text();
  let data: unknown = undefined;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      if (res.ok) throw new ApiError({ code: 'BAD_RESPONSE', status: res.status, requestId: requestIdHeader });
    }
  }

  if (res.ok) return data as T;

  const retryAfter = Number(res.headers.get('Retry-After'));
  const common = {
    status: res.status,
    retryAfterSeconds: Number.isFinite(retryAfter) && retryAfter > 0 ? retryAfter : undefined,
  };
  const err = isApiErrorBody(data)
    ? new ApiError({
        ...common,
        code: toErrorCode(data.code, res.status),
        message: data.message,
        requestId: data.requestId ?? requestIdHeader,
        details: data.details,
      })
    : new ApiError({ ...common, code: toErrorCode('', res.status), requestId: requestIdHeader });

  if (err.code === 'UNAUTHENTICATED' && token) signalUnauthenticated();
  throw err;
}

export const api = {
  get: <T>(path: string, opts?: Omit<RequestOptions, 'method' | 'body'>) => apiRequest<T>(path, { ...opts, method: 'GET' }),
  post: <T>(path: string, body?: unknown, opts?: Omit<RequestOptions, 'method' | 'body'>) =>
    apiRequest<T>(path, { ...opts, method: 'POST', body }),
  put: <T>(path: string, body?: unknown, opts?: Omit<RequestOptions, 'method' | 'body'>) =>
    apiRequest<T>(path, { ...opts, method: 'PUT', body }),
  patch: <T>(path: string, body?: unknown, opts?: Omit<RequestOptions, 'method' | 'body'>) =>
    apiRequest<T>(path, { ...opts, method: 'PATCH', body }),
  delete: <T>(path: string, opts?: Omit<RequestOptions, 'method' | 'body'>) => apiRequest<T>(path, { ...opts, method: 'DELETE' }),
};
