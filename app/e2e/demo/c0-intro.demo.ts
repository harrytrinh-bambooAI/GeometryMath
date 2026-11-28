import type { Page } from '@playwright/test';
import { test } from './kit.ts';

/**
 * T-261128-73 · Màn mở đầu (sản phẩm + "by BambooAI") và màn kết. Nội dung lấy từ DOMAIN-PROFILE.md §1 (sản phẩm, người
 * dùng, khác biệt) và bảng chặng; về BambooAI chỉ dùng tên (không số liệu, khách hàng, logo — không có tệp logo trong repo).
 */

const SHELL = (inner: string) => `<!doctype html><meta charset="utf-8"><body style="margin:0;height:100vh;display:flex;flex-direction:column;
  align-items:center;justify-content:center;background:linear-gradient(135deg,#0f172a,#1e3a8a);color:#fff;font-family:system-ui,-apple-system,sans-serif">${inner}</body>`;

async function slide(page: Page, html: string) {
  await page.setContent(SHELL(html));
}

const li = (items: string[]) =>
  `<ul style="font-size:34px;line-height:1.6;max-width:1400px;margin:24px 0 0;padding-left:40px">${items.map((x) => `<li>${x}</li>`).join('')}</ul>`;

test('00-intro | Giới thiệu sản phẩm · by BambooAI', async ({ page, demo }) => {
  await slide(
    page,
    `<div style="font:600 28px system-ui;letter-spacing:.14em;color:#93c5fd;text-transform:uppercase">Product demo</div>
     <h1 style="font-size:84px;margin:26px 60px 8px;text-align:center">Hình Học Không Gian Cùng AI</h1>
     <div style="font-size:46px;color:#e2e8f0">Geometry Math Support</div>
     <div style="margin-top:48px;font:600 34px system-ui;color:#86efac">by BambooAI</div>`,
  );
  await demo.say('Geometry Math Support. Solid geometry with A I, by BambooAI.', 2500);
  await slide(
    page,
    `<h2 style="font-size:60px;margin:0">The problem</h2>${li([
      'Solid geometry is hard to picture on a flat page',
      'AI answers can be wrong — and give the solution away too early',
      'Teachers need to stay in control of what students see and when',
    ])}`,
  );
  await demo.say(
    'Solid geometry is hard to picture on paper, and generic AI can be wrong, or give answers away too early.',
  );
  await slide(
    page,
    `<h2 style="font-size:60px;margin:0">Who it is for</h2>${li([
      'Math teachers in lower and upper secondary school (THCS, THPT)',
      'Their students — on laptop, iPad and phone',
    ])}`,
  );
  await demo.say('It is built for secondary school math teachers and their students.');
  await slide(
    page,
    `<h2 style="font-size:60px;margin:0">Principles</h2>${li([
      'Figures and answers are verified by computation, not trusted from AI',
      'Learn first, answer later: hints first, solutions unlock at the teacher’s time',
      'The teacher decides: every AI result is a proposal until a person approves it',
    ])}`,
  );
  await demo.say(
    'Figures and answers are verified by computation. Hints come first, solutions later. And the teacher always decides.',
  );
  await slide(
    page,
    `<h2 style="font-size:60px;margin:0">Roadmap shown in this demo</h2>${li([
      '<b>Stage 1 · MVP</b> — 3D problem bank, assign, hints, class monitor',
      '<b>Stage 2</b> — practice sets with auto-checking, classroom presentation',
      '<b>Stage 3</b> — classes with join codes, class results, teacher overview',
      '<b>Stage 4</b> — a second subject on the same framework (not in this demo)',
      '<b>Stage 5</b> — AI teaching assistant: lesson plans, exams, knowledge base, handwritten grading',
    ])}`,
  );
  await demo.say(
    'This demo covers stages one, two, three and five. All data is sample data, and the AI is simulated.',
  );
});

test('99-outro | Kết thúc', async ({ page, demo }) => {
  await slide(
    page,
    `<h2 style="font-size:64px;margin:0">Summary</h2>${li([
      'Verified 3D geometry for teaching and learning',
      'Hints before answers; solutions unlock on the teacher’s schedule',
      'AI assists with lesson plans, exams, Q&amp;A and grading — the teacher approves',
    ])}
     <div style="margin-top:56px;font-size:40px">Hình Học Không Gian Cùng AI · Geometry Math Support</div>
     <div style="margin-top:20px;font:600 36px system-ui;color:#86efac">BambooAI</div>`,
  );
  await demo.say(
    'Verified geometry, hints before answers, and an AI assistant that leaves the final decision to the teacher. Geometry Math Support, by BambooAI. Thank you for watching.',
  );
  await demo.hold(1500);
});
