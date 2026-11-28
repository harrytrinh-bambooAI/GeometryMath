import { expect, test } from '../../support/fixtures.ts';
import { captureFinalState } from '../../support/final-state.ts';
import { API, BL1_ANSWERS, assigned, attempt, bearer, exhaust, sandbox, signIn, student, walk } from './s2e8.ts';

/**
 * US-S2E8-04, ca LOẠI B của `result.p0m.ui.spec.ts` (DECISIONS 27/11 "Chiến lược E2E", đợt 2 E2E-SPLIT2): bố cục màn kết
 * quả (máy tính + điện thoại) do ca loại A 075 giữ; ca này chỉ kiểm dữ liệu "không điểm, phần trăm, xếp hạng" bằng gọi
 * API, cuối luồng mở màn kết quả (không click), kiểm chữ trên màn và chụp 1 ảnh. Mã TC giữ nguyên trong tiêu đề.
 */

test('@TC-S2E8-076 @loai-B không có điểm, phần trăm hay xếp hạng', async ({ request, browser }, testInfo) => {
  const sb = await sandbox(request);
  const set = await assigned(request, sb);
  const an = student(sb, 'An');
  for (const [i, p] of (['ok', 'out', 'ok', 'ok', 'out'] as const).entries()) {
    if (p === 'ok') await attempt(request, an.token, set.items[i]!.assignmentId, BL1_ANSWERS[i]!);
    else await exhaust(request, an.token, set.items[i]!);
  }
  const r = await request.get(`${API}/api/v1/student/practice-sets/${set.id}/result`, { headers: bearer(an.token) });
  expect(r.status()).toBe(200);
  const body = (await r.json()) as Record<string, unknown>;
  const { keys, strings } = walk(body);
  expect(keys.filter((k) => /score|point|percent|rank|total|grade|diem/i.test(k)), 'khóa kiểu điểm / tổng / hạng').toEqual([]);
  for (const s of strings) {
    if (/^[0-9a-f]{8}-[0-9a-f]{4}-/.test(s) || /^\d{4}-\d{2}-\d{2}T/.test(s) || s.startsWith('Lời giải mở lúc')) continue;
    expect(s).not.toMatch(/điểm|%|\/5|hạng/i);
  }
  // chỉ có hai số: câu đúng và câu cần xem lại (và số câu chưa làm = 0)
  expect(Object.entries(body).filter(([, v]) => typeof v === 'number')).toEqual([
    ['correct', 3],
    ['review', 2],
    ['remaining', 0],
  ]);
  await captureFinalState(browser, testInfo, async (page) => {
    await signIn(page, an, 'STUDENT', 'An');
    await page.goto(`/hs/bai-luyen/${set.id}/ket-qua`);
    await expect(page.getByTestId('s2e8-result')).toBeVisible();
    const text = await page.getByTestId('s2e8-result').innerText();
    for (const banned of ['điểm', 'Điểm', '%', '/5', 'hạng', 'xếp hạng']) expect(text, `màn kết quả có "${banned}"`).not.toContain(banned);
    expect(text).not.toMatch(/\b3\s*\/\s*5\b|\b[0-9]+\s*trên\s*5\b/);
  });
});
