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

export const saveDraft = (d: Draft) => run('readwrite', (s) => s.put(d)).then(() => undefined);
export const loadDraft = (assignmentId: string) => run<Draft | undefined>('readonly', (s) => s.get(assignmentId) as IDBRequest<Draft | undefined>);
export const clearDraft = (assignmentId: string) => run('readwrite', (s) => s.delete(assignmentId)).then(() => undefined);
