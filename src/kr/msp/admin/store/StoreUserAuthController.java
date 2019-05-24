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
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.store.UserAuth.dto.AuthUserDto;
import kr.msp.admin.store.UserAuth.dto.UserAuthRegistDto;
import kr.msp.admin.store.UserAuth.dto.UserGroupDto;
import kr.msp.admin.store.UserAuth.service.StoreUserAuthService;
import kr.msp.admin.store.userAppAuth.service.UserAppAuthService;

@Controller
@RequestMapping(value="admin/store")
public class StoreUserAuthController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	UserAppAuthService userAppAuthService;
		
	@Autowired(required=true)
	StoreUserAuthService storeUserAuth;
	
	@Autowired(required = true) 
    private MessageSource messageSource;
	
	/**
	 * 사용자 그룹관리 > 권한그룹 
	 * @param model
	 * @param request
	 * @return
	 * @since 2014. 5. 8. by 이명보
	 */
	@RequestMapping( value="userAuth" ,method=RequestMethod.GET)
	public String UserAuthGet(Model model, HttpServletRequest request ){
		
		List<UserGroupDto> groupList = storeUserAuth.selectGroup();
		
		model.addAttribute( "groupList", groupList );
		
		return "admin/store/userAuth/userAuthMain";
	}
	
	/**
	 * 사용자 그룹관리 > 권한그룹 > 권한그룹별 사용자 목록
	 * @param model
	 * @param userGroup
	 * @return
	 * @since 2014. 5. 8. by 이명보
	 */
	@RequestMapping( value="userAuth/userList" ,method=RequestMethod.GET)
	public String UserAuthUserListGet(Model model, UserGroupDto userGroup ){
		
		List<AuthUserDto> userList = storeUserAuth.selectUser(userGroup);
		
		model.addAttribute( "userList", userList );
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAuth/userAuthList";
	}
	
	
	@RequestMapping( value="userAuth/addUser_org" ,method=RequestMethod.GET)
	public String UserAuthAddUserGet_org(Model model, UserGroupDto userGroup ) throws JsonGenerationException, JsonMappingException, IOException{
		
		List< HashMap<String, Object> > userList = userAppAuthService.selectAuthUserDivisionList();
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(userList);
		
		logger.debug(data);
		
		model.addAttribute("userList",data);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAuth/addUserTree";
	}
	
	/**
	 * 사용자 그룹관리 => 사용자 ==> 추가 ==>조직도 Ajax 
	 * @param model
	 * @param request
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 7. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping( value="userAuth/divisionTree" )
	public String UserAppAuthAddUserDivisionAjax( Model model, HttpServletRequest request ) throws JsonGenerationException, JsonMappingException, IOException{
		
		
		logger.debug("addUserAuthAjax.........................");
		
		
		String parentCd = ServletRequestUtils.getStringParameter(request, "parentCd", "0");
		String subCnt = ServletRequestUtils.getStringParameter(request, "subCnt", "0");
		int memberCnt = ServletRequestUtils.getIntParameter(request, "memberCnt", 0);
		
		logger.debug("subCnt :::........................." + subCnt);
		logger.debug("memberCnt :::........................." + memberCnt);

		
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
				
				userList.addAll(empList);
			}
			
			result = userList;
		}
		
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(result);
		
		logger.debug(">>> StoreUserAuth:::::" + data);
		
		return data;
	}
	
	/**
	 * 사용자 그룹관리 ==> 사용자 추가 ==> 사용자 목록
	 * @param model
	 * @param userGroup
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 7. by UncleJoe
	 */
	@RequestMapping( value="userAuth/addUser" ,method=RequestMethod.GET)
	public String UserAuthAddUserGet(Model model, UserGroupDto userGroup ) throws JsonGenerationException, JsonMappingException, IOException{
		
//		List< HashMap<String, Object> > userList = userAppAuthService.selectAuthUserDivisionList();
		/*
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(null);
		
		logger.debug(data);
		
		model.addAttribute("userList",data);
		*/
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/userAuth/addUserTree";
	}
	
	/**
	 * 사용자 그룹관리 ==> 사용자 ==> 사용자 추가/삭제후 저장
	 * @param model
	 * @param request
	 * @param userAuthRegist
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 5. 8. by 이명보
	 */
	@ResponseBody
	@RequestMapping( value="userAuth/regist", method=RequestMethod.POST)
	public String UserAuthRegistPost( UserAuthRegistDto userAuthRegist, HttpServletRequest request, Model model) throws JsonGenerationException, JsonMappingException, IOException{
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		userAuthRegist.setREG_ID(S_ID_USER);
		
		int result = 0;
			
		try {
			result = storeUserAuth.userAuthRegist( userAuthRegist );
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.store.device.alert.save", null, LocaleContextHolder.getLocale()) ); //"저장 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.store.device.alert.saveFail", null, LocaleContextHolder.getLocale()) ); // "저장에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}

}