package kr.msp.admin.securepush;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import kr.msp.admin.securepush.organization.dto.DeptDto;
import kr.msp.admin.securepush.organization.dto.EmpDto;
import kr.msp.admin.securepush.organization.dto.OrgParamDto;
import kr.msp.admin.securepush.organization.service.OrganizationService;

/**
 * 시큐어 푸쉬 조직도 조회 관련 컨트롤러.
 */
@RequestMapping( value="admin/securepush")
@Controller
public class OrganizationController {

	private final static Logger logger = LoggerFactory.getLogger(OrganizationController.class);

	//공통 페이지 로우수
	private @Value("${common.list.row_size}") int row_size;
	//공통 페이지 수
	private @Value("${common.list.page_size}") int page_size;
	
	@Autowired(required = true) 
	private MessageSource messageSource;
	
	@Autowired(required=true)
	private OrganizationService orgService;
	
	/**
	 * 조직도 메뉴 진입 처리.
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value="organization" , method=RequestMethod.GET)
	public String organizationGet( Model model, OrgParamDto paramDto, HttpServletRequest request ){

		try{
			List<DeptDto> deptList = orgService.getDeptList(paramDto);
			model.addAttribute("deptList", deptList);
		}catch(Exception e){
			logger.error("Exception caught.", e);
		}
		
		return "admin/securepush/organization/organizationMain";
	}
	
	/**
	 * 조직도 - 부서 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "organization/findDept" , method = RequestMethod.POST)
	public String findDeptPost(Model model, OrgParamDto paramDto, HttpServletRequest request) throws Exception {
		
		List<DeptDto> deptList = orgService.getDeptList(paramDto);
		
		model.addAttribute( "deptList", deptList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/organization/selectOrgDeptList";
	}
	
	/**
	 * 조직도 - 사용자 검색.
	 * @param model
	 * @param paramDto
	 * @return
	 */
	@RequestMapping( value = "organization/findUser" , method = RequestMethod.POST)
	public String findUserPost(Model model, OrgParamDto paramDto, HttpServletRequest request) throws Exception {
		
		List<EmpDto> empList = orgService.getEmpList(paramDto);
		
		model.addAttribute( "empList", empList );
		model.addAttribute("layout", "layout/null.vm");
		return "admin/securepush/organization/selectOrgUserList";
	}
}
