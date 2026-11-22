# work/src/qc — nguồn của test case (QC)

Chủ sở hữu: QC. `gen_cases.py` đọc AC của BA (`work/epics/E<n>/stories_ac.md`, chỉ đọc), bảng phân loại `E<n>_meta.txt` và case viết tay `E<n>_extra.md`, rồi sinh `work/epics/E<n>/test_cases.md` kèm bảng đối chiếu hai chiều. Tệp sinh ra không sửa tay.

- `E<n>_meta.txt`: một dòng mỗi tiêu chí: `TC | AC | ưu tiên (H/M/L) | loại (CN chức năng, PQ phân quyền, DL dữ liệu trả về, PCN phi chức năng, TB thiết bị) | tầng (DV đơn vị, API tích hợp API, DB tích hợp CSDL, TAI tải, TC thủ công trên thiết bị; nối bằng +) | tài khoản | nhóm CL | ghi chú`. Id case không đổi khi BA thêm tiêu chí: thêm dòng mới với id kế tiếp.
- `E<n>_extra.md`: case viết tay (biên, bảng quyết định, NFR) theo 12 cột của tệp sinh ra trừ cột "Tự động hóa" (cột đó lấy từ `E<n>_auto.txt` hoặc mặc định), đặt dưới `## US-GEO-nnn` hoặc `## X`; Remark phải trỏ về tiêu chí (`US-GEO-nnn · SCn`) hoặc NFR-GEO / BR-GEO / REQ-GEO / D-GEO.
- `E<n>_auto.txt` (PO-50): cột "Tự động hóa" của từng case: `TC-GEO-nnnn | E2E-UI / E2E-API / IT / Manual | lý do`. Chỉ ghi case khác mặc định; mặc định suy từ tầng và loại (`auto_default` trong `gen_cases.py`): tải → Manual (Gatling) · tích hợp API + thủ công → E2E-UI · chỉ thủ công → E2E-UI, riêng loại thiết bị → Manual · CSDL hoặc đơn vị → IT · chỉ API → E2E-API. Manual luôn có lý do.
- Kiểm độc lập AC ↔ TC và cột Tự động hóa: `python3 -I work/tools/check_ac_tc.py` (thêm `--trace work/traceability.md` để ghi bảng truy vết).
- Chạy: `python3 work/src/qc/gen_cases.py E1` (thoát 1 khi có tiêu chí chưa có case, id trùng hoặc ngoài dải, họ mã chưa giải thích); `--init` in khung meta.
- Dải id xuyên epic chia sẵn: E1 0800…0814 · E2 0815…0824 · E3 0825…0834 · E4 0835…0849 · E5 0850…0859 · E6 0860…0879 · E7 0880…0899.
