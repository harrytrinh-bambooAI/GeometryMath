/**
 * Che bí mật trong bằng chứng (http.json, ảnh bảng yêu cầu/phản hồi): giá trị trường `token`, tiêu đề
 * `Authorization`, và mọi chuỗi dạng JWT / `Bearer ...` / `"token":"..."` còn sót trong văn bản.
 */
export const MASK = '***(đã che)***';
const SECRET_KEYS = /^(token|authorization)$/i;
const JWT = /eyJ[\w-]+\.[\w-]+\.[\w-]+/g;
const BEARER = /Bearer\s+[^\s"',]+/g;
const TOKEN_FIELD = /("token"\s*:\s*")[^"]*(")/gi;
export function redactText(s: string): string {
  return s.replace(BEARER, `Bearer ${MASK}`).replace(JWT, MASK).replace(TOKEN_FIELD, `$1${MASK}$2`);
}
export function redact(v: unknown): unknown {
  if (typeof v === 'string') return redactText(v);
  if (Array.isArray(v)) return v.map(redact);
  if (v && typeof v === 'object') {
    return Object.fromEntries(
      Object.entries(v as Record<string, unknown>).map(([k, x]) => [k, SECRET_KEYS.test(k) && x != null ? MASK : redact(x)]),
    );
  }
  return v;
}
