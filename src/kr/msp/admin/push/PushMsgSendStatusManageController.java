package kr.msp.admin.push;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kr.msp.admin.base.collection.SingleData;
import kr.msp.admin.base.urlconnect.PushUrlConnect;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;
import kr.msp.admin.push.msgSendStatus.dto.FailMsgBean;
import kr.msp.admin.push.msgSendStatus.dto.SendMsgStatusBean;
import kr.msp.admin.push.msgSendStatus.service.IPushMsgSendStatusService;
import kr.msp.common.util.DateUtil;

/**
 * Created with IntelliJ IDEA.
 * User: Yoo Byung Hee
 * Date: 14. 2. 17
 * Time: 오후 6:19
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class PushMsgSendStatusManageController {
    //로거 설정
    Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    //공통 페이지 로우수
    private @Value("${common.list.row_size}") int row_size;
    //공통 페이지 수
    private @Value("${common.list.page_size}") int page_size;

    @Autowired(required=true)
    private MsgSendManageService msgSendManage;

    @Autowired(required=true)
    private IPushMsgSendStatusService pushMsgSendStatusService;
    
    @Autowired(required = true) 
	private MessageSource messageSource; 

    private @Value("${admin.push.receiver.host}") String DEFAULT_RECEIVER_HOST;	//리시버 호스트

    /**
     * 발송현환 GET
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="admin/push/msgSendStatus", method= RequestMethod.GET)
    public ModelAndView msgSendStatusGet(HttpServletRequest request,@RequestParam Map<String,Object> reqMap){
        reqMap.put("DATE_VIEW_S", DateUtil.getBaseFromDate());
        reqMap.put("DATE_VIEW_E", DateUtil.getBaseToDate());
        
        reqMap.put("PAGE_NUM",1);
        reqMap.put("PAGE_SIZE",row_size);

        //PUSH 서비스 조회
        PushServiceDto pushService = new PushServiceDto();
        pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
        List<PushServiceDto> pushServiceList = msgSendManage.SelectPushServiceAll(pushService);
        
        //푸쉬 발송 현황 조회
        List<SendMsgStatusBean> sendMsgStatusBeanList = (List<SendMsgStatusBean>)pushMsgSendStatusService.selSendMsgStatusList(reqMap);
        
        //재발송 타입
        for(SendMsgStatusBean sendMsgStatusBean : sendMsgStatusBeanList){
        	reqMap.put("SEQNO",""+sendMsgStatusBean.getSEQNO());
        	Integer conFailCnt = pushMsgSendStatusService.selConFailMsgCount(reqMap);
        	Integer notRecvCnt = pushMsgSendStatusService.selNotRecvMsgCount(reqMap);
        	Integer notReadCnt = pushMsgSendStatusService.selNotReadMsgCount(reqMap);
        	if(conFailCnt>0){ 
        		sendMsgStatusBean.addRETRY_TYPE_LIST(SendMsgStatusBean.RETRY_TYPE_CON_FAIL);
        	}
        	if(notRecvCnt>0 && "Y".equals(sendMsgStatusBean.getDB_IN())){ 
        		sendMsgStatusBean.addRETRY_TYPE_LIST(SendMsgStatusBean.RETRY_TYPE_NOT_RECV);
        	}
        	if(notReadCnt>0 && "Y".equals(sendMsgStatusBean.getDB_IN())){ 
        		sendMsgStatusBean.addRETRY_TYPE_LIST(SendMsgStatusBean.RETRY_TYPE_NOT_READ);
        	}
        }
        
        ModelAndView mv = new ModelAndView("admin/push/msgSendStatus/msgSendStatusMain");
        mv.addObject("sendMsgStatusBeanList", sendMsgStatusBeanList);
        mv.addObject("pushServiceList",pushServiceList);
       
        mv.addObject("R_PAGE_NUM", 1);
        mv.addObject("R_PAGE_SIZE",page_size);
        mv.addObject("R_ROW_SIZE",row_size);
        mv.addObject("DEFAULT_RECEIVER_HOST",DEFAULT_RECEIVER_HOST);
        return mv;
    }

    /**
     * 발송 현황 POST
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="admin/push/msgSendStatus", method= RequestMethod.POST)
    public ModelAndView msgSendStatusPost(HttpServletRequest request,@RequestParam Map<String,Object> reqMap){
        String fromDate = (String) reqMap.get("DATE_VIEW_S");
        reqMap.put("DATE_VIEW_S", fromDate +" 00:00:00");
        
        String toDate = (String) reqMap.get("DATE_VIEW_E");
        reqMap.put("DATE_VIEW_E", toDate+" 23:59:59");
    	
        List<SendMsgStatusBean> sendMsgStatusBeanList = (List<SendMsgStatusBean>)pushMsgSendStatusService.selSendMsgStatusList(reqMap);
        
        //재발송 타입
        for(SendMsgStatusBean sendMsgStatusBean : sendMsgStatusBeanList){
        	reqMap.put("SEQNO",""+sendMsgStatusBean.getSEQNO());
        	Integer conFailCnt = pushMsgSendStatusService.selConFailMsgCount(reqMap);
        	Integer notRecvCnt = pushMsgSendStatusService.selNotRecvMsgCount(reqMap);
        	Integer notReadCnt = pushMsgSendStatusService.selNotReadMsgCount(reqMap);
        	if(conFailCnt>0){ 
        		sendMsgStatusBean.addRETRY_TYPE_LIST(SendMsgStatusBean.RETRY_TYPE_CON_FAIL);
        	}
        	if(notRecvCnt>0 && "Y".equals(sendMsgStatusBean.getDB_IN())){ 
        		sendMsgStatusBean.addRETRY_TYPE_LIST(SendMsgStatusBean.RETRY_TYPE_NOT_RECV);
        	}
        	if(notReadCnt>0 && "Y".equals(sendMsgStatusBean.getDB_IN())){ 
        		sendMsgStatusBean.addRETRY_TYPE_LIST(SendMsgStatusBean.RETRY_TYPE_NOT_READ);
        	}
        }
        
        ModelAndView mv = new ModelAndView("admin/push/msgSendStatus/msgSendStatusList");
        mv.addObject("sendMsgStatusBeanList", sendMsgStatusBeanList);

        mv.addObject("R_PAGE_NUM", reqMap.get("PAGE_NUM"));
        mv.addObject("R_ROW_SIZE", reqMap.get("PAGE_SIZE"));
        mv.addObject("R_PAGE_SIZE",page_size);
        mv.addObject("layout", "layout/null.vm");
        
        mv.addObject("DEFAULT_RECEIVER_HOST",DEFAULT_RECEIVER_HOST);
        
        return mv;
    }

    /**
     * 발송정보 상세
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="admin/push/sendDatailPop", method= RequestMethod.POST)
    public ModelAndView sendDatailPopPost(HttpServletRequest request,@RequestParam Map<String,Object> reqMap){

        SendMsgStatusBean sendMsgStatusBean = (SendMsgStatusBean)pushMsgSendStatusService.selSendMsgStatusDetail(reqMap);
        int TOTALSENDCNT = 0;
        int SENTCNT = 0;
        int SENT_PERCENT = 0;
        String TypeStr = messageSource.getMessage("menu.push.sendManage.each", null, LocaleContextHolder.getLocale()); //"개별";
        if(sendMsgStatusBean!=null) {
            TOTALSENDCNT = sendMsgStatusBean.getTOTAL_SEND_CNT();
            SENTCNT = sendMsgStatusBean.getSEND_CNT() + sendMsgStatusBean.getFAIL_CNT();
            if(TOTALSENDCNT!=0){
            	SENT_PERCENT = (SENTCNT*100 / TOTALSENDCNT);
            }

            if("C".equals(sendMsgStatusBean.getTYPE())){
                TypeStr = "CSV "+messageSource.getMessage("menu.stats.push.send", null, LocaleContextHolder.getLocale()); //발송
            }else if("G".equals(sendMsgStatusBean.getTYPE())){
                TypeStr = messageSource.getMessage("menu.push.groupSend", null, LocaleContextHolder.getLocale());//"그룹 발송";
            }else if("A".equals(sendMsgStatusBean.getTYPE())){
                TypeStr = messageSource.getMessage("menu.push.allSend", null, LocaleContextHolder.getLocale());//"전체 발송";
            }
        }

        String isProgressBarView = "N";
//        System.out.println("########### sendMsgStatusBean.getREGDATE().length():"+sendMsgStatusBean.getREGDATE().length()+ "   TOTALSENDCNT:"+TOTALSENDCNT+"  SENTCNT:"+SENTCNT);
        if(TOTALSENDCNT > SENTCNT && sendMsgStatusBean.getREGDATE().length()==19){
            String RegDate = sendMsgStatusBean.getREGDATE();
            int year = Integer.parseInt(RegDate.substring(0,4));
            int month = Integer.parseInt(RegDate.substring(5,7));
            int day = Integer.parseInt(RegDate.substring(8,10));
            int hour = Integer.parseInt(RegDate.substring(11,13));
            int minute = Integer.parseInt(RegDate.substring(14,16));
            int second = Integer.parseInt(RegDate.substring(17,19));

            Calendar adate = Calendar.getInstance();
            Calendar bdate = Calendar.getInstance();
            adate.set(year,month,day,hour,minute,second);

            //발송시간이 한시간이 안지났을때만 발송 프로그래스바 보여주기 위해
            if(adate.getTimeInMillis()+(3600*1000) > bdate.getTimeInMillis() && !reqMap.containsKey("SHOWPROGRESS")){
                isProgressBarView = "Y";
            }
        }

        ModelAndView mv = new ModelAndView("admin/push/msgSendStatus/msgSendDetailPop");
        mv.addObject("sendMsgStatusBean", sendMsgStatusBean);
        mv.addObject("SENTCNT",SENTCNT);
        mv.addObject("SENT_PERCENT",SENT_PERCENT);
        mv.addObject("SEQNO",reqMap.get("SEQNO"));
        mv.addObject("TypeStr",TypeStr);
        mv.addObject("isProgressBarView",isProgressBarView);
        mv.addObject("layout", "layout/null.vm");
        mv.addObject("RECEIVER_HOSTURL",reqMap.get("HOST_URL"));
        return mv;
    }

    /**
     * 발송정보 상세
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="admin/push/sendDatailJson", method= RequestMethod.GET)
    public ModelAndView sendDatailJson(HttpServletRequest request,@RequestParam Map<String,Object> reqMap){

        SendMsgStatusBean sendMsgStatusBean = (SendMsgStatusBean)pushMsgSendStatusService.selSendMsgStatusDetail(reqMap);

        String resultCode = "0000";
        String resultMsg = "SUCCESS";
        int TOTALSENDCNT = 0;
        int SENTCNT = 0;
        int SUCCESS_CNT= 0;
        int SENT_PERCENT = 0;
        int FAIL_CNT = 0;
        int UPNS_CNT = 0;
        int APNS_CNT = 0;
        int GCM_CNT = 0;
        int READ_CNT = 0;

        if(sendMsgStatusBean!=null) {
            TOTALSENDCNT = sendMsgStatusBean.getTOTAL_SEND_CNT();
            SENT_PERCENT = (SENTCNT*100 / TOTALSENDCNT);
            SENTCNT = sendMsgStatusBean.getSEND_CNT() + sendMsgStatusBean.getFAIL_CNT();
            SUCCESS_CNT = sendMsgStatusBean.getSEND_CNT();
            FAIL_CNT = sendMsgStatusBean.getFAIL_CNT();
            UPNS_CNT = sendMsgStatusBean.getUPNS_CNT();
            APNS_CNT = sendMsgStatusBean.getAPNS_CNT();
            GCM_CNT = sendMsgStatusBean.getGCM_CNT();
            READ_CNT = sendMsgStatusBean.getREAD_CNT();
        }else{
            resultCode = "500";
            resultMsg = messageSource.getMessage("menu.push.controller.noData", null, LocaleContextHolder.getLocale())
            		+" [SEQNO]:"+reqMap.get("SEQNO"); //데이타가 존재 하지 않습니다.
        }

        ModelAndView mv = new ModelAndView("jsonView");
        mv.addObject("resultCode",resultCode);
        mv.addObject("resultMsg",resultMsg);
        mv.addObject("SEQNO",reqMap.get("SEQNO"));
        mv.addObject("SENT_PERCENT",SENT_PERCENT);
        mv.addObject("TOTALSENDCNT",TOTALSENDCNT);
        mv.addObject("SENTCNT",SENTCNT);
        mv.addObject("SUCCESS_CNT",SUCCESS_CNT);
        mv.addObject("FAIL_CNT",FAIL_CNT);
        mv.addObject("UPNS_CNT",UPNS_CNT);
        mv.addObject("APNS_CNT",APNS_CNT);
        mv.addObject("GCM_CNT",GCM_CNT);
        mv.addObject("READ_CNT",READ_CNT);
        return mv;
    }

    /**
     * 리시버 연결 현재 발송 상태 얻어오기
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="admin/push/getReceiverSendStatus", method= RequestMethod.POST)
    public ModelAndView getReceiverSendStatusPost(HttpServletRequest request,@RequestParam Map<String,String> reqMap){
        String DEFAULT_RECEIVER_HOST = this.DEFAULT_RECEIVER_HOST;

        Set<Map.Entry<String,String>> s1 = reqMap.entrySet();
        for(Map.Entry<String,String> me : s1){
            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
        }
        ModelAndView mv = new ModelAndView("jsonView"); //Ajax로 콜하였으므로 JSON BodyString 던져 준다
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if(!reqMap.containsKey("SEQNO")){
            resultMap.put("resultCode","405");
            resultMap.put("resultMsg",messageSource.getMessage("menu.push.controller.paramError", null, LocaleContextHolder.getLocale())); //필수파라미터 에러
        }else {
            SingleData paramData = new SingleData();
            paramData.add("SEQNO", reqMap.get("SEQNO"));
            if(reqMap.containsKey("RECEIVER_HOSTURL") && !reqMap.get("RECEIVER_HOSTURL").equals("")){
                DEFAULT_RECEIVER_HOST = reqMap.get("RECEIVER_HOSTURL");
            }
            logger.info("###### DEFAULT_RECEIVER_HOST:"+DEFAULT_RECEIVER_HOST);
            resultMap = sendIF(DEFAULT_RECEIVER_HOST + "/rcv_getSentMsgInfo.ctl", paramData);
        }
        mv.addObject("callbackData",resultMap);
        return mv;
    }

    /**
     * 발송실패 리스트
     * @param request
     * @param reqMap
     * @return
     */
    @RequestMapping(value="admin/push/failListPop")
    public ModelAndView failListPopGet(HttpServletRequest request,@RequestParam Map<String,Object> reqMap){

        int req_PageNum = 1;
        int req_PageSize = page_size;

        if(!reqMap.containsKey("PAGE_NUM")){
            reqMap.put("PAGE_NUM",req_PageNum);
        }else{
        	try{
        		req_PageNum = Integer.parseInt(""+reqMap.get("PAGE_NUM"));
        	}catch(Exception e){}
        }

        if(!reqMap.containsKey("PAGE_SIZE")){
            reqMap.put("PAGE_SIZE",req_PageSize);
        }else{
        	try{
        		req_PageSize = Integer.parseInt(""+reqMap.get("PAGE_SIZE"));
        	}catch(Exception e){}
        }


        List<FailMsgBean> failMsgBeanList = (List<FailMsgBean>)pushMsgSendStatusService.selFailMsgList(reqMap);
        ModelAndView mv = new ModelAndView("admin/push/msgSendStatus/msgFailListPop");
        mv.addObject("failMsgBeanList", failMsgBeanList);
        mv.addObject("PAGE_NUM", req_PageNum);
        mv.addObject("PAGE_SIZE",req_PageSize);
        mv.addObject("SEQNO",reqMap.get("SEQNO"));
        mv.addObject("layout", "layout/null.vm");
        return mv;
    }


    // 인터페이스 전송
    public Map<String,Object> sendIF(String url, SingleData paramData){

        PushUrlConnect PushUrlCon   = new PushUrlConnect();
        SingleData returnData           = new SingleData();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        PushUrlCon.setDoInput(true);
        PushUrlCon.setDoOutput(true);
        PushUrlCon.setDoOutput(true);
        PushUrlCon.setRequestMethod("POST");
        PushUrlCon.setConnectTimeout(10000); // 10초
        PushUrlCon.setReadTimeout(30000); //30초대기
        PushUrlCon.setReadEncodingType("UTF-8");

        PushUrlCon.setParam  = paramData;
        PushUrlCon.setUrl    = url;

        try {
            returnData = PushUrlCon.getUrlConnect();
            String resHttpBodyRaw = returnData.getString("RESULT");

            JSONObject jsonObject = new JSONObject(resHttpBodyRaw);

            JSONObject returnHeadJson = jsonObject.getJSONObject("HEADER");
            JSONObject returnBodyJson= jsonObject.getJSONObject("BODY");

            String resultCode = returnHeadJson.getString("RESULT_CODE");
            String resultMsg = returnHeadJson.getString("RESULT_BODY");

            if(resultCode!=null && resultCode.equals("0000")){ //발송성공
                resultMap.put("resultCode","0000");
            }else{ //발송 실패
                resultMap.put("resultCode",resultCode);
            }
            resultMap.put("resultMsg",resultMsg);

            Map<String,Object> bodyMap = new Gson().fromJson(returnBodyJson.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
            resultMap.put("bodyData",bodyMap);
        } catch (Exception e) {
            resultMap.put("resultCode","500");
            resultMap.put("resultMsg",messageSource.getMessage("menu.push.controller.notConnect", null, LocaleContextHolder.getLocale())); //리시버와 연결되지 않았거나 데이타 오류입니다.
            logger.error("Exception caught.", e);
        }finally{

        }
        return resultMap;
    }
}
