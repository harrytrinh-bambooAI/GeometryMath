import type { Page } from '@playwright/test';
import { test } from './kit.ts';
import { PV as PV5, openBank, signInLane, filterBy } from '../tests/e5/e5.ts';
import { cls, signIn as e6SignIn, twoClasses } from '../tests/e6/e6.ts';
import { PV } from '../tests/e1/e1.ts';
import { openWorkspace, sandbox, tab } from '../tests/e4/e4.ts';
import { asg, box, golden, signInAs } from '../tests/e7/e7.ts';

/** T-261128-73 · Chặng 1 (MVP): kho đề + chi tiết đề + giao bài; học sinh vào lớp, làm bài trên hình 3D, gợi ý; theo dõi lớp. */

async function drag(page: Page, testId: string, dx = 160, dy = 40) {
  const b = await page.getByTestId(testId).first().boundingBox();
  if (!b) return;
  const cx = b.x + b.width / 2;
  const cy = b.y + b.height / 2;
  await page.mouse.move(cx, cy);
  await page.mouse.down();
  await page.mouse.move(cx + dx, cy + dy, { steps: 30 });
  await page.mouse.up();
}

test('01-gv-kho-de-giao-bai | Giáo viên: Kho đề, chi tiết đề, giao bài', async ({ page, request, demo }) => {
  await demo.card('Problem bank → Assign', 'Stage 1 · MVP — teacher finds a problem, checks the 3D figure and solution, assigns it', 2800, '', 'Stage one, the M V P. A teacher finds a problem, checks the figure and the solution, and assigns it to a class.');
  await signInLane(page, request);
  await demo.step('kho đề', async () => {
    await openBank(page);
    await demo.say('The problem bank: solid geometry problems, labelled “verified” by the geometry engine');
    await demo.tap(page.getByTestId('e5-filter-level-thpt'));
    await demo.say('Filter by school level, solid type and topic');
    await filterBy(page, 'topic', 'angle');
    await demo.say('Upper secondary, angle problems', 1500);
  });
  await demo.step('chi tiết đề', async () => {
    await page.goto(`/gv/de/${PV5('HKG-01')}`);
    await page.getByTestId('e2-problem-detail').waitFor();
    await demo.say('Each problem has its statement, a 3D figure, the answer and a step-by-step solution');
    await drag(page, 'viewer', 200, 30);
    await demo.say('The figure rotates with a mouse or a finger');
    await demo.mark(page.getByTestId('e5-answer'), 1200);
    await page.mouse.wheel(0, 600);
    await demo.say('The answer matches the engine’s own computation');
  });
  await demo.step('giao bài', async () => {
    const sb = await twoClasses(request);
    await e6SignIn(page, sb.teacher, 'Giáo viên mẫu', 'teacher');
    await page.goto(`/gv/giao-bai/${PV('HKG-06')}`);
    await page.getByTestId('e6-assign').waitFor();
    await demo.say('To assign: pick a class, allow hints or questions, and set when the solution unlocks');
    await demo.tap(page.getByTestId('e6-class-11A1'));
    await demo.hold(800);
    await demo.tap(page.getByTestId('e6-assign-submit'));
    await page.getByTestId('e6-assign-done').waitFor();
    await demo.say('Assigned to class 11A1');
    void cls;
  });
});

test('02-hs-vao-lop-lam-bai | Học sinh: vào lớp, làm bài trên hình 3D, xem gợi ý', async ({ page, request, demo }) => {
  await demo.card('Student: join, solve, hints', 'Stage 1 · MVP — join with a name and class code, work on the 3D figure, ask for hints step by step', 2800, '', 'Now the student side. Students join with a name and a class code, then work directly on the 3D figure.');
  await demo.step('vào lớp', async () => {
    await page.goto('/hs/vao-lop');
    await demo.say('No account needed: just a display name and the class code');
    await page.getByTestId('e1-join-name').pressSequentially('An', { delay: 120 });
    await page.getByTestId('e1-join-code').pressSequentially('HK7M2A', { delay: 120 });
    await demo.tap(page.getByTestId('e1-join-submit'));
    await page.waitForURL(/\/hs\/bai-cua-em$/);
    await demo.say('“My work” lists everything the teacher has assigned');
  });
  await demo.step('làm bài', async () => {
    const sb = await sandbox(request, [{ students: ['Hà'], assignments: [{ key: 'BG-1', code: 'HKG-01' }] }]);
    await openWorkspace(page, sb.classes[0]!.students['Hà']!, sb.classes[0]!.assignments['BG-1']!, 'Hà');
    await demo.say('The workspace: problem on the left, interactive 3D figure on the right');
    await drag(page, 'viewer', 220, 20);
    await demo.say('Rotate the figure to see the spatial relations');
    await tab(page, 'hint');
    await demo.say('Hints come in levels, so the answer is not given away');
    await demo.tap(page.getByTestId('e4-hint-next'));
    await page.getByTestId('e4-hint-1-body').waitFor();
    await demo.say('Hint one points the student in the right direction');
  });
});

test('03-gv-theo-doi-lop | Giáo viên: theo dõi lớp theo thời gian thực', async ({ page, request, demo }) => {
  await demo.card('Class monitor', 'Stage 1 · MVP — who opened the problem, who used hints, who is stuck', 2800, '', 'Back to the teacher, who can follow the whole class live.');
  await demo.step('theo dõi', async () => {
    const b = await box(request, golden());
    await signInAs(page, b.teacher, 'Giáo viên mẫu', 'teacher');
    await page.goto(`/gv/theo-doi/${asg(b, 'BG-1')}`);
    await page.getByTestId('e7-monitor').waitFor();
    await demo.say('Four summary cards: opened, hints used, questions asked, and time spent on the figure');
    await demo.mark(page.getByTestId('e7-card-hints'), 1200);
    await page.mouse.wheel(0, 400);
    await demo.say('Per student: status, time spent and the hint level reached. No rankings.');
  });
});
