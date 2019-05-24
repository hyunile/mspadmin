package kr.msp.admin.mobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.mobile.statistics.service.StatisticsService;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14. 2. 28
 * Time: 오후 12:57
 * To change this template use File | Settings | File Templates.
 */

@RequestMapping(value="/admin/mobile/statistics/")
@Controller
public class MobileStaticsOSController {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    @Autowired(required=true)
    protected StatisticsService statisticsService;

    @RequestMapping(value="OSStat", method= RequestMethod.GET)
    public ModelAndView OSStat(@RequestParam Map<String,Object> map, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = map.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        List<Map<String,Object>> mobServCodes = statisticsService.SelectSvcCode(reqMap);

        ModelAndView mv = new ModelAndView();
        mv.addObject("mobServiceList",mobServCodes);
        mv.setViewName("admin/mobile/statistics/OSStat");

        return mv;
    }
    @RequestMapping(value="OSStat", method= RequestMethod.POST)
    public ModelAndView OSStatPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = reqMap.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        String req_beginDate = reqMap.get("START_DT").toString();
        String req_endDate = reqMap.get("END_DT").toString()+" 23:59:59";
        reqMap.put("END_DT",req_endDate);
        List<HashMap<String,Object>> dbList = new ArrayList<HashMap<String, Object>>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        dbList = statisticsService.SelectOSStatistics(reqMap);

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
        mv.setViewName("admin/mobile/statistics/OSStatList");
        mv.addObject("jsonList", data);
        mv.addObject("statisticsList", dbList);
        mv.addObject("MaxCnt",MaxCnt);
        mv.addObject("R_PAGE_NUM", reqMap.get("PAGE_NUM"));  //페이지 번호
        mv.addObject("R_ROW_SIZE", reqMap.get("PAGE_SIZE"));  // row 수
        mv.addObject("R_PAGE_SIZE", 10);    //페이지 블럭수

        mv.addObject("layout","layout/null.vm");
        return mv;
    }

}
