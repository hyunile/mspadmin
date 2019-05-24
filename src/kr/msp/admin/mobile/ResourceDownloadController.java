package kr.msp.admin.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import kr.msp.admin.mobile.resourceManage.service.ResourceManageService;


@Controller
public class ResourceDownloadController {

    @Autowired(required=true)
    ResourceManageService resourceManageService;
    
    @Autowired(required = true) 
	private MessageSource messageSource; 

    private static final Logger logger = LoggerFactory.getLogger(ResourceDownloadController.class);
    private static final int BUFFER_SIZE = 4096;

	@RequestMapping(value = "admin/mobile/rsc/download", method = RequestMethod.GET)
    public void fileDownload(@RequestParam Map<String,Object> reqMap, HttpServletRequest request, HttpServletResponse response) throws Exception{
		logger.info("\n == handleFormDownload Enter == ");
        String fileName = "";
		if(!reqMap.containsKey("FILE_IDX") && !reqMap.containsKey("D_TYPE")){
            throw new Exception(messageSource.getMessage("menu.mobile.common.incorrectParam", null, LocaleContextHolder.getLocale())); //파라미터가 올바르지 않습니다.
        }
        if(reqMap.get("D_TYPE").equals("rsc")){
            Map<String,Object> fileAttachMap = resourceManageService.SelectFileAttach(reqMap);
            if(fileAttachMap==null){
                throw new Exception(messageSource.getMessage("menu.mobile.common.noFile", null, LocaleContextHolder.getLocale()));  //파일이 존재 하지 않습니다.
            }
            fileName = fileAttachMap.get("FILE_PATH").toString();
        }else{
            throw new Exception(messageSource.getMessage("menu.mobile.common.incorrectDown", null, LocaleContextHolder.getLocale()));  //다운로드 요청이 올바르지 않습니다
        }

		File downloadFile = new File(fileName);
		FileInputStream inputStream = null;
		OutputStream outStream = null;
		try {
			inputStream = new FileInputStream(downloadFile);
	
			String mimeType = "application/octet-stream";
			  
			response.setContentType(mimeType);
			response.setContentLength((int) downloadFile.length());
		 
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
			response.setHeader(headerKey, headerValue);
		 
			outStream = response.getOutputStream();
		 
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
		 
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	        
		} catch (Exception e) {
            throw new Exception(messageSource.getMessage("menu.mobile.common.noFilePath", null, LocaleContextHolder.getLocale())); //지정된 파일을 찾을 수 없습니다.
		} finally{
			if(inputStream != null ) try{inputStream.close();}catch (Exception e) {logger.error("Exception caught.", e);}
			if(outStream != null ) try{outStream.close();}catch (Exception e) {logger.error("Exception caught.", e);}
		}

	}
	

}
