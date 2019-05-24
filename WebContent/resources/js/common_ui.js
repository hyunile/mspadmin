/* 
 * ##################################################################################################
 * ##
 * ##   *********************************************************************************************************
 * ##    *******************************************************************************************************
 * ##     **
 * ##      **						UI 핸들링에 필요한 함수 모음
 * ##     **
 * ##    *******************************************************************************************************
 * ##   *********************************************************************************************************
 * ##
 * ##################################################################################################
 */

//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_charAt_chosung
//
//		
//			설명 : 
//						문자의 초성 을 구한다.
//
//
//			인수 : 
//						a
//							- 검사할 문자
//
//			반환값 : 
//						입력된 문자의 초성 문자
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var c = fn_charAt_jongsung("수")
//									=> c : "ㅅ"
//							
//								var c = fn_charAt_jongsung("희")
//									=> c : "ㅎ"
//
//								var c = fn_charAt_jongsung("강")
//									=> c : "ㄱ"
//								
// ------------------------------------------------------------------------------------------------------
String.prototype.fn_charAt_chosung = function()
{
	var r = ((this.charCodeAt(this.length-1) - parseInt('0xac00',16)) /28) / 21;
	var t = String.fromCharCode(r + parseInt('0x1100',16));
	return t;
};
 

//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_charAt_jungsung
//
//		
//			설명 : 
//						문자의 중성 을 구한다.
//
//
//			인수 : 
//						a
//							- 검사할 문자
//
//			반환값 : 
//						입력된 문자의 중성 문자
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var c = fn_charAt_jongsung("수")
//									=> c : "ㅜ"
//							
//								var c = fn_charAt_jongsung("희")
//									=> c : "ㅢ"
//
//								var c = fn_charAt_jongsung("강")
//									=> c : "ㅏ"
//								
// ------------------------------------------------------------------------------------------------------
String.prototype.fn_charAt_jungsung = function()
{
	var r = ((this.charCodeAt(this.length-1)- parseInt('0xac00',16)) / 28) % 21;
	var t = String.fromCharCode(r + parseInt('0x1161',16));
	return t;
};
//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_charAt_jongsung
//
//		
//			설명 : 
//						문자의 종성을 구한다.
//
//
//			인수 : 
//						a
//							- 검사할 문자
//
//			반환값 : 
//						입력된 문자의 종성 문자
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var c = "수".fn_charAt_jongsung();
//									=> c : ""
//							
//								var c = fn_charAt_jongsung("철순")
//									=> c : "ㄴ"
//								
// ------------------------------------------------------------------------------------------------------
String.prototype.fn_charAt_jongsung = function()
{
	var r = (this.charCodeAt(0) - parseInt('0xac00',16)) % 28;
	var t = String.fromCharCode(r + parseInt('0x11A8') -1);
	return t;
};




//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_haveJongSungSpk
//
//		
//			설명 : 
//						마지막 글씨에 종성이 있는 문장인지 검사
//
//
//			인수 : 
//						str
//							- 검사할 문장
//
//			반환값 : 
//						종성 있는지 여부
//						true - 종성 있음
//						false - 종성 없음
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var en = fn_haveJongSungSpk("철수")
//									=> en : false
//							
//								var en = fn_haveJongSungSpk("철순")
//									=> en : true
//								
//------------------------------------------------------------------------------------------------------
String.prototype.fn_haveJongSungSpk = function()
{
	// 마지막 문자에 받침이 있으면 "은", 없으면 "는" 을 반환 한다.
	var lastChar = this.charAt(this.length-1);
	var retval = false;
	switch(lastChar)
	{
	case "0": 	// "0"의 경우는 좀 특별하지만, 1조 단위가 아니라면 모두 받침이 있는 발음임...
	case "1": case "3": case "6": case "7": case "8":
	{
		// 받침 있는 발음
		retval = true;
	}
	break;
	case "2": case "4": case "5": case "9":
		{
			//받침 없는 발음
			retval = false;
		}
		break;
	default:
		{
			if(lastChar.fn_charAt_jongsung() == "ᆧ")		// 공백 아님.. 수정시 주의 할것!!
				{
					retval = false;
				}
			else
				{
					retval = true;
				}
		}
		break;
	}
	
	return retval;
};


// ------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_getEunNeun
//
//		
//			설명 : 
//						마지막 문자를 가지고 "은"/"는" 을 구한다.
//
//
//			인수 : 
//						str
//							- 검사할 문장
//
//			반환값 : 
//						"은" or "는"
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var en = fn_getEunNeun("철수")
//									=> en : "는"
//							
//								var en = fn_getEunNeun("철순")
//									=> en : "은"
//								
// ------------------------------------------------------------------------------------------------------
String.prototype.fn_getEunNeun = function()
{
	// 마지막 문자에 받침이 있으면 "은", 없으면 "는" 을 반환 한다.
	var lastChar = "";
	var retval = "";
	
	if(this.length > 0)
	{
		lastChar = this.charAt(this.length-1);
		if(lastChar.fn_haveJongSungSpk())
		{
			// 받침 있는 발음
			retval = "은";
		}
		else
		{
			//받침 없는 발음
			retval = "는";
		}
	}
	
	return retval;
};






//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_getEeGa
//
//		
//			설명 : 
//						마지막 문자를 가지고 "이"/"가" 을 구한다.
//
//
//			인수 : 
//						str
//							- 검사할 문장
//
//			반환값 : 
//						"이" or "가"
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var eg = fn_getEeGa("철수")
//									=> eg : "가"
//							
//								var eg = fn_getEeGa("철순")
//									=> eg : "이"
//								
//------------------------------------------------------------------------------------------------------
String.prototype.fn_getEeGa = function()
{
	// 마지막 문자에 받침이 있으면 "은", 없으면 "는" 을 반환 한다.
	var lastChar = "";
	var retval = "";
	
	if(this.length>0)
	{
		lastChar = this.charAt(this.length-1);
		if(lastChar.fn_haveJongSungSpk())
		{
			// 받침 있는 발음
			retval = "이";
		}
		else
		{
			//받침 없는 발음
			retval = "가";
		}
	}
	
	return retval;
};





//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_getEulLeul
//
//		
//			설명 : 
//						마지막 문자를 가지고 "을"/"를" 을 구한다.
//
//
//			인수 : 
//						str
//							- 검사할 문장
//
//			반환값 : 
//						"을" or "를"
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var eg = fn_getEeGa("철수")
//									=> eg : "를"
//							
//								var eg = fn_getEeGa("철순")
//									=> eg : "을"
//								
//------------------------------------------------------------------------------------------------------
String.prototype.fn_getEulLeul = function()
{
	// 마지막 문자에 받침이 있으면 "은", 없으면 "는" 을 반환 한다.
	var lastChar = "";
	var retval = "";
	
	if(this.length > 0)
	{
		lastChar = this.charAt(this.length-1);
		if(lastChar.fn_haveJongSungSpk())
		{
			// 받침 있는 발음
			retval = "을";
		}
		else
		{
			//받침 없는 발음
			retval = "를";
		}
	}
	return retval;
};





//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_isNumber
//
//		
//			설명 : 
//						숫자 타입인지 검사 한다.
//
//
//			인수 : 
//						str
//							- 검사할 문장
//						isFloatEnable
//							- 실수를 허용할지 여부
//								: true - 실수 허용(소숫점 있음)
//								 false - 정수만 허용(소숫점 없음)
//
//			반환값 : 
//						숫자 인지 여부
//						true - 숫자임.
//						false - 숫자 아님.
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var eg = "100.1".fn_isNumber(false)
//									=> eg : false	-> "100.1"은 정수가 아님
//							
//								var eg = "100.1".fn_isNumber(true)
//									=> eg : true	-> "100.1"은 실수 임
//							
//								var eg = "100".fn_isNumber(false)
//									=> eg : true	-> "100"은 정수임
//							
//								var eg = "100".fn_isNumber(true)
//									=> eg : true	-> "100"은 실수임
//								
//								var eg = "100.0.1".fn_isNumber(false)
//									=> eg : false		-> 소숫점이 2개 포함됨.
//								
//------------------------------------------------------------------------------------------------------
String.prototype.fn_isNumber = function(isFloatEnable)
{
	// 마지막 문자에 받침이 있으면 "은", 없으면 "는" 을 반환 한다.
	
	var i=0;
	var retval = (this.length>0);
	var dotCnt = 0;
	for(i=0; retval && i<this.length; i++)
	{
		switch(this.charAt(i))
		{
		case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
			{
				retval = true;
			}
			break;
		case '.':
			{
				retval = isFloatEnable && (dotCnt == 0);
				dotCnt = dotCnt+1;
			}
			break;
		default:
			{
				retval = false;
			}
			break;
		}
	}
	
	return retval;
};

//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_bLength
//
//		
//			설명 : 
//						문자열의 byte 길이를 계산 한다.
//
//
//			인수 : 
//						없음
//
//			반환값 : 
//						byte 길이
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var eg = "100.1".fn_isNumber(false)
//									=> eg : false	-> "100.1"은 정수가 아님
//							
//								var eg = "100.1".fn_isNumber(true)
//									=> eg : true	-> "100.1"은 실수 임
//							
//								var eg = "100".fn_isNumber(false)
//									=> eg : true	-> "100"은 정수임
//							
//								var eg = "100".fn_isNumber(true)
//									=> eg : true	-> "100"은 실수임
//								
//								var eg = "100.0.1".fn_isNumber(false)
//									=> eg : false		-> 소숫점이 2개 포함됨.
//								
//------------------------------------------------------------------------------------------------------
String.prototype.fn_bLength = function()
{
	var len = 0;
	for(var i=0; i<this.length; i++)
	{
		var c = this.charCodeAt(i);
		len = len + (c>>8 ? Number(UNICODE_BYTES) : 1);	// byte 길이로 3byte문자(12bit 이상 길이)는 3자리,2byte 문자(8bit 이상 길이) , 기타 1자리 길이로 검사
	}
	return len;
};



//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_split
//
//		
//			설명 : 
//						구분자로 나눠서 문자열 배열을 반환한다.
//
//
//			인수 : 
//						sprChar 
//							- 자를 구분자
//
//			반환값 : 
//						구분자로 잘라서 생성된 문자열 배열
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var arrStr = "test-tesaaa-aasx".fn_split("-");
//
//------------------------------------------------------------------------------------------------------
String.prototype.fn_split = function(sprChar)
{
	
	var retval = new Array();
	var sIdx = 0;
	var eIdx = 0;
	
	while( (eIdx = this.indexOf(sprChar, sIdx)) != -1)
	{
		retval[retval.length] = this.substring(sIdx, eIdx);
		sIdx = eIdx + 1;
	}
	if(sIdx<=this.length)
	{
		retval[retval.length] = this.substring(sIdx, this.length);
	}
	
	return retval;
};


//------------------------------------------------------------------------------------------------------
//
//			함수명 : fn_validation
//
//		
//			설명 : 
//						validation 처리를 한다.
//
//
//			인수 : 
//						type
//							- 데이터 타입
//								: x - 필수 입력
//								: i - 숫자
//								: 	in - 정수
//								: 	if - 실수형
//								: s - 문자열(생략 가능.. 쓰나 안쓰나 마찬가지임..;;)
//								: 	l - 문자열 길이. "l:2"와 같이 "l:[길이]" 를 입력 한다.
//						name 
//							- 데이터 이름(필드명)
//
//			반환값 : 
//						성공 여부
//						true - 오류 없음
//						false - 오류 있음(validation check fail)
//
//
//			사용법 예 :
//
//
//					(!) 실행 ::
//								
//								var rv = "".fn_validation("x,i", "이름")
//									=> rv : false, 
//										팝업 메세지 : "이름이 입력되지 않았습니다.";
//								
//								var rv = "가".fn_validation("x,i", "이름")
//									=> rv : false, 
//										팝업 메세지 : "이름이 숫자가 아닙니다.";
//								
//								var rv = "하늘이".fn_validation("x,s,l:2", "이름")
//									=> rv : false, 
//										팝업 메세지 : "이름이 너무 깁니다.";
//								
//------------------------------------------------------------------------------------------------------
String.prototype.fn_validation = function(type, name)
{
	var arrType = type.fn_split(",");
	var i = 0;
	var retval = true;
	for(i = 0; i< arrType.length && retval; i++)
	{
		
		switch(arrType[i])
		{
		case 'x':
			{
				// 필수 입력
				retval = (this!="");
				if(!retval)
				{
					alert(name + name.fn_getEulLeul() + " 확인 하세요.");
				}
			}
			break;
		case 'i':
		case 'if':
			{
				retval =  (this=="") || this.fn_isNumber(true);
				if(!retval)
				{
					alert(name + name.fn_getEunNeun() + "  숫자만 입력 가능합니다.");
				}
			}
			break;
		case 'in':
		{
			retval = (this=="") || this.fn_isNumber(false);
			if(!retval)
			{
				alert(name + name.fn_getEunNeun() + "  정수만 입력 가능합니다.");
			}
		}
		break;
		default:
			{
				var arrAtt = arrType[i].fn_split(":");
				switch(arrAtt[0])
				{
				case 'l':
					{
						// 길이 체크
						retval = this.fn_bLength()<=Number(arrAtt[1]);
						
						if(!retval)
						{
							alert(name + name.fn_getEeGa() + " 너무 깁니다.");
						}					
					}
					break;
				}
			}
			break;
		}
		
	}
	

	return retval;
};





// ------------------------------------------------------------------------------------------------------
//		Element들에게 추가 함수를 정의 함.
//		필요시 추가해서 사용.
// ------------------------------------------------------------------------------------------------------
jQuery.fn.extend(
							{
								// ------------------------------------------------------------------------------------------------------
								//
								//			함수명 : fn_mp_reset
								//
								//		
								//			설명 : 
								//						해당 Element를 초기상태로 만듬.
								//	 					만약 Element가 Container(table, tr, td, div, span...) 들이라면 하위 Element들에게 이벤트버블링 발생
								//
								//
								//			반환값 : 
								//						없음
								//
								//
								//			사용법 예 :
								//
								//					(!) ui side ::
								//
								//							<table class="conInput_date" id="resetEl">
								//								<tr>
								//									<td><input type="text" name="S_YYYY" maxlength="4" class="date" /></td>
								//									<td>
								//										<select name="S_MM">	<!-- target에 지정한 input class들을 control함) -->
								//											<option value="1">1월</option>
								//											<option value="2">2월</option>
								//			    						</select>										
								//									</td>
								//								</tr>
								//							</table>
								//
								//					(!)동작 ::
								//							컨테이너(conInput_date)의 하위에 있는 항목들을 모두 초기 상태로 리셋 하려고 한다.
								//							=> S_YYYY : 입력한 텍스트를 지움
								//							=> S_MM : 첫번째 항목을 선택하도록 변경
								//
								//
								//					(!) 실행 ::
								//								$( ".conInput_date").each(function(i){
								//									$(this).fn_mp_reset();
								//								});
								//
								//								혹은
								//
								//								$("#resetEl").fn_mp_reset();
								//
								// ------------------------------------------------------------------------------------------------------
								fn_mp_reset : function()
								{
									// 만약 자신이 input이라면 값을 초기화 시킴.
									var nodename = $(this).prop("nodeName").toLowerCase();
									switch(nodename)
									{
										// containers define!!
										case "table": case "tbody": case "tr": case "thead": case "th": case "td":
										case "div":
										{
											$(this).children().each(function(i){ $(this).fn_mp_reset(); });
										}
										break;
										// element define!!
										case "input":
										{
											// 타입에 따라 설정
											var type = $(this).prop("type").toLowerCase();
											switch(type)
											{
												case "text": 				$(this).val(""); 																							break;
												case "checkbox": 	$("[name='" + $(this).prop("name") + "']")[0].attr('checked','checked'); 	break;
											}
											//console.debug("type="+type);
										}
										break;
										case "select":
										{
											// 1번째 항목 선택
											if($(this).children().length>0)
											{
												$(this)[0].value = $(this).children()[0].value;
											}
										}
										break;
										//case "option": case "img": case "br": case "h4": case "button": case "colgroup":
										//case "col": case "a":
										//{
										//}
										//break;
										//default:
										//{
										//	debug.console("fn_mp_reset() :: cannot find element type!!(" + nodename + ")");
										//}
										//break;
									}		// end switch()	
								}	// end of fn_mp_reset;
							// Element에 알아서 값을 셋팅 함.
							// -----------------------------------------------------------------------------------
							// fn_mp_setValue
							// -----------------------------------------------------------------------------------
							,	fn_mp_setValue : function(value)
								{
									// 만약 자신이 input이라면 값을 초기화 시킴.
								
									if(!$(this).prop("nodeName"))
									{
										return;
									}
									var nodename = $(this).prop("nodeName").toLowerCase();
									switch(nodename)
									{
										// element define!!
										case "input":
										{
											// 타입에 따라 설정
											var type = $(this).prop("type").toLowerCase();
											switch(type)
											{
												case "text": 				$(this).val(value); 																					break;
												case "checkbox": 	
												{
													$("[name='" + $(this).prop("name") + "']").each(function(i){
														if($(this).val() == value)
														{
															$(this).attr('checked','checked');
														}
													});
												}
												break;
											}
										}
										break;
										case "select":
										{
											// 1번째 항목 선택
											$(this)[0].value = value;
										}
										break;
										default:
										{
											debug.console("fn_mp_setValue() :: cannot find element type!!(" + nodename + ")");
										}
										break;
									}		// end switch()	
							}	// end of fn_mp_reset;
							
							// -----------------------------------------------------------------------------------
							// fn_mp_validate
							//		(!) 폼 안에 있는 Element들의 validation을 수행 함.
							//				Validation을 할 대상 Element들은 2가지 attribute를 가지고 있어야 함.
							//	
							//				valid_name 
							//								 - 해당 Element의 이름.
							//									경고창에서 사용할 이름.
							//
							//				valid_type		
							//								 - validation을 실행할 타입
							//									각 타입은 쉼표(,)로 구분 한다.
							//
							//									: x - 필수 입력
							//									: i - 숫자
							//									: 	in - 정수
							//									: 	if - 실수형
							//									: s - 문자열(생략 가능.. 쓰나 안쓰나 마찬가지임..;;)
							//									: 	l - 문자열 길이. "l:2"와 같이 "l:[길이]" 를 입력 한다.
							//
							//
							//		(!) UI : 
							//
							//							<form name="test_form">
							//								<input type="hidden" />
							//								<table>
							//									<tr>
							//										<td>
							//											숫자 					: <input type="text"  name="input_text" valid_name="숫자" valid_type="i"/><br/>
							//											정수 					: <input type="text"  name="input_text" valid_name="정수" valid_type="in"/><br/>
							//											실수 					: <input type="text"  name="input_text" valid_name="실수" valid_type="if"/><br/>
							//											필수,숫자 			: <input type="text"  name="input_text" valid_name="필수,숫자" valid_type="x,n" /><br />
							//											문자열:20 			: <input type="text"  name="input_text" valid_name="문자열:20" valid_type="s,l:20"/><br/>
							//											필수, 문자열:3 	: <input type="text"  name="input_text" valid_name="필수, 문자열:3" valid_type="x,s,l:3"/><br/>
							//											<input type="checkbox"  name="ck_test" value="1" valid_name="체크박스1" valid_type="x"/>
							//											<input type="checkbox"  name="ck_test" value="2" valid_name="체크박스2" valid_type="x"/><br/>
							//											<input type="radio"  name="rb_test" value="1" valid_name="라디오버튼" valid_type="x"/>
							//											<input type="radio"  name="rb_test" value="2" valid_name="라디오버튼" valid_type="x"/><br/>
							//											<select name="sc_test" valid_name="select박스" valid_type="x">
							//												<option value="">선택</option>
							//												<option value="1">1</option>
							//												<option value="2">2</option>
							//											</select>
							//										</td>
							//									</tr>
							//								</table>
							//							</form>
							//
							//
							//		(!) 사용 예
							//					$(test_form).fn_mp_validate();
							//
							//
							// -----------------------------------------------------------------------------------
							//validation check
							,	fn_mp_validate : function()
							{
								var nodename = $(this).prop("nodeName").toLowerCase();
								var retval = true;

								var valEl = false;
								var valStr = "";
								var valid_type = $(this).attr("valid_type");
								var valid_name = $(this).attr("valid_name");
								
								switch(nodename)
								{
									// ===================================================
									// containers.. find child element..
									// ===================================================
									case "form":
									case "table": 	case "tbody": 	case "tr": 	case "thead": 	case "th": 	case "td":
									case "div": 		case "span":
									{
										$(this).children().each(function(i){ 
											retval = $(this).fn_mp_validate();
											if(!retval)
											{
												return false;
											}
										});
									}
									break;
									// ===================================================
									// elements.. validation check..
									// ===================================================
									case "input":
									{
										// 타입에 따라 설정
										if(valEl = (valid_type && valid_name))
										{
											switch($(this).prop("type").toLowerCase())
											{
												case "checkbox": 	valStr = $("[name='" + $(this).prop("name") + "']:checked").val(); 	break;
												case "radio": 			valStr = $("[name='" + $(this).prop("name") + "']:checked").val();		break;
												default: 					valStr = $(this).val(); 																		break;
											}
										}
									}
									break;
									case "textarea":
									{
										if(valEl = (valid_type && valid_name))
										{
											valStr = $(this).val();
										}										
									}
									break;
									case "select":
									{
										if(valEl = (valid_type && valid_name))
										{
											valStr = $(this).val();
										}
									}
									break;
									// ===================================================
									//// -------------------- 적용 시키지 않는 Element ---------------------------- //
									//case "option": case "img": case "br": case "h4": case "button": case "colgroup":
									//case "col": case "a":
									//{
									//
									//}
									//break;
									//	// -------------------- 적용 시키지 않는 Element ---------------------------- //
									//default:
									//{
									//	debug.console("fn_mp_validate() :: cannot find element type!!(" + nodename + ")");
									//}
									//break;
									// ===================================================
								}		// end switch()
								
								if(valEl)
								{
									if(!valStr)
									{
										valStr = "";
									}
									retval = valStr.fn_validation(valid_type, valid_name);
									if(!retval)
									{
										$(this).focus();
									}
								}
								return retval;
							}	// end of fn_mp_validate;
							
							
							, reset_sort : function()
							{
								$(this).removeClass("asc");
								$(this).removeClass("desc");
							}
							// 헤더를 오름차순 UI로 설정 함
							,	asc : function()
							{
								// 정렬 : 오름차순
								$(this).removeClass("desc");
								$(this).addClass("asc");
							}
							// 헤더를 내림차순 UI로 설정 함
							,	dsec : function()
							{
								// 정렬 : 내림차순
								$(this).removeClass("asc");
								$(this).addClass("desc");
							}
							
							
							// -----------------------------------------------------------------------------------
							// end function defines..
							// -----------------------------------------------------------------------------------
						});


// ------------------------------------------------------------------------------------------------------
//
//		함수명 : fn_mp_initSelectOptionElement
//
//
//		설명 : 
//			
//			해당 Element가 변경되었을때 값을 입력하는 부분을 초기화 시키도록 이벤트를 연결하고,
//			화면에 1개의 값 입력 폼이 보이도록 함.
//
//
//		반환값 : 
//					없음
//
//		사용법 예
//				(!) ui side ::
//
//						<!-- 검색구분(타겟은 valueClass속성에 해당 class값을 넣어서 참조 가능하도록 함.) -->
//						<select id="conSelect_dtType" valueClass="conInput_date">	<!-- target에 지정한 input class들을 control함) -->
//							<option value="0">월별</option>	<!-- 선택시 첫번째 값 입력 폼이 보이도록 함 -->
//							<option value="1">일별</option>	<!-- 선택시 두번째 값 입력 폼이 보이도록 함 -->
//						</select>
//
//
//						<!-- 첫번째 값 입력 폼 항목(월별) :: class에 검색구분의 valueClass와 동일하게 설정 됨. -->
//						<table class="conInput_date">
//							<tr>
//								<td><input type="text" name="S_YYYY" maxlength="4" class="date" /></td>
//								<td>
//									<select name="S_MM">	<!-- target에 지정한 input class들을 control함) -->
//										<option value="1">1월</option>
//										<option value="2">2월</option>
//		    						</select>										
//								</td>
//							</tr>
//						</table>
//
//
//						<!-- 두번째 값 입력 폼 항목(일별) :: class에 검색구분의 valueClass와 동일하게 설정 됨. -->
//						<table class="conInput_date">
//								<tr>
//								<td><input type="text" name="BEGIN_DT" maxlength="8" class="date" /></td>
//								<td>~</td>
//								<td><input type="text" name="END_DT" maxlength="8" class="date" /></td>
//								</tr>
//						</table>
//
//				(!)동작 ::
//						1. 검색구분(conSelect_dtType)을 선택하게 되면, 
//						2. 이전 입력폼 화면은 숨기고, 선택한 검색구분에 맞는 입력 폼 화면이 보이게 함.
//						3. 이전에 입력 된 값들은 모두 지워지고 초기화 시킴.
//
//
//				(!) 실행 ::
//
//						initSelSearch("conSelect_dtType");	-> 초기화 하려는 구분 Element의 id를 넣음
//
//
//
//				(!) 주의사항 ::
//
//						1. 검색구분이 되는(이벤트를 발생시키는) element는 타겟이 되는 항목을 지정할 필요가 있다.
//							valueClass에 교차해서 보여질 elements들의 class를 지정하도록 했다.
//
//						2. 교차해서 보여질 항목(class == conInput_date)과 구분값의 option(conSelect_dtType)의 option의 갯수는 동일해야 한다.
//
// ------------------------------------------------------------------------------------------------------
function fn_mp_initSelectOptionElement(selectObjId)
{

	// hide input object
	var targetObjClassName = $("[id='" + selectObjId + "']").attr("valueClass");
	$( "." + targetObjClassName).each(function(i){
		if(i != 0){
			$(this).hide();
		}
	});
	
	// bind onChange event to select object
	$(document).on("change", "#"+selectObjId, function(){

		var su = $(this).val();
    	
		$( "." + targetObjClassName).each(function(i){
			$(this).fn_mp_reset();
		});
		
		
		$( "." + targetObjClassName).hide();
		$( "." + targetObjClassName+":eq(" + su + ")").show();
	});
}
























// '''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//		DATE PICKER 관련 함수 - 시작
// ...................................................................................................................................
// 참고 : http://api.jqueryui.com/datepicker/#option-altField

// ----------------------------------------------------
//	함수명 : fn_mp_init_datePicker
//
//	내용	: 		
//				JQuery의 DatePicker를 사용하기 위해 초기화 하는 함수
//				필드가 1개 일때 사용하도록 한다.
//
//	param : 
//				- pickerID 	: Date Picker의 Element ID
//				- targetElID 	: 날짜 선택시 선택된 날을 넣을 Text필드의 ElementID
//
//
// 사용법 : 
//					(!) ui
//							<!-- Begin DatePicker-->
//                            <input class="datePicker_dtB" type="text" id="START_DT" name="START_DT" value="$!searchParam.START_DT" maxlength="8" style="width:80px;"/>
//                            <input class="datePicker_dtB" type="hidden" id="PIK_BTN_BEGIN" style="width: 0px; border-style: none; background-color:transparent;"/>
//								<!-- End DatePicker-->
//								~
//								<!-- Begin DatePicker-->
//								<input class="datePicker_dtE" type="text" id="END_DT" name="END_DT" value="$!searchParam.END_DT" maxlength="8"  style="width:80px;"/>
//								<input class="datePicker_dtE" type="hidden" id="PIK_BTN_END" style="width: 0px; border-style: none; background-color:transparent;" />
//								<!-- End DatePicker-->
//
//					(!) JS
//								bindEvent_datePicker("PIK_BTN_BEGIN", "START_DT");
//
// ----------------------------------------------------
function fn_mp_init_datePicker(pickerID, targetElID, format)
{
	if(!format)
	{
		format = "yymmdd";
	}
	
    $("#" + pickerID).datepicker({
    		regional: "ko",
    		dateFormat: "yymmdd",
    		showOn: "button",
    		buttonImage: RESOURCES_PATH+"/css/4.0/img/icon_calen.gif",
    		buttonImageOnly: true,
    		altField: "#"+targetElID,
    		altFormat: format
    	});
}
// end bindEvent_datePicker()












// ----------------------------------------------------
//	 함수명 : fn_mp_init_datePicker_multiForm
//	
//	설명 		: 	jQuery의 datePicker를 편하게 사용하기 위한 함수
//					년 월 일 필드가 분리 되어 있을때 사용 가능 하도록 함.
//					
//	param : 
//				- frmName	: form 의 name
//				- objName	: 검사할 element 의 name 속성
//				- type			: 검사할 element의 type 속성
//				- errMsg		: 오류가 있을시 alert로 사용할 메세지
//
// 사용법 : 
//							(!) UI
//										<!-- 년도 입력 필드 -->
//										<input type="text" name="S_DT_YYYY" id="S_DT_YYYY" value="$!searchParam.S_DT_YYYY" maxlength="4" style="width:30px;"/>
//										<!-- 월 입
//										<select name="S_DT_MM" id="S_DT_MM">	<!-- target에 지정한 input class들을 control함) -->
//                        		          	<option value="01" #if($!searchParam.S_DT_MM=='01') selected='selected' #end>1월</option>
//                        		          	<option value="02" #if($!searchParam.S_DT_MM=='02') selected='selected' #end>2월</option>
//                        		          	<option value="03" #if($!searchParam.S_DT_MM=='03') selected='selected' #end>3월</option>
//                        		          	<option value="04" #if($!searchParam.S_DT_MM=='04') selected='selected' #end>4월</option>
//                        		          	<option value="05" #if($!searchParam.S_DT_MM=='05') selected='selected' #end>5월</option>
//                        		          	<option value="06" #if($!searchParam.S_DT_MM=='06') selected='selected' #end>6월</option>
//                        		          	<option value="07" #if($!searchParam.S_DT_MM=='07') selected='selected' #end>7월</option>
//                        		          	<option value="08" #if($!searchParam.S_DT_MM=='08') selected='selected' #end>8월</option>
//                        		          	<option value="09" #if($!searchParam.S_DT_MM=='09') selected='selected' #end>9월</option>
//                        		          	<option value="10" #if($!searchParam.S_DT_MM=='10') selected='selected' #end>10월</option>
//                        		          	<option value="11" #if($!searchParam.S_DT_MM=='11') selected='selected' #end>11월</option>
//                        		          	<option value="12" #if($!searchParam.S_DT_MM=='12') selected='selected' #end>12월</option>
//                        			    </select>										
//										<input type="hidden" id="PIK_BTN_YYYY_MM" style="width: 0px; border-style: none;" />
//
//						(!) JS
//									bindEvent_datePicker_muti("PIK_BTN_YYYY_MM", "S_DT_YYYY", "S_DT_MM");	// 일 필드 없어도 사용 가능 함.
//	return : 없음
//
//
// ----------------------------------------------------
function fn_mp_init_datePicker_multiForm(pickerID, targetYYYYElId, targetMMElId, targetDDElId)
{
	
    $("#" + pickerID).datepicker({
		regional: "ko",
		dateFormat: "yymmdd",
		showOn: "button",
		buttonImage: RESOURCES_PATH+"/css/4.0/img/icon_calen.gif",
		buttonImageOnly: true,
		onSelect: function(dates) { 
													if(dates.length)
													{
														// dateFormat: "yymmdd",
                                                        $('#' + targetYYYYElId).fn_mp_setValue(dates.substring(0, 4)); 
                                                        $('#' + targetMMElId).fn_mp_setValue(dates.substring(4, 6)); 
                                                        $('#' + targetDDElId).fn_mp_setValue(dates.substring(6, 8)); 
													}
                                                } 
			
    });

	 
	
	

	// ---------------------------------------------------------------------------------------------
	// 날짜 입력 필드에 직접 날짜를 입력 했을때, DatePicker의 날짜도 변경 시키도록 이벤트를 걸어 줌.
	// ---------------------------------------------------------------------------------------------
	var curdt 	= $("#" + pickerID).val();
	var yyyy 	= targetYYYYElId && $('#'+targetYYYYElId) ? $('#'+targetYYYYElId).val()     : curdt.substring(0, 4) ;
	var mm 	= targetMMElId   && $('#'+targetMMElId)    ? $('#'+targetMMElId).val()   		: curdt.substring(4, 6) ;
	var dd 		= targetDDElId    && $('#'+targetDDElId)     ? $('#'+targetDDElId).val() 		: curdt.substring(6, 8) ;
	if(targetYYYYElId)
        $('#'+targetYYYYElId).change(function() { 
    		yyyy 	= targetYYYYElId && $('#'+targetYYYYElId) ? $('#'+targetYYYYElId).val()     : $("#" + pickerID).val().substring(0, 4) ;
        	$('#' + pickerID).datepicker('setDate', new Date(  yyyy, mm,  dd)); 
		});
	if(targetMMElId)
        $('#'+targetMMElId).change(function() { 
    		mm 	= targetMMElId   && $('#'+targetMMElId)    ? $('#'+targetMMElId).val()   : Number($("#" + pickerID).val().substring(4, 6)) ;
			$('#' + pickerID).datepicker('setDate', new Date(  yyyy, mm,  dd)); 
        });
	if(targetDDElId)
        $('#'+targetDDElId).change(function() { 
    		dd 		= targetDDElId    && $('#'+targetDDElId)     ? $('#'+targetDDElId).val() 		: $("#" + pickerID).val().substring(6, 8) ;
			$('#' + pickerID).datepicker('setDate', new Date(  yyyy, mm,  dd)); 
        });
}
// '''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//		DATE PICKER 관련 함수 - 끝
// ...................................................................................................................................








//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
// LIST CHECKBOX 관련 함수 - 시작
//...................................................................................................................................
//------------------------------------------------------------------------------------------------------
//
//		함수명 : fn_mp_bindEvent_listCheckbox
//
//
//		설명 : 
//			
//					테이블 안에 있는 Checkbox의 이벤트를 연결 시키는 함수.
//					
//					테이블 헤더에 있는 체크박스 클릭시 : 
//							- 행에 있는 체크박스 중 일부가 체크 되어 있다면 모두 해제 시킴.
//							- 행에 체크된 체크박스가 없다면 모두 체크 함.
//							- 행에 모든 체크박스가 체크 되어 있다면 모두 체크 해제 함.
//					
//					
//
//		파라메터 :
//			- head_ck_cls	
//								: String
//								: 헤더에 있는 체크박스의 class 명
//			- row_ck_cls
//								: String
//								: 행에 있는 체크박스의 class 명
//
//
//		사용 예 :
//
//			(!) UI 정의
//
//					<table>
//						<tr>
//							<td><input type="checkbox" class="ck_head" ... /></td>		<-- 헤더체크박스
//						</tr>
//						<tr>
//							<td><input type="checkbox" class="ck_row" ... /></td>		<-- 행의 체크박스
//						</tr>
//		
//							........ 중략 ...
//
//						<tr>
//							<td><input type="checkbox" class="ck_row" ... /></td>		<-- 행의 체크박스
//						</tr>
//					</table>
//
//			(!) 사용법
//				$(document).ready(function()
//				{
//					... 초기화 정의 ....
//
//					fn_mp_bindEvent_listCheckbox("ck_head", "ck_row");
//				}
//
//------------------------------------------------------------------------------------------------------
function fn_mp_bindEvent_listCheckbox(head_ck_cls, row_ck_cls, callbackFunc)
{
	$("."+head_ck_cls).bind("click", function()
	{
		var thisCheck =$(this).is(":checked");
		$( "." + row_ck_cls).each(function(i){
			$(this).prop('checked', thisCheck);
		});
		if(callbackFunc)
		{
			var onEa = 0;
			var  offEa = 0;
			onEa = $(":checkbox[class='"+row_ck_cls+"']:checked").length;		// 체크된 행의 체크박스 갯수
			offEa = $(":checkbox[class='"+row_ck_cls+"']").length - onEa;		// 체크되지 않은 행의 체크박스 갯수
			callbackFunc(onEa, offEa);
		}
	});
	
	$("."+row_ck_cls).bind("click", function()
	{
		var onEa = 0;
		var  offEa = 0;
		onEa = $(":checkbox[class='"+row_ck_cls+"']:checked").length;		// 체크된 행의 체크박스 갯수
		offEa = $(":checkbox[class='"+row_ck_cls+"']").length - onEa;		// 체크되지 않은 행의 체크박스 갯수
		$("."+head_ck_cls).prop('checked', offEa == 0);

		if(callbackFunc)
		{
			callbackFunc(onEa, offEa);
		}
	});	
}

//------------------------------------------------------------------------------------------------------
//
//		함수명 : fn_mp_geCheckedCheckboxValues
//
//
//		설명 : 
//			
//					테이블 안에 있는 Checkbox중 선택된 checkbox의 value 값들을 꺼낸다.
//					반환 되는 데이터 형식은 문자열로, 입력받은 split 문자들로 연결 한다.
//
//					예 : 
//							
//
//		파라메터 :
//			- head_ck_cls	
//								: String
//								: 헤더에 있는 체크박스의 class 명
//			- row_ck_cls
//								: String
//								: 행에 있는 체크박스의 class 명
//
//
//		사용 예 :
//
//			(!) UI 정의
//
//					<table>
//						<tr>
//							<td><input type="checkbox" class="ck_head" ... /></td>		<-- 헤더체크박스
//						</tr>
//						<tr>
//							<td><input type="checkbox" class="ck_row" ... /></td>		<-- 행의 체크박스
//						</tr>
//		
//							........ 중략 ...
//
//						<tr>
//							<td><input type="checkbox" class="ck_row" ... /></td>		<-- 행의 체크박스
//						</tr>
//					</table>
//
//			(!) 사용법
//				fn_mp_geCheckedCheckboxValues("ck_row", "|");
//				=> "1|2|3|4|....|n" 형태로 value 속성의 값들이 연결 되어서 반환 됨.
//
//				fn_mp_geCheckedCheckboxValues("ck_row", "^");
//				=> "1^2^3^4^....^n" 형태로 value 속성의 값들이 연결 되어서 반환 됨.
//
//------------------------------------------------------------------------------------------------------

function fn_mp_geCheckedCheckboxValues(row_ck_cls, splitChar)
{
	var retval = "";
	$(":checkbox[class='"+row_ck_cls+"']:checked").each(function(i){
		if(i>0)
			retval = retval + splitChar;
		retval = retval + this.value;
	});
	return retval;
}


//------------------------------------------------------------------------------------------------------
//
//		함수명 : fn_mp_syncTableCheckbox()
//
//
//		설명 : 
//			
//					Row에 있는 CheckBox 의 상태로 Header의 Checkbox를 셋팅 함.
//
//							
//
//		파라메터 :
//			- head_ck_cls	
//								: String
//								: 헤더에 있는 체크박스의 class 명
//			- row_ck_cls
//								: String
//								: 행에 있는 체크박스의 class 명
//
//
//------------------------------------------------------------------------------------------------------
function fn_mp_syncTableCheckbox(head_ck_cls, row_ck_cls)
{
	var onEa = 0;
	var  offEa = 0;
	
	onEa = $(":checkbox[class='"+row_ck_cls+"']:checked").length;		// 체크된 행의 체크박스 갯수
	offEa = $(":checkbox[class='"+row_ck_cls+"']").length - onEa;		// 체크되지 않은 행의 체크박스 갯수
	$("."+head_ck_cls).prop('checked', offEa == 0);
}

//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//LIST CHECKBOX 관련 함수 - 시작
//...................................................................................................................................














//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
// 검색창 관련 함수 - 시작
//...................................................................................................................................
//------------------------------------------------------------------------------------------------------
//
//		함수명 : fn_mp_initSelectOptionElement
//
//
//		설명 : 
//			
//			해당 Element가 변경되었을때 값을 입력하는 부분을 초기화 시키도록 이벤트를 연결하고,
//			화면에 1개의 값 입력 폼이 보이도록 함.
//
//
//		반환값 : 
//					없음
//
//		사용법 예
//				(!) ui side ::
//
//						<!-- 검색구분(타겟은 valueClass속성에 해당 class값을 넣어서 참조 가능하도록 함.) -->
//						<select id="conSelect_dtType" valueClass="conInput_date">	<!-- target에 지정한 input class들을 control함) -->
//							<option value="0">월별</option>	<!-- 선택시 첫번째 값 입력 폼이 보이도록 함 -->
//							<option value="1">일별</option>	<!-- 선택시 두번째 값 입력 폼이 보이도록 함 -->
//						</select>
//
//
//						<!-- 첫번째 값 입력 폼 항목(월별) :: class에 검색구분의 valueClass와 동일하게 설정 됨. -->
//						<table class="conInput_date">
//							<tr>
//								<td><input type="text" name="S_YYYY" maxlength="4" class="date" /></td>
//								<td>
//									<select name="S_MM">	<!-- target에 지정한 input class들을 control함) -->
//										<option value="1">1월</option>
//										<option value="2">2월</option>
//		    						</select>										
//								</td>
//							</tr>
//						</table>
//
//
//						<!-- 두번째 값 입력 폼 항목(일별) :: class에 검색구분의 valueClass와 동일하게 설정 됨. -->
//						<table class="conInput_date">
//								<tr>
//								<td><input type="text" name="BEGIN_DT" maxlength="8" class="date" /></td>
//								<td>~</td>
//								<td><input type="text" name="END_DT" maxlength="8" class="date" /></td>
//								</tr>
//						</table>
//
//				(!)동작 ::
//						1. 검색구분(conSelect_dtType)을 선택하게 되면, 
//						2. 이전 입력폼 화면은 숨기고, 선택한 검색구분에 맞는 입력 폼 화면이 보이게 함.
//						3. 이전에 입력 된 값들은 모두 지워지고 초기화 시킴.
//
//
//				(!) 실행 ::
//
//						initSelSearch("conSelect_dtType");	-> 초기화 하려는 구분 Element의 id를 넣음
//						or
//						initSelSearch("conSelect_dtType", function callback(selectEl){...... process ....});	-> 초기화 하려는 구분 Element의 id를 넣음
//
//
//
//				(!) 주의사항 ::
//
//						1. 검색구분이 되는(이벤트를 발생시키는) element는 타겟이 되는 항목을 지정할 필요가 있다.
//							valueClass에 교차해서 보여질 elements들의 class를 지정하도록 했다.
//
//						2. 교차해서 보여질 항목(class == conInput_date)과 구분값의 option(conSelect_dtType)의 option의 갯수는 동일해야 한다.
//
//						3. option의 value는 연결 시킬 conInput_data의 순번을 적어 둔다.
//
//------------------------------------------------------------------------------------------------------
function fn_mp_initSelectOptionElement(selectObjId, onChangeCallback)
{

	// hide input object
	var targetObjClassName = $("[id='" + selectObjId + "']").attr("valueClass");
	$( "." + targetObjClassName).each(function(i){
		if(i != $("[id='" + selectObjId + "']").val())
		{
			$(this).hide();
		}
	});
	
	// bind onChange event to select object
	$(document).on("change", "#"+selectObjId, function(){

		var su = $(this).val();		// 
	
		$( "." + targetObjClassName).each(function(i){
			if(i != su)
			{
				$(this).fn_mp_reset();
			}
		});
		
		$( "." + targetObjClassName).hide();
		$( "." + targetObjClassName+":eq(" + su + ")").show();
		
		// callback 실행
		if(onChangeCallback)
		{
			onChangeCallback(this);
		}
	});
}




//'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
//검색창 관련 함수 - 끝
//...................................................................................................................................
function reset_sort(classname)
{
	$("." + classname).removeClass("asc");
	$("." + classname).removeClass("desc");
}


function fn_mp_bindEvent_sortHeader(formName, selectFunction, orderField, orderType)
{
	var frm = $("form[name="+formName+"]");
	$(".sort").bind("click", function()
			{
				$(frm).find("input[name=ORDER_FIELD]").val($(this).attr("fname"));
				if($(this).hasClass("desc"))
				{
					$(frm).find("input[name=ORDER_TYPE]").val("ASC");
				}
				else
				{
					$(frm).find("input[name=ORDER_TYPE]").val("DESC");
				}
				//$[selectFunction](1);
				selectFunction(1);
			});

	if(orderType != "")
	{
		$("th[fname="+orderField+"]").addClass("DESC"==orderType ? "desc" : "asc");
	}
}
