import { useEffect, useState } from 'react';
import { tid } from '../../shared/testid.ts';
import { Icon } from '../../shared/ui/Icon.tsx';
import { MyAssignmentsPage } from '../e1/MyAssignmentsPage.tsx';
import { PrivacyGate } from '../s3e5/PrivacyGate.tsx';
import { MSG, s3e6Api, type MyClass } from './api.ts';

/** US-S3E6-02 (BR-S3E6-15): mục "Lớp đã lưu trữ" ở Bài của em — chỉ xem, MSG-S3-96. */
export function ArchivedClasses() {
  const [items, setItems] = useState<MyClass[]>([]);
  useEffect(() => {
    let live = true;
    s3e6Api
      .myClasses()
      .then((r) => live && setItems(r.items.filter((c) => c.archived && c.status === 'active')))
      .catch(() => undefined); // tài khoản MVP không có lớp chặng 3: không hiện mục
    return () => {
      live = false;
    };
  }, []);
  if (items.length === 0) return null;
  return (
    <section className="card s3e6-archived" data-testid={tid('s3e6', 'archived-classes')}>
      <h3>Lớp đã lưu trữ</h3>
      <ul>
        {items.map((c) => (
          <li key={c.className} data-testid={tid('s3e6', 'archived-class')}>
            <b>{c.className}</b>
            <span className="muted s3e6-archived-msg">
              <Icon name="lock" /> {MSG.s96}
            </span>
          </li>
        ))}
      </ul>
    </section>
  );
}

/** Bài của em: cổng thông báo riêng tư (S3-E5) + mục Lớp đã lưu trữ + màn gốc E1. */
export function MyAssignmentsWithArchived() {
  return (
    <PrivacyGate>
      <ArchivedClasses />
      <MyAssignmentsPage />
    </PrivacyGate>
  );
}
