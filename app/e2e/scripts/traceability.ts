/**
 * Truy vết TC ↔ test E2E ↔ kết quả chạy.  Chạy: `node scripts/traceability.ts [--run <mã>] [--all] [--epic E1]`
 * (Node ≥ 22.18 tự bỏ kiểu TypeScript; không cần cài gì thêm.)
 * Nguồn:
 *   - TC: artifacts/GEO/work/epics/E*\/test_cases.md (hoặc GEO_TC_DIR), cột "Tự động hóa" (E2E-UI / E2E-API / IT / Manual).
 *   - Test IT: mã TC-GEO-nnnn / TC_GEO_nnnn trong app/server/src/test/java; kết quả ở server/target/*-reports.
 *   - Test: mọi tag @TC-... trong app/e2e/tests/**.
 *   - Kết quả: evidence/<run>/summary.json (mặc định lượt trong evidence/latest.txt).
 * "Ứng viên E2E" = TC có tầng chứa "E2E", "tích hợp API" hoặc "thủ công trên thiết bị" (tự động hóa được bằng
 * Playwright api/ui). TC "E2E trình duyệt" là bắt buộc phải có test; liệt kê riêng.
 * Ghi thêm evidence/<run>/traceability.json khi có lượt chạy.
 */
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const here = import.meta.dirname;
const e2eDir = path.resolve(here, '..');
const tcDir = process.env.GEO_TC_DIR ?? path.resolve(e2eDir, '../../artifacts/GEO/work/epics');
const args = process.argv.slice(2);
const argOf = (k: string) => {
  const i = args.indexOf(k);
  return i >= 0 ? args[i + 1] : undefined;
};
const showAll = args.includes('--all');
const onlyEpic = argOf('--epic');

type Tc = { id: string; epic: string; title: string; layer: string; auto: string; candidate: boolean; required: boolean };
const tcs = new Map<string, Tc>();
const splitRow = (line: string) => line.trim().slice(1, -1).split(/(?<!\\)\|/).map((c) => c.trim());
for (const epic of readdirSync(tcDir).filter((d) => /^E\d+$/.test(d)).sort()) {
  const f = path.join(tcDir, epic, 'test_cases.md');
  if (!existsSync(f)) continue;
  let hdr: string[] | null = null;
  for (const line of readFileSync(f, 'utf8').split('\n')) {
    if (line.startsWith('| No |')) {
      hdr = splitRow(line);
      continue;
    }
    if (!line.startsWith('| TC-GEO-') || !hdr || !hdr.includes('Remark')) continue;
    const cols = splitRow(line);
    if (cols.length !== hdr.length) continue; // hàng của bảng khác (đối chiếu), không phải hàng case
    const row = Object.fromEntries(hdr.map((h, i) => [h, cols[i] ?? '']));
    const id = row['No']!;
    if (!id || tcs.has(id)) continue;
    // Cột "Tự động hóa" (PO-50): E2E-UI | E2E-API | IT | Manual — <lý do>. Thiếu cột thì suy từ tầng cũ.
    const layer = row['Tầng tự động hóa'] ?? '';
    const auto = (row['Tự động hóa'] ?? '').split(' ')[0] || (/E2E/.test(layer) ? 'E2E-UI' : '');
    tcs.set(id, {
      id,
      epic,
      title: row['Tiêu đề'] ?? '',
      layer,
      auto,
      candidate: /^(E2E-UI|E2E-API|IT)$/.test(auto),
      required: /^(E2E-UI|E2E-API|IT)$/.test(auto),
    });
  }
}

// Tag trong mã test
const tagged = new Map<string, Set<string>>();
const walk = (d: string): string[] =>
  readdirSync(d).flatMap((n) => {
    const p = path.join(d, n);
    return statSync(p).isDirectory() ? walk(p) : /\.(ts|tsx|js)$/.test(n) ? [p] : [];
  });
for (const file of walk(path.join(e2eDir, 'tests'))) {
  for (const m of readFileSync(file, 'utf8').matchAll(/@(TC-[A-Z]+-[0-9A-Z-]+)/g)) {
    const id = m[1]!;
    if (!tagged.has(id)) tagged.set(id, new Set());
    tagged.get(id)!.add(path.relative(e2eDir, file));
  }
}

// Test IT (JUnit): mã TC trong tên lớp/phương thức hoặc @DisplayName, dạng TC-GEO-0123 hoặc TC_GEO_0123
const serverTest = path.resolve(e2eDir, '../server/src/test/java');
const itTagged = new Map<string, Set<string>>();
if (existsSync(serverTest)) {
  const walkJava = (d: string): string[] =>
    readdirSync(d).flatMap((n) => {
      const p = path.join(d, n);
      return statSync(p).isDirectory() ? walkJava(p) : n.endsWith('.java') ? [p] : [];
    });
  for (const file of walkJava(serverTest)) {
    for (const m of readFileSync(file, 'utf8').matchAll(/TC[-_]GEO[-_](\d{4})/g)) {
      const id = `TC-GEO-${m[1]}`;
      if (!itTagged.has(id)) itTagged.set(id, new Set());
      itTagged.get(id)!.add(path.relative(path.resolve(e2eDir, '..'), file));
    }
  }
}
// Kết quả JUnit: target/{surefire,failsafe}-reports/TEST-*.xml; testcase có mã TC trong tên → đạt/trượt
const junit = new Map<string, string>();
for (const dir of ['surefire-reports', 'failsafe-reports']) {
  const d = path.resolve(e2eDir, '../server/target', dir);
  if (!existsSync(d)) continue;
  for (const n of readdirSync(d).filter((x) => /^TEST-.*\.xml$/.test(x))) {
    const xml = readFileSync(path.join(d, n), 'utf8');
    for (const m of xml.matchAll(/<testcase\b([^>]*?)(\/>|>([\s\S]*?)<\/testcase>)/g)) {
      const attrs = m[1] ?? '';
      const body = m[3] ?? '';
      // tên ca JUnit: @DisplayName "TC-GEO-0123 …" hoặc tên phương thức tc0123_…
      const ids = [...attrs.matchAll(/(?:TC[-_]GEO[-_]|\btc)(\d{4})/g)].map((x) => `TC-GEO-${x[1]}`);
      const st = /<(failure|error)\b/.test(body) ? 'failed' : /<skipped\b/.test(body) ? 'skipped' : 'passed';
      for (const id of ids) {
        const prev = junit.get(id);
        junit.set(id, prev === 'failed' || st === 'failed' ? 'failed' : prev === 'skipped' ? prev : st);
      }
    }
  }
}
for (const [id, files] of itTagged) {
  if (tcs.get(id)?.auto !== 'IT') continue;
  if (!tagged.has(id)) tagged.set(id, new Set());
  for (const f of files) tagged.get(id)!.add(f);
}

// Kết quả chạy
const latestFile = path.join(e2eDir, 'evidence', 'latest.txt');
const run = argOf('--run') ?? (existsSync(latestFile) ? readFileSync(latestFile, 'utf8').trim() : undefined);
type Summary = { run: string; tcs: Record<string, { status: string; tests: Array<{ project: string; dir: string }> }> };
let summary: Summary | undefined;
if (run) {
  const f = path.join(e2eDir, 'evidence', run, 'summary.json');
  if (existsSync(f)) summary = JSON.parse(readFileSync(f, 'utf8')) as Summary;
}
const statusOf = (id: string) =>
  (tcs.get(id)?.auto === 'IT' ? junit.get(id) : summary?.tcs[id]?.status) ?? (tagged.has(id) ? 'chưa chạy' : '—');

// Bảng theo epic
const pad = (s: string | number, n: number) => String(s).padEnd(n);
const out: string[] = [];
out.push(`TRUY VẾT TC ↔ E2E   nguồn TC: ${path.relative(process.cwd(), tcDir)}   lượt: ${summary ? run : '(chưa có kết quả)'}`);
out.push('');
out.push(`${pad('Epic', 6)}${pad('Tổng TC', 9)}${pad('Ứng viên', 10)}${pad('Bắt buộc', 10)}${pad('Có test', 9)}${pad('Đạt', 6)}${pad('Trượt', 7)}Ứng viên chưa có test`);
const epics = [...new Set([...tcs.values()].map((t) => t.epic))].filter((e) => !onlyEpic || e === onlyEpic);
const rows: Record<string, unknown>[] = [];
for (const epic of epics) {
  const list = [...tcs.values()].filter((t) => t.epic === epic);
  const withTest = list.filter((t) => tagged.has(t.id));
  const passed = withTest.filter((t) => statusOf(t.id) === 'passed').length;
  const failed = withTest.filter((t) => statusOf(t.id) === 'failed').length;
  const cand = list.filter((t) => t.candidate);
  const missing = cand.filter((t) => !tagged.has(t.id)).length;
  const req = list.filter((t) => t.required).length;
  out.push(`${pad(epic, 6)}${pad(list.length, 9)}${pad(cand.length, 10)}${pad(req, 10)}${pad(withTest.length, 9)}${pad(passed, 6)}${pad(failed, 7)}${missing}`);
  rows.push({ epic, total: list.length, candidates: cand.length, required: req, withTest: withTest.length, passed, failed, candidatesWithoutTest: missing });
}

const withTests = [...tcs.values()].filter((t) => tagged.has(t.id) && (!onlyEpic || t.epic === onlyEpic));
out.push('');
out.push(`TC có test (${withTests.length}):`);
for (const t of withTests) out.push(`  ${t.id}  [${statusOf(t.id)}]  ${t.title}  <- ${[...tagged.get(t.id)!].join(', ')}`);

const unknown = [...tagged.keys()].filter((id) => !tcs.has(id)).sort();
out.push('');
out.push(`Mã trong test nhưng không có trong test_cases.md (test khung hoặc gõ sai) (${unknown.length}):`);
for (const id of unknown) out.push(`  ${id}  [${statusOf(id)}]  <- ${[...tagged.get(id)!].join(', ')}`);

const requiredMissing = [...tcs.values()].filter((t) => t.required && !tagged.has(t.id) && (!onlyEpic || t.epic === onlyEpic));
out.push('');
out.push(`TC tự động hóa (E2E-UI / E2E-API / IT) CHƯA có test (${requiredMissing.length}):`);
for (const t of requiredMissing) out.push(`  ${t.id}  ${t.epic}  ${t.title}`);

const candMissing = [...tcs.values()].filter((t) => t.candidate && !t.required && !tagged.has(t.id) && (!onlyEpic || t.epic === onlyEpic));
out.push('');
out.push(`Ứng viên E2E khác chưa có test (${candMissing.length})${showAll ? '' : ' — 15 mục đầu, thêm --all để xem hết'}:`);
for (const t of showAll ? candMissing : candMissing.slice(0, 15)) out.push(`  ${t.id}  ${t.epic}  [${t.auto}]  ${t.title}`);

console.log(out.join('\n'));

if (summary && run) {
  const json = {
    run,
    epics: rows,
    withTests: withTests.map((t) => ({ id: t.id, status: statusOf(t.id), files: [...tagged.get(t.id)!] })),
    unknownTags: unknown.map((id) => ({ id, status: statusOf(id) })),
    requiredMissing: requiredMissing.map((t) => t.id),
    candidatesMissing: candMissing.map((t) => t.id),
  };
  writeFileSync(path.join(e2eDir, 'evidence', run, 'traceability.json'), JSON.stringify(json, null, 2));
}
