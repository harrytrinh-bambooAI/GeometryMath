import { test } from './kit.ts';
import { assigned, openPlay, sandbox as s2e8Sandbox, signIn as s2e8SignIn, submit as s2e8Submit } from '../tests/s2e8/s2e8.ts';
import { present } from '../tests/s2e7/s2e7.ts';
import { signIn as s3SignIn, world } from '../tests/s3e1/s3e1.ts';
import { board051, openBoard } from '../tests/s3e3/s3e3.ts';
import { ACTIVE_8, assignedSet, fullWorld, hoursRound, interact, klass, lan, open, who } from '../tests/s3e4/s3e4.ts';

/** T-261128-73 · Chặng 2 (soạn bài luyện, học sinh luyện, trình chiếu) và chặng 3 (lớp học, bảng kết quả, tổng quan). */

const FIVE = ['LC-01', 'SP-01', 'LA-01', 'LB-01', 'HCN-01'];

test('04-gv-hs-bai-luyen | Chặng 2: soạn bài luyện, học sinh luyện và nhận phản hồi', async ({ page, request, demo }) => {
  await demo.card('Auto-checked practice sets', 'Stage 2 — teacher picks problems with verified answers; students answer on a math keypad and get instant feedback', 2800, '', 'Stage two adds practice sets that are checked automatically.');
  await demo.step('soạn bài luyện', async () => {
    const sb = await s2e8Sandbox(request);
    await s2e8SignIn(page, sb.teacher, 'TEACHER', 'Cô Hạnh');
    await page.goto('/gv/bai-luyen/moi');
    await page.getByTestId('s2e8-compose').waitFor();
    await demo.say('The teacher ticks problems whose answers the engine can check');
    for (const c of FIVE) {
      await page.getByTestId(`s2e8-problem-tick-${c}`).check();
      await demo.hold(350);
    }
    await page.getByTestId('s2e8-compose-title').pressSequentially('Góc trong hình chóp', { delay: 60 });
    await demo.tap(page.getByTestId('s2e8-compose-save'));
    await page.getByTestId('s2e8-compose-saved').waitFor();
    await demo.say('Five problems saved as a draft, ready to assign');
  });
  await demo.step('học sinh luyện', async () => {
    const sb = await s2e8Sandbox(request);
    const set = await assigned(request, sb);
    await openPlay(page, sb, set.id);
    await demo.say('Students see the figure, the question, a math keypad, and three attempts per question');
    await s2e8Submit(page, '7');
    await demo.say('A wrong answer is marked at once, with a level-one hint and one attempt used');
    await s2e8Submit(page, '6');
    await demo.say('A correct answer is confirmed, and the student moves on');
  });
});

test('05-gv-trinh-chieu | Chặng 2: trình chiếu lời giải từng bước trên lớp', async ({ page, request, demo }, info) => {
  await demo.card('Classroom presentation', 'Stage 2 — big figure, solution covered, revealed one step at a time', 2800, '', 'Teachers can also present a problem in class.');
  await demo.step('trình chiếu', async () => {
    await present(page, request, info, 1);
    await demo.say('Full-screen view: a large figure, with the solution still covered');
    await demo.tap(page.getByTestId('s2e7-next'));
    await demo.say('Reveal step one, and the figure highlights exactly the elements it uses');
    await page.evaluate(() => (document.activeElement as HTMLElement | null)?.blur());
    await page.keyboard.press('ArrowRight');
    await demo.say('The arrow key reveals the next step');
    await page.keyboard.press('ArrowRight');
    await demo.hold(1500);
  });
});

test('06-gv-lop-hoc-ket-qua | Chặng 3: lớp học, duyệt học sinh, bảng kết quả lớp × bài', async ({ page, request, demo }) => {
  await demo.card('Classes and results', 'Stage 3 — classes with join codes, approving students, a class-by-problem results board', 2800, '', 'Stage three is about running real classes.');
  await demo.step('lớp học', async () => {
    const w = await world(request);
    await s3SignIn(page, w.people['Cô Lan']!);
    await page.goto('/gv/lop-hoc');
    await demo.say('Each class has its own join code');
    await page.goto(`/gv/lop-hoc/${w.classes['11A1']!.id}`);
    await page.getByTestId('s3e1-class-name').waitFor();
    await demo.say('Inside a class: active students, students waiting for approval, and those who left');
    await page.mouse.wheel(0, 500);
    await demo.say('The teacher approves or rejects join requests, and can reset a temporary password');
  });
  await demo.step('bảng kết quả', async () => {
    const sb = await board051(request);
    await openBoard(page, sb);
    await demo.say('The results board: one cell per student and per problem');
    const cell = page.getByTestId('s3e3-cell').first();
    await demo.mark(cell, 1200);
    await demo.say('Open a cell to see the evidence: attempts and hints used');
  });
});

test('07-gv-tong-quan | Chặng 3: Tổng quan giáo viên', async ({ page, request, demo }) => {
  await demo.card('Teacher overview', 'Stage 3 — assigned work, locked solutions and pending students on one screen', 2800, '', 'And a single overview page for the teacher.');
  await demo.step('tổng quan', async () => {
    const w = await fullWorld(request);
    const due = hoursRound(7 * 24);
    const release = hoursRound(9 * 24);
    const a1 = await assignedSet(request, lan(w), klass(w, '11A1').id, { title: 'Bộ bài tuần 15', items: [1, 2, 3, 4], dueAt: due, releaseAt: release });
    for (const name of ACTIVE_8.filter((n) => n !== 'Khánh Linh')) await interact(request, who(w, name), a1.items[0]!.assignmentId!);
    await open(page, lan(w), '/gv/tong-quan');
    await demo.say('Three cards: assigned sets and who has not opened them, solutions still locked, and students waiting');
    await demo.mark(page.getByTestId('s3e4-card-assigned'), 1500);
    await demo.mark(page.getByTestId('s3e4-card-pending').or(page.getByTestId('s3e4-pending-total')), 1500);
  });
});
