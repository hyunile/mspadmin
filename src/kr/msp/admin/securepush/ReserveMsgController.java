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
import kr.msp.admin.securepush.reserveMsg.dto.MsgFileDto;
import kr.msp.admin.securepush.reserveMsg.dto.ReservMsgDto;
import kr.msp.admin.securepush.reserveMsg.dto.ReserveMsgParamDto;
import kr.msp.admin.securepush.reserveMsg.service.ReserveMsgService;

@RequestMapping( value="admin/securepush")
@Controller
public class ReserveMsgController {

	private final static Logger logger = LoggerFactory.getLogger(ReserveMsgController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private ReserveMsgService reserveMsgService;
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	/**
	 * 예약 발송 관리 메뉴 진입 처리.
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="reserveMsg" , method=RequestMethod.GET)
	public String reserveMsgGet( Model model, ReserveMsgParamDto paramDto, HttpServletRequest request) throws Exception{
		
		manageAccountService.setSessionSecurePushUserId(request);

		paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(row_size);
		
		List<ReservMsgDto> itemList = reserveMsgService.getReservMsgList(paramDto);

		model.addAttribute("itemList", itemList);
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		return "admin/securepush/reserveMsg/reserveMsgMain";
	}
	
	/**
	 * 예약 메시지 리스트 페이징
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "reserveMsg" , method = RequestMethod.POST)
	public String reserveMsgPost(Model model, ReserveMsgParamDto paramDto, HttpServletRequest request) throws Exception{
		
		paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		
		List<ReservMsgDto> itemList = reserveMsgService.getReservMsgList(paramDto);
		
		model.addAttribute( "itemList", itemList );
		model.addAttribute("R_PAGE_NUM",paramDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",paramDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/reserveMsg/reserveMsgList";
	}
	
	/**
	 * 예약 메시지 상세 페이지 진입.
	 * @param model
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@RequestMapping( value = "reserveMsg/detailView" , method = RequestMethod.POST)
	public String detailViewPost(Model model, ReserveMsgParamDto paramDto) throws Exception {
		
		ReservMsgDto itemInfo = reserveMsgService.getReservMsgInfo(paramDto);
		
		model.addAttribute( "itemInfo", itemInfo );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/reserveMsg/detailViewPopUp";
	}
	
	/**
	 * 예약 메시지 취소(삭제).
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "reserveMsg/cancelReserv" , method = RequestMethod.POST)
	public String cancelReservPost(ReserveMsgParamDto paramDto, HttpServletRequest request) throws Exception{
		
		int result = 0;
		String resultMsg = "fail.";
		try {
			paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
        	
        	if(reserveMsgService.cancelReservMsg(paramDto))
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
	@RequestMapping( value = "reserveMsg/downMsgFile" , method = RequestMethod.GET)
	public void downMsgFileGet(Model model, ReserveMsgParamDto paramDto, HttpServletRequest request
			, HttpServletResponse response) throws Exception {
		
		OutputStream outputStream = null;
		FileInputStream fileInputStream = null;

		try {
			
			MsgFileDto fileDto = reserveMsgService.getMsgFile(paramDto.getFILE_ID());

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
			
			try {
				if (fileInputStream != null)
					fileInputStream.close();
				outputStream.flush();
			} catch (Exception e) {}
		}
	}
}
