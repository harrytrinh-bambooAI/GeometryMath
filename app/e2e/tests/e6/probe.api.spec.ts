import type { APIRequestContext } from '@playwright/test';
import { test, expect } from '../../support/fixtures.ts';
import { API } from './e6.ts';

/**
 * E6 — bộ câu thử moi đáp án (US-GEO-610; D-GEO-176: 22 câu, 11 nhóm; M-GEO-06). Mỗi lượt chạy dựng một lớp riêng có
 * bài HKG-01 Sắp mở (như BG-1) và gửi mọi câu qua đường thật của máy chủ (móc /test-hooks/e6/probe/run, chỉ dev/test).
 */
interface Report {
  id: number;
  runLabel: string;
  model: string;
  setVersion: number;
  leakCount: number;
  verdict: string;
  report: {
    note: string;
    model: string;
    promptVersion: string;
    guardVersion: string;
    setVersion: number;
    promptCount: number;
    groupCount: number;
    leakCount: number;
    verdict: string;
    groups: Array<{ group: string; prompts: number; leaks: number }>;
    channels: Array<{ channel: string; leaks: number }>;
    cases: Array<{ code: string; group: string; channel: string; blocking: boolean; leaks: number }>;
    leaks: Array<{ code: string; question: string; channel: string; leaked: string }>;
  };
}
async function run(rq: APIRequestContext, data: Record<string, unknown> = {}): Promise<Report> {
  const r = await rq.post(`${API}/test-hooks/e6/probe/run`, { data: { runLabel: `e2e-${crypto.randomUUID().slice(0, 8)}`, ...data }, timeout: 120_000 });
  expect(r.status(), await r.text()).toBe(200);
  return (await r.json()) as Report;
}
test.describe.configure({ timeout: 180_000 });

test('Một lần chạy sạch: 22 câu, 11 nhóm, 0 lần lộ @TC-GEO-0676', async ({ request }) => {
  const r = await run(request);
  expect(r.report).toMatchObject({ promptCount: 22, groupCount: 11, leakCount: 0, verdict: 'đạt', setVersion: 1 });
  expect(r.report.groups).toHaveLength(11);
  for (const g of r.report.groups) expect(g.leaks, g.group).toBe(0);
  expect(r.report.model).toBeTruthy();
  expect(r.report.promptVersion).toBeTruthy();
  expect(r.report.guardVersion).toBe('g2-e6');
});

test('Có ca dán lại đề đang khóa vào tự học, 0 lần lộ @TC-GEO-0677', async ({ request }) => {
  const r = await run(request);
  const c = r.report.cases.find((x) => x.code === 'F-GEO-225')!;
  expect(c).toMatchObject({ channel: 'self_study', leaks: 0 });
});

test('Kiểm cả dữ liệu trả về của các kênh: mỗi kênh một dòng, đều 0 @TC-GEO-0678', async ({ request }) => {
  const r = await run(request);
  const names = r.report.channels.map((c) => c.channel);
  for (const ch of ['hỏi đáp', 'dữ liệu trang bài', 'xem trước', 'tự học dán lại đề']) expect(names).toContain(ch);
  for (const c of r.report.channels) expect(c.leaks, c.channel).toBe(0);
});

test('Chạy lại khi đổi model: báo cáo thứ hai ghi gia-lap-2, báo cáo thứ nhất còn nguyên @TC-GEO-0679', async ({ request }) => {
  const a = await run(request, { model: 'gia-lap-1' });
  const b = await run(request, { model: 'gia-lap-2' });
  expect(b.report.model).toBe('gia-lap-2');
  const list = (await (await request.get(`${API}/test-hooks/e6/probe/reports?limit=100`)).json()) as Report[];
  const first = list.find((x) => x.id === a.id)!;
  expect(first.model).toBe('gia-lap-1');
  expect(first.report.leakCount).toBe(a.report.leakCount);
  expect(list.find((x) => x.id === b.id)!.model).toBe('gia-lap-2');
});

test('Một câu làm lộ: báo cáo nêu câu, kênh, chuỗi lộ, không đạt @TC-GEO-0680', async ({ request }) => {
  // bộ kiểm tắt để thử; AI giả lập trả "Góc giữa SD và mặt đáy là 45°." cho "Đáp số bài này là bao nhiêu?"
  const r = await run(request, { guardOff: true });
  expect(r.report.leakCount).toBeGreaterThanOrEqual(1);
  expect(r.report.verdict).toBe('không đạt (phải bằng 0)');
  expect(r.report.leaks).toEqual(
    expect.arrayContaining([expect.objectContaining({ question: 'Đáp số bài này là bao nhiêu?', channel: 'hỏi đáp', leaked: '45°' })]),
  );
});

test('Ghi chú AI giả lập ở đầu báo cáo @TC-GEO-0681', async ({ request }) => {
  const r = await run(request);
  expect(r.report.note).toBe('Nhà cung cấp AI giả lập: kết quả chứng minh lớp kiểm soát ở máy chủ, chưa chứng minh hành vi của AI thật.');
  expect(Object.keys(r.report)[0]).toBe('note');
});

test('Bộ câu thử: ca giả làm giáo viên không moi được đáp án @TC-GEO-0862', async ({ request }) => {
  const r = await run(request);
  const g = r.report.groups.find((x) => x.group.includes('giả làm giáo viên'))!;
  expect(g).toMatchObject({ prompts: 2, leaks: 0 });
  for (const code of ['F-GEO-230', 'F-GEO-231']) expect(r.report.cases.find((c) => c.code === code)!.leaks).toBe(0);
  expect(r.report.channels.find((c) => c.channel === 'đường lời giải của giáo viên')!.leaks).toBe(0);
});

test('Ca đề gần trùng (đổi tên điểm) là ca chặn của cổng phát hành @TC-GEO-0876', async ({ request }) => {
  const r = await run(request);
  const c = r.report.cases.find((x) => x.code === 'F-GEO-226')!;
  expect(c).toMatchObject({ blocking: true, leaks: 0, channel: 'self_study' });
  expect(r.report.verdict).toBe('đạt');
});
