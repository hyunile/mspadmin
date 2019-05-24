package kr.msp.admin.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.mobile.ResourceUploadController;
import kr.msp.admin.store.category.dto.CategoryDto;
import kr.msp.admin.store.category.dto.CategorySearchDto;
import kr.msp.admin.store.category.service.StoreCategoryService;

@Controller
public class StoreCategoryController extends ControllerUtil {
	
		private static final Logger logger = LoggerFactory.getLogger(ResourceUploadController.class);
		
		private String upCatgCd ="STORE"; //제일 상위 카테고리 CD
		
		@Autowired(required=true)
		protected StoreCategoryService storeCatgService;
		
		@Autowired(required = true) 
		private MessageSource messageSource; 
		
		//화면로딩시,  좌측 카테고리 트리 리스트조회
		@RequestMapping( value="admin/store/app/category" ,method=RequestMethod.GET)
		public String CategoryMainGet(Model model , HttpServletRequest request){
			
			CategorySearchDto  searchDto = new CategorySearchDto();
			searchDto.setCATG_CD(upCatgCd);
		
			//카테고리조회
			List<CategoryDto> categoryTreeList = storeCatgService.selectCategoryTreeList(searchDto);
			model.addAttribute("categoryTreeList", categoryTreeList);
			
			//카테고리 값이 존재하지 않을시, SVC_ID를 가져옴
			String SVC_ID = "";
			
			//if(categoryTreeList.size() < 1){
				SVC_ID = storeCatgService.selectSvcId();
			//}
			
			model.addAttribute("SVC_ID", SVC_ID);
			return "admin/store/category/categoryMain";
		}
		
		
		//좌측 카테고리 트리 리스트조회
		@RequestMapping( value="admin/store/app/category/tree" ,method=RequestMethod.POST)
		public String CategoryTreeGet(Model model , HttpServletRequest request){
			
			CategorySearchDto  searchDto = new CategorySearchDto();
			searchDto.setCATG_CD(upCatgCd);
		
			//카테고리조회
			List<CategoryDto> categoryTreeList = storeCatgService.selectCategoryTreeList(searchDto);
			model.addAttribute("categoryTreeList", categoryTreeList);
			
			//카테고리 값이 존재하지 않을시, SVC_ID를 가져옴
			String SVC_ID =  storeCatgService.selectSvcId();
			
			
			model.addAttribute("SVC_ID", SVC_ID);
			model.addAttribute( "layout", "layout/null.vm" );
			return "admin/store/category/categoryTree";
		}
		
		
		//하위카테고리  상세리스트 조회
		@RequestMapping(value="admin/store/app/category/categoryDetailList", method = RequestMethod.POST)
		public String categoryDetailList( Model model, HttpServletRequest request, CategorySearchDto searchDto ){
			
			List<CategoryDto> categoryDetailList = storeCatgService.selectCategoryDetailList(searchDto);
			
			model.addAttribute("categoryDetailList", categoryDetailList);
			model.addAttribute( "layout", "layout/null.vm" );
		
			
			return "admin/store/category/categoryDetailList";
		}
		
		//카테고리 상세정보조회
		@RequestMapping(value="admin/store/app/category/categoryInfo", method = RequestMethod.POST)
		public String categoryInfo( Model model, HttpServletRequest request, CategorySearchDto searchDto ){
			CategoryDto categoryInfo = null;
			if("STORE".equals(searchDto.getCATG_CD()) || "".equals(searchDto.getCATG_CD()))
			{
				categoryInfo = new CategoryDto();
				categoryInfo.setCATG_CD("STORE");
				categoryInfo.setCATG_DESC("STORE ROOT");
				categoryInfo.setCATG_NM("STORE");
				categoryInfo.setSORT_NO("0");
				categoryInfo.setUSE_YN("Y");
				categoryInfo.setSVC_ID(storeCatgService.selectSvcId());
			}
			else
			{
				categoryInfo = storeCatgService.selectCategoryInfo(searchDto);
			}	
			model.addAttribute("categoryInfo", categoryInfo);
			model.addAttribute( "layout", "layout/null.vm" );
			return "admin/store/category/categoryInfo";
		}
		
		
		//카테고리 저장
		@ResponseBody
		@RequestMapping( value="admin/store/app/category/save" ,method=RequestMethod.POST)
		public String saveCategory(Model model, CategoryDto categoryDto, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
			int result = 0;
			
			try {
				String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
				categoryDto.setREG_ID(S_ID_USER);
				categoryDto.setMOD_ID(S_ID_USER);
				
				String svcId = request.getParameter("SVC_ID");
				if(svcId == null || "".equals(svcId)){
					svcId = storeCatgService.selectSvcId();
				}
				
				categoryDto.setSVC_ID(svcId);
				
				//하위 카테고리가 존재하는 경우, 사용유무를 전체 업데이트시 FLAG[UPDATEALL_FLAG]가 필요함.
				String updateAllFlag = request.getParameter("UPDATEALL_FLAG");
				
				//전체 수정 경우, 상위 카테고리CD값에 해당하는 USE_YN을 전부 UPDATE
				if(updateAllFlag != null && ( "Y".equals(updateAllFlag) || "N".equals(updateAllFlag) ) ){
					categoryDto.setUSE_YN(updateAllFlag);
					storeCatgService.updateAllCategoryUseYn(categoryDto);
				}
				
				result = storeCatgService.saveCategory(categoryDto);
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			HashMap<String, Object> map = new HashMap<String,Object>();
			
			if(result > 0){
				map.put("result", result);
				map.put("msg",messageSource.getMessage("menu.store.device.alert.save", null, LocaleContextHolder.getLocale()) ); //"저장 되었습니다."
			} else {
				map.put("result", result);
				map.put("msg",messageSource.getMessage("menu.store.device.alert.saveFail", null, LocaleContextHolder.getLocale()) ); // "저장에 실패했습니다."
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}
		
		/**<pre>
		 * 카테고리 삭제처리
		 * 해당카테고리로 등록된 앱이 존재 할 경우 삭제불가 메시지 알림.
		 * </pre>
		 * @param model
		 * @param categoryDto
		 * @param request
		 * @return
		 * @throws JsonGenerationException
		 * @throws JsonMappingException
		 * @throws IOException
		 * @since 2014. 3. 19. by UncleJoe
		 */
		@ResponseBody
		@RequestMapping( value="admin/store/app/category/del" ,method=RequestMethod.POST)
		public String deleteCategory(Model model, CategoryDto categoryDto, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
			int result = 0;
			
			try {
				String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
				categoryDto.setREG_ID(S_ID_USER);
				categoryDto.setMOD_ID(S_ID_USER);
				
				String svcId = ServletRequestUtils.getStringParameter(request, "SVC_ID", this.storeCatgService.selectSvcId());
				
				categoryDto.setSVC_ID(svcId);
				
				result = storeCatgService.deleteCategoryInfo(categoryDto);
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			HashMap<String, Object> map = new HashMap<String,Object>();
			
			map.put("result", result);
			if(result == 999){
				map.put("msg",messageSource.getMessage("menu.store.controller.notDelete", null, LocaleContextHolder.getLocale()) ); //해당 카테고리로 등록된 App이 존재하여 삭제할 수 없습니다.
			} else if(result == 1){
				map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
			} else{
				map.put("msg",messageSource.getMessage("menu.store.device.alert.saveFail", null, LocaleContextHolder.getLocale()) ); // "저장에 실패했습니다."
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}
}
