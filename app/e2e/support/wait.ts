import type { Page, Response } from '@playwright/test';
import { expect } from '@playwright/test';

/**
 * Chờ theo phản hồi API thay vì theo thời gian (T-261126-43). Máy tải cao làm một lượt POST + GET làm mới mất hơn 5 s
 * (thời gian chờ mặc định của expect) nên `click(); expect(...).toBeVisible()` đỏ giả. Cách dùng: đăng ký chờ TRƯỚC
 * thao tác, rồi kiểm mã phản hồi (lỗi thật vẫn đỏ, kèm thân phản hồi để chẩn đoán). Thời hạn chờ = thời hạn của test.
 */
export interface ApiMatch {
  method?: string;
  /** khớp với pathname (không gồm query) */
  path: RegExp;
}

const matches = (r: Response, m: ApiMatch) =>
  (!m.method || r.request().method() === m.method) && m.path.test(new URL(r.url()).pathname);

/**
 * Làm `action`, chờ lần lượt các phản hồi `steps` (bước sau chỉ tính khi bước trước đã tới, ví dụ POST gợi ý rồi GET
 * làm mới), kiểm từng phản hồi có mã 2xx. Trả về các phản hồi theo thứ tự.
 */
export async function untilApi(page: Page, steps: ApiMatch[], action: () => Promise<unknown>): Promise<Response[]> {
  let seen = 0;
  const got: Response[] = [];
  let done!: () => void;
  const all = new Promise<void>((resolve) => (done = resolve));
  const onResponse = (r: Response) => {
    const step = steps[seen];
    if (step && matches(r, step)) {
      got.push(r);
      seen += 1;
      // bước lỗi thì không chờ bước sau (sẽ không tới): dừng ngay để kiểm mã bên dưới báo đỏ kèm thân phản hồi
      if (seen === steps.length || r.status() >= 300) done();
    }
  };
  page.on('response', onResponse);
  try {
    await action();
    await all;
  } finally {
    page.off('response', onResponse);
  }
  for (const [i, r] of got.entries()) {
    expect(r.status(), `${steps[i]!.method ?? ''} ${r.url()}: ${await r.text().catch(() => '')}`).toBeLessThan(300);
  }
  return got;
}

const enc = encodeURIComponent;
/** Đường API học sinh của một bài (khớp chính xác pathname). */
export const studentApi = (id: string, tail: string) => new RegExp(`^/api/v1/student/assignments/${enc(id)}${tail}$`);

/** Bấm "Xem gợi ý n": chờ POST gợi ý mức n rồi GET /help làm mới (HintTab) — sau đó DOM đã có gợi ý mới. */
export async function clickHintNext(page: Page, id: string, level: number): Promise<void> {
  await untilApi(
    page,
    [
      { method: 'POST', path: studentApi(id, `/hints/${level}`) },
      { method: 'GET', path: studentApi(id, '/help') },
    ],
    () => page.getByTestId('e4-hint-next').click(),
  );
}
