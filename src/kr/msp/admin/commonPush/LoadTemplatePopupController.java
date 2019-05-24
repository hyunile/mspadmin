package kr.msp.admin.commonPush;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.push.template.dto.TemplateDto;
import kr.msp.admin.push.template.service.TemplateManageService;

/**
 * 템플릿 불러오기 공통 팝업 컨트롤러.
 */
@RequestMapping(value="admin/commonPush")
@Controller
public class LoadTemplatePopupController {
	
	private @Value("${common.list.row_size}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size}") int page_size; //공통 페이지 페이지 수
	
	@Autowired(required=true)
	private TemplateManageService temlateManage;
	
	/**
	 * 템플릿 불러오기 공통 팝업 메인 페이지.
	 * @param model
	 * @return
	 */
	@RequestMapping(value="popup/template", method=RequestMethod.GET)
	public String getTemplatePopup( Model model){
		
		TemplateDto template = new TemplateDto();
		template.setPAGE_NUM(1);
		template.setPAGE_SIZE(row_size );
		
		List<TemplateDto> templateList = temlateManage.SelectTemplateList(template);
		
		model.addAttribute("templateList",templateList );
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/template/main";
	}
	
	/**
	 * 템플릿 불러오기 공통 팝업 리스트 조회.
	 * @param model
	 * @param template
	 * @return
	 */
	@RequestMapping(value="popup/template" , method=RequestMethod.POST)
	public String getTemplatePopupPost( Model model , TemplateDto template ){
		template.setPAGE_SIZE(row_size );
		List<TemplateDto> templateList = temlateManage.SelectTemplateList(template);
		
		model.addAttribute("templateList",templateList );
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",template.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/commonPush/popup/template/list";
	}
	
	/**
	 * 템플릿 불러오기 공통 팝업 단건 조회.
	 * @param model
	 * @param template
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="popup/template/detail" , method=RequestMethod.POST)
	public String getTemplateDetailPost( Model model , TemplateDto template ) throws Exception{
		
		TemplateDto templateOne = temlateManage.SelectTemplateOne(template);
		int result = 0;
		if(templateOne == null)result = 1;
		Map<String, Object> rtMap = new HashMap<String, Object>();
		rtMap.put("result", result);
		rtMap.put("data", templateOne);
		ObjectMapper mapper = new ObjectMapper();
		// 대문자로 키 값 세팅.
		mapper.setPropertyNamingStrategy(new LowerCaseNamingStrategy());
		String data = mapper.writeValueAsString(rtMap);
		return data;
	}
}
