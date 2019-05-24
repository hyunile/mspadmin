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
import kr.msp.admin.store.statistics.dto.AppCodeDto;
import kr.msp.admin.store.statistics.dto.StatisticsDownloadDto;
import kr.msp.admin.store.statistics.dto.StatisticsDownloadParamDto;
import kr.msp.admin.store.statistics.dto.SvcCodeDto;
import kr.msp.admin.store.statistics.service.StatisticsDownloadService;


@Controller
public class StoreStatisticsDownloadController extends ControllerUtil{
	
		private final static Logger logger = LoggerFactory.getLogger(StoreStatisticsDownloadController.class);
		
		private @Value("${common.dir.store}") String DEFAULT_STORE_DIRECTORY;	//기본 앱스토어 디렉토리
		
				
		//공통 페이지 로우수
		private @Value("${common.list.row_size}") int row_size;
		//공통 페이지 수
		private @Value("${common.list.page_size}") int page_size;
		
		@Autowired(required=true)
		protected StatisticsDownloadService m_service;
       
		
		
		
		
		private void validation_dateParams(StatisticsDownloadParamDto searchDto)
		{
			SimpleDateFormat dFormat = new SimpleDateFormat ( "yyyyMMdd" );
			Calendar cal = Calendar.getInstance ( );
			
			
			if(searchDto.getSTART_DT() == null || "".equals(searchDto.getSTART_DT()))
			{
				// 현재 달의 1일 날짜
				cal.set(Calendar.DATE, 1);
				searchDto.setSTART_DT(dFormat.format ( cal.getTime ( ) ));
			}
			
			if(searchDto.getEND_DT() == null || "".equals(searchDto.getEND_DT()))
			{
				// 현재 달의 마지막 날짜
				cal.set(Calendar.DATE, cal.getActualMaximum ( Calendar.DATE ));
				searchDto.setEND_DT(dFormat.format ( cal.getTime ( ) ) );
			}
		}
		
		
		
		
		//화면로딩리스트조회
		@RequestMapping( value="admin/store/statistics/download" ,method=RequestMethod.GET)
		public String StatisticsDownloadMainGet(Model model , HttpServletRequest request){

			StatisticsDownloadParamDto searchDto = new StatisticsDownloadParamDto();

			validation_dateParams(searchDto);
			// paging 설정
			searchDto.setPAGE_NUM(1);
			searchDto.setPAGE_SIZE(row_size);


			
			
			
			
	
			// 서비스 ID 목록 조회
			
			
			//통계목록 조회
			List<SvcCodeDto> svcCodeList = m_service.selectSvcCode(searchDto);
			model.addAttribute("svcCodeList", svcCodeList);
			if( (searchDto.getSVC_ID()==null || "".equals(searchDto.getSVC_ID())) && svcCodeList.size()>0)
			{
				searchDto.setSVC_ID(String.valueOf(svcCodeList.get(0).getSVC_ID()));
			}
			else
			{
				searchDto.setSVC_ID("");		// 쿼리 에러 방지를 위해서..
			}
			List<AppCodeDto> appCode =  m_service.selectAppCode(searchDto);
			if( (searchDto.getAPP_IDX()==null || "".equals(searchDto.getAPP_IDX())) && appCode.size()>0)
			{
				searchDto.setAPP_IDX(String.valueOf(appCode.get(0).getAPP_IDX()));
			}
			else
			{
				searchDto.setAPP_IDX("");		// 쿼리 에러 방지를 위해서..
			}
			
			
			model.addAttribute("appCodeList", appCode);
			
			if(
					(searchDto.getAPP_IDX() != null && !"".equals(searchDto.getAPP_IDX()))
				&& (searchDto.getAPP_IDX() != null && !"".equals(searchDto.getAPP_IDX()))
				)
			{
				model.addAttribute("rangeList", m_service.selectStatisticsRangeDownload(searchDto));
				model.addAttribute("prevList", m_service.selectStatisticsPrevDownload(searchDto));
				model.addAttribute("currentList", m_service.selectStatisticsCurrentDownload(searchDto));
			}
			
			model.addAttribute("searchParam", searchDto);
			

			model.addAttribute("R_CUR_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_CUR_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_CUR_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			model.addAttribute("R_PREV_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_PREV_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_PREV_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			model.addAttribute("R_RANGE_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_RANGE_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_RANGE_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			model.addAttribute("rtnCd", rtnCd);
			
			
			return "admin/store/statistics/downloadMain";
		}
		
		
		
		//화면리스트조회
		@RequestMapping( value="admin/store/statistics/download" ,method=RequestMethod.POST)
		public String StatisticsDownloadMainPost(Model model, HttpServletRequest request,  StatisticsDownloadParamDto searchDto){

			
			// paging 설정
			searchDto.setPAGE_NUM(1);
			searchDto.setPAGE_SIZE(row_size);

			
			
			validation_dateParams(searchDto);
			

			
			//통계목록 조회
			List<SvcCodeDto> svcCodeList = m_service.selectSvcCode(searchDto);
			model.addAttribute("svcCodeList", svcCodeList);

			if( (searchDto.getSVC_ID()==null || "".equals(searchDto.getSVC_ID())) && svcCodeList.size()>0)
			{
				searchDto.setSVC_ID(String.valueOf(svcCodeList.get(0).getSVC_ID()));
			}

			
			model.addAttribute("appCodeList", m_service.selectAppCode(searchDto));
			model.addAttribute("rangeList", m_service.selectStatisticsRangeDownload(searchDto));
			model.addAttribute("prevList", m_service.selectStatisticsPrevDownload(searchDto));
			model.addAttribute("currentList", m_service.selectStatisticsCurrentDownload(searchDto));
			
			
			model.addAttribute("searchParam", searchDto);
			

			model.addAttribute("R_CUR_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_CUR_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_CUR_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			model.addAttribute("R_PREV_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_PREV_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_PREV_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			model.addAttribute("R_RANGE_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_RANGE_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_RANGE_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			model.addAttribute("rtnCd", rtnCd);
			
		
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/statistics/downloadLayout";
		}

		

		
		//전월 리스트조회
		@RequestMapping( value="admin/store/statistics/downListPrev" ,method=RequestMethod.POST)
		public String StatisticsDownloadPrevPost(Model model, HttpServletRequest request,  StatisticsDownloadParamDto searchDto){

			
			validation_dateParams(searchDto);
			
			//통계목록 조회
			model.addAttribute("prevList", m_service.selectStatisticsPrevDownload(searchDto));
			
			model.addAttribute("searchParam", searchDto);
			
			model.addAttribute("R_PREV_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_PREV_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_PREV_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			model.addAttribute("rtnCd", rtnCd);
			
		
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/statistics/downloadPrevList";
		}

		//화면리스트조회
		@RequestMapping( value="admin/store/statistics/downListCurrent" ,method=RequestMethod.POST)
		public String StatisticsDownloadCurrentPost(Model model, HttpServletRequest request,  StatisticsDownloadParamDto searchDto){

			
			// paging 설정
			
			validation_dateParams(searchDto);
			

			
			//통계목록 조회
			
			model.addAttribute("currentList", m_service.selectStatisticsCurrentDownload(searchDto));
			
			
			model.addAttribute("searchParam", searchDto);
			

			model.addAttribute("R_CUR_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_CUR_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_CUR_ROW_SIZE", searchDto.getPAGE_SIZE());
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			model.addAttribute("rtnCd", rtnCd);
			
		
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/statistics/downloadCurrentList";
		}

		//기간별 조회
		@RequestMapping( value="admin/store/statistics/downListRange" ,method=RequestMethod.POST)
		public String StatisticsDownloadRangePost(Model model, HttpServletRequest request,  StatisticsDownloadParamDto searchDto){
			//joinSearchDto.setPAGE_SIZE(row_size);
			//userSearchDto.setBOARD_TYPE(boardType);
			
			validation_dateParams(searchDto);
			
			//통계목록 조회
			List<StatisticsDownloadDto> list = m_service.selectStatisticsRangeDownload(searchDto);
			model.addAttribute("rangeList", list);

			model.addAttribute("searchParam", searchDto);

			// 
			model.addAttribute("R_RANGE_PAGE_NUM",searchDto.getPAGE_NUM());
			model.addAttribute("R_RANGE_PAGE_SIZE",searchDto.getPAGE_SIZE());
			model.addAttribute("R_RANGE_ROW_SIZE", searchDto.getPAGE_SIZE());
			
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/statistics/downloadRangeList";
		}
		
		
		
		
		@RequestMapping( value="admin/store/statistics/downStatisticsExcel" ,method=RequestMethod.POST)
		public void StatisticsUserExcelPost(Model model,  HttpServletRequest request ,HttpServletResponse response, StatisticsDownloadParamDto searchDto){
			
			validation_dateParams(searchDto);

			//통계목록 조회
			
			
	
			//String str_fileName = DEFAULT_STORE_DIRECTORY + "/excelTemplete/statistics_join.xlsx";
			String[] colArr = {"APP_IDX", "APP_NM", "CNT"};
			String[] titleArr = {"순번", "이름", "횟수"};
			int[] widthArr = {3000, 3000, 3000};
			List<StatisticsDownloadDto> list = m_service.selectStatisticsRangeDownload(searchDto);

			
			// 다운로드 시작..
			try {
				XSSFWorkbook wb = ExcelUtil.createListXlsx(makeList(list), titleArr, widthArr, colArr);
				FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
				fileDownloadUtil.fileDownload(wb, "다운로드통계현황.xlsx",response);
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			} finally {
			}
		}
}
