package kr.msp.admin.commonPush;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.push.msgSend.dto.MsgSendDto;
import kr.msp.admin.push.template.dto.TemplateDto;
import kr.msp.common.util.FileUploadUtil;

/**
 * 미리보기 공통 팝업 컨트롤러.
 */
@RequestMapping(value="admin/commonPush")
@Controller
public class PreviewPopupController {
	private final static Logger logger = LoggerFactory.getLogger(PreviewPopupController.class);
    
	private @Value("${common.list.row_size}") int row_size; //공통 페이지 로우수
	private @Value("${common.list.page_size}") int page_size; //공통 페이지 페이지 수
	
	@Resource(name ="fileUploadUtil")
	private FileUploadUtil fileUploadUtil;
	
	/**
	 * 미리보기 공통 팝업 메인 페이지.
	 * @param model
	 * @return
	 */
	@RequestMapping(value="popup/preview", method=RequestMethod.GET)
	public String getPreviewPopup( Model model){
		
		TemplateDto template = new TemplateDto();
		template.setPAGE_NUM(1);
		template.setPAGE_SIZE(row_size );
		
		model.addAttribute("R_PAGE_NUM",template.getPAGE_NUM());
		model.addAttribute("R_ROW_SIZE",row_size);
		model.addAttribute("R_PAGE_SIZE",page_size);
		model.addAttribute("layout", "layout/null.vm" );
		return "admin/commonPush/popup/preview/main";
	}
	
	/**
	 * 미리보기 공통 팝업 타입별 조회.
	 * @param model
	 * @param template
	 * @return
	 */
	@RequestMapping(value="popup/preview" , headers = "content-type=multipart/*", method=RequestMethod.POST)
	public String getPreviewPopupPost( Model model , MsgSendDto preview,@RequestParam(value="IMAGE_FILE",required=false) MultipartFile IMAGE_FILE, HttpServletResponse response){
		//FileUploadUtil fileUploadUtil = FileUploadUtil.getInstance();
		String imageUploadUrl = "";
		String htmlContentsUrl = "";
		try{
			if(preview.getImageUrl() != null){
				imageUploadUrl = preview.getImageUrl();
			}
			if(IMAGE_FILE !=null && IMAGE_FILE.getSize() != 0){
				
				imageUploadUrl = fileUploadUtil.msgImageUpload(IMAGE_FILE);
			}
			//방안1 내용이없으면 만들지도 않음
	        if(preview.getWEBEDIT() != null && !"".equals(preview.getWEBEDIT())){
	            htmlContentsUrl = fileUploadUtil.htmlUpload(0, preview.getWEBEDIT());
	        }
		} catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		/*String VideoUrl = preview.getVideoUrl();
		VideoUrl = VideoUrl.replace("watch?v=", "v/");
		model.addAttribute("VideoUrl", VideoUrl);*/
		model.addAttribute("MESSAGE",preview.getMESSAGE());
		model.addAttribute("EXT",preview.getEXT());
		model.addAttribute("imageUploadUrl",imageUploadUrl);
		model.addAttribute("htmlContentsUrl",htmlContentsUrl);
		model.addAttribute("layout", "layout/null.vm");
		response.addHeader("X-Frame-Options", "SAMEORIGIN");
		return "admin/commonPush/popup/preview/main";
	}
}
