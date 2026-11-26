#!/usr/bin/env node
/**
 * make e2e-affected (T-261126-45; DECISIONS 26/11 "Chiến lược regression theo mức ảnh hưởng"; epic-common v1.5).
 * Đo mức ảnh hưởng từ diff so với BASE (mặc định worktree-mvp-wave0) theo bản đồ dữ liệu
 * artifacts/GEO/work/s23/impact-map.json, rồi chạy scripts/e2e.sh MỘT lượt với đúng tập spec:
 *   L0  spec của epic sở hữu tệp bị đụng + smoke (@smoke)          trên api + ui-chromium
 *   L1  L0 + spec các epic phụ thuộc (deps của luật)              trên api + ui-chromium
 *   L2  trọn bộ                                                   trên api + ui-chromium
 *   L0/L1 thêm: spec P0 điện thoại (MOBILE_P0, *.p0m.ui.spec.ts) của màn bị đụng trên mobile-pixel / mobile-iphone.
 * Chọn test bằng một biểu thức -g của Playwright (khớp "<project> <tệp> <tiêu đề> <tag>"), nên không cần --project.
 *
 * Cách dùng (từ app/):
 *   node scripts/e2e-affected.mjs [--dry-run] [--base <ref>] [--files a,b,…] [--map <json>] [-- <tham số Playwright thêm>]
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
const RANK = { none: 0, L0: 1, L1: 2, L2: 3 };
const DESKTOP = '(api|ui-chromium)';
const MOBILE = 'mobile-(pixel|iphone)';
// Bản sao quy ước MOBILE_P0 của playwright.config.ts (chỉ để IN danh sách; việc chọn do project mobile-* tự lọc).
const MOBILE_P0 = [
  /(e1\/student-join|e2\/labels|e4\/(hints|solution|qa)|e5\/(catalog|detail)|e6\/(assign|release)|e7\/monitor)\.ui\.spec\.ts$/,
  /\.p0m\.ui\.spec\.ts$/,
];

function parseArgs(argv) {
  const o = { dryRun: false, selftest: false, base: process.env.BASE || 'worktree-mvp-wave0', files: null, map: null, pw: [] };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--') { o.pw.push(...argv.slice(i + 1)); break; }
    else if (a === '--dry-run') o.dryRun = true;
    else if (a === '--selftest') o.selftest = true;
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
  const m = /^(?:(s[23])e(\d+)|e(\d+))$/i.exec(raw);
  if (!m) return raw.toUpperCase();
  return m[1] ? `${m[1].toUpperCase()}-E${m[2]}` : `E${m[3]}`;
};
function specDirOf(epic, map) {
  if (map.specDirs && epic in map.specDirs) return map.specDirs[epic];
  const m = /^(?:(S[23])-)?E(\d+)$/.exec(epic);
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

function plan(files, map) {
  const items = files.map((f) => classify(f, map));
  const level = items.reduce((acc, x) => (RANK[x.level] > RANK[acc] ? x.level : acc), 'none');
  const eff = level === 'none' ? 'L0' : level;
  const owners = [...new Set(items.filter((x) => x.level !== 'none' && x.epic).map((x) => x.epic))];
  const deps = eff === 'L1' ? [...new Set(items.filter((x) => x.level === 'L1').flatMap((x) => x.deps))].filter((d) => !owners.includes(d)) : [];
  const epics = [...owners, ...deps];
  const res = { level: eff, base: null, files: items, owners, deps, epics, specDirs: [], missingSpecs: [], specs: [], mobileSpecs: [], grep: '' };
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
  res.mobileSpecs = ownerDirs.flatMap((d) => specFiles(d)).filter((f) => MOBILE_P0.some((re) => re.test(f)));
  const esc = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const named = res.specDirs.filter((d) => d !== '').map(esc);
  const alts = [`^${DESKTOP} .*@smoke\\b`];
  if (named.length) alts.push(`^${DESKTOP} (${named.join('|')})/`);
  if (res.specDirs.includes('')) alts.push(`^${DESKTOP} [^/ ]+\\.spec\\.ts `);
  const mob = ownerDirs.filter((d) => d !== '').map(esc);
  if (res.mobileSpecs.length && mob.length) alts.push(`^${MOBILE} (${mob.join('|')})/`);
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
  if (p.level !== 'L2') {
    out(`Epic: ${p.epics.join(', ') || '(không)'}${p.missingSpecs.length ? ` · chưa có spec: ${p.missingSpecs.join(', ')}` : ''}`);
    out(`Spec api + ui-chromium (${p.specs.length} tệp) + mọi test @smoke:`);
    for (const s of p.specs) out(`  ${s}`);
    out(`Spec điện thoại mobile-pixel/mobile-iphone (${p.mobileSpecs.length} tệp): ${p.mobileSpecs.join(', ') || '(không)'}`);
  } else {
    out('Spec: trọn bộ api + ui-chromium (smoke nằm trong đó)');
  }
  out(`Playwright -g: ${p.grep}`);
}

function selftest(map) {
  let bad = 0;
  for (const c of map.selftest ?? []) {
    const p = plan(c.files, map);
    const errs = [];
    if (p.level !== c.level) errs.push(`mức ${p.level} ≠ ${c.level}`);
    const same = (a, b) => a.length === b.length && a.every((x) => b.includes(x));
    if (c.epicsExact && !same(p.epics, c.epicsExact)) errs.push(`epic [${p.epics}] ≠ [${c.epicsExact}]`);
    for (const e of c.epicsInclude ?? []) if (!p.epics.includes(e)) errs.push(`thiếu epic ${e}`);
    for (const d of c.specDirsInclude ?? []) if (!p.specDirs.includes(d)) errs.push(`thiếu thư mục spec ${d}`);
    if (p.level !== 'L2' && !p.grep.includes('@smoke')) errs.push('thiếu smoke');
    const nSpec = p.level === 'L2' ? 'trọn bộ' : `${p.specs.length} spec + ${p.mobileSpecs.length} spec điện thoại + smoke`;
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
  const p = plan(files, map);
  p.base = o.files ? null : o.base;
  printPlan(p);
  if (o.dryRun) return;
  const run = process.env.GEO_E2E_RUN || new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit',
  }).format(new Date()).replace(/[-: ]/g, (c) => (c === ' ' ? '-' : ''));
  const t0 = Date.now();
  const r = spawnSync(path.join(APP, 'scripts', 'e2e.sh'), ['-g', p.grep, ...o.pw], {
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
