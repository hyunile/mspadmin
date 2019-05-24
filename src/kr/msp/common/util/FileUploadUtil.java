package kr.msp.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.base.utils.ftp.FTPClientUtils;
import kr.msp.admin.base.utils.ftp.IFTPClientUtils;
import kr.msp.admin.base.utils.ftp.SFTPClientUtils;

@Service("fileUploadUtil")
public class FileUploadUtil {
	private final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);

	@Qualifier("mspXmlConfiguration")
    @Autowired(required = true)
    private Configuration mspConfig;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
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
  	//FTP로 리소스파일 sync 맞출 여부
  	private @Value("${pushserver.resource.sync:false}") String FTP_USE_FLAG;	//FTP로 리소스파일 sync 맞출 여부
    
    /*public static FileUploadUtil instance = new FileUploadUtil();
    
    private FileUploadUtil(){}
    
    public static FileUploadUtil getInstance(){
        return instance;
    }*/
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
                    throw new Exception(messageSource.getMessage("menu.push.controller.extensionLimit", null, LocaleContextHolder.getLocale())); // "확장자가 GIF, JPG, PNG 이미지 파일만 첨부 가능합니다."
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
	 * html 에 파라미터로 입력된 내용을 write하여 업로드 한 뒤, 업로드 되어있는 url을 리턴
	 * @param type : 0 일경우 WEBPAGE 내용 업로드, 1일경우 EXT정보 업로드 
	 * @param contents : type이 0일경우 WEBPAGE 내용, type이 1일경우 포맷된 ext 문자열
	 * @return 해당내용으로 구성된 html파일이 업로드된 위치
	 */
	public String htmlUpload(int type, String contents) throws Exception{
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
	                throw new SocketException("FTP "+messageSource.getMessage("common.text.server", null, LocaleContextHolder.getLocale())+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.connectFail", null, LocaleContextHolder.getLocale())); //서버 연결에 실패하였습니다 
	            } catch (IOException e) {
	                logger.error("Exception caught.", e);
	                throw new SocketException("FTP "+messageSource.getMessage("common.text.server", null, LocaleContextHolder.getLocale())+"["+lm_sHostName+"] "+messageSource.getMessage("menu.push.controller.sendFileFail", null, LocaleContextHolder.getLocale())); //서버 파일 전송 실패하였습니다.
	            }
	        }
	        return result;
	    }
}
