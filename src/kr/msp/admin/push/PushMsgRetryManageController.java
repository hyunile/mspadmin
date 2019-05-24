package kr.msp.admin.push;

import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.msp.admin.base.collection.SingleData;
import kr.msp.admin.base.urlconnect.PushUrlConnect;
import kr.msp.admin.push.msgSend.dto.MsgRetryDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;

//import com.oreilly.servlet.MultipartRequest;
//import org.springframework.web.multipart.MultipartRequest;

@RequestMapping(value="admin/push")
@Controller
public class PushMsgRetryManageController {

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

    private static Logger logger = LoggerFactory.getLogger(PushMsgRetryManageController.class);

	

//	@ResponseBody
	@RequestMapping(value="msgRetry/retry", method=RequestMethod.POST)
	public ModelAndView pushMsgRetryPost(HttpServletRequest request, MsgRetryDto msgRetry , HttpServletResponse response){
		logger.info("pushMsgRetryPost");
		//<20140221-JHW> 메시지 발송정보가 추가 및 수정됨에 따라 새로운 메시지 포맷팅 시작
		String imageUploadUrl = "";
		String htmlContentsUrl = "";
        
        String saveCsvFileAbsPath = "";
        boolean isCSVFileUpload = false;

        Map<String,Object> returnMap = new HashMap<String, Object>();
        Map<String,Object> resultMap = null;

        try {
           
            //WEB EDITOR을 사용했을 경우 html파일을 만든다.
            if(msgRetry.getRETRY_TYPE().equals("") ){
            	returnMap.put("resultCode","500");
                returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.sendFail", null, LocaleContextHolder.getLocale())
                		+": RETRY_TYPE"+messageSource.getMessage("menu.push.controller.set", null, LocaleContextHolder.getLocale()));  //발송실패 / 을 설정하세요
            }
            String RETRY_TYPE = msgRetry.getRETRY_TYPE();		// RETRY_TYPE
            String SEQNO = msgRetry.getSEQNO();					// SEQNO
            SingleData paramData = new SingleData();
            
          	paramData.add("RETRY_TYPE", RETRY_TYPE);			
            paramData.add("SEQNO", SEQNO);						
            paramData.add("SPLIT_MSG_CNT", "0");				
            paramData.add("DELAY_SECOND", "0");					
            
    		logger.info("RETRY_TYPE:"+RETRY_TYPE+", SEQNO:"+SEQNO);
    		
        	returnMap.put("resultCode", "0000");
            resultMap = sendIF(response, DEFAULT_RECEIVER_HOST + "/rcv_retry_message.ctl", paramData);
            
            // view에 보낼 알림메세지 만들기
            if(resultMap.get("resultCode").equals("0000") || resultMap.get("resultMsg").equals("OK")){
                returnMap.put("resultCode", "0000");
                returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.msgSendSuccess", null, LocaleContextHolder.getLocale())); //메시지 발송요청이 성공하였습니다.
                if(resultMap.containsKey("bodyData")){
                    returnMap.put("bodyData", resultMap.get("bodyData"));
                }
            }else{
                returnMap.put("resultCode", "500");
                returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.sendFail", null, LocaleContextHolder.getLocale())
                		+":"+resultMap.get("resultMsg")); //발송실패
            }

        }catch(Exception e){
            returnMap.put("resultCode","500");
            returnMap.put("resultMsg",messageSource.getMessage("menu.push.controller.sendFail", null, LocaleContextHolder.getLocale())+":"+e.toString()); //발송실패
        }

        ModelAndView mv = new ModelAndView("jsonView");
        mv.addAllObjects(returnMap);
        return mv;
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
    
}






