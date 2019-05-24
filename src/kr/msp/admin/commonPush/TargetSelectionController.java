package kr.msp.admin.commonPush;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.campaign.targetSelection.dto.TargetSelectionDTO;
import kr.msp.admin.campaign.targetSelection.dto.TargetSelectionParamDTO;
import kr.msp.admin.campaign.targetSelection.service.TargetSelectionService;
import kr.msp.admin.push.group.dto.PushUserGroupDto;
import kr.msp.admin.push.groupAuth.service.PushGroupAuthManageService;

@RequestMapping(value="admin/commonPush/popup")
@Controller
public class TargetSelectionController {
	
	private @Value("${common.list.row_size}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size}") int page_size; //공통 페이지 페이지 수
	
	@Autowired(required=true)
	TargetSelectionService targetSelectionService;
	
	@Autowired(required=true)
	PushGroupAuthManageService pushGroupAuthManageService;
	
	@RequestMapping(value="/targetSelection", method=RequestMethod.GET )
	public String getTargetSelection(Model model, HttpServletRequest request, TargetSelectionParamDTO targetSelectionParamDTO){
		List<PushUserGroupDto> pushUserGroupList = pushGroupAuthManageService.SelectPushGroupList();
		
		model.addAttribute( "pushUserGroupList", pushUserGroupList );
		targetSelectionParamDTO.setPAGE_SIZE(page_size);
		
		model.addAttribute("serviceCode",targetSelectionParamDTO.getSERVICECODE());
		model.addAttribute("appId",targetSelectionParamDTO.getAPPID());
		model.addAttribute("R_PAGE_NUM",targetSelectionParamDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/targetSelection/targetSelectionPopUp";
	}
	
	@RequestMapping(value="/targetSelection/user", method=RequestMethod.POST )
	public String getTargetSelectionUserPost(Model model, HttpServletRequest request, TargetSelectionParamDTO targetSelectionParamDTO){
		//사용자 조회
		targetSelectionParamDTO.setPAGE_SIZE(row_size);
		List<TargetSelectionDTO> listTargetSelectionDTO = null;
		
		if(targetSelectionParamDTO.getUSERIDTYPE().equals("CUID")){
			listTargetSelectionDTO = targetSelectionService.getTargetUserList(targetSelectionParamDTO);
		}else if(targetSelectionParamDTO.getUSERIDTYPE().equals("PSID")){
			listTargetSelectionDTO = targetSelectionService.getTargetUserPSIDList(targetSelectionParamDTO);
		}else{
			listTargetSelectionDTO = new ArrayList<TargetSelectionDTO>();
		}
		
        model.addAttribute("pushUserList", listTargetSelectionDTO);
		model.addAttribute("R_PAGE_NUM",targetSelectionParamDTO.getPAGE_NUM());
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/commonPush/popup/targetSelection/targetSelectionPopUpTabUserOutList";
	}
	
	@RequestMapping(value="/targetSelection/group", method=RequestMethod.POST )
	public String getTargetSelectionGroupPost(Model model, HttpServletRequest request, TargetSelectionParamDTO targetSelectionParamDTO){
		targetSelectionParamDTO.setPAGE_SIZE(row_size);
		
		List<TargetSelectionDTO> listTargetSelectionDTO = targetSelectionService.getTargetGroupList(targetSelectionParamDTO);
		
		model.addAttribute("groupList", listTargetSelectionDTO);
		model.addAttribute("R_PAGE_NUM",targetSelectionParamDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/targetSelection/targetSelectionPopUpTabGroupOutList";
	}
}
