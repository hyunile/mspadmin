package kr.msp.admin.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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

import kr.msp.admin.push.pnsid.dto.PushPnsidDto;
import kr.msp.admin.push.pnsid.service.PushPnsidManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushPnsidManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushPnsidManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private PushPnsidManageService pushPnsidManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="pnsid", method=RequestMethod.GET)
	public String pushPnsidigGet( Model model){
		
		PushPnsidDto pushPnsid = new PushPnsidDto();
		pushPnsid.setPAGE_NUM(1);
		pushPnsid.setPAGE_SIZE( row_size );
		
		List<PushPnsidDto> pushPnsidList = pushPnsidManage.SelectPnsidList(pushPnsid);
		
		model.addAttribute("pushPnsidList", pushPnsidList );
		model.addAttribute("R_PAGE_NUM",pushPnsid.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/pnsid/pnsidMain";
	}
	
	@RequestMapping(value="pnsid" , method=RequestMethod.POST)
	public String pushPnsidigPost( Model model , PushPnsidDto pushPnsid ){
		List<PushPnsidDto> pushPnsidList = pushPnsidManage.SelectPnsidList(pushPnsid);
		
		model.addAttribute("pushPnsidList",pushPnsidList );
		model.addAttribute("R_PAGE_NUM",pushPnsid.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",pushPnsid.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/pnsid/pnsidList";
	}
	
	//PNS ID 등록 화면
	@RequestMapping(value="pnsid/regist" , method=RequestMethod.GET)
	public String pushPnsidRegistGet( Model model , PushPnsidDto pushPnsid ){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/pnsid/pnsidModify";
	}
	
	//PNS ID 수정 화면
	@RequestMapping(value="pnsid/edit" , method=RequestMethod.GET)
	public String pushPnsidEditGet( Model model , PushPnsidDto pushPnsid ){
		
		PushPnsidDto pushPnsidOne= pushPnsidManage.SelectPnsidOne(pushPnsid);
		
		model.addAttribute("pushPnsidOne",pushPnsidOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/pnsid/pnsidModify";
	}
	
	//PNS ID 등록
	@ResponseBody
	@RequestMapping(value="pnsid/check" , method=RequestMethod.POST)
	public String pushPnsidCheckPost( PushPnsidDto pushPnsid ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = -1;
		
		try {
			result = pushPnsidManage.SelectPnsidDupCheck(pushPnsid);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.getError", null, LocaleContextHolder.getLocale()) );//"오류가 발생했습니다. 계속해서 같은 오류발생시 관리자에게 문의하십시오."
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.PNSID", null, LocaleContextHolder.getLocale()) ); //사용할수 있는 PNS ID 입니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.noPNSID", null, LocaleContextHolder.getLocale()) ); //사용할수 없는 PNS ID 입니다. 
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//PNS ID 등록
	@ResponseBody
	@RequestMapping(value="pnsid/regist" , method=RequestMethod.POST)
	public String pushPnsidRegistPost( PushPnsidDto pushPnsid ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = pushPnsidManage.InsertPnsid(pushPnsid);
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
	
	//PNS ID 수정
	@ResponseBody
	@RequestMapping(value="pnsid/edit" , method=RequestMethod.POST)
	public String pushPnsidEditPost( PushPnsidDto pushPnsid ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = pushPnsidManage.UpdatePnsid(pushPnsid);
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
	
	//PNS ID 삭제
	@ResponseBody
	@RequestMapping(value="pnsid/delete" , method=RequestMethod.POST)
	public String pushPnsidDeletePost( PushPnsidDto pushPnsid ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = pushPnsidManage.DeletePnsid(pushPnsid);
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
