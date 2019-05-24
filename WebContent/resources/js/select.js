$(function(){
	//셀렉트박스 작동
	$(".tbl-select, .select, .tbl-info-select").each(function(){
		$(this).children("select").change(function(){
			var option = $(this).children("option:selected").text();
			$(this).siblings("label").text(option);
		});
	});

	//datepicker 한글화
	$(".datepicker").datepicker({altFormat: "yy.mm.dd"});
	$.datepicker.setDefaults({
				dateFormat: 'yy.mm.dd',
				prevText: '이전 달',
				nextText: '다음 달',
				monthNames: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
				monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
				dayNames: ['일', '월', '화', '수', '목', '금', '토'],
				dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
				dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
				showMonthAfterYear: true,
				yearSuffix: '년'
		});
});

$(document).ready(function(){
	var fileTarget = $('.inpFile .typeFile');

	fileTarget.on('change', function(){
		if(window.FileReader){
			var filename = $(this)[0].files[0].name;
		} else {
			var filename = $(this).val().split('/').pop().split('\\').pop();
		}

		$(this).siblings('.fileText').val(filename);
	});
});

//2017-02-20 추가
$(function(){
	$("td input").click(function(){
		$(this).parents("tr").toggleClass("on");
	});
});
