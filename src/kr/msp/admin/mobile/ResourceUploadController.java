package kr.msp.admin.mobile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
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
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.mobile.dto.MobServiceDto;
import kr.msp.admin.mobile.system.ResourceManager;


@Controller
public class ResourceUploadController {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceUploadController.class);

//	private File destinationDir = null;
//	private File destUnzipDir = null;

	private @Value("${common.dir.resource}") String DEFAULT_RESOURCE_DIRECTORY;	//기본 리소스 디렉토리
	
	@Autowired(required=true)
	protected ResourceManager manager;

	@Autowired(required = true) 
	private MessageSource messageSource;

	//모바일 서비스 목록조회 
	public List<MobServiceDto> getMobileService(String userid){
		return manager.getMobileService(userid);
	}
	/*
	public void setDestinationDir(File destinationDir) {
		String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
    	String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");
        this.destinationDir = new File(destinationDir, pathYear + File.separator + pathMonth + File.separator);
	}
	*/
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "admin/mobile/rsc/upload/doprocess", method = RequestMethod.POST)
    public String handleFormUpload(Model model, @RequestParam("FILE1") MultipartFile file, @RequestParam("SVC_ID") String serviceId, HttpServletRequest request) {

		File destinationDir = null;
		File destUnzipDir = null;

		logger.info("\n --- handleFormUpload Enter ---");
		logger.info("- request param -");
		logger.info("SVC_ID : " + serviceId);
		logger.info("FILE1 : " + file.getName());
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		logger.info("S_ID_USER : " + S_ID_USER);
		
		List<MobServiceDto> serviceList = getMobileService(S_ID_USER);
		model.addAttribute("SVC_ID",serviceId);
		model.addAttribute("serviceList",serviceList);
		
		model.addAttribute("rtnCd","000");
		model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpload", null, LocaleContextHolder.getLocale())); //"업로드 완료"
		
		if (StringUtils.isBlank(serviceId)) {
			logger.error("ERROR :  Not entered SERVICE_ID");
			model.addAttribute("rtnCd","999");
			model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.noServiceId", null, LocaleContextHolder.getLocale())); //서비스 아이디가 입력되지 않음
			
			return "admin/mobile/resourceUpload";	
			
		}
		
		if (destinationDir == null) {
			String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
	    	String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");
    		destinationDir = new File(DEFAULT_RESOURCE_DIRECTORY + "update" + File.separator + "temp" + File.separator + pathYear + File.separator + pathMonth + File.separator);
    	}
		
		destUnzipDir = new File(DEFAULT_RESOURCE_DIRECTORY+ "update" + File.separator + "upload" + File.separator + serviceId + File.separator);
		try {
			
			FileUtils.forceMkdir(destUnzipDir);
			FileUtils.forceMkdir(destinationDir);
			
			// 처음 실행 여부 확인(디렉토리 없으면 최초 실행)
			boolean isInitRes = false;
			List<File> resFileList = (List<File>)FileUtils.listFiles(destUnzipDir,null, true);
			if ( resFileList.size() < 1 ) {
				isInitRes = true;
			}
			
			String prefixFileName = UUID.randomUUID().toString();
			// 업로드 디렉토리 이동
			String filePath = destinationDir.getAbsolutePath() + File.separator + prefixFileName + "-" + file.getOriginalFilename();
			File toFile = new File(filePath);
			file.transferTo(toFile);
			
			// 리소스 압축 파일을 풀고 저장
			manager.importResourceFile(serviceId,file, toFile,destUnzipDir, isInitRes);
			
		} catch (IOException e) {
			logger.error("Exception caught.", e);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		
		return "admin/mobile/resourceUpload";	
		
    }
	
}
