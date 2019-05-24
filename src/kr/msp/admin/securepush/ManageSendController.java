package kr.msp.admin.securepush;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;
import kr.msp.admin.securepush.manageSend.dto.ManageSendParamDto;
import kr.msp.admin.securepush.manageSend.dto.MsgDto;
import kr.msp.admin.securepush.manageSend.dto.MsgFileDto;
import kr.msp.admin.securepush.manageSend.service.ManageSendService;
import kr.msp.admin.securepush.organization.dto.DeptDto;
import kr.msp.admin.securepush.organization.dto.EmpDto;

/**
 * 시큐어 푸쉬 발송 관리 메뉴 관련 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class ManageSendController {

	private final static Logger logger = LoggerFactory.getLogger(ManageSendController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private ManageSendService manageSendService;
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	/**
	 * 발송 관리 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="manageSend" , method=RequestMethod.GET)
	public String manageSendGet( Model model, ManageSendParamDto paramDto, HttpServletRequest request ) throws Exception{

		manageAccountService.setSessionSecurePushUserId(request);

		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(row_size);
		
		// 현재 로그인 한 사용자의 사용자 아이디로 목록 조회 조건을 제한한다.
		paramDto.setSENDER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<MsgDto> itemList = manageSendService.getSentMsgList(paramDto);

		model.addAttribute("itemList", itemList);
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/securepush/manageSend/manageSendMain";
	}
	
	/**
	 * 발송 메시지 리스트 페이징
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageSend" , method = RequestMethod.POST)
	public String manageSendPost(Model model, ManageSendParamDto paramDto, HttpServletRequest request) throws Exception{
		
		// 현재 로그인 한 사용자의 사용자 아이디로 목록 조회 조건을 제한한다.
		paramDto.setSENDER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<MsgDto> itemList = manageSendService.getSentMsgList(paramDto);
		
		model.addAttribute( "itemList", itemList );
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",paramDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageSend/manageSendList";
	}
	
	/**
	 * 발송 메시지 상세 페이지 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "manageSend/detailView" , method = RequestMethod.POST)
	public String detailViewPost(Model model, ManageSendParamDto paramDto) throws Exception {
		
		MsgDto itemInfo = manageSendService.getSentMsgInfo(paramDto);
		
		model.addAttribute( "itemInfo", itemInfo );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageSend/detailViewPopUp";
	}
	
	/**
	 * 메시지 전달.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageSend/forwardMsg" , method = RequestMethod.POST)
	public String forwardMsgPost(ManageSendParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String senderId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(senderId == null || "".equals(senderId))
        		throw new Exception(messageSource.getMessage("menu.securePush.msgSend.invalidSender", null, LocaleContextHolder.getLocale())); //발신자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setSENDER_ID(senderId);
        	
        	if(manageSendService.forwardMsg(paramDto))
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
	 * 메시지 전달 시 조직도 검색 팝업 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageSend/selectOrg" , method = RequestMethod.POST)
	public String selectOrgPost(Model model, ManageSendParamDto paramDto, HttpServletRequest request){
		try{
			paramDto.setSENDER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
			paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
			List<DeptDto> deptList = manageSendService.getDeptList(paramDto);
			model.addAttribute("deptList", deptList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageSend/selectOrgPopUp";
	}
	
	/**
	 * 조직도 팝업 - 부서 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageSend/findDept" , method = RequestMethod.POST)
	public String findDeptPost(Model model, ManageSendParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setSENDER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<DeptDto> deptList = manageSendService.getDeptList(paramDto);
		
		model.addAttribute( "deptList", deptList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageSend/selectOrgPopUpDeptList";
	}
	
	/**
	 * 조직도 팝업 - 사용자 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageSend/findUser" , method = RequestMethod.POST)
	public String findUserPost(Model model, ManageSendParamDto paramDto, HttpServletRequest request) throws Exception {
		
		paramDto.setSENDER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<EmpDto> empList = manageSendService.getEmpList(paramDto);
		
		model.addAttribute( "empList", empList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/manageSend/selectOrgPopUpUserList";
	}
	
	/**
	 * 발신 메시지 삭제 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "manageSend/deleteMsg" , method = RequestMethod.POST)
	public String deleteMsgPost(ManageSendParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
        	if(manageSendService.deleteMsg(paramDto))
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
	 * 메시지 첨부파일 다운로드.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "manageSend/downMsgFile" , method = RequestMethod.GET)
	public void downMsgFileGet(Model model, ManageSendParamDto paramDto, HttpServletRequest request
			, HttpServletResponse response) throws Exception {
		
		OutputStream outputStream = null;
		FileInputStream fileInputStream = null;

		try {
			
			MsgFileDto fileDto = manageSendService.getMsgFile(paramDto.getFILE_ID());

			File file = new File(fileDto.getFILE_PATH());
			fileInputStream = new FileInputStream(file);
			
			String mimeType = "application/octet-stream";
			response.setContentType(mimeType);
			response.setContentLength((int) file.length());

			response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileDto.getFILE_NM(), "UTF-8") + "\";");
			response.setHeader("Content-Transfer-Encoding", "binary");

			outputStream = response.getOutputStream();

			FileCopyUtils.copy(fileInputStream, outputStream);

		} catch (Exception e) {
			response.reset();
			logger.error("Exception caught.", e);
			throw new Exception(messageSource.getMessage("menu.mobile.common.noFilePath", null, LocaleContextHolder.getLocale())); //지정된 파일을 찾을 수 없습니다.
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
					outputStream.flush();
				} catch (Exception e) {
				}
			}
		}
	}
	
	
}
