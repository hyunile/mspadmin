package kr.msp.admin.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mybatis.spring.SqlSessionTemplate;
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

import kr.msp.admin.system.user.dto.AuthUserGroup;
import kr.msp.admin.system.user.dto.UserSearchDto;
import kr.msp.admin.system.user.dto.UsersDto;
import kr.msp.admin.system.user.dto.UsersParamDto;
import kr.msp.admin.system.user.service.SystemUserService;
import kr.msp.util.Sha256Util;

@Controller
@RequestMapping( value = "admin/system")
public class SystemUserController {

	private final static Logger logger = LoggerFactory.getLogger(SystemUserController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	protected SystemUserService systemUserService;

	@Autowired(required=true)
	private SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	@RequestMapping( value = "user" , method = RequestMethod.GET)
	public String UserGet(Model model, UsersParamDto usersParam){
		usersParam.setPAGE_NUM(1);
		usersParam.setPAGE_SIZE(row_size);
		
		List<UsersDto> usersList = systemUserService.SelectCodeGroup(usersParam);
		List<AuthUserGroup> searchGroupList = systemUserService.SelectSearchAuthGroup();
		
		model.addAttribute( "usersList", usersList );
		model.addAttribute( "searchGroupList", searchGroupList );
		model.addAttribute("R_PAGE_NUM",usersParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/system/user/userMain";
	}
	
	//사용자 리스트 페이징
	@RequestMapping( value = "user" , method = RequestMethod.POST)
	public String UserPost(Model model, UsersParamDto usersParam, UserSearchDto userSearch ){
		if(!userSearch.getSE_ID_USER().equals("")){
			usersParam.setID_USER(userSearch.getSE_ID_USER());
		}
		if(!userSearch.getSE_NM_USER().equals("")){
			usersParam.setNM_USER(userSearch.getSE_NM_USER());
		}
		if(!userSearch.getSE_ID_GROUP().equals("")){
			usersParam.setID_GROUP(userSearch.getSE_ID_GROUP());
		}
		
		List<UsersDto> usersList = systemUserService.SelectCodeGroup(usersParam);
		
		model.addAttribute( "usersList", usersList );
		model.addAttribute("R_PAGE_NUM",usersParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",usersParam.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/user/userList";
	}
	
	//사용자 수정 화면
	@RequestMapping( value = "user/edit" , method = RequestMethod.GET)
	public String UserEditGet(Model model, UsersParamDto usersParam){
		
		UsersDto userOne = systemUserService.SelectOneUser(usersParam);
		List<AuthUserGroup> authUserGroupList = systemUserService.SelectUserAuthGroup(usersParam);
		
		model.addAttribute( "userOne", userOne );
		model.addAttribute( "authUserGroupList", authUserGroupList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/user/userModify";
	}
	
	//사용자 등록 화면
	@RequestMapping( value = "user/regist" , method = RequestMethod.GET)
	public String UserRegistGet(Model model, UsersParamDto usersParam){
		
		List<AuthUserGroup> authUserGroupList = systemUserService.SelectUserAuthGroup(usersParam);
		
		model.addAttribute( "authUserGroupList", authUserGroupList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/system/user/userModify";
	}
	
	//사용자 등록
	@ResponseBody
	@RequestMapping( value = "user/regist" , method = RequestMethod.POST)
	public String UserRegistPost( UsersDto users, HttpServletRequest request) throws Exception{
		
		String password = users.getPASSWORD();
//		users.setPASSWORD( CryptoUtils.encrypt(password) );
        users.setPASSWORD( Sha256Util.getEncrypt(password) );
		users.setID_INSERT((String) request.getSession().getAttribute("S_ID_USER"));
		int result = 0;
		
		try {
			result = systemUserService.InsertUsers(users);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	@RequestMapping( value = "user/edit" , method = RequestMethod.POST)
	public String UserEditPost( UsersDto users, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException {
		HashMap<String, Object> map = new HashMap<String,Object>();
		int result = 0;

		String password = users.getPASSWORD();
		String newPassword = request.getParameter("NEW_PASSWORD");
		String loginID = (String) request.getSession().getAttribute("S_ID_USER");

		if(password==null){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale())+" [PASSWORD is NULL]" ); //"수정에 실패했습니다."
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}

		if(newPassword==null){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale())+" [NEW_PASSWORD is NULL]" ); //"수정에 실패했습니다."
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}

		users.setID_UPDATE(loginID);
		// TODO : 로그인 한 아이디로 이전 패스워드 검증로직 추가
		Map<String,Object> dbPasswdMap = sqlSessionTemplate.selectOne("kr.msp.admin.system.user.mapper.SystemUserDao.SelPassCheck",users);
		String enc_password = Sha256Util.getEncrypt(password);
		if(dbPasswdMap==null || !dbPasswdMap.get("USER_PW").equals(enc_password)){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale())+" [Previous passwords do not match.]" ); //"수정에 실패했습니다."
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}
		
		if (!newPassword.equals("")) {
//			users.setPASSWORD( CryptoUtils.encrypt(password) );
            users.setPASSWORD( Sha256Util.getEncrypt(newPassword) );
		} else {
			users.setPASSWORD(null);
		}

		try {
			result = systemUserService.UpdateUsers(users);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
		}
		

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
	@RequestMapping( value = "user/delete" , method = RequestMethod.POST)
	public String UserDeletePost(UsersDto users ) throws Exception{
		int result = 0;
        HashMap<String, Object> map = new HashMap<String,Object>();
        try {
            result = systemUserService.DeleteUsers(users);
            if(result > 0){
                map.put("result", result);
                map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
            } else {
                map.put("result", result);
                map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
            }
        }catch(Exception e){
            map.put("result", result);
            map.put("msg", messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale())
            		+" : ["+messageSource.getMessage("menu.push.sendManage.alert.askAdmin", null, LocaleContextHolder.getLocale())+"]"); //삭제에 실패했습니다    관리자에게 연락바랍니다.
        }

			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//사용자 삭제
	@ResponseBody
	@RequestMapping( value = "user/check" , method = RequestMethod.POST)
	public String UserCheckPost(UsersDto users ) throws Exception{
		
		int result = systemUserService.SelectUserCount(users);
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.system.controller.alreadyID", null, LocaleContextHolder.getLocale())); //아이디가 이미 존재합니다.
		} else {
			map.put("result", result);
			map.put("msg", messageSource.getMessage("menu.system.controller.useID", null, LocaleContextHolder.getLocale())); //사용가능한 아이디 입니다.
		}	
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
