package kr.msp.admin.push;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.msp.admin.base.collection.SingleData;
import kr.msp.admin.base.urlconnect.PushUrlConnect;
import kr.msp.admin.base.utils.ftp.FTPClientUtils;
import kr.msp.admin.base.utils.ftp.IFTPClientUtils;
import kr.msp.admin.base.utils.ftp.SFTPClientUtils;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.group.dto.PushUserGroupDto;
import kr.msp.admin.push.msgSend.dto.MsgSendDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;
import kr.msp.admin.push.sendType.dto.PushSendTypeDto;
import kr.msp.admin.push.template.dto.TemplateDto;
import kr.msp.admin.push.user.dto.PushUserDto;

//import com.oreilly.servlet.MultipartRequest;
//import org.springframework.web.multipart.MultipartRequest;

@RequestMapping(value="admin/push")
@Controller
public class PushMsgSendManageController {

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;

	//발송관련 업로드 기본경로
	private @Value("${admin.push.message.absolute_path}") String absolute_path;
	//발송관련 이미지 업로드  폴더명
	private @Value("${admin.push.message.imageFolderName}") String imageFolderName;
	//발송관련 html 업로드  폴더명
	private @Value("${admin.push.message.htmlContentFolderName}") String htmlContentFolderName;
	//최종 부가정보가 담긴 html 업로드  폴더명
	private @Value("${admin.push.message.totalInfoFolderName}") String totalInfoFolderName;
	//발송관련 업로드 기본경로(웹상 접근경로
	private @Value("${admin.push.message.absolute_path_web}") String absolute_path_web;
    //CSV파일 전송 임시저장폴더
    private @Value("${admin.push.message.csvFolderName:csv}") String csvFoldername;
    //FTP로 CSV 리시버 파일 전송여부
    private @Value("${pushserver.receiver.csv_send_yn:false}") String FTP_CSV_FLAG;	//FTP로 CSV파일 전송 여부

    private @Value("${datasource.dbtype:oracle}") String DBTYPE;	//DB 타입가져오기

    //FTP로 리소스파일 sync 맞출 여부
    private @Value("${pushserver.resource.sync:false}") String FTP_USE_FLAG;	//FTP로 리소스파일 sync 맞출 여부

    @Qualifier("mspXmlConfiguration")
    @Autowired(required = true)
    private Configuration mspConfig;

	@Autowired(required=true)
	private MsgSendManageService msgSendManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 

	private @Value("${admin.push.receiver.host}") String DEFAULT_RECEIVER_HOST;	//기본 리소스 디렉토리

    private static Logger logger = LoggerFactory.getLogger(PushMsgSendManageController.class);

	@RequestMapping(value="msgSend", method=RequestMethod.GET)
	public String pushMsgSendGet(Model model, HttpServletRequest request){

		//PUSH 사용자 조회
		PushUserDto pushUser = new PushUserDto();
		pushUser.setPAGE_NUM(1);
		pushUser.setPAGE_SIZE(row_size);
		
		/*
		 * 2014년 8월 19일 : 발송 대상자 중 서비스 가입한 사용자에 대해서만 보회 가능하도록 수정
		 */
//		List<PushUserDto> pushUserList = msgSendManage.SelectPushUserService(pushUser);
        Map<String,String> reqMap = new HashMap<String, String>();
//		int totalPushRegDevice = msgSendManage.SelectPushServiceRegCount(reqMap);
		
		
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = msgSendManage.SelectPushServiceAll(pushService);

		//템플릿 조회
		List<TemplateDto> templateList = msgSendManage.SelectTemplateAll();

		//발송유형 조회
		List<PushSendTypeDto> sendTypeList  =  msgSendManage.SelectSendTypeAll();

		//PUSH 구룹 조회
		List<PushUserGroupDto> pushUserGroupList = msgSendManage.SelectGroupAll();


		model.addAttribute("pushUserGroupList", pushUserGroupList);
		model.addAttribute("pushUserList", null);
        model.addAttribute("totalPushRegDevice",0);
		model.addAttribute("pushServiceList", pushServiceList);
		model.addAttribute("templateList", templateList);
		model.addAttribute("sendTypeList", sendTypeList);
		model.addAttribute("R_PAGE_NUM",pushUser.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);


		return "admin/push/msgSend/msgSendMain";
	}

    @RequestMapping(value="getAllSendUserCntJson", method=RequestMethod.POST)
    public ModelAndView getAllSendUserCntJson(){
        Map<String,String> reqMap = new HashMap<String, String>();
        int totalPushRegDevice = msgSendManage.SelectPushServiceRegCount(reqMap);
        ModelAndView mv = new ModelAndView("jsonView");
        mv.addObject("TOT_PSID_CNT",totalPushRegDevice);
        return mv;
    }


	@RequestMapping(value="msgSend/user" , method=RequestMethod.POST)
	public String pushMsgSendUserGet(Model model, PushUserDto pushUser){

		//PUSH 사용자 조회
		pushUser.setPAGE_SIZE(row_size);
//		List<PushUserDto> pushUserList = msgSendManage.SelectPushUserService(pushUser);
        List<PushUserDto> pushUserList = msgSendManage.SelectPushUserServiceRegistration(pushUser);

        for(PushUserDto pushUserDto : pushUserList){
            logger.debug("############ "+ pushUserDto.getPSID());
        }

        model.addAttribute("pushUserList", pushUserList);
		model.addAttribute("R_PAGE_NUM",pushUser.getPAGE_NUM());
		model.addAttribute("layout", "layout/null.vm");
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);


		return "admin/push/msgSend/msgSendUser";
	}

	@RequestMapping(value="msgSend/group" , method=RequestMethod.POST)
	public String pushMsgSendGroupGet( Model model , PushUserGroupDto pushUserGroup ){

		pushUserGroup.setPAGE_SIZE(row_size);
		List<PushUserGroupDto> pushUserGroupList = msgSendManage.SelectGroupList(pushUserGroup);

		model.addAttribute( "pushUserGroupList", pushUserGroupList );
		model.addAttribute("R_PAGE_NUM",pushUserGroup.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm");

		return "admin/push/msgSend/msgSendGroup";
	}

	//사용자 등록
//	@ResponseBody
	@RequestMapping(value="msgSend/send", method=RequestMethod.POST)
	public ModelAndView pushMsgSendSendPost(HttpServletRequest request, MsgSendDto msgSend , HttpServletResponse response
			,@RequestParam("imageFile") MultipartFile IMAGE_FILE, @RequestParam("CSV_FILE") MultipartFile CSV_FILE){

		//<20140221-JHW> 메시지 발송정보가 추가 및 수정됨에 따라 새로운 메시지 포맷팅 시작
		String imageUploadUrl = "";
		String htmlContentsUrl = "";
        String newExt = "";

        String saveCsvFileAbsPath = "";
        boolean isCSVFileUpload = false;

        Map<String,Object> returnMap = new HashMap<String, Object>();
        Map<String,Object> resultMap = null;

        try {
            //Image 파일 업로드 IMAGE_FILE
            if(IMAGE_FILE !=null && IMAGE_FILE.getSize()>0){ // CSV파일 업로드
                imageUploadUrl = msgImageUpload(IMAGE_FILE);
            }

            //CSV파일 파일 업로드 매핑
            if(CSV_FILE !=null && CSV_FILE.getSize()>0){ // CSV파일 업로드
                //isCSVFileUpload 변수를 전역으로 잡지않은 이유는 전역변수 일경우 해당 클래스가 싱글톤이기 때문 동시에 다른 사용자의 업로드가 있을시 문제소지가 있기 때문.
                saveCsvFileAbsPath = msgCSVUpload(CSV_FILE);
                isCSVFileUpload = true; // 위에서 에러가 발생하면 exception 발생 하므로 이 부분이 true가 되지 않음
            }

            //WEB EDITOR을 사용했을 경우 html파일을 만든다.
            if(msgSend.getMsgCategory().equals("1") || msgSend.getMsgCategory().equals("4")){
                htmlContentsUrl = htmlUpload(0, msgSend.getWEBEDIT());
            }else{
                htmlContentsUrl = messageFormat(msgSend.getEXT(), htmlContentsUrl, msgSend.getMsgCategory(), msgSend.getVideoUrl(), imageUploadUrl);
            }
            newExt = htmlContentsUrl;

            /*

            //방안1 내용이없으면 만들지도 않음
            if(msgSend.getWEBEDIT() != null && !"".equals(msgSend.getWEBEDIT())){
                htmlContentsUrl = htmlUpload(0, msgSend.getWEBEDIT());
            }

            //기본메세지가 아닐 경우만... Ext html파일을 만들고 만든 html url정보를 넘겨 클라이언트 앱이 해당 url을 호출하여 JSON 데이타를 파싱하게 만든다.
            if(msgSend.getMsgCategory().equals("0")){
                if(!"".equals(msgSend.getEXT().trim())){
                    //1.ext에 대하여 사용자가 선택한 구분값에 따라 주요 정보를 파이프(|)구분자를 두어 포맷한다.
                    newExt = messageFormat(msgSend.getEXT(), htmlContentsUrl, msgSend.getMsgCategory(), msgSend.getVideoUrl(), imageUploadUrl);
                    //2.이렇게 포맷된 문자열(newExt)을 html에 write 하여 저장된 url 주소가 최종 정보가 도니다.
                    newExt = htmlUpload(1, newExt);
                }
            }else{
                //1.ext에 대하여 사용자가 선택한 구분값에 따라 주요 정보를 파이프(|)구분자를 두어 포맷한다.
                newExt = messageFormat(msgSend.getEXT(), htmlContentsUrl, msgSend.getMsgCategory(), msgSend.getVideoUrl(), imageUploadUrl);
                //2.이렇게 포맷된 문자열(newExt)을 html에 write 하여 저장된 url 주소가 최종 정보가 도니다.
                newExt = htmlUpload(1, newExt);
            }
            */


            String TEMPLATE_YN = msgSend.getTEMPLATE_YN();			// 템플릿 사용여부
            String USER_TYPE   = msgSend.getUSER_TYPE();			// 대상 타입 (ALL:전체, GROUP:그룹, EACH:개인)
            String SEND_TYPE   = msgSend.getSEND_TYPE();			// 발송 타입 (I:즉시, R:예약)

            SingleData paramData = new SingleData();
            {// 메시지 공통
                paramData.add("APP_ID", 	 msgSend.getAPPID());					// APPID
                paramData.add("SOUNDFILE",   "alert.aif");							// 사운드파일명
                paramData.add("BADGENO", 	 "1");									// 뱃지번호
                paramData.add("PRIORITY", 	 msgSend.getPRIORITY());				// 우선순위
    //			paramData.add("EXT", 		 msgSend.getEXT());						// 확장파라미터
                paramData.add("EXT", 		 newExt);								// <20140221-JHW> 이미지 및 동영상 url 정보가 표시된 새로운 EXT
                paramData.add("SENDERCODE",  "admin");								// 발송자 (admin)
                paramData.add("SERVICECODE", msgSend.getSERVICECODE());				// 발송정책
                paramData.add("TEMPLATE_YN", TEMPLATE_YN);                           //템플릿 사용여부
                paramData.add("DB_IN", msgSend.getDB_IN());     
                // 즉시
                if(SEND_TYPE.equals("I")){
                    paramData.add("RESERVEDATE", "");
                // 예약
                }else if(SEND_TYPE.equals("R")){
                    paramData.add("RESERVEDATE", msgSend.getSEND_DATE() + " " + msgSend.getSEND_HOUR() + msgSend.getSEND_MINUTE() + "00");
                }
            }

            paramData.add("MESSAGE", URLEncoder.encode(msgSend.getMESSAGE(), "UTF-8"));					// 메시지 내용

            // 전체 전송
            if(USER_TYPE.equals("ALL")){
                paramData.add("TYPE", "A");									// 구분
                paramData.add("GROUPSEQ", "");								// 그룹 일련번호
                resultMap = sendIF(response, DEFAULT_RECEIVER_HOST + "/rcv_register_groupmessage.ctl", paramData);
            // 그룹 전송
            }else if(USER_TYPE.equals("GROUP")){
                paramData.add("TYPE", "G");									// 구분
                paramData.add("GROUPSEQ", msgSend.getGROUPSEQ());			// 그룹 일련번호
                resultMap = sendIF(response, DEFAULT_RECEIVER_HOST +"/rcv_register_groupmessage.ctl", paramData);
            // 개인 전송
            }else if(USER_TYPE.equals("EACH")){
                String CUIDS[] 	   = request.getParameterValues("CUIDS");
                String USER_SELS[] = request.getParameterValues("USER_SELS");
                String PSID_SELS[] = request.getParameterValues("PSID_SELS");
                ////CUID배열로 보내기
                JSONArray cuidJsonArray = new JSONArray();
                for (int i = 0; i < CUIDS.length; i++) {
                    //배열로 보내기(리시버 구현 되어 있을시)
                    cuidJsonArray.put(CUIDS[i]);
                }
                ////보낼서버(PNSID)배열로 보내기
                JSONArray psidJsonArray = new JSONArray();
                for (int i = 0; i < PSID_SELS.length; i++) {
                    //배열로 보내기(리시버 구현 되어 있을시)
                    psidJsonArray.put(PSID_SELS[i]);
                }
                paramData.add("CUID", cuidJsonArray);
                paramData.add("REQ_PSIDS", psidJsonArray); //발송 요청 PSID
                resultMap = sendIF(response, DEFAULT_RECEIVER_HOST + "/rcv_register_message.ctl", paramData);
                // CSV 전송
            }else if(USER_TYPE.equals("CSV")){ //csv 파일을 이용한 전송
                if(!isCSVFileUpload){
                    throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.noFileData", null, LocaleContextHolder.getLocale())); //파일 데이터가 없습니다.
                }
                //리시버 첨부파일 기능 구현 했을때
                resultMap =  multipartSendIF(DEFAULT_RECEIVER_HOST +"/rcv_register_csvmessage.ctl", paramData,saveCsvFileAbsPath);
            }

            // view에 보낼 알림메세지 만들기
            if(resultMap.get("resultCode").equals("0000") || resultMap.get("resultMsg").equals("OK")){
                returnMap.put("resultCode", "0000");
                returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.msgSendSuccess", null, LocaleContextHolder.getLocale())); //"메시지 발송요청이 성공하였습니다."
                if(resultMap.containsKey("bodyData")){
                    returnMap.put("bodyData", resultMap.get("bodyData"));
                }
            }else{
                returnMap.put("resultCode", "500");
                returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.sendFail", null, LocaleContextHolder.getLocale())+":"+resultMap.get("resultMsg")); //발송실패
            }

        }catch(Exception e){
            returnMap.put("resultCode","500");
            returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.sendFail", null, LocaleContextHolder.getLocale())+":"+e.toString()); //발송실패
        }

        ModelAndView mv = new ModelAndView("jsonView");
        mv.addAllObjects(returnMap);
        return mv;
	}
    // 리시버 첨부파일 전송
    public Map<String,Object> multipartSendIF(String url, SingleData paramData, String attFileSrc) throws Exception{

        Map<String,Object> resultMap = new HashMap<String, Object>();

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 5000);
        params.setParameter("ENCTYPE", "multipart/form-data");

        HttpClient client = new DefaultHttpClient(params);
        HttpPost post = new HttpPost(url);
//        post.addHeader("user_data_enc", "Y");
        post.setHeader("ENCTYPE", "multipart/form-data");

        MultipartEntity entry = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        Map<String,Object> reqParams = paramData.getHashMap();
        Iterator<String> keys = reqParams.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            entry.addPart(key, new StringBody((String)reqParams.get(key), "text/plain", Charset.forName("UTF-8")));
        }
        logger.info("####### attFileSrc:"+attFileSrc);
        File file = new File(attFileSrc);
        ContentBody fileBody = new FileBody(file);
        entry.addPart("upload_file", fileBody);

        post.setEntity(entry);

        HttpResponse response = client.execute(post);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity he = response.getEntity();
        System.out.println("status:"+status);
        String resStr = "";
        String resCode = "";

        if (status == HttpStatus.SC_OK) {
            InputStream is = he.getContent();
            BufferedReader bufRd = new BufferedReader(new InputStreamReader(is, "utf-8"));
            StringBuffer sb             = new StringBuffer();
            String line = null;
            while ((line = bufRd.readLine()) != null) {
                sb.append(line + "\n");
            }

            JSONObject json = new JSONObject( sb.toString());
            JSONObject respHeadJson = json.getJSONObject("HEADER");

            resCode = respHeadJson.getString("RESULT_CODE");
            resStr = respHeadJson.getString("RESULT_BODY");  // 응답 결과 메세지
            if(!resCode.equals("0000")){
                throw new SocketException(messageSource.getMessage("menu.push.controller.errorCode", null, LocaleContextHolder.getLocale())
                		+" : ["+resCode+"] "+messageSource.getMessage("menu.push.controller.errorMsg", null, LocaleContextHolder.getLocale())+":"+resStr); //에러응답코드   오류메세지
            }else{
                resStr = json.get("BODY").toString();
                Map<String,Object> httpRawBodyMap = new Gson().fromJson(resStr,new TypeToken<HashMap<String, Object>>() {}.getType());
                resultMap.put("bodyData",httpRawBodyMap);
            }
        } else {
            throw new SocketException(messageSource.getMessage("menu.push.controller.errorCode", null, LocaleContextHolder.getLocale())
            		+" : ["+status+"] "+messageSource.getMessage("menu.push.controller.receiverError", null, LocaleContextHolder.getLocale())); //에러응답코드     리시버 응답 오류입니다.
        }

        resultMap.put("resultCode","0000");
        resultMap.put("resultMsg",resStr);
//        System.out.println("###### resultMap:"+new JSONObject(resultMap).toString());
        return resultMap;

    }
	// 인터페이스 전송
	public Map<String,Object> sendIF(HttpServletResponse response, String url, SingleData paramData) throws Exception{

		PushUrlConnect PushUrlCon   = new PushUrlConnect();
		SingleData returnData           = new SingleData();
        Map<String,Object> resultMap = new HashMap<String, Object>();

		{//속성 셋팅
			PushUrlCon.setDoInput(true);
			PushUrlCon.setDoOutput(true);
			PushUrlCon.setDoOutput(true);
			PushUrlCon.setRequestMethod("POST");
			PushUrlCon.setConnectTimeout(5000); // 5초
			PushUrlCon.setReadTimeout(90000); //1분대기
			PushUrlCon.setReadEncodingType("UTF-8");

			PushUrlCon.setParam  = paramData;
			PushUrlCon.setUrl    = url;
		}

		{//호출
			try {
				returnData = PushUrlCon.getUrlConnect();
				JSONObject json = new JSONObject(returnData.getString("RESULT"));
				JSONObject jsonHeader = new JSONObject(json.get("HEADER").toString());
                String resultCode = jsonHeader.get("RESULT_CODE").toString();
                String resultMsg = jsonHeader.get("RESULT_BODY").toString();
                String resStr = "";
                try {
                    resStr = json.get("BODY").toString();
                    Map<String,Object> httpRawBodyMap = new Gson().fromJson(resStr,new TypeToken<HashMap<String, Object>>() {}.getType());
                    resultMap.put("bodyData",httpRawBodyMap);
                }catch (Exception e1){  //리시버에서 BODY 값을 안넘길 경우 예외처리
                    resultMap.put("bodyData","JSON Parse Error");
                }
                if(resultCode!=null && resultCode.equals("0000")){ //발송성공
                    resultMap.put("resultCode","0000");
                }else{ //발송 실패
                    resultMap.put("resultCode",resultCode);
                }
                resultMap.put("resultMsg",resultMsg);
            } catch (Exception e) {
                logger.error("Exception caught.", e);
                throw new SocketException(messageSource.getMessage("menu.push.controller.notConnect", null, LocaleContextHolder.getLocale())); //리시버와 연결되지 않았거나 데이타 오류입니다.
			}finally{

			}
		}

		return resultMap;
	}


	/**
	 * 요청된 이미지 파일을 저장 후 저장된 위치정보를 반환
	 * @param file : 발송시 저장한 이미지 정보
	 * @return : 이미지가 저장된 url
	 */
	public String msgImageUpload(MultipartFile file) throws Exception{
		String path = "";
		String path_web = "";
		String fName = "";
        if(!file.isEmpty()){
            path = absolute_path + imageFolderName + File.separator;
            path_web = absolute_path_web + imageFolderName + File.separator;

            File destinationDir = new File(path);
            //디렉토리생성
            FileUtils.forceMkdir(destinationDir);

            //휘원추가
            SimpleDateFormat sdt = new SimpleDateFormat("yyyyMMddHHmmssSS");
            String namePrefix = sdt.format(Calendar.getInstance().getTime());
            //휘원추가 끝.
            String orgFileName = file.getOriginalFilename();
            int index = orgFileName.lastIndexOf(".");
            if (index != -1) {
                String fileExt  = orgFileName.substring(index + 1);
                fName = namePrefix +"."+fileExt;
                if(!fileExt.toLowerCase().equals("gif") && !fileExt.toLowerCase().equals("jpg") && !fileExt.toLowerCase().equals("jpeg") && !fileExt.toLowerCase().equals("png")){
                    throw new Exception(messageSource.getMessage("menu.push.controller.extensionLimit", null, LocaleContextHolder.getLocale()));  //"확장자가 GIF, JPG, PNG 이미지 파일만 첨부 가능합니다."
                }
            }else{
                throw new Exception(messageSource.getMessage("menu.push.controller.noExtension", null, LocaleContextHolder.getLocale())); //확장자가 존재하지 않습니다.
            }

            File toFile = new File(path + fName);
            file.transferTo(toFile);

            //푸쉬 이미지 배포 파일 동기화 FTP 업데이트 전송
            boolean lm_bResourceSync = BooleanUtils.toBoolean(FTP_USE_FLAG);
            if(lm_bResourceSync) {
                sendFTPResourceFile(path + fName, imageFolderName);
            }
        }

		return path_web + fName;
	}

    /**
     * 요청된 csv 파일을 저장 후 저장된 위치정보를 반환
     * @param CSV_FILE : 발송 CSV 파일
     * @return : 저장된 CSV 파일명
     */
    public String msgCSVUpload(MultipartFile CSV_FILE) throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(CSV_FILE.getInputStream()));
        String line = null;
        int i=0;
        while((line = in.readLine()) != null) {
            if(line.indexOf(",")<0){
                throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.notCorrectFile", null, LocaleContextHolder.getLocale())
                		+" CUID, "+messageSource.getMessage("menu.push.controller.needName ", null, LocaleContextHolder.getLocale())); // 파일 데이터가 올바르지 않습니다. / 이름이 있어야 합니다.
            }else{
                String[] chkValues = line.split(",");
                if(chkValues.length!=2){
                    throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.notCorrectFile", null, LocaleContextHolder.getLocale())
                    		+" CUID, "+messageSource.getMessage("menu.push.controller.needName ", null, LocaleContextHolder.getLocale())); // 파일 데이터가 올바르지 않습니다. / 이름이 있어야 합니다.
                }
            }
            i++;
            if(i==2){ //두라인 정보만 체크(웹이기 때문 대용량일 경우 Reponse Time out 날 수 있기 때문)
                break;
            }
        }
        if(i==0){
            throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.noFileData", null, LocaleContextHolder.getLocale())); //"CSV 파일 데이터가 없습니다." 
        }
        String csvAbsFolderSrc = absolute_path + csvFoldername;
        String orgFileName = CSV_FILE.getOriginalFilename();
        int index = orgFileName.lastIndexOf(".");
        String orgFileNameExt  = orgFileName.substring(index + 1);

        String nowSysDate = new SimpleDateFormat("yyyyMMddHmsS").format(new Date());  //현재시간
//        String csvSaveFileName = CSV_FILE.getOriginalFilename(); //한글파일명일 경우 깨짐
        String csvSaveFileName = nowSysDate+"."+orgFileNameExt;
        File file = new File(csvAbsFolderSrc + File.separator + csvSaveFileName);
        File makeFolder = new File(csvAbsFolderSrc);
        FileUtils.forceMkdir(makeFolder);
        CSV_FILE.transferTo(file);
        //리시버 서버의 FTP 전송로직
        //푸쉬 이미지 배포 파일 동기화 FTP 업데이트 전송
        boolean lm_csvSync = BooleanUtils.toBoolean(FTP_CSV_FLAG);
        logger.info("###### CSV FTP 설정값: "+lm_csvSync);
        if(lm_csvSync) {
            sendFTPCsvFile(file.getAbsolutePath(), "");
        }
        return file.getAbsolutePath();
//        return csvSaveFileName;
    }

	/**
	 * 사용자가 선택한 구분값에 따라 각 내용을 구분자 | 를 이용하여 조합한 문자열을 리턴
	 * @param ext : Notification 내용
	 * @param webEditUrl : WEB PAGE 에 작성된 내용이 저장된 HTML파일이 업로드되어있는 URL
	 * @param category : 구분
	 * @param videoUrl : 동영상(URL)
	 * @param imageUrl : 업로드한 IMAGE 파일이 저장되어있는 URL
	 * @return
	 */
	public String messageFormat(String ext, String webEditUrl, String category, String videoUrl, String imageUrl){
		String result = "";
		switch(Integer.parseInt(category)){
		case 0:
			result = category+"|"+ext;
			break;
		case 1:
			result = category+"|"+ext+"|"+imageUrl+"|"+webEditUrl;
			break;
		case 2:
			result = category+"|"+ext+"|"+imageUrl+"|"+videoUrl;
			break;
		case 3:
			result = category+"|"+ext+"|"+imageUrl;
			break;
		case 4:
			result = category+"|"+ext+"|"+webEditUrl;
			break;
		}
		return result;
	}

	
	/**
	 * html 에 파라미터로 입력된 내용을 write하여 업로드 한 뒤, 업로드 되어있는 url을 리턴
	 * @param type : 0 일경우 WEBPAGE 내용 업로드, 1일경우 EXT정보 업로드 
	 * @param contents : type이 0일경우 WEBPAGE 내용, type이 1일경우 포맷된 ext 문자열
	 * @return 해당내용으로 구성된 html파일이 업로드된 위치
	 */
	private String htmlUpload(int type, String contents) throws Exception{
		String path = "";
		String path_web = "";
		String fName = "";
		String suffix = "";
		FileWriter file_writer = null;
		BufferedWriter buff_writer = null;
		PrintWriter print_writer = null;
		//web editor로 작성한 html이 업로드 되는 폴더
        String UploadDetailFolder = htmlContentFolderName;
		if(type == 0){
			path = absolute_path + htmlContentFolderName + "/";
			path_web = absolute_path_web + htmlContentFolderName + "/";
		//최종ext 정보가 담긴 html이 업로드 되는 폴더
		}else{
            UploadDetailFolder = totalInfoFolderName;
			suffix = "_I";
			path = absolute_path + totalInfoFolderName + "/";
			path_web = absolute_path_web + totalInfoFolderName + "/";
		}

        File destinationDir = new File(path);
        //디렉토리생성
        FileUtils.forceMkdir(destinationDir);

        //휘원추가
        SimpleDateFormat sdt = new SimpleDateFormat("MMddHHmmss");
        String namePrefix = sdt.format(Calendar.getInstance().getTime());
        //휘원추가 끝.
        fName = namePrefix +suffix+".html";
        File toFile = new File(path + fName);
//		file.transferTo(toFile);

        file_writer = new FileWriter(toFile);
//			buff_writer = new BufferedWriter(file_writer);
        buff_writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(toFile.getPath()), "UTF8"));
        print_writer = new PrintWriter(buff_writer,true);
        print_writer.println(contents);
        print_writer.close();

        //푸쉬 html 파일 동기화 FTP 업데이트 전송
        boolean lm_bResourceSync = BooleanUtils.toBoolean(FTP_USE_FLAG);
        if(lm_bResourceSync) {
            sendFTPResourceFile(path + fName, UploadDetailFolder);
        }

		return path_web + fName;
	}

    private boolean sendFTPResourceFile(String localFile, String remoteNewDir) throws SocketException, IOException  {
        boolean result = false;
        String[] lm_sTargets = mspConfig.getStringArray("pushserver.ftp.target.list");

        for (String lm_sTarget: lm_sTargets) {
            String lm_sFtpType = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".type");
            String lm_sHostName = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".hostname");
            int lm_iPort = NumberUtils.toInt(mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".port"));
            String lm_sId = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".id");
            String lm_sPassword = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".password");
            String lm_sRemotePath = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".remotepath") + remoteNewDir+"/";

            String lm_privateKey = "";
            String lm_passphrase = "";
            if(lm_sFtpType.equals("sftp2")){
                lm_privateKey = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".privatekey");
                lm_passphrase = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".passphrase");
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

            try {
                ftp.putFileToServer(localFile, lm_sRemotePath);
                ftp.disconnection();
                result = true;
            } catch (SocketException e) {
                logger.error("Exception caught.", e);
                throw new SocketException("FTP "+messageSource.getMessage("common.text.server", null, LocaleContextHolder.getLocale())
                		+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null, LocaleContextHolder.getLocale())); //서버 / 연결에 실패하였습니다.
            } catch (IOException e) {
                logger.error("Exception caught.", e);
                throw new SocketException("FTP "+messageSource.getMessage("common.text.server", null, LocaleContextHolder.getLocale())
                		+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.sendFileFail", null, LocaleContextHolder.getLocale())); //서버 / 파일 전송 실패하였습니다.
            }
        }
        return result;
    }

    private boolean sendFTPCsvFile(String localFile, String remoteNewDir) throws SocketException, IOException  {
        boolean result = false;
        String[] lm_sTargets = mspConfig.getStringArray("pushserver.receiver.ftp.target.list");

        for (String lm_sTarget: lm_sTargets) {
            String lm_sFtpType = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".type");
            String lm_sHostName = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".hostname");
            int lm_iPort = NumberUtils.toInt(mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".port"));
            String lm_sId = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".id");
            String lm_sPassword = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".password");
            String lm_sRemotePath = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".remotepath");

            String lm_privateKey = "";
            String lm_passphrase = "";
            if(lm_sFtpType.equals("sftp2")){
                lm_privateKey = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".privatekey");
                lm_passphrase = mspConfig.getString("pushserver.receiver.ftp.target." + lm_sTarget + ".passphrase");
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

            try {
                ftp.putFileToServer(localFile, lm_sRemotePath);
                ftp.disconnection();
                result = true;
                logger.info("###### CSV FTP 전송 성공: ["+lm_sHostName +"] "+lm_sRemotePath);
            } catch (SocketException e) {
                logger.error("Exception caught.", e);
                throw new SocketException(messageSource.getMessage("menu.push.controller.ftpServer", null, LocaleContextHolder.getLocale())
                		+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null, LocaleContextHolder.getLocale())); //리시버 서버에 FTP 서버  / 연결에 실패하였습니다.
            } catch (IOException e) {
                logger.error("Exception caught.", e);
                throw new IOException(messageSource.getMessage("menu.push.controller.ftpServer", null, LocaleContextHolder.getLocale())
                		+"["+lm_sHostName+"] CSV"+messageSource.getMessage("menu.push.controller.sendFileFail", null, LocaleContextHolder.getLocale()) +" ("+e.toString()+")"); //리시버 서버에 FTP 서버  / 파일 전송 실패하였습니다.
            }
        }
        return result;
    }
}