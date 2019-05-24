package kr.msp.admin.commonPush;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.push.template.dto.TemplateDto;

/**
 * 타켓팅 대상 확인 공통 팝업 컨트롤러.
 */
@RequestMapping(value="admin/commonPush")
@Controller
public class TargetVerifyPopupController {
	
	private @Value("${common.list.row_size}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size}") int page_size; //공통 페이지 페이지 수
	
	/**
	 * 타켓팅 대상 확인 공통 팝업 메인 페이지.
	 * @param model
	 * @return
	 */
	@RequestMapping(value="popup/verifytarget", method=RequestMethod.GET)
	public String getVerifyTargetPopup( Model model){
		
		TemplateDto template = new TemplateDto();
		template.setPAGE_NUM(1);
		template.setPAGE_SIZE(row_size );
		
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/verifytarget/main";
	}
	
	/**
	 * 타켓팅 대상 확인 공통 팝업 리스트 조회.
	 * @param model
	 * @param template
	 * @return
	 */
	@RequestMapping(value="popup/verifytarget" , method=RequestMethod.POST)
	public String getVerifyTargetPopupPost( Model model , TemplateDto template ){
		template.setPAGE_SIZE(row_size );
		
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",template.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/commonPush/popup/verifytarget/list";
	}
}
