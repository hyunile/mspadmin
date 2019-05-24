package kr.msp.admin.push;

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

import kr.msp.admin.push.sender.dto.PushSenderDto;
import kr.msp.admin.push.sender.service.PushSenderManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushSenderManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushSenderManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	PushSenderManageService pushSenderManageService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="sender" , method=RequestMethod.GET)
	public String pushSenderGet( Model model){
		PushSenderDto pushSender = new PushSenderDto();
		pushSender.setPAGE_NUM(1);
		pushSender.setPAGE_SIZE(row_size);
		List<PushSenderDto> pushSenderList = pushSenderManageService.SelectSenderList(pushSender);
		
		model.addAttribute( "pushSenderList", pushSenderList );
		model.addAttribute("R_PAGE_NUM",pushSender.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/sender/senderMain";
	}
	
	@RequestMapping(value="sender" , method=RequestMethod.POST)
	public String pushSenderPost( Model model , PushSenderDto pushSender){
		List<PushSenderDto> pushSenderList = pushSenderManageService.SelectSenderList(pushSender);
		
		model.addAttribute( "pushSenderList", pushSenderList );
		model.addAttribute("R_PAGE_NUM",pushSender.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",pushSender.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/sender/senderList";
	}
	
	//발송자 등록 화면
	@RequestMapping(value="sender/regist" , method=RequestMethod.GET)
	public String pushSenderRegistPost( Model model , PushSenderDto pushSender){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/sender/senderModify";
	}
	
	//발송자 수정 화면
	@RequestMapping(value="sender/edit" , method=RequestMethod.GET)
	public String pushSenderEditPost( Model model , PushSenderDto pushSender){
		
		PushSenderDto pushSenderOne = pushSenderManageService.SelectSenderOne(pushSender);
		
		model.addAttribute("pushSenderOne",pushSenderOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/sender/senderModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="sender/check" , method=RequestMethod.POST)
	public String pushSenderCheckPost( PushSenderDto pushSender ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushSenderManageService.SelectSenderDupCheck(pushSender);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.notUseSenderCode", null, LocaleContextHolder.getLocale()) ); //사용할수 없는 발송자 코드입나다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.useSenderCode", null, LocaleContextHolder.getLocale()) ); //사용할수 있는 발송자 코드입나다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping(value="sender/regist" , method=RequestMethod.POST)
	public String pushSenderRegistPost(HttpServletRequest request, PushSenderDto pushSender ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			pushSender.setREGID((String) request.getSession().getAttribute("S_ID_USER"));
			result = pushSenderManageService.InsertSender(pushSender);
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
	
	//사용자 수정
	@ResponseBody
	@RequestMapping(value="sender/edit" , method=RequestMethod.POST)
	public String pushSenderEditPost(HttpServletRequest request, PushSenderDto pushSender ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			pushSender.setMODID((String) request.getSession().getAttribute("S_ID_USER"));
			result = pushSenderManageService.UpdateSender(pushSender);
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
	
	//사용자 삭제
	@ResponseBody
	@RequestMapping(value="sender/delete" , method=RequestMethod.POST)
	public String pushSenderDeletePost( PushSenderDto pushSender ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = pushSenderManageService.UpdateSenderDelete(pushSender);
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
