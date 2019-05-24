package kr.msp.admin.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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

import kr.msp.admin.push.appManage.dto.PnsSelectVarDto;
import kr.msp.admin.push.appManage.dto.PushPnsDto;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.appManage.service.PushAppManageService;

@RequestMapping( value="admin/push")
@Controller
public class PushAppManageController {

    private final static Logger logger = LoggerFactory.getLogger(PushAppManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	PushAppManageService pushAppManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="appManage" , method=RequestMethod.GET)
	public String pushAppMangeGet( Model model, HttpServletRequest request ){
		PushServiceDto pushService = new PushServiceDto();
		pushService.setPAGE_NUM(1);
		pushService.setPAGE_SIZE(row_size);
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = pushAppManage.SelectPushService(pushService);
		
		model.addAttribute( "pushServiceList", pushServiceList );
		model.addAttribute("R_PAGE_NUM",pushService.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/appManage/appManageMain";
	}
	
	@RequestMapping(value="appManage" , method=RequestMethod.POST)
	public String pushAppMangePost( Model model, HttpServletRequest request, PushServiceDto pushService){
		
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = pushAppManage.SelectPushService(pushService);
		
		model.addAttribute( "pushServiceList", pushServiceList );
		model.addAttribute("R_PAGE_NUM",pushService.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",pushService.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/appManage/appManageList";
	}
	
	//PUSH 서비스 상세 화면
	@RequestMapping(value="appManage/edit" , method=RequestMethod.GET)
	public String pushAppMangeEditGet( Model model, PushServiceDto pushService){
		
		PushServiceDto pushServiceOne = pushAppManage.SelectPushServiceOne(pushService);
		
		model.addAttribute( "pushServiceOne", pushServiceOne );
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/appManage/appManageModify";
	}
	
	//PUSH 서비스 수정
	@ResponseBody
	@RequestMapping(value="appManage/edit" , method=RequestMethod.POST)
	public String pushAppMangeEditPost( PushServiceDto pushService ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushAppManage.UpdatePushService(pushService);
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
	
	//pns 리스트 조회
	@RequestMapping(value="appManage/pns" , method=RequestMethod.GET)
	public String pushAppMangePnsGet( Model model, PushPnsDto pushPns ){
			
		List<PushPnsDto> pushPnsList = pushAppManage.SelectPnsList(pushPns);
			
		model.addAttribute( "pushPnsList", pushPnsList );
		model.addAttribute( "CLICK_APPID",pushPns.getAPPID() );
		model.addAttribute( "layout","layout/null.vm" );
			
		return "admin/push/appManage/pushPnsList";
	}
	
	//pns 등록 화면
	@RequestMapping(value="appManage/pns/regist" , method=RequestMethod.GET)
	public String pushAppMangePnsRegistGet( Model model, PushPnsDto pushPns ){
				
		List<PnsSelectVarDto> PnsSelectVarList = pushAppManage.SelectPnsVarList();
				
		model.addAttribute( "PnsSelectVarList", PnsSelectVarList );
		model.addAttribute( "PNS_APPID", pushPns.getAPPID());
		model.addAttribute("layout","layout/null.vm");
				
		return "admin/push/appManage/pushPnsModify";
	}
	
	//pns 등록
	@ResponseBody
	@RequestMapping(value="appManage/pns/regist" , method=RequestMethod.POST)
	public String pushAppMangePnsWindowGet( PushPnsDto pushPns) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		

		result = pushAppManage.InsertPushPns(pushPns);
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		};
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		byte[] bytes = data.getBytes("utf-8");
        data = new String(bytes,"ISO-8859-1");
		
		
		return data;
	}
	
	//pns 중복체크
	@ResponseBody
	@RequestMapping(value="appManage/pns/check" , method=RequestMethod.POST)
	public String pushAppMangePnsCheckPost( PushPnsDto pushPns ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushAppManage.PushPnsCheck(pushPns);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.noPNSID", null, LocaleContextHolder.getLocale()) ); //사용할수 없는 PNS ID 입니다. 
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.PNSID", null, LocaleContextHolder.getLocale()) ); //사용할수 있는 PNS ID 입니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//pns 등록 화면
	@RequestMapping(value="appManage/pns/edit" , method=RequestMethod.GET)
	public String pushAppMangePnsEditGet( Model model, PushPnsDto pushPns ){
				
		List<PnsSelectVarDto> PnsSelectVarList = pushAppManage.SelectPnsVarList();
		PushPnsDto pushPnsOne = pushAppManage.selectPnsOne(pushPns);
				
		model.addAttribute( "PnsSelectVarList", PnsSelectVarList );
		model.addAttribute( "pushPnsOne", pushPnsOne );
		model.addAttribute( "PNS_APPID", pushPns.getAPPID());
		model.addAttribute("layout","layout/null.vm");
				
		return "admin/push/appManage/pushPnsModify";
	}
	
	//pns 수정
	@ResponseBody
	@RequestMapping(value="appManage/pns/edit" , method=RequestMethod.POST)
	public String pushAppMangePnsEditPOST( PushPnsDto pushPns , @RequestParam("CERT_FILE") MultipartFile CERT_FILE ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			if(CERT_FILE != null){
				if(CERT_FILE.getSize() > 0){
					pushPns.setKEYSTORE(CERT_FILE.getOriginalFilename());
					pushPns.setCERT(CERT_FILE.getBytes());
				} else {
					pushPns.setCERT(null);
				}
			}
			result = pushAppManage.UpdatePushPns(pushPns);
		} catch (IOException e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		};
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		byte[] bytes = data.getBytes("utf-8");
        data = new String(bytes,"ISO-8859-1");
		
		return data;
	}
	
	//PUSH 서비스 삭제
	@ResponseBody
	@RequestMapping(value="appManage/pns/delete" , method=RequestMethod.POST)
	public String pushAppMangeDeletePost( PushPnsDto pushPns ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushAppManage.DeletePushPns(pushPns);
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
}
