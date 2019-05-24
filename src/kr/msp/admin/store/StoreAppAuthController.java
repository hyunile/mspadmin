package kr.msp.admin.store;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.store.appInfo.dto.AppInfoSearchDto;
import kr.msp.admin.store.userAppAuth.dto.UserAddAuthDto;
import kr.msp.admin.store.userAppAuth.dto.UserAppValuesDto;
import kr.msp.admin.store.userAppAuth.service.UserAppAuthService;

@Controller
@RequestMapping(value="admin/store")
public class StoreAppAuthController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	UserAppAuthService userAppAuthService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	/**
	 * App 사용권한 관리 Main
	 * @param model
	 * @param request
	 * @return
	 * @since 2013. 12. 30. by 이명보
	 */
	@RequestMapping( value="userAppAuth" ,method=RequestMethod.GET)
	public String UserAppAuthGet(Model model, HttpServletRequest request ){
		
		AppInfoSearchDto appInfoSearch = new AppInfoSearchDto();
		
		appInfoSearch.setPAGE_NUM(1);
		appInfoSearch.setPAGE_SIZE(row_size);
		appInfoSearch.setORDER_TARGET("APP_IDX");
		appInfoSearch.setORDER_TYPE("DESC");
		
		//model.addAttribute("platformList", userAppAuthService.selectUserAppAuthList( appInfoSearch ));
		model.addAttribute("categoryList", userAppAuthService.selectCataAllList());
		model.addAttribute("R_PAGE_NUM",appInfoSearch.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/store/userAppAuth/userAppAuthMain";
	}
	
	/**
	 * App 사용권한 관리 List
	 * @param model
	 * @param request
	 * @param appInfoSearch
	 * @return
	 * @since 2014. 5. 1. by UncleJoe
	 */
	@RequestMapping( value="userAppAuth" ,method=RequestMethod.POST)
	public String UserAppAuthPost(AppInfoSearchDto appInfoSearch, HttpServletRequest request, Model model){
		model.addAttribute("platformList", userAppAuthService.selectUserAppAuthList( appInfoSearch ));
		model.addAttribute("R_PAGE_NUM",appInfoSearch.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAppAuth/userAppAuthList";
	}
	
	/**
	 * App사용권한관리 ==> 권한등록 ==> App 목록
	 * @param model
	 * @param request
	 * @param appInfoSearch
	 * @return
	 * @since 2014. 6. 5. by UncleJoe
	 */
	@RequestMapping( value="userAppAuth/Auth" , method=RequestMethod.POST )
	public String UserAppAuthDeployPost(AppInfoSearchDto appInfoSearch, HttpServletRequest request, Model model){
		
		model.addAttribute("AppList", userAppAuthService.selectAppAuthList( appInfoSearch ));
		
		return "admin/store/userAppAuth/addAuth";
	}
	
	
	@RequestMapping( value="userAppAuth/userList" , method=RequestMethod.GET )
	public String getUserAppAuthUserList(UserAddAuthDto userAuth, HttpServletRequest request, Model model) throws JsonGenerationException, JsonMappingException, IOException{
		
		logger.debug("##################################");
		logger.debug("######## getUserAppAuthUserList ###############");
		logger.debug("##################################");
		//APP_ID를 이용하여 현재 등록된 사용자 목록을 보여준다.
		userAuth.setPAGE_SIZE(9999);
		userAuth.setPAGE_NUM(1);
			
		List<UserAddAuthDto> userList = userAppAuthService.selectAppAuthUser(userAuth);
			
		model.addAttribute("userList", userList);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAppAuth/userlist";
		
		
	}
	
	
	/**
	 * App 사용권한 관리 사용자 추가팝업(기본사용자)
	 * @param model
	 * @param request
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 1. by UncleJoe
	 */
	@RequestMapping( value="userAppAuth/addUser" , method=RequestMethod.GET )
	public String UserAppAuthAddUserGet( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		/*
		List< HashMap<String, Object> > userList = userAppAuthService.selectAuthUserList();
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(userList);
		
		logger.debug(data);
		
		model.addAttribute("userList",data);
		
		*/
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAppAuth/addUserShow";
	}
	

	/**
	 * App관리 ==> App사용권한 관리 ==> 사용자 목록 ==> 사용자 추가 ==> 사용자 그룹별  
	 * ==> 위의 링크는 잘못되었음.
	 * @param model
	 * @param request
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 1. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping( value="userAppAuth/addUserAuth")
	public String UserAppAuthAddUserAuthGet( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		
		logger.debug("addUserAuth.........................");
		/*
		String parentCd = ServletRequestUtils.getStringParameter(request, "parentCd", "0");
		
		List<Map<String, Object> > userList = userAppAuthService.getDivisionListByParentCd(parentCd);//selectAuthUserList();
		if(parentCd.equals("0")){
			parentCd = "0000";
			List<Map<String, Object> > userListTmp = userAppAuthService.getDivisionListByParentCd(parentCd);//selectAuthUserList();
			userList.addAll(userListTmp);
		}
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(userList);
		
		logger.debug(data);
		
		model.addAttribute("userList",data);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAppAuth/addUserAuthTree";
		*/
		
		List< HashMap<String, Object> > userList = userAppAuthService.selectAuthUserList();
		
//		ObjectMapper mapper = new ObjectMapper();
		String data = new ObjectMapper().writeValueAsString(userList);
		
		logger.debug(data);
		
		//model.addAttribute("userList",data);
		
		return data;
	}
	
	/**
	 * 조직도 List
	 * @param model
	 * @param request
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 1. by UncleJoe
	 */
	@RequestMapping( value="userAppAuth/addUserDivision" , method=RequestMethod.GET )
	@Deprecated
	public String UserAppAuthAddUserDivisionGet( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		

		logger.debug("addUserAuth.........................");
		
/*
		String parentCd = ServletRequestUtils.getStringParameter(request, "parentCd", "0");
		
		List<Map<String, Object> > userList = userAppAuthService.getDivisionListByParentCd(parentCd);//selectAuthUserList();
		if(parentCd.equals("0")){
			parentCd = "0000";
			List<Map<String, Object> > userListTmp = userAppAuthService.getDivisionListByParentCd(parentCd);//selectAuthUserList();
			userList.addAll(userListTmp);
		}
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(userList);
		
		logger.debug(data);
		model.addAttribute("userList",data);
		
		*/
		model.addAttribute("layout","layout/null.vm");
		
//		return "admin/store/userAppAuth/addUserAuthDivision";
		return "admin/store/userAppAuth/addUserDivision";
	}
	
	/**
	 * App관리 => App 사용권한 관리 ==> 사용자 추가 ==> 조직도 Ajax 
	 * @param model
	 * @param request
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 7. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping( value="userAppAuth/divisionTree" )
	public String UserAppAuthAddUserDivisionAjax( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		logger.debug("############################################");
		logger.debug("       UserAppAuthAddUserDivisionAjax       ");
		logger.debug("############################################");
		
		String parentCd = ServletRequestUtils.getStringParameter(request, "parentCd", "0");
		String subCnt = ServletRequestUtils.getStringParameter(request, "subCnt", "0");
		int memberCnt = ServletRequestUtils.getIntParameter(request, "memberCnt", 0);
		boolean parentSelected = ServletRequestUtils.getBooleanParameter(request, "selected", false);
		
		
		logger.debug("parentCd [{}]", parentCd);
		logger.debug("subCnt [{}]", subCnt);
		logger.debug("memberCnt [{}]", memberCnt);
		logger.debug("parentSelected [{}]", parentSelected);
		
		
		List<Map<String, Object> > result = null;
		
		List<Map<String, Object> > userList = userAppAuthService.getDivisionListByParentCd(parentCd);//selectAuthUserList();
		
		if(parentCd.equals("0") && userList.size() == 1){
			parentCd = "0000";
			Map<String, Object> map = userList.get(0);
			map.put("expand", true);
			map.put("isLazy", false);
			
			List<Map<String, Object> > userListTmp = userAppAuthService.getDivisionListByParentCd(parentCd);//selectAuthUserList();
			if(memberCnt > 0){
				List<Map<String, Object> > empList = userAppAuthService.getEmpListByDeptCd(parentCd);//selectAuthUserList();
				
				userListTmp.addAll(empList);
			}
			map.put("children", userListTmp);
			
			result = new ArrayList<Map<String,Object>>();
			result.add(0, map);
		}else{
			
			if(memberCnt > 0){
				List<Map<String, Object> > empList = userAppAuthService.getEmpListByDeptCd(parentCd);//selectAuthUserList();
				for (Map<String, Object> map : empList) {
					map.put("select", parentSelected);
				}
				userList.addAll(empList);
			}
			
			result = userList;
		}
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(result);
		
		logger.debug(data);
		
		return data;

	}
	
	@ResponseBody
	@RequestMapping( value="userAppAuth/userGroupTree" )
	public String userGroupTreeAjax( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		
		logger.debug("############################################");
		logger.debug("                     userGroupTreeAjax       ");
		logger.debug("############################################");
		
		String parentCd = ServletRequestUtils.getStringParameter(request, "parentCd", "0");
		String subCnt = ServletRequestUtils.getStringParameter(request, "subCnt", "0");
		int memberCnt = ServletRequestUtils.getIntParameter(request, "memberCnt", 0);
		boolean parentSelected = ServletRequestUtils.getBooleanParameter(request, "selected", false);
		boolean hideCheckbox = ServletRequestUtils.getBooleanParameter(request, "hideCheckbox", false);
		
		
		logger.debug("parentCd [{}]", parentCd);
		logger.debug("subCnt [{}]", subCnt);
		logger.debug("memberCnt [{}]", memberCnt);
		logger.debug("parentSelected [{}]", parentSelected);
		
		List<Map<String, Object> > result = null;
		
		List<Map<String, Object> > userList = userAppAuthService.selectUserDefinedGroupTree(parentCd);//selectAuthUserList();
		
			
		if(memberCnt > 0){
			List<Map<String, Object> > empList = userAppAuthService.selectUserListByUserDefinedGroup(parentCd);//selectAuthUserList();
			for (Map<String, Object> map : empList) {
				map.put("select", parentSelected);
				map.put("hideCheckbox", hideCheckbox);
			}
			userList = empList;
			//userList.addAll(empList);
		}
			
		
		
		result = userList;
	
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(result);
		
		logger.debug("userGroupTreeAjax:::" + data);
		
		return data;
		
	}
	
	@RequestMapping( value="userAppAuth/addUserDivision_org" , method=RequestMethod.GET )
	public String UserAppAuthAddUserDivisionGet_org( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		List< HashMap<String, Object> > userList = userAppAuthService.selectAuthUserDivisionList();
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(userList);
		
		logger.debug(data);
		
		model.addAttribute("userList",data);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAppAuth/addUserAuthTree";
	}
	
	
	
	@ResponseBody
	@RequestMapping( value="userAppAuth/regist", method=RequestMethod.POST)
	public String NoticeAuthPost(Model model, UserAppValuesDto userAppValues 
			, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		userAppValues.setREG_ID( (String) request.getSession().getAttribute("S_ID_USER") );
		
		int result = 0;
			
		try {
			result = userAppAuthService.insertUserAppAuth( userAppValues );
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
	
	@RequestMapping( value="userAppAuth/edit" ,method=RequestMethod.GET)
	public String UserAppAuthEditGet(Model model , UserAddAuthDto userAuth){
		
		userAuth.setPAGE_SIZE(row_size);
		
		List<UserAddAuthDto> userList = userAppAuthService.selectAppAuthUser(userAuth);
		
		model.addAttribute("userList", userList);
		model.addAttribute("R_PAGE_NUM",userAuth.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAppAuth/appAuthInfo";
	}
	
	@ResponseBody
	@RequestMapping( value="userAppAuth/delete", method=RequestMethod.POST)
	public String UserAppAuthDeletePost(Model model 
			, @RequestParam("DEL_AUTH_ID") ArrayList<String> authIdList ) throws JsonGenerationException, JsonMappingException, IOException{	
		int result = 0;
			
		try {
			result = userAppAuthService.userAppAuthDelete( authIdList );
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
