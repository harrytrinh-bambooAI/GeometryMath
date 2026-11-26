/* s23d.js · Phiên bản: v0.1 · 2026-11-26 · Designer Dom (T-261126-40) · màn còn thiếu trong kịch bản S23: tạo lớp, đã vào Kho, Bài của em (bộ bài), mã khôi phục HS + khôi phục, trình chiếu mất mạng, xóa tài khoản, đóng/lưu trữ bộ bài.
   Lịch sử: v0.1 · 26/11/2026 · tạo mới (runner-T-261126-40). Giữ token GEO hiện tại. Dữ liệu mẫu. Tải sau s23c.js, trước s23k.js. */

/* ===== kb1 b4 · Tạo lớp (S3-E1 · PO-66 G4: duyệt từng em luôn bật) ===== */
reg('c3-02-tao-lop','web','C3c','C3-02 · Tạo lớp (duyệt từng em luôn bật)',()=>screen('x','gv',3,'Tạo lớp mới','Lớp học › Tạo lớp',`
<div class="card pad col" style="width:640px;flex-shrink:0;gap:16px"><h2 style="margin:0">Thông tin lớp</h2>
 <div><div class="lbl">Tên lớp</div><div class="field" id="f-ten">${I('users')}11A1<span class="cur1" style="height:18px"></span></div><div class="tiny muted" style="margin-top:4px">Tên em thấy trong danh sách lớp. Không dùng tên riêng của học sinh.</div></div>
 <div class="row" style="gap:14px;align-items:flex-start"><div class="grow"><div class="lbl">Năm học</div><div class="field">${I('cal')}2026–2027<span class="grow"></span>${I('arrow','transform:rotate(90deg)')}</div></div>
 <div class="grow"><div class="lbl">Khối</div><div class="row" style="gap:8px">${['10','11','12'].map(k=>`<span class="chip ${k==='11'?'on':''}" style="padding:9px 22px;font-size:14px">${k}</span>`).join('')}</div></div></div>
 <div class="hint lock" id="f-duyet" style="align-items:center;gap:12px"><div class="toggle" style="opacity:.55"></div><div class="grow"><b>Giáo viên duyệt từng em vào lớp</b><div class="small muted">Luôn bật, không tắt được. Em nhập mã lớp sẽ chờ thầy cô duyệt.</div></div>${ch('','Luôn bật','lock')}</div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Hủy','ghost')}${btn('Tạo lớp','','arrow')}</div></div>
<div class="card pad col grow" style="gap:12px;background:var(--soft);min-width:0"><h2 style="margin:0">Sau khi tạo lớp</h2>
 ${[['lock','Hệ thống sinh mã lớp','Mã ngẫu nhiên dạng HK-xxxx, không chứa thông tin cá nhân.'],['file','Thầy cô chia mã hoặc in trang A4','Mã lớp đặt lại được; mã cũ hết hiệu lực ngay.'],['check','Thầy cô duyệt từng em','Chỉ em được duyệt mới thấy bộ bài của lớp.']].map((s,i)=>`<div class="row" style="align-items:flex-start;gap:11px"><div class="num on">${i+1}</div><div><b>${s[1]}</b><div class="small muted">${s[2]}</div></div></div>`).join('')}
 <div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Giới hạn số lần nhập mã của mỗi tài khoản chống dò mã lớp (PO-66 G4).</span></div></div>`,{concept:1}));

/* ===== kb2 b9 · Đã duyệt, đã vào Kho (S2-E1…E3) ===== */
reg('c2-05-da-vao-kho','web','C2c','C2-05 · Đã duyệt, đã vào Kho',()=>screen('x','gv',1,'Đã duyệt, đã vào Kho','Kho đề › Hình chóp › Góc giữa SC và đáy',`
<div class="card pad col" style="width:640px;flex-shrink:0;gap:14px"><div class="row" style="gap:12px"><div class="icb grn" style="width:48px;height:48px">${I('check','width:26px;height:26px')}</div><div><h2 style="margin:0;font-size:20px">Đã duyệt lời giải v2 và đưa vào Kho</h2><div class="small muted">Hôm nay 12:06 · Cô Lan Anh</div></div></div>
 <div class="col" style="gap:6px" id="f-nhan"><div class="pt">NHÃN HAI TRỤC</div><div class="row" style="flex-wrap:wrap;gap:6px">${trust(1,1)}</div><div class="tiny muted">Máy kiểm: tan SCA = 1 khớp đáp số 45°. Người: cô Lan Anh đã duyệt.</div></div>
 <div class="col" style="gap:6px"><div class="pt">PHIÊN BẢN LỜI GIẢI</div><div class="hint act" style="align-items:center"><div class="num on" style="width:34px;border-radius:9px">v2</div><div class="grow"><b>Bạn sửa bước 3, máy kiểm đạt</b><div class="tiny muted">Hôm nay 11:58 · duyệt 12:06 · phiên bản đã đóng, sửa tiếp sẽ tạo v3</div></div></div></div>
 <div class="row" style="gap:8px;flex-wrap:wrap">${btn('Giao vào bộ bài','','send')}${btn('Xem trong Kho','ghost','book')}${btn('Thêm đề khác','ghost','plus')}</div></div>
<div class="card pad col grow" style="gap:12px;background:var(--soft);min-width:0"><h2 style="margin:0">Ai thấy gì</h2>
 ${[['school','Giáo viên','Xem ngay lời giải đầy đủ trong Kho chung (PO-66 G2).','grn'],['users','Học sinh có lớp','Chỉ thấy đề khi thầy cô đưa vào bộ bài; lời giải mở theo giờ cô đặt.','pri'],['cube','Học sinh tự học','Chỉ nhận gợi ý, lời giải khóa.','yel']].map(r=>`<div class="hint" style="align-items:center"><div class="icb ${r[3]}">${I(r[0])}</div><div class="grow"><b>${r[1]}</b><div class="small muted">${r[2]}</div></div></div>`).join('')}</div>`,{concept:1}));

/* ===== kb3 b5 + kb4 b2 · Bài của em (một mô hình "bộ bài", G1) ===== */
const bset=(dev,t,st,stc,l1,l2,l3,btnT,dim)=>`<div class="card pad col bset" style="gap:8px;${dim?'background:var(--soft)':''}"><div class="row" style="gap:8px"><div class="icb ${dim?'':'pri'}">${I('layers')}</div><div class="grow"><b style="font-size:16px">${t}</b><div style="font-size:14px;color:var(--muted)">${l1}</div></div>${dev==='ipad'?ch(stc,st):''}</div>
 ${dev==='phone'?`<div>${ch(stc,st)}</div>`:''}
 <div class="row" style="gap:8px;flex-wrap:wrap"><span class="chip" style="font-size:14px">${I('clock')}${l2}</span><span class="chip ${dim?'':'yel'}" style="font-size:14px">${I('lock')}${l3}</span></div>
 ${btnT?`<div class="row">${btn(btnT,'','arrow')}</div>`:''}</div>`;
const baiList=(dev)=>`<div class="col grow" style="gap:12px;min-width:0"><div class="row" style="gap:8px"><div class="tabs" style="border:none;padding:0;width:${dev==='ipad'?'420px':'100%'}"><span class="on">Đang giao · 2</span><span>Đã đóng · 1</span></div></div>
 ${bset(dev,'Bộ bài tuần 15 · lớp 11A1','Đang làm','pri','Cô Lan Anh · 4 đề · làm xong 2/4','Hạn Thứ Sáu 18/12 · 20:00','Lời giải mở 18:00 thứ Sáu · còn 2 lượt ở Đề 3','Làm tiếp Đề 3')}
 ${bset(dev,'Bộ luyện góc · lớp 11A1','Chưa làm','','Cô Lan Anh · 5 đề · mỗi đề 3 lượt','Hạn Thứ Ba 22/12 · 20:00','Lời giải mở 20:00 thứ Ba','Bắt đầu')}
 ${bset(dev,'Bộ bài tuần 14 · lớp 11A1','Đã đóng','','Xem kết quả · 4 đề · 3 đúng','Đã đóng 14/12','Lời giải đã mở',null,1)}
 ${dev==='ipad'?`<div class="flow" style="font-size:14px">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Em chỉ thấy bộ của lớp em đã được duyệt. Mỗi bộ cho biết hạn, lượt còn và giờ mở lời giải.</span></div>`:''}</div>`;
reg('c3-05-bai-cua-em-ipad','ipad','C3c','C3-05 · Bài của em · danh sách bộ bài (iPad)',()=>HS('Bài của em','Bài của em › Lớp 11A1',baiList('ipad')));
reg('c3-05-bai-cua-em-iphone','phone','C3c','C3-05 · Bài của em · danh sách bộ bài (iPhone)',()=>HS('Bài của em','Lớp 11A1',baiList('phone')));

/* ===== kb6 b2 · mã khôi phục cho học sinh (PO-66 G3) ===== */
reg('c3-01-ma-nguoi-dung-hs','web','C3c','C3-01 · Mã người dùng + mã khôi phục · học sinh tự học',()=>V['c3-01-ma-nguoi-dung'].fn()
 .replace('chào cô Lan Anh','chào em Bình An').replace('GV-7K3M-92QX','HS-4P9T-37WD').replace('RC-8F2D-KQ47-W9TN','RC-5N7H-X2MB-C8VR')
 .replace('Vào trang giáo viên','Vào Tự học').replace('đưa cho học sinh khi cần','em nhớ hoặc chép lại')
 .replace('Tôi đã lưu mã khôi phục','Em đã lưu mã khôi phục')
 .replace(/<div class="tiny muted">[^<]*<\/div><\/div>$/,'<div class="tiny muted">Em tự học chưa có thầy cô để nhờ đặt lại mật khẩu, nên mã khôi phục là cách lấy lại tài khoản. Em vào lớp sau này vẫn giữ mã này.</div></div>'));

/* ===== kb6 b6 · nhập mã khôi phục → đặt mật khẩu mới ===== */
const khoiPhuc=(st)=>bare(`<div class="col" style="flex:1;align-items:center;justify-content:center;gap:14px;background:linear-gradient(160deg,#EEF1FF,#F6F1FF)"><div class="logo" style="margin:0">${I('cube')}</div><div style="font-size:26px;font-weight:800">${st==='xong'?'Đã đặt mật khẩu mới':'Lấy lại tài khoản bằng mã khôi phục'}</div>
 <div class="card pad col" style="width:520px;gap:14px;padding:26px">${st==='xong'?`
  ${ban('grn','check','Mật khẩu mới đã dùng được. Mã khôi phục cũ hết hiệu lực.')}
  <div><div class="lbl" style="font-size:14px">Mã khôi phục MỚI · chỉ hiện một lần</div><div class="code lt" style="font-size:19px">RC-2J9W-M4TD-6QKP</div></div>
  <div class="row small" style="font-size:14px"><span class="cbx on">${I('check')}</span><span>Em đã lưu mã khôi phục mới</span></div>${btn('Vào Bài của em','','arrow')}`:`
  <div><div class="lbl" style="font-size:14px">Mã người dùng</div><div class="field" style="padding:14px;font-size:17px">${I('users')}<span class="mono">HS-4P9T-37WD</span></div></div>
  <div><div class="lbl" style="font-size:14px">Mã khôi phục</div><div class="field" style="padding:14px;font-size:17px;${st==='sai'?'border-color:var(--red)':''}">${I('lock')}<span class="mono">${st==='sai'?'RC-5N7H-X2MB-C8VX':st==='het'?'RC-5N7H-X2MB-C8V':'RC-5N7H-X2MB-C8VR'}</span></div>
   ${st==='sai'?`<div class="small" style="color:#8A2A1A;font-weight:700;margin-top:5px;font-size:14px">Mã khôi phục chưa đúng. Em còn 7 lần thử trong 15 phút.</div>`:''}</div>
  <div><div class="lbl" style="font-size:14px">Mật khẩu mới (8 đến 64 ký tự)</div><div class="field" style="padding:14px;font-size:17px">${I('lock')}••••••••••<span class="grow"></span><span class="tiny muted">Hiện</span></div></div>
  ${st==='het'?ban('red','lock','Em đã thử quá 10 lần. Thử lại sau 14 phút.','Đã vào lớp: nhờ cô giáo đặt lại mật khẩu cho em ngay (không cần chờ).'):''}
  ${st==='het'?btn('Đặt mật khẩu mới','ghost','lock'):btn('Đặt mật khẩu mới','','arrow')}
  <div class="small muted" style="font-size:14px">Chưa nhớ mã? ${st==='het'?'':'Em đã vào lớp thì nhờ giáo viên của lớp đặt lại.'}</div>`}</div></div>`);
reg('c3-01-khoi-phuc','ipad','C3c','C3-01 · Khôi phục · nhập mã khôi phục, đặt mật khẩu mới (iPad)',()=>khoiPhuc('nhap'));
reg('c3-01-khoi-phuc-sai','ipad','C3c','C3-01 · Khôi phục · mã sai (iPad)',()=>khoiPhuc('sai'));
reg('c3-01-khoi-phuc-het-luot','ipad','C3c','C3-01 · Khôi phục · hết lượt thử, tạm khóa (iPad)',()=>khoiPhuc('het'));
reg('c3-01-khoi-phuc-xong','ipad','C3c','C3-01 · Khôi phục · xong, cấp mã khôi phục mới (iPad)',()=>khoiPhuc('xong'));

/* ===== kb7 b5 · Trình chiếu khi mất mạng ===== */
reg('c2-04-mat-mang','web','C2c','C2-04 · Trình chiếu · mất mạng, vẫn chiếu (đề và bước đã tải sẵn)',()=>pres(3,false,true));

/* ===== US-S3E5-05 · Xóa tài khoản (xác nhận 2 bước) ===== */
const caiDat=()=>`<div class="col grow" style="gap:14px;min-width:0"><div class="card pad col" style="gap:12px"><h2 style="margin:0">Tài khoản của em</h2>
 <div class="row" style="gap:12px"><div class="grow"><div class="lbl" style="font-size:14px">Tên hiển thị</div><div class="field">Bình An</div></div><div class="grow"><div class="lbl" style="font-size:14px">Mã người dùng</div><div class="field mono">HS-4P9T-37WD</div></div></div>
 <div class="row">${btn('Đổi mật khẩu','ghost sm','lock')}${btn('Xem thông báo riêng tư','ghost sm','shield')}</div></div>
 <div class="card pad col" style="gap:10px;border-color:#F3B6A8"><h2 style="margin:0;color:#8A2A1A">Xóa tài khoản</h2><div style="font-size:14px;color:var(--muted)">Xóa tên, bài làm và lượt hỏi AI của em. Không khôi phục được.</div><div class="row"><span class="btn" id="b-xoa" style="background:var(--red);color:#fff">${I('x')}Xóa tài khoản</span></div></div></div>`;
reg('c3-08-xoa-tai-khoan-1','ipad','C3c','C3-08 · Xóa tài khoản · bước 1/2 cảnh báo (HS, iPad)',()=>HS('Cài đặt','Bài của em › Cài đặt',caiDat()+`
<div class="ov"><div class="modal" style="width:620px"><div class="row" style="gap:10px"><div class="icb red">${I('flag')}</div><h2 style="margin:0">Xóa tài khoản? <span class="chip">Bước 1/2</span></h2></div>
 <div class="small" style="line-height:1.75;font-size:14.5px" id="m-xoa"><b>Khi xóa:</b><br>• Tên hiển thị, bài làm, lượt hỏi AI và tư cách thành viên lớp của em bị xóa.<br>• Bài làm của em biến khỏi bảng lớp 11A1 và mọi số liệu.<br>• Mã HS-4P9T-37WD và mã khôi phục hết hiệu lực, không cấp lại, không khôi phục được.<br>• Dữ liệu được xóa hẳn khỏi máy chủ trong tối đa 30 ngày (TẠM).</div>
 <div class="hint" style="align-items:center;gap:10px">${I('file','width:20px;height:20px;color:var(--pri)')}<div class="grow small" style="font-size:14px">Chính sách lưu và xóa dữ liệu nằm trong <u>thông báo riêng tư</u>.</div></div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Giữ tài khoản','')}${btn('Tiếp tục xóa','ghost','arrow')}</div></div></div>`));
reg('c3-08-xoa-tai-khoan-2','ipad','C3c','C3-08 · Xóa tài khoản · bước 2/2 mật khẩu + gõ XÓA (HS, iPad)',()=>HS('Cài đặt','Bài của em › Cài đặt',caiDat()+`
<div class="ov"><div class="modal" style="width:620px"><div class="row" style="gap:10px"><div class="icb red">${I('lock')}</div><h2 style="margin:0">Xác nhận xóa <span class="chip">Bước 2/2</span></h2></div>
 <div><div class="lbl" style="font-size:14px">Nhập lại mật khẩu</div><div class="field" style="padding:13px;font-size:16px">${I('lock')}••••••••••</div></div>
 <div><div class="lbl" style="font-size:14px">Gõ chữ <b class="mono">XÓA</b> để xác nhận</div><div class="field" style="padding:13px;font-size:16px" id="f-xoa"><span class="mono">XÓA</span><span class="cur1" style="height:18px"></span></div></div>
 <div class="tiny muted" style="font-size:14px">Sai mật khẩu tính vào giới hạn thử (10 lần mỗi 15 phút). Bấm xóa hai lần vẫn chỉ xóa một lần.</div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Quay lại','ghost')}<span class="btn" style="background:var(--red);color:#fff">${I('x')}Xóa tài khoản của tôi</span></div></div></div>`));
reg('c3-08-xoa-gv-bi-chan','web','C3c','C3-08 · Xóa tài khoản · giáo viên còn lớp Đang dạy bị chặn',()=>screen('x','gv',3,'Cài đặt','Tài khoản › Cài đặt',`
<div class="card pad col" style="width:560px;flex-shrink:0;gap:12px"><h2 style="margin:0">Tài khoản của cô Lan Anh</h2><div class="field mono">${I('users')}GV-7K3M-92QX</div>
 <div class="row">${btn('Đổi mật khẩu','ghost sm','lock')}${btn('Thông báo riêng tư','ghost sm','shield')}</div>
 <div class="pt" style="margin-top:8px">VÙNG NGUY HIỂM</div><div class="row"><span class="btn" style="background:var(--red);color:#fff">${I('x')}Xóa tài khoản</span></div></div>
<div class="ov"><div class="modal" style="width:580px"><div class="row" style="gap:10px"><div class="icb yel">${I('lock')}</div><h2 style="margin:0">Chưa xóa được tài khoản</h2></div>
 ${ban('yel','flag','Cô còn 2 lớp đang dạy.','Lưu trữ hết lớp rồi mới xóa tài khoản. Học sinh vẫn xóa được ở mọi lúc.')}
 <div class="col" style="gap:8px" id="l-lop">${[['11A1','32 em'],['11A2','30 em · 2 em chờ duyệt']].map(c=>`<div class="hint" style="align-items:center"><div class="icb pri">${I('users')}</div><div class="grow"><b>${c[0]}</b><div class="small muted">Đang dạy · ${c[1]}</div></div>${btn('Lưu trữ lớp','ghost sm','layers')}</div>`).join('')}</div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Đóng','ghost')}${btn('Đến Lớp học','','arrow')}</div></div></div>`,{concept:1}));

/* ===== US-S3E2-04 · Đóng / Lưu trữ bộ bài ===== */
const boRow=(t,sub,st,stc,acts)=>`<div class="hint" style="align-items:center"><div class="icb ${stc==='grn'?'grn':'pri'}">${I('layers')}</div><div class="grow"><b>${t}</b><div class="small muted">${sub}</div></div>${ch(stc,st)}${acts||''}</div>`;
reg('c3-03-dong-bo','web','C3c','C3-03 · Đóng bộ bài (một chiều)',()=>screen('x','gv',3,'Bộ bài lớp 11A1','Lớp học › 11A1 › Bộ bài',`
<div class="card pad col grow" style="gap:10px;min-width:0"><div class="tabs" style="border:none;padding:0;width:520px"><span class="on">Đang giao · 2</span><span>Đã đóng · 1</span><span>Đã lưu trữ · 3</span></div>
 ${boRow('Bộ bài tuần 15','4 đề · 28/32 em đã làm · hạn 18/12 20:00','Đã giao','grn',btn('Đóng bộ','ghost sm','lock'))}
 ${boRow('Bộ bài tuần 16','4 đề · chưa giao','Nháp','',btn('Giao bài','sm','send'))}
 <div class="tiny muted" style="font-size:13px">Bộ Nháp không đóng được. Bộ Đã giao mới đóng được.</div></div>
<div class="ov"><div class="modal" style="width:600px"><div class="row" style="gap:10px"><div class="icb org">${I('lock')}</div><h2 style="margin:0">Đóng bộ bài tuần 15?</h2></div>
 ${ban('yel','flag','Đóng là một chiều, không mở lại được.','Muốn các em làm tiếp thì tạo bộ mới.')}
 <div class="small" style="line-height:1.75" id="m-dong"><b>Khi đóng:</b><br>• Em không gửi thêm đáp số và không xin thêm gợi ý.<br>• Em vẫn xem kết quả của mình.<br>• Lịch mở lời giải vẫn chạy (18:00 thứ Sáu).<br>• Bảng lớp vẫn đọc được số liệu.</div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Để sau','ghost')}${btn('Đóng bộ','org','lock')}</div></div></div>`,{concept:1}));
reg('c3-03-luu-tru-bo','web','C3c','C3-03 · Lưu trữ bộ bài (bộ Đã đóng)',()=>screen('x','gv',3,'Bộ bài lớp 11A1','Lớp học › 11A1 › Bộ bài › Đã đóng',`
<div class="card pad col grow" style="gap:10px;min-width:0"><div class="tabs" style="border:none;padding:0;width:520px"><span>Đang giao · 1</span><span class="on">Đã đóng · 1</span><span>Đã lưu trữ · 3</span></div>
 ${boRow('Bộ bài tuần 15','Đã đóng 19/12 · em xem kết quả, không gửi thêm','Đã đóng','',btn('Lưu trữ bộ','org sm','layers'))}
 ${ban('pri','shield','Lưu trữ giúp màn "Bài đang giao" gọn lại.','Bộ rời "Bài đang giao" và danh sách chính của học sinh, nằm ở mục "Bộ đã lưu trữ". Bảng lớp vẫn đọc được số liệu.')}
 <div class="tiny muted" style="font-size:13px">Bộ Đã giao chưa đóng thì chưa lưu trữ được.</div></div>`,{concept:1}));
