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

import kr.msp.admin.push.monitorUser.dto.MonitorUserDto;
import kr.msp.admin.push.monitorUser.service.MonitorUserManageService;

@RequestMapping(value = "admin/push")
@Controller
public class PushMonitorUserManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushMonitorUserManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	MonitorUserManageService monitorUserManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="monitorUser" , method=RequestMethod.GET)
	public String pushMonitorUserGet( Model model){
		MonitorUserDto monitorUser = new MonitorUserDto();
		monitorUser.setPAGE_NUM(1);
		monitorUser.setPAGE_SIZE(row_size);
		List<MonitorUserDto> monitorUserList = monitorUserManage.SelectSupervisorList(monitorUser);
		
		model.addAttribute( "monitorUserList", monitorUserList );
		model.addAttribute("R_PAGE_NUM",monitorUser.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/monitorUser/monitorUserMain";
	}
	
	@RequestMapping(value="monitorUser" , method=RequestMethod.POST)
	public String pushMonitorUserPost( Model model , MonitorUserDto monitorUser){
		
		monitorUser.setPAGE_SIZE(row_size);
		List<MonitorUserDto> monitorUserList = monitorUserManage.SelectSupervisorList(monitorUser);
		
		model.addAttribute( "monitorUserList", monitorUserList );
		model.addAttribute("R_PAGE_NUM",monitorUser.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitorUser/monitorUserList";
	}
	
	//모니터링 수진자 등록 화면
	@RequestMapping(value="monitorUser/regist" , method=RequestMethod.GET)
	public String pushMonitorUserRegistPost( Model model , MonitorUserDto monitorUser){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitorUser/monitorUserModify";
	}
	
	//모니터링 수진자 수정 화면
	@RequestMapping(value="monitorUser/edit" , method=RequestMethod.GET)
	public String pushMonitorUserEditPost( Model model , MonitorUserDto monitorUser){
		
		MonitorUserDto monitorUserOne = monitorUserManage.SelectSupervisorOne(monitorUser);
		
		model.addAttribute("monitorUserOne",monitorUserOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitorUser/monitorUserModify";
	}
	
	
	//모니터링 수진자 등록
	@ResponseBody
	@RequestMapping(value="monitorUser/regist" , method=RequestMethod.POST)
	public String pushMonitorUserRegistPost( MonitorUserDto monitorUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = monitorUserManage.InsertSupervisor(monitorUser);
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
	
	//모니터링 수진자 수정
	@ResponseBody
	@RequestMapping(value="monitorUser/edit" , method=RequestMethod.POST)
	public String pushMonitorUserEditPost( MonitorUserDto monitorUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = monitorUserManage.UpdateSupervisor(monitorUser);
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
	
	//모니터링 수진자 삭제
	@ResponseBody
	@RequestMapping(value="monitorUser/delete" , method=RequestMethod.POST)
	public String pushMonitorUserDeletePost( MonitorUserDto monitorUser ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = monitorUserManage.UpdateSupervisorDelete(monitorUser);
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
