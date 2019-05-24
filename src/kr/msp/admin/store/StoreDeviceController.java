/**
 * 앱스토어의 사용자 단말기 관리를 하는 콘트롤러
 *  sllim 2013.10.28 
 */
package kr.msp.admin.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.device.dto.DeviceDto;
import kr.msp.admin.store.device.dto.DeviceSearchDto;
import kr.msp.admin.store.device.dto.PlatformDto;
import kr.msp.admin.store.device.service.StoreDeviceService;

@Controller
public class StoreDeviceController extends ControllerUtil {

	private @Value("${store.empty_image_url}") String EMPTY_IMAGE_URL ;      //BINARY Path
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	protected StoreDeviceService deviceService;
	
	@Autowired(required = true) 
	private MessageSource messageSource; 

	//최초 단말기 리스트조회
	@RequestMapping( value="admin/store/device" ,method=RequestMethod.GET)
	public String DeviceList(Model model){
		logger.debug("####################################### GET "+this.getClass().getName()) ;
		DeviceSearchDto dto = new DeviceSearchDto();
		dto.setPAGE_NUM(1);
		dto.setPAGE_SIZE(row_size);
		
		//단말기 조회
		List<DeviceDto> deviceList = deviceService.selectDeviceList(dto);
		
		model.addAttribute("deviceList", deviceList);
		//model.addAttribute("R_PAGE_NUM", "1");
		//model.addAttribute("R_ROW_SIZE", row_size);
		//model.addAttribute("R_PAGE_SIZE",page_size);
		setPager(model, 1);
		
		return "admin/store/device/deviceMain";
	}

	//검색버튼 클릭 단말기 리스트조회
	@RequestMapping( value="admin/store/device" ,method=RequestMethod.POST)
	public String DeviceListPost(Model model, DeviceSearchDto dto){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		List<DeviceDto> deviceList = deviceService.selectDeviceList(dto);
	
		model.addAttribute("deviceList", deviceList);
		//model.addAttribute("R_PAGE_NUM", dto.getPAGE_NUM());
		//model.addAttribute("R_ROW_SIZE", row_size);
		//model.addAttribute("R_PAGE_SIZE",page_size);
		setPager(model, dto.getPAGE_NUM());
		setOrder(model, dto.getORDER_FIELD(), dto.getORDER_TYPE());
	    model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/device/deviceList";
	}
		
	
	//검색버튼 클릭 단말기 리스트조회
	@RequestMapping( value="admin/store/edit_useyn" ,method=RequestMethod.POST)
	public String DeviceUseYNUpdatePost(Model model, DeviceSearchDto dto){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		
		deviceService.updateUseYN(dto);
		
		dto.setPAGE_SIZE(row_size);
		List<DeviceDto> deviceList = deviceService.selectDeviceList(dto);
		
		model.addAttribute("deviceList", deviceList);
		//model.addAttribute("R_PAGE_NUM", dto.getPAGE_NUM());
		//model.addAttribute("R_ROW_SIZE", row_size);
		//model.addAttribute("R_PAGE_SIZE",page_size);
		setPager(model, dto.getPAGE_NUM());
		setOrder(model, dto.getORDER_FIELD(), dto.getORDER_TYPE());
		model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/device/deviceList";
	}
	
	//추가버튼 클릭 단말기 등록화면
	@RequestMapping( value="admin/store/deviceAdd" ,method=RequestMethod.GET)
	public String DeviceAdd(Model model){
		logger.debug("####################################### GET "+this.getClass().getName()) ;

		model.addAttribute("platList",     deviceService.selectPlatformList());//플랫폼 목록(select box)
		model.addAttribute("mfgList",      deviceService.selectCommonCode("ST003"));//코드 제조사 목록(select box)
		//앱리스트와 지원앱의 목록
		model.addAttribute("appList",      deviceService.appList(null));//앱리스트
		model.addAttribute("appDevcList",  null);//단말기의 지원앱리스트

		return "admin/store/device/deviceAdd";
	}

	//select 박스 선택시 하위 select 박스 데이터 그리기용(플랫폼버전 선택)
	@ResponseBody
	@RequestMapping( value="admin/store/json/getPlatver" , method=RequestMethod.POST)
	public String postHandler(Model model, DeviceDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;
	
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		List<PlatformDto> data = deviceService.selectPlatformVerList(dto);
		for(PlatformDto row : data){
			JSONObject readRow = new JSONObject();
			readRow.put("PLAT_VER_IDX", row.getPLAT_VER_IDX());
			readRow.put("PLAT_VER_CD",  row.getPLAT_VER_CD());
			readRow.put("PLAT_VER_NM",  row.getPLAT_VER_NM());
			
			arr.put(readRow);
		}
		
		obj.put("ver", arr);

		return obj.toString();
	}
	
	//단말기 정보 갱신화면 
	@RequestMapping( value="admin/store/selectDevice" ,method=RequestMethod.POST)
	public String selectDevice(Model model, DeviceDto dto){
		logger.debug("####################################### GET " + this.getClass().getName()) ;
		DeviceDto device = deviceService.selectDevice(dto);
		dto.setPLAT_IDX(device.getPLAT_IDX());
	
		model.addAttribute("platList"   , deviceService.selectPlatformList());//플랫폼 목록(select box)
		model.addAttribute("mfgList"    , deviceService.selectCommonCode("ST003"));//코드 제조사 목록(select box)
		model.addAttribute("device"     , device);
		model.addAttribute("image"      , deviceService.selectDeviceImg(dto));
		model.addAttribute("ver"        , deviceService.selectPlatformVerList(dto));
		model.addAttribute("appList"    , deviceService.appList(dto));     //전체앱리스트
		model.addAttribute("appDevcList", deviceService.appDevcList(dto)); //단말기의 지원앱리스트

		return "admin/store/device/deviceAdd";
	}

	//단말기 신규정보 저장하기
	@ResponseBody
	@RequestMapping( value="admin/store/saveDevice" , method=RequestMethod.POST)
	public String saveDevice( @RequestParam("FILE1") MultipartFile file, Model model, DeviceDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.saveDevice(dto, file));

		return obj.toString();
	}

	//단말기 신규정보 갱신하기
	@ResponseBody
	@RequestMapping( value="admin/store/updateDevice" , method=RequestMethod.POST)
	public String updateDevice(@RequestParam("FILE1") MultipartFile file, Model model, DeviceDto dto) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.updateDevice(dto, file));

		return obj.toString();
	}

	//단말기 신규정보 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/deleteDevice" , method=RequestMethod.POST)
	public String deleteDevice(Model model, DeviceDto dto) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.deleteDevice(dto));

		return obj.toString();
	}
	
	/**
	 * 단말 멀티선택 후 삭제
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 * @since 2014. 1. 13. by UncleJoe
	 */
	@ResponseBody
	@RequestMapping( value="admin/store/deleteDevices" , method=RequestMethod.POST)
	public String deleteMultiDevice(HttpServletRequest request, Model model) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		
		JSONObject obj = new JSONObject();
		
		String []deviceList = ServletRequestUtils.getStringParameter(request, "DEL_LIST","").split(",");
		int deleteCount = 0;
		String message = messageSource.getMessage("menu.store.controller.successDelete", null, LocaleContextHolder.getLocale()); //정상적으로 삭제되었습니다.
		if(deviceList.length> 0){
			
			deleteCount = deviceService.deleteMultiDevice(deviceList);
			
			if(deleteCount == 0 )
				message = messageSource.getMessage("menu.store.controller.noDeviceInfo", null, LocaleContextHolder.getLocale()); //"삭제된 단말정보가 없습니다."
		}else{
			message = messageSource.getMessage("menu.store.controller.noDeviceInfo", null, LocaleContextHolder.getLocale()); //"삭제된 단말정보가 없습니다."
		}
		
		obj.put("message", message);
		obj.put("count", deleteCount);
		
		return obj.toString();
	}
	
	//추가버튼 클릭 단말기 등록화면 
	@RequestMapping( value="admin/store/platMgmt" ,method=RequestMethod.GET)
	public String platMgmt(Model model, PlatformDto dto ){
		logger.debug("####################################### GET " + this.getClass().getName()) ;
		List<PlatformDto> platList = deviceService.selectPlatformList();
	
		model.addAttribute("platList", platList);//플랫폼 목록(select box)
		model.addAttribute("platCd",   deviceService.selectCommonCode("ST002"));
	    model.addAttribute("layout","layout/null.vm");
		
		return "admin/store/device/popup_platMgmt";
	}
	
	//플랫폼 목록
	@ResponseBody
	@RequestMapping( value="admin/store/platformList" , method=RequestMethod.POST)
	public String platformList(Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		
		List<PlatformDto> data = deviceService.selectPlatformList();
		JSONArray arr = new JSONArray();
		//velocity는 java 객체를 인식하지만 jquery는 인식못함으로 string객체로 변경해줘야 된다.
		for(PlatformDto row:data){
			JSONObject readRow = new JSONObject();

			readRow.put("MFG_CD",     row.getMFG_CD());
			readRow.put("MFG_NM",     row.getMFG_NM());
			readRow.put("PLAT_CD",    row.getPLAT_CD());
			readRow.put("PLAT_CD_NM", row.getPLAT_CD_NM());
			readRow.put("PLAT_DESC",  row.getPLAT_DESC());
			readRow.put("PLAT_IDX",   row.getPLAT_IDX());
			readRow.put("PLAT_NM",    row.getPLAT_NM());
			
			arr.put(readRow);
		}
		

		obj.put("platList", arr);

		return obj.toString();
	}
	//플랫폼별 버전 목록
	@ResponseBody
	@RequestMapping( value="admin/store/versionList" , method=RequestMethod.POST)
	public String versionList(Model model, DeviceDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();

		List<PlatformDto> data = deviceService.selectPlatformVerList(dto);
		JSONArray arr = new JSONArray();
		//velocity는 java 객체를 인식하지만 jquery는 인식못함으로 string객체로 변경해줘야 된다.
		for(PlatformDto row:data){
			JSONObject readRow = new JSONObject();
			readRow.put("PLAT_VER_IDX", row.getPLAT_VER_IDX());
			readRow.put("PLAT_VER_CD",  row.getPLAT_VER_CD());
			readRow.put("PLAT_VER_NM",  row.getPLAT_VER_NM());
			
			arr.put(readRow);
		}
		
		obj.put("ver", arr);

		return obj.toString();
	}

	//플랫폼 저장하기
	@ResponseBody
	@RequestMapping( value="admin/store/platformSave" , method=RequestMethod.POST)
	public String platformSave(Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.platformSave(dto));
		
		return obj.toString();
	}
	
	//플랫폼 수정하기
	@ResponseBody
	@RequestMapping( value="admin/store/platformUpdate" , method=RequestMethod.POST)
	public String platformUpdate(Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.platformUpdate(dto));

		return obj.toString();
	}
	
	//플랫폼 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/platformDelete" , method=RequestMethod.POST)
	public String platformDelete(HttpServletRequest request, Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		logger.debug("requet::" + request.getParameter("PLAT_IDX"));
		
		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.platformDelete(dto));

		return obj.toString();
	}

	//플랫폼버전 저장하기
	@ResponseBody
	@RequestMapping( value="admin/store/versionSave" , method=RequestMethod.POST)
	public String versionSave(Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.versionSave(dto));

		return obj.toString();
	}

	//플랫폼버전 수정하기
	@ResponseBody
	@RequestMapping( value="admin/store/versionUpdate" , method=RequestMethod.POST)
	public String versionUpdate(Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.versionUpdate(dto));

		return obj.toString();
	}

	//플랫폼버전 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/versionDelete" , method=RequestMethod.POST)
	public String versionDelete(Model model, PlatformDto dto ) throws Exception{
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.versionDelete(dto));

		return obj.toString();
	}

	private static final int BUFFER_SIZE = 4096;
	//파일 다운로드(스트림)
	@RequestMapping(value = "admin/store/download", method = RequestMethod.GET)
    public void fileDownload(@ModelAttribute("fileName") String fileName, HttpServletRequest request, HttpServletResponse response){
		
		
		// encoding test //
		//		String[] encTypes = {"EUC-KR", "ISO-8859-1", "UTF-8"};
		//		try {
		//			for(int i=0; i<encTypes.length; i++)
		//			{
		//				for(int j=0; j<encTypes.length; j++)
		//				{
		//					if(i != j)
		//					{
		//						System.out.println(encTypes[i]+", "+encTypes[j] + "=" +"org=>"+request.getParameter("fileName")+", change="+URLDecoder.decode(URLEncoder.encode(request.getParameter("fileName"), encTypes[i]), encTypes[j]));
		//					}
		//				}
		//			}
		//		} catch (UnsupportedEncodingException e2) {
		//			// TODO Auto-generated catch block
		//			logger.error("Exception caught.", e2);
		//		}
		
		
		// ------------------------------------------------------------------------------
		// Get 방식 한글이 깨질때 해결 방법 - 1
		// ------------------------------------------------------------------------------
		//		logger.debug(this.getClass().getName() + " == fileDownload Enter == ");
		//		try {
		//			fileName = URLDecoder.decode(URLEncoder.encode(request.getParameter("fileName"), "ISO-8859-1"), "UTF-8");
		//		} catch (UnsupportedEncodingException e1) {
		//			// TODO Auto-generated catch block
		//			logger.error("Exception caught.", e1);
		//		}


		// ------------------------------------------------------------------------------
		// Get 방식 한글이 깨질때 해결 방법 -2
		// ------------------------------------------------------------------------------
		//	 만약 fileName이 한글일때 깨진다면, Get방식에서 인코딩 설정이 안되어 있는 것이다.
		//	 tomcat의 server.xml 을 수정한다.(URIEncoding 수정)
		//	 <Connector connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"  URIEncoding="UTF-8"/>

		File downloadFile = new File(fileName);
		FileInputStream inputStream = null;
		OutputStream outStream = null;
		try {
			if(!downloadFile.exists() || !downloadFile.isFile())
			{
				// 파일이 아님...
				// 투명 이미지로 대체 함.
				fileName = request.getSession().getServletContext().getRealPath(EMPTY_IMAGE_URL);
				downloadFile = new File(fileName);
			}
			
			inputStream = new FileInputStream(downloadFile);
	
			String mimeType = "application/octet-stream";
			  
			response.setContentType(mimeType);
			response.setContentLength((int) downloadFile.length());
		 
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
			response.setHeader(headerKey, headerValue);
		 
			outStream = response.getOutputStream();
		 
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
		 
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outStream.write(buffer, 0, bytesRead);
	        }
	        
		} catch (Exception e) {
			logger.error("Exception caught.", e);
		} finally{
			if(inputStream != null ) try{inputStream.close();}catch (Exception e) {logger.error("Exception caught.", e);}
			if(outStream != null ) try{outStream.close();}catch (Exception e) {logger.error("Exception caught.", e);}
		}

	}
}