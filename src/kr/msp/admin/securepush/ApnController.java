package kr.msp.admin.securepush;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.securepush.apn.service.ApnService;

/**
 * APN 등록 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class ApnController {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	private ApnService apnService;
	
	/**
	 * 이용 약관 관리 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="apn" , method=RequestMethod.GET)
	public String apnGet( Model model, HttpServletRequest request ){
		model.addAttribute("apnMap", apnService.getApn());
		
		logger.debug("$$$$$$$$$$ /admin/securepush $$$$$$$$$$");
		
		return "admin/securepush/apn/apnMain";
	}
	
	/**
	 * 이용약관 등록 처리.
	 * @param paramDto
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping( value = "apn/save" , method = RequestMethod.POST)
	public String registerPost(@RequestParam(value="apnName")String apnName, @RequestParam(value="apnIp")String apnIp
			, @RequestParam(value="USE_YN")String useYn, HttpServletRequest request) throws Exception{
		int result = 0;
		String resultMsg = "fail.";
		HashMap<String,String> reqMap = new HashMap<String,String>();
		reqMap.put("apnName", apnName);
		reqMap.put("apnIp", apnIp);
		reqMap.put("USE_YN", useYn);
		try {
        	if(apnService.registerApn(reqMap))
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
