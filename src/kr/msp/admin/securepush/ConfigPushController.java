package kr.msp.admin.securepush;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
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

import kr.msp.admin.securepush.configPush.dto.ConfigPushParamDto;
import kr.msp.admin.securepush.configPush.service.ConfigPushService;

/**
 * 푸시 관리 메뉴 관련 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class ConfigPushController {

	private final static Logger logger = LoggerFactory.getLogger(ConfigPushController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private ConfigPushService configPushService;
	
	/**
	 * 푸시 관리 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="configPush" , method=RequestMethod.GET)
	public String configPushGet( Model model, HttpServletRequest request ){

		model.addAttribute("itemInfo", configPushService.getMsgConfig());
		
		return "admin/securepush/configPush/configPushMain";
	}
	
	/**
	 * 푸시메시지 설정 등록 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "configPush/register" , method = RequestMethod.POST)
	public String registerPost(ConfigPushParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
        	if(configPushService.registerMsgConf(paramDto))
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
	 * 아이콘 이미지 다운로드.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "configPush/downIconFile" , method = RequestMethod.GET)
	public void downIconFileGet(Model model, ConfigPushParamDto paramDto, HttpServletRequest request
			, HttpServletResponse response) throws Exception {
		
		OutputStream outputStream = null;
		FileInputStream fileInputStream = null;

		try {

			File file = new File(paramDto.getICON_PATH());
			fileInputStream = new FileInputStream(file);
			
			String mimeType = "application/octet-stream";
			response.setContentType(mimeType);
			response.setContentLength((int) file.length());

			String fileName = FilenameUtils.getName(paramDto.getICON_PATH());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\";");
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
