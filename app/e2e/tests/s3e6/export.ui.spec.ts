import { readFile } from 'node:fs/promises';
import type { Page } from '@playwright/test';
import { expect, test } from '../../support/fixtures.ts';
import { board051, openBoard } from '../s3e3/s3e3.ts';
import { csvLines } from './s3e6.ts';

/** US-S3E6-02 trên trình duyệt: "Xuất kết quả" ở Kết quả lớp tải tệp CSV. */

async function download(page: Page) {
  const wait = page.waitForEvent('download');
  await page.getByTestId('s3e6-export').click();
  const d = await wait;
  const path = await d.path();
  return { name: d.suggestedFilename(), text: await readFile(path, 'utf8') };
}

test('Xuất bảng tuần 15 đúng hàng và cột @TC-S3E6-11', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  const { text } = await download(page);
  const lines = csvLines(text);
  expect(lines[0]).toBe('Tên hiển thị,Mã người dùng,Đã rời,Đề 1,Đề 2,Đề 3,Đề 4');
  expect(lines).toHaveLength(9);
  const names = lines.slice(1).map((l) => l.split(',')[0]);
  expect(names).toEqual(['Bình An', 'Chi Mai', 'Đức Huy', 'Gia Bảo', 'Hà My', 'Khánh Linh', 'Minh Quân', 'Ngọc Anh']);
  const linh = lines.find((l) => l.startsWith('Khánh Linh,'))!;
  expect(linh.split(',').slice(3)).toEqual(['Chưa làm', 'Chưa làm', 'Chưa làm', 'Chưa làm']);
  const an = lines.find((l) => l.startsWith('Bình An,'))!;
  expect(an.split(',').slice(3)).toEqual(['Đúng', 'Xem lại', 'Đúng', 'Xem lại']);
  expect(text).not.toMatch(/Tổng|%|Điểm|Hạng/);
});

test('Tên tệp không chứa tên học sinh @TC-S3E6-12', async ({ page, request }) => {
  const sb = await board051(request);
  await openBoard(page, sb);
  const { name } = await download(page);
  expect(name).toMatch(/^ket-qua-11A1-bo-bai-tuan-15-\d{8}$|^ket-qua-11A1-bo-bai-tuan-15-\d{8}\.csv$/);
  for (const s of Object.values(sb.classes[0]!.students)) expect(name).not.toContain(s.userCode);
  expect(name).not.toMatch(/binh|an-|mai|huy/i);
});

test('Ô bắt đầu bằng dấu công thức được vô hiệu @TC-S3E6-13', async ({ page, request }) => {
  const sb = await board051(request, { students: [{ name: '=HYPERLINK("http://x","a")' }, { name: '+84 Lan' }] });
  await openBoard(page, sb);
  const { text } = await download(page);
  const lines = csvLines(text);
  // ô có dấu phẩy/nháy kép được bọc nháy kép theo RFC 4180; nội dung bắt đầu bằng dấu nháy đơn
  expect(lines.some((l) => l.startsWith('"\'=HYPERLINK(""http://x"",""a"")"'))).toBe(true);
  expect(lines.some((l) => l.startsWith("'+84 Lan,"))).toBe(true);
  expect(lines.some((l) => /^[=+\-@]/.test(l))).toBe(false);
});
