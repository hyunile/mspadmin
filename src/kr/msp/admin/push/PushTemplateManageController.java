package kr.msp.admin.push;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.push.template.dto.TemplateDto;
import kr.msp.admin.push.template.service.TemplateManageService;
import kr.msp.common.util.FileUploadUtil;

@RequestMapping(value="admin/push")
@Controller
public class PushTemplateManageController {

	private final static Logger logger = LoggerFactory.getLogger(PushTemplateManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private TemplateManageService temlateManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@Resource(name ="fileUploadUtil")
	private FileUploadUtil fileUploadUtil;
	
	@RequestMapping(value="template", method=RequestMethod.GET)
	public String pushTemplateigGet( Model model){
		
		TemplateDto template = new TemplateDto();
		template.setPAGE_NUM(1);
		template.setPAGE_SIZE(row_size );
		
		List<TemplateDto> templateList = temlateManage.SelectTemplateList(template);
		
		model.addAttribute("templateList",templateList );
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/push/template/templateMain";
	}
	
	@RequestMapping(value="template" , method=RequestMethod.POST)
	public String pushTemplateigPost( Model model , TemplateDto template ){
		List<TemplateDto> templateList = temlateManage.SelectTemplateList(template);
		
		model.addAttribute("templateList",templateList );
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",template.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/template/templateList";
	}
	
	//템플릿 등록 화면
	@RequestMapping(value="template/regist" , method=RequestMethod.GET)
	public String pushTemplateRegistGet( Model model , TemplateDto template){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/template/templateModify";
	}
	
	//템플릿 수정 화면
	@RequestMapping(value="template/edit" , method=RequestMethod.GET)
	public String pushTemplateEditGet( Model model , TemplateDto template){
		
		TemplateDto templateOne = temlateManage.SelectTemplateOne(template);
		
		model.addAttribute("templateOne",templateOne);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/push/template/templateModify";
	}
	
	/**
	 * 템플릿 등록<br>
	 * 4.1 부터 template_code값을 서버에서 발급.<br>
	 * 기존의 template_code는 template_title 컬럼으로 처리.
	 * @param template
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Deprecated
	@ResponseBody
	@RequestMapping(value="template/check" , method=RequestMethod.POST)
	public String pushTemplateCheckPost( TemplateDto template ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = -1;
		
		try {
			result = temlateManage.SelectTemplateDupCheck(template);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.getError", null, LocaleContextHolder.getLocale()) );//"오류가 발생했습니다. 계속해서 같은 오류발생시 관리자에게 문의하십시오."
		} else if(result == 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.useTemplate", null, LocaleContextHolder.getLocale())); //"사용할수 있는 템플릿 ID 입나다." 
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.push.controller.notUseTemplate", null, LocaleContextHolder.getLocale())); //"사용할수 없는 템플릿 ID 입나다." 
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//템플릿 등록
	@ResponseBody
	@RequestMapping(value="template/regist" , method=RequestMethod.POST)
	public String pushTemplateRegistPost( TemplateDto template, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		template.setREG_ID((String) request.getSession().getAttribute("S_ID_USER"));
		template.setTEMPLATE_CODE(Long.toString(System.currentTimeMillis()));
		
		try {
			String imgUrl;
			if(template.getIMAGE_FILE()!=null && template.getIMAGE_FILE().getSize()>0){
				imgUrl = fileUploadUtil.msgImageUpload(template.getIMAGE_FILE());
				template.setIMAGE_URL(imgUrl);
			}
			result = temlateManage.InsertTemplate(template);
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
	
	//템플릿 수정
	@ResponseBody
	@RequestMapping(value="template/edit" , method=RequestMethod.POST)
	public String pushTemplateEditPost(TemplateDto template, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		template.setMOD_ID((String) request.getSession().getAttribute("S_ID_USER"));
		
		try {
			String imgUrl;
			if(template.getIMAGE_FILE()!=null && template.getIMAGE_FILE().getSize()>0){
				imgUrl = fileUploadUtil.msgImageUpload(template.getIMAGE_FILE());
				template.setIMAGE_URL(imgUrl);
			}
			result = temlateManage.UpdateTemplate(template);
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
	
	//템플릿 삭제
	@ResponseBody
	@RequestMapping(value="template/delete" , method=RequestMethod.POST)
	public String pushTemplateDeletePost( TemplateDto template ) throws JsonGenerationException, JsonMappingException, IOException {
		int result = 0;
		
		try {
			result = temlateManage.DeleteTemplate(template);
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
	
	//이미지 업로드 후 URL 발급.
	@ResponseBody
	@RequestMapping(value="template/imgupload" , method=RequestMethod.POST)
	public String pushTemplateImgUploadPost( @RequestParam("IMAGE_FILE") MultipartFile imgFile, HttpServletRequest request ) throws Exception {
		int result = 0;
		String imgUrl = "";
		try {
			if(imgFile!=null && imgFile.getSize()>0){
				imgUrl = fileUploadUtil.msgImageUpload(imgFile);
				result = 1;
			}
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg","success. Image URL:" + imgUrl );
			map.put("imgUrl", imgUrl);
		} else {
			map.put("result", result);
			map.put("msg","fail to image upload." );
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
}
