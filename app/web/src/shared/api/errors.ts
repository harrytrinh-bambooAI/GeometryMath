/**
 * Lỗi phía máy khách, khớp mô hình lỗi thống nhất của máy chủ
 * (`server/.../common/error/ApiError.java`: { code, message, requestId, details[] }, mã ở `ErrorCode.java`).
 */
export type ServerErrorCode =
  | 'VALIDATION_FAILED'
  | 'INVALID_CURSOR'
  | 'UNAUTHENTICATED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'METHOD_NOT_ALLOWED'
  | 'CONFLICT'
  | 'PAYLOAD_TOO_LARGE'
  | 'UNSUPPORTED_MEDIA_TYPE'
  | 'RATE_LIMITED'
  | 'DEPENDENCY_UNAVAILABLE'
  | 'SERVICE_OVERLOADED'
  | 'TIMEOUT'
  | 'INTERNAL';

/** Mã chỉ có ở máy khách: không gọi được máy chủ, hoặc thân lỗi không đúng định dạng. */
export type ClientErrorCode = 'NETWORK' | 'BAD_RESPONSE';

export type ErrorCode = ServerErrorCode | ClientErrorCode;

export interface FieldIssue {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  code: string;
  message: string;
  requestId?: string;
  details?: FieldIssue[];
}

/** Câu tiếng Việt dự phòng khi máy chủ không gửi message (giữ đồng bộ với ErrorCode.java). */
export const FALLBACK_MESSAGES: Record<ErrorCode, string> = {
  VALIDATION_FAILED: 'Dữ liệu gửi lên không hợp lệ.',
  INVALID_CURSOR: 'Con trỏ phân trang không hợp lệ.',
  UNAUTHENTICATED: 'Bạn cần đăng nhập để tiếp tục.',
  FORBIDDEN: 'Bạn không có quyền thực hiện thao tác này.',
  NOT_FOUND: 'Không tìm thấy dữ liệu yêu cầu.',
  METHOD_NOT_ALLOWED: 'Phương thức không được hỗ trợ.',
  CONFLICT: 'Thao tác xung đột với trạng thái hiện tại của dữ liệu.',
  PAYLOAD_TOO_LARGE: 'Dữ liệu gửi lên quá lớn.',
  UNSUPPORTED_MEDIA_TYPE: 'Định dạng dữ liệu không được hỗ trợ.',
  RATE_LIMITED: 'Bạn thao tác quá nhanh, vui lòng thử lại sau.',
  DEPENDENCY_UNAVAILABLE: 'Dịch vụ phụ trợ tạm thời chưa sẵn sàng, vui lòng thử lại.',
  SERVICE_OVERLOADED: 'Hệ thống đang quá tải, vui lòng thử lại sau ít giây.',
  TIMEOUT: 'Yêu cầu xử lý quá thời gian cho phép.',
  INTERNAL: 'Hệ thống gặp lỗi, vui lòng thử lại sau.',
  NETWORK: 'Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.',
  BAD_RESPONSE: 'Máy chủ trả dữ liệu không đúng định dạng.',
};

export class ApiError extends Error {
  readonly code: ErrorCode;
  readonly status: number;
  readonly requestId: string | undefined;
  readonly details: FieldIssue[];
  /** Số giây chờ trước khi thử lại (tiêu đề Retry-After, có ở 429/503). */
  readonly retryAfterSeconds: number | undefined;

  constructor(init: {
    code: ErrorCode;
    status: number;
    message?: string;
    requestId?: string;
    details?: FieldIssue[];
    retryAfterSeconds?: number;
  }) {
    super(init.message || FALLBACK_MESSAGES[init.code]);
    this.name = 'ApiError';
    this.code = init.code;
    this.status = init.status;
    this.requestId = init.requestId;
    this.details = init.details ?? [];
    this.retryAfterSeconds = init.retryAfterSeconds;
  }

  /** Có nên cho người dùng bấm "Thử lại" không. */
  get retryable(): boolean {
    return (
      this.code === 'RATE_LIMITED' ||
      this.code === 'SERVICE_OVERLOADED' ||
      this.code === 'DEPENDENCY_UNAVAILABLE' ||
      this.code === 'TIMEOUT' ||
      this.code === 'NETWORK'
    );
  }
}

const KNOWN = new Set<string>(Object.keys(FALLBACK_MESSAGES));

export function isApiErrorBody(v: unknown): v is ApiErrorBody {
  return (
    typeof v === 'object' &&
    v !== null &&
    typeof (v as { code?: unknown }).code === 'string' &&
    typeof (v as { message?: unknown }).message === 'string'
  );
}

export function toErrorCode(raw: string, status: number): ErrorCode {
  if (KNOWN.has(raw)) return raw as ErrorCode;
  if (status === 401) return 'UNAUTHENTICATED';
  if (status === 403) return 'FORBIDDEN';
  if (status === 404) return 'NOT_FOUND';
  return status >= 500 ? 'INTERNAL' : 'VALIDATION_FAILED';
}
