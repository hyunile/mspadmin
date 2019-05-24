package kr.msp.admin.campaign;


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

import kr.msp.admin.campaign.automationPush.dto.AutoPushMsgSendDTO;
import kr.msp.admin.campaign.automationPush.dto.AutomationPushPDTO;
import kr.msp.admin.campaign.automationPush.service.AutomationPushService;
import kr.msp.admin.commonPush.BasePushController;
import kr.msp.admin.commonPush.commonPushSend.service.CommonPushSendService;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;

@RequestMapping(value="admin/campaign")
@Controller
public class AutomationPushController extends BasePushController {
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private MsgSendManageService msgSendManage;
	
	@Autowired(required=true)
	private CommonPushSendService commonPushSendService;
	
	@Autowired(required=true)
	private AutomationPushService automationPushService;
	
	
	@RequestMapping(value="/automationPush", method=RequestMethod.GET )
	public String getCampaignManage( Model model, HttpServletRequest request, AutomationPushPDTO automationPushPDTO){
		
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = msgSendManage.SelectPushServiceAll(pushService);
		
		model.addAttribute("pushServiceList", pushServiceList);
		model.addAttribute("R_PAGE_NUM",automationPushPDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/campaign/automationPush/automationPush";
	}
	
	@ResponseBody
	@RequestMapping(value="/automationPush/setAutomationPushMsg", method=RequestMethod.POST )
	public String sendCmpPushMsg(AutoPushMsgSendDTO autoPushMsgSendDTO) throws JsonGenerationException, JsonMappingException, IOException{
		//TYPE;
		autoPushMsgSendDTO.setUSER_TYPE("EACH");
		//CUIDS;
		String[] cuids = {"5CD959D0-3290-4141-8421-F9C27D41CC8A", "test20000"};
		autoPushMsgSendDTO.setCUIDS(cuids);
		//SERVICECODE;
		autoPushMsgSendDTO.setSERVICECODE("ALL");
		//SENDERCODE;
		autoPushMsgSendDTO.setSENDERCODE("admin");
		//SENDTYPE;
		autoPushMsgSendDTO.setSEND_TYPE("I");
		//PRIORITY;
		autoPushMsgSendDTO.setPRIORITY("1");
		
		
		autoPushMsgSendDTO = (AutoPushMsgSendDTO) genePushData(autoPushMsgSendDTO);
		if(autoPushMsgSendDTO.getPSID() != null){
			autoPushMsgSendDTO.setSTR_PSID(autoPushMsgSendDTO.getPSID().toString());
		}
		if(autoPushMsgSendDTO.getCUID() != null){
			autoPushMsgSendDTO.setSTR_CUID(autoPushMsgSendDTO.getCUID().toString());
		}
		autoPushMsgSendDTO.setSTATUS(1);
		
		int result = 0;
		try{
			automationPushService.addAutomationPush(autoPushMsgSendDTO);
			result = 1;
		}catch(Exception e){
			result = 0;
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
