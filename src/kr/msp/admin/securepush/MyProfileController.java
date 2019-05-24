package kr.msp.admin.securepush;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.securepush.manageAccount.dto.ManageAccountDto;
import kr.msp.admin.securepush.manageAccount.dto.ManageAccountParamDto;
import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;

/**
 * 시큐어 푸쉬 내 정보 메뉴 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class MyProfileController {

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private ManageAccountService accountService;
	
	/**
	 * 내 정보 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="myProfile" , method=RequestMethod.GET)
	public String myProfileGet( Model model, HttpServletRequest request ) throws Exception {

		ManageAccountParamDto paramDto = new ManageAccountParamDto();
		paramDto.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		ManageAccountDto accountDto = accountService.getManageAccountInfo(paramDto);
		if(accountDto != null && accountDto.getPHOTO_PATH() != null){
			// 관리자 페이지로부터의 접속은 인증키 체크 해제되어 있음. 필수파라메터로 더미값 적용.
			String _photoPath = accountDto.getPHOTO_PATH() + "&authkey=1";
			accountDto.setPHOTO_PATH(_photoPath);
		}
		model.addAttribute("itemInfo", accountDto);
		
		return "admin/securepush/myProfile/myProfileMain";
	}
}
