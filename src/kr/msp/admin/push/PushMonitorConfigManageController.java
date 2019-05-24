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

import kr.msp.admin.push.monitorConfig.dto.MonitorConfigDto;
import kr.msp.admin.push.monitorConfig.service.MonitorConfigManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushMonitorConfigManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushMonitorConfigManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	MonitorConfigManageService monitorConfigManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="monitorConfig" , method=RequestMethod.GET)
	public String pushMonitorConfigGet( Model model){
		MonitorConfigDto monitorConfig = new MonitorConfigDto();
		monitorConfig.setPAGE_NUM(1);
		monitorConfig.setPAGE_SIZE(row_size);
		List<MonitorConfigDto> monitorConfigList = monitorConfigManage.SelectServerList(monitorConfig);
		
		model.addAttribute( "monitorConfigList", monitorConfigList );
		model.addAttribute("R_PAGE_NUM",monitorConfig.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/monitorConfig/monitorConfigMain";
	}
	
	@RequestMapping(value="monitorConfig" , method=RequestMethod.POST)
	public String pushMonitorConfigPost( Model model , MonitorConfigDto monitorConfig){
		
		monitorConfig.setPAGE_SIZE(row_size);
		List<MonitorConfigDto> monitorConfigList = monitorConfigManage.SelectServerList(monitorConfig);
		
		model.addAttribute( "monitorConfigList", monitorConfigList );
		model.addAttribute("R_PAGE_NUM",monitorConfig.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitorConfig/monitorConfigList";
	}
	
	//발송자 등록 화면
	@RequestMapping(value="monitorConfig/regist" , method=RequestMethod.GET)
	public String pushMonitorConfigRegistPost( Model model){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitorConfig/monitorConfigWrite";
	}
	
	
	
	@ResponseBody
	@RequestMapping(value="monitorConfig/check" , method=RequestMethod.POST)
	public String pushMonitorConfigCheckPost( MonitorConfigDto monitorConfig ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
		
		try {
			result = monitorConfigManage.SelectServerDupCheck(monitorConfig);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.getError", null, LocaleContextHolder.getLocale()) ); //"오류가 발생했습니다. 계속해서 같은 오류발생시 관리자에게 문의하십시오."
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.useServer", null, LocaleContextHolder.getLocale()) ); //사용할수 있는 서버입니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.notUseServer", null, LocaleContextHolder.getLocale()) ); //사용할수 없는 서버입나다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="monitorConfig/regist" , method=RequestMethod.POST)
	public String pushMonitorConfigRegistPost( MonitorConfigDto monitorConfig ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = monitorConfigManage.InsertServer(monitorConfig);
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
	
	
	//사용자 삭제
	@ResponseBody
	@RequestMapping(value="monitorConfig/delete" , method=RequestMethod.POST)
	public String pushMonitorConfigDeletePost( MonitorConfigDto monitorConfig ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = monitorConfigManage.DeleteServer(monitorConfig);
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
