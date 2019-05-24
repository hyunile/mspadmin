package kr.msp.admin.securepush;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.securepush.registerHR.dto.RegisterHRParamDto;
import kr.msp.admin.securepush.registerHR.service.RegisterHRService;

/**
 * 인사정보 등록 메뉴 관련 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class RegisterHRController {

	private final static Logger logger = LoggerFactory.getLogger(RegisterHRController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private RegisterHRService registerHRService;
	
	/**
	 * 인사 등록 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="registerHR" , method=RequestMethod.GET)
	public String registerHRGet( Model model, HttpServletRequest request ){
		
		return "admin/securepush/registerHR/registerHRMain";
	}
	
	/**
	 * 엑셀 인사 정보 등록 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "registerHR/register" , method = RequestMethod.POST)
	public String registerPost(RegisterHRParamDto paramDto, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		try {
        	if(registerHRService.registerHRFile(paramDto))
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
