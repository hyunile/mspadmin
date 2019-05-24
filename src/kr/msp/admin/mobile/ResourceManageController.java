package kr.msp.admin.mobile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ZipCompressUtil;
import kr.msp.admin.mobile.resourceManage.dto.ResourceFileDetailDto;
import kr.msp.admin.mobile.resourceManage.dto.ResourceGoMenuDto;
import kr.msp.admin.mobile.resourceManage.service.ResourceManageService;
import kr.msp.admin.mobile.resourceUpload.dto.MobServiceDto;
import kr.msp.admin.mobile.resourceUpload.dto.ResourceDeployDto;
import kr.msp.admin.mobile.resourceUpload.dto.ResourceFileDto;

@Controller
@RequestMapping(value="admin/mobile")
public class ResourceManageController {

	private final static Logger logger = LoggerFactory.getLogger(ResourceManageController.class);

	//공통 페이지 로우수
	private int row_size = 25; 
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required=true)
	ResourceManageService resourceManageService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	private @Value("${admin.resource.compress.partition.size}") long COMPRESS_PARTITION_SIZE;
	
	@RequestMapping(value="resourceManageMenu",method=RequestMethod.POST)
	public String resourceManageMenu( Model model ,HttpServletRequest request, ResourceGoMenuDto resourceGoMenu){
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		request.getSession().setAttribute("S_ID_LEFT_MENU",resourceGoMenu.getMENU_GO());
		
		List<MobServiceDto> mobServiceList = resourceManageService.SelectMobService(S_ID_USER);
				
		model.addAttribute("mobServiceList", mobServiceList);
		model.addAttribute("R_SVC_ID", resourceGoMenu.getSVC_ID() );
		
		return "admin/mobile/resourceManage/resourceManageMain";
	}
	
	
	@RequestMapping(value="resourceManage",method=RequestMethod.GET)
	public String resourceManage( Model model ,HttpServletRequest request){
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		List<MobServiceDto> mobServiceList = resourceManageService.SelectMobService(S_ID_USER);
		
		model.addAttribute("mobServiceList", mobServiceList);
		return "admin/mobile/resourceManage/resourceManageMain";
	}
	
	@RequestMapping(value="resourceManage",method=RequestMethod.POST)
	public String resourceManage( Model model ,HttpServletRequest request, ResourceFileDto resourceFile){
		
		if(resourceFile.getPAGE_SIZE() == 0 ){
			resourceFile.setPAGE_SIZE(row_size);
		}
		if(resourceFile.getORDER_TARGET() == null && resourceFile.getORDER_TARGET().equals("")){
			resourceFile.setORDER_TARGET("FILE_NM");
		}
		
		if( resourceFile.getORDER_TYPE() == null && resourceFile.getORDER_TYPE().equals("") ){
			resourceFile.setORDER_TYPE("ASC");
		}
		
		List<ResourceFileDto> mobServiceList = resourceManageService.SelectResourceManage(resourceFile);
		
		model.addAttribute( "mobServiceList", mobServiceList );
		model.addAttribute("R_PAGE_NUM",resourceFile.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",resourceFile.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/mobile/resourceManage/resourceManageList";
	}
	
	//리소스 배포화면 - 다음 버전조회
	@RequestMapping(value="resourceManage/deploy",method=RequestMethod.POST )
	public String resourceDeployDPLY_VER( Model model,HttpServletRequest request,ResourceDeployDto resourceDeploy) {
		
		String DEPLOY_VERSION = resourceManageService.SelectResourceDeployVersion( resourceDeploy );
		
		resourceDeploy.setDEPLOY_VERSION( StringUtils.leftPad(DEPLOY_VERSION, 6, "0") );
		
		model.addAttribute("resourceDeploy", resourceDeploy);
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/mobile/resourceManage/resourceDeployPop";
	}
	
	//리소스 배포화면 - 다음 버전조회
	@ResponseBody
	@RequestMapping(value="resourceManage/doprocess" , method=RequestMethod.POST)
	public String resourceDeployDoproocess( Model model,HttpServletRequest request,ResourceDeployDto resourceDeploy) throws JsonGenerationException, JsonMappingException, IOException {
		
		int result = -1;
		
		//init Variables
		HashMap<String, String> resTargetFileMap = new HashMap<String, String>();
	   	List<HashMap<String, Object>> resSelectTargetFileList = new ArrayList<HashMap<String, Object>>(); // 선택된 리소스 파일(배포, 삭제)
	   	List<HashMap<String, String>> resTargetPartFileList = new ArrayList<HashMap<String, String>>(); // 리소스 배포 분할 파일 저장(이 list의 size가 zip파일 갯수)
	   	long resTotalFileSize = 0; // 배포 파일을 분할 하기위한 파일 사이즈
	   	
	   	// serviceid로 appid를 조회한다.
	   	String appId = resourceManageService.selectAppid(resourceDeploy.getSVC_ID());
//	   	System.out.println(appId);
	   	boolean isAddResExist = false;
		boolean isDeleteResExist = false;
		
		List<ResourceFileDto> toDeployList = null;
		List<ResourceFileDto> toDeployDeleteList = null;
		
		
		if(resourceDeploy.getRESOURCE_CON().equals("C")){  //선택배포
			
			if(resourceDeploy.getRESOURCE_LIST() != null && resourceDeploy.getRESOURCE_LIST().size() > 0){
				isAddResExist = true;
			}
			
			if(isAddResExist){
				toDeployList = resourceManageService.selectResourceListByList( resourceDeploy.getRESOURCE_LIST() );
			}
			
		} else if( resourceDeploy.getRESOURCE_CON().equals("A") ){       //전체배포
			ResourceFileDto resourceFileOne = new ResourceFileDto();
			resourceFileOne.setSVC_ID( resourceDeploy.getSVC_ID() );
			toDeployList = resourceManageService.SelectResourceAll( resourceFileOne);
			
			if(toDeployList.size() > 0){
				isAddResExist = true;
			}
		} else if( resourceDeploy.getRESOURCE_CON().equals("D") ){      //삭제배포
			
			if(resourceDeploy.getRESOURCE_LIST() != null && resourceDeploy.getRESOURCE_LIST().size() > 0){
				isDeleteResExist = true;
			}
			
			if(isDeleteResExist){
				toDeployDeleteList = resourceManageService.selectResourceListByList( resourceDeploy.getRESOURCE_LIST() );
			}
		} else if( resourceDeploy.getRESOURCE_CON().equals("S") ){           //조회항목배포
			ResourceFileDto resourceFileOne = new ResourceFileDto();
			resourceFileOne.setSVC_ID( resourceDeploy.getSVC_ID() );
			
			resourceDeploy.setRESOURCE_LIST( new ArrayList <String> () );
			
			toDeployList = resourceManageService.SelectResourceSearch( resourceDeploy);
			
			if(toDeployList.size() > 0){
				isAddResExist = true;
			}
		}
		
		int count=0;
		File resRoot = new File( resourceManageService.getResourceServiceDir( resourceDeploy.getSVC_ID() ));
		
		if(isAddResExist){
			for (ResourceFileDto dto : toDeployList) {
				// 선택된 파일 모두 저장 한다. 파일경로, 삭제여부
				HashMap<String, Object> resSelectTargetFile = new HashMap<String, Object>();
	    		String resRelativePath = ZipCompressUtil.toPath(resRoot, new File(dto.getFILE_PATH()));	//www/html/xxx.html 같은 상대경로 리턴
	    		resTargetFileMap.put(resRelativePath, StringUtils.defaultIfEmpty( resourceDeploy.getDEPLOY_FULL_ENC() ,"N"));	//암호화여부
	    		
//	    		resSelectTargetFile.put("FILE_PATH", dto.getFILE_PATH());
	    		resSelectTargetFile.put("FILE_PATH", resRelativePath);
	    		resSelectTargetFile.put("FILE_SIZE", dto.getFILE_SIZE());
	    		resSelectTargetFile.put("DEL_FLAG",  "N");
	    		resSelectTargetFile.put("RSC_ID", dto.getRSC_ID());
                resSelectTargetFile.put("ABS_FILE_PATH",dto.getFILE_PATH());
	    		resSelectTargetFileList.add(resSelectTargetFile);
	    		
	    		// 초기 파일 사이즈 저장, 다음 파일 사이즈 누적
				if (count == 0 || count != (toDeployList.size()-1)) {
	    			resTotalFileSize+=dto.getFILE_SIZE();
				}
				
				if( resourceDeploy.getRESOURCE_CON().equals("S") ){
					resourceDeploy.getRESOURCE_LIST().add( dto.getRSC_FILE_IDX() + "" );
				}
				
				// 설정 파일 사이즈 보다 크거나 마지막 이면 압축할 대상 파일을 압축 파일별 목록에 저장
				if((resTotalFileSize >= COMPRESS_PARTITION_SIZE) ||
						(resTotalFileSize < COMPRESS_PARTITION_SIZE && count== toDeployList.size()-1)){
					
					// 1. 파일 사이즈 초기화
					resTotalFileSize = 0;
					// 2. 압축 파일 단위 파일로 구분
					HashMap<String, String> resFileTempMap = (HashMap<String, String>)resTargetFileMap.clone();
					// 3. 압축할 대상 리소스 파일 목록에 저장
					resTargetPartFileList.add(resFileTempMap);
					// 4. 클리어
					resTargetFileMap.clear();
				}
				count++;
			}
		}
		
		if(isDeleteResExist){
			for (ResourceFileDto dto : toDeployDeleteList) {
				// 선택된 파일 모두 저장 한다. 파일경로, 삭제여부
//                System.out.println("#####################dto.getFILE_PATH():"+dto.getFILE_PATH());
				HashMap<String, Object> resSelectTargetFile = new HashMap<String, Object>();
//				resSelectTargetFile.put("FILE_PATH", dto.getFILE_PATH());
				String resRelativePath = ZipCompressUtil.toPath(resRoot, new File(dto.getFILE_PATH()));	//www/html/xxx.html 같은 상대경로 리턴
				resSelectTargetFile.put("FILE_PATH", resRelativePath);
	    		resSelectTargetFile.put("FILE_SIZE", dto.getFILE_SIZE());
	    		resSelectTargetFile.put("DEL_FLAG",  "Y");
	    		resSelectTargetFile.put("RSC_ID", dto.getRSC_ID());
                resSelectTargetFile.put("ABS_FILE_PATH",dto.getFILE_PATH());
	    		resSelectTargetFileList.add(resSelectTargetFile);
			}
		}
				
		resourceDeploy.setUSE_YN("N");
		resourceDeploy.setDPLY_FULL_YN("N");
		resourceDeploy.setREF_VER("");
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");	//관리자 ID
		resourceDeploy.setREG_ID(S_ID_USER);

        Map<String, Object> map = new HashMap<String,Object>();

		try {
            map = resourceManageService.deployFile(resourceDeploy, resTargetPartFileList, resSelectTargetFileList, resourceDeploy.getSVC_ID(), appId);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		
		String mapRet = String.valueOf(map.get("msg"));
		Pattern p = Pattern.compile("exception", Pattern.CASE_INSENSITIVE);
		
		Matcher m = p.matcher(mapRet);
		if (m.find( )) {
		
			  map.put("msg",messageSource.getMessage("service.common.deployFail", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
			 //map.put("msg", "오류가 발생하였습니다.");
	         //System.out.println("Found value: " + m.group(0) );
	         //System.out.println("Org value    : " + a );
	         //System.out.println("Changed value: " + m.replaceAll("") );
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	@ResponseBody
	@RequestMapping(value="resourceManage/delete",method=RequestMethod.POST)
	public String resourceMangeDelete( Model model , ResourceDeployDto resourceDeploy) throws JsonGenerationException, JsonMappingException, IOException{
		int result = -1;
		
		try {
			result = resourceManageService.resourceManageDelete(resourceDeploy.getRESOURCE_LIST());
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
			
		HashMap<String, Object> map = new HashMap<String,Object>();
			
		if(result == -1){
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
	
	//리소스 상세정보(클릭시 상세정보 보기)
	@RequestMapping(value="resourceManage/info",method=RequestMethod.GET)
	public String resourceDetailPop( Model model, ResourceDeployDto resourceDeploy) throws Exception{
		
		//기본정보
		List<ResourceFileDto> basicInfo = resourceManageService.seleSctResourceListByRSCID(resourceDeploy);
		
		ResourceFileDto basicDto = null;
		if(basicInfo != null){
			 basicDto = basicInfo.get(0);
		}
		
		//배포등의 정보
		List<ResourceFileDetailDto> detailInfo = resourceManageService.selectRSC_FileInfo(resourceDeploy);
		
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("basicDto",basicDto);
		model.addAttribute("detailInfo",detailInfo);
		
		return "admin/mobile/resourceManage/resourceDetailPop";
	}
}
