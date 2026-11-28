import { expect, test } from '../../support/fixtures.ts';
import { API, aiCalls, bearer, hook } from '../s3e1/s3e1.ts';
import { EIGHT, MSG, NOTICES, aiScenario, bc01, draft, draftOk, notifications, s5e5World, send } from './s5e5.ts';

/**
 * US-S5E5-01 — trợ lý soạn nháp thông báo lớp, giáo viên sửa và tự bấm Gửi (kênh thông báo trong ứng dụng S3-E4);
 * US-S5E5-04-SC6 — báo cáo dùng trợ lý. Máy chủ thật, AI giả lập task class_notice.
 */

type Err = { code: string; message: string; details?: Array<{ field: string; message: string }> };

async function setup(request: Parameters<typeof s5e5World>[0]) {
  const w = await s5e5World(request);
  const lan = w.people['Cô Lan']!;
  const c11 = w.classes['11A1']!;
  const set = await bc01(request, lan, c11.id);
  return { w, lan, c11, setId: set.id };
}

test('Nháp chưa gửi thì học sinh không thấy @TC-S5E5-02', async ({ request }) => {
  const { w, lan, c11, setId } = await setup(request);
  const d = await draftOk(request, lan, c11.id, setId);
  expect(d.status).toBe('draft');
  expect(d.humanLabel).toBe('AI soạn');
  const an = w.people['Bình An']!;
  const mine = await notifications(request, an);
  expect(mine.filter((n) => n.kind === 'class_notice')).toHaveLength(0);
  expect(JSON.stringify(mine)).not.toContain(d.body.slice(0, 20));
});

test('AI chèn tên học sinh vào thông báo thì bị loại khỏi nháp @TC-S5E5-03', async ({ request }) => {
  const { w, lan, c11, setId } = await setup(request);
  await aiScenario(request, lan, 's5e5-name');
  const d = await draftOk(request, lan, c11.id, setId);
  expect(d.body).not.toContain('Chi Mai');
  expect(d.body).toContain('cả lớp nộp bài trước 20:00');
  const flagged = d.unsure.find((u) => u.text.includes('cả lớp nộp bài'));
  expect(flagged?.label).toBe('AI chưa chắc');
  // yêu cầu gửi AI không chứa tên hay mã học sinh nào của 11A1
  const sent = (await (await request.get(`${API}/test-hooks/s5e5/notice-prompts?teacherId=${lan.id}`)).json()) as string[];
  expect(sent.length).toBeGreaterThan(0);
  for (const name of [...EIGHT, 'Văn Tú']) {
    const p = w.people[name]!;
    for (const s of sent) {
      expect(s).not.toContain(p.displayName);
      expect(s).not.toContain(p.userCode);
    }
  }
  await aiScenario(request, lan, '');
});

test('Thông báo đã gửi có dòng "Soạn với trợ lý AI, thầy cô đã duyệt" @TC-S5E5-04', async ({ request }) => {
  const { w, lan, c11, setId } = await setup(request);
  const d = await draftOk(request, lan, c11.id, setId);
  const s = await send(request, lan, d.id);
  expect(s.status(), await s.text()).toBe(200);
  const an = w.people['Bình An']!;
  const n = (await notifications(request, an)).find((x) => x.kind === 'class_notice')!;
  const r = await request.get(`${API}/api/v1/notifications/${n.id}`, { headers: bearer(an.token) });
  expect(r.status()).toBe(200);
  const detail = (await r.json()) as { body: string };
  expect(detail.body).toContain('— Cô Lan');
  expect(detail.body).toContain(MSG.aiLine);
  expect(detail.body).toContain(d.body);
});

test('Lượt soạn thứ 31 trong ngày bị chặn ở trợ lý thông báo @TC-S5E5-05', async ({ request }) => {
  const { w, lan, c11, setId } = await setup(request);
  // 30 lượt soạn hôm nay (giáo án) — móc S5-E1
  await hook(request, 's5e1/turns', { orgId: w.orgId, userId: lan.id, count: 30 });
  const before = await aiCalls(request, lan.id);
  const r = await draft(request, lan, { classId: c11.id, kind: 'due', refId: setId });
  expect(r.status()).toBe(429);
  expect(Number(r.headers()['retry-after'])).toBeGreaterThan(0);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('RATE_LIMITED');
  expect(e.message).toBe(MSG.s06);
  expect(await aiCalls(request, lan.id)).toBe(before);
});

test('AI viết sai giờ hạn thì máy chủ thay bằng giờ đúng @TC-S5E5-06', async ({ request }) => {
  const { lan, c11, setId } = await setup(request);
  await aiScenario(request, lan, 's5e5-wrong-time');
  const d = await draftOk(request, lan, c11.id, setId);
  expect(d.body).toContain('20:00');
  expect(d.body).not.toContain('21:00');
  const flagged = d.unsure.find((u) => u.text.includes('20:00'));
  expect(flagged?.label).toBe('AI chưa chắc');
  expect(flagged?.why).toContain('giờ hạn');
  await aiScenario(request, lan, '');
});

test('Thông báo 1 001 ký tự bị từ chối @TC-S5E5-07', async ({ request }) => {
  const { w, lan, c11, setId } = await setup(request);
  const d = await draftOk(request, lan, c11.id, setId);
  const r = await send(request, lan, d.id, 'a'.repeat(1001));
  expect(r.status()).toBe(400);
  const e = (await r.json()) as Err;
  expect(e.code).toBe('VALIDATION_FAILED');
  expect(e.message).toBe(MSG.s56);
  for (const name of EIGHT) {
    expect((await notifications(request, w.people[name]!)).filter((n) => n.kind === 'class_notice')).toHaveLength(0);
  }
  // biên: 1 000 ký tự được gửi
  const ok = await send(request, lan, d.id, 'b'.repeat(1000));
  expect(ok.status(), await ok.text()).toBe(200);
});

test('Không gửi được tới lớp của người khác @TC-S5E5-08', async ({ request }) => {
  const { w, lan, setId } = await setup(request);
  const b10 = w.classes['10B']!.id;
  const r = await draft(request, lan, { classId: b10, kind: 'due', refId: setId });
  expect(r.status()).toBe(404);
  expect(((await r.json()) as Err).code).toBe('NOT_FOUND');
  const free = await draft(request, lan, { classId: b10, kind: 'free', note: 'Nghỉ học thứ Hai' });
  expect(free.status()).toBe(404);
  // nháp của cô Lan cũng không gửi được bằng tài khoản thầy Khôi
  const mine = await draftOk(request, lan, w.classes['11A1']!.id, setId);
  const other = await send(request, w.people['Thầy Khôi']!, mine.id);
  expect(other.status()).toBe(404);
  const mina = w.people['Mi Na']!;
  expect((await notifications(request, mina)).filter((n) => n.kind === 'class_notice')).toHaveLength(0);
});

test('Báo cáo tỉ lệ nháp trợ lý được duyệt sau khi sửa @TC-S5E5-29', async ({ request }) => {
  const { w, lan, c11, setId } = await setup(request);
  await hook(request, 's5e5/lesson-plans', { orgId: w.orgId, teacherId: lan.id, drafts: 10, approved: 6, edited: 4 });
  for (let i = 0; i < 3; i++) {
    const d = await draftOk(request, lan, c11.id, setId);
    const s = await send(request, lan, d.id, i === 0 ? `${d.body} Cô bổ sung: mang thước kẻ.` : undefined);
    expect(s.status(), await s.text()).toBe(200);
  }
  const r = await request.get(`${API}/api/v1/teacher/copilot/usage?days=30`, { headers: bearer(lan.token) });
  expect(r.status(), await r.text()).toBe(200);
  const u = (await r.json()) as {
    items: Array<{ kind: string; drafts: number; acceptedPct: number; acceptedAfterEditPct: number }>;
  };
  expect(u.items.find((i) => i.kind === 'lesson_plan')).toMatchObject({ drafts: 10, acceptedPct: 60, acceptedAfterEditPct: 40 });
  expect(u.items.find((i) => i.kind === 'notice')).toMatchObject({ drafts: 3, acceptedPct: 100, acceptedAfterEditPct: 33 });
  // học sinh không xem được báo cáo
  const an = await request.get(`${API}/api/v1/teacher/copilot/usage?days=30`, { headers: bearer(w.people['Bình An']!.token) });
  expect(an.status()).toBe(403);
  void NOTICES;
});
