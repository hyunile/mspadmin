package kr.msp.admin.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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

import kr.msp.admin.system.notice.dto.NoticeAuthGroupDto;
import kr.msp.admin.system.service.dto.ServiceDto;
import kr.msp.admin.system.service.dto.ServiceParamDto;
import kr.msp.admin.system.service.service.ServiceManageService;
import kr.msp.admin.system.serviceAuth.dto.ServiceAuthDto;
import kr.msp.admin.system.serviceAuth.service.SystemServiceAuthService;

@RequestMapping( value="admin/system")
@Controller
public class systemServiceAuthController {

	private final static Logger logger = LoggerFactory.getLogger(systemServiceAuthController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	ServiceManageService serviceManageService;
	
	@Autowired(required=true)
	SystemServiceAuthService systemServiceAuthService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="svcauth" , method=RequestMethod.GET)
	public String serviceAuthGet( Model model){
		ServiceParamDto serviceParam = new ServiceParamDto();
		serviceParam.setPAGE_NUM(1);
		serviceParam.setPAGE_SIZE(row_size);
		
		model.addAttribute("R_PAGE_NUM",serviceParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		List<ServiceDto> serviceList = serviceManageService.SelectServiceList(serviceParam);
		
		model.addAttribute("serviceList", serviceList);
		return "admin/system/serviceAuth/serviceAuthMain";
	}
	
	//서비스 리스트 페이징
	@RequestMapping(value="svcauth" , method=RequestMethod.POST)
	public String serviceAuthPost( Model model, ServiceParamDto serviceParam){
		serviceParam.setPAGE_SIZE(row_size);
		List<ServiceDto> serviceList = serviceManageService.SelectServiceList(serviceParam);
		model.addAttribute("serviceList", serviceList);
		model.addAttribute("layout", "layout/null.vm" );
		model.addAttribute("R_PAGE_NUM",serviceParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/system/serviceAuth/serviceAuthList";
	}
	
	//사용자 권한그룹 조회
	@RequestMapping(value="svcauth/grp" , method=RequestMethod.POST)
	public String serviceAuthGrpPost( Model model, ServiceAuthDto serviceAuth ){
		
		List<NoticeAuthGroupDto> systemAuthGroupList = systemServiceAuthService.SelectServiceAuthGroupList(serviceAuth);
		
		model.addAttribute("systemAuthGroupList",systemAuthGroupList);
		model.addAttribute("R_SVC_ID",serviceAuth.getSVC_ID());
		model.addAttribute("layout", "layout/null.vm" );
		
		return "admin/system/serviceAuth/userAuthList";
	}
	
	//서비스 권한 등록
	@ResponseBody
	@RequestMapping(value="svcauth/regist" , method=RequestMethod.POST)
	public String serviceAuthRegistPost( ServiceAuthDto serviceAuth ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = systemServiceAuthService.InsertServiceAuth(serviceAuth);
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
	
	//서비스 권한 삭제
	@ResponseBody
	@RequestMapping(value="svcauth/delete" , method=RequestMethod.POST)
	public String serviceAuthDeletetPost( ServiceAuthDto serviceAuth ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = systemServiceAuthService.DelectServiceAuth(serviceAuth);
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
