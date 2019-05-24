package kr.msp.admin.securepush;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;
import kr.msp.admin.securepush.manageGrp.dto.GroupDto;
import kr.msp.admin.securepush.manageGrp.dto.ManageGrpParamDto;
import kr.msp.admin.securepush.manageGrp.service.ManageGrpService;
import kr.msp.admin.securepush.organization.dto.DeptDto;
import kr.msp.admin.securepush.organization.dto.EmpDto;

/**
 * 시큐어 푸쉬 그룹관리 메뉴 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class ManageGrpController {

	private final static Logger logger = LoggerFactory.getLogger(ManageGrpController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private ManageGrpService manageGrpService;
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	/**
	 * 그룹 관리 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="manageGrp" , method=RequestMethod.GET)
	public String manageGrpGet( Model model, ManageGrpParamDto paramDto, HttpServletRequest request ) throws Exception {
		manageAccountService.setSessionSecurePushUserId(request);
		
		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(row_size);

		// 현재 로그인 한 사용자의 사용자 아이디로 목록 조회 조건을 제한한다.
		paramDto.setREG_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		
		List<GroupDto> itemList = manageGrpService.selectGroupList(paramDto);
		
		model.addAttribute("itemList", itemList);
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/securepush/manageGrp/manageGrpMain";
	}
	
	/**
	 * 그룹 관리 리스트 페이징
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageGrp" , method = RequestMethod.POST)
	public String manageGrpPost(Model model, ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception{
		
		// 현재 로그인 한 사용자의 사용자 아이디로 목록 조회 조건을 제한한다.
		paramDto.setREG_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<GroupDto> itemList = manageGrpService.selectGroupList(paramDto);
		
		model.addAttribute( "itemList", itemList );
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",paramDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageGrp/manageGrpList";
	}
	
	/**
	 * 그룹 관리 상세 페이지 진입.(그룹 구성원 목록 팝업)
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "manageGrp/detailView" , method = RequestMethod.POST)
	public String detailViewPost(Model model, ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setREG_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		GroupDto itemInfo = manageGrpService.getGroupInfo(paramDto);
		
		model.addAttribute( "itemInfo", itemInfo );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageGrp/detailViewPopUp";
	}
	
	
	/**
	 * 그룹 생성 및 수정 시 조직도 검색 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageGrp/selectOrg" , method = RequestMethod.POST)
	public String selectOrgPost(Model model, ManageGrpParamDto paramDto, HttpServletRequest request){
		
		try{
			paramDto.setREG_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
			paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
			List<DeptDto> deptList = manageGrpService.getDeptList(paramDto);
			model.addAttribute("deptList", deptList);
			// 그룹 수정일 경우 해당 그룹의 구성원 목록을 내려줌.
			if(paramDto.getGRP_ID() != null){
				GroupDto itemInfo = manageGrpService.getGroupInfo(paramDto);
				model.addAttribute( "groupInfo", itemInfo );
			}
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageGrp/selectOrgPopUp";
	}
	
	/**
	 * 조직도 팝업 - 부서 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageGrp/findDept" , method = RequestMethod.POST)
	public String findDeptPost(Model model, ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setREG_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<DeptDto> deptList = manageGrpService.getDeptList(paramDto);
		
		model.addAttribute( "deptList", deptList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageGrp/selectOrgPopUpDeptList";
	}
	
	/**
	 * 조직도 팝업 - 사용자 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageGrp/findUser" , method = RequestMethod.POST)
	public String findUserPost(Model model, ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setREG_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<EmpDto> empList = manageGrpService.getEmpList(paramDto);
		
		model.addAttribute( "empList", empList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageGrp/selectOrgPopUpUserList";
	}
	
	/**
	 * 그룹 생성 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageGrp/addGrpPop" , method = RequestMethod.POST)
	public String addGrpPopPost(Model model, ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception {
		
		List<Map> groupColorList = manageGrpService.selectGrpColorList();
		
		model.addAttribute( "groupColorList", groupColorList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageGrp/addGrpPopUp";
	}
	
	/**
	 * 그룹 생성 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageGrp/addGrp" , method = RequestMethod.POST)
	public String addGrpPost(ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String senderId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(senderId == null || "".equals(senderId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(senderId);
        	
        	if(manageGrpService.addGroup(paramDto))
        		result = 1;
		} catch (Exception e) {
			logger.error("Exception caught.", e);
			resultMsg = e.getMessage();
		}
        
        HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg","success.");
		} else {
			map.put("result", result);
			map.put("msg", resultMsg);
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	

	/**
	 * 그룹명 중복 체크.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageGrp/grpNameCheck" , method = RequestMethod.POST)
	public String grpNameCheckPost(ManageGrpParamDto paramDto, HttpServletRequest request ) throws Exception{
		
		int result = 0;
		String resultMsg = "fail.";
		try {
        	manageGrpService.checkDuplicateGroupName(paramDto);
        	result = 1;
		} catch (Exception e) {
			logger.error("Exception caught.", e);
			resultMsg = e.getMessage();
		}
        
        HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg","success.");
		} else {
			map.put("result", result);
			map.put("msg", resultMsg);
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	/**
	 * 그룹 수정 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageGrp/updateGrp" , method = RequestMethod.POST)
	public String updateGrpPost(ManageGrpParamDto paramDto, HttpServletRequest request ) throws Exception{
		
		int result = 0;
		String resultMsg = "fail.";
		try {
			String senderId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(senderId == null || "".equals(senderId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(senderId);
        	
        	manageGrpService.updateGroup(paramDto);
        	
        	result = 1;
		} catch (Exception e) {
			logger.error("Exception caught.", e);
			resultMsg = e.getMessage();
		}
        
        HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg","success.");
		} else {
			map.put("result", result);
			map.put("msg", resultMsg);
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	/**
	 * 그룹 삭제 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageGrp/deleteGrp" , method = RequestMethod.POST)
	public String deleteGrpPost(ManageGrpParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String senderId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(senderId == null || "".equals(senderId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(senderId);
        	
        	if(manageGrpService.deleteGroup(paramDto))
        		result = 1;
		} catch (Exception e) {
			logger.error("Exception caught.", e);
			resultMsg = e.getMessage();
		}
        
        HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg","success.");
		} else {
			map.put("result", result);
			map.put("msg", resultMsg);
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
