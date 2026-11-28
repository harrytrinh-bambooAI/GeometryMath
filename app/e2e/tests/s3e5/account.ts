import { randomUUID } from 'node:crypto';
import type { APIRequestContext } from '@playwright/test';
import { API, bearer } from '../s3e1/s3e1.ts';

/** Tiện ích E2E của US-S3E5-05 (xóa tài khoản, T-261126-32). */
export const deleteAccount = (rq: APIRequestContext, token: string, userId: string, data: Record<string, unknown>) =>
  rq.post(`${API}/api/v1/account/users/${userId}/delete`, { headers: bearer(token), data: { requestId: randomUUID(), ...data } });

export const LOGIN_MSG = 'Mã hoặc mật khẩu chưa đúng';
