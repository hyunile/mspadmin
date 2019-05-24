package kr.msp.admin.store;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.base.utils.ExcelUtil;
import kr.msp.admin.base.utils.FileDownloadUtil;
import kr.msp.admin.store.statistics.dto.StatisticsJoinExcelDto;
import kr.msp.admin.store.statistics.dto.StatisticsJoinGraphDto;
import kr.msp.admin.store.statistics.dto.StatisticsJoinParamDto;
import kr.msp.admin.store.statistics.service.StatisticsJoinService;


@RequestMapping(value="/admin/store")
@Controller
public class StoreStatisticsJoinController extends ControllerUtil{

		private final static Logger logger = LoggerFactory.getLogger(StoreStatisticsJoinController.class);

		//private static final Logger logger = LoggerFactory.getLogger(ResourceUploadController.class);
		
//		private @Value("${common.dir.store}") String DEFAULT_STORE_DIRECTORY;	//기본 앱스토어 디렉토리
//		private @Value("${common.resources.path}") String RESOURCES_PATH;
//		private @Value("${common.root.path}") String CONTEXT_PATH;		
				
		//공통 페이지 로우수
		private @Value("${common.list.row_size}") int row_size;
		//공통 페이지 수
		private @Value("${common.list.page_size}") int page_size;
		
		@Autowired(required=true)
		protected StatisticsJoinService m_service;
       
		
		
		
		
		private void validation_dateParams(StatisticsJoinParamDto joinSearchDto)
		{
			SimpleDateFormat dFormat = new SimpleDateFormat ( "yyyyMMdd" );
			Calendar cal = Calendar.getInstance ( );
			
			
			if(joinSearchDto.getSC_GBN_DATE() == 0)
			{
				// 월별 검색일때
				String yyyy =  joinSearchDto.getS_DT_YYYY();
				String mm = joinSearchDto.getS_DT_MM();
				String dd = "";
				
				if(yyyy==null || "".equals(yyyy))
				{
					yyyy = String.valueOf(cal.get(Calendar.YEAR));
					joinSearchDto.setS_DT_YYYY(yyyy);
				}
				if(mm == null || "".equals(mm))
				{
					mm = String.valueOf(101+cal.get(Calendar.MONTH)).substring(1);
					joinSearchDto.setS_DT_MM(mm);
				}
				
				
				int iMM = Integer.parseInt(mm) ;	// 1부터 시작하는 월
				dd = String.valueOf(
												iMM<=7 ? ( (iMM%2) == 1 ? 31 : 30)	// 7월 까지는 홀수달이 큰달
															: 	( (iMM%2) == 0 ? 31 : 30)	// 8월 부터는 짝수달이 큰달
												);
				
				joinSearchDto.setSTART_DT( yyyy + mm + "01");
				joinSearchDto.setEND_DT( yyyy + mm + dd);		// 최대 날짜가 31일이 아니지만.....
			}
			
			
			if(joinSearchDto.getSTART_DT() == null || "".equals(joinSearchDto.getSTART_DT()))
			{
				// 현재 달의 1일 날짜
				cal.set(Calendar.DATE, 1);
				joinSearchDto.setSTART_DT(dFormat.format ( cal.getTime ( ) ));
			}
			
			if(joinSearchDto.getEND_DT() == null || "".equals(joinSearchDto.getEND_DT()))
			{
				// 현재 달의 마지막 날짜
				cal.set(Calendar.DATE, cal.getActualMaximum ( Calendar.DATE ));
				joinSearchDto.setEND_DT(dFormat.format ( cal.getTime ( ) ) );
			}
		}
		
		
		
		
		//화면로딩리스트조회
		@RequestMapping( value="statistics/join" ,method=RequestMethod.GET)
		public String StatisticsUserMainGet(Model model , HttpServletRequest request){

			StatisticsJoinParamDto joinSearchDto = new StatisticsJoinParamDto();

			// paging 설정
			joinSearchDto.setPAGE_NUM(1);
			joinSearchDto.setPAGE_SIZE(row_size);

			joinSearchDto.setRECORD_CNT(20);
			joinSearchDto.setSVC_ID(1);
			
			
			
			validation_dateParams(joinSearchDto);
			
			
			model.addAttribute("svcCodeList", m_service.selectSvcCode(joinSearchDto));
			//통계목록 조회
			model.addAttribute("statisticsList", m_service.selectStatisticsJoin(joinSearchDto));
			model.addAttribute("searchParam", joinSearchDto);

			model.addAttribute("R_PAGE_NUM",joinSearchDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",joinSearchDto.getRECORD_CNT());
			model.addAttribute("R_PAGE_SIZE",joinSearchDto.getPAGE_SIZE());

			
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			model.addAttribute("rtnCd", rtnCd);
			
			
			
			
			// 통계 페이지의 초기에는 아무 내용도 출력하지 않음.
			return "admin/store/statistics/joinMain";
		}
		

		//화면리스트조회
		@RequestMapping( value="statistics/join" ,method=RequestMethod.POST)
		public String StatisticsUserMainPost(Model model, HttpServletRequest request,  StatisticsJoinParamDto joinSearchDto){
			//joinSearchDto.setPAGE_SIZE(row_size);
			//userSearchDto.setBOARD_TYPE(boardType);
			
			if(joinSearchDto.getSC_GBN_DATE() == 1) {
				if(joinSearchDto.getSTART_DT() != null) {
					joinSearchDto.setSTART_DT(joinSearchDto.getSTART_DT().replaceAll("\\.", "") );
				}
				if(joinSearchDto.getEND_DT() != null) {
					joinSearchDto.setEND_DT(joinSearchDto.getEND_DT().replaceAll("\\.", "") );
				}
			}
			
			validation_dateParams(joinSearchDto);
			
			
			

			model.addAttribute("svcCodeList", m_service.selectSvcCode(joinSearchDto));
			//통계목록 조회
			model.addAttribute("statisticsList", m_service.selectStatisticsJoin(joinSearchDto));
			model.addAttribute("searchParam", joinSearchDto);
			
			
			model.addAttribute("R_PAGE_NUM",joinSearchDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",joinSearchDto.getRECORD_CNT());
			model.addAttribute("R_PAGE_SIZE",page_size);

			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/statistics/joinList";
		}
		
		
		@RequestMapping(value="statistics/graph" , method=RequestMethod.POST)
		public String StatisticsUserGraphPost(Model model, HttpServletRequest request,  StatisticsJoinParamDto joinSearchDto){
			
			if(joinSearchDto.getSC_GBN_DATE() == 1) {
				if(joinSearchDto.getSTART_DT() != null) {
					joinSearchDto.setSTART_DT(joinSearchDto.getSTART_DT().replaceAll("\\.", "") );
				}
				if(joinSearchDto.getEND_DT() != null) {
					joinSearchDto.setEND_DT(joinSearchDto.getEND_DT().replaceAll("\\.", "") );
				}
			}
			
			validation_dateParams(joinSearchDto);

			
			
			List<StatisticsJoinGraphDto> chartDataList = m_service.selectStatisticsGraph(joinSearchDto);
			
			//model.addAttribute("RESOURCES_PATH", RESOURCES_PATH);
			//model.addAttribute("CONTEXT_PATH", CONTEXT_PATH);
			
			
			model.addAttribute("chartDataList", chartDataList);
			model.addAttribute("layout", "layout/null.vm");
			
			return "admin/store/statistics/joinGraph";
		}
		
		
		
		@RequestMapping( value="statistics/excel" ,method=RequestMethod.POST)
		public void StatisticsUserExcelPost(Model model,  HttpServletRequest request ,HttpServletResponse response, StatisticsJoinParamDto joinSearchDto){
			
			if(joinSearchDto.getSC_GBN_DATE() == 1) {
				if(joinSearchDto.getSTART_DT() != null) {
					joinSearchDto.setSTART_DT(joinSearchDto.getSTART_DT().replaceAll("\\.", "") );
				}
				if(joinSearchDto.getEND_DT() != null) {
					joinSearchDto.setEND_DT(joinSearchDto.getEND_DT().replaceAll("\\.", "") );
				}
			}
			
			validation_dateParams(joinSearchDto);

			//통계목록 조회
			//String str_fileName = DEFAULT_STORE_DIRECTORY + "/excelTemplete/statistics_join.xlsx";
			String[] colArr = {"RNUM", "D01", "D02", "D03", "D04", "D05", "D06", "D07", "D08", "D09", "D10", "D11", "D12", "D13", "D14", "D15", "D16", "D17", "D18", "D19", "D20", "D21", "D22", "D23", "D24", "D25", "D26", "D27", "D28", "D29", "D30", "D31"};
			String[] titleArr = {"순번", "1일", "2일", "3일", "4일", "5일", "6일", "7일", "8일", "9일", "10일", "11일", "12일", "13일", "14일", "15일", "16일", "17일", "18일", "19일", "20일", "21일", "22일", "23일", "24일", "25일", "26일", "27일", "28일", "29일", "30일", "31일"};
			int[] widthArr = {3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000, 3000};
			List<StatisticsJoinExcelDto> list = m_service.selectStatisticsExcel(joinSearchDto);

			
			// 다운로드 시작..
			try {
				XSSFWorkbook wb = ExcelUtil.createListXlsx(makeList(list), titleArr, widthArr, colArr);
				FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
				fileDownloadUtil.fileDownload(wb, "statistics_join.xlsx",response);
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			} finally {
			}
		}
}
