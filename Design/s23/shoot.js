// shoot.js · Phiên bản: v0.3 · 2026-11-26 (Dom: dùng s23-screens-c.html = màn cũ + màn mới) · chụp ảnh các màn S23.
// Chạy: node Design/s23/shoot.js [id...]  (dùng playwright-core của app/e2e; chỉ đọc, không sửa app/)
const path=require('path');
const PW=process.env.PW_CORE||path.resolve(__dirname,'../../app/e2e/node_modules/playwright-core'); // worktree thiếu node_modules → đặt PW_CORE
const {chromium}=require(PW);
const root=path.resolve(__dirname,'..');
const page_url='file://'+path.join(root,'prototype','s23-screens-c.html');
(async()=>{
 const b=await chromium.launch();const ctx=await b.newContext({deviceScaleFactor:1,viewport:{width:1500,height:960}});
 const p=await ctx.newPage();const errs=[];p.on('pageerror',e=>errs.push(e.message));
 await p.goto(page_url+'?cap=1#c2-02-nhap',{waitUntil:'domcontentloaded'});
 let ids=await p.evaluate(()=>Object.keys(V));
 if(process.argv.length>2)ids=process.argv.slice(2);
 for(const id of ids){
  await p.goto(page_url+'?cap=1#'+id,{waitUntil:'domcontentloaded'});await p.reload({waitUntil:'domcontentloaded'});
  await p.waitForFunction(i=>document.body.dataset.ready===i,id);
  await p.evaluate(()=>document.fonts&&document.fonts.ready);
  await p.addStyleTag({content:'*{animation:none!important;transition:none!important}.spin{border-top-color:#3B5BDB}'});
  const dev=await p.evaluate(i=>V[i].dev,id);
  const [w,h]={web:[1440,900],ipad:[1180,820],phone:[390,844]}[dev];
  await p.setViewportSize({width:w+20,height:h+20});
  await p.locator('.screen').first().screenshot({path:path.join(__dirname,id+'.png')});
  console.log(id,dev);
 }
 if(errs.length)console.log('LỖI JS:',errs);
 await b.close();
})();
