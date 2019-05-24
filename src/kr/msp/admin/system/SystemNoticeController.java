package kr.msp.admin.system;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.system.notice.dto.NoticeAuthGroupDto;
import kr.msp.admin.system.notice.dto.NoticeAuthGroupParamDto;
import kr.msp.admin.system.notice.dto.NoticeDto;
import kr.msp.admin.system.notice.dto.NoticeSerchDto;
import kr.msp.admin.system.notice.service.NoticeManageService;

@Controller
public class SystemNoticeController {

	private final static Logger logger = LoggerFactory.getLogger(SystemNoticeController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size; 
	
	@Autowired(required=true)
	protected NoticeManageService systemManageManager;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 

	@RequestMapping(value="admin/main", method=RequestMethod.GET)
	public String NoticeManage(Model model , HttpServletRequest request, @ModelAttribute("NO_NOTICE") String NO_NOTICE){
		
		List<NoticeDto> list = systemManageManager.SelectMainNoticeList(request);
		model.addAttribute("list", list);
			
		return "admin/common/noticeManage";
	}
	
	@RequestMapping(value="admin/main", method=RequestMethod.POST)
	public String NoticeManagePost(Model model , @ModelAttribute("NO_NOTICE") String NO_NOTICE){
			
		//공지사항 리스트
		NoticeDto entbox_notice = systemManageManager.SelectMainNoticePopData(NO_NOTICE);
			
		model.addAttribute("entbox_notice", entbox_notice);
		model.addAttribute("layout", "layout/null.vm");
			
		return "admin/common/mainNoticePop";
	}
	
	@RequestMapping( value="admin/system/notice" ,method=RequestMethod.GET)
	public String NoticeMainGet(Model model){
		NoticeDto noticeDto = new NoticeDto();
		noticeDto.setPAGE_NUM(1);
		noticeDto.setPAGE_SIZE(row_size);
		//공지사항 조회
		List<NoticeDto> noticeList = systemManageManager.SelectNoticeList(noticeDto);
		model.addAttribute("noticeList", noticeList);
		model.addAttribute("R_PAGE_NUM",noticeDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/system/notice/noticeMain";
	}
	
	@RequestMapping( value="admin/system/notice/auth", method=RequestMethod.GET)
	public String NoticeAuthGet(Model model, NoticeAuthGroupParamDto noticeAuthGroupParam){
		
		//공지사항 권한정보 조회
		if(!noticeAuthGroupParam.getNO_NOTICE().equals("")){
			List<NoticeAuthGroupDto> systemAuthGroupList = systemManageManager.SelectNoticeAuthGroupList(noticeAuthGroupParam);
			model.addAttribute("systemAuthGroupList",systemAuthGroupList);
		}
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/notice/noticeAuthGroup";
	}
	
	@ResponseBody
	@RequestMapping( value="admin/system/notice/auth", method=RequestMethod.POST)
	public String NoticeAuthPost(Model model, NoticeAuthGroupDto noticeAuthGroup, @ModelAttribute("COMMAND") String COMMAND) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		
		try {
			if(COMMAND.equals("insert")){
				result = systemManageManager.InsertNoticeAuthGroup(noticeAuthGroup);
			} else if(COMMAND.equals("delete")){
				result = systemManageManager.DeleteNoticeAuthGroup(noticeAuthGroup);
			}
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.system.controller.apply", null, LocaleContextHolder.getLocale())); //적용 되었습니다.
		} else {
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.system.controller.applyFail", null, LocaleContextHolder.getLocale())); //적용에 실패했습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@RequestMapping( value="admin/system/notice" ,method=RequestMethod.POST)
	public String NoticeMainPost(Model model, NoticeDto noticeDto , NoticeSerchDto noticeSerchDto){
		
		noticeDto.setPAGE_SIZE(row_size);
		
		if( noticeSerchDto.getSEARCH_CONTENTS() != null && !noticeSerchDto.getSEARCH_CONTENTS().equals("") ){
			noticeDto.setCONTENTS(noticeSerchDto.getSEARCH_CONTENTS());
		}
		
		if(noticeSerchDto.getSEARCH_NM_TITLE() != null && !noticeSerchDto.getSEARCH_NM_TITLE().equals("")){
			noticeDto.setNM_TITLE(noticeSerchDto.getSEARCH_NM_TITLE());
		}
		
		//공지사항 조회
		List<NoticeDto> noticeList = systemManageManager.SelectNoticeList(noticeDto);
		model.addAttribute("noticeList", noticeList);
		model.addAttribute("R_PAGE_NUM",noticeDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/system/notice/noticeList";
	}
	
	@RequestMapping( value="/admin/system/notice/regist" , method=RequestMethod.GET )
	public String NoticeRegistGet(Model model){
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/notice/noticeInfo";
	}
	
	@ResponseBody
	@RequestMapping( value="/admin/system/notice/regist" , method=RequestMethod.POST)
	public String NoticeRegistPost(Model model, HttpServletRequest request, NoticeDto noticeDto ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		String NO_NOTICE = systemManageManager.SelectNewNoNotice();
		noticeDto.setNO_NOTICE(NO_NOTICE);
		noticeDto.setID_INSERT((String) request.getSession().getAttribute("S_ID_USER"));
		
		try {
			//신규팝업 등록
			result = systemManageManager.InsertNotice(noticeDto);
		} catch (Exception e) {
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
	
	@RequestMapping( value="admin/system/notice/edit" ,method=RequestMethod.GET)
	public String NoticeEditGet(Model model, NoticeDto noticeDto){
		
		NoticeDto noticeInfoDto = systemManageManager.SelectMainNoticePopData(noticeDto.getNO_NOTICE());
		
		model.addAttribute("noticeDto", noticeInfoDto);
		model.addAttribute("layout","layout/null.vm");
		return "admin/system/notice/noticeInfo";
	}
	
	@ResponseBody
	@RequestMapping( value="admin/system/notice/edit" ,method=RequestMethod.POST)
	public String NoticeEditPost(Model model, NoticeDto noticeDto , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		noticeDto.setID_UPDATE((String)request.getSession().getAttribute("S_ID_USER"));
		
		try {
			//신규팝업 등록
			result = systemManageManager.UpdateNotice(noticeDto);
		} catch (Exception e) {
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
	
	@ResponseBody
	@RequestMapping( value="admin/system/notice/delete" ,method=RequestMethod.POST)
	public String NoticeEditDelete(Model model, NoticeDto noticeDto , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		noticeDto.setID_UPDATE((String)request.getSession().getAttribute("S_ID_USER"));
		
		try {
			//신규팝업 등록
			result = systemManageManager.DeleteNotice(noticeDto);
		} catch (Exception e) {
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