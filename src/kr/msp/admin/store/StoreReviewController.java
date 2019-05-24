package kr.msp.admin.store;



import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.review.dto.StoreReviewDeleteParamDto;
import kr.msp.admin.store.review.dto.StoreReviewDto;
import kr.msp.admin.store.review.dto.StoreReviewParamDto;
import kr.msp.admin.store.review.service.StoreReviewService;
import kr.msp.admin.store.service.dto.ServiceDto;



@Controller
public class StoreReviewController extends ControllerUtil{
	
//		private static final Logger logger = LoggerFactory.getLogger(ResourceUploadController.class);
		
		private @Value("${common.dir.store}") String DEFAULT_STORE_DIRECTORY;	//기본 앱스토어 디렉토리
		
		public final static int row_size = 10;
		//공통 페이지 수
		public final static int page_size = 5;
				
//		//공통 페이지 로우수
//		private @Value("${common.list.row_size}") int row_size;
//		//공통 페이지 수
//		private @Value("${common.list.page_size}") int page_size;
		
		@Autowired(required=true)
		protected StoreReviewService m_service;
       
		
		
		//스토어 서비스 화면 로딩 조회 
		@RequestMapping(value="admin/store/review" , method=RequestMethod.GET)
		public String ReviewListGet( Model model, HttpServletRequest request ){
			StoreReviewParamDto param = new StoreReviewParamDto();
			param.setORDER_FIELD("REG_DT");
			param.setORDER_TYPE("DESC");
			
			param.setPAGE_NUM(1);
			param.setPAGE_SIZE(row_size);
			
			model.addAttribute("revList", m_service.selectReviewList(param));
			
			
			model.addAttribute("R_PAGE_NUM",  param.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",  param.getPAGE_SIZE());
			model.addAttribute("R_PAGE_SIZE", page_size );
			
			setOrder(model, param.getORDER_FIELD(),param.getORDER_TYPE());
			
			return "admin/store/review/reviewMain";
		}
		
		
		
		
		@RequestMapping(value="admin/store/review/list" , method=RequestMethod.POST)
		public String ReviewListPost( Model model, ServiceDto serviceDto, HttpServletRequest request , StoreReviewParamDto param){
			
			model.addAttribute("revList", m_service.selectReviewList(param));
			
			
			model.addAttribute("R_PAGE_NUM",  param.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",  param.getPAGE_SIZE());
			model.addAttribute("R_PAGE_SIZE", page_size );
			
			setOrder(model, param.getORDER_FIELD(),param.getORDER_TYPE());
			
			model.addAttribute("layout", "layout/null.vm");
			return "admin/store/review/reviewList";
		}
		
		
		@RequestMapping(value="admin/store/review/deleteReview" , method=RequestMethod.POST)
		public String ReviewDeletePost( Model model, ServiceDto serviceDto, HttpServletRequest request, StoreReviewParamDto param ){
			
			String uid = (String) request.getSession().getAttribute("S_ID_USER");
			
			
			
			// ------------------------------------- 삭제용 파라메터 생성 --------------------------------------
			String[] paramArr = param.getDELEV_DATA().split("#");
			
			StoreReviewDeleteParamDto[] delParams = new StoreReviewDeleteParamDto[paramArr.length];
			for(int i=0; i<paramArr.length; i++)
			{
				delParams[i] = new StoreReviewDeleteParamDto();
				delParams[i].setRV_NO(Integer.parseInt(paramArr[i]));
			}
			// ------------------------------------- 삭제 --------------------------------------
			
			
			
			HashMap<String, Object> delParam = new HashMap<String, Object>();
			delParam.put("USERID", uid);
			delParam.put("PARAM_BEAN", delParams);
			m_service.deleteReviewInfo(delParam);
			
			
			model.addAttribute("revList", m_service.selectReviewList(param));
			
			model.addAttribute("R_PAGE_NUM",  param.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",  param.getPAGE_SIZE());
			model.addAttribute("R_PAGE_SIZE", param.getPAGE_SIZE());
			
			model.addAttribute("layout", "layout/null.vm");
			return "admin/store/review/reviewList";
		}
		
		@RequestMapping(value="admin/store/review/info" , method=RequestMethod.GET)
		public String ReviewListPost( Model model, StoreReviewDto storeReview){
			
			
			StoreReviewDto storeReviewOne = m_service.selectReviewInfo(storeReview);
			
			model.addAttribute("storeReviewOne", storeReviewOne);
			model.addAttribute("layout", "layout/null.vm");
			return "admin/store/review/reviewInfo";
		}
		
}
