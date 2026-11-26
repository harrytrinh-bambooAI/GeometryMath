/* s23k.js · Phiên bản: v0.1 · 2026-11-26 · thanh nav gọn + trình phát kịch bản (hash #kb=<mã>&b=<n>) + trang "Cách hệ thống vận hành" + chế độ Trình chiếu toàn màn hình
   Lịch sử: v0.1 · 26/11/2026 · tạo mới (T-261126-38, Designer Dom). Chỉ dùng màn đã đăng ký trong V; bước thiếu màn ghi "chưa vẽ". */
(function(){
const LS={get(k){try{return localStorage.getItem('s23.'+k);}catch(e){return null;}},set(k,v){try{localStorage.setItem('s23.'+k,v);}catch(e){}}};
const S=(s,w,t,a,y,b,h,m)=>({s,w,t,a,y,b,h,m});
const KBS=[
{id:'kb1',name:'KB1 · Giáo viên mở lớp',sum:'Đăng ký bằng tên, nhận mã người dùng, tạo lớp và mã lớp, duyệt từng em vào lớp.',steps:[
 S('c3-01-dang-ky','gv','Đăng ký chỉ bằng tên','Cô Lan Anh gõ tên hiển thị, chọn vai Giáo viên, tự đặt mật khẩu và tick đã đọc thông báo riêng tư.','Quyết định PO-61: không thu email, số điện thoại hay dữ liệu cá nhân; đăng ký mất nửa phút.','Hệ thống sinh mã người dùng duy nhất, giới hạn số tài khoản tạo trong một khoảng thời gian để chống tạo hàng loạt.','.cbx'),
 S('c3-01-ma-nguoi-dung','gv','Nhận mã người dùng và mã khôi phục','Cô sao chép mã người dùng (dùng để đăng nhập) và mã khôi phục (chỉ hiện một lần).','Không có email nên không có cách gửi lại mật khẩu; mã khôi phục là lối thoát duy nhất.','Máy chủ chỉ lưu bản băm của mật khẩu và mã khôi phục, không lưu bản gốc.','.code.lt'),
 S('c3-01-ma-chua-tick','gv','Chưa lưu mã thì chưa được đi tiếp','Nút “Vào trang giáo viên” bị khóa cho tới khi cô tick “Tôi đã lưu mã khôi phục”.','Mã chỉ hiện một lần; khóa nút để người dùng không bỏ qua.','Hệ thống ghi nhận việc tick (không lưu mã) để tránh nhấn nhầm.','.cbx'),
 S(null,'gv','Tạo lớp','Cô đặt tên lớp 11A1 và bấm Tạo lớp.','Lớp là chỗ gom học sinh và bộ bài.','Hệ thống sinh mã lớp ngẫu nhiên, không chứa thông tin cá nhân.',null,'Form tạo lớp (tên lớp, năm học) chưa vẽ riêng; bước sau là kết quả sau khi tạo.'),
 S('c3-07-ma-lop','gv','Mã lớp và bật duyệt từng em','Cô sao chép hoặc in mã lớp, bật “Giáo viên duyệt từng em”.','PO-66 G4: chống dò mã lớp bằng giới hạn lượt thử và duyệt từng em.','Mã lớp có thể đặt lại; mã cũ hết hiệu lực ngay.','.toggle'),
 S('c3-07-vao-lop','hs','Học sinh nhập mã lớp','Em Bình An mở iPad, nhập mã lớp và chờ duyệt.','Em chỉ cần mã lớp, không cần khai thông tin cá nhân.','Hệ thống đếm số lần nhập mã của tài khoản; nhập quá giới hạn thì tạm khóa.','.code'),
 S('c3-07-duyet-dat-lai-mk','gv','Giáo viên duyệt từng em','Cô thấy danh sách chờ, duyệt từng em vào lớp, có thể đặt lại mật khẩu cho em.','Tránh người lạ vào lớp; học sinh quên mật khẩu thì nhờ cô đặt lại.','Em được duyệt mới thấy bộ bài của lớp; em chưa duyệt không thấy gì.','table'),
 S('c3-07-bi-tu-choi','hs','Nếu cô từ chối','Em nhận thông báo bị từ chối vào lớp và được hướng dẫn hỏi lại cô.','Không để em chờ vô hạn mà không biết lý do.','Trạng thái “bị từ chối” lưu trên tài khoản em, mã lớp không bị lộ thêm.',null)]},
{id:'kb2',name:'KB2 · Giáo viên thêm đề',sum:'Dán chữ hoặc ảnh, AI dựng hình và lời giải, kiểm và sửa, lệch đáp số bị chặn duyệt, duyệt vào Kho.',steps:[
 S('c2-02-nhap','gv','Dán chữ hoặc ảnh đề','Cô dán đề bằng chữ hoặc chụp ảnh đề.','Nhập nhanh nhất có thể, không phải gõ lại công thức.','Ảnh được đọc bằng Claude vision; ô nào chưa chắc sẽ được tô để cô sửa.','textarea,.field'),
 S('c2-02-dang-dung','ai','AI dựng hình và lời giải','AI đọc đề, dựng hình 3D và viết lời giải từng bước.','Cô có ngay bản nháp để soát thay vì làm từ đầu.','Máy dựng hình tính toạ độ thật; AI không “vẽ tay” nên hình đúng với số liệu đề.','.spin,.steps'),
 S('c2-02-anh-mo','gv','Ngoại lệ: ảnh mờ','Cô nhận thông báo ảnh mờ và được gợi ý chụp lại hoặc gõ chữ.','Đọc sai đề thì mọi bước sau đều sai.','Hệ thống từ chối dựng khi độ tin cậy thấp thay vì đoán.','.uncn,.chip.red'),
 S('c2-02-engine-khong-dung','gv','Ngoại lệ: máy không dựng được','Máy báo không dựng được hình cho đề này và đề xuất cách khác.','Nói thẳng giới hạn thay vì cho hình sai.','Lỗi ghi lại để đội kỹ thuật bổ sung dạng đề.','.uncn,.chip.red'),
 S('c2-02-lech-dap-so','gv','Ngoại lệ: lệch đáp số','Đáp số AI viết khác đáp số máy tính được.','Hai nguồn độc lập mà lệch là dấu hiệu có lỗi.','Máy kiểm đáp số chạy lại phép tính và so với lời giải AI.','.chip.red,.uncn'),
 S('c2-03-chua-kiem','gv','Soát: chưa kiểm chứng','Cô xem từng bước lời giải cùng hình; bước chưa kiểm chứng có nhãn.','Cô là người chốt cuối, AI chỉ đề xuất.','Mỗi bước có trạng thái kiểm chứng lưu theo phiên bản.','.chip.yel,.chip'),
 S('c2-03-lech-chan-duyet','gv','Lệch đáp số thì chặn duyệt','Nút Duyệt bị khóa; cô sửa lời giải hoặc đáp số rồi kiểm lại.','Không để lời giải sai vào Kho chung.','Máy kiểm chạy lại sau mỗi lần sửa; chỉ khi khớp mới mở nút.','.btn'),
 S('c2-04a-chat-chinh-nhap','ai','Chat để chỉnh nháp','Cô nhắn AI chỉnh bước hoặc hình; AI hỏi lại khi chưa rõ, có trước/sau.','Sửa bằng lời nhanh hơn sửa từng ô.','Mỗi lần chỉnh tạo phiên bản mới của nháp, quay lại được.','.chat,.bubble'),
 S(null,'gv','Duyệt và vào Kho','Cô bấm Duyệt, đề vào Kho đề.','Đề đã duyệt dùng được cho bộ bài và Trình chiếu.','Giáo viên xem lời giải Kho chung ngay (PO-66 G2); học sinh tự học chỉ thấy gợi ý.',null,'Màn xác nhận “đã vào Kho” chưa vẽ riêng.'),
 S('c2-03-da-giao','gv','Sửa sau khi đã giao','Đề đã giao cho lớp, cô sửa thì chỉ tạo phiên bản mới, bài đã giao giữ nguyên.','Không làm đổi bài các em đang làm.','Phiên bản cũ vẫn gắn với bài đã giao.','.chip')]},
{id:'kb3',name:'KB3 · Giáo viên soạn bộ bài',sum:'Soạn bộ bài, đặt lịch mở lời giải, giao lớp, học sinh nhận thông báo.',steps:[
 S('c3-03-bo-bai','gv','Soạn bộ bài','Cô chọn các đề từ Kho vào một bộ bài.','PO-66 G1: một mô hình “bộ bài” dùng chung cho luyện và bài tập.','Bộ bài lưu danh sách đề và phiên bản đề tại thời điểm giao.','.cbx'),
 S('c2-08-soan-bai-luyen','gv','Cài đặt luyện và giờ mở lời giải','Cô chọn kiểu đáp số, sai số, số lượt thử, lớp và giờ mở lời giải.','Cô quyết định khi nào các em được xem lời giải.','Lời giải khóa ở máy chủ tới giờ đặt; máy học sinh không có sẵn lời giải để lộ.','.toggle'),
 S('c2-08-ban-phim-nang-cao','gv','Câu cần biểu thức','Với câu có đáp số là biểu thức, bàn phím toán nâng cao được bật.','Học sinh gõ được căn, phân số, mũ trên iPad.','Máy kiểm so sánh biểu thức tương đương, không so từng ký tự.','.kbd2'),
 S('c2-09c-trac-nghiem-gv','gv','Câu trắc nghiệm','Cô soạn câu trắc nghiệm, đánh dấu đáp án đúng.','Có câu nhanh gọn để kiểm tra khái niệm.','Máy chấm trắc nghiệm ngay, không cần kiểm biểu thức.','.cbx'),
 S('c3-04-thong-bao-ipad','hs','Học sinh nhận thông báo (iPad)','Em thấy thông báo có bộ bài mới của lớp.','Em biết có bài mà không cần cô nhắc.','Thông báo trong ứng dụng, không dùng email hay số điện thoại.','.card',' Danh sách “Bài của em” chưa vẽ riêng; thông báo mở thẳng bộ bài.'),
 S('c3-04-thong-bao-iphone','hs','Cùng thông báo trên iPhone','Thông báo hiển thị gọn trên màn nhỏ.','Nhiều em chỉ có điện thoại.','Cùng một dữ liệu, giao diện co lại theo thiết bị.','.card')]},
{id:'kb4',name:'KB4 · Học sinh làm bài',sum:'Vào lớp bằng mã, Bài của em, làm bài trên iPad hoặc iPhone với bàn phím toán, sai nhận gợi ý, hết lượt, xem kết quả, mất mạng.',steps:[
 S('c3-07-vao-lop','hs','Vào lớp bằng mã','Em nhập mã lớp, chờ cô duyệt.','Chỉ em được duyệt mới vào lớp.','Giới hạn tổng lượt thử mã theo tài khoản.','.code'),
 S('c3-04-thong-bao-ipad','hs','Bài của em','Em thấy bộ bài cô giao.','Mở bài nhanh nhất.','Danh sách chỉ hiện bộ bài của lớp em đã được duyệt.','.card','Danh sách “Bài của em” chưa vẽ riêng; đang dùng màn thông báo thay.'),
 S('c2-06-dang-tai','hs','Đang tải câu hỏi','Em thấy trạng thái đang tải.','Không để màn trắng khiến em tưởng bị lỗi.','Câu hỏi được tải về trước khi em bắt đầu.','.spin'),
 S('c2-06-luyen-ipad','hs','Làm bài trên iPad','Em xem đề, hình 3D và nhập đáp số bằng bàn phím toán.','Bàn phím riêng để gõ độ, căn, phân số mà không cần bàn phím chữ.','Máy kiểm chấm đáp số có sai số cô cho phép.','.kbd2'),
 S('c2-06-luyen-iphone','hs','Làm bài trên iPhone','Cùng bài, bố cục dọc, bàn phím toán nằm dưới.','Em chỉ có điện thoại vẫn làm được.','Cùng logic chấm với iPad.','.kbd2'),
 S('c2-06-sai-goi-y','hs','Sai thì nhận gợi ý','Em nhập 60° chưa đúng, được gợi ý không lộ đáp số.','Học bằng cách tự nghĩ lại.','Gợi ý lấy từ lời giải nhưng chỉ phần dẫn dắt; còn 1 lượt.','.mathin.bad,.chip.yel'),
 S('c2-06-het-luot','hs','Hết lượt','Hết 3 lượt, lời giải còn khóa tới 18:00 thứ Sáu; em xem gợi ý hoặc nhờ cô.','Giữ thời gian để cả lớp tự nghĩ trước.','Khóa lời giải ở máy chủ theo giờ cô đặt.','.btn'),
 S('c2-07-ket-qua','hs','Xem kết quả','Em xem từng câu đúng hay cần xem lại; không có điểm tổng.','Không xếp hạng để em không so sánh.','Giáo viên thấy chi tiết từng câu của em.','.chip.grn'),
 S('c2-06-mat-mang','hs','Trường hợp mất mạng','Mất mạng khi gửi, em được báo và đáp số được giữ để gửi lại.','Không mất bài khi wifi chập chờn.','Đáp số lưu tạm trên máy, gửi lại khi có mạng; lượt thử chỉ tính khi máy chủ nhận.','.banner,.uncn,.chip.red')]},
{id:'kb5',name:'KB5 · Giáo viên chữa bài',sum:'Bảng lớp × bài, “Cả lớp vướng ở đâu”, chữa bài sang Trình chiếu (lời giải vẫn khóa trên máy học sinh), xuất kết quả, lưu trữ lớp.',steps:[
 S('c3-04-ket-qua-lop','gv','Bảng lớp × bài','Cô xem từng em, từng đề; không có cột tổng và không xếp hạng.','Cô cần biết ai vướng ở đâu để dạy, không để chấm điểm.','Bảng tổng hợp từ bài làm đã gửi.','table'),
 S('c3-04-ket-qua-lop','ai','Cả lớp vướng ở đâu','AI gộp các bài làm thành nhận định, ví dụ nhiều em sai giao tuyến.','Tiết kiệm công đọc từng bài.','Chỉ gộp khi đủ 5 bài làm cho một đề (Q6); mỗi nhận định mở được bài làm căn cứ.','.card[style*="D9CCFF"]'),
 S('c3-04-ket-qua-lop','gv','Xem bài làm căn cứ','Cô mở bài làm của em Chi Mai ở Đề 3 để kiểm nhận định của AI.','AI có thể sai; cô xác nhận.','Bằng chứng gồm hình chụp thao tác và gợi ý em đã xin.','.cell.hl'),
 S('c3-06a-chua-bai-trinh-chieu','gv','Chữa bài sang Trình chiếu','Cô bấm “Chữa bài này” và thấy xem trước màn sẽ chiếu.','Chiếu thẳng đề em vướng, không soạn lại.','Lời giải vẫn khóa trên máy học sinh đến giờ đã đặt.','.btn'),
 S('c2-04-trinh-chieu','gv','Trình chiếu hiện từng bước','Cô bấm để hiện từng bước lời giải trên màn chiếu.','Cả lớp nghĩ từng bước.','Màn chiếu chỉ là màn của cô; máy học sinh không đổi.','.stpb.cur'),
 S('c2-04-che-loi-giai','gv','Che lời giải','Cô che lời giải để cả lớp tự nghĩ trước.','Đặt câu hỏi trước khi lộ đáp án.','Che chỉ ở màn chiếu; khóa thật nằm ở máy chủ.','.stpb.hid'),
 S('c3-06b-xuat-ket-qua','gv','Xuất kết quả','Cô xuất kết quả lớp ra tệp.','Lưu hồ sơ, báo cáo.','Tệp chỉ có mã người dùng, không có dữ liệu cá nhân.','.btn'),
 S('c3-06c-luu-tru-lop','gv','Lưu trữ lớp cuối năm','Cô lưu trữ lớp, dữ liệu chuyển sang chế độ chỉ xem.','Gọn danh sách lớp, giữ lại lịch sử.','Theo chính sách lưu và xóa dữ liệu bài làm.','.btn')]},
{id:'kb6',name:'KB6 · Học sinh tự học (không lớp)',sum:'Luyện một mình, chỉ có gợi ý, mã khôi phục khi quên mật khẩu.',steps:[
 S('c3-01-dang-ky','hs','Tự đăng ký bằng tên','Em chọn vai Học sinh, gõ tên, đặt mật khẩu.','Không cần lớp vẫn học được.','Cùng cơ chế đăng ký với giáo viên.','.cbx'),
 S('c3-01-ma-nguoi-dung','hs','Mã người dùng và mã khôi phục','Em nhận mã người dùng và mã khôi phục.','PO-66 G3: học sinh tự học cũng có mã khôi phục vì không có cô để nhờ đặt lại.','Mã khôi phục chỉ hiện một lần.','.code.lt','Màn này hiện vẽ cho giáo viên; bản dành cho học sinh tự học chưa vẽ lại.'),
 S('c2-06-luyen-ipad','hs','Luyện một mình','Em chọn đề từ Kho chung và làm với bàn phím toán.','Có việc để luyện ngay.','Máy kiểm chấm đáp số.','.kbd2'),
 S('c2-06-sai-goi-y','hs','Chỉ có gợi ý','Em sai và chỉ nhận gợi ý, không xem được lời giải.','PO-66 G2: học sinh tự học khóa lời giải để tự nghĩ.','Lời giải Kho chung chỉ mở cho giáo viên.','.chip.yel'),
 S('c3-01-dang-nhap','hs','Quên mật khẩu','Màn đăng nhập iPad; em tự học dùng mã khôi phục thay vì nhờ cô.','Không có email, mã khôi phục là cách duy nhất.','Màn hiện ghi “nhờ giáo viên của lớp” cho em có lớp.','.card'),
 S(null,'hs','Đặt mật khẩu mới','Em nhập mã khôi phục và đặt mật khẩu mới.','Lấy lại tài khoản không cần dữ liệu cá nhân.','Mã khôi phục dùng xong thì cấp mã mới.',null,'Màn nhập mã khôi phục / đặt mật khẩu mới chưa vẽ.')]},
{id:'kb7',name:'KB7 · Trình chiếu trên lớp',sum:'Giáo viên mở Trình chiếu, che lời giải, hiện từng bước, học sinh hỏi khi lời giải còn khóa, mất mạng khi đang chiếu.',steps:[
 S('c3-06a-chua-bai-trinh-chieu','gv','Mở Trình chiếu','Cô bấm “Chữa bài này” để mở Trình chiếu.','Chiếu thẳng đề đang cần chữa.','Màn chiếu tải sẵn đề và các bước.','.btn','Nút “Trình chiếu” mở từ màn Soát đề và Soạn bộ bài chưa vẽ; tạm mở từ kết quả lớp.'),
 S('c2-04-che-loi-giai','gv','Che lời giải','Cô che lời giải, cả lớp tự nghĩ.','Cho các em thử trước.','Che ở màn chiếu; khóa thật ở máy chủ.','.stpb.hid'),
 S('c2-04-trinh-chieu','gv','Hiện từng bước','Cô hiện từng bước và dùng bút, xoay hình.','Dẫn dắt từng ý.','Hình 3D xoay được; trạng thái bước chỉ nằm trên máy cô.','.stpb.cur'),
 S('c2-06-het-luot','hs','Học sinh hỏi lại khi lời giải còn khóa','Em hết lượt, lời giải còn khóa, chọn “Xem lại cùng thầy cô”.','Em có đường hỏi lại thay vì tự mở lời giải.','Yêu cầu của em gửi về cô; lời giải trên máy em vẫn khóa.','.btn.soft'),
 S('c2-04-trinh-chieu','gv','Mất mạng khi đang chiếu','Đang chiếu mà mất mạng vẫn chiếu tiếp được.','Lớp học không thể dừng vì wifi.','Đề và các bước đã tải sẵn trước khi chiếu.','text:Đã tải sẵn','Màn thông báo mất mạng riêng cho Trình chiếu chưa vẽ; dùng chip “Đã tải sẵn” trên màn chiếu.')]}
];
const BYID={};KBS.forEach(k=>BYID[k.id]=k);
let fs=false,playing=false,timer=null,cur=null;
const esc=s=>String(s==null?'':s).replace(/[&<>]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;'}[c]));
const roleL={gv:'Giáo viên',hs:'Học sinh',ai:'AI'};

function intro(){
 const N=(c,r,w,t,sub,col,kb)=>{const x=10+c*200,y=20+r*120;return `<a href="#kb=${kb}&amp;b=1"><rect x="${x}" y="${y}" width="${w||180}" height="78" rx="12" fill="#fff" stroke="${col}" stroke-width="2.5"/><text x="${x+12}" y="${y+25}" font-size="14" font-weight="800" fill="${col}">${esc(t)}</text>${sub.map((l,i)=>`<text x="${x+12}" y="${y+46+i*16}" font-size="12" fill="#44506B">${esc(l)}</text>`).join('')}</a>`;};
 const A=(x1,y1,x2,y2)=>`<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="#8F9BB5" stroke-width="2" marker-end="url(#ar)"/>`;
 const svg=`<svg viewBox="0 0 1000 352" role="img" aria-label="Sơ đồ vận hành"><defs><marker id="ar" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto"><path d="M0,0 L8,4 L0,8 z" fill="#8F9BB5"/></marker></defs>
 ${A(190,59,208,59)}${A(390,59,408,59)}${A(590,59,608,59)}${A(790,59,808,59)}${A(900,98,900,138)}${A(808,179,790,179)}${A(608,179,590,179)}${A(408,179,390,179)}${A(208,179,190,179)}
 ${N(0,0,0,'Giáo viên thêm đề',['dán chữ hoặc ảnh'],'#3B5BDB','kb2')}${N(1,0,0,'AI dựng hình',['và viết lời giải'],'#7C4DFF','kb2')}${N(2,0,0,'Máy kiểm đáp số',['lệch thì chặn duyệt'],'#9A6B00','kb2')}${N(3,0,0,'Giáo viên soát, duyệt',['sửa bằng chat'],'#3B5BDB','kb2')}${N(4,0,0,'Kho đề',['GV xem lời giải ngay'],'#F0522B','kb2')}
 ${N(4,1,0,'Bộ bài',['một mô hình chung','đặt giờ mở lời giải'],'#3B5BDB','kb3')}${N(3,1,0,'Lớp (mã lớp)',['GV duyệt từng em vào'],'#3B5BDB','kb1')}${N(2,1,0,'Học sinh làm bài',['iPad/iPhone, chỉ gợi ý','lời giải khóa theo giờ'],'#0F8A5F','kb4')}${N(1,1,0,'Kết quả lớp',['bảng lớp × bài','“cả lớp vướng ở đâu”'],'#9A6B00','kb5')}${N(0,1,0,'Chữa bài',['Trình chiếu trên lớp'],'#3B5BDB','kb7')}
 ${N(0,2,380,'Mọi người đăng ký bằng tên',['nhận mã người dùng + mật khẩu tự đặt','không thu dữ liệu cá nhân'],'#3B5BDB','kb1')}${N(2,2,580,'Học sinh tự học (không lớp)',['lấy đề từ Kho chung, chỉ xem gợi ý, lời giải khóa','có mã khôi phục khi quên mật khẩu'],'#0F8A5F','kb6')}
 </svg>`;
 const rules=[['Một mô hình “bộ bài”','Luyện và bài tập dùng chung một cấu trúc (PO-66 G1).'],['Giáo viên xem lời giải ngay','Lời giải Kho chung mở ngay cho giáo viên; học sinh tự học chỉ có gợi ý (G2).'],['Lời giải khóa theo giờ','Khóa ở máy chủ tới giờ cô đặt, máy học sinh không giữ lời giải.'],['Máy kiểm đáp số','AI viết lời giải, máy tính lại đáp số; lệch thì không cho duyệt.'],['Mã khôi phục','Cả giáo viên và học sinh tự học đều có mã khôi phục (G3).'],['Duyệt từng em vào lớp','Chống dò mã lớp bằng giới hạn lượt thử và giáo viên duyệt (G4).']];
 return `<div class="kbintro"><h1>Cách hệ thống vận hành</h1><p class="lead">Bấm vào một ô trong sơ đồ hoặc chọn một kịch bản bên dưới để đi từng bước. Mỗi bước mở đúng màn bản mẫu và giải thích ai đang làm gì, vì sao, hệ thống làm gì phía sau.</p><div class="kbfig">${svg}</div>
 <div class="kbrules">${rules.map(r=>`<div><b>${r[0]}</b>${r[1]}</div>`).join('')}</div>
 <h2>Chọn kịch bản</h2><div class="kblist">${KBS.map(k=>`<a href="#kb=${k.id}&amp;b=1"><b>${esc(k.name)}</b><span>${esc(k.sum)}</span><br><i>${k.steps.length} bước ›</i></a>`).join('')}</div></div>`;
}
function route(h){
 if(h===''||/^kb=/.test(h)){
  const p=new URLSearchParams(h.replace(/^kb=/,'kb='));const kb=p.get('kb')||'intro';
  const kbo=BYID[kb];
  if(!kbo){return {intro:true,id:null,html:intro(),title:'Cách hệ thống vận hành',ready:'kb-intro',fs:false};}
  let b=parseInt(p.get('b')||'1',10);if(!(b>=1))b=1;if(b>kbo.steps.length)b=kbo.steps.length;
  const st=kbo.steps[b-1];
  return {kb,b,kbo,st,id:st.s,fs:p.get('fs')==='1',title:kbo.name+' · bước '+b,ready:st.s||('kb-'+kb+'-'+b),
   html:`<div class="kbmiss"><b>Màn này chưa vẽ</b>${esc(st.m||'')}<br><br>${esc(st.t)}: ${esc(st.a)}</div>`};
 }
 return null;
}
function go(kb,b){location.hash='kb='+kb+'&b='+b;}
function step(d){const K=cur;if(!K||!K.kbo)return;const n=K.b+d;if(n<1||n>K.kbo.steps.length)return;go(K.kb,n);}
function noteHtml(K){const st=K.st;return `<div class="h"><span class="role ${st.w}">${roleL[st.w]}</span>${esc(st.t)}</div><div class="c"><b>ĐANG LÀM GÌ</b>${esc(st.a)}</div><div class="c"><b>VÌ SAO</b>${esc(st.y)}</div><div class="c"><b>HỆ THỐNG LÀM GÌ PHÍA SAU</b>${esc(st.b)}</div>${st.m?`<div class="miss">Chưa vẽ: ${esc(st.m)}</div>`:''}`;}
function nav(el,id,v,grps,K){
 cur=K;
 const mini=LS.get('mini')==='1',menu=LS.get('menu')==='1',nmin=LS.get('note')==='min';
 el.className='s23nav'+(mini?' mini':'');
 const label=K?(K.intro?'Cách hệ thống vận hành':K.kbo.name):(v?v.label:'');
 let h=`<div class="nv1"><b><span class="lg">Bản mẫu chặng 2–3 (S23)</span><span class="sh">S23</span></b><span class="cur">${esc(label)}</span><button data-nv="menu">Danh sách màn ▾</button><button class="go" data-nv="kb">▶ Kịch bản</button><button class="tg" data-nv="mini" title="Thu / mở thanh">${mini?'S23 ▾':'▴'}</button></div>`;
 h+=`<div class="nvm${menu&&!mini?' open':''}"><div class="grp"><span class="g">Kịch bản</span><a href="#kb=intro" class="${K&&K.intro?'on':''}">Cách vận hành</a>${KBS.map(k=>`<a href="#kb=${k.id}&amp;b=1" class="${K&&K.kb===k.id?'on':''}">${esc(k.name)}</a>`).join('')}</div>`+Object.keys(grps).map(g=>`<div class="grp"><span class="g">${g}</span>`+grps[g].map(k=>`<a href="#${k}" class="${!K&&k===id?'on':''}">${V[k].label}</a>`).join('')+'</div>').join('')+`<div class="grp"><a href="index.html">← Bản mẫu chính</a></div></div>`;
 if(K&&K.kbo){h+=`<div class="kbbar${nmin?' min':''}"><div class="kbtop"><span class="nm">${esc(K.kbo.name)}</span><span class="cnt">Bước ${K.b}/${K.kbo.steps.length}</span><div class="kbprog"><i style="width:${Math.round(K.b/K.kbo.steps.length*100)}%"></i></div><button data-nv="prev" ${K.b<=1?'disabled':''}>‹ Trước</button><button class="nx" data-nv="next" ${K.b>=K.kbo.steps.length?'disabled':''}>Tiếp ›</button><button data-nv="fs">⛶ Trình chiếu</button><button data-nv="note">${nmin?'Mở chú thích ▾':'Thu chú thích ▴'}</button></div><div class="kbnote">${noteHtml(K)}</div></div>`;}
 el.innerHTML=h;
}
function setFs(on){
 if(on===fs)return;fs=on;
 document.body.classList.toggle('kbfs',fs);
 clearInterval(timer);
 if(fs){playing=false;timer=setInterval(()=>{if(playing){if(cur&&cur.kbo&&cur.b<cur.kbo.steps.length)step(1);else playing=false;paintFs();}},6000);
  try{const r=document.documentElement.requestFullscreen&&document.documentElement.requestFullscreen();if(r&&r.catch)r.catch(()=>{});}catch(e){}}
 else{try{if(document.fullscreenElement)document.exitFullscreen();}catch(e){}if(cur&&cur.fs){cur.fs=false;try{history.replaceState(null,'','#kb='+cur.kb+'&b='+cur.b);}catch(e){}}}
 after(cur,cur&&cur.id?V[cur.id]:null);
}
function paintFs(){
 let b=document.getElementById('kbfsbar');if(!b){b=document.createElement('div');b.id='kbfsbar';b.className='kbfsbar';document.body.appendChild(b);}
 const K=cur;if(!K||!K.kbo){b.innerHTML='';return;}const st=K.st;
 b.innerHTML=`<div class="pg" style="width:${Math.round(K.b/K.kbo.steps.length*100)}%"></div><div class="st">${K.kbo.name} · Bước ${K.b}/${K.kbo.steps.length} · ${playing?'đang chạy (Space: dừng)':'đã dừng (Space: chạy)'} · ←→ chuyển · Esc thoát</div><div class="t"><span class="role ${st.w}">${roleL[st.w]}</span>${esc(st.t)}</div><div class="x"><div><b>Đang làm gì:</b> ${esc(st.a)}</div><div><b>Vì sao:</b> ${esc(st.y)}</div><div><b>Phía sau:</b> ${esc(st.b)}</div></div>${st.m?`<div style="color:#FFE2A6;font-weight:700">Chưa vẽ: ${esc(st.m)}</div>`:''}`;
}
function after(K,v){
 cur=K;
 const app=document.getElementById('app');
 if(K&&K.kbo&&K.fs&&!fs){setFs(true);return;}
 if(fs&&(!K||!K.kbo)){fs=false;document.body.classList.remove('kbfs');clearInterval(timer);}
 document.body.dataset.kb=K&&K.kbo?K.kb+':'+K.b:'';
 let hl=0;
 if(v&&app){
  // nút Trình chiếu / Chữa bài mở màn trình chiếu
  app.querySelectorAll('.btn,.chip,.tb span').forEach(e=>{const t=(e.textContent||'').trim();if(t.length>40||e.children.length>6)return;
   if(/^Chữa bài|Trình chiếu|^Chiếu/.test(t)&&!(/^c2-04-/.test(cur&&cur.id||'')&&e.closest('.tb')&&/^Trình chiếu$/.test(t))){e.dataset.go='c2-04-trinh-chieu';}
   else if(/^(Đang che|Che) lời giải$/.test(t)&&e.closest('.tb')){e.dataset.go=/^Đang/.test(t)?'c2-04-trinh-chieu':'c2-04-che-loi-giai';}});
  if(K&&K.st&&K.st.h){
   let el=null;const h=K.st.h;
   try{if(h.indexOf('text:')===0){const t=h.slice(5);el=[...app.querySelectorAll('.chip,.btn,span,div')].filter(e=>e.children.length<4&&(e.textContent||'').indexOf(t)>=0).pop();}
   else el=app.querySelector(h);}catch(e){}
   if(el){el.classList.add('kbhl');hl=1;if(!fs){try{el.scrollIntoView({block:'nearest',inline:'nearest'});}catch(e){}}}
  }
  document.body.dataset.hl=String(hl);
 }
 if(fs){
  paintFs();
  const s=app.querySelector('.screen');
  if(s&&v){const [W,H]=DEVS[v.dev],aw=innerWidth,ah=innerHeight-118,sc=Math.min(aw/(W+30),ah/(H+30));s.style.transform=`translateX(-50%) scale(${sc})`;s.style.top=Math.max(0,(ah-H*sc)/2)+'px';}
 }
}
document.addEventListener('click',e=>{
 const g=e.target.closest('[data-go]');if(g&&document.getElementById('app').contains(g)){location.hash=g.dataset.go;return;}
 const n=e.target.closest('[data-nv]');if(!n)return;const a=n.dataset.nv;
 if(a==='menu'){LS.set('menu',LS.get('menu')==='1'?'0':'1');LS.set('mini','0');rerender();}
 else if(a==='mini'){LS.set('mini',LS.get('mini')==='1'?'0':'1');rerender();}
 else if(a==='kb'){LS.set('menu','0');location.hash='kb=intro';rerender();}
 else if(a==='prev')step(-1);else if(a==='next')step(1);
 else if(a==='note'){LS.set('note',LS.get('note')==='min'?'0':'1'.replace('1','min'));rerender();}
 else if(a==='fs')setFs(true);
});
document.addEventListener('click',e=>{if(e.target.closest('.nvm a')){LS.set('menu','0');setTimeout(rerender,0);}},true);
function rerender(){if(typeof show==='function')show();}
document.addEventListener('keydown',e=>{
 const t=e.target;if(t&&/INPUT|TEXTAREA/.test(t.tagName))return;
 if(!cur||!cur.kbo)return;
 if(e.key==='ArrowRight'){step(1);}else if(e.key==='ArrowLeft'){step(-1);}
 else if(fs&&e.key===' '){e.preventDefault();playing=!playing;paintFs();}
 else if(fs&&e.key==='Escape'){setFs(false);}
});
document.addEventListener('fullscreenchange',()=>{if(!document.fullscreenElement&&fs)setFs(false);});
window.S23K={route,nav,after,KBS,setFs,intro};
})();
