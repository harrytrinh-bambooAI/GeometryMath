import { feature as e1 } from './e1/index.tsx';
import { feature as e2 } from './e2/index.tsx';
import { feature as e3 } from './e3/index.tsx';
import { feature as e4 } from './e4/index.tsx';
import { feature as e5 } from './e5/index.tsx';
import { feature as e6 } from './e6/index.tsx';
import { feature as e7 } from './e7/index.tsx';
import { feature as s2e1 } from './s2e1/index.tsx';
import { feature as s2e2 } from './s2e2/index.tsx';
import { feature as s2e3 } from './s2e3/index.tsx';
import { feature as s2e4 } from './s2e4/index.tsx';
import { feature as s2e5 } from './s2e5/index.tsx';
import { feature as s2e6 } from './s2e6/index.tsx';
import { feature as s2e7 } from './s2e7/index.tsx';
import { feature as s2e8 } from './s2e8/index.tsx';
import { feature as s2e9 } from './s2e9/index.tsx';
import { feature as s3e1 } from './s3e1/index.tsx';
import { feature as s3e2 } from './s3e2/index.tsx';
import { feature as s3e3 } from './s3e3/index.tsx';
import { feature as s3e4 } from './s3e4/index.tsx';
import { feature as s3e5 } from './s3e5/index.tsx';
import { feature as s3e6 } from './s3e6/index.tsx';
import type { FeatureModule } from './types.ts';

/**
 * Danh sách epic cắm vào khung. Thứ tự = thứ tự mục trên rail; route cùng đường dẫn: epic sau thay epic trước.
 * Epic không cần sửa tệp này: chặng 2–3 đã cắm sẵn s2e1…s3e6 (T-261126-41), task chỉ sửa `<id>/index.tsx` của mình.
 */
export const features: FeatureModule[] = [
  e1, e2, e3, e4, e5, e6, e7,
  s2e1, s2e2, s2e3, s2e4, s2e5, s2e6, s2e7, s2e8, s2e9,
  s3e1, s3e2, s3e3, s3e4, s3e5, s3e6,
];

export type { FeatureId, FeatureModule, NavItem } from './types.ts';
