/**
 * 앱스토어의 사용자 단말기 관리를 하는 콘트롤러
 *  sllim 2013.11.01 
 */
package kr.msp.admin.store;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.userdevice.dto.UserDeviceDto;
import kr.msp.admin.store.userdevice.dto.UserDeviceSearchDto;
import kr.msp.admin.store.userdevice.service.StoreUserDeviceService;


@Controller
public class StoreUserDeviceController extends ControllerUtil{

	private Logger logger = Logger.getLogger(this.getClass());

	@Autowired(required=true)
	protected StoreUserDeviceService deviceService;
	
	//최초 사용자단말기 리스트조회
	@RequestMapping( value="admin/store/userdevice/searchList" ,method=RequestMethod.GET)
	public String UserDeviceList(Model model) throws Exception {
		logger.debug("####################################### GET "+this.getClass().getName()) ;

		UserDeviceSearchDto dto = new UserDeviceSearchDto();
		dto.setPAGE_NUM(1);
		dto.setPAGE_SIZE(this.row_size);

		//단말기 조회
		List<UserDeviceDto> userdeviceList = deviceService.selectUserDeviceList(dto);

		model.addAttribute("userdeviceList", userdeviceList);
		setPager(model, 1);
		return "admin/store/userdevice/userdeviceMain";
	}

	//검색버튼 클릭 사용자단말기 리스트조회
	@RequestMapping( value="admin/store/userdevice/searchList" ,method=RequestMethod.POST)
	public String DeviceListPost(Model model, UserDeviceSearchDto dto) throws Exception {
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		List<UserDeviceDto> userdeviceList = deviceService.selectUserDeviceList(dto);
	
		model.addAttribute("userdeviceList", userdeviceList);
		
		setPager(model, dto.getPAGE_NUM());
		setOrder(model, dto.getORDER_FIELD(), dto.getORDER_TYPE());
		
	    model.addAttribute("layout", "layout/null.vm");
		
		return "admin/store/userdevice/userdeviceList";
	}

	//사용자단말기 정보 갱신화면 
	@RequestMapping( value="admin/store/userdevice/selectUserDevice" ,method=RequestMethod.POST)
	public String selectDevice(Model model, UserDeviceDto dto ){
		logger.debug("####################################### POST " + this.getClass().getName()) ;

		model.addAttribute("userdevice" , deviceService.selectUserDevice(dto));	//사용자단말기정보
		model.addAttribute("gubunList"  , deviceService.selectCommonCode("ST004")); //코드 제조사 목록(select box)
		model.addAttribute("devcList"   , deviceService.selectDevice());		//단말기정보
		model.addAttribute("layout", "layout/null.vm");
		return "admin/store/userdevice/userdeviceAdd";
	}

	//추가버튼 클릭 단말기 등록화면
	@RequestMapping( value="admin/store/userdevice/userdeviceAdd" ,method=RequestMethod.GET)
	public String DeviceAdd(Model model){
		logger.debug("####################################### GET "+this.getClass().getName()) ;

		model.addAttribute("gubunList"  , deviceService.selectCommonCode("ST004")); //코드 제조사 목록(select box)
		model.addAttribute("devcList"   , deviceService.selectDevice());		//단말기정보
		model.addAttribute("layout", "layout/null.vm");
		return "admin/store/userdevice/userdeviceAdd";
	}
	
	//사용자단말기 신규추가저장하기
	@ResponseBody
	@RequestMapping( value="admin/store/userdevice/saveUserdevice" ,method=RequestMethod.POST)
	public String saveUserdevice(Model model, UserDeviceDto dto, HttpServletRequest request) throws Exception {
		logger.debug("####################################### POST "+this.getClass().getName()) ;

		dto.setREG_ID((String) request.getSession().getAttribute("S_ID_USER"));
		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.saveUserdevice(dto));

		return obj.toString();
	}
	
//	@ResponseBody
//	@RequestMapping( value="admin/store/userdevice/empnoCh" ,method=RequestMethod.POST)
//	public String UserDeviceEmpnoCh(Model model, UserDeviceDto dto ) throws JsonGenerationException, JsonMappingException, IOException{
//		
//		int result = 0;
//		
//		try {
//			result = deviceService.selectUserCount(dto);	  //삭제
//		} catch (Exception e) {
//			logger.error("Exception caught.", e);
//		}
//		
//		HashMap<String, Object> map = new HashMap<String,Object>();
//		if("".equals(dto.getEMP_NO()))
//		{
//			map.put("result", -1 );
//			map.put("msg","사번을 입력 하세요." );
//		}
//		else if(result < 1){
//			map.put("result", 1);
//			map.put("msg","사용할 수 있는 사번입니다." );
//		} else {
//			map.put("result", -1 );
//			map.put("msg","중복하는 사번이있습니다." );
//		}
//		
//		ObjectMapper mapper = new ObjectMapper();
//		String data = mapper.writeValueAsString(map);
//		return data;
//	}
	
	//사용자단말기 정보갱신하기
	@ResponseBody
	@RequestMapping( value="admin/store/userdevice/updateUserdevice" ,method=RequestMethod.POST)
	public String updateUserdevice(Model model, UserDeviceDto dto) throws Exception {
		logger.debug("####################################### POST "+this.getClass().getName()) ;

		//dto.setREG_ID((String) request.getSession().getAttribute("S_ID_USER"));
		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.updateUserdevice(dto));

		return obj.toString();
	}

	//사용자단말기 추가시 사용자검색 페이지 이동
	@RequestMapping( value="admin/store/userdevice/userSearch" ,method=RequestMethod.GET)
	public String userSearch(Model model) throws Exception {
		logger.debug("####################################### GET "+this.getClass().getName()) ;

		model.addAttribute("layout","layout/null.vm");

		return "admin/store/userdevice/popup_userSearch";
	}

	//사용자단말기 추가시 사용자검색버튼 클릭
	@ResponseBody
	@RequestMapping( value="admin/store/userdevice/userSearch" ,method=RequestMethod.POST)
	public String userSearchList(Model model,UserDeviceSearchDto dto) throws Exception {
		logger.debug("####################################### GET "+this.getClass().getName()) ;
		dto.setPAGE_SIZE(this.row_size);
		if(dto.getPAGE_NUM()==0){
			dto.setPAGE_NUM(1);
		}
		
		JSONObject obj = new JSONObject();
		JSONArray  arr = new JSONArray();
		List<UserDeviceDto> data = deviceService.userSearchList(dto);
		String total = "0";
		for(UserDeviceDto row:data){
			total = row.getTOT_CNT();
			JSONObject readRow = new JSONObject();
			readRow.put("EMP_NO",  row.getEMP_NO());
			readRow.put("EMP_NM",  row.getEMP_NM());
			readRow.put("GRP_NM", row.getGRP_NM());
			arr.put(readRow);
		}
		obj.put("userlist",   arr);
		obj.put("J_TOT",      total);
		obj.put("R_PAGE_NUM", dto.getPAGE_NUM());
		obj.put("R_ROW_SIZE", row_size);
		obj.put("R_PAGE_SIZE",page_size);

		return obj.toString();
	}

	//사용자단말기 삭제하기
	@ResponseBody
	@RequestMapping( value="admin/store/userdevice/deleteUserdevice" ,method=RequestMethod.POST)
	public String deleteUserdevice(Model model, UserDeviceDto dto) throws Exception {
		logger.debug("####################################### POST "+this.getClass().getName()) ;

		JSONObject obj = new JSONObject();
		obj.put("count", deviceService.deleteUserdevice(dto));

		return obj.toString();
	}
}