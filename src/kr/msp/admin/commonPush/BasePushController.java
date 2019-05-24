package kr.msp.admin.commonPush;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.map.PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.base.utils.CryptoUtil;
import kr.msp.admin.base.utils.ftp.FTPClientUtils;
import kr.msp.admin.base.utils.ftp.IFTPClientUtils;
import kr.msp.admin.base.utils.ftp.SFTPClientUtils;
import kr.msp.admin.push.msgSend.dto.MsgSendDto;


@RequestMapping(value="admin/campaign")
@Controller
public class BasePushController {
	
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
  	//DB 타입가져오기
  	private @Value("${datasource.dbtype:oracle}") String DBTYPE;
  	//FTP로 리소스파일 sync 맞출 여부
    private @Value("${pushserver.resource.sync:false}") String FTP_USE_FLAG;
    //디폴트 뱃지 카운트 세팅
  	private @Value("${admin.push.default_badgeno}") String DEFAULT_BADGENO;
	
	@Qualifier("mspXmlConfiguration")
    @Autowired(required = true)
    private Configuration mspConfig;
	
	@Autowired
	private ConfigurableApplicationContext applicationContext;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 
	
	private static Logger logger = LoggerFactory.getLogger(BasePushController.class);
	
	protected MsgSendDto genePushData(MsgSendDto msgSendDto){
		String imageUploadUrl = "";
		String htmlContentsUrl = "";
		String saveCsvFileAbsPath = "";
        
		String newExt = msgSendDto.getEXT();
        MultipartFile IMAGE_FILE = msgSendDto.getIMAGE_FILE(); 
        MultipartFile CSV_FILE = msgSendDto.getCSV_FILE();
        
        msgSendDto.setMsgCategory(msgSendDto.getMSGTYPE());
        
        boolean isCSVFileUpload = false;

        try{
        	if(msgSendDto.getImageUrl() != null && !msgSendDto.getImageUrl().equals("")){
        		imageUploadUrl = msgSendDto.getImageUrl();
        	}
        	
        	//Image 파일 업로드 IMAGE_FILE
            if(IMAGE_FILE !=null && IMAGE_FILE.getSize()>0){ // IMAGE_FILE 파일 업로드
                imageUploadUrl = msgImageUpload(IMAGE_FILE);
            }

            //CSV파일 파일 업로드 매핑
            if(CSV_FILE !=null && CSV_FILE.getSize()>0){ // CSV파일 업로드
                //isCSVFileUpload 변수를 전역으로 잡지않은 이유는 전역변수 일경우 해당 클래스가 싱글톤이기 때문 동시에 다른 사용자의 업로드가 있을시 문제소지가 있기 때문.
                saveCsvFileAbsPath = msgCSVUpload(CSV_FILE);
                msgSendDto.setATTACHFILE(saveCsvFileAbsPath);
                isCSVFileUpload = true; // 위에서 에러가 발생하면 exception 발생 하므로 이 부분이 true가 되지 않음
            }
            //WEB EDITOR을 사용했을 경우 html파일을 만든다.
            if(msgSendDto.getMsgCategory().equals("1") || msgSendDto.getMsgCategory().equals("4")) {
                String secure = msgSendDto.getSECURE();
                if ("Y".equals(secure)) {
                    // 암호화를 위해 appid로 키를 설정 함.
                    CryptoUtil oCryptoUtil = applicationContext.getBean(CryptoUtil.class);
                    oCryptoUtil.initial(msgSendDto.getAPPID());
                    msgSendDto.setMsgCategory("5");

                }
                htmlContentsUrl = htmlUpload(0, msgSendDto.getWEBEDIT(), secure);
            }else if(msgSendDto.getMsgCategory().equals("0")){
                if(!msgSendDto.getEXT().equals("")) {
                    htmlContentsUrl = messageFormat(msgSendDto.getEXT(), htmlContentsUrl, msgSendDto.getMsgCategory(), msgSendDto.getVideoUrl(), imageUploadUrl);
                }
            }else{
                htmlContentsUrl = messageFormat(msgSendDto.getEXT(), htmlContentsUrl, msgSendDto.getMsgCategory(), msgSendDto.getVideoUrl(), imageUploadUrl);
            }
            newExt = htmlContentsUrl;
            msgSendDto.setEXT(newExt);
/*
            //WEB EDITOR을 사용했을 경우 html파일을 만든다.
            if(msgSendDto.getMsgCategory().equals("1") || msgSendDto.getMsgCategory().equals("4")){
                String secure =  msgSendDto.getSECURE();
                if(secure.equals("Y")){
                    // 암호화를 위해 appid로 키를 설정 함.
                    CryptoUtil oCryptoUtil = applicationContext.getBean(CryptoUtil.class);
                    oCryptoUtil.initial(msgSendDto.getAPPID());
                    msgSendDto.setMsgCategory("5");

                }
                htmlContentsUrl = htmlUpload(0, msgSendDto.getWEBEDIT(), secure);
            }

            //기본이 아니면 부가 정보를 담은 html을 만들어 EXT값에 URL주소를 보낸다
            if(!msgSendDto.getMsgCategory().equals("0") || "CSV".equals(msgSendDto.getUSER_TYPE())){
                String makeExtInfoHtml = messageFormat(msgSendDto.getEXT(), htmlContentsUrl, msgSendDto.getMsgCategory(), msgSendDto.getVideoUrl(), imageUploadUrl);
                htmlContentsUrl = htmlUpload(1, makeExtInfoHtml, "N");
                newExt = htmlContentsUrl;
                msgSendDto.setEXT(newExt);
            }
*/
            msgSendDto.setSOUNDFILE("alert.aif");
            msgSendDto.setBADGENO(DEFAULT_BADGENO);
            
            // 즉시
            if(msgSendDto.getSEND_TYPE().equals("I")){
            	msgSendDto.setRESERVEDATE("");
            // 예약
            }else if(msgSendDto.getSEND_TYPE().equals("R")){
            	String reserveDate = msgSendDto.getSEND_DATE() + " " + msgSendDto.getSEND_HOUR() + msgSendDto.getSEND_MINUTE() + "00";
            	reserveDate = reserveDate.replace(".", "");
            	reserveDate = reserveDate.trim();
            	msgSendDto.setRESERVEDATE(reserveDate);
            }
            // 전체 전송
	        if(msgSendDto.getUSER_TYPE().equals("ALL")){
	        	msgSendDto.setTYPE("A");									// 구분
	        	msgSendDto.setGROUPSEQ("");								// 그룹 일련번호
	        // 맴버 전송    
	        }else if(msgSendDto.getUSER_TYPE().equals("MEMBER")){
	        	msgSendDto.setTYPE("M");									// 구분
	            msgSendDto.setGROUPSEQ("");								// 그룹 일련번호
	        //게스트 전송    
	        }else if(msgSendDto.getUSER_TYPE().equals("GUESTS")){
	        	msgSendDto.setTYPE("N");
	        	msgSendDto.setGROUPSEQ("");		
//	        	String CUIDS[] = {"GUEST"};
//	            ////CUID배열로 보내기
//	            JSONArray cuidJsonArray = new JSONArray();
//	            for (int i = 0; i < CUIDS.length; i++) {
//	                //배열로 보내기(리시버 구현 되어 있을시)
//	                cuidJsonArray.put(CUIDS[i]);
//	            }
//	            msgSendDto.setCUID(cuidJsonArray);
	        // 그룹 전송
	        }else if(msgSendDto.getUSER_TYPE().equals("GROUP")){
	            msgSendDto.setTYPE("G");	
	            // 구분
	            
	            /*
	             * 임시 2016.01.06 nhj
	             * 복수개의 그룹 발송기능 지원 안함 첫번째 그룹만 발송한다.
	             * 
	            */
	            String GROUPSEQS[] = msgSendDto.getGROUPSEQS();
	            String groupSeq = new String();
	            if(GROUPSEQS != null){
	            	groupSeq = GROUPSEQS[0];
	            }
	            msgSendDto.setGROUPSEQ(groupSeq);			// 그룹 일련번호
	        // 개인 전송
	        }else if(msgSendDto.getUSER_TYPE().equals("EACH")){
	        	msgSendDto.setTYPE("E");	
	            String CUIDS[] 	   = msgSendDto.getCUIDS();
	            String PSID_SELS[] = msgSendDto.getPSID_SELS();
	            ////CUID배열로 보내기
	            JSONArray cuidJsonArray = new JSONArray();
	            if(CUIDS != null){
	            	for (int i = 0; i < CUIDS.length; i++) {
		                //배열로 보내기(리시버 구현 되어 있을시)
		                cuidJsonArray.put(CUIDS[i]);
		            }
	            }
	            ////보낼서버(PSID)배열로 보내기
	            JSONArray psidJsonArray = new JSONArray();
	            if(PSID_SELS != null){
	            	for (int i = 0; i < PSID_SELS.length; i++) {
		                //배열로 보내기(리시버 구현 되어 있을시)
		                psidJsonArray.put(PSID_SELS[i]);
		            }
	            }
	            if(cuidJsonArray.length() != 0){
	            	msgSendDto.setCUID(cuidJsonArray);
	            }
	            
	            if(psidJsonArray.length() != 0){
	            	msgSendDto.setPSID(psidJsonArray);
	            }
	            // CSV 전송
	        }else if(msgSendDto.getUSER_TYPE().equals("CSV")){ //csv 파일을 이용한 전송
	        	msgSendDto.setTYPE("C");
	            //리시버 첨부파일 기능 구현 했을때
	        }
        }catch(Exception e){
        	logger.debug(e.toString());
        }
        
        return msgSendDto;
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
	private String messageFormat(String ext, String webEditUrl, String category, String videoUrl, String imageUrl){
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
			case 5:
				result = category+"|"+ext+"|"+imageUrl+"|"+webEditUrl;
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
	private String htmlUpload(int type, String contents, String secure) throws Exception{
		String path = "";
		String path_web = "";
		String fName = "";
		String suffix = "";
		FileWriter file_writer = null;
		BufferedWriter buff_writer = null;
		PrintWriter print_writer = null;
        StringBuffer sb = new StringBuffer();

		//web editor로 작성한 html이 업로드 되는 폴더
        String UploadDetailFolder = htmlContentFolderName;
		if(type == 0){
            sb.append("<html><head>");
            sb.append("<meta charset=\"UTF-8\" name=\"viewport\" content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0\"/>");
            sb.append("</head><body>");
            sb.append(contents);
            sb.append("</body></html>");
			path = absolute_path + htmlContentFolderName + "/";
			path_web = absolute_path_web + htmlContentFolderName + "/";
		//최종ext 정보가 담긴 html이 업로드 되는 폴더
		}else{
            sb.append(contents);
            UploadDetailFolder = totalInfoFolderName;
			suffix = "_msp";
			path = absolute_path + totalInfoFolderName + "/";
			path_web = absolute_path_web + totalInfoFolderName + "/";
		}
		
		//보안설정 푸시
        if(secure.equals("Y")){
        	byte[] bytes = sb.toString().getBytes();
        	InputStream inputStream = new ByteArrayInputStream(bytes);
        	OutputStream outputStream = new ByteArrayOutputStream();
        	CryptoUtil.encrypt(inputStream, outputStream);
        	sb = new StringBuffer(outputStream.toString());
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
        
//      
        if(type == 0){
        	print_writer.println(sb.toString());
        }else {
        	print_writer.println(URLEncoder.encode(sb.toString(), "UTF-8").replace("+", "%20"));
        }
        print_writer.close();
        
        //푸쉬 html 파일 동기화 FTP 업데이트 전송
        boolean lm_bResourceSync = BooleanUtils.toBoolean(FTP_USE_FLAG);
        if(lm_bResourceSync) {
            sendFTPResourceFile(path + fName, UploadDetailFolder);
        }

		return path_web + fName;
	}
	
	/**
	 * 요청된 이미지 파일을 저장 후 저장된 위치정보를 반환
	 * @param file : 발송시 저장한 이미지 정보
	 * @return : 이미지가 저장된 url
	 */
	private String msgImageUpload(MultipartFile file) throws Exception{
		String path = "";
		String path_web = "";
		String fName = "";
        if(!file.isEmpty()){
            path = absolute_path + imageFolderName + "/";
            path_web = absolute_path_web + imageFolderName + "/";

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
                    throw new Exception(messageSource.getMessage("menu.push.controller.extensionLimit", null, LocaleContextHolder.getLocale())); //"확장자가 GIF, JPG, PNG 이미지 파일만 첨부 가능합니다."
                }
            }else{
                throw new Exception(messageSource.getMessage("menu.push.controller.noExtension", null, LocaleContextHolder.getLocale())); //"확장자가 존재하지 않습니다."
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
    private String msgCSVUpload(MultipartFile CSV_FILE) throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(CSV_FILE.getInputStream()));
        String line = null;
        int i=0;
        while((line = in.readLine()) != null) {
            if(line.indexOf(",")<0){
                throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.notCorrectFile", null, LocaleContextHolder.getLocale())
                		+" CUID, "+messageSource.getMessage("menu.push.controller.needName", null, LocaleContextHolder.getLocale())); //파일 데이터가 올바르지 않습니다. 이름이 있어야 합니다.
            }else{
                String[] chkValues = line.split(",");
                if(chkValues.length<2 || chkValues.length>12){
                    throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.notCorrectFile", null, LocaleContextHolder.getLocale())
                    		+" CUID, "+messageSource.getMessage("menu.push.controller.needName", null, LocaleContextHolder.getLocale())); //파일 데이터가 올바르지 않습니다. 이름이 있어야 합니다.
                }
            }
            i++;
            if(i==2){ //두라인 정보만 체크(웹이기 때문 대용량일 경우 Reponse Time out 날 수 있기 때문)
                break;
            }
        }
        if(i==0){
            throw new Exception("CSV "+messageSource.getMessage("menu.push.controller.noFileData", null, LocaleContextHolder.getLocale())); //+"파일 데이터가 없습니다."
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
    }
    
    private boolean sendFTPResourceFile(String localFile, String remoteNewDir) throws SocketException, IOException  {
        boolean result = false;
        String[] lm_sTargets = mspConfig.getStringArray("pushserver.ftp.target.list");

        for (String lm_sTarget: lm_sTargets) {
            String lm_sFtpType = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".type");
            String lm_sHostName = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".hostname");
            int lm_iPort = NumberUtils.toInt(mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".port"));
            String lm_sId = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".id");
            String lm_sRemotePath = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".remotepath") + remoteNewDir+"/";
            String lm_sPassword = mspConfig.getString("pushserver.ftp.target." + lm_sTarget + ".password");
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
                		+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null, LocaleContextHolder.getLocale())); //서버 / 파일 전송 실패하였습니다..
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
                		+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null, LocaleContextHolder.getLocale())); //리시버 서버에 FTP 서버   연결에 실패하였습니다.
            } catch (IOException e) {
                logger.error("Exception caught.", e);
                throw new IOException(messageSource.getMessage("menu.push.controller.ftpServer", null, LocaleContextHolder.getLocale())
                		+"["+lm_sHostName+"] CSV"+messageSource.getMessage("menu.push.controller.sendFileFail", null, LocaleContextHolder.getLocale())+" ("+e.toString()+")"); //리시버 서버에 FTP 서버    파일 전송 실패하였습니다.
            }
        }
        return result;
    }
}

class LowerCaseNamingStrategy extends LowerCaseWithUnderscoresStrategy {
    @Override
    public String translate(String arg0) {
        return arg0.toUpperCase();
    }
}
