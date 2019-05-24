package kr.msp.admin.push;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.push.monitoring.dto.MonitoringDto;
import kr.msp.admin.push.monitoring.service.MonitoringManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushMonitoringManageController {
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private MonitoringManageService monitoringManage;
	
	@RequestMapping(value="monitoring" , method=RequestMethod.GET)
	public String pushMonitoringGet( Model model){
		MonitoringDto monitoring = new MonitoringDto();
		monitoring.setPAGE_NUM(1);
		monitoring.setPAGE_SIZE(row_size);
		List<MonitoringDto> monitoringList = monitoringManage.SelectServerList(monitoring);
		
		model.addAttribute( "monitoringList", monitoringList );
		model.addAttribute("R_PAGE_NUM",monitoring.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/monitoring/monitoringMain";
	}
	
	@RequestMapping(value="monitoring" , method=RequestMethod.POST)
	public String pushMonitoringPost( Model model , MonitoringDto monitoring){
		
		monitoring.setPAGE_SIZE(row_size);
		List<MonitoringDto> monitoringList = monitoringManage.SelectServerList(monitoring);
		
		model.addAttribute( "monitoringList", monitoringList );
		model.addAttribute("R_PAGE_NUM",monitoring.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",monitoring.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitoring/monitoringList";
	}
	
	
	@RequestMapping(value="monitoring/info" , method=RequestMethod.GET)
	public String pushMonitoringInfoPost( Model model , MonitoringDto monitoring){
		
		monitoring.setPAGE_SIZE(row_size);
		List<MonitoringDto> monitoringinfoList = monitoringManage.SelectServerLogList(monitoring);
		
		model.addAttribute( "monitoringinfoList", monitoringinfoList );
		model.addAttribute("R_PAGE_NUM",monitoring.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("R_IPADDRESS", monitoring.getIPADDRESS());
		model.addAttribute("R_PORT", monitoring.getPORT());
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/monitoring/monitoringInfo";
	}
	
}
