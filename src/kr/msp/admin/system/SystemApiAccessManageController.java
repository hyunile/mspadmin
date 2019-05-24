package kr.msp.admin.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.system.apiAccess.dto.ApiAuthAccessDto;
import kr.msp.admin.system.apiAccess.dto.AuthAccessDto;
import kr.msp.admin.system.apiAccess.dto.RestApiDto;
import kr.msp.admin.system.apiAccess.service.ApiAccessManageService;

@RequestMapping(value="admin/system")
@Controller
public class SystemApiAccessManageController {

	private final static Logger logger = LoggerFactory.getLogger(SystemApiAccessManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private ApiAccessManageService apiAccessManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="apiAccess", method=RequestMethod.GET)
	public String ApiAccessGet( Model model ){
		
		//system 사용자 조회
		AuthAccessDto authAccess = new AuthAccessDto();
		authAccess.setPAGE_NUM(1);
		authAccess.setPAGE_SIZE(row_size);
		
		List<AuthAccessDto> authAccessList = apiAccessManage.GetAuthAccess(authAccess);
		
		model.addAttribute("authAccessList", authAccessList);
		model.addAttribute("R_PAGE_NUM",authAccess.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/system/apiAccess/apiAccessMain";
	}
	
	@RequestMapping(value="apiAccess", method=RequestMethod.POST)
	public String ApiAccessPost( Model model, AuthAccessDto authAccess ){
		
		authAccess.setPAGE_SIZE(row_size);
		
		List<AuthAccessDto> authAccessList = apiAccessManage.GetAuthAccess(authAccess);
		
		model.addAttribute("authAccessList", authAccessList);
		model.addAttribute("R_PAGE_NUM",authAccess.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		
		return "admin/system/apiAccess/apiAccessList";
	}
	
	@RequestMapping(value="apiAccess/regist", method=RequestMethod.GET)
	public String ApiAccessRegistGet( Model model, AuthAccessDto authAccess ){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/system/apiAccess/apiAccessModify";
	}
	
	@RequestMapping(value="apiAccess/edit", method=RequestMethod.GET)
	public String ApiAccessEditGet( Model model, AuthAccessDto authAccess ){
		
		AuthAccessDto authAccessOne = apiAccessManage.GetAuthAccessOne(authAccess);
		
		model.addAttribute("authAccessOne",authAccessOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/system/apiAccess/apiAccessModify";
	}
	
	@ResponseBody
	@RequestMapping(value="apiAccess/regist", method=RequestMethod.POST)
	public String ApiAccessRegistPost( Model model, AuthAccessDto authAccess ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = -1;
		int check_su = 0;
		if( authAccess.getACCESS_KEY() == null || authAccess.getACCESS_KEY().equals("") ){
			authAccess.setACCESS_KEY( UUID.randomUUID().toString() ); 
		}
		
		System.out.println( authAccess.getACCESS_KEY() );
		check_su = apiAccessManage.AccessKeycheck(authAccess);
		
		if(check_su <= 0){
			result = 0;
		}
		
		if( result == 0){
			try {
				result = apiAccessManage.InsertAuthAccess(authAccess);
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.system.controller.sameAccess", null, LocaleContextHolder.getLocale()) ); //중복하는 ACCESS KEY가 있습니다.
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."           
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="apiAccess/edit", method=RequestMethod.POST)
	public String ApiAccessEditPost( Model model, AuthAccessDto authAccess ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.UpdateAuthAccess(authAccess);
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
	
	@ResponseBody
	@RequestMapping(value="apiAccess/delete", method=RequestMethod.POST)
	public String ApiAccessDeletePost( Model model, AuthAccessDto authAccess ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.DeleteAuthAccess(authAccess);
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
	
	@RequestMapping(value="apiAccess/restApi", method=RequestMethod.GET)
	public String ApiAccessRestApiGet( Model model ){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/system/apiAccess/restApiList";
	}
	
	@RequestMapping(value="apiAccess/restApi", method=RequestMethod.POST)
	public String ApiAccessRestApiPost( Model model, AuthAccessDto authAccess ){
		
		authAccess.setPAGE_SIZE(row_size);
		
		List<RestApiDto> restApiList = apiAccessManage.GetRestAPI(authAccess);
		
		model.addAttribute("restApiList", restApiList);
		model.addAttribute("R_PAGE_NUM",authAccess.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		
		return "admin/system/apiAccess/restApiList";
	}
	
	@RequestMapping(value="apiAccess/restApi/regist", method=RequestMethod.GET)
	public String ApiAccessRestRegistGet( Model model  ){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/system/apiAccess/restApiModify";
	}
	
	@RequestMapping(value="apiAccess/restApi/edit", method=RequestMethod.GET)
	public String ApiAccessRestEditGet( Model model, RestApiDto restApi ){
		
		RestApiDto restApiOne = apiAccessManage.GetRestAPIone(restApi);
		
		model.addAttribute("restApiOne",restApiOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/system/apiAccess/restApiModify";
	}
	
	@ResponseBody
	@RequestMapping(value="apiAccess/restApi/regist", method=RequestMethod.POST)
	public String ApiAccessRestRegistPost( Model model, RestApiDto restApi ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.InsertRestAPI(restApi);
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
	
	@ResponseBody
	@RequestMapping(value="apiAccess/restApi/edit", method=RequestMethod.POST)
	public String ApiAccessRestEditPost( Model model, RestApiDto restApi ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.UpdateRestAPI(restApi);
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
	
	@ResponseBody
	@RequestMapping(value="apiAccess/restApi/delete", method=RequestMethod.POST)
	public String ApiAccessRestDeletePost( Model model, RestApiDto restApi ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.DeleteRestAPI(restApi);
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
	
	@ResponseBody
	@RequestMapping(value="apiAccess/authAccess/regist", method=RequestMethod.POST)
	public String ApiAccessAuthRegistPost( Model model, ApiAuthAccessDto apiAuthAccess ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.InsertApiAuthAccess(apiAuthAccess);
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
	
	@ResponseBody
	@RequestMapping(value="apiAccess/authAccess/delete", method=RequestMethod.POST)
	public String ApiAccessAuthDeletePost( Model model, ApiAuthAccessDto apiAuthAccess ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			result = apiAccessManage.DeleteApiAuthAccess(apiAuthAccess);
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
