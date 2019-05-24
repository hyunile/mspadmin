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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.user.dto.EmpCdDto;
import kr.msp.admin.store.user.dto.UserDivisionDto;
import kr.msp.admin.store.user.dto.UserDto;
import kr.msp.admin.store.user.dto.UserParamDto;
import kr.msp.admin.store.user.service.StoreUserService;
import kr.msp.util.Sha256Util;

@Controller
public class StoreUserController extends ControllerUtil{
	
		private static final Logger logger = LoggerFactory.getLogger(StoreUserController.class);
		
		private @Value("${common.dir.store}") String DEFAULT_STORE_DIRECTORY;	//기본 앱스토어 디렉토리
		
		
		private String EMP_CD_KIND	= "01";		// 직급구분코드 종류
		
		
		//공통 페이지 로우수
		private @Value("${common.list.row_size}") int row_size;
		//공통 페이지 수
		private @Value("${common.list.page_size}") int page_size;
		
		@Autowired(required=true)
		protected StoreUserService m_service;
		
		@Autowired(required = true) 
	    private MessageSource messageSource; 
       
		
		private List<EmpCdDto> selectEmpCdList(String kind)
		{
			EmpCdDto param = new EmpCdDto();
			param.setKIND(kind);
			return m_service.selectEmpCdList(param);
		}
		
		//화면로딩리스트조회
		@RequestMapping( value="admin/store/user_dbg" ,method=RequestMethod.GET)
		public String UserMainGet_dbg(Model model , HttpServletRequest request){
			logger.info("####################################### UserMainGet ") ;
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			
			UserParamDto  userDto = new UserParamDto();
			
			//userDto.setBOARD_TYPE(boardType);
			userDto.setPAGE_NUM(1);
			userDto.setPAGE_SIZE(row_size);
			userDto.setKIND(EMP_CD_KIND);
			
			

			//회원목록 조회
			model.addAttribute("userList", m_service.selectUserList(userDto));
			model.addAttribute("empList", selectEmpCdList(EMP_CD_KIND));			// 직급코드

			
			model.addAttribute("R_PAGE_NUM",userDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",userDto.getPAGE_SIZE());
			model.addAttribute("R_PAGE_SIZE",page_size);
			
			model.addAttribute("rtnCd", rtnCd);
			
			if("insertOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
			}else if("insertFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("insertFail_1".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("updateOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}else if("updateUseYNOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateUseYNFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}
		
			return "admin/store/user/userMain_dbg";
		}
		//화면로딩리스트조회
		@RequestMapping( value="admin/store/user" ,method=RequestMethod.GET)
		public String UserMainGet(Model model , HttpServletRequest request){
			logger.info("####################################### UserMainGet ") ;
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			
			UserParamDto  userDto = new UserParamDto();
			
			//userDto.setBOARD_TYPE(boardType);
			userDto.setPAGE_NUM(1);
			userDto.setPAGE_SIZE(row_size);
			userDto.setKIND(EMP_CD_KIND);
			
			
			
			//회원목록 조회
			model.addAttribute("userList", m_service.selectUserList(userDto));
			model.addAttribute("empList", selectEmpCdList(EMP_CD_KIND));			// 직급코드
			
			
			model.addAttribute("R_PAGE_NUM",userDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",userDto.getPAGE_SIZE());
			model.addAttribute("R_PAGE_SIZE",page_size);
			
			model.addAttribute("rtnCd", rtnCd);
			
			if("insertOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
			}else if("insertFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("insertFail_1".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("updateOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}else if("updateUseYNOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateUseYNFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}
			
			return "admin/store/user/userMain";
		}
		
		//화면리스트조회
		@RequestMapping( value="admin/store/user" ,method=RequestMethod.POST)
		public String UserMainPost(Model model, HttpServletRequest request,  UserParamDto userDto){
			logger.info("####################################### UserMainPost ") ;
//			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			logger.info("###msg "  + request.getParameter("msg")) ;
			//userDto.setPAGE_SIZE(row_size);
			
			userDto.setKIND(EMP_CD_KIND);
			
			//회원목록 조회
			model.addAttribute("userList", m_service.selectUserList(userDto));
			model.addAttribute("empList", selectEmpCdList(EMP_CD_KIND));			// 직급코드

			model.addAttribute("R_PAGE_NUM",userDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",userDto.getPAGE_SIZE());
			model.addAttribute("R_PAGE_SIZE",page_size);

			setOrder(model, userDto.getORDER_FIELD(), userDto.getORDER_TYPE());
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/user/userList";
		}
		
		//등록화면 이동
		@RequestMapping( value="/admin/store/user/regist" , method=RequestMethod.GET )
		public String UserRegistGet(Model model){

			model.addAttribute("empList", selectEmpCdList(EMP_CD_KIND));			// 직급코드
			model.addAttribute("workcdList", m_service.selectWorkCdList());

			model.addAttribute("layout", "layout/null.vm");
			return "/admin/store/user/userInfo";
		}
		
		
		//등록
		@RequestMapping( value="/admin/store/user/regist" , method=RequestMethod.POST )
		public String UserRegistPost(Model model,  HttpServletRequest request, UserDto userDto){
			
			logger.debug("//////////////////" + userDto.getEMP_NO());
			
			if(userDto.getPASSWD() != null && !userDto.getPASSWD().equals("")){
//				userDto.setPASSWD( CryptoUtils.encrypt(userDto.getPASSWD()) );
				userDto.setPASSWD( Sha256Util.getEncrypt(userDto.getPASSWD()) );
			}
			
			String rtnCd = "999";
			int result = 0;
			
			try {
				if(m_service.selectUserCount(userDto)==0)
				{
					result = m_service.insertUsers(userDto);
					if(result > 0){
						rtnCd = "insertOk";
					} else {
						rtnCd = "insertFail";
					}
					
				}
				else
				{
					rtnCd = "insertFail_1";
				}
				
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			return "redirect:/admin/store/user?rtnCd=" + rtnCd;
		}
	
		//상세 정보 조회
		@RequestMapping( value="admin/store/user/edit" ,method=RequestMethod.GET)
		public String UserEditGet(Model model, UserParamDto userSearchDto){

			userSearchDto.setKIND(EMP_CD_KIND);
			UserDto dto = m_service.selectOneUser(userSearchDto);
			List<UserDivisionDto> divisionList = m_service.selectDivisonList();
			
			model.addAttribute("divisionList", divisionList);
			model.addAttribute("userDto", dto);
			model.addAttribute("empList", selectEmpCdList(EMP_CD_KIND));			// 직급코드
			model.addAttribute("workcdList", m_service.selectWorkCdList());
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/user/userInfo";
		}
		
		
		//수정
		@RequestMapping( value="admin/store/user/edit" , method=RequestMethod.POST)
		public String UserEditPost( Model model,  HttpServletRequest request, UserDto userDto ) throws JsonGenerationException, JsonMappingException, IOException{
			logger.info("######### 수정 START ##################33");
			logger.info("request  BOARD_IDX=================" + request.getParameter("BOARD_IDX"));	
			
			if(userDto.getPASSWD() != null && !userDto.getPASSWD().equals("")){
//				userDto.setPASSWD( EncryptoUtil.encrypt(userDto.getPASSWD()) );
//				userDto.setPASSWD( CryptoUtils.encrypt(userDto.getPASSWD()) );
				userDto.setPASSWD( Sha256Util.getEncrypt(userDto.getPASSWD()) );
			}

 			logger.debug("EMP_NO="+userDto.getEMP_NO());
		    logger.debug("EMP_CD="+userDto.getEMP_CD());
		    logger.debug("WORK_CD="+userDto.getWORK_CD());
		    logger.debug("EMAIL="+userDto.getEMAIL());
			
			
			String rtnCd = "999";
			int result = 0;
			
			try {
				
				result = m_service.updateUsers(userDto);
				
				if(result > 0){
					rtnCd = "updateOk";
				} else {
					rtnCd = "updateFail";
				}
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			return "redirect:/admin/store/user?rtnCd=" + rtnCd;
		}
		
		
		
		//수정
		@RequestMapping( value="admin/store/user/edit_useyn" , method=RequestMethod.POST)
		public String UserEditUseYNPost( Model model,  HttpServletRequest request, UserParamDto userDto ) throws JsonGenerationException, JsonMappingException, IOException{
			
			
			userDto.setKIND(EMP_CD_KIND);
			String rtnCd = "999";
			int result = 0;
			logger.debug(userDto.getEMP_NO());
			try {
				
				result = m_service.updateUseYN(userDto);
				
				if(result > 0){
					rtnCd = "updateUseYNOk";
				} else {
					rtnCd = "updateUseYNFail";
				}
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			
			
			if("insertOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
			}else if("insertFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("insertFail_1".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("updateOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}else if("updateUseYNOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateUseYNFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}
			model.addAttribute("rtnCd", rtnCd);
			
			
			
//			return "redirect:/admin/store/user?rtnCd=" + rtnCd;
			return "forward:/admin/store/user";
		}
		
		@ResponseBody
		@RequestMapping( value="admin/store/user/delete" ,method=RequestMethod.POST)
		public String UserEditDelete(Model model, UserDto userDto , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
			
			int result = 0;
			
			try {
				result = m_service.deleteUsers(userDto);	  //삭제
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			HashMap<String, Object> map = new HashMap<String,Object>();
			
			if(result > 0){
				map.put("result", result);
				map.put("msg",messageSource.getMessage("menu.mobile.common.successDelete", null, LocaleContextHolder.getLocale()) ); //"삭제 되었습니다."
			} else {
				map.put("result", -1);
				map.put("msg",messageSource.getMessage("menu.mobile.common.failDelete", null, LocaleContextHolder.getLocale())
						+"\n("+messageSource.getMessage("menu.store.controller.firstDelete", null, LocaleContextHolder.getLocale())+")" );  // 삭제에 실패했습니다. 권한이나 그룹정보를 먼저 삭제하세요.
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}
		
		@ResponseBody
		@RequestMapping( value="admin/store/user/empnoCh" ,method=RequestMethod.POST)
		public String UserEmpnoCh(Model model, UserDto userDto ) throws JsonGenerationException, JsonMappingException, IOException{
			
			int result = 0;
			
			try {
				result = m_service.selectUserCount(userDto);	  //삭제
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			HashMap<String, Object> map = new HashMap<String,Object>();
			
			if(result < 1){
				map.put("result", 1);
				map.put("msg",messageSource.getMessage("menu.store.controller.useNumber", null, LocaleContextHolder.getLocale()) ); //사용할수있는 사번입니다.  
			} else {
				map.put("result", -1 );
				map.put("msg",messageSource.getMessage("menu.store.controller.sameNumber", null, LocaleContextHolder.getLocale()) ); //중복하는 사번이있습니다.
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}
		
		@ResponseBody
		@RequestMapping( value="admin/store/user/useridCh" ,method=RequestMethod.POST)
		public String UseridCh(Model model, UserDto userDto ) throws JsonGenerationException, JsonMappingException, IOException{
			
			int result = 0;
			
			try {
				result = m_service.selectUserCount(userDto);	  //삭제
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			HashMap<String, Object> map = new HashMap<String,Object>();
			
			if(result < 1){
				map.put("result", 1);
				map.put("msg",messageSource.getMessage("menu.store.controller.useID", null, LocaleContextHolder.getLocale()) ); //사용할수있는 아이디입니다.
			} else {
				map.put("result", -1);
				map.put("msg",messageSource.getMessage("menu.store.controller.sameID", null, LocaleContextHolder.getLocale()) ); //중복하는 아이디가있습니다.
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String data = mapper.writeValueAsString(map);
			return data;
		}
		
		
}
