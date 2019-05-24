package kr.msp.admin.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ZipCompressUtil;
import kr.msp.admin.base.utils.ftp.FILETranceUtils;
import kr.msp.admin.base.utils.ftp.FTPClientUtils;
import kr.msp.admin.base.utils.ftp.IFTPClientUtils;
import kr.msp.admin.base.utils.ftp.SFTPClientUtils;
import kr.msp.admin.base.utils.tree.TreeUtil;
import kr.msp.admin.mobile.dto.DeployListDto;
import kr.msp.admin.mobile.dto.DeployZipDto;
import kr.msp.admin.mobile.dto.MobServiceDto;
import kr.msp.admin.mobile.dto.ResDeployDetailDto;
import kr.msp.admin.mobile.dto.ResDeployDto;
import kr.msp.admin.mobile.dto.ResourceFileDetailDto;
import kr.msp.admin.mobile.dto.ResourceFileDto;
import kr.msp.admin.mobile.resourceManage.dto.ResourceGoMenuDto;
import kr.msp.admin.mobile.resourceManage.dto.ResourcesFtpDto;
import kr.msp.admin.mobile.system.ResourceManager;


/**
 * @info : 모피어스 어드민 관리(모바일관리 - 리소스관리)
 * @author 장문규
 */
@Controller
public class ResourceController {
    @Qualifier("mspXmlConfiguration")
    @Autowired(required = true) 
    private Configuration mspConfig;

    private @Value("${common.dir.resource}") String DEFAULT_RESOURCE_DIRECTORY;	//기본 리소스 디렉토리(msp.xml)에 정의된 정보 가져옴
    private @Value("${webplatform.local.use:false}") String WEBPLATFORM_LOCAL_USE_FLAG; //웹플렛폼 local 사용 여부
    private @Value("${webplatform.ftp.use:false}") String WEBPLATFORM_FTP_USE_FLAG; //웹플렛폼 FTP 사용 여부
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
	
	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int defaultRowCnt;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int defaultPageCnt;
	
	@Autowired(required=true)
	protected ResourceManager manager;
	
	@Autowired(required = true) 
	private MessageSource messageSource;

	//모바일 서비스 목록조회
	public List<MobServiceDto> getMobileService(String userid){
		return manager.getMobileService(userid);
	}
	
	//TODO 개발중 메인화면 완료후 제거!!
	@RequestMapping("admin/mobile")
	public String developTemp( Model model ){
		logger.info("개발 메인화면 진입");
		logger.info("DEFAULT_RESOURCE_DIRECTORY : " + DEFAULT_RESOURCE_DIRECTORY);
		return "admin/mobile/devTemp";
	}
	
	//리소스 배포화면 - 서비스 리스트 조회
	@RequestMapping("admin/mobile/rsc/upload")
	public String resourceUploadService( Model model,HttpServletRequest request,@ModelAttribute("SVC_ID") String SVC_ID) 
	{
		logger.info("\n --- resourceUploadService Enter ---");
		logger.info("- request param -");
		logger.info("SVC_ID : " + SVC_ID);
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		logger.info("S_ID_USER : " + S_ID_USER);
		
		//service 목록만 조회하여 리턴
		List<MobServiceDto> serviceList = getMobileService(S_ID_USER);
		model.addAttribute("serviceList", serviceList);
		
		return "admin/mobile/resourceUpload";
	}
	
	//리소스 삭제용도 popup - 리소스 전체조회
	@RequestMapping("admin/mobile/rsc/uploadPop")
	public String resourceUploadDelPopup( Model model, HttpServletRequest request,@ModelAttribute("SVC_ID") String SVC_ID) throws Exception
	{
		logger.info("\n --- resourceUploadDelPopup Enter ---");
		logger.info("- request param -");
		logger.info("SVC_ID : " + SVC_ID);
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		logger.info("S_ID_USER : " + S_ID_USER);
		
		List<ResourceFileDto> resourceList = manager.selectResourceListByService(SVC_ID);
		
		model.addAttribute("resTot", TreeUtil.resourceListToTree(resourceList , DEFAULT_RESOURCE_DIRECTORY));
		
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/mobile/ajax/resourceUploadPop";
	}
	

	//리소스 신규목록 - 최근 업로드된 리소스 조회
	@RequestMapping("admin/mobile/rsc/uploadNewResourceList")
	public String newResourceList( Model model, HttpServletRequest request,ResourceFileDto dto) throws Exception
	{
		logger.info("\n --- resourceList Enter ---");
		logger.info("PAGE_NUM :" + dto.getPAGE_NUM());
		logger.info("SVC_ID :" + dto.getSVC_ID());
		
		//업로드한 리소스들 목록 리턴
		dto.setSTS_CD("W");
		dto.setPAGE_NUM(dto.getPAGE_NUM());
		dto.setPAGE_SIZE(defaultRowCnt);
		List<ResourceFileDto> newResourceList = manager.selectResourceListByStatus(dto);
		
		model.addAttribute("newResourceList",newResourceList);
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("R_PAGE_NUM",dto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",defaultRowCnt);
		model.addAttribute("R_PAGE_SIZE",defaultPageCnt);
		
		return "admin/mobile/ajax/resourceUploadDetail";
	}
	
	
	
	
	//리소스 배포화면 - 다음 버전조회
	@RequestMapping("admin/mobile/rsc/deploy")
	public String resourceDeployDPLY_VER( Model model,HttpServletRequest request,
			@ModelAttribute("SVC_ID") String SVC_ID,
			@ModelAttribute("SVC_NM") String SVC_NM,
			@ModelAttribute("DEPLOY_ADD_LIST") ArrayList<String> DEPLOY_ADD_LIST,
			@ModelAttribute("DEPLOY_DEL_LIST") ArrayList<String> DEPLOY_DEL_LIST) 
	{
		logger.info("\n --- resourceDeployDPLY_VER Enter ---");
		logger.info("- request param -");
		logger.info("SVC_ID : " + SVC_ID);
		logger.info("SVC_NM : " + SVC_NM);
		logger.info("DEPLOY_ADD_LIST : " + DEPLOY_ADD_LIST);
		logger.info("DEPLOY_DEL_LIST : " + DEPLOY_DEL_LIST);
		
		String DPLY_VER = manager.selectDeployNextVersion(SVC_ID);
		DPLY_VER = StringUtils.leftPad(DPLY_VER, 6, "0");
		System.out.println("DPLY_VER : "+ DPLY_VER);
		
		model.addAttribute("DPLY_VER",DPLY_VER);
		model.addAttribute("SVC_ID",SVC_ID);
		model.addAttribute("SVC_NM",SVC_NM);
		model.addAttribute("DEPLOY_ADD_LIST",DEPLOY_ADD_LIST);
		model.addAttribute("DEPLOY_DEL_LIST",DEPLOY_DEL_LIST);
		
		return "admin/mobile/resourceDeploy";
	}
	
	//배포 실행 
	private @Value("${admin.resource.compress.partition.size}") long COMPRESS_PARTITION_SIZE;
	@RequestMapping("admin/mobile/rsc/deploy/doprocess")
	public String resourceDeployProcess( Model model,HttpServletRequest request,
			@ModelAttribute("SVC_ID") String SVC_ID,
			@ModelAttribute("DEPLOY_FULL_ENC") String DEPLOY_FULL_ENC,
			@ModelAttribute("DPLY_VER") String DPLY_VER,
			@ModelAttribute("DPLY_TP") String DPLY_TP,
			@ModelAttribute("DPLY_NM") String DPLY_NM,
			@ModelAttribute("DPLY_DESC") String DPLY_DESC,
			@ModelAttribute("DEPLOY_ADD_LIST") ArrayList<String> DEPLOY_ADD_LIST,
			@ModelAttribute("DEPLOY_DEL_LIST") ArrayList<String> DEPLOY_DEL_LIST) throws Exception{
		
		logger.info("\n --- resourceDeployProcess Enter ---");
		logger.info("- request param -");
		logger.info("SVC_ID : " + SVC_ID);
		logger.info("DEPLOY_FULL_ENC : " + DEPLOY_FULL_ENC);	//암호화여부
		logger.info("DPLY_VER : " + DPLY_VER);										//버전
		logger.info("DPLY_TP : " + DPLY_TP);					//배포타입
		logger.info("DPLY_NM : " + DPLY_NM);				//제목
		logger.info("DPLY_DESC : " + DPLY_DESC);										//설명
		logger.info("DEPLOY_ADD_LIST : " + DEPLOY_ADD_LIST);	//배포할 리스트
		logger.info("DEPLOY_DEL_LIST : " + DEPLOY_DEL_LIST);		//삭제할 리스트
		
		//추가... client에서 paging
		ResourceFileDto dplyAddDto = new ResourceFileDto();
		dplyAddDto.setSVC_ID(SVC_ID);
		dplyAddDto.setSTS_CD("W");
		dplyAddDto.setPAGE_NUM(1);
		dplyAddDto.setPAGE_SIZE(Integer.MAX_VALUE);
		List<ResourceFileDto> newResourceList = manager.selectResourceListByStatus(dplyAddDto);
		
		ArrayList<String> newDEPLOY_ADD_LIST = new ArrayList<String>(); 
		for (int i = 0; i < newResourceList.size(); i++) {
			newDEPLOY_ADD_LIST.add(Long.toString(newResourceList.get(i).getRSC_ID()));
		}
		
		DEPLOY_ADD_LIST = newDEPLOY_ADD_LIST;
		
		//init Variables
		HashMap<String, String> resTargetFileMap = new HashMap<String, String>();
    	List<HashMap<String, Object>> resSelectTargetFileList = new ArrayList<HashMap<String, Object>>(); // 선택된 리소스 파일(배포, 삭제)
    	List<HashMap<String, String>> resTargetPartFileList = new ArrayList<HashMap<String, String>>(); // 리소스 배포 분할 파일 저장(이 list의 size가 zip파일 갯수)
    	long resTotalFileSize = 0; // 배포 파일을 분할 하기위한 파일 사이즈
		
		// serviceid로 appid를 조회한다.
    	String appId = manager.selectAppid(SVC_ID);
    	
		//DEPLOY_ADD_LIST 와 DEPLOY_DEL_LIST size가 0이면 에러
		if((DEPLOY_ADD_LIST==null || DEPLOY_ADD_LIST.size()==0) && (DEPLOY_DEL_LIST==null || DEPLOY_DEL_LIST.size()==0)){
			logger.error("ERROR :  DEPLOY_ADD_LIST & DEPLOY_DEL_LIST is null or 0 size");
			model.addAttribute("rtnCd","999");
			model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.noResource", null, LocaleContextHolder.getLocale()));  //배포할 리소스가 하나도 없습니다.
			
			return resourceUploadService(model, request, SVC_ID);	//리소스 업로드 페이지로 이동
		}
		
		//Add하려는 목록과 Del하려는 목록중 겹치는게 있을때
		boolean isAddResExist = false;
		boolean isDeleteResExist = false;
		if(DEPLOY_DEL_LIST != null && DEPLOY_DEL_LIST.size() > 0){
			DEPLOY_ADD_LIST.removeAll(DEPLOY_DEL_LIST);
			isDeleteResExist = true;
		}
		
		if(DEPLOY_ADD_LIST!=null && DEPLOY_ADD_LIST.size() >0 ){
			isAddResExist = true;
		}
		//리소스 배포할 목록을 조회해옴
		List<ResourceFileDto> toDeployList = null; 
		if(isAddResExist){
			toDeployList = manager.selectResourceListByList(DEPLOY_ADD_LIST);
		}
		
		//지울 리소스 있으면 목록조회
		List<ResourceFileDto> toDeployDeleteList = null;
		if(isDeleteResExist){
			toDeployDeleteList = manager.selectResourceListByList(DEPLOY_DEL_LIST);
		}
		
		//참고 [기존소스- file1개당] : 0:path.1:암호화여부,2:size,3:삭제여부
		
		//배포할 리소스만큼 loop
		int count=0;
		File resRoot = new File(manager.getResourceServiceDir(SVC_ID));
		
		if(isAddResExist){
			for (ResourceFileDto dto : toDeployList) {
				// 선택된 파일 모두 저장 한다. 파일경로, 삭제여부
				HashMap<String, Object> resSelectTargetFile = new HashMap<String, Object>();
	    		String resRelativePath = ZipCompressUtil.toPath(resRoot, new File(dto.getFILE_PATH()));	//www/html/xxx.html 같은 상대경로 리턴
	    		resTargetFileMap.put(resRelativePath, StringUtils.defaultIfEmpty(DEPLOY_FULL_ENC,"N"));	//암호화여부
	    		
//	    		resSelectTargetFile.put("FILE_PATH", dto.getFILE_PATH());
	    		resSelectTargetFile.put("FILE_PATH", resRelativePath);
	    		resSelectTargetFile.put("FILE_SIZE", dto.getFILE_SIZE());
	    		resSelectTargetFile.put("DEL_FLAG",  "N");
	    		resSelectTargetFileList.add(resSelectTargetFile);
	    		
	    		// 초기 파일 사이즈 저장, 다음 파일 사이즈 누적
				if (count == 0 || count != (toDeployList.size()-1)) {
	    			resTotalFileSize+=dto.getFILE_SIZE();
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
		}//end: isAddResExist true
	
		//삭제할 리소스만큼 loop
		if(isDeleteResExist){
			for (ResourceFileDto dto : toDeployDeleteList) {
				// 선택된 파일 모두 저장 한다. 파일경로, 삭제여부
				HashMap<String, Object> resSelectTargetFile = new HashMap<String, Object>();
//				resSelectTargetFile.put("FILE_PATH", dto.getFILE_PATH());
				String resRelativePath = ZipCompressUtil.toPath(resRoot, new File(dto.getFILE_PATH()));	//www/html/xxx.html 같은 상대경로 리턴
				resSelectTargetFile.put("FILE_PATH", resRelativePath);
	    		resSelectTargetFile.put("FILE_SIZE", dto.getFILE_SIZE());
	    		resSelectTargetFile.put("DEL_FLAG",  "Y");
	    		resSelectTargetFileList.add(resSelectTargetFile);
			}
		}//end: isDeleteResExist true
		
		ResDeployDto resDeployDto = new ResDeployDto();
		
		resDeployDto.setDPLY_NM(DPLY_NM);
		resDeployDto.setDPLY_VER(DPLY_VER);
		resDeployDto.setDPLY_DESC(DPLY_DESC);
		resDeployDto.setDPLY_TP(DPLY_TP);
		resDeployDto.setUSE_YN("N");
		resDeployDto.setSVC_ID(SVC_ID);
		resDeployDto.setDPLY_FULL_YN("N");
		resDeployDto.setREF_VER("");
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");	//관리자 ID
		resDeployDto.setREG_ID(S_ID_USER);
		
		
		manager.deployFile(resDeployDto, resTargetPartFileList, resSelectTargetFileList, SVC_ID, appId);

		return "";//deployList(model, request, SVC_ID);
	}
	
	@RequestMapping("admin/mobile/rsc/deployListMenu")
	public String deployListMenu( Model model, HttpServletRequest request
			, @ModelAttribute("SVC_ID") String SVC_ID
			, @ModelAttribute("NUM") String NUM
			, @ModelAttribute("SIZE") String SIZE 
			, ResourceGoMenuDto resourceGoMenu) throws Exception
	{
		
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		request.getSession().setAttribute("S_ID_LEFT_MENU",resourceGoMenu.getMENU_GO());
		
		//service 목록만 조회하여 리턴
		List<MobServiceDto> serviceList = getMobileService(S_ID_USER);
		
		String CON = "W";
		if(NUM != null && !NUM.equals("")){
			CON = "R";
		}
		
		model.addAttribute("R_SVC_ID", resourceGoMenu.getSVC_ID() );
		model.addAttribute("SVC_ID", SVC_ID);
		model.addAttribute("NUM", NUM);
		model.addAttribute("CON", CON);
		model.addAttribute("SIZE", SIZE);
		model.addAttribute("serviceList", serviceList);
		
		return "admin/mobile/resourceDeployList";
	}
			
	//배포 목록조회
	@RequestMapping("admin/mobile/rsc/deployList")
	public String deployList( Model model, HttpServletRequest request
			, @ModelAttribute("SVC_ID") String SVC_ID
			, @ModelAttribute("NUM") String NUM
			, @ModelAttribute("SIZE") String SIZE ) throws Exception
	{
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		
		//service 목록만 조회하여 리턴
		List<MobServiceDto> serviceList = getMobileService(S_ID_USER);
		
		String CON = "W";
		if(NUM != null && !NUM.equals("")){
			CON = "R";
		}
		
		model.addAttribute("SVC_ID", SVC_ID);
		model.addAttribute("NUM", NUM);
		model.addAttribute("CON", CON);
		model.addAttribute("SIZE", SIZE);
		model.addAttribute("serviceList", serviceList);
		
		return "admin/mobile/resourceDeployList";
	}
	
	//배포 목록조회 (ajax페이지용)
	@RequestMapping("admin/mobile/rsc/ajax/deployList")
	public String deployList( Model model ,DeployListDto dto , HttpServletRequest request){
		
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		dto.setUSER_ID(S_ID_USER);
		
		List<DeployListDto> deployList =  manager.selectDeployList(dto);
		
		model.addAttribute("deployList",deployList);
		model.addAttribute("SVC_ID", dto.getSVC_ID());
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("R_PAGE_NUM",dto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",dto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",defaultPageCnt);
		
		return "admin/mobile/ajax/deployList";
		
	}
	
	//배포 상세조회
	@RequestMapping("admin/mobile/rsc/deploy/deployDetail")
	public String deployDetail( Model model, DeployListDto listDto 
			, @ModelAttribute("NUM") String NUM
			, @ModelAttribute("SIZE") String SIZE ) throws Exception{
		logger.info(" -- Enter deployDetail -- ");
		logger.info("DPLY_IDX : " + listDto.getDPLY_IDX());
		logger.info("SVC_ID : " + listDto.getSVC_ID());
		logger.info("SVC_NM : " + listDto.getSVC_NM());
		logger.info("REG_DTTM : " + listDto.getREG_DTTM());
		logger.info("DPLY_NM : " + listDto.getDPLY_NM());
		logger.info("DPLY_VER : " + listDto.getDPLY_VER());
		logger.info("FILE_NAME : " + listDto.getFILE_NAME());
		logger.info("FILE_PATH : " + listDto.getFILE_PATH());
		logger.info("DPLY_TP : " + listDto.getDPLY_TP());
		logger.info("USE_YN : " + listDto.getUSE_YN());
		logger.info("DPLY_DESC : " + listDto.getDPLY_DESC());
		logger.info("PAGE_NUM : " + listDto.getPAGE_NUM());
        logger.info("FILE_IDX : " + listDto.getFILE_IDX());
		
		model.addAttribute("DPLY_IDX",listDto.getDPLY_IDX());
		model.addAttribute("SVC_ID",listDto.getSVC_ID());
		model.addAttribute("SVC_NM",listDto.getSVC_NM());
		model.addAttribute("REG_DTTM",listDto.getREG_DTTM());
		model.addAttribute("DPLY_NM",listDto.getDPLY_NM());
		model.addAttribute("DPLY_VER",listDto.getDPLY_VER());
        model.addAttribute("FILE_IDX",listDto.getFILE_IDX());
		model.addAttribute("FILE_NAME",listDto.getFILE_NAME());
		model.addAttribute("FILE_PATH",listDto.getFILE_PATH());
		model.addAttribute("DPLY_TP",listDto.getDPLY_TP());
		model.addAttribute("USE_YN",listDto.getUSE_YN());
		model.addAttribute("DPLY_DESC",listDto.getDPLY_DESC());
		model.addAttribute("NUM",NUM);
		model.addAttribute("SIZE",SIZE);
		
		int pageNum = listDto.getPAGE_NUM();
		boolean isFirstEnter = false;	//최초진입여부
		if(pageNum == 0){
			isFirstEnter = true;
			pageNum =1;
		}
		
		//tree구현을 위해 paging 없이 진행
/*		List<ResDeployDetailDto> detailListForTree = null;
		{
			DeployListDto treeDto = listDto;
			treeDto.setPAGE_NUM(1);
			treeDto.setPAGE_SIZE(Integer.MAX_VALUE);
			detailListForTree = manager.selectDeployDetailList(treeDto);
		}
		*/
		listDto.setPAGE_NUM(pageNum);
		listDto.setPAGE_SIZE(defaultRowCnt);
		
		//DPLY_IDX로 TBL_RES_DEPLOY_DETAIL 찾기
		List<ResDeployDetailDto> detailList = manager.selectDeployDetailList(listDto);
		
		model.addAttribute("detailList",detailList);
//		model.addAttribute("detailListTree",TreeUtil.resourceListToTree(detailListForTree, DEFAULT_RESOURCE_DIRECTORY));
		model.addAttribute("R_PAGE_NUM",listDto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",defaultRowCnt);
		model.addAttribute("R_PAGE_SIZE",defaultPageCnt);
		
		//페이징용 ajax페이지로 return
		if(!isFirstEnter){
			model.addAttribute("layout", "layout/null.vm");
			return "admin/mobile/ajax/deployDetailList";
		}
		
		return "admin/mobile/deployDetail";
		
	}
	
	//배포정보 수정
	@ResponseBody
	@RequestMapping("admin/mobile/rsc/deploy/deployUpdate")
	public String deployUpdate( Model model, DeployListDto listDto) throws Exception{

        //한글깨짐 문제때문에 처리
        listDto.setDPLY_DESC(URLDecoder.decode(listDto.getDPLY_DESC(), "UTF-8"));

		int result = 0;
        String resultMsg = messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()); //"수정에 실패했습니다.";
		
		try{
			result = manager.updateDeploy(listDto);
            if(result>0) {
                resultMsg = messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()); //수정 되었습니다.
            }
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		//웹플랫폼 동기화를 위하여 msp.xml에 설정한 웹플랫폼 배포 디렉토리를 전체 삭제 후 다시 업로드
        boolean webplatformLocalUseFlag = BooleanUtils.toBoolean(WEBPLATFORM_LOCAL_USE_FLAG);
        boolean webplatformFtpUseFlag = BooleanUtils.toBoolean(WEBPLATFORM_FTP_USE_FLAG);

        if(listDto.getDPLY_TP().equals(listDto.getORG_DPLY_TP()) && listDto.getUSE_YN().equals(listDto.getORG_USE_YN())){ //사용상태와 배포모드가 변경된게 없으면 재 배포할 필요없다.
            webplatformLocalUseFlag = false;
        }
        if(webplatformLocalUseFlag){
//            System.out.println("##########웹플랫폼 배포 Start~~~!");
            FILETranceUtils fileTranceUtils = new FILETranceUtils();
            List<ResourcesFtpDto> sendResourcesFtpDtos = new ArrayList<ResourcesFtpDto>();
            Map<String,String> chkUploadFileMap = new HashMap<String, String>();

            try {
                String localFileRootSrc = DEFAULT_RESOURCE_DIRECTORY + "update" + File.separator + "upload" + File.separator;
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // 방법1 : 모바일앱에 배포할 압축파일 리스트를 가져와 풀은 다음 FTP 전송 방법
                // 장점: 완료속도는 느리나 복구 기능 구현 가능하고 모바일앱과 배포 소스같음
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                List<DeployZipDto> webPlatformDeployZipList = manager.selectWebPlatformDeployZipList(listDto);
                //
                if (webPlatformDeployZipList != null && webPlatformDeployZipList.size() > 0) {
                    String webPloatFormTempSrc = DEFAULT_RESOURCE_DIRECTORY + "update" + File.separator + "webplateform_temp" + File.separator +listDto.getSVC_ID() + File.separator;
                    File webPloatFormTempDir = new File(webPloatFormTempSrc);
                    //파일 삭제
                    if( webPloatFormTempDir.isDirectory() ){
                        try {
                            fileTranceUtils.deleteDirectory(webPloatFormTempDir);
                        }catch (Exception e){
                            logger.error("Exception caught.", e);
                        }
                    }
                    FileUtils.forceMkdir(webPloatFormTempDir);
                    // 배포할 zip을 Temp 폴더에 압축을 푼다.
                    Map<String,File> unzipMap = new HashMap<String, File>();
                    for(DeployZipDto deployZipDto : webPlatformDeployZipList){
                        if(deployZipDto.getFILE_PATH()!=null && !deployZipDto.getFILE_PATH().equals("")) {
                            File zipFile = new File(deployZipDto.getFILE_PATH());
                            ZipCompressUtil.unzipMap(new FileInputStream(zipFile), webPloatFormTempDir, Charset.defaultCharset().name(), unzipMap);
                        }else{ //삭제 배포일경우
                            Map<String,String> paramMap = new HashMap<String, String>();
                            paramMap.put("SVC_ID",listDto.getSVC_ID());
                            paramMap.put("DPLY_IDX",""+deployZipDto.getDPLY_IDX());
                            List<Map<String,String>> delFiles = manager.getDeployDelFiles(paramMap);
                            if(delFiles!=null){
                                for(Map<String,String> delFileMap : delFiles){
                                    if(unzipMap.containsKey(delFileMap.get("FILE_PATH"))){
                                        unzipMap.remove(delFileMap.get("FILE_PATH"));
                                        try {
                                            FileUtils.forceDelete(new File(webPloatFormTempSrc + delFileMap.get("FILE_PATH")));
                                        }catch (Exception e){
                                            logger.error("Exception caught.", e);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Set<Map.Entry<String,File>> unzipMapSet = unzipMap.entrySet();
                    for(Map.Entry<String,File> me : unzipMapSet){
                        File upzipfile = me.getValue();
                        ResourcesFtpDto resourcesFtpDto = new ResourcesFtpDto();
                        String ftpSendAbsFileSrc = upzipfile.getAbsolutePath();
                        String ftpSendFileName = upzipfile.getName();
                        resourcesFtpDto.setAbsFilePath(ftpSendAbsFileSrc);
                        resourcesFtpDto.setFileName(ftpSendFileName);

                        int startSrcPathLen = webPloatFormTempSrc.length(); //DEFAULT_RESOURCE_DIRECTORY 다음의 폴더경로를 맞추기 위해
                        int endSrcPathLen = ftpSendAbsFileSrc.length() - ftpSendFileName.length();
                        String makePathAndFile = ftpSendAbsFileSrc.substring(startSrcPathLen, endSrcPathLen);
                        makePathAndFile = makePathAndFile.replace(File.separator, "/");
                        resourcesFtpDto.setMakeSubFolderSrc(listDto.getSVC_ID()+"/"+makePathAndFile);
//                        System.out.println("*********** 파일FullName:"+resourcesFtpDto.getAbsFilePath()+"  파일Name:"+resourcesFtpDto.getFileName()+"  만들subFolder:"+resourcesFtpDto.getMakeSubFolderSrc());
                        sendResourcesFtpDtos.add(resourcesFtpDto);
                    }

                }
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // 방법1 마침
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                //방법2: DB에서 배포할 파일 리소트를 수집해서 FTP 전송 방법
                // 장점: 완료속도가 더 빠르나 이전 파일로 복귀할 수 없음. 모바일앱과 배포 소스가 다를 수 있음
                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                List<DeployListDto> webPlatformDeployList = manager.selectWebPlatformDeployList(listDto);
//                //방법2:DB에서 웹플랫폼에 배포할 파일 담는다.
//                if (webPlatformDeployList != null && webPlatformDeployList.size() > 0) {
//
//                    for (int i = 0; i < webPlatformDeployList.size(); i++) {
//
//                        DeployListDto deployListDto = webPlatformDeployList.get(i);
//                        String hiddenFileChk = deployListDto.getFILE_NAME().substring(0,1);
//                        if(hiddenFileChk.equals(".")){ //hidden파일은 전송하지 않는다.
//                            continue;
//                        }
//                        if(!chkUploadFileMap.containsKey(deployListDto.getFILE_PATH())) { //중복되는 파일은 한번만 전송하기 위해
//                            chkUploadFileMap.put(deployListDto.getFILE_PATH(),"Y");
//                            ResourcesFtpDto resourcesFtpDto = new ResourcesFtpDto();
//                            String absFileSrc = localFileRootSrc + listDto.getSVC_ID() + File.separator + deployListDto.getFILE_PATH();
//
//                            resourcesFtpDto.setAbsFilePath(absFileSrc);
//                            resourcesFtpDto.setFileName(deployListDto.getFILE_NAME());
//
//                            int startSrcPathLen = localFileRootSrc.length(); //DEFAULT_RESOURCE_DIRECTORY 다음의 폴더경로를 맞추기 위해
//                            int endSrcPathLen = absFileSrc.length() - deployListDto.getFILE_NAME().length();
//                            String makePathAndFile = absFileSrc.substring(startSrcPathLen, endSrcPathLen);
//                            makePathAndFile = makePathAndFile.replace(File.separator, "/");
//                            resourcesFtpDto.setMakeSubFolderSrc(makePathAndFile);
//
//                            sendResourcesFtpDtos.add(resourcesFtpDto);
//                        }
//                    }
//                }
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // 방법2 마침
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                //물리적 디렉토리가 같은 웹플랫폼 리소스 전송 로직
                if (webplatformLocalUseFlag) {
                	
                    String[] localTargets = mspConfig.getStringArray("webplatform.local.target.list");
                    fileTranceUtils = new FILETranceUtils();
                    for (String localTarget : localTargets) {
                        String deployPath = mspConfig.getString("webplatform.local.target." + localTarget + ".dev_deploypath");
                        if (!listDto.getDPLY_TP().equals("0")) {  //운영모드일경우
                            deployPath = (String) mspConfig.getString("webplatform.local.target." + localTarget + ".real_deploypath");
                        }
                        //웹플랫폼 배포시 웹플랫폼 앱기준 디렉토리 초기화(삭제)
                        File delDirectory = new File(deployPath + listDto.getSVC_ID());
                        fileTranceUtils.deleteDirectory(delDirectory);

//                        //웹플랫폼에 배포
                        if (sendResourcesFtpDtos.size() > 0) { //보낼 파일이 있는 경우
                            fileTranceUtils.putFileToServer(sendResourcesFtpDtos, deployPath);
                        }
                    }
                    result = 1;
                    resultMsg = messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()); //수정 되었습니다.
                }

                //FTP, SFTP를 이용 전송하는 로직
                if(webplatformFtpUseFlag) {
                    String[] lm_sTargets = mspConfig.getStringArray("webplatform.ftp.target.list");
                    for (String lm_sTarget : lm_sTargets) {

                        String lm_sFtpType = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".type");
                        String lm_sHostName = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".hostname");
                        int lm_iPort = NumberUtils.toInt(mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".port"));
                        String lm_sId = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".id");
                        String lm_sPassword = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".password");

                        String lm_sRemotePath = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".dev_remotepath");
                        if (!listDto.getDPLY_TP().equals("0")) {  //운영모드일경우
                            lm_sRemotePath = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".real_remotepath");
                        }

						String lm_privateKey = "";
						String lm_passphrase = "";
						if(lm_sFtpType.equals("sftp2")){
							lm_privateKey = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".privatekey");
							lm_passphrase = mspConfig.getString("webplatform.ftp.target." + lm_sTarget + ".passphrase");
							logger.debug("##### lm_privateKey:"+lm_privateKey+"     lm_passphrase:"+lm_passphrase+"   lm_sRemotePath:"+lm_sRemotePath);
						}

						IFTPClientUtils ftp = null;
						if(lm_sFtpType.equals("sftp")){
							ftp = new SFTPClientUtils();
							ftp.init(lm_sHostName, lm_iPort, lm_sId, lm_sPassword);
						}else if(lm_sFtpType.equals("sftp2")) {
							ftp = new SFTPClientUtils();
							ftp.init(lm_sHostName, lm_iPort, lm_sId, lm_privateKey, lm_passphrase);
						}else if(lm_sFtpType.equals("sftp_enc")) {
							ftp = new SFTPClientUtils();
							ftp.init_enc(lm_sHostName, lm_iPort, lm_sId, lm_sPassword);
						}else if(lm_sFtpType.equals("ftp_enc")) {
							ftp = new FTPClientUtils();
							ftp.init_enc(lm_sHostName, lm_iPort, lm_sId, lm_sPassword);
						}else{
							ftp = new FTPClientUtils();
							ftp.init(lm_sHostName, lm_iPort, lm_sId, lm_sPassword);
						}

                        //웹플랫폼 배포시 웹플랫폼 앱기준 디렉토리 초기화(삭제)
                        String delDirectory = lm_sRemotePath + listDto.getSVC_ID();
                        ftp.deleteDirectory(delDirectory,"");
                        try {
                            if (sendResourcesFtpDtos.size() > 0) { //보낼 파일이 있는 경우
                                ftp.putFileToServer(sendResourcesFtpDtos, lm_sRemotePath);
                            }
                            ftp.disconnection();
                        } catch (SocketException ex) {
                            logger.error("Exception caught.", ex);
                            throw new SocketException("웹플랫폼 FTP 서버[" + lm_sHostName + "] 전송실패 :" + ex.toString());
                        } catch (IOException ex) {
                            logger.error("Exception caught.", ex);
                            throw new SocketException("웹플랫폼 FTP 서버[" + lm_sHostName + "] 전송실패 :" + ex.toString());
                        }
                    }

                    result = 1;
                    resultMsg = messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()); //수정 되었습니다.
                }

            }catch(Exception e){
                logger.error("Exception caught.", e);
                result = 0;
                resultMsg = messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()); //"수정에 실패하였습니다.("+e.toString()+")";
            }
        }

		HashMap<String, Object> map = new HashMap<String,Object>();
		if(result > 0){
			map.put("result", result);
			map.put("msg",resultMsg );
		} else {
			map.put("result", result);
			map.put("msg",resultMsg );
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
	
	//배포정보 삭제
	@ResponseBody
	@RequestMapping("admin/mobile/rsc/deploy/deployDelete")
	public String deployDelete( Model model, HttpServletRequest request, HttpServletResponse response, DeployListDto listDto) throws Exception{
		logger.info(" -- Enter deployDelete -- ");
		logger.info(" DPLY_IDX : " + listDto.getDPLY_IDX());
		logger.info(" SVC_ID : " + listDto.getSVC_ID());
		logger.info(" FILE_PATH : " + listDto.getFILE_PATH());
		
		int fileAttachResult=0;
		int dplydtlResult=0;
		int dplyResult=0;
		boolean fileDel = false;
		try{
			//DB삭제 1. TM_MOB_FILE_ATTACH
			fileAttachResult = manager.deleteFileAttach(listDto);
			//DB삭제 2. TM_MOB_DPLY_DTL
			dplydtlResult = manager.deleteDplyDtl(listDto);
			//DB삭제 3. TM_MOB_DPLY
			dplyResult = manager.deleteDply(listDto);
			
			//실제 물리적 파일 삭제(DB삭제 완료후 마지막에 지움)
			File file = new File(listDto.getFILE_PATH());
			if(file.exists()){
				fileDel = file.delete();
			}
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		int result = 0;
		if(fileAttachResult>0 && dplydtlResult>0 && dplyResult>0 && fileDel){
			result = 1;
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("SVC_ID", listDto.getSVC_ID());
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //삭제 되었습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
		
	}
	
	
	//// [3] 리소스 관리 부분 

	//리소스들의 리스트 조회
	@RequestMapping("admin/mobile/ajax/resourceDetail")
	public String resourceDetail( Model model ,ResourceFileDto dto){
		logger.info(" -- Enter resourceDetail -- ");
		logger.info("PAGE_NUM : " + dto.getPAGE_NUM());
		logger.info("PAGE_SIZE : " + dto.getPAGE_SIZE());
		logger.info("ORDER_TARGET : " + dto.getORDER_TARGET());
		logger.info("ORDER_TYPE : " + dto.getORDER_TYPE());

		List<ResourceFileDto> resourceList = manager.selectResourceList(dto);
		
		logger.info("resourceList size ::: " + resourceList.size());
		model.addAttribute("resourceList", resourceList);
		model.addAttribute("layout", "layout/null.vm");
		
		model.addAttribute("R_PAGE_NUM",dto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",dto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",defaultPageCnt);
		
		return "admin/mobile/ajax/resourceDetail";
		
	}
	
	/*
	 * 리소스 전체 목록 조회, 해당 service의 전체 리소스파일을 조회한다.
	 * 최초 조회
	 */
	@RequestMapping("admin/mobile/rsc")
	public String resourceList( Model model,HttpServletRequest request,ResourceFileDto dto)
	{
		logger.info(" -- Enter resourceList -- ");
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		logger.info("S_ID_USER : " + S_ID_USER);
		String SVC_ID = dto.getSVC_ID();
		logger.info("org SVC_ID : " + SVC_ID);
		
		List<MobServiceDto> serviceList = getMobileService(S_ID_USER);
		
		if(SVC_ID == null || "".equals(SVC_ID.trim())){
			SVC_ID="";
			if(serviceList != null && serviceList.size() > 0){
				SVC_ID = serviceList.get(0).getSVC_ID();
				logger.info("chg serviceid : " + SVC_ID);
			}
		}
		
		dto.setSVC_ID(SVC_ID);
		dto.setPAGE_NUM(1);
		dto.setPAGE_SIZE(defaultRowCnt);
		dto.setORDER_TARGET("RSC_ID");
		dto.setORDER_TYPE("DESC");
		
		List<ResourceFileDto> resourceList = manager.selectResourceList(dto);
		
		model.addAttribute("R_PAGE_NUM",dto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",dto.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",defaultPageCnt);
		model.addAttribute("resourceList", resourceList);
		model.addAttribute("serviceList", serviceList);
		
		return "admin/mobile/resourceList";
	}
	
	//리소스 상세정보(클릭시 상세정보 보기)
	@RequestMapping("admin/mobile/ajax/resourceDetailPop")
	public String resourceDetailPop( Model model, ResourceFileDetailDto dplyDto) throws Exception{
		logger.info(" -- Enter resourceDetailPop -- ");
		logger.info("DPLY_IDX : " + dplyDto.getRSC_ID());
		
		//기본정보
		List<ResourceFileDto> basicInfo = manager.selectResourceListByRSCID(dplyDto);
		
		ResourceFileDto basicDto = null;
		if(basicInfo != null){
			 basicDto = basicInfo.get(0);
		}
		
		//배포등의 정보
		List<ResourceFileDetailDto> detailInfo = manager.selectRSC_FileInfo(dplyDto);
		
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("basicDto",basicDto);
		model.addAttribute("detailInfo",detailInfo);
		
		return "admin/mobile/ajax/resourceDetailPop";
		
	}
	
	@ResponseBody
	//리소스 삭제(TB_MOB_RSC_FILE의 목록파일을 제거)
	@RequestMapping("admin/mobile/rsc/resourceDelete")
	public String resourceDelete( Model model, 	
			HttpServletRequest request,
			@ModelAttribute("SVC_ID") String SVC_ID, 
			@ModelAttribute("RSC_DEL_LIST") ArrayList<String> RSC_DEL_LIST) throws Exception{
		
		logger.info(" -- Enter resourceDelete -- ");
		logger.info("SVC_ID : " + SVC_ID);
		logger.info("RSC_DEL_LIST : " + RSC_DEL_LIST);
		
		int dbResultSum = 0;
		int fileResultSum = 0;
		
		for (int i = 0; i < RSC_DEL_LIST.size(); i++) {
			String sRsc_id = RSC_DEL_LIST.get(i);
			
			//1. 파일삭제
			ResourceFileDetailDto dto = new ResourceFileDetailDto();
			dto.setRSC_ID(Long.parseLong(sRsc_id));
			 List<ResourceFileDto> tempList = manager.selectResourceListByRSCID(dto);
			 if(tempList != null && tempList.size() > 0 ){
				 String file_path = tempList.get(0).getFILE_PATH();
				 File file = new File(file_path);
				 boolean delResult = file.delete();
				 logger.info("rscid: "+  sRsc_id + "  / file delete result : " + delResult);
				 fileResultSum ++;
			 }
			 
			 //2. DB삭제
			 int result = manager.deleteRSC_FILE(dto);
			 logger.info("result : " + result);
			 dbResultSum += result;
		}
		
		int result = 0;
		if(dbResultSum == RSC_DEL_LIST.size() && dbResultSum == fileResultSum){
			result = 1;
		}
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("SVC_ID", SVC_ID);
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //삭제 되었습니다.
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //삭제에 실패했습니다.
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
		
	}
	
}
