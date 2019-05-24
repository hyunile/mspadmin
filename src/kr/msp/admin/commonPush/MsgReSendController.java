package kr.msp.admin.commonPush;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value="admin/commonPush")
@Controller
public class MsgReSendController {
	
    
	private @Value("${common.list.row_size}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size}") int page_size; //공통 페이지 페이지 수
	
	@RequestMapping(value="popup/msgReSend", method=RequestMethod.POST)
	public String getPreviewPopup( Model model){

		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/msgReSend/msgReSend";
	}
}
