/**
 * 앱스토어 APP 정보를  관리 하는 콘트롤러
 */
package kr.msp.admin.store;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.base.utils.ftp.FTPClientManager;
import kr.msp.admin.base.utils.ftp.FTPClientUtils;
import kr.msp.admin.base.utils.ftp.IFTPClientUtils;
import kr.msp.admin.base.utils.ftp.SFTPClientUtils;
import kr.msp.admin.store.appInfo.dto.AppImageDto;
import kr.msp.admin.store.appInfo.dto.AppInfoDto;
import kr.msp.admin.store.appInfo.dto.AppInfoSearchDto;
import kr.msp.admin.store.appInfo.dto.AppPlatInfoDto;
import kr.msp.admin.store.appInfo.dto.BinaryDto;
import kr.msp.admin.store.appInfo.dto.IOSPlistInfo;
import kr.msp.admin.store.appInfo.dto.MultiFileDto;
import kr.msp.admin.store.appInfo.service.AppInfoService;
import kr.msp.admin.store.category.dto.CategoryDto;
import kr.msp.admin.store.category.dto.CategorySearchDto;
import kr.msp.admin.store.category.service.StoreCategoryService;
import kr.msp.admin.store.device.dto.DeviceDto;
import kr.msp.admin.store.device.dto.PlatformDto;
import kr.msp.admin.store.device.service.StoreDeviceService;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


/**<pre>
 *  앱정보 등록/수정/삭제를 처리담당한다.
 * </pre>
 * @author UncleJoe
 * @since 2014. 5. 13.
 */
@Controller
public class StoreAppInfoController extends ControllerUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**<pre>
     * Store에서 binary, image,icon등을 서비스하기 위한 Root URL
     * Admin URL을 설정할 수도 있으나 G/W를 설정해야 하는 경우& 제3의 URL 을 설정해서 서비스하기도 함.
     * G/W 의 msp.xml에도 동일한 설정을 유지해야 한다.
     * </pre>
     */
    @Value("${store.service.url}")
    private String STORE_SERVICE_URL;

    /**
     * STORE_SERVICE_URL 뒤에 올 image/icon root 경로
     */
    @Value("${store.path.img}")
    private String IMAGE_PATH ;
    /**
     * STORE_SERVICE_URL 뒤에 올 binary(apk,plist, ipa) 파일의 root 경로
     */
    @Value("${store.path.bin}")
    private String BINARY_PATH ;

    /**
     * binary,image파일을 업로드 하기 위한 물리적인 절대 경로
     */
    @Value("${store.upload.absolute_path}")
    private String ABSOLUTE_PATH ;

    /**<pre>
     * FTP로 리소스파일 sync 여부.
     * 스토어 서버가 이중화 또는 다중으로 분산되어 있을 경우
     * 업로드 되는 binary, image를 서버끼리 Sync 하기 위한 옵션.
     * NAS, SAN등의 storage를 사용하지 않는 환경에서 사용한다.
     * WAS1, WAS2 양쪽 모두에 FTP(SFTP)가 세팅되어 있어야 한다.
     * </pre>
     */
    @Value("${storeserver.resource.sync:false}")
    private String FTP_USE_FLAG;

    @Autowired(required=true)
    private DataSourceTransactionManager transactionManager;

    @Autowired(required=true)
    protected AppInfoService appInfoService;

    @Autowired(required=true)
    protected StoreDeviceService deviceService;

    @Autowired(required=true)
    protected StoreCategoryService storeCategoryService;
    
    @Autowired(required = true) 
	private MessageSource messageSource; 

//    @Autowired(required=true)
//    private CommonsConfigurationFactoryBean mspConfig;

    @Autowired(required = true)
    @Qualifier("mspXmlConfiguration")
    private Configuration mspConfig;


    private final String TYPE_ANDROID = "10";
    private final String TYPE_IOS 	  = "20";


    /**
     * App 정보관리 목록
     * @param model
     * @param request
     * @param dto
     * @return
     * @since 2014. 5. 9. by 이명보
     */
    @RequestMapping(value="/admin/store/appInfo", method=RequestMethod.GET)
    public String appMain(Model model, HttpServletRequest request, AppInfoSearchDto dto){


        String rtnCd = ServletRequestUtils.getStringParameter(request, "rtnCd","empty");

        dto.setPAGE_NUM(1);
        dto.setPAGE_SIZE(row_size);
        dto.setORDER_TARGET("APP_IDX");
        dto.setORDER_TYPE("DESC");

        model.addAttribute("platformList", appInfoService.selectBoxPlatformList());
        model.addAttribute("appInfoList", appInfoService.selectAppInfoList(dto));
        setPager(model, 1);

        if("insertOk".equals(rtnCd)){
            model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
        }else if("insertFail".equals(rtnCd)){
            model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
        }else if("updateOk".equals(rtnCd)){
            model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
        }else if("updateFail".equals(rtnCd)){
            model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
        }else if("ftpFail".equals(rtnCd)){
            model.addAttribute("rtnMsg", "FTP "+messageSource.getMessage("menu.store.controller.sendFail", null, LocaleContextHolder.getLocale())); //전송에 실패하였습니다.
        }else if("".equals(rtnCd)){
            model.addAttribute("rtnMsg",messageSource.getMessage("menu.store.controller.fileSendFail", null, LocaleContextHolder.getLocale()) ); //파일등록에 실패하였습니다.
        }

        return "admin/store/appInfo/appInfoMain";
    }

    //메인페이지(POST) - 테이블내용만.
    @RequestMapping(value="/admin/store/appInfo", method=RequestMethod.POST)
    public String appMainPost(Model model, AppInfoSearchDto dto){

        logger.debug("#################################" + this.getClass().getName());

        dto.setPAGE_SIZE(row_size);

        model.addAttribute("appInfoList", appInfoService.selectAppInfoList(dto));
        model.addAttribute("layout","layout/null.vm");

        setPager(model, dto.getPAGE_NUM());

        return "admin/store/appInfo/appInfoList";
    }


    //상세페이지(POST) - 테이블내용만.
    @RequestMapping(value="/admin/store/appInfo/detailList", method=RequestMethod.POST)
    public String appInfoDetailList(Model model, AppInfoSearchDto searchDto){

        logger.debug("#################################" + this.getClass().getName());
        logger.debug("############APP_IDX #####" + searchDto.getAPP_IDX());


        AppInfoDto appInfo = appInfoService.selectAppDetailInfo(searchDto);

        BinaryDto binaryInfo = appInfoService.selectAppBinaryInfo(searchDto);

        //BinVer 를 '.'로 split 편집
        if(binaryInfo != null) 	binaryInfo = editBinVer( binaryInfo );

        //플랫폼 버젼 select box list 가져오기
        AppInfoSearchDto searDao = new AppInfoSearchDto();
        searDao.setPLAT_CD(TYPE_ANDROID); //안드로이드
        List<PlatformDto> verList10 = appInfoService.selectPlatformVerListByPlatCd(searDao);
        searDao.setPLAT_CD(TYPE_IOS); //애플
        List<PlatformDto> verList20 = appInfoService.selectPlatformVerListByPlatCd(searDao);

        //searchDto.setServiceUrl(STORE_SERVICE_URL);
        searchDto.setServiceUrl(ABSOLUTE_PATH);

        model.addAttribute("verList10",    verList10);//플랫폼버젼 목록(select box)
        model.addAttribute("verList20",    verList20);//플랫폼 버젼목록(select box)
        model.addAttribute("platList",     deviceService.selectPlatformList());//플랫폼 목록(select box)
        model.addAttribute("distList",     appInfoService.selectCommoncode("ST006"));//IOS배포타입(select box)
        model.addAttribute("appInfo",  appInfo);
        model.addAttribute("binaryInfo", binaryInfo);
        model.addAttribute("appImageInfo", appInfoService.selectAppImageInfo(searchDto));
        model.addAttribute("editModeYN","Y");

        return "admin/store/appInfo/appInfoAdd";
    }

    //바이너리 History 리스트 팝업
    @RequestMapping(value="admin/store/appInfo/binaryHistory" , method=RequestMethod.GET)
    public String binaryHistoryGet( Model model, BinaryDto binaryDto){

        logger.info("getAPP_PLAT_IDX>>>>>>>>>>>>>"  +  binaryDto.getAPP_PLAT_IDX());
        binaryDto.setPAGE_SIZE(row_size);
        List<BinaryDto>  binaryHistoryList = appInfoService.selectBinaryHistoryList(binaryDto);


        model.addAttribute( "binaryHistoryList", binaryHistoryList );

        model.addAttribute("layout","layout/null.vm");
        setPager(model, binaryDto.getPAGE_NUM());

        return "admin/store/appInfo/binaryHistoryListPop";
    }

    //BinVer 를 '.'로 split 편집
    private BinaryDto editBinVer(BinaryDto dto){

        if(dto.getBIN_VER_10() != null ){
            String[] ver = StringUtils.split(dto.getBIN_VER_10(),".");

            if(ver.length == 3 ){
                dto.setBIN_VER1_10(ver[0]);
                dto.setBIN_VER2_10(ver[1]);
                dto.setBIN_VER3_10(ver[2]);
            }
        }

        if(dto.getBIN_VER_20() != null){

            String[] ver = StringUtils.split(dto.getBIN_VER_20(),".");

            if(ver.length == 3 ){
                dto.setBIN_VER1_20(ver[0]);
                dto.setBIN_VER2_20(ver[1]);
                dto.setBIN_VER3_20(ver[2]);
            }
        }

        return dto;
    }

    //앱리스트 삭제
    @ResponseBody
    @RequestMapping("admin/store/appInfo/deleteList")
    public String appInfoDelete(@ModelAttribute("DEL_LIST") ArrayList<String> DEL_LIST, HttpServletRequest request,Model model) throws Exception{

        logger.info(" ### appInfoDelete START ========= ");
        logger.info("DEL_LIST : " + DEL_LIST);
        String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");

        int dbResultSum = 0;
        int result = 0;

        try{

            for (int i = 0; i < DEL_LIST.size(); i++) {
                String idx = DEL_LIST.get(i) ;

                AppInfoDto dto = new AppInfoDto();
                dto.setAPP_IDX(idx);
                dto.setMOD_ID(S_ID_USER);
                //2. DB삭제

                appInfoService.deleteAppImgByAppIDx(dto); //앱이미지 정보 삭제
                result = appInfoService.deleteTbStoApp(dto); //앱정보 삭제
                appInfoService.deleteTbStoAppPlat(dto); //앱플랫폼 정보 삭제

                List<AppInfoDto> idxs =  appInfoService.selectAppPlatIdxList(dto);

                //앱플랫폼 일련번호 리스트를 구해서 해당하는 바이너리 테이블을 삭제한다.
                for(int j = 0; j < idxs.size(); j++){
                    AppInfoDto dto2 = new AppInfoDto();
                    String appPlatIdx =  idxs.get(j).getAPP_PLAT_IDX();
                    dto2.setAPP_PLAT_IDX(appPlatIdx);
                    appInfoService.deleteTbStoBin(dto2); //앱 바이너리 정보 삭제
                }


                logger.info("result : " + result);
                dbResultSum += result;
            }

        } catch (Exception e) {
            logger.error("Exception caught.", e);
        }

        HashMap<String, Object> map = new HashMap<String,Object>();

        if(dbResultSum > 0){
            map.put("result", result);
            map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
        } else {
            map.put("result", result);
            map.put("msg",messageSource.getMessage("menu.store.device.alert.saveError", null, LocaleContextHolder.getLocale()) ); //"저장중 장애가 발생하였니다."
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(map);

        return data;

    }

    //앱정보 삭제
    @ResponseBody
    @RequestMapping("admin/store/appInfo/deleteAppInfo")
    public String deleteAppInfo(AppInfoDto dto, HttpServletRequest request, Model model) throws Exception{

        logger.info(" ### deleteAppInfo START ========= ");

        int result = 0;
        String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");

        dto.setMOD_ID(S_ID_USER);
        //2. DB삭제

        appInfoService.beginTransaction();

        try {

            result = appInfoService.deleteAppImgByAppIDx(dto); //앱이미지 정보 삭제
            result = appInfoService.deleteTbStoApp(dto); //앱정보 삭제
            result = appInfoService.deleteTbStoAppPlat(dto); //앱플랫폼 정보 삭제

            List<AppInfoDto> idxs =  appInfoService.selectAppPlatIdxList(dto);

            //앱플랫폼 일련번호 리스트를 구해서 해당하는 바이너리 테이블을 삭제한다.
            for(int j = 0; j < idxs.size(); j++){
                AppInfoDto dto2 = new AppInfoDto();
                String appPlatIdx =  idxs.get(j).getAPP_PLAT_IDX();
                dto2.setAPP_PLAT_IDX(appPlatIdx);
                result = appInfoService.deleteTbStoBin(dto2); //앱 바이너리 정보 삭제
            }


            logger.info("result : " + result);

            appInfoService.commitTransaction();

        } catch (Exception e) {
            appInfoService.rollbackTransaction();
            logger.error("Exception caught.", e);
        }

        HashMap<String, Object> map = new HashMap<String,Object>();

        if(result > 0){
            map.put("result", result);
            map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
        } else {
            map.put("result", result);
            map.put("msg",messageSource.getMessage("menu.store.device.alert.deleteError", null, LocaleContextHolder.getLocale()) );  //"삭제중 장애가 발생하였니다."
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(map);

        return data;

    }

    /**
     * App 등록/수정 화면
     * @param model
     * @return
     * @since 2014. 5. 9. by UncleJoe
     */
    @RequestMapping( value="admin/store/appInfoAdd" ,method=RequestMethod.GET)
    public String appInfoAdd(Model model){

        AppInfoDto dto = appInfoService.selectSvcInfo();

        model.addAttribute("serviceInfo", dto);
        model.addAttribute("platList"   , deviceService.selectPlatformList());//플랫폼 목록(select box)
        model.addAttribute("distList"   , appInfoService.selectCommoncode("ST006"));//IOS배포타입(select box)


        return "admin/store/appInfo/appInfoAdd";
    }

    /**
     * App정보 등록
     * @param multiFiles
     * @param model
     * @param request
     * @param appInfoDto
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @since 2014. 5. 9. by 이명보
     */
    @RequestMapping( value="admin/store/appInfo/regist" , method=RequestMethod.POST)
    public String appInfoRegistPost( @ModelAttribute("frmAppInfo") MultiFileDto multiFiles, AppInfoDto appInfoDto, HttpServletRequest request, Model model) throws JsonGenerationException, JsonMappingException, IOException{
		/*
		 * FileUpload와 동시처리를 위해 Ajax가 아닌 form.submit올 처리함.
		 */
        logger.debug("######### 앱정보 등록 START #################33");

        String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
        String rtnCd = "999";
        int result = 0;

        appInfoDto.setREG_ID(S_ID_USER);


        /****************************************************/
        //앱정보 일련번호 가져오기
        int appIdx = appInfoService.selectAppIdx();
        logger.info("###appInfoDto appIdx ["+ appIdx + "]" );
        /****************************************************/

        appInfoService.beginTransaction();

        try {
            appInfoDto.setAPP_IDX(String.valueOf(appIdx));
            /****************************************************/
            //앱정보 등록
            result = appInfoService.insertAppInfo(appInfoDto);
            /****************************************************/

            //플랫폼 구분 갯수대로 앱플랫폼정보 등록
            List<String> platIdxCheckList = appInfoDto.getPLAT_IDX(); //체크된 플랫폼만큼
            List<String> platCdList = appInfoDto.getPLAT_CD();   //총 플랫폼
            //List<String> updateDescList = appInfoDto.getUPDATE_DESC(); //바이너리 변경내역

            List<MultipartFile> files = multiFiles.getFiles();  //여러 파일 정보  get

            String platCd_t = ""; //플랫폼코드
//			String platVerIdx_t = ""; //플랫폼버젼 일련번호
            String binVer_t = "";
//			String updateDesc_t = "";
            String iosFullImg = "";
            String iosDisplayImg = "";

            String distType_t = appInfoDto.getDIST_TYPE(); //배포타입
            String callUrl_t  = appInfoDto.getCALL_URL();  //호출url

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //앱 이미지  파일 업로드, 앱 이미지 정보 등록
            //platCdList 사이즈는 플랫폼 갯수 , files.size()는 이미지파일 총갯수 (빈파일 포함, 7개)
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            int insert_cnt = 1;//등록한  이미지 갯수
            int file_cnt = 1;  //총 이미지 갯수

            // files 배열 총 9개 중에 files[0],files[1] 은 바이너리 파일 ,
            // files[2], files[3],files[4] 는 thumbNail 이미지 파일,
            // files[5] ,files[6],files[7],files[8]은 preview 이미지 파일

            //  platCdList.size() = 2, files.size() = 9 , i는 2부터 시작.
            for(int i = platCdList.size();  i < files.size() ; i++){

                MultipartFile multipartFile = files.get(i);
                String fileName = multipartFile.getOriginalFilename();
                logger.info("### 이미지 fileName >>> " + fileName);

                //파일이름이 존재하면 파일업로드와 파일정보등록
                AppImageDto imageDto = null;
                try {
                    if(!multipartFile.isEmpty()){
                        imageDto = uploadImageFile(appInfoDto, multipartFile, file_cnt);
                        if(i==2){ // APP ICON(120*120)일 경우 IOS7에서는 반드시 아이콘 URL이 필요하므로 해당 아이콘으로 셋팅
                            iosFullImg = imageDto.getFILE_NM()+"."+imageDto.getEXT_NM();
                            iosDisplayImg = imageDto.getFILE_NM()+"."+imageDto.getEXT_NM();
                        }
                        imageDto.setREG_ID(S_ID_USER);
                        //fileDto.setDISP_NO(String.valueOf(insert_cnt));
                        imageDto.setDISP_NO(String.valueOf(file_cnt));
                        /****************************************************/
                        result = appInfoService.insertAppImage(imageDto);		//앱이미지 파일 등록
                        /****************************************************/
                        insert_cnt += 1;
                    }
                } catch (SocketException e){
                    logger.error("Exception caught.", e);
                    return "redirect:/admin/store/appInfo?rtnCd=ftpFail";
                } catch (IOException e) {
                    logger.error("Exception caught.", e);
                    return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                } catch (Exception e) {
                    logger.error("Exception caught.", e);
                    return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                }
                file_cnt += 1;
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            List<PlatformDto> platformDbList = appInfoService.selectBoxPlatformList();//플랫폼 리스트


            //앱플랫폼정보, 바이너리 정보  등록

            //platIdxCheckList 는 체크한 플랫폼 리스트만 넘어옴.
            AppPlatInfoDto appPlatDto = new AppPlatInfoDto();
            appPlatDto.setAPP_IDX(appInfoDto.getAPP_IDX());
            appPlatDto.setCALL_URL(appInfoDto.getCALL_URL());
            appPlatDto.setDIST_TYPE(appInfoDto.getDIST_TYPE());
            appPlatDto.setPLAT_IDX(appInfoDto.getPLAT_IDX());
            appPlatDto.setPLAT_IDX_T(appInfoDto.getPLAT_IDX_T());
            appPlatDto.setREG_ID(appInfoDto.getREG_ID());
            appPlatDto.setUSE_YN("Y");

            for(int i=0; i<platformDbList.size() ; i++){

                PlatformDto platDbDto = platformDbList.get(i);

                //체크한 플랫폼만 앱플랫폼정보, 바이너리 정보  등록
                for(int j=0; j<platIdxCheckList.size() ; j++){

                    String platIdxChk = platIdxCheckList.get(j);  //선택한 플랫폼IDX만 셋팅

                    if( platIdxChk.equals( platDbDto.getPLAT_IDX() ) ){
                        platCd_t = platDbDto.getPLAT_CD();

                        appInfoDto.setPLAT_IDX_T(platIdxChk);
                        appPlatDto.setPLAT_IDX_T(appInfoDto.getPLAT_IDX_T());
                        //애플만 배포타입, 호출URL 값을 셋팅
                        if(TYPE_IOS.equals(platCd_t)){

                            appInfoDto.setDIST_TYPE(distType_t);
                            appInfoDto.setCALL_URL(callUrl_t);

                            appPlatDto.setDIST_TYPE(distType_t);
                            appPlatDto.setCALL_URL(callUrl_t);

                        }else{
                            logger.info("###platCd_t ===========================["+ platCd_t + "]"   );

                            appInfoDto.setDIST_TYPE("");
                            appInfoDto.setCALL_URL("");

                            appPlatDto.setDIST_TYPE("");
                            appPlatDto.setCALL_URL("");
                        }

                        if(TYPE_ANDROID.equals(platCd_t)) {
                            appPlatDto.setPAKG_ID(appInfoDto.getPAKG_ID_10());
                        } else if(TYPE_IOS.equals(platCd_t)) {
                            appPlatDto.setPAKG_ID(appInfoDto.getPAKG_ID_20());
                        }

                        /****************************************************/
                        //앱플랫폼정보 일련번호 가져오기
                        int appPlatIdx = appInfoService.selectAppPlatIdx();
                        logger.info("###appInfoDto appPlatIdx --------------------------["+ appPlatIdx + "]" );
                        /****************************************************/
                        appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdx));
                        appPlatDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX());
                        /****************************************************/
                        result = appInfoService.insertAppPlat(appPlatDto); //앱플랫폼 정보 등록
                        /****************************************************/

//						String platVerIdx =  "PLAT_VER_IDX_" + platCd_t ; 
                        String platVerIdx_t = request.getParameter("PLAT_VER_IDX_" + platCd_t); //앱플랫폼 일련번호는 화면에서 'PLAT_VER_IDX_10', 'PLAT_VER_IDX_20' 형태임.

                        String binVer =  "BIN_VER_" + platCd_t ;
                        binVer_t = request.getParameter(binVer);
                        //updateDesc_t = updateDescList.get(i);

                        appInfoDto.setPLAT_VER_IDX(platVerIdx_t);
                        appInfoDto.setBIN_VER(binVer_t);

                        if(TYPE_ANDROID.equals(platCd_t)){
                            appInfoDto.setUPDATE_DESC_T( appInfoDto.getUPDATE_DESC_10());
                        }else if(TYPE_IOS.equals(platCd_t)){
                            appInfoDto.setUPDATE_DESC_T( appInfoDto.getUPDATE_DESC_20());

                        }


                        MultipartFile multipartFile = files.get(i);

                        String fileName = multipartFile.getOriginalFilename();

                        logger.info("### 바이너리 platVerIdx >>> PLAT_VER_IDX_" + platCd_t);
                        logger.info("### 바이너리 orgName >>> " + fileName);
                        logger.info("### 바이너리 fileName >>> " + multipartFile.getName());

                        //파일이름이 존재하면 파일업로드와 파일정보등록
                        BinaryDto binaryDto = null;
                        try {

//                            binaryDto = this.uploadBinaryFile(appInfoDto, multipartFile, platCd_t);
                            binaryDto = uploadFile(appInfoDto, multipartFile, platCd_t, iosFullImg, iosDisplayImg);
                            binaryDto.setREG_ID(S_ID_USER);
                            // IOS 일때는 pl
//							if(platFormType.equals("35")) {
//								String plistFilePath = uploadPath + File.separator + fileName + "." + IOSPlistInfo.getExtention();
//								String version = (String)params.get("appVersion");
//								String identifier = reqParam.getString("PACKAGENAME");
//								String title = reqParam.getString("APPNAME");
//								String url = app.getServiceURL() + "/" + app.getBinaryPath() + "/" +fileName + ".ipa";
//								IOSPlistInfo.createiOSPlistFile(plistFilePath, title, url, version, identifier);	
//
//					        	if (lm_bResourceSync)			
//					        		IOSPlistInfo.sendResourceFile(plistFilePath, app.getBinaryPath() + File.separator );  
//					        	
//							}         
                            /****************************************************/
                            result = appInfoService.insertBinary(binaryDto);        //바이너리 파일 등록
                            /****************************************************/
                        } catch (SocketException e){
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=ftpFail";
                        } catch (IOException e) {
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                        }  catch (Exception e) {
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                        }


                    }else{
                        continue;
                    }
                }	// end for

            } //end for


            if(result > 0){
                rtnCd = "insertOk";
            } else {
                rtnCd = "insertFail";
            }

            appInfoService.commitTransaction();

        } catch (Exception e) {
            appInfoService.rollbackTransaction();
            logger.error("Exception caught.", e);
        }

        return "redirect:/admin/store/appInfo?rtnCd=" + rtnCd;
    }


    //수정
    /**
     * App 정보 수정
     * @param multiFiles
     * @param model
     * @param request
     * @param appInfoDto
     * @param binaryDto
     * @param imageDto
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @since 2014. 5. 9. by UncleJoe
     */
    @RequestMapping( value="admin/store/appInfo/modify" , method=RequestMethod.POST)
    public String AppInfoModifyPost( @ModelAttribute("frmAppInfo") MultiFileDto multiFiles, Model model,  HttpServletRequest request, AppInfoDto appInfoDto, BinaryDto binaryDto, AppImageDto imageDto) throws JsonGenerationException, JsonMappingException, IOException{

        logger.info("######### 앱정보 수정 v1.1 START #################33");

        String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
        String rtnCd = "999";
        int result = 0;

        appInfoDto.setMOD_ID(S_ID_USER);
        appInfoDto.setREG_ID(S_ID_USER);

        /****************************************************/
        //앱정보 일련번호
        String appIdx = appInfoDto.getAPP_IDX();
        logger.info("###appInfoDto appIdx ------------------["+ appIdx + "]" );
        /****************************************************/

        //transaction START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        appInfoService.beginTransaction();

        try {

            /****************************************************/
            //앱정보 수정
            result = appInfoService.updateAppInfo(appInfoDto);
            /****************************************************/

            //플랫폼 구분 갯수대로 앱플랫폼정보 등록
            List<String> platIdxCheckList = appInfoDto.getPLAT_IDX(); //체크된 플랫폼만큼
            List<String> platCdList = appInfoDto.getPLAT_CD();
            //List<String> updateDescList = appInfoDto.getUPDATE_DESC(); //바이너리 변경내역

            List<MultipartFile> files = multiFiles.getFiles();         //여러 파일 정보  get

            String platCd_t = ""; //플랫폼코드

            String distType_t = appInfoDto.getDIST_TYPE(); //배포타입
            String callUrl_t 	= appInfoDto.getCALL_URL();  //호출url
            String iosFullImg = imageDto.getFILE_NM_1();
            String iosDisplayImg = imageDto.getFILE_NM_1();

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //앱 이미지  파일 업로드, 앱 이미지 정보 등록
            //platCdList 사이즈는 플랫폼 갯수 , files.size()는 이미지파일 총갯수 (빈파일 포함, 7개)
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            int insert_cnt = 1;//등록한  이미지 갯수
            int file_cnt = 1;  //총 이미지 갯수

            List<String> imgIdxs = imageDto.getIMG_IDXS() ;   //이미지 일련번호 리스트

            // files 배열 총 9개 중에 files[0],files[1] 은 바이너리 파일 ,
            // files[2], files[3],files[4] 는 thumbNail 이미지 파일,
            // files[5] ,files[6],files[7],files[8]은 preview 이미지 파일

            //  platCdList.size() = 2, files.size() = 9 , i는 2부터 시작.
            for(int i = platCdList.size();  i < files.size() ; i++){

                String imgIdx = imgIdxs.get(i-2);

                MultipartFile multipartFile = files.get(i);
                String fileName = multipartFile.getOriginalFilename();

                logger.info("### 이미지 fileName >>> " + fileName);

                //파일이름이 존재하면 파일업로드와 파일정보등록
                AppImageDto imageDto2 = null;

                if(!multipartFile.isEmpty()){
                    try {
                        imageDto2 = uploadImageFile(appInfoDto, multipartFile, file_cnt);
                        if(i==2){ // APP ICON(120*120)일 경우 IOS7에서는 반드시 아이콘 URL이 필요하므로 해당 아이콘으로 셋팅
                            iosFullImg = imageDto2.getFILE_NM()+"."+imageDto2.getEXT_NM();
                            iosDisplayImg = imageDto2.getFILE_NM()+"."+imageDto2.getEXT_NM();
                        }
                    } catch (SocketException e){
                        logger.error("Exception caught.", e);
                        return "redirect:/admin/store/appInfo?rtnCd=ftpFail";
                    } catch (IOException e) {
                        logger.error("Exception caught.", e);
                        return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                    } catch (Exception e) {
                        logger.error("Exception caught.", e);
                        return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                    }
                    //fileDto.setDISP_NO(String.valueOf(insert_cnt));
                    imageDto2.setDISP_NO(String.valueOf(file_cnt));
                    imageDto2.setIMG_IDX(imgIdx);
                    imageDto2.setREG_ID(S_ID_USER);  //이미지 수정시, 존재하지 않으면 등록
                    imageDto2.setMOD_ID(S_ID_USER);
                    /****************************************************/
                    if(imgIdx == null || "".equals(imgIdx)){
                        result = appInfoService.insertAppImage(imageDto2);		//앱이미지 파일 수정
                    }else{
                        result = appInfoService.updateAppImage(imageDto2);		//앱이미지 파일 수정
                    }
                    /****************************************************/
                    insert_cnt += 1;

                }

                file_cnt += 1;

            }
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            List<PlatformDto> platformDbList = appInfoService.selectBoxPlatformList();// //총 플랫폼  리스트

            //수정시, 기존에 선택된  플랫폼 체크가 해제되는 경우는 삭제처리 해줘야 함.
            //기존에 선택된 앱플랫폼 SELECT
            if(platIdxCheckList != null && platIdxCheckList.size() > 0){

                String[] PLAT_IDX_ARR = new String[platIdxCheckList.size()];

                for(int j=0; j<platIdxCheckList.size() ; j++){
                    String  platIdxChk = platIdxCheckList.get(j);
                    PLAT_IDX_ARR[j] = platIdxChk;
                }

                appInfoDto.setPLAT_IDX_ARR(PLAT_IDX_ARR);
                List<AppInfoDto> diff = new ArrayList<AppInfoDto>();
                diff = appInfoService.selectAppPlatIdxDiffList(appInfoDto);

                //기존에 선택등록된 platform에서  체크된 플랫폼을 비교하여
                //체크되지 않은 것을 추출하여 delete
                if(diff != null){
                    for(int i= 0; i < diff.size(); i++){
                        AppInfoDto  diffDto = diff.get(i);
                        diffDto.setMOD_ID(S_ID_USER);
                        /****************************************************/
                        appInfoService.deleteTbStoAppPlat2(diffDto);
                        appInfoService.deleteTbStoBin(diffDto) ;
                        /****************************************************/
                    }
                }
            }

            AppPlatInfoDto appPlatDto = new AppPlatInfoDto();
            appPlatDto.setAPP_IDX(appInfoDto.getAPP_IDX());
            appPlatDto.setCALL_URL(appInfoDto.getCALL_URL());
            appPlatDto.setDIST_TYPE(appInfoDto.getDIST_TYPE());
            appPlatDto.setPLAT_IDX(appInfoDto.getPLAT_IDX());
            appPlatDto.setREG_ID(appInfoDto.getREG_ID());
            appPlatDto.setUSE_YN("Y");
            appPlatDto.setMOD_ID(S_ID_USER);
            appPlatDto.setREG_ID(S_ID_USER);

            //앱플랫폼정보, 바이너리 정보  수정
            //platIdxCheckList 는 체크한 플랫폼 리스트만 넘어옴.
            for(int i=0; i<platformDbList.size() ; i++){ // 총 플랫폼 갯수만큼

                PlatformDto platDbDto = platformDbList.get(i);

                //체크한 플랫폼만 앱플랫폼정보, 바이너리 정보  수정
                for(int j=0; j<platIdxCheckList.size() ; j++){

                    String platIdxChk = platIdxCheckList.get(j);  //선택한 플랫폼IDX만 셋팅

                    if( platIdxChk.equals( platDbDto.getPLAT_IDX() ) ){

                        platCd_t = platDbDto.getPLAT_CD();

                        appInfoDto.setPLAT_IDX_T(platIdxChk);
                        appPlatDto.setPLAT_IDX_T(appInfoDto.getPLAT_IDX_T());


                        //애플만 배포타입, 호출URL 값을 셋팅
                        if(TYPE_IOS.equals(platCd_t)){

                            appInfoDto.setDIST_TYPE(distType_t);
                            appInfoDto.setCALL_URL(callUrl_t);

                            appPlatDto.setDIST_TYPE(distType_t);
                            appPlatDto.setCALL_URL(callUrl_t);

                        }else{
                            logger.info("###platCd_t ===========================["+ platCd_t + "]"   );

                            appInfoDto.setDIST_TYPE("");
                            appInfoDto.setCALL_URL("");

                            appPlatDto.setDIST_TYPE("");
                            appPlatDto.setCALL_URL("");
                        }


                        //앱플랫폼정보 일련번호 가져오기
                        int appPlatIdx = 0;
                        if(TYPE_ANDROID.equals(platCd_t)){
                            appPlatIdx = Integer.parseInt(StringUtils.defaultIfEmpty(binaryDto.getAPP_PLAT_IDX_10(),"0"));

                            //앱플랫폼정보 일련번호 가져오기
                            if( appPlatIdx == 0){
                                int appPlatIdxInt = appInfoService.selectAppPlatIdx();
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdxInt));
                            }else{
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdx));
                            }
                            appPlatDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX());
                            appPlatDto.setPAKG_ID(appInfoDto.getPAKG_ID_10());
                            logger.info("###appInfoDto appPlatIdx --------------["+ appPlatIdx + "]" );

                            /****************************************************/
                            result = appInfoService.saveAppPlat(appPlatDto); //앱플랫폼 정보 수정
                            /****************************************************/

                        }else if(TYPE_IOS.equals(platCd_t)){
//							appPlatIdx = binaryDto.getAPP_PLAT_IDX_20();
                            appPlatIdx = Integer.parseInt(StringUtils.defaultIfEmpty(binaryDto.getAPP_PLAT_IDX_20(),"0"));

                            //앱플랫폼정보 일련번호 가져오기
                            if(appPlatIdx == 0){
                                int appPlatIdxInt = appInfoService.selectAppPlatIdx();
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdxInt));
                            }else{
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdx));
                            }

                            appPlatDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX());
                            appPlatDto.setPAKG_ID(appInfoDto.getPAKG_ID_20());

                            logger.info("###appInfoDto appPlatIdx --------------["+ appPlatIdx + "]" );

                            /****************************************************/
                            result = appInfoService.saveAppPlat(appPlatDto); //앱플랫폼 정보 수정
                            /****************************************************/
                        }

                        if(TYPE_ANDROID.equals(platCd_t)){
                            appInfoDto.setPLAT_VER_IDX(binaryDto.getPLAT_VER_IDX_10());
                            appInfoDto.setBIN_VER(binaryDto.getBIN_VER_10());
                            appInfoDto.setUPDATE_DESC_T( appInfoDto.getUPDATE_DESC_10());
                        }else if(TYPE_IOS.equals(platCd_t)){
                            appInfoDto.setPLAT_VER_IDX(binaryDto.getPLAT_VER_IDX_20());
                            appInfoDto.setBIN_VER(binaryDto.getBIN_VER_20());
                            appInfoDto.setUPDATE_DESC_T( appInfoDto.getUPDATE_DESC_20());
                        }

                        MultipartFile multipartFile = files.get(i);
                        BinaryDto binaryDto2 = null;
                        try {
                            //binary file upload
//                            binaryDto2 = this.uploadBinaryFile(appInfoDto, multipartFile, platCd_t);
                            binaryDto2 = uploadFile(appInfoDto, multipartFile, platCd_t,iosFullImg,iosDisplayImg);
                            binaryDto2.setREG_ID(S_ID_USER);
                        } catch (SocketException e){
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=ftpFail";
                        } catch (IOException e) {
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                        }  catch (Exception e) {
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                        }
                        /****************************************************/
                        if(multipartFile.isEmpty()){ //파일내역이 비어 있으면
                            result = appInfoService.updateBinary(binaryDto2);		//바이너리 내용만 수정
                        }else{
                            //result = appInfoService.updateBinaryFile(binaryDto2); //바이너리 내용파일 수정
                            result = appInfoService.insertBinary(binaryDto2);	   	//바이너리 파일의 경우는 무조건 등록
                        }
                        /****************************************************/

                    }else{
                        continue;
                    }
                }	// end for

            } //end for

            if(result > 0){
                rtnCd = "updateOk";
            } else {
                rtnCd = "updateFail";
            }

            appInfoService.commitTransaction();
        }catch(Exception e){
            appInfoService.rollbackTransaction() ;
            rtnCd = "updateFail";
            logger.error("Exception caught.", e);

        }

        return "redirect:/admin/store/appInfo?rtnCd=" + rtnCd;
    }


    //수정
    @RequestMapping( value="admin/store/appInfo/modify_org" , method=RequestMethod.POST)
    public String AppInfoModifyPost_org( @ModelAttribute("frmAppInfo") MultiFileDto multiFiles, Model model,  HttpServletRequest request, AppInfoDto appInfoDto, BinaryDto binaryDto, AppImageDto imageDto) throws JsonGenerationException, JsonMappingException, IOException{

        logger.info("######### 앱정보 등록 START #################33");

        String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
        String rtnCd = "999";
        int result = 0;

        appInfoDto.setMOD_ID(S_ID_USER);
        appInfoDto.setREG_ID(S_ID_USER);

        /****************************************************/
        //앱정보 일련번호
        String appIdx = appInfoDto.getAPP_IDX();
        logger.info("###appInfoDto appIdx ------------------["+ appIdx + "]" );
        /****************************************************/

        //transaction START >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        appInfoService.beginTransaction();

        try {

            /****************************************************/
            //앱정보 수정
            result = appInfoService.updateAppInfo(appInfoDto);
            /****************************************************/

            //플랫폼 구분 갯수대로 앱플랫폼정보 등록
            List<String> platIdxCheckList = appInfoDto.getPLAT_IDX(); //체크된 플랫폼만큼
            List<String> platCdList = appInfoDto.getPLAT_CD();
            //List<String> updateDescList = appInfoDto.getUPDATE_DESC(); //바이너리 변경내역

            List<MultipartFile> files = multiFiles.getFiles();         //여러 파일 정보  get

            String platCd_t = ""; //플랫폼코드

            String distType_t = appInfoDto.getDIST_TYPE(); //배포타입
            String callUrl_t 	= appInfoDto.getCALL_URL();  //호출url

            List<PlatformDto> platformDbList =   appInfoService.selectBoxPlatformList();// //총 플랫폼  리스트

            //수정시, 기존에 선택된  플랫폼 체크가 해제되는 경우는 삭제처리 해줘야 함.
            //기존에 선택된 앱플랫폼 SELECT
            if(platIdxCheckList != null && platIdxCheckList.size() > 0){

                String[] PLAT_IDX_ARR = new String[platIdxCheckList.size()];

                for(int j=0; j<platIdxCheckList.size() ; j++){
                    String  platIdxChk = platIdxCheckList.get(j);
                    PLAT_IDX_ARR[j] = platIdxChk;
                }

                appInfoDto.setPLAT_IDX_ARR(PLAT_IDX_ARR);
                List<AppInfoDto> diff = new ArrayList<AppInfoDto>();
                diff = appInfoService.selectAppPlatIdxDiffList(appInfoDto);

                //기존에 선택등록된 platform에서  체크된 플랫폼을 비교하여
                //체크되지 않은 것을 추출하여 delete
                if(diff != null){
                    for(int i= 0; i < diff.size(); i++){
                        AppInfoDto  diffDto = diff.get(i);
                        diffDto.setMOD_ID(S_ID_USER);
                        /****************************************************/
                        appInfoService.deleteTbStoAppPlat2(diffDto);
                        appInfoService.deleteTbStoBin(diffDto) ;
                        /****************************************************/
                    }
                }
            }

            AppPlatInfoDto appPlatDto = new AppPlatInfoDto();
            appPlatDto.setAPP_IDX(appInfoDto.getAPP_IDX());
            appPlatDto.setCALL_URL(appInfoDto.getCALL_URL());
            appPlatDto.setDIST_TYPE(appInfoDto.getDIST_TYPE());
            appPlatDto.setPLAT_IDX(appInfoDto.getPLAT_IDX());
            appPlatDto.setREG_ID(appInfoDto.getREG_ID());
            appPlatDto.setUSE_YN("Y");
            appPlatDto.setMOD_ID(S_ID_USER);
            appPlatDto.setREG_ID(S_ID_USER);

            //앱플랫폼정보, 바이너리 정보  수정
            //platIdxCheckList 는 체크한 플랫폼 리스트만 넘어옴.
            for(int i=0; i<platformDbList.size() ; i++){ // 총 플랫폼 갯수만큼

                PlatformDto platDbDto = platformDbList.get(i);

                //체크한 플랫폼만 앱플랫폼정보, 바이너리 정보  수정
                for(int j=0; j<platIdxCheckList.size() ; j++){

                    String platIdxChk = platIdxCheckList.get(j);  //선택한 플랫폼IDX만 셋팅

                    if( platIdxChk.equals( platDbDto.getPLAT_IDX() ) ){
                        platCd_t = platDbDto.getPLAT_CD();

                        appInfoDto.setPLAT_IDX_T(platIdxChk);
                        appPlatDto.setPLAT_IDX_T(appInfoDto.getPLAT_IDX_T());


                        //애플만 배포타입, 호출URL 값을 셋팅
                        if(!TYPE_IOS.equals(platCd_t)){
                            appInfoDto.setDIST_TYPE("");
                            appInfoDto.setCALL_URL("");
                        }else{
                            appInfoDto.setDIST_TYPE(distType_t);
                            appInfoDto.setCALL_URL(callUrl_t);
                        }


                        //앱플랫폼정보 일련번호 가져오기
                        int appPlatIdx = 0;
                        if(TYPE_ANDROID.equals(platCd_t)){
//							appPlatIdx = binaryDto.getAPP_PLAT_IDX_10();
                            appPlatIdx = Integer.parseInt(StringUtils.defaultIfEmpty(binaryDto.getAPP_PLAT_IDX_10(),"0"));

                            //앱플랫폼정보 일련번호 가져오기
//							if(appPlatIdx == null || "".equals(appPlatIdx) || "0".equals(appPlatIdx)){
                            if(appPlatIdx == 0){
                                int appPlatIdxInt = appInfoService.selectAppPlatIdx();
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdxInt));
                            }else{
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdx));
                            }
                            appPlatDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX());
                            appPlatDto.setPAKG_ID(appInfoDto.getPAKG_ID_10());
                            logger.info("###appInfoDto appPlatIdx --------------["+ appPlatIdx + "]" );

                            /****************************************************/
                            result = appInfoService.saveAppPlat(appPlatDto); //앱플랫폼 정보 수정
                            /****************************************************/

                        }else if(TYPE_IOS.equals(platCd_t)){
                            //appPlatIdx = binaryDto.getAPP_PLAT_IDX_20();
                            appPlatIdx = Integer.parseInt(StringUtils.defaultIfEmpty(binaryDto.getAPP_PLAT_IDX_20(),"0"));

                            //앱플랫폼정보 일련번호 가져오기
//							if(appPlatIdx == null || "".equals(appPlatIdx) || "0".equals(appPlatIdx)){
                            if(appPlatIdx ==0){
                                int appPlatIdxInt = appInfoService.selectAppPlatIdx();
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdxInt));
                            }else{
                                appInfoDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdx));
                            }

                            appPlatDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX());
                            appPlatDto.setPAKG_ID(appInfoDto.getPAKG_ID_20());

                            logger.info("###appInfoDto appPlatIdx --------------["+ appPlatIdx + "]" );

                            /****************************************************/
                            result = appInfoService.saveAppPlat(appPlatDto); //앱플랫폼 정보 수정
                            /****************************************************/
                        }

                        if(TYPE_ANDROID.equals(platCd_t)){
                            appInfoDto.setPLAT_VER_IDX(binaryDto.getPLAT_VER_IDX_10());
                            appInfoDto.setBIN_VER(binaryDto.getBIN_VER_10());
                            appInfoDto.setUPDATE_DESC_T( appInfoDto.getUPDATE_DESC_10());
                        }else if(TYPE_IOS.equals(platCd_t)){
                            appInfoDto.setPLAT_VER_IDX(binaryDto.getPLAT_VER_IDX_20());
                            appInfoDto.setBIN_VER(binaryDto.getBIN_VER_20());
                            appInfoDto.setUPDATE_DESC_T( appInfoDto.getUPDATE_DESC_20());
                        }

                        MultipartFile multipartFile = files.get(i);
                        BinaryDto binaryDto2 = null;
                        try {
                            binaryDto2 = this.uploadBinaryFile(appInfoDto, multipartFile, platCd_t);
                            binaryDto2.setREG_ID(S_ID_USER);
                        } catch (SocketException e){
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=ftpFail";
                        } catch (IOException e) {
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                        }  catch (Exception e) {
                            logger.error("Exception caught.", e);
                            return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                        }
                        /****************************************************/
                        if(multipartFile.isEmpty()){ //파일내역이 비어 있으면
                            result = appInfoService.updateBinary(binaryDto2);		//바이너리 내용만 수정
                        }else{
                            //result = appInfoService.updateBinaryFile(binaryDto2); //바이너리 내용파일 수정
                            result = appInfoService.insertBinary(binaryDto2);	   	//바이너리 파일의 경우는 무조건 등록
                        }
                        /****************************************************/

                    }else{
                        continue;
                    }
                }	// end for

            } //end for

            //앱 이미지  파일 업로드, 앱 이미지 정보 등록
            //platCdList 사이즈는 플랫폼 갯수 , files.size()는 이미지파일 총갯수 (빈파일 포함, 7개)

//			int insert_cnt = 1;//등록한  이미지 갯수
            int file_cnt = 1;  //총 이미지 갯수


            List<String> imgIdxs = imageDto.getIMG_IDXS() ;   //이미지 일련번호 리스트

            // files 배열 총 9개 중에 files[0],files[1] 은 바이너리 파일 ,
            // files[2], files[3],files[4] 는 thumbNail 이미지 파일,
            // files[5] ,files[6],files[7],files[8]은 preview 이미지 파일

            //  platCdList.size() = 2, files.size() = 9 , i는 2부터 시작.
            for(int i = platCdList.size();  i < files.size() ; i++){

                String imgIdx = imgIdxs.get(i-2);

                MultipartFile multipartFile = files.get(i);
                String fileName = multipartFile.getOriginalFilename();
                logger.info("### 이미지 fileName >>> " + fileName);

                //파일이름이 존재하면 파일업로드와 파일정보등록
                AppImageDto imageDto2 = null;

                if(!multipartFile.isEmpty()){
                    try {
                        imageDto2 = this.uploadImageFile(appInfoDto, multipartFile, file_cnt);
                    } catch (SocketException e){
                        logger.error("Exception caught.", e);
                        return "redirect:/admin/store/appInfo?rtnCd=ftpFail";
                    } catch (IOException e) {
                        logger.error("Exception caught.", e);
                        return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                    } catch (Exception e) {
                        logger.error("Exception caught.", e);
                        return "redirect:/admin/store/appInfo?rtnCd=binaryFail";
                    }
                    //fileDto.setDISP_NO(String.valueOf(insert_cnt));
                    imageDto2.setDISP_NO(String.valueOf(file_cnt));
                    imageDto2.setIMG_IDX(imgIdx);
                    imageDto2.setREG_ID(S_ID_USER);  //이미지 수정시, 존재하지 않으면 등록
                    imageDto2.setMOD_ID(S_ID_USER);
                    /****************************************************/
                    if(imgIdx == null || "".equals(imgIdx)){
                        result = appInfoService.insertAppImage(imageDto2);		//앱이미지 파일 수정
                    }else{
                        result = appInfoService.updateAppImage(imageDto2);		//앱이미지 파일 수정
                    }
                    /****************************************************/
//					insert_cnt += 1;

                }

                file_cnt += 1;

            }

            if(result > 0){
                rtnCd = "updateOk";
            } else {
                rtnCd = "updateFail";
            }

            appInfoService.commitTransaction();
        }catch(Exception e){
            appInfoService.rollbackTransaction() ;
            rtnCd = "updateFail";
            logger.error("Exception caught.", e);

        }

        return "redirect:/admin/store/appInfo?rtnCd=" + rtnCd;
    }


    @ResponseBody
    //앱이미지 한건 삭제
    @RequestMapping("admin/store/appInfo/deleteAppImg")
    public String deleteAppImg( Model model,
                                HttpServletRequest request, AppImageDto dto ) throws Exception{

        logger.info(" ### appInfoDelete START ========= ");

        int result = 0;
        String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
        dto.setMOD_ID(S_ID_USER);


        result = appInfoService.deleteAppImgByImgIDx(dto);
        logger.info("result : " + result);

        HashMap<String, Object> map = new HashMap<String,Object>();

        if(result > 0){
            map.put("result", result);
            map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
        } else {
            map.put("result", result);
            map.put("msg",messageSource.getMessage("menu.store.device.alert.saveError", null, LocaleContextHolder.getLocale()) ); //"저장중 장애가 발생하였니다."
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(map);

        return data;

    }

    @ResponseBody
    //스토어 앱정보의 사용유무 토글
    @RequestMapping("admin/store/appInfo/stsCd")
    public String updateStoreAppStsCd( Model model,
                                       HttpServletRequest request, AppInfoDto dto ) throws Exception{

        int result = 0;

        try {
            String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");

            dto.setMOD_ID(S_ID_USER);

            result = appInfoService.updateStoreAppStsCd(dto);

        } catch (Exception e) {
            logger.error("Exception caught.", e);
        }

        HashMap<String, Object> map = new HashMap<String,Object>();

        if(result > 0){
            map.put("result", result);
            //map.put("msg","저장 되었습니다." );
        } else {
            map.put("result", result);
            //map.put("msg","저장에 실패했습니다." );
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(map);

        return data;

    }


    //앱명, 패키지명, 호출URL  중복체크
    @ResponseBody
    @RequestMapping("/admin/store/appInfoAdd/checkDup")
    public String checkDup( Model model,
                            HttpServletRequest request, AppInfoDto dto ) throws Exception{

        int result = 0;

        String chk = dto.getCHECK();
        logger.info(" --  .getPAKG_ID_10-- " + dto.getPAKG_ID_10() );
        logger.info(" --  .getPAKG_ID_20-- " + dto.getPAKG_ID_20() );

        try {
            if("CALL_URL".equals(chk))
                result = appInfoService.checkDupCallUrl(dto);
            else
                result = appInfoService.checkDupApp(dto);

        } catch (Exception e) {
            logger.error("Exception caught.", e);
        }


        HashMap<String, Object> map = new HashMap<String,Object>();

        if(result > 0){//중복건수가 있으면
            if("APP_NM".equals(chk)){
                map.put("flag","EXIST" );
                map.put("msg", messageSource.getMessage("menu.store.controller.alreadyApp", null, LocaleContextHolder.getLocale())); //해당 App 명은 이미 존재합니다.
            }else if("PAKG_ID_10".equals(chk)){
                map.put("flag","EXIST" );
                map.put("msg", messageSource.getMessage("menu.store.controller.alreadyPack", null, LocaleContextHolder.getLocale())); //해당 Package 명은 이미 존재합니다.
            }else if("PAKG_ID_20".equals(chk)){
                map.put("flag","EXIST" );
                map.put("msg", messageSource.getMessage("menu.store.controller.alreadyPack", null, LocaleContextHolder.getLocale())); //해당 Package 명은 이미 존재합니다.
            }else if("CALL_URL".equals(chk)){
                map.put("flag","EXIST" );
                map.put("msg", messageSource.getMessage("menu.store.controller.alreadyURL", null, LocaleContextHolder.getLocale())); //해당 URL은 이미 존재합니다.
            }
        } else {

            if("APP_NM".equals(chk)){
                map.put("flag","OK" );
                map.put("msg", messageSource.getMessage("menu.store.controller.useApp", null, LocaleContextHolder.getLocale())); //사용할 수 있는 App 명입니다.
            }else if("PAKG_ID_10".equals(chk)){
                map.put("flag","OK" );
                map.put("msg", messageSource.getMessage("menu.store.controller.usePack", null, LocaleContextHolder.getLocale())); //사용할 수 있는 Package 명입니다.
            }else if("PAKG_ID_20".equals(chk)){
                map.put("flag","OK" );
                map.put("msg", messageSource.getMessage("menu.store.controller.usePack", null, LocaleContextHolder.getLocale())); //사용할 수 있는 Package 명입니다.
            }else if("CALL_URL".equals(chk)){
                map.put("flag","OK" );
                map.put("msg", messageSource.getMessage("menu.store.controller.useURL", null, LocaleContextHolder.getLocale())); //사용할 수 있는  URL입니다.
            }
        }

        map.put("result", result);

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(map);

        return data;

    }



    //select 박스 선택시 하위 select 박스 데이터 그리기용(플랫폼버전 선택)
    @ResponseBody
    @RequestMapping( value="admin/store/appInfo/getPlatVer" , method=RequestMethod.POST)
    public String postHandler(Model model, DeviceDto dto ) throws Exception{
        logger.debug("####################################### POST " + this.getClass().getName()) ;

        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        List<PlatformDto> data = deviceService.selectPlatformVerList(dto);

        for(PlatformDto row : data){
            JSONObject readRow = new JSONObject();
            readRow.put("PLAT_VER_IDX", row.getPLAT_VER_IDX());
            readRow.put("PLAT_VER_CD",  row.getPLAT_VER_CD());
            readRow.put("PLAT_VER_NM",  row.getPLAT_VER_NM());

            arr.put(readRow);
        }

        obj.put("ver", arr);

        return obj.toString();
    }

    //카테고리 트리 팝업 호출
    @RequestMapping( value="admin/store/appInfo/tree" ,method=RequestMethod.GET)
    public String CategoryTreeGet(Model model , HttpServletRequest request){

        CategorySearchDto  searchDto = new CategorySearchDto();
        searchDto.setCATG_CD("STORE");

        //카테고리조회
        List<CategoryDto> categoryTreeList = storeCategoryService.selectCategoryTreeList(searchDto);
        model.addAttribute("categoryTreeList", categoryTreeList);
        model.addAttribute( "flag", "appInfo" );  //카테고리화면에서 호출하는것과 구별하는 flag
        model.addAttribute( "layout", "layout/null.vm" );
        return "admin/store/category/categoryTree";
    }




    /**
     * 바이너리 파일 upload 후  등록할 파일정보 SETTING
     *
     * ios/Android 구분에 따른 bizLogic 추가됨에 따라 paltformType 파라메터 추가.
     * 2014. 1. 7. by UncleJoe
     * @param appInfoDto
     * @param file
     * @param platFormType
     * @return
     * @throws IOException
     */
    private  BinaryDto uploadBinaryFile(AppInfoDto appInfoDto, MultipartFile file, String platFormType) throws IOException, SocketException{

        //바이너리 파일내역  저장
        BinaryDto binaryDto = new BinaryDto();
        String ABSOLUTE_PATH = this.ABSOLUTE_PATH;

        if(!file.isEmpty()){
            //String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
            //String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");

            if(ABSOLUTE_PATH.endsWith(File.separator) == false) ABSOLUTE_PATH = ABSOLUTE_PATH + File.separator;

            String path = ABSOLUTE_PATH + BINARY_PATH + File.separator;

            File destinationDir = new File(path);
            //디렉토리생성
            FileUtils.forceMkdir(destinationDir);


            // 업로드 디렉토리 이동
            String orgFileName 	= file.getOriginalFilename();
            String fileExt 		= getFileExt(orgFileName);
            String fileName 	= UUID.randomUUID().toString();
            String fName 		= fileName + "." + fileExt;
            File toFile 		= new File(path + fName);
            file.transferTo(toFile);

            //FTP를 이용 동기화 처리
            //스토어 바이너리 파일 동기화 FTP 업데이트 전송
            boolean lm_bResourceSync = BooleanUtils.toBoolean(FTP_USE_FLAG);

            if(lm_bResourceSync)
                sendFTPResourceFile(path + fName, BINARY_PATH);

            logger.info("\n --- fName  ===========" + fName);
            logger.info("\n --- path + fName  ===========" + path + fName);

            binaryDto.setORG_BIN_FILE_NM(file.getOriginalFilename()); //원래 파일이픔
            binaryDto.setBIN_FILE_NM(fName); //변경된 파일이름
            binaryDto.setDL_URL(path); //경로
            binaryDto.setFILE_SIZE(String.valueOf(file.getSize())); //파일 사이즈

            //iOS의 경우 ipa를 plist 파일을 생성한다.
            if(platFormType.equals(TYPE_IOS)) {

                String title  	= appInfoDto.getAPP_NM();
                String version 	= appInfoDto.getBIN_VER();
                String identifier =  appInfoDto.getPAKG_ID_20();
                String plistFilePath = path + fileName + "." + IOSPlistInfo.getExtention();

                String url 		    = STORE_SERVICE_URL +"/" + BINARY_PATH +"/" + fileName + ".ipa";
                String imageMainUrl = STORE_SERVICE_URL +"/" + IMAGE_PATH +"/" + fileName;

                IOSPlistInfo.createiOSPlistFile(plistFilePath, title, url, imageMainUrl, version, identifier);

                if(lm_bResourceSync)
                    sendFTPResourceFile(path + fileName + ".plist", BINARY_PATH);
            }

        }

        binaryDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX()); //앱플랫폼 일련번호
        binaryDto.setPLAT_VER_IDX(appInfoDto.getPLAT_VER_IDX()); //플랫폼 버젼 일련번호
        binaryDto.setBIN_VER(appInfoDto.getBIN_VER() );
        binaryDto.setUPDATE_DESC(appInfoDto.getUPDATE_DESC_T());

        logger.info("binaryDto:::" + binaryDto.toString());


        return binaryDto;

    }

    /**
     * 바이너리 파일 upload 후  등록할 파일정보 SETTING
     *
     * ios/Android 구분에 따른 bizLogic 추가됨에 따라 paltformType 파라메터 추가.
     * 2014. 1. 7. by UncleJoe
     * @param appInfoDto
     * @param file
     * @param platFormType
     * @return
     * @throws IOException
     */
    private  BinaryDto uploadFile(AppInfoDto appInfoDto, MultipartFile file, String platFormType, String iosFullImg, String iosDisplayImg) throws IOException, SocketException{

        //바이너리 파일내역  저장
        BinaryDto binaryDto = new BinaryDto();
        String ABSOLUTE_PATH = this.ABSOLUTE_PATH;

        if(!file.isEmpty()){
            //String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
            //String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");

            if(ABSOLUTE_PATH.endsWith(File.separator) == false) ABSOLUTE_PATH = ABSOLUTE_PATH + File.separator;

            String path = ABSOLUTE_PATH + BINARY_PATH + File.separator;

            File destinationDir = new File(path);
            //디렉토리생성
            FileUtils.forceMkdir(destinationDir);


            // 업로드 디렉토리 이동
            String orgFileName 	= file.getOriginalFilename();
            String fileExt 		= getFileExt(orgFileName);
            String fileName 	= UUID.randomUUID().toString();
            String fName 		= fileName + "." + fileExt;
            File toFile 		= new File(path + fName);
            file.transferTo(toFile);

            //FTP를 이용 동기화 처리
            //스토어 바이너리 파일 동기화 FTP 업데이트 전송
            boolean lm_bResourceSync = BooleanUtils.toBoolean(FTP_USE_FLAG);

            if(lm_bResourceSync)
                sendFTPResourceFile(path + fName, BINARY_PATH);

            logger.info("\n --- fName  ===========" + fName);
            logger.info("\n --- path + fName  ===========" + path + fName);

            binaryDto.setORG_BIN_FILE_NM(file.getOriginalFilename()); //원래 파일이픔
            binaryDto.setBIN_FILE_NM(fName); //변경된 파일이름
            binaryDto.setDL_URL(path); //경로
            binaryDto.setFILE_SIZE(String.valueOf(file.getSize())); //파일 사이즈

            //iOS의 경우 ipa를 plist 파일을 생성한다.
            if(platFormType.equals(TYPE_IOS)) {

                String title  	= appInfoDto.getAPP_NM();
                String version 	= appInfoDto.getBIN_VER();
                String identifier =  appInfoDto.getPAKG_ID_20();
                String plistFilePath = path + fileName + "." + IOSPlistInfo.getExtention();

                String url 		    = STORE_SERVICE_URL +"/" + BINARY_PATH +"/" + fileName + ".ipa";
                String imageMainUrl = STORE_SERVICE_URL +"/" + IMAGE_PATH +"/" + fileName;
                String iosFullImgUrl = STORE_SERVICE_URL +"/" + IMAGE_PATH +"/" + iosFullImg;
                String iosDisplayImgUrl = STORE_SERVICE_URL +"/" + IMAGE_PATH +"/" + iosDisplayImg;

                IOSPlistInfo.createiOSPlistFile(plistFilePath, title, url, imageMainUrl, version, identifier ,iosFullImgUrl, iosDisplayImgUrl);

                if(lm_bResourceSync)
                    sendFTPResourceFile(path + fileName + ".plist", BINARY_PATH);
            }

        }

        binaryDto.setAPP_PLAT_IDX(appInfoDto.getAPP_PLAT_IDX()); //앱플랫폼 일련번호
        binaryDto.setPLAT_VER_IDX(appInfoDto.getPLAT_VER_IDX()); //플랫폼 버젼 일련번호
        binaryDto.setBIN_VER(appInfoDto.getBIN_VER() );
        binaryDto.setUPDATE_DESC(appInfoDto.getUPDATE_DESC_T());

        logger.info("binaryDto:::" + binaryDto.toString());


        return binaryDto;

    }

    private String getFileExt(String orgFileName){

        if(orgFileName.indexOf(".") > 0 ){
            return orgFileName.substring(orgFileName.lastIndexOf(".")+1,orgFileName.length());
        }else{
            return orgFileName;
        }
    }

    // 파일 upload 후  등록할 파일정보 SETTING
    private AppImageDto uploadImageFile(AppInfoDto appInfoDto, MultipartFile file, int idx) throws IOException, SocketException{

        //바이너리 파일내역  저장
        AppImageDto fileDto = new AppImageDto();

        if(!file.isEmpty()){

            String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
            String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");

//		    String path =DEFAULT_STORE_DIRECTORY + "appInfo" + File.separator +  "img" + File.separator + pathYear + File.separator + pathMonth + File.separator;

            // ------------------------------------------------------
            // 임시로 경로를 apk 로 수정하기 위해서 path year를 뺌...
            //String imageUrl = "/" + IMAGE_PATH + File.separator + pathYear;
            //String path = ABSOLUTE_PATH + IMAGE_PATH + File.separator + pathYear + File.separator ;
            // ------------------------------------------------------
            String ABSOLUTE_PATH = this.ABSOLUTE_PATH;
            String imageUrl = File.separator + IMAGE_PATH ;
            if(ABSOLUTE_PATH.endsWith(File.separator) == false) ABSOLUTE_PATH = ABSOLUTE_PATH + File.separator;
            String path = ABSOLUTE_PATH + IMAGE_PATH + File.separator ;

            File destinationDir = new File(path);
            FileUtils.forceMkdir(destinationDir);  //디렉토리생성

            String filePathName ="";
            String orgFileName = file.getOriginalFilename();
            // 업로드 디렉토리 이동
            int lastIdx = 0;
            if(orgFileName.indexOf(".") > 0 ){
                lastIdx = orgFileName.lastIndexOf(".");
            }

            String fileName = UUID.randomUUID().toString();

            String fileNoExtName = getFileExt(orgFileName);//(System.currentTimeMillis()+"_") + orgFileName.substring(0,  lastIdx );
            String fileFullName = fileName + "." + fileNoExtName;// (System.currentTimeMillis()+"_") + orgFileName; //확장자 포함된 파일명


            File toFile = new File(path + fileFullName);
            file.transferTo(toFile);

            //FTP를 이용 동기화 처리
            //스토어 바이너리 파일 동기화 FTP 업데이트 전송
            boolean lm_bResourceSync = BooleanUtils.toBoolean(FTP_USE_FLAG);
            if(lm_bResourceSync)
                sendFTPResourceFile(path + fileFullName, IMAGE_PATH);

            logger.info("\n --- filePathName  ===========" + filePathName);

            fileDto.setAPP_IDX(appInfoDto.getAPP_IDX() ); //앱정보 일련번호
            //fileDto.setFILE_NM(fileFullName); //변경된 파일이름
            fileDto.setFILE_NM(fileName); //변경된 파일이름
            fileDto.setEXT_NM(fileNoExtName);//org.springframework.util.StringUtils.getFilenameExtension(orgFileName));
            fileDto.setFILE_SIZE(String.valueOf(file.getSize()));
            fileDto.setIMG_PATH(path);
            fileDto.setDL_URL(imageUrl.replace("\\", "/"));


            fileDto.setREG_ID(appInfoDto.getREG_ID());

            if(idx < 4){
                fileDto.setIMG_TYPE("10");	 //IMG_TYPE : thumbNail Image - 10,  preview Image - 20
                if(idx == 1){
                    fileDto.setIMG_W("120");
                    fileDto.setIMG_H("120");
                }else if(idx == 2){
                    fileDto.setIMG_W("75");
                    fileDto.setIMG_H("75");
                }else if(idx == 3){
                    fileDto.setIMG_W("30");
                    fileDto.setIMG_H("30");

                }
            }else{
                fileDto.setIMG_TYPE("20");
                fileDto.setIMG_W("120");
                fileDto.setIMG_H("240");
            }
            //DISP_NO 는 INSERT 전에 SET
            //fileDto.setORG_BIN_FILE_NM(file.getOriginalFilename()); //원래 파일이픔
        }
        return fileDto;

    }


    // 앱정보 등록의 3번째 화면을 불러 옴.
    @RequestMapping( value="admin/store/appInfo/loadMenu3" ,method=RequestMethod.POST)
    public String LoadMenu3Post(Model model , HttpServletRequest request){

        AppInfoSearchDto searchDto = new AppInfoSearchDto();
        searchDto.setCATG_CD("STORE");
        searchDto.setAPP_IDX(request.getParameter("APP_IDX"));

        @SuppressWarnings("rawtypes")
        Enumeration pnames = request.getParameterNames();
        HashMap<String, String> paramMap = new HashMap<String, String>();
        while (pnames.hasMoreElements()) {
            String pname = (String)pnames.nextElement();
            paramMap.put(pname, request.getParameter(pname));
        }


        searchDto.setPLAT_CD(TYPE_ANDROID); //안드로이드
        List<PlatformDto> verList10 = appInfoService.selectPlatformVerListByPlatCd(searchDto);
        searchDto.setPLAT_CD(TYPE_IOS); //애플
        List<PlatformDto> verList20 = appInfoService.selectPlatformVerListByPlatCd(searchDto);

        //searchDto.setServiceUrl(STORE_SERVICE_URL);
        searchDto.setServiceUrl(ABSOLUTE_PATH);

        model.addAttribute("verList10",    verList10);//플랫폼버젼 목록(select box)
        model.addAttribute("verList20",    verList20);//플랫폼 버젼목록(select box)
        model.addAttribute("distList",     appInfoService.selectCommoncode("ST006"));//IOS배포타입(select box)

        model.addAttribute("platList",     deviceService.selectPlatformList());//플랫폼 목록(select box)
        if(searchDto.getAPP_IDX() != null && !"".equals(searchDto.getAPP_IDX())) {
            model.addAttribute("appImageInfo", appInfoService.selectAppImageInfo(searchDto));
        }
        model.addAttribute("scval", paramMap);
        model.addAttribute( "layout", "layout/null.vm" );

        return "admin/store/appInfo/appInfoAdd_menu3";
    }

    private boolean sendFTPResourceFile(String localFile, String remoteNewDir) throws SocketException, IOException  {
        boolean result = false;
//		 System.out.println("sendResourceFile(FTP) enter");
//		 PropertiesPropertySource props = (PropertiesPropertySource) applicationContext.getEnvironment().getPropertySources().get("msp-properties");

        String[] ftpTargets = mspConfig.getStringArray("storeserver.ftp.target.list");

        for (String lm_sTarget : ftpTargets) {

            String lm_sFtpType 	= mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".type");
            String lm_sHostName = mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".hostname");
            int lm_iPort 		= mspConfig.getInt("storeserver.ftp.target." + lm_sTarget + ".port");
            String lm_sId 		= mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".id");
            String lm_sPassword = mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".password");
            String lm_sRemotePath = mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".remotepath") + remoteNewDir+"/";

            String lm_privateKey = "";
            String lm_passphrase = "";
            if(lm_sFtpType.equals("sftp2")){
                lm_privateKey = mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".privatekey");
                lm_passphrase = mspConfig.getString("storeserver.ftp.target." + lm_sTarget + ".passphrase");
                logger.debug("##### lm_privateKey:"+lm_privateKey+"     lm_passphrase:"+lm_passphrase+"   lm_sRemotePath:"+lm_sRemotePath);
            }
        
        /*
        CompositeConfiguration props = mspConfig.getConfiguration();
        String[] lm_sTargets = StringUtils.split((String) props.getProperty("storeserver.ftp.target"), ";");

        for (String lm_sTarget: lm_sTargets) {

            String lm_sFtpType = (String) props.getProperty("storeserver.ftp.target." + lm_sTarget + ".type");
            String lm_sHostName = (String) props.getProperty("storeserver.ftp.target." + lm_sTarget + ".hostname");
            int lm_iPort = NumberUtils.toInt((String) props.getProperty("storeserver.ftp.target." + lm_sTarget + ".port"));
            String lm_sId = (String) props.getProperty("storeserver.ftp.target." + lm_sTarget + ".id");
            String lm_sPassword = (String) props.getProperty("storeserver.ftp.target." + lm_sTarget + ".password");
            String lm_sRemotePath = (String) props.getProperty("storeserver.ftp.target." + lm_sTarget + ".remotepath") + remoteNewDir+"/";
         
            IFTPClientUtils ftp = null;
            if(lm_sFtpType.equals("sftp")){
                ftp = new SFTPClientUtils();
            }else{
                ftp = new FTPClientUtils();
            }
            */
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

            try {
                ftp.putFileToServer(localFile, lm_sRemotePath);
                ftp.disconnection();
                result = true;
            } catch (SocketException e) {
                logger.error("Exception caught.", e);
                throw new SocketException("FTP "+messageSource.getMessage("common.text.server", null, LocaleContextHolder.getLocale())+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null,Locale.getDefault())); //서버 / 연결에 실패하였습니다.
            } catch (IOException e) {
                logger.error("Exception caught.", e);
                throw new SocketException("FTP "+messageSource.getMessage("common.text.server", null, LocaleContextHolder.getLocale())+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null,Locale.getDefault())); //서버 / 파일 전송 실패하였습니다..
            }
        }
        return result;
    }


    /**<pre>
     * 모바일 관리 ==> 앱 업데이트 관리 ==> 모바일 업데이트 등록/수정
     * 해당 app_id, system_name에 따른 store binary url정보 조회
     * </pre> 
     * @param request
     * @param model
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @since 2014. 6. 9. by UncleJoe
     */
    @RequestMapping(value="admin/store/appInfo/binaryUrl")
    @ResponseBody
    public String getRecentBinaryUrl(HttpServletRequest request, Model model) throws JsonGenerationException, JsonMappingException, IOException{
		
    	/*
    	 * 1. os, package_id 를 이용하여 app_plat_idx값 조회
    	 * 2. 리스트 조회후 첫번째 값 조회
    	 * 3. apk, plist값 만들어준다.  
    	 */

        String pakgId = ServletRequestUtils.getStringParameter(request, "package_id", "");
        String systemName = ServletRequestUtils.getStringParameter(request, "system_name", "");

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("package_id", pakgId);
        param.put("system_name", systemName.equals("A")?"Android":"iOS");
        Map<String, Object> appInfo = this.appInfoService.getAppNoByPakgIdAndSystemName(param);

        int appPlatIdx = MapUtils.getIntValue(appInfo, "APP_NO");


        String url = "";
        BinaryDto binaryDto = new BinaryDto();
        binaryDto.setAPP_PLAT_IDX(String.valueOf(appPlatIdx));
        logger.info("getRecentBinaryUrl START >>>>>>>>>>>>>"  +  binaryDto.getAPP_PLAT_IDX());

        binaryDto.setPAGE_SIZE(9999);
        List<BinaryDto>  binaryHistoryList = appInfoService.selectBinaryHistoryList(binaryDto);
        if(binaryHistoryList.size() == 0){
            //MSG 알림. 스토어에 등록된 바이너리 파일이 없습니다.
            url = "";
        }else {
            BinaryDto data = binaryHistoryList.get(0);
            String STORE_SERVICE_URL = this.STORE_SERVICE_URL;
            String BINARY_PATH = this.BINARY_PATH;
            String fileName = data.getBIN_FILE_NM().replace(".ipa", ".plist"); //iOS ipa는 plist로 변경하여 전달한다.

            if (STORE_SERVICE_URL.endsWith("/"))
                STORE_SERVICE_URL = STORE_SERVICE_URL.substring(0, STORE_SERVICE_URL.length() - 1);

            if (BINARY_PATH.endsWith("/") == false)
                BINARY_PATH = BINARY_PATH + "/";

            if (BINARY_PATH.startsWith("/") == false)
                BINARY_PATH = "/" + BINARY_PATH;

            if (fileName.startsWith("/") == true)
                fileName = fileName.substring(1);

            url = STORE_SERVICE_URL + BINARY_PATH + fileName;

            if(systemName.equals("I")){
                url = "itms-services://?action=download-manifest&url="+url;
            }

        }

        HashMap<String, Object> map = new HashMap<String,Object>();
        map.put("result", url);

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(map);

    }


}