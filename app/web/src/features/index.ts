import { feature as e1 } from './e1/index.tsx';
import { feature as e2 } from './e2/index.tsx';
import { feature as e3 } from './e3/index.tsx';
import { feature as e4 } from './e4/index.tsx';
import { feature as e5 } from './e5/index.tsx';
import { feature as e6 } from './e6/index.tsx';
import { feature as e7 } from './e7/index.tsx';
import type { FeatureModule } from './types.ts';

/** Danh sách epic cắm vào khung. Thứ tự = thứ tự mục trên rail. Epic không cần sửa tệp này. */
export const features: FeatureModule[] = [e1, e2, e3, e4, e5, e6, e7];

export type { FeatureModule, NavItem } from './types.ts';
