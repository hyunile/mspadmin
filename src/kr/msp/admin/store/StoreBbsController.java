/**
 * 앱스토어WEB의 게시판을 관리 하는 콘트롤러
 *  sllim 
 *  2013.11.08
 */
package kr.msp.admin.store;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.bbs.dto.AppbbsDto;
import kr.msp.admin.store.bbs.dto.AppbbsSearchDto;
import kr.msp.admin.store.bbs.dto.FileUpload;
import kr.msp.admin.store.bbs.service.AppbbsService;

@Controller
public class StoreBbsController extends ControllerUtil {

	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	protected AppbbsService appbbsService;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 

	//메인페이지(GET) 
	@RequestMapping(value="/admin/store/bbs", method=RequestMethod.GET)
	public String appMain(Model model, AppbbsSearchDto dto){
		logger.debug("#################################" + this.getClass().getName());
		dto.setPAGE_NUM(1);
		dto.setPAGE_SIZE(row_size);
		if(StringUtils.isEmpty(dto.getBOARD_TYPE()))
			dto.setBOARD_TYPE("NOTICE");
		
		dto.setORDER_TARGET("REF");
		dto.setORDER_TYPE("DESC");
		
		model.addAttribute("bbsList", appbbsService.selectAppbbs(dto));
		model.addAttribute("type",    dto.getBOARD_TYPE());
		setPager(model, 1);

		return "admin/store/appbbs/appMain";
	}
	
	//메인페이지(POST) - 테이블내용만.
	@RequestMapping(value="admin/store/bbs/main", method=RequestMethod.POST)
	public String appMainPost(Model model, AppbbsSearchDto dto){
		logger.debug("#################################" + this.getClass().getName());
		dto.setORDER_TARGET("REF");
		dto.setORDER_TYPE("DESC");

		model.addAttribute("bbsList", appbbsService.selectAppbbs(dto));
		model.addAttribute("type",    dto.getBOARD_TYPE());
	    model.addAttribute("layout","layout/null.vm");
		setPager(model, dto.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",  dto.getPAGE_SIZE());

		return "admin/store/appbbs/appMain_table";
	}
	
	//게시물 상세보기(단일건수)
	@RequestMapping(value="admin/store/bbs/view", method=RequestMethod.POST)
	public String appMainView(Model model, AppbbsSearchDto dto){
		logger.debug("#################################" + this.getClass().getName());

		
		//카운트증가
		if(dto.getBOARD_IDX() != null && !"".equals(dto.getBOARD_IDX()))
		{
			appbbsService.updateHit(dto);
		}
		model.addAttribute("files",     appbbsService.selectAppbbsFiles(dto));
		model.addAttribute("row",       appbbsService.selectAppbbsView(dto));
		model.addAttribute("codeST001", appbbsService.selectCommoncode("QNA".equals(dto.getBOARD_TYPE()) ? "ST005" : "ST001"));//ST001,ST005
		//model.addAttribute("s_userid",  ObjectUtils.toString(request.getSession().getAttribute("S_USERID")));
	    model.addAttribute("layout","layout/null.vm");
		setPager(model, dto.getPAGE_NUM());
		return "admin/store/appbbs/view_table";
	}
	
	//게시물 저장하기(신규 및 수정)
	@RequestMapping( value="admin/store/bbs/saveBbs" , method=RequestMethod.POST)
	public String saveBbs(@ModelAttribute("upForm") FileUpload files, Model model, AppbbsDto dto, HttpServletRequest request){
		logger.debug("#################################" + this.getClass().getName());
		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		//printMap(makeHashMap(dto));
		dto.setREG_ID(S_ID_USER) ;
		
		AppbbsSearchDto searchDto = new AppbbsSearchDto();
		searchDto.setBOARD_TYPE(dto.getBOARD_TYPE());
		searchDto.setPAGE_NUM(dto.getPAGE_NUM());
		searchDto.setPAGE_SIZE(row_size);
		searchDto.setORDER_TARGET("REF");
		searchDto.setORDER_TYPE("DESC");

		model.addAttribute("success", StringUtils.isNotEmpty(dto.getBOARD_IDX()) ? appbbsService.updateBbs(dto, files) : appbbsService.insertBbs(dto, files));
		model.addAttribute("bbsList", appbbsService.selectAppbbs(searchDto));
		model.addAttribute("type",    dto.getBOARD_TYPE());
		setPager(model, dto.getPAGE_NUM());
		
		return "admin/store/appbbs/appMain";
	}
	
	//게시물 상세화면에서 파일한개 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/bbs/deleteFile" , method=RequestMethod.POST)
	public String deleteFile(Model model, AppbbsDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;
	
		JSONObject obj = new JSONObject();
		obj.put("success", appbbsService.deleteFile(dto));

		return obj.toString();
	}
	
	//게시물 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/bbs/deleteBbs" , method=RequestMethod.POST)
	public String deleteBbs(Model model, AppbbsDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;
	
		JSONObject obj = new JSONObject();
		obj.put("success", appbbsService.deleteBbs(dto));

		return obj.toString();
	}
	
	//체크된 게시물 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/bbs/deleteChk")
	public String deleteChk(Model model, AppbbsDto dto , HttpServletRequest request,
			  @ModelAttribute("DEL_CHK") ArrayList<String> DEL_CHK) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		logger.info("DEL_CHK : " + DEL_CHK);
		int dbResultSum = 0;
		int result = 0;
		try{
			for (int i = 0; i < DEL_CHK.size(); i++) {
				String idx = DEL_CHK.get(i) ;
				dto.setBOARD_IDX(idx);
				result = appbbsService.deleteBbs(dto); //앱정보 삭제
				 
				 logger.info("result : " + result);
				 dbResultSum += result;
			}
			logger.info("###dbResultSum :"+dbResultSum);
			logger.info("###DEL_CHK.SIZE : "+DEL_CHK.size());
			if(dbResultSum<DEL_CHK.size()){
				dbResultSum=0;
			}
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		}
		
		HashMap<String, Object> map = new HashMap<String,Object>();
	
		if(dbResultSum > 0){
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
		} else {
			map.put("result", result);
			map.put("msg",messageSource.getMessage("menu.store.notice.alert.notDelete", null, LocaleContextHolder.getLocale()) );  //"답변이 있는 게시글은 삭제가 불가능합니다."
		}
		ObjectMapper mapper = new ObjectMapper();
		String data = mapper.writeValueAsString(map);
		return data;
	}
}
