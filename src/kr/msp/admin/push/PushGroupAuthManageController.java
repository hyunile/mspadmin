package kr.msp.admin.push;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import kr.msp.admin.push.groupAuth.dto.PushGroupAuthDto;
import kr.msp.admin.push.groupAuth.dto.PushGroupAuthListDto;
import kr.msp.admin.push.groupAuth.service.PushGroupAuthManageService;
import kr.msp.admin.push.user.dto.PushUserDto;

@RequestMapping( value="admin/push")
@Controller
public class PushGroupAuthManageController {
	private final static Logger logger = LoggerFactory.getLogger(PushGroupAuthManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	PushGroupAuthManageService pushGroupAuthManageService;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@RequestMapping(value="groupAuth" , method=RequestMethod.GET)
	public String pushGroupGet( Model model){
		
		List<PushUserGroupDto> pushUserGroupList = pushGroupAuthManageService.SelectPushGroupList();
		
		model.addAttribute( "pushUserGroupList", pushUserGroupList );
		
		return "admin/push/groupAuth/pushGroupAuthMain";
	}
	
	@RequestMapping(value="groupAuth/outuser" , method=RequestMethod.POST)
	public String pushGroupAuthOutUserPost( Model model,PushUserGroupDto pushUserGroup){
		
		pushUserGroup.setPAGE_SIZE(row_size);
		List<PushUserDto> pushOutUserList  = pushGroupAuthManageService.SelectOutUserList(pushUserGroup);
		
		model.addAttribute( "pushOutUserList", pushOutUserList );
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("R_PAGE_NUM",pushUserGroup.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/groupAuth/pushGroupAuthOutUserList";
	}
	//
	@RequestMapping(value="groupAuth/inuser" , method=RequestMethod.POST)
	public String pushGroupAuthInUserPost( Model model,PushUserGroupDto pushUserGroup){
		
		pushUserGroup.setPAGE_SIZE(row_size);
		List<PushUserDto> pushInUserList  = pushGroupAuthManageService.SelectInUserList(pushUserGroup);
		
		model.addAttribute( "pushInUserList", pushInUserList );
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("R_PAGE_NUM",pushUserGroup.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/groupAuth/pushGroupAuthInUserList";
	}
	
	//2015.09.17 cuid 중복체크 추가.
	private PushGroupAuthListDto removeDuplicateCuid(PushGroupAuthListDto pushGroupAuthList){
		Set<String> setCuid = new HashSet<String>();
		
		List<PushGroupAuthDto> resultList = new ArrayList<PushGroupAuthDto>(); 
		for( PushGroupAuthDto pushGroupAuthDto : pushGroupAuthList.getPushGroupAuthList()){
			if(!setCuid.contains(pushGroupAuthDto.getCUID())){
				setCuid.add(pushGroupAuthDto.getCUID());
				resultList.add(pushGroupAuthDto);
			}
		}
		pushGroupAuthList.setPushGroupAuthList(resultList);
		
		return pushGroupAuthList;
	}
	
	//그룹권한 등록
	@ResponseBody
	@RequestMapping(value="groupAuth/regist" , method=RequestMethod.POST)
	public String pushGroupRegistPost( PushGroupAuthListDto pushGroupAuthList ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushGroupAuthManageService.InsertPushGroupAuth(removeDuplicateCuid(pushGroupAuthList));
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
	
	//그룹권한 삭제
	@ResponseBody
	@RequestMapping(value="groupAuth/delete" , method=RequestMethod.POST)
	public String pushGroupDeletePost( PushGroupAuthListDto pushGroupAuthList ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushGroupAuthManageService.DeletePushGroupAuth(pushGroupAuthList);
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
