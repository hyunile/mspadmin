package kr.msp.admin.commonPush;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import kr.msp.admin.campaign.campaignPush.dto.TestGroupUserDTO;
import kr.msp.admin.campaign.campaignPush.service.TestGroupUserService;

/**
 * 테스터 선택 공통 팝업 컨트롤러.
 */
@RequestMapping(value="admin/commonPush")
@Controller
public class TesterSelectionPopupController {
	
	private @Value("${common.list.row_size:20}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size:20}") int page_size; //공통 페이지 페이지 수

	@Autowired(required = true)
	private TestGroupUserService testGroupUserService;
	
	/**
	 * 테스터 선택 공통 팝업 메인 페이지.
	 * @param model
	 * @return
	 */
	@RequestMapping(value="popup/tester", method=RequestMethod.GET)
	public String getTesterPopup( Model model,@RequestParam Map<String,String> reqMap){
		reqMap.put("PAGE_NUM", "1");
		reqMap.put("PAGE_SIZE", ""+row_size);
		List<TestGroupUserDTO> testGroupUserDTOList = testGroupUserService.getTestGroupUser(reqMap);

		model.addAttribute("R_PAGE_NUM",reqMap.get("PAGE_NUM"));
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		model.addAttribute("testGroupUserDTOList", testGroupUserDTOList);
		return "admin/commonPush/popup/tester/main";
	}
	
	/**
	 * 테스터 선택 공통 팝업 리스트 조회.
	 * @param model
	 * @param reqMap
	 * @return
	 */
	@RequestMapping(value="popup/tester" , method=RequestMethod.POST)
	public String getTesterPopupPost( Model model ,@RequestParam Map<String,String> reqMap ){
		reqMap.put("PAGE_SIZE",""+row_size);
		List<TestGroupUserDTO> testGroupUserDTOList = testGroupUserService.getTestGroupUser(reqMap);
		
		model.addAttribute("R_PAGE_NUM", reqMap.get("PAGE_NUM"));
		model.addAttribute("R_ROW_SIZE",reqMap.get("PAGE_SIZE"));
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("testGroupUserDTOList", testGroupUserDTOList);
		
		return "admin/commonPush/popup/tester/list";
	}
}
