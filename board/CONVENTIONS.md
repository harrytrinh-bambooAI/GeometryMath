# Quy ước điều phối

Áp dụng cho mọi đợt chạy nhiều agent trong dự án này. Rút từ skill po-ba-qc (`04_collaboration-playbook.md` §11, `07_GUIDE_goal_run.md`).

## 1. Vai và quyền
| Vai | Ai | Quyết định gì | Giới hạn |
|---|---|---|---|
| Chủ đầu tư | Người dùng | Mọi thứ, bất kỳ lúc nào | — |
| PO | Phiên chính | Phạm vi, ưu tiên, xung đột giữa các agent, giả định cho điểm còn mở, nghiệm thu | Không đổi Final của chủ đầu tư; không nâng giả định thành "đã chốt"; không gửi gì ra ngoài |
| A (điều phối) | Phiên chính | Thứ tự, agent, ngân sách, tiến độ | Không tự phán nội dung; chuyển cho vai PO |
| Agent thực hiện | Agent con | File của chính nó | Không sửa file của agent khác; muốn đổi thì gửi `proposal` |
| RV (phản biện) | Agent con, khác model với tác giả | Không quyết gì; chỉ tìm chỗ sai, chỗ thiếu | Chỉ ghi `review/RV_findings.md` |

## 2. Sở hữu file
| File | Chủ |
|---|---|
| `PO-DECISIONS.md`, `board/*`, `questions/*`, `PROGRESS.md` | PO / A |
| `PRODUCT-MASTER.md`, `MVP.md`, `ROADMAP.md` | PO |
| `DOMAIN-PROFILE.md` | agent DOMAIN |
| `STAGES-AFTER-MVP.md` | agent STAGES |
| `Design/stages-2-5.html`, `Design/stages/c2…c5-*.png` | agent DESIGN-2-5 |
| `Design/stages-6-9.html`, `Design/stages/c6…c9-*.png` | agent DESIGN-6-9 |
| `Design/prototype/tour.*` | agent TOUR |
| `Design/prototype/index.html`, `Design/assets/*`, `Design/screens.html` | PO / A |

## 3. Luật
1. **Nguồn trước.** Mọi yêu cầu phải dẫn về `PO-DECISIONS.md`, `PRODUCT-MASTER.md`, `MVP.md` hoặc `STAGES-AFTER-MVP.md`. Ý kiến của agent khác không phải là nguồn.
2. **Không bịa yêu cầu.** Chi tiết chưa có trong nguồn thì hoặc bỏ, hoặc gửi `question` lên bảng. PO trả lời bằng một giả định có mã; agent không tự đoán.
3. **Giả định không phải quyết định.** Việc dựng trên giả định ghi rõ mã giả định và mang nhãn tạm thời.
4. **Tranh luận có giới hạn.** Một xung đột tối đa 2 lượt mỗi bên, mỗi lượt kèm nguồn. Sau đó PO phán bằng một `decision`; bên không đồng ý ghi một dòng bảo lưu, không mở lại.
5. **Giới hạn vòng lặp.** Mỗi màn hình hoặc mỗi mục tài liệu tối đa 2 vòng sửa sau khi tự kiểm. Mỗi hạng mục tối đa 3 lượt nghiệm thu. Còn lỗi thì ghi vào "khoảng trống đã biết" và đi tiếp.
6. **Điểm kiểm tra.** CP-1 bắt đầu (kèm giả định), CP-2 khoảng một nửa, CP-3 xong bản nháp, CP-4 đã đối chiếu chéo. Mỗi điểm một tin `status`: đã xong / đang giả định gì / đang vướng gì.
7. **Chỉ đăng khi làm thay đổi việc của người khác.** Tiến độ gộp vào tin `status`.
8. **Đồng ý phải nói đã kiểm gì, ở đâu.** Đồng ý không kèm kiểm tra được tính là "chưa xem".
9. **Nghiệm thu.** PO tự mở sản phẩm (ảnh, file, chạy lại test) trước khi nhận; không nhận chỉ dựa trên báo cáo.
10. **Phản biện.** Trước khi một bộ sản phẩm được coi là xong, một agent RV khác model với tác giả rà lại và ghi `review/RV_findings.md`. Chủ file sửa hoặc bác kèm nguồn.
11. **Ngân sách.** Trước mỗi đợt chạy nhiều agent, A nêu kế hoạch và ước lượng token, chờ chủ đầu tư đồng ý. Vượt ước lượng 30% thì dừng hỏi.
12. **Agent mới, không fork.** Một việc một agent mới với bản giao việc ngắn: vai, file sở hữu, nhiệm vụ, nguồn phải đọc, cách tự kiểm.
13. **Không gửi gì cho bên ngoài.** Chỉ chủ đầu tư chuyển tài liệu cho khách hàng hay đối tác.

## 4. Mẫu tin trên bảng
```
### M-<nnn> · <question | proposal | conflict | decision | handoff | status | escalate> · từ: <vai> → tới: <vai | tất cả>
- ref: <file / màn hình / mã quyết định>
- nội dung: <ngắn; dẫn đường dẫn, không chép lại>
- nguồn: <tài liệu gốc + vị trí>   (bắt buộc với question / proposal / conflict)
- tác động: <người nhận phải đổi gì>
- cần trước: <điểm kiểm tra>
```
