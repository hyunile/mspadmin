package kr.msp.admin.mobile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.base.utils.tree.TreeUtil;
import kr.msp.admin.mobile.resourceConfig.dto.ResourceExtDto;
import kr.msp.admin.mobile.resourceUpload.dto.MobServiceDto;
import kr.msp.admin.mobile.resourceUpload.dto.ResourceFileDto;
import kr.msp.admin.mobile.resourceUpload.service.MobileResourceUploadService;

@Controller
@RequestMapping(value="admin/mobile")
public class MobileResourceUploadController {
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수 
	private @Value("${common.list.page_size}") int page_size;
	
	private @Value("${common.dir.resource}") String DEFAULT_RESOURCE_DIRECTORY;	//기본 리소스 디렉토리
		

	
	@Autowired(required=true)
	private MobileResourceUploadService mobileResourceUploadService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	private static Logger logger = LoggerFactory.getLogger(MobileResourceUploadController.class);
		
	@RequestMapping(value="resourceUpload",method=RequestMethod.GET)
	public String mobileResourceUploadGet( Model model ,HttpServletRequest request){
		
		logger.info("mobileResourceUploadGet START"); 
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<MobServiceDto> mobServiceList = mobileResourceUploadService.SelectMobService(S_ID_USER);
		
		List<ResourceExtDto> resourceExtList = mobileResourceUploadService.SelectMobExtAll(); 
		
		model.addAttribute( "mobServiceList", mobServiceList );
		model.addAttribute("resourceExtList", resourceExtList);
		
		logger.info("mobileResourceUploadGet FINISH");
		
		return "admin/mobile/resourceUpload/resourceUploadMain";
	}
	
	@ResponseBody
	@SuppressWarnings("unchecked")
	@RequestMapping(value="resourceUpload/tempUpload",method=RequestMethod.POST)
	public ModelAndView mobileTempUploadPost( Model model ,HttpServletRequest request
			, @RequestParam("RES_FILE") MultipartFile RES_FILE , MobServiceDto mobService ) throws JsonGenerationException, JsonMappingException, IOException{

		File destinationDir = null;

		int result = -1;

		String SVC_ID = mobService.getSVC_ID();
		
		if (destinationDir == null) {
			String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
	    	String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");
    		destinationDir = new File(DEFAULT_RESOURCE_DIRECTORY + "update" + File.separator + "temp" + File.separator + pathYear + File.separator + pathMonth + File.separator);
    	}

		File destUnzipDir = new File(DEFAULT_RESOURCE_DIRECTORY+ "update" + File.separator + "upload" + File.separator + "temp" + File.separator + SVC_ID + File.separator);
        HashMap<String, Object> map = new HashMap<String,Object>();
		try{
			//파일 삭제
			if( destUnzipDir.isDirectory() ){
				removeDIR(DEFAULT_RESOURCE_DIRECTORY+ "update" + File.separator + "upload" + File.separator + "temp" + File.separator + SVC_ID + File.separator);
			}
			
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
			String filePath = destinationDir.getAbsolutePath() + File.separator + prefixFileName + "-" + RES_FILE.getOriginalFilename();
			File toFile = new File(filePath);
			RES_FILE.transferTo(toFile);
			
			// 리소스 압축 파일을 풀고 저장
			result = mobileResourceUploadService.ResourceTempFile(SVC_ID ,RES_FILE, toFile ,destUnzipDir, isInitRes);

		} catch (IOException e) {
			logger.error("Exception caught.", e);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
            map.put("result", result);
            //map.put("msg",e.getMessage());
            map.put("msg",messageSource.getMessage("menu.mobile.common.failUpload", null, LocaleContextHolder.getLocale()) ); //업로드에 실패 했습니다.
		}
		
		if(result == -1){
			map.put("result", result);
            if(!map.containsKey("msg")) {
                map.put("msg",messageSource.getMessage("menu.mobile.common.failUpload", null, LocaleContextHolder.getLocale()) ); //업로드에 실패 했습니다.
            }
		} else {
			map.put("result", result);
			map.put("msg","" );
		}

        ModelAndView mv = new ModelAndView();
        mv.setViewName("jsonMultiPartView");
        mv.addAllObjects(map);
        return mv;
//		ObjectMapper mapper = new ObjectMapper();
//		String data = mapper.writeValueAsString(map);
//		return data;
	}
	
	public static void removeDIR(String source){
		File[] listFile = new File(source).listFiles(); 
		try{
			if(listFile.length > 0){
				for(int i = 0 ; i < listFile.length ; i++){
					if(listFile[i].isFile()){
						listFile[i].delete();
					}else{
						removeDIR(listFile[i].getPath());
					}
					listFile[i].delete();
				}
			}
		}catch(Exception e){
			System.err.println(System.err);
			System.exit(-1); 
		}	
	}
	
	@RequestMapping(value="resourceUpload/tempShow" , method=RequestMethod.GET)
	public String resourceUploadTempShowGet( Model model , ResourceFileDto resourceFile ){
		
		List<ResourceFileDto> resourceSummaryList = mobileResourceUploadService.SelsectResourceSummary( resourceFile);	
		
		model.addAttribute("resourceSummaryList", resourceSummaryList );
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/resourceUpload/resourceTempShow";
	}
	
	@RequestMapping(value="resourceUpload/rscSummary" , method=RequestMethod.GET)
	public String resourceUploadRscSummay( Model model , ResourceFileDto resourceFile ){
		
		List<ResourceFileDto> resourceSummaryList = mobileResourceUploadService.SelsectResourceSummary( resourceFile);	
		
		model.addAttribute("resourceSummaryList", resourceSummaryList );
			
		model.addAttribute("layout", "layout/null.vm");
		return "admin/mobile/resourceUpload/resourceSummary";
	}
	
	/*
	@RequestMapping(value="resourceUpload/rscTree" , method=RequestMethod.GET)
	public String resourceUploadRscTree( Model model , ResourceFileDto resourceFile ) throws Exception{
		
		List<ResourceFileDto> resourceSummaryList = mobileResourceUploadService.SelsectResourceTree( resourceFile );	
		
		model.addAttribute("resTot", TreeUtil.resourceListToTree(resourceSummaryList , DEFAULT_RESOURCE_DIRECTORY));
			
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/resourceUpload/resourceTree";
	}
	*/
	@RequestMapping(value="resourceUpload/rscTree" , method=RequestMethod.GET)
	public String resourceUploadRscTree( Model model , ResourceFileDto resourceFile ) throws Exception{
		
		List<ResourceFileDto> resourceSummaryList = mobileResourceUploadService.SelsectResourceTree( resourceFile );	
		String rscListOrg = TreeUtil.resourceListToTree(resourceSummaryList , DEFAULT_RESOURCE_DIRECTORY);
		String changeRscList = rscListOrg;
		try {
			
			changeRscList = getRemovedFilePathTree(rscListOrg);
			
		} catch(Exception e) {
			logger.error("Exception caught.", e);
		}
		
		model.addAttribute("resTot", changeRscList);
			
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/resourceUpload/resourceTree";
	}
	
	public String getRemovedFilePathTree(String rscListOrg) throws Exception {
		String dirOrg = DEFAULT_RESOURCE_DIRECTORY;
		String changeRscList = rscListOrg;
		
		String OS = System.getProperty("os.name").toLowerCase();
		
	    if(OS.indexOf("win") >= 0) {
	    	// in case of windows server
			
	    	
	    	dirOrg = dirOrg.replaceAll("/", "\\\\");
	    	Pattern p = Pattern.compile("([a-z]{0,1}):", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(dirOrg);
	    	
			if (m.find( )) {
				
				dirOrg = m.replaceAll("");
				dirOrg = dirOrg.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
				
//				System.out.println("1:"+dirOrg);
				dirOrg = "(\"file_path\":\")([a-z]{0,1})(:{0,1})" + dirOrg + "update\\\\\\\\upload\\\\\\\\temp\\\\\\\\";
			} else {
				//no found
				
				dirOrg = dirOrg.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
//				System.out.println("2:"+dirOrg);
				dirOrg = "(\"file_path\":\")([a-z]{0,1})(:{0,1})" + dirOrg + "update\\\\\\\\upload\\\\\\\\temp\\\\\\\\";
			}
			

	    	rscListOrg = rscListOrg.replaceAll("/", "\\\\");

//			System.out.println(dirOrg);
//			System.out.println(rscListOrg);
	    	
			p = Pattern.compile(dirOrg, Pattern.CASE_INSENSITIVE);
			m = p.matcher(rscListOrg);

			if (m.find( )) {
				
				 changeRscList = m.replaceAll("\"file_path\":\"");

//				 System.out.println("Org value    : " + rscListOrg );
//		         System.out.println("Changed value: " + changeRscList );
		    }else {
		         
//		    		System.out.println("NO FOUND" );
		    }
			
		} else {
			// in case of unix, linux series
			
			dirOrg = "\"file_path\":\"" + dirOrg + "update/upload/temp/";
			
			Pattern p = Pattern.compile(dirOrg);
			Matcher m = p.matcher(rscListOrg);
			
			if (m.find( )) {
				 
				 changeRscList = m.replaceAll("\"file_path\":\"");

//		         System.out.println("Org value    : " + rscListOrg );
//		         System.out.println("Changed value: " +  changeRscList);
		    } else {
//		         System.out.println("NO MATCH");
		    }
		}
	    
	    return changeRscList;
	}
	
	@ResponseBody
	@RequestMapping(value="resourceUpload/summaryDelete" , method=RequestMethod.POST)
	public String resourceSummayDelete( Model model , @RequestParam("CH_FILE_EXT") String [] FILE_EXT
			, ResourceFileDto resourceFile ) throws JsonGenerationException, JsonMappingException, IOException{
		
		HashMap<String,Object> deleteTrmpMap = new HashMap<String,Object>();
		deleteTrmpMap.put("SVC_ID", resourceFile.getSVC_ID());
		deleteTrmpMap.put("FILE_EXT", FILE_EXT);
		
		int result = -1;
		
		try {
			result = mobileResourceUploadService.DeleteTempResSummay( deleteTrmpMap );
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result < 1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //삭제 되었습니다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	
	@ResponseBody
	@RequestMapping(value="resourceUpload/treeDelete" , method=RequestMethod.POST)
	public String resourceTreeDelete( Model model , @RequestParam("RSC_FILE_IDX") String [] RSC_FILE_IDX
			) throws JsonGenerationException, JsonMappingException, IOException{
		
		HashMap<String,Object> deleteTrmpMap = new HashMap<String,Object>();
		deleteTrmpMap.put("RSC_FILE_IDX", RSC_FILE_IDX);
				
		int result = -1;
		
		try {
			result = mobileResourceUploadService.DeleteTempResTree( deleteTrmpMap );
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result < 1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //삭제 되었습니다.
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	
	@ResponseBody
	@RequestMapping(value="resourceUpload/regist" , method=RequestMethod.POST)
	public String resourceUploadRegist( Model model , ResourceFileDto resourceFile) throws JsonGenerationException, JsonMappingException, IOException{
				
		int result = -1;
		
		try {
            logger.info("###########################리소스 저장 업로드 시작###################");
			result = mobileResourceUploadService.resourceUploadRegist(resourceFile);
            logger.info("###########################리소스 저장 업로드 마침###################");
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result < 1){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //등록에 실패했습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		}
			
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
