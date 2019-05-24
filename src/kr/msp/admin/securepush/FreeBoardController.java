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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.securepush.freeBoard.dto.BoardCatgDto;
import kr.msp.admin.securepush.freeBoard.dto.BoardDto;
import kr.msp.admin.securepush.freeBoard.dto.BoardFileDto;
import kr.msp.admin.securepush.freeBoard.dto.FreeBoardParamDto;
import kr.msp.admin.securepush.freeBoard.service.FreeBoardService;
import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;
import kr.msp.admin.securepush.msgSend.dto.UserDto;
import kr.msp.admin.securepush.msgSend.service.MsgSendService;

/**
 * 자유 게시판 관리 관련 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class FreeBoardController {

	private final static Logger logger = LoggerFactory.getLogger(FreeBoardController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private FreeBoardService freeBoardService;
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	@Autowired(required=true)
	private MsgSendService msgSendService;
	
	/**
	 * 자유 게시판 관리 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="freeBoard" , method=RequestMethod.GET)
	public String freeBoardGet( Model model, FreeBoardParamDto paramDto, HttpServletRequest request ) throws Exception{
		
		manageAccountService.setSessionSecurePushUserId(request);
		
		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(row_size);
		
		// TODO: 상수 처리할 것.
		paramDto.setBOARD_TYPE("FREE");
		List<BoardDto> itemList = freeBoardService.getBoardList(paramDto);
		model.addAttribute( "catgList", freeBoardService.getBoardCatgList(paramDto) );

		model.addAttribute("itemList", itemList);
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/securepush/freeBoard/freeBoardMain";
	}
	
	/**
	 * 자유게시판 메뉴 리스트 페이징
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "freeBoard" , method = RequestMethod.POST)
	public String freeBoardPost(Model model, FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception{
		
		paramDto.setBOARD_TYPE("FREE");
		List<BoardDto> itemList = freeBoardService.getBoardList(paramDto);
		model.addAttribute( "catgList", freeBoardService.getBoardCatgList(paramDto) );
		
		model.addAttribute( "itemList", itemList );
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",paramDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/freeBoard/freeBoardList";
	}
	
	/**
	 * 자유게시판 등록 페이지 진입.(페이지 팝업)
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "freeBoard/register" , method = RequestMethod.GET)
	public String registerGet(Model model, FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception {
		
		model.addAttribute( "catgList", freeBoardService.getBoardCatgList(paramDto) );
		
		// 보내는 사람 정보 조회.
		String sIdUser = (String)request.getSession().getAttribute("S_ID_USER");
		UserDto senderInfo = msgSendService.getSenderInfo(sIdUser);
		if(senderInfo != null && senderInfo.getUSER_ID() == null){
			senderInfo.setUSER_ID(sIdUser);
		}
		model.addAttribute("senderInfo",senderInfo);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/freeBoard/detailViewPopUp";
	}
	
	/**
	 * 자유게시판 상세 페이지 진입.(수정 페이지 팝업)
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "freeBoard/detailView" , method = RequestMethod.POST)
	public String detailViewPost(Model model, FreeBoardParamDto paramDto) throws Exception {
		
		BoardDto itemInfo = freeBoardService.getBoardDetail(paramDto);
		
		if(paramDto.getBOARD_ID() == null){
			model.addAttribute( "catgList", freeBoardService.getBoardCatgList(paramDto) );
		}
		
		model.addAttribute( "itemInfo", itemInfo );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/freeBoard/detailViewPopUp";
	}
	
	/**
	 * 자유게시판 카테고리 목록 팝업 페이지 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "freeBoard/categoryList" , method = RequestMethod.POST)
	public String categoryListPost(Model model, FreeBoardParamDto paramDto) throws Exception {
		
		List<BoardCatgDto> itemList = freeBoardService.getBoardCatgList(paramDto);
		
		model.addAttribute( "itemList", itemList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/freeBoard/categoryListPopUp";
	}
	
	/**
	 * 자유게시판 카테고리 상세 팝업 진입. (수정 페이지)
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "freeBoard/categoryView" , method = RequestMethod.POST)
	public String categoryViewPost(Model model, FreeBoardParamDto paramDto) throws Exception {
		
		BoardCatgDto itemInfo = freeBoardService.getBoardCatgInfo(paramDto);
		
		model.addAttribute( "itemInfo", itemInfo );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/freeBoard/categoryViewPopUp";
	}
	
	/**
	 * 자유게시판 카테고리 등록 페이지 진입.(페이지 팝업)
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "freeBoard/addCategory" , method = RequestMethod.GET)
	public String addCategoryGet(Model model, FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception {
		
		// 보내는 사람 정보 조회.
		String sIdUser = (String)request.getSession().getAttribute("S_ID_USER");
		UserDto senderInfo = msgSendService.getSenderInfo(sIdUser);
		if(senderInfo != null && senderInfo.getUSER_ID() == null){
			senderInfo.setUSER_ID(sIdUser);
		}
		model.addAttribute("senderInfo",senderInfo);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/freeBoard/categoryViewPopUp";
	}
	
	/**
	 * 게시글 등록 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/register" , method = RequestMethod.POST)
	public String registerPost(FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String regId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(regId == null || "".equals(regId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(regId);
        	paramDto.setBOARD_TYPE("FREE");
        	if(freeBoardService.registBoard(paramDto))
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
	 * 게시글 수정 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/updateBoard" , method = RequestMethod.POST)
	public String updateBoardPost(FreeBoardParamDto paramDto, HttpServletRequest request ) throws Exception{
		
		int result = 0;
		String resultMsg = "fail.";
		try {
			String regId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(regId == null || "".equals(regId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(regId);
        	paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
        	
        	if(freeBoardService.updateBoard(paramDto))
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
	 * 게시글 삭제 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/deleteBoard" , method = RequestMethod.POST)
	public String deleteBoardPost(FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String regId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(regId == null || "".equals(regId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(regId);
        	paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
        	
        	if(freeBoardService.deleteBoard(paramDto))
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
	 * 카테고리 등록 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/addCategory" , method = RequestMethod.POST)
	public String addCategoryPost(FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String regId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(regId == null || "".equals(regId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(regId);
        	
        	if(freeBoardService.registerBoardCatg(paramDto))
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
	 * 카테고리 수정 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/updateCategory" , method = RequestMethod.POST)
	public String updateCategoryPost(FreeBoardParamDto paramDto, HttpServletRequest request ) throws Exception{
		
		int result = 0;
		String resultMsg = "fail.";
		try {
			String regId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(regId == null || "".equals(regId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(regId);
        	
        	if(freeBoardService.updateBoardCatg(paramDto))
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
	 * 카테고리 삭제 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/deleteCategory" , method = RequestMethod.POST)
	public String deleteCategoryPost(FreeBoardParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
			String regId = (String)request.getSession().getAttribute("S_SPUSH_ID_USER");
        	if(regId == null || "".equals(regId))
        		throw new Exception(messageSource.getMessage("menu.securePush.manageGrp.invalidWriter", null, LocaleContextHolder.getLocale())); //작성자 정보가 유효하지 않습니다. 시큐어 푸쉬 사용자로 등록된 계정인지 확인해 주세요.
        	
        	paramDto.setREG_ID(regId);
        	
        	if(freeBoardService.deleteBoardCatg(paramDto))
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
	 * 작성 화면에서 선택한 첨부파일을 임시 저장함.
	 * @param attachFile
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="freeBoard/attachUpload" , method=RequestMethod.POST, produces = "text/html; charset=utf8")
	public String attachUploadPost( @RequestParam("ATTACH_FILE") MultipartFile attachFile, HttpServletRequest request ) throws Exception {
		// 클라이언트 ajaxfileupload.js에서 응답 content-type을 application-json으로 하였을 때 
		// 오류가 발생하는 현상이 있음. 현재는 text/html로 응답하고 utf-8 명시하여 한글 파일명이 깨지지 않도록 처리.
		int result = 0;
		String fileKey = "";
		String fileName = "";
		String fileSize = "";
		try {
			if(attachFile!=null && attachFile.getSize()>0){
				fileKey = freeBoardService.attachUploadAndGenerateKey(attachFile);
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
	 * 작성 화면에서 선택한 첨부파일을 삭제 처리함.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "freeBoard/attachDelete" , method = RequestMethod.POST)
	public String attachDeletePost(FreeBoardParamDto paramDto, HttpServletRequest request ) throws Exception{
		
		int result = 0;
		String resultMsg = "fail.";
		try {
        	
        	if(freeBoardService.deleteBoardFile(paramDto))
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
	 * 게시판 첨부파일 다운로드.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "freeBoard/downBoardFile" , method = RequestMethod.GET)
	public void downBoardFileGet(Model model, FreeBoardParamDto paramDto, HttpServletRequest request
			, HttpServletResponse response) throws Exception {
		
		OutputStream outputStream = null;
		FileInputStream fileInputStream = null;

		try {
			
			BoardFileDto fileDto = freeBoardService.getBoardFile(paramDto.getFILE_ID());

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
