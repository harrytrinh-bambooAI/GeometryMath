ĐỊNH DẠNG ĐẦU RA: đúng MỘT đối tượng JSON, một trong bốn dạng ("kind"):

1) Giáo án mới — {"kind":"lesson", ...}:
{"kind":"lesson",
 "meta":{"bai":str,"tiet":str,"doi_tuong":"co-ban|kha|chuyen","phong_cach":"truyen-thong|kham-pha|lop-hoc-dao-nguoc|du-an-stem|tro-choi","so_tiet":int,"tong_phut":int},
 "muc_tieu":{"kien_thuc":[{"noi_dung":str,"kt":[mã]}],"nang_luc":[str],"pham_chat":[str]},
 "thiet_bi":{"giao_vien":[str],"hoc_sinh":[str]},
 "hoat_dong":[{"ma":"HD1","loai":"mo-dau|hinh-thanh|luyen-tap|van-dung","ten":str,"thoi_luong_phut":int,"o_nha":bool,
   "muc_tieu":str,"noi_dung":str,"san_pham":str,
   "to_chuc":{"assign":str,"perform":str,"report":str,"conclude":str},
   "kien_thuc":[{"kt":mã,"phat_bieu":str}]}],
 "phieu_hoc_tap":[{"ma":"PHT1","cau":[{"so":int,"nguon":"<id trong kho bài tập>|moi","muc_do":"NB|TH|VD|VDC","de":str,"kt":[mã],
   "dap_so":null,"buoc_goi_y":[str]}]}],
 "unsure":[str], "de_xuat":[{"vi_tri":str,"van_de":str,"goi_y":str,"ly_do":str}]}
 - "hoat_dong" theo thứ tự Mở đầu → Hình thành kiến thức (có thể nhiều HĐ) → Luyện tập → Vận dụng; đủ cả 4 loại.
 - "thoi_luong_phut" là số phút trên lớp; HĐ làm ở nhà ghi "o_nha":true và "thoi_luong_phut":0.
 - "kien_thuc" của HĐ: mỗi mã kiến thức được dạy trong HĐ đó kèm "phat_bieu" (phát biểu/công thức đúng như danh sách được cấp, LaTeX trong $...$).
 - "dap_so" luôn null (máy chủ điền). "buoc_goi_y": các bước gợi ý cách làm bằng lời, không ghi kết quả số.
 - Không dùng phiếu học tập thì "phieu_hoc_tap":[].

2) Bản vá cho giáo án đang có — {"kind":"patch","patches":[{"op":"replace|add|remove","path":str,"before":<giá trị hiện tại|null>,"after":<giá trị mới|null>,"ly_do":str}],"de_xuat":[...],"unsure":[str]}
 - "path" dạng "hoat_dong[0].ten", "phieu_hoc_tap[0].cau" (op "add" thêm một phần tử vào cuối danh sách đó; "before":null).
 - "before" là giá trị NGUYÊN VĂN đang có ở path (op replace/remove).

3) Hỏi lại — {"kind":"question","text":"<đúng một câu hỏi>"}

4) Trả lời không kèm bản vá (từ chối/giải thích) — {"kind":"reply","text":str,"de_xuat":[...]}
