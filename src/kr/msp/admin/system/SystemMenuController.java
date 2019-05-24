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

import kr.msp.admin.common.dto.MenuDto;
import kr.msp.admin.common.menu.service.AdminMenuService;
import kr.msp.admin.system.menu.dto.MenuParamDto;
import kr.msp.admin.system.menu.service.SystemMenuService;

@Controller
@RequestMapping( value= "admin/system")
public class SystemMenuController {

	private final static Logger logger = LoggerFactory.getLogger(SystemMenuController.class);
	
	@Autowired(required=true)
	SystemMenuService systemMenuService;
	
	@Autowired(required=true)
	private AdminMenuService adminMenuService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping( value = "menu" , method = RequestMethod.GET)
	public String MenuGet(Model model){
		
		
//		List<MenuDto> menuList = systemMenuService.SelectMenuList();
		List<MenuDto> menuList = adminMenuService.getAllMenuList();
		model.addAttribute( "menuList", menuList );
		return "admin/system/menu/menuMain";
	}
	
	@RequestMapping( value = "menu" , method = RequestMethod.POST)
	public String MenuPost(Model model){
//		List<MenuDto> menuList = systemMenuService.SelectMenuList();
		List<MenuDto> menuList = adminMenuService.getAllMenuList();
		model.addAttribute( "menuList", menuList );
		model.addAttribute( "layout", "layout/null.vm" );
		return "admin/system/menu/menuList";
	}
	
	//메뉴 상세정보
	@RequestMapping(value="menu/info", method = RequestMethod.POST)
	public String MenuInfoPost( Model model, MenuParamDto menuParam ){
		
		MenuDto menuOne = systemMenuService.SelectMenuInfo(menuParam);
		
		model.addAttribute("menuOne", menuOne);
		model.addAttribute( "layout", "layout/null.vm" );
		return "admin/system/menu/menuInfo";
	}
	
	//메뉴 수정
	@ResponseBody
	@RequestMapping(value="menu/edit", method = RequestMethod.POST)
	public String MenuEditPost( Model model, MenuDto menu ,HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		menu.setID_UPDATE((String) request.getSession().getAttribute("S_ID_USER"));
		
		try {
			result = systemMenuService.UpdateMenu(menu);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("ID_MENU", menu.getID_MENU() );
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
	
	//메뉴 상세정보
	@RequestMapping(value="menu/regist", method = RequestMethod.GET)
	public String MenuRegistGet( Model model ){
		
		model.addAttribute( "layout", "layout/null.vm" );
		return "admin/system/menu/menuInfo";
	}
	
	//메뉴 등록
	@ResponseBody
	@RequestMapping(value="menu/regist", method = RequestMethod.POST)
	public String MenuRegistPost( Model model, MenuDto menu ,HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		menu.setID_INSERT((String) request.getSession().getAttribute("S_ID_USER"));
		
		try {
			result = systemMenuService.InsertMenu(menu);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		String ID_MENU = systemMenuService.SelectIdMenu();
		map.put("ID_MENU", ID_MENU );
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
	
	//메뉴 삭제
	@ResponseBody
	@RequestMapping(value="menu/delete", method = RequestMethod.POST)
	public String MenuDeletePost( Model model, MenuDto menu ) throws IOException{
		
		int result = 0;
		
		try {
			result = systemMenuService.DeleteMenu(menu);
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
