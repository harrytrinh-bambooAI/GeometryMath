#!/usr/bin/env node
/**
 * make e2e-affected (T-261126-45; DECISIONS 26/11 "Chiến lược regression theo mức ảnh hưởng"; epic-common v1.5).
 * Đo mức ảnh hưởng từ diff so với BASE (mặc định worktree-mvp-wave0) theo bản đồ dữ liệu
 * artifacts/GEO/work/s23/impact-map.json, rồi chạy scripts/e2e.sh MỘT lượt với đúng tập spec:
 *   L0  spec của epic sở hữu tệp bị đụng + smoke (@smoke)          trên api + ui-chromium
 *   L1  L0 + spec các epic phụ thuộc (deps của luật)              trên api + ui-chromium
 *   L2  trọn bộ                                                   trên api + ui-chromium
 *   LA  project api trọn bộ, KHÔNG UI (T-261128-69, DECISIONS 28/11 "E2E cách lai"): tệp dùng chung (allowed_fields.tsv,
 *       sql-lint.properties, application*.yml, AnthropicPrompts/MockAiProvider, Makefile, README) đổi dữ liệu/cấu hình chứ
 *       không đổi màn hình. Diff có cả tệp LA lẫn L0/L1: mức theo L0/L1 + thêm api trọn bộ (apiFull). L2 vẫn thắng.
 *   --ui-own (mặc định BẬT khi mức ≤ L1; --no-ui-own để tắt): UI (ui-chromium) chỉ chạy spec *.ui.spec.ts của epic SỞ HỮU
 *     tệp đổi; spec các epic phụ thuộc (deps) chỉ chạy ở project api. Mobile P0 giữ như cũ. Trọn bộ mọi project: make e2e-full.
 *   Diff CHỈ app/server/src/main/** (mã, tài nguyên, migration) (E2E-SPLIT, DECISIONS 27/11 "Chiến lược E2E"): api + ca UI
 *     loại A của màn/epic liên quan trên ui-chromium; KHÔNG chạy mobile-*; ca UI gắn @loai-B (nếu còn) bị loại bằng
 *     --grep-invert. Ca loại B đã chuyển sang `*.flow.api.spec.ts` (project api). Diff đụng app/web/** thì chạy UI như
 *     trên; gate (make e2e trọn bộ) không đổi.
 *   L0/L1 thêm: spec P0 điện thoại (MOBILE_P0, *.p0m.ui.spec.ts) của màn bị đụng trên mobile-pixel / mobile-iphone.
 * Chọn test bằng một biểu thức -g của Playwright (khớp "<project> <tệp> <tiêu đề> <tag>"), nên không cần --project.
 *
 * Cách dùng (từ app/):
 *   node scripts/e2e-affected.mjs [--dry-run] [--ui-own|--no-ui-own] [--base <ref>] [--files a,b,…] [--map <json>] [-- <tham số Playwright thêm>]
 *   node scripts/e2e-affected.mjs --selftest          # 4 diff mẫu trong mục "selftest" của bản đồ (dry-run, exit 1 nếu lệch)
 *   BASE=<ref> thay cho --base. Diff = (merge-base BASE HEAD) .. cây làm việc + tệp chưa theo dõi.
 * Kết quả: in mức, lý do, danh sách spec; lượt thật ghi e2e/evidence/<run>/impact.json.
 */
import { execFileSync, spawnSync } from 'node:child_process';
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync, mkdirSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const APP = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const TESTS = path.join(APP, 'e2e', 'tests');
const FEATURES = path.join(APP, 'web', 'src', 'features');
const SERVER_ONLY = /^app\/server\/src\/main\/(java|resources)\//;
const B_TAG = '@loai-B';
// E2E-SPLIT2: ca B đã chuyển (*.flow.api.spec.ts, project api) cũng gắn @loai-B để truy vết; --grep-invert chỉ loại ca
// @loai-B còn ở project UI/mobile (danh sách để sau), KHÔNG loại ca B chạy bằng API.
const B_UI_INVERT = `^(ui|mobile)-[a-z]+ .*${B_TAG}\\b`;
const RANK = { none: 0, L0: 1, L1: 2, L2: 3 }; // LA không nằm trong thang: xem plan()
const DESKTOP = '(api|ui-chromium)';
const MOBILE = 'mobile-(pixel|iphone)';
// Bản sao quy ước MOBILE_P0 của playwright.config.ts (chỉ để IN danh sách; việc chọn do project mobile-* tự lọc).
const MOBILE_P0 = [
  /(e1\/student-join|e2\/labels|e4\/(hints|solution|qa)|e5\/(catalog|detail)|e6\/(assign|release)|e7\/monitor)\.ui\.spec\.ts$/,
  /\.p0m\.ui\.spec\.ts$/,
];

function parseArgs(argv) {
  const o = { dryRun: false, selftest: false, base: process.env.BASE || 'worktree-mvp-wave0', files: null, map: null, uiOwn: null, pw: [] };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--') { o.pw.push(...argv.slice(i + 1)); break; }
    else if (a === '--dry-run') o.dryRun = true;
    else if (a === '--selftest') o.selftest = true;
    else if (a === '--ui-own') o.uiOwn = true;
    else if (a === '--no-ui-own') o.uiOwn = false;
    else if (a === '--base') o.base = argv[++i];
    else if (a === '--files') o.files = argv[++i].split(',').map((s) => s.trim()).filter(Boolean);
    else if (a === '--map') o.map = argv[++i];
    else o.pw.push(a);
  }
  return o;
}

const git = (...args) => execFileSync('git', args, { cwd: APP, encoding: 'utf8' }).trim();
const repoRoot = () => git('rev-parse', '--show-toplevel');

function changedFiles(base) {
  try { git('rev-parse', '--verify', '--quiet', `${base}^{commit}`); } catch {
    throw new Error(`không thấy BASE '${base}' (đặt --base hoặc BASE=…)`);
  }
  const mb = git('merge-base', base, 'HEAD');
  const root = repoRoot();
  const run = (...a) => execFileSync('git', a, { cwd: root, encoding: 'utf8' }).split('\n').filter(Boolean);
  const files = new Set([...run('diff', '--name-only', mb), ...run('ls-files', '--others', '--exclude-standard')]);
  return [...files].sort();
}

const epicOf = (raw) => {
  const m = /^(?:(s[235])e(\d+)|e(\d+))$/i.exec(raw);
  if (!m) return raw.toUpperCase();
  return m[1] ? `${m[1].toUpperCase()}-E${m[2]}` : `E${m[3]}`;
};
function specDirOf(epic, map) {
  if (map.specDirs && epic in map.specDirs) return map.specDirs[epic];
  const m = /^(?:(S[235])-)?E(\d+)$/.exec(epic);
  return m ? `${m[1] ? m[1].toLowerCase() : ''}e${m[2]}` : epic.toLowerCase();
}

let importCache = null;
/** Feature web (thư mục features/<id>) đang import app/web/src/<rel> -> danh sách epic. */
function importers(rel) {
  importCache ??= new Map();
  if (importCache.has(rel)) return importCache.get(rel);
  const re = new RegExp(`from '(\\.\\./)+${rel.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}(/|\\.ts|')`);
  const out = [];
  for (const id of existsSync(FEATURES) ? readdirSync(FEATURES) : []) {
    const dir = path.join(FEATURES, id);
    if (!statSync(dir).isDirectory()) continue;
    if (walk(dir).some((f) => /\.(tsx?|jsx?)$/.test(f) && re.test(readFileSync(f, 'utf8')))) {
      out.push(id === 'settings' ? 'THEME' : epicOf(id));
    }
  }
  importCache.set(rel, out);
  return out;
}
function walk(dir) {
  const out = [];
  for (const e of readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) out.push(...walk(p)); else out.push(p);
  }
  return out;
}

const sub = (s, m) => s.replace(/\$(\d)/g, (_, n) => m[Number(n)] ?? '');

/** Một tệp -> { file, level, epic, deps, reason }. Không khớp luật nào = L2. */
function classify(file, map) {
  for (const r of map.rules) {
    const m = new RegExp(r.match).exec(file);
    if (!m) continue;
    let level = r.level, epic = r.epic ? epicOf(sub(r.epic, m)) : null, deps = r.deps ?? [];
    let reason = `luật /${r.match}/${r.note ? ` (${r.note})` : ''}`;
    if (r.ranges) {
      const v = Number(m[1]);
      const hit = r.ranges.find((x) => v >= x.from && v <= x.to);
      if (!hit) return { file, level: 'L2', epic: null, deps: [], reason: `migration V${v} ngoài khoảng của epic nào (PLAN §4)` };
      epic = hit.epic; deps = hit.deps ?? []; reason = `migration V${v} thuộc khoảng V${hit.from}–V${hit.to} (${hit.epic})`;
    }
    const exp = [];
    for (const d of deps) {
      const dd = sub(d, m);
      if (dd.startsWith('@imports:')) exp.push(...importers(dd.slice('@imports:'.length)));
      else exp.push(dd);
    }
    return { file, level, epic, deps: [...new Set(exp)].filter((d) => d !== epic), reason };
  }
  return { file, level: 'L2', epic: null, deps: [], reason: 'không có trong bản đồ ảnh hưởng -> L2' };
}

function specFiles(rel) {
  const dir = path.join(TESTS, rel);
  if (rel === '') return readdirSync(TESTS).filter((f) => f.endsWith('.spec.ts'));
  if (!existsSync(dir)) return [];
  return walk(dir).filter((f) => f.endsWith('.spec.ts')).map((f) => path.relative(TESTS, f).split(path.sep).join('/'));
}

function plan(files, map, uiOwnOpt = null) {
  const items = files.map((f) => classify(f, map));
  const apiFull = items.some((x) => x.level === 'LA');
  const level = items.filter((x) => x.level !== 'LA').reduce((acc, x) => (RANK[x.level] > RANK[acc] ? x.level : acc), 'none');
  const laOnly = apiFull && level === 'none';
  const eff = laOnly ? 'LA' : level === 'none' ? 'L0' : level;
  const uiOwn = eff === 'L2' || eff === 'LA' ? false : (uiOwnOpt ?? true);
  const owners = [...new Set(items.filter((x) => x.level !== 'none' && x.epic).map((x) => x.epic))];
  const deps = eff === 'L1' ? [...new Set(items.filter((x) => x.level === 'L1').flatMap((x) => x.deps))].filter((d) => !owners.includes(d)) : [];
  const epics = [...owners, ...deps];
  const serverOnly = level !== 'none' && level !== 'L2' && items.every((x) => x.level === 'none' || SERVER_ONLY.test(x.file));
  const res = { level: eff, apiFull, uiOwn, uiDirs: [], serverOnly, grepInvert: serverOnly ? B_UI_INVERT : '', base: null, files: items, owners, deps, epics, specDirs: [], missingSpecs: [], specs: [], mobileSpecs: [], grep: '' };
  if (eff === 'LA') {
    res.serverOnly = false; res.grepInvert = '';
    res.specs = ['(trọn bộ project api, không UI)'];
    res.grep = '^api ';
    return res;
  }
  if (eff === 'L2') {
    res.specs = ['(trọn bộ api + ui-chromium)'];
    res.grep = `^${DESKTOP} `;
    return res;
  }
  const dirOf = (e) => specDirOf(e, map);
  const withDir = epics.filter((e) => dirOf(e) === '' || existsSync(path.join(TESTS, dirOf(e))));
  res.missingSpecs = epics.filter((e) => !withDir.includes(e));
  res.specDirs = withDir.map(dirOf);
  res.specs = withDir.flatMap((e) => specFiles(dirOf(e)).filter((f) => /\.(api|ui)\.spec\.ts$/.test(f)));
  const ownerDirs = owners.filter((e) => withDir.includes(e)).map(dirOf);
  res.mobileSpecs = serverOnly ? [] : ownerDirs.flatMap((d) => specFiles(d)).filter((f) => MOBILE_P0.some((re) => re.test(f)));
  const esc = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const named = res.specDirs.filter((d) => d !== '').map(esc);
  const alts = [`^${DESKTOP} .*@smoke\\b`];
  if (apiFull) alts.push('^api ');
  if (uiOwn) {
    // --ui-own: api cho owners + deps; ui-chromium chỉ cho owners
    res.uiDirs = ownerDirs;
    const ownNamed = ownerDirs.filter((d) => d !== '').map(esc);
    if (named.length) alts.push(`^api (${named.join('|')})/`);
    if (res.specDirs.includes('')) alts.push('^api [^/ ]+\\.spec\\.ts ');
    if (ownNamed.length) alts.push(`^ui-chromium (${ownNamed.join('|')})/`);
    if (ownerDirs.includes('')) alts.push('^ui-chromium [^/ ]+\\.spec\\.ts ');
  } else {
    res.uiDirs = res.specDirs;
    if (named.length) alts.push(`^${DESKTOP} (${named.join('|')})/`);
    if (res.specDirs.includes('')) alts.push(`^${DESKTOP} [^/ ]+\\.spec\\.ts `);
  }
  const mob = ownerDirs.filter((d) => d !== '').map(esc);
  if (!serverOnly && res.mobileSpecs.length && mob.length) alts.push(`^${MOBILE} (${mob.join('|')})/`);
  res.grep = alts.join('|');
  return res;
}

function printPlan(p, out = console.log) {
  out(`Mức: ${p.level}${p.base ? ` (diff so với ${p.base})` : ''} · ${p.files.length} tệp đổi`);
  const shown = p.files.filter((x) => x.level !== 'none');
  for (const x of shown.slice(0, 40)) out(`  ${x.level.padEnd(4)} ${x.file}${x.epic ? ` -> ${x.epic}` : ''}${x.deps.length ? ` (+${x.deps.join(', ')})` : ''} · ${x.reason}`);
  if (shown.length > 40) out(`  … ${shown.length - 40} tệp nữa`);
  const nNone = p.files.length - shown.length;
  if (nNone) out(`  ${nNone} tệp mức none (tài liệu/test đơn vị/công cụ: chỉ smoke)`);
  if (p.level !== 'L2' && p.level !== 'LA') {
    out(`Epic: ${p.epics.join(', ') || '(không)'}${p.missingSpecs.length ? ` · chưa có spec: ${p.missingSpecs.join(', ')}` : ''}`);
    out(`Spec api${p.uiOwn ? '' : ' + ui-chromium'} (${p.specs.length} tệp) + mọi test @smoke${p.apiFull ? ' + api trọn bộ (có tệp dùng chung LA)' : ''}:`);
    for (const s of p.specs) out(`  ${s}`);
    out(p.uiOwn ? `--ui-own: UI ui-chromium chỉ epic sở hữu: ${p.uiDirs.map((d) => d || '(gốc)').join(', ') || '(không)'} (spec epic phụ thuộc chỉ chạy ở api)` : '--no-ui-own: UI chạy cả epic phụ thuộc');
    out(`Spec điện thoại mobile-pixel/mobile-iphone (${p.mobileSpecs.length} tệp): ${p.mobileSpecs.join(', ') || '(không)'}`);
  } else if (p.level === 'LA') {
    out('Mức LA: tệp dùng chung -> project api trọn bộ, KHÔNG UI, không mobile (UI: make e2e-full lượt định kỳ)');
  } else {
    out('Spec: trọn bộ api + ui-chromium (smoke nằm trong đó)');
  }
  out(`Playwright -g: ${p.grep}${p.grepInvert ? ` · --grep-invert ${p.grepInvert}` : ''}${p.serverOnly ? ' · diff chỉ server: api + ca UI loại A, không mobile' : ''}`);
}

function selftest(map) {  // ca của bản đồ chạy với --ui-own mặc định (bật khi ≤ L1)
  let bad = 0;
  for (const c of map.selftest ?? []) {
    const p = plan(c.files, map, null);
    const errs = [];
    if (p.level !== c.level) errs.push(`mức ${p.level} ≠ ${c.level}`);
    const same = (a, b) => a.length === b.length && a.every((x) => b.includes(x));
    if (c.epicsExact && !same(p.epics, c.epicsExact)) errs.push(`epic [${p.epics}] ≠ [${c.epicsExact}]`);
    for (const e of c.epicsInclude ?? []) if (!p.epics.includes(e)) errs.push(`thiếu epic ${e}`);
    for (const d of c.specDirsInclude ?? []) if (!p.specDirs.includes(d)) errs.push(`thiếu thư mục spec ${d}`);
    if (p.level !== 'L2' && p.level !== 'LA' && !p.grep.includes('@smoke')) errs.push('thiếu smoke');
    if (c.noUi && (/ui-|mobile|\(api\|ui/.test(p.grep) || p.mobileSpecs.length || p.grepInvert)) errs.push(`có UI trong grep: ${p.grep}`);
    if (c.apiFull !== undefined && p.apiFull !== c.apiFull) errs.push(`apiFull ${p.apiFull} ≠ ${c.apiFull}`);
    if (c.apiFull && !p.grep.includes('^api ')) errs.push('thiếu ^api trọn bộ');
    if (c.uiDirsExact && !same(p.uiDirs, c.uiDirsExact)) errs.push(`uiDirs [${p.uiDirs}] ≠ [${c.uiDirsExact}]`);
    if (c.uiDirsExact) {
      for (const d of p.specDirs.filter((x) => x && !c.uiDirsExact.includes(x))) if (new RegExp(p.grep).test(`ui-chromium ${d}/x.ui.spec.ts t`)) errs.push(`UI chạy spec ngoài epic sở hữu (${d})`);
      for (const d of c.uiDirsExact) if (!new RegExp(p.grep).test(`ui-chromium ${d}/x.ui.spec.ts t`)) errs.push(`UI thiếu epic sở hữu ${d}`);
      for (const d of p.specDirs.filter(Boolean)) if (!new RegExp(p.grep).test(`api ${d}/x.api.spec.ts t`)) errs.push(`api thiếu ${d}`);
    }
    if (c.serverOnly !== undefined && p.serverOnly !== c.serverOnly) errs.push(`serverOnly ${p.serverOnly} ≠ ${c.serverOnly}`);
    if (c.serverOnly) {
      if (p.grepInvert !== B_UI_INVERT) errs.push('thiếu --grep-invert @loai-B (chỉ UI)');
      // ca B chạy bằng API (project api) không bị loại; ca B còn ở UI/mobile thì bị loại
      const inv = new RegExp(p.grepInvert);
      if (inv.test(`api s3e1/classes.flow.api.spec.ts Ca B ${B_TAG}`)) errs.push('--grep-invert loại cả ca B chạy API');
      if (!inv.test(`ui-chromium s3e5/account.ui.spec.ts Ca B ${B_TAG}`) || !inv.test(`mobile-pixel x.ui.spec.ts Ca ${B_TAG}`)) errs.push('--grep-invert không loại ca B ở UI');
      // mọi ca *.flow.api.spec.ts của diff đều được chọn (không bị grep-invert): đếm tệp flow có trong danh sách spec
      for (const f of p.specs.filter((s) => /\.flow\.api\.spec\.ts$/.test(s))) if (inv.test(`api ${f} x ${B_TAG}`)) errs.push(`ca B API bị loại ở ${f}`);
      if (p.mobileSpecs.length) errs.push('diff chỉ server mà còn spec mobile');
      // 0 ca UI loại B: không spec *.ui.spec.ts nào được chọn còn test gắn @loai-B (ca B nằm ở *.api.spec.ts)
      const bad = p.specs.filter((f) => /\.ui\.spec\.ts$/.test(f)).filter((f) => /@loai-B\b/.test(readFileSync(path.join(TESTS, f), 'utf8')));
      if (bad.length) errs.push(`còn ca UI loại B ở ${bad.join(', ')}`);
      for (const f of c.apiSpecsInclude ?? []) if (!p.specs.includes(f)) errs.push(`thiếu spec API ${f}`);
    }
    const nSpec = p.level === 'L2' ? 'trọn bộ' : p.level === 'LA' ? 'api trọn bộ, không UI' : `${p.specs.length} spec + ${p.mobileSpecs.length} spec điện thoại + smoke`;
    console.log(`${errs.length ? 'LỆCH' : 'ĐẠT '} · ${c.name} · ${p.level} · epic ${p.epics.join(', ') || '-'} · ${nSpec}${errs.length ? ` · ${errs.join('; ')}` : ''}`);
    if (errs.length) bad++;
  }
  console.log(`selftest: ${(map.selftest ?? []).length - bad}/${(map.selftest ?? []).length} đạt`);
  return bad ? 1 : 0;
}

function main() {
  const o = parseArgs(process.argv.slice(2));
  const mapPath = o.map ?? process.env.GEO_IMPACT_MAP ?? path.join(repoRoot(), 'artifacts/GEO/work/s23/impact-map.json');
  const map = JSON.parse(readFileSync(mapPath, 'utf8'));
  if (o.selftest) process.exit(selftest(map));
  const files = o.files ?? changedFiles(o.base);
  const p = plan(files, map, o.uiOwn);
  p.base = o.files ? null : o.base;
  printPlan(p);
  if (o.dryRun) return;
  const run = process.env.GEO_E2E_RUN || new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit',
  }).format(new Date()).replace(/[-: ]/g, (c) => (c === ' ' ? '-' : ''));
  const t0 = Date.now();
  const r = spawnSync(path.join(APP, 'scripts', 'e2e.sh'), ['-g', p.grep, ...(p.grepInvert ? ['--grep-invert', p.grepInvert] : []), ...o.pw], {
    cwd: APP, stdio: 'inherit', env: { ...process.env, GEO_E2E_RUN: run },
  });
  const rc = r.status ?? 1;
  const dir = path.join(APP, 'e2e', 'evidence', run);
  mkdirSync(dir, { recursive: true });
  writeFileSync(path.join(dir, 'impact.json'), JSON.stringify({ run, map: path.relative(repoRoot(), mapPath), exitCode: rc, seconds: Math.round((Date.now() - t0) / 1000), ...p }, null, 2));
  console.log(`impact.json: ${path.join(dir, 'impact.json')} · mức ${p.level} · exit ${rc}`);
  process.exit(rc);
}

main();
