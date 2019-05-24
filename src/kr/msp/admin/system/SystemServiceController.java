package kr.msp.admin.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.system.service.dto.PushServiceDto;
import kr.msp.admin.system.service.dto.ServiceDto;
import kr.msp.admin.system.service.dto.ServiceParamDto;
import kr.msp.admin.system.service.dto.ServiceSubDto;
import kr.msp.admin.system.service.mapper.ServiceManageDao;
import kr.msp.admin.system.service.service.ServiceManageService;
import kr.msp.core.license.LicenseValidator;

@RequestMapping( value="admin/system")
@Controller
public class SystemServiceController {

	private final static Logger logger = LoggerFactory.getLogger(SystemServiceController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private ServiceManageService serviceManageService;

	@Autowired(required=true)
	private LicenseValidator licenseValidator;
	
	@Autowired(required=true)
	private SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	/**<pre>
	 *  서비스관리 메인
	 * </pre>
	 * @param model
	 * @return
	 */
	@RequestMapping(value="service" , method=RequestMethod.GET)
	public String serviceGet(@ModelAttribute("baseParam")ServiceParamDto serviceParam,  Model model){
		//ServiceParamDto serviceParam = new ServiceParamDto();
		serviceParam.setPAGE_NUM(1);
		serviceParam.setPAGE_SIZE(row_size);
		
		List<ServiceDto> serviceList = serviceManageService.SelectServiceList(serviceParam);
//		model.addAttribute("R_PAGE_NUM",serviceParam.getPAGE_NUM());
		model.addAttribute("R_PAGE_NUM",serviceParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("serviceList", serviceList);
		
		model.addAttribute("ENABLE_PUSH", serviceManageService.IsEnablePush());
		
		return "admin/system/service/serviceMain";
	}
	
	//서비스 리스트 페이징
	@RequestMapping(value="service" , method=RequestMethod.POST)
	public String servicePost( Model model, @ModelAttribute("baseParam") ServiceParamDto serviceParam){
		List<ServiceDto> serviceList = serviceManageService.SelectServiceList(serviceParam);
		model.addAttribute("serviceList", serviceList);
		model.addAttribute("R_PAGE_NUM",serviceParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",serviceParam.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		model.addAttribute("ENABLE_PUSH", serviceManageService.IsEnablePush());

		return "admin/system/service/serviceList";
	}
	
	//서비스 등록 화면
	@RequestMapping(value="service/regist" , method=RequestMethod.GET)
	public String serviceRegistGet( Model model){
		model.addAttribute("LICENSE_APP_IDS", licenseValidator.getAppIds());
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/service/serviceInfo";
	}
	
	//서비스 수정 화면
	@RequestMapping(value="service/edit" , method=RequestMethod.GET)
	public String serviceEditGet( Model model, ServiceParamDto serviceParam){
		ServiceDto serviceOne = serviceManageService.SelectServiceOne(serviceParam);
		model.addAttribute("serviceOne",serviceOne);
		model.addAttribute("LICENSE_APP_IDS", licenseValidator.getAppIds());
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/service/serviceInfo";
	}
	
	//앱아이디 중복 체크
	@ResponseBody
	@RequestMapping(value="service/check" , method=RequestMethod.POST)
	public String ServiceCheckPost(ServiceParamDto serviceParam) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.SelectAppIdCheck(serviceParam);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.system.controller.sameAppID", null, LocaleContextHolder.getLocale())); //동일한 앱아이디가 존재합니다.
		} else {
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.system.controller.useAppID", null, LocaleContextHolder.getLocale())); //사용가능한 앱아이디 입니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//서비스 등록
	@ResponseBody
	@RequestMapping( value="service/regist" , method=RequestMethod.POST)
	public String ServiceRegistPost(Model model, ServiceDto serviceDto ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		try {
			//result = serviceManageService.InsertService(serviceDto);
			result = serviceManageService.insertAllService(serviceDto);
			
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
	
	//서비스 수정
	@ResponseBody
	@RequestMapping( value="service/edit" , method=RequestMethod.POST)
	public String ServiceEditPost(Model model, ServiceDto serviceDto ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.UpdateService(serviceDto);
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
	
	//모바일 서비스 등록 화면
	@RequestMapping(value="service/mob/regist" , method=RequestMethod.GET)
	public String serviceMobRegistGet( Model model , ServiceParamDto serviceParam){
		
		ServiceDto serviceOne = serviceManageService.SelectServiceOne(serviceParam);
		model.addAttribute("serviceOne",serviceOne);
		model.addAttribute("serviceName","MOB_SVC");
		model.addAttribute("SVC_ID",serviceParam.getSVC_ID());
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/service/serviceWrite";
	}
	//PUSH 서비스 등록 화면
	@RequestMapping(value="service/push/regist" , method=RequestMethod.GET)
	public String servicePushRegistGet( Model model , ServiceParamDto serviceParam){
		
		ServiceDto serviceOne = serviceManageService.SelectServiceOne(serviceParam);
		model.addAttribute("serviceOne",serviceOne);
		model.addAttribute("serviceName","PUSH_SVC");
		model.addAttribute("SVC_ID",serviceParam.getSVC_ID());
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/service/serviceWrite";
	}
	//앱스토어 서비스 등록 화면
	@RequestMapping(value="service/sto/regist" , method=RequestMethod.GET)
	public String serviceStoRegistGet( Model model , ServiceParamDto serviceParam){
		
		ServiceDto serviceOne = serviceManageService.SelectServiceOne(serviceParam);
		model.addAttribute("serviceOne",serviceOne);
		model.addAttribute("serviceName","STO_SVC");
		model.addAttribute("SVC_ID",serviceParam.getSVC_ID());
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/service/serviceWrite";
	}
	//모바일 서비스 등록
	@ResponseBody
	@RequestMapping( value="service/mob/regist" , method=RequestMethod.POST)
	public String serviceMobRegistPost(Model model, ServiceSubDto serviceSub ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.InsertMobService(serviceSub);
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
	//PUSH 서비스 등록
	@ResponseBody
	@RequestMapping( value="service/push/regist" , method=RequestMethod.POST)
	public String servicePushRegistPost(Model model, PushServiceDto pushService ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.InsertPushService(pushService);
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
	//앱 서비스 등록
	@ResponseBody
	@RequestMapping( value="service/sto/regist" , method=RequestMethod.POST)
	public String serviceStoRegistPost(Model model, ServiceSubDto serviceSub ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.InsertStoService(serviceSub);
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
	
	//모바일 서비스 수정
	@ResponseBody
	@RequestMapping( value="service/mob/edit" , method=RequestMethod.POST)
	public String serviceMobEditPost(Model model, ServiceSubDto serviceSub ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.UpdateMobService(serviceSub);
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
	
	/**
	 * 스토어서비스 상태값 변경
	 * @param model
	 * @param serviceSub
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 3. 14. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping( value="service/sto/edit" , method=RequestMethod.POST)
	public String serviceStoEditPost(Model model, ServiceSubDto serviceSub ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			//result = serviceManageService.UpdatesStoService(serviceSub);

			ServiceManageDao serviceManage = this.sqlSessionTemplate.getMapper(ServiceManageDao.class);
			
			int storeServiceUseCount = 0;
			if(serviceSub.getUSE_YN().equals("Y")){
				storeServiceUseCount = serviceManage.getUseStoreServiceCount();
				if(storeServiceUseCount > 0)
					result = 999;
			}
			if(storeServiceUseCount == 0)
				result = serviceManageService.UpdatesStoService(serviceSub);
			
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result == 1 ){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
		}else if(result == 999 ){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.system.controller.usingService", null, LocaleContextHolder.getLocale()) ); //사용중인 스토어서비스가 존재합니다.\n스토어서비스는 한개만 사용가능합니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//PUSH 서비스 수정
	@ResponseBody
	@RequestMapping( value="service/push/edit" , method=RequestMethod.POST)
	public String servicePushEditPost(Model model, ServiceSubDto serviceSub ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = serviceManageService.UpdatePushService(serviceSub);
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
}
