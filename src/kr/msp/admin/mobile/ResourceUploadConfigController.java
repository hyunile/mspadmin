package kr.msp.admin.mobile;

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
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.mobile.resourceConfig.dto.ResourceExtDto;
import kr.msp.admin.mobile.resourceConfig.service.MobileResourceConfigService;

@Controller
@RequestMapping(value="admin/mobile")
public class ResourceUploadConfigController {

	private final static Logger logger = LoggerFactory.getLogger(ResourceUploadConfigController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	MobileResourceConfigService mobileResourceCongif;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@RequestMapping(value="resourceConfig",method=RequestMethod.GET)
	public String ResourceConfigGet( Model model ,HttpServletRequest request){
		
		ResourceExtDto resourceExt = new ResourceExtDto();
		resourceExt.setPAGE_NUM(1);
		resourceExt.setPAGE_SIZE(row_size);
		
		List<ResourceExtDto> resourceExeList = mobileResourceCongif.SelectResourceConfig(resourceExt);
		
		model.addAttribute("resourceExeList", resourceExeList);
		model.addAttribute("R_PAGE_NUM",resourceExt.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/mobile/resourceConfig/resourceConfigMain";
	}
	
	@RequestMapping(value="resourceConfig",method=RequestMethod.POST)
	public String ResourceConfigPost( Model model ,HttpServletRequest request, ResourceExtDto resourceExt){
		
		List<ResourceExtDto> resourceExeList = mobileResourceCongif.SelectResourceConfig(resourceExt);
		
		model.addAttribute("resourceExeList", resourceExeList);
		model.addAttribute("R_PAGE_NUM",resourceExt.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",resourceExt.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/mobile/resourceConfig/resourceConfigList";
	}
	
	@RequestMapping(value="resourceConfig/regist",method=RequestMethod.GET)
	public String ResourceConfigRegistGet( Model model ,HttpServletRequest request){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/resourceConfig/resourceConfigRegist";
	}
	
	@RequestMapping(value="resourceConfig/edit",method=RequestMethod.GET)
	public String ResourceConfigEditGet( Model model ,HttpServletRequest request ,ResourceExtDto resourceExt){
		
		ResourceExtDto resourceExeOne = mobileResourceCongif.SelectResourceConfigOne(resourceExt);
		
		model.addAttribute("resourceExeOne", resourceExeOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/resourceConfig/resourceConfigRegist";
	}
	
	@ResponseBody
	@RequestMapping(value="resourceConfig/regist",method=RequestMethod.POST)
	public String ResourceConfigRegistPost( Model model , ResourceExtDto resourceExe , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
		
		resourceExe.setREG_ID((String) request.getSession().getAttribute("S_ID_USER"));
		
		try {
			result = mobileResourceCongif.InsertResourceConfig(resourceExe);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.getError", null, LocaleContextHolder.getLocale()) ); //오류가 발생했습니다. 계속해서 같은 오류발생시 관리자에게 문의하십시오.
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.sameExtension", null, LocaleContextHolder.getLocale()) );  //중복하는 확장자가 있습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successExtension", null, LocaleContextHolder.getLocale()) ); //확장자가 등록 되었습니다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="resourceConfig/edit",method=RequestMethod.POST)
	public String ResourceConfigEditPost( Model model , ResourceExtDto resourceExe , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
		
		resourceExe.setMOD_ID((String) request.getSession().getAttribute("S_ID_USER"));
		
		try {
			result = mobileResourceCongif.UpdateResourceConfig(resourceExe);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //수정에 실패했습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale())  ); //수정 되었습니다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="resourceConfig/delete",method=RequestMethod.POST)
	public String ResourceConfigDeletePost( Model model , ResourceExtDto resourceExe ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
				
		try {
			result = mobileResourceCongif.DeleteFesourceConfig(resourceExe);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //삭제 되었습니다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
