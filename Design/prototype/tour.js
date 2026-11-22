/* ===== Trình chiếu có chú thích (guided tour) =====
   Dùng lại state và hành động thật của bản mẫu: st, dev, go(), render(), ACT, init().
   Mỗi bước là một chuỗi thao tác tất định [phần tử được "bấm" | null, hàm thao tác, thời gian chờ ms].
   Đi lùi = đặt lại dữ liệu rồi chạy lại các bước trước ở chế độ đồng bộ (không hiệu ứng). */
(function(){
'use strict';
const AUTO_MS=5000,PULSE_MS=650;
const ROLE={gv:['Giáo viên','#3B5BDB'],hs:['Học sinh','#F0522B'],ph:['Phụ huynh','#0F8A5F'],tr:['Nhà trường','#7C4DFF'],ql:['Cơ quan quản lý','#0EA5E9']};
const DEVN={web:'Web',ipad:'iPad',phone:'iPhone'};
const $=s=>document.querySelector('#app '+s);
const $$=s=>[...document.querySelectorAll('#app '+s)];
const byText=(s,t)=>$$(s).find(e=>e.textContent.includes(t));
const cur=()=>st.asg.find(a=>a.p==='p1'&&!a.self);
const slide=v=>{const r=document.getElementById('rng');if(!r)return;r.value=v;r.dispatchEvent(new Event('input',{bubbles:true}));};

/* ---------------- KỊCH BẢN ---------------- */
const MVP={key:'mvp',name:'Kịch bản chính: từ giao bài tới phụ huynh',
 base:()=>{const s=init();s.asg=s.asg.filter(a=>a.id!=='a1');s.cur='a3';s.sel.rel='ngay bây giờ';return s;},
 steps:[
 {role:'gv',dev:'web',title:'Giáo viên mở kho đề',act:'Giáo viên vào mục Kho đề trên máy tính',
  text:'Mỗi đề có sẵn hình 3D và lời giải từng bước do AI soạn; nhãn “Đã kiểm chứng” cho biết đáp số đã được đối chiếu bằng tính toán. Thầy cô chọn đề có sẵn thay vì tự dựng hình từ đầu.',
  seq:[[null,()=>go('gv-lib','gv')]],target:'[data-act="prob"][data-arg="p1"]'},
 {role:'gv',dev:'web',title:'Xem đề và lời giải đã kiểm chứng',act:'Bấm vào đề hình chóp S.ABCD',
  text:'Hệ thống mở hình dựng sẵn cùng lời giải ba bước và đáp số 45°. Giáo viên đọc trước đúng những gì học sinh sẽ được xem, nên yên tâm trước khi giao.',
  seq:[['[data-act="prob"][data-arg="p1"]',()=>ACT.prob('p1')]],target:'.body > .col > .card.pad'},
 {role:'gv',dev:'web',title:'Chọn lớp, đặt giờ mở lời giải',act:'Chọn lớp 11A1, chọn “19:00 hôm nay”',
  text:'Dòng ghi chú đổi ngay: trước 19:00 học sinh chỉ thấy đề, hình và gợi ý. Thời điểm mở lời giải do giáo viên quyết định, đây là quy tắc cốt lõi của sản phẩm.',
  seq:[['[data-act="selcls"][data-arg="11A1"]',()=>ACT.selcls('11A1')],['[data-act="selrel"][data-arg="19:00 hôm nay"]',()=>ACT.selrel('19:00 hôm nay')]],target:'.body > .card.pad.col'},
 {role:'gv',dev:'web',title:'Giao bài cho cả lớp',act:'Bấm “Giao bài cho 11A1”',
  text:'Sau màn chờ ngắn, hệ thống báo đã giao và chuyển thẳng sang Theo dõi lớp. Dòng của An còn ghi “Chưa mở”, “Chưa bắt đầu” vì em chưa vào bài.',
  seq:[['[data-act="assign"]',()=>ACT.assign(),900]],target:'table tbody tr'},
 {role:'hs',dev:'ipad',title:'Học sinh thấy bài mới ngay',act:'Chuyển sang iPad của học sinh An',
  text:'Bài vừa giao nằm đầu danh sách với nhãn “Mới giao” và dòng “Lời giải mở 19:00 hôm nay”. Em biết có việc cần làm và khi nào mới được xem lời giải, không cần mã bài hay đường dẫn riêng.',
  seq:[[null,()=>go('hs-home','hs')]],target:'[data-act="open"]'},
 {role:'hs',dev:'ipad',title:'Mở bài, xoay hình để nhìn rõ',act:'Bấm vào bài, rồi kéo ngón tay trên hình để xoay',
  text:'Hình chóp xoay theo tay và cạnh khuất tự chuyển sang nét đứt ở mỗi góc nhìn. Em tự tìm góc nhìn dễ hình dung nhất thay vì nhìn một hình in cố định.',
  seq:[['[data-act="open"]',()=>ACT.open(cur().id),350],[null,()=>FIG.setView({yaw:78,pitch:34}),800],[null,()=>FIG.setView({yaw:-24,pitch:10}),800],[null,()=>FIG.setView({yaw:42,pitch:24}),650]],target:'.canvas',place:'right',align:'end'},
 {role:'hs',dev:'ipad',title:'Vẽ thêm lên hình, mở rộng mặt đáy',act:'Chọn Điểm rồi chạm cạnh SD; chọn Đoạn rồi nối A với M; chọn Mở rộng rồi chạm mặt đáy',
  text:'Hình có thêm trung điểm M của SD, đoạn AM, và mặt đáy (ABCD) được mở rộng ra ngoài hình vuông. Em thử ý tưởng ngay trên hình; mỗi thao tác được tính là hoạt động để giáo viên biết em đang làm.',
  seq:[['[data-act="tool"][data-arg="point"]',()=>ACT.tool('point')],[null,()=>FIG.addPoint({mid:['S','D']}),700],['[data-act="tool"][data-arg="segment"]',()=>ACT.tool('segment')],[null,()=>FIG.addSegment('A','M'),700],
   ['[data-act="tool"][data-arg="extend"]',()=>ACT.tool('extend')],[null,()=>FIG.growFace(['A','B','C','D']),700]],target:'.canvas',place:'right',align:'end'},
 {role:'hs',dev:'ipad',title:'Đo trên hình: đại lượng đề hỏi bị ẩn',act:'Chọn Đo dài rồi chạm SA; chọn Đo góc rồi chạm SD và AD',
  text:'SA hiện ngay “a”, còn góc SDA chỉ ghi “ẩn tới khi mở lời giải” vì đó chính là điều đề hỏi. Công cụ đo giúp em kiểm tra dữ kiện trung gian, không thành đường tắt để lấy đáp số.',
  seq:[['[data-act="tool"][data-arg="length"]',()=>ACT.tool('length')],[null,()=>FIG.measureLength('S','A'),800],['[data-act="tool"][data-arg="angle"]',()=>ACT.tool('angle')],[null,()=>FIG.measureAngle(['S','D'],['A','D']),800]],target:'.canvas',place:'right',align:'end'},
 {role:'hs',dev:'ipad',title:'Xin gợi ý 1: chỉ hướng nghĩ',act:'Bấm “Xem gợi ý 1”',
  text:'Hệ thống nhắc lại khái niệm cần dùng, chưa đưa phép tính nào; các thuật ngữ quan trọng được gạch chân chấm để bấm vào. Mỗi lần xin gợi ý đều được ghi lại để giáo viên biết em cần trợ giúp tới mức nào.',
  seq:[['[data-act="hint"]',()=>ACT.hint()]],target:'.side .hint.act'},
 {role:'hs',dev:'ipad',title:'Bấm từ khóa, đọc lại bài giảng',act:'Bấm từ khóa “Góc giữa đường thẳng và mặt phẳng” trong gợi ý',
  text:'Bài giảng mở ra với nội dung ngắn, hình minh họa và dòng nguồn: bài soạn của giáo viên, mục 1.3 trong giáo án của lớp, nhãn “Giáo viên đã duyệt”. Em đọc lại đúng điều thầy cô đã dạy, kèm các từ khóa liên quan để bấm tiếp.',
  seq:[['.side .kw[data-arg="gdm"]',()=>ACT.lec('gdm')]],target:'.modal'},
 {role:'hs',dev:'ipad',title:'Gợi ý 2: bước kế tiếp, vẫn chưa có đáp số',act:'Đóng bài giảng, bấm “Xem gợi ý 2”',
  text:'Gợi ý cụ thể hơn một bậc: nên tìm hình chiếu của điểm nào. Tiến độ lên 2/3, còn thẻ lời giải bên dưới vẫn khóa tới giờ giáo viên đặt.',
  seq:[['.modal [data-act="modal"]',()=>ACT.modal('')],['[data-act="hint"]',()=>ACT.hint()]],target:'.side > .pad'},
 {role:'hs',dev:'ipad',title:'Hỏi trợ lý: câu trả lời có nguồn',act:'Mở tab Hỏi đáp, hỏi “Vì sao A là hình chiếu của S?”',
  text:'Trợ lý giải thích dựa trên bài giảng “Hình chiếu vuông góc”, ghi dòng “Nguồn” bấm được ngay dưới câu trả lời và gạch chân các từ khóa. Câu trả lời lấy từ giáo trình của lớp nên khớp với cách thầy cô dạy.',
  seq:[['[data-act="tab"][data-arg="chat"]',()=>ACT.tab('chat')],['[data-act="ask"][data-arg="Vì sao A là hình chiếu của S?"]',()=>ACT.ask('Vì sao A là hình chiếu của S?'),850]],target:()=>$$('.side .bub').slice(-2)},
 {role:'hs',dev:'ipad',title:'Hỏi thẳng đáp số, bị từ chối khéo',act:'Hỏi tiếp “Đáp số là bao nhiêu?”',
  text:'Trợ lý nói rõ chưa nêu đáp số được vì lời giải chưa mở, rồi dẫn em về bài giảng liên quan và gợi ý đang có. AI ở đây giúp học sinh nghĩ, không làm hộ.',
  seq:[['[data-act="ask"][data-arg="Đáp số là bao nhiêu?"]',()=>ACT.ask('Đáp số là bao nhiêu?'),850]],target:()=>$$('.side .bub').slice(-2)},
 {role:'hs',dev:'ipad',title:'Tab Lời giải đang khóa',act:'Bấm tab Lời giải',
  text:'Em thấy rõ lời giải có ba bước nhưng nội dung bị che, kèm giờ mở 19:00 hôm nay. Khóa hiện công khai nên em hiểu đây là quy định của lớp, không phải lỗi của ứng dụng.',
  seq:[['[data-act="tab"][data-arg="sol"]',()=>ACT.tab('sol')]],target:'.side > .pad > .col'},
 {role:'gv',dev:'web',title:'Giáo viên theo dõi: dòng của An đã cập nhật',act:'Trở lại máy tính của giáo viên, mục Theo dõi',
  text:'Dòng của An giờ ghi: đã mở bài, dùng hai gợi ý, hỏi hai câu, đang chờ lời giải. Cột bên phải hiện từng câu em hỏi kèm nguồn trong giáo trình mà trợ lý đã dùng, nên giáo viên thấy lớp vướng ở đâu mà chưa cần chấm bài nào.',
  seq:[[null,()=>{st.role='gv';ACT.track(cur().id);}]],target:'[data-part="asked"]',place:'left'},
 {role:'gv',dev:'web',title:'Đổi giờ mở lời giải',act:'Bấm “Đổi giờ”, chọn “20:00 hôm nay”',
  text:'Thẻ khóa đổi sang “Mở 20:00 hôm nay” và bài trên máy học sinh cập nhật theo. Giáo viên điều chỉnh được theo nhịp thực tế của lớp.',
  seq:[['[data-act="modal"][data-arg="time"]',()=>ACT.modal('time')],['[data-act="settime"]',()=>ACT.settime('20:00 hôm nay')]],target:'.body > .col:last-child > .card',place:'bottom'},
 {role:'gv',dev:'web',title:'Hoặc mở ngay cho cả lớp',act:'Bấm “Mở ngay”',
  text:'Thấy nhiều em đã dùng hết gợi ý và đang chờ, giáo viên mở sớm. Lời giải mở cho cả lớp cùng lúc; tình trạng các em đang chờ đổi theo.',
  seq:[['[data-act="release"]',()=>ACT.release(cur().id)]],target:'.body > .col:last-child > .card',place:'bottom'},
 {role:'hs',dev:'ipad',title:'Học sinh xem lời giải: bước 1 hiện trên hình',act:'Trên iPad, An mở lại bài',
  text:'Tab Lời giải đã mở và bước 1 được vẽ ngay lên hình em đang dùng: tô SA, hình chiếu AD và đánh dấu góc vuông tại A. Bước đang xem có mục “Vì sao nghĩ ra bước này?” nên em học cách nghĩ, không chỉ chép đáp số.',
  seq:[[null,()=>go('hs-home','hs')],['[data-act="open"]',()=>{ACT.open(cur().id);ACT.tab('sol');},1100]],target:'.canvas',place:'right',align:'end'},
 {role:'hs',dev:'ipad',title:'Hình được vẽ thêm dần theo từng bước',act:'Bấm bước 2, rồi bước 3',
  text:'Bước 2 tô tam giác SAD và đánh dấu góc SDA, bước 3 ghi 45° vào góc đó; số đo góc lúc trước bị ẩn nay cũng hiện 45°. Mỗi bước của lời giải nằm ở đâu trong không gian, em thấy ngay trên hình.',
  seq:[['[data-act="step"][data-arg="1"]',()=>ACT.step(1),1300],['[data-act="step"][data-arg="2"]',()=>ACT.step(2),1300]],target:'.canvas',place:'right',align:'end'},
 {role:'ph',dev:'phone',title:'Phụ huynh xem tiến độ của con',act:'Chuyển sang iPhone của phụ huynh',
  text:'Số bài hoàn thành, số lần xem gợi ý, số câu đã hỏi khớp đúng với việc An vừa làm. Phụ huynh thấy con học thế nào, còn điểm chỉ hiện sau khi giáo viên duyệt.',
  seq:[[null,()=>go('ph-home','ph')]],target:()=>[$('.body > .col > .row'),$('.body > .col > .card.pad.col')]},
 {role:'ph',dev:'phone',title:'Phụ huynh nhắn giáo viên',act:'Gửi “Ở nhà tôi nên giúp con luyện phần nào ạ?”',
  text:'Tin nhắn đi trong cùng ứng dụng và giáo viên trả lời bằng việc làm cụ thể: xem lại hai bài khoảng cách, mỗi bài khoảng 10 phút. Nhà trường và gia đình nói chuyện trên cùng một dữ liệu.',
  seq:[['[data-act="psend"]',()=>ACT.psend(),1100]],target:'.body > .card.pad.col'},
 {role:'gv',dev:'phone',title:'Giáo viên kiểm tra nhanh trên điện thoại',act:'Giáo viên mở Tổng quan trên iPhone',
  text:'Cùng dữ liệu như bản web, bố cục xếp lại một cột: bài đang giao đã ghi “Lời giải đã mở” và tin nhắn của phụ huynh nằm ngay ở Tổng quan. Một vòng giao bài, học, theo dõi, báo phụ huynh đã khép kín.',
  seq:[[null,()=>go('gv-home','gv')]],target:'.body > .card.pad.col'}
 ]};

const VISION={key:'vision',name:'Tầm nhìn: 9 chặng',
 base:()=>init(),
 steps:[
 {role:'gv',dev:'web',stage:2,title:'Giáo viên tự thêm đề',act:'Giáo viên vào Kho đề, bấm Thêm đề',
  text:'Thầy cô gõ đề hoặc nhập từ ảnh, PDF; AI đề xuất lời giải và đối chiếu đáp số, giáo viên sửa rồi duyệt mới đưa vào kho. Giá trị: kho đề lớn dần theo đúng giáo trình của từng thầy cô.',
  seq:[[null,()=>go('gv-edit','gv')]],target:'.body > .card',place:'right'},
 {role:'hs',dev:'ipad',stage:2,title:'Trọn môn Hình học: thêm hình phẳng',act:'Học sinh mở mục Môn khác',
  text:'Cùng khung làm bài, nay là bài tam giác vuông: hình đã kiểm chứng, hai mức gợi ý, lời giải mở theo giờ giáo viên đặt. Giá trị: một công cụ cho cả hình phẳng lẫn hình không gian.',
  seq:[[null,()=>go('hs-subject','hs')],[null,()=>ACT.subj('phang')]],target:'.canvas',place:'right',align:'end'},
 {role:'gv',dev:'web',stage:3,title:'Lớp học và tài khoản chính thức',act:'Giáo viên mở mục Lớp học',
  text:'Mỗi lớp có mã riêng để học sinh tham gia; chặng này thêm tài khoản chính thức và tạo nhiều lớp. Giá trị: giáo viên quản lý cả năm học thay vì từng bài lẻ.',
  seq:[[null,()=>go('gv-class','gv')]],target:'.body > .col'},
 {role:'hs',dev:'ipad',stage:4,title:'Môn học thứ hai: Hàm số và đồ thị',act:'Chọn “Hàm số và đồ thị”, kéo hệ số a về −1,5',
  text:'Parabol lật bề lõm xuống ngay và lời giải thích đổi theo. Giá trị: khung gợi ý, khóa lời giải và theo dõi lớp được dùng lại nguyên vẹn cho môn mới.',
  seq:[[null,()=>go('hs-subject','hs')],['.views [data-arg="hamso"]',()=>ACT.subj('hamso')],['#rng',()=>slide(-1.5)]],target:'.canvas',place:'right',align:'end'},
 {role:'gv',dev:'web',stage:5,title:'Trợ lý giáo viên soạn nháp',act:'Gõ yêu cầu 3 bài khoảng cách, bấm “Soạn nháp”',
  text:'Trợ lý soạn bộ đề dễ, vừa, khó từ kho đề của chính thầy cô; bài chưa kiểm chứng được thì gắn nhãn cần xem lại. Giá trị: tiết kiệm giờ soạn bài, nhưng chỉ thứ thầy cô duyệt mới vào kho.',
  seq:[[null,()=>go('gv-copilot','gv')],['[data-act="draft"]',()=>ACT.draft(),1300]],target:'.body > .card:last-child',place:'left'},
 {role:'gv',dev:'web',stage:5,title:'AI đọc bài viết tay, đề xuất điểm',act:'Giáo viên mở mục Chấm bài',
  text:'AI khoanh đúng dòng sai trong bài của An (dùng SD thay cho AD) và đề xuất điểm theo từng tiêu chí ở bên phải. Đây mới là đề xuất, chưa phải điểm.',
  seq:[[null,()=>go('gv-grade','gv')]],target:'.body > .card',place:'right'},
 {role:'gv',dev:'web',stage:5,title:'Giáo viên nhận từng tiêu chí rồi duyệt',act:'Bấm “Nhận” ở hai tiêu chí còn lại, rồi “Duyệt và trả bài”',
  text:'Tổng 7.5/10 trở thành điểm chính thức và bài được trả. Giá trị: chấm nhanh hơn, nhưng không điểm nào tới học sinh hay phụ huynh nếu chưa qua tay giáo viên.',
  seq:[['[data-act="acc"][data-arg="2"]',()=>ACT.acc(2)],['[data-act="acc"][data-arg="3"]',()=>ACT.acc(3)],['[data-act="approve"]',()=>ACT.approve(),800]],target:'.body > .col.grow',place:'left'},
 {role:'ph',dev:'phone',stage:6,title:'Phụ huynh thấy điểm sau khi duyệt',act:'Chuyển sang iPhone của phụ huynh',
  text:'Điểm 7.5/10 vừa duyệt xuất hiện kèm nhận xét của giáo viên; trước đó thẻ này chỉ ghi “đang chờ giáo viên duyệt”. Giá trị: gia đình nhận thông tin đã được thầy cô xác nhận.',
  seq:[[null,()=>go('ph-home','ph')]],target:()=>byText('.body .card.pad.row','Bài kiểm tra')},
 {role:'gv',dev:'web',stage:6,title:'Hồ sơ học tập dựa trên bằng chứng',act:'Giáo viên mở Hồ sơ của An từ mục Lớp học',
  text:'Mỗi nhận định về chỗ em hay vướng đều dẫn về bài làm và câu hỏi làm căn cứ, kèm ghi nhận của giáo viên. Giá trị: hiểu từng học sinh qua dữ liệu thật, không qua cảm tính.',
  seq:[[null,()=>go('gv-profile','gv')]],target:'.body > .col:last-child'},
 {role:'hs',dev:'ipad',stage:7,title:'Nhóm học liên trường',act:'An nhắn nhóm “Vậy kẻ AH ⊥ SO là xong đúng không?”',
  text:'Cả nhóm cùng thao tác trên một hình; trợ lý nhóm gợi ý tiếp chứ không nêu đáp số, và có giáo viên điều phối theo dõi. Giá trị: học cùng bạn trong một không gian an toàn.',
  seq:[[null,()=>go('hs-group','hs')],['[data-act="gsend"]',()=>ACT.gsend(),950]],target:'.body > .side',place:'left'},
 {role:'tr',dev:'web',stage:8,title:'Vận hành nhà trường',act:'Chuyển sang tài khoản quản trị của trường',
  text:'Ban giám hiệu xem hoạt động theo tổ bộ môn: bài đã giao, tỉ lệ hoàn thành, thời gian trả bài. Giá trị: điều hành bằng số liệu tổng hợp, không soi bài của từng học sinh.',
  seq:[[null,()=>go('tr-home','tr')]],target:'.body > .col > .card.pad'},
 {role:'ql',dev:'web',stage:9,title:'Cơ quan quản lý theo dõi triển khai',act:'Chuyển sang tài khoản cơ quan quản lý',
  text:'Một văn bản hướng dẫn được theo dõi qua bốn mức: đã nhận, đã xác nhận, đang triển khai, đã báo cáo. Giá trị: biết trường nào cần hỗ trợ, trong đúng phạm vi dữ liệu được ủy quyền.',
  seq:[[null,()=>go('ql-home','ql')]],target:'.body > .col > .card.pad'}
 ]};
const CMD=['Đổi bài 2 sang mức dễ hơn','Viết lại lời giải bài 1 ngắn gọn hơn','Bài 2: viết lời giải theo hướng dùng hình chiếu','Gộp gợi ý của bài 3 thành một gợi ý ngắn',
 'Sửa lại bài 2','Bài 2: đổi đề sang mức dễ hơn','Thêm một gợi ý cho bài 3','Thêm một ví dụ có số cụ thể','Viết lại cho dễ hiểu hơn, hợp với lớp 9B','Bài giảng 2: thêm một ví dụ có số cụ thể'];
const HAND='Nhớ lại: góc giữa SC và (ABCD) là góc giữa SC và hình chiếu của nó trên (ABCD). Em tìm hình chiếu của SC trước.';
// Nhận / Bỏ đề xuất đang chờ của bản nháp (PO-46)
const ACC=k=>()=>{const q=st[k].prop[0];if(q)ACT.dacc(k+','+q.id);},REJ=k=>()=>{const q=st[k].prop[0];if(q)ACT.drej(k+','+q.id);};
const CURR={key:'curr',name:'Soạn bài và giáo trình',
 base:()=>init(),
 steps:[
 {role:'gv',dev:'web',stage:'2 và 5',title:'Giáo trình chung của lớp 11A1',act:'Giáo viên mở mục Giáo trình',
  text:'Giáo trình xếp theo chương, bài học, bài tập; mỗi bài tập ghi nguồn gốc và hai nhãn: máy đã kiểm chứng chưa, giáo viên đã duyệt chưa. Bên phải là ba lối đưa bài vào: soạn với AI, import tài liệu, chụp ảnh.',
  seq:[[null,()=>go('gv-curr','gv')]],target:'[data-part="entry"]'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Định hướng bằng vài lựa chọn, AI soạn nháp',act:'Chọn “Dùng hình chiếu, không dùng tọa độ”, rồi bấm gửi',
  text:'Sau màn chờ ngắn, bản nháp hiện 3 bài; mỗi bài có đề, hình, hai gợi ý, lời giải từng bước, đáp số và nhãn “Đã kiểm chứng”, “AI soạn”. Giáo viên chỉ nêu hướng, phần soạn chi tiết do AI làm.',
  seq:[['[data-go="gv-compose"]',()=>go('gv-compose')],['[data-act="cset"][data-arg="dir,hc"]',()=>ACT.cset('dir,hc')],['[data-act="csend"]:not([data-arg])',()=>ACT.csend(),1400]],target:'[data-part="draft"]',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Yêu cầu chưa rõ thì AI hỏi lại',act:'Nhắn “Sửa lại bài 2”',
  text:'AI không tự đoán: nó hỏi lại thầy cô muốn sửa đề, gợi ý hay lời giải, kèm các lựa chọn bấm được. Bản nháp chưa đổi gì, nên giáo viên không phải dò xem AI đã sửa nhầm chỗ nào.',
  seq:[[`[data-act="csend"][data-arg="${CMD[4]}"]`,()=>ACT.csend(CMD[4]),900]],target:()=>$$('[data-part="chat"] .bub').slice(-2),place:'right'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Giáo viên trả lời, AI đề xuất “trước → sau”',act:'Bấm lựa chọn “Đề: đổi sang mức dễ hơn”',
  text:'Đề xuất hiện ngay trên thẻ bài 2: đề cũ gạch đi, đề mới bên dưới, kèm lý do, định hướng đã dựa vào và chỗ AI chưa chắc về mức độ. Bản nháp vẫn nguyên cho tới khi giáo viên quyết định.',
  seq:[[`[data-part="opts"] [data-arg="${CMD[5]}"]`,()=>ACT.csend(CMD[5]),900]],target:'[data-item="1"] .prop',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Nhận từng đề xuất',act:'Bấm “Nhận” ở bài 2; nhắn “Viết lại lời giải bài 1 ngắn gọn hơn”, rồi “Nhận”',
  text:'Bài 2 được thay bằng một bài dễ hơn, lời giải bài 1 rút còn các bước tính; thẻ vừa đổi có viền cam, nhãn “Vừa đổi” và dòng máy kiểm lại. Giáo viên sửa bằng lời nói thường, và chỉ thay đổi nào được nhận mới vào bản nháp.',
  seq:[['[data-act="dacc"]',ACC('cmp')],[`[data-act="csend"][data-arg="${CMD[1]}"]`,()=>ACT.csend(CMD[1]),900],['[data-act="dacc"]',ACC('cmp')]],target:'[data-item="0"]',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Bỏ một đề xuất, hoàn tác một thay đổi',act:'Nhắn “Thêm một gợi ý cho bài 3” rồi bấm “Bỏ”; nhắn “Gộp gợi ý của bài 3…”, bấm “Nhận”, rồi “Hoàn tác”',
  text:'Đề xuất bị bỏ thì bài 3 giữ nguyên; thay đổi vừa nhận cũng trả lại được bằng một lần bấm. Khung chat ghi rõ từng lượt, nên giáo viên thử nhiều hướng mà không sợ hỏng bản nháp.',
  seq:[[`[data-act="csend"][data-arg="${CMD[6]}"]`,()=>ACT.csend(CMD[6]),900],['[data-act="drej"]',REJ('cmp')],[`[data-act="csend"][data-arg="${CMD[3]}"]`,()=>ACT.csend(CMD[3]),900],['[data-act="dacc"]',ACC('cmp')],['[data-item="2"] [data-act="dundo"]',()=>ACT.dundo('cmp')]],target:()=>$$('[data-part="chat"] .bub').slice(-4),place:'right'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Sửa tay ngay trên bản nháp, xem lịch sử',act:'Bấm vào gợi ý 1 của bài 1 và gõ lại; mở “Lịch sử thay đổi”',
  text:'Bài 1 mang nhãn “Thầy cô đã sửa”; AI kiểm lại và báo gợi ý mới không lộ đáp số. Lịch sử ghi ai đổi gì: AI đề xuất, thầy cô nhận, bỏ, hoàn tác hay tự sửa; chưa có gì được lưu vào giáo trình.',
  seq:[['[data-edit="cmp,ex,0,h0"]',()=>ACT.dedit('cmp,ex,0,h0',HAND)],['[data-act="dhist"]',()=>ACT.dhist('cmp')]],target:()=>[$('[data-part="hist"]'),$('[data-item="0"]')],place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Xem như học sinh',act:'Bấm “Xem như học sinh” ở bài 1, rồi “Xem gợi ý 2”',
  text:'Bản xem trước hiện đúng thứ học sinh sẽ thấy: gợi ý 1, rồi gợi ý 2, còn lời giải vẫn khóa tới giờ giáo viên đặt khi giao bài. Giáo viên kiểm tra được trải nghiệm của học sinh trước khi duyệt.',
  seq:[['[data-act="pv"][data-arg="cmp,0"]',()=>ACT.pv('cmp,0')],['[data-act="pvhint"]',()=>ACT.pvhint()]],target:'.modal',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Duyệt và lưu vào giáo trình',act:'Đóng bản xem trước, bấm “Duyệt và lưu vào giáo trình”',
  text:'Ba bài vào đúng bài học của giáo trình với nguồn “Soạn với AI” và nhãn đổi thành “Giáo viên đã duyệt”. AI không tự lưu: không bài nào vào giáo trình nếu giáo viên chưa bấm duyệt.',
  seq:[['.modal [data-act="modal"]',()=>ACT.modal('')],['[data-act="csave"]',()=>ACT.csave(),950]],target:()=>$$('[data-new]').slice(0,2)},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Soạn một bài giảng với AI',act:'Mở “Soạn với AI”, chọn loại “Bài giảng”, khái niệm “Hai đường thẳng chéo nhau”, ví dụ “Không”, rồi bấm gửi',
  text:'Giáo viên chỉ chọn khái niệm, lớp, độ dài, có ví dụ và hình hay không. AI soạn nháp bài giảng cho khái niệm giáo án chưa có: tên, nội dung ngắn, hình nhỏ và từ khóa liên quan; bài giảng không nêu đáp số của bài tập nào.',
  seq:[['[data-go="gv-compose"]',()=>go('gv-compose')],['[data-act="ckind"][data-arg="lec"]',()=>ACT.ckind('lec')],['[data-act="cset"][data-arg="lo.ex,khong"]',()=>ACT.cset('lo.ex,khong')],['[data-act="csend"]:not([data-arg])',()=>ACT.csend(),1400]],target:'[data-part="draft"]',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Chỉnh bài giảng qua chat, AI nêu chỗ chưa chắc',act:'Nhắn “Thêm một ví dụ có số cụ thể” rồi “Nhận”; nhắn “Viết lại cho dễ hiểu hơn, hợp với lớp 9B”',
  text:'Ví dụ được thêm sau khi giáo viên nhận, máy đã tính lại các con số. Với yêu cầu thứ hai, AI đưa đề xuất trước → sau và nói thẳng nó chưa chắc nội dung này hợp với lớp 9B; quyền quyết định vẫn ở giáo viên.',
  seq:[[`[data-act="csend"][data-arg="${CMD[7]}"]`,()=>ACT.csend(CMD[7]),900],['[data-act="dacc"]',ACC('cmp')],[`[data-act="csend"][data-arg="${CMD[8]}"]`,()=>ACT.csend(CMD[8]),900]],target:'[data-litem="0"] .prop',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Xem bài giảng như học sinh',act:'Bấm “Bỏ” ở đề xuất cho lớp 9B, rồi “Xem như học sinh”',
  text:'Bản xem trước là đúng hộp bài giảng học sinh mở ra khi bấm từ khóa: tên khái niệm, dòng nguồn, nội dung, hình, ví dụ và từ khóa liên quan. Giáo viên thấy trước điều học sinh sẽ đọc.',
  seq:[['[data-act="drej"]',REJ('cmp')],['[data-act="lpv"]',()=>ACT.lpv('cmp,0')]],target:'.modal',place:'left'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Duyệt và lưu bài giảng',act:'Đóng bản xem trước, bấm “Duyệt và lưu vào giáo trình”',
  text:'Bài giảng vào khối “Bài giảng và khái niệm” với nguồn “Soạn với AI” và nhãn “Giáo viên đã duyệt”, thay cho bản AI tự viết đang chờ duyệt. Từ lúc này học sinh bấm từ khóa “hai đường thẳng chéo nhau” sẽ thấy đúng bài này.',
  seq:[['.modal [data-act="modal"]',()=>ACT.modal('')],['[data-act="csave"]',()=>ACT.csave(),950]],target:'[data-lec="cheo"]',place:'top'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Import giáo án: tách cả bài tập lẫn bài giảng',act:'Chọn “Import tài liệu”, chọn tệp Giao-an-Hinh-khong-gian-11A1.docx, bấm “Đọc và nhận diện”',
  text:'Hệ thống đọc tài liệu và chia kết quả thành hai nhóm: 6 bài tập (đã dựng hình, kiểm chứng, soạn gợi ý và lời giải; bài 6 mang nhãn “Cần xem lại”) và 7 bài giảng lấy từ phần lý thuyết. Giáo viên không phải chép lại giáo án.',
  seq:[['[data-act="impopen"][data-arg="doc"]',()=>ACT.impopen('doc')],['[data-act="impfile"]',()=>ACT.impfile()],['[data-act="imprun"]',()=>ACT.imprun(),2700]],target:()=>[$('[data-part="results"] > .card'),$('[data-part="groups"]')],place:'bottom'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Nhóm Bài giảng: chọn mục, sửa bằng chat',act:'Mở nhóm “Bài giảng”, bỏ chọn mục 7, nhắn “Bài giảng 2: thêm một ví dụ có số cụ thể”, rồi “Nhận”',
  text:'Mỗi bài giảng ghi mục của giáo án mà nó được lấy ra và được đối chiếu với kho: trùng khớp, hay sẽ cập nhật. Giáo viên xem từng mục, bỏ mục không muốn lưu và sửa qua chat như với bài tập.',
  seq:[['[data-act="impgrp"][data-arg="lec"]',()=>ACT.impgrp('lec')],['[data-act="lsel"][data-arg="6"]',()=>ACT.lsel(6)],[`[data-act="impchat"][data-arg="${CMD[9]}"]`,()=>ACT.impchat(CMD[9]),900],['[data-act="dacc"]',ACC('imp')]],target:'[data-litem="1"]'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Lưu bài từ tài liệu, rồi chọn nhiều ảnh',act:'Bấm “Duyệt và lưu”, mở “Chụp hoặc chọn ảnh”, chọn 3 ảnh trang vở',
  text:'Bốn bài tập mới từ giáo án vào giáo trình, hai bài trùng được bỏ qua; bài giảng vừa sửa được cập nhật trong kho. Tiếp theo giáo viên đưa lên ba trang vở viết tay; từng ảnh bỏ được hoặc đổi thứ tự trước khi đọc.',
  seq:[['[data-act="impsave"]',()=>ACT.impsave(),950],['[data-act="impopen"][data-arg="img"]',()=>ACT.impopen('img')],['[data-act="imppick"]',()=>ACT.imppick()]],target:'[data-part="thumbs"]',place:'bottom'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Đọc chữ viết tay, sửa chỗ máy chưa chắc',act:'Bấm “Đọc và nhận diện”, rồi xác nhận hai ô vàng ở bài 1',
  text:'Bên trái là chữ máy đọc được từ ảnh, chỗ chưa chắc tô vàng và sửa được ngay; bên phải là bài đã chuẩn hóa kèm hình, gợi ý, lời giải. Giáo viên chỉ phải soát đúng những chỗ máy không chắc.',
  seq:[['[data-act="imprun"]',()=>ACT.imprun(),2700],['[data-act="ocrok"][data-arg="x5:1"]',()=>ACT.ocrok('x5:1','2a')],['[data-act="ocrok"][data-arg="x5:3"]',()=>ACT.ocrok('x5:3','thể tích')]],target:'[data-item="0"]'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Sửa tiếp bằng chatbot',act:'Nhắn “Bài 2: viết lời giải theo hướng dùng hình chiếu”, rồi “Gộp gợi ý của bài 3 thành một gợi ý ngắn”; bấm “Nhận” ở từng đề xuất',
  text:'Lời giải bài 2 đổi từ tọa độ sang dựng hình chiếu, đáp số vẫn là 2a√3/3; hai gợi ý của bài 3 gộp thành một. Lời giải theo đúng cách dạy của giáo viên mà không phải soạn lại từ đầu.',
  seq:[[`[data-act="impchat"][data-arg="${CMD[2]}"]`,()=>ACT.impchat(CMD[2]),900],['[data-act="dacc"]',ACC('imp')],[`[data-act="impchat"][data-arg="${CMD[3]}"]`,()=>ACT.impchat(CMD[3]),900],['[data-act="dacc"]',ACC('imp')]],target:'[data-item="1"]'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Giáo trình chung đã có bài mới',act:'Bấm “Duyệt và lưu vào giáo trình”',
  text:'Ba bài từ ảnh vào giáo trình với nguồn “Import ảnh”. Bài hình cầu vẫn giữ nhãn “Cần xem lại”: giáo viên duyệt được nội dung, nhưng không tự gắn được nhãn kiểm chứng của máy.',
  seq:[['[data-act="impsave"]',()=>ACT.impsave(),950]],target:'[data-part="les-l4"]'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Duyệt bài giảng do AI viết',act:'Ở khối “Bài giảng và khái niệm”, mở bài “Tỉ số lượng giác trong tam giác vuông”, bấm “Duyệt bài giảng này”',
  text:'Giáo án của thầy cô chưa có khái niệm này nên AI viết thay và gắn nhãn “AI soạn · chờ giáo viên duyệt”. Giáo viên đọc rồi bấm duyệt: nhãn đổi thành “Giáo viên đã duyệt”, và học sinh thấy đúng nhãn đó khi bấm từ khóa.',
  seq:[['[data-lec="tslg"]',()=>ACT.lec('tslg')],['.modal [data-act="kbok"]',()=>ACT.kbok('tslg')]],target:'.modal'},
 {role:'gv',dev:'web',stage:'2 và 5',title:'Giao một bài trong giáo trình cho lớp',act:'Đóng bài giảng, bấm “Giao bài” ở bài khoảng cách vừa soạn, rồi “Giao bài cho 11A1”',
  text:'Bài đi qua đúng luồng giao bài đã có: chọn lớp, đặt giờ mở lời giải 19:00, rồi sang Theo dõi lớp. Mạch soạn hoặc import, giáo trình, giao bài, học sinh làm đã liền nhau.',
  seq:[['.modal [data-act="modal"]',()=>ACT.modal('')],['[data-act="cassign"][data-arg="x2e"]',()=>ACT.cassign('x2e')],['[data-act="assign"]',()=>ACT.assign(),950]],target:'table tbody tr'},
 {role:'gv',dev:'web',stage:6,title:'Giáo trình riêng: bằng chứng trước, đề xuất sau',act:'Mở “Tạo giáo trình riêng”, chọn An, bấm “Đề xuất giáo trình riêng”',
  text:'Cột giữa nêu phần An đã vững và đang luyện, kèm bài và câu hỏi làm căn cứ. AI rút lộ trình từ giáo trình chung: phần Thể tích lược bớt, phần Khoảng cách thêm bài và gợi ý kỹ hơn, mỗi thay đổi có lý do.',
  seq:[[null,()=>go('gv-curr')],['[data-go="gv-personal"]',()=>go('gv-personal')],['[data-act="psel"][data-arg="an"]',()=>ACT.psel('an')],['[data-act="ppropose"]',()=>ACT.ppropose(),1150]],target:'[data-part="evid"]',place:'right'},
 {role:'gv',dev:'web',stage:6,title:'Giáo viên thêm, bỏ từng mục rồi duyệt',act:'Giữ lại bài “Hình chóp tứ giác đều”, rồi bấm “Duyệt và áp dụng”',
  text:'Giáo viên giữ lại một bài AI định lược bớt, rồi duyệt; chỉ khi đó lộ trình mới áp dụng cho An. Dòng dưới cùng là toàn bộ những gì phụ huynh sẽ thấy; không có xếp hạng hay so sánh giữa các em.',
  seq:[['[data-act="ptog"][data-p="x5"]',()=>ACT.ptog(st.per.prop.rows.findIndex(r=>r.p==='x5'))],['[data-act="papprove"]',()=>ACT.papprove(),850]],target:'[data-part="approve"]',place:'top'},
 {role:'hs',dev:'ipad',stage:6,title:'Học sinh thấy lộ trình của mình',act:'Trên iPad, An bấm thẻ “Lộ trình của em”',
  text:'An thấy đúng lộ trình giáo viên vừa duyệt: phần đang luyện có thêm bài, bài đã giao mở được ngay. Em biết mình cần làm gì tiếp theo, và vẫn theo quy tắc gợi ý trước, lời giải mở theo giờ giáo viên đặt.',
  seq:[[null,()=>go('hs-home','hs')],['[data-go="hs-path"]',()=>go('hs-path')]],target:'[data-part="path"]'},
 {role:'ph',dev:'phone',stage:6,title:'Phụ huynh chỉ thấy một dòng đã duyệt',act:'Chuyển sang iPhone của phụ huynh',
  text:'Phụ huynh thấy một dòng tóm tắt do giáo viên duyệt: con được rút gọn phần nào, luyện thêm phần nào. Bằng chứng chi tiết và nội dung câu con hỏi không hiển thị ở đây.',
  seq:[[null,()=>go('ph-home','ph')]],target:'[data-part="phsum"]'}
 ]};
const SCRIPTS=[MVP,VISION,CURR];

/* ---------------- BỘ HẸN GIỜ ---------------- */
// Trong lúc trình chiếu, setTimeout của bản mẫu (busy, toast, câu trả lời trễ) được ghi lại
// để có thể chạy ngay (khi bấm Tiếp) hoặc hủy (khi đi lùi). Thoát tour thì trả về như cũ.
const oST=window.setTimeout,oCT=window.clearTimeout;
const rST=(f,ms)=>oST.call(window,f,ms);
let mode='off';const pend=new Map();
function tST(fn,ms,...a){
 if(typeof fn!=='function')return oST.call(window,fn,ms);
 if(mode==='sync'){fn(...a);return 0;}
 const h=oST.call(window,()=>{pend.delete(h);fn(...a);},ms);pend.set(h,()=>fn(...a));return h;}
function tCT(h){pend.delete(h);oCT.call(window,h);}
function flush(){const m=mode;mode='sync';while(pend.size){const[h,f]=pend.entries().next().value;pend.delete(h);oCT.call(window,h);f();}mode=m;}
function cancelAll(){pend.forEach((f,h)=>oCT.call(window,h));pend.clear();
 document.getElementById('busy').classList.remove('on');document.getElementById('toast').classList.remove('on');}
const sleep=ms=>new Promise(r=>rST(r,ms));

/* ---------------- GIAO DIỆN ---------------- */
const T={on:false,sc:null,i:-1,k:0,ready:false,quiet:false,auto:false,autoH:0,first:true};
let token=0,refH=0;
const mk=(id,html)=>{const e=document.createElement('div');e.id=id;e.innerHTML=html||'';document.body.appendChild(e);return e;};
const block=mk('tour-block'),spot=mk('tour-spot'),clk=mk('tour-click'),pill=mk('tour-pill','<i></i><b></b><span></span>');
const pop=mk('tour-pop',`<i class="tp-arrow"></i>
 <div class="tp-head"><span class="tp-role"><i></i><b></b></span><span class="tp-stage"></span><span class="tp-count"></span></div>
 <h3 class="tp-title"></h3><div class="tp-act"></div><p class="tp-text"></p>
 <div class="tp-bar"><i></i></div>
 <div class="tp-ctl"><button data-t="prev">← Trước</button><button data-t="auto">▶ Tự chạy</button><button data-t="exit">Thoát</button><button data-t="next">Tiếp →</button></div>
 <div class="tp-keys"><kbd>←</kbd> <kbd>→</kbd> chuyển bước · <kbd>Space</kbd> tự chạy · <kbd>Esc</kbd> thoát</div>`);
pop.setAttribute('role','dialog');pop.setAttribute('aria-live','polite');
const menu=mk('tour-menu',`<div class="tm-h">Chọn kịch bản trình chiếu</div>
 <div class="tm-i" data-tour="mvp"><div class="tm-n">▶</div><div><b>${MVP.name}</b><small>${MVP.steps.length} bước · Giáo viên → Học sinh (hình 3D, bài giảng, hỏi đáp) → Phụ huynh</small></div></div>
 <div class="tm-i v" data-tour="vision"><div class="tm-n">▶</div><div><b>${VISION.name}</b><small>${VISION.steps.length} bước · các màn hình concept từ chặng 2 đến chặng 9</small></div></div>
 <div class="tm-i c" data-tour="curr"><div class="tm-n">▶</div><div><b>${CURR.name}</b><small>${CURR.steps.length} bước · soạn bài tập và bài giảng với AI, chat chỉnh bản nháp, import, giáo trình riêng</small></div></div>
 <div class="tm-k"><kbd>←</kbd> <kbd>→</kbd> chuyển bước · <kbd>Space</kbd> tự chạy · <kbd>Esc</kbd> thoát</div>`);
const P=s=>pop.querySelector(s);

function injectBtn(){const d=document.getElementById('demo');if(!d||d.querySelector('.tour-btn'))return;
 const b=document.createElement('span');b.className='tour-btn';b.textContent='▶ Trình chiếu';
 const r=d.querySelector('[data-reset]');if(r)d.insertBefore(b,r);else d.appendChild(b);
 const h=d.offsetHeight+'px',root=document.documentElement;
 if(root.style.getPropertyValue('--dh')!==h){root.style.setProperty('--dh',h);if(document.querySelector('#app .screen'))frame();}}

const baseRender=window.render;
window.render=function(){baseRender.apply(this,arguments);injectBtn();if(T.on&&!T.quiet&&T.ready)refreshSoon();};
window.addEventListener('resize',()=>{injectBtn();closeMenu();if(T.on&&T.ready)refreshSoon();});
injectBtn();

function openMenu(){const b=document.querySelector('#demo .tour-btn');if(!b)return;const r=b.getBoundingClientRect();
 menu.classList.add('on');menu.style.top=(r.bottom+8)+'px';menu.style.left=Math.max(8,Math.min(innerWidth-menu.offsetWidth-8,r.right-menu.offsetWidth))+'px';}
function closeMenu(){menu.classList.remove('on');}
document.addEventListener('click',e=>{
 if(e.target.closest('.tour-btn')){menu.classList.contains('on')?closeMenu():openMenu();return;}
 const it=e.target.closest('#tour-menu [data-tour]');if(it){closeMenu();return start(it.dataset.tour);}
 if(!e.target.closest('#tour-menu'))closeMenu();
 const b=e.target.closest('#tour-pop button');if(!b||!T.on)return;
 b.blur();
 ({prev,next,exit,auto:toggleAuto})[b.dataset.t]();});
document.addEventListener('keydown',e=>{
 if(e.key==='Escape'&&menu.classList.contains('on')){closeMenu();return;}
 if(!T.on)return;
 const f={ArrowRight:next,ArrowLeft:prev,Escape:exit,' ':toggleAuto}[e.key];
 if(f){e.preventDefault();e.stopPropagation();f();}},true);

/* ---------------- ĐIỀU KHIỂN ---------------- */
function start(key){
 T.sc=SCRIPTS.find(s=>s.key===key)||MVP;T.on=true;T.auto=false;T.first=true;T.i=-1;
 window.setTimeout=tST;window.clearTimeout=tCT;mode='live';
 document.body.classList.add('tour-on');rebuild(0);}
function exit(){
 if(!T.on)return;token++;stopAuto();T.on=false;T.ready=false;T.quiet=false;mode='off';
 window.setTimeout=oST;window.clearTimeout=oCT;                 // các hẹn giờ còn dang dở vẫn tự chạy nốt
 document.body.classList.remove('tour-on');[spot,clk,pill,pop].forEach(e=>e.classList.remove('on'));
 dev=null;st.modal=null;render();}
function next(){if(!T.on)return;if(T.i>=T.sc.steps.length-1){if(T.ready)exit();else finishCurrent(true);return;}goTo(T.i+1);}
function prev(){if(T.on&&T.i>0)goTo(T.i-1);}
function goTo(i){const n=T.sc.steps.length;if(i<0||i>=n)return;
 if(i===T.i+1&&T.i>=0){finishCurrent(false);runLive(i);}else rebuild(i);}
// chạy nốt (đồng bộ) phần còn lại của bước hiện tại: thao tác chưa làm và các phản hồi trễ
function finishCurrent(showIt){token++;stopAuto();clk.classList.remove('on');T.quiet=true;
 const s=T.sc.steps[T.i],m=mode;mode='sync';dev=s.dev;
 while(T.k<s.seq.length){s.seq[T.k][1]();T.k++;}
 mode=m;flush();T.quiet=false;if(showIt)show();}
function rebuild(i){token++;stopAuto();clk.classList.remove('on');T.quiet=true;cancelAll();
 mode='sync';st=T.sc.base();
 for(let k=0;k<=i;k++){const s=T.sc.steps[k];dev=s.dev;s.seq.forEach(m=>m[1]());}
 mode='live';T.i=i;T.k=T.sc.steps[i].seq.length;T.quiet=false;show();}
async function runLive(i){const my=token,s=T.sc.steps[i];T.i=i;T.k=0;T.ready=false;T.quiet=true;mode='live';dev=s.dev;
 if(s.seq.some(m=>m[0]||m[2]))hold(s);
 for(const m of s.seq){
  if(m[0]){const el=$(m[0]);if(el){reveal([el]);press(el);await sleep(PULSE_MS);if(my!==token)return;clk.classList.remove('on');}}
  m[1]();T.k++;
  if(m[2]){await sleep(m[2]);if(my!==token)return;}}
 T.quiet=false;show();}

function hold(s){spot.classList.remove('on');pop.classList.add('hold');
 pill.querySelector('b').textContent=s.act;pill.querySelector('span').textContent=(T.i+1)+'/'+T.sc.steps.length;pill.classList.add('on');}
function press(el){const r=el.getBoundingClientRect();clk.classList.remove('on');void clk.offsetWidth;
 Object.assign(clk.style,{left:r.left-4+'px',top:r.top-4+'px',width:r.width+8+'px',height:r.height+8+'px'});clk.classList.add('on');}

function show(){const s=T.sc.steps[T.i],n=T.sc.steps.length,d=devOf();
 pill.classList.remove('on');clk.classList.remove('on');
 P('.tp-role i').style.background=ROLE[s.role][1];P('.tp-role b').textContent=ROLE[s.role][0]+' · '+DEVN[d];
 P('.tp-stage').textContent=s.stage?'Chặng '+s.stage:'';P('.tp-stage').style.display=s.stage?'':'none';
 P('.tp-count').textContent=(T.i+1)+'/'+n;P('.tp-title').textContent=s.title;P('.tp-act').textContent=s.act;P('.tp-text').textContent=s.text;
 P('[data-t=prev]').disabled=T.i===0;P('[data-t=next]').textContent=T.i===n-1?'Kết thúc ✓':'Tiếp →';
 pop.classList.add('on');pop.classList.remove('hold');
 if(T.first){pop.classList.add('jump');spot.classList.add('jump');}
 T.ready=true;refresh();
 if(T.first){void pop.offsetWidth;pop.classList.remove('jump');spot.classList.remove('jump');T.first=false;}
 refreshSoon();syncAuto();}
function refreshSoon(){refresh();oCT.call(window,refH);refH=rST(()=>{if(T.on&&T.ready)refresh();},380);}

/* ---------------- ĐỊNH VỊ ---------------- */
function targets(s){let t=s.target;if(!t)return[];if(typeof t==='function')t=t();if(typeof t==='string')t=$(t);return(Array.isArray(t)?t:[t]).filter(Boolean);}
function union(els){let a=null;els.forEach(e=>{const r=e.getBoundingClientRect();if(!r.width&&!r.height)return;
 a=a?{left:Math.min(a.left,r.left),top:Math.min(a.top,r.top),right:Math.max(a.right,r.right),bottom:Math.max(a.bottom,r.bottom)}:{left:r.left,top:r.top,right:r.right,bottom:r.bottom};});return a;}
// cuộn vùng chứa (bên trong khung thiết bị, có thể đang bị scale) để phần tử nằm giữa
function reveal(els){const scr=document.querySelector('#app .screen');let p=els[0]&&els[0].parentElement;
 while(p&&scr&&scr.contains(p)){
  if(/(auto|scroll)/.test(getComputedStyle(p).overflowY)&&p.scrollHeight>p.clientHeight+2){
   const pr=p.getBoundingClientRect(),k=pr.height/p.offsetHeight||1,r=union(els);if(!r)return;
   const d=r.bottom-r.top>pr.height?r.top-pr.top-12*k:(r.top+r.bottom)/2-(pr.top+pr.bottom)/2;
   p.scrollTop+=d/k;return;}
  p=p.parentElement;}}
function refresh(){if(!T.on||!T.ready)return;const s=T.sc.steps[T.i],els=targets(s);
 if(document.getElementById('busy').classList.contains('on')||!els.length){spot.classList.remove('on');return place(null,s);}
 reveal(els);let R=union(els);
 const scr=document.querySelector('#app .screen');
 if(R&&scr){const c=scr.getBoundingClientRect(),pd=6;
  R={left:Math.max(R.left-pd,c.left+3),top:Math.max(R.top-pd,c.top+3),right:Math.min(R.right+pd,c.right-3),bottom:Math.min(R.bottom+pd,c.bottom-3)};
  const rail=document.querySelector('#app.dev-phone .rail');if(rail)R.bottom=Math.min(R.bottom,rail.getBoundingClientRect().top-3);}
 if(!R||R.right-R.left<4||R.bottom-R.top<4){spot.classList.remove('on');return place(null,s);}
 Object.assign(spot.style,{left:R.left+'px',top:R.top+'px',width:R.right-R.left+'px',height:R.bottom-R.top+'px'});spot.classList.add('on');
 place(R,s);}
function place(R,s){const vw=innerWidth,vh=innerHeight,M=10,G=22,w=pop.offsetWidth,h=pop.offsetHeight,ar=P('.tp-arrow');
 const cl=(v,a,b)=>Math.max(a,Math.min(b,v));
 let x,y,side='';
 if(!R){x=(vw-w)/2;y=vh-h-24;}
 else{
  const sp={right:vw-M-G-R.right,left:R.left-M-G,bottom:vh-M-G-R.bottom,top:R.top-M-G},need={right:w,left:w,bottom:h,top:h};
  // ưu tiên đặt bên cạnh (trái/phải), sau đó mới tới dưới/trên; mỗi cặp chọn phía rộng hơn
  let order=[...(sp.right>=sp.left?['right','left']:['left','right']),...(sp.bottom>=sp.top?['bottom','top']:['top','bottom'])];
  if(s.place)order=[].concat(s.place,order);
  side=order.find(k=>sp[k]>=need[k])||'';
  const cx=(R.left+R.right)/2,cy=(R.top+R.bottom)/2,al=s.align||'center';
  if(side==='right'||side==='left'){x=side==='right'?R.right+G:R.left-G-w;
   y=cl(al==='start'?R.top:al==='end'?R.bottom-h:cy-h/2,M,vh-M-h);
   ar.style.left=ar.style.right='';ar.style.top=cl(cy-y-8,22,h-38)+'px';ar.style.bottom='';}
  else if(side){y=side==='bottom'?R.bottom+G:R.top-G-h;
   x=cl(al==='start'?R.left:al==='end'?R.right-w:cx-w/2,M,vw-M-w);
   ar.style.top=ar.style.bottom='';ar.style.left=cl(cx-x-8,22,w-38)+'px';ar.style.right='';}
  else{ // không đủ chỗ ở cả bốn phía: chọn góc màn hình che phần tử ít nhất
   let best=1e18;[[M,M],[vw-M-w,M],[M,vh-M-h],[vw-M-w,vh-M-h]].forEach(c=>{
    const ov=Math.max(0,Math.min(c[0]+w,R.right)-Math.max(c[0],R.left))*Math.max(0,Math.min(c[1]+h,R.bottom)-Math.max(c[1],R.top));
    if(ov<best){best=ov;x=c[0];y=c[1];}});}}
 pop.dataset.side=side;pop.style.left=Math.round(x)+'px';pop.style.top=Math.round(y)+'px';}

/* ---------------- TỰ CHẠY ---------------- */
function stopAuto(){oCT.call(window,T.autoH);T.autoH=0;const b=P('.tp-bar i');b.classList.remove('run');b.style.animationDuration='';}
function syncAuto(){stopAuto();const b=P('.tp-bar i'),btn=P('[data-t=auto]'),last=T.i>=T.sc.steps.length-1;
 btn.classList.toggle('on',T.auto);btn.textContent=T.auto?'❚❚ Tạm dừng':'▶ Tự chạy';b.classList.remove('full');
 if(!T.auto||!T.ready)return;
 if(last){T.auto=false;btn.classList.remove('on');btn.textContent='▶ Tự chạy';b.classList.add('full');return;}
 void b.offsetWidth;b.style.animationDuration=AUTO_MS+'ms';b.classList.add('run');
 T.autoH=rST(()=>{if(T.on&&T.auto)goTo(T.i+1);},AUTO_MS);}
function toggleAuto(){if(!T.on)return;T.auto=!T.auto;syncAuto();if(T.auto&&!T.ready){P('[data-t=auto]').classList.add('on');}}

window.__tour={start,exit,next,prev,toggleAuto,scripts:SCRIPTS.map(s=>({key:s.key,name:s.name,n:s.steps.length})),
 state:()=>({on:T.on,key:T.sc&&T.sc.key,i:T.i,n:T.sc?T.sc.steps.length:0,ready:T.ready,auto:T.auto,pending:pend.size})};
})();
