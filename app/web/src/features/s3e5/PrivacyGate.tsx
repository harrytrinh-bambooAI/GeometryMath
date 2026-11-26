import { useCallback, useEffect, useState, type ReactNode } from 'react';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { buildUrl } from '../../shared/api/url.ts';
import { getToken } from '../../shared/auth/session.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

interface Notice {
  version: string;
  title: string;
  body: string;
  ackedVersion: string | null;
  required: boolean;
}

/**
 * US-S3E5-02: tài khoản có mã chưa đọc bản hiện hành của thông báo riêng tư thấy màn MSG-S3-81 trước khi vào trang
 * đầu (Bài của em / Kho đề); bấm "Đã đọc" thì máy chủ ghi cờ cùng phiên bản do máy chủ quyết.
 */
export function PrivacyGate({ children }: { children: ReactNode }) {
  const [notice, setNotice] = useState<Notice | null>(null);
  const [state, setState] = useState<'loading' | 'gate' | 'open'>('loading');
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      // gọi thẳng (không qua api.get): thẻ hết hạn ở đây KHÔNG phát tín hiệu đăng xuất, để trang con tự gặp 401
      const token = getToken();
      if (!token) throw new Error('chưa đăng nhập');
      const res = await fetch(buildUrl('/api/v1/account/privacy'), { headers: { Accept: 'application/json', Authorization: `Bearer ${token}` } });
      if (!res.ok) throw new Error(String(res.status));
      const n = (await res.json()) as Notice;
      setNotice(n);
      setState(n.required ? 'gate' : 'open');
    } catch {
      setState('open'); // tài khoản MVP không có mã / chưa đăng nhập: trang con tự xử lý
    }
  }, []);
  useEffect(() => {
    const t = setTimeout(() => void load(), 0);
    return () => clearTimeout(t);
  }, [load]);

  const ack = async () => {
    try {
      await api.post('/api/v1/account/privacy-ack', { version: notice?.version });
      setState('open');
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Đã có lỗi xảy ra, vui lòng thử lại.');
      await load();
    }
  };

  if (state === 'open' || !notice) return <>{children}</>;
  return (
    <section className="card s3e5-notice" data-testid={tid('s3e5', 'privacy')}>
      <h1 className="s3e5-title" data-testid={tid('s3e5', 'privacy', 'title')}>
        <Icon name="shield" /> Thông báo riêng tư (bản {notice.version})
      </h1>
      <div className="s3e5-body" data-testid={tid('s3e5', 'privacy', 'body')}>
        {notice.body.split('\n').map((p, i) => (
          <p key={i}>{p}</p>
        ))}
      </div>
      {error ? <div className="alert" role="alert">{error}</div> : null}
      <button type="button" className="btn primary s3e5-ack" onClick={() => void ack()} data-testid={tid('s3e5', 'privacy', 'ack')}>
        <Icon name="check" /> Đã đọc
      </button>
    </section>
  );
}
