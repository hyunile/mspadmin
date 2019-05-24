package kr.msp.admin.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

import kr.msp.admin.common.dto.MenuDto;
import kr.msp.admin.common.mainHome.dto.MainHomeSummaryDailyGraph;
import kr.msp.admin.common.mainHome.dto.MainHomeSummayMobileDTO;
import kr.msp.admin.common.mainHome.dto.MainHomeSummayPushDTO;
import kr.msp.admin.common.mainHome.dto.MainHomeSummayStoreDTO;
import kr.msp.admin.common.mainHome.dto.MobilePeriodStatDTO;
import kr.msp.admin.common.mainHome.dto.MobileProtocolStatusDTO;
import kr.msp.admin.common.mainHome.dto.MobileResourceDeployListDTO;
import kr.msp.admin.common.mainHome.dto.StoreAppDownloadStatDTO;
import kr.msp.admin.common.mainHome.dto.StoreAppListStatDTO;
import kr.msp.admin.common.mainHome.dto.StoreQABoardDTO;
import kr.msp.admin.common.mainHome.service.MainHomePushService;
import kr.msp.admin.common.mainHome.service.MainHomeService;
import kr.msp.admin.securepush.manageAccount.service.ManageAccountService;
import kr.msp.admin.securepush.manageSend.dto.ManageSendParamDto;
import kr.msp.admin.securepush.manageSend.dto.MsgDto;
import kr.msp.admin.securepush.manageSend.service.ManageSendService;
import kr.msp.admin.securepush.reserveMsg.dto.ReservMsgDto;
import kr.msp.admin.securepush.reserveMsg.dto.ReserveMsgParamDto;
import kr.msp.admin.securepush.reserveMsg.service.ReserveMsgService;
import kr.msp.admin.system.notice.dto.NoticeDto;
import kr.msp.admin.system.notice.service.NoticeManageService;
import kr.msp.common.util.AdminCommonUtil;
import kr.msp.common.util.DateUtil;

/**
 * 메인 홈 controller 
 * @author joey
 * @since 2015.02.27
 */
@Controller
public class MainHomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(MainHomeController.class);
	
	/*조회 기준 기간*/
	private @Value("${admin.mainHomeThumbNailView.searchPeriod}") int searchPeriod;
	
	@Value("${secure_push.useSecurePushStyle:false}") 
	private boolean useSecurePushStyle;

	/* mainHome summaryview */
	public static final String TOP_MENU_ID_MOBILE = "0000000001";
	public static final String TOP_MENU_ID_PUSH = "0000000013";
	public static final String TOP_MENU_ID_STORE = "0000000049";
	
	/* mainHome summaryview url*/
	public static final String SUMMARY_VIEW_URL_PU001 = "/admin/mainHome/pushSendStat"; //push - 발송현황
	//public static final String SUMMARY_VIEW_URL_PU002 = "/admin/mainHome/pushReserveStat"; //push - 예약현황
	public static final String SUMMARY_VIEW_URL_PU002 = "/admin/mainHome/pushRegistStat"; //push - 가입현황
	public static final String SUMMARY_VIEW_URL_PU003 = "/admin/mainHome/pushSendTotStat"; //push - 발송통계
	
	public static final String SUMMARY_VIEW_URL_ST001 = "/admin/mainHome/storeAppListStat"; //store - 등록현황
	public static final String SUMMARY_VIEW_URL_ST002 = "/admin/mainHome/storeAppDownloadStat"; //store - 다운로드현황 
	public static final String SUMMARY_VIEW_URL_ST003 = "/admin/mainHome/storeQABoard"; //store - Q&A게시판
	
	public static final String SUMMARY_VIEW_URL_MO001 = "/admin/mainHome/mobileResourceDeployList"; //모바일 관리 - 리소스 등록현황
	public static final String SUMMARY_VIEW_URL_MO002 = "/admin/mainHome/mobilePeriodStat"; //모바일 관리 - 기간별 사용통계
	public static final String SUMMARY_VIEW_URL_MO003 = "/admin/mainHome/mobileResourceDownloadList"; // 모바일 관리 - 리소스 다운로드 현황
	public static final String SUMMARY_VIEW_URL_MO004 = "/admin/mainHome/mobileProtocolStatus"; // 모바일 관리 - 전문 사용통계
	
	public static final String SUMMARY_VIEW_URL_SP001 = "/admin/mainHome/securePushManageSend"; //시큐어 푸쉬 - 발신메시지 관리
	public static final String SUMMARY_VIEW_URL_SP002 = "/admin/mainHome/securePushReserveMsg"; //시큐어 푸쉬 - 예약발송 관리
	public static final String SUMMARY_VIEW_URL_SP003 = "/admin/mainHome/pushSendStatSpush"; //push - 발송현황(시큐어푸쉬용)
	
	// 바로가기 링크 url. 메뉴 아이디, 부모메뉴 아이디, url, 이름
	private String[][] LINK_URL_SYSTEM = null;
	private String[][] LINK_URL_MOBILE = null;
	private String[][] LINK_URL_PUSH = null;
	private String[][] LINK_URL_STORE = null;
	private String[][] LINK_URL_SECUREPUSH = null;

	
	@Autowired(required=true)
	private MainHomeService mainHomeService;
	
	@Autowired(required=true)
	private MainHomePushService mainHomePushService;
	
	@Autowired(required=true)
	private NoticeManageService noticeManageService;
	
	private HashMap<String , Integer> MAP_TOP_MENU_WEIGHT;
	private HashMap<String , List<String>> MAP_SUMMARY_VIEW_URL;
	private List<String> LIST_SUMMARY_VIEW_URL_MOBILE;
	private List<String> LIST_SUMMARY_VIEW_URL_PUSH;
	private List<String> LIST_SUMMARY_VIEW_URL_STORE;

	// CGLIB(for AOP) require a no-args constructor.
	public MainHomeController() {}

	@Autowired
	public MainHomeController(MessageSource messageSource,
							  @Value("${admin.mainHomeThumbNailView.weight.mobile}") int mobileWeight,
							  @Value("${admin.mainHomeThumbNailView.weight.push}") int pushWeight,
							  @Value("${admin.mainHomeThumbNailView.weight.store}") int storeWeight) {

        LINK_URL_SYSTEM = new String[][]{{"0000000073", "0000000066", "/admin/system/service", messageSource.getMessage("menu.system.serviceManage", null,LocaleContextHolder.getLocale())} //"서비스 관리"
            ,{"0000000071", "0000000066", "/admin/system/menu", messageSource.getMessage("menu.system.menuManage", null,LocaleContextHolder.getLocale())} //"메뉴 관리"
            ,{"0000000069", "0000000066", "/admin/system/user", messageSource.getMessage("menu.system.adminUserManage", null,LocaleContextHolder.getLocale())} //"관리자 계정 관리"
            ,{"0000000068", "0000000066", "/admin/system/menu", messageSource.getMessage("menu.system.menuManage", null,LocaleContextHolder.getLocale())}}; // 메뉴 관리

        LINK_URL_MOBILE = new String[][]{{"0000000002", "0000000001", "/admin/mobile/resourceUpload", messageSource.getMessage("menu.mobile.upload", null, LocaleContextHolder.getLocale())} //"리소스 업로드"
            ,{"0000000004", "0000000001", "/admin/mobile/rsc/deployList", messageSource.getMessage("menu.mobile.deploy", null, LocaleContextHolder.getLocale())} //"배포 관리"
            ,{"0000000009", "0000000001", "/admin/mobile/mobileService", messageSource.getMessage("menu.system.appUpdateManage", null, LocaleContextHolder.getLocale()) } //"앱 업데이트 관리"
            ,{"0000000008", "0000000001", "/admin/mobile/appVersion", messageSource.getMessage("menu.system.appBinaryManage", null, LocaleContextHolder.getLocale())}}; //앱 바이너리 버전 관리"

        LINK_URL_PUSH = new String[][]{{"0000000015", "0000000013", "/admin/push/appManage", messageSource.getMessage("menu.system.appManage", null, LocaleContextHolder.getLocale())} //"APP 관리"
            ,{"0000000023", "0000000013", "/admin/push/user", messageSource.getMessage("menu.system.userManage", null, LocaleContextHolder.getLocale())} //"사용자 관리"
            ,{"0000000017", "0000000013", "/admin/push/normalPush", messageSource.getMessage("menu.system.sendPush", null, LocaleContextHolder.getLocale())} // "PUSH 발송"
            ,{"0000000024", "0000000013", "/admin/push/group", messageSource.getMessage("menu.system.targetGroupManage", null, LocaleContextHolder.getLocale())}}; //"발송대상 그룹 관리"

        LINK_URL_STORE = new String[][]{{"0000000055", "0000000049", "/admin/store/appInfo", messageSource.getMessage("menu.system.appInfoManage", null, LocaleContextHolder.getLocale())} //"App정보 관리"
            ,{"0000000050", "0000000049", "/admin/store/user", messageSource.getMessage("menu.system.userManage", null, LocaleContextHolder.getLocale())} //"사용자 관리"
            ,{"0000000057", "0000000049", "/admin/store/device", messageSource.getMessage("menu.system.deviceManage", null, LocaleContextHolder.getLocale())} //"단말기 관리"
            ,{"0000000058", "0000000049", "/admin/store/userdevice/searchList", messageSource.getMessage("menu.system.userDeviceManage", null, LocaleContextHolder.getLocale())}}; //"사용자 단말기 관리"

        LINK_URL_SECUREPUSH = new String[][]{{"0000000096", "0000000093", "/admin/securepush/individualMsgSend", messageSource.getMessage("menu.securePush.msgSend.writeIndividualMsg", null, LocaleContextHolder.getLocale())} //개별메시지 작성
        ,{"0000000095", "0000000093", "/admin/securepush/grpMsgSend", messageSource.getMessage("menu.securePush.msgSend.writeGrpMsg", null, LocaleContextHolder.getLocale())} //그룹메시지 작성
        ,{"0000000100", "0000000093", "/admin/securepush/manageGrp", messageSource.getMessage("menu.securePush.manageGrp", null, LocaleContextHolder.getLocale())}}; //그룹관리

		// MainPage Summary Base Data
		MAP_TOP_MENU_WEIGHT = new HashMap<String , Integer>();
		MAP_TOP_MENU_WEIGHT.put(TOP_MENU_ID_MOBILE, mobileWeight);
		MAP_TOP_MENU_WEIGHT.put(TOP_MENU_ID_PUSH, pushWeight);
		MAP_TOP_MENU_WEIGHT.put(TOP_MENU_ID_STORE, storeWeight);

		LIST_SUMMARY_VIEW_URL_MOBILE = new ArrayList<String>();
		LIST_SUMMARY_VIEW_URL_MOBILE.add(SUMMARY_VIEW_URL_MO001);
		LIST_SUMMARY_VIEW_URL_MOBILE.add(SUMMARY_VIEW_URL_MO004);
		LIST_SUMMARY_VIEW_URL_MOBILE.add(SUMMARY_VIEW_URL_MO002);

		LIST_SUMMARY_VIEW_URL_PUSH = new ArrayList<String>();
		LIST_SUMMARY_VIEW_URL_PUSH.add(SUMMARY_VIEW_URL_PU001);
		LIST_SUMMARY_VIEW_URL_PUSH.add(SUMMARY_VIEW_URL_PU002);
		LIST_SUMMARY_VIEW_URL_PUSH.add(SUMMARY_VIEW_URL_PU003);

		LIST_SUMMARY_VIEW_URL_STORE = new ArrayList<String>();
		LIST_SUMMARY_VIEW_URL_STORE.add(SUMMARY_VIEW_URL_ST001);
		LIST_SUMMARY_VIEW_URL_STORE.add(SUMMARY_VIEW_URL_ST002);
		LIST_SUMMARY_VIEW_URL_STORE.add(SUMMARY_VIEW_URL_ST003);

		MAP_SUMMARY_VIEW_URL = new HashMap<String , List<String>>();
		MAP_SUMMARY_VIEW_URL.put(TOP_MENU_ID_MOBILE, LIST_SUMMARY_VIEW_URL_MOBILE);
		MAP_SUMMARY_VIEW_URL.put(TOP_MENU_ID_PUSH, LIST_SUMMARY_VIEW_URL_PUSH);
		MAP_SUMMARY_VIEW_URL.put(TOP_MENU_ID_STORE, LIST_SUMMARY_VIEW_URL_STORE);
	}

	@RequestMapping(value = "admin/mainHome", method = RequestMethod.GET)
	public String getMspAdminMainGet(HttpServletRequest request, Model model ) {
		
		model.addAttribute("lsbFlg", "N");
		
		String returnUrl = "admin/common/mainHome";
		if(useSecurePushStyle){
			// 시큐어 푸쉬 전용 메인은 메뉴 활성화 여부에 상관없이 고정된 뷰를 사용.
			returnUrl = "admin/common/mainHomeSecurePush";
			Gson gson = new Gson();
			List<String> listSummaryView = getSummaryViewListSecurePush();
			String jsonSummaryView = gson.toJson(listSummaryView);
			model.addAttribute("jsonSummaryView", jsonSummaryView);
			model.addAttribute("linkList", getSpushLinkList());
			model.addAttribute("useSecurePushStyle", useSecurePushStyle);
		}else{
			//공지사항 조회.
			List<NoticeDto> listNotice = noticeManageService.SelectMainNoticeList(request);
			//요약뷰 정보 조회.
			List<MenuDto> listMenuDto = getMenuList(request);
			List<String> listSummaryView = makeSummaryViewList(listMenuDto);
			Gson gson = new Gson();
			String jsonSummaryView = gson.toJson(listSummaryView);
			model.addAttribute("noticelist", listNotice);
			model.addAttribute("jsonSummaryView", jsonSummaryView);
			// 바로가기 조회.
			model.addAttribute("linkList", getLinkList(listMenuDto));
		}
		return returnUrl;
	}
	
	/**
	 * 시큐어 푸쉬용 summary view 목록 가져오기.
	 * @return
	 */
	private List<String> getSummaryViewListSecurePush(){
		List<String> result = new ArrayList<String>();
		result.add(SUMMARY_VIEW_URL_SP001);
		result.add(SUMMARY_VIEW_URL_SP002);
		result.add(SUMMARY_VIEW_URL_SP003);
		return result;
	}
		
	/**
	 * 시큐어 푸쉬 용 바로가기 링크 목록 가져오기.
	 * @return
	 */
	private List<MenuDto> getSpushLinkList(){
		List<MenuDto> linkMenuList = new ArrayList<MenuDto>();
		linkMenuList.add(getMenuDto(LINK_URL_SECUREPUSH[0]));
		linkMenuList.add(getMenuDto(LINK_URL_SECUREPUSH[1]));
		linkMenuList.add(getMenuDto(LINK_URL_SECUREPUSH[2]));
		
		return linkMenuList;
	}
	
	//요약뷰 리스트 생성
	private List<String> makeSummaryViewList(List<MenuDto> listMenuDto){
		List<String> result;
		int contentCnt = listMenuDto.size(); //사용자가 사용하는 컨텐츠의 개수
		if(contentCnt == 1){
			result = getSummaryViewListType1(listMenuDto.get(0));
		}else if(contentCnt == 2){
			result = getSummaryViewListType2(listMenuDto);
		}else if(contentCnt == 3){
			result = getSummaryViewListType3();
		}else{
			result = new ArrayList<String>();
		}
		return result;
	}
	//사용자가 사용하는 컨텐츠가 1개일경우
	private List<String> getSummaryViewListType1(MenuDto menuDto){
		List<String> result;
		String menuId = menuDto.getID_MENU();
		if(menuId.equals(TOP_MENU_ID_MOBILE)){
			result = LIST_SUMMARY_VIEW_URL_MOBILE;
		}else if(menuId.equals(TOP_MENU_ID_PUSH)){
			result = LIST_SUMMARY_VIEW_URL_PUSH;
		}else if(menuId.equals(TOP_MENU_ID_STORE)){
			result = LIST_SUMMARY_VIEW_URL_STORE;
		}else{
			result = new ArrayList<String>();
		}
		return result;
	}
	
	//사용자가 사용하는 컨텐츠가 2개일경우
	private List<String> getSummaryViewListType2(List<MenuDto> listMenuDto){
		List<String> result  = new ArrayList<String>();
		String menuIdA = listMenuDto.get(0).getID_MENU();
		String menuIdB = listMenuDto.get(1).getID_MENU();
		
		int menuAWeight = MAP_TOP_MENU_WEIGHT.get(menuIdA);
		int menuBWeight = MAP_TOP_MENU_WEIGHT.get(menuIdB);
		String bMenuId;
		String sMenuId;
		
		if(menuAWeight > menuBWeight){
			bMenuId = menuIdA;
			sMenuId = menuIdB;
		}else{
			bMenuId = menuIdB;
			sMenuId = menuIdA;
		}
		
		//가중치값 큰 컨텐츠 2개 + 작은 컨텐츠1개
		for(int i=0; i<2; i++){
			result.add(MAP_SUMMARY_VIEW_URL.get(bMenuId).get(i));
		}
		result.add(MAP_SUMMARY_VIEW_URL.get(sMenuId).get(0));
		return result;
	}
	
	//사용자가 사용하는 컨텐츠가 3개일경우
	private List<String> getSummaryViewListType3(){
		List<String> result = new ArrayList<String>();
		result.add(LIST_SUMMARY_VIEW_URL_MOBILE.get(0));
		result.add(LIST_SUMMARY_VIEW_URL_PUSH.get(0));
		result.add(LIST_SUMMARY_VIEW_URL_STORE.get(0));
		return result;
	}
	
	/**
	 * 활성화된 메뉴에 따라 바로가기 링크 조정.
	 * @param listMenuDto
	 * @return
	 */
	private List<MenuDto> getLinkList(List<MenuDto> listMenuDto){
		// 활성화된 메뉴에 한하여 가중치 비율에 따라 바로가기 메뉴를 할당한다.
		// 활성화된 메뉴 바로가기 3개 + 시스템 바로가기 메뉴 1개.
		List<MenuDto> linkMenuList = new ArrayList<MenuDto>();
		if(listMenuDto!=null && listMenuDto.size()>0){
			// 3개 모두 활성화되었을 경우 각각 1개씩
			if(listMenuDto.size() == 3){
				linkMenuList.add(getMenuDto(LINK_URL_MOBILE[0]));
				linkMenuList.add(getMenuDto(LINK_URL_PUSH[0]));
				linkMenuList.add(getMenuDto(LINK_URL_STORE[0]));
			}else if(listMenuDto.size() == 2){ // 2개 활성화되었을 경우 가중치가 높은 것 2개, 낮은 것 1개
				int weight1 = MAP_TOP_MENU_WEIGHT.get(listMenuDto.get(0).getID_MENU());
				int weight2 = MAP_TOP_MENU_WEIGHT.get(listMenuDto.get(1).getID_MENU());
				if(weight1 > weight2){
					linkMenuList.add(getMenuDto(listMenuDto.get(0).getID_MENU(), 0));
					linkMenuList.add(getMenuDto(listMenuDto.get(0).getID_MENU(), 1));
					linkMenuList.add(getMenuDto(listMenuDto.get(1).getID_MENU(), 0));
				}else{
					linkMenuList.add(getMenuDto(listMenuDto.get(1).getID_MENU(), 0));
					linkMenuList.add(getMenuDto(listMenuDto.get(1).getID_MENU(), 1));
					linkMenuList.add(getMenuDto(listMenuDto.get(0).getID_MENU(), 0));
				}
			}else if(listMenuDto.size() == 1){ // 1개만 활성화되었을 경우 해당 카테고리의 바로가기 3개
				linkMenuList.add(getMenuDto(listMenuDto.get(0).getID_MENU(), 0));
				linkMenuList.add(getMenuDto(listMenuDto.get(0).getID_MENU(), 1));
				linkMenuList.add(getMenuDto(listMenuDto.get(0).getID_MENU(), 2));
			}
			
		}
		// 나머지는 시스템 메뉴로 채운다.
		int sysMenuIdx = 0;
		for(int i=linkMenuList.size();i<4;i++){
			linkMenuList.add(getMenuDto(LINK_URL_SYSTEM[sysMenuIdx++]));
		}
		
		return linkMenuList;
	}
	
	/**
	 * menuDto 객체 생성.
	 * @param linkUrlArr
	 * @return
	 */
	private MenuDto getMenuDto(String[] linkUrlArr){
		MenuDto menuDto = new MenuDto();
		if(linkUrlArr!=null && linkUrlArr.length==4){
			menuDto.setID_MENU(linkUrlArr[0]);
			menuDto.setID_MENU_PARENT(linkUrlArr[1]);
			menuDto.setMENU_URL(linkUrlArr[2]);
			menuDto.setNM_MENU(linkUrlArr[3]);
		}
		return menuDto;
	}
	
	/**
	 * 메뉴아이디와 인덱스 기준으로 menuDto 객체 생성.
	 * @return
	 */
	private MenuDto getMenuDto(String menuId, int index){
		MenuDto menuDto = new MenuDto();
		if(menuId.equals(TOP_MENU_ID_MOBILE)){
			menuDto = getMenuDto(LINK_URL_MOBILE[index]);
		}else if(menuId.equals(TOP_MENU_ID_PUSH)){
			menuDto = getMenuDto(LINK_URL_PUSH[index]);
		}else if(menuId.equals(TOP_MENU_ID_STORE)){
			menuDto = getMenuDto(LINK_URL_STORE[index]);
		}
		return menuDto;
	}
		
	//사용자 대매뉴 리스트
	private List<MenuDto> getMenuList(HttpServletRequest request){
		List<MenuDto> result = new ArrayList<MenuDto>();
		List<MenuDto> listMenuDto = mainHomeService.getTopMenu(request);
		
		for(MenuDto menuDto : listMenuDto){
			//전체 대매뉴 리스트중 상용자가 사용하는 해당 대매뉴리스트만 add
			if(menuDto.getID_MENU().equals(TOP_MENU_ID_MOBILE) || menuDto.getID_MENU().equals(TOP_MENU_ID_PUSH) || menuDto.getID_MENU().equals(TOP_MENU_ID_STORE)){
				result.add(menuDto);
			}
		}
		return result;
	}

	/**
	 * push - 발송현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/pushSendStat", method = RequestMethod.POST)
	public String getPushSendStatSummary(HttpServletRequest request, Model model){
		MainHomeSummayPushDTO mainHomeSummayPushDTO = new MainHomeSummayPushDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		mainHomeSummayPushDTO.setFROM_DATE(fromDate);
		mainHomeSummayPushDTO.setTO_DATE(toDate);
		
		MainHomeSummayPushDTO result = null;
		
		try{
			result = mainHomePushService.getPushSendStatSummary(mainHomeSummayPushDTO);
		}catch(Exception e){
		}
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		
		if(result != null){
			model.addAttribute("gnr_send_cnt", result.getGNR_SEND_CNT());
			model.addAttribute("gnr_read_cnt", result.getGNR_READ_CNT());
			model.addAttribute("gnr_fail_cnt", result.getGNR_FAIL_CNT());
			model.addAttribute("gnr_recv_cnt", result.getGNR_RECV_CNT());
		}else{
			model.addAttribute("gnr_send_cnt", 0);
			model.addAttribute("gnr_read_cnt", 0);
			model.addAttribute("gnr_fail_cnt", 0);
			model.addAttribute("gnr_recv_cnt", 0);
		}
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/pushSendStat";
	}
	
	/**
	 * push - 가입현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/pushRegistStat", method = RequestMethod.POST)
	public String getPushRegistStatSummary(HttpServletRequest request, Model model){
		
		MainHomeSummayPushDTO mainHomeSummayPushDTO = new MainHomeSummayPushDTO();
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		mainHomeSummayPushDTO.setFROM_DATE(fromDate);
		mainHomeSummayPushDTO.setTO_DATE(toDate);
		
		List<MainHomeSummayPushDTO> resultList = mainHomePushService.getPushRegistStatSummary(mainHomeSummayPushDTO);
		try{
			fillAbsentDate(fromDate, DateUtil.getDateByCurrent(0), resultList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		model.addAttribute("listDto", resultList);
		model.addAttribute("strPeriodParam",strPeriodParam);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/pushRegistStat";
	}
	
	/**
     * DB에서 조회한 통계 데이터를 보정처리함.
     * @param startDt
     * @param endDt
     * @param dbList
     * @return
     * @throws Exception
     */
    protected void fillAbsentDate(String startDt, String endDt, List<MainHomeSummayPushDTO> dbList) throws Exception {
        
        // 값이 없는 날짜도 표시되도록 보정 처리.
        
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    	Calendar cal1 = Calendar.getInstance();
    	cal1.setTime(sdf.parse(startDt));
    	
    	Calendar cal2 = Calendar.getInstance();
    	cal2.setTime(sdf.parse(endDt));
    	int _idx = 0;
    	while(cal1.getTimeInMillis() <= cal2.getTimeInMillis()){
    		
    		String timeStr = sdf.format(cal1.getTime());
    		boolean findFlag = false;
    		for(int j=0;j<dbList.size();j++){
    			MainHomeSummayPushDTO _pushDto = dbList.get(j);
    			if(_pushDto.getDT() != null && timeStr.equals(_pushDto.getDT())){
    				findFlag = true;
    				break;
    			}
    		}
    		if(!findFlag){
    			MainHomeSummayPushDTO absentDbInfo = new MainHomeSummayPushDTO();
    			absentDbInfo.setDT(timeStr);
    			absentDbInfo.setCNT(0);
    			if(_idx>dbList.size())
    				dbList.add(absentDbInfo);
    			else
    				dbList.add(_idx, absentDbInfo);
    		}
    		cal1.add(Calendar.DATE, 1);
    		_idx++;
    	}
    }
	
	
	
	/**
	 * push - 예약현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/pushReserveStat", method = RequestMethod.POST)
	public String getPushReserveStatSummary(HttpServletRequest request, Model model){
		MainHomeSummayPushDTO mainHomeSummayPushDTO = new MainHomeSummayPushDTO();
		MainHomeSummayPushDTO result = mainHomePushService.getPushReserveStatSummary(mainHomeSummayPushDTO);
		
		int gnrSucCnt = result.getGNR_TOT_CNT() - result.getGNR_FAIL_CNT();
		int cmpSucCnt = result.getCMP_TOT_CNT() - result.getCMP_FAIL_CNT();
		
		model.addAttribute("gnr_suc_cnt", gnrSucCnt);
		model.addAttribute("gnr_fail_cnt", result.getGNR_FAIL_CNT());
		model.addAttribute("cmp_suc_cnt", cmpSucCnt);
		model.addAttribute("cmp_fail_cnt", result.getCMP_FAIL_CNT());
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/pushReserveStat";
	}
	
	/**
	 * push - 발송통계
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/pushSendTotStat", method = RequestMethod.POST)
	public String getPushSendTotStatSummary(HttpServletRequest request, Model model){
		MainHomeSummayPushDTO mainHomeSummayPushDTO = new MainHomeSummayPushDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		
		mainHomeSummayPushDTO.setFROM_DATE(fromDate);
		mainHomeSummayPushDTO.setTO_DATE(toDate);
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		MainHomeSummayPushDTO result = mainHomePushService.getPushSendTotStatSummary(mainHomeSummayPushDTO);
		
		int gnrTotCnt = 0;
		int gcmSentCnt = 0;
		int apnsSentCnt = 0;
		int upnsSentCnt = 0;
		float gcmTotPer = 0;
		float apnsTotPer = 0;
		float upnsTotPer = 0;
		if(result != null){
			gnrTotCnt = result.getGNR_TOT_CNT();
			gcmSentCnt = result.getGCM_SENT_CNT();
			apnsSentCnt = result.getAPNS_SENT_CNT();
			upnsSentCnt = result.getUPNS_SENT_CNT();
			if(gnrTotCnt != 0){
				gcmTotPer = ((float)gcmSentCnt)*100f / ((float)gnrTotCnt);
				apnsTotPer = ((float)apnsSentCnt) * 100f / ((float)gnrTotCnt);
				upnsTotPer = ((float)upnsSentCnt) * 100f / ((float)gnrTotCnt);
			}
		}else{
			gnrTotCnt = 0;
			gcmTotPer = 0;
			apnsTotPer = 0;
			upnsTotPer = 0;
		}
		
		List<HashMap<String,Object>> listData = new ArrayList<HashMap<String,Object>>();
		if(gnrTotCnt != 0 ){
            HashMap<String,Object> hashData1 = new HashMap<String, Object>();
            hashData1.put("CNT", gcmSentCnt);
            hashData1.put("TITLE", "GCM");
            listData.add(hashData1);
            HashMap<String,Object> hashData2 = new HashMap<String, Object>();
            hashData2.put("CNT", apnsSentCnt);
            hashData2.put("TITLE", "APNS");
            listData.add(hashData2);
            HashMap<String,Object> hashData = new HashMap<String, Object>();
			hashData.put("CNT", upnsSentCnt);
            hashData.put("TITLE", "UPNS");
            listData.add(hashData);
		}
		
		String jSonGraphData = AdminCommonUtil.getGraphJSonString(listData, "TITLE");
		model.addAttribute("jSonGraphData", jSonGraphData);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		model.addAttribute("gnr_tot_cnt", gnrTotCnt);
		model.addAttribute("gcm_sent_cnt", gcmSentCnt);
		model.addAttribute("apns_sent_cnt", apnsSentCnt);
		model.addAttribute("upns_sent_cnt", upnsSentCnt);
		model.addAttribute("gcm_tot_per", Math.round(gcmTotPer));
		model.addAttribute("apns_tot_per", Math.round(apnsTotPer));
		model.addAttribute("upns_tot_per", Math.round(upnsTotPer));
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/pushSendTotStat";
	}
	
	/**
	 * App(store) - 등록현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/storeAppListStat", method = RequestMethod.POST)
	public String getStoreAppListStatSummary(HttpServletRequest request, Model model){
		MainHomeSummayStoreDTO mainHomeSummayStoreDTO = new MainHomeSummayStoreDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0);
		
		mainHomeSummayStoreDTO.setFROM_DATE(fromDate);
		mainHomeSummayStoreDTO.setTO_DATE(toDate);
		
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		List<StoreAppListStatDTO> listStoreAppListStatDTO = mainHomeService.getStoreAppListStatSummary(mainHomeSummayStoreDTO);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		model.addAttribute("listDto", listStoreAppListStatDTO);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/storeAppListStat";
	}
	
	/**
	 * App(store) - 다운로드 현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/storeAppDownloadStat", method = RequestMethod.POST)
	public String getStoreAppDownloadStatSummary(HttpServletRequest request, Model model){
		MainHomeSummayStoreDTO mainHomeSummayStoreDTO = new MainHomeSummayStoreDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		
		mainHomeSummayStoreDTO.setFROM_DATE(fromDate);
		mainHomeSummayStoreDTO.setTO_DATE(toDate);
		
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		List<StoreAppDownloadStatDTO> listStoreAppDownloadStatDTO = mainHomeService.getStoreStoreAppDownloadStatSummary(mainHomeSummayStoreDTO);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		model.addAttribute("listDto", listStoreAppDownloadStatDTO);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/storeAppDownloadStat";
	}
	
	/**
	 * App(store) - Q&A 게시판
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/storeQABoard", method = RequestMethod.POST)
	public String getStoreQABoardSummary(HttpServletRequest request, Model model){
		MainHomeSummayStoreDTO mainHomeSummayStoreDTO = new MainHomeSummayStoreDTO();
		List<StoreQABoardDTO> listStoreQABoardDTO = mainHomeService.getStoreQABoardSummary(mainHomeSummayStoreDTO);
		
		model.addAttribute("listDto", listStoreQABoardDTO);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/storeQABoard";
	}
	
	/**
	 * 모바일 관리 - 리소스 등록현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/mobileResourceDeployList", method = RequestMethod.POST)
	public String getMobileResourceDeployList(HttpServletRequest request, Model model){
		MainHomeSummayMobileDTO mainHomeSummayMobileDTO = new MainHomeSummayMobileDTO();
		String sIdUser = (String) request.getSession().getAttribute("S_ID_USER");

		mainHomeSummayMobileDTO.setS_ID_USER(sIdUser);
		List<MobileResourceDeployListDTO> listMobileResourceDeployListDTO = mainHomeService.getMobileResourceDeployListSummary(mainHomeSummayMobileDTO);
		
		model.addAttribute("listDto", listMobileResourceDeployListDTO);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/mobileResourceDeployList";
	}
	
	/**
	 * 모바일 관리 - 리소스 다운로드 현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/mobileResourceDownloadList", method = RequestMethod.POST)
	public String getMobileResourceDownloadList(HttpServletRequest request, Model model){
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/mobileResourceDownloadList";
	}
	
	/**
	 * 모바일 관리 - 기간별 사용 통계
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/mobilePeriodStat", method = RequestMethod.POST)
	public String getMobilePeriodStat(HttpServletRequest request, Model model){
		MainHomeSummayMobileDTO mainHomeSummayMobileDTO = new MainHomeSummayMobileDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		String sIdUser = (String) request.getSession().getAttribute("S_ID_USER");
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		mainHomeSummayMobileDTO.setFROM_DATE(fromDate);
		mainHomeSummayMobileDTO.setTO_DATE(toDate);
		mainHomeSummayMobileDTO.setS_ID_USER(sIdUser);
		
		MobilePeriodStatDTO mobilePeriodStatDTO = mainHomeService.getMobilePeriodStatSummary(mainHomeSummayMobileDTO);
		
		List<MainHomeSummaryDailyGraph> listDailyGraph = mobilePeriodStatDTO.getDailyGraph();
    	
		Calendar fromCal = Calendar.getInstance();
		fromCal.add(Calendar.DAY_OF_MONTH, searchPeriod);
		Calendar toCal = Calendar.getInstance();
		List<MainHomeSummaryDailyGraph> listData = makeFullDataList(listDailyGraph, toCal, fromCal);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		model.addAttribute("listData", listData);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/mobilePeriodStat";
	}
	
	private TreeMap<String, Integer> getBaseDateTreeMap(Calendar bCal, Calendar sCal){
		TreeMap<String, Integer> result = new TreeMap<String, Integer>();
		DateFormat df = new SimpleDateFormat("MM.dd");
		while (sCal.compareTo(bCal) < 0){
			sCal.add(Calendar.DATE, 1);
			String key = df.format(sCal.getTime());
			result.put(key, 0);
		}
		return result;
	}
	
	private List<MainHomeSummaryDailyGraph> makeFullDataList(List<MainHomeSummaryDailyGraph> listMainHomeSummaryDailyGraph, Calendar bCal, Calendar sCal){
		List<MainHomeSummaryDailyGraph> result = new ArrayList<MainHomeSummaryDailyGraph>();
		
		TreeMap<String, Integer> baseDateTreeMap = getBaseDateTreeMap(bCal, sCal);
		
		DateFormat df = new SimpleDateFormat("MM.dd");
		for(MainHomeSummaryDailyGraph mainHomeSummaryDailyGraph : listMainHomeSummaryDailyGraph){
			String _dtStr = mainHomeSummaryDailyGraph.getDT();
			if(_dtStr != null){
				String key = _dtStr;
				if(_dtStr.contains(".") && _dtStr.length() ==  10){ // yyyy.mm.dd
					key = _dtStr.substring(5, 10);
				}else if(_dtStr.length() == 8){ // yyyymmdd
					key = _dtStr.substring(4, 6) + "." + _dtStr.substring(6, 8);
				}
				
				baseDateTreeMap.put(key, mainHomeSummaryDailyGraph.getCNT());
			}
		}
		
		for(String key : baseDateTreeMap.keySet()){
			MainHomeSummaryDailyGraph mainHomeSummaryDailyGraph = new MainHomeSummaryDailyGraph();
			mainHomeSummaryDailyGraph.setTITLE(key);
			mainHomeSummaryDailyGraph.setCNT(baseDateTreeMap.get(key));
			result.add(mainHomeSummaryDailyGraph);
		}
		return result;
	}
	
	/**
	 * 모바일 관리 - 전문통계
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/mobileProtocolStatus", method = RequestMethod.POST)
	public String getMobileProtocolStatus(HttpServletRequest request, Model model){
		MainHomeSummayMobileDTO mainHomeSummayMobileDTO = new MainHomeSummayMobileDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		String sIdUser = (String) request.getSession().getAttribute("S_ID_USER");
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		mainHomeSummayMobileDTO.setFROM_DATE(fromDate);
		mainHomeSummayMobileDTO.setTO_DATE(toDate);
		mainHomeSummayMobileDTO.setS_ID_USER(sIdUser);
		
		
		MobileProtocolStatusDTO mobileProtocolStatusDTO = mainHomeService.getMobileProtocolStatusSummary(mainHomeSummayMobileDTO);
		List<MainHomeSummaryDailyGraph> listDailyGraph =mobileProtocolStatusDTO.getDailyGraph();
    	
		Calendar fromCal = Calendar.getInstance();
		fromCal.add(Calendar.DAY_OF_MONTH, searchPeriod);
		Calendar toCal = Calendar.getInstance();
		List<MainHomeSummaryDailyGraph> listData = makeFullDataList(listDailyGraph, toCal, fromCal);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		model.addAttribute("listProtocol", listData);
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/mobileProtocolStatus";
	}
	
	@Autowired(required=true)
	private ManageAccountService manageAccountService;
	
	@Autowired(required=true)
	private ManageSendService manageSendService;
	
	@Autowired(required=true)
	private ReserveMsgService reserveMsgService;
	
	/**
	 * 시큐어 푸쉬 - 발신메시지 관리
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/securePushManageSend", method = RequestMethod.POST)
	public String getSecurePushManageSend(HttpServletRequest request, Model model) throws Exception{
		
		manageAccountService.setSessionSecurePushUserId(request);
		
		ManageSendParamDto paramDto = new ManageSendParamDto();
		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(3);
		// 현재 로그인 한 사용자의 사용자 아이디로 목록 조회 조건을 제한한다.
		paramDto.setSENDER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		List<MsgDto> itemList = manageSendService.getSentMsgList(paramDto);

		model.addAttribute("itemList", itemList);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/securePushManageSend";
	}
	
	/**
	 * 시큐어 푸쉬 - 예약메시지 관리
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "admin/mainHome/securePushReserveMsg", method = RequestMethod.POST)
	public String getSecurePushReserveMsg(HttpServletRequest request, Model model) throws Exception{
		
		manageAccountService.setSessionSecurePushUserId(request);
		
		ReserveMsgParamDto paramDto = new ReserveMsgParamDto();
		paramDto.setUSER_ID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
		paramDto.setAUTH_GRP_ID((String)request.getSession().getAttribute("S_ID_AUTH_GRP"));
		paramDto.setPAGE_NUM(1);
		paramDto.setPAGE_SIZE(3);
		
		List<ReservMsgDto> itemList = reserveMsgService.getReservMsgList(paramDto);

		model.addAttribute("itemList", itemList);
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/securePushReserveMsg";
	}
	
	/**
	 * push - 시큐어 푸쉬용 발송현황
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "admin/mainHome/pushSendStatSpush", method = RequestMethod.POST)
	public String getPushSendStatSummarySpush(HttpServletRequest request, Model model) throws Exception{
		MainHomeSummayPushDTO mainHomeSummayPushDTO = new MainHomeSummayPushDTO();
		
		String fromDate = DateUtil.getDateByCurrent(searchPeriod);
		String toDate = DateUtil.getDateByCurrent(0)+" 23:59:59";
		mainHomeSummayPushDTO.setFROM_DATE(fromDate);
		mainHomeSummayPushDTO.setTO_DATE(toDate);
		
		MainHomeSummayPushDTO result = null;
		
		try{
			// 시큐어 푸쉬 권한에 따라 수퍼 관리자는 전체 사용자의 통계 정보를 보여주고 
			// 일반, 고급 관리자는 자신이 보낸 메시지에 대한 통계 정보만 보여준다.
			manageAccountService.setSessionSecurePushUserId(request);
			String authGrpId = (String)request.getSession().getAttribute("S_ID_AUTH_GRP");
			if(authGrpId != null && !"0000000001".equals(authGrpId)){
				mainHomeSummayPushDTO.setSENDERID((String)request.getSession().getAttribute("S_SPUSH_ID_USER"));
			}
			result = mainHomePushService.getPushSendStatSummary(mainHomeSummayPushDTO);
		}catch(Exception e){
		}
		String strPeriodParam = DateUtil.getViewFormDate(fromDate) + " ~ " + DateUtil.getViewFormDate(toDate);
		
		model.addAttribute("strPeriodParam",strPeriodParam);
		
		if(result != null){
			model.addAttribute("gnr_send_cnt", result.getGNR_SEND_CNT());
			model.addAttribute("gnr_read_cnt", result.getGNR_READ_CNT());
			model.addAttribute("gnr_fail_cnt", result.getGNR_FAIL_CNT());
			model.addAttribute("gnr_recv_cnt", result.getGNR_RECV_CNT());
		}else{
			model.addAttribute("gnr_send_cnt", 0);
			model.addAttribute("gnr_read_cnt", 0);
			model.addAttribute("gnr_fail_cnt", 0);
			model.addAttribute("gnr_recv_cnt", 0);
		}
		
		model.addAttribute("layout", "layout/null.vm");
		return "admin/common/mainHomeSummary/pushSendStat";
	}
	
}
