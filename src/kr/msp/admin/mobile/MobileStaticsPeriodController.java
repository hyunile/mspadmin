package kr.msp.admin.mobile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.mobile.statistics.service.StatisticsService;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14. 2. 26
 * Time: 오후 2:33
 * To change this template use File | Settings | File Templates.
 */
@RequestMapping(value="/admin/mobile/statistics/")
@Controller
public class MobileStaticsPeriodController {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    @Autowired(required=true)
    protected StatisticsService statisticsService;
    
    @Autowired(required = true) 
	private MessageSource messageSource; 

    @RequestMapping(value="periodStat", method= RequestMethod.GET)
    public ModelAndView UserStat(@RequestParam Map<String,Object> map, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = map.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        List<Map<String,Object>> mobServCodes = statisticsService.SelectSvcCode(reqMap);

        ModelAndView mv = new ModelAndView();
        mv.addObject("mobServiceList",mobServCodes);
        mv.setViewName("admin/mobile/statistics/periodStat");

        return mv;
    }
    
    @RequestMapping(value="periodStat", method= RequestMethod.POST)
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
        dbList = statisticsService.SelectPeroidStatistics(reqMap);

        String req_priodType = reqMap.get("priodType").toString();
        String ChartLabel = "&nbsp;"+messageSource.getMessage("menu.stats.common.dayUseStat", null, LocaleContextHolder.getLocale()); //일별 사용통계 
        if(req_priodType.equals("H")){     //시간별 통계
            ChartLabel = "&nbsp;"+messageSource.getMessage("menu.stats.common.timeUseStat", null, LocaleContextHolder.getLocale()); // 시간별 사용통계
        }else if(req_priodType.equals("W")){     //주별 통계
            ChartLabel = "&nbsp;"+messageSource.getMessage("menu.stats.common.weekUseStat", null, LocaleContextHolder.getLocale());  // 주별 사용통계
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
        mv.setViewName("admin/mobile/statistics/periodList");
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
                    DB_DT = DB_DT+ messageSource.getMessage("common.text.hour", null, LocaleContextHolder.getLocale());//"시";
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
        		if(i<10)timeStr = "0" + i + messageSource.getMessage("common.text.hour", null, LocaleContextHolder.getLocale()); //시
        		else timeStr = i + messageSource.getMessage("common.text.hour", null, LocaleContextHolder.getLocale()); //시
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

}
