/* s23c.js · Phiên bản: v0.1 · 2026-11-26 · Designer Dom (T-261126-36) · màn còn thiếu chặng 2–3 (Q_Dom mục C1–C8).
   Lịch sử: v0.1 · 26/11/2026 · tạo mới (runner-design-dom). Dữ liệu mẫu, không gọi AI thật. Tải sau s23.js và s23b.js. */
const ch=(c,t,i)=>`<span class="chip ${c||''}">${i?I(i):''}${t}</span>`;
const h2=(t,x)=>`<div class="row"><h2 style="margin:0">${t}</h2>${x?`<span class="grow"></span>${x}`:''}</div>`;
const tp=(t)=>`<div class="pt">${t}</div>`;
const G=(inner,st)=>`<div class="col grow" style="gap:14px;min-width:0;${st||''}">${inner}</div>`;
const GV=(title,crumb,body,o)=>screen('x','gv',1,title,crumb,body,Object.assign({concept:1},o||{}));
const HS=(title,crumb,body,o)=>screen('x','hs',0,title,crumb,body,Object.assign({noSeg:1,concept:1},o||{}));
const cb=(on)=>`<span class="cbx ${on?'on':''}">${on?I('check'):''}</span>`;
const Q1='Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng đáy.';

/* ===== C1 · S2-E4 · chat chỉnh nháp ===== */
reg('c2-04a-chat-chinh-nhap','web','C2b','C2-04a · Chat chỉnh nháp · AI hỏi lại, trước/sau',()=>GV('Chỉnh nháp với AI','Kho đề › Nháp "Góc giữa SC và đáy" › Trao đổi',`
<div class="card pad col" style="width:480px;flex-shrink:0;gap:12px;min-height:0">${h2('Trao đổi',ch('pur','AI soạn · chưa lưu','spark'))}
 <div class="col grow" style="gap:10px;overflow:hidden">
  <div class="bub me" style="align-self:flex-end;max-width:80%;background:var(--pri);color:#fff;border-radius:16px 16px 4px 16px;padding:10px 14px;font-size:14px">Đổi cạnh SA thành 2a rồi làm đề khó hơn.</div>
  <div style="max-width:92%;background:var(--soft);border-radius:16px 16px 16px 4px;padding:10px 14px;font-size:14px;line-height:1.55"><b>Hai ý, mình làm rõ ý hai trước.</b><br>"Khó hơn" thầy cô muốn theo hướng nào?
   <div class="row" style="gap:6px;flex-wrap:wrap;margin-top:8px">${ch('on','Thêm một câu hỏi phụ')}${ch('','Đổi sang góc giữa hai mặt phẳng')}${ch('','Số liệu không đẹp')}${ch('','Để mình tự chọn')}</div></div>
  <div class="bub me" style="align-self:flex-end;max-width:80%;background:var(--pri);color:#fff;border-radius:16px 16px 4px 16px;padding:10px 14px;font-size:14px">Thêm một câu hỏi phụ: tính thể tích.</div>
  <div style="max-width:92%;background:var(--soft);border-radius:16px 16px 16px 4px;padding:10px 14px;font-size:14px;line-height:1.55">Mình đề xuất 2 thay đổi, xem bên phải. <b>Chưa có gì được lưu</b> cho đến khi thầy cô bấm "Áp dụng".</div></div>
 <div class="field" style="padding:12px 14px">${I('chat')}<span class="muted" style="font-weight:500">Nhập yêu cầu, ví dụ "đổi cạnh BC thành 14"</span><span class="grow"></span>${btn('Gửi','sm','send')}</div></div>
<div class="card pad col grow" style="gap:12px;min-width:0">${h2('Đề xuất của AI · so sánh trước / sau',ch('yel','Máy chưa kiểm','flag'))}
 <div class="row" style="gap:12px;align-items:stretch"><div class="card pad col grow" style="gap:6px;background:var(--soft)">${tp('TRƯỚC · bản nháp hiện tại')}<div style="font-size:14px;line-height:1.7">Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = <span class="strike">a√2</span>. Tính góc giữa SC và mặt phẳng đáy.</div><div class="small muted">Đáp số: 45° (máy đã kiểm)</div></div>
 <div class="card pad col grow" style="gap:6px;border-color:var(--pri)">${tp('SAU · đề xuất')}<div style="font-size:14px;line-height:1.7">Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = <span class="ins">2a</span>. <span class="ins">a) </span>Tính góc giữa SC và mặt phẳng đáy. <span class="ins">b) Tính thể tích khối chóp S.ABCD.</span></div><div class="small muted">Đáp số: <span class="mono">đang chờ máy tính lại</span></div></div></div>
 <div class="row" style="gap:12px;align-items:stretch"><div class="thumb grow" style="height:150px">${pyramid({})}</div><div class="thumb grow" style="height:150px">${pyramid({ang:1})}</div></div>
 ${ban('yel','flag','Đáp số cũ (45°) không còn đúng sau khi đổi SA.','Máy sẽ tính lại khi thầy cô bấm Áp dụng. Đáp số do máy tính, không do AI (PO-46).')}
 <div class="row" style="gap:8px">${btn('Áp dụng vào nháp','','check')}${btn('Chỉ áp dụng ý 1','ghost')}${btn('Bỏ đề xuất','ghost','x')}<span class="grow"></span><span class="small muted">Bấm Hoàn tác được sau khi áp dụng</span></div></div>`));

/* ===== C1 · S2-E4 · import docx/pdf ===== */
const impRow=(n,t,st,sel,sub)=>{const m={new:['grn','Mới','check'],dup:['yel','Trùng kho · 92%','layers'],blur:['yel','Đọc chưa rõ','flag'],th:['','Lý thuyết · bỏ qua','book']}[st];return `<div class="hint" style="align-items:center;${st==='th'?'background:var(--soft);border-style:dashed':''}">${cb(sel)}<div class="num">${n}</div><div class="grow"><div style="font-size:13.5px;font-weight:600;line-height:1.5">${t}</div>${sub?`<div class="small muted">${sub}</div>`:''}</div>${ch(m[0],m[1],m[2])}${st==='dup'?btn('So sánh','soft sm','eye'):''}${st==='blur'?btn('Sửa chữ','soft sm','edit'):''}</div>`;};
reg('c2-04b-import-tach-bai','web','C2b','C2-04b · Import docx · tách từng bài',()=>GV('Nhập từ tệp','Kho đề › Thêm đề › Nhập từ Word/PDF',`
<div class="col grow" style="gap:14px;min-width:0"><div class="card pad row" style="gap:14px"><div class="icb pri" style="width:44px;height:44px">${I('file')}</div><div class="grow"><b>GiaoAn_HinhChop_11.docx</b><div class="small muted">14 trang · 1,2 MB · tải lên 11:42 · máy tìm thấy 7 phần</div></div>${ch('grn','Đã tách xong','check')}${btn('Đổi tệp','ghost sm','reset')}</div>
 <div class="card pad col grow" style="gap:8px;overflow:hidden">${h2('7 phần tìm thấy · chọn bài muốn nhập',`<span class="small muted">Lý thuyết (bài giảng) chưa nhập được: dời chặng 5</span>`)}
 ${impRow(1,'Bài 1. Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA ⊥ (ABCD)… Tính góc giữa SC và đáy.','new',1,'Trang 3 · AI chép lại nguyên văn, thầy cô sửa được')}
 ${impRow(2,'Bài 2. Tính khoảng cách từ A đến mặt phẳng (SBC).','new',1,'Trang 4')}
 ${impRow(3,'Bài 3. Cho hình hộp chữ nhật có ba kích thước 2, 3, 4. Tính thể tích.','dup',0,'Giống đề "Thể tích khối hộp chữ nhật" đã có trong Kho chung')}
 ${impRow(4,'Bài 4. Tính góc giữa hai mặt phẳng (SAB) và (SCD)… (chữ mờ ở dòng 2)','blur',1,'Trang 6 · bản quét, AI đọc chắc 2/3 dòng')}
 ${impRow(5,'Lý thuyết: Định nghĩa góc giữa đường thẳng và mặt phẳng','th',0,'Trang 2 · không phải bài tập')}
 ${impRow(6,'Bài 5. Thiết diện của hình chóp cắt bởi mặt phẳng đi qua trung điểm…','new',1,'Trang 8')}</div></div>
<div class="card pad col" style="width:350px;flex-shrink:0;gap:12px">${h2('Sẽ nhập')}
 <div class="hint" style="align-items:center"><div class="icb grn">${I('check')}</div><div><b>4 bài</b><div class="small muted">vào Nháp "Của tôi"</div></div></div>
 <div class="hint" style="align-items:center"><div class="icb yel">${I('layers')}</div><div><b>1 bài trùng</b><div class="small muted">chưa chọn, bấm So sánh</div></div></div>
 <div class="hint" style="align-items:center"><div class="icb pur">${I('book')}</div><div><b>1 phần lý thuyết</b><div class="small muted">bỏ qua</div></div></div>
 <div class="grow"></div><div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Mỗi bài thành một nháp riêng, mang nhãn "AI soạn · Cần xem lại". Chưa bài nào vào Kho cho đến khi thầy cô duyệt.</span></div>${btn('Nhập 4 bài vào nháp','','arrow')}</div>`));

reg('c2-04c-doi-chieu-trung','web','C2b','C2-04c · Đối chiếu trùng với kho',()=>GV('So sánh bài trùng','Kho đề › Nhập từ tệp › Bài 3',`
<div class="col grow" style="gap:14px;min-width:0">${ban('yel','layers','Bài 3 giống đề đã có trong Kho chung (độ giống 92%).','Khác nhau chỉ ở dấu cách và cách viết "ba kích thước". Hệ thống chỉ so với Kho chung và đề của chính thầy cô; đề riêng của giáo viên khác không hiện ra.')}
 <div class="row grow" style="gap:14px;align-items:stretch;min-height:0"><div class="card pad col grow" style="gap:10px">${tp('TRONG TỆP WORD · Bài 3')}<div style="font-size:15px;line-height:1.7">Cho hình hộp chữ nhật có ba kích thước <span class="mark">2, 3, 4</span>. Tính thể tích.</div><div class="thumb" style="height:160px">${sh.hop('#3B5BDB')}</div><div class="small muted">Chưa có đáp số · máy chưa tính</div></div>
 <div class="card pad col grow" style="gap:10px;background:var(--soft)">${tp('TRONG KHO CHUNG · Thể tích khối hộp chữ nhật')}<div style="font-size:15px;line-height:1.7">Cho hình hộp chữ nhật có các kích thước <span class="mark">2 cm, 3 cm, 4 cm</span>. Tính thể tích của khối hộp.</div><div class="thumb" style="height:160px;background:#fff">${sh.hop('#3B5BDB')}</div><div class="row">${ch('grn','Đã kiểm chứng','check')}${ch('pri','Giáo viên đã duyệt','shield')}<span class="small muted">Đáp số 24 cm³ · dùng ở 3 lớp</span></div></div></div>
 <div class="row" style="gap:8px">${btn('Dùng đề trong Kho (bỏ bài này)','','check')}${btn('Giữ cả hai','ghost','layers')}${btn('Thay bằng bản trong tệp (tạo phiên bản mới)','ghost','edit')}<span class="grow"></span><span class="small muted">Mặc định: dùng đề trong Kho (TẠM)</span></div></div>`));

reg('c2-04d-import-loi-tep','web','C2b','C2-04d · Import · tệp lỗi',()=>{const c=(icn,col,t,p,b)=>`<div class="card pad col" style="gap:10px;flex:1"><div class="icb ${col}" style="width:44px;height:44px">${I(icn)}</div><b style="font-size:15px">${t}</b><div class="small muted" style="line-height:1.55">${p}</div><div class="grow"></div>${b}</div>`;return GV('Nhập từ tệp','Kho đề › Thêm đề › Nhập từ Word/PDF › Tệp chưa đọc được',`
<div class="col grow" style="gap:14px;min-width:0">${ban('red','flag','Chưa nhập được tệp. Chưa có gì bị thay đổi trong Kho.','Thầy cô thử một trong các cách bên dưới. Đã chọn tệp nào thì tên tệp vẫn hiện để chọn lại.')}
 <div class="row grow" style="gap:14px;align-items:stretch">
 ${c('file','red','Tệp bị hỏng','Word không mở được "GiaoAn_cu.docx". Thử mở bằng Word rồi lưu lại, hoặc xuất PDF.',btn('Chọn tệp khác','ghost','file'))}
 ${c('lock','org','Tệp đặt mật khẩu','Hệ thống không nhập tệp có mật khẩu. Thầy cô bỏ mật khẩu rồi tải lại, hoặc chụp từng trang.',btn('Tải lại','ghost','reset'))}
 ${c('camera','yel','PDF là bản quét ảnh','Máy đọc được nhưng có thể sai chữ. Các ô chưa chắc sẽ tô vàng để thầy cô sửa trước khi nhập.',btn('Vẫn đọc bản quét','','arrow'))}
 ${c('layers','pri','Tệp quá lớn','Tối đa 10 MB hoặc 40 trang mỗi lần. Tách tệp thành hai phần rồi tải từng phần.',btn('Chọn tệp khác','ghost','file'))}</div>
 <div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Giới hạn 10 MB / 40 trang là TẠM (DQ-33). Tệp chỉ dùng để tách bài, không lưu lại sau khi nhập xong (TẠM).</span></div></div>`);});

/* ===== C2 · S2-E9 ===== */
reg('c2-09a-bien-the','web','C2b','C2-09a · Biến thể đề · đáp số máy tính lại',()=>{const vr=(n,a,sa,ans,st,note)=>`<div class="hint" style="align-items:center;gap:12px">${cb(st!=='bad')}<div class="num">${n}</div><div class="thumb" style="width:70px;height:56px">${pyramid({})}</div><div class="grow"><b>a = ${a} · SA = ${sa}</b><div class="small muted">${note}</div></div><div style="text-align:right"><div class="small muted">Máy tính</div><b class="mono">${ans}</b></div>${st==='bad'?ch('red','Không dựng được','flag'):st==='dup'?ch('yel','Trùng đề khác','layers'):ch('yel','Chưa duyệt','clock')}</div>`;return GV('Tạo biến thể','Kho đề › Góc giữa SC và đáy › Biến thể',`
<div class="card pad col" style="width:380px;flex-shrink:0;gap:12px">${h2('Đề gốc',ch('grn','Đã kiểm chứng','check'))}<div style="font-size:13.5px;line-height:1.65">${Q1}</div><div class="thumb" style="height:130px">${pyramid({})}</div>${tp('SỐ LIỆU ĐƯỢC ĐỔI')}
 <div class="row"><div class="field grow">Cạnh a: 3 → 8</div></div><div class="row"><div class="field grow">SA: a√2 → a·(1 đến 3)</div></div><div class="row"><div class="field grow">Số biến thể: 3</div></div>${btn('Tạo 3 biến thể','','spark')}</div>
<div class="card pad col grow" style="gap:8px;min-width:0">${h2('3 biến thể đề xuất',`<span class="small muted">Chỉ đáp số do máy tính, AI không đoán</span>`)}
 ${vr(1,5,'5√2','45°','new','Cùng dạng, số khác đề gốc')}${vr(2,6,'6','arctan(1/√2) ≈ 35,3°','new','Đáp số không đẹp: máy ghi cả dạng gần đúng')}${vr(3,3,'3√2','45°','dup','Cùng đáp số với biến thể 1; hình đồng dạng')}
 ${ban('red','flag','Có bộ số không dựng được hình (ví dụ SA ≤ 0).','Bộ số bị loại, không đưa vào danh sách. Máy tạo bộ khác thay thế.')}
 <div class="grow"></div><div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Biến thể là nháp "AI soạn · Cần xem lại" cho đến khi thầy cô duyệt (PO-02). Không tự giao cho lớp.</span></div>
 <div class="row" style="gap:8px">${btn('Lưu 2 biến thể đã chọn vào nháp','','check')}${btn('Tạo bộ khác','ghost','reset')}</div></div>`);});

reg('c2-09b-xuat-lien-ket','web','C2b','C2-09b · Xuất ảnh/PDF + liên kết chỉ xem',()=>GV('Kho đề hình không gian','Kho đề › Góc giữa SC và đáy',`<div class="col grow" style="gap:14px"><div class="card pad grow" style="background:var(--soft)"><div class="thumb" style="height:100%">${pyramid({})}</div></div></div>
<div class="ov"><div class="modal" style="width:620px;gap:12px"><div class="row"><div class="icb pri">${I('send')}</div><h2 style="margin:0">Xuất đề &amp; chia sẻ</h2><span class="grow"></span>${I('x','width:20px;height:20px')}</div>
 <div class="tabs" style="border:none;padding:0"><span class="on">Xuất tệp</span><span>Liên kết chỉ xem</span></div>
 <div class="row" style="gap:10px"><div class="hint grow" style="border-color:var(--pri);background:var(--pri-s)">${I('file')}<div><b>PDF · A4</b><div class="small muted">In cho cả lớp</div></div></div><div class="hint grow">${I('camera')}<div><b>Ảnh PNG</b><div class="small muted">Dán vào Zalo, slide</div></div></div></div>
 <div class="row" style="gap:10px">${cb(0)}<div><b>Kèm lời giải</b><div class="small muted">Mặc định tắt. Thầy cô tự tick mới xuất lời giải.</div></div></div>
 ${ban('yel','lock','Đề này đang khóa lời giải ở lớp 11A1 đến 18:00 thứ Sáu.','Nếu kèm lời giải, tệp có thể lộ ra ngoài hệ thống. Bấm Kèm lời giải sẽ hỏi lại một lần nữa.')}
 <div class="hint" style="gap:10px">${I('layers')}<div class="grow"><b>Liên kết chỉ xem</b><div class="mono small" style="margin-top:4px">geo.app/x/7Kq3-mP9d</div><div class="small muted">Hết hạn sau 7 ngày · chỉ đề và hình, không lời giải · thầy cô thu hồi được</div></div>${btn('Sao chép','ghost sm','layers')}${btn('Thu hồi','ghost sm','x')}</div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Hủy','ghost')}${btn('Xuất PDF','','arrow')}</div></div></div>`));

reg('c2-09c-trac-nghiem-gv','web','C2b','C2-09c · Soạn câu trắc nghiệm',()=>{const o=(l,t,ok,w)=>`<div class="hint" style="align-items:center;${ok?'border-color:var(--grn);background:var(--grn-s)':''}${w?'border-color:#F3D894;background:var(--yel-s)':''}"><div class="num ${ok?'done':''}">${l}</div><div class="grow"><b class="mono" style="font-size:15px">${t}</b>${w?`<div class="small" style="color:#7A5200">${w}</div>`:''}</div>${ok?ch('grn','Đáp án đúng','check'):''}</div>`;return GV('Soạn bài luyện','Kho đề › Bài luyện mới › Câu 3 · trắc nghiệm',`
<div class="card pad col grow" style="gap:10px;min-width:0">${h2('Câu 3 · chọn đáp án',`<div class="seg"><span>Một số</span><span>Biểu thức</span><span class="on">Chọn đáp án</span></div>`)}<div style="font-size:14.5px;line-height:1.65">${Q1}</div>${tp('4 LỰA CHỌN · chọn 1 đáp án đúng')}
 ${o('A','30°')}${o('B','45°',1)}${o('C','60°',0,'Giống lựa chọn D khi làm tròn. Thầy cô xem lại.')}${o('D','60,0°',0,'Trùng với C.')}
 <div class="row small muted">${I('plus','width:14px;height:14px')}Thêm lựa chọn (tối đa 5) · kéo để đổi thứ tự</div></div>
<div class="card pad col" style="width:340px;flex-shrink:0;gap:12px">${h2('Cài đặt câu')}<div><div class="lbl">Số lượt thử</div><div class="row">${ch('on','1 lượt')}${ch('','2')}${ch('','3')}</div><div class="tiny muted" style="margin-top:4px">Mặc định 1 lượt: nhiều lượt dễ đoán bằng loại trừ.</div></div>
 <div class="row"><div class="toggle"></div><div class="small"><b>Xáo thứ tự lựa chọn</b><div class="tiny muted">Mỗi em một thứ tự</div></div></div>
 ${ban('pri','shield','Máy kiểm tra: có đúng 1 lựa chọn trùng với đáp số máy tính (45°).','Phản hồi cho học sinh không lộ lựa chọn nào đúng.')}${ban('yel','flag','2 lựa chọn gần giống nhau.','Sửa trước khi giao.')}<div class="grow"></div>${btn('Lưu câu','','check')}</div>`);});

reg('c2-09d-trac-nghiem-hs','ipad','C2b','C2-09d · HS làm câu trắc nghiệm (iPad)',()=>HS('Bài luyện: Góc trong hình chóp','Bài của em › Bộ luyện 11A1 › Câu 3',`
<div class="canvas c3d grow" style="min-width:0"><div class="prob"><div class="icb pri">${I('file')}</div><div class="grow">${Q1}</div></div><div class="fig">${pyramid({})}</div></div>
<div class="card side pad col" style="gap:12px;width:400px"><div class="row" style="gap:8px">${ch('pri','Câu 3/5','cube')}${ch('yel','Còn 1/1 lượt','flame')}</div>${tp('CHỌN MỘT ĐÁP ÁN')}
 ${[['A','30°',0],['B','45°',1],['C','60°',0],['D','90°',0]].map(r=>`<div class="hint" style="align-items:center;min-height:56px;${r[2]?'border-color:var(--pri);background:var(--pri-s)':''}"><div class="num" style="width:32px;height:32px;font-size:15px">${r[0]}</div><b style="font-size:18px" class="mono">${r[1]}</b><span class="grow"></span>${r[2]?I('check','width:22px;height:22px;color:var(--pri)'):''}</div>`).join('')}
 <div class="small muted">Em chọn B. Bấm Trả lời để chốt: câu này chỉ có 1 lượt.</div><div class="grow"></div>${btn('Trả lời','','check')}</div>`));

/* ===== C3 · S2-E8 bài luyện: đang tải, mất mạng ===== */
const sk=(w,h,st)=>`<div class="skel" style="width:${w};height:${h}px;${st||''}"></div>`;
reg('c2-06-dang-tai','ipad','C2b','C2-06 · Bài luyện · đang tải câu hỏi (iPad)',()=>HS('Bài luyện: Góc trong hình chóp','Bài của em › Bộ luyện 11A1 › Câu 2',`
<div class="canvas c3d grow" style="min-width:0;display:flex;flex-direction:column;gap:14px;padding:24px;align-items:flex-start">${sk('90%',22)}${sk('70%',22)}<div class="grow" style="width:100%;display:flex;align-items:center;justify-content:center;flex-direction:column;gap:12px"><div class="spin"></div><b style="font-size:16px">Đang tải câu hỏi và hình…</b><div class="small muted">Thường mất vài giây. Câu 1 em đã làm vẫn được lưu.</div></div></div>
<div class="card side pad col" style="gap:12px;width:400px"><div class="row" style="gap:8px">${ch('pri','Câu 2/5','cube')}${ch('','Số lượt: đang tải','clock')}</div>${tp('ĐÁP SỐ CỦA EM')}<div class="mathin" style="opacity:.55;border-color:var(--line)"><span class="muted" style="font-family:inherit;font-weight:500;font-size:16px">Chờ câu hỏi tải xong</span></div><div class="grow"></div><div class="kbd2" style="opacity:.45;pointer-events:none">${KEYS.map(r=>r.map(k=>`<b>${k}</b>`).join('')).join('')}<b class="fn w2">x</b><b class="fn w2">Xóa hết</b><b class="ok w2" style="background:#9AA6C8">Kiểm tra</b></div></div>`));

reg('c2-06-mat-mang','phone','C2b','C2-06 · Bài luyện · mất mạng khi gửi (iPhone)',()=>HS('Bài luyện: Góc trong hình chóp','Bài của em › Bộ luyện 11A1 › Câu 2',`
<div class="col grow" style="gap:10px;min-width:0">${lp(2,'key')}${ban('red','flag','Chưa gửi được: mất kết nối mạng.','Đáp số 45° vẫn giữ trên máy. Lượt chưa bị trừ. Em bấm Gửi lại khi có mạng.',btn('Gửi lại','sm','reset'))}<div class="mathin"><span class="v">45°</span><span class="grow"></span>${ch('red','Chưa gửi','clock')}</div><div class="card pad" style="font-size:15px;line-height:1.5">${PROB}</div></div>${kbd()}`));

/* ===== C4 · C3-07 mã sai, bị từ chối ===== */
reg('c3-07-ma-sai','ipad','C3b','C3-07 · HS nhập mã lớp sai / lớp đã khóa (iPad)',()=>HS('Vào lớp','Bài của em › Vào lớp',`
<div class="col grow" style="align-items:center;justify-content:center;gap:16px;min-width:0"><div class="card pad col" style="width:560px;gap:14px;padding:26px"><h2 style="margin:0">Nhập mã lớp của thầy cô</h2>
 <div class="field" style="padding:16px;font-size:24px;justify-content:center;letter-spacing:.12em;border-color:var(--red);background:#FFF8F7"><span class="mono">HK-4P7X</span></div>
 ${ban('red','flag','Mã lớp chưa đúng.','Em kiểm tra lại 6 ký tự (chữ O và số 0 dễ nhầm). Còn 3 lần thử, sau đó chờ 10 phút.')}
 ${btn('Gửi xin vào lớp','','send')}
 <div class="hint" style="gap:10px;background:var(--soft)">${I('lock','width:20px;height:20px;color:var(--muted)')}<div class="small"><b>Gặp "Lớp đã đóng nhận thêm"?</b><br>Thầy cô đã khóa mã lớp. Em nhắn thầy cô để được mở lại hoặc nhận mã mới.</div></div>
 <div class="row small muted" style="gap:6px">${I('cube','width:14px;height:14px')}Trong lúc đó, em vẫn dùng Tự học được.</div></div></div>`));

reg('c3-07-bi-tu-choi','phone','C3b','C3-07 · HS bị từ chối vào lớp (iPhone)',()=>HS('Vào lớp','Bài của em › Vào lớp',`
<div class="col grow" style="gap:12px;min-width:0"><div class="card pad col" style="gap:12px"><div class="icb red" style="width:44px;height:44px">${I('flag')}</div><h2 style="margin:0">Cô Lan Anh chưa nhận em vào lớp 11A1</h2><div class="small" style="line-height:1.6">Yêu cầu của em gửi lúc 08:12 hôm nay chưa được chấp nhận. Có thể cô nhận nhầm tên, hoặc lớp đã đủ. Hệ thống không biết lý do.</div>
 ${ban('pri','chat','Em nhắn cô trực tiếp (ngoài ứng dụng) để hỏi lý do.','Ứng dụng không thu số điện thoại hay email của em (PO-61).')}</div>
 ${btn('Nhập mã lớp khác','','arrow')}${btn('Vào Tự học','ghost','cube')}<div class="tiny muted">Em không bị khóa tài khoản. Em gửi lại cùng mã được tối đa 2 lần mỗi ngày (TẠM).</div></div>`));

/* ===== C5 · bàn phím biểu thức nâng cao ===== */
reg('c2-08-ban-phim-nang-cao','ipad','C2b','C2-08 · Bàn phím biểu thức nâng cao (iPad)',()=>HS('Bài luyện: Khoảng cách','Bài của em › Bộ luyện 11A1 › Câu 4 · biểu thức',`
<div class="canvas c3d grow" style="min-width:0"><div class="prob"><div class="icb pri">${I('file')}</div><div class="grow">Tính khoảng cách từ A đến mặt phẳng (SBC). Viết đáp số theo a.</div></div><div class="fig">${pyramid({proj:1})}</div></div>
<div class="card side pad col" style="gap:10px;width:470px"><div class="row" style="gap:8px">${ch('pri','Câu 4/5','cube')}${ch('yel','Còn 3/3 lượt','flame')}</div>${tp('ĐÁP SỐ CỦA EM · dạng biểu thức')}
 <div class="mathin" style="font-size:26px;min-height:92px;gap:10px"><span class="v" style="display:flex;align-items:center;gap:10px">a<span class="fr"><span>√2</span><i></i><span class="box">▫</span></span></span><span class="cur1"></span></div>
 <div class="small muted">Chấp nhận dạng tương đương: <b class="mono">a√2/2</b> = <b class="mono">√2a/2</b> = <b class="mono">a/√2</b>. Thiếu dấu ngoặc: máy báo "chưa hiểu biểu thức", <b>không trừ lượt</b>.</div>
 <div class="tabs" style="border:none;padding:0"><span>Số</span><span class="on">Phân số, căn</span><span>Mũ, chỉ số</span></div>
 <div class="kbd2 kbd-adv">${[['▫⁄▫','Phân số'],['√▫','Căn'],['ⁿ√▫','Căn bậc n'],['▫²','Bình phương'],['▫ⁿ','Mũ'],['|▫|','Giá trị tuyệt đối'],['π','pi'],['(  )','Ngoặc'],['a','cạnh a'],['◀','trái'],['▶','phải'],['⌫','xóa']].map(k=>`<b class="${'◀▶⌫'.includes(k[0])?'fn':''}"><span>${k[0]}</span><small>${k[1]}</small></b>`).join('')}<b class="fn w2">Xóa hết</b><b class="ok w4">${I('check','width:18px;height:18px')}Kiểm tra</b></div></div>`));

/* ===== C6 · S3-E4 thông báo ===== */
const nt=[['cube','pri','Bài mới: Bộ bài tuần 15','Cô Lan Anh vừa giao cho lớp 11A1 · hạn 20:00 thứ Sáu','10 phút trước',1],['clock','yel','Sắp hết hạn: Bộ bài tuần 15','Còn 20 giờ. Em còn 2 đề chưa làm','Hôm qua 20:00',1],['unlock','grn','Lời giải đã mở: Bộ luyện góc','Em xem lại 2 câu cần xem lại','Hôm qua 18:00',0],['users','grn','Cô duyệt em vào lớp 11A1','Bây giờ em thấy bài của lớp','Thứ Hai',0],['flag','red','Cô chưa nhận em vào lớp 11A2','Em nhắn cô để hỏi lý do','Thứ Hai',0]];
const ntList=(dev)=>`<div class="col grow" style="gap:10px;min-width:0"><div class="row" style="gap:8px"><div class="tabs" style="border:none;padding:0;width:${dev==='ipad'?'320px':'100%'}"><span class="on">Tất cả</span><span>Chưa đọc · 2</span></div>${dev==='ipad'?`<span class="grow"></span>${btn('Đánh dấu đã đọc hết','ghost sm','check')}`:''}</div>
 ${nt.map(n=>`<div class="hint" style="align-items:center;${n[5]?'background:var(--pri-s);border-color:#C9D3F5':''}"><div class="icb ${n[1]}">${I(n[0])}</div><div class="grow"><b style="font-size:${dev==='ipad'?15:14}px">${n[2]}</b><div class="small muted">${n[3]}</div></div><div style="text-align:right;flex-shrink:0"><div class="tiny muted">${n[4]}</div>${n[5]?'<span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:var(--pri);margin-top:4px"></span>':''}</div></div>`).join('')}
 <div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Chỉ thông báo trong ứng dụng (không SMS, không email). Không nhắc từ 21:00 đến 06:00 (TẠM).</span></div></div>`;
reg('c3-04-thong-bao-ipad','ipad','C3b','C3-04 · Thông báo trong ứng dụng (iPad)',()=>HS('Thông báo','Bài của em › Thông báo',ntList('ipad')));
reg('c3-04-thong-bao-iphone','phone','C3b','C3-04 · Thông báo trong ứng dụng (iPhone)',()=>HS('Thông báo','Bài của em › Thông báo',ntList('phone')));

/* ===== C7 · S3-E6 ===== */
reg('c3-06a-chua-bai-trinh-chieu','web','C3b','C3-06a · Chữa bài từ kết quả lớp → Trình chiếu',()=>GV('Kết quả lớp 11A1','Theo dõi › Bộ bài tuần 15 › Chữa bài',`
<div class="card pad col" style="width:520px;flex-shrink:0;gap:12px">${h2('Cả lớp vướng ở đâu',ch('pur','AI gộp','spark'))}
 <div class="hint col" style="gap:8px;border-color:var(--pri)"><b>Đề 3 · hai mặt phẳng</b><div class="small">Nhiều em xác định sai giao tuyến của (SAB) và (SCD).</div><div class="row" style="gap:6px">${ch('','6 bài làm','eye')}${ch('yel','Cần thầy cô xác nhận')}</div><div class="row" style="gap:8px">${btn('Chữa bài này','','play')}${btn('Xem bài làm căn cứ','ghost sm','eye')}</div></div>
 <div class="hint col" style="gap:8px"><b>Đề 2 · khoảng cách</b><div class="small">Một nhóm em dựng đường cao không vuông góc.</div><div class="row">${btn('Chữa bài này','ghost sm','play')}</div></div></div>
<div class="card pad col grow" style="gap:12px;min-width:0;background:var(--soft)">${h2('Sẽ mở Trình chiếu',ch('','Xem trước'))}
 <div class="row" style="gap:8px;flex-wrap:wrap">${ch('pri','Đề 3 · hai mặt phẳng','cube')}${ch('pri','Bước 2 · tìm giao tuyến','flag')}${ch('grn','Đã tải sẵn','check')}</div>
 <div class="card pad row grow" style="gap:12px;align-items:center;background:#141B2D;color:#fff;border:none"><div style="width:42%">${pyramid({})}</div><div class="col" style="gap:8px;flex:1"><div class="stpb" style="background:rgba(255,255,255,.07);border-radius:12px;padding:10px 12px;font-size:13px;opacity:.6">1. Hai mặt phẳng (SAB), (SCD) có chung điểm S.</div><div class="stpb cur" style="background:rgba(124,155,255,.18);border:1.5px solid #7C9BFF;border-radius:12px;padding:10px 12px;font-size:14px">2. AB // CD nên giao tuyến là đường thẳng qua S song song AB.</div><div class="stpb hid" style="border:1.5px dashed rgba(255,255,255,.2);border-radius:12px;padding:10px 12px;font-size:13px;color:#8F9BB5">3 · bấm → để hiện</div></div></div>
 ${ban('yel','lock','Lời giải bộ này còn khóa trên máy học sinh đến 18:00.','Trình chiếu chỉ là màn của thầy cô; máy học sinh vẫn khóa.')}${ban('pri','edit','Đề 3 vừa sửa (v3) hôm nay 09:40.','Bước 2 có thể đã đổi so với lúc các em làm bài.')}</div>`));

reg('c3-06b-xuat-ket-qua','web','C3b','C3-06b · Xuất kết quả lớp',()=>GV('Kết quả lớp 11A1','Theo dõi › Bộ bài tuần 15',`<div class="card pad grow" style="background:var(--soft)">${tp('BẢNG LỚP × BÀI (đang xem)')}<div style="opacity:.35">${[1,2,3,4,5].map(()=>sk('100%',30,'margin-bottom:10px')).join('')}</div></div>
<div class="ov"><div class="modal" style="width:640px"><div class="row"><div class="icb pri">${I('file')}</div><h2 style="margin:0">Xuất kết quả lớp 11A1</h2></div>
 <div><div class="lbl">Bộ bài</div><div class="row" style="gap:6px;flex-wrap:wrap">${ch('on','Tuần 15','check')}${ch('on','Tuần 14','check')}${ch('','Tuần 13')}</div></div>
 <div><div class="lbl">Định dạng</div><div class="row">${ch('on','CSV (mở bằng Excel)')}${ch('','XLSX')}</div></div>
 <div><div class="lbl">Xem trước 3 dòng đầu</div><table style="width:100%;font-size:12.5px;border-collapse:collapse"><tr style="color:var(--muted)"><td>Mã</td><td>Tên</td><td>Đề 1</td><td>Đề 2</td><td>Đề 3</td></tr>${[['HS-4P9T','Bình An','Đúng','Xem lại','Đúng'],['HS-8L2K','Chi Mai','Đúng','Xem lại','Xem lại'],['HS-3M7Q',"'=Đức Huy",'Đúng','Đúng','Xem lại']].map(r=>`<tr style="border-top:1px solid var(--line)">${r.map(c=>`<td style="padding:6px 4px" class="${c.startsWith("'")?'mono':''}">${c}</td>`).join('')}</tr>`).join('')}</table><div class="tiny muted" style="margin-top:4px">Tên bắt đầu bằng = + − @ được thêm dấu ' phía trước để bảng tính không chạy thành công thức.</div></div>
 ${ban('pri','shield','Không có cột tổng, không xếp hạng. Chỉ trạng thái từng đề.','Tệp chứa tên và mã học sinh: thầy cô giữ cẩn thận (PO-61).')}
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Hủy','ghost')}${btn('Tải tệp','','arrow')}</div></div></div>`));

reg('c3-06c-luu-tru-lop','web','C3b','C3-06c · Lưu trữ lớp cuối năm',()=>screen('x','gv',3,'Lớp học','Lớp học › Năm học 2026–2027',`<div class="row grow" style="gap:14px;align-items:flex-start">${[['11A1','Đang dạy · 32 em','grn',0],['11A2','Đang dạy · 30 em · 2 em chờ duyệt','grn',1],['10B3','Đã lưu trữ 05/2026 · 28 em','',0]].map(c=>`<div class="card pad col" style="width:330px;gap:10px;${c[3]?'border-color:var(--pri)':''}"><div class="row"><div class="icb pri">${I('users')}</div><h2 style="margin:0">${c[0]}</h2><span class="grow"></span>${ch(c[2],c[2]?'Đang dạy':'Đã lưu trữ')}</div><div class="small muted">${c[1]}</div><div class="row">${btn('Mở lớp','soft sm')}${c[2]?btn('Lưu trữ lớp','ghost sm','layers'):btn('Khôi phục','ghost sm','reset')}</div></div>`).join('')}</div>
<div class="ov"><div class="modal" style="width:560px"><div class="row"><div class="icb org">${I('layers')}</div><h2 style="margin:0">Lưu trữ lớp 11A2?</h2></div>
 ${ban('yel','flag','Còn 2 em đang chờ duyệt.','Thầy cô duyệt hoặc từ chối trước, nếu không các em sẽ thấy "lớp đã đóng".')}
 <div class="small" style="line-height:1.7"><b>Khi lưu trữ:</b><br>• Mã lớp HK-4P7Q đóng, không nhận thêm em.<br>• Không giao bài mới. Em vẫn xem lại bài và kết quả đã có.<br>• Lớp ẩn khỏi danh sách Lớp học; xem lại ở mục "Đã lưu trữ".<br>• Thầy cô khôi phục lại được trong 12 tháng (TẠM).</div>
 <div class="hint" style="gap:10px;align-items:center">${I('file','width:20px;height:20px;color:var(--pri)')}<div class="grow small"><b>Xuất kết quả lớp trước?</b> Nên lưu tệp trước khi lưu trữ.</div>${btn('Xuất','ghost sm')}</div>
 <div class="field">Gõ tên lớp <b class="mono" style="margin:0 6px">11A2</b> để xác nhận<span class="grow"></span></div>
 <div class="row" style="justify-content:flex-end;gap:8px">${btn('Để sau','ghost')}${btn('Lưu trữ lớp','org','layers')}</div></div></div>`));

/* ===== C8 · trạng thái rỗng/lỗi chung ===== */
const st=(icn,col,t,p,b)=>`<div class="card pad col" style="gap:8px;align-items:flex-start"><div class="icb ${col}" style="width:40px;height:40px">${I(icn)}</div><b style="font-size:15px">${t}</b><div class="small muted" style="line-height:1.55">${p}</div><div class="row" style="gap:6px;margin-top:2px">${b}</div></div>`;
reg('c8-trang-thai-gv','web','C8','C8 · Bộ trạng thái rỗng / lỗi chung (giáo viên)',()=>GV('Bộ trạng thái chung','Quy ước cho mọi màn C2/C3 · giáo viên',`<div class="col grow" style="gap:14px;min-width:0"><div class="row" style="gap:14px;align-items:stretch">
 ${st('users','pri','Lớp chưa có học sinh','Chia sẻ mã lớp HK-4P7Q hoặc in trang A4 cho học sinh vào lớp.',btn('Xem mã lớp','sm','users')+btn('In trang lớp','ghost sm','file'))}
 ${st('book','pur','Kho đề còn trống','Thêm đề đầu tiên bằng cách dán chữ, tải ảnh hoặc nhập từ Word.',btn('Thêm đề','sm','plus')+btn('Xem Kho chung','ghost sm','layers'))}
 ${st('spark','org','AI chưa trả lời được','Bản nháp chữ vẫn giữ. Thử lại hoặc dựng tay. Không có đề nào bị mất.',btn('Thử lại','sm','reset')+btn('Dựng tay','ghost sm','edit'))}</div>
 <div class="row" style="gap:14px;align-items:stretch">
 ${st('lock','yel','Phiên đã hết hạn','Thầy cô đăng nhập lại bằng mã GV và mật khẩu. Nháp đang soạn được giữ trong máy.',btn('Đăng nhập lại','sm','arrow'))}
 ${st('flag','red','Chưa kết nối mạng','Bạn đang xem bản đã lưu. Nút gửi, giao bài tạm khóa cho tới khi có mạng.',btn('Thử lại','sm','reset'))}
 ${st('shield','pri','Bạn chưa có quyền xem lớp này','Lớp thuộc giáo viên khác. Nếu nhầm, nhờ giáo viên đó mời thầy cô vào lớp.',btn('Về Lớp học','ghost sm','users'))}</div>
 <div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Quy ước: mỗi trạng thái có (1) nói rõ chuyện gì, (2) nói dữ liệu còn hay mất, (3) tối đa 2 nút. Lỗi không đổ lỗi cho người dùng. Không dùng màu đỏ cho trạng thái rỗng.</span></div></div>`));
reg('c8-trang-thai-hs','ipad','C8','C8 · Bộ trạng thái rỗng / lỗi chung (học sinh iPad)',()=>HS('Bộ trạng thái chung','Quy ước cho mọi màn C2/C3 · học sinh',`<div class="col grow" style="gap:14px;min-width:0"><div class="row" style="gap:14px;align-items:stretch">
 ${st('users','pri','Em chưa vào lớp nào','Nhập mã lớp của thầy cô để thấy bài. Em vẫn Tự học được.',btn('Nhập mã lớp','sm','users')+btn('Tự học','ghost sm','cube'))}
 ${st('book','pur','Chưa có bài nào','Thầy cô chưa giao bài mới. Em làm thêm bài Tự học nhé.',btn('Tự học','sm','cube'))}</div>
 <div class="row" style="gap:14px;align-items:stretch">
 ${st('flag','red','Chưa tải được bài','Em kiểm tra mạng rồi bấm Thử lại. Bài làm trước đó của em vẫn còn.',btn('Thử lại','sm','reset'))}
 ${st('lock','yel','Phiên đã hết hạn','Em nhập mã HS và mật khẩu để vào lại. Đáp số em vừa nhập vẫn được giữ.',btn('Đăng nhập lại','sm','arrow'))}</div>
 ${ban('red','flag','Đang không có mạng. Em vẫn xem được bài đã tải; chưa gửi được đáp số.')}</div>`));

/* ===== C3-01 · trạng thái trước khi tick "đã lưu" ===== */
reg('c3-01-ma-chua-tick','web','C3b','C3-01 · Mã khôi phục · chưa tick "đã lưu" (nút khóa)',()=>V['c3-01-ma-nguoi-dung'].fn().replace('<span class="cbx on">'+I('check')+'</span>','<span class="cbx"></span>').replace(/<span class="btn ">(?=(?:(?!<\/span>).)*Vào trang giáo viên)/,'<span class="btn" style="background:#B9C2D8;cursor:not-allowed">').replace('Vào trang giáo viên</span>','Vào trang giáo viên · tick "đã lưu" để mở</span>'));
