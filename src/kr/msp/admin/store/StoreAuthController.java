/**
 * 앱스토어의 앱권한순서 관리를 하는 콘트롤러
 *  2013.11.29
 */
package kr.msp.admin.store;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.base.utils.ControllerUtil;
import kr.msp.admin.store.appauth.dto.AppAuthGroupParamDto;
import kr.msp.admin.store.appauth.dto.AppAuthPagerInfoDto;
import kr.msp.admin.store.appauth.dto.AppAuthParamDto;
import kr.msp.admin.store.appauth.dto.AppAuthUserParamDto;
import kr.msp.admin.store.appauth.service.AppAuthService;


@Controller
public class StoreAuthController extends ControllerUtil {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired(required=true)
	protected AppAuthService service;
	
	
	private AppAuthPagerInfoDto makePager(int page, int rowSize, int pageSize){
		AppAuthPagerInfoDto info = new AppAuthPagerInfoDto();
		info.setR_PAGE_NUM(page);
		info.setR_PAGE_SIZE(pageSize);
		info.setR_ROW_SIZE(rowSize);
		
		return info;
	}
	
	
	private void selectDefaultList(Model model, int appIdx)
	{
		AppAuthParamDto param = new AppAuthParamDto();
		param.setPAGE_NUM(1);
		param.setPAGE_SIZE(row_size);
		if(appIdx>0)		param.setAPP_IDX(appIdx);
		
		AppAuthGroupParamDto grpParam = new AppAuthGroupParamDto();
		grpParam.setPAGE_NUM(1);
		grpParam.setPAGE_SIZE(page_size);
		if(appIdx>0)		grpParam.setAPP_IDX(String.valueOf(appIdx));
		
		AppAuthUserParamDto usrParam = new AppAuthUserParamDto();
		usrParam.setPAGE_NUM(1);
		usrParam.setPAGE_SIZE(page_size);
		if(appIdx>0)		usrParam.setAPP_IDX(String.valueOf(appIdx));
		
		//사용권한 조회
		model.addAttribute("appList", service.selectAppList());
		if(appIdx != 0)
		{
			model.addAttribute("grpList", service.selectGroupList(grpParam));
			model.addAttribute("usrList", service.selectUserList(usrParam));
			model.addAttribute("authList", service.selectAppAuthInfoList(param));
		}
		
//		model.addAttribute("grpPager", makePager(grpParam.getPAGE_NUM(), 	grpParam.getPAGE_SIZE(), 	grpParam.getPAGE_SIZE()));
//		model.addAttribute("usrPager", makePager(usrParam.getPAGE_NUM(), 	usrParam.getPAGE_SIZE(), 	usrParam.getPAGE_SIZE()));
//		model.addAttribute("authPager", makePager(param.getPAGE_NUM(), 		param.getPAGE_SIZE(), 		param.getPAGE_SIZE()));

	}
	
	
	// 
	@RequestMapping( value="admin/store/appauth/auth" ,method=RequestMethod.GET)
	public String SelectListGet(Model model){
		logger.debug("####################################### GET "+this.getClass().getName()) ;
		
		selectDefaultList(model, 0);
		model.addAttribute("AUTH_TYPE", "00");
		return "admin/store/appauth/AuthMain";
	}
	
	//화면 갱신
	@RequestMapping( value="admin/store/appauth/auth" ,method=RequestMethod.POST)
	public String SelectListPost(Model model, HttpServletRequest request , AppAuthParamDto param){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		
		selectDefaultList(model, param.getAPP_IDX());
		model.addAttribute("APP_IDX", param.getAPP_IDX());
		model.addAttribute("AUTH_TYPE", param.getAUTH_TYPE());
		
		//model.addAttribute("layout","layout/null.vm");
		return "admin/store/appauth/AuthMain";
	}
	
	
	
	// 권한 목록 조회
	@RequestMapping( value="admin/store/appauth/authList" ,method=RequestMethod.POST)
	public String AuthListPost(Model model, HttpServletRequest request , AppAuthParamDto param){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		model.addAttribute("authList", service.selectAppAuthInfoList(param));
//		model.addAttribute("authPager", makePager(param.getPAGE_NUM(), 		param.getPAGE_SIZE(), 		param.getPAGE_SIZE()));
		model.addAttribute("APP_IDX", param.getAPP_IDX());

		model.addAttribute("layout","layout/null.vm");
		return "admin/store/appauth/AuthList";
	}
	
	
	// 사용자 목록 조회
	@RequestMapping( value="admin/store/appauth/userList" ,method=RequestMethod.POST)
	public String UserListPost(Model model, HttpServletRequest request , AppAuthUserParamDto param){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		model.addAttribute("usrList", service.selectUserList(param));
//		model.addAttribute("usrPager", makePager(param.getPAGE_NUM(), 		param.getPAGE_SIZE(), 		param.getPAGE_SIZE()));
		model.addAttribute("APP_IDX", param.getAPP_IDX());
		model.addAttribute("EMP_NM", param.getEMP_NM());
		model.addAttribute("EMP_NO", param.getEMP_NO());
		model.addAttribute("layout","layout/null.vm");
	
		return "admin/store/appauth/UserList";
	}
	
	
	
	// 그룹목록 조회
	@RequestMapping( value="admin/store/appauth/groupList" ,method=RequestMethod.POST)
	public String UserListPost(Model model, HttpServletRequest request , AppAuthGroupParamDto param){
		logger.debug("####################################### POST " + this.getClass().getName()) ;
		model.addAttribute("grpList", service.selectGroupList(param));
//		model.addAttribute("grpPager", makePager(param.getPAGE_NUM(), 		param.getPAGE_SIZE(), 		param.getPAGE_SIZE()));
		model.addAttribute("APP_IDX", param.getAPP_IDX());
		model.addAttribute("GRP_ID", param.getGRP_ID());
		model.addAttribute("GRP_NM", param.getGRP_NM());
		model.addAttribute("layout","layout/null.vm");
		return "admin/store/appauth/GroupList";
	}
	
	
	
	
	 @RequestMapping( value="admin/store/appauth/saveAuth",	method=RequestMethod.POST)
	public String SaveAuthPost(Locale locale, Model model, HttpServletRequest request, @RequestBody String body)
	 {
		 ObjectMapper om = new ObjectMapper();
		 ArrayList<Map<String, String>> param = null;
		 String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
		 
		 service.beginTran();
		 try {
			 // json data parse..
			param = om.readValue(URLDecoder.decode(body, "UTF-8"), new TypeReference<ArrayList<Map<String, String>>>(){});
		
			for(int i=0; i<param.size(); i++)
			{
				Map<String, String> row = param.get(i);
				String crud = row.get("CRUD");
				if("C".equals(crud))
				{
					// insert
					AppAuthParamDto paramDto = new AppAuthParamDto();
					paramDto.setAUTH_TYPE(row.get("AUTH_TYPE"));
					paramDto.setAPP_IDX(Integer.parseInt(row.get("APP_IDX")));
					paramDto.setEMP_NO(row.get("EMP_NO"));
					paramDto.setGRP_ID(row.get("GRP_ID"));
					paramDto.setUSERID(S_ID_USER);
					service.insertAppAuthInfo(paramDto);
				}
				else if("D".equals(crud))
				{
					// delete
					HashMap<String, String> paramDel = new HashMap<String, String>();
					paramDel.put("AUTH_ID", row.get("AUTH_ID"));
					service.deleteAppAuthInfo(Integer.parseInt(row.get("AUTH_ID")));
				}
			}
			
			
			 service.commitTran();
			
		 } catch (JsonParseException e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
			service.rollbackTran();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			logger.error("Exception caught.", e);
			service.rollbackTran();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			service.rollbackTran();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			service.rollbackTran();
		}
		 catch(Exception e)
		 {
				e.printStackTrace();
				service.rollbackTran();
		 }

	 
		// select List
		AppAuthParamDto paramAuth = new AppAuthParamDto();
		paramAuth.setPAGE_NUM(1);
		paramAuth.setPAGE_SIZE(row_size);
		paramAuth.setAPP_IDX(Integer.parseInt(param.get(0).get("APP_IDX")));
		
		AppAuthGroupParamDto grpParam = new AppAuthGroupParamDto();
		grpParam.setPAGE_NUM(1);
		grpParam.setPAGE_SIZE(page_size);
		grpParam.setAPP_IDX(param.get(0).get("APP_IDX"));
		
		AppAuthUserParamDto usrParam = new AppAuthUserParamDto();
		usrParam.setPAGE_NUM(1);
		usrParam.setPAGE_SIZE(page_size);
		usrParam.setAPP_IDX(param.get(0).get("APP_IDX"));
		
		
		
		
		model.addAttribute("AUTH_TYPE", param.get(0).get("AUTH_TYPE"));
		
		
		
		//사용권한 조회
		model.addAttribute("appList", service.selectAppList());
		model.addAttribute("grpList", service.selectGroupList(grpParam));
		model.addAttribute("usrList", service.selectUserList(usrParam));
		model.addAttribute("authList", service.selectAppAuthInfoList(paramAuth));
		
//		model.addAttribute("grpPager", makePager(grpParam.getPAGE_NUM(), 		grpParam.getPAGE_SIZE(), 		grpParam.getPAGE_SIZE()));
//		model.addAttribute("usrPager", makePager(usrParam.getPAGE_NUM(), 		usrParam.getPAGE_SIZE(), 		usrParam.getPAGE_SIZE()));
//		model.addAttribute("authPager", makePager(paramAuth.getPAGE_NUM(), 	paramAuth.getPAGE_SIZE(), 	paramAuth.getPAGE_SIZE()));
		
		model.addAttribute("layout","layout/null.vm");
		 return "admin/store/appauth/AuthLayout";
	 }
	
	 
	 
	 
	 
	// 권한 추가
//	@RequestMapping( value="admin/store/appauth/addAuth" ,method=RequestMethod.POST)
//	public String AddAuthPost(Model model,HttpServletRequest request ,  AppAuthParamDto param){
//		logger.debug("####################################### POST " + this.getClass().getName()) ;
//		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
//		param.setUSERID(S_ID_USER);
//		String[] paramArr = param.getDELEV_DATA().split("#");
//		for(int i=0; i<paramArr.length; i++)
//		{
//			if("00".equals(param.getAUTH_TYPE()))
//			{
//				//회원
//				param.setEMP_NO(paramArr[i]);
//			}
//			else
//			{
//				// 그룹
//				param.setGRP_ID(paramArr[i]);
//			}
//			service.insertAppAuthInfo(param);
//		}
//
//		
//		selectDefaultList(model, param.getAPP_IDX());
//		model.addAttribute("APP_IDX", param.getAPP_IDX());
//		model.addAttribute("AUTH_TYPE", param.getAUTH_TYPE());
//
//		
//		return "admin/store/appauth/AuthMain";
//	}
	
	
	// 권한 삭제
//	@RequestMapping( value="admin/store/appauth/delAuth" ,method=RequestMethod.POST)
//	public String DelAuthPost(Model model,HttpServletRequest request ,  AppAuthParamDto param){
//		logger.debug("####################################### POST " + this.getClass().getName()) ;
//		String S_ID_USER = (String) request.getSession().getAttribute("S_ID_USER");
//		param.setUSERID(S_ID_USER);
//		
//		HashMap<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("PARAM_BEAN", param.getDELEV_DATA().split("#"));
//		
//		service.deleteAppAuthList(paramMap);
//		
//		selectDefaultList(model, param.getAPP_IDX());
//		model.addAttribute("APP_IDX", param.getAPP_IDX());
//		model.addAttribute("AUTH_TYPE", param.getAUTH_TYPE());
//		
//		
//		return "admin/store/appauth/AuthMain";
//	}




}
