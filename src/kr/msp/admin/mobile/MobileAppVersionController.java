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

import kr.msp.admin.mobile.appVersion.dto.MobileAppVersionDto;
import kr.msp.admin.mobile.appVersion.dto.PlatListDto;
import kr.msp.admin.mobile.appVersion.service.MobileAppVersionService;
import kr.msp.admin.mobile.mobileService.dto.MobileServiceDto;
import kr.msp.admin.mobile.mobileService.service.MobileServiceManage;

@Controller
@RequestMapping(value="admin/mobile")
public class MobileAppVersionController {

	private final static Logger logger = LoggerFactory.getLogger(MobileAppVersionController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	protected MobileAppVersionService manager;
	
	@Autowired(required=true)
	MobileServiceManage mobileServiceManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	//조회 
	@RequestMapping(value="appVersion", method=RequestMethod.GET)
	public String RscAppVersionGet( Model model, HttpServletRequest request )
	{
		
		MobileServiceDto mobileService = new MobileServiceDto();
		mobileService.setPAGE_NUM(1);
		mobileService.setPAGE_SIZE(row_size);
		mobileService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<MobileServiceDto> mobileServiceList = mobileServiceManage.GetAppServiceList(mobileService);
		
		model.addAttribute( "mobileServiceList", mobileServiceList );
		model.addAttribute("R_PAGE_NUM",mobileService.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/mobile/appVersion/appVersionMain";
	}
	
	@RequestMapping(value="appVersion", method=RequestMethod.POST)
	public String RscAppVersionPost( Model model, MobileServiceDto mobileService , HttpServletRequest request ) 
	{
		mobileService.setPAGE_SIZE(row_size);
		mobileService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<MobileServiceDto> mobileServiceList = mobileServiceManage.GetAppServiceList(mobileService);
		
		model.addAttribute( "mobileServiceList", mobileServiceList );
		model.addAttribute("R_PAGE_NUM",mobileService.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/appVersion/mobServiseList";
	}
	
	@RequestMapping(value="appVersion/list", method=RequestMethod.POST)
	public String RscAppVersionListPost( Model model, MobileAppVersionDto appVersion , HttpServletRequest request ) 
	{
		appVersion.setPAGE_SIZE(row_size);
		List<MobileAppVersionDto> appVersionList = manager.selectMobileAppService(appVersion);
		
		model.addAttribute( "appVersionList", appVersionList );
		model.addAttribute("R_PAGE_NUM",appVersion.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/appVersion/appVersionList";
	}
	
	
	//상세
	@RequestMapping(value="appVersion/edit", method=RequestMethod.GET)
	public String RscAppVersionEditGet( Model model, MobileAppVersionDto mobileAppVersion) 
	{		
		List<PlatListDto> platList = manager.SelectPlatList();
		
		model.addAttribute("R_SVC_NM",mobileAppVersion.getSVC_NM());
		model.addAttribute("platList", platList);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/appVersion/appVersionInfo";
	}
	
	//수정
	
	@ResponseBody
	@RequestMapping( value="appVersion/edit" ,method=RequestMethod.POST)
	public String RscAppVersionEditPost(Model model, MobileAppVersionDto mobileAppVersion, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		mobileAppVersion.setMOD_ID((String)request.getSession().getAttribute("S_ID_USER"));
		
		try {
			result = manager.updateMobileAppVersion(mobileAppVersion);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale())  ); //수정 되었습니다.
			
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //수정에 실패했습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//등록
	
	@RequestMapping(value="appVersion/regist", method=RequestMethod.GET)
	public String RscAppVersionRegistGet( Model model, HttpServletRequest request , MobileAppVersionDto mobileAppVersion) 
	{
		List<PlatListDto> platList = manager.SelectPlatList();
		List<MobileAppVersionDto> maxAppVersionList = manager.selectMobileAppVersionMax(mobileAppVersion);
		
		model.addAttribute("platList", platList);
		model.addAttribute("maxAppVersionList",maxAppVersionList);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/mobile/appVersion/appVersionInfo";
	}
	
	@ResponseBody
	@RequestMapping( value="appVersion/regist" ,method=RequestMethod.POST)
	public String RscAppVersionRegistPost(Model model, MobileAppVersionDto mobileAppVersion , HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		mobileAppVersion.setREG_ID((String)request.getSession().getAttribute("S_ID_USER"));

		if(mobileAppVersion.getPLAT_IDX() == null)
		{
			mobileAppVersion.setPLAT_IDX("");
		}
		
		try {
			result = manager.insertMobileAppVersion(mobileAppVersion);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //등록에 실패했습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping( value="appVersion/delete" ,method=RequestMethod.POST)
	public String RscAppVersionDeletePost(Model model, MobileAppVersionDto mobileAppVersion ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = manager.deleteMobileAppVersion(mobileAppVersion);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //삭제 되었습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
