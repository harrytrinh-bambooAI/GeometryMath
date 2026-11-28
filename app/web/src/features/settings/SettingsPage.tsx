import { useEffect, useState } from 'react';
import { tid } from '../../shared/testid.ts';
import { THEME_EVENT, currentTheme, saveTheme } from '../../shared/theme/theme.ts';
import type { SaveResult } from '../../shared/theme/theme.ts';
import { THEMES } from '../../shared/theme/themes.ts';
import type { ThemeId } from '../../shared/theme/themes.ts';
import './settings.css';
import { MathText } from '../../shared/math/MathText.tsx';

/**
 * Màn Cài đặt (T-261126-50), chung cho giáo viên (/gv/cai-dat) và học sinh (/hs/cai-dat): chọn giao diện.
 * Mỗi ô xem trước tự mang `data-theme` của nó nên màu, font, bo góc lấy đúng token của theme đó.
 */
export function SettingsPage({ area }: { area: 'gv' | 'hs' }) {
  const [theme, setTheme] = useState<ThemeId>(currentTheme);
  const [saved, setSaved] = useState<SaveResult | null>(null);

  useEffect(() => {
    // theme của tài khoản về sau khi mở màn (đồng bộ ở khung) -> cập nhật ô đang chọn
    const on = () => setTheme(currentTheme());
    window.addEventListener(THEME_EVENT, on);
    return () => window.removeEventListener(THEME_EVENT, on);
  }, []);

  const choose = async (id: ThemeId) => {
    setTheme(id);
    setSaved(null);
    setSaved(await saveTheme(id));
  };

  return (
    <div className="card settings" data-testid={tid(area, 'settings')}>
      <h3>Giao diện</h3>
      <p className="muted small">Chọn kiểu hiển thị. Lựa chọn được lưu theo tài khoản của bạn.</p>
      <div className="theme-grid" role="radiogroup" aria-label="Giao diện" data-testid={tid('settings', 'theme')}>
        {THEMES.map((t) => (
          <label key={t.id} className="theme-opt" data-theme={t.id} data-testid={tid('settings', 'theme', t.id)}>
            <input
              type="radio"
              name="theme"
              value={t.id}
              checked={theme === t.id}
              onChange={() => void choose(t.id)}
              data-testid={tid('settings', 'theme', t.id, 'radio')}
            />
            <span className="theme-preview" aria-hidden="true">
              <span className="tp-bar" />
              <span className="tp-card">
                <span className="tp-line" />
                <span className="tp-btn">Aa</span>
              </span>
            </span>
            <span className="theme-name">{t.name}</span>
            <span className="theme-sum"><MathText text={t.summary} /></span>
          </label>
        ))}
      </div>
      <p className="small muted" role="status" data-testid={tid('settings', 'theme', 'status')}>
        {saved === 'account' ? 'Đã lưu theo tài khoản.' : saved === 'local' ? 'Đã lưu trên thiết bị này.' : ''}
      </p>
    </div>
  );
}
