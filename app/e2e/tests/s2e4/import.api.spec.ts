import { expect, test } from '../../support/fixtures.ts';
import { FX10, FX12, LAN, LAN_DOC, MSG, ORG_LIB, THREE, bearer, createImport, docx, exercises, importsUrl, ok, pdf, teacher, teacherLib, tokenFor, upload, xlsx, type Import } from './s2e4.ts';

/** S2-E4 — API import (US-S2E4-02, 03): tệp hỏng / mật khẩu, trần 10 MB, trần 50 bài, xlsx chưa nhận, không lộ đề của người khác. */

type Err = { code: string; message: string };

test('@TC-S2E4-016 tệp hỏng hoặc đặt mật khẩu bị từ chối', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(16, info));
  const broken = await upload(request, t, 'hong.docx', Buffer.from('PK\u0003\u0004 đây không phải tệp Word', 'utf8'));
  expect(broken.status()).toBe(400);
  expect((await broken.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.broken });

  const locked = await upload(request, t, 'khoa.pdf', pdf('mat-khau'));
  expect(locked.status()).toBe(400);
  expect((await locked.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.password });

  // không mục nào được tạo: lượt import ở trạng thái từ chối, không có mục
  const c = await ok<Import>(await createImport(request, t, 'hong2.docx', 12));
  const r = await request.put(`${importsUrl}/${c.id}/file?offset=0&total=12`, {
    headers: { ...bearer(t), 'Content-Type': 'application/octet-stream' },
    data: Buffer.from('khong phai zi', 'utf8').subarray(0, 12),
  });
  expect(r.status()).toBe(400);
  const v = await ok<Import>(await request.get(`${importsUrl}/${c.id}`, { headers: bearer(t) }));
  expect(v.status).toBe('rejected');
  expect(v.error).toBe(MSG.broken);
  expect(v.items).toHaveLength(0);
});

test('@TC-S2E4-017 tệp đúng 10 MB nhận, 10 MB cộng 1 byte bị chặn', async ({ request }, info) => {
  test.slow();
  const t = await tokenFor(request, teacher(17, info));
  const exact = docx(THREE, 10_485_760);
  expect(exact.length).toBe(10_485_760);
  const v = await ok<Import>(await upload(request, t, 'vua-10MB.docx', exact));
  expect(v.status).toBe('parsed');
  expect(v.items).toHaveLength(3);

  const over = docx(THREE, 10_485_761);
  expect(over.length).toBe(10_485_761);
  const r = await upload(request, t, 'qua-10MB.docx', over);
  expect(r.status()).toBe(413);
  expect((await r.json()) as Err).toMatchObject({ code: 'PAYLOAD_TOO_LARGE', message: MSG.tooBig });
});

test('@TC-S2E4-018 tệp 50 bài nhận, 51 bài báo vượt', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(18, info));
  const v50 = await ok<Import>(await upload(request, t, '50-bai.docx', docx(exercises(50))));
  expect(v50.items).toHaveLength(50);
  expect(v50.found).toBe(50);
  expect(v50.notes.filter((n) => n.includes('chỉ lấy'))).toHaveLength(0);

  const v51 = await ok<Import>(await upload(request, t, '51-bai.docx', docx(exercises(51))));
  expect(v51.items).toHaveLength(50);
  expect(v51.items[49]!.title).toBe('Bài 50');
  expect(v51.found).toBe(51);
  expect(v51.notes).toContain('Tệp có 51 bài, chỉ lấy 50 bài đầu');
});

test('@TC-S2E4-020 định dạng xlsx chưa nhận', async ({ request }, info) => {
  const t = await tokenFor(request, teacher(20, info));
  const sheet = xlsx();
  const r = await upload(request, t, 'bang-diem.xlsx', sheet);
  expect(r.status()).toBe(400);
  expect((await r.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.onlyDocxPdf });
  // đổi đuôi thành docx vẫn bị nhận ra theo nội dung; không mục nào được tạo
  const renamed = await upload(request, t, 'bang-diem.docx', sheet);
  expect(renamed.status()).toBe(400);
  expect((await renamed.json()) as Err).toMatchObject({ code: 'VALIDATION_FAILED', message: MSG.onlyDocxPdf });
});

test('@TC-S2E4-025 không lộ đề Của tôi của người khác qua đối chiếu', async ({ request }, info) => {
  // Cô Lan có đề FX-12 trong "Của tôi" của cô (không ở Kho chung); Cô Trang import cùng đề.
  const t = await tokenFor(request, teacherLib(25, info), ORG_LIB);
  const res = await upload(request, t, 'GiaoAn.docx', docx([`Bài 1. ${FX12}`, `Bài 2. ${FX10}`]));
  const body = await res.text();
  const v = await ok<Import>(res);
  const fx12 = v.items[0]!;
  expect(fx12.state).toBe('new');
  expect(fx12.note ?? null).toBeNull();
  expect(fx12.refId ?? null).toBeNull();
  expect(fx12.checked).toBe(true);
  expect(body).not.toContain('Lan'); // không câu nào nhắc tới Cô Lan hay đề của cô
  expect(body).not.toContain(LAN);
  expect(body).not.toContain(LAN_DOC);
  // đối chiếu vẫn chạy với Kho chung: FX-10 (đề Kho chung) được nhận ra
  expect(v.items[1]!.state).toBe('exists');
  expect(v.items[1]!.refKind).toBe('library');
  // chính Cô Lan import thì đề của cô được nhận ra (đối chiếu với Của tôi của chính mình)
  const lan = await tokenFor(request, LAN, ORG_LIB);
  const own = await ok<Import>(await upload(request, lan, 'GiaoAn.docx', docx([`Bài 1. ${FX12}`])));
  expect(own.items[0]!.state).toBe('exists');
  expect(own.items[0]!.refKind).toBe('mine');
});
