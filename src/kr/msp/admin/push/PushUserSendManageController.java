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
import kr.msp.admin.push.sender.dto.PushSenderDto;
import kr.msp.admin.push.userSend.dto.PushUserSendDto;
import kr.msp.admin.push.userSend.service.PushUserSendManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushUserSendManageController {
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
		
		@Autowired(required=true)
		private PushUserSendManageService pushUserSendManage;
		
		@RequestMapping(value="userSend",method=RequestMethod.GET)
		public String pushUserSendGet(HttpServletRequest request,Model model){
			
			//PUSH 서비스 조회
			PushServiceDto pushService = new PushServiceDto();
			pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
			List<PushServiceDto> pushServiceList = pushUserSendManage.SelectPushServiceAll(pushService);
			
			List<PushSenderDto> pushSenderList = pushUserSendManage.SelectSenderAll();
			
			model.addAttribute("pushServiceList",pushServiceList);
			model.addAttribute("pushSenderList",pushSenderList);
			
			return "admin/push/userSend/userSendMain";
		}
		
		@RequestMapping(value="userSend",method=RequestMethod.POST)
		public String pushUserSendPost( Model model, PushUserSendDto pushUserSend ){
			pushUserSend.setPAGE_SIZE(row_size);
			List<PushUserSendDto> pushUserSendList = pushUserSendManage.selectUserSendList(pushUserSend);
			
			model.addAttribute( "pushUserSendList", pushUserSendList );
			model.addAttribute("R_PAGE_NUM",pushUserSend.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",row_size);
			model.addAttribute("R_PAGE_SIZE",page_size);
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/push/userSend/userSendList";
		}
		
		@RequestMapping(value="userSend/infoList",method=RequestMethod.POST)
		public String pushUserSendInfoListPost( Model model, PushUserSendDto pushUsrSend ){
			
			List<PushUserSendDto> pushUserSendInfoList = pushUserSendManage.selectUserSendDetailList(pushUsrSend);
			
			model.addAttribute( "pushUserSendInfoList", pushUserSendInfoList );
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/push/userSend/userSendInfoList";
		}
}
