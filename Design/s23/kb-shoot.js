// kb-shoot.js · Phiên bản: v0.1 · 2026-11-26 (T-261126-38) · chụp nav gọn, menu, trang mở đầu, bước 1 mỗi kịch bản, trình chiếu; đi hết mọi bước kiểm pageerror.
// Chạy: PW_CORE=<đường dẫn playwright-core> node Design/s23/kb-shoot.js   (mặc định như shoot.js)
const path=require('path'),fs=require('fs');
const PW=process.env.PW_CORE||path.resolve(__dirname,'../../app/e2e/node_modules/playwright-core');
const {chromium}=require(PW);
const proto=path.resolve(__dirname,'..','prototype');
const out=path.join(__dirname,'kb');fs.mkdirSync(out,{recursive:true});
const URLS={c:'file://'+path.join(proto,'s23-screens-c.html'),a:'file://'+path.join(proto,'s23-screens.html')};
(async()=>{
 const b=await chromium.launch();const log=[];let bad=0;
 const mk=async(w,h)=>{const ctx=await b.newContext({deviceScaleFactor:1,viewport:{width:w,height:h}});const p=await ctx.newPage();p.errs=[];p.on('pageerror',e=>p.errs.push('pageerror: '+e.message));p.on('console',m=>{if(m.type()==='error'&&!/Failed to load resource|ERR_/.test(m.text()))p.errs.push('console: '+m.text());});return p;};
 const open=async(p,u,hash)=>{await p.goto(u+hash,{waitUntil:'domcontentloaded'});await p.reload({waitUntil:'domcontentloaded'});await p.waitForFunction(()=>document.body.dataset.ready);await p.evaluate(()=>document.fonts&&document.fonts.ready);await p.addStyleTag({content:'*{animation:none!important;transition:none!important}'});};
 const p=await mk(1440,900);
 // 1) kiểm cả hai trang: mở đầu + 7 kịch bản đi hết bước
 for(const key of ['c','a']){
  await open(p,URLS[key],'');
  const nh=await p.evaluate(()=>document.querySelector('.nv1').getBoundingClientRect().height);
  log.push(`[${key}] mở đầu OK · cao thanh nav ${nh}px`);
  const kbs=await p.evaluate(()=>S23K.KBS.map(k=>[k.id,k.steps.length]));
  for(const [id,n] of kbs){
   let hl=0,miss=0;
   for(let i=1;i<=n;i++){
    await p.evaluate(h=>{location.hash=h;},`kb=${id}&b=${i}`);
    await p.waitForFunction(v=>document.body.dataset.kb===v,`${id}:${i}`);
    const r=await p.evaluate(()=>({hl:document.body.dataset.hl,miss:!!document.querySelector('.kbmiss'),ready:document.body.dataset.ready}));
    if(r.hl==='1')hl++;if(r.miss)miss++;
    if(!r.miss){const ok=await p.evaluate(i=>!!V[document.body.dataset.ready],i);if(!ok){log.push(`  LỖI ${id} b${i}: màn không có trong V`);bad++;}}
   }
   log.push(`[${key}] ${id}: ${n} bước đi hết · tô sáng ${hl}/${n} · chưa vẽ ${miss}`);
  }
  // nút Trình chiếu bấm được
  await open(p,URLS[key],'#c3-06a-chua-bai-trinh-chieu');
  const n=await p.evaluate(()=>document.querySelectorAll('#app [data-go]').length);
  await p.evaluate(()=>document.querySelector('#app [data-go]').click());
  await p.waitForTimeout(150);
  log.push(`[${key}] nút Trình chiếu/Chữa bài gắn liên kết: ${n} · sau bấm hash=${await p.evaluate(()=>location.hash)}`);
  // chế độ trình chiếu: chạy tự động
  await open(p,URLS[key],'#kb=kb1&b=1&fs=1');
  await p.waitForFunction(()=>document.body.classList.contains('kbfs'));
  await p.keyboard.press('ArrowRight');await p.waitForFunction(()=>document.body.dataset.kb==='kb1:2');
  await p.keyboard.press(' ');await p.keyboard.press('Escape');
  await p.waitForFunction(()=>!document.body.classList.contains('kbfs'));
  log.push(`[${key}] trình chiếu: vào, ←→, Space, Esc thoát OK`);
 }
 // 2) ảnh bằng chứng (trang -c)
 await open(p,URLS.c,'');await p.screenshot({path:path.join(out,'01-nav-gon.png')});
 await p.evaluate(()=>document.querySelector('[data-nv=menu]').click());await p.waitForTimeout(100);await p.screenshot({path:path.join(out,'02-menu-mo.png')});
 await p.evaluate(()=>{try{localStorage.clear();}catch(e){}});
 await open(p,URLS.c,'#kb=intro');await p.screenshot({path:path.join(out,'03-mo-dau.png'),fullPage:true});
 for(let i=1;i<=7;i++){await open(p,URLS.c,`#kb=kb${i}&b=1`);await p.screenshot({path:path.join(out,`04-kb${i}-b1.png`)});}
 await open(p,URLS.c,'#kb=kb4&b=3&fs=1');await p.waitForFunction(()=>document.body.classList.contains('kbfs'));await p.keyboard.press(' ');await p.screenshot({path:path.join(out,'05-trinh-chieu.png')});
 const q=await mk(390,844);await open(q,URLS.c,'#kb=kb4&b=5');await q.screenshot({path:path.join(out,'06-kb4-iphone.png')});
 log.push('iPhone nav cao '+await q.evaluate(()=>document.querySelector('.nv1').getBoundingClientRect().height)+'px');
 const errs=[...p.errs,...q.errs];
 log.push(errs.length?'LỖI CONSOLE: '+errs.join(' | '):'Không có pageerror/console error ở mọi trang đã mở');
 console.log(log.join('\n'));
 await b.close();process.exit(errs.length||bad?1:0);
})().catch(e=>{console.error(e);process.exit(2);});
