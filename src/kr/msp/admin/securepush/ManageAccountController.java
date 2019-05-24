package kr.msp.admin.securepush;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

import kr.msp.admin.securepush.manageAccount.dto.ManageAccountDto;
import kr.msp.admin.securepush.manageAccount.dto.ManageAccountParamDto;
import kr.msp.admin.securepush.manageAccount.dto.ManageAccountSysUserDto;
import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;
import kr.msp.admin.securepush.organization.dto.DeptDto;
import kr.msp.admin.securepush.organization.dto.EmpDto;
import kr.msp.admin.system.user.dto.AuthUserGroup;

/**
 * 시큐어 푸쉬 관리자 계정 관리 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class ManageAccountController {

	private final static Logger logger = LoggerFactory.getLogger(ManageAccountController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	/**
	 * 사용자 관리 메뉴 진입 처리.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping(value="manageAccount" , method=RequestMethod.GET)
	public String manageAccountGet( Model model, ManageAccountParamDto paramDto){
		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(row_size);
		
		List<ManageAccountSysUserDto> itemList = manageAccountService.getManageAccountList(paramDto);
		List<AuthUserGroup> searchGroupList = manageAccountService.getAuthGroupList();

		model.addAttribute("itemList", itemList);
		model.addAttribute("searchGroupList", searchGroupList);
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/securepush/manageAccount/manageAccountMain";
	}
	
	/**
	 * 사용자 관리 리스트 페이징
	 * @param model
	 * @param usersParam
	 * @param userSearch
	 * @return
	 */
	@RequestMapping( value = "manageAccount" , method = RequestMethod.POST)
	public String manageAccountPost(Model model, ManageAccountParamDto paramDto){
		
		List<ManageAccountSysUserDto> itemList = manageAccountService.getManageAccountList(paramDto);
		
		model.addAttribute( "itemList", itemList );
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",paramDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageAccount/manageAccountList";
	}
	
	
	/**
	 * 사용자 관리 등록 팝업 화면(조직도)
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageAccount/registOrg" , method = RequestMethod.GET)
	public String registOrgGet(Model model, ManageAccountParamDto paramDto){
		try{
			List<DeptDto> deptList = manageAccountService.getDeptList(paramDto);
			model.addAttribute("deptList", deptList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageAccount/selectOrgPopUp";
	}
	
	/**
	 * 사용자 관리 등록 팝업 화면(조직도) - 부서 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageAccount/findDept" , method = RequestMethod.POST)
	public String findDeptPost(Model model, ManageAccountParamDto paramDto) throws Exception {
		
		List<DeptDto> deptList = manageAccountService.getDeptList(paramDto);
		
		model.addAttribute( "deptList", deptList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageAccount/selectOrgPopUpDeptList";
	}
	
	/**
	 * 사용자 관리 등록 팝업 화면(조직도) - 사용자 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageAccount/findUser" , method = RequestMethod.POST)
	public String findUserPost(Model model, ManageAccountParamDto paramDto) throws Exception {
		
		List<EmpDto> empList = manageAccountService.getEmpList(paramDto);
		
		model.addAttribute( "empList", empList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageAccount/selectOrgPopUpUserList";
	}
	
	/**
	 * 사용자 관리 등록 팝업 화면(사용자 정보 입력)
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping( value = "manageAccount/registUser" , method = RequestMethod.POST)
	public String manageAccountRegistUserGet(Model model, ManageAccountParamDto paramDto) throws Exception{
		
		EmpDto empInfo = manageAccountService.getEmpInfo(paramDto);
		model.addAttribute("itemInfo", empInfo);
		List<AuthUserGroup> authGroupList = manageAccountService.getAuthGroupList();
		model.addAttribute("authGroupList", authGroupList);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageAccount/writePopUp";
	}
	
	/**
	 * 관리자 등록 처리.
	 * @param paramDto
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageAccount/regist" , method = RequestMethod.POST)
	public String registPost( ManageAccountParamDto paramDto, HttpServletRequest request) throws Exception{
		
		int result = 0;
		
		try {
			paramDto.setREG_ID((String) request.getSession().getAttribute("S_ID_USER"));
			if(manageAccountService.registAdminUser(paramDto))
				result = 1;
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
	

	/**
	 * 관리자 계정 수정 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageAccount/editUser" , method = RequestMethod.POST)
	public String editUserPost(Model model, ManageAccountParamDto paramDto) throws Exception {
		
		ManageAccountDto manageAccountDto = manageAccountService.getManageAccountInfo(paramDto);
		model.addAttribute("itemInfo", manageAccountDto);
		List<AuthUserGroup> authGroupList = manageAccountService.getAuthGroupList();
		model.addAttribute("authGroupList", authGroupList);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageAccount/editPopUp";
	}
	
	/**
	 * 관리자 계정 수정.
	 * @param paramDto
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageAccount/edit" , method = RequestMethod.POST)
	public String manageAccountEditPost( ManageAccountParamDto paramDto, HttpServletRequest request) throws Exception {
			
		int result = 0;
		try {
			paramDto.setREG_ID((String)request.getSession().getAttribute("S_ID_USER"));
			if(manageAccountService.updateAdminUser(paramDto))
				result = 1;
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
	
	/**
	 * 관리자 계정 삭제.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageAccount/delete" , method = RequestMethod.POST)
	public String manageAccountDeletePost(ManageAccountParamDto paramDto) throws Exception{
		int result = 0;
        HashMap<String, Object> map = new HashMap<String,Object>();
        try {
        	if(manageAccountService.deleteAdminUser(paramDto))
        		result = 1;
            
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
	
	/**
	 * 아이디 중복 체크.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageAccount/idcheck" , method = RequestMethod.POST)
	public String idCheckPost(ManageAccountParamDto paramDto ) throws Exception{
		
		int result = 1;
		
		if(manageAccountService.checkIdDuplicate(paramDto))
			result = 0;
		
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
