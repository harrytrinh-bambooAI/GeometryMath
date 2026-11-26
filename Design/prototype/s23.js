/* s23.js · Phiên bản: v0.2 (Dom: chữ HS lớn hơn; thêm nút "Xem lại cùng thầy cô" + "Em nghĩ đáp số này đúng" ở trạng thái hết lượt) · v0.1 · 2026-11-26 · khung + màn C2 (C2-02, 03, 04, 06, 07, 08). Dữ liệu mẫu, không AI thật. */
NAV.gv=[['home','Tổng quan'],['book','Kho đề'],['layers','Giáo trình'],['users','Lớp học'],['chart','Theo dõi'],['edit','Chấm bài'],['spark','Trợ lý']];
NAV.hs=[['home','Bài của em'],['cube','Tự học'],['users','Nhóm học'],['layers','Môn khác'],['clock','Lịch sử']];
const DEVS={web:[1440,900],ipad:[1180,820],phone:[390,844]};
const _screen=screen;screen=function(id,r,on,t,c,b,o){return _screen(id,r,on,t,c,b,Object.assign({},o||{},{noSeg:1}));};
const V={};            // id -> {dev,grp,label,fn}
const reg=(id,dev,grp,label,fn)=>{V[id]={dev,grp,label,fn};};
const I=(n,s)=>ic(n,s||'');
const PROB='Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, SA ⊥ (ABCD), SA = a√2. Tính góc giữa SC và mặt phẳng đáy.';
const STEPS=[
 'Vì SA ⊥ (ABCD) nên AC là hình chiếu của SC lên mặt đáy, suy ra góc (SC, (ABCD)) = góc <b>SCA</b>.',
 'ABCD là hình vuông cạnh a nên AC = a√2.',
 'Tam giác SAC vuông tại A: tan SCA = SA / AC = a√2 / a√2 = 1.',
 'Vậy góc giữa SC và mặt phẳng đáy bằng <b>45°</b>.'];
const trust=(m,h)=>`<span class="chip ${m?'grn':'yel'}">${I(m?'check':'flag')}${m?'Đã kiểm chứng':'Cần xem lại'}</span><span class="chip ${h?'pri':'pur'}">${I(h?'shield':'spark')}${h?'Giáo viên đã duyệt':'AI soạn'}</span>`;
const ban=(c,icn,h,p,btn)=>`<div class="ban ${c}">${I(icn)}<div class="grow"><div>${h}</div>${p?`<p>${p}</p>`:''}${btn||''}</div></div>`;
const pg=(arr)=>`<div class="pgs">${arr.map((a,i)=>`${i?'<span class="ln"></span>':''}<div class="p ${a[0]}">${I(a[1])}<div><div>${a[2]}</div><div class="tiny" style="font-weight:500">${a[3]}</div></div></div>`).join('')}</div>`;
const fig=(o)=>`<div class="bigfig">${pyramid(o||{})}</div>`;
const bare=(inner,cls,style)=>`<section class="screen nofr ${cls||''}" style="${style||''}">${inner}</section>`;
const btn=(t,c,icn)=>`<span class="btn ${c||''}">${icn?I(icn):''}${t}</span>`;

/* ================= C2-02 Thêm đề mới ================= */
const ps4=(a,b,c,d)=>pg([[a[0],a[1]||'file','1 · Đọc',a[2]||'Chép lại đề'],[b[0],b[1]||'cube','2 · Dựng',b[2]||'Dựng hình'],[c[0],c[1]||'spark','3 · Giải',c[2]||'Soạn lời giải'],[d[0],d[1]||'check','4 · Kiểm',d[2]||'Máy tính lại']]);
const addProb=(txt)=>`<div class="card pad col" style="gap:8px"><div class="pt">ĐỀ ĐÃ CHÉP LẠI · thầy cô sửa được</div><div style="font-size:14.5px;line-height:1.65">${txt||PROB}</div><div class="row small muted">${I('edit','width:14px;height:14px')}Bấm vào chữ để sửa. Phần tô vàng là chỗ AI chưa chắc.</div></div>`;
reg('c2-02-nhap','web','C2','C2-02 · Thêm đề · nhập',()=>screen('x','gv',1,'Thêm đề mới','Kho đề › Thêm đề',`
<div class="col grow" style="gap:14px;min-width:0"><div class="card pad col grow" style="gap:14px">
 <div class="tabs" style="border:none;padding:0;width:360px"><span class="on">${I('file')}Dán chữ</span><span>${I('camera')}Tải ảnh</span></div>
 <div class="field grow" style="align-items:flex-start;background:var(--soft);font-weight:500;font-size:15px;line-height:1.7;padding:16px">${PROB}<span class="cur1" style="height:18px;margin-left:2px"></span></div>
 <div class="dz" style="padding:16px;flex-direction:row;justify-content:center;gap:14px">${I('camera','width:26px;height:26px;color:var(--pri)')}<div style="text-align:left"><b>Hoặc kéo ảnh đề vào đây</b><div class="small muted">Ảnh chụp sách, ảnh màn hình. Tối đa 5 ảnh. Chụp thẳng, đủ sáng, chữ đọc được.</div></div>${btn('Chọn ảnh','ghost sm','plus')}</div>
 <div class="row" style="gap:8px;flex-wrap:wrap"><span class="small muted" style="font-weight:700">Loại hình</span><span class="chip on">Hình chóp</span><span class="chip">Lăng trụ</span><span class="chip">Khối tròn xoay</span><span class="chip">AI tự nhận</span><span class="grow"></span>${btn('Cho AI soạn nháp','','spark')}</div>
</div></div>
<div class="card pad col" style="width:380px;flex-shrink:0;gap:12px"><h2>AI sẽ làm gì</h2>
 ${[['file','Đọc','Chép lại đề thành chữ; ô chưa chắc tô vàng cho thầy cô sửa'],['cube','Dựng','Máy dựng hình từ điều kiện của đề'],['spark','Giải','AI soạn lời giải từng bước, có đáp số'],['check','Kiểm','Máy tính lại từng bước và đối chiếu đáp số']].map((s,i)=>`<div class="row" style="align-items:flex-start;gap:11px"><div class="num on">${i+1}</div><div><b>${s[1]}</b><div class="small muted">${s[2]}</div></div></div>`).join('')}
 <div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Bản nháp chưa vào Kho. Thầy cô duyệt rồi mới dùng được.</span></div></div>`,{concept:1}));

reg('c2-02-dang-dung','web','C2','C2-02 · Thêm đề · đang dựng',()=>screen('x','gv',1,'Thêm đề mới','Kho đề › Thêm đề › Đang soạn nháp',`
<div class="col grow" style="gap:14px;min-width:0">${ps4(['ok','check','Xong · 4 dòng'],['on','cube','Đang dựng…'],['wait','clock','Chờ'],['wait','clock','Chờ'])}
 <div class="row grow" style="gap:14px;align-items:stretch;min-height:0">${`<div class="col" style="width:470px;gap:14px">${addProb()}<div class="card pad col"><div class="pt">NGUỒN</div><div class="row small"><div class="icb pri" style="width:34px;height:34px">${I('file')}</div>Dán chữ · 112 ký tự · 11:42</div></div></div>`}
 <div class="card grow col" style="align-items:center;justify-content:center;gap:14px;background:var(--soft)"><div class="spin"></div><b style="font-size:16px">Đang dựng hình từ điều kiện của đề…</b><div class="small muted">Thường mất dưới 1 phút. Thầy cô có thể làm việc khác, bản nháp sẽ lưu ở "Của tôi".</div><div style="opacity:.35;width:260px">${pyramid({})}</div></div></div></div>`,{concept:1}));

reg('c2-02-anh-mo','web','C2','C2-02 · Ngoại lệ · ảnh mờ',()=>screen('x','gv',1,'Thêm đề mới','Kho đề › Thêm đề › Ảnh chưa đọc rõ',`
<div class="col grow" style="gap:14px;min-width:0">${ps4(['bad','flag','Đọc chưa rõ'],['wait','clock'],['wait','clock'],['wait','clock'])}
 ${ban('yel','flag','Ảnh hơi mờ: AI đọc chắc 2 trên 4 dòng.','Hai dòng cuối (độ dài SA và yêu cầu) chưa chắc. Thầy cô chụp lại, hoặc gõ tay phần tô vàng rồi bấm Xác nhận.')}
 <div class="row grow" style="gap:14px;align-items:stretch;min-height:0"><div class="card pad col" style="width:470px;gap:10px"><div class="pt">ẢNH GỐC</div><div class="blurimg">Cho hình chóp S.ABCD, đáy là hình vuông cạnh a, SA vuông góc với đáy, SA = ?… Tính góc giữa …</div><div class="row">${btn('Chụp lại','ghost sm','camera')}${btn('Tải ảnh khác','ghost sm','plus')}</div></div>
 <div class="card pad col grow" style="gap:10px"><div class="pt">ĐỀ ĐÃ CHÉP LẠI · sửa trực tiếp</div><div style="font-size:15px;line-height:2">Cho hình chóp S.ABCD, đáy ABCD là hình vuông cạnh a, <span class="okc">SA ⊥ (ABCD)</span>, <span class="unc">SA = a√2 ?</span>. <span class="unc">Tính góc giữa SC và mặt phẳng đáy ?</span></div>
 <div class="uncn">${I('flag')}Ô vàng là chỗ AI chưa chắc. Nhãn "Cần xem lại" giữ nguyên đến khi máy kiểm lại đạt (PO-38).</div><div class="grow"></div><div class="row"><span class="grow"></span>${btn('Xác nhận chữ và dựng tiếp','','check')}</div></div></div></div>`,{concept:1}));

reg('c2-02-engine-khong-dung','web','C2','C2-02 · Ngoại lệ · máy không dựng được',()=>screen('x','gv',1,'Thêm đề mới','Kho đề › Thêm đề › Chưa dựng được hình',`
<div class="col grow" style="gap:14px;min-width:0">${ps4(['ok','check','Xong'],['bad','flag','Chưa dựng được'],['wait','clock'],['wait','clock'])}
 ${ban('red','flag','Máy chưa dựng được hình từ đề này.','Đề viết "mặt phẳng (P) đi qua M và song song với SB" nhưng chưa nói M nằm ở đâu trên cạnh SD. Cần thêm điều kiện về vị trí điểm M.')}
 <div class="row grow" style="gap:14px;align-items:stretch;min-height:0"><div class="col" style="width:470px;gap:14px">${addProb('Cho hình chóp S.ABCD, đáy là hình bình hành. Gọi M thuộc SD. Mặt phẳng (P) đi qua M và song song với SB, cắt hình chóp theo thiết diện. Tính diện tích thiết diện.')}</div>
 <div class="card pad col grow" style="gap:10px"><h2>Chọn cách đi tiếp</h2>
 <div class="opt2 on"><span class="rd"></span><div class="grow"><b>Bổ sung điều kiện cho máy</b><div class="small muted">Ví dụ: SM = 2·MD</div></div></div><div class="field" style="margin-left:30px">${I('edit')}<span class="muted" style="font-weight:500">SM = 2·MD</span></div>
 <div class="opt2"><span class="rd"></span><div class="grow"><b>Dựng tay trong trình dựng hình</b><div class="small muted">Thầy cô đặt điểm, mặt phẳng; máy kiểm tra giúp</div></div>${I('cube')}</div>
 <div class="opt2"><span class="rd"></span><div class="grow"><b>Lưu bản nháp chữ, chưa có hình</b><div class="small muted">Mang nhãn "Cần xem lại", chưa giao được cho học sinh</div></div>${I('file')}</div>
 <div class="grow"></div><div class="row"><span class="grow"></span>${btn('Dựng lại','','cube')}</div></div></div></div>`,{concept:1}));

reg('c2-02-lech-dap-so','web','C2','C2-02 · Ngoại lệ · lệch đáp số',()=>screen('x','gv',1,'Thêm đề mới','Kho đề › Thêm đề › Đáp số chưa khớp',`
<div class="col grow" style="gap:14px;min-width:0">${ps4(['ok','check','Xong'],['ok','check','Xong'],['ok','check','Xong'],['bad','flag','Lệch đáp số'])}
 ${ban('red','flag','Đáp số trong đề (60°) không khớp với máy tính lại (45°).','Bản nháp bị chặn duyệt cho đến khi hai bên khớp. Thầy cô chọn một cách xử lý bên dưới.')}
 <div class="row grow" style="gap:14px;align-items:stretch;min-height:0"><div class="card pad col grow" style="gap:12px"><h2>Đối chiếu</h2>
 <table><tr><th></th><th>Đáp số</th><th>Nguồn</th></tr><tr><td><b>Đề ghi</b></td><td class="mono">60°</td><td class="small muted">Chữ trong đề thầy cô dán</td></tr><tr><td><b>AI giải</b></td><td class="mono">45°</td><td class="small muted">Bước 4 của lời giải</td></tr><tr style="background:var(--red-s)"><td><b>Máy tính lại</b></td><td class="mono" style="color:var(--red)">45°</td><td class="small muted">tan SCA = SA / AC = 1</td></tr></table>
 <div class="uncn">${I('flag')}Nhiều khả năng đáp số trong đề là của câu khác (góc giữa SC và (SAB) = 30°). Thầy cô xem lại đề gốc.</div></div>
 <div class="card pad col" style="width:420px;gap:10px"><h2>Cách xử lý</h2>${btn('Lấy đáp số của máy (45°)','','check')}${btn('Giữ 60° và sửa đề / lời giải','ghost','edit')}${btn('Mở soát lời giải','ghost','eye')}<div class="small muted">Không có nút "Duyệt" khi còn lệch. Giáo viên quyết định cuối, nhưng không đi tắt qua nhãn máy.</div></div></div></div>`,{concept:1}));

/* ================= C2-03 Soát và duyệt lời giải ================= */
function soat(state){ // 'unv' | 'bad' | 'ok' | 'sent'
 const bad=state==='bad',sent=state==='sent',appr=state==='ok'||sent,mach=state==='ok'||sent;
 const ver=sent?'v2':'v2';
 const stp=(i,t,k)=>{const b=bad&&i===2,v=mach||(!bad&&k==='ok');
  return `<div class="stp ${b?'bad':''} ${i===0&&!sent?'cur':''}"><div class="num ${b?'':v?'done':''}" style="${b?'background:var(--red);color:#fff':''}">${i+1}</div><div class="tx ${sent?'':'ed'}" ${sent?'':'contenteditable="true"'}>${t}</div><div class="col" style="gap:4px;align-items:flex-end;flex-shrink:0">${b?`<span class="chip red">${I('flag')}Lệch</span>`:v?`<span class="chip grn">${I('check')}Máy khớp</span>`:`<span class="chip yel">${I('clock')}Chưa kiểm</span>`}${sent?'':`<span class="tiny muted">${I('edit','width:12px;height:12px')} sửa</span>`}</div></div>`;};
 const s3=bad?'Tam giác SAC vuông tại A: tan SCA = SA / AC = a√2 / a = <b style="color:var(--red)">√2</b>.':STEPS[2];
 const s4=bad?'Vậy góc giữa SC và mặt phẳng đáy bằng <b style="color:var(--red)">54,7°</b>.':STEPS[3];
 const st=[STEPS[0],STEPS[1],s3,s4];
 const kinds=['ok','ok','ok','ok'];
 return screen('x','gv',1,'Soát và duyệt lời giải','Kho đề › Hình chóp › Góc giữa SC và đáy',`
 <div class="card col" style="width:400px;flex-shrink:0;padding:14px;gap:10px"><div class="row"><b>Hình</b><span class="grow"></span>${trust(mach,appr)}</div><div style="height:300px">${fig({ang:1})}</div><div class="small muted">Đề: ${PROB}</div></div>
 <div class="col grow" style="gap:10px;min-width:0"><div class="row"><h2 style="margin:0">Lời giải · ${ver}</h2><span class="chip">${I('clock')}Phiên bản ${ver} ${sent?'· đã giao':'· đang soạn'}</span><span class="grow"></span>${sent?btn('Tạo phiên bản mới (v3)','','edit'):btn('Kiểm tra lại','ghost sm','check')}</div>
 ${bad?ban('red','flag','Bước 3 lệch: a√2 ≠ AC = a√2 · máy tính lại tan SCA = 1.','Máy chặn duyệt. Sửa bước 3 và bấm "Kiểm tra lại".'):''}
 ${sent?ban('pri','lock','Đề đã giao cho 11A1 (hạn 18:00 thứ Sáu). Bản v2 đã khóa.','Sửa sẽ tạo phiên bản mới v3; bài đã giao vẫn dùng v2 cho đến khi thầy cô chọn "Áp dụng cho lớp".'):''}
 ${st.map((t,i)=>stp(i,t,kinds[i])).join('')}<div class="row small muted" style="gap:6px">${I('plus','width:14px;height:14px')}Thêm bước · kéo để đổi thứ tự</div></div>
 <div class="card pad col" style="width:340px;flex-shrink:0;gap:12px"><h2 style="margin:0">Hộp duyệt</h2>
 <div class="col" style="gap:6px"><div class="pt">NHÃN HAI TRỤC</div><div class="row" style="flex-wrap:wrap;gap:6px">${trust(mach,appr)}</div><div class="tiny muted">Trục máy do máy gắn. Trục người do thầy cô gắn. Hai trục độc lập.</div></div>
 <div class="col" style="gap:6px"><div class="pt">LỊCH SỬ PHIÊN BẢN</div>${[['v2',sent?'Đã giao 11A1 · 14:30':'Đang soạn · bạn sửa bước 3','Hôm nay 11:58'],['v1','AI soạn · máy chưa kiểm','Hôm nay 11:44']].map((h,i)=>`<div class="hint ${i===0?'act':''}" style="padding:9px 11px;align-items:center"><div class="num ${i===0?'on':''}" style="width:30px;border-radius:9px">${h[0]}</div><div class="grow"><div style="font-weight:700;font-size:12.8px">${h[1]}</div><div class="tiny muted">${h[2]}</div></div></div>`).join('')}</div>
 <div class="grow"></div>
 ${sent?`<div class="ban grn">${I('shield')}<div>Giáo viên đã duyệt v2 · máy đã kiểm chứng</div></div>`:appr?'':bad?`<div class="ban red">${I('lock')}<div>Chặn duyệt: còn 1 bước lệch.</div></div>`:`<div class="ban yel">${I('flag')}<div>Duyệt lúc này sẽ giữ nhãn "Cần xem lại" cho đến khi máy kiểm đạt.</div></div>`}
 ${sent?'':btn('Duyệt lời giải này',bad?'ghost':'grn',bad?'lock':'shield')}
 ${sent?'':`<div class="tiny muted" style="text-align:center">Duyệt xong đề vào "Giáo viên đã duyệt" và giao được cho lớp.</div>`}</div>`,{concept:1});}
reg('c2-03-chua-kiem','web','C2','C2-03 · Soát · chưa kiểm chứng',()=>soat('unv'));
reg('c2-03-lech-chan-duyet','web','C2','C2-03 · Soát · lệch đáp số, chặn duyệt',()=>soat('bad'));
reg('c2-03-da-giao','web','C2','C2-03 · Soát · đã giao, chỉ tạo phiên bản mới',()=>soat('sent'));

/* ================= C2-04 Trình chiếu ================= */
function pres(n,hide){
 const vis=hide?0:n;
 const stepsH=STEPS.map((t,i)=>i<vis?`<div class="stpb ${i===vis-1?'cur':''}"><span class="mono" style="color:#7C9BFF">${i+1}.</span> ${t}</div>`:`<div class="stpb hid">${I('lock','width:18px;height:18px')}Bước ${i+1}${hide?' · lời giải đang che':' · bấm → để hiện'}</div>`).join('');
 return bare(`<div class="row" style="padding:14px 22px;gap:14px"><div class="tb"><span class="on">${I('eye')}Trình chiếu</span><span>${I('line')}Bút</span><span>${I('reset')}Xoay hình</span><span class="${hide?'on':''}" style="${hide?'background:var(--org)':''}">${I('lock')}${hide?'Đang che lời giải':'Che lời giải'}</span></div><span class="chip" style="background:rgba(255,255,255,.1);color:#C9D2E6;border:none">${I('shield')}Đã tải sẵn · mất mạng vẫn chiếu</span><span class="chip" style="background:rgba(255,255,255,.1);color:#C9D2E6;border:none">${I('lock')}Máy học sinh vẫn khóa lời giải</span><span class="grow"></span><div class="tb"><span>${I('x')}Thoát (Esc)</span></div></div>
 <div class="row" style="padding:0 22px;gap:18px;flex:1;align-items:stretch;min-height:0"><div class="fg grow">${pyramid({ang:hide?0:(n>=3?1:0)})}</div>
 <div class="col" style="width:500px;gap:12px;overflow:hidden"><div class="stpb" style="font-size:17px;background:rgba(255,255,255,.04)"><div class="tiny" style="color:#8F9BB5;font-weight:800;margin-bottom:4px">ĐỀ</div>${PROB}</div>${stepsH}</div></div>
 <div class="row" style="padding:16px 22px;justify-content:center;gap:14px"><span class="btn ghost" style="background:rgba(255,255,255,.08);color:#fff;border:none;padding:14px 22px">${I('arrow','transform:scaleX(-1)')}Bước trước</span><span class="mono" style="font-size:16px">${hide?'Lời giải che':`Bước ${vis} / 4`}</span><span class="btn" style="padding:14px 26px;font-size:15px">${hide?'Hiện lời giải':'Hiện bước kế'}${I('arrow')}</span></div>`,'pres');}
reg('c2-04-trinh-chieu','web','C2','C2-04 · Trình chiếu · hiện từng bước',()=>pres(2,false));
reg('c2-04-che-loi-giai','web','C2','C2-04 · Trình chiếu · che lời giải',()=>pres(0,true));

/* ================= C2-06 / C2-07 Bài luyện (HS) ================= */
const KEYS=[['7','8','9','(',')','⌫'],['4','5','6','+','−','√'],['1','2','3','×','÷','π'],['0',',','°','/','^','a']];
const kbd=()=>`<div class="kbd2">${KEYS.map(r=>r.map(k=>`<b data-act="key" data-arg="${k}" class="${k==='⌫'?'fn':''}">${k}</b>`).join('')).join('')}<b class="fn w2" data-act="key" data-arg="x">x</b><b class="fn w2">Xóa hết</b><b class="ok w2">${I('check','width:18px;height:18px')}Kiểm tra</b></div>`;
const lp=(v,s)=>`<div class="row" style="gap:8px;flex-wrap:wrap"><span class="chip pri">${I('cube')}Câu ${v}/5</span><span class="chip ${s==='out'?'red':'yel'}">${I('flame')}Còn ${s==='out'?0:s==='bad'?1:2}/3 lượt</span><span class="grow"></span><div class="steps" style="width:150px">${[0,1,2,3,4].map(i=>`<i class="${i<v-1?'g':i===v-1?'on':''}"></i>`).join('')}</div></div>`;
function luyen(dev,s){ // s: 'key' | 'bad' | 'out'
 const ph=dev==='phone';
 const mi=s==='bad'?`<div class="mathin bad"><span class="v">60°</span></div>`:s==='out'?`<div class="mathin bad" style="opacity:.6"><span class="v">60°</span></div>`:`<div class="mathin"><span class="v">45°</span><span class="cur1"></span><span class="grow"></span><span class="tiny muted" style="font-family:inherit;font-weight:600">đơn vị: độ</span></div>`;
 const fb=s==='bad'?ban('yel','bulb','Chưa đúng. Còn 1 lượt.',`Gợi ý: góc giữa SC và mặt đáy là góc giữa SC và <b>hình chiếu</b> của nó lên đáy. Hình chiếu của SC là đoạn nào?`):s==='out'?ban('red','lock','Hết lượt cho câu này.','Lời giải còn khóa đến 18:00 thứ Sáu (thầy cô đặt). Em vẫn xem được gợi ý.')+`<div class="row" style="gap:8px">${btn('Xem lại cùng thầy cô','soft','users')}${btn('Em nghĩ đáp số này đúng','ghost','flag')}</div>`:`<div class="small muted">Nhập đáp số rồi bấm Kiểm tra. Chấp nhận sai số ±0,5°.</div>`;
 const body=ph?`<div class="col grow" style="gap:10px;min-width:0">${lp(2,s)}<div class="card pad" style="font-size:15px;line-height:1.5">${PROB}</div><div style="height:140px" class="card">${fig()}</div>${mi}${fb}</div>${s==='out'?'':kbd()}`
 :`<div class="canvas c3d grow" style="min-width:0"><div class="prob"><div class="icb pri">${I('file')}</div><div class="grow">${PROB}</div></div><div class="fig">${pyramid({})}</div></div>
 <div class="card side pad col" style="gap:12px;width:400px">${lp(2,s)}<div class="pt">ĐÁP SỐ CỦA EM</div>${mi}${fb}<div class="grow"></div>${s==='out'?`<div class="row">${btn('Xem gợi ý','ghost','bulb')}${btn('Câu tiếp theo','','arrow')}</div>`:kbd()}</div>`;
 return screen('x','hs',0,s==='out'?'Hết lượt':'Bài luyện: Góc trong hình chóp','Bài của em › Bộ luyện 11A1 › Câu 2',body,{noSeg:1,concept:1});}
reg('c2-06-luyen-ipad','ipad','C2','C2-06 · Bài luyện · iPad + bàn phím toán',()=>luyen('ipad','key'));
reg('c2-06-luyen-iphone','phone','C2','C2-06 · Bài luyện · iPhone + bàn phím toán',()=>luyen('phone','key'));
reg('c2-06-sai-goi-y','ipad','C2','C2-06 · Bài luyện · sai, có gợi ý',()=>luyen('ipad','bad'));
reg('c2-06-het-luot','phone','C2','C2-06 · Bài luyện · hết lượt (iPhone)',()=>luyen('phone','out'));

reg('c2-07-ket-qua','ipad','C2','C2-07 · Kết quả bài luyện',()=>screen('x','hs',0,'Kết quả: Bộ luyện góc','Bài của em › Bộ luyện 11A1 › Kết quả',`
<div class="col grow" style="gap:14px;min-width:0"><div class="row" style="gap:14px"><div class="card stat">${`<div class="icb grn">${I('check')}</div>`}<div><b>3 câu đúng</b><div class="small muted">ngay lần thử đầu: 2</div></div></div><div class="card stat"><div class="icb yel">${I('eye')}</div><div><b>2 câu cần xem lại</b><div class="small muted">em xem gợi ý rồi làm lại</div></div></div><div class="card stat"><div class="icb pri">${I('lock')}</div><div><b>Lời giải mở 18:00 thứ Sáu</b><div class="small muted">còn 1 ngày 3 giờ</div></div></div></div>
 <div class="card pad col grow" style="gap:8px"><h2>Từng câu</h2>${[[1,'45°','g','Đúng ngay lần 1',0],[2,'60° → 45°','g','Đúng ở lần 2',0],[3,'√2','y','Cần xem lại · hết lượt',1],[4,'a√3/3','g','Đúng ngay lần 1',0],[5,'—','y','Cần xem lại · chưa làm',1]].map(r=>`<div class="hint" style="align-items:center"><div class="num ${r[2]==='g'?'done':''}" style="${r[2]==='y'?'background:var(--yel);color:#fff':''}">${r[0]}</div><div class="grow"><b>Câu ${r[0]} · đáp số của em: <span class="mono">${r[1]}</span></b><div class="small muted">${r[3]}</div></div><span class="chip ${r[2]==='g'?'grn':'yel'}">${r[2]==='g'?'Đúng':'Cần xem lại'}</span><span class="chip">${I('lock')}Lời giải khóa</span>${r[4]?btn('Làm lại','soft sm','reset'):''}</div>`).join('')}
 <div class="flow">${I('shield','width:15px;height:15px;color:var(--pri)')}<span>Không có điểm tổng và không xếp hạng (TẠM theo Q6). Giáo viên thấy chi tiết từng câu.</span></div></div></div>`,{noSeg:1,concept:1}));

/* ================= C2-08 Soạn bài luyện (GV) ================= */
reg('c2-08-soan-bai-luyen','web','C2','C2-08 · Soạn bài luyện',()=>screen('x','gv',1,'Soạn bài luyện','Kho đề › Bài luyện mới',`
<div class="card pad col grow" style="gap:8px;min-width:0"><div class="row"><h2 style="margin:0">Chọn đề (5)</h2><span class="grow"></span><div class="field" style="padding:6px 10px">${I('search')}<span class="muted" style="font-weight:500">Tìm trong Của tôi, Kho chung…</span></div></div>
 ${[['chop','Góc giữa SC và mặt đáy','45°',1,1],['chop','Khoảng cách từ A đến (SBC)','a√2/2',1,1],['hop','Thể tích khối hộp chữ nhật','24 cm³',1,1],['lang','Góc giữa hai mặt phẳng','60°',1,1],['non','Thiết diện qua trục hình nón','—',0,0]].map(r=>`<div class="hint" style="align-items:center;${r[4]?'':'background:var(--soft);border-style:dashed'}"><span class="cbx ${r[3]?'on':''}">${r[3]?I('check'):''}</span><div class="thumb" style="width:64px;height:54px">${sh[r[0]]('#3B5BDB')}</div><div class="grow"><b>${r[1]}</b><div class="small muted">Đáp số: ${r[2]}</div></div>${r[4]?`<span class="chip grn">${I('check')}Máy chấm được</span>`:`<span class="chip yel">${I('flag')}Chưa có đáp số máy chấm được</span>`}</div>`).join('')}
 <div class="uncn">${I('flag')}Đề "Thiết diện qua trục hình nón" chưa có đáp số số học nên không chọn được cho bài luyện. Thêm đáp số ở Soát lời giải.</div></div>
<div class="card pad col" style="width:340px;flex-shrink:0;gap:12px"><h2 style="margin:0">Cài đặt</h2>
 <div><div class="lbl">Kiểu đáp số</div><div class="grp"><span class="chip on">Một số</span><span class="chip">Biểu thức</span><span class="chip">Chọn đáp án</span></div></div>
 <div><div class="lbl">Sai số cho phép</div><div class="row"><div class="field grow">±&nbsp;0,5</div><div class="seg"><span class="on">độ</span><span>%</span></div></div></div>
 <div><div class="lbl">Số lượt thử mỗi câu</div><div class="row">${['1','2','3','∞'].map((x,i)=>`<span class="chip ${i===2?'on':''}" style="padding:7px 16px;font-size:13px">${x}</span>`).join('')}</div></div>
 <div><div class="lbl">Lớp và giờ mở</div><div class="field">${I('users')}11A1<span class="grow"></span>${I('cal')}16/12 · 18:00</div></div>
 <div class="row"><div class="toggle"></div><div class="small"><b>Lời giải mở sau giờ nộp</b><div class="tiny muted">Khóa ở máy chủ</div></div></div></div>
<div class="card pad col" style="width:300px;flex-shrink:0;gap:10px;background:var(--soft)"><div class="row"><b>Xem trước như học sinh</b></div><div style="border:6px solid #141B2D;border-radius:26px;background:#fff;padding:12px;display:flex;flex-direction:column;gap:8px;flex:1;min-height:0"><span class="chip pri" style="align-self:flex-start">Câu 1/5</span><div class="tiny" style="line-height:1.45">${PROB}</div><div style="height:110px">${fig()}</div><div class="mathin" style="font-size:16px;padding:8px 10px">45°</div><div class="kbd2" style="grid-template-columns:repeat(6,1fr);padding:5px;gap:4px;transform-origin:top;font-size:10px"><b style="min-height:26px;padding:4px 0;font-size:11px">7</b><b style="min-height:26px;padding:4px 0;font-size:11px">8</b><b style="min-height:26px;padding:4px 0;font-size:11px">9</b><b style="min-height:26px;padding:4px 0;font-size:11px">(</b><b style="min-height:26px;padding:4px 0;font-size:11px">)</b><b style="min-height:26px;padding:4px 0;font-size:11px">⌫</b></div></div>${btn('Giao bài luyện','','send')}</div>`,{concept:1}));

/* ================= khung hiển thị ================= */
const MORE=[];  // s23b.js đẩy thêm
function show(){
 const q=new URLSearchParams(location.search),id=location.hash.slice(1)||Object.keys(V)[0],cap=q.get('cap');
 const v=V[id]||V[Object.keys(V)[0]];
 const app=document.getElementById('app'),nav=document.getElementById('s23nav');
 const grps={};Object.keys(V).forEach(k=>{(grps[V[k].grp]=grps[V[k].grp]||[]).push(k);});
 nav.style.display=cap?'none':'flex';
 nav.innerHTML='<b>Bản mẫu chặng 2–3 (S23)</b><a href="index.html" style="background:rgba(255,255,255,.18)">← Bản mẫu chính</a>'+Object.keys(grps).map(g=>`<span class="g">${g}</span>`+grps[g].map(k=>`<a href="#${k}" class="${k===id?'on':''}">${V[k].label}</a>`).join('')).join('');
 app.className='dev-'+v.dev;
 app.innerHTML=v.fn();
 const s=app.querySelector('.screen'),[W,H]=DEVS[v.dev];
 s.style.cssText=`width:${W}px;height:${H}px;margin:${cap?0:'18px auto'};border-radius:${cap?0:v.dev==='ipad'?26:v.dev==='phone'?40:0}px;${cap?'':v.dev==='web'?'':'box-shadow:0 0 0 10px #000,0 0 0 12px #3a3f4d;'}position:relative;overflow:hidden`+(s.classList.contains('nofr')?'':'');
 if(v.dev!=='web'&&s.classList.contains('nofr')){/* màn không có thanh bên */}
 document.title=v.label+' · S23';
 document.body.dataset.ready=id;
}
document.addEventListener('click',e=>{const t=e.target.closest('[data-act="key"]');if(!t)return;const el=document.querySelector('.mathin .v');if(!el)return;const k=t.dataset.arg;el.textContent=k==='⌫'?el.textContent.slice(0,-1):el.textContent+k;});
