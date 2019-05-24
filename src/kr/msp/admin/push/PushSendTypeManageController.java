package kr.msp.admin.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.msp.admin.base.collection.SingleData;
import kr.msp.admin.base.urlconnect.PushUrlConnect;
import kr.msp.admin.push.appManage.dto.PnsSelectVarDto;
import kr.msp.admin.push.sendType.dto.PushSendTypeDto;
import kr.msp.admin.push.sendType.service.PushSendTypeManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushSendTypeManageController {
	private final static Logger logger = LoggerFactory.getLogger(PushSendTypeManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;

    private @Value("${admin.push.receiver.host}") String DEFAULT_RECEIVER_HOST;	//리시버 호스트

	@Autowired(required=true)
	PushSendTypeManageService pushSendTypeManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource;

	@RequestMapping(value="sendType" , method=RequestMethod.GET)
	public String pushSendTypeGet( Model model){
		PushSendTypeDto pushSendType = new PushSendTypeDto();
		pushSendType.setPAGE_NUM(1);
		pushSendType.setPAGE_SIZE(row_size);
		List<PushSendTypeDto> pushSendTypeList = pushSendTypeManage.SelectSendTypeList(pushSendType);

		model.addAttribute( "pushSendTypeList", pushSendTypeList );
		model.addAttribute("R_PAGE_NUM",pushSendType.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);

		return "admin/push/sendType/sendTypeMain";
	}
	
	@RequestMapping(value="sendType" , method=RequestMethod.POST)
	public String pushSendTypePost( Model model , PushSendTypeDto pushSendType ){
		List<PushSendTypeDto> pushSendTypeList = pushSendTypeManage.SelectSendTypeList(pushSendType);
		
		model.addAttribute( "pushSendTypeList", pushSendTypeList );
		model.addAttribute("R_PAGE_NUM",pushSendType.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",pushSendType.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/sendType/sendTypeList";
	}
	
	//발송유형 등록 화면
	@RequestMapping(value="sendType/regist" , method=RequestMethod.GET)
	public String pushSendTypeRegistGet( Model model){
		
		List<PnsSelectVarDto> pnsSelectVarList = pushSendTypeManage.SelectPnsVarList();
		
		model.addAttribute("pnsSelectVarList", pnsSelectVarList);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/sendType/sendTypeModify";
	}
	
	//발송유형 수정 화면
	@RequestMapping(value="sendType/edit" , method=RequestMethod.GET)
	public String pushSendTypeEditGet( Model model , PushSendTypeDto pushSendType){
		
		PushSendTypeDto pushSendTypeOne  = pushSendTypeManage.SelectSendTypeOne(pushSendType);
		
		model.addAttribute("pushSendTypeOne", pushSendTypeOne);
		model.addAttribute("layout", "layout/null.vm");
			
		return "admin/push/sendType/sendTypeModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="sendType/check" , method=RequestMethod.POST)
	public String pushSenderCheckPost( PushSendTypeDto pushSendType ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
		
		try {
			result = pushSendTypeManage.SelectSendTypeDupCheck(pushSendType);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.getError", null, LocaleContextHolder.getLocale()) );//"오류가 발생했습니다. 계속해서 같은 오류발생시 관리자에게 문의하십시오."
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.useSenderCode", null, LocaleContextHolder.getLocale()) ); //사용할수 있는 발송자 코드입나다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.notUseSenderCode", null, LocaleContextHolder.getLocale()) ); //사용할수 없는 발송자 코드입나다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="sendType/regist" , method=RequestMethod.POST)
	public String pushSenTypeRegistPost( PushSendTypeDto pushSendType ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushSendTypeManage.InsertSendType(pushSendType);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale())  ); //"등록 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="sendType/edit" , method=RequestMethod.POST)
	public String pushSenTypeEditPost( PushSendTypeDto pushSendType ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushSendTypeManage.UpdateSendType(pushSendType);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="sendType/delete" , method=RequestMethod.POST)
	public String pushSenTypeDeletePost( PushSendTypeDto pushSendType ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushSendTypeManage.DeleteSendType(pushSendType);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}

    @RequestMapping(value="receiverApply" , method=RequestMethod.POST)
    public ModelAndView receiverApplyPost(HttpServletRequest request,@RequestParam Map<String,String> reqMap){

        ModelAndView mv = new ModelAndView("jsonView"); //Ajax로 콜하였으므로 JSON BodyString 던져 준다
        Map<String, Object> resultMap = new HashMap<String, Object>();
        SingleData paramData = new SingleData();
        resultMap = sendIF(DEFAULT_RECEIVER_HOST + "/rcv_reload_serviceCode.ctl", paramData);

        mv.addObject("callbackData",resultMap);
        return mv;
    }

    // 인터페이스 전송
    public Map<String,Object> sendIF(String url, SingleData paramData){

        PushUrlConnect PushUrlCon   = new PushUrlConnect();
        SingleData returnData           = new SingleData();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        PushUrlCon.setDoInput(true);
        PushUrlCon.setDoOutput(true);
        PushUrlCon.setDoOutput(true);
        PushUrlCon.setRequestMethod("POST");
        PushUrlCon.setConnectTimeout(5000); // 5초
        PushUrlCon.setReadTimeout(5000); //1분대기
        PushUrlCon.setReadEncodingType("UTF-8");

        PushUrlCon.setParam  = paramData;
        PushUrlCon.setUrl    = url;

        try {
            returnData = PushUrlCon.getUrlConnect();
            String resHttpBodyRaw = returnData.getString("RESULT");

            JSONObject jsonObject = new JSONObject(resHttpBodyRaw);

            JSONObject returnHeadJson = jsonObject.getJSONObject("HEADER");
            JSONObject returnBodyJson= jsonObject.getJSONObject("BODY");

            String resultCode = returnHeadJson.getString("RESULT_CODE");
            String resultMsg = returnHeadJson.getString("RESULT_BODY");

            if(resultCode!=null && resultCode.equals("0000")){ //발송성공
                resultMap.put("resultCode","0000");
            }else{ //발송 실패
                resultMap.put("resultCode",resultCode);
            }
            resultMap.put("resultMsg",resultMsg);

            Map<String,Object> bodyMap = new Gson().fromJson(returnBodyJson.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
            resultMap.put("bodyData",bodyMap);
        } catch (Exception e) {
            resultMap.put("resultCode","500");
            resultMap.put("resultMsg",messageSource.getMessage("menu.push.controller.notConnect", null, LocaleContextHolder.getLocale())); //"리시버와 연결되지 않았거나 데이타 오류입니다."
            logger.error("Exception caught.", e);
        }finally{

        }
        return resultMap;
    }
}
