document.addEventListener('DOMContentLoaded',async()=>{
 const el=$('#home-deals');if(!el)return;
 let list=[],isDemo=false;
 try{list=await api('/deals');if(!Array.isArray(list))list=list?.content||[]}catch(err){console.error('딜 조회 실패:',err);el.innerHTML='<div class="notice">실제 딜 조회 실패: '+escapeHtml(err.message)+'</div>';return;}
 if(!list.length){el.innerHTML='<div class="empty">현재 등록된 타임딜이 없어요. 다음 타임딜을 기대해 주세요!</div>';return}
 if(isDemo){el.insertAdjacentHTML('beforebegin','<div class="demo-notice">디자인 미리보기 · 백엔드가 연결되면 실제 딜로 자동 교체됩니다.</div>')}
 el.innerHTML=list.slice(0,8).map(dealCard).join('');
 const d=list.find(x=>dealStatus(x)==='ACTIVE')||list[0];
 if(d){$('#hero-name').textContent=d.productName||'오늘의 타임딜';$('#hero-price').textContent=won(d.dealPrice);$('#hero-discount').textContent=d.originalPrice?Math.round((1-d.dealPrice/d.originalPrice)*100)+'%':'특가';$('#hero-icon').innerHTML=art(d);const id=d.demo?null:dealId(d);if(id!=null)$('#hero-link').href='deal-detail.html?id='+encodeURIComponent(id);else $('#hero-link').href='deals.html'}
});
