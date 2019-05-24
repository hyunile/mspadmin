package kr.msp.admin.system;

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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.system.group.dto.AuthGroupDto;
import kr.msp.admin.system.menuAuth.dto.MenuAuthDto;
import kr.msp.admin.system.menuAuth.dto.MenuAuthListDto;
import kr.msp.admin.system.menuAuth.service.SystemMenuAuthService;

@Controller
@RequestMapping(value="admin/system")
public class SystemMenuAuthController {

	private final static Logger logger = LoggerFactory.getLogger(SystemMenuAuthController.class);
	
	@Autowired(required=true)
	SystemMenuAuthService systemMenuAuthService;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@RequestMapping(value="menuAuth" , method = RequestMethod.GET)
	public String MenuAuthGet(Model model){
		
		List<AuthGroupDto> authGroupList = systemMenuAuthService.SelectAuthGroupAll();
		model.addAttribute("authGroupList", authGroupList);
		
		return "admin/system/menuAuth/menuAuthMain";
	}
	
	//메뉴 권한 정보 화면
	@RequestMapping(value="menuAuth/edit" , method = RequestMethod.GET)
	public String MenuAuthEditGet(Model model,MenuAuthDto menuAuthDto){
		List<MenuAuthDto> menuAuthList = systemMenuAuthService.SelectMenuAuthGroup(menuAuthDto);
		
		model.addAttribute("menuAuthList", menuAuthList);
		model.addAttribute("layout","layout/null.vm");
		return "admin/system/menuAuth/menuAuthInfo";
	}
	
	//메뉴 권한 등록
	@ResponseBody
	@RequestMapping(value="menuAuth/delete", method = RequestMethod.POST)
	public String MenuAuthDelete(MenuAuthListDto menuAuthListDto, MenuAuthDto menuAuthDto, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		
		List<MenuAuthDto> menuAuthList = menuAuthListDto.getMenuAuthList();
		
		int result = 0;
		
		try {
			result = systemMenuAuthService.InsertMenuAuth(menuAuthList,menuAuthDto,request);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	
}
