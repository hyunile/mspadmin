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

import kr.msp.admin.mobile.appVersion.dto.PlatListDto;
import kr.msp.admin.mobile.mobileService.dto.MobileServiceDto;
import kr.msp.admin.mobile.mobileService.dto.MobileServiceUpdateDto;
import kr.msp.admin.mobile.mobileService.service.MobileServiceManage;


@RequestMapping(value="admin/mobile")
@Controller
public class MobileServiceManageController {
	
	
	private static final Logger logger = LoggerFactory.getLogger(MobileServiceManageController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	MobileServiceManage mobileServiceManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping(value="mobileService",method=RequestMethod.GET)
	public String mobileServiceGet( Model model ,HttpServletRequest request){
		
		MobileServiceDto mobileService = new MobileServiceDto();
		mobileService.setPAGE_NUM(1);
		mobileService.setPAGE_SIZE(row_size);
		mobileService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<MobileServiceDto> mobileServiceList = mobileServiceManage.GetAppServiceList(mobileService);
		
		model.addAttribute( "mobileServiceList", mobileServiceList );
		model.addAttribute("R_PAGE_NUM",mobileService.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/mobile/mobileService/mobileServiceMain";
	}
	
	//모바일 서비스 리스트
	@RequestMapping(value="mobileService",method=RequestMethod.POST)
	public String mobileServicePost( Model model ,HttpServletRequest request, MobileServiceDto mobileService){
		
		mobileService.setPAGE_SIZE(row_size);
		mobileService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<MobileServiceDto> mobileServiceList = mobileServiceManage.GetAppServiceList(mobileService);
		
		model.addAttribute( "mobileServiceList", mobileServiceList );
		model.addAttribute("R_PAGE_NUM",mobileService.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/mobileService/mobileServiceList";
	}
	
	//모바일 서비스 상세 화면
	@RequestMapping(value="mobileService/info",method=RequestMethod.GET)
	public String mobileServiceInfoGet( Model model , MobileServiceDto mobileService){
			
		MobileServiceDto mobileServiceOne = mobileServiceManage.GetAppServiceOne(mobileService);
		
		model.addAttribute("mobileServiceOne", mobileServiceOne);
		model.addAttribute("layout", "layout/null.vm");
			
		return "admin/mobile/mobileService/mobileServiceInfo";
	}
	
	//모바일 서비스 업데이트 null 리스트
	@RequestMapping(value="mobileService/mobileUpdateList",method=RequestMethod.GET)
	public String mobileServiceUpdateListGet( Model model ){
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/mobileService/mobileServiceUpdateList";
	}
	
	//모바일 서비스 업데이트 리스트
	@RequestMapping(value="mobileService/mobileUpdateList",method=RequestMethod.POST)
	public String mobileServiceUpdateListPost( Model model , MobileServiceUpdateDto mobileServiceUpdate){
		
		mobileServiceUpdate.setPAGE_SIZE(row_size);
		List<MobileServiceUpdateDto> mobileServiceUpdateList = mobileServiceManage.GetAppUpdateList(mobileServiceUpdate);
		
		model.addAttribute( "mobileServiceUpdateList", mobileServiceUpdateList );
		model.addAttribute("R_PAGE_NUM",mobileServiceUpdate.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/mobileService/mobileServiceUpdateList";
	}
	
	//모바일 서비스 update version 등록 화면
	@RequestMapping(value="mobileService/regist",method=RequestMethod.GET)
	public String mobileServiceUpdateInsertGet( Model model ,HttpServletRequest request, MobileServiceUpdateDto mobileServiceUpdate){
		
		List<PlatListDto> platList = mobileServiceManage.SelectPlatList();
		
		model.addAttribute( "platList", platList );
		model.addAttribute( "layout", "layout/null.vm" );
		
		return "admin/mobile/mobileService/mobileVersionInfo";
	}
	
	//모바일 서비스 update version 등록
	@ResponseBody
	@RequestMapping(value="mobileService/regist",method=RequestMethod.POST)
	public String mobileServiceUpdateInsertPost( Model model , MobileServiceUpdateDto mobileServiceUpdate) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		try {
			logger.debug(mobileServiceUpdate.toString());
			int chkInt = mobileServiceManage.chkAppUpdate(mobileServiceUpdate);
            if(chkInt>0){  //이미 등록되어 있는 앱 일경우
                HashMap<String, Object> map = new HashMap<String,Object>();
                map.put("result", chkInt);
                map.put("msg",messageSource.getMessage("menu.mobile.common.alreadyIn", null, LocaleContextHolder.getLocale()) ); //해당 디바이스 유형으로 이미 등록되어 있는 모바일 서비스 입니다.\n\n수정버튼을 클릭 후 삭제 후 등록하여 사용해 주세요!
                ObjectMapper mapper = new ObjectMapper();
                String data = mapper.writeValueAsString(map);
                return data;
            }
			result = mobileServiceManage.InsertAppUpdate(mobileServiceUpdate);
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
	
	//사용자 중복체크
	@ResponseBody
	@RequestMapping(value="mobileService/check" , method=RequestMethod.POST)
	public String mobileServiceCheckPost( MobileServiceUpdateDto mobileServiceUpdate ) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
		
		try {
			result = mobileServiceManage.GetAppUpdateCheck(mobileServiceUpdate);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		if(result == -1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.highVersion", null, LocaleContextHolder.getLocale()) );  //중복하거나 높은 버젼이있습니다. 
		} else if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.highVersion", null, LocaleContextHolder.getLocale()) ); //중복하거나 높은 버젼이있습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.useVersion", null, LocaleContextHolder.getLocale()) );  //사용할수 있는 버젼입니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	/**
	 * 앱업데이트 => 모바일 업데이트 목록 => 수정화면호출
	 * @param model
	 * @param request
	 * @param mobileServiceUpdate
	 * @return
	 * @since 2014. 1. 10. by UncleJoe
	 */
	@RequestMapping(value="mobileService/edit",method=RequestMethod.GET)
	public String mobileServiceEditGet( Model model ,HttpServletRequest request, MobileServiceUpdateDto mobileServiceUpdate){
			
		MobileServiceUpdateDto moblileServiceUpdateOne = mobileServiceManage.GetAppUpdateOne(mobileServiceUpdate);
		
		
		logger.debug(">>>>>>>>>>" + moblileServiceUpdateOne);
		
		model.addAttribute("moblileServiceUpdateOne", moblileServiceUpdateOne);
		
		model.addAttribute("layout", "layout/null.vm");
			
		return "admin/mobile/mobileService/mobileVersionInfo";
	}
	
	//모바일 서비스 update version 수정
	@ResponseBody
	@RequestMapping(value="mobileService/edit",method=RequestMethod.POST)
	public String mobileServiceUpdatEditPost( MobileServiceUpdateDto mobileServiceUpdate) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = mobileServiceManage.UpdateAppUpdate(mobileServiceUpdate);
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
	
	//모바일 서비스 update version 삭제 화면
	@ResponseBody
	@RequestMapping(value="mobileService/delete",method=RequestMethod.POST)
	public String mobileServiceUpdatDeletePost( MobileServiceUpdateDto mobileServiceUpdate) throws JsonGenerationException, JsonMappingException, IOException{
		int result = 0;
		
		try {
			result = mobileServiceManage.DeleteAppUpdate(mobileServiceUpdate);
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
	
	//모바일 서비스 update version 삭제 화면
	@ResponseBody
	@RequestMapping(value="mobileService/appVersion",method=RequestMethod.POST)
	public String mobileServiceUpdatAppVersionPost( MobileServiceUpdateDto mobileServiceUpdate) throws JsonGenerationException, JsonMappingException, IOException{
		
		List<MobileServiceDto> appVersion = mobileServiceManage.selectAppVersion(mobileServiceUpdate);
				
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(appVersion);
				
		return data;
	}
	@ResponseBody
	@RequestMapping(value="mobileService/lastMarketUrl",method=RequestMethod.POST)
	public String mobileServiceLastMaketUrlPost( MobileServiceUpdateDto mobileServiceUpdate) throws JsonGenerationException, JsonMappingException, IOException{
		
		
		logger.debug(mobileServiceUpdate.toString());
		
		String marketUrl = mobileServiceManage.getLastMarketUrl(mobileServiceUpdate);
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		
		map.put("marketUrl",marketUrl);
		
		System.out.println(marketUrl);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		logger.debug("data" + data);
		
		return data;
		
		
	}
}
