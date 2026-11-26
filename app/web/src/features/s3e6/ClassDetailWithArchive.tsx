import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router';
import { ApiError } from '../../shared/api/errors.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { ClassDetailPage } from '../s3e1/ClassDetailPage.tsx';
import { MSG, s3e6Api } from './api.ts';

/**
 * US-S3E6-02: "Lưu trữ lớp" (hộp MSG-S3-93) và "Bỏ lưu trữ" trên màn một lớp (màn gốc của S3-E1 giữ nguyên bên dưới).
 * Lưu trữ: lớp chỉ còn xem, mã lớp ngừng dùng, em Chờ duyệt bị từ chối; bỏ lưu trữ: lớp về Đang dạy với mã mới.
 */
export function ClassDetailWithArchive() {
  const { id = '' } = useParams();
  const [cls, setCls] = useState<{ name: string; status: 'active' | 'archived' } | null>(null);
  const [confirm, setConfirm] = useState<'archive' | 'unarchive' | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [version, setVersion] = useState(0);

  const load = useCallback(async () => {
    try {
      const d = await s3e6Api.classDetail(id);
      setCls({ name: d.name, status: d.status });
    } catch {
      setCls(null);
    }
  }, [id]);

  useEffect(() => {
    const t = setTimeout(() => void load(), 0);
    return () => clearTimeout(t);
  }, [load]);

  const run = async (what: 'archive' | 'unarchive') => {
    setError(null);
    try {
      if (what === 'archive') await s3e6Api.archive(id);
      else await s3e6Api.unarchive(id);
      setConfirm(null);
      await load();
      setVersion((v) => v + 1); // vẽ lại màn gốc với trạng thái mới
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Đã có lỗi xảy ra, vui lòng thử lại.');
    }
  };

  return (
    <>
      {cls ? (
        <div className="s3e6-bar" data-testid={tid('s3e6', 'archive-bar')} data-status={cls.status}>
          {cls.status === 'active' ? (
            <button type="button" className="btn ghost" onClick={() => setConfirm('archive')} data-testid={tid('s3e6', 'archive')}>
              <Icon name="save" /> Lưu trữ lớp
            </button>
          ) : (
            <>
              <span className="chip" data-testid={tid('s3e6', 'archived')}>
                <Icon name="lock" /> Đã lưu trữ
              </span>
              <button type="button" className="btn ghost" onClick={() => setConfirm('unarchive')} data-testid={tid('s3e6', 'unarchive')}>
                <Icon name="redo" /> Bỏ lưu trữ
              </button>
            </>
          )}
          {error ? (
            <span className="s3e6-err" role="alert" data-testid={tid('s3e6', 'archive', 'error')}>
              {error}
            </span>
          ) : null}
        </div>
      ) : null}
      <ClassDetailPage key={version} />
      {confirm && cls ? (
        <div className="s3e6-modal" role="dialog" aria-modal="true" aria-labelledby="s3e6-archive-title" data-testid={tid('s3e6', 'archive', 'dialog')}>
          <div className="card s3e6-dialog">
            <h2 id="s3e6-archive-title" className="s3e6-dialog-title">
              {confirm === 'archive' ? MSG.s93(cls.name) : `Bỏ lưu trữ lớp ${cls.name}? Lớp về Đang dạy với mã lớp mới.`}
            </h2>
            <div className="s3e6-row">
              <button type="button" className="btn" onClick={() => void run(confirm)} data-testid={tid('s3e6', 'archive', 'confirm')}>
                {confirm === 'archive' ? 'Lưu trữ' : 'Bỏ lưu trữ'}
              </button>
              <button type="button" className="btn ghost" onClick={() => setConfirm(null)} data-testid={tid('s3e6', 'archive', 'cancel')}>
                Hủy
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}
