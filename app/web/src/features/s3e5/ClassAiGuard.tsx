import { useEffect, useState, type ReactNode } from 'react';
import { useParams } from 'react-router';
import { api } from '../../shared/api/client.ts';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';

export const MSG_83 = 'Lớp này đã tắt trợ lý AI. Em vẫn làm bài và xem gợi ý có sẵn.';

/**
 * US-S3E5-04-SC1: bài thuộc lớp đã tắt trợ lý AI thì ẩn khung Hỏi đáp của màn bài (giữ nguyên phần còn lại) và hiện
 * MSG-S3-83. Máy chủ vẫn chặn mọi lượt hỏi (ClassAiSwitchFilter), không dựa vào giao diện.
 */
export function ClassAiGuard({ children }: { children: ReactNode }) {
  const { id = '' } = useParams();
  const [off, setOff] = useState(false);
  useEffect(() => {
    let live = true;
    api.get<{ aiEnabled: boolean }>(`/api/v1/student/class-ai/${encodeURIComponent(id)}`).then(
      (r) => live && setOff(!r.aiEnabled),
      () => live && setOff(false),
    );
    return () => {
      live = false;
    };
  }, [id]);
  return (
    <div className={off ? 's3e5-ai-off' : undefined} data-testid={tid('s3e5', 'ai-guard')} data-ai={off ? 'off' : 'on'}>
      {off ? (
        <p className="alert s3e5-ai-msg" role="status" data-testid={tid('s3e5', 'ai-off')}>
          <Icon name="info" inline /> {MSG_83}
        </p>
      ) : null}
      {children}
    </div>
  );
}
