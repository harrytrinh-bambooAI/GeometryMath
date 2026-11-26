import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router';
import { tid } from '../../shared/testid.ts';
import { messageOf, s3e1Api, type ClassDetail } from './api.ts';
import { qrMatrix } from './qr.ts';

/** Trang in A4 dán lên bảng (US-S3E1-01-SC1, DQ-24): tên lớp, mã QR, mã lớp cỡ lớn, bốn bước vào lớp. */
export function PrintPage() {
  const { id = '' } = useParams();
  const [c, setC] = useState<ClassDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    s3e1Api.detail(id).then(setC, (e: unknown) => setError(messageOf(e)));
  }, [id]);
  const link = c ? `${window.location.origin}/hs/lop-cua-em?ma=${encodeURIComponent(c.code)}` : '';
  const qr = useMemo(() => (link ? qrMatrix(link) : null), [link]);
  if (error) return <div className="alert" role="alert">{error}</div>;
  if (!c || !qr) return <p className="muted">Đang tải…</p>;
  const n = qr.length + 8;
  return (
    <article className="s3e1-print" data-testid={tid('s3e1', 'print-page')}>
      <h1 className="s3e1-print-title" data-testid={tid('s3e1', 'print-page', 'name')}>Lớp {c.name}</h1>
      <svg className="s3e1-qr" viewBox={`0 0 ${n} ${n}`} role="img" aria-label={`Mã QR vào lớp ${c.name}`} data-testid={tid('s3e1', 'print-page', 'qr')}>
        <rect width={n} height={n} className="s3e1-qr-bg" />
        {qr.flatMap((row, y) => row.map((dark, x) => (dark ? <rect key={`${x}-${y}`} x={x + 4} y={y + 4} width={1} height={1} /> : null)))}
      </svg>
      <div className="s3e1-print-code" data-testid={tid('s3e1', 'print-page', 'code')}>{c.code}</div>
      <ol className="s3e1-print-steps" data-testid={tid('s3e1', 'print-page', 'steps')}>
        <li>Mở ứng dụng, đăng nhập bằng mã người dùng của em (chưa có thì bấm Đăng ký).</li>
        <li>Chọn “Lớp của em” rồi “Vào lớp”.</li>
        <li>Gõ mã lớp {c.code} (hoặc quét mã QR) rồi bấm “Vào lớp”.</li>
        <li>Chờ thầy cô duyệt; trong lúc chờ em vẫn dùng Tự học.</li>
      </ol>
      <button type="button" className="btn primary s3e1-noprint" onClick={() => window.print()} data-testid={tid('s3e1', 'print-page', 'print')}>
        In trang này
      </button>
    </article>
  );
}
