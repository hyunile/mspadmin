package kr.msp.admin.campaign;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.campaign.approval.dto.ApprovalDTO;
import kr.msp.admin.campaign.approval.service.ApprovalService;
import kr.msp.admin.commonPush.commonPushSend.service.CommonPushSendService;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.msgSend.dto.MsgSendBaseDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;

@RequestMapping(value="admin/campaign")
@Controller
public class ApprovalController {
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private MsgSendManageService msgSendManage;
	
	@Autowired(required=true)
	private ApprovalService approvalService;
	
	@Autowired(required=true)
	private CommonPushSendService commonPushSendService; 
	
	@RequestMapping(value="/approve", method=RequestMethod.GET )
	public String getApproval(Model model, HttpServletRequest request, @RequestParam Map<String,Object> reqMap) {
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = msgSendManage.SelectPushServiceAll(pushService);
		
		model.addAttribute("pushServiceList", pushServiceList);
		
		model.addAttribute("R_PAGE_NUM", reqMap.get("PAGE_NUM"));
		model.addAttribute("R_ROW_SIZE", row_size);
		model.addAttribute("R_PAGE_SIZE", page_size);
		return "admin/campaign/approval/approval";
	}
	
	@RequestMapping(value="/approve", method=RequestMethod.POST)
	public String getCampaignListPost( Model model, HttpServletRequest request, @RequestParam Map<String,Object> reqMap) {
		reqMap.put("PAGE_SIZE", page_size);
		List<ApprovalDTO> listResult = approvalService.getCmpApprovalList(reqMap);
		
		model.addAttribute("listApproval", listResult);
		model.addAttribute("R_PAGE_NUM", reqMap.get("PAGE_NUM"));
		model.addAttribute("R_ROW_SIZE", reqMap.get("PAGE_SIZE"));
		model.addAttribute("R_PAGE_SIZE", page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/campaign/approval/approvalList";
	}
	
	@ResponseBody
	@RequestMapping(value="/agreeCampaign", method=RequestMethod.POST)
	public String agreeCampaignPost( Model model, HttpServletRequest request, @RequestParam Map<String,Object> reqMap) throws JsonGenerationException, JsonMappingException, IOException, JSONException {
		reqMap.put("APPROVAL_SENDERCODE", (String) request.getSession().getAttribute("S_ID_USER"));
		reqMap.put("APPROVAL_FLAG", "A");
		
		ApprovalDTO approvalDTO = approvalService.getCmpApproval(reqMap);
		MsgSendBaseDto msgSendBaseDto = new MsgSendBaseDto();
		
		msgSendBaseDto.setTYPE(approvalDTO.getTYPE());
		String psids = approvalDTO.getPSID();
		if(psids != null){
			JSONArray jSonPsid = new JSONArray(psids);
			msgSendBaseDto.setPSID(jSonPsid);
		}
		
		String cuids = approvalDTO.getCUID();
		if(approvalDTO.getCUID() != null){
			JSONArray jSonCuid = new JSONArray(cuids);
			msgSendBaseDto.setCUID(jSonCuid);
		}
		
		msgSendBaseDto.setGROUPSEQ(approvalDTO.getGROUPSEQ());
		msgSendBaseDto.setMESSAGE(approvalDTO.getMESSAGE());
		msgSendBaseDto.setPRIORITY(Integer.toString(approvalDTO.getPRIORITY()));
		msgSendBaseDto.setBADGENO(Integer.toString(approvalDTO.getBADGENO()));
		msgSendBaseDto.setRESERVEDATE(approvalDTO.getRESERVEDATE());
		msgSendBaseDto.setSERVICECODE(approvalDTO.getSERVICECODE());
		msgSendBaseDto.setSOUNDFILE(approvalDTO.getSOUNDFILE());
		msgSendBaseDto.setEXT(approvalDTO.getEXT());
		msgSendBaseDto.setSENDERCODE(approvalDTO.getSENDERCODE());
		msgSendBaseDto.setAPPID(approvalDTO.getAPP_ID());
		msgSendBaseDto.setATTACHFILE(approvalDTO.getATTACHFILE());
		msgSendBaseDto.setDB_IN(approvalDTO.getDB_IN());
		
		Map<String,Object> returnMap = commonPushSendService.pushMsgSend(msgSendBaseDto);
		
		return updateCmpApprovalFlag(reqMap);
	}
	
	@ResponseBody
	@RequestMapping(value="/rejectCampaign", method=RequestMethod.POST)
	public String rejectCampaignPost( Model model, HttpServletRequest request, @RequestParam Map<String,Object> reqMap) throws JsonGenerationException, JsonMappingException, IOException {
		reqMap.put("APPROVAL_SENDERCODE", (String) request.getSession().getAttribute("S_ID_USER"));
		reqMap.put("APPROVAL_FLAG", "R");
		return updateCmpApprovalFlag(reqMap);
	}
	
	@ResponseBody
	@RequestMapping(value="/cancelCampaign", method=RequestMethod.POST)
	public String cancelCampaignPost( Model model, HttpServletRequest request, @RequestParam Map<String,Object> reqMap) throws JsonGenerationException, JsonMappingException, IOException {
		reqMap.put("APPROVAL_SENDERCODE", (String) request.getSession().getAttribute("S_ID_USER"));
		reqMap.put("APPROVAL_FLAG", "C");
		return updateCmpApprovalFlag(reqMap);
	}
	
	private String updateCmpApprovalFlag(Map<String,Object> reqMap) throws JsonGenerationException, JsonMappingException, IOException {
		int result = approvalService.updateCmpApprovalFlag(reqMap);
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
