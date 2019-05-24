package kr.msp.admin.commonPush;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.push.sender.dto.PushSenderDto;
import kr.msp.admin.push.sender.service.PushSenderManageService;

@RequestMapping(value="admin/commonPush/popup")
@Controller
public class MsgSenderController {

	private final static Logger logger = LoggerFactory.getLogger(MsgSenderController.class);

	private @Value("${common.list.row_size}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size}") int page_size; //공통 페이지 페이지 수
	
	
	@Autowired(required=true)
	private PushSenderManageService pushSenderManageService;
	
	@RequestMapping(value="/msgSender", method=RequestMethod.GET )
	public String getCampaignManage( Model model, HttpServletRequest request, PushSenderDto pushSenderDto){
		pushSenderDto.setPAGE_SIZE(page_size);
		
		List<PushSenderDto> listPushSenderDto = pushSenderManageService.SelectSenderList(pushSenderDto);
		
		
		model.addAttribute("listMsgSender",listPushSenderDto);
		model.addAttribute("R_PAGE_NUM",pushSenderDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/msgSender/msgSenderPopUp";
	}
	
	@RequestMapping(value="/msgSender", method=RequestMethod.POST )
	public String getCampaignManagePost( Model model, HttpServletRequest request, PushSenderDto pushSenderDto){
		pushSenderDto.setPAGE_SIZE(page_size);
		List<PushSenderDto> listPushSenderDto = pushSenderManageService.SelectSenderList(pushSenderDto);
		
		model.addAttribute("listMsgSender",listPushSenderDto);
		model.addAttribute("R_PAGE_NUM",pushSenderDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/msgSender/msgSenderPopUpList";
	}
	
	@ResponseBody
	@RequestMapping(value="/addMsgSender", method=RequestMethod.POST )
	public String addMsgSender( Model model, HttpServletRequest request, PushSenderDto pushSenderDto) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		try{
			pushSenderManageService.InsertSender(pushSenderDto);
			result = 1;
		}catch(Exception e){
			if(e instanceof org.springframework.dao.DuplicateKeyException){
				result = 2;
			}
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/delMsgSender", method=RequestMethod.POST )
	public String delMsgSender( Model model, HttpServletRequest request, PushSenderDto pushSenderDto) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		try{
			pushSenderManageService.UpdateSenderDelete(pushSenderDto);
			result = 1;
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
