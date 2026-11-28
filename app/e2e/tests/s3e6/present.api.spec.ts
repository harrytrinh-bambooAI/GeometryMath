import { expect, test } from '../../support/fixtures.ts';
import { NAMES, board051, student } from '../s3e3/s3e3.ts';
import { present, setRelease } from './s3e6.ts';

/** US-S3E6-01 qua API: phản hồi chiếu không có dữ liệu học sinh, gộp lần bấm, quyền. */

interface Presentation {
  sessionId: string;
  title: string;
  statement: string;
  steps: string[];
  startStep: number;
  locked: boolean;
}

test('Màn chiếu không có tên hay mã học sinh @TC-S3E6-03', async ({ request }) => {
  const sb = await board051(request);
  await setRelease(request, sb.classes[0]!.sets[0]!.id, -60);
  const r = await present(request, sb, sb.teacher.token, 3, 2);
  expect(r.status()).toBe(200);
  const text = await r.text();
  const p = JSON.parse(text) as Presentation;
  expect(Object.keys(p).sort()).toEqual(['locked', 'sessionId', 'startStep', 'statement', 'steps', 'title']);
  expect(p.steps.length).toBeGreaterThan(0);
  expect(text).not.toMatch(/HS-[A-Z0-9]{4}-[A-Z0-9]{4}/);
  for (const name of NAMES) expect(text).not.toContain(name);
  expect(text).not.toMatch(/"(works|students|studentCount|n|count)"/);
});

test('Bấm hai lần trong 1 giây chỉ mở một phiên @TC-S3E6-07', async ({ request }) => {
  const sb = await board051(request);
  const a = (await (await present(request, sb, sb.teacher.token, 3, 2)).json()) as Presentation;
  await new Promise((res) => setTimeout(res, 1000));
  const b = (await (await present(request, sb, sb.teacher.token, 3, 2)).json()) as Presentation;
  expect(b.sessionId).toBe(a.sessionId); // một phiên, một sự kiện đo (gộp trong 5 giây)
});

test('Giáo viên khác không mở được @TC-S3E6-08', async ({ request }) => {
  const sb = await board051(request);
  const r = await present(request, sb, sb.otherTeacher.token, 3, 2);
  expect(r.status()).toBe(404);
  expect(((await r.json()) as { code: string }).code).toBe('NOT_FOUND');
  // không có phiên của thầy Khôi: chủ lớp mở vẫn ra phiên mới của chính cô
  const own = await present(request, sb, sb.teacher.token, 3, 2);
  expect(own.status()).toBe(200);
});

test('Học sinh không gọi được @TC-S3E6-09', async ({ request }) => {
  const sb = await board051(request);
  const an = student(sb, 'Bình An');
  const r = await present(request, sb, an.token, 3, 2);
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { code: string }).code).toBe('FORBIDDEN');
});
