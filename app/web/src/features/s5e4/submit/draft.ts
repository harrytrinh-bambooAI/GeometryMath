/**
 * Bản nháp bài nộp trên máy (BR-S5E4-09): IndexedDB giữ ảnh các trang, khóa lũy đẳng và cờ "chờ gửi lại" để mất mạng
 * không mất bài. Hết chỗ lưu thì ném lỗi QuotaExceededError (màn báo MSG "Máy em hết chỗ lưu…", US-S5E4-03-FC3).
 */

export interface DraftPage {
  blob: Blob;
  contentType: string;
  derived?: Blob;
  clarity: number;
  sha: string;
  uploadId?: string;
  derivedUploadId?: string;
}

export interface Draft {
  assignmentId: string;
  key: string;
  pending: boolean;
  pages: DraftPage[];
}

const DB = 'geo-s5e4-submit';
const STORE = 'drafts';

function open(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const r = indexedDB.open(DB, 1);
    r.onupgradeneeded = () => r.result.createObjectStore(STORE, { keyPath: 'assignmentId' });
    r.onsuccess = () => resolve(r.result);
    r.onerror = () => reject(r.error);
  });
}

function run<T>(mode: IDBTransactionMode, op: (s: IDBObjectStore) => IDBRequest<T>): Promise<T> {
  return open().then(
    (db) =>
      new Promise<T>((resolve, reject) => {
        let req: IDBRequest<T>;
        try {
          const tx = db.transaction(STORE, mode);
          req = op(tx.objectStore(STORE));
          tx.oncomplete = () => {
            db.close();
            resolve(req.result);
          };
          tx.onerror = () => {
            db.close();
            reject(tx.error ?? req.error);
          };
          tx.onabort = () => {
            db.close();
            reject(tx.error ?? req.error);
          };
        } catch (e) {
          db.close();
          reject(e);
        }
      }),
  );
}

/**
 * Ảnh lưu dạng ArrayBuffer (không lưu Blob): WebKit (Safari, Playwright webkit) có thể từ chối ghi Blob vào IndexedDB
 * ("Error preparing Blob/File data…") → nháp không lưu (TC-S5E4-49 trượt riêng ui-webkit; T-261128-68). Nháp cũ lưu Blob
 * vẫn đọc được.
 */
interface StoredBlob {
  buf: ArrayBuffer;
  type: string;
}
type Stored = Omit<Draft, 'pages'> & { pages: Array<Omit<DraftPage, 'blob' | 'derived'> & { blob: StoredBlob | Blob; derived?: StoredBlob | Blob }> };

const pack = async (b: Blob): Promise<StoredBlob> => ({ buf: await b.arrayBuffer(), type: b.type });
const unpack = (b: StoredBlob | Blob): Blob => (b instanceof Blob ? b : new Blob([b.buf], { type: b.type }));

export async function saveDraft(d: Draft): Promise<void> {
  const pages = [];
  for (const p of d.pages) pages.push({ ...p, blob: await pack(p.blob), derived: p.derived ? await pack(p.derived) : undefined });
  const stored: Stored = { ...d, pages };
  await run('readwrite', (s) => s.put(stored));
}

export async function loadDraft(assignmentId: string): Promise<Draft | undefined> {
  const d = await run<Stored | undefined>('readonly', (s) => s.get(assignmentId) as IDBRequest<Stored | undefined>);
  if (!d) return undefined;
  return { ...d, pages: d.pages.map((p) => ({ ...p, blob: unpack(p.blob), derived: p.derived ? unpack(p.derived) : undefined })) };
}
export const clearDraft = (assignmentId: string) => run('readwrite', (s) => s.delete(assignmentId)).then(() => undefined);
