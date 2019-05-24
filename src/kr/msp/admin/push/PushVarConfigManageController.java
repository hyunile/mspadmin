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

import kr.msp.admin.push.varConfig.dto.VarConfigDto;
import kr.msp.admin.push.varConfig.service.VarConfigManageService;

@RequestMapping(value="admin/push")
@Controller
public class PushVarConfigManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushVarConfigManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private VarConfigManageService varConfigManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@RequestMapping(value="varConfig", method=RequestMethod.GET)
	public String pushVarConfigGet( Model model){
		
		VarConfigDto varConfig = new VarConfigDto();
		varConfig.setPAGE_NUM(1);
		varConfig.setPAGE_SIZE(row_size );
		
		List<VarConfigDto> varConfigList = varConfigManage.SelectVarList(varConfig);
		
		model.addAttribute("varConfigList",varConfigList );
		model.addAttribute("R_PAGE_NUM",varConfig.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/varConfig/varConfigMain";
	}
	
	@RequestMapping(value="varConfig" , method=RequestMethod.POST)
	public String pushVarConfigPost( Model model , VarConfigDto varConfig ){
		List<VarConfigDto> varConfigList = varConfigManage.SelectVarList(varConfig);
		
		model.addAttribute("varConfigList",varConfigList );
		model.addAttribute("R_PAGE_NUM",varConfig.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",varConfig.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/varConfig/varConfigList";
	}
	
	//다말변수 등록 화면
	@RequestMapping(value="varConfig/regist" , method=RequestMethod.GET)
	public String pushVarConfigRegistGet( Model model , VarConfigDto varConfig){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/varConfig/varConfigModify";
	}
	
	/**
	 * 단말변수 등록프로세스
	 * @param varConfig
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 1. 11. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping(value="varConfig/regist" , method=RequestMethod.POST)
	public String pushVarConfigRegistPost( VarConfigDto varConfig ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = varConfigManage.InsertVar(varConfig);
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
	
	//다말변수 수정 화면
	@RequestMapping(value="varConfig/edit" , method=RequestMethod.GET)
	public String pushVarConfigEditGet( Model model , VarConfigDto varConfig){
		
		VarConfigDto varConfigOne = varConfigManage.SelectVarOne(varConfig);
		
		model.addAttribute("varConfigOne",varConfigOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/varConfig/varConfigModify";
	}
	
	//다말변수 등록
	@ResponseBody
	@RequestMapping(value="varConfig/check" , method=RequestMethod.POST)
	public String pushVarConfigCheckPost( VarConfigDto varConfig ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = -1;
		
		try {
			result = varConfigManage.SelectVarDupCheck(varConfig);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.getError", null, LocaleContextHolder.getLocale()) );//"오류가 발생했습니다. 계속해서 같은 오류발생시 관리자에게 문의하십시오."
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.useSenderCode", null, LocaleContextHolder.getLocale()) ); //"사용할수 있는 발송자 코드입나다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.notUseSenderCode", null, LocaleContextHolder.getLocale()) ); //"사용할수 없는 발송자 코드입나다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	
	
	//다말변수 수정
	@ResponseBody
	@RequestMapping(value="varConfig/edit" , method=RequestMethod.POST)
	public String pushVarConfigEditPost( VarConfigDto varConfig ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = varConfigManage.UpdateVar(varConfig);
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
	
	//다말변수 삭제
	@ResponseBody
	@RequestMapping(value="varConfig/delete" , method=RequestMethod.POST)
	public String pushVarConfigDeletePost( VarConfigDto varConfig ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = varConfigManage.DeleteVar(varConfig);
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