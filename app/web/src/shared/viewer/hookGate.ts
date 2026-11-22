/** Cổng bật móc `window.__geoScene` (PO-52): dev hoặc bản build có VITE_GEO_TEST_HOOKS=1; production thì không. */
export interface HookEnv {
  DEV?: boolean;
  VITE_GEO_TEST_HOOKS?: string;
}
export const sceneHookEnabled = (env: HookEnv): boolean => env.DEV === true || env.VITE_GEO_TEST_HOOKS === '1';

