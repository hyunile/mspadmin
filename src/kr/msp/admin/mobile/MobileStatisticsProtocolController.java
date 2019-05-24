package kr.msp.admin.mobile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.base.utils.ExcelUtil;
import kr.msp.admin.base.utils.FileDownloadUtil;
import kr.msp.admin.mobile.statistics.service.StatisticsService;


@RequestMapping(value="/admin/mobile/statistics/")
@Controller
public class MobileStatisticsProtocolController extends ControllerUtil{

    private Logger logger = Logger.getLogger(this.getClass().getName());
    //공통 페이지 로우수
    private @Value("${common.list.row_size}") int row_size;
    //공통 페이지 수
    private @Value("${common.list.page_size}") int page_size;

    @Autowired(required=true)
    protected StatisticsService statisticsService;

    @Autowired(required = true) 
	private MessageSource messageSource; 

    @RequestMapping(value="protocolStat", method= RequestMethod.GET)
    public ModelAndView UserStat(@RequestParam Map<String,Object> map, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = map.entrySet();
        for(Map.Entry<String,Object> me : s1){
            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
        }
        Map<String,Object> reqMap = new HashMap<String,Object>();
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        List<Map<String,Object>> mobServCodes = statisticsService.SelectSvcCode(reqMap);

        ModelAndView mv = new ModelAndView();
        mv.addObject("mobServiceList",mobServCodes);
        mv.setViewName("admin/mobile/statistics/protocolStat");

        return mv;
    }
    @RequestMapping(value="protocolStat", method= RequestMethod.POST)
    public ModelAndView UserStatPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = reqMap.entrySet();
        for(Map.Entry<String,Object> me : s1){
            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
        }
        String req_beginDate = reqMap.get("START_DT").toString();
        String req_endDate = reqMap.get("END_DT").toString();
        List<String> SearchDayList = new ArrayList<String>();      //시작일~종료일을 배열로 만들어 넣음

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        try {
            Date beginDate = formatter.parse(req_beginDate);
            Date endDate = formatter.parse(req_endDate);
            long diff = endDate.getTime() - beginDate.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTime(beginDate);
            String strDate = formatter.format(cal.getTime());
            SearchDayList.add(strDate.substring(8,10));
            for(int i=0; i<diffDays; i++){
                cal.add(Calendar.DATE,1);
                strDate = formatter.format(cal.getTime());
                SearchDayList.add(strDate.substring(8,10));
            }

        } catch (ParseException e) {
            logger.error("Exception caught.", e);  //To change body of catch statement use File | Settings | File Templates.
            logger.error("ERROR:"+e.toString());
        }

        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));

        ModelAndView mv = new ModelAndView();
        mv.setViewName("admin/mobile/statistics/protocolList");
        mv.addObject("statisticsList", statisticsService.SelectProtocolStatistics(reqMap));
        mv.addObject("daylist", SearchDayList);
        mv.addObject("R_PAGE_NUM", reqMap.get("PAGE_NUM"));  //페이지 번호
        mv.addObject("R_ROW_SIZE", reqMap.get("PAGE_SIZE"));  // row 수
        mv.addObject("R_PAGE_SIZE", 10);    //페이지 블럭수

        mv.addObject("layout","layout/null.vm");
        return mv;
    }

    @RequestMapping(value="protocolgraph" , method=RequestMethod.POST)
    public ModelAndView StatisticsProtocolGraphPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request){
        Set<Map.Entry<String,Object>> s1 = reqMap.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        String req_beginDate = reqMap.get("START_DT").toString();
        String req_endDate = reqMap.get("END_DT").toString();
        List<Map<String,Object>> CharDataList = new ArrayList<Map<String,Object>>();      //시작일~종료일을 배열로 만들어 넣음
        List<Map<String,Object>> chartDB_Datas = statisticsService.SelectProtocolStatisticsChart(reqMap);    /// 쿼리가 한의 로우만 나옴
        Map<String,Object> DBMap = null;
        if(chartDB_Datas!=null && chartDB_Datas.size()>0){
            DBMap = chartDB_Datas.get(0);
        }

        if(DBMap == null)
        {
            DBMap = new HashMap<String, Object>();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        try {

            Map<String,Object> DateMap = new HashMap<String, Object>();
            Date beginDate = formatter.parse(req_beginDate);
            Date endDate = formatter.parse(req_endDate);
            long diff = endDate.getTime() - beginDate.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTime(beginDate);
            String strDate = formatter.format(cal.getTime());
            if(DBMap.containsKey("D1")){
                DateMap.put("REG_DT",strDate.substring(5,10));
                DateMap.put("CNT",DBMap.get("D1"));
            }else{
                DateMap.put("REG_DT",strDate.substring(5,10));
                DateMap.put("CNT",0);
            }

            CharDataList.add(DateMap);
            for(int i=0; i<diffDays; i++){
                Map<String,Object> DateMap2 = new HashMap<String, Object>();
                cal.add(Calendar.DATE,1);
                strDate = formatter.format(cal.getTime());
                if(DBMap.containsKey("D"+(i+2))){
                    DateMap2.put("REG_DT",strDate.substring(5,10));
                    DateMap2.put("CNT",DBMap.get("D"+(i+2)));
                }else{
                    DateMap2.put("REG_DT",strDate.substring(5,10));
                    DateMap2.put("CNT",0);
                }
                CharDataList.add(DateMap2);
            }

        } catch (ParseException e) {
            logger.error("Exception caught.", e);  //To change body of catch statement use File | Settings | File Templates.
            logger.error("ERROR:"+e.toString());
        }
        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));
        ModelAndView mv = new ModelAndView();
        mv.addObject("layout", "layout/null.vm");
        mv.addObject("chartDataList",CharDataList);
        mv.setViewName("admin/mobile/statistics/protocolGraph");

        return mv;
    }

    @RequestMapping( value="protocolexcel" ,method=RequestMethod.POST)
    public void StatisticsProtocolExcelPost(@RequestParam Map<String,Object> reqMap, HttpServletRequest request,HttpServletResponse response){

        Set<Map.Entry<String,Object>> s1 = reqMap.entrySet();
//        for(Map.Entry<String,Object> me : s1){
//            logger.info("############넘어온 파라미터:"+me.getKey() + ":" + me.getValue());
//        }
        String req_beginDate = reqMap.get("START_DT").toString();
        String req_endDate = reqMap.get("END_DT").toString();
        List<String> SearchDayList = new ArrayList<String>();      //시작일~종료일을 배열로 만들어 넣음

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
        try {
            Date beginDate = formatter.parse(req_beginDate);
            Date endDate = formatter.parse(req_endDate);
            long diff = endDate.getTime() - beginDate.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTime(beginDate);
            String strDate = formatter.format(cal.getTime());
            SearchDayList.add(messageSource.getMessage("menu.stats.mobile.protocolCode", null, LocaleContextHolder.getLocale())); //"전문코드"
            SearchDayList.add(strDate.substring(8,10)+messageSource.getMessage("common.text.day", null, LocaleContextHolder.getLocale()));  //일
            for(int i=0; i<diffDays; i++){
                cal.add(Calendar.DATE,1);
                strDate = formatter.format(cal.getTime());
                SearchDayList.add(strDate.substring(8,10)+messageSource.getMessage("common.text.day", null, LocaleContextHolder.getLocale())); //일
            }

        } catch (ParseException e) {
            logger.error("Exception caught.", e);  //To change body of catch statement use File | Settings | File Templates.
            logger.error("ERROR:"+e.toString());
        }

        reqMap.put("S_ID_USER",(String) request.getSession().getAttribute("S_ID_USER"));

        //통계목록 조회
        //String str_fileName = DEFAULT_STORE_DIRECTORY + "/excelTemplete/statistics_join.xlsx";
        List<String> cols = new ArrayList<String>();
        cols.add("COMP_CD");
        for(int i=1; i<SearchDayList.size(); i++){
            cols.add( "D"+(i));
        }
        String[] colArr = cols.toArray(new String[cols.size()]);
        String[] titleArr = SearchDayList.toArray(new String[SearchDayList.size()]);
        int[] widthArr = {3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000};
        List<HashMap<String,Object>> list = statisticsService.SelectProtocolStatisticsJoinExcel(reqMap);


        // 다운로드 시작..
        try {
            XSSFWorkbook wb = ExcelUtil.createListXlsx(list, titleArr, widthArr, colArr);
            FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
            fileDownloadUtil.fileDownload(wb, "statistics_join.xlsx",response);
        } catch (Exception e) {
            logger.error("Exception caught.", e);
        } finally {
        }
    }

}
