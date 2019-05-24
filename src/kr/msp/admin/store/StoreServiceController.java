package kr.msp.admin.store;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.mobile.ResourceUploadController;
import kr.msp.admin.store.service.dto.ServiceDto;
import kr.msp.admin.store.service.service.StoreServiceService;

@Controller
public class StoreServiceController {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceUploadController.class);
	
	
	@Autowired(required=true)
	StoreServiceService serviceManageService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	//스토어 서비스 화면 로딩 조회 
	@RequestMapping(value="admin/store/service" , method=RequestMethod.GET)
	public String serviceGet( Model model){
		ServiceDto serviceInfo = new ServiceDto();
	    
		serviceInfo.setUSE_YN("1"); //로딩시 사용중인 서비스 선택
		serviceInfo = serviceManageService.selectStoreServiceInfo(serviceInfo);
	    //List<ServiceDto> serviceList = serviceManageService.SelectBoxServiceList();
	    if(serviceInfo == null){
	    	serviceInfo = new ServiceDto();
	    	serviceInfo.setAPP_ID("");
	    	serviceInfo.setSVC_NM("");
	    }	
	    model.addAttribute("serviceInfo", serviceInfo);
		return "admin/store/service/serviceMain";
	}
	
	
	//스토어 서비스 조회 상세
	@RequestMapping(value="admin/store/service/selectInfo" , method=RequestMethod.POST)
	public String servicePost( Model model, ServiceDto serviceDto, HttpServletRequest request ){
		
		ServiceDto serviceInfo = new ServiceDto();
	
		//스토어 앱서비스 정보 조회 [TB_STO_SVC]
		
		serviceDto.setUSE_YN("Y"); //로딩시 사용중인 서비스 선택
		serviceInfo = serviceManageService.selectStoreServiceInfo(serviceDto); 
		
		//앱스토어 서비스에 값이 없으면  서비스 테이블에서 값을 가져온다.
		if(serviceInfo == null ){
			serviceInfo = serviceManageService.selectServiceInfo(serviceDto); 
		}
		
	  
		model.addAttribute("serviceInfo", serviceInfo); //사용중인 store service 정보
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/store/service/serviceInfo";
	}
	
	
	//앱 서비스 저장
	@ResponseBody
	@RequestMapping( value="admin/store/service/save" , method=RequestMethod.POST)
	public String saveStoreService(Model model, ServiceDto serviceDto, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		String dupCheck = ""; //APP_ID값 중복 체크
		
		try {
			String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
			serviceDto.setREG_ID(S_ID_USER);
			serviceDto.setMOD_ID(S_ID_USER);
			
			ServiceDto serviceInfo = new ServiceDto();
		   
			//svc_id가 있는지 체크
			serviceInfo = serviceManageService.selectStoreServiceInfo(serviceDto); 
		    
			//앱스토어  서비스가 존재하지 않으면
			if(serviceInfo == null ){
				//등록전에 APP_ID가 중복되는게 있는지 체크
				serviceInfo = serviceManageService.selectServiceInfo(serviceDto); 
				
				if(serviceInfo != null){ //값이 있으면 APP_ID값 중복
					dupCheck = "DUP";
				}else{
					//SVC_ID 가져오기
					int svcId = serviceManageService.selectSvcId();
					serviceDto.setSVC_ID(String.valueOf(svcId));
					
					result = serviceManageService.insertService(serviceDto);
					result = serviceManageService.deleteStoreService(serviceDto);  //기존 서비스는 전부 삭제처리[USE_YN => 'N' 으로 모두UPDATE]
					result = serviceManageService.insertStoreService(serviceDto);   //신규서비스 INSERT
				}	
			}else{
				
				result = serviceManageService.updateStoreService(serviceDto);
			}
			
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if("DUP".equals(dupCheck)){
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.store.controller.sameAPPID", null, LocaleContextHolder.getLocale())); //APP ID가 중복되었습니다.
			
		}else{
		
			if(result > 0){
				map.put("result", result);
				map.put("msg",messageSource.getMessage("menu.store.device.alert.save", null, LocaleContextHolder.getLocale()) ); //"저장 되었습니다."
			} else {
				map.put("result", result);
				map.put("msg",messageSource.getMessage("menu.store.device.alert.saveFail", null, LocaleContextHolder.getLocale()) ); // "저장에 실패했습니다."
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
		
}
