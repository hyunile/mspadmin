package kr.msp.admin.campaign;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.campaign.approval.dto.ApprovalDTO;
import kr.msp.admin.campaign.approval.service.ApprovalService;
import kr.msp.admin.campaign.campaignPush.dto.CampaignPushAdminRsrvDTO;
import kr.msp.admin.campaign.campaignPush.dto.CampaignPushParamDTO;
import kr.msp.admin.campaign.campaignPush.dto.CmpPushMsgSendDTO;
import kr.msp.admin.campaign.campaignPush.dto.TargetConditionDTO;
import kr.msp.admin.campaign.campaignPush.dto.TargetConditionPDTO;
import kr.msp.admin.campaign.campaignPush.service.CampaignPushService;
import kr.msp.admin.commonPush.BasePushController;
import kr.msp.admin.commonPush.commonPushSend.service.CommonPushSendService;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;
import kr.msp.admin.push.sendType.dto.PushSendTypeDto;
import kr.msp.common.util.LowerCaseNamingStrategy;



@RequestMapping(value="admin/campaign")
@Controller
public class CampaignPushController extends BasePushController {
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
    @Autowired(required=true)
    private ApprovalService approvalService;
    
	@Autowired(required=true)
	private MsgSendManageService msgSendManage;
	
	@Autowired(required=true)
	private CampaignPushService campaignPushService;
	
	@Autowired(required=true)
	private CommonPushSendService commonPushSendService;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 
	
	@Qualifier("mspXmlConfiguration")
    @Autowired(required = true)
    private Configuration mspConfig;
	
	private static Logger logger = LoggerFactory.getLogger(CampaignPushController.class);
	
	@RequestMapping(value="/campaignPush", method=RequestMethod.GET )
	public String getCampaignManage( Model model, HttpServletRequest request, CampaignPushParamDTO campaignPushParamDTO){
		String S_ID_AUTH_GRP = (String) request.getSession().getAttribute("S_ID_AUTH_GRP"); 
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = msgSendManage.SelectPushServiceAll(pushService);
		
		//발송유형 조회
		List<PushSendTypeDto> sendTypeList  =  msgSendManage.SelectSendTypeAll();
		
		campaignPushParamDTO.setTRG_CD("INIT");
		List<TargetConditionDTO> listTargetConditionDTO = campaignPushService.getTargetConditionAllList(campaignPushParamDTO);
		
		
		model.addAttribute("userId", S_ID_USER);
		model.addAttribute("auth", S_ID_AUTH_GRP);
		model.addAttribute("pushServiceList", pushServiceList);
		model.addAttribute("sendTypeList", sendTypeList);
		model.addAttribute("targetConditionList", listTargetConditionDTO);
		
		model.addAttribute("R_PAGE_NUM",campaignPushParamDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/campaign/campaignPush/campaignPush";
	}
	
	@ResponseBody
	@RequestMapping(value="/campaignPush/targetCondition", method=RequestMethod.POST )
	public String getTargetCondition(CampaignPushParamDTO campaignPushParamDTO) throws JsonGenerationException, JsonMappingException, IOException{
		if(campaignPushParamDTO.getTRG_CD() == "INIT"){
			return null;
		}
		List<TargetConditionDTO> listTargetConditionDTO = campaignPushService.getTargetConditionAllList(campaignPushParamDTO);
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("TRG_CD", campaignPushParamDTO.getTRG_CD());
		map.put("VIEW_ID", campaignPushParamDTO.getVIEW_ID());
		map.put("result", listTargetConditionDTO);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/campaignPush/preTargeting", method=RequestMethod.POST )
	public String getPreTargetingList(CampaignPushParamDTO campaignPushParamDTO) throws JsonGenerationException, JsonMappingException, IOException{
		if(campaignPushParamDTO.getCONDITION_D2().size() != campaignPushParamDTO.getCONDITION_D3().size()){
			return null;
		}
		TargetConditionPDTO targetConditionPDTO = campaignPushService.getTargetConditionList(campaignPushParamDTO);
		targetConditionPDTO.setAPPID(campaignPushParamDTO.getAPP_ID());
		List<String> listCuid = campaignPushService.getPreTargetingList(targetConditionPDTO);
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("LIST_CUID", listCuid);
		map.put("LIST_SIZE", listCuid.size());
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/campaignPush/sendCmpPushMsg", method=RequestMethod.POST )
	public String sendCmpPushMsg(CmpPushMsgSendDTO cmpPushMsgSendDTO) throws JsonGenerationException, JsonMappingException, IOException{
		String conditionCuids = null;
		boolean isAdminReserve = false;
		if(cmpPushMsgSendDTO.getTARGET_LIST_TYPE()!=null && cmpPushMsgSendDTO.getTARGET_LIST_TYPE().equals(0)){//발송시 타겟추출.
			CampaignPushParamDTO campaignPushParamDTO = new CampaignPushParamDTO();
			campaignPushParamDTO.setCONDITION_D2(cmpPushMsgSendDTO.getCONDITION_D2());
			campaignPushParamDTO.setCONDITION_D3(cmpPushMsgSendDTO.getCONDITION_D3());
			TargetConditionPDTO targetConditionPDTO = campaignPushService.getTargetConditionList(campaignPushParamDTO);

			if(cmpPushMsgSendDTO.getSEND_TYPE().equals("I")){//즉시발송
				targetConditionPDTO.setAPPID(cmpPushMsgSendDTO.getAPPID());
				List<String> listCuid = campaignPushService.getPreTargetingList(targetConditionPDTO);
				if(listCuid.size() != 0){
					String[] cUIDS = new String[listCuid.size()];
					cUIDS = listCuid.toArray(cUIDS);
					cmpPushMsgSendDTO.setCUIDS(cUIDS);
				}
			}else{//예약발송.
				isAdminReserve = true;
				ObjectMapper mapper = new ObjectMapper();
				mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
				conditionCuids = mapper.writeValueAsString(targetConditionPDTO);
			}
		}
		cmpPushMsgSendDTO = (CmpPushMsgSendDTO) genePushData(cmpPushMsgSendDTO);
		
		Map<String, Object> map;
		if(isAdminReserve){
			//예약테이블에 직접 저장.
			CampaignPushAdminRsrvDTO campaignPushAdminRsrvDTO = getCampaignPushAdminRsrvDTO(cmpPushMsgSendDTO, conditionCuids);
			map = setAdminRsrvCampaignPush(campaignPushAdminRsrvDTO);
			map.put("ISRESERVE", "T");
		}else{
			//발송
			map = commonPushSendService.pushMsgSend(cmpPushMsgSendDTO);
			map.put("ISRESERVE", "F");
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	private Map<String, Object> setAdminRsrvCampaignPush(CampaignPushAdminRsrvDTO campaignPushAdminRsrvDTO){
		boolean result = campaignPushService.setAdminRsrvCampaignPush(campaignPushAdminRsrvDTO);
		Map<String, Object> map = new HashMap<String, Object>();
		if(result){
			map.put("resultCode", "0000");
			map.put("resultMsg", messageSource.getMessage("menu.push.controller.msgSendSuccess", null, LocaleContextHolder.getLocale())); //"메시지 발송요청이 성공하였습니다."
		}else{
			map.put("resultCode", "500");
			map.put("resultMsg",messageSource.getMessage("menu.push.controller.sendFail", null, LocaleContextHolder.getLocale())
					+": DB"+messageSource.getMessage("common.text.error", null, LocaleContextHolder.getLocale())); //발송실패  에러
		}
		return map;
	}
	
	public String sendCmpPushMsgFromApproval(CmpPushMsgSendDTO cmpPushMsgSendDTO) throws JsonGenerationException, JsonMappingException, IOException{
        Map<String, Object> map = commonPushSendService.pushMsgSend(cmpPushMsgSendDTO);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/campaignPush/sendCmpPushMsgApproval", method=RequestMethod.POST )
	public String sendCmpPushMsgApproval(CmpPushMsgSendDTO cmpPushMsgSendDTO) throws Exception{
		cmpPushMsgSendDTO = (CmpPushMsgSendDTO) genePushData(cmpPushMsgSendDTO);
		ApprovalDTO approvalDTO = new ApprovalDTO(); 
		
		approvalDTO.setADMIN_TITLE(cmpPushMsgSendDTO.getADMINTITLE());
		approvalDTO.setTYPE(cmpPushMsgSendDTO.getTYPE());
		if(cmpPushMsgSendDTO.getPSID() != null){
			approvalDTO.setPSID(cmpPushMsgSendDTO.getPSID().toString());
		}
		if(cmpPushMsgSendDTO.getCUID() != null){
			approvalDTO.setCUID(cmpPushMsgSendDTO.getCUID().toString());
		}
		approvalDTO.setGROUPSEQ(cmpPushMsgSendDTO.getGROUPSEQ());
		approvalDTO.setMESSAGE(cmpPushMsgSendDTO.getMESSAGE());
		approvalDTO.setPRIORITY(Integer.parseInt(cmpPushMsgSendDTO.getPRIORITY()));
		approvalDTO.setBADGENO(Integer.parseInt(cmpPushMsgSendDTO.getBADGENO()));
		if(cmpPushMsgSendDTO.getRESERVEDATE() != null && !"".equals(cmpPushMsgSendDTO.getRESERVEDATE())){
			approvalDTO.setRESERVEDATE(cmpPushMsgSendDTO.getRESERVEDATE());
		}
		
		approvalDTO.setSENDERCODE(cmpPushMsgSendDTO.getSENDERCODE());
		approvalDTO.setSERVICECODE(cmpPushMsgSendDTO.getSERVICECODE());
		approvalDTO.setSOUNDFILE(cmpPushMsgSendDTO.getSOUNDFILE());
		approvalDTO.setEXT(cmpPushMsgSendDTO.getEXT());
		approvalDTO.setAPP_ID(cmpPushMsgSendDTO.getAPPID());
		approvalDTO.setATTACHFILE(cmpPushMsgSendDTO.getATTACHFILE());
		approvalDTO.setDB_IN(cmpPushMsgSendDTO.getDB_IN());
		approvalDTO.setCMP_TYPE("C");
	
		String textType= null;
		if("0".equals(cmpPushMsgSendDTO.getMSGTYPE())){
			textType = "T";
		}else if("1".equals(cmpPushMsgSendDTO.getMSGTYPE())){
			textType = "R";
		}
		approvalDTO.setTEXT_TYPE(textType);
		Date date = new Date();
		
		approvalDTO.setREGDATE(date.toString());
		
		if("I".equals(cmpPushMsgSendDTO.getSEND_TYPE())){
			approvalDTO.setRESERVE_FLAG("N");
		}else if("R".equals(cmpPushMsgSendDTO.getSEND_TYPE())){
			approvalDTO.setRESERVE_FLAG("Y");
		}
		
		approvalDTO.setAPPROVAL_FLAG("S");
		approvalService.insertCmpApproval(approvalDTO);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("resultCode", "0000");
		map.put("resultMsg",messageSource.getMessage("menu.push.controller.msgSendSuccess", null, LocaleContextHolder.getLocale())); //"메시지 발송요청이 성공하였습니다."
		ObjectMapper mapper = new ObjectMapper();
		mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	private CampaignPushAdminRsrvDTO getCampaignPushAdminRsrvDTO(CmpPushMsgSendDTO cmpPushMsgSendDTO, String conditionCuids){
		CampaignPushAdminRsrvDTO campaignPushAdminRsrvDTO = new CampaignPushAdminRsrvDTO();
		
		campaignPushAdminRsrvDTO.setCUID(conditionCuids);
		
		campaignPushAdminRsrvDTO.setTYPE(cmpPushMsgSendDTO.getTYPE());
		campaignPushAdminRsrvDTO.setGROUPSEQ(cmpPushMsgSendDTO.getGROUPSEQ());
		campaignPushAdminRsrvDTO.setMESSAGE(cmpPushMsgSendDTO.getMESSAGE());
		campaignPushAdminRsrvDTO.setPRIORITY(Integer.parseInt(cmpPushMsgSendDTO.getPRIORITY()));
		campaignPushAdminRsrvDTO.setBADGENO(Integer.parseInt(cmpPushMsgSendDTO.getBADGENO()));
		campaignPushAdminRsrvDTO.setRESERVEDATE(cmpPushMsgSendDTO.getRESERVEDATE());
		campaignPushAdminRsrvDTO.setSENDERCODE(cmpPushMsgSendDTO.getSENDERCODE());
		campaignPushAdminRsrvDTO.setSERVICECODE(cmpPushMsgSendDTO.getSERVICECODE());
		campaignPushAdminRsrvDTO.setSOUNDFILE(cmpPushMsgSendDTO.getSOUNDFILE());
		campaignPushAdminRsrvDTO.setEXT(cmpPushMsgSendDTO.getEXT());
		campaignPushAdminRsrvDTO.setAPP_ID(cmpPushMsgSendDTO.getAPPID());
		campaignPushAdminRsrvDTO.setATTACHFILE(cmpPushMsgSendDTO.getATTACHFILE());
		campaignPushAdminRsrvDTO.setDB_IN(cmpPushMsgSendDTO.getDB_IN());
		campaignPushAdminRsrvDTO.setRESERVETYPE("A");
		campaignPushAdminRsrvDTO.setISCONDITION("Y");
		
		return campaignPushAdminRsrvDTO;
	}
}