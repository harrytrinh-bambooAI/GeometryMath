import { existsSync, readFileSync, realpathSync } from 'node:fs';
import { createRequire, registerHooks } from 'node:module';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

/**
 * T-261128-74 · Môi trường cho math-render.spec.ts.
 * 1) Nạp cổng worktree (app/.env.ports) vào biến môi trường TRƯỚC khi nạp các helper e2e (helper đọc
 *    GEO_E2E_BASE_URL / GEO_PORT lúc nạp mô-đun).
 * 2) Chọn đúng bản Playwright của trình chạy. Gọi `npx playwright test e2e/tests/ui/math-render.spec.ts` từ app/ (không có
 *    node_modules) thì trình chạy là bản npx, khác bản app/e2e/node_modules; hai bản không cùng nạp được trong một tiến
 *    trình ("Requiring @playwright/test second time" / "did not expect test() to be called here"). Khi đó móc phân giải
 *    `@playwright/test` của mọi helper về bản của trình chạy. Chạy trong app/e2e thì hai bản là một, không móc gì.
 *    Spec chỉ nạp helper bằng import() động (sau khi móc đã cài).
 */
export const APP_DIR = path.resolve(import.meta.dirname, '../../..');
const ports = path.join(APP_DIR, '.env.ports');
if (existsSync(ports)) {
  for (const line of readFileSync(ports, 'utf8').split('\n')) {
    const m = /^\s*(GEO_[A-Z_]+)=(\S+)/.exec(line);
    if (m && process.env[m[1]!] === undefined) process.env[m[1]!] = m[2]!;
  }
}
process.env.GEO_E2E_BASE_URL ??= `http://127.0.0.1:${process.env.GEO_PORT ?? 8080}`;
process.env.GEO_E2E_WEB_URL ??= `http://127.0.0.1:${process.env.GEO_WEB_PORT ?? 5173}`;
export const API_URL = process.env.GEO_E2E_BASE_URL;
export const WEB_URL = process.env.GEO_E2E_WEB_URL;

const req = createRequire(import.meta.url);
const localTest = req.resolve('@playwright/test');

/** Thư mục gói `playwright` chứa tệp đang chạy (CLI hoặc tiến trình worker), hoặc null. */
function runnerPackage(): string | null {
  try {
    let d = path.dirname(realpathSync(process.argv[1] ?? ''));
    while (d !== path.dirname(d)) {
      const pj = path.join(d, 'package.json');
      if (existsSync(pj) && (JSON.parse(readFileSync(pj, 'utf8')) as { name?: string }).name === 'playwright') return d;
      d = path.dirname(d);
    }
  } catch {
    /* không xác định được: dùng bản cục bộ */
  }
  return null;
}
const runner = runnerPackage();
const localPkg = path.dirname(realpathSync(req.resolve('playwright/package.json')));
const testJs = runner && realpathSync(runner) !== localPkg ? path.join(runner, 'test.js') : localTest;
if (testJs !== localTest) {
  const cjs = pathToFileURL(testJs).href;
  const esm = pathToFileURL(testJs.replace(/\.js$/, '.mjs')).href;
  registerHooks({
    resolve(specifier, context, next) {
      if (specifier !== '@playwright/test' && specifier !== 'playwright/test') return next(specifier, context);
      return context.conditions?.includes('require')
        ? { url: cjs, format: 'commonjs', shortCircuit: true }
        : { url: esm, format: 'module', shortCircuit: true };
    },
  });
}
export const pw = req(testJs) as typeof import('@playwright/test');
