package kr.msp.admin.fraudPrevent;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.fraudPrevent.appManage.dto.FraAppManageDto;
import kr.msp.admin.fraudPrevent.appManage.dto.FraAppManageParamDto;
import kr.msp.admin.fraudPrevent.appManage.service.FraAppManageService;
import kr.msp.admin.mobile.resourceUpload.dto.MobServiceDto;
import kr.msp.admin.mobile.resourceUpload.service.MobileResourceUploadService;

@RequestMapping(value="admin/fraudPrevent")
@Controller
public class FraudPreventController {

	private final static Logger logger = LoggerFactory.getLogger(FraudPreventController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	private @Value("${common.dir.fraudPrevent}") String DEFAULT_FRAUD_PREVENT_DIRECTORY;	//기본 리소스 디렉토리
	private @Value("${common.archive.charset:EUC-KR}") String ARCHIVE_CHARSET;	//압축해제시 기본 CharSet
	
	@Autowired(required=true)
	private MobileResourceUploadService mobileResourceUploadService;
	@Autowired(required=true)
	private FraAppManageService fraAppManageService;
	@Autowired(required = true) 
    private MessageSource messageSource; 
	
	@RequestMapping(value="/appManageMain", method=RequestMethod.GET )
	public String getAppManageMain( Model model, HttpServletRequest request, FraAppManageParamDto fraAppManageParamDto){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<MobServiceDto> listMobServiceDto = mobileResourceUploadService.SelectMobService(S_ID_USER);

		fraAppManageParamDto.setPAGE_NUM(1);
		fraAppManageParamDto.setPAGE_SIZE(row_size);
		List<FraAppManageDto> listFraAppManageDto =fraAppManageService.getFraudPreventAppManageList(fraAppManageParamDto);
		
		model.addAttribute("serviceList", listMobServiceDto);
		model.addAttribute("fraServiceList", listFraAppManageDto);
		
		model.addAttribute("R_PAGE_NUM",fraAppManageParamDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		return "admin/fraudPrevent/appManage/appManageMain";
	}
	
	@RequestMapping(value="/appManageMain", method=RequestMethod.POST)
	public String getAppManageMainPost( Model model, HttpServletRequest request, FraAppManageParamDto fraAppManageParamDto){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<MobServiceDto> listMobServiceDto = mobileResourceUploadService.SelectMobService(S_ID_USER);
		List<FraAppManageDto> listFraAppManageDto =fraAppManageService.getFraudPreventAppManageList(fraAppManageParamDto);
		
		model.addAttribute("serviceList", listMobServiceDto);
		model.addAttribute("fraServiceList", listFraAppManageDto);
		
		model.addAttribute("R_PAGE_NUM",fraAppManageParamDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",fraAppManageParamDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/fraudPrevent/appManage/appManageList";
	}
	
	@RequestMapping(value="/appManageDetail", method=RequestMethod.GET)
	public String getAppMangeDetailAdd( Model model, HttpServletRequest request){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		List<MobServiceDto> listMobServiceDto = mobileResourceUploadService.SelectMobService(S_ID_USER);
		
		model.addAttribute("serviceList", listMobServiceDto);
		model.addAttribute("isNewAdd", 1);
		
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/fraudPrevent/appManage/appManageDetail";
	}
	
	@RequestMapping(value="/appManageDetail", method=RequestMethod.POST)
	public String getAppMangeDetailPost( Model model, HttpServletRequest request, FraAppManageParamDto fraAppManageParamDto){
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		List<MobServiceDto> listMobServiceDto = mobileResourceUploadService.SelectMobService(S_ID_USER);
		
		FraAppManageDto fraAppManageDto =fraAppManageService.getFraudPreventAppManage(fraAppManageParamDto);
		
		model.addAttribute("serviceList", listMobServiceDto);
		model.addAttribute("fraService", fraAppManageDto);
		model.addAttribute("isNewAdd", 0);
		
		model.addAttribute("R_ROW_SIZE",fraAppManageParamDto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/fraudPrevent/appManage/appManageDetail";
	}
	
	@ResponseBody
	@RequestMapping(value="/addAppManage", method=RequestMethod.POST)
	public String addAppMange( Model model, HttpServletRequest request, FraAppManageParamDto fraAppManageParamDto) throws JsonGenerationException, JsonMappingException, IOException{
		// 1: true
		// 0: false
		//-1: 파일 처리
		//-2: db-duplicateKey 
		//-3: db- pk key null 
		int resultCode = 0;
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		fraAppManageParamDto.setREG_ID(S_ID_USER);
		fraAppManageParamDto.setHASH_BUILD_VER("1.0.0");
		
		File destinationDir = new File(DEFAULT_FRAUD_PREVENT_DIRECTORY);
		File destUnzipDir = new File(destinationDir + File.separator + "temp"); 
		fraAppManageParamDto.setARCHIVE_CHARSET(ARCHIVE_CHARSET);
		fraAppManageParamDto.setDestinationDir(destinationDir);
		fraAppManageParamDto.setDestUnzipDir(destUnzipDir);
		HashMap<String, Object> resultMap = null;
		fraAppManageParamDto.setAPP_ID(null);
		
		resultMap = fraAppManageService.uploadFraudPreventAppManage(fraAppManageParamDto);
		
		if(resultMap != null){
			if(fraAppManageParamDto.getAPP_ID() == null || fraAppManageParamDto.getAPP_VER() == null || fraAppManageParamDto.getOS_TYPE() == null){
				resultCode = -3;
			}else{
				ObjectMapper mapper = new ObjectMapper();
				fraAppManageParamDto.setHASH_VAL(mapper.writeValueAsString(resultMap));
				try{
					fraAppManageService.addFraudPreventAppManageList(fraAppManageParamDto);
					resultCode = 1;
				}catch(Exception e){
					if(e instanceof DuplicateKeyException){
						resultCode = -2;
					}else{
						logger.error("Exception caught.", e);
					}
				}
			}
		}else{
			resultCode = -1;
		}
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", resultCode);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/modifyAppManage", method=RequestMethod.POST)
	public String modifyAppMange( Model model, HttpServletRequest request, FraAppManageParamDto fraAppManageParamDto) throws Exception{
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		fraAppManageParamDto.setMOD_ID(S_ID_USER);
		fraAppManageParamDto.setHASH_BUILD_VER("1.0.0");
		
		int result = 0;
		try{
			fraAppManageService.modifyFraudPreventAppManageList(fraAppManageParamDto);
			result = 1;
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="/deleteAppManage", method=RequestMethod.POST)
	public String deleteAppMange( Model model, HttpServletRequest request, FraAppManageParamDto fraAppManageParamDto) throws Exception{
		int result = 0;
		try{
			fraAppManageService.deleteFraudPreventAppManageList(fraAppManageParamDto);
			result = 1;
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("result", result);
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@RequestMapping(value = "/download", method = RequestMethod.GET)
    public void fileDownload(HttpServletRequest request, HttpServletResponse response, FraAppManageParamDto fraAppManageParamDto) throws Exception{
		String fileName =  fraAppManageService.selectFraudPreventFileNm(fraAppManageParamDto);
		File downloadFile = new File(DEFAULT_FRAUD_PREVENT_DIRECTORY + fileName);
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
		 
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
		 
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	        
		} catch (Exception e) {
            throw new Exception(messageSource.getMessage("menu.mobile.common.noFilePath", null, LocaleContextHolder.getLocale())); //"지정된 파일을 찾을 수 없습니다."
		} finally{
			if(inputStream != null ) try{inputStream.close();}catch (Exception e) {logger.error("Exception caught.", e);}
			if(outStream != null ) try{outStream.close();}catch (Exception e) {logger.error("Exception caught.", e);}
		}

	}
}
