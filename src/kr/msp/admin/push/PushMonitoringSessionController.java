package kr.msp.admin.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.mobile.resourceUpload.dto.MobServiceDto;
import kr.msp.admin.mobile.resourceUpload.service.MobileResourceUploadService;
import kr.msp.admin.push.monitorConfig.dto.MonitorConfigDto;
import kr.msp.admin.push.monitorConfig.service.MonitorConfigManageService;
import kr.msp.admin.push.monitorSession.dto.MonitorSessionDto;
import kr.msp.admin.push.monitorSession.service.MonitorSessionService;

@RequestMapping(value = "admin/push/monitoring")
@Controller
public class PushMonitoringSessionController {
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private MobileResourceUploadService mobileResourceUploadService;
	@Autowired(required=true)
	private MonitorSessionService monitorSessionService;
	@Autowired(required=true)
	MonitorConfigManageService monitorConfigManage;
	
	@RequestMapping(value="/session" , method=RequestMethod.GET)
	public String getMotinoringSessionList(Model model, HttpServletRequest request, MonitorSessionDto monitorSessionParamDto){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		List<MobServiceDto> listMobServiceDto = mobileResourceUploadService.SelectMobService(S_ID_USER);
		MonitorConfigDto monitorConfig = new MonitorConfigDto();
		List<MonitorConfigDto> ListMonitorConfigDto = monitorConfigManage.selectServerListAll(monitorConfig);
		
		monitorSessionParamDto.setPAGE_NUM(1);
		monitorSessionParamDto.setPAGE_SIZE(row_size);
		
		List<MonitorSessionDto> listMonitorSessionDto = monitorSessionService.getMonitoringSessionList(monitorSessionParamDto);
		
		for(MonitorSessionDto dto : listMonitorSessionDto){
			for(MobServiceDto svcDto : listMobServiceDto){
				if(dto.getAPPID().equals(svcDto.getAPP_ID())){
					dto.setSVC_NM(svcDto.getSVC_NM());
				}
			}
			if(dto.getLOGIN() == null){
				dto.setLOGIN("N");
			}
		}
		
		model.addAttribute("serverList", ListMonitorConfigDto);
		model.addAttribute("serviceList", listMobServiceDto);
		model.addAttribute("monitorSessionList", listMonitorSessionDto);
		
		model.addAttribute("R_PAGE_NUM",monitorSessionParamDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/monitorSession/monitorSessionMain";
	}
	
	@RequestMapping(value="/session" , method=RequestMethod.POST)
	public String getMotinoringSessionListPost(Model model, HttpServletRequest request, MonitorSessionDto monitorSessionParamDto){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		List<MobServiceDto> listMobServiceDto = mobileResourceUploadService.SelectMobService(S_ID_USER);
		
		List<MonitorSessionDto> listMonitorSessionDto = monitorSessionService.getMonitoringSessionList(monitorSessionParamDto);
		MonitorConfigDto monitorConfig = new MonitorConfigDto();
		List<MonitorConfigDto> ListMonitorConfigDto = monitorConfigManage.selectServerListAll(monitorConfig);
		
		for(MonitorSessionDto dto : listMonitorSessionDto){
			for(MobServiceDto svcDto : listMobServiceDto){
				if(dto.getAPPID().equals(svcDto.getAPP_ID())){
					dto.setSVC_NM(svcDto.getSVC_NM());
				}
			}
			if(dto.getLOGIN() == null){
				dto.setLOGIN("N");
			}
		}
		model.addAttribute("serverList", ListMonitorConfigDto);
		model.addAttribute("serviceList", listMobServiceDto);
		model.addAttribute("monitorSessionList", listMonitorSessionDto);
		
		model.addAttribute("R_PAGE_NUM",monitorSessionParamDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",monitorSessionParamDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		
		return "admin/push/monitorSession/monitorSessionList";
	}
	
	
	@RequestMapping(value="/sessionDetail", method=RequestMethod.POST)
	public String getMotinoringSessionDetail( Model model, HttpServletRequest request, MonitorSessionDto monitorSessionParamDto){
		
		MonitorSessionDto monitorSessionDto = monitorSessionService.getMonitoringSessionDetail(monitorSessionParamDto);
		if(monitorSessionDto.getLOGIN() == null){
			monitorSessionDto.setLOGIN("N");
		}
		
		model.addAttribute("monitorSessionDetail", monitorSessionDto);
		
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/push/monitorSession/monitorSessionDetail";
	}
	
	@ResponseBody
	@RequestMapping(value="/checkSession", method=RequestMethod.POST)
	public String checkMotinoringSession( Model model, HttpServletRequest request, MonitorSessionDto monitorSessionParamDto) throws JsonGenerationException, JsonMappingException, IOException{
		boolean result = false;
		if(!(monitorSessionParamDto.getSERVERNAME() == null || monitorSessionParamDto.getSERVERNAME().equals(""))){
			result = monitorSessionService.checkUserSession(monitorSessionParamDto);
		}
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;	
	}
	
	@ResponseBody
	@RequestMapping(value="/terminateSession", method=RequestMethod.POST)
	public String terminateMotinoringSession( Model model, HttpServletRequest request, MonitorSessionDto monitorSessionParamDto) throws JsonGenerationException, JsonMappingException, IOException{
		boolean result = false;
		if(!(monitorSessionParamDto.getSERVERNAME() == null || monitorSessionParamDto.getSERVERNAME().equals(""))){
			result = monitorSessionService.terminateUserSession(monitorSessionParamDto);
		}
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
