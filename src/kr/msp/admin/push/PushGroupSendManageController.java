package kr.msp.admin.push;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.groupReserveSend.dto.GroupReserveSendDto;
import kr.msp.admin.push.groupSend.dto.GroupSendDto;
import kr.msp.admin.push.groupSend.service.GroupSendManageService;
import kr.msp.admin.push.sender.dto.PushSenderDto;
import kr.msp.admin.push.userSend.dto.PushUserSendDto;

@RequestMapping(value="admin/push")
@Controller
public class PushGroupSendManageController {
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	GroupSendManageService groupSendManage;
	
	@RequestMapping(value="groupSend",method=RequestMethod.GET)
	public String pushGroupSendGet( Model model ,HttpServletRequest request){
		
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = groupSendManage.SelectPushServiceAll(pushService);
		
		List<PushSenderDto> pushSenderList = groupSendManage.SelectSenderAll();
		
		model.addAttribute("pushServiceList",pushServiceList);
		model.addAttribute("pushSenderList",pushSenderList);
		
		return "admin/push/groupSend/groupSendMain";
	}
	
	@RequestMapping(value="groupSend",method=RequestMethod.POST)
	public String pushGroupSendPost( Model model , GroupSendDto groupSend){
		
		groupSend.setPAGE_SIZE(row_size);
		List<GroupSendDto> groupReserveSendList = groupSendManage.SelectGroupSendList(groupSend);
		
		model.addAttribute( "groupReserveSendList", groupReserveSendList );
		model.addAttribute("R_PAGE_NUM",groupSend.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupSend/groupSendList";
	}
	
	@RequestMapping(value="groupSend/info",method=RequestMethod.GET)
	public String pushGroupSendInfoGet( Model model , GroupReserveSendDto groupReserveSend){
		
		GroupReserveSendDto groupReserveSendOne = groupSendManage.SelectGroupReserveSendOne(groupReserveSend);
		
		model.addAttribute( "groupReserveSendOne", groupReserveSendOne );
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupSend/groupSendInfo";
	}
	
	@RequestMapping(value="groupSend/infoList",method=RequestMethod.GET)
	public String pushGroupSendInfoListGet( Model model , GroupSendDto groupSend){
		
		groupSend.setPAGE_SIZE(row_size);
		List<GroupSendDto> groupUserSendinfoList = groupSendManage.SelectGroupUserSendList(groupSend);
		
		model.addAttribute( "groupUserSendinfoList", groupUserSendinfoList );
		model.addAttribute("R_PAGE_NUM",groupSend.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("R_SEQNO",groupSend.getSEQNO());
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupSend/groupSendInfoList";
	}
	
	@RequestMapping(value="groupSend/receiveInfo",method=RequestMethod.GET)
	public String pushGroupSendInfoListInfoGet( Model model , PushUserSendDto pushUsrSend){
		
		List<PushUserSendDto> pushUserSendInfoList = groupSendManage.selectUserSendDetailList(pushUsrSend);
		
		model.addAttribute( "pushUserSendInfoList", pushUserSendInfoList );
		model.addAttribute( "R_SEQNO", pushUsrSend.getSEQNO() );
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/push/groupSend/groupSendReceiveInfo";
	}
	
	
}
