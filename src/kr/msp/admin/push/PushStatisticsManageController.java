package kr.msp.admin.push;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.push.appManage.dto.PushServiceDto;
import kr.msp.admin.push.msgSend.service.MsgSendManageService;
import kr.msp.admin.push.statistics.dto.PushStatisticsMsgDTO;
import kr.msp.admin.push.statistics.dto.PushStatisticsPDTO;
import kr.msp.admin.push.statistics.service.IPushStatisticsService;
import kr.msp.common.util.DateUtil;

@Controller
public class PushStatisticsManageController {

    private final static Logger logger = LoggerFactory.getLogger(PushStatisticsManageController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;

    private @Value("${admin.push.receiver.host}") String DEFAULT_RECEIVER_HOST;	//리시버 호스트
	
	@Autowired(required=true)
	private IPushStatisticsService pushStatisticsService;
	
	@Autowired(required=true)
	private MsgSendManageService msgSendManage;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	
    @RequestMapping(value="/admin/push/regUserStat ", method= RequestMethod.GET)
    public ModelAndView regUserStatGet(@RequestParam Map<String,Object> map, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = map.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        //PUSH 서비스 조회
        PushServiceDto pushService = new PushServiceDto();
        pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
        List<PushServiceDto> pushServiceList = pushStatisticsService.SelectPushServiceAll(pushService);

        ModelAndView mv = new ModelAndView();
        mv.addObject("pushServiceList",pushServiceList);
        mv.setViewName("admin/push/pushStatistics/regUserStatMain");

        return mv;
    }

    @RequestMapping(value="/admin/push/regUserStat", method= RequestMethod.POST)
    public ModelAndView UserStatPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = reqMap.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        String req_beginDate = reqMap.get("START_DT").toString();
        String req_endDate = reqMap.get("END_DT").toString()+" 23:59:59";
        reqMap.put("END_DT",req_endDate);
        List<HashMap<String,Object>> dbList = new ArrayList<HashMap<String, Object>>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        dbList = pushStatisticsService.SelectPeroidStatistics(reqMap);

        String req_priodType = reqMap.get("priodType").toString();
        String ChartLabel = "&nbsp;"+messageSource.getMessage("menu.stats.common.monthUseStat", null, LocaleContextHolder.getLocale());  // 월별 사용통계
        if(req_priodType.equals("H")){     //시간별 통계
            ChartLabel = "&nbsp;"+messageSource.getMessage("menu.stats.common.timeUseStat", null, LocaleContextHolder.getLocale()); // 시간별 사용통계
        }else if(req_priodType.equals("W")){     //주별 통계
            ChartLabel = "&nbsp"+messageSource.getMessage("menu.stats.common.weekUseStat", null, LocaleContextHolder.getLocale());  // 주별 사용통계
        }else if(req_priodType.equals("D")){     //일별 통계
            ChartLabel = "&nbsp;"+messageSource.getMessage("menu.stats.common.dayUseStat", null, LocaleContextHolder.getLocale()); //일별 사용통계 
        }
        
        List<Map<String, Object>> TuningDBList = new ArrayList<Map<String, Object>>();
		try {
			TuningDBList = tuningDbMap(req_priodType, reqMap.get("START_DT").toString(), reqMap.get("END_DT").toString(), dbList);
		} catch (Exception e1) {
			logger.error("Exception caught.", e1);
		}

		int MaxCnt = 0;
        for(Map<String, Object> row : TuningDBList) {
            if(row.containsKey("CNT") && row.get("CNT") != null) {
                int current = Integer.parseInt(row.get("CNT").toString());
                if(MaxCnt < current) {
                    MaxCnt = current;
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = null;
        try {
            data = mapper.writeValueAsString(TuningDBList);
        } catch (IOException e) {
            logger.error("Exception caught.", e);  //To change body of catch statement use File | Settings | File Templates.
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/push/pushStatistics/regUserStatList");
        mv.addObject("jsonList", data);
        mv.addObject("statisticsList", TuningDBList);
        mv.addObject("ChartLabel",ChartLabel);
        mv.addObject("req_priodType",req_priodType);
        mv.addObject("MaxCnt",MaxCnt);
        mv.addObject("R_PAGE_NUM", reqMap.get("PAGE_NUM"));  //페이지 번호
        mv.addObject("R_ROW_SIZE", reqMap.get("PAGE_SIZE"));  // row 수
        mv.addObject("R_PAGE_SIZE", 10);    //페이지 블럭수

        mv.addObject("layout","layout/null.vm");
        return mv;
    }
    
    /**
     * DB에서 조회한 통계 데이터를 보정처리함.
     * @param req_priodType
     * @param startDt
     * @param endDt
     * @param dbList
     * @return
     * @throws Exception
     */
    protected List<Map<String,Object>> tuningDbMap(String req_priodType, String startDt, String endDt, List<HashMap<String,Object>> dbList) throws Exception {
        List<Map<String,Object>> TuningDBList = new ArrayList<Map<String, Object>>();
        
        if(dbList!=null && dbList.size()>0){
            for(int i=0; i<dbList.size(); i++){
                Map<String,Object> TuningDBMap = new HashMap<String, Object>();
                Map<String,Object> dbMap = dbList.get(i);
                String DB_DT = dbMap.get("DT").toString();
                if(req_priodType.equals("H")){    //시간별
                    DB_DT = DB_DT+messageSource.getMessage("common.text.hour", null, LocaleContextHolder.getLocale());//"시";
                }else if(req_priodType.equals("W")){  //주별
                    switch (Integer.parseInt(DB_DT)){
                    case 1:
                        DB_DT = messageSource.getMessage("common.text.sunday", null, LocaleContextHolder.getLocale()); //"일요일";
                        break;
                    case 2:
                        DB_DT = messageSource.getMessage("common.text.monday", null, LocaleContextHolder.getLocale()); //"월요일";
                        break;
                    case 3:
                        DB_DT = messageSource.getMessage("common.text.tuesday", null, LocaleContextHolder.getLocale()); //"화요일";
                        break;
                    case 4:
                        DB_DT = messageSource.getMessage("common.text.wednesday", null, LocaleContextHolder.getLocale()); //"수요일";
                        break;
                    case 5:
                        DB_DT = messageSource.getMessage("common.text.thursday", null, LocaleContextHolder.getLocale()); //"목요일";
                        break;
                    case 6:
                        DB_DT = messageSource.getMessage("common.text.friday", null, LocaleContextHolder.getLocale()); //"금요일";
                        break;
                    case 7:
                        DB_DT = messageSource.getMessage("common.text.saturday", null, LocaleContextHolder.getLocale()); //"토요일";
                        break;
                    }
                }else{    //일별

                    System.out.println("###############DB_DT:"+DB_DT);
                    if(DB_DT.length()==8) {
                        DB_DT = DB_DT.substring(0, 4) + "." + DB_DT.substring(4, 6) + "." + DB_DT.substring(6, 8);
                    }
                }
                TuningDBMap.put("DT",DB_DT);
                TuningDBMap.put("CNT",dbMap.get("CNT"));
                TuningDBList.add(TuningDBMap);
                int connectCnt = Integer.parseInt(dbMap.get("CNT").toString());
            }
        }
        
        // 값이 없는 날짜, 요일, 시간도 표시되도록 보정 처리.
        if(req_priodType.equals("H")){
        	String timeStr = "00";
        	for(int i=0;i<24;i++){
        		if(i<10)timeStr = "0" + i + messageSource.getMessage("common.text.hour", null, LocaleContextHolder.getLocale());//"시";
        		else timeStr = i + messageSource.getMessage("common.text.hour", null, LocaleContextHolder.getLocale());//"시";
        		boolean findFlag = false;
        		for(int j=0;j<TuningDBList.size();j++){
        			Map<String, Object> _dbMap = TuningDBList.get(j);
        			if(_dbMap != null && _dbMap.get("DT") != null && timeStr.equals(""+_dbMap.get("DT"))){
        				findFlag = true;
        				break;
        			}
        		}
        		if(!findFlag){
        			Map<String, Object> absentDbMap = new HashMap<String, Object>();
        			absentDbMap.put("DT", timeStr);
        			absentDbMap.put("CNT", 0);
        			if(i>TuningDBList.size())
        				TuningDBList.add(absentDbMap);
        			else
        				TuningDBList.add(i, absentDbMap);
        		}
        	}
        }else if(req_priodType.equals("W")){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        	Calendar cal1 = Calendar.getInstance();
        	cal1.setTime(sdf.parse(startDt));
        	
        	Calendar cal2 = Calendar.getInstance();
        	cal2.setTime(sdf.parse(endDt));
        	int _idx = 0;
        	while(cal1.getTimeInMillis() <= cal2.getTimeInMillis()){
        		String timeStr = "";
        		switch (cal1.get(Calendar.DAY_OF_WEEK)){
                case 1:
                	timeStr = messageSource.getMessage("common.text.sunday", null, LocaleContextHolder.getLocale()); //"일요일";
                    break;
                case 2:
                	timeStr = messageSource.getMessage("common.text.monday", null, LocaleContextHolder.getLocale()); //"월요일";
                    break;
                case 3:
                	timeStr = messageSource.getMessage("common.text.tuesday", null, LocaleContextHolder.getLocale()); //"화요일";
                    break;
                case 4:
                	timeStr = messageSource.getMessage("common.text.wednesday", null, LocaleContextHolder.getLocale()); //"수요일";
                    break;
                case 5:
                	timeStr = messageSource.getMessage("common.text.thursday", null, LocaleContextHolder.getLocale()); //"목요일";
                    break;
                case 6:
                	timeStr = messageSource.getMessage("common.text.friday", null, LocaleContextHolder.getLocale()); //"금요일";
                    break;
                case 7:
                	timeStr = messageSource.getMessage("common.text.saturday", null, LocaleContextHolder.getLocale()); //"토요일";
                    break;
        		}
        		boolean findFlag = false;
        		for(int j=0;j<TuningDBList.size();j++){
        			Map<String, Object> _dbMap = TuningDBList.get(j);
        			if(_dbMap != null && _dbMap.get("DT") != null && timeStr.equals(""+_dbMap.get("DT"))){
        				findFlag = true;
        				break;
        			}
        		}
        		if(!findFlag){
        			Map<String, Object> absentDbMap = new HashMap<String, Object>();
        			absentDbMap.put("DT", timeStr);
        			absentDbMap.put("CNT", 0);
        			if(_idx>TuningDBList.size())
        				TuningDBList.add(absentDbMap);
        			else
        				TuningDBList.add(_idx, absentDbMap);
        		}
        		cal1.add(Calendar.DAY_OF_WEEK, 1);
        		_idx++;
        	}
        }else if(req_priodType.equals("D")){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        	Calendar cal1 = Calendar.getInstance();
        	cal1.setTime(sdf.parse(startDt));
        	
        	Calendar cal2 = Calendar.getInstance();
        	cal2.setTime(sdf.parse(endDt));
        	int _idx = 0;
        	while(cal1.getTimeInMillis() <= cal2.getTimeInMillis()){
        		
        		String timeStr = sdf.format(cal1.getTime());
        		boolean findFlag = false;
        		for(int j=0;j<TuningDBList.size();j++){
        			Map<String, Object> _dbMap = TuningDBList.get(j);
        			if(_dbMap != null && _dbMap.get("DT") != null && timeStr.equals(""+_dbMap.get("DT"))){
        				findFlag = true;
        				break;
        			}
        		}
        		if(!findFlag){
        			Map<String, Object> absentDbMap = new HashMap<String, Object>();
        			absentDbMap.put("DT", timeStr);
        			absentDbMap.put("CNT", 0);
        			if(_idx>TuningDBList.size())
        				TuningDBList.add(absentDbMap);
        			else
        				TuningDBList.add(_idx, absentDbMap);
        		}
        		cal1.add(Calendar.DATE, 1);
        		_idx++;
        	}
        }else if(req_priodType.equals("M")){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        	Calendar cal1 = Calendar.getInstance();
        	cal1.setTime(sdf.parse(startDt));
        	
        	Calendar cal2 = Calendar.getInstance();
        	cal2.setTime(sdf.parse(endDt));
        	int _idx = 0;
        	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
        	while(cal1.getTimeInMillis() <= cal2.getTimeInMillis()){
        		String timeStr = sdf1.format(cal1.getTime());
        		boolean findFlag = false;
        		for(int j=0;j<TuningDBList.size();j++){
        			Map<String, Object> _dbMap = TuningDBList.get(j);
        			if(_dbMap != null && _dbMap.get("DT") != null && timeStr.equals(""+_dbMap.get("DT"))){
        				findFlag = true;
        				break;
        			}
        		}
        		if(!findFlag){
        			Map<String, Object> absentDbMap = new HashMap<String, Object>();
        			absentDbMap.put("DT", timeStr);
        			absentDbMap.put("CNT", 0);
        			if(_idx>TuningDBList.size())
        				TuningDBList.add(absentDbMap);
        			else
        				TuningDBList.add(_idx, absentDbMap);
        		}
        		cal1.add(Calendar.MONTH, 1);
        		_idx++;
        	}
        }
        return TuningDBList;
    }

    @RequestMapping(value="/admin/push/regPnsidStat", method= RequestMethod.GET)
    public ModelAndView regPnsidStatGet(@RequestParam Map<String,Object> map, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = map.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));

        PushServiceDto pushService = new PushServiceDto();
        pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
        List<PushServiceDto> pushServiceList = pushStatisticsService.SelectPushServiceAll(pushService);

        ModelAndView mv = new ModelAndView();
        mv.addObject("pushServiceList",pushServiceList);
        mv.setViewName("admin/push/pushStatistics/pnsidStatMain");

        return mv;
    }
    @RequestMapping(value="/admin/push/regPnsidStat", method= RequestMethod.POST)
    public ModelAndView regPnsidStatPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = reqMap.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        String req_beginDate = reqMap.get("START_DT").toString();
        String req_endDate = reqMap.get("END_DT").toString()+" 23:59:59";
        reqMap.put("END_DT",req_endDate);
        List<HashMap<String,Object>> dbList = new ArrayList<HashMap<String, Object>>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        dbList = pushStatisticsService.selectPerioOSStatistics(reqMap);

        int MaxCnt = 0;
        if(dbList!=null && dbList.size()>0){
            for(int i=0; i<dbList.size(); i++){
                Map<String,Object> dbMap = dbList.get(i);
                int connectCnt = Integer.parseInt(dbMap.get("CNT").toString());
                if(MaxCnt<connectCnt){
                    MaxCnt=connectCnt;
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String data = null;
        try {
            data = mapper.writeValueAsString(dbList);
        } catch (IOException e) {
            logger.error("Exception caught.", e);  //To change body of catch statement use File | Settings | File Templates.
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/push/pushStatistics/pnsidStatList");
        mv.addObject("jsonList", data);
        mv.addObject("statisticsList", dbList);
        mv.addObject("MaxCnt",MaxCnt);
        mv.addObject("R_PAGE_NUM", reqMap.get("PAGE_NUM"));  //페이지 번호
        mv.addObject("R_ROW_SIZE", reqMap.get("PAGE_SIZE"));  // row 수
        mv.addObject("R_PAGE_SIZE", 10);    //페이지 블럭수

        mv.addObject("layout","layout/null.vm");
        return mv;
    }

    @RequestMapping(value="/admin/push/sysSentStatus", method= RequestMethod.GET)
    public ModelAndView getSysSentStatusGet(@RequestParam Map<String,Object> map, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = map.entrySet();
        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        reqMap.put("PAGE_NUM","1");
        reqMap.put("PAGE_SIZE",""+row_size);
        

        List<Map<String,Object>> senderCodeList = pushStatisticsService.selectSenderCode(reqMap);
        List<Map<String,Object>> sysSentMsgList = pushStatisticsService.selectSysSentMsg(reqMap);
        ModelAndView mv = new ModelAndView();
        
        mv.addObject("senderCodeList",senderCodeList);
        mv.addObject("sysSentMsgList",sysSentMsgList);
        mv.addObject("DEFAULT_RECEIVER_HOST",DEFAULT_RECEIVER_HOST);
        
        mv.addObject("R_PAGE_NUM", 1);
        mv.addObject("R_ROW_SIZE",row_size);
        mv.addObject("R_PAGE_SIZE",page_size);
        mv.setViewName("admin/push/pushStatistics/sysMsgStatusMain");
        return mv;
    }

    @RequestMapping(value="/admin/push/sysSentStatus", method= RequestMethod.POST)
    public ModelAndView getSysSentStatusPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request){

    	String req_beginDate = (String)reqMap.get("START_DT");
        String req_endDate = (String)reqMap.get("END_DT");   
        reqMap.put("START_DT", req_beginDate.replaceAll("[.]", ""));  
        reqMap.put("END_DT", req_endDate.replaceAll("[.]", "")); 

        List<Map<String,Object>> senderCodeList = pushStatisticsService.selectSenderCode(reqMap);
        List<Map<String,Object>> sysSentMsgList = pushStatisticsService.selectSysSentMsg(reqMap);

        ModelAndView mv = new ModelAndView();
        
        mv.addObject("senderCodeList",senderCodeList);
        mv.addObject("sysSentMsgList",sysSentMsgList);
        mv.addObject("DEFAULT_RECEIVER_HOST",DEFAULT_RECEIVER_HOST);

        mv.addObject("R_PAGE_NUM",reqMap.get("PAGE_NUM"));
        mv.addObject("R_PAGE_SIZE", page_size);
        mv.addObject("R_ROW_SIZE",reqMap.get("PAGE_SIZE"));

        mv.setViewName("admin/push/pushStatistics/sysMsgStatusList");
        mv.addObject("layout","layout/null.vm");
        return mv;
    }
    
    @RequestMapping(value="/admin/push/staticsMsg", method=RequestMethod.GET )
	public String getStaticsMsg(Model model, HttpServletRequest request, PushStatisticsPDTO pushStatisticsPDTO) {
		
		//PUSH 서비스 조회
		PushServiceDto pushService = new PushServiceDto();
		pushService.setUSER_ID((String) request.getSession().getAttribute("S_ID_USER"));
		List<PushServiceDto> pushServiceList = msgSendManage.SelectPushServiceAll(pushService);
		
		pushStatisticsPDTO.setFROM_DATE(DateUtil.getBaseFromDate());
		pushStatisticsPDTO.setTO_DATE(DateUtil.getBaseToDate());
		
		pushStatisticsPDTO.setPAGE_NUM(1);
		pushStatisticsPDTO.setPAGE_SIZE(row_size);
		
		PushStatisticsMsgDTO totPushStatisticsMsgDTO = pushStatisticsService.getTotStaticsMsg(pushStatisticsPDTO);
		if(totPushStatisticsMsgDTO == null){
			totPushStatisticsMsgDTO = new PushStatisticsMsgDTO();
		}
		totPushStatisticsMsgDTO.setSUCC_CNT(totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() - totPushStatisticsMsgDTO.getFAIL_CNT());
		
		Double succRate = Math.floor((double)totPushStatisticsMsgDTO.getSUCC_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setSUCC_RATE(succRate.intValue());
		Double failRate = Math.floor((double)totPushStatisticsMsgDTO.getFAIL_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setFAIL_RATE(failRate.intValue());
		Double recvRate = Math.floor((double)totPushStatisticsMsgDTO.getRECV_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setRECV_RATE(recvRate.intValue());
		Double readRate = Math.floor((double)totPushStatisticsMsgDTO.getREAD_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setREAD_RATE(readRate.intValue());
		model.addAttribute("totPushStatisticsMsg", totPushStatisticsMsgDTO);
		
		List<PushStatisticsMsgDTO> listPushStatisticsMsgDTO = pushStatisticsService.getStaticsMsgList(pushStatisticsPDTO);
		
		model.addAttribute("pushServiceList", pushServiceList);
		model.addAttribute("listPushStatisticsMsgDTO", listPushStatisticsMsgDTO);
		
		model.addAttribute("R_PAGE_NUM", pushStatisticsPDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE", row_size);
		model.addAttribute("R_PAGE_SIZE", page_size);
		return "admin/push/staticsMsg/staticsMsgMain";
	}
    
    @RequestMapping(value="/admin/push/staticsMsg", method=RequestMethod.POST)
	public String getStaticsMsgPost( Model model, HttpServletRequest request,PushStatisticsPDTO pushStatisticsPDTO) {
		pushStatisticsPDTO.setFROM_DATE(pushStatisticsPDTO.getFROM_DATE() +" 00:00:00");
		pushStatisticsPDTO.setTO_DATE(pushStatisticsPDTO.getTO_DATE() +" 23:59:59");
		
		PushStatisticsMsgDTO totPushStatisticsMsgDTO = pushStatisticsService.getTotStaticsMsg(pushStatisticsPDTO);
		if(totPushStatisticsMsgDTO == null){
			totPushStatisticsMsgDTO = new PushStatisticsMsgDTO();
		}
		totPushStatisticsMsgDTO.setSUCC_CNT(totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() - totPushStatisticsMsgDTO.getFAIL_CNT());
		
		Double succRate = Math.floor((double)totPushStatisticsMsgDTO.getSUCC_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setSUCC_RATE(succRate.intValue());
		Double failRate = Math.floor((double)totPushStatisticsMsgDTO.getFAIL_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setFAIL_RATE(failRate.intValue());
		Double recvRate = Math.floor((double)totPushStatisticsMsgDTO.getRECV_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setRECV_RATE(recvRate.intValue());
		Double readRate = Math.floor((double)totPushStatisticsMsgDTO.getREAD_CNT() / (double)totPushStatisticsMsgDTO.getTOTAL_SEND_CNT() * 100);
		totPushStatisticsMsgDTO.setREAD_RATE(readRate.intValue());
		model.addAttribute("totPushStatisticsMsg", totPushStatisticsMsgDTO);
		
		List<PushStatisticsMsgDTO> listPushStatisticsMsgDTO = pushStatisticsService.getStaticsMsgList(pushStatisticsPDTO);
		model.addAttribute("listPushStatisticsMsgDTO", listPushStatisticsMsgDTO);
		
		model.addAttribute("R_PAGE_NUM", pushStatisticsPDTO.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE", pushStatisticsPDTO.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE", page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/push/staticsMsg/staticsMsgList";
	}
    @RequestMapping(value="/admin/push/staticsMsgDetail", method=RequestMethod.GET)
	public String getStaticsMsgDetail( Model model, HttpServletRequest request,PushStatisticsPDTO pushStatisticsPDTO) {
    	
//    	if(pushStatisticsPDTO.getDATE() == null){
//    		pushStatisticsPDTO.setDATE(DateUtil.getDateByCurrent(0));
//    	}
//    	
//    	List<PushStatisticsMsgDTO> listPushStatisticsMsgDTO = pushStatisticsService.getStaticsRecvMsgGraphByHour(pushStatisticsPDTO);
//    	model.addAttribute("listMsgByHour", listPushStatisticsMsgDTO);
    	model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/push/staticsMsg/staticsMsgDetail";
    }	
}
