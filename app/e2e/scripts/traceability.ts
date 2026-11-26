/**
 * Truy vết TC ↔ test ↔ kết quả chạy.
 * Chạy: `node scripts/traceability.ts [--run <mã>] [--all] [--epic E1,S2-E1|MVP|S2|S3] [--defer US-S2E5-05,…] [--strict]`
 * (Node ≥ 22.18 tự bỏ kiểu TypeScript; không cần cài gì thêm.)
 * Nguồn:
 *   - TC: artifacts/GEO/work/epics/{E*,S2-E*,S3-E*}/test_cases.md (hoặc GEO_TC_DIR), mã TC-GEO-nnnn / TC-S2En-nnn /
 *     TC-S3En-nn; cột "Tự động hóa" (E2E-UI / E2E-API / IT / UT / Manual). Story của TC = tiêu đề `### US-…` gần nhất.
 *   - Test Playwright: tag @TC-… trong app/e2e/tests/**; kết quả evidence/<run>/summary.json (mặc định evidence/latest.txt).
 *   - Test JUnit (IT, UT): mã TC-GEO-0123 / TC_GEO_0123 / TC-S2E1-001 / TC_S2E1_001 trong app/server/src/test/java;
 *     kết quả server/target/{surefire,failsafe}-reports: ca JUnit (lớp + phương thức) nối với mã TC ở @DisplayName /
 *     chú thích ngay trên phương thức hoặc javadoc lớp (đọc từ mã nguồn, vì báo cáo không ghi @DisplayName).
 *   - Test đơn vị web (UT): mã TC trong tên test của app/web/src/**\/*.test.ts(x) (chưa đọc kết quả: "chưa chạy").
 * "Bắt buộc" = TC có Tự động hóa E2E-UI | E2E-API | IT | UT.
 * --epic: danh sách phân tách dấu phẩy; bí danh MVP = mọi E<n>, S2 = mọi S2-E<n>, S3 = mọi S3-E<n>. Không có = tất cả.
 * --defer US-…: TC của các story này không tính vào --strict (in riêng; dùng khi story giao cho task sau).
 * --strict: exit 1 khi có TC bắt buộc thiếu test, có TC trượt, hoặc --epic có tên không tồn tại. Không có --strict: exit 0.
 * Ghi thêm evidence/<run>/traceability.json khi có lượt chạy.
 */
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const here = import.meta.dirname;
const e2eDir = path.resolve(here, '..');
const appDir = path.resolve(e2eDir, '..');
const tcDir = process.env.GEO_TC_DIR ?? path.resolve(appDir, '../artifacts/GEO/work/epics');
const args = process.argv.slice(2);
const argOf = (k: string) => {
  const i = args.indexOf(k);
  return i >= 0 ? args[i + 1] : undefined;
};
const listOf = (k: string) => (argOf(k) ?? '').split(',').map((s) => s.trim()).filter(Boolean);
const showAll = args.includes('--all');
const strict = args.includes('--strict');
const deferred = new Set(listOf('--defer'));

const EPIC_DIR = /^(E\d+|S[23]-E\d+)$/;
const allEpics = readdirSync(tcDir)
  .filter((d) => EPIC_DIR.test(d) && existsSync(path.join(tcDir, d, 'test_cases.md')))
  .sort((a, b) => a.localeCompare(b, 'en', { numeric: true }));
const ALIASES: Record<string, (e: string) => boolean> = {
  MVP: (e) => /^E\d+$/.test(e),
  S2: (e) => e.startsWith('S2-'),
  S3: (e) => e.startsWith('S3-'),
};
const problems: string[] = [];
const wanted = listOf('--epic');
const selected = new Set<string>();
for (const w of wanted) {
  const alias = ALIASES[w.toUpperCase()];
  if (alias) allEpics.filter(alias).forEach((e) => selected.add(e));
  else if (allEpics.includes(w)) selected.add(w);
  else problems.push(`--epic "${w}" không có thư mục ${path.relative(process.cwd(), tcDir)}/${w}/test_cases.md`);
}
const inScope = (epic: string) => wanted.length === 0 || selected.has(epic);

// ---- TC từ test_cases.md
type Tc = { id: string; epic: string; story: string; title: string; auto: string; required: boolean };
const tcs = new Map<string, Tc>();
const TC_ROW = /^\| (TC-(?:GEO-\d{4}|S[23]E\d+-\d+)) \|/;
const AUTO = /^(E2E-UI|E2E-API|IT|UT)$/;
const splitRow = (line: string) => line.trim().slice(1, -1).split(/(?<!\\)\|/).map((c) => c.trim());
for (const epic of allEpics) {
  let hdr: string[] | null = null;
  let story = '';
  for (const line of readFileSync(path.join(tcDir, epic, 'test_cases.md'), 'utf8').split('\n')) {
    if (line.startsWith('## ')) story = '';
    const us = /^###\s+(US-[A-Z0-9]+-\d+)/.exec(line);
    if (us) story = us[1]!;
    if (line.startsWith('| No |')) {
      hdr = splitRow(line);
      continue;
    }
    if (!TC_ROW.test(line) || !hdr || !hdr.includes('Remark')) continue;
    const cols = splitRow(line);
    if (cols.length !== hdr.length) continue; // hàng của bảng khác (đối chiếu), không phải hàng case
    const row = Object.fromEntries(hdr.map((h, i) => [h, cols[i] ?? '']));
    const id = row['No']!;
    if (!id || tcs.has(id)) continue;
    // Cột "Tự động hóa" (PO-50). Thiếu cột thì suy từ tầng cũ.
    const layer = row['Tầng tự động hóa'] ?? '';
    const auto = (row['Tự động hóa'] ?? '').split(' ')[0] || (/E2E/.test(layer) ? 'E2E-UI' : '');
    tcs.set(id, { id, epic, story, title: row['Tiêu đề'] ?? '', auto, required: AUTO.test(auto) });
  }
}

// ---- Mã TC trong mã test
const walk = (d: string, ok: (n: string) => boolean): string[] =>
  !existsSync(d)
    ? []
    : readdirSync(d).flatMap((n) => {
        const p = path.join(d, n);
        if (n === 'node_modules' || n === 'dist' || n.startsWith('.')) return [];
        return statSync(p).isDirectory() ? walk(p, ok) : ok(n) ? [p] : [];
      });
const tagged = new Map<string, Set<string>>();
const addTag = (id: string, file: string) => {
  if (!tagged.has(id)) tagged.set(id, new Set());
  tagged.get(id)!.add(path.relative(appDir, file));
};
// Playwright: @TC-… trong tiêu đề hoặc tag (mọi loại TC)
for (const file of walk(path.join(e2eDir, 'tests'), (n) => /\.(ts|tsx|js)$/.test(n)))
  for (const m of readFileSync(file, 'utf8').matchAll(/@(TC-[A-Z][A-Z0-9]*-[0-9A-Z]+)\b/g)) addTag(m[1]!, file);
/** Mã TC chuẩn hóa từ văn bản mã nguồn / tên ca JUnit: TC_GEO_0123, TC-S2E1-001, TC_S3E6_01, tc0123_… (MVP). */
const idsIn = (s: string, methodStyle = false): string[] => {
  const out = [
    ...[...s.matchAll(/TC[-_]GEO[-_](\d{4})/g)].map((m) => `TC-GEO-${m[1]}`),
    ...[...s.matchAll(/TC[-_](S[23]E\d+)[-_](\d+)/gi)].map((m) => `TC-${m[1]!.toUpperCase()}-${m[2]}`),
  ];
  if (methodStyle) out.push(...[...s.matchAll(/\btc(\d{4})/g)].map((m) => `TC-GEO-${m[1]}`));
  return [...new Set(out)];
};
// JUnit (IT, UT) và test đơn vị web (UT): chỉ tính cho TC có Tự động hóa tương ứng
const codeTags: Array<[string, RegExp, RegExp]> = [
  [path.join(appDir, 'server/src/test/java'), /\.java$/, /^(IT|UT)$/],
  [path.join(appDir, 'web/src'), /\.test\.tsx?$/, /^UT$/],
];
for (const [dir, fileRe, autoRe] of codeTags)
  for (const file of walk(dir, (n) => fileRe.test(n)))
    for (const id of idsIn(readFileSync(file, 'utf8'))) if (autoRe.test(tcs.get(id)?.auto ?? '')) addTag(id, file);

/**
 * Mã TC của từng phương thức test JUnit, đọc từ mã nguồn: báo cáo surefire/failsafe chỉ ghi lớp + tên phương thức
 * (không ghi @DisplayName), còn mã TC nằm ở @DisplayName / chú thích / javadoc ngay trên phương thức. Mã TC ở phần
 * đầu lớp (javadoc lớp) áp cho mọi test của lớp đó; lớp lồng (Outer$Inner) thừa kế mã của lớp ngoài.
 */
const TEST_ANNOT = /@(Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\b/;
/** Bỏ chú thích và chuỗi khỏi phần đầu khai báo để nhận dạng cú pháp (mã TC vẫn đọc từ bản đầy đủ). */
const codeOnly = (s: string) =>
  s.replace(/\/\/[^\n]*|\/\*[\s\S]*?\*\/|"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'/g, ' ').trim();
function javaTestIds(src: string): { methods: Map<string, string[]>; classes: Map<string, string[]> } {
  const methods = new Map<string, string[]>();
  const classes = new Map<string, string[]>();
  const pkg = /^\s*package\s+([\w.]+)\s*;/m.exec(src)?.[1];
  type Frame = { cls?: string; ids: string[]; acc: string; paren: number; arr: number };
  const stack: Frame[] = [{ ids: [], acc: '', paren: 0, arr: 0 }];
  const top = () => stack[stack.length - 1]!;
  const end = (k: number) => (k < 0 ? src.length : k);
  let i = 0;
  while (i < src.length) {
    const f = top();
    const c = src[i]!;
    let j = i + 1;
    if (src.startsWith('//', i)) j = end(src.indexOf('\n', i));
    else if (src.startsWith('/*', i)) j = src.indexOf('*/', i + 2) < 0 ? src.length : src.indexOf('*/', i + 2) + 2;
    else if (src.startsWith('"""', i)) j = src.indexOf('"""', i + 3) < 0 ? src.length : src.indexOf('"""', i + 3) + 3;
    else if (c === '"' || c === "'") {
      while (j < src.length && src[j] !== c) j += src[j] === '\\' ? 2 : 1;
      j += 1;
    } else if (c === '(') f.paren += 1;
    else if (c === ')') f.paren = Math.max(0, f.paren - 1);
    else if (c === '{' && (f.paren > 0 || f.arr > 0)) f.arr += 1; // mảng trong chú thích: @ValueSource(ints = {1, 2})
    else if (c === '}' && f.arr > 0) f.arr -= 1;
    else if (c === '{') {
      const head = f.acc;
      const code = codeOnly(head);
      const decl = [...code.matchAll(/\b(?:class|interface|enum|record)\s+(\w+)/g)].pop();
      const frame: Frame = { ids: [], acc: '', paren: 0, arr: 0 };
      if (decl && !/\)\s*(?:throws[\w.,\s]*)?$/.test(code)) {
        frame.cls = f.cls ? `${f.cls}$${decl[1]}` : pkg ? `${pkg}.${decl[1]}` : decl[1];
        frame.ids = [...new Set([...f.ids, ...idsIn(head)])];
        classes.set(frame.cls, frame.ids);
      } else if (f.cls && TEST_ANNOT.test(code)) {
        const name = /(\w+)\s*\((?:[^()]|\([^()]*\))*\)\s*(?:throws[\w.,\s]*)?$/.exec(code)?.[1];
        if (name) methods.set(`${f.cls}#${name}`, [...new Set([...idsIn(head, true), ...idsIn(name, true)])]);
      }
      stack.push(frame);
      i = j;
      continue;
    } else if (c === '}') {
      if (stack.length > 1) stack.pop();
      top().acc = '';
      i = j;
      continue;
    } else if (c === ';' && f.paren === 0 && f.arr === 0) {
      f.acc = '';
      i = j;
      continue;
    }
    f.acc += src.slice(i, j);
    i = j;
  }
  return { methods, classes };
}
const javaMethods = new Map<string, string[]>();
const javaClasses = new Map<string, string[]>();
for (const file of walk(path.join(appDir, 'server/src/test/java'), (n) => n.endsWith('.java'))) {
  const { methods, classes } = javaTestIds(readFileSync(file, 'utf8'));
  methods.forEach((v, k) => javaMethods.set(k, v));
  classes.forEach((v, k) => javaClasses.set(k, v));
}
/** Mã TC của một ca trong báo cáo JUnit: mã của phương thức (nguồn) + mã đầu lớp + mã trong tên (kiểu tc0123_…). */
const junitIds = (attrs: string): string[] => {
  const cls = /\bclassname="([^"]*)"/.exec(attrs)?.[1] ?? '';
  const method = (/\bname="([^"]*)"/.exec(attrs)?.[1] ?? '').split(/[([]/)[0]!.trim();
  return [
    ...new Set([...(javaMethods.get(`${cls}#${method}`) ?? []), ...(javaClasses.get(cls) ?? []), ...idsIn(attrs, true)]),
  ];
};

// ---- Kết quả
const junit = new Map<string, string>();
for (const dir of ['surefire-reports', 'failsafe-reports']) {
  const d = path.join(appDir, 'server/target', dir);
  if (!existsSync(d)) continue;
  for (const n of readdirSync(d).filter((x) => /^TEST-.*\.xml$/.test(x))) {
    const xml = readFileSync(path.join(d, n), 'utf8');
    for (const m of xml.matchAll(/<testcase\b([^>]*?)(\/>|>([\s\S]*?)<\/testcase>)/g)) {
      const body = m[3] ?? '';
      const st = /<(failure|error)\b/.test(body) ? 'failed' : /<skipped\b/.test(body) ? 'skipped' : 'passed';
      for (const id of junitIds(m[1] ?? '')) {
        const prev = junit.get(id);
        junit.set(id, prev === 'failed' || st === 'failed' ? 'failed' : prev === 'skipped' ? prev : st);
      }
    }
  }
}
const latestFile = path.join(e2eDir, 'evidence', 'latest.txt');
const run = argOf('--run') ?? (existsSync(latestFile) ? readFileSync(latestFile, 'utf8').trim() : undefined);
type Summary = { run: string; tcs: Record<string, { status: string }> };
let summary: Summary | undefined;
if (run) {
  const f = path.join(e2eDir, 'evidence', run, 'summary.json');
  if (existsSync(f)) summary = JSON.parse(readFileSync(f, 'utf8')) as Summary;
}
/** Gộp kết quả JUnit và Playwright: có trượt → trượt; có đạt → đạt; còn lại bỏ qua / chưa chạy. */
const statusOf = (id: string) => {
  const st = [junit.get(id), summary?.tcs[id]?.status].filter(Boolean) as string[];
  if (st.includes('failed')) return 'failed';
  if (st.includes('passed')) return 'passed';
  if (st.length) return st[0]!;
  return tagged.has(id) ? 'chưa chạy' : '—';
};

// ---- Báo cáo
const pad = (s: string | number, n: number) => String(s).padEnd(n);
const out: string[] = [];
const scopeTcs = [...tcs.values()].filter((t) => inScope(t.epic));
out.push(`TRUY VẾT TC ↔ TEST   nguồn TC: ${path.relative(process.cwd(), tcDir)}   lượt: ${summary ? run : '(chưa có kết quả)'}`);
out.push(`Phạm vi: ${wanted.length ? [...selected].join(', ') || '(trống)' : 'tất cả'}${deferred.size ? `   hoãn: ${[...deferred].join(', ')}` : ''}`);
out.push('');
out.push(`${pad('Epic', 8)}${pad('Tổng TC', 9)}${pad('Bắt buộc', 10)}${pad('Có test', 9)}${pad('Đạt', 6)}${pad('Trượt', 7)}${pad('Hoãn', 6)}Bắt buộc chưa có test`);
const rows: Record<string, unknown>[] = [];
for (const epic of allEpics.filter(inScope)) {
  const list = scopeTcs.filter((t) => t.epic === epic);
  const withTest = list.filter((t) => tagged.has(t.id));
  const passed = withTest.filter((t) => statusOf(t.id) === 'passed').length;
  const failed = withTest.filter((t) => statusOf(t.id) === 'failed').length;
  const req = list.filter((t) => t.required);
  const def = req.filter((t) => deferred.has(t.story)).length;
  const missing = req.filter((t) => !tagged.has(t.id) && !deferred.has(t.story)).length;
  out.push(`${pad(epic, 8)}${pad(list.length, 9)}${pad(req.length, 10)}${pad(withTest.length, 9)}${pad(passed, 6)}${pad(failed, 7)}${pad(def, 6)}${missing}`);
  rows.push({ epic, total: list.length, required: req.length, withTest: withTest.length, passed, failed, deferred: def, requiredWithoutTest: missing });
}

const withTests = scopeTcs.filter((t) => tagged.has(t.id));
out.push('');
out.push(`TC có test (${withTests.length})${showAll ? '' : ' — chỉ in TC trượt / chưa chạy, thêm --all để xem hết'}:`);
for (const t of withTests)
  if (showAll || statusOf(t.id) !== 'passed')
    out.push(`  ${t.id}  [${statusOf(t.id)}]  ${t.title}  <- ${[...tagged.get(t.id)!].join(', ')}`);

const unknown = [...tagged.keys()].filter((id) => !tcs.has(id)).sort();
out.push('');
out.push(`Mã trong test nhưng không có trong test_cases.md (test khung hoặc gõ sai) (${unknown.length}):`);
for (const id of unknown) out.push(`  ${id}  [${statusOf(id)}]  <- ${[...tagged.get(id)!].join(', ')}`);

const requiredMissing = scopeTcs.filter((t) => t.required && !tagged.has(t.id) && !deferred.has(t.story));
out.push('');
out.push(`TC tự động hóa (E2E-UI / E2E-API / IT / UT) CHƯA có test (${requiredMissing.length})${showAll ? '' : ' — 30 mục đầu'}:`);
for (const t of showAll ? requiredMissing : requiredMissing.slice(0, 30)) out.push(`  ${t.id}  ${t.epic}  ${t.story}  [${t.auto}]  ${t.title}`);

const failedTcs = withTests.filter((t) => statusOf(t.id) === 'failed' && !deferred.has(t.story));
const deferredTcs = scopeTcs.filter((t) => t.required && deferred.has(t.story));
const unknownDefer = [...deferred].filter((s) => !scopeTcs.some((t) => t.story === s));
if (deferredTcs.length || unknownDefer.length) {
  out.push('');
  out.push(`Hoãn (--defer, không tính --strict): ${deferredTcs.length} TC bắt buộc; ${deferredTcs.filter((t) => !tagged.has(t.id)).length} chưa có test.`);
  if (unknownDefer.length) out.push(`  Cảnh báo: --defer không khớp story nào trong phạm vi: ${unknownDefer.join(', ')}`);
}

const ok = problems.length === 0 && requiredMissing.length === 0 && failedTcs.length === 0;
out.push('');
for (const p of problems) out.push(`LỖI: ${p}`);
out.push(
  `${strict ? 'STRICT' : 'Kết luận'}: ${ok ? 'ĐẠT' : 'KHÔNG ĐẠT'} — thiếu test ${requiredMissing.length}, trượt ${failedTcs.length}` +
    `, hoãn ${deferredTcs.length}, chưa chạy ${withTests.filter((t) => statusOf(t.id) === 'chưa chạy').length}` +
    (strict ? '' : ' (không có --strict: luôn exit 0)'),
);
console.log(out.join('\n'));

if (summary && run) {
  const json = {
    run,
    scope: wanted.length ? [...selected] : allEpics,
    deferredStories: [...deferred],
    epics: rows,
    withTests: withTests.map((t) => ({ id: t.id, status: statusOf(t.id), files: [...tagged.get(t.id)!] })),
    unknownTags: unknown.map((id) => ({ id, status: statusOf(id) })),
    requiredMissing: requiredMissing.map((t) => t.id),
    failed: failedTcs.map((t) => t.id),
    deferred: deferredTcs.map((t) => t.id),
    strictOk: ok,
  };
  writeFileSync(path.join(e2eDir, 'evidence', run, 'traceability.json'), JSON.stringify(json, null, 2));
}
if (strict && !ok) process.exit(1);
