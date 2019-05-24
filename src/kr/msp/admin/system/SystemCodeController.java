package kr.msp.admin.system;

import java.io.IOException;
import java.util.ArrayList;
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

import kr.msp.admin.system.code.dto.CodeGroupDto;
import kr.msp.admin.system.code.dto.CodeGroupParamDto;
import kr.msp.admin.system.code.dto.CodeGroupSearchDto;
import kr.msp.admin.system.code.dto.CodeSubDto;
import kr.msp.admin.system.code.dto.CodeSubParamDto;
import kr.msp.admin.system.code.service.SystemCodeService;

@Controller
@RequestMapping( value = "admin/system")
public class SystemCodeController {

	private final static Logger logger = LoggerFactory.getLogger(SystemCodeController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	
	@Autowired(required=true)
	SystemCodeService systemCodeService;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 
	
	@RequestMapping( value = "code" , method = RequestMethod.GET)
	public String CodeGet(Model model,CodeGroupParamDto codeGroupParam){
		
		codeGroupParam.setPAGE_NUM(1);
		codeGroupParam.setPAGE_SIZE(row_size);
		
		//공통코드 사용
		ArrayList<String> GET_CODE = new ArrayList<String>();
		//GET_CODE.add("sy001");
		//model.addAttribute("GET_CODE",GET_CODE );
		
		//공통코드그룹 조회
		List<CodeGroupDto> codeGroupList = systemCodeService.SelectCodeGroup(codeGroupParam);
		model.addAttribute("R_PAGE_NUM",codeGroupParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("codeGroupList", codeGroupList);
		return "admin/system/code/codeMain";
	}
	
	@RequestMapping( value = "code/grp" , method = RequestMethod.POST )
	public String CodeGrpPost(Model model,CodeGroupParamDto codeGroupParam,CodeGroupSearchDto codeGroupSearch){
		
		codeGroupParam.setPAGE_NUM(codeGroupSearch.getCODE_NUM());
		if(!codeGroupSearch.getSE_CD_GRP().equals("")){
			codeGroupParam.setCD_GRP( codeGroupSearch.getSE_CD_GRP() );
		}
		
		if(!codeGroupSearch.getSE_NM_GRP().equals("")){
			codeGroupParam.setNM_GRP(codeGroupSearch.getSE_NM_GRP() );
		}
		
		//공통코드그룹 조회
		List<CodeGroupDto> codeGroupList = systemCodeService.SelectCodeGroup(codeGroupParam);
		model.addAttribute( "codeGroupList", codeGroupList );
		model.addAttribute("R_PAGE_NUM",codeGroupParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",codeGroupParam.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/code/codeGrpList";
	}
	
	@ResponseBody
	@RequestMapping( value = "code/grp/delete" , method = RequestMethod.POST )
	public String CodeGrpDelete(Model model,CodeGroupParamDto codeGroupParam ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = systemCodeService.DeleteCodeGroup(codeGroupParam);
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
	
	
	@RequestMapping( value = "code/grp/edit" , method = RequestMethod.GET)
	public String CodeGrpEditGet(Model model,CodeGroupParamDto codeGroupParamDto){
		
		CodeGroupDto codeGroup = systemCodeService.SelectOneCodeGroup(codeGroupParamDto);
		
		model.addAttribute("codeGroup", codeGroup);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/code/codeGropModify";
	}
	
	@ResponseBody
	@RequestMapping( value = "code/grp/edit" , method = RequestMethod.POST)
	public String CodeGrpEditPost(Model model, CodeGroupDto codeGroup,HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		codeGroup.setID_UPDATE((String) request.getSession().getAttribute("S_ID_USER"));
		
		int result = 0;
		
		try {
			result = systemCodeService.UpdateCodeGroup(codeGroup);
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
	
	@RequestMapping( value = "code/grp/regist" , method = RequestMethod.GET)
	public String CodeGrpRegistGet(Model model){
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/code/codeGropModify";
	}
	
	@ResponseBody
	@RequestMapping( value = "code/grp/regist" , method = RequestMethod.POST )
	public String CodeGrpRegistPost(Model model, CodeGroupDto codeGroup,HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		codeGroup.setID_INSERT((String) request.getSession().getAttribute("S_ID_USER"));
		int result = 0;
		
		try {
			result = systemCodeService.InsertCodeGroup(codeGroup);
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
	
	//공통코드 죄회
	@RequestMapping( value = "code/sub" , method = RequestMethod.POST )
	public String CodeSubPost( Model model, CodeSubParamDto codeSubParam ){
		
		System.out.println("code/sub::::::::::");
		
		List<CodeSubDto> codeSubList = systemCodeService.SelectCode(codeSubParam);
		model.addAttribute("codeSubList",codeSubList);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/code/codeSubList";
	}
	
	//코드 등록 화면
	@RequestMapping( value = "code/sub/regist" , method = RequestMethod.GET)
	public String CodeSubRegistGet(Model model){
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/code/codeSubModify";
	}
	
	//코드 등록
	@ResponseBody
	@RequestMapping( value = "code/sub/regist" , method = RequestMethod.POST)
	public String CodeSubRegistPost( CodeSubDto codeSub , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		
		codeSub.setID_INSERT((String) request.getSession().getAttribute("S_ID_USER"));
		int result = 0;
		
		try {
			result = systemCodeService.InsertCode(codeSub);
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
	
	//코드 등록 화면
	@RequestMapping( value = "code/sub/edit" , method = RequestMethod.GET)
	public String CodeSubEditGet(Model model , CodeSubParamDto codeSubParam){
		CodeSubDto codeSub = systemCodeService.SelectOneCode(codeSubParam);
		model.addAttribute("codeSub",codeSub);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/code/codeSubModify";
	}
	
	//코드 등록
	@ResponseBody
	@RequestMapping( value = "code/sub/edit" , method = RequestMethod.POST)
	public String CodeSubEditPost(CodeSubDto codeSub , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		codeSub.setID_UPDATE((String) request.getSession().getAttribute("S_ID_USER"));
		int result = systemCodeService.UpdateCode(codeSub);
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
	
	//코드 삭제
	@ResponseBody
	@RequestMapping( value = "code/sub/delete" , method = RequestMethod.POST)
	public String CodeSubDeletePost(CodeSubParamDto codeSubParam ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = systemCodeService.DeleteCode(codeSubParam);
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
