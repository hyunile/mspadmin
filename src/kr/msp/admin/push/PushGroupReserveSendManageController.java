package kr.msp.admin.push;

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

import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.groupReserveSend.dto.GroupReserveSendDto;
import kr.msp.admin.push.groupReserveSend.service.GroupReserveSendManageService;
import kr.msp.admin.push.sender.dto.PushSenderDto;

@RequestMapping(value="admin/push")
@Controller
public class PushGroupReserveSendManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushGroupReserveSendManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	GroupReserveSendManageService groupReserveSendManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="groupReserveSend",method=RequestMethod.GET)
	public String pushGroupReserveSendGet( Model model ,HttpServletRequest request){
		
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = groupReserveSendManage.SelectPushServiceAll(pushService);
		
		List<PushSenderDto> pushSenderList = groupReserveSendManage.SelectSenderAll();
		
		model.addAttribute("pushServiceList",pushServiceList);
		model.addAttribute("pushSenderList",pushSenderList);
		
		return "admin/push/groupReserveSend/groupReserveSendMain";
	}
	
	@RequestMapping(value="groupReserveSend",method=RequestMethod.POST)
	public String pushGroupReserveSendPost( Model model , GroupReserveSendDto groupReserveSend){
		
		groupReserveSend.setPAGE_SIZE(row_size);
		List<GroupReserveSendDto> groupReserveSendList = groupReserveSendManage.SelectGroupReserveSendList(groupReserveSend);
		
		model.addAttribute( "groupReserveSendList", groupReserveSendList );
		model.addAttribute("R_PAGE_NUM",groupReserveSend.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupReserveSend/groupReserveSendList";
	}
	
	@RequestMapping(value="groupReserveSend/info",method=RequestMethod.GET)
	public String pushGroupReserveSendInfoGet( Model model , GroupReserveSendDto groupReserveSend){
		
		GroupReserveSendDto groupReserveSendOne = groupReserveSendManage.SelectGroupReserveSendOne(groupReserveSend);
		
		model.addAttribute( "groupReserveSendOne", groupReserveSendOne );
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupReserveSend/groupReserveSendInfo";
	}
	
	@RequestMapping(value="groupReserveSend/infoList",method=RequestMethod.GET)
	public String pushGroupReserveSendInfoListGet( Model model , GroupReserveSendDto groupReserveSend){
		
		groupReserveSend.setPAGE_SIZE(row_size);
		List<GroupReserveSendDto> groupUserSendinfoList = groupReserveSendManage.SelectGroupUserSendList(groupReserveSend);
		
		model.addAttribute( "groupUserSendinfoList", groupUserSendinfoList );
		model.addAttribute("R_PAGE_NUM",groupReserveSend.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("R_SEQNO",groupReserveSend.getSEQNO());
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupReserveSend/groupReserveSendInfoList";
	}
	
	//메시지 예약 취소
	@ResponseBody
	@RequestMapping(value="groupReserveSend/delete" , method=RequestMethod.POST)
	public String groupUserReserveSendDeletePost( HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		String result_str = groupReserveSendManage.SelectMsgDeleteCheck(request);
		
		if(result_str.equals("")){
			try {
				result = groupReserveSendManage.DeleteGroupMessage(request);
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			if(result_str.equals("")){
				map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
			} else {
				map.put("msg", result_str + messageSource.getMessage("menu.push.controller.noGroupID", null, LocaleContextHolder.getLocale()) ); // 그룹ID 메시지를 삭제할수 없습니다.
			}
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
		
	}
	
}
