package kr.msp.admin.common;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
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
import org.springframework.web.servlet.ModelAndView;

import kr.msp.admin.base.utils.MenuAuthCheckFilter;
import kr.msp.admin.common.dto.GoMenuUrlDto;
import kr.msp.admin.common.dto.UsersDto;
import kr.msp.admin.common.service.HomeService;
import kr.msp.admin.common.xss.XssFilterManager;
import kr.msp.core.license.LicenseValidator;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private @Value("${common.password.changeday:90}") int PASSWDCHGDAY;	//패스워드 변경일
    
    @Value("${admin.mainHomeFirstPageUse:false}")
    private boolean mainHomeFirstPageUse;

    @Autowired(required=true)
	protected HomeService homeService;
 
	@Autowired
	private LicenseValidator licenseValidator;
	
	@Autowired(required = true) 
    private MessageSource messageSource; 
	
	
	@Value("${common.security_mode:true}")
	private String SECURITY_MODE;
	@Value("${common.referer_check:false}")
	private String IS_REFERER_CHECK;
	@Value("${common.uri_check:false}")
	private String IS_URI_CHECK;
	@Value("${common.xss_add_filter:}")
	private String XSS_ADDFILTER;
	//private static boolean isSecurityRoad = false;

	@PostConstruct
	public void init() {
		if (BooleanUtils.toBoolean(this.SECURITY_MODE))
		{
			int addFilterCnt = XssFilterManager.getInstance().getAddFilterCnt();
			if (!this.XSS_ADDFILTER.equals(""))
			{
				String[] xssAddFilterArr = this.XSS_ADDFILTER.split(",");
				if (addFilterCnt != xssAddFilterArr.length) {
					for (int i = 0; i < xssAddFilterArr.length; i++) {
						XssFilterManager.getInstance().addXssFilterList(xssAddFilterArr[i].trim());
					}
				}
			}
		}
	}

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model,HttpServletRequest request ) throws Exception{
		
		logger.info("ADMIN '/' START ");
		
		checkSecurity(request);
		
		
		model.addAttribute("layout", "layout/null.vm");
	
		return "home";
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String homePost( Model model , @ModelAttribute("COMMAND") String COMMAND ,UsersDto entbox_users ,
							HttpServletRequest request) throws Exception{
		String go_url ="home";
		String login_msg = "";
		
		checkSecurity(request);
		
		
		if(COMMAND.equals("LOGIN")){
			Map<String,Object> respMap = homeService.login_check_new(entbox_users, request, PASSWDCHGDAY);

            int LOGIN = (Integer)respMap.get("LOGINCODE");
			//로그인 성공
			if(LOGIN == 1){
//				login_msg = request.getSession().getAttribute("S_NM_USER") +" 로그인 하셨습니다. ";
				go_url = "admin/common/msg_handling";
				model.addAttribute("msg",login_msg);
				// 메인 홈에 대한 첫 페이지 분기.
				if(mainHomeFirstPageUse)
					model.addAttribute("url", request.getContextPath() + "/admin/mainHome/");
				else
					model.addAttribute("url", request.getContextPath() + "/admin/main");
				model.addAttribute("layout", "layout/null.vm");
            } else if(LOGIN == 0){ //로그인 성공하였으나 패스워드 변경해야 할 경우 처리
                login_msg = PASSWDCHGDAY +messageSource.getMessage("login.alert.noChangePWD", null, LocaleContextHolder.getLocale())+"!"; //일 동안 패스워드를 변경하지 않았습니다. 패스워드를 변경해 주세요
                go_url = "admin/common/msg_handling";
                model.addAttribute("msg",login_msg);
                model.addAttribute("url", request.getContextPath() + "/admin/system/user");
                model.addAttribute("layout", "layout/null.vm");
			//로그인 실패
			} else if(LOGIN == -1) {
                String passwdFainAlert = "";
                if(respMap.containsKey("LOGIN_LOCK_CNT") && respMap.containsKey("LOGIN_FAIL_CNT")){
                    passwdFainAlert = "("+respMap.get("LOGIN_FAIL_CNT")+"/"+respMap.get("LOGIN_LOCK_CNT")+")";
                }
//                login_msg = "아이디 또는 비밀번호가 다릅니다"+passwdFainAlert+".  다시확인해 주세요.";
                login_msg = messageSource.getMessage("login.alert.wrongIDPWD", null, LocaleContextHolder.getLocale()); //아이디 또는 비밀번호가 다릅니다. 다시확인해 주세요.
                go_url = "home";
                model.addAttribute("layout", "layout/null.vm");
            }else if(LOGIN == -3){
                String passwdFainAlert = "";
                if(respMap.containsKey("LOGIN_LOCK_CNT")){
                    passwdFainAlert = "("+respMap.get("LOGIN_LOCK_CNT")+")";
                }
                login_msg = messageSource.getMessage("login.alert.pwdErrorNum", null, LocaleContextHolder.getLocale())
                		+passwdFainAlert+" "+messageSource.getMessage("login.alert.lock", null, LocaleContextHolder.getLocale()); // 비밀번호 오류 횟수   제한에 의해 계정이 잠겼습니다.
                go_url = "home";
                model.addAttribute("layout", "layout/null.vm");
            }else if(LOGIN == -4){
                login_msg = messageSource.getMessage("login.alert.lockID", null, LocaleContextHolder.getLocale());  //비밀번호 오류 횟수 제한에 의해 계정이 잠겨있습니다.
                go_url = "home";
                model.addAttribute("layout", "layout/null.vm");
			} else {
				login_msg = messageSource.getMessage("login.alert.wrongIDPWD", null, LocaleContextHolder.getLocale()); //아이디 또는 비밀번호가 다릅니다. 다시확인해 주세요.
				go_url = "home";
				model.addAttribute("layout", "layout/null.vm");
			}

            if(respMap.containsKey("DOUBLELOGIN")){  // 중복 로그인일 경우
                login_msg = entbox_users.getID_USER()+messageSource.getMessage("login.alert.alreadyLogin", null, LocaleContextHolder.getLocale())+"\n"+login_msg; //는 이미 로그인한 사용자가 존재 하여 기존 사용자를 만료시켰습니다.
                model.addAttribute("msg",login_msg); 
            }
			
		} else {
			
		}

		model.addAttribute("login_msg",login_msg);
		
		logger.info("go_url is {}", go_url);
		
		return go_url;
	}
	
	public void checkSecurity(HttpServletRequest request)
		    throws Exception
		  {
		    boolean SECURITY_MODE_FLAG = BooleanUtils.toBoolean(this.SECURITY_MODE);
		    boolean IS_REFERER_CHECK_FLAG = BooleanUtils.toBoolean(this.IS_REFERER_CHECK);
		    boolean IS_URI_CHECK_FLAG = BooleanUtils.toBoolean(this.IS_URI_CHECK);
		    
		    String menuUrl = request.getRequestURI().replace(request.getContextPath(), "");
		    try
		    {
		      if (IS_URI_CHECK_FLAG)
		      {
		        String[] uri = request.getRequestURI().split("/");
		        System.out.println("uri.length:" + uri.length);
		        if ((uri.length > 0) && (uri[(uri.length - 1)].indexOf(".") > -1))
		        {
		          request.setAttribute("ERROR_MSG", this.messageSource.getMessage("service.common.incorrectPath", null, LocaleContextHolder.getLocale()) + "(URI Contains Dot(.))");
		          throw new Exception(this.messageSource.getMessage("service.common.incorrectPath", null, LocaleContextHolder.getLocale()) + "(URI Contains Dot(.))");
		        }
		      }
		      if (IS_REFERER_CHECK_FLAG)
		      {
		        String refererURI = request.getHeader("referer");
		        if ((refererURI != null) && (refererURI.indexOf(request.getContextPath()) == -1))
		        {
		          request.setAttribute("ERROR_MSG", this.messageSource.getMessage("service.common.incorrectPath", null, LocaleContextHolder.getLocale()) + "(RefererURI Is Worng)");
		          throw new Exception(this.messageSource.getMessage("service.common.incorrectPath", null, LocaleContextHolder.getLocale()) + "(RefererURI Is Worng)");
		        }
		      }
		      if (SECURITY_MODE_FLAG)
		      {
		        /*if (!isSecurityRoad)
		        {
		          int addFilterCnt = XssFilterManager.getInstance().getAddFilterCnt();
		          if (!this.XSS_ADDFILTER.equals(""))
		          {
		            String[] xssAddFilterArr = this.XSS_ADDFILTER.split(",");
		            if (addFilterCnt != xssAddFilterArr.length) {
		              for (int i = 0; i < xssAddFilterArr.length; i++) {
		                XssFilterManager.getInstance().addXssFilterList(xssAddFilterArr[i].trim());
		              }
		            }
		          }
		          isSecurityRoad = true;
		        }*/
		        List<String> xssFilterList = XssFilterManager.getInstance().getXssFilterList();
		        Enumeration enums = request.getParameterNames();
		        while (enums.hasMoreElements())
		        {
		          String paramName = (String)enums.nextElement();
		          
		          String[] parameters = request.getParameterValues(paramName);
		          String chkParams = parameters[0].trim().toLowerCase();
		          try
		          {
		            chkParams = URLDecoder.decode(chkParams, "UTF-8");
		          }
		          catch (Exception localException1) {}
		          boolean isXssTagFind = false;
		          try
		          {
		            if ((chkParams.indexOf("'") > -1) || (chkParams.indexOf("\"") > -1) || (chkParams.indexOf("<") > -1) || (chkParams.indexOf(">") > -1) || (chkParams.indexOf("(") > -1) || (chkParams.indexOf("{") > -1)) {
		              isXssTagFind = true;
		            }
		          }
		          catch (Exception e)
		          {
		            logger.error("Exception caught.", e);
		          }
		          if (isXssTagFind) {
		            for (int i = 0; i < xssFilterList.size(); i++)
		            {
		              String xssFilter = ((String)xssFilterList.get(i)).trim().toLowerCase();
		              if ((!"WEBEDIT".equals(paramName)) || (
		                (!((String)xssFilterList.get(i)).equals("img src")) && (!((String)xssFilterList.get(i)).equals("<style")))) {
		                if (chkParams.indexOf(xssFilter) > -1)
		                {
		                  String message = paramName + this.messageSource.getMessage("service.common.testFail1", null, LocaleContextHolder.getLocale()) + 
		                    "(" + xssFilter + ")" + this.messageSource.getMessage("service.common.testFail2", null, LocaleContextHolder.getLocale());
		                  Exception e = new RuntimeException(message);
		                  request.setAttribute("error", e);
		                  request.setAttribute("uri", request.getRequestURI());
		                  request.setAttribute("message", message);
		                  request.setAttribute("exception_type", "RuntimeException");
		                  request.setAttribute("ERROR_MSG", message);
		                  throw e;
		                }
		              }
		            }
		          }
		        }
		        if ((request.getParameter("SVC_ID") != null) && (!"".equals(request.getParameter("SVC_ID"))) && (!"undefined".equals(request.getParameter("SVC_ID")))) {
		          try
		          {
		            Integer.parseInt(request.getParameter("SVC_ID").trim());
		          }
		          catch (Exception e)
		          {
		            request.setAttribute("ERROR_MSG", this.messageSource.getMessage("service.common.dataPrevent", null, LocaleContextHolder.getLocale()) + "(002)");
		            throw new Exception(this.messageSource.getMessage("service.common.dataPrevent", null, LocaleContextHolder.getLocale()) + "(002)");
		          }
		        }
		        if ((request.getParameter("goMenuUrl") != null) && (!"".equals(request.getParameter("goMenuUrl"))) && 
		          (request.getParameter("goMenuUrl").indexOf("http") > 0))
		        {
		          request.setAttribute("ERROR_MSG", "XSS " + this.messageSource.getMessage("service.common.dataPrevent", null, LocaleContextHolder.getLocale()) + "(003)");
		          throw new Exception("XSS " + this.messageSource.getMessage("service.common.dataPrevent", null, LocaleContextHolder.getLocale()) + "(003)");
		        }
		      }
		    }
		    catch (Exception e)
		    {
		    	logger.error("Exception caught.", e);
		    	throw e;
		    }
		  }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(Locale locale, Model model ) {

        logger.info("ADMIN '/' START ");

        model.addAttribute("layout", "layout/null.vm");

        return "home";
    }


    @RequestMapping("/logOut")
	public String logout(Model model , HttpServletRequest request){
		
		//세션 삭제
		request.getSession().invalidate();
		model.addAttribute("layout", "layout/null.vm");
        return "home";
	}
	
	@RequestMapping(value = "/response")
	public Map<String, Object> home(Model model) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("Hello", "World");
		
		return response;
	}
	
	@RequestMapping(value = "/lose_session")
	public String LoseSession(Model model, HttpServletRequest request) {
		
		model.addAttribute("msg",messageSource.getMessage("login.alert.endSession", null, LocaleContextHolder.getLocale())); //세션이 종료되었습니다.
		model.addAttribute("url", request.getContextPath() + "/");
		model.addAttribute("layout", "layout/null.vm");
		
		return "admin/common/msg_handling";
	}
	
	
	@RequestMapping(value="/common/go_menu_url")
	public ModelAndView GoMenuUrl(HttpServletRequest request, HttpServletResponse response, GoMenuUrlDto goMenuUrl) throws IOException{
		
		request.getSession().setAttribute("S_ID_LEFT_MENU",goMenuUrl.getGoMenuId());
		request.getSession().setAttribute("S_ID_TOP_MENU",goMenuUrl.getGoMenuTopId());
		
		String goUrl = goMenuUrl.getGoMenuUrl();
        String goMenuId = goMenuUrl.getGoMenuId();
        String goMenuTopId = goMenuUrl.getGoMenuTopId();
        
        //parameter 정합성 체크
        //외부 url 사용금지 처리
        
        int paramSize = 0;
        Map paramMap = request.getParameterMap();
        if(paramMap != null) {
        	paramSize = paramMap.size();
        }
        
        if(paramSize ==0 || paramSize > 3) {
        	//parameter 는 총 3개 이다.
        	throw new IOException("Rink Injection (004)");
        }
        	
    	//로그인 체크 후 session 에 담는다.
        HashMap<String,String> authMenuMap = new HashMap();
		Object objAuthMenuMap = request.getSession().getAttribute("S_ID_AUTH_CHK_MENU_URL");
		if(objAuthMenuMap != null) {
			authMenuMap = (HashMap<String,String>)objAuthMenuMap;  
		}
        
		if(goUrl != null && !"".equals(goUrl)) {
			
			boolean isAuthMenuUrl = authMenuMap.containsKey(goUrl);
			boolean isMenuCode = MenuAuthCheckFilter.getInstance().isUri(goUrl);
			
			if(!isAuthMenuUrl && !isMenuCode) {
				
				throw new IOException("Rink Injection");
				
			}
			
		}
		
		if(goMenuId != null && !"".equals(goMenuId)) {
			
			boolean isMenuId = authMenuMap.containsValue(goMenuId);
			
			
			if(!isMenuId) {
				
				throw new IOException("Rink Injection");
				
			}
			
		}
    	
		if(goMenuTopId != null && !"".equals(goMenuTopId)) {
			
			boolean isMenuId = authMenuMap.containsValue(goMenuTopId);
			
			
			if(!isMenuId) {
				
				throw new IOException("Rink Injection");
				
			}
			
		}
		
       	if(goUrl == null || goUrl.equals("")){
			goUrl = "/admin/main";
		}
        if(goMenuId!=null && !"".equals(goMenuId) && goMenuId.length()>10){
            throw new IOException("Data Injection (007)");
        }
//        System.out.println("####################go_url:"+go_url+ "     "+go_url.toLowerCase().indexOf("http"));
        if(goUrl.toLowerCase().indexOf("http")>-1) {
            throw new IOException("Rink Injection (004)");
        }else if(goUrl.toLowerCase().indexOf("script")>-1) {
            throw new IOException("Rink Injection (005)");
        }else if(goUrl.toLowerCase().indexOf("alert")>-1){
            throw new IOException("Rink Injection (006)");
        }else{
            ModelAndView mv = new ModelAndView();
            mv.setViewName("redirect:"+goUrl);
            return mv;
        }

	}


}
