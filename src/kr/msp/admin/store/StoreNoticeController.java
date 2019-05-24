package kr.msp.admin.store;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.mobile.ResourceUploadController;
import kr.msp.admin.store.notice.dto.NoticeCommonDto;
import kr.msp.admin.store.notice.dto.NoticeDto;
import kr.msp.admin.store.notice.dto.NoticeFileDto;
import kr.msp.admin.store.notice.dto.NoticeFileUpload;
import kr.msp.admin.store.notice.dto.NoticeSearchDto;
import kr.msp.admin.store.notice.service.StoreNoticeService;

@Controller
public class StoreNoticeController {
	
		private static final Logger logger = LoggerFactory.getLogger(ResourceUploadController.class);
		private File destinationDir = null;
		
		//msp.xml에 C:\msp\resourceMoong\ 로 테스트시 로컬 지정
		private @Value("${common.dir.store}") String DEFAULT_STORE_DIRECTORY;	//기본 앱스토어 디렉토리
		
				
		//공통 페이지 로우수
		private @Value("${common.list.row_size}") int row_size;
		//공통 페이지 수
		private @Value("${common.list.page_size}") int page_size;
		
		private String boardType="NOTICE"; //공지사항 
		
		
		@Autowired(required=true)
		protected StoreNoticeService systemManageManager;
		
		@Autowired(required = true) 
		private MessageSource messageSource; 
       
		
		//화면로딩리스트조회
		@RequestMapping( value="admin/store/notice" ,method=RequestMethod.GET)
		public String NoticeMainGet(Model model , HttpServletRequest request){
			logger.info("####################################### NoticeMainGet ") ;
			String rtnCd = request.getParameter("rtnCd") == null ? "empty" : request.getParameter("rtnCd");
			
			NoticeSearchDto  noticeDto = new NoticeSearchDto();
			
			noticeDto.setBOARD_TYPE(boardType);
			noticeDto.setPAGE_NUM(1);
			noticeDto.setPAGE_SIZE(row_size);
			//공지사항 조회
			List<NoticeDto> noticeList = systemManageManager.selectNoticeList(noticeDto);
			model.addAttribute("noticeList", noticeList);
			model.addAttribute("R_PAGE_NUM",noticeDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",row_size);
			model.addAttribute("R_PAGE_SIZE",page_size);
			
			model.addAttribute("rtnCd", rtnCd);
			
			if("insertOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successInsert", null, LocaleContextHolder.getLocale())  ); //"등록 되었습니다."
			}else if("insertFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failInsert", null, LocaleContextHolder.getLocale()) ); //"등록에 실패했습니다."
			}else if("updateOk".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.successUpdate", null, LocaleContextHolder.getLocale()) ); //"수정 되었습니다."
			}else if("updateFail".equals(rtnCd)){
				model.addAttribute("rtnMsg",messageSource.getMessage("menu.mobile.common.failUpdate", null, LocaleContextHolder.getLocale()) ); //"수정에 실패했습니다."
			}
		
			return "admin/store/notice/noticeMain";
		}
		
		//화면리스트조회
		@RequestMapping( value="admin/store/notice" ,method=RequestMethod.POST)
		public String NoticeMainPost(Model model, HttpServletRequest request, NoticeDto noticeDto , NoticeSearchDto noticeSearchDto){
			logger.info("####################################### NoticeMainPost ") ;
			logger.info("###msg "  + request.getParameter("msg")) ;
			noticeSearchDto.setPAGE_SIZE(row_size);
			noticeSearchDto.setBOARD_TYPE(boardType);
			
			//공지사항 조회
			List<NoticeDto> noticeList = systemManageManager.selectNoticeList(noticeSearchDto);
			model.addAttribute("noticeList", noticeList);
			model.addAttribute("R_PAGE_NUM",noticeDto.getPAGE_NUM());
			model.addAttribute("R_ROW_SIZE",row_size);
			model.addAttribute("R_PAGE_SIZE",page_size);
			model.addAttribute("layout","layout/null.vm");
			
			return "admin/store/notice/noticeList";
		}
		
		//등록화면 이동
		@RequestMapping( value="/admin/store/notice/regist" , method=RequestMethod.GET )
		public String NoticeRegistGet(Model model){
			
			String code = "ST001"; //공지사항 구분 코드
			List<NoticeCommonDto> commonCodeList = systemManageManager.selectCommonCodeList(code);
			model.addAttribute("selectBoxList", commonCodeList);
		
			model.addAttribute("layout", "layout/null.vm");
			return "admin/store/notice/noticeInfoReg";
		}
		
		
		//등록
		@RequestMapping( value="/admin/store/notice/regist" , method=RequestMethod.POST)
		//public String NoticeRegistPost( @RequestParam("FILE1") MultipartFile file, Model model,  HttpServletRequest request, NoticeDto noticeDto ) throws JsonGenerationException, JsonMappingException, IOException{
		public String NoticeRegistPost( @ModelAttribute("frmNotice") NoticeFileUpload uploadFiles, Model model,  HttpServletRequest request, NoticeDto noticeDto ) throws JsonGenerationException, JsonMappingException, IOException{
			
			logger.info("######### 등록 파일업로드 START ##################33");
			
			String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
			String rtnCd = "999";
			int result = 0;
			
			//글순번 get
			String boardIdx = systemManageManager.selectNewNoNotice();
			noticeDto.setBOARD_TYPE(boardType);
			noticeDto.setBOARD_IDX(boardIdx) ;
			noticeDto.setREG_ID(S_ID_USER);
			
			//###############################################
			//앱스토어 서비스가 아직 결정된 상태가 아니므로 , 추후 수정해야함.
			if (StringUtils.isBlank(noticeDto.getSVC_ID())) {
				noticeDto.setSVC_ID("22");
			}
			//###############################################
			
			//신규 공지사항 정보 등록
			try {
				result = systemManageManager.insertNotice(noticeDto);
				
				if(result > 0){
					rtnCd = "insertOk";
				} else {
					rtnCd = "insertFail";
				}
				
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
		
			
            //여러 파일 정보를 get
			List<MultipartFile> files = uploadFiles.getFiles();         
			
			if(null != files && files.size() > 0) {            
				
				logger.info("############# files.size()===" + files.size() );
				for (MultipartFile multipartFile : files) {                 
					String fileName = multipartFile.getOriginalFilename();                
					logger.info("############# fileName===" + fileName);
					
					//파일이름이 존재하면 파일업로드와 파일정보등록
			
					try {
						
						NoticeFileDto fileDto = uploadFile(noticeDto, multipartFile);
						systemManageManager.insertStoreFile(fileDto);	
						
					} catch (IOException e) {
						logger.error("Exception caught.", e);
					} catch (Exception e) {
						logger.error("Exception caught.", e);
					}
					
				}        
			}
			
		
			
			return "redirect:/admin/store/notice?rtnCd=" + rtnCd;
		}
		
		
		// 파일 upload 후  등록할 파일정보 SETTING
		private  NoticeFileDto  uploadFile(NoticeDto noticeDto, MultipartFile file) throws IOException{
			
			String pathYear = DateFormatUtils.format(System.currentTimeMillis(), "yyyy");
		    String pathMonth = DateFormatUtils.format(System.currentTimeMillis(), "MM");
		    //String path = DEFAULT_STORE_DIRECTORY + "update" + File.separator + "upload" + File.separator + pathYear + File.separator + pathMonth + File.separator;
		    String path =DEFAULT_STORE_DIRECTORY + "board" + File.separator + pathYear + File.separator + pathMonth + File.separator;
		   
		    File destinationDir = new File(path);
		   //디렉토리생성
			FileUtils.forceMkdir(destinationDir);
			
			String filePathName ="";
			// 업로드 디렉토리 이동
			
			String fName = (System.currentTimeMillis()+"_") + file.getOriginalFilename();
			File toFile = new File(path + fName);
			file.transferTo(toFile);
			
			logger.info("\n --- filePathName  ===========" + filePathName);	
				
			//파일내역  저장
			NoticeFileDto fileDto = new NoticeFileDto();
			fileDto.setBOARD_IDX(noticeDto.getBOARD_IDX());
			fileDto.setFILE_NAME(file.getOriginalFilename());
			fileDto.setFILE_SAVE_NAME(fName);
			fileDto.setFILE_PATH(path);
			fileDto.setFILE_EXT(org.springframework.util.StringUtils.getFilenameExtension(file.getOriginalFilename()));
			fileDto.setFILE_SIZE(String.valueOf(file.getSize()));
			fileDto.setREG_ID(noticeDto.getREG_ID());
				
			return fileDto;
			
		}
		
	
		//상세 및 수정화면 조회
		@RequestMapping( value="admin/store/notice/edit" ,method=RequestMethod.GET)
		public String NoticeEditGet(Model model, NoticeDto noticeDto){
		    String code = "ST001"; //공지사항 구분 코드
			List<NoticeCommonDto> commonCodeList = systemManageManager.selectCommonCodeList(code);
			
			noticeDto.setBOARD_TYPE(boardType);
			NoticeDto noticeInfoDto = systemManageManager.selectMainNoticePopData(noticeDto);
			
			List<NoticeFileDto>  noticeFileList = systemManageManager.selectStoreFileList(noticeDto);
	
			
			
			//조회수 COUNT
			try {
				systemManageManager.updateNoticeHit(noticeDto);
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			model.addAttribute("noticeFileList", noticeFileList);
			model.addAttribute("selectBoxList", commonCodeList);
			model.addAttribute("noticeDto", noticeInfoDto);
			model.addAttribute("layout","layout/null.vm");
			return "admin/store/notice/noticeInfo";
		}
		
		
		//수정
		@RequestMapping( value="admin/store/notice/edit" , method=RequestMethod.POST)
		//public String NoticeEditPost( @RequestParam("FILE1") MultipartFile file, Model model,  HttpServletRequest request, NoticeDto noticeDto ) throws JsonGenerationException, JsonMappingException, IOException{
		public String NoticeEditPost( @ModelAttribute("frmNotice") NoticeFileUpload uploadFiles, Model model,  HttpServletRequest request, NoticeDto noticeDto ) throws JsonGenerationException, JsonMappingException, IOException{
			logger.info("######### 수정 START ##################33");
			logger.info("request  BOARD_IDX=================" + request.getParameter("BOARD_IDX"));	
			
			String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
			noticeDto.setMOD_ID(S_ID_USER);
			noticeDto.setREG_ID(S_ID_USER);
			String rtnCd = "999";
			int result = 0;
			
			try {
				
				result = systemManageManager.updateNotice(noticeDto);
				
				if(result > 0){
					rtnCd = "updateOk";
				} else {
					rtnCd = "updateFail";
				}
				
			} catch (Exception e) {
				logger.error("Exception caught.", e);
			}
			
			//###############################################
			
			//###############################################
			//파일이름이 존재하면 파일업로드와 파일정보등록
			
		    //여러 파일 정보를 get
			List<MultipartFile> files = uploadFiles.getFiles();         
				
			if(null != files && files.size() > 0) {  
				for (MultipartFile multipartFile : files) {      
					if(!multipartFile.isEmpty()){
						String fileName = multipartFile.getOriginalFilename();                
						logger.info("############# fileName===" + fileName);
					
						//파일이름이 존재하면 파일업로드와 파일정보등록
					
						try {
								
							//board_idx 게시판 키를 가지고 기존에 화일이 있으면 삭제
							//NoticeFileDelete(model, noticeDto, );
							
							//파일 upload
							NoticeFileDto fileDto = uploadFile(noticeDto, multipartFile);
							
							//파일정보등록 
							systemManageManager.insertStoreFile(fileDto);	
						} catch (IOException e) {
							logger.error("Exception caught.", e);
						} catch (Exception e) {
							logger.error("Exception caught.", e);
						}
					}	
				}
			}
			
			/*
						
			if(!file.getOriginalFilename().isEmpty()){
				
				try {
					
					//board_idx 게시판 키를 가지고 기존에 화일이 있으면 삭제
					NoticeFileDelete(model, noticeDto);
					
					//파일 upload
					NoticeFileDto fileDto = uploadFile(noticeDto, file);
					
					//파일정보등록 
					systemManageManager.insertStoreFile(fileDto);	
					
				} catch (IOException e) {
					logger.error("Exception caught.", e);
				} catch (Exception e) {
					logger.error("Exception caught.", e);
				}
				
			
			}
			*/
			
			//###############################################
			
			
			return "redirect:/admin/store/notice?rtnCd=" + rtnCd;
		}
		
		//게시판정보삭제
		@ResponseBody
		@RequestMapping( value="admin/store/notice/delete" ,method=RequestMethod.POST)
		public String NoticeEditDelete(Model model, NoticeDto noticeDto , HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
			
			int result = 0;
			noticeDto.setMOD_ID((String)request.getSession().getAttribute("S_ID_USER"));
			
			try {
				
				//파일정보  경로파일명 가져와서 파일삭제
				List<NoticeFileDto>  noticeFileList = systemManageManager.selectStoreFileList(noticeDto);
				
				//삭제할  파일 리스트
				if(noticeFileList != null && noticeFileList.size() > 0){
					
					NoticeFileDto fileDto = new NoticeFileDto(); 
					
					for(int i = 0; i < noticeFileList.size() ; i++){
						fileDto = noticeFileList.get(i);
						
					    String filePathName = fileDto.getFILE_PATH() + fileDto.getFILE_SAVE_NAME() ; //실제 저장되는 이름 
						logger.info("Delete  filePathName ================="+ filePathName);
						
						File file = new File(filePathName);
						file.delete();
					}
					
					 //게시판별   화일정보 전체삭제
					result = systemManageManager.deleteNoticeFileAll(noticeDto);
				}
				
				
				result = systemManageManager.deleteNotice(noticeDto);	  //게시판  삭제
				
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
		
		//파일 한건 삭제
		@ResponseBody
		@RequestMapping( value="admin/store/notice/fileDelete" ,method=RequestMethod.POST)
		public String NoticeFileDelete(Model model, NoticeDto noticeDto, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException{
			int result = 0;
			
			try {
				
				//파일정보  경로파일명 가져와서 파일삭제
				List<NoticeFileDto>  noticeFileList = systemManageManager.selectStoreFileList(noticeDto);
				
				//한건 삭제대상
				String fileIdx = request.getParameter("FILE_IDX");
				logger.info("NoticeFileDelete  FILE_IDX ================="+fileIdx );
				
				if(noticeFileList != null && noticeFileList.size() > 0){
			
					for( int i=0; i<noticeFileList.size(); i++ ){
						NoticeFileDto fileDto = noticeFileList.get(i);
						
						if(fileIdx.equals(fileDto.getFILE_IDX())){
							String filePathName = fileDto.getFILE_PATH() + fileDto.getFILE_SAVE_NAME() ;
							logger.info("Delete  filePathName ================="+ filePathName);
							
							File file = new File(filePathName);
							file.delete();
						}	
					}
				}
				
				logger.info("request.getParameter FILE_IDX ==========="+ request.getParameter("FILE_IDX"));
				
				NoticeFileDto fileDto = new NoticeFileDto();
				fileDto.setFILE_IDX(request.getParameter("FILE_IDX")); //개별 화일 하나씩 삭제 
				//파일정보삭제
				result = systemManageManager.deleteNoticeFile(fileDto);
				
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
