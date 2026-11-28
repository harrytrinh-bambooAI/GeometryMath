import type { APIRequestContext, Page } from '@playwright/test';
import { test } from './kit.ts';
import { signInPage } from '../tests/e1/e1.ts';
import { API, bearer, hook, signIn, world } from '../tests/s3e1/s3e1.ts';
import { KT15, LAN, createSet, importBank, lanToken, type ExamSet } from '../tests/s5e2/s5e2.ts';
import { S5E3_WORLD, tl01 } from '../tests/s5e3/kb.ts';
import { Q_SC1, setup as qaSetup } from '../tests/s5e3/qa.ts';
import { rb01, s5e4World, setup as s5e4Setup } from '../tests/s5e4/s5e4.ts';
import { FIX, P1, P2, ack, aiOn, submitOk } from '../tests/s5e4/submit/submit.ts';
import { gradingWorld, sc1 } from '../tests/s5e4/review/grading.ts';
import { bc01 as s5e5bc01, s5e5World } from '../tests/s5e5/s5e5.ts';
import { graded, summary } from '../tests/s5e5/insights.ts';
import { mock, setup as s5e6Setup, submit as s5e6Submit, waitAi } from '../tests/s5e6/s5e6b.ts';

/**
 * T-261128-73 · Chặng 5 (trợ lý AI cho giáo viên). AI giả lập (MockAiProvider); cờ geo.s5.grading / geo.s5.ai-grading bật
 * trong profile dev local — clip E4/E6 gắn nhãn "Bản thử chặng 5 — tắt trong bản phát hành".
 */

const S5 = 'Stage 5 · AI assistant for teachers';
const TRIAL = 'Preview — stage 5 flags are on only in this local dev session and off in release builds';

async function shoot(page: Page, file: string) {
  await page.getByTestId('s5e4-file').setInputFiles(FIX(file));
  await page.getByTestId('s5e4-candidate').waitFor();
  await page.waitForTimeout(900);
  await page.getByTestId('s5e4-use').click();
  await page.getByTestId('s5e4-candidate').waitFor({ state: 'detached' });
}

test('08-s5e1-giao-an | S5-E1: trợ lý soạn giáo án theo PPCT', async ({ page, request, demo }) => {
  await demo.card('Lesson plan assistant', `${S5} · S5-E1 — pick a lesson from the syllabus, AI drafts the plan, the teacher reviews it`, 3000, '', 'Stage five brings an AI assistant for teachers. First, lesson plans.');
  await demo.step('form giáo án', async () => {
    const w = await world(request);
    await signIn(page, w.people['Cô Lan']!);
    await page.goto('/gv/giao-an/moi');
    await page.getByTestId('s5e1-form').waitFor();
    await demo.say('Pick the chapter, lesson and period from the official syllabus');
    await page.getByTestId('s5e1-chapter').selectOption('7');
    await page.getByTestId('s5e1-lesson').selectOption('26');
    await page.getByTestId('s5e1-period').selectOption({ label: 'Tiết 1/2' });
    await demo.say('The number of periods and the key knowledge points are filled in automatically');
    await demo.tap(page.getByTestId('s5e1-doituong-kha'));
    await demo.say('Choose the student level and the teaching style');
    await demo.tap(page.getByTestId('s5e1-submit'));
    await page.waitForURL(/\/gv\/giao-an\/[0-9a-f-]{36}$/);
    await demo.say('The AI drafts the plan. In this demo the AI is simulated.');
    await page.locator('[data-testid="s5e1-result"]:not([data-status="drafting"])').waitFor({ timeout: 20_000 });
    await demo.say('The draft has objectives, a sequence of activities, and minutes per activity');
    await page.mouse.wheel(0, 600);
    await demo.say('It is labelled as written by AI, so the teacher reviews and edits before using it');
    await page.mouse.wheel(0, 700);
    await demo.hold(1500);
  });
});

test('09-s5e2-ra-de-ma-tran | S5-E2: ra đề theo ma trận, nhiều mã đề', async ({ page, request, demo }) => {
  await demo.card('Exams from a test matrix', `${S5} · S5-E2 — choose an exam type; the engine fills it from the verified question bank, in several versions`, 3000, '', 'Next, exams built from a standard test matrix.');
  await importBank(request);
  await signInPage(page, request, LAN);
  await demo.step('form ra đề', async () => {
    await page.goto('/gv/ra-de');
    await page.getByTestId('s5e2-form').waitFor();
    await demo.say('Choosing an exam type pre-fills the form from the standard matrix');
    await page.getByTestId('s5e2-loai-de').selectOption(KT15);
    await demo.say('A fifteen-minute test: six multiple-choice and two short-answer questions, ten points, four versions');
    await demo.mark(page.getByTestId('s5e2-tong-diem'), 1200);
  });
  await demo.step('bộ đề', async () => {
    const r = await createSet(request, await lanToken(request), { matrixCode: KT15, variantCount: 4, seed: 2026 });
    const set = (await r.json()) as ExamSet;
    await page.goto(`/gv/ra-de/${set.id}`);
    await page.getByTestId('s5e2-nhan-nguoi').waitFor();
    await demo.say('Every question carries two labels: machine-verified, and approved by a person');
    await demo.tap(page.getByTestId('s5e2-ma-101'));
    await demo.say('Version one oh one');
    await demo.mark(page.getByTestId('s5e2-cau-3'), 1000);
    await demo.tap(page.getByTestId('s5e2-cau-3-thay'));
    await demo.say('Swap one question, and the new one keeps the same topic, type, difficulty and points');
  });
});

test('10-s5e2-de-nhap-tai-lieu | S5-E2: trợ lý soạn đề nháp từ tài liệu', async ({ page, request, demo }) => {
  await demo.card('Draft questions from documents', `${S5} · S5-E2 — AI drafts questions from an active document; the geometry engine checks every answer`, 3000, '', 'The assistant can also draft questions from the teacher’s own documents.');
  await demo.step('đề nháp', async () => {
    const w = await world(request, S5E3_WORLD);
    const lan = w.people['Cô Lan']!;
    const c11 = w.classes['11A1']!;
    const doc = await tl01(request, lan, c11.id);
    await signIn(page, lan);
    await page.goto(`/gv/ra-de/tu-tai-lieu/${doc.id}?lop=${c11.id}`);
    await page.getByTestId('s5e2-draft').waitFor();
    await demo.say('The source is an active class document, version two');
    await demo.tap(page.getByTestId('s5e2-draft-run'));
    await page.locator('[data-testid^="s5e2-draft-item-"]').first().waitFor({ timeout: 20_000 });
    await demo.say('Questions the engine can build come with a computed answer; unverified ones go last and are not pre-selected');
    await page.mouse.wheel(0, 500);
    await demo.hold(1800);
  });
});

test('11-s5e3-kho-tri-thuc-hoi-dap | S5-E3: kho tri thức và hỏi đáp có nguồn', async ({ page, request, demo }) => {
  await demo.card('Knowledge base and sourced Q&A', `${S5} · S5-E3 — versioned teacher documents; students ask, the AI answers with a cited source`, 3000, '', 'Stage five also adds a knowledge base, and question answering with sources.');
  const c = await qaSetup(request);
  await demo.step('kho tri thức', async () => {
    await signIn(page, c.lan);
    await page.goto('/gv/kho-tri-thuc');
    await demo.say('Teachers keep versioned documents: active, waiting for review, or archived');
    await page.mouse.wheel(0, 400);
    await demo.hold(1200);
  });
  await demo.step('hỏi đáp', async () => {
    await signIn(page, c.an);
    await page.goto(`/hs/hoi-dap?bai=${c.bc.id}`);
    await demo.say('Students can ask within the scope of the assignment and the help level the teacher allows');
    await page.getByTestId('s5e3-qa-question').pressSequentially(Q_SC1, { delay: 25 });
    await demo.tap(page.getByTestId('s5e3-qa-send'));
    await page.getByTestId('s5e3-qa-answer').waitFor();
    await demo.say('The answer explains the concept without solving the problem, and cites its source');
    await demo.tap(page.getByTestId('s5e3-qa-source'));
    await page.getByTestId('s5e3-qa-reader').waitFor();
    await demo.say('Tapping the source opens exactly the section that was quoted');
  });
});

test('12-s5e4-rubric-nop-bai | S5-E4: rubric và học sinh chụp nộp bài', async ({ page, request, demo }) => {
  await demo.card('Rubrics and paper submissions', `${S5} · S5-E4 — teacher writes a rubric; students photograph each page of their work and submit`, 3000, TRIAL, 'Handwritten work. The teacher writes a rubric, and students photograph their pages.');
  await demo.step('rubric', async () => {
    const w = await s5e4World(request);
    const lan = w.people['Cô Lan']!;
    const rb = await rb01(request, lan);
    await signIn(page, lan);
    await page.goto(`/gv/rubric?id=${rb.id}`);
    await demo.trial(true);
    await page.getByTestId('s5e4-editor').waitFor();
    await demo.say('A five-criterion rubric with a live total: ten out of ten');
    await page.getByTestId('s5e4-crit-points-2').fill('3');
    await demo.say('If the points do not add up to ten, a warning appears and saving is blocked');
    await page.getByTestId('s5e4-crit-points-2').fill('2');
    await demo.hold(1000);
  });
  await demo.step('nộp bài', async () => {
    const { w, bc } = await s5e4Setup(request);
    const an = w.people['Bình An']!;
    await ack(request, an);
    await signIn(page, an);
    await page.goto(`/hs/nop-bai?bc=${bc.id}`);
    await demo.trial(true);
    await page.getByTestId('s5e4-hs-assignment').waitFor();
    await demo.say('The student opens a graded assignment');
    await demo.tap(page.getByTestId('s5e4-start'));
    await shoot(page, 'bn01-p1.jpg');
    await demo.say('Page one is photographed, and checked for sharpness before it is accepted');
    await shoot(page, 'bn01-p2.jpg');
    await demo.say('Page two. Pages can be reordered if needed.');
    await page.getByTestId('s5e4-page-2').locator('[data-uploaded="true"]').or(page.locator('[data-testid="s5e4-page-2"][data-uploaded="true"]')).waitFor();
    await demo.tap(page.getByTestId('s5e4-submit'));
    await page.getByTestId('s5e4-submitted-msg').waitFor();
    await demo.say('Submitted, with a confirmation that shows the server time');
  });
});

test('13-s5e4-hang-cho-cham-tay | S5-E4: hàng chờ chấm và chấm tay theo rubric', async ({ page, request, demo }) => {
  await demo.card('Grading queue and manual grading', `${S5} · S5-E4 — flagged work first; photo on the left, rubric on the right, shortcut A to approve`, 3000, TRIAL, 'Then the teacher grades, from a queue.');
  await demo.step('hàng chờ', async () => {
    const c = await gradingWorld(request);
    await sc1(request, c);
    await signIn(page, c.lan);
    await page.goto('/gv/hang-cho');
    await demo.trial(true);
    await page.getByTestId('s5e4-queue-class').selectOption(c.c11.id);
    await page.getByTestId('s5e4-queue-assignment').selectOption(c.bc.id);
    await page.getByTestId('s5e4-queue-summary').waitFor();
    await demo.say('Flagged submissions, such as unreadable or late work, come first. There is no ranking column.');
    await demo.mark(page.locator('.s5e4-queue-row').first(), 1500);
  });
  await demo.step('chấm tay', async () => {
    const c = await gradingWorld(request);
    const an = c.w.people['Bình An']!;
    const mai = c.w.people['Chi Mai']!;
    await ack(request, an);
    await submitOk(request, an, c.bc.id, [P1, P2]);
    await ack(request, mai);
    const bn02 = await submitOk(request, mai, c.bc.id, [P1, P2, P2]);
    await signIn(page, c.lan);
    await page.goto(`/gv/cham/${bn02.id}`);
    await demo.trial(true);
    await page.getByTestId('s5e4-review-rubric').waitFor();
    await demo.say('The student’s pages on the left, the rubric on the right');
    const pts = ['2', '3', '0', '1', '1'];
    for (let k = 0; k < 5; k++) {
      await page.getByTestId(`s5e4-crit-input-${k + 1}`).fill(pts[k]!);
      await demo.hold(350);
    }
    await demo.mark(page.getByTestId('s5e4-review-total'), 1000);
    await demo.say('The total is computed automatically: seven out of ten');
    await page.getByTestId('s5e4-review-comment').pressSequentially('Xem lại cách tính SB', { delay: 50 });
    await demo.tap(page.getByTestId('s5e4-review-approve'));
    await page.getByTestId('s5e4-review-toast').waitFor();
    await demo.say('After approval, the next submission in the queue opens automatically');
  });
});

test('14-s5e5-thong-bao-cong-tac | S5-E5: thông báo do trợ lý soạn, công tắc AI và hạn mức', async ({ page, request, demo }) => {
  await demo.card('Class notices, AI switch and usage', `${S5} · S5-E5 — AI drafts a notice, the teacher edits and sends; AI on or off per class; usage over 30 days`, 3000, '', 'The assistant also drafts class notices, and every class has its own AI switch.');
  await demo.step('thông báo', async () => {
    const w = await s5e5World(request);
    const lan = w.people['Cô Lan']!;
    await s5e5bc01(request, lan, w.classes['11A1']!.id);
    await signIn(page, lan);
    await page.goto('/gv/tro-ly/thong-bao');
    await page.getByTestId('s5e5-notice-page').waitFor();
    await page.getByTestId('s5e5-class').selectOption({ label: '11A1' });
    await page.getByTestId('s5e5-kind').selectOption('due');
    await demo.say('Pick a class and a notice type, here a due-date reminder');
    await demo.tap(page.getByTestId('s5e5-compose'));
    await page.getByTestId('s5e5-label').waitFor();
    await demo.say('An AI draft with the correct local due time. The teacher edits the last sentence.');
    const body = page.getByTestId('s5e5-body');
    const text = await body.inputValue();
    await body.fill(text.replace(/Em nào cần hỏi thêm.*$/, 'Em nào nộp sớm cô sẽ chữa trước giờ học.'));
    await demo.hold(1200);
    await demo.tap(page.getByTestId('s5e5-send'));
    await page.getByTestId('s5e5-toast').waitFor();
    await demo.say('Sent to the eight active students. A student who left the class gets nothing.');
    const ai = page.getByTestId('s5e5-ai-class');
    await ai.scrollIntoViewIfNeeded();
    await demo.mark(ai, 800);
    await demo.say('Per class, AI grading and the assistant can be switched on or off, with thirty-day usage');
    await demo.mark(page.getByTestId('s5e5-usage'), 1500);
  });
});

test('15-s5e5-loi-sai-lop | S5-E5: tổng hợp lỗi sai của lớp và bộ luyện nhắc lại', async ({ page, request, demo }) => {
  await demo.card('Where the class goes wrong', `${S5} · S5-E5 — common errors from graded work, with evidence; a follow-up practice set`, 3000, TRIAL, 'From graded work, the assistant summarises where the whole class goes wrong.');
  await demo.step('lỗi của lớp', async () => {
    const c = await graded(request);
    await summary(request, c.lan, c.bc.id);
    await signIn(page, c.lan);
    await page.goto(`/gv/bai-co-cham/${c.bc.id}/loi-cua-lop`);
    await demo.trial(true);
    await page.getByTestId('s5e5-insight-1').waitFor({ timeout: 20_000 });
    await demo.say('Each insight lists its evidence, by submission. No rankings and no percentages.');
    await demo.mark(page.getByTestId('s5e5-errors-raw'), 1500);
    await demo.tap(page.getByTestId('s5e5-remind-1'));
    await page.getByTestId('s5e5-remind').waitFor();
    await demo.say('It suggests up to three practice questions on the same topic, plus related document sections');
    await demo.tap(page.getByTestId('s5e5-evidence-1-2'));
    await page.getByTestId('s5e4-review').waitFor();
    await demo.say('Clicking the evidence opens that student’s actual work');
  });
});

async function kpiReport(request: APIRequestContext) {
  const w = await s5e4World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  await hook(request, 's5e6/setup', { orgId: w.orgId });
  await aiOn(request, lan, c11.id);
  const MAX = [2, 4, 2, 1, 1];
  const p = (gv: number[], ai: number[], topic = 'khoảng cách') => ({ topic, criteria: MAX.map((max, i) => ({ max, gv: gv[i]!, ai: ai[i]! })), resultOk: true });
  const papers = [
    ...Array.from({ length: 15 }, (_, i) => p(MAX, MAX, i % 2 ? 'thể tích' : 'khoảng cách')),
    ...Array.from({ length: 5 }, () => p([2, 2, 2, 1, 1], [2, 3, 2, 1, 1])),
  ];
  await hook(request, 's5e6/kpi-seed', { orgId: w.orgId, classId: c11.id, papers });
  const r = await request.get(`${API}/api/v1/teacher/my-classes/${c11.id}/ai-grading/report`, { headers: bearer(lan.token) });
  return (await r.json()) as Record<string, unknown>;
}

test('16-s5e6-ai-de-xuat-diem | S5-E6: AI đề xuất điểm, giáo viên nhận từng tiêu chí', async ({ page, request, demo }) => {
  await demo.card('AI-proposed grades', `${S5} · S5-E6 — AI proposes a grade per criterion and marks the region it used; the teacher accepts, edits or drops each`, 3000, TRIAL, 'Finally, AI-proposed grading.');
  await demo.step('đề xuất', async () => {
    const c = await s5e6Setup(request, 'ai_proposal');
    await mock(request, c.w.people['Bình An']!, ['F-S5-090']);
    const { sub } = await s5e6Submit(request, c, 'Bình An');
    await waitAi(request, c.lan, sub.id);
    await signIn(page, c.lan);
    await page.goto(`/gv/cham-ai/${sub.id}`);
    await demo.trial(true);
    await page.getByTestId('s5e6-ai-points-2').waitFor();
    await demo.say('Each criterion shows the AI’s proposed points. Approve stays disabled until every criterion is handled.');
    await demo.tap(page.getByTestId('s5e6-crit-name-2'));
    await page.getByTestId('s5e6-region').waitFor();
    await demo.say('Click a criterion to see the region of the page the AI relied on');
    for (let k = 1; k <= 5; k++) {
      await page.getByTestId(`s5e6-accept-${k}`).click();
      await demo.hold(450);
    }
    await demo.say('Accept each criterion. The total is ten out of ten.');
    await demo.tap(page.getByTestId('s5e6-approve'));
    await demo.say('Approved. The final grade is always the teacher’s decision.');
  });
});

test('17-s5e6-song-song-bao-cao | S5-E6: chạy song song và báo cáo khớp AI–GV', async ({ page, request, demo }) => {
  await demo.card('Parallel mode and agreement report', `${S5} · S5-E6 — AI grades silently while the teacher grades; compare afterwards; a KPI gate decides when AI proposals open`, 3000, TRIAL, 'Before AI proposals are switched on for a class, the AI runs in parallel.');
  await demo.step('song song', async () => {
    const c = await s5e6Setup(request, 'parallel');
    await mock(request, c.w.people['Chi Mai']!, ['F-S5-091']);
    const { sub } = await s5e6Submit(request, c, 'Chi Mai');
    await waitAi(request, c.lan, sub.id);
    await signIn(page, c.lan);
    await page.goto(`/gv/cham-ai/${sub.id}`);
    await demo.trial(true);
    await page.getByTestId('s5e6-shadow-msg').waitFor();
    await demo.say('In parallel mode the AI score stays hidden while the teacher grades, to avoid anchoring');
    const pts = ['2', '2,5', '0', '1', '0,5'];
    for (let k = 1; k <= 5; k++) {
      await page.getByTestId(`s5e6-input-${k}`).fill(pts[k - 1]!);
      await demo.hold(350);
    }
    await demo.tap(page.getByTestId('s5e6-approve'));
    await page.getByTestId('s5e6-comparison').waitFor();
    await page.getByTestId('s5e6-comparison').scrollIntoViewIfNeeded();
    await demo.say('After approval, AI and teacher points are compared criterion by criterion');
  });
  await demo.step('báo cáo khớp', async () => {
    const r = await kpiReport(request);
    const m = (r.metrics ?? {}) as Record<string, { k?: number; n?: number; value?: number | null; lower?: number | null }>;
    const pct = (v: number | null | undefined) => (v == null ? '—' : `${(v * 100).toFixed(1).replace('.', ',')} %`);
    const rows = (['b', 'c', 'f', 'h'] as const)
      .filter((k) => m[k])
      .map((k) => `<tr><td>(${k})</td><td>${m[k]!.k ?? ''}/${m[k]!.n ?? ''}</td><td>${pct(m[k]!.value)}</td><td>${pct(m[k]!.lower)}</td></tr>`)
      .join('');
    const reasons = ((r.reasons as string[]) ?? []).map((x) => `<li>${x}</li>`).join('');
    await demo.card('AI–teacher agreement · class ' + String(r.className ?? ''), `${String(r.n_bai)} papers · ${String(r.n_cap)} criterion pairs · AI-proposal gate: ${r.gateOpen ? 'OPEN' : 'CLOSED'}`, 400, 'Report data from the /ai-grading/report API (shown as a table for this demo)');
    await page.evaluate(
      ([rowsHtml, reasonsHtml]) => {
        const d = document.createElement('div');
        d.style.cssText = 'margin-top:28px;background:#fff;color:#0f172a;border-radius:14px;padding:20px 28px;font:22px system-ui;min-width:900px';
        d.innerHTML = `<table style="width:100%;border-collapse:collapse"><tr style="text-align:left;color:#475569"><th>KPI</th><th>k/n</th><th>Rate</th><th>95 % lower bound</th></tr>${rowsHtml}</table>${reasonsHtml ? `<ul style="font-size:18px;color:#334155">${reasonsHtml}</ul>` : ''}<p style="font-size:18px;color:#475569;margin:10px 0 0">(b) criterion pairs within ±0.25 points · (c) result direction matches the engine · the Wilson 95 % lower bound vs. the KPI threshold decides the gate.</p>`;
        document.body.appendChild(d);
      },
      [rows, reasons] as const,
    );
    await demo.say('The agreement report counts how often AI and teacher points fall within a quarter point, with a ninety-five percent confidence bound. The gate for AI proposals opens only when every K P I is met.', 4000);
  });
});
