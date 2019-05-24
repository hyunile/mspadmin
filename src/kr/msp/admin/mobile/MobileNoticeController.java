package kr.msp.admin.mobile;

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

import kr.msp.admin.mobile.notice.dto.MobileNoticeDto;
import kr.msp.admin.mobile.notice.dto.MobileNoticeParamDto;
import kr.msp.admin.mobile.notice.dto.MobileSvcDto;
import kr.msp.admin.mobile.notice.service.MobileNoticeService;

@Controller
public class MobileNoticeController {

	private final static Logger logger = LoggerFactory.getLogger(MobileNoticeController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	MobileNoticeService mobileNoticeService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	//메인화면
	@RequestMapping(value="admin/mobile/notice",method=RequestMethod.GET)
	public String mobileNoticeGet(Model model,HttpServletRequest request ){
		
		MobileNoticeParamDto mobileNoticeParam = new MobileNoticeParamDto();
		mobileNoticeParam.setPAGE_NUM(1);
		mobileNoticeParam.setPAGE_SIZE(row_size);
		mobileNoticeParam.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		List<MobileNoticeDto> mobileNoticeList = mobileNoticeService.GetClientNoticeList(mobileNoticeParam);
		
		MobileSvcDto mobileSvc = new MobileSvcDto();
		mobileSvc.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		List<MobileSvcDto> mobileSvcList = mobileNoticeService.GetMobileSvc(mobileSvc);
		
		model.addAttribute("R_PAGE_NUM",mobileNoticeParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("mobileSvcList",mobileSvcList);
		model.addAttribute("mobileNoticeList", mobileNoticeList );
		return "admin/mobile/notice/mobileNoticeMain";
	}
	
	//모바일 공지사항 조회
	@RequestMapping(value="admin/mobile/notice",method=RequestMethod.POST)
	public String mobileNoticePost(Model model, MobileNoticeParamDto mobileNoticeParam,HttpServletRequest request){
		
		mobileNoticeParam.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		List<MobileNoticeDto> mobileNoticeList = mobileNoticeService.GetClientNoticeList(mobileNoticeParam);
		
		MobileSvcDto mobileSvc = new MobileSvcDto();
		mobileSvc.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		List<MobileSvcDto> mobileSvcList = mobileNoticeService.GetMobileSvc(mobileSvc);
		
		model.addAttribute("R_PAGE_NUM",mobileNoticeParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",mobileNoticeParam.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("mobileSvcList",mobileSvcList);
		model.addAttribute("mobileNoticeList", mobileNoticeList );
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/mobile/notice/mobileNoticeList";
	}
	
	//모바일 공지사항 등록 화면
	@RequestMapping(value="admin/mobile/notice/regist",method=RequestMethod.GET)
	public String mobileNoticeRegistGet(Model model,HttpServletRequest request){
		
		MobileSvcDto mobileSvc = new MobileSvcDto();
		mobileSvc.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		List<MobileSvcDto> mobileSvcList = mobileNoticeService.GetMobileSvc(mobileSvc);
		
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("mobileSvcList",mobileSvcList);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/mobile/notice/noticeInfo";
	}
	
	//모바일 공지사항 수정 화면
	@RequestMapping(value="admin/mobile/notice/edit",method=RequestMethod.GET)
	public String mobileNoticeEditGet(HttpServletRequest request, Model model,MobileNoticeDto mobileNoticeDto){
		
		MobileNoticeDto mobileNoticeOne = mobileNoticeService.GetClientNoticeOneList(mobileNoticeDto);
		
		MobileSvcDto mobileSvc = new MobileSvcDto();
		mobileSvc.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		List<MobileSvcDto> mobileSvcList = mobileNoticeService.GetMobileSvc(mobileSvc);
		
		model.addAttribute("mobileSvcList",mobileSvcList);
		model.addAttribute("mobileNoticeOne",mobileNoticeOne);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/mobile/notice/noticeInfo";
	}

	//모바일 공지사항 등록
	@ResponseBody
	@RequestMapping(value="admin/mobile/notice/regist",method=RequestMethod.POST)
	public String mobileNoticeRegistPOST(Model model,HttpServletRequest request,MobileNoticeDto mobileNoticeDto) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = mobileNoticeService.InsertMobileNotice(mobileNoticeDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//모바일 공지사항 등록 
	@ResponseBody
	@RequestMapping(value="admin/mobile/notice/edit",method=RequestMethod.POST)
	public String mobileNoticeEditPOST(Model model,HttpServletRequest request,MobileNoticeDto mobileNoticeDto) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = mobileNoticeService.UpdateMobileNotice(mobileNoticeDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//모바일 공지사항 등록 
	@ResponseBody
	@RequestMapping(value="admin/mobile/notice/delete",method=RequestMethod.POST)
	public String mobileNoticeDeletePOST(Model model,HttpServletRequest request,MobileNoticeDto mobileNoticeDto) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = mobileNoticeService.DeleteMobileNotice(mobileNoticeDto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
