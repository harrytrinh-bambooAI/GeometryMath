import { execFileSync } from 'node:child_process';
import { copyFileSync, existsSync, mkdirSync, mkdtempSync, readdirSync, readFileSync, rmSync, statSync, writeFileSync } from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import type { FullConfig, FullResult, Reporter, Suite, TestCase, TestResult } from '@playwright/test/reporter';
import { redactText } from '../support/redact.ts';

/**
 * Reporter bằng chứng (PO-50). Với MỖI test (đạt hay trượt) chép ảnh chụp, trace, video, http.json, lỗi vào
 *   evidence/<run>/<TC-id>/<project>__<tên-test>/
 * rồi sinh evidence/<run>/summary.json (TC -> trạng thái -> đường dẫn bằng chứng) và evidence/<run>/index.html.
 * evidence/latest.txt giữ mã lượt mới nhất (make e2e-report mở bản này).
 * Mã TC lấy từ tag của test: '@TC-GEO-0100' (TC của QC) hoặc '@TC-KHUNG-nn' (test khung, không thuộc test_cases.md).
 * Test không gắn mã TC vẫn có bằng chứng, xếp vào thư mục _khong-ma-TC.
 */
/** Tệp văn bản trong trace.zip (sự kiện, mạng, tài nguyên JSON/HTML/JS, mã nguồn test): che JWT/Bearer/token. */
const TRACE_TEXT = /\.(trace|network|stacks|json|txt|html|js|ts|css|xml)$/i;

/**
 * Che bí mật trong trace.zip (FIX4a: trace dự án UI còn JWT ở tiêu đề Authorization, phản hồi /dev/token,
 * localStorage). Giải nén vào thư mục tạm, che từng tệp văn bản, nén lại vào target; trace vẫn mở được bằng
 * `playwright show-trace`. Lỗi (thiếu zip/unzip) thì KHÔNG chép trace (không để lộ), trả false.
 */
export function redactTraceZip(src: string, target: string): boolean {
  const tmp = mkdtempSync(path.join(os.tmpdir(), 'geo-trace-'));
  try {
    execFileSync('unzip', ['-q', '-o', src, '-d', tmp]);
    const walk = (d: string): string[] =>
      readdirSync(d).flatMap((n) => {
        const f = path.join(d, n);
        return statSync(f).isDirectory() ? walk(f) : [f];
      });
    for (const f of walk(tmp)) {
      if (!TRACE_TEXT.test(f)) continue;
      const s = readFileSync(f, 'utf8');
      const r = redactText(s);
      if (r !== s) writeFileSync(f, r);
    }
    rmSync(target, { force: true });
    execFileSync('zip', ['-q', '-r', '-X', path.resolve(target), '.'], { cwd: tmp });
    return true;
  } catch {
    rmSync(target, { force: true });
    return false;
  } finally {
    rmSync(tmp, { recursive: true, force: true });
  }
}

const TC_TAG = /^@(TC-GEO-\d{4}|TC-KHUNG-\d+)$/;
/** Chuỗi trông giống mã TC (để cảnh báo khi sai định dạng, ví dụ @TC-GEO-120, TC_GEO_0120, @tc-geo-0120). */
const TC_LIKE = /@?\bTC[-_ ]?[A-Z]*[-_ ]?\d+[0-9A-Z-]*/gi;
const NO_TC = '_khong-ma-TC';

type Evidence = { screenshots: string[]; trace?: string; video?: string; http?: string; error?: string; other: string[] };
type Entry = {
  tc: string;
  title: string;
  file: string;
  line: number;
  project: string;
  status: TestResult['status'];
  outcome: ReturnType<TestCase['outcome']>;
  ok: boolean;
  durationMs: number;
  retry: number;
  dir: string;
  evidence: Evidence;
};

const stripAnsi = (s: string) => s.replace(/\u001b\[[0-9;]*m/g, '');
const esc = (s: string) => s.replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c] ?? c);
const extOf = (contentType: string) =>
  ({ 'image/png': '.png', 'image/jpeg': '.jpg', 'application/json': '.json', 'text/plain': '.txt', 'video/webm': '.webm', 'application/zip': '.zip', 'text/markdown': '.md' })[
    contentType
  ] ?? '';

export default class EvidenceReporter implements Reporter {
  private readonly outRoot: string;
  private runDir = '';
  private run = '';
  private startedAt = new Date();
  private entries: Entry[] = [];
  private usedDirs = new Set<string>();
  private projects: string[] = [];
  private suite: Suite | undefined;
  private warned = new Set<string>();

  constructor(options: { outDir?: string } = {}) {
    this.outRoot = path.resolve(options.outDir ?? 'evidence');
  }

  printsToStdio(): boolean {
    return false;
  }

  onBegin(config: FullConfig, suite: Suite): void {
    this.suite = suite;
    for (const t of suite.allTests()) this.warnMalformedTc(t);
    this.run = process.env.GEO_E2E_RUN ?? 'local';
    this.runDir = path.join(this.outRoot, this.run);
    this.projects = config.projects.map((p) => p.name);
    mkdirSync(this.runDir, { recursive: true });
  }

  /** Tiêu đề/tag có chuỗi giống mã TC nhưng sai định dạng (@TC-GEO-nnnn, @TC-KHUNG-n) => cảnh báo, không tính. */
  private warnMalformedTc(test: TestCase): void {
    const valid = new Set(test.tags.filter((t) => TC_TAG.test(t)));
    const candidates = [...test.title.matchAll(TC_LIKE), ...test.tags.join(' ').matchAll(TC_LIKE)].map((m) => m[0]);
    for (const c of candidates) {
      const tag = c.startsWith('@') ? c : `@${c}`;
      if (valid.has(tag) && c.startsWith('@')) continue;
      const key = `${test.location.file}:${test.location.line}|${c}`;
      if (this.warned.has(key)) continue;
      this.warned.add(key);
      process.stderr.write(
        `[bằng chứng] CẢNH BÁO: "${c}" ở ${path.relative(process.cwd(), test.location.file)}:${test.location.line} ` +
          `("${test.title}") không đúng định dạng mã TC (@TC-GEO-nnnn hoặc @TC-KHUNG-n); không được tính vào truy vết.\n`,
      );
    }
  }

  onTestEnd(test: TestCase, result: TestResult): void {
    const tcs = test.tags.map((t) => TC_TAG.exec(t)?.[1]).filter((x): x is string => !!x);
    const project = test.parent.project()?.name ?? 'khong-ro';
    for (const tc of tcs.length ? tcs : [NO_TC]) {
      const slug = test.title
        .replace(/@\S+/g, '')
        .normalize('NFD')
        .replace(/[̀-ͯ]/g, '')
        .replace(/[đĐ]/g, 'd')
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '')
        .slice(0, 48);
      const base = `${project}__${slug || 'test'}`;
      let rel = path.join(tc, result.retry ? `${base}-retry${result.retry}` : base);
      for (let n = 2; this.usedDirs.has(rel); n++) rel = path.join(tc, `${base}-${n}`);
      this.usedDirs.add(rel);
      const dir = path.join(this.runDir, rel);
      mkdirSync(dir, { recursive: true });
      const ev: Evidence = { screenshots: [], other: [] };

      result.attachments.forEach((a, i) => {
        let name: string;
        if (a.name === 'screenshot' || a.contentType.startsWith('image/')) {
          name = ev.screenshots.length ? `screenshot-${ev.screenshots.length + 1}.png` : 'screenshot.png';
        } else if (a.name === 'trace') name = 'trace.zip';
        else if (a.name === 'video') name = 'video.webm';
        else name = a.name.includes('.') ? a.name : `${a.name || `dinh-kem-${i}`}${extOf(a.contentType)}`;
        const target = path.join(dir, name);
        if (a.path && existsSync(a.path) && name === 'trace.zip') {
          if (!redactTraceZip(a.path, target)) return;
          copyFileSync(target, a.path); // bản thô trong test-results cũng được che
        } else if (a.path && existsSync(a.path)) copyFileSync(a.path, target);
        else if (a.body) writeFileSync(target, a.body);
        else return;
        const r = path.relative(this.runDir, target);
        if (name.startsWith('screenshot')) ev.screenshots.push(r);
        else if (name === 'trace.zip') ev.trace = r;
        else if (name === 'video.webm') ev.video = r;
        else if (name === 'http.json') ev.http = r;
        else ev.other.push(r);
      });

      if (result.errors.length) {
        const target = path.join(dir, 'error.txt');
        writeFileSync(target, result.errors.map((e) => stripAnsi(e.stack ?? e.message ?? String(e.value))).join('\n\n---\n\n'));
        ev.error = path.relative(this.runDir, target);
      }

      this.entries.push({
        tc,
        title: test.title,
        file: path.relative(process.cwd(), test.location.file),
        line: test.location.line,
        project,
        status: result.status,
        outcome: test.outcome(),
        ok: test.outcome() === 'expected' || test.outcome() === 'flaky',
        durationMs: result.duration,
        retry: result.retry,
        dir: rel,
        evidence: ev,
      });
    }
  }

  onEnd(result: FullResult): void {
    // Test không chạy tới (lượt bị ngắt, dừng sớm vì quá số lỗi, bị bỏ qua trước khi chạy): onTestEnd không
    // được gọi => vẫn đưa vào summary với trạng thái tương ứng (interrupted / skipped), không có bằng chứng.
    const seen = new Set(this.entries.map((e) => `${e.file}:${e.line}|${e.project}`));
    for (const t of this.suite?.allTests() ?? []) {
      const project = t.parent.project()?.name ?? 'khong-ro';
      const file = path.relative(process.cwd(), t.location.file);
      if (seen.has(`${file}:${t.location.line}|${project}`)) continue;
      const status: TestResult['status'] =
        result.status === 'interrupted' || result.status === 'timedout' ? 'interrupted' : 'skipped';
      const tcs = t.tags.map((x) => TC_TAG.exec(x)?.[1]).filter((x): x is string => !!x);
      for (const tc of tcs.length ? tcs : [NO_TC]) {
        this.entries.push({
          tc,
          title: t.title,
          file,
          line: t.location.line,
          project,
          status,
          outcome: t.outcome(),
          ok: false,
          durationMs: 0,
          retry: 0,
          dir: '',
          evidence: { screenshots: [], other: [] },
        });
      }
    }

    // Lần thử cuối cùng của mỗi (TC, test, project) quyết định trạng thái.
    const last = new Map<string, Entry>();
    for (const e of this.entries) last.set(`${e.tc}|${e.file}:${e.line}|${e.project}`, e);
    const finals = [...last.values()];
    // Tổng số test đếm theo (test, project), KHÔNG nhân theo số mã TC gắn trên một test.
    const perTest = new Map<string, Entry>();
    for (const e of finals) perTest.set(`${e.file}:${e.line}|${e.project}`, e);
    const tests = [...perTest.values()];

    const tcs: Record<string, { status: 'passed' | 'failed' | 'skipped'; tests: Entry[] }> = {};
    for (const e of this.entries) (tcs[e.tc] ??= { status: 'passed', tests: [] }).tests.push(e);
    for (const [tc, v] of Object.entries(tcs)) {
      const mine = finals.filter((e) => e.tc === tc);
      v.status = mine.some((e) => !e.ok && e.status !== 'skipped')
        ? 'failed'
        : mine.every((e) => e.status === 'skipped')
          ? 'skipped'
          : 'passed';
    }
    const totals = {
      tests: tests.length,
      passed: tests.filter((e) => e.ok && e.status !== 'skipped').length,
      failed: tests.filter((e) => !e.ok && e.status !== 'skipped' && e.status !== 'interrupted').length,
      interrupted: tests.filter((e) => e.status === 'interrupted').length,
      skipped: tests.filter((e) => e.status === 'skipped').length,
      tcs: Object.keys(tcs).length,
      tcsPassed: Object.values(tcs).filter((t) => t.status === 'passed').length,
      tcsFailed: Object.values(tcs).filter((t) => t.status === 'failed').length,
    };
    const summary = {
      run: this.run,
      startedAt: this.startedAt.toISOString(),
      finishedAt: new Date().toISOString(),
      status: result.status,
      t0: process.env.GEO_E2E_T0 ?? null,
      projects: this.projects,
      totals,
      tcs,
    };
    writeFileSync(path.join(this.runDir, 'summary.json'), JSON.stringify(summary, null, 2));
    writeFileSync(path.join(this.runDir, 'index.html'), this.html(summary));
    writeFileSync(path.join(this.outRoot, 'latest.txt'), `${this.run}\n`);
    process.stdout.write(
      `\n[bằng chứng] ${totals.passed}/${totals.tests} test đạt, ${totals.tcsPassed}/${totals.tcs} mã TC đạt -> ${path.join(this.runDir, 'index.html')}\n`,
    );
  }

  private html(s: {
    run: string;
    status: string;
    startedAt: string;
    t0: string | null;
    projects: string[];
    totals: Record<string, number>;
    tcs: Record<string, { status: string; tests: Entry[] }>;
  }): string {
    const link = (p: string | undefined, label: string) => (p ? `<a href="${esc(p)}">${label}</a>` : '');
    const rows = Object.entries(s.tcs)
      .sort(([a], [b]) => a.localeCompare(b))
      .flatMap(([tc, v]) =>
        v.tests.map(
          (e, i) => `<tr class="${e.ok ? 'ok' : e.status === 'skipped' ? 'skip' : 'bad'}">
        ${i === 0 ? `<td rowspan="${v.tests.length}"><b>${esc(tc)}</b><br><span class="st ${v.status}">${v.status === 'passed' ? 'ĐẠT' : v.status === 'failed' ? 'TRƯỢT' : 'BỎ QUA'}</span></td>` : ''}
        <td>${esc(e.title)}<br><small>${esc(e.file)}:${e.line}</small></td>
        <td>${esc(e.project)}${e.retry ? ` (thử lại ${e.retry})` : ''}</td>
        <td>${esc(e.status)}<br><small>${(e.durationMs / 1000).toFixed(1)} s</small></td>
        <td>${e.evidence.screenshots.map((p) => `<a href="${esc(p)}"><img src="${esc(p)}" loading="lazy"></a>`).join('')}</td>
        <td>${[
          link(e.evidence.trace, 'trace'),
          link(e.evidence.video, 'video'),
          link(e.evidence.http, 'http.json'),
          link(e.evidence.error, 'lỗi'),
          ...e.evidence.other.map((p) => link(p, path.basename(p))),
          e.dir ? link(e.dir + '/', 'thư mục') : '(không chạy tới, không có bằng chứng)',
        ]
          .filter(Boolean)
          .join(' · ')}</td></tr>`,
        ),
      )
      .join('\n');
    const t = s.totals;
    return `<!doctype html><html lang="vi"><head><meta charset="utf-8"><title>Bằng chứng E2E ${esc(s.run)}</title>
<style>
body{font:14px 'Be Vietnam Pro',system-ui,sans-serif;margin:24px;color:#141B2D;background:#F3F5FB}
h1{font-size:20px;margin:0 0 4px} .meta{color:#5F6B85;margin-bottom:16px}
.cards{display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap} .card{background:#fff;border:1px solid #E3E8F2;border-radius:12px;padding:10px 16px}
.card b{font-size:20px;display:block}
table{border-collapse:collapse;width:100%;background:#fff} th,td{border:1px solid #E3E8F2;padding:8px;vertical-align:top;text-align:left}
th{background:#F7F9FD;font-size:12px;color:#5F6B85} small{color:#5F6B85}
img{max-width:260px;max-height:170px;border:1px solid #E3E8F2;border-radius:6px;margin:2px}
.st{display:inline-block;margin-top:4px;padding:2px 8px;border-radius:99px;font-size:11px;font-weight:700}
.st.passed{background:#DFF5EC;color:#0F8A5F} .st.failed{background:#FDE7E5;color:#D92D20} .st.skipped{background:#FFF4D6;color:#9A6B00}
tr.bad td{background:#FFF8F7} a{color:#3B5BDB}
</style></head><body>
<h1>Bằng chứng E2E · lượt ${esc(s.run)}</h1>
<div class="meta">Kết quả chung: <b>${esc(s.status)}</b> · bắt đầu ${esc(s.startedAt)} (UTC) · T0 ${esc(s.t0 ?? '—')} · dự án: ${esc(s.projects.join(', '))}</div>
<div class="cards">
<div class="card"><b>${t.passed}/${t.tests}</b>test đạt</div>
<div class="card"><b>${t.failed}</b>test trượt</div>
<div class="card"><b>${t.interrupted}</b>không chạy tới · <b style="display:inline;font-size:14px">${t.skipped}</b> bỏ qua</div>
<div class="card"><b>${t.tcsPassed}/${t.tcs}</b>mã TC đạt</div>
<div class="card"><a href="summary.json">summary.json</a><br><a href="traceability.txt">traceability.txt</a></div>
</div>
<p><small>Xem trace: <code>cd app/e2e && npx playwright show-trace evidence/${esc(s.run)}/&lt;TC&gt;/&lt;dự án&gt;/trace.zip</code></small></p>
<table><tr><th>Mã TC</th><th>Test</th><th>Dự án</th><th>Trạng thái</th><th>Ảnh chụp</th><th>Bằng chứng khác</th></tr>
${rows}
</table></body></html>`;
  }
}
