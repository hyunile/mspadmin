package kr.msp.admin.push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
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
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.base.collection.SingleData;
import kr.msp.admin.base.urlconnect.PushUrlConnect;
import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.reserve.IPushReserveService;
import kr.msp.admin.push.reserve.dto.ReserveMsgDto;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-09-12
 * Time: 오전 11:24
 * To change this template use File | Settings | File Templates.
 */
@RequestMapping(value="admin/push")
@Controller
public class PushReserveController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired(required=true)
    private IPushReserveService pushReserveService;
    
    @Autowired(required = true) 
	private MessageSource messageSource; 
    //공통 페이지 로우수
    private @Value("${common.list.row_size:20}") int row_size;
    //공통 페이지 수
  	private @Value("${common.list.page_size}") int page_size;

    private @Value("${admin.push.receiver.host}") String DEFAULT_RECEIVER_HOST;	//기본 리소스 디렉토리

    @RequestMapping(value="reserveSend", method= RequestMethod.GET)
    public String pushUserReserveSendGet(HttpServletRequest request,Model model,@RequestParam Map<String,Object> reqMap){
        //PUSH 서비스 조회
        reqMap.put("PAGE_NUM",1);
        reqMap.put("PAGE_SIZE",row_size);
        PushServiceDto pushService = new PushServiceDto();
        pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
        List<PushServiceDto> pushServiceList = pushReserveService.SelectPushServiceAll(pushService);
        List<ReserveMsgDto> reserveMsgDtoList = pushReserveService.selectPushReserveMsg(reqMap);

        model.addAttribute("pushServiceList",pushServiceList);
        model.addAttribute("reserveMsgDtoList",reserveMsgDtoList);
        
        model.addAttribute("R_PAGE_NUM",reqMap.get("PAGE_NUM"));
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);

        return "admin/push/reserve/reserveMain";
    }

    @RequestMapping(value="reserveSend", method= RequestMethod.POST)
    public String pushUserReserveSendPost(HttpServletRequest request,Model model,@RequestParam Map<String,Object> reqMap){

        //PUSH 서비스 조회
        PushServiceDto pushService = new PushServiceDto();
        pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
        List<ReserveMsgDto> reserveMsgDtoList = pushReserveService.selectPushReserveMsg(reqMap);

        model.addAttribute("reserveMsgDtoList",reserveMsgDtoList);
        model.addAttribute("layout", "layout/null.vm");
        
        model.addAttribute("R_PAGE_NUM",reqMap.get("PAGE_NUM"));
		model.addAttribute("R_ROW_SIZE",reqMap.get("PAGE_SIZE"));
		model.addAttribute("R_PAGE_SIZE",page_size);

        return "admin/push/reserve/reserveList";
    }

    @RequestMapping(value="reserveSend/delete", method = RequestMethod.POST)
    public ModelAndView cancelReservePush(HttpServletRequest request,HttpServletResponse response,@RequestParam Map<String,String> reqMap){

        String REQ_RESERVE_SEQNO  = reqMap.get("RESERVE_SEQNO");
        SingleData paramData = new SingleData();
        paramData.add("RESERVE_SEQNO", REQ_RESERVE_SEQNO);

        Map<String,Object> resMap = sendIF(response, DEFAULT_RECEIVER_HOST + "/rcv_message_cancel_reservation.ctl", paramData);

        ModelAndView mv = new ModelAndView("jsonView");
        mv.addObject("resultCode",resMap.get("resultCode"));
        mv.addObject("resultMsg",resMap.get("resultMsg"));
        return mv;
    }

    // 인터페이스 전송
    public Map<String,Object> sendIF(HttpServletResponse response, String url, SingleData paramData){

        PushUrlConnect PushUrlCon   = new PushUrlConnect();
        SingleData returnData           = new SingleData();
        Map<String,Object> resultMap = new HashMap<String, Object>();

        PushUrlCon.setDoInput(true);
        PushUrlCon.setDoOutput(true);
        PushUrlCon.setDoOutput(true);
        PushUrlCon.setRequestMethod("POST");
        PushUrlCon.setConnectTimeout(5000); // 5초
        PushUrlCon.setReadTimeout(90000); //1분 30초대기
        PushUrlCon.setReadEncodingType("UTF-8");

        PushUrlCon.setParam  = paramData;
        PushUrlCon.setUrl    = url;

        try {
            returnData = PushUrlCon.getUrlConnect();
            JSONObject json = new JSONObject(returnData.getString("RESULT"));
            JSONObject jsonHeader = new JSONObject(json.get("HEADER").toString());
            String resultCode = jsonHeader.get("RESULT_CODE").toString();
            String resStr = "";
            try {
                resStr = json.get("BODY").toString();
                JSONObject jsonBody = new JSONObject(resStr);
                resultMap.put("bodyData",jsonBody);
            }catch (Exception e1){  //리시버에서 BODY 값을 안넘길 경우 예외처리
                resultMap.put("bodyData",null);
            }
            if(resultCode!=null && resultCode.equals("0000")){ //발송성공
                resultMap.put("resultCode","0000");
            }else{ //발송 실패
                resultMap.put("resultCode",resultCode);
            }
            resultMap.put("resultMsg",resStr);
        } catch (Exception e) {
            logger.error("Exception caught.", e);
            resultMap.put("resultCode","500");
            resultMap.put("resultMsg",messageSource.getMessage("menu.push.controller.notConnect", null, LocaleContextHolder.getLocale())); //"리시버와 연결되지 않았거나 데이타 오류입니다."
        }finally{

        }

        return resultMap;
    }
}
