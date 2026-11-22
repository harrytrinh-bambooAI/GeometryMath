/**
 * Quy ước data-testid (chi tiết: app/web/README.md):
 *   <vùng>-<thành phần>[-<chi tiết>]   chữ thường, không dấu, nối bằng gạch ngang
 * - vùng: `shell` (khung), `home`, `gv` (giáo viên), `hs` (học sinh), hoặc mã epic `e1`…`e7` cho màn của epic.
 * - Phần tử lặp: thêm khóa ổn định cuối cùng, ví dụ `e2-assignment-row-BG-1` (dùng mã nghiệp vụ, không dùng chỉ số).
 * Dùng tid(...) thay vì gõ chuỗi tay để không sai quy ước.
 */
export function tid(area: string, ...parts: Array<string | number>): string {
  const norm = (s: string | number) =>
    String(s)
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .replace(/[^A-Za-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  return [area, ...parts].map(norm).filter(Boolean).join('-');
}
