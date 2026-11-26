import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

interface Notice {
  version: string;
  title: string;
  body: string;
}

/** Liên kết tới mục "Xóa tài khoản" ở Cài đặt của vùng hiện tại (/gv hoặc /hs), BR-S3E5-54. */
export function DeleteAccountLink() {
  const { pathname } = useLocation();
  const area = pathname.startsWith('/gv') ? 'gv' : 'hs';
  return (
    <Link className="s3e5-settings-link" to={`/${area}/cai-dat#xoa-tai-khoan`} data-testid={tid('s3e5', 'privacy', 'delete-link')}>
      Cài đặt › Xóa tài khoản
    </Link>
  );
}

/**
 * US-S3E5-05 SC3: xem lại thông báo riêng tư bản hiện hành (MSG-S3-81) bất cứ lúc nào, kể cả khi đã đọc; văn bản nêu
 * chính sách lưu và xóa, kèm liên kết tới Cài đặt "Xóa tài khoản".
 */
export function PrivacyNoticePage() {
  const [notice, setNotice] = useState<Notice | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let live = true;
    api
      .get<Notice>('/api/v1/account/privacy')
      .then((n) => live && setNotice(n))
      .catch((e: unknown) => live && setError(e instanceof ApiError ? e.message : 'Đã có lỗi xảy ra, vui lòng thử lại.'));
    return () => {
      live = false;
    };
  }, []);

  if (error) {
    return (
      <div className="alert" role="alert" data-testid={tid('s3e5', 'notice', 'error')}>
        {error}
      </div>
    );
  }
  if (!notice) return <p className="muted">Đang tải…</p>;
  return (
    <section className="card s3e5-notice" data-testid={tid('s3e5', 'notice')}>
      <h1 className="s3e5-title" data-testid={tid('s3e5', 'notice', 'title')}>
        <Icon name="shield" /> Thông báo riêng tư (bản {notice.version})
      </h1>
      <div className="s3e5-body" data-testid={tid('s3e5', 'notice', 'body')}>
        {notice.body.split('\n').map((p, i) => (
          <p key={i}>{p}</p>
        ))}
      </div>
      <DeleteAccountLink />
    </section>
  );
}
