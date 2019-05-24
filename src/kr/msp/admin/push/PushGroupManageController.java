package kr.msp.admin.push;

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

import kr.msp.admin.push.group.dto.PushUserGroupDto;
import kr.msp.admin.push.group.service.PushGroupManageService;
import kr.msp.admin.push.groupAuth.service.PushGroupAuthManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushGroupManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushGroupManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	PushGroupManageService pushGroupManageService;
	
	@Autowired(required=true)
	PushGroupAuthManageService pushGroupAuthManageService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="group" , method=RequestMethod.GET)
	public String pushGroupGet( Model model){
		PushUserGroupDto pushUserGroup = new PushUserGroupDto();
		pushUserGroup.setPAGE_NUM(1);
		pushUserGroup.setPAGE_SIZE(row_size);
		List<PushUserGroupDto> pushUserGroupList = pushGroupManageService.SelectGroupList(pushUserGroup);
		
		model.addAttribute( "pushUserGroupList", pushUserGroupList );
		model.addAttribute("R_PAGE_NUM",pushUserGroup.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/group/pushGroupMain";
	}
	
	
	@RequestMapping(value="group" , method=RequestMethod.POST)
	public String pushGroupPost( Model model , PushUserGroupDto pushUserGroup ){
		List<PushUserGroupDto> pushUserGroupList = pushGroupManageService.SelectGroupList(pushUserGroup);
		
		model.addAttribute( "pushUserGroupList", pushUserGroupList );
		model.addAttribute("R_PAGE_NUM",pushUserGroup.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",pushUserGroup.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/group/pushGroupList";
	}
	
	//사용자 등록 화면
	@RequestMapping(value="group/regist" , method=RequestMethod.GET)
	public String pushGroupRegistGet( Model model , PushUserGroupDto pushUserGroup ){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/group/pushGroupModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="group/regist" , method=RequestMethod.POST)
	public String pushGroupRegistPost( PushUserGroupDto pushUserGroup ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushGroupManageService.InsertPushGroup(pushUserGroup);
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
	
	//사용자 등록 화면
	@RequestMapping(value="group/edit" , method=RequestMethod.GET)
	public String pushGroupEditGet( Model model , PushUserGroupDto pushUserGroup ){
		
		PushUserGroupDto pushUserGroupOne = pushGroupManageService.SelectGroupOne(pushUserGroup);
		
		model.addAttribute("pushUserGroupOne",pushUserGroupOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/group/pushGroupModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="group/edit" , method=RequestMethod.POST)
	public String pushGroupEditPost( PushUserGroupDto pushUserGroup ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushGroupManageService.UpdatePushGroup(pushUserGroup);
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
	
	
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="group/delete" , method=RequestMethod.POST)
	public String pushGroupEditDelete( PushUserGroupDto pushUserGroup ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushGroupManageService.deletePushGroup(pushUserGroup);
			if(result>0){
				pushGroupAuthManageService.DeleteAllPushGroupAuth(pushUserGroup);
			}
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
