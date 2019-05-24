//공통 페이징 처리
function fn_page(paging_div_class, ctx, isPopup){
	fn_setDefaultValueSelRowSize();
	
	if(!paging_div_class)
	{
		paging_div_class = "j_page_div";
	}
	if(ctx == null){
		ctx = "";
	}
	$("."+paging_div_class).each(function(){
		var now = Number($(this).find("input[name='j_now']").val());
		var tot = Number($(this).find("input[name='J_tot']").val());
		var row = Number($(this).find("input[name='j_row']").val());
		var page = Number($(this).find("input[name='j_page']").val());
		var event = $(this).find("input[name='j_event']").val();
		if(isPopup != true){
			var spaTotCnt = document.getElementById('spaTotCnt');
			if(spaTotCnt != null && !isNaN(tot)){
				spaTotCnt.textContent = tot + " 개";
			}
		}
		
		
		if(tot > 0){
			if(now == ""){
				now = 1;
			}
			
			var pageCount = Math.ceil(tot/row)
			if( now > pageCount){
				now = pageCount;
			}
			
			var start = (now - 1) * row + 1;
			var end = now * row;
			
			var number = tot - (now - 1) * row;
			
			var startPage = Math.floor(now/page) *  page +  1;
			
			if(now%page == 0) {
				startPage -= page;
			}
			
			var endPage = startPage + page - 1;
			
			if(endPage > pageCount){
				endPage = pageCount;
			}

			var str = "<div class='pager'>";
			
			if( 1 < startPage){
				//str += "<a href='javascript:" + event + "(" + 1 + ")'><img src='"+ctx+"/resources/img/btn/paging2_prev2.gif' alt='처음 목록' /></a>";
				str += "<a href='javascript:" + event + "(" + (startPage - page ) + ")' class='prev'></a>";
			} else {
				
				//str += "<img src='"+ctx+"/resources/img/btn/paging_prev2.gif' alt='처음 목록 없음' />";
				//str += "<img src='"+ctx+"/resources/img/btn/paging_prev.gif' alt='이전 목록 없음' class='mar_10' />";
			}
			
			str += "<div class='num'>";
			for(var i = startPage; i <= endPage; i++){
				//if(i != startPage){
				//	str += "<span>|</span>";
				//}
				if(i == now){
					str += "<a class='current' href='javascript:" + event + "(" + i + ")' >" + i + "</a>";
				} else {
					str += "<a href='javascript:" + event + "(" + i + ")'>" + i + "</a>";
				}
			}
			str += "</div>";
			
			if(endPage < pageCount){
				str += "<a href='javascript:"+ event + "(" + (endPage + 1) + ")' class='next'></a>";
				//str += "<a href='javascript:"+ event + "(" + pageCount + ")'><img src='"+ctx+"/resources/img/btn/paging2_next2.gif' alt='마지막 목록' /></a>";
			} else {
				//str += "<img src='"+ctx+"/resources/img/btn/paging_next.gif' alt='다음 목록 없음' class='mal_10' />";
				//str += "<img src='"+ctx+"/resources/img/btn/paging_next2.gif' alt='마지막 목록 없음' />";
			}
			str += "</div>";

			$(this).html(str);
		}
	});
}

//설정파일에 설정된 조회 ROW 삽입.
function fn_setDefaultValueSelRowSize(){
	var select = document.getElementById('selRowSize');
	if(select != null){
		//설정된 기본 ROW개수의 option 생성.
		var newOption = document.createElement('option');
		newOption.value =  INIT_ROW_CNT;
		newOption.text = INIT_ROW_CNT + "개씩 보기";
		
		var selectedIdx = select.selectedIndex;
		
		select.value = INIT_ROW_CNT;
		if (select.value !== INIT_ROW_CNT ){
			select.options.add(newOption, select.options[0]);
		}
		select.selectedIndex = selectedIdx;
		
		fn_commonSetRowSize();
	}
}

// 그리드 상단의 조회 ROW 변경시 호출.
function fn_commonSetRowSize(){
	var select = document.getElementById('selRowSize');
	var pageSize = document.getElementById('PAGE_SIZE');
	var jRow = document.getElementsByName('j_row');
	if(select != null && pageSize != null){
		pageSize.value = select.value;
	}
	if(select != null && jRow != null){
		document.getElementsByName('j_row').value = select.value;
	}
}

//마스크를 생성해 전체화면을 덮고, 애니메이션 효과를 부여한 후 팝업창을 출력 
function wrapMask(seq) {
	// 화면에 너비와 너비를 구함
	var width  = $(document).width();
	var height = $(document).height();
	// 링크 없애기
	$("#layerpopup"+(seq != null ? seq : "")+" .uplink").hide();
	// 마스크를 화면의 높이와 너비로 만들어 전체화면에 덮기 및 애니매이션 효과
	$("#layerpopup"+(seq != null ? seq : "")+" .mask"+(seq != null ? seq : "")).css({'width':width,'height':height});
	$("#layerpopup"+(seq != null ? seq : "")+" .mask"+(seq != null ? seq : "")).fadeTo("fast", 0.2);
}

function centerWindow(seq){
	var left   = ( $(window).scrollLeft() + ($(window).width() -  $("#layerpopup"+(seq != null ? seq : "")+" .window"+(seq != null ? seq : "")).width()) / 2 );
	var top    = ( $(window).scrollTop()  + ($(window).height() - $("#layerpopup"+(seq != null ? seq : "")+" .window"+(seq != null ? seq : "")).height()) / 2 );
	$("#layerpopup"+(seq != null ? seq : "")+" .window"+(seq != null ? seq : "")).css({'left':left,'top':top, 'position':'absolute'});
	var width  = $("#layerpopup"+(seq != null ? seq : "")+" .window"+(seq != null ? seq : "")).width();
	var height = $("#layerpopup"+(seq != null ? seq : "")+" .window"+(seq != null ? seq : "")).height();

	if(seq != null){
		$(".lightbox"+seq).css({'width':width,'height':height,"margin":"0px 0px 0px 0px"});
	}else{
		$(".lightbox").css({'width':width,'height':height,"margin":"-" + (height/2) + "px 0 0 -"+ (width/2) +"px"});
	}
}


//공통코드 리스트 페이지 호출(공통)
function fn_getCommonList( url, type, data, target, dataType ){
	
	if(!dataType)
	{
		dataType = "html";
	}
	var AjaxHTML = $.ajax({
							url: url ,
							type: type ,
							data: data ,
							dataType: dataType ,
							async: false ,
							cache:false
						}).responseText;	
	if(target)
	{
		target.html(AjaxHTML);
//        target[0].innerHTML=AjaxHTML;
	}
}

//height auto 자동크기 지정
function fn_getCommonPopupDiv (url, type, data, target , X , title ){
	
	target.dialog({
		autoOpen: false,
		modal   : true,
		width   : X,
		height  : "auto",
		title   : title,
		open    : function(){ fn_commondialogDesign(target.attr("id")); }//타이틀 및 닫기버튼디자인적용(ID전달 필요)
	});

	fn_getCommonList( url , type, data, target );
	
	//열기
	target.dialog('open');
}

//height 선택 지정
function fn_getCommonPopupDivY (url, type, data, target , X , Y , title ){
	
	target.dialog({
		autoOpen: false,
		modal   : true,
		width   : X,
		height  : Y,
		title   : title,
		open    : function(){ fn_commondialogDesign(target.attr("id")); }//타이틀 및 닫기버튼디자인적용(ID전달 필요)
	});
	
	fn_getCommonList( url , type, data, target );
	
	//열기
	target.dialog('open');

}

function fn_commondialogDesign(uid) { 	

	var obj = jQuery('div[aria-describedby="'+uid+'"]');	//해당팝업객체 ID
	var titlebar = jQuery('.ui-dialog-titlebar'      ,obj);
	jQuery(".ui-dialog-titlebar-close",obj).remove();		//디폴트 close 제거 후 이벤트객체 추가
	
	// 닫기 버튼이 무한대로 추가 되는 문제 수정
	if(jQuery(titlebar).find("img").length==0){
		jQuery(titlebar).append('<img src="' + RESOURCES_PATH + '/images/common/layer_close.gif" style="position:absolute;cursor:pointer;top: 1.5em;right: 1em;" onclick="fn_closePopup();">');
	}
}

//마스크 및 파업창 닫기
function fn_closePopup(){
	jQuery(".ui-dialog-content").dialog('close');
}

function fc_chk_byte(ls_str) {
	
	var ari_max=length;
	var li_str_len = ls_str.length; // 전체길이
	
	// 변수초기화 
	var li_max = ari_max; // 제한할 글자수 크기 
	var i = 0;     // for문에 사용 
	var li_byte = 0;  // 한글일경우는 2 그밗에는 1을 더함 
	var ls_one_char = ""; // 한글자씩 검사한다 
	
	for(i=0; i< li_str_len; i++) 
	{ 
		// 한글자추출 
		ls_one_char = ls_str.charAt(i); 
		
		// 한글이면 2를 더한다. 
		if (escape(ls_one_char).length > 4) { 
			li_byte += 2; 
		}else{   // 그밖의 경우는 1을 더한다. 
			li_byte++; 
		} 
		// 전체 크기가 li_max를 넘지않으면 
		if(li_byte <= li_max){ 
			li_len = i + 1; 
		} 
	} 
	
	return li_byte;
}

//숫자만을 기입받게 하는 방법
function onlyNumber() {
	if ( !window.event.shiftKey ) {
		if ( (event.keyCode < 1 || event.keyCode > 31) 
		&& (event.keyCode < 37 || event.keyCode > 40) 
		&& (event.keyCode != 46)
		&& (event.keyCode < 48 || event.keyCode > 57) 
		&& (event.keyCode < 96 || event.keyCode > 105) 
		&& (event.keyCode != 116) ) {
			event.keyCode = 10;
			event.returnValue=false;
		}
	} else {
		event.returnValue=false; 
	}
}

/**
 * 특수문자 입력 제한하기 html 
 * 
 */
function fnFilter(txt) {
	var chktext = /[\/?|\(\)~`!^%\'\"<>]/gi;
	//정규식 구문
	if (chktext.test(txt)) {
	    alert("특수문자는 입력하실 수 없습니다.");
		return false;
	}
	return true;
};

//숫자 체크 false true
function isNumber(s) {
	s += ''; // 문자열로 변환
	s = s.replace(/^\s*|\s*$/g, ''); // 좌우 공백 제거
	if (s == '' || isNaN(s)) return false;
	return true;
}