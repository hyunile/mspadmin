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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;
import kr.msp.admin.securepush.manageGrp.dto.GroupDto;
import kr.msp.admin.securepush.manageGrp.dto.GroupUserDto;
import kr.msp.admin.securepush.msgSend.dto.MsgSendParamDto;
import kr.msp.admin.securepush.msgSend.dto.UserDto;
import kr.msp.admin.securepush.msgSend.service.MsgSendService;
import kr.msp.admin.securepush.organization.dto.DeptDto;
import kr.msp.admin.securepush.organization.dto.EmpDto;

/**
 * 시큐어 푸쉬 메시지 보내기 메뉴 처리 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class MsgSendController {

	private final static Logger logger = LoggerFactory.getLogger(MsgSendController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private MsgSendService msgSendService;
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	/**
	 * 그룹 메시지 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="grpMsgSend" , method=RequestMethod.GET)
	public String grpMsgSendGet( Model model, HttpServletRequest request ) throws Exception {
		
		manageAccountService.setSessionSecurePushUserId(request);

		String sIdUser = (String)request.getSession().getAttribute("S_ID_USER");
		// 보내는 사람 정보 조회.
		UserDto senderInfo = msgSendService.getSenderInfo(sIdUser);
		
		// 수퍼 관리자는 시큐어 푸쉬 사용 권한에 포함되지만, 시큐어 푸쉬 사용자에 포함되지 않을 수 있다.
		// 어드민 기본 admin 수퍼 관리자 권한 사용자는 시큐어 푸쉬 사용자에 없음.
		// 이런 경우 해당 어드민 아이디를 세팅하여 발신자로 보내게끔 한다. 
		// 이 경우, 어드민 시큐어 푸쉬 발송 목록에서만 해당 발송 메시지 정보를 확인할 수 있음.
		// 사용 시나리오 상 수퍼 관리자라도 시큐어 푸쉬 사용자관리에서 계정을 맵핑시켜서 
		// 메시지를 전송토록 가이드할 필요가 있음.
		if(senderInfo != null && senderInfo.getUSER_ID() == null){
			senderInfo.setUSER_ID(sIdUser);
		}
		model.addAttribute("senderInfo",senderInfo);
		
		return "admin/securepush/msgSend/grpMsgSendMain";
	}
	
	
	/**
	 * 개별 메시지 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="individualMsgSend" , method=RequestMethod.GET)
	public String individualMsgSendGet( Model model, HttpServletRequest request ) throws Exception {
		manageAccountService.setSessionSecurePushUserId(request);

		// 보내는 사람 정보 조회.
		UserDto senderInfo = msgSendService.getSenderInfo((String)request.getSession().getAttribute("S_ID_USER"));
		
		model.addAttribute("senderInfo",senderInfo);
		
		return "admin/securepush/msgSend/individualMsgSendMain";
	}
	
	/**
	 * 메시지 전송 화면에서 선택한 첨부파일을 임시 저장함.
	 * @param attachFile
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="msgSend/attachUpload" , method=RequestMethod.POST, produces = "text/html; charset=utf8")
	public String attachUploadPost( @RequestParam("ATTACH_FILE") MultipartFile attachFile, HttpServletRequest request ) throws Exception {
		// 클라이언트 ajaxfileupload.js에서 응답 content-type을 application-json으로 하였을 때 
		// 오류가 발생하는 현상이 있음. 현재는 text/html로 응답하고 utf-8 명시하여 한글 파일명이 깨지지 않도록 처리.
		int result = 0;
		String fileKey = "";
		String fileName = "";
		String fileSize = "";
		try {
			if(attachFile!=null && attachFile.getSize()>0){
				fileKey = msgSendService.attachUploadAndGenerateKey(attachFile);
				fileName = attachFile.getOriginalFilename();
				fileSize = "" + attachFile.getSize();
				result = 1;
			}
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg","success.");
			map.put("fileKey", fileKey);
			map.put("fileName", fileName);
			map.put("fileSize", fileSize);
		} else {
			map.put("result", result);
			map.put("msg","fail to upload." );
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	/**
	 * 그룹 메시지 전송 요청.
	 * @param paramDto
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="msgSend/groupSend" , method=RequestMethod.POST)
	public String groupSendPost( MsgSendParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String senderId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(senderId == null || "".equals(senderId))
        		throw new Exception(messageSource.getMessage("menu.securePush.msgSend.invalidSender", null, LocaleContextHolder.getLocale())); //발신자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
			msgSendService.sendGroupMsg(paramDto);
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
	 * 그룹 선택 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "msgSend/selectGroup" , method = RequestMethod.POST)
	public String selectGroupPost(Model model, MsgSendParamDto paramDto) throws Exception {
		
		List<GroupDto> groupList = msgSendService.selectGroupList(paramDto);
		
		model.addAttribute( "groupList", groupList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/msgSend/selectGrpPopUp";
	}
	
	/**
	 * 그룹 구성원 조회 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "msgSend/viewGroupUser" , method = RequestMethod.POST)
	public String viewGroupUserPost(Model model, MsgSendParamDto paramDto) throws Exception {
		
		List<GroupUserDto> groupUserList = msgSendService.selectGroupUserList(paramDto);
		
		model.addAttribute( "groupUserList", groupUserList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/msgSend/viewGrpUserPopUp";
	}
	
	/**
	 * 개별 메시지 전송 요청.
	 * @param paramDto
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="msgSend/individualSend" , method=RequestMethod.POST)
	public String individualSendPost( MsgSendParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String senderId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(senderId == null || "".equals(senderId))
        		throw new Exception(messageSource.getMessage("menu.securePush.msgSend.invalidSender", null, LocaleContextHolder.getLocale())); //발신자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
			msgSendService.sendIndividualMsg(paramDto);
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
	 * 조직도 검색 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "msgSend/selectOrg" , method = RequestMethod.POST)
	public String selectOrgPost(Model model, MsgSendParamDto paramDto, HttpServletRequest request){
		try{
			paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
			// 현재 사용자의 권한 그룹 아이디를 서비스 로직으로 전달하여 조직도 권한별 
			// 노출 제한이 동작토록 한다.
			paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
			List<DeptDto> deptList = msgSendService.getDeptList(paramDto);
			model.addAttribute("deptList", deptList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/msgSend/selectOrgPopUp";
	}
	
	/**
	 * 조직도 팝업 - 부서 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "msgSend/findDept" , method = RequestMethod.POST)
	public String findDeptPost(Model model, MsgSendParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<DeptDto> deptList = msgSendService.getDeptList(paramDto);
		
		model.addAttribute( "deptList", deptList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/msgSend/selectOrgPopUpDeptList";
	}
	
	/**
	 * 조직도 팝업 - 사용자 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "msgSend/findUser" , method = RequestMethod.POST)
	public String findUserPost(Model model, MsgSendParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<EmpDto> empList = msgSendService.getEmpList(paramDto);
		
		model.addAttribute( "empList", empList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/msgSend/selectOrgPopUpUserList";
	}
}
