/**
 * 앱스토어의 사용자 단말기 관리를 하는 콘트롤러
 *  sllim 2013.11.01 
 */
package kr.msp.admin.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.base.utils.CommonUtil;
import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.common.service.HomeService;


@Controller
public class CommonController extends ControllerUtil{
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	protected HomeService homeService;
	
	//private static @Value("${common.root.path}") String ROOT_PATH;

	private static final int BUFFER_SIZE = 4096;
	
	//파일 다운로드(스트림)
//	@RequestMapping(value = "${common.root.path}/admin/download", method = RequestMethod.GET)
	@RequestMapping(value = "/admin/download", method = RequestMethod.GET)
    public void fileDownload(@ModelAttribute("fileName") String fileName,@ModelAttribute("dispName") String dispName, HttpServletRequest request, HttpServletResponse response){
		logger.debug(this.getClass().getName() + " == fileDownload Enter == ");
		FileInputStream inputStream = null;
		OutputStream outStream = null;
		try {
			
			//한글처리 
			//CommonUtil.printEncodingString(fileName);
			fileName = CommonUtil.convertEncoding(fileName, "8859_1", "UTF-8");
			dispName = CommonUtil.convertEncoding(dispName, "8859_1", "UTF-8");
			
			File downloadFile = new File(fileName);
			
			inputStream = new FileInputStream(downloadFile);
	
			String mimeType = "application/octet-stream";
			  
			response.setContentType(mimeType);
			response.setContentLength((int) downloadFile.length());
		 
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", URLEncoder.encode(dispName, "UTF-8"));
			response.setHeader(headerKey, headerValue);
		 
			outStream = response.getOutputStream();
		 
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
		 
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	        
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		} finally{
			if(inputStream != null) try{inputStream.close();}catch (Exception e) { logger.error("Exception caught.", e); }
			if(outStream != null) try{outStream.close();}catch (Exception e) { logger.error("Exception caught.", e); }
		}
	}
}