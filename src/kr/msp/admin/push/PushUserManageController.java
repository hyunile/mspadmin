package kr.msp.admin.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.msp.admin.base.collection.SingleData;
import kr.msp.admin.base.urlconnect.PushUrlConnect;
import kr.msp.admin.base.utils.FileDownloadUtil;
import kr.msp.admin.common.dto.ResultDto;
import kr.msp.admin.push.user.dto.PushUserDto;
import kr.msp.admin.push.user.service.PushUserManageService;


@RequestMapping(value="admin/push")
@Controller
public class PushUserManageController {

    //로거 설정
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	//고객 엑셀 대량등로 샘플 경로
	private String fileName = "/excelSampleFile.xlsx";
	//페이지 사이즈
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
    private @Value("${admin.push.receiver.host}") String DEFAULT_RECEIVER_HOST;	//리시버 호스트

	@Autowired(required=true)
	private PushUserManageService pushUserManageService;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@RequestMapping(value="user" , method=RequestMethod.GET)
	public String pushUserGet( Model model){
		PushUserDto pushUser = new PushUserDto();
		pushUser.setPAGE_NUM(1);
		pushUser.setPAGE_SIZE(row_size);
		List<PushUserDto> pushUserList = pushUserManageService.SelectPushUserService(pushUser);
        int TotalCnt = 0;
        if(pushUserList!=null && pushUserList.size()>0){
            TotalCnt = pushUserList.get(0).getTOT_CNT();
        }
		model.addAttribute("TotalCnt",TotalCnt);
		model.addAttribute( "pushUserList", pushUserList );
		model.addAttribute("R_PAGE_NUM",pushUser.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/user/pushUserMain";
	}
	
	@RequestMapping(value="user", method=RequestMethod.POST)
	public String pushUserPost(Model model, PushUserDto pushUser){
		List<PushUserDto> pushUserList = pushUserManageService.SelectPushUserService(pushUser);
		
		model.addAttribute( "pushUserList", pushUserList );
		model.addAttribute("R_PAGE_NUM",pushUser.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",pushUser.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/user/pushUserList";
	}
	
	//사용자 등록 화면
	@RequestMapping(value="user/regist", method=RequestMethod.GET)
	public String pushUserRegistGet(Model model){
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/push/user/pushUserModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="user/regist" , method=RequestMethod.POST)
	public String pushUserRegistPost( PushUserDto pushUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushUserManageService.InsertPushUser(pushUser);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 중복체크
	@ResponseBody
	@RequestMapping(value="user/check" , method=RequestMethod.POST)
	public String pushUserCheckPost( PushUserDto pushUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushUserManageService.PushUserCheck(pushUser);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.push.controller.notUseCUID", null, LocaleContextHolder.getLocale())); //"사용할수 없는 CUID 입니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.useCUID", null, LocaleContextHolder.getLocale())); //"사용할수 있는 CUID 입니다."
		} 
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 수정 화면
	@RequestMapping(value="user/edit", method=RequestMethod.GET)
	public String pushUserEditGet(Model model,PushUserDto pushUser){
		
		PushUserDto pushUserOne = pushUserManageService.SelectPushUserOne(pushUser);
		
		model.addAttribute("pushUserOne", pushUserOne);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/push/user/pushUserModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="user/edit" , method=RequestMethod.POST)
	public String pushUserEditPost( PushUserDto pushUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushUserManageService.UpdatePushUser(pushUser);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 삭제
	@ResponseBody
	@RequestMapping(value="user/delete" , method=RequestMethod.POST)
	public String pushUserDeletePost( PushUserDto pushUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushUserManageService.updateUserDelete(pushUser);
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
	
	//사용자 등록 화면
	@RequestMapping(value="user/file/regist", method=RequestMethod.GET)
	public String pushUserFileRegistGet(Model model){
			
		model.addAttribute("layout", "layout/null.vm");
		return "admin/push/user/pushUserFile";
	}
	//샘플 다운로드
	@RequestMapping(value="user/file/download" , method=RequestMethod.POST)
	public void pushUserFileDoqnloadPost(HttpServletRequest request, HttpServletResponse response ) throws JsonGenerationException, JsonMappingException, IOException{
        String realAbsWebRootPath = request.getSession().getServletContext().getRealPath("/");
        String fileName = realAbsWebRootPath + this.fileName;
        logger.debug("################################## realAbsWebRootPath: {}", fileName);
        FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
		fileDownloadUtil.fileDownloadExcel(fileName,response);
			
	}
	
	//사용자 대량 등록
	@ResponseBody
	@RequestMapping(value="user/file/regist" , method=RequestMethod.POST)
	public String pushUserEditPost( @RequestParam("USER_FILE") MultipartFile USER_FILE ) throws JsonGenerationException, JsonMappingException, IOException{
		System.out.println(USER_FILE.getName());
		ResultDto result =  pushUserManageService.InsertPushUserAll(USER_FILE);
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		System.out.println(result.getMsg());
		
		map.put("result", result.getResult());
		map.put("msg",result.getMsg() );
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		byte[] bytes = data.getBytes("utf-8");
        data = new String(bytes,"ISO-8859-1");
        
		return data;
	}

    /**
     * 리시버리로드
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="receiverReload", method= RequestMethod.POST)
    public ModelAndView receiverReloadPost(HttpServletRequest request,@RequestParam Map<String,String> reqMap){

        ModelAndView mv = new ModelAndView("jsonView"); //Ajax로 콜하였으므로 JSON BodyString 던져 준다
        Map<String, Object> resultMap = new HashMap<String, Object>();
        SingleData paramData = new SingleData();
        resultMap = sendIF(DEFAULT_RECEIVER_HOST + "/rcv_reload_pushUserData.ctl", paramData);

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
