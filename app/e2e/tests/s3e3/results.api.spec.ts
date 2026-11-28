import { expect, test } from '../../support/fixtures.ts';
import {
  active,
  aiCalls,
  bearer,
  board051,
  BOARD,
  evidenceUrl,
  getJson,
  NAMES,
  sandbox,
  script,
  setUrl,
  STATUS,
  student,
  walk,
  WEEK15,
  type Cell,
} from './s3e3.ts';

/** S3-E3 · API Kết quả lớp: trường được phép, quyền theo quan hệ, phân trang con trỏ, nhãn và câu chữ của thẻ gộp. */

interface Row {
  userId: string;
  displayName: string;
  userCode: string;
  left: boolean;
  cells: string[];
}
interface BoardBody {
  rows: Row[];
  columns: Array<{ position: number; title: string }>;
  nextCursor?: string | null;
}
interface StuckBody {
  insights: Array<{ position: number; text: string; n: number; labels: string[]; evidence: unknown[] }>;
  items: Array<{ position: number; mode: string; works: number }>;
}
const BANNED = /total|sum|score|percent|rank|average|best|worst|count/i;
const CHI_MAI_Q = 'Giao tuyến của hai mặt phẳng là đường nào ạ?';

test('@TC-S3E3-03 thân phản hồi bảng lớp không chứa tổng, phần trăm, điểm hay hạng; no-store; không tổng số bản ghi', async ({ request }) => {
  const sb = await board051(request);
  const r = await request.get(`${setUrl(sb)}/board`, { headers: bearer(sb.teacher.token) });
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const body = (await r.json()) as BoardBody;
  expect(body.rows).toHaveLength(8);
  for (const row of body.rows) {
    expect(Object.keys(row).sort()).toEqual(['cells', 'displayName', 'left', 'userCode', 'userId']);
    expect(row.cells).toEqual(BOARD[row.displayName]!.map((c: Cell) => STATUS[c]));
  }
  for (const k of walk(body).keys) expect(k, `khóa ${k}`).not.toMatch(BANNED);
});

test('@TC-S3E3-16 giáo viên khác gọi kết quả lớp 11A1 nhận NOT_FOUND, thân lỗi không lộ lớp hay học sinh', async ({ request }) => {
  const sb = await board051(request);
  const r = await request.get(`${setUrl(sb)}/board`, { headers: bearer(sb.otherTeacher.token) });
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  for (const s of ['11A1', WEEK15, ...NAMES]) expect(text).not.toContain(s);
  expect(text).not.toMatch(/\d+ (em|học sinh)/);
});

test('@TC-S3E3-17 học sinh gọi kết quả lớp và bằng chứng của bạn nhận FORBIDDEN', async ({ request }) => {
  const sb = await board051(request);
  const an = student(sb, 'Bình An');
  for (const url of [`${setUrl(sb)}/board`, evidenceUrl(sb, 3, student(sb, 'Chi Mai').id)]) {
    const r = await request.get(url, { headers: bearer(an.token) });
    expect(r.status(), url).toBe(403);
    const text = await r.text();
    expect(JSON.parse(text).code).toBe('FORBIDDEN');
    for (const s of NAMES) expect(text).not.toContain(s);
  }
});

test('@TC-S3E3-18 ghép lớp 11A1 với bộ của lớp 11A2 (cùng giáo viên) nhận NOT_FOUND', async ({ request }) => {
  const kids = ['Quốc Bảo', 'Thảo Vy', 'Tuấn Kiệt'];
  const sb = await sandbox(request, [
    { name: '11A1', students: active(NAMES), sets: [{ title: WEEK15, cells: BOARD }] },
    { name: '11A2', students: active(kids), sets: [{ title: WEEK15, cells: { 'Quốc Bảo': ['Đúng'] } }] },
  ]);
  const r = await request.get(`${setUrl(sb, 0, 0).replace(sb.classes[0]!.sets[0]!.id, sb.classes[1]!.sets[0]!.id)}/board`, {
    headers: bearer(sb.teacher.token),
  });
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  for (const s of kids) expect(text).not.toContain(s);
});

test('@TC-S3E3-19 bài làm của Chi Mai ở lớp 10B không lọt vào bảng 11A1', async ({ request }) => {
  const sb = await sandbox(
    request,
    [{ name: '11A1', students: [{ name: 'Bình An' }, { name: 'Chi Mai', shared: true }], sets: [{ title: WEEK15, cells: { 'Chi Mai': ['Đúng', 'Xem lại', 'Xem lại', 'Chưa làm'] } }] }],
    [{ name: '10B', students: [{ name: 'Mi Na' }, { name: 'Chi Mai', shared: true }], sets: [{ title: 'Bộ riêng lớp 10B', items: 6, cells: { 'Chi Mai': ['Đúng', 'Đúng', 'Đúng', 'Đúng', 'Đúng', 'Đúng'] } }] }],
  );
  expect(sb.otherClasses[0]!.students['Chi Mai']!.id).toBe(student(sb, 'Chi Mai').id);
  const r = await request.get(`${setUrl(sb)}/board`, { headers: bearer(sb.teacher.token) });
  const text = await r.text();
  const body = JSON.parse(text) as BoardBody;
  const mai = body.rows.find((x) => x.displayName === 'Chi Mai')!;
  expect(mai.cells).toEqual(['correct', 'review', 'review', 'todo']);
  expect(body.columns).toHaveLength(4);
  for (const s of ['Bộ riêng lớp 10B', '10B', 'Mi Na', 'hình chữ nhật']) expect(text).not.toContain(s);
});

test('@TC-S3E3-20 lớp 101 em: trang đầu 100 hàng, trang hai 1 hàng, con trỏ bị sửa nhận INVALID_CURSOR, không tổng số', async ({ request }) => {
  const sb = await sandbox(request, [{ name: '11B9', bulkStudents: 101, sets: [{ title: 'Bộ lớn', items: 2 }] }]);
  const h = { headers: bearer(sb.teacher.token) };
  const one = (await (await request.get(`${setUrl(sb)}/board`, h)).json()) as BoardBody;
  expect(one.rows).toHaveLength(100);
  expect(one.nextCursor).toBeTruthy();
  const two = (await (await request.get(`${setUrl(sb)}/board?cursor=${encodeURIComponent(one.nextCursor!)}`, h)).json()) as BoardBody;
  expect(two.rows).toHaveLength(1);
  expect(two.nextCursor ?? null).toBeNull();
  const codes = new Set([...one.rows, ...two.rows].map((x) => x.userCode));
  expect(codes.size).toBe(101);
  const c = one.nextCursor!;
  const mid = Math.floor(c.length / 2);
  const broken = c.slice(0, mid) + (c[mid] === 'A' ? 'B' : 'A') + c.slice(mid + 1);
  const bad = await request.get(`${setUrl(sb)}/board?cursor=${encodeURIComponent(broken)}`, h);
  expect(bad.status()).toBeGreaterThanOrEqual(400);
  expect(((await bad.json()) as { code: string }).code).toBe('INVALID_CURSOR');
  for (const k of [...walk(one).keys, ...walk(two).keys]) expect(k).not.toMatch(BANNED);
});

test('@TC-S3E3-29 bằng chứng của em ngoài lớp (Mi Na chỉ thuộc 10B) nhận NOT_FOUND', async ({ request }) => {
  const sb = await board051(request, {}, [{ name: '10B', students: [{ name: 'Mi Na' }], sets: [{ title: 'Bộ 10B', cells: { 'Mi Na': ['Đúng'] } }] }]);
  const mina = sb.otherClasses[0]!.students['Mi Na']!;
  const other = sb.otherClasses[0]!;
  for (const url of [evidenceUrl(sb, 1, mina.id), `${setUrl(sb).replace(sb.classes[0]!.id, other.id).replace(sb.classes[0]!.sets[0]!.id, other.sets[0]!.id)}/evidence?item=1&userId=${mina.id}`]) {
    const r = await request.get(url, { headers: bearer(sb.teacher.token) });
    expect(r.status(), url).toBe(404);
    const text = await r.text();
    expect(JSON.parse(text).code).toBe('NOT_FOUND');
    expect(text).not.toContain('Mi Na');
  }
});

test('@TC-S3E3-30 giáo viên khác đọc bằng chứng Chi Mai × Đề 3 của 11A1 nhận NOT_FOUND, không lộ tên hay câu hỏi', async ({ request }) => {
  const sb = await board051(request, { events: [{ student: 'Chi Mai', item: 3, kind: 'ask', text: CHI_MAI_Q, at: '15:12' }] });
  const r = await request.get(evidenceUrl(sb, 3, student(sb, 'Chi Mai').id), { headers: bearer(sb.otherTeacher.token) });
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  expect(text).not.toContain('Chi Mai');
  expect(text).not.toContain('Giao tuyến');
});

test('@TC-S3E3-31 thân phản hồi bằng chứng chỉ có trường được phép', async ({ request }) => {
  const sb = await board051(request, {
    events: [
      { student: 'Chi Mai', item: 3, kind: 'hint', level: 1, at: '15:10' },
      { student: 'Chi Mai', item: 3, kind: 'ask', text: CHI_MAI_Q, at: '15:12' },
    ],
  });
  const r = await request.get(evidenceUrl(sb, 3, student(sb, 'Chi Mai').id), { headers: bearer(sb.teacher.token) });
  expect(r.status()).toBe(200);
  expect(r.headers()['cache-control']).toContain('no-store');
  const allowed = ['userId', 'displayName', 'userCode', 'left', 'item', 'position', 'title', 'status', 'openedAt', 'attempts', 'hints', 'questions', 'at', 'value', 'matched', 'level', 'text', 'stoppedStep', 'updatedAt'];
  for (const k of new Set(walk(await r.json()).keys)) {
    expect(allowed, `khóa ${k}`).toContain(k);
    expect(k).not.toMatch(/email|phone|birth|school|avatar|photo|ip|device/i);
  }
});

test('@TC-S3E3-41 không thẻ nào mang nhãn "Đã kiểm chứng": nhãn do bộ gộp tự gắn bị bỏ', async ({ request }) => {
  const sb = await board051(request);
  await script(request, sb, {
    1: [{ text: 'Nhiều em nhầm hình chiếu của SC', works: [1, 2] }],
    2: [{ text: 'Nhiều em kẻ sai đường cao', works: [1, 2, 3], label: 'Đã kiểm chứng' }],
    3: [{ text: 'Nhiều em xác định sai giao tuyến của (SAB) và (SCD)', works: [1, 2, 3, 4, 5, 6] }],
  });
  const r = await request.get(`${setUrl(sb)}/stuck`, { headers: bearer(sb.teacher.token) });
  expect(r.status()).toBe(200);
  const text = await r.text();
  expect(text).not.toContain('Đã kiểm chứng');
  const body = JSON.parse(text) as StuckBody;
  expect(body.insights).toHaveLength(3);
  for (const i of body.insights) expect(i.labels).toEqual(['AI gộp', 'Cần thầy cô xác nhận']);
});

test('@TC-S3E3-51 giáo viên khác gọi "Cả lớp vướng ở đâu" của 11A1 nhận NOT_FOUND, bộ gộp không bị gọi', async ({ request }) => {
  const sb = await board051(request);
  const r = await request.get(`${setUrl(sb)}/stuck`, { headers: bearer(sb.otherTeacher.token) });
  expect(r.status()).toBe(404);
  const text = await r.text();
  expect(JSON.parse(text).code).toBe('NOT_FOUND');
  expect(text).not.toContain('insights');
  for (const s of NAMES) expect(text).not.toContain(s);
  expect(await aiCalls(request, sb.otherTeacher.id)).toBe(0);
  expect(await aiCalls(request, sb.teacher.id)).toBe(0);
});

test('@TC-S3E3-52 học sinh gọi "Cả lớp vướng ở đâu" nhận FORBIDDEN, bộ gộp không bị gọi', async ({ request }) => {
  const sb = await board051(request);
  const an = student(sb, 'Bình An');
  const r = await request.get(`${setUrl(sb)}/stuck`, { headers: bearer(an.token) });
  expect(r.status()).toBe(403);
  expect(((await r.json()) as { code: string }).code).toBe('FORBIDDEN');
  expect(await aiCalls(request, an.id)).toBe(0);
  expect(await aiCalls(request, sb.teacher.id)).toBe(0);
});

test('@TC-S3E3-56 thẻ không có phần trăm hay xếp hạng toàn lớp: câu vi phạm bị loại hoặc cắt', async ({ request }) => {
  const sb = await board051(request);
  await script(request, sb, {
    3: [{ text: '75% lớp sai bước này', works: [1, 2, 3] }],
    4: [{ text: 'Đây là đề khó nhất lớp. Nhiều em quên điều kiện đáy là hình vuông.', works: [1, 2, 3] }],
  });
  const r = await request.get(`${setUrl(sb)}/stuck`, { headers: bearer(sb.teacher.token) });
  const text = await r.text();
  const body = JSON.parse(text) as StuckBody;
  expect(text).not.toContain('%');
  expect(text).not.toMatch(/khó nhất|xếp hạng|phần trăm/);
  expect(body.insights).toHaveLength(1);
  expect(body.insights[0]!.text).toBe('Nhiều em quên điều kiện đáy là hình vuông.');
  expect(body.insights[0]!.n).toBe(3);
  for (const k of walk(body).keys) expect(k).not.toMatch(BANNED);
});
