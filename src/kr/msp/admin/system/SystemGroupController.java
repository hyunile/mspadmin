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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.system.group.dto.AuthGroupDto;
import kr.msp.admin.system.group.dto.AuthGroupParamDto;
import kr.msp.admin.system.group.dto.FirstMenuDto;
import kr.msp.admin.system.group.service.SystemGroupService;
import kr.msp.admin.system.user.dto.AuthGroupSearchDto;

@Controller
@RequestMapping( value = "admin/system")
public class SystemGroupController {

	private final static Logger logger = LoggerFactory.getLogger(SystemGroupController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	SystemGroupService systemGroupService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping( value = "group" , method = RequestMethod.GET)
	public String GroupGet(Model model, AuthGroupParamDto authGroupParam ){
		
		authGroupParam.setPAGE_NUM(1);
		authGroupParam.setPAGE_SIZE(row_size);
		
		List<AuthGroupDto> authGroupList = systemGroupService.SelectAuthGroup(authGroupParam);
		
		model.addAttribute("R_PAGE_NUM",authGroupParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		model.addAttribute("authGroupList", authGroupList);
		return "admin/system/group/groupMain";
	}
	
	@RequestMapping( value = "group" , method = RequestMethod.POST)
	public String GroupPost(Model model, AuthGroupParamDto authGroupParam , AuthGroupSearchDto authGroupSearch){
		if(!authGroupSearch.getSE_NM_GROUP().equals("")){
			authGroupParam.setNM_GROUP(authGroupSearch.getSE_NM_GROUP());
		}
		
		List<AuthGroupDto> authGroupList = systemGroupService.SelectAuthGroup(authGroupParam);
		
		model.addAttribute("R_PAGE_NUM",authGroupParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",authGroupParam.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("authGroupList", authGroupList);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/group/groupList";
	}
	
	
	
	@RequestMapping( value = "group/regist" , method = RequestMethod.GET)
	public String GroupRegistGet(Model model ){
		
		List<FirstMenuDto> firstMenuList = systemGroupService.FirstMenuSelectBoxList();
		
		model.addAttribute("firstMenuList", firstMenuList);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/group/groupModify";
	}
	
	//그룹등록
	@ResponseBody
	@RequestMapping( value = "group/regist" , method = RequestMethod.POST)
	public String GroupRegistPost( AuthGroupDto authGroupDto,HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		authGroupDto.setID_INSERT( (String) request.getSession().getAttribute("S_ID_USER") );
		int result = 0;
		result = systemGroupService.InsertAuthGroup(authGroupDto);
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
	
	//그룹 수정 화면
	@RequestMapping( value = "group/edit" , method = RequestMethod.GET)
	public String GroupRegistEdit(Model model, AuthGroupParamDto authGroupParam ){
		
		List<FirstMenuDto> firstMenuList = systemGroupService.FirstMenuSelectBoxList();
		
		AuthGroupDto authGroupOne = systemGroupService.SelectOneAuthGroup(authGroupParam);
		
		model.addAttribute("authGroupOne",authGroupOne);
		model.addAttribute("firstMenuList", firstMenuList);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/system/group/groupModify";
	}
	
	//그룹 수정
	@ResponseBody
	@RequestMapping( value = "group/edit" , method = RequestMethod.POST)
	public String GroupEditPost( AuthGroupDto authGroupDto,HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		authGroupDto.setID_UPDATE( (String) request.getSession().getAttribute("S_ID_USER") );
		
		int result = 0;
		
		result = systemGroupService.UpdateAuthGroup(authGroupDto);
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
	
	//그룹 삭제
	@ResponseBody
	@RequestMapping( value = "group/delete" , method = RequestMethod.POST)
	public String GroupDeletePost( AuthGroupDto authGroupDto,HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException {
		
		int result = 0;
		
		try {
			result = systemGroupService.DeleteAuthGroup(authGroupDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else if( result == 0 ) {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
		} else if( result == -1 ){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.system.controller.deleteProblem", null, LocaleContextHolder.getLocale())); //삭제에 문제가 생겼습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
