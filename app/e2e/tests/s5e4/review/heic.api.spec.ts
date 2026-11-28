import { expect, test } from '../../../support/fixtures.ts';
import { API, bearer } from '../../s3e1/s3e1.ts';
import { pageUrl, sha256 } from '../s5e4.ts';
import { ack, submitRaw, upload, P1, type Submission } from '../submit/submit.ts';
import { gradingWorld } from './grading.ts';

/**
 * BR-S5E4-27 với ảnh gốc HEIC (T-261128-60, nối Dev-S5E4-07): máy chủ gỡ mục Exif của hộp meta trước khi lưu, băm tính
 * sau khi gỡ; ảnh gốc giáo viên xem (để chấm) không còn GPS/thiết bị. Tệp HEIC tối giản dựng trong test (ftyp + meta
 * {hdlr, iinf, iloc} + mdat).
 */
function box(type: string, ...parts: Buffer[]): Buffer {
  const body = Buffer.concat(parts);
  const h = Buffer.alloc(8);
  h.writeUInt32BE(8 + body.length, 0);
  h.write(type, 4, 'latin1');
  return Buffer.concat([h, body]);
}
const full = (type: string, version: number, ...parts: Buffer[]) => box(type, Buffer.from([version, 0, 0, 0]), ...parts);
const u16 = (n: number) => {
  const b = Buffer.alloc(2);
  b.writeUInt16BE(n);
  return b;
};
const u32 = (n: number) => {
  const b = Buffer.alloc(4);
  b.writeUInt32BE(n);
  return b;
};

function heicWithExif(): Buffer {
  const image = Buffer.from('IMAGEDATA-hvc1-frame-'.repeat(8), 'latin1');
  const exif = Buffer.concat([u32(0), Buffer.from('Exif\0\0MM\0*GPS 21.0285,105.8542 Apple iPhone 15', 'latin1')]);
  const ftyp = box('ftyp', Buffer.from('heic', 'latin1'), u32(0), Buffer.from('mif1heic', 'latin1'));
  const hdlr = full('hdlr', 0, u32(0), Buffer.from('pict', 'latin1'), Buffer.alloc(12), Buffer.from([0]));
  const infe = (id: number, type: string) => full('infe', 2, u16(id), u16(0), Buffer.from(type, 'latin1'), Buffer.from([0]));
  const iinf = full('iinf', 0, u16(2), infe(1, 'hvc1'), infe(2, 'Exif'));
  const ilocFor = (o1: number, o2: number) =>
    full('iloc', 0, Buffer.from([0x44, 0x00]), u16(2), u16(1), u16(0), u16(1), u32(o1), u32(image.length), u16(2), u16(0), u16(1), u32(o2), u32(exif.length));
  const metaFor = (o1: number, o2: number) => full('meta', 0, hdlr, iinf, ilocFor(o1, o2));
  const head = ftyp.length + metaFor(0, 0).length + 8; // + tiêu đề mdat
  return Buffer.concat([ftyp, metaFor(head, head + image.length), box('mdat', image, exif)]);
}

test('Ảnh gốc HEIC lưu đã gỡ EXIF/GPS, băm tính sau khi gỡ', async ({ request }) => {
  const c = await gradingWorld(request);
  const an = c.w.people['Bình An']!;
  await ack(request, an);
  const heic = heicWithExif();
  expect(heic.includes(Buffer.from('GPS', 'latin1'))).toBe(true);
  const h = await upload(request, an, heic, 'image/heic');
  const j = await upload(request, an, P1, 'image/jpeg');
  const r = await submitRaw(request, an, c.bc.id, [{ uploadId: h, derivedUploadId: j }]);
  expect(r.status(), await r.text()).toBe(201);
  const s = (await r.json()) as Submission;
  const u = await request.get(pageUrl(s.versionId), { headers: bearer(c.lan.token) });
  expect(u.status(), await u.text()).toBe(200);
  const img = await request.get(`${API}${((await u.json()) as { url: string }).url}`);
  expect(img.status()).toBe(200);
  const stored = Buffer.from(await img.body());
  expect(stored.length).toBe(heic.length);
  for (const bad of ['Exif\0\0MM', 'GPS', 'iPhone', 'Apple']) expect(stored.includes(Buffer.from(bad, 'latin1'))).toBe(false);
  expect(stored.includes(Buffer.from('IMAGEDATA-hvc1-frame-', 'latin1'))).toBe(true);
  expect(stored.subarray(4, 8).toString('latin1')).toBe('ftyp');
  expect(s.pages[0]!.sha256).toBe(sha256(stored));
});
