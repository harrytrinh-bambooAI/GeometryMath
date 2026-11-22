import re, os
B = os.path.dirname(os.path.abspath(__file__))
txt = open(f'{B}/results/scale_checks.txt', encoding='utf-8').read()
secs = re.split(r'\n?(===== .*)\n', '\n' + txt)
out = ["Trong dữ liệu thử, một số bảng nhỏ (900 từ khóa, 300 bài giảng, 60 đề kho, 35 cấp phép) nên bộ lập kế hoạch chọn quét tuần tự vì rẻ hơn. Để chứng minh chỉ mục vẫn phục vụ khi bảng lớn, `bench/03_scale_checks.sql` chèn tạm trong **một giao dịch** thêm 50.000 bài giảng, 150.000 từ khóa, 20.000 đề kho, 50.000 cấp phép, chạy lại các truy vấn tương ứng với vai `geo_app`, rồi ROLLBACK (và VACUUM). Kết quả (máy cục bộ):\n",
       "| Truy vấn | Quy mô | Nút chính trong kế hoạch | Thực thi (ms) |", "|---|---|---|---|"]
details = []
i = 1
while i < len(secs):
    head = secs[i].replace('=====', '').strip(); body = secs[i + 1]
    parts = re.split(r'\n(--- .*)\n', body)
    chunks = [(head, parts[0])]
    for k in range(1, len(parts), 2):
        chunks.append((head + ' ' + parts[k].replace('---', '').strip(), parts[k + 1]))
    for h, b in chunks:
        ex = re.findall(r'Execution Time: ([\d.]+) ms', b)
        nodes = []
        for m in re.finditer(r'(Bitmap Heap Scan|Bitmap Index Scan|Index Only Scan|Index Scan|Seq Scan) (?:using |on )(\w+)', b):
            n = f'{m.group(1)} {m.group(2)}'
            if n not in nodes: nodes.append(n)
        if ex:
            q = h.split(' @ ')[0].replace('Q', 'Q', 1)
            out.append(f'| {q} | {h.split(" @ ")[1] if " @ " in h else h} | {"; ".join(nodes)} | {ex[-1]} |')
    i += 2
out.append("\nKết luận: ở quy mô lớn Q11b dùng Bitmap Index Scan trên `keyword_trgm_idx`, Q13/Q13b dùng `problem_catalog_filter_idx` (một phần), Q18 dùng `share_grant_lookup_idx` (một phần), Q12 dùng `lecture_search_idx` (GIN); không có Seq Scan trên bảng lớn. Q12 v1 (xếp hạng mọi bài khớp) chậm đi theo số bài khớp, v2 (ứng viên giới hạn 200) giữ ổn định: xem mục 4.")
out = [x.replace("| 50k bài giảng (cụm hiếm: thiet dien) |", "| 50k bài giảng (cụm hiếm: thiet dien) v1: xếp hạng mọi bài khớp |", 1) if x.startswith("| Q12 qa retrieval | 50k bài giảng (cụm hiếm: thiet dien) |") else x for x in out]
open(f'{B}/catalogue_scale.md', 'w', encoding='utf-8').write("\n".join(out) + "\n")
print("\n".join(out))
