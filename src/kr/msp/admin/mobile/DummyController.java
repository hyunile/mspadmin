package kr.msp.admin.mobile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.mobile.dummy.dto.DummyDto;
import kr.msp.admin.mobile.dummy.dto.DummyParamDto;
import kr.msp.admin.mobile.dummy.mapper.DummyDao;
import kr.msp.admin.mobile.notice.dto.MobileSvcDto;

/**<pre>
 * DUMMY 데이터를 이용한 GW 전문개발을 위한 관리자 화면
 * </pre>
 * @author UncleJoe
 * @since 2014. 1. 4.
 */
@Controller
@RequestMapping("admin/mobile/dummy")
public class DummyController {

	private final static Logger logger = LoggerFactory.getLogger(DummyController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;


	@Autowired(required=true)
	private SqlSessionTemplate sqlSession;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 
	
	public DummyDao getDummyDao() {
		return sqlSession.getMapper(DummyDao.class);
	}
	
	/**
	 * dummy vm file path
	 */
	private String _VIEW_PATH = "admin/mobile/dummy/";
	
	
	/**
	 * 공통 APP SERVICE LIST
	 * @param request
	 * @return
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@ModelAttribute("mobileSvcList")
	public List<MobileSvcDto> getMobileSvcLists(HttpServletRequest request ) {

		MobileSvcDto mobileSvc = new MobileSvcDto();
		mobileSvc.setUSER_ID((String)request.getSession().getAttribute("S_ID_USER"));
		
		return getDummyDao().getMobileSvc(mobileSvc);
		

	}
	
	/**
	 * DUMMY MAIN
	 * @param model
	 * @param request
	 * @return
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@RequestMapping(method=RequestMethod.GET)
	public String getDummMain(Model model,HttpServletRequest request ){
		
		DummyParamDto dummyParam = new DummyParamDto();
		dummyParam.setPAGE_NUM(1);
		dummyParam.setPAGE_SIZE(row_size);
		dummyParam.setUserId((String)request.getSession().getAttribute("S_ID_USER"));
		
		List<DummyDto> dummyList = getDummyDao().getDummyList(dummyParam);
		
		model.addAttribute("R_PAGE_NUM",dummyParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("dummyList", dummyList );
		
		return _VIEW_PATH + "dummyMain";
	}
	
	/**
	 * DUMMY LIST AJAX PAGE
	 * @param model
	 * @param dummyParam
	 * @param request
	 * @return
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@RequestMapping(method=RequestMethod.POST)
	public String getDummyPost(Model model, DummyParamDto dummyParam,HttpServletRequest request){
		
		dummyParam.setUserId((String)request.getSession().getAttribute("S_ID_USER"));
		List<DummyDto> dummyList = getDummyDao().getDummyList(dummyParam);
		
		model.addAttribute("R_PAGE_NUM",dummyParam.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",dummyParam.getPAGE_SIZE());
		model.addAttribute("R_PAGE_SIZE",page_size);
		
		model.addAttribute("dummyList", dummyList );
		model.addAttribute("layout","layout/null.vm");
		
		return _VIEW_PATH + "dummyList";
	}
	
	
	/**
	 * DUMMY 등록 폼
	 * @param model
	 * @param request
	 * @return
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@RequestMapping(value="/regist",method=RequestMethod.GET)
	public String dummyRegistForm(Model model,HttpServletRequest request){
		
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
//		model.addAttribute("mobileSvcList",mobileSvcList);
		model.addAttribute("layout","layout/null.vm");
		
		return _VIEW_PATH + "dummyInfo";
	}
	
	/**
	 * DUMMY 수정 화면
	 * @param model
	 * @param dummyDto
	 * @return
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@RequestMapping(value="/edit",method=RequestMethod.GET)
	public String dummyEditForm(Model model,DummyDto dummyDto){
		
		DummyDto dummyInfo = getDummyDao().getDummyInfo(dummyDto);
		
		model.addAttribute("dummyInfo",dummyInfo);
		model.addAttribute("layout","layout/null.vm");
		
		return _VIEW_PATH + "dummyInfo";
	}
	 
	/**
	 * DUMMY 등록처리
	 * @param model
	 * @param request
	 * @param dummyDto
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping(value="/regist",method=RequestMethod.POST)
	public String dummyRegistExecute(Model model,HttpServletRequest request,DummyDto dummyDto) throws JsonGenerationException, JsonMappingException, IOException{
		
		
		int result = 0;
		try {
			result = getDummyDao().insertDummyInfo(dummyDto);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
	}
	
	//모바일 공지사항 등록 
	/**
	 * DUMMY INFO UPDATe 완료
	 * @param model
	 * @param request
	 * @param dummyDto
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping(value="/edit",method=RequestMethod.POST)
	public String dummyEditExecute(Model model,HttpServletRequest request,DummyDto dummyDto) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = getDummyDao().updateDummyInfo(dummyDto);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
	}
	 
	/**
	 * DUMMY DATA DELETE 
	 * @param model
	 * @param request
	 * @param dummyDto
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @since 2014. 1. 4. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping(value="/delete",method=RequestMethod.POST)
	public String dummyDeleteExecute(Model model,HttpServletRequest request,DummyDto dummyDto) throws JsonGenerationException, JsonMappingException, IOException{
		
		int result = 0;
		try {
			result = getDummyDao().deleteDummyInfo(dummyDto);
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		
		if(result > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale()) ); //"삭제에 실패했습니다."
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		
		return data;
	}
}
