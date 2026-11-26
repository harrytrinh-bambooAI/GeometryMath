import { SettingsPage } from '../settings/SettingsPage.tsx';
import { DeleteAccountCard } from './DeleteAccountCard.tsx';

/**
 * US-S3E5-05 (BR-S3E5-50): màn Cài đặt chung (chọn giao diện, T-261126-50) giữ nguyên, thêm mục "Xóa tài khoản" ở
 * dưới. Route `cai-dat` của feature đứng trước route mặc định của khung nên thay nó.
 */
export function SettingsWithDelete({ area }: { area: 'gv' | 'hs' }) {
  return (
    <>
      <SettingsPage area={area} />
      <DeleteAccountCard />
    </>
  );
}
